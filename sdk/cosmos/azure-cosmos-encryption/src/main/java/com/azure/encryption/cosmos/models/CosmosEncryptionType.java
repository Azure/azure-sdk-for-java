// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.encryption.cosmos.models;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Algorithms for use with client-side encryption support in Azure Cosmos DB.
 */
public enum CosmosEncryptionType {
    /**
     * Randomized encryption uses a method that encrypts data in a less predictable manner. Randomized encryption is
     * more secure.
     */
    RANDOMIZED("Randomized"),

    /**
     * Deterministic encryption always generates the same encrypted value for any given plain text value.
     */
    DETERMINISTIC("Deterministic");

    private final String encryptionType;
    private static final Map<String, CosmosEncryptionType> ENUM_MAP;

    CosmosEncryptionType(String encryptionType) {
        this.encryptionType = encryptionType;
    }

    @Override
    public String toString() {
        return this.encryptionType;
    }

    public String getName() {
        return this.encryptionType;
    }

    // Build an immutable map of String name to enum pairs.
    // Any Map impl can be used.

    static {
        Map<String, CosmosEncryptionType> map = new ConcurrentHashMap<>();
        for (CosmosEncryptionType instance : CosmosEncryptionType.values()) {
            map.put(instance.getName(), instance);
        }
        ENUM_MAP = Collections.unmodifiableMap(map);
    }

    public static CosmosEncryptionType get(String name) {
        return ENUM_MAP.get(name);
    }
}
