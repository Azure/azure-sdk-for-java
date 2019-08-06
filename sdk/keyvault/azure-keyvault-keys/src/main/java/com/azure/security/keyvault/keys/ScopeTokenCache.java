// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.credentials.AccessToken;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * A token cache that supports caching a token and refreshing it.
 */
class ScopeTokenCache {
    private static final int REFRESH_TIMEOUT_SECONDS = 30;

    private final AtomicBoolean wip;
    private AccessToken cache;
    private final ReplayProcessor<AccessToken> emitterProcessor = ReplayProcessor.create(1);
    private final FluxSink<AccessToken> sink = emitterProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
    private final Function<String[], Mono<AccessToken>> getNew;
    private String[] scopes;

    /**
     * Creates an instance of RefreshableTokenCredential with default scheme "Bearer".
     *
     * @param getNew a method to get a new token
     */
    ScopeTokenCache(Function<String[], Mono<AccessToken>> getNew) {
        this.wip = new AtomicBoolean(false);
        this.getNew = getNew;
    }

    public void scopes(String... scopes) {
        this.scopes = scopes;
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
                return getNew.apply(scopes).doOnNext(ac -> cache = ac)
                        .doOnNext(sink::next)
                        .doOnError(sink::error)
                        .doOnTerminate(() -> wip.set(false));
            } else {
                return emitterProcessor.next();
            }
        });
    }
}
