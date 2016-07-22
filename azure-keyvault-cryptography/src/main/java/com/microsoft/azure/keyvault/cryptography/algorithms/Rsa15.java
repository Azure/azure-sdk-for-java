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

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.microsoft.azure.keyvault.cryptography.ICryptoTransform;

public final class Rsa15 extends RsaEncryption {

    class Rsa15Decryptor implements ICryptoTransform {

        private final Cipher _cipher;

        Rsa15Decryptor(KeyPair keyPair, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {

            // Create a cipher object using the provider, if specified
            if (provider == null) {
                _cipher = Cipher.getInstance(RSA15);
            } else {
                _cipher = Cipher.getInstance(RSA15, provider);
            }

            // encrypt the plain text using the public key
            _cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        }

        @Override
        public byte[] doFinal(byte[] plaintext) throws IllegalBlockSizeException, BadPaddingException {

            return _cipher.doFinal(plaintext);
        }

    }

    class Rsa15Encryptor implements ICryptoTransform {

        private final Cipher _cipher;

        Rsa15Encryptor(KeyPair keyPair, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {

            // Create a cipher object using the provider, if specified
            if (provider == null) {
                _cipher = Cipher.getInstance(RSA15);
            } else {
                _cipher = Cipher.getInstance(RSA15, provider);
            }

            // encrypt the plain text using the public key
            _cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
        }

        @Override
        public byte[] doFinal(byte[] plaintext) throws IllegalBlockSizeException, BadPaddingException {

            return _cipher.doFinal(plaintext);
        }

    }

    final static String RSA15 = "RSA/ECB/PKCS1Padding";

    public final static String AlgorithmName = "RSA1_5";

    public Rsa15() {
        super(AlgorithmName);
    }

    @Override
    public ICryptoTransform CreateEncryptor(KeyPair keyPair) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        return CreateEncryptor(keyPair, null);
    }

    @Override
    public ICryptoTransform CreateEncryptor(KeyPair keyPair, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        return new Rsa15Encryptor(keyPair, provider);
    }

    @Override
    public ICryptoTransform CreateDecryptor(KeyPair keyPair) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        return CreateDecryptor(keyPair, null);
    }

    @Override
    public ICryptoTransform CreateDecryptor(KeyPair keyPair, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        return new Rsa15Decryptor(keyPair, provider);
    }

}
