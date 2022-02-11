// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption.models;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the encryption algorithms supported for key encryption.
 *
 */
public enum KeyEncryptionKeyAlgorithm {
    /**
     * RSA public key cryptography algorithm with Optimal Asymmetric Encryption Padding (OAEP) padding.
     */
    RSA_OAEP("RSA_OAEP");

    private final String keyEncryptionKeyAlgorithmName;
    private static final Map<String, KeyEncryptionKeyAlgorithm> ENUM_MAP;

    KeyEncryptionKeyAlgorithm(String keyEncryptionKeyAlgorithmName) {
        this.keyEncryptionKeyAlgorithmName = keyEncryptionKeyAlgorithmName;
    }

    /**
     * Returns the keyEncryptionKeyAlgorithm name
     * @return keyEncryptionKeyAlgorithmName
     */
    @Override
    public String toString() {
        return this.keyEncryptionKeyAlgorithmName;
    }

    /**
     * Returns the keyEncryptionKeyAlgorithm name
     * @return keyEncryptionKeyAlgorithmName
     */
    public String getName() {
        return this.keyEncryptionKeyAlgorithmName;
    }

    // Build an immutable map of String name to enum pairs.
    // Any Map impl can be used.

    static {
        Map<String, KeyEncryptionKeyAlgorithm> map = new ConcurrentHashMap<>();
        for (KeyEncryptionKeyAlgorithm instance : KeyEncryptionKeyAlgorithm.values()) {
            map.put(instance.getName(), instance);
        }
        ENUM_MAP = Collections.unmodifiableMap(map);
    }

    /**
     *  Gets the KeyEncryptionKeyAlgorithm enum back from the string value
     * @param name the string value
     * @return KeyEncryptionKeyAlgorithm enum
     */
    public static KeyEncryptionKeyAlgorithm get(String name) {
        return ENUM_MAP.get(name);
    }
}
