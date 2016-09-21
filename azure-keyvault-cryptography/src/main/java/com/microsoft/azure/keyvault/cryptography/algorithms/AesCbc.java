/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault.cryptography.algorithms;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.microsoft.azure.keyvault.cryptography.ICryptoTransform;
import com.microsoft.azure.keyvault.cryptography.SymmetricEncryptionAlgorithm;

public abstract class AesCbc extends SymmetricEncryptionAlgorithm {

    final int keySizeInBytes;
    final int keySize;
    static class AesCbcDecryptor implements ICryptoTransform {

        private final Cipher _cipher;

        AesCbcDecryptor(byte[] key, byte[] iv, Provider provider) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

            // Create the cipher using the Provider if specified
            if (provider == null) {
                _cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            } else {
                _cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", provider);
            }

            _cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
        }

        @Override
        public byte[] doFinal(byte[] plaintext) throws IllegalBlockSizeException, BadPaddingException {
            return _cipher.doFinal(plaintext);
        }
    }

    static class AesCbcEncryptor implements ICryptoTransform {

        private final Cipher _cipher;

        AesCbcEncryptor(byte[] key, byte[] iv, Provider provider) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

            // Create the cipher using the Provider if specified
            if (provider == null) {
                _cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            } else {
                _cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", provider);
            }

            _cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
        }

        @Override
        public byte[] doFinal(byte[] plaintext) throws IllegalBlockSizeException, BadPaddingException {
            return _cipher.doFinal(plaintext);
        }
    }

    protected AesCbc(String name, int size) {
        super(name);
        keySize = size;
        keySizeInBytes = size >> 3;
    }

    @Override
    public ICryptoTransform CreateEncryptor(byte[] key, byte[] iv, byte[] authenticationData) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        
        if (key == null || key.length < keySizeInBytes) {
            throw new InvalidKeyException("key must be at least " + keySize + " bits in length");
        }
        
        return new AesCbcEncryptor(Arrays.copyOfRange(key, 0, keySizeInBytes), iv, null);
    }

    @Override
    public ICryptoTransform CreateEncryptor(byte[] key, byte[] iv, byte[] authenticationData, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {

        if (key == null || key.length < keySizeInBytes) {
            throw new InvalidKeyException("key must be at least " + keySize + " bits in length");
        }
        
        return new AesCbcEncryptor(Arrays.copyOfRange(key, 0, keySizeInBytes), iv, provider);
    }

    @Override
    public ICryptoTransform CreateDecryptor(byte[] key, byte[] iv, byte[] authenticationData) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {

        if (key == null || key.length < keySizeInBytes) {
            throw new InvalidKeyException("key must be at least " + keySize + " bits in length");
        }
        
        return new AesCbcDecryptor(Arrays.copyOfRange(key, 0, keySizeInBytes), iv, null);
    }

    @Override
    public ICryptoTransform CreateDecryptor(byte[] key, byte[] iv, byte[] authenticationData, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {

        if (key == null || key.length < keySizeInBytes) {
            throw new InvalidKeyException("key must be at least " + keySize + " bits in length");
        }
        
        return new AesCbcDecryptor(Arrays.copyOfRange(key, 0, keySizeInBytes), iv, provider);
    }
}
