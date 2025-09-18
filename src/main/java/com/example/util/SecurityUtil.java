package com.example.util;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.regex.Pattern;
import org.mindrot.jbcrypt.BCrypt;

public class SecurityUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]{3,20}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s-]{2,50}$");
    private static final int BCRYPT_ROUNDS = 12;

    // AES Key (16 bytes for AES-128)
    private static final String AES_KEY = "1234567890123456"; // Replace for production with secure key

    // ================= PASSWORD =================
    public static String hashPassword(String password) {
        if (password == null || password.trim().isEmpty()) return null;
        return BCrypt.hashpw(password.trim(), BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) return false;
        try {
            return BCrypt.checkpw(plainPassword.trim(), hashedPassword);
        } catch (Exception e) {
            System.err.println("Password check error: " + e.getMessage());
            return false;
        }
    }

    // ================= VALIDATION =================
    public static boolean isValidEmail(String email) { return email != null && EMAIL_PATTERN.matcher(email).matches(); }
    public static boolean isValidUsername(String username) { return username != null && USERNAME_PATTERN.matcher(username).matches(); }
    public static boolean isValidPassword(String password) { return password != null && password.length() >= 6; }
    public static boolean isValidName(String name) { return name != null && NAME_PATTERN.matcher(name).matches(); }
    public static String escapeHtml(String input) {
        if (input == null) return null;
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }

    // ================= AES ENCRYPTION =================
    public static String encrypt(String data) {
        if (data == null) return null;
        try {
            Key key = new SecretKeySpec(AES_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            System.err.println("Encryption error: " + e.getMessage());
            return null;
        }
    }

    public static String decrypt(String encryptedData) {
        if (encryptedData == null) return null;
        try {
            Key key = new SecretKeySpec(AES_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("Decryption error: " + e.getMessage());
            return null;
        }
    }
}
