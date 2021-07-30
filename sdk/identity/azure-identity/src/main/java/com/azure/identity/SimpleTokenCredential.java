// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientBuilder;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.util.LoggingUtil;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

/**
 * A credential provider that provides token credential based on the access token specified at credential construction.
 */
@Immutable
public class SimpleTokenCredential implements TokenCredential {
    private final AccessToken accessToken;

    /**
     * Creates a SimpleTokenCredential using specified access token and expiry time.
     * @param accessToken the user specified access token.
     */
    SimpleTokenCredential(String accessTokenStr, AccessToken accessToken) {
        this.accessToken = accessTokenStr == null ? accessToken
            : new AccessToken(accessTokenStr, OffsetDateTime.now().plusMinutes(10));
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return Mono.just(accessToken);
    }
}
