// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography.implementation;

import java.util.HashMap;
import java.util.Map;

final class AlgorithmResolver {

    static final AlgorithmResolver DEFAULT;

    static {
        Map<String, Algorithm> algorithms = new HashMap<>();
        algorithms.put(Aes128Cbc.ALGORITHM_NAME, new Aes128Cbc());
        algorithms.put(Aes192Cbc.ALGORITHM_NAME, new Aes192Cbc());
        algorithms.put(Aes256Cbc.ALGORITHM_NAME, new Aes256Cbc());

        algorithms.put(Aes128CbcPad.ALGORITHM_NAME, new Aes128CbcPad());
        algorithms.put(Aes192CbcPad.ALGORITHM_NAME, new Aes192CbcPad());
        algorithms.put(Aes256CbcPad.ALGORITHM_NAME, new Aes256CbcPad());

        algorithms.put(Aes128CbcHmacSha256.ALGORITHM_NAME, new Aes128CbcHmacSha256());
        algorithms.put(Aes192CbcHmacSha384.ALGORITHM_NAME, new Aes192CbcHmacSha384());
        algorithms.put(Aes256CbcHmacSha512.ALGORITHM_NAME, new Aes256CbcHmacSha512());

        algorithms.put(Aes128Kw.ALGORITHM_NAME, new Aes128Kw());
        algorithms.put(Aes192Kw.ALGORITHM_NAME, new Aes192Kw());
        algorithms.put(Aes256Kw.ALGORITHM_NAME, new Aes256Kw());

        algorithms.put(Rsa15.ALGORITHM_NAME, new Rsa15());
        algorithms.put(RsaOaep.ALGORITHM_NAME, new RsaOaep());

        algorithms.put(Es256k.ALGORITHM_NAME, new Es256k());
        algorithms.put(Es256.ALGORITHM_NAME, new Es256());
        algorithms.put(Es384.ALGORITHM_NAME, new Es384());
        algorithms.put(Es512.ALGORITHM_NAME, new Es512());

        DEFAULT = new AlgorithmResolver(algorithms);
    }

    private final Map<String, Algorithm> algorithms;

    private AlgorithmResolver(Map<String, Algorithm> algorithms) {
        this.algorithms = algorithms;
    }

    /**
     * Returns the implementation for an algorithm name.
     *
     * @param algorithmName The algorithm name.
     * @return The implementation for the algorithm or null.
     */
    public Algorithm get(String algorithmName) {
        return algorithms.get(algorithmName);
    }
}
