// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.implementation;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * A token cache that supports caching a token and refreshing it.
 */
class ScopeTokenCache {
    private final AtomicBoolean wip;
    private AccessToken cache;
    private final ReplayProcessor<AccessToken> emitterProcessor = ReplayProcessor.create(1);
    private final FluxSink<AccessToken> sink = emitterProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
    private final Function<TokenRequestContext, Mono<AccessToken>> getNew;
    private TokenRequestContext request;


    /**
     * Creates an instance of RefreshableTokenCredential with default scheme "Bearer".
     *
     * @param getNew a method to get a new token
     */
    ScopeTokenCache(Function<TokenRequestContext, Mono<AccessToken>> getNew) {
        this.wip = new AtomicBoolean(false);
        this.getNew = getNew;
    }

    public void setTokenRequest(TokenRequestContext request) {
        this.request = request;
    }

    /**
     * Asynchronously get a token from either the cache or replenish the cache with a new token.
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> getToken() {
        if (cache != null && !cache.isExpired()) {
            return Mono.just(cache);
        }
        return Mono.defer(() -> {
            if (!wip.getAndSet(true)) {
                return getNew.apply(request).doOnNext(ac -> cache = ac)
                        .doOnNext(sink::next)
                        .doOnError(sink::error)
                        .doOnTerminate(() -> wip.set(false));
            } else {
                return emitterProcessor.next();
            }
        });
    }
}
