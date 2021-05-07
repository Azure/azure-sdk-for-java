// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.util.logging.ClientLogger;

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

abstract class AesKw extends LocalKeyWrapAlgorithm {
    static final int BLOCK_SIZE_IN_BITS = 64;
    static final byte[] DEFAULT_IV =
        new byte[]{(byte) 0xA6, (byte) 0xA6, (byte) 0xA6, (byte) 0xA6, (byte) 0xA6, (byte) 0xA6, (byte) 0xA6,
            (byte) 0xA6};
    static final String CIPHER_NAME = "AESWrap";

    static class AesKwDecryptor implements ICryptoTransform {
        final Cipher cipher;

        AesKwDecryptor(byte[] key, byte[] iv, Provider provider)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException {

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
        public byte[] doFinal(byte[] plaintext)
            throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException {

            return cipher.unwrap(plaintext, "AESWrap", Cipher.SECRET_KEY).getEncoded();
        }

    }

    static class AesKwEncryptor implements ICryptoTransform {

        final Cipher cipher;

        AesKwEncryptor(byte[] key, byte[] iv, Provider provider)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException {

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
        public byte[] doFinal(byte[] plaintext)
            throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

            return cipher.wrap(new SecretKeySpec(plaintext, "AES"));
        }

    }

    private final ClientLogger logger = new ClientLogger(AesKw.class);

    protected AesKw(String name) {
        super(name);
    }

    @Override
    public ICryptoTransform createEncryptor(byte[] key)
        throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
        InvalidAlgorithmParameterException {

        return createEncryptor(key, null, null);
    }

    @Override
    public ICryptoTransform createEncryptor(byte[] key, Provider provider)
        throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
        InvalidAlgorithmParameterException {

        return createEncryptor(key, null, provider);
    }

    @Override
    public ICryptoTransform createEncryptor(byte[] key, byte[] iv)
        throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
        InvalidAlgorithmParameterException {

        return createEncryptor(key, iv, null);
    }

    @Override
    public ICryptoTransform createEncryptor(byte[] key, byte[] iv, Provider provider)
        throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
        InvalidAlgorithmParameterException {

        if (key == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("key"));
        }

        if (key.length != 128 >> 3 && key.length != 192 >> 3 && key.length != 256 >> 3) {
            throw logger.logExceptionAsError(new IllegalArgumentException("key length must be 128, 192 or 256 bits"));
        }

        if (iv != null) {
            // iv length must be 64 bits
            if (iv.length != BLOCK_SIZE_IN_BITS >> 3) {
                throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                    "iv length must be %s bits", BLOCK_SIZE_IN_BITS)));
            }
            // iv cannot be specified with the default provider
            if (provider == null) {
                throw logger.logExceptionAsError(new IllegalArgumentException(
                    "user specified iv is not supported with the default provider"));
            }
        }

        return new AesKwEncryptor(key, iv == null ? DEFAULT_IV : iv, provider);

    }

    @Override
    public ICryptoTransform createDecryptor(byte[] key)
        throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
        InvalidAlgorithmParameterException {

        return createDecryptor(key, null, null);
    }

    @Override
    public ICryptoTransform createDecryptor(byte[] key, Provider provider)
        throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
        InvalidAlgorithmParameterException {

        return createDecryptor(key, null, provider);
    }

    @Override
    public ICryptoTransform createDecryptor(byte[] key, byte[] iv)
        throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
        InvalidAlgorithmParameterException {

        return createDecryptor(key, iv, null);
    }

    @Override
    public ICryptoTransform createDecryptor(byte[] key, byte[] iv, Provider provider)
        throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
        InvalidAlgorithmParameterException {

        if (key == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("key"));
        }

        if (key.length != 128 >> 3 && key.length != 192 >> 3 && key.length != 256 >> 3) {
            throw logger.logExceptionAsError(new IllegalArgumentException("key length must be 128, 192 or 256 bits"));
        }

        if (iv != null) {
            // iv length must be 64 bits
            if (iv.length != BLOCK_SIZE_IN_BITS >> 3) {
                throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                    "iv length must be %s bits", BLOCK_SIZE_IN_BITS)));
            }
            // iv cannot be specified with the default provider
            if (provider == null) {
                throw logger.logExceptionAsError(new IllegalArgumentException(
                    "user specified iv is not supported with the default provider"));
            }
        }

        return new AesKwDecryptor(key, iv == null ? DEFAULT_IV : iv, provider);
    }
}
