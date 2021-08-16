// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.Arrays;

abstract class AesCbcPad extends SymmetricEncryptionAlgorithm {
    final int keySizeInBytes;
    final int keySize;

    protected AesCbcPad(String name, int size) {
        super(name);

        keySize = size;
        keySizeInBytes = size >> 3;
    }

    static class AesCbcPadEncryptor implements ICryptoTransform {
        private final Cipher cipher;

        AesCbcPadEncryptor(byte[] key, byte[] iv, Provider provider) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

            // Create the cipher using the Provider if specified
            if (provider == null) {
                cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            } else {
                cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", provider);
            }

            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
        }

        @Override
        public byte[] doFinal(byte[] plaintext) throws IllegalBlockSizeException, BadPaddingException {
            return cipher.doFinal(plaintext);
        }
    }

    static class AesCbcPadDecryptor implements ICryptoTransform {
        private final Cipher cipher;

        AesCbcPadDecryptor(byte[] key, byte[] iv, Provider provider) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

            // Create the cipher using the Provider if specified
            if (provider == null) {
                cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            } else {
                cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", provider);
            }

            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
        }

        @Override
        public byte[] doFinal(byte[] plaintext) throws IllegalBlockSizeException, BadPaddingException {
            return cipher.doFinal(plaintext);
        }
    }

    @Override
    public ICryptoTransform createEncryptor(byte[] key, byte[] iv, byte[] additionalAuthenticatedData,
                                            byte[] authenticationTag)
        throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
        InvalidAlgorithmParameterException {

        return createEncryptor(key, iv, additionalAuthenticatedData, null, null);
    }

    @Override
    public ICryptoTransform createEncryptor(byte[] key, byte[] iv, byte[] additionalAuthenticatedData,
                                            byte[] authenticationTag, Provider provider)
        throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
        InvalidAlgorithmParameterException {

        if (key == null || key.length < keySizeInBytes) {
            throw new InvalidKeyException("key must be at least " + keySize + " bits in length");
        }

        return new AesCbcPadEncryptor(Arrays.copyOfRange(key, 0, keySizeInBytes), iv, provider);
    }

    @Override
    public ICryptoTransform createDecryptor(byte[] key, byte[] iv, byte[] additionalAuthenticatedData,
                                            byte[] authenticationTag)
        throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
        InvalidAlgorithmParameterException {

        return createDecryptor(key, iv, additionalAuthenticatedData, authenticationTag, null);
    }

    @Override
    public ICryptoTransform createDecryptor(byte[] key, byte[] iv, byte[] additionalAuthenticatedData,
                                            byte[] authenticationTag, Provider provider)
        throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
        InvalidAlgorithmParameterException {

        if (key == null || key.length < keySizeInBytes) {
            throw new InvalidKeyException("key must be at least " + keySize + " bits in length");
        }

        return new AesCbcPadDecryptor(Arrays.copyOfRange(key, 0, keySizeInBytes), iv, provider);
    }
}
