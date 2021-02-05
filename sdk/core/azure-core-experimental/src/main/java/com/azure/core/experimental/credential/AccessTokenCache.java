// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.credential;

import com.azure.core.credential.AccessToken;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.core.publisher.Signal;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A token cache that supports caching a token and refreshing it.
 */
public class AccessTokenCache {
    // The delay after a refresh to attempt another token refresh
    private static final Duration REFRESH_DELAY = Duration.ofSeconds(30);
    // the offset before token expiry to attempt proactive token refresh
    private static final Duration REFRESH_OFFSET = Duration.ofMinutes(5);
    private volatile AccessToken cache;
    private volatile OffsetDateTime nextTokenRefresh = OffsetDateTime.now();
    private final AtomicReference<MonoProcessor<AccessToken>> wip;
    private final Supplier<Mono<AccessToken>> tokenSupplier;
    private final Predicate<AccessToken> shouldRefresh;
    private final ClientLogger logger = new ClientLogger(AccessTokenCache.class);

    /**
     * Creates an instance of AccessTokenCache with default scheme "Bearer".
     *
     * @param tokenSupplier a method to get a new token
     */
    public AccessTokenCache(Supplier<Mono<AccessToken>> tokenSupplier) {
        Objects.requireNonNull(tokenSupplier, "The token supplier cannot be null");
        this.wip = new AtomicReference<>();
        this.tokenSupplier = tokenSupplier;
        this.shouldRefresh = accessToken -> OffsetDateTime.now()
            .isAfter(accessToken.getExpiresAt().minus(REFRESH_OFFSET));
    }

    /**
     * Asynchronously get a token from either the cache or replenish the cache with a new token.
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> getToken() {
        return getToken(this.tokenSupplier, false);
    }

    /**
     * Asynchronously get a token from either the cache or replenish the cache with a new token.
     *
     * @param tokenSupplier The method to get a new token
     * @param forceRefresh The flag indicating if the cache needs to be skipped and a token needs to be fetched via the
     * credential.
     * @return The Publisher that emits an AccessToken
     */
    public Mono<AccessToken> getToken(Supplier<Mono<AccessToken>> tokenSupplier, boolean forceRefresh) {
        return Mono.defer(retrieveToken(tokenSupplier, forceRefresh))
            // Keep resubscribing as long as Mono.defer [token acquisition] emits empty().
            .repeatWhenEmpty((Flux<Long> longFlux) -> longFlux.concatMap(ignored -> Flux.just(true)));
    }

    private Supplier<Mono<? extends AccessToken>> retrieveToken(Supplier<Mono<AccessToken>> tokenSupplier,
                                                                boolean forceRefresh) {
        return () -> {
            try {
                if (wip.compareAndSet(null, MonoProcessor.create())) {
                    final MonoProcessor<AccessToken> monoProcessor = wip.get();
                    OffsetDateTime now = OffsetDateTime.now();
                    Mono<AccessToken> tokenRefresh;
                    Mono<AccessToken> fallback;
                    if (forceRefresh) {
                        tokenRefresh = Mono.defer(tokenSupplier);
                        fallback = Mono.empty();
                    } else if (cache != null && !shouldRefresh.test(cache)) {
                        // fresh cache & no need to refresh
                        tokenRefresh = Mono.empty();
                        fallback = Mono.just(cache);
                    } else if (cache == null || cache.isExpired()) {
                        // no token to use
                        if (now.isAfter(nextTokenRefresh)) {
                            // refresh immediately
                            tokenRefresh = Mono.defer(tokenSupplier);
                        } else {
                            // wait for timeout, then refresh
                            tokenRefresh = Mono.defer(tokenSupplier)
                                               .delaySubscription(Duration.between(now, nextTokenRefresh));
                        }
                        // cache doesn't exist or expired, no fallback
                        fallback = Mono.empty();
                    } else {
                        // token available, but close to expiry
                        if (now.isAfter(nextTokenRefresh)) {
                            // refresh immediately
                            tokenRefresh = Mono.defer(tokenSupplier);
                        } else {
                            // still in timeout, do not refresh
                            tokenRefresh = Mono.empty();
                        }
                        // cache hasn't expired, ignore refresh error this time
                        fallback = Mono.just(cache);
                    }
                    return tokenRefresh
                       .materialize()
                       .flatMap(processTokenRefreshResult(monoProcessor, now, fallback))
                       .doOnError(monoProcessor::onError)
                       .doFinally(ignored -> wip.set(null));
                } else if (cache != null && !cache.isExpired() && !forceRefresh) {
                    // another thread might be refreshing the token proactively, but the current token is still valid
                    return Mono.just(cache);
                } else {
                    // another thread is definitely refreshing the expired token
                    //If this thread, needs to force refresh, then it needs to resubscribe.
                    if (forceRefresh) {
                        return Mono.empty();
                    }
                    MonoProcessor<AccessToken> monoProcessor = wip.get();
                    if (monoProcessor == null) {
                        // the refreshing thread has finished
                        return Mono.just(cache);
                    } else {
                        // wait for refreshing thread to finish but defer to updated cache in case just missed onNext()
                        return monoProcessor.switchIfEmpty(Mono.defer(() -> Mono.just(cache)));
                    }
                }
            } catch (Throwable t) {
                return Mono.error(t);
            }
        };
    }

    private Function<Signal<AccessToken>, Mono<? extends AccessToken>> processTokenRefreshResult(
        MonoProcessor<AccessToken> monoProcessor, OffsetDateTime now, Mono<AccessToken> fallback) {
        return signal -> {
            AccessToken accessToken = signal.get();
            Throwable error = signal.getThrowable();
            if (signal.isOnNext() && accessToken != null) { // SUCCESS
                logger.info(refreshLog(cache, now, "Acquired a new access token"));
                cache = accessToken;
                monoProcessor.onNext(accessToken);
                monoProcessor.onComplete();
                nextTokenRefresh = OffsetDateTime.now().plus(REFRESH_DELAY);
                return Mono.just(accessToken);
            } else if (signal.isOnError() && error != null) { // ERROR
                logger.error(refreshLog(cache, now, "Failed to acquire a new access token"));
                nextTokenRefresh = OffsetDateTime.now().plus(REFRESH_DELAY);
                return fallback.switchIfEmpty(Mono.error(error));
            } else { // NO REFRESH
                monoProcessor.onComplete();
                return fallback;
            }
        };
    }

    private static String refreshLog(AccessToken cache, OffsetDateTime now, String log) {
        StringBuilder info = new StringBuilder(log);
        if (cache == null) {
            info.append(".");
        } else {
            Duration tte = Duration.between(now, cache.getExpiresAt());
            info.append(" at ").append(tte.abs().getSeconds()).append(" seconds ")
                .append(tte.isNegative() ? "after" : "before").append(" expiry. ")
                .append("Retry may be attempted after ").append(REFRESH_DELAY.getSeconds()).append(" seconds.");
            if (!tte.isNegative()) {
                info.append(" The token currently cached will be used.");
            }
        }
        return info.toString();
    }
}
