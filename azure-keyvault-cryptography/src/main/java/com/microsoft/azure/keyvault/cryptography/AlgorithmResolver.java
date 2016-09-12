/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault.cryptography;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.microsoft.azure.keyvault.cryptography.algorithms.Aes128Cbc;
import com.microsoft.azure.keyvault.cryptography.algorithms.Aes128CbcHmacSha256;
import com.microsoft.azure.keyvault.cryptography.algorithms.Aes192Cbc;
import com.microsoft.azure.keyvault.cryptography.algorithms.Aes192CbcHmacSha384;
import com.microsoft.azure.keyvault.cryptography.algorithms.Aes256Cbc;
import com.microsoft.azure.keyvault.cryptography.algorithms.Aes256CbcHmacSha512;
import com.microsoft.azure.keyvault.cryptography.algorithms.AesKw128;
import com.microsoft.azure.keyvault.cryptography.algorithms.AesKw192;
import com.microsoft.azure.keyvault.cryptography.algorithms.AesKw256;
import com.microsoft.azure.keyvault.cryptography.algorithms.Rs256;
import com.microsoft.azure.keyvault.cryptography.algorithms.Rsa15;
import com.microsoft.azure.keyvault.cryptography.algorithms.RsaOaep;

public class AlgorithmResolver {

    public static final AlgorithmResolver Default = new AlgorithmResolver();

    static {
        Default.put(Aes128CbcHmacSha256.ALGORITHM_NAME, new Aes128CbcHmacSha256());
        Default.put(Aes192CbcHmacSha384.ALGORITHM_NAME, new Aes192CbcHmacSha384());
        Default.put(Aes256CbcHmacSha512.ALGORITHM_NAME, new Aes256CbcHmacSha512());

        Default.put(Aes128Cbc.ALGORITHM_NAME, new Aes128Cbc());
        Default.put(Aes192Cbc.ALGORITHM_NAME, new Aes192Cbc());
        Default.put(Aes256Cbc.ALGORITHM_NAME, new Aes256Cbc());

        Default.put(AesKw128.ALGORITHM_NAME, new AesKw128());
        Default.put(AesKw192.ALGORITHM_NAME, new AesKw192());
        Default.put(AesKw256.ALGORITHM_NAME, new AesKw256());

        Default.put(Rsa15.ALGORITHM_NAME, new Rsa15());
        Default.put(RsaOaep.ALGORITHM_NAME, new RsaOaep());

        Default.put( Rs256.ALGORITHM_NAME, new Rs256() );
        // Default.put( RsNull.ALGORITHM_NAME, new RsNull() );
    }

    private final ConcurrentMap<String, Algorithm> _algorithms = new ConcurrentHashMap<String, Algorithm>();

    /**
     * Returns the implementation for an algorithm name.
     * 
     * @param algorithmName The algorithm name.
     * @return The implementation for the algorithm or null.
     */
    public Algorithm get(String algorithmName) {
        return _algorithms.get(algorithmName);
    }

    /**
     * Add/Update a named algorithm implementation.
     * 
     * @param algorithmName The algorithm name.
     * @param provider The implementation of the algorithm.
     */
    public void put(String algorithmName, Algorithm provider) {
        _algorithms.put(algorithmName, provider);
    }

    /**
     * Remove a named algorithm implementation.
     * 
     * @param algorithmName The algorithm name
     */
    public void remove(String algorithmName) {
        _algorithms.remove(algorithmName);
    }
}
