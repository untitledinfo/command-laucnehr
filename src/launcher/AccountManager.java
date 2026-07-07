package launcher;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AccountManager {

    private final Config config;

    public AccountManager(Config config) {
        this.config = config;
    }

    public List<Account> list() {
        List<Account> out = new ArrayList<>();
        for (Map<String, Object> m : config.getAccounts()) {
            out.add(Account.fromMap(m));
        }
        return out;
    }

    public Account getActive() {
        String activeUuid = config.getString("activeAccountUuid", "");
        for (Account a : list()) {
            if (a.uuid.equals(activeUuid)) return a;
        }
        return null;
    }

    public void setActive(String uuid) {
        config.set("activeAccountUuid", uuid);
    }

    public boolean nameTaken(String name) {
        for (Account a : list()) {
            if (a.name.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    public Account addOffline(String username) {
        String uuid = offlineUuid(username).toString();
        Account acc = new Account("offline", username, uuid, "", "");
        config.getAccounts().add(acc.toMap());
        return acc;
    }

    public void addOrUpdateMsa(String name, String uuid, String accessToken, String refreshToken) {
        List<Map<String, Object>> accounts = config.getAccounts();
        for (Map<String, Object> m : accounts) {
            if (uuid.equals(m.get("uuid"))) {
                m.put("name", name);
                m.put("accessToken", accessToken);
                m.put("refreshToken", refreshToken);
                return;
            }
        }
        Account acc = new Account("msa", name, uuid, accessToken, refreshToken);
        accounts.add(acc.toMap());
    }

    public void remove(String uuid) {
        config.getAccounts().removeIf(m -> uuid.equals(m.get("uuid")));
        if (uuid.equals(config.getString("activeAccountUuid", ""))) {
            config.set("activeAccountUuid", "");
        }
    }

    /** Matches Mojang's own offline-mode UUID algorithm. */
    public static UUID offlineUuid(String username) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
    }
}
