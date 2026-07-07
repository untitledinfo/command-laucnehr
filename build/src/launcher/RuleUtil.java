package launcher;

import java.util.List;
import java.util.Map;

public final class RuleUtil {

    private RuleUtil() {
    }

    /** Short OS name the way Mojang's manifests express it: "windows" / "linux" / "osx". */
    public static String currentOsName() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return "windows";
        if (os.contains("mac")) return "osx";
        return "linux";
    }

    public static boolean is64Bit() {
        String arch = System.getProperty("os.arch").toLowerCase();
        return arch.contains("64");
    }

    public static String nativesClassifier() {
        String os = currentOsName();
        return "natives-" + os;
    }

    /**
     * Evaluates a Mojang-style "rules" array (used by libraries and by the
     * modern "arguments" schema). Returns true if the entry should be used
     * on the current platform. If there is no "rules" key, it is always
     * allowed.
     */
    @SuppressWarnings("unchecked")
    public static boolean isAllowed(Map<String, Object> entry) {
        Object rulesObj = entry.get("rules");
        if (!(rulesObj instanceof List)) return true;
        List<Object> rules = (List<Object>) rulesObj;
        boolean allowed = false; // if rules exist, default deny until a matching allow rule fires
        String os = currentOsName();
        for (Object ro : rules) {
            Map<String, Object> rule = Json.asObject(ro);
            String action = Json.asString(rule.get("action"), "allow");
            boolean matches = true;
            Object osRule = rule.get("os");
            if (osRule instanceof Map) {
                Map<String, Object> osMap = (Map<String, Object>) osRule;
                String name = Json.asString(osMap.get("name"), null);
                if (name != null && !name.equals(os)) {
                    matches = false;
                }
            }
            if (matches) {
                allowed = action.equals("allow");
            }
        }
        return allowed;
    }
}
