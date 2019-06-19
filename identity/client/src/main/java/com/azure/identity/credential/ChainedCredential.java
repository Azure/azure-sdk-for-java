// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.credentials.AccessToken;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.exception.ClientAuthenticationException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A token credential provider that can provide a credential from a list of providers.
 */
public class ChainedCredential implements TokenCredential {
    private final Deque<TokenCredential> credentials;

    /**
     * Create an instance of chained token credential that aggregates a list of token
     * credentials.
     */
    public ChainedCredential() {
        credentials = new ArrayDeque<>();
    }

    /**
     * Adds a credential to try to authenticate at the front of the chain.
     * @param credential the credential to be added to the front of chain
     * @return the ChainedCredential itself
     */
    public ChainedCredential addFirst(TokenCredential credential) {
        credentials.addFirst(credential);
        return this;
    }

    /**
     * Adds a credential to try to authenticate at the last of the chain.
     * @param credential the credential to be added to the end of chain
     * @return the ChainedCredential itself
     */
    public ChainedCredential addLast(TokenCredential credential) {
        credentials.addLast(credential);
        return this;
    }

    @Override
    public Mono<AccessToken> getToken(String... scopes) {
        return Flux.fromIterable(credentials)
            .flatMap(p -> p.getToken(scopes))
            .onErrorResume(t -> Mono.empty())
            .next()
            .switchIfEmpty(Mono.error(new ClientAuthenticationException("No credential can provide a token in the chain", null)));
    }
}
