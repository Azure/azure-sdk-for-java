// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography.implementation;

import io.clientcore.core.instrumentation.logging.ClientLogger;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.Objects;

abstract class AesKw extends LocalKeyWrapAlgorithm {
    private static final ClientLogger LOGGER = new ClientLogger(AesKw.class);
    static final int BLOCK_SIZE_IN_BITS = 64;
    static final String CIPHER_NAME = "AESWrap";
    static final byte[] DEFAULT_IV = new byte[] {
        (byte) 0xA6,
        (byte) 0xA6,
        (byte) 0xA6,
        (byte) 0xA6,
        (byte) 0xA6,
        (byte) 0xA6,
        (byte) 0xA6,
        (byte) 0xA6 };

    static class AesKwDecryptor implements ICryptoTransform {
        final Cipher cipher;

        AesKwDecryptor(byte[] key, byte[] iv, Provider provider) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

            if (provider == null) {
                cipher = Cipher.getInstance(CIPHER_NAME);
            } else {
                cipher = Cipher.getInstance(CIPHER_NAME, provider);
            }

            // The default provider does not support the specification of IV. This is guarded by the CreateEncrypter
            // wrapper method and the iv parameter can be ignored when using the default provider.
            if (provider == null) {
                cipher.init(Cipher.UNWRAP_MODE, new SecretKeySpec(key, "AES"));
            } else {
                cipher.init(Cipher.UNWRAP_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            }
        }

        @Override
        public byte[] doFinal(byte[] plaintext) throws InvalidKeyException, NoSuchAlgorithmException {

            return cipher.unwrap(plaintext, "AESWrap", Cipher.SECRET_KEY).getEncoded();
        }

    }

    static class AesKwEncryptor implements ICryptoTransform {
        final Cipher cipher;

        AesKwEncryptor(byte[] key, byte[] iv, Provider provider) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

            if (provider == null) {
                cipher = Cipher.getInstance(CIPHER_NAME);
            } else {
                cipher = Cipher.getInstance(CIPHER_NAME, provider);
            }

            // The default provider does not support the specification of IV. This is guarded by the CreateEncrypter
            // wrapper method and the iv parameter can be ignored when using the default provider.
            if (provider == null) {
                cipher.init(Cipher.WRAP_MODE, new SecretKeySpec(key, "AES"));
            } else {
                cipher.init(Cipher.WRAP_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            }
        }

        @Override
        public byte[] doFinal(byte[] plaintext) throws IllegalBlockSizeException, InvalidKeyException {

            return cipher.wrap(new SecretKeySpec(plaintext, "AES"));
        }

    }

    protected AesKw(String name) {
        super(name);
    }

    @Override
    public ICryptoTransform createEncryptor(byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException,
        InvalidKeyException, InvalidAlgorithmParameterException {

        return createEncryptor(key, null, null);
    }

    @Override
    public ICryptoTransform createEncryptor(byte[] key, Provider provider) throws NoSuchAlgorithmException,
        NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

        return createEncryptor(key, null, provider);
    }

    @Override
    public ICryptoTransform createEncryptor(byte[] key, byte[] iv) throws NoSuchAlgorithmException,
        NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

        return createEncryptor(key, iv, null);
    }

    @Override
    public ICryptoTransform createEncryptor(byte[] key, byte[] iv, Provider provider) throws NoSuchAlgorithmException,
        NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

        Objects.requireNonNull(key, "'key' cannot be null.");

        if (key.length != 128 >> 3 && key.length != 192 >> 3 && key.length != 256 >> 3) {
            throw LOGGER.throwableAtError()
                .addKeyValue("length", key.length)
                .log("key length must be 128, 192 or 256 bits.", IllegalArgumentException::new);
        }

        if (iv != null) {
            // iv length must be 64 bits
            if (iv.length != BLOCK_SIZE_IN_BITS >> 3) {
                throw LOGGER.throwableAtError()
                    .addKeyValue("length", iv.length)
                    .addKeyValue("expectedLength", BLOCK_SIZE_IN_BITS >> 3)
                    .log("Invalid iv length.", IllegalArgumentException::new);
            }
            // iv cannot be specified with the default provider
            if (provider == null) {
                throw LOGGER.throwableAtError()
                    .log("user specified iv is not supported with the default provider.",
                        IllegalArgumentException::new);
            }
        }

        return new AesKwEncryptor(key, iv == null ? DEFAULT_IV : iv, provider);

    }

    @Override
    public ICryptoTransform createDecryptor(byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException,
        InvalidKeyException, InvalidAlgorithmParameterException {

        return createDecryptor(key, null, null);
    }

    @Override
    public ICryptoTransform createDecryptor(byte[] key, Provider provider) throws NoSuchAlgorithmException,
        NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

        return createDecryptor(key, null, provider);
    }

    @Override
    public ICryptoTransform createDecryptor(byte[] key, byte[] iv) throws NoSuchAlgorithmException,
        NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

        return createDecryptor(key, iv, null);
    }

    @Override
    public ICryptoTransform createDecryptor(byte[] key, byte[] iv, Provider provider) throws NoSuchAlgorithmException,
        NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

        Objects.requireNonNull(key, "'key' cannot be null.");

        if (key.length != 128 >> 3 && key.length != 192 >> 3 && key.length != 256 >> 3) {
            throw LOGGER.throwableAtError()
                .addKeyValue("length", key.length)
                .log("key length must be 128, 192 or 256 bits.", IllegalArgumentException::new);
        }

        if (iv != null) {
            // iv length must be 64 bits
            if (iv.length != BLOCK_SIZE_IN_BITS >> 3) {
                throw LOGGER.throwableAtError()
                    .addKeyValue("length", iv.length)
                    .addKeyValue("expectedLength", BLOCK_SIZE_IN_BITS >> 3)
                    .log("Invalid iv length.", IllegalArgumentException::new);
            }
            // iv cannot be specified with the default provider
            if (provider == null) {
                throw LOGGER.throwableAtError()
                    .log("user specified iv is not supported with the default provider.",
                        IllegalArgumentException::new);
            }
        }

        return new AesKwDecryptor(key, iv == null ? DEFAULT_IV : iv, provider);
    }
}
