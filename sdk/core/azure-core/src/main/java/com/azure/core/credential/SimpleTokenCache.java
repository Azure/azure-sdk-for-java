// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import reactor.core.publisher.FluxSink;
import reactor.core.publisher.FluxSink.OverflowStrategy;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * A token cache that supports caching a token and refreshing it.
 */
public class SimpleTokenCache {
    private static final int REFRESH_TIMEOUT_SECONDS = 30;

    private final AtomicBoolean wip;
    private AccessToken cache;
    private final ReplayProcessor<AccessToken> emitterProcessor = ReplayProcessor.create(1);
    private final FluxSink<AccessToken> sink = emitterProcessor.sink(OverflowStrategy.BUFFER);
    private final Supplier<Mono<AccessToken>> tokenSupplier;

    /**
     * Creates an instance of RefreshableTokenCredential with default scheme "Bearer".
     *
     * @param tokenSupplier a method to get a new token
     */
    public SimpleTokenCache(Supplier<Mono<AccessToken>> tokenSupplier) {
        this.wip = new AtomicBoolean(false);
        this.tokenSupplier = tokenSupplier;
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
                return tokenSupplier.get().doOnNext(ac -> cache = ac)
                    .doOnNext(sink::next)
                    .doOnError(sink::error)
                    .doOnTerminate(() -> wip.set(false));
            } else {
                return emitterProcessor.next();
            }
        });
    }
}
