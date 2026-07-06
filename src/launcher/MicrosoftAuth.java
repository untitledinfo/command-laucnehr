package launcher;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the standard Microsoft -> Xbox Live -> XSTS -> Minecraft Services
 * device-code login chain.
 *
 * IMPORTANT: Microsoft requires every application talking to Xbox Live /
 * Minecraft Services to use its OWN registered Azure AD application (App
 * registrations -> "Public client/native" platform, no client secret,
 * redirect URI not required for device code flow). Replace CLIENT_ID below
 * with the Application (client) ID from your own registration at
 * https://portal.azure.com before this will work.
 */
public class MicrosoftAuth {

    private static final String CLIENT_ID = "PUT-YOUR-AZURE-APP-CLIENT-ID-HERE";
    private static final String SCOPE = "XboxLive.signin offline_access";

    private static final String DEVICE_CODE_URL =
            "https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode";
    private static final String TOKEN_URL =
            "https://login.microsoftonline.com/consumers/oauth2/v2.0/token";
    private static final String XBL_AUTH_URL = "https://user.auth.xboxlive.com/user/authenticate";
    private static final String XSTS_AUTH_URL = "https://xsts.auth.xboxlive.com/xsts/authorize";
    private static final String MC_LOGIN_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";
    private static final String MC_PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";

    public static class DeviceCode {
        public String deviceCode;
        public String userCode;
        public String verificationUri;
        public int interval;
        public int expiresIn;
    }

    public interface DeviceCodeCallback {
        void onCodeReady(DeviceCode code);
    }

    public DeviceCode requestDeviceCode() throws IOException, InterruptedException {
        String body = "client_id=" + CLIENT_ID + "&scope=" + urlEncode(SCOPE);
        String resp = Http.postForm(DEVICE_CODE_URL, body, "application/x-www-form-urlencoded");
        Map<String, Object> json = Json.parseObject(resp);
        if (json.containsKey("error")) {
            throw new IOException("Device code request failed: " + json.get("error_description"));
        }
        DeviceCode dc = new DeviceCode();
        dc.deviceCode = Json.asString(json.get("device_code"), null);
        dc.userCode = Json.asString(json.get("user_code"), null);
        dc.verificationUri = Json.asString(json.get("verification_uri"), "https://microsoft.com/link");
        dc.interval = Json.asInt(json.get("interval"), 5);
        dc.expiresIn = Json.asInt(json.get("expires_in"), 900);
        return dc;
    }

    /** Blocks (polling) until the user finishes signing in, or throws on timeout/denial. */
    public Map<String, String> pollForToken(DeviceCode dc) throws IOException, InterruptedException {
        long deadline = System.currentTimeMillis() + dc.expiresIn * 1000L;
        int intervalMs = Math.max(dc.interval, 5) * 1000;

        while (System.currentTimeMillis() < deadline) {
            Thread.sleep(intervalMs);
            String body = "grant_type=urn:ietf:params:oauth:grant-type:device_code"
                    + "&client_id=" + CLIENT_ID
                    + "&device_code=" + dc.deviceCode;
            String resp = Http.postForm(TOKEN_URL, body, "application/x-www-form-urlencoded");
            Map<String, Object> json = Json.parseObject(resp);
            if (json.containsKey("access_token")) {
                Map<String, String> out = new LinkedHashMap<>();
                out.put("access_token", Json.asString(json.get("access_token"), null));
                out.put("refresh_token", Json.asString(json.get("refresh_token"), null));
                return out;
            }
            String error = Json.asString(json.get("error"), "");
            if (error.equals("authorization_pending")) {
                continue; // keep polling
            } else if (error.equals("slow_down")) {
                intervalMs += 5000;
            } else {
                throw new IOException("Login failed: " + json.get("error_description"));
            }
        }
        throw new IOException("Login timed out. Please try again.");
    }

    public Map<String, String> refreshMsToken(String refreshToken) throws IOException, InterruptedException {
        String body = "grant_type=refresh_token"
                + "&client_id=" + CLIENT_ID
                + "&refresh_token=" + refreshToken
                + "&scope=" + urlEncode(SCOPE);
        String resp = Http.postForm(TOKEN_URL, body, "application/x-www-form-urlencoded");
        Map<String, Object> json = Json.parseObject(resp);
        if (!json.containsKey("access_token")) {
            throw new IOException("Failed to refresh Microsoft token: " + json.get("error_description"));
        }
        Map<String, String> out = new LinkedHashMap<>();
        out.put("access_token", Json.asString(json.get("access_token"), null));
        out.put("refresh_token", Json.asString(json.get("refresh_token"), null));
        return out;
    }

    /** Full chain: MS access token -> Xbox Live -> XSTS -> Minecraft -> profile. */
    public Account completeLogin(String msAccessToken, String msRefreshToken) throws IOException, InterruptedException {
        // 1) Xbox Live
        Map<String, Object> xblBody = new LinkedHashMap<>();
        Map<String, Object> xblProps = new LinkedHashMap<>();
        xblProps.put("AuthMethod", "RPS");
        xblProps.put("SiteName", "user.auth.xboxlive.com");
        xblProps.put("RpsTicket", "d=" + msAccessToken);
        xblBody.put("Properties", xblProps);
        xblBody.put("RelyingParty", "http://auth.xboxlive.com");
        xblBody.put("TokenType", "JWT");

        String xblResp = Http.postJson(XBL_AUTH_URL, Json.write(xblBody), null);
        Map<String, Object> xblJson = Json.parseObject(xblResp);
        String xblToken = Json.asString(xblJson.get("Token"), null);
        if (xblToken == null) throw new IOException("Xbox Live auth failed: " + xblResp);

        // 2) XSTS
        Map<String, Object> xstsBody = new LinkedHashMap<>();
        Map<String, Object> xstsProps = new LinkedHashMap<>();
        xstsProps.put("SandboxId", "RETAIL");
        xstsProps.put("UserTokens", List.of(xblToken));
        xstsBody.put("Properties", xstsProps);
        xstsBody.put("RelyingParty", "rp://api.minecraftservices.com/");
        xstsBody.put("TokenType", "JWT");

        String xstsResp = Http.postJson(XSTS_AUTH_URL, Json.write(xstsBody), null);
        Map<String, Object> xstsJson = Json.parseObject(xstsResp);
        if (xstsJson.containsKey("XErr")) {
            throw new IOException("XSTS auth failed (this Microsoft account may need adult verification, "
                    + "or does not own Minecraft): " + xstsJson.get("XErr"));
        }
        String xstsToken = Json.asString(xstsJson.get("Token"), null);
        Map<String, Object> displayClaims = Json.asObject(xstsJson.get("DisplayClaims"));
        List<Object> xui = Json.asArray(displayClaims.get("xui"));
        String uhs = xui.isEmpty() ? null : Json.asString(Json.asObject(xui.get(0)).get("uhs"), null);
        if (xstsToken == null || uhs == null) throw new IOException("XSTS auth response missing token/uhs");

        // 3) Minecraft Services login
        Map<String, Object> mcBody = new LinkedHashMap<>();
        mcBody.put("identityToken", "XBL3.0 x=" + uhs + ";" + xstsToken);
        String mcResp = Http.postJson(MC_LOGIN_URL, Json.write(mcBody), null);
        Map<String, Object> mcJson = Json.parseObject(mcResp);
        String mcAccessToken = Json.asString(mcJson.get("access_token"), null);
        if (mcAccessToken == null) throw new IOException("Minecraft services login failed: " + mcResp);

        // 4) Profile (name + uuid)
        String profileResp = getProfile(mcAccessToken);
        Map<String, Object> profileJson = Json.parseObject(profileResp);
        if (profileJson.containsKey("error") || !profileJson.containsKey("id")) {
            throw new IOException("Could not load Minecraft profile (does this account own the game?): " + profileResp);
        }
        String rawId = Json.asString(profileJson.get("id"), null);
        String name = Json.asString(profileJson.get("name"), null);
        String formattedUuid = formatUuid(rawId);

        return new Account("msa", name, formattedUuid, mcAccessToken, msRefreshToken);
    }

    private String getProfile(String mcAccessToken) throws IOException, InterruptedException {
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder(java.net.URI.create(MC_PROFILE_URL))
                .header("Authorization", "Bearer " + mcAccessToken)
                .GET().build();
        java.net.http.HttpResponse<String> resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
        return resp.body();
    }

    private static String formatUuid(String raw) {
        if (raw == null || raw.length() != 32) return raw;
        return raw.substring(0, 8) + "-" + raw.substring(8, 12) + "-" + raw.substring(12, 16) + "-"
                + raw.substring(16, 20) + "-" + raw.substring(20);
    }

    private static String urlEncode(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }
}
