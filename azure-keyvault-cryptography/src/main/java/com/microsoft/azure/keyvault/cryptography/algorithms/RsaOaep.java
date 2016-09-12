/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
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

public final class RsaOaep extends RsaEncryption {

    class RsaOaepDecryptor implements ICryptoTransform {

        private final Cipher _cipher;

        RsaOaepDecryptor(KeyPair keyPair, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {

            // Create a cipher object using the provider, if specified
            if (provider == null) {
                _cipher = Cipher.getInstance(RSAOAEP);
            } else {
                _cipher = Cipher.getInstance(RSAOAEP, provider);
            }

            // encrypt the plain text using the public key
            _cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        }

        @Override
        public byte[] doFinal(byte[] plaintext) throws IllegalBlockSizeException, BadPaddingException {

            return _cipher.doFinal(plaintext);
        }

    }

    class RsaOaepEncryptor implements ICryptoTransform {

        private final Cipher _cipher;

        RsaOaepEncryptor(KeyPair keyPair, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {

            // Create a cipher object using the provider, if specified
            if (provider == null) {
                _cipher = Cipher.getInstance(RSAOAEP);
            } else {
                _cipher = Cipher.getInstance(RSAOAEP, provider);
            }

            // encrypt the plain text using the public key
            _cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
        }

        @Override
        public byte[] doFinal(byte[] plaintext) throws IllegalBlockSizeException, BadPaddingException {

            return _cipher.doFinal(plaintext);
        }

    }

    final static String RSAOAEP = "RSA/ECB/OAEPWithSHA1AndMGF1Padding";

    public final static String ALGORITHM_NAME = "RSA-OAEP";

    public RsaOaep() {
        super(ALGORITHM_NAME);
    }

    @Override
    public ICryptoTransform CreateEncryptor(KeyPair keyPair) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        return CreateEncryptor(keyPair, null);
    }

    @Override
    public ICryptoTransform CreateEncryptor(KeyPair keyPair, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {

        return new RsaOaepEncryptor(keyPair, provider);
    }

    @Override
    public ICryptoTransform CreateDecryptor(KeyPair keyPair) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        return CreateDecryptor(keyPair, null);
    }

    @Override
    public ICryptoTransform CreateDecryptor(KeyPair keyPair, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {

        return new RsaOaepDecryptor(keyPair, provider);
    }

}
