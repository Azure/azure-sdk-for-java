// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.cosmos.kafka.connect.CosmosAccessToken;
import com.azure.cosmos.kafka.connect.CosmosAccessTokenProvider;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Adapts a {@link CosmosAccessTokenProvider} to a {@link TokenCredential} for the Cosmos client.
 */
public class CosmosAccessTokenProviderCredential implements TokenCredential, AutoCloseable {
    private final CosmosAccessTokenProvider provider;

    public CosmosAccessTokenProviderCredential(CosmosAccessTokenProvider provider) {
        this.provider = checkNotNull(provider, "Argument 'provider' must not be null");
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        // Providers may do blocking I/O (for example, reading a token from a secret store),
        // so do not run them on the Cosmos client's event loop threads.
        return Mono.fromCallable(() -> getTokenSync(request))
            .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public AccessToken getTokenSync(TokenRequestContext request) {
        CosmosAccessToken token = provider.getAccessToken(request.getScopes());
        if (token == null) {
            throw new IllegalStateException(
                "Access token provider '" + provider.getClass().getName() + "' returned a null token");
        }

        return new AccessToken(token.getToken(), token.getExpiresAt());
    }

    @Override
    public void close() {
        provider.close();
    }
}
