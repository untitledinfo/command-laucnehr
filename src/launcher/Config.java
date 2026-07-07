package launcher;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Loads/saves launcher_config.json in the working directory. */
public class Config {

    private static final Path FILE = Paths.get("launcher_config.json");

    private final Map<String, Object> data;

    private Config(Map<String, Object> data) {
        this.data = data;
    }

    public static Config load() {
        Map<String, Object> defaults = defaults();
        if (Files.exists(FILE)) {
            try {
                String text = Files.readString(FILE, StandardCharsets.UTF_8);
                Map<String, Object> loaded = Json.parseObject(text);
                defaults.putAll(loaded);
            } catch (Exception e) {
                System.out.println("Warning: could not load config, using defaults: " + e.getMessage());
            }
        }
        return new Config(defaults);
    }

    private static Map<String, Object> defaults() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("minecraftDirectory", defaultMinecraftDir());
        m.put("ram", 4096.0);
        m.put("modLoader", "None");
        m.put("extraJvmArgs", "");
        m.put("javaPath", "");
        m.put("accounts", new ArrayList<Object>());
        m.put("activeAccountUuid", "");
        m.put("server", "");
        return m;
    }

    private static String defaultMinecraftDir() {
        String home = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            return (appData != null ? appData : home) + "/.minecraft";
        } else if (os.contains("mac")) {
            return home + "/Library/Application Support/minecraft";
        } else {
            return home + "/.minecraft";
        }
    }

    public void save() {
        try {
            Files.writeString(FILE, Json.write(data), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Error saving config: " + e.getMessage());
        }
    }

    public String getString(String key, String fallback) {
        Object v = data.get(key);
        return v == null ? fallback : String.valueOf(v);
    }

    public int getInt(String key, int fallback) {
        Object v = data.get(key);
        if (v instanceof Number) return ((Number) v).intValue();
        return fallback;
    }

    public void set(String key, Object value) {
        data.put(key, value);
    }

    public Object get(String key) {
        return data.get(key);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getAccounts() {
        Object v = data.get("accounts");
        if (!(v instanceof List)) {
            List<Map<String, Object>> fresh = new ArrayList<>();
            data.put("accounts", fresh);
            return fresh;
        }
        return (List<Map<String, Object>>) (List<?>) v;
    }

    public Path getMinecraftDir() {
        return Paths.get(getString("minecraftDirectory", defaultMinecraftDir()));
    }
}
