// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.Closeable;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

// TODO change to package private? moderakh
public class AesCryptoServiceProvider {
    private static final String ALGO_NAME = "AES";

    private final Cipher cipher;
    private final SecretKeySpec secretKeySpec;
    private IvParameterSpec ivspec;

    enum PaddingMode {
        PKCS5("PKCS5Padding"),
        PKCS7("PKCS7Padding");

        String value;

        PaddingMode(String value) {
            this.value = value;
        }
    }

    enum CipherMode {
        CBC("CBC");

        String value;

        CipherMode(String value) {
            this.value = value;
        }
    }

    public AesCryptoServiceProvider(byte[] key, PaddingMode padding, CipherMode mode) {
        try {
            secretKeySpec = new SecretKeySpec(key, ALGO_NAME);

            cipher = Cipher.getInstance(String.format("%s/%s/%s", ALGO_NAME, mode.value, padding.value));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static class ICryptoTransform implements Closeable {
        private final Cipher cipher;

        public ICryptoTransform(Cipher cipher) {
            this.cipher = cipher;
        }

        @Override
        public void close() {

        }

        public int transformBlock(
            byte[] inputBuffer,
            int inputOffset,
            int inputCount,
            byte[] outputBuffer,
            int outputOffset) {

            try {
                return cipher.update(inputBuffer, inputOffset, inputCount, outputBuffer, outputOffset);
            } catch (ShortBufferException e) {
                throw new IllegalStateException(e);
            }
        }

        public byte[] transformFinalBlock(byte[] inputBuffer, int inputOffset, int inputCount) {
            try {
                return cipher.doFinal(inputBuffer, inputOffset, inputCount);
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public ICryptoTransform createDecryptor() {
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivspec);
            return new ICryptoTransform(cipher);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new IllegalStateException(e);
        }
    }

    public ICryptoTransform createEncryptor() {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivspec);
            return new ICryptoTransform(cipher);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new IllegalStateException(e);
        }
    }

    public void setIv(byte[] iv) {
        ivspec = new IvParameterSpec(iv);
    }
}
