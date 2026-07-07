package launcher;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Implements the vanilla Minecraft "Server List Ping" handshake, no third-party library needed. */
public class ServerPinger {

    public static class PingResult {
        public boolean online;
        public String versionName = "";
        public int playersOnline;
        public int playersMax;
        public String motd = "";
        public String error = "";
    }

    public static PingResult ping(String host, int port, int timeoutMs) {
        PingResult result = new PingResult();
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port), timeoutMs);
            socket.setSoTimeout(timeoutMs);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            // Handshake packet: id=0x00, protocol=-1, host, port, next state=1 (status)
            ByteArrayOutputStream handshake = new ByteArrayOutputStream();
            DataOutputStream hs = new DataOutputStream(handshake);
            writeVarInt(hs, 0x00);
            writeVarInt(hs, -1);
            writeString(hs, host);
            hs.writeShort(port);
            writeVarInt(hs, 1);
            writePacket(out, handshake.toByteArray());

            // Status request packet: id=0x00, empty body
            writePacket(out, new byte[]{0x00});

            // Read response packet
            readVarInt(in); // packet length
            int packetId = readVarInt(in);
            if (packetId != 0x00) throw new IOException("Unexpected packet id: " + packetId);
            int strLen = readVarInt(in);
            byte[] strBytes = new byte[strLen];
            in.readFully(strBytes);
            String json = new String(strBytes, StandardCharsets.UTF_8);

            Map<String, Object> root = Json.parseObject(json);
            Map<String, Object> version = Json.asObject(root.get("version"));
            Map<String, Object> players = Json.asObject(root.get("players"));
            Object descriptionObj = root.get("description");

            result.online = true;
            result.versionName = Json.asString(version.get("name"), "unknown");
            result.playersOnline = Json.asInt(players.get("online"), 0);
            result.playersMax = Json.asInt(players.get("max"), 0);
            result.motd = extractMotd(descriptionObj);
        } catch (Exception e) {
            result.online = false;
            result.error = e.getMessage() != null ? e.getMessage() : e.toString();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static String extractMotd(Object desc) {
        if (desc instanceof String) return (String) desc;
        if (desc instanceof Map) {
            Map<String, Object> m = (Map<String, Object>) desc;
            Object text = m.get("text");
            return text != null ? String.valueOf(text) : "";
        }
        return "";
    }

    private static void writePacket(DataOutputStream out, byte[] body) throws IOException {
        ByteArrayOutputStream lenBuf = new ByteArrayOutputStream();
        writeVarInt(new DataOutputStream(lenBuf), body.length);
        out.write(lenBuf.toByteArray());
        out.write(body);
        out.flush();
    }

    private static void writeVarInt(DataOutputStream out, int value) throws IOException {
        while (true) {
            if ((value & ~0x7F) == 0) {
                out.writeByte(value);
                return;
            }
            out.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
    }

    private static int readVarInt(DataInputStream in) throws IOException {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            read = in.readByte();
            int value = (read & 0x7F);
            result |= (value << (7 * numRead));
            numRead++;
            if (numRead > 5) throw new IOException("VarInt too big");
        } while ((read & 0x80) != 0);
        return result;
    }

    private static void writeString(DataOutputStream out, String s) throws IOException {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        writeVarInt(out, bytes.length);
        out.write(bytes);
    }
}
