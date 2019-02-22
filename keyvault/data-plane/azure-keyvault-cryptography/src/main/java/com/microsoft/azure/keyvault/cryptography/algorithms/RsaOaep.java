// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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

    static class RsaOaepDecryptor implements ICryptoTransform {

        private final Cipher cipher;

        RsaOaepDecryptor(KeyPair keyPair, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {

            // Create a cipher object using the provider, if specified
            if (provider == null) {
                cipher = Cipher.getInstance(RSAOAEP);
            } else {
                cipher = Cipher.getInstance(RSAOAEP, provider);
            }

            // encrypt the plain text using the public key
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        }

        @Override
        public byte[] doFinal(byte[] plaintext) throws IllegalBlockSizeException, BadPaddingException {

            return cipher.doFinal(plaintext);
        }

    }

    static class RsaOaepEncryptor implements ICryptoTransform {

        private final Cipher cipher;

        RsaOaepEncryptor(KeyPair keyPair, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {

            // Create a cipher object using the provider, if specified
            if (provider == null) {
                cipher = Cipher.getInstance(RSAOAEP);
            } else {
                cipher = Cipher.getInstance(RSAOAEP, provider);
            }

            // encrypt the plain text using the public key
            cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
        }

        @Override
        public byte[] doFinal(byte[] plaintext) throws IllegalBlockSizeException, BadPaddingException {

            return cipher.doFinal(plaintext);
        }

    }

    static final String RSAOAEP = "RSA/ECB/OAEPWithSHA1AndMGF1Padding";

    public static final String ALGORITHM_NAME = "RSA-OAEP";

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
