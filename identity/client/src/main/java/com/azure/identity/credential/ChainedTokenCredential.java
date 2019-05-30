package com.azure.identity.credential;

import com.azure.core.credentials.TokenCredential;
import com.azure.core.exception.ClientAuthenticationException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A token credential provider that can provide a credential from a list of providers.
 */
public class ChainedTokenCredential extends TokenCredential {
    private final Deque<TokenCredential> credentials;

    /**
     * Create an instance of chained token credential that aggregates a list of token
     * credentials.
     */
    public ChainedTokenCredential() {
        credentials = new ArrayDeque<>();
    }

    public ChainedTokenCredential addFirst(TokenCredential credential) {
        credentials.addFirst(credential);
        return this;
    }

    public ChainedTokenCredential addLast(TokenCredential credential) {
        credentials.addLast(credential);
        return this;
    }

    @Override
    public Mono<String> getTokenAsync(String resource) {
        return Flux.fromIterable(credentials)
            .flatMap(p -> p.getTokenAsync(resource))
            .next()
            .switchIfEmpty(Mono.error(new ClientAuthenticationException("No credential can provide a token in the chain", null)));
    }
}
