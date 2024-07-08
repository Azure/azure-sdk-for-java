// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.cryptography;

/**
 * An object capable of synchronously retrieving key encryption keys from a provided key identifier.
 */
public interface KeyEncryptionKeyResolver {

    /**
     * Retrieves the {@link KeyEncryptionKey} corresponding to the specified {@code keyId}
     *
     * @param keyId The key identifier of the key encryption key to retrieve
     * @return The key encryption key corresponding to the specified {@code keyId}
     */
    KeyEncryptionKey buildKeyEncryptionKey(String keyId);
}
