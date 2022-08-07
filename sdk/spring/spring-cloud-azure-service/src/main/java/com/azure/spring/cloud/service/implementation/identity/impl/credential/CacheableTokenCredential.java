// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.impl.credential;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.spring.cloud.service.implementation.identity.api.Cache;
import com.azure.spring.cloud.service.implementation.identity.impl.credential.adapter.CacheKeyDescriptor;
import reactor.core.publisher.Mono;

/**
 * TokenCredential that can cache AccessToken.
 */
public class CacheableTokenCredential implements TokenCredential {

    private final TokenCredential delegate;
    private final Cache<String, AccessToken> cache;

    public CacheableTokenCredential(Cache<String, AccessToken> cache,
                                    TokenCredential tokenCredential) {
        this.delegate = tokenCredential;
        this.cache = cache;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        if (delegate instanceof CacheKeyDescriptor) {
            String cacheKey = ((CacheKeyDescriptor<String, TokenRequestContext>) delegate).getCacheKey(request);

            return Mono.defer(() -> {
                AccessToken accessToken = cache.get(cacheKey);
                if (accessToken != null && !accessToken.isExpired()) {
                    return Mono.just(accessToken);
                } else {
                    return Mono.empty();
                }
            }).switchIfEmpty(Mono.defer(() -> this.delegate.getToken(request))
                    .doOnNext(token -> cache.put(cacheKey, token)));
        } else {
            return this.delegate.getToken(request);
        }
    }

}
