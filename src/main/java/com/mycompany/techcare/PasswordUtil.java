package com.mycompany.techcare;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtil {

    private static final int ITERATIONS = 210_000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String hashPassword(char[] password)
            throws GeneralSecurityException {

        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);

        byte[] hash = deriveKey(password, salt, ITERATIONS);

        return "pbkdf2$"
                + ITERATIONS + "$"
                + Base64.getUrlEncoder().withoutPadding().encodeToString(salt)
                + "$"
                + Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    public static boolean verifyPassword(char[] password, String storedHash)
            throws GeneralSecurityException {

        if (password == null || storedHash == null) {
            return false;
        }

        String[] parts = storedHash.split("\\$", -1);

        if (parts.length != 4 || !"pbkdf2".equals(parts[0])) {
            return false;
        }

        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getUrlDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getUrlDecoder().decode(parts[3]);

            if (iterations < 10_000 || iterations > 1_000_000) {
                return false;
            }

            byte[] actualHash = deriveKey(password, salt, iterations);
            return MessageDigest.isEqual(expectedHash, actualHash);

        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static byte[] deriveKey(char[] password, byte[] salt, int iterations)
            throws GeneralSecurityException {

        PBEKeySpec spec = new PBEKeySpec(
                password,
                salt,
                iterations,
                KEY_LENGTH
        );

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }
}