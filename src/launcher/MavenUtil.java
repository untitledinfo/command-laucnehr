package launcher;

public final class MavenUtil {

    private MavenUtil() {
    }

    /**
     * Converts a maven coordinate ("group:artifact:version" or
     * "group:artifact:version:classifier") into the relative path Mojang/
     * Fabric/Forge use under a libraries/ directory.
     */
    public static String coordToPath(String coord) {
        String[] parts = coord.split(":");
        if (parts.length < 3) return null;
        String group = parts[0].replace('.', '/');
        String artifact = parts[1];
        String version = parts[2];
        String classifier = parts.length > 3 ? "-" + parts[3] : "";
        return group + "/" + artifact + "/" + version + "/" + artifact + "-" + version + classifier + ".jar";
    }
}
