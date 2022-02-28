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
public enum KeyEncryptionAlgorithm {
    /**
     * RSA public key cryptography algorithm with Optimal Asymmetric Encryption Padding (OAEP) padding.
     */
    RSA_OAEP("RSA_OAEP");

    private final String keyEncryptionKeyAlgorithmName;
    private static final Map<String, KeyEncryptionAlgorithm> ENUM_MAP;

    KeyEncryptionAlgorithm(String keyEncryptionKeyAlgorithmName) {
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
        Map<String, KeyEncryptionAlgorithm> map = new ConcurrentHashMap<>();
        for (KeyEncryptionAlgorithm instance : KeyEncryptionAlgorithm.values()) {
            map.put(instance.getName(), instance);
        }
        ENUM_MAP = Collections.unmodifiableMap(map);
    }

    /**
     *  Gets the KeyEncryptionAlgorithm enum back from the string value
     * @param name the string value
     * @return KeyEncryptionAlgorithm enum
     */
    public static KeyEncryptionAlgorithm get(String name) {
        return ENUM_MAP.get(name);
    }
}
