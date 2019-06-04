// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.identity.AccessToken;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.FluxSink.OverflowStrategy;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * The base class for all credentials that intends to refresh tokens after expiration.
 */
public class RefreshableTokenCache {
    private static final int REFRESH_TIMEOUT_SECONDS = 30;

    private final Map<String, AccessToken> cache;
    private final Map<String, AtomicBoolean> wips;
    private final EmitterProcessor<String> emitterProcessor = EmitterProcessor.create(false);
    private final FluxSink<String> sink = emitterProcessor.sink(OverflowStrategy.BUFFER);
    private final Function<String, Mono<AccessToken>> authenticate;

    /**
     * Creates an instance of RefreshableTokenCredential with default scheme "Bearer".
     * @param authenticate the function to asynchronously acquire an authentication result with a given resource
     */
    public RefreshableTokenCache(Function<String, Mono<AccessToken>> authenticate){
        cache = new ConcurrentHashMap<>();
        wips = new ConcurrentHashMap<>();
        this.authenticate = authenticate;
    }

    /**
     * Asynchronously refreshes an authentication result with a given resource and a
     * previous authentication result. This is useful for OAuth refresh tokens.
     *
     * @param expiredAccessToken the expired AccessToken object
     * @param resource the AAD resource to acquire token for.
     * @return a Publisher that emits a single Authentication result, or an error if failed
     */
    protected Mono<AccessToken> refresh(AccessToken expiredAccessToken, String resource) {
        return authenticate.apply(resource);
    }

    /**
     * Asynchronously get a token from either the cache or replenish the cache with a new token.
     * @param resource the resource to get token for
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> getToken(String resource) {
        if (isCached(resource)) {
            return Mono.just(cache.get(resource));
        } else {
            return Mono.fromCallable(() -> {
                // initialize the wip for this resource
                synchronized (wips) {
                    if (!wips.containsKey(resource)) {
                        wips.put(resource, new AtomicBoolean(false));
                    }
                    return wips.get(resource);
                }
            }).flatMap(wip -> {
                Mono<AccessToken> receiver = emitterProcessor.filter(s -> s.equals(resource)).next()
                    .timeout(Duration.ofSeconds(REFRESH_TIMEOUT_SECONDS))
                    .map(cache::get);

                if (!wip.getAndSet(true)) {
                    Mono<AccessToken> acquirer = Mono.empty();
                    if (cache.containsKey(resource)) {
                        acquirer = acquirer.switchIfEmpty(refresh(cache.get(resource), resource));
                    }
                    acquirer = acquirer.onErrorResume(t -> Mono.empty())
                        .switchIfEmpty(authenticate.apply(resource))
                        .doOnNext(val -> {
                            cache.put(resource, val);
                            // notify the receivers
                            sink.next(resource);
                            wip.set(false);
                        });
                    // Acquirer will emit the token, but we want to subscribe to it too
                    // with a receiver in case there's no other thread waiting, leaving
                    // the event in the buffer. The receiver needs to be listening first
                    // before the acquirer emits a result, hence Flux.merge().
                    return Flux.merge(receiver, acquirer).last();
                } else {
                    return receiver;
                }
            });
        }
    }

    private boolean isCached(String resource) {
        return cache.containsKey(resource) && !cache.get(resource).isExpired();
    }
}
