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
 * <pre>
 * UsernamePasswordCredential usernamePasswordCredential = new UsernamePasswordCredentialBuilder&#40;&#41;
 *     .clientId&#40;clientId&#41;
 *     .username&#40;username&#41;
 *     .password&#40;password&#41;
 *     .build&#40;&#41;;
 * InteractiveBrowserCredential interactiveBrowserCredential = new InteractiveBrowserCredentialBuilder&#40;&#41;
 *     .clientId&#40;clientId&#41;
 *     .port&#40;8765&#41;
 *     .build&#40;&#41;;
 * ChainedTokenCredential credential = new ChainedTokenCredentialBuilder&#40;&#41;
 *     .addLast&#40;usernamePasswordCredential&#41;
 *     .addLast&#40;interactiveBrowserCredential&#41;
 *     .build&#40;&#41;;
 * </pre>
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
            }), 1)
            .next()
            .switchIfEmpty(Mono.defer(() -> Mono.error(new RuntimeException("Tried "
                + credentials.stream().map(c -> c.getClass().getSimpleName()).collect(Collectors.joining(", "))
                + " but failed to acquire a token for any of them. Please verify the environment for either of them"
                + " and see more details in the causes below.", cause.get()))));
    }
}
