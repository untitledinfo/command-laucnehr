package launcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Minimal Fabric loader support: fetches the Fabric profile JSON (which is
 * itself a valid "version json" that inherits from the vanilla version) and
 * downloads its extra libraries. The merged result behaves like an
 * installed version whose id is "<mcVersion>-fabric-<loaderVersion>".
 */
public class FabricSupport {

    private static final String META_BASE = "https://meta.fabricmc.net/v2/versions/loader/";

    public static List<String> listLoaderVersions(String mcVersion) throws IOException, InterruptedException {
        String body = Http.getString(META_BASE + mcVersion);
        List<Object> arr = Json.asArray(Json.parse(body));
        List<String> out = new ArrayList<>();
        for (Object o : arr) {
            Map<String, Object> entry = Json.asObject(o);
            Map<String, Object> loader = Json.asObject(entry.get("loader"));
            String version = Json.asString(loader.get("version"), null);
            if (version != null) out.add(version);
        }
        return out;
    }

    /**
     * Installs Fabric on top of an already-installed vanilla version.
     * Returns the synthetic version id to launch.
     */
    public static String install(Path mcDir, String mcVersion, String loaderVersion,
                                  MinecraftInstaller.StatusListener listener) throws Exception {
        String profileUrl = META_BASE + mcVersion + "/" + loaderVersion + "/profile/json";
        String profileText = Http.getString(profileUrl);
        Map<String, Object> profile = Json.parseObject(profileText);

        String fabricId = mcVersion + "-fabric-" + loaderVersion;
        Path versionDir = mcDir.resolve("versions").resolve(fabricId);
        Files.createDirectories(versionDir);

        // Fabric libraries use {name, url} maven coordinates rather than
        // Mojang's {downloads:{artifact:{url,path}}} shape - resolve them here.
        Path librariesDir = mcDir.resolve("libraries");
        List<Object> libraries = Json.asArray(profile.get("libraries"));
        int i = 0;
        for (Object lo : libraries) {
            i++;
            Map<String, Object> lib = Json.asObject(lo);
            String name = Json.asString(lib.get("name"), null);
            String repoUrl = Json.asString(lib.get("url"), "https://maven.fabricmc.net/");
            if (name == null) continue;
            String path = mavenCoordToPath(name);
            Path dest = librariesDir.resolve(path);
            if (!Files.exists(dest)) {
                if (listener != null) listener.onStatus("Fabric library " + i + "/" + libraries.size(), i, libraries.size());
                String url = repoUrl.endsWith("/") ? repoUrl + path : repoUrl + "/" + path;
                try {
                    Http.download(url, dest, null);
                } catch (IOException e) {
                    System.out.println("Warning: failed to download fabric lib " + name + ": " + e.getMessage());
                }
            }
        }

        // Persist a version json for this synthetic id so it shows as installed;
        // it inherits the vanilla jar/assets and points at Fabric's main class.
        profile.put("inheritsFrom", mcVersion);
        Files.writeString(versionDir.resolve(fabricId + ".json"), Json.write(profile));

        return fabricId;
    }

    private static String mavenCoordToPath(String coord) {
        return MavenUtil.coordToPath(coord);
    }
}
