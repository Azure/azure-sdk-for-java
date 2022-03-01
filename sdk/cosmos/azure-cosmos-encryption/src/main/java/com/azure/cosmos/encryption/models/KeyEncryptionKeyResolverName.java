// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption.models;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Has constants for names of well-known implementations of {@link com.azure.core.cryptography.KeyEncryptionKeyResolver}.
 */
public enum KeyEncryptionKeyResolverName {
     AZURE_KEY_VAULT("AZURE_KEY_VAULT");

    private final String resolverName;
    private static final Map<String, KeyEncryptionKeyResolverName> ENUM_MAP;

    KeyEncryptionKeyResolverName(String resolverName) {
        this.resolverName = resolverName;
    }

    /**
     * Returns the KeyEncryptionKeyResolver name
     * @return resolverName
     */
    @Override
    public String toString() {
        return this.resolverName;
    }

    /**
     * Returns the KeyEncryptionKeyResolver name
     * @return resolverName
     */
    public String getName() {
        return this.resolverName;
    }

    // Build an immutable map of String name to enum pairs.
    // Any Map impl can be used.

    static {
        Map<String, KeyEncryptionKeyResolverName> map = new ConcurrentHashMap<>();
        for (KeyEncryptionKeyResolverName instance : KeyEncryptionKeyResolverName.values()) {
            map.put(instance.getName(), instance);
        }
        ENUM_MAP = Collections.unmodifiableMap(map);
    }

    /**
     *  Gets the keyEncryptionKeyResolverName enum back from the string value
     * @param name the string value
     * @return KeyEncryptionKeyResolverName enum
     */
    public static KeyEncryptionKeyResolverName get(String name) {
        return ENUM_MAP.get(name);
    }
}

