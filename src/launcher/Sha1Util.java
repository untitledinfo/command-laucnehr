package launcher;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Sha1Util {

    private Sha1Util() {
    }

    public static String hex(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            try (InputStream in = Files.newInputStream(file)) {
                byte[] buf = new byte[16384];
                int n;
                while ((n = in.read(buf)) != -1) {
                    digest.update(buf, 0, n);
                }
            }
            StringBuilder sb = new StringBuilder();
            for (byte b : digest.digest()) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
    }

    /** True if the file exists and its SHA-1 matches (case-insensitive); false if missing/mismatched/unreadable. */
    public static boolean matches(Path file, String expectedSha1) {
        if (expectedSha1 == null || expectedSha1.isBlank()) return Files.exists(file);
        if (!Files.exists(file)) return false;
        try {
            return hex(file).equalsIgnoreCase(expectedSha1);
        } catch (IOException e) {
            return false;
        }
    }
}
