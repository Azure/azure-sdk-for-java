// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.Arrays;

class Aes192Kw extends AesKw {

    public static final String ALGORITHM_NAME = "A192KW";

    static final int KEY_SIZE_IN_BYTES = 192 >> 3;

    Aes192Kw() {
        super(ALGORITHM_NAME);
    }

    @Override
    public ICryptoTransform createEncryptor(byte[] key, byte[] iv, Provider provider)
        throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
        InvalidAlgorithmParameterException {

        CryptoUtil.validate(key, KEY_SIZE_IN_BYTES);

        return super.createEncryptor(Arrays.copyOfRange(key, 0, KEY_SIZE_IN_BYTES), iv, provider);
    }

    @Override
    public ICryptoTransform createDecryptor(byte[] key, byte[] iv, Provider provider)
        throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
        InvalidAlgorithmParameterException {

        CryptoUtil.validate(key, KEY_SIZE_IN_BYTES);

        return super.createDecryptor(Arrays.copyOfRange(key, 0, KEY_SIZE_IN_BYTES), iv, provider);
    }

}
