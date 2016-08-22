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

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;

import javax.crypto.NoSuchPaddingException;

import com.microsoft.azure.keyvault.cryptography.ICryptoTransform;

public final class AesKw192 extends AesKw {

    public static final String AlgorithmName = "A192KW";
    
    static final int KeySizeInBytes = 192 >> 3;

    public AesKw192() {
        super(AlgorithmName);
    }

    @Override
    public ICryptoTransform CreateEncryptor(byte[] key, byte[] iv, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {

        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }

        if (key.length < KeySizeInBytes) {
            throw new IllegalArgumentException("key must be at least 192 bits long");
        }

        return super.CreateEncryptor(Take(KeySizeInBytes,key), iv, provider);
    }

    @Override
    public ICryptoTransform CreateDecryptor(byte[] key, byte[] iv, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {

        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }

        if (key.length < KeySizeInBytes) {
            throw new IllegalArgumentException("key must be at least 192 bits long");
        }

        return super.CreateDecryptor(Take(KeySizeInBytes,key), iv, provider);
    }

}
