// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.credentials.AccessToken;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.implementation.annotation.Immutable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Deque;

/**
 * A token credential provider that can provide a credential from a list of providers.
 *
 * <p><strong>Sample: Construct a ChainedTokenCredential with silent username+password login tried first, then interactive browser login as needed (e.g. when 2FA is turned on in the directory).</strong></p>
 * {@codesnippet com.azure.identity.credential.chainedtokencredential.construct}
 */
@Immutable
public class ChainedTokenCredential implements TokenCredential {
    private final Deque<TokenCredential> credentials;

    /**
     * Create an instance of chained token credential that aggregates a list of token
     * credentials.
     */
    ChainedTokenCredential(Deque<TokenCredential> credentials) {
        this.credentials = credentials;
    }

    @Override
    public Mono<AccessToken> getToken(String... scopes) {
        return Flux.fromIterable(credentials)
            .flatMap(p -> p.getToken(scopes).onErrorResume(t -> Mono.empty()))
            .next()
            .switchIfEmpty(Mono.error(new ClientAuthenticationException("No credential can provide a token in the chain", null)));
    }
}
