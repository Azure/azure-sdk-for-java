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

import javax.crypto.NoSuchPaddingException;

import com.microsoft.azure.keyvault.cryptography.ByteExtensions;
import com.microsoft.azure.keyvault.cryptography.ICryptoTransform;

public final class AesKw256 extends AesKw {

    public static final String AlgorithmName = "A256KW";
    
    static final int KeySizeInBytes = 256 >> 3;

    public AesKw256() {
        super(AlgorithmName);
    }

    @Override
    public ICryptoTransform CreateEncryptor(byte[] key, byte[] iv, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {

        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }

        if (key.length < KeySizeInBytes) {
            throw new IllegalArgumentException("key must be at least 256 bits long");
        }

        return super.CreateEncryptor(ByteExtensions.take(key, KeySizeInBytes), iv, provider);
    }

    @Override
    public ICryptoTransform CreateDecryptor(byte[] key, byte[] iv, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {

        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }

        if (key.length < KeySizeInBytes) {
            throw new IllegalArgumentException("key must be at least 256 bits long");
        }

        return super.CreateDecryptor(ByteExtensions.take(key, KeySizeInBytes), iv, provider);
    }

}
