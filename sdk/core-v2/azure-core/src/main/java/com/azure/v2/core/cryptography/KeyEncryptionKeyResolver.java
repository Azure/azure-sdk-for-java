// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.cryptography;

/**
 * Resolves key encryption keys from their identifiers.
 */
@FunctionalInterface
public interface KeyEncryptionKeyResolver {
    /**
     * Resolves the key encryption key identified by {@code keyId}.
     *
     * @param keyId The key identifier.
     * @return The resolved key encryption key.
     */
    KeyEncryptionKey buildKeyEncryptionKey(String keyId);
}
