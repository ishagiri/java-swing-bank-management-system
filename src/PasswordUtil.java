import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {

    public static String hashPassword(String password) {
        if (password == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public static boolean verifyPassword(String enteredPassword, String storedHash) {
        if (enteredPassword == null || storedHash == null) {
            return false;
        }
        // Direct match for legacy plaintext passwords
        if (enteredPassword.equals(storedHash)) {
            return true;
        }
        // SHA-256 hash match
        String hashed = hashPassword(enteredPassword);
        return hashed.equalsIgnoreCase(storedHash);
    }
}
