package launcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MinecraftInstaller {

    private static final String VERSION_MANIFEST_URL =
            "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

    public interface StatusListener {
        void onStatus(String text, long done, long total);
    }

    private final Path mcDir;
    private final StatusListener listener;

    public MinecraftInstaller(Path mcDir, StatusListener listener) {
        this.mcDir = mcDir;
        this.listener = listener;
    }

    private void status(String text) {
        if (listener != null) listener.onStatus(text, 0, 0);
    }

    private void progress(String text, long done, long total) {
        if (listener != null) listener.onStatus(text, done, total);
    }

    /** Fetches the version manifest as a list of maps: id, type, url, releaseTime. */
    public static List<Map<String, Object>> fetchVersionList() throws IOException, InterruptedException {
        String body = Http.getString(VERSION_MANIFEST_URL);
        Map<String, Object> root = Json.parseObject(body);
        List<Object> versions = Json.asArray(root.get("versions"));
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object v : versions) {
            out.add(Json.asObject(v));
        }
        return out;
    }

    /** Returns the list of already-installed version IDs (based on versions/<id>/<id>.json presence). */
    public List<String> getInstalledVersions() {
        List<String> out = new ArrayList<>();
        Path versionsDir = mcDir.resolve("versions");
        if (!Files.isDirectory(versionsDir)) return out;
        try {
            Files.list(versionsDir).forEach(dir -> {
                if (Files.isDirectory(dir)) {
                    String id = dir.getFileName().toString();
                    if (Files.exists(dir.resolve(id + ".json"))) {
                        out.add(id);
                    }
                }
            });
        } catch (IOException ignored) {
        }
        return out;
    }

    /**
     * Installs a vanilla version: version json, client jar, libraries + natives, assets.
     * Returns the parsed version json map (also handed to the launcher).
     */
    public Map<String, Object> installVersion(String versionId) throws Exception {
        status("Resolving version " + versionId + "...");
        List<Map<String, Object>> versions = fetchVersionList();
        Map<String, Object> match = null;
        for (Map<String, Object> v : versions) {
            if (versionId.equals(v.get("id"))) {
                match = v;
                break;
            }
        }
        if (match == null) {
            throw new IOException("Version not found in manifest: " + versionId);
        }

        Path versionDir = mcDir.resolve("versions").resolve(versionId);
        Files.createDirectories(versionDir);

        String versionJsonUrl = Json.asString(match.get("url"), null);
        status("Downloading version metadata...");
        String versionJsonText = Http.getString(versionJsonUrl);
        Files.writeString(versionDir.resolve(versionId + ".json"), versionJsonText, StandardCharsets.UTF_8);
        Map<String, Object> versionJson = Json.parseObject(versionJsonText);

        installFromVersionJson(versionId, versionJson);
        return versionJson;
    }

    /** Shared install logic also used by Fabric (which reuses the vanilla parent's assets/libs). */
    @SuppressWarnings("unchecked")
    public void installFromVersionJson(String versionId, Map<String, Object> versionJson) throws Exception {
        Path versionDir = mcDir.resolve("versions").resolve(versionId);
        Files.createDirectories(versionDir);

        // --- client jar ---
        Map<String, Object> downloads = Json.asObject(versionJson.get("downloads"));
        Map<String, Object> clientDownload = Json.asObject(downloads.get("client"));
        String clientUrl = Json.asString(clientDownload.get("url"), null);
        Path clientJar = versionDir.resolve(versionId + ".jar");
        if (clientUrl != null && !Files.exists(clientJar)) {
            status("Downloading client jar...");
            Http.download(clientUrl, clientJar, (done, total) -> progress("Client jar", done, total));
        }

        // --- libraries + natives ---
        Path librariesDir = mcDir.resolve("libraries");
        Path nativesDir = versionDir.resolve(versionId + "-natives");
        Files.createDirectories(nativesDir);

        List<Object> libraries = Json.asArray(versionJson.get("libraries"));
        int libIndex = 0;
        for (Object lo : libraries) {
            libIndex++;
            Map<String, Object> lib = Json.asObject(lo);
            if (!RuleUtil.isAllowed(lib)) continue;

            Map<String, Object> libDownloads = Json.asObject(lib.get("downloads"));
            Map<String, Object> artifact = Json.asObject(libDownloads.get("artifact"));
            if (artifact.get("url") != null) {
                String url = Json.asString(artifact.get("url"), null);
                String path = Json.asString(artifact.get("path"), null);
                if (url != null && path != null) {
                    Path dest = librariesDir.resolve(path);
                    if (!Files.exists(dest)) {
                        status("Downloading library " + libIndex + "/" + libraries.size());
                        Http.download(url, dest, (done, total) -> progress("Libraries", done, total));
                    }
                }
            }

            // Legacy per-OS "classifiers" natives (pre 1.19 style)
            Object classifiersObj = libDownloads.get("classifiers");
            if (classifiersObj instanceof Map) {
                Map<String, Object> classifiers = (Map<String, Object>) classifiersObj;
                Map<String, Object> nativesMap = Json.asObject(lib.get("natives"));
                String classifierKey = Json.asString(nativesMap.get(RuleUtil.currentOsName()), null);
                if (classifierKey != null) {
                    classifierKey = classifierKey.replace("${arch}", RuleUtil.is64Bit() ? "64" : "32");
                    Object classifierEntryObj = classifiers.get(classifierKey);
                    if (classifierEntryObj != null) {
                        Map<String, Object> classifierEntry = Json.asObject(classifierEntryObj);
                        String url = Json.asString(classifierEntry.get("url"), null);
                        String path = Json.asString(classifierEntry.get("path"), null);
                        if (url != null && path != null) {
                            Path dest = librariesDir.resolve(path);
                            if (!Files.exists(dest)) {
                                status("Downloading natives...");
                                Http.download(url, dest, (done, total) -> progress("Natives", done, total));
                            }
                            extractNatives(dest, nativesDir);
                        }
                    }
                }
            }
        }

        // --- assets ---
        Object assetIndexObj = versionJson.get("assetIndex");
        if (assetIndexObj instanceof Map) {
            Map<String, Object> assetIndex = Json.asObject(assetIndexObj);
            String assetIndexId = Json.asString(assetIndex.get("id"), "legacy");
            String assetIndexUrl = Json.asString(assetIndex.get("url"), null);

            Path indexesDir = mcDir.resolve("assets").resolve("indexes");
            Files.createDirectories(indexesDir);
            Path indexFile = indexesDir.resolve(assetIndexId + ".json");

            String indexText;
            if (Files.exists(indexFile)) {
                indexText = Files.readString(indexFile, StandardCharsets.UTF_8);
            } else {
                status("Downloading asset index...");
                indexText = Http.getString(assetIndexUrl);
                Files.writeString(indexFile, indexText, StandardCharsets.UTF_8);
            }

            Map<String, Object> indexJson = Json.parseObject(indexText);
            Map<String, Object> objects = Json.asObject(indexJson.get("objects"));
            Path objectsDir = mcDir.resolve("assets").resolve("objects");

            int total = objects.size();
            int i = 0;
            for (Map.Entry<String, Object> entry : objects.entrySet()) {
                i++;
                Map<String, Object> obj = Json.asObject(entry.getValue());
                String hash = Json.asString(obj.get("hash"), null);
                if (hash == null) continue;
                String prefix = hash.substring(0, 2);
                Path dest = objectsDir.resolve(prefix).resolve(hash);
                if (!Files.exists(dest)) {
                    String url = "https://resources.download.minecraft.net/" + prefix + "/" + hash;
                    Http.download(url, dest, null);
                }
                if (i % 25 == 0 || i == total) {
                    progress("Assets " + i + "/" + total, i, total);
                }
            }
        }

        status("Install complete for " + versionId);
    }

    private void extractNatives(Path jarFile, Path targetDir) throws IOException {
        try (ZipInputStream zin = new ZipInputStream(Files.newInputStream(jarFile))) {
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                String name = entry.getName();
                if (entry.isDirectory() || name.startsWith("META-INF/")) continue;
                // only extract native libs
                if (name.endsWith(".dll") || name.endsWith(".so") || name.endsWith(".dylib") || name.endsWith(".jnilib")) {
                    Path outPath = targetDir.resolve(new java.io.File(name).getName());
                    try (OutputStream out = Files.newOutputStream(outPath,
                            java.nio.file.StandardOpenOption.CREATE,
                            java.nio.file.StandardOpenOption.TRUNCATE_EXISTING)) {
                        zin.transferTo(out);
                    }
                }
            }
        }
    }
}
