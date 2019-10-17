// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Deque;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * A token credential provider that can provide a credential from a list of providers.
 *
 * <p><strong>Sample: Construct a ChainedTokenCredential with silent username+password login tried first, then
 * interactive browser login as needed (e.g. when 2FA is turned on in the directory).</strong></p>
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
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        AtomicReference<Throwable> cause = new AtomicReference<>();
        return Flux.fromIterable(credentials)
            .flatMap(p -> p.getToken(request).onErrorResume(t -> {
                if (cause.get() != null) {
                    t.initCause(cause.get());
                }
                cause.set(t);
                return Mono.empty();
            }))
            .next()
            .switchIfEmpty(Mono.defer(() -> Mono.error(new RuntimeException("Tried "
                + credentials.stream().map(c -> c.getClass().getSimpleName()).collect(Collectors.joining(", "))
                + " but failed to acquire a token for any of them. Please verify the environment for either of them"
                + " and see more details in the causes below.", cause.get()))));
    }
}
