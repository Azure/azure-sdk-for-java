// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;

class Rsa15 extends RsaEncryption {

    static class Rsa15Decryptor implements ICryptoTransform {

        private final Cipher cipher;

        Rsa15Decryptor(KeyPair keyPair, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {

            // Create a cipher object using the provider, if specified
            if (provider == null) {
                cipher = Cipher.getInstance(RSA15);
            } else {
                cipher = Cipher.getInstance(RSA15, provider);
            }

            // encrypt the plain text using the public key
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        }

        @Override
        public byte[] doFinal(byte[] plaintext) throws IllegalBlockSizeException, BadPaddingException {

            return cipher.doFinal(plaintext);
        }

    }

    static class Rsa15Encryptor implements ICryptoTransform {

        private final Cipher cipher;

        Rsa15Encryptor(KeyPair keyPair, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {

            // Create a cipher object using the provider, if specified
            if (provider == null) {
                cipher = Cipher.getInstance(RSA15);
            } else {
                cipher = Cipher.getInstance(RSA15, provider);
            }

            // encrypt the plain text using the public key
            cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
        }

        @Override
        public byte[] doFinal(byte[] plaintext) throws IllegalBlockSizeException, BadPaddingException {

            return cipher.doFinal(plaintext);
        }

    }

    static final String RSA15 = "RSA/ECB/PKCS1Padding";

    public static final String ALGORITHM_NAME = "RSA1_5";

    Rsa15() {
        super(ALGORITHM_NAME);
    }

    @Override
    public ICryptoTransform createEncryptor(KeyPair keyPair) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        return createEncryptor(keyPair, null);
    }

    @Override
    public ICryptoTransform createEncryptor(KeyPair keyPair, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        return new Rsa15Encryptor(keyPair, provider);
    }

    @Override
    public ICryptoTransform createDecryptor(KeyPair keyPair) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        return createDecryptor(keyPair, null);
    }

    @Override
    public ICryptoTransform createDecryptor(KeyPair keyPair, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        return new Rsa15Decryptor(keyPair, provider);
    }

}
