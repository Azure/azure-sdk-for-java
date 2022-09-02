// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.implementation.token;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Implementation of AccessTokenResolver that takes a {@link TokenCredential} as input
 * and outputs a publisher that emits a single access token.
 */
public class AccessTokenResolverImpl implements AccessTokenResolver {

    private final AccessTokenResolverOptions options;

    AccessTokenResolverImpl() {
        this.options = new AccessTokenResolverOptions();
    }

    public AccessTokenResolverImpl(AccessTokenResolverOptions options) {
        this.options = options;
    }

    /**
     * Get a Publisher that emits a single access token.
     * @param tokenCredential An AAD credential that acquires a token.
     * @return A Publisher that emits a single access token.
     */
    @Override
    public Mono<AccessToken> apply(TokenCredential tokenCredential) {
        Objects.requireNonNull(tokenCredential);
        TokenRequestContext request = new TokenRequestContext();
        request.setTenantId(this.options.getTenantId());
        request.setClaims(this.options.getClaims());
        request.addScopes(this.options.getScopes());
        return tokenCredential.getToken(request);
    }
}
