package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordHasher {
    private static final int SALT_LENGTH = 16;
    private static final String ALGORITHM = "SHA-256";

    /**
     * Hash a password using SHA-256 with a random salt
     * @param password The password to hash
     * @return A string containing the salt and hashed password
     */
    public static String hashPassword(String password) {
        try {
            // Generate random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            // Hash the password with salt
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());

            // Encode salt and hashed password to store in database
            String encodedSalt = Base64.getEncoder().encodeToString(salt);
            String encodedHash = Base64.getEncoder().encodeToString(hashedPassword);

            // Return salt and hash separated by a colon
            return encodedSalt + ":" + encodedHash;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Verify a password against a stored hash
     * @param password The password to verify
     * @param storedHash The stored hash (salt:hash)
     * @return True if the password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            // Split the stored hash into salt and hash
            String[] parts = storedHash.split(":");
            if (parts.length != 2) {
                return false;
            }

            String storedSalt = parts[0];
            String storedHashedPassword = parts[1];

            // Decode the salt
            byte[] salt = Base64.getDecoder().decode(storedSalt);

            // Hash the input password with the stored salt
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            String encodedHash = Base64.getEncoder().encodeToString(hashedPassword);

            // Compare the stored hash with the generated hash
            return storedHashedPassword.equals(encodedHash);
        } catch (Exception e) {
            return false;
        }
    }
}