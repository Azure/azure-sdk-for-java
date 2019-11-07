// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class AlgorithmResolver {

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

        Default.put(Es256k.ALGORITHM_NAME, new Es256k());
        Default.put(Es256.ALGORITHM_NAME, new Es256());
        Default.put(Es384.ALGORITHM_NAME, new Es384());
        Default.put(Es512.ALGORITHM_NAME, new Es512());
    }

    private final Map<String, Algorithm> algorithms = new ConcurrentHashMap<>();

    /**
     * Returns the implementation for an algorithm name.
     *
     * @param algorithmName The algorithm name.
     * @return The implementation for the algorithm or null.
     */
    public Algorithm get(String algorithmName) {
        return algorithms.get(algorithmName);
    }

    /**
     * Add/Update a named algorithm implementation.
     *
     * @param algorithmName The algorithm name.
     * @param provider The implementation of the algorithm.
     */
    public void put(String algorithmName, Algorithm provider) {
        algorithms.put(algorithmName, provider);
    }

    /**
     * Remove a named algorithm implementation.
     *
     * @param algorithmName The algorithm name
     */
    public void remove(String algorithmName) {
        algorithms.remove(algorithmName);
    }
}
