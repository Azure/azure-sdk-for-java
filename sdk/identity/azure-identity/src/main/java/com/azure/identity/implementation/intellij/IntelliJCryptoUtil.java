package com.azure.identity.implementation.intellij;

import com.azure.identity.CredentialUnavailableException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Encryption and decryption utilities..
 *
 */
public class IntelliJCryptoUtil {

    private static final byte[] SALSA20_IV = Hex.decode("E830094B97205D2A".getBytes());

//    /**
//     * Gets a digest for a UTF-8 encoded string
//     *
//     * @param string the string
//     * @return a digest as a byte array
//     */
    @SuppressWarnings("unused")
//    public static byte[] getDigest(String string) {
//        return getDigest(string, "UTF-8");
//    }

    /**
     * Gets a digest for a string
     *
     * @param string the string
     * @param encoding the encoding of the String
     * @return a digest as a byte array
     */
//    public static byte[] getDigest(String string, String encoding) {
//        if (string == null || string.length() == 0)
//            throw new IllegalArgumentException("String cannot be null or empty");
//
//        if (encoding == null || encoding.length() == 0)
//            throw new IllegalArgumentException("Encoding cannot be null or empty");
//
//        MessageDigest md = getMessageDigestSHA256();
//
//        try {
//            byte[] bytes = string.getBytes(encoding);
//            md.update(bytes, 0, bytes.length);
//            return md.digest();
//        } catch (UnsupportedEncodingException e) {
//            throw new IllegalStateException(encoding + " is not supported");
//        }
//    }

    /**
     * Gets SHA-256 message digest
     *
     * @return The MessageDigest with SHA-256 algorithm.
     */
    public static MessageDigest getMessageDigestSHA256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new CredentialUnavailableException("Algorithm SHA-256 is not supported."
                + " Decryption of IntelliJ Token data is not possible.", e);
        }
    }

    /**
     * Create a key from the parameters passed
     */
    public static byte[] createKey(byte[] key, byte[] baseSeed, byte[] transformSeed, long rounds) {

        final byte[] transformedKey = Aes.transformKey(transformSeed, key, rounds);

        final MessageDigest md = getMessageDigestSHA256();
        final byte[] transformedKeyDigest = md.digest(transformedKey);

        md.update(baseSeed);
        return md.digest(transformedKeyDigest);
    }

    /**
     * Create a decrypted input stream from an encrypted one
     */
    public static InputStream getDecryptedInputStream (InputStream encryptedInputStream, byte[] keyData, byte[] ivData) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

            SecretKey key = new SecretKeySpec(keyData, "AES");
            IvParameterSpec iv = new IvParameterSpec(ivData);

            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            return new CipherInputStream(encryptedInputStream, cipher);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException
            | InvalidAlgorithmParameterException e) {
            throw new CredentialUnavailableException("Error Decrypting the IntelliJ cache database.", e);
        }
    }


    public static Salsa20 createSalsa20CryptoEngine(byte[] key) {
        Salsa20 salsa20Decrypt;
        try {
            MessageDigest md = getMessageDigestSHA256();
            byte[] mdKey = md.digest(key);

            salsa20Decrypt = new Salsa20();
            salsa20Decrypt.engineInitDecrypt(mdKey, SALSA20_IV);
        } catch (Exception e) {
            throw new CredentialUnavailableException("Error creating the Salsa 20 Decryption Engine.", e);
        }
        return salsa20Decrypt;
    }

    public static byte[] decrypt(byte[] encryptedText, Salsa20 salsa20) {
        byte[] output = new byte[encryptedText.length];
        try {
            return salsa20.crypt(encryptedText, 0, encryptedText.length);
        } catch (Exception e) {
            throw new IllegalStateException("Error when Decryption", e);
        }
    }

}
