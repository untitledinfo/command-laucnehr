package launcher;

public class Account {
    public String type;   // "offline" or "msa"
    public String name;
    public String uuid;
    public String accessToken;
    public String refreshToken;

    public Account(String type, String name, String uuid, String accessToken, String refreshToken) {
        this.type = type;
        this.name = name;
        this.uuid = uuid;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public java.util.Map<String, Object> toMap() {
        java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("type", type);
        m.put("name", name);
        m.put("uuid", uuid);
        m.put("accessToken", accessToken);
        m.put("refreshToken", refreshToken);
        return m;
    }

    public static Account fromMap(java.util.Map<String, Object> m) {
        return new Account(
                Json.asString(m.get("type"), "offline"),
                Json.asString(m.get("name"), "Player"),
                Json.asString(m.get("uuid"), ""),
                Json.asString(m.get("accessToken"), ""),
                Json.asString(m.get("refreshToken"), "")
        );
    }
}
