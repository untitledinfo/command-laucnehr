package launcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Rather than reimplementing Forge's own bytecode-patching install pipeline,
 * this downloads the official Forge installer jar and runs it in headless
 * "--installClient" mode, which does the real work itself (including
 * downloading vanilla + Forge libraries). We simply diff the versions/
 * directory before and after to discover the id Forge assigned.
 */
public class ForgeSupport {

    private static final String PROMOTIONS_URL =
            "https://maven.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json";
    private static final String MAVEN_BASE = "https://maven.minecraftforge.net/net/minecraftforge/forge/";

    /** Returns the recommended (falls back to latest) Forge build for a Minecraft version, or null. */
    public static String recommendedForgeVersion(String mcVersion) throws IOException, InterruptedException {
        String body = Http.getString(PROMOTIONS_URL);
        Map<String, Object> root = Json.parseObject(body);
        Map<String, Object> promos = Json.asObject(root.get("promos"));
        Object recommended = promos.get(mcVersion + "-recommended");
        if (recommended != null) return Json.asString(recommended, null);
        Object latest = promos.get(mcVersion + "-latest");
        return latest != null ? Json.asString(latest, null) : null;
    }

    /**
     * Downloads and runs the Forge installer for mcVersion+forgeVersion against mcDir.
     * Returns the new version id that appeared in versions/, so the caller can select it.
     */
    public static String install(Path mcDir, String mcVersion, String forgeVersion, String javaExe,
                                  MinecraftInstaller.StatusListener listener) throws Exception {
        String combined = mcVersion + "-" + forgeVersion;
        String installerUrl = MAVEN_BASE + combined + "/forge-" + combined + "-installer.jar";

        Path tempDir = Files.createTempDirectory("forge-installer");
        Path installerJar = tempDir.resolve("forge-installer.jar");

        if (listener != null) listener.onStatus("Downloading Forge installer...", 0, 0);
        Http.download(installerUrl, installerJar, (done, total) -> {
            if (listener != null) listener.onStatus("Downloading Forge installer...", done, total);
        });

        Path versionsDir = mcDir.resolve("versions");
        Files.createDirectories(versionsDir);
        Set<String> before = listVersionDirs(versionsDir);

        if (listener != null) listener.onStatus("Running Forge installer (this can take a minute)...", 0, 0);
        ProcessBuilder pb = new ProcessBuilder(
                javaExe, "-jar", installerJar.toAbsolutePath().toString(),
                "--installClient", mcDir.toAbsolutePath().toString());
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (listener != null) listener.onStatus(line, 0, 0);
            }
        }
        int exit = process.waitFor();
        if (exit != 0) {
            throw new IOException("Forge installer exited with code " + exit);
        }

        Set<String> after = listVersionDirs(versionsDir);
        after.removeAll(before);
        if (after.isEmpty()) {
            // Installer may have run silently against an already-installed version.
            for (String id : listVersionDirs(versionsDir)) {
                if (id.startsWith(mcVersion) && id.contains("forge")) return id;
            }
            throw new IOException("Forge installer finished but no new version was found. It may already be installed.");
        }
        return after.iterator().next();
    }

    private static Set<String> listVersionDirs(Path versionsDir) throws IOException {
        Set<String> out = new HashSet<>();
        if (!Files.isDirectory(versionsDir)) return out;
        try (var stream = Files.list(versionsDir)) {
            stream.filter(Files::isDirectory).forEach(p -> out.add(p.getFileName().toString()));
        }
        return out;
    }
}
