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
    private final String UnavailableError = this.getClass().getSimpleName() + " authentication failed. ---> ";
    private final String FailedError = this.getClass().getSimpleName()
            + " failed to retrieve a token from the included credentials.(";

    /**
     * Create an instance of chained token credential that aggregates a list of token
     * credentials.
     */
    ChainedTokenCredential(Deque<TokenCredential> credentials) {
        this.credentials = credentials;
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        final StringBuilder errorMsg = new StringBuilder();
        return Flux.fromIterable(credentials)
            .flatMap(p -> p.getToken(request).onErrorResume(t -> {
                if (t.getMessage() != null && !t.getMessage().contains("authentication unavailable")) {
                    throw new RuntimeException(
                            UnavailableError + p.getClass().getSimpleName() + " authentication failed.", t);
                }
                errorMsg.append(" ").append(t.getMessage());
                return Mono.empty();
            }), 1)
            .next()
            .switchIfEmpty(Mono.defer(() -> Mono.error(new RuntimeException(
                        FailedError + errorMsg.toString() + " )"))));
    }
}
