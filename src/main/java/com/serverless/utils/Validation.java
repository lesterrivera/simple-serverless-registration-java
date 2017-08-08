package com.serverless.utils;

import com.serverless.RegisterSubscriberHandler;
import org.apache.log4j.Logger;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

/**
 * Validates the verifyToken using strong cryptographic hash
 */
public class Validation {

    private static final Logger LOG = Logger.getLogger(Validation.class);

    /**
     * Generates a strong hash value
     * @param password - password value (in the clear)
     * @param salt - salt value
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    private static byte[] generateStrongPasswordHash(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        int iterations = 1000;
        char[] chars = password.toCharArray();

        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return hash;
    }

    /**
     * Generates a salt value
     * @return
     * @throws NoSuchAlgorithmException
     */
    private static byte[] getSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }

    /**
     * Convert byte array to a Hex value
     * @param array
     * @return
     * @throws NoSuchAlgorithmException
     */
    private static String toHex(byte[] array) throws NoSuchAlgorithmException {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0) {
            return String.format("%0"  +paddingLength + "d", 0) + hex;
        } else {
            return hex;
        }
    }

    /**
     * Converts Hex value to byte array
     * @param hex
     * @return
     * @throws NoSuchAlgorithmException
     */
    private static byte[] fromHex(String hex) throws NoSuchAlgorithmException {
        byte[] bytes = new byte[hex.length() / 2];
        for(int i = 0; i<bytes.length ;i++)
        {
            bytes[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

    /**
     * Generates a hash value from a clear value
     * @param password
     * @return
     * @throws Exception
     */
    public static String generateHash(String password) throws Exception {

        // Generate the salt
        byte[] salt = getSalt();
        // Generate the hash
        byte[] hash = generateStrongPasswordHash(password, salt);

        // Return as a parsable string
        return toHex(salt) + ":" + toHex(hash);
    }

    /**
     * Validates a clear value with an associate salted hash value
     * @param password
     * @param saltedHash
     * @return
     * @throws Exception
     */
    public static Boolean validateHash(String password, String saltedHash) throws Exception {
        // Split the saltedHash
        String[] parts = saltedHash.split(":");
        byte[] salt = fromHex(parts[0]);
        byte[] hash = fromHex(parts[1]);

        // Generate the test hash from the new password
        byte[] testHash = generateStrongPasswordHash(password, salt);

        // Test the two hash values to determine if the same
        int diff = hash.length ^ testHash.length;
        for(int i = 0; i < hash.length && i < testHash.length; i++) {
            diff |= hash[i] ^ testHash[i];
        }
        return diff == 0;
    }

    /**
     * Generate a token of the specified size
     * @param size - the number of characters of the token
     * @return
     */
    public static String generateVerifyToken(int size) {

        StringBuilder generatedToken = new StringBuilder();
        try {
            SecureRandom number = SecureRandom.getInstance("SHA1PRNG");
            // Generate 20 integers 0..20
            for (int i = 0; i < size; i++) {
                generatedToken.append(number.nextInt(9));
            }
        } catch (Exception e) {
            LOG.error(e,e);
        }

        return generatedToken.toString();
    }
}
