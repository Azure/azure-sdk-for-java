// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credentials;

import reactor.core.publisher.Mono;

/**
 * The base class for credentials that can provide a token.
 */
@FunctionalInterface
public interface TokenCredential {
    /**
     * Asynchronously get a token for a given resource/audience.
     * @param scopes the scopes the token will be used for
     * @return a Publisher that emits a single token
     */
    Mono<String> getToken(String... scopes);
}
