package com.azure.identity.implementation.intellij;

// use spongycastle repackaging of bouncycastle in deference to Android needs

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Encryption and decryption utilities..
 *
 * @author jo
 */
public class IntelliJCryptoUtil {

    /**
     * Gets a digest for a UTF-8 encoded string
     *
     * @param string the string
     * @return a digest as a byte array
     */
    @SuppressWarnings("unused")
    public static byte[] getDigest(String string) {
        return getDigest(string, "UTF-8");
    }

    /**
     * Gets a digest for a string
     *
     * @param string the string
     * @param encoding the encoding of the String
     * @return a digest as a byte array
     */
    public static byte[] getDigest(String string, String encoding) {
        if (string == null || string.length() == 0)
            throw new IllegalArgumentException("String cannot be null or empty");

        if (encoding == null || encoding.length() == 0)
            throw new IllegalArgumentException("Encoding cannot be null or empty");

        MessageDigest md = getMessageDigestInstance();

        try {
            byte[] bytes = string.getBytes(encoding);
            md.update(bytes, 0, bytes.length);
            return md.digest();
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(encoding + " is not supported");
        }
    }

    /**
     * Gets a SHA-256 message digest instance
     *
     * @return A MessageDigest
     */
    public static MessageDigest getMessageDigestInstance() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not supported");
        }
    }

    /**
     * Create a final key from the parameters passed
     */
    public static byte[] getFinalKeyDigest(byte[] key, byte[] masterSeed, byte[] transformSeed, long transformRounds) {

        final byte[] transformedKey = Aes.transformKey(transformSeed, key, transformRounds);

        final MessageDigest md = getMessageDigestInstance();
        final byte[] transformedKeyDigest = md.digest(transformedKey);

        md.update(masterSeed);
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
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static byte[] key;

    public static Salsa20 createSalsa20(byte[] key) {
        Salsa20 salsa20Decrypt;
        try {
            MessageDigest md = getMessageDigestInstance();
            byte[] mdKey = md.digest(key);
            byte[] iv = new IvParameterSpec(mdKey, 0, 8).getIV();

            salsa20Decrypt = new Salsa20();
            salsa20Decrypt.engineInitDecrypt(mdKey, iv);
        } catch (Exception e) {
            throw new IllegalStateException("Error when creating Salsa 20 Decryptor", e);
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
