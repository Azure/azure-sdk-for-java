// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography.implementation;

import io.clientcore.core.instrumentation.logging.ClientLogger;

import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.Arrays;

class Aes256Kw extends AesKw {
    private static final ClientLogger LOGGER = new ClientLogger(Aes256Kw.class);
    public static final String ALGORITHM_NAME = "A256KW";

    static final int KEY_SIZE_IN_BYTES = 256 >> 3;

    Aes256Kw() {
        super(ALGORITHM_NAME);
    }

    @Override
    public ICryptoTransform createEncryptor(byte[] key, byte[] iv, Provider provider) throws InvalidKeyException,
        NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {

        CryptographyUtils.validate(key, KEY_SIZE_IN_BYTES, LOGGER);

        return super.createEncryptor(Arrays.copyOfRange(key, 0, KEY_SIZE_IN_BYTES), iv, provider);
    }

    @Override
    public ICryptoTransform createDecryptor(byte[] key, byte[] iv, Provider provider) throws InvalidKeyException,
        NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {

        CryptographyUtils.validate(key, KEY_SIZE_IN_BYTES, LOGGER);

        return super.createDecryptor(Arrays.copyOfRange(key, 0, KEY_SIZE_IN_BYTES), iv, provider);
    }

}
