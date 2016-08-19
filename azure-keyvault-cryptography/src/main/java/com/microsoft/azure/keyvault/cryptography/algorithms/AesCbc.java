/**
 *
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.microsoft.azure.keyvault.cryptography.algorithms;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.microsoft.azure.keyvault.cryptography.ICryptoTransform;
import com.microsoft.azure.keyvault.cryptography.SymmetricEncryptionAlgorithm;

public abstract class AesCbc extends SymmetricEncryptionAlgorithm {

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

    protected AesCbc(String name) {
        super(name);
    }

    @Override
    public ICryptoTransform CreateEncryptor(byte[] key, byte[] iv, byte[] authenticationData) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {

        return new AesCbcEncryptor(key, iv, null);
    }

    @Override
    public ICryptoTransform CreateEncryptor(byte[] key, byte[] iv, byte[] authenticationData, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {

        return new AesCbcEncryptor(key, iv, provider);
    }

    @Override
    public ICryptoTransform CreateDecryptor(byte[] key, byte[] iv, byte[] authenticationData) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {

        return new AesCbcDecryptor(key, iv, null);
    }

    @Override
    public ICryptoTransform CreateDecryptor(byte[] key, byte[] iv, byte[] authenticationData, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {

        return new AesCbcDecryptor(key, iv, provider);
    }
}
