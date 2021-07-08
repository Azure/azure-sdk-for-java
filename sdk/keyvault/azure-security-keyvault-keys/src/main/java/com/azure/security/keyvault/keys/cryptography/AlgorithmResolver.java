// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class AlgorithmResolver {

    static final AlgorithmResolver DEFAULT = new AlgorithmResolver();

    static {
        DEFAULT.put(Aes128Cbc.ALGORITHM_NAME, new Aes128Cbc());
        DEFAULT.put(Aes192Cbc.ALGORITHM_NAME, new Aes192Cbc());
        DEFAULT.put(Aes256Cbc.ALGORITHM_NAME, new Aes256Cbc());

        DEFAULT.put(Aes128CbcPad.ALGORITHM_NAME, new Aes128CbcPad());
        DEFAULT.put(Aes192CbcPad.ALGORITHM_NAME, new Aes192CbcPad());
        DEFAULT.put(Aes256CbcPad.ALGORITHM_NAME, new Aes256CbcPad());

        DEFAULT.put(Aes128CbcHmacSha256.ALGORITHM_NAME, new Aes128CbcHmacSha256());
        DEFAULT.put(Aes192CbcHmacSha384.ALGORITHM_NAME, new Aes192CbcHmacSha384());
        DEFAULT.put(Aes256CbcHmacSha512.ALGORITHM_NAME, new Aes256CbcHmacSha512());

        DEFAULT.put(Aes128Kw.ALGORITHM_NAME, new Aes128Kw());
        DEFAULT.put(Aes192Kw.ALGORITHM_NAME, new Aes192Kw());
        DEFAULT.put(Aes256Kw.ALGORITHM_NAME, new Aes256Kw());

        DEFAULT.put(Rsa15.ALGORITHM_NAME, new Rsa15());
        DEFAULT.put(RsaOaep.ALGORITHM_NAME, new RsaOaep());

        DEFAULT.put(Es256k.ALGORITHM_NAME, new Es256k());
        DEFAULT.put(Es256.ALGORITHM_NAME, new Es256());
        DEFAULT.put(Es384.ALGORITHM_NAME, new Es384());
        DEFAULT.put(Es512.ALGORITHM_NAME, new Es512());
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
