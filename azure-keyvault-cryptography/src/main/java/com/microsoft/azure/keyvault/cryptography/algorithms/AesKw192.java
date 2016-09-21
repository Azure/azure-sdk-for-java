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

import javax.crypto.NoSuchPaddingException;

import com.microsoft.azure.keyvault.cryptography.ICryptoTransform;

public final class AesKw192 extends AesKw {

    public static final String ALGORITHM_NAME = "A192KW";
    
    static final int KeySizeInBytes = 192 >> 3;

    public AesKw192() {
        super(ALGORITHM_NAME);
    }

    @Override
    public ICryptoTransform CreateEncryptor(byte[] key, byte[] iv, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {

        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }

        if (key.length < KeySizeInBytes) {
            throw new IllegalArgumentException("key must be at least 192 bits long");
        }

        return super.CreateEncryptor(Arrays.copyOfRange(key, 0, KeySizeInBytes), iv, provider);
    }

    @Override
    public ICryptoTransform CreateDecryptor(byte[] key, byte[] iv, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {

        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }

        if (key.length < KeySizeInBytes) {
            throw new IllegalArgumentException("key must be at least 192 bits long");
        }

        return super.CreateDecryptor(Arrays.copyOfRange(key, 0, KeySizeInBytes), iv, provider);
    }

}
