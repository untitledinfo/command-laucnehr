package launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MinecraftLauncher {

    public interface OutputListener {
        void onLine(String line);
    }

    private final Config config;

    public MinecraftLauncher(Config config) {
        this.config = config;
    }

    /**
     * Launches the given (already installed) version for the given account.
     * versionId is the id used for the game directory/profile (e.g. "1.20.4"
     * or a fabric synthetic id like "1.20.4-fabric-0.15.11").
     */
    public Process launch(String versionId, Account account, String serverAddress, OutputListener output) throws Exception {
        Path mcDir = config.getMinecraftDir();
        Path versionDir = mcDir.resolve("versions").resolve(versionId);
        Path versionJsonFile = versionDir.resolve(versionId + ".json");
        if (!Files.exists(versionJsonFile)) {
            throw new IOException("Version metadata not found for " + versionId + ". Install it first.");
        }
        Map<String, Object> versionJson = Json.parseObject(Files.readString(versionJsonFile, StandardCharsets.UTF_8));

        // Resolve inheritance (Fabric profiles set inheritsFrom -> vanilla version)
        String inheritsFrom = Json.asString(versionJson.get("inheritsFrom"), null);
        Map<String, Object> parentJson = null;
        String parentId = null;
        if (inheritsFrom != null) {
            parentId = inheritsFrom;
            Path parentFile = mcDir.resolve("versions").resolve(parentId).resolve(parentId + ".json");
            parentJson = Json.parseObject(Files.readString(parentFile, StandardCharsets.UTF_8));
        }

        String mainClass = Json.asString(versionJson.get("mainClass"),
                parentJson != null ? Json.asString(parentJson.get("mainClass"), "net.minecraft.client.main.Main")
                        : "net.minecraft.client.main.Main");

        String jarVersionId = parentId != null ? parentId : versionId;
        Path clientJar = mcDir.resolve("versions").resolve(jarVersionId).resolve(jarVersionId + ".jar");

        // --- classpath ---
        List<String> classpath = new ArrayList<>();
        collectLibraryPaths(mcDir, versionJson, classpath);
        if (parentJson != null) collectLibraryPaths(mcDir, parentJson, classpath);
        classpath.add(clientJar.toString());

        // --- natives dir ---
        Path nativesDir = mcDir.resolve("versions").resolve(jarVersionId).resolve(jarVersionId + "-natives");

        // --- profile / game dir ---
        Path profileDir = mcDir.resolve("profiles").resolve(versionId);
        Files.createDirectories(profileDir);
        Files.createDirectories(profileDir.resolve("mods"));

        String assetIndexId = "legacy";
        Object assetIndexObj = (versionJson.get("assetIndex") != null) ? versionJson.get("assetIndex")
                : (parentJson != null ? parentJson.get("assetIndex") : null);
        if (assetIndexObj instanceof Map) {
            assetIndexId = Json.asString(Json.asObject(assetIndexObj).get("id"), "legacy");
        }

        String javaPath = config.getString("javaPath", "");
        if (javaPath == null || javaPath.isBlank()) {
            javaPath = findJavaExecutable();
        }

        int ram = config.getInt("ram", 4096);

        List<String> cmd = new ArrayList<>();
        cmd.add(javaPath);
        cmd.add("-Xmx" + ram + "M");
        cmd.add("-Xms" + Math.min(ram, 1024) + "M");
        cmd.add("-Djava.library.path=" + nativesDir.toAbsolutePath());
        String extra = config.getString("extraJvmArgs", "").trim();
        if (!extra.isEmpty()) {
            for (String tok : extra.split("\\s+")) cmd.add(tok);
        }
        cmd.add("-cp");
        cmd.add(String.join(File.pathSeparator, classpath));
        cmd.add(mainClass);

        // Game arguments (works across old and modern versions without needing
        // to parse Mojang's full conditional "arguments" schema).
        cmd.add("--username"); cmd.add(account.name);
        cmd.add("--version"); cmd.add(versionId);
        cmd.add("--gameDir"); cmd.add(profileDir.toAbsolutePath().toString());
        cmd.add("--assetsDir"); cmd.add(mcDir.resolve("assets").toAbsolutePath().toString());
        cmd.add("--assetIndex"); cmd.add(assetIndexId);
        cmd.add("--uuid"); cmd.add(account.uuid.replace("-", ""));
        cmd.add("--accessToken"); cmd.add(account.accessToken == null || account.accessToken.isBlank() ? "0" : account.accessToken);
        cmd.add("--userType"); cmd.add(account.type.equals("msa") ? "msa" : "legacy");
        cmd.add("--versionType"); cmd.add("release");

        if (serverAddress != null && !serverAddress.isBlank()) {
            String host = serverAddress;
            String port = "25565";
            if (serverAddress.contains(":")) {
                String[] parts = serverAddress.split(":", 2);
                host = parts[0];
                port = parts[1];
            }
            cmd.add("--server"); cmd.add(host);
            cmd.add("--port"); cmd.add(port);
        }

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(profileDir.toFile());
        pb.redirectErrorStream(true);
        Process process = pb.start();

        if (output != null) {
            Thread reader = new Thread(() -> {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        output.onLine(line);
                    }
                } catch (IOException ignored) {
                }
            }, "mc-output-reader");
            reader.setDaemon(true);
            reader.start();
        }

        return process;
    }

    @SuppressWarnings("unchecked")
    private void collectLibraryPaths(Path mcDir, Map<String, Object> versionJson, List<String> out) {
        Path librariesDir = mcDir.resolve("libraries");
        List<Object> libraries = Json.asArray(versionJson.get("libraries"));
        for (Object lo : libraries) {
            Map<String, Object> lib = Json.asObject(lo);
            if (!RuleUtil.isAllowed(lib)) continue;

            Map<String, Object> downloads = Json.asObject(lib.get("downloads"));
            Map<String, Object> artifact = Json.asObject(downloads.get("artifact"));
            String path = Json.asString(artifact.get("path"), null);
            if (path != null) {
                Path full = librariesDir.resolve(path);
                if (Files.exists(full)) out.add(full.toString());
                continue;
            }

            // Fabric-style {name, url} libraries have no "downloads" block
            String name = Json.asString(lib.get("name"), null);
            if (name != null) {
                String[] parts = name.split(":");
                if (parts.length >= 3) {
                    String group = parts[0].replace('.', '/');
                    String artifactId = parts[1];
                    String version = parts[2];
                    String rel = group + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".jar";
                    Path full = librariesDir.resolve(rel);
                    if (Files.exists(full)) out.add(full.toString());
                }
            }
        }
    }

    private String findJavaExecutable() {
        String javaHome = System.getProperty("java.home");
        String exe = System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java";
        Path candidate = Path.of(javaHome, "bin", exe);
        if (Files.exists(candidate)) return candidate.toString();
        return "java"; // hope it's on PATH
    }
}
