// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.cryptography;

import reactor.core.publisher.Mono;

/**
 * An object capable of asynchronously retrieving key encryption keys from a provided key identifier.
 */
public interface AsyncEncryptionKeyResolver {

    /**
     * Retrieves the {@link AsyncEncryptionKey} corresponding to the specified {@code keyId}
     *
     * @param keyId The key identifier of the key encryption key to retrieve
     * @return The encryption key corresponding to the specified {@code keyId}
     */
    Mono<AsyncEncryptionKey> resolveKey(String keyId);
}
