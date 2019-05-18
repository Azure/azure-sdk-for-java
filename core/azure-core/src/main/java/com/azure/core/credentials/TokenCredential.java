// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credentials;

import reactor.core.publisher.Mono;

/**
 * The base class for credentials that can provide a token.
 */
public abstract class TokenCredential {
    private final String scheme;

    /**
     * Creates a token credential with scheme "Bearer";
     */
    protected TokenCredential() {
        this("Bearer");
    }

    /**
     * Creates a token credential with the provided scheme.
     * @param scheme the scheme for the token.
     */
    protected TokenCredential(String scheme) {
        this.scheme = scheme;
    }

    /**
     * @return the scheme of the token
     */
    public String scheme() {
        return scheme;
    }

    /**
     * Asynchronously get a token for a given resource/audience.
     * @param resource the resource/audience the token will be used for
     * @return a Publisher that emits a single token
     */
    public abstract Mono<String> getTokenAsync(String resource);
}
