package com.microsoft.windowsazure.services.media;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.microsoft.windowsazure.core.utils.Base64;

public final class EncryptionUtils {

    // Enforce noninstantiability with a private constructor
    private EncryptionUtils() {
        // not called
    }

    public static byte[] encryptSymmetricKeyData(X509Certificate certificate, byte[] contentKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            Key publicKey = certificate.getPublicKey();
            cipher.init(Cipher.ENCRYPT_MODE, publicKey, new SecureRandom());
            return cipher.doFinal(contentKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String calculateChecksum(byte[] contentKey, UUID contentKeyIdUuid) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(contentKey, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptionResult = cipher.doFinal(contentKeyIdUuid.toString().getBytes("UTF8"));
            byte[] checksumByteArray = new byte[8];
            System.arraycopy(encryptionResult, 0, checksumByteArray, 0, 8);
            return Base64.encode(checksumByteArray);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Overwrites the supplied byte array with RNG generated data which destroys
     * the original contents.
     * 
     * @param keyToErase
     *            The content key to erase.
     */
    public static void eraseKey(byte[] keyToErase) {
        if (keyToErase != null) {
            SecureRandom random;
            try {
                random = SecureRandom.getInstance("SHA1PRNG");
                random.nextBytes(keyToErase);
            } catch (NoSuchAlgorithmException e) {
                // never reached
            }
        }
    }

    public static String getThumbPrint(X509Certificate cert)
            throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] der = cert.getEncoded();
        md.update(der);
        byte[] digest = md.digest();
        return hexify(digest);
    }

    public static String hexify(byte[] bytes) {
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        StringBuffer buf = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; ++i) {
            buf.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
            buf.append(hexDigits[bytes[i] & 0x0f]);
        }
        return buf.toString();
    }
}