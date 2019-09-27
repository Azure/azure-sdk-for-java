// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.cryptography;

/**
 * An object capable of synchronously retrieving key encryption keys from a provided key identifier.
 */
public interface EncryptionKeyResolver {

    /**
     * Retrieves the {@link EncryptionKey} corresponding to the specified {@code keyId}
     *
     * @param keyId The key identifier of the key encryption key to retrieve
     * @return The encryption key corresponding to the specified {@code keyId}
     */
    EncryptionKey resolveKey(String keyId);
}
