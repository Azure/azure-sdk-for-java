// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption.models;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Algorithms for use with client-side encryption support in Azure Cosmos DB.
 *
 */
public enum CosmosEncryptionAlgorithm {
    /**
     * Authenticated Encryption algorithm based on https://tools.ietf.org/html/draft-mcgrew-aead-aes-cbc-hmac-sha2-05
     */
    AEAD_AES_256_CBC_HMAC_SHA256("AEAD_AES_256_CBC_HMAC_SHA256");
    private final String algorithm;
    private static final Map<String, CosmosEncryptionAlgorithm> ENUM_MAP;

    CosmosEncryptionAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Returns the name of algorithm
     * @return algorithm name
     */
    @Override
    public String toString() {
        return this.algorithm;
    }

    /**
     * Returns the name of algorithm
     * @return algorithm name
     */
    public String getName() {
        return this.algorithm;
    }

    // Build an immutable map of String name to enum pairs.
    // Any Map impl can be used.

    static {
        Map<String, CosmosEncryptionAlgorithm> map = new ConcurrentHashMap<>();
        for (CosmosEncryptionAlgorithm instance : CosmosEncryptionAlgorithm.values()) {
            map.put(instance.getName(), instance);
        }
        ENUM_MAP = Collections.unmodifiableMap(map);
    }

    /**
     *  Gets the CosmosEncryptionAlgorithm enum back from the string value
     * @param name the string value
     * @return CosmosEncryptionAlgorithm enum
     */
    public static CosmosEncryptionAlgorithm get(String name) {
        return ENUM_MAP.get(name);
    }
}
