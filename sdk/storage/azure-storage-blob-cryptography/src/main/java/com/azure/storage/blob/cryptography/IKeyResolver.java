// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.cryptography;

import reactor.core.publisher.Mono;

public interface IKeyResolver {
    /**
     * Retrieves an IKey implementation for the specified key identifier.
     * Implementations should check the format of the kid to ensure that it is
     * recognized. Null, rather than an exception, should be returned for
     * unrecognized key identifiers to enable chaining of key resolvers.
     *
     * @param kid The key identifier to resolve.
     * @return A ListenableFuture containing the resolved IKey
     */

    Mono<IKey> resolveKeyAsync(String kid);
}
