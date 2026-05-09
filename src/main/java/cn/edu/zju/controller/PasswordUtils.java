package cn.edu.zju.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public final class PasswordUtils {

    private PasswordUtils() {
    }

    public static String hashPassword(String password) {
        byte[] hash = sha256(password);
        return Base64.getEncoder().encodeToString(hash);
    }

    public static boolean matches(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }

        String normalizedStoredHash = storedHash.trim();
        try {
            // Backward compatibility: support old "salt:hash" records.
            if (normalizedStoredHash.contains(":")) {
                String[] parts = normalizedStoredHash.split(":", 2);
                byte[] salt = Base64.getDecoder().decode(parts[0]);
                byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
                byte[] actualHash = sha256(salt, rawPassword);
                return MessageDigest.isEqual(expectedHash, actualHash);
            }

            // Compatibility: some seed/test data was stored as hex(SHA-256).
            if (isHexSha256(normalizedStoredHash)) {
                byte[] expectedHash = hexToBytes(normalizedStoredHash);
                byte[] actualHash = sha256(rawPassword);
                return MessageDigest.isEqual(expectedHash, actualHash);
            }

            byte[] expectedHash = Base64.getDecoder().decode(normalizedStoredHash);
            byte[] actualHash = sha256(rawPassword);
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (IllegalArgumentException e) {
            // Invalid hash encoding in database should fail authentication safely.
            return false;
        }
    }

    private static byte[] sha256(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(password.getBytes(StandardCharsets.UTF_8));
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static byte[] sha256(byte[] salt, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            digest.update(password.getBytes(StandardCharsets.UTF_8));
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static boolean isHexSha256(String value) {
        return value.length() == 64 && value.matches("^[0-9a-fA-F]{64}$");
    }

    private static byte[] hexToBytes(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            int high = Character.digit(hex.charAt(i), 16);
            int low = Character.digit(hex.charAt(i + 1), 16);
            bytes[i / 2] = (byte) ((high << 4) + low);
        }
        return bytes;
    }
}
