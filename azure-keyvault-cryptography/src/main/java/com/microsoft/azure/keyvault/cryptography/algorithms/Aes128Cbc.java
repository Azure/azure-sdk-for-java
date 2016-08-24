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

public class Aes128Cbc extends AesCbc {

    public static final String AlgorithmName = "A128CBC";
    
    static final int KeySizeInBytes = 128 >> 3;

    public Aes128Cbc() {
        super(AlgorithmName);
    }
    
    @Override
    public ICryptoTransform CreateEncryptor(byte[] key, byte[] iv, byte[] authenticationData) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {

    	if (key == null || key.length < KeySizeInBytes) {
    		throw new InvalidKeyException("key must be at least 128 bits in length");
    	}
    	
        return new AesCbcEncryptor(ByteExtensions.take(key, KeySizeInBytes), iv, null);
    }

    @Override
    public ICryptoTransform CreateEncryptor(byte[] key, byte[] iv, byte[] authenticationData, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {

    	if (key == null || key.length < KeySizeInBytes) {
    		throw new InvalidKeyException("key must be at least 128 bits in length");
    	}
    	
        return new AesCbcEncryptor(ByteExtensions.take(key, KeySizeInBytes), iv, provider);
    }

    @Override
    public ICryptoTransform CreateDecryptor(byte[] key, byte[] iv, byte[] authenticationData) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {

    	if (key == null || key.length < KeySizeInBytes) {
    		throw new InvalidKeyException("key must be at least 128 bits in length");
    	}
    	
        return new AesCbcDecryptor(ByteExtensions.take(key, KeySizeInBytes), iv, null);
    }

    @Override
    public ICryptoTransform CreateDecryptor(byte[] key, byte[] iv, byte[] authenticationData, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {

    	if (key == null || key.length < KeySizeInBytes) {
    		throw new InvalidKeyException("key must be at least 128 bits in length");
    	}
    	
        return new AesCbcDecryptor(ByteExtensions.take(key, KeySizeInBytes), iv, provider);
    }
}
