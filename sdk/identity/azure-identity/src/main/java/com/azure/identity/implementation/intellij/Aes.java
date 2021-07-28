/*
 * Includes code from the Openkeepass project, which is licensed under Apache-2.0
 *
 * License Link: https://github.com/cternes/openkeepass/blob/master/LICENSE.txt
 * -------------------------------------------------------------------------------------------------
 * Note:
 *
 * The AES implementation taken from the project: https://github.com/cternes/openkeepass
 *
 * is specifically from the following file:
 *
 *  https://github.com/cternes/openkeepass/blob/master/src/main/java/de/slackspace/openkeepass/crypto/Aes.java
 *
 * The original idea is modified and refactored to adapt to identity use case.
 *  -------------------------------------------------------------------------------------------------
 */
/*
 * Portions Copyright (c) Microsoft Corporation
 */

package com.azure.identity.implementation.intellij;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class Aes {

    private static final String MSG_KEY_MUST_NOT_BE_NULL = "Key must not be null";
    private static final String MSG_IV_MUST_NOT_BE_NULL = "IV must not be null";
    private static final String MSG_DATA_MUST_NOT_BE_NULL = "Data must not be null";
    private static final String KEY_TRANSFORMATION = "AES/ECB/NoPadding";
    private static final String DATA_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";

    private Aes() {
    }

    public static byte[] decrypt(byte[] key, byte[] ivRaw, byte[] data) {
        if (key == null) {
            throw new IllegalArgumentException(MSG_KEY_MUST_NOT_BE_NULL);
        }
        if (ivRaw == null) {
            throw new IllegalArgumentException(MSG_IV_MUST_NOT_BE_NULL);
        }
        if (data == null) {
            throw new IllegalArgumentException(MSG_DATA_MUST_NOT_BE_NULL);
        }

        return transformData(key, ivRaw, data, Cipher.DECRYPT_MODE);
    }

    private static byte[] transformData(byte[] key, byte[] ivRaw, byte[] encryptedData, int operationMode) {
        try {
            Cipher cipher = Cipher.getInstance(DATA_TRANSFORMATION);
            Key aesKey = new SecretKeySpec(key, KEY_ALGORITHM);
            IvParameterSpec iv = new IvParameterSpec(ivRaw);
            cipher.init(operationMode, aesKey, iv);
            return cipher.doFinal(encryptedData);
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException("The specified algorithm is unknown", e);
        } catch (NoSuchPaddingException e) {
            throw new UnsupportedOperationException("The specified padding is unknown", e);
        } catch (InvalidKeyException e) {
            throw new UnsupportedOperationException(e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new UnsupportedOperationException(e);
        } catch (IllegalBlockSizeException e) {
            throw new UnsupportedOperationException(e);
        } catch (BadPaddingException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public static byte[] transformKey(byte[] key, byte[] data, long rounds) {
        if (key == null) {
            throw new IllegalArgumentException(MSG_KEY_MUST_NOT_BE_NULL);
        }
        if (data == null) {
            throw new IllegalArgumentException(MSG_DATA_MUST_NOT_BE_NULL);
        }
        if (rounds < 1) {
            throw new IllegalArgumentException("Rounds must be > 1");
        }

        try {
            Cipher c = Cipher.getInstance(KEY_TRANSFORMATION);
            Key aesKey = new SecretKeySpec(key, KEY_ALGORITHM);
            c.init(Cipher.ENCRYPT_MODE, aesKey);

            for (long i = 0; i < rounds; ++i) {
                c.update(data, 0, 16, data, 0);
                c.update(data, 16, 16, data, 16);
            }

            return data;
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException("The specified algorithm is unknown", e);
        } catch (NoSuchPaddingException e) {
            throw new UnsupportedOperationException("The specified padding is unknown", e);
        } catch (InvalidKeyException e) {
            throw new UnsupportedOperationException(
                    "The key has the wrong size. Have you installed Java Cryptography Extension (JCE)? Is the master key correct?", e);
        } catch (ShortBufferException e) {
            throw new AssertionError(e);
        }
    }

}
