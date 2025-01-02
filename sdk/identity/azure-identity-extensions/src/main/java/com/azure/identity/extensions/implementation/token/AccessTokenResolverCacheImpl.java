// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.extensions.implementation.token;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.identity.extensions.implementation.utils.StringUtils.getAccessTokenCacheKey;

/**
 * Cache implementation of AccessTokenResolver that takes a {@link TokenCredential} as input
 * and outputs a publisher that emits a single access token.
 */
public class AccessTokenResolverCacheImpl implements AccessTokenResolver {

    private static final ClientLogger LOGGER = new ClientLogger(AccessTokenResolverCacheImpl.class);

    private static final ConcurrentHashMap<String, AccessToken> CACHE = new ConcurrentHashMap<>();

    private final AccessTokenResolverOptions options;

    AccessTokenResolverCacheImpl() {
        this.options = new AccessTokenResolverOptions();
    }

    public AccessTokenResolverCacheImpl(AccessTokenResolverOptions options) {
        this.options = options;
    }

    /**
     * Get a Publisher that emits a single access token and cache it.
     * @param tokenCredential A Microsoft Entra credential that acquires a token.
     * @return A Publisher that emits a single access token.
     */
    @Override
    public Mono<AccessToken> apply(TokenCredential tokenCredential) {
        Objects.requireNonNull(tokenCredential);

        String accessTokenCacheKey = getAccessTokenCacheKey(this.options);
        AccessToken accessToken = CACHE.get(accessTokenCacheKey);
        if (accessToken != null) {
            if (!accessToken.isExpired()) {
                LOGGER.verbose("Returning access token from cache.");
                return Mono.just(accessToken);
            } else {
                CACHE.remove(accessTokenCacheKey);
            }
        }

        TokenRequestContext request = new TokenRequestContext();
        request.setTenantId(this.options.getTenantId());
        request.setClaims(this.options.getClaims());
        request.addScopes(this.options.getScopes());
        return tokenCredential.getToken(request).doOnSuccess(token -> {
            CACHE.put(accessTokenCacheKey, token);
            LOGGER.verbose("The access token cached.");
        });
    }
}
