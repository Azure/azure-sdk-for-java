// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.FluxSink.OverflowStrategy;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A token cache that supports caching a token and refreshing it.
 */
public class SimpleTokenCache {
    private static final Duration REFRESH_TIMEOUT = Duration.ofSeconds(30);

    private final AtomicBoolean wip;
    private volatile AccessToken cache;
    private volatile OffsetDateTime nextTokenRefresh = OffsetDateTime.now();
    private final ReplayProcessor<AccessToken> emitterProcessor = ReplayProcessor.create(1);
    private final FluxSink<AccessToken> sink = emitterProcessor.sink(OverflowStrategy.BUFFER);
    private final Supplier<Mono<AccessToken>> tokenSupplier;
    private final Predicate<AccessToken> shouldRefresh;
    private final ClientLogger logger = new ClientLogger(SimpleTokenCache.class);

    /**
     * Creates an instance of RefreshableTokenCredential with default scheme "Bearer".
     *
     * @param tokenSupplier a method to get a new token
     */
    public SimpleTokenCache(Supplier<Mono<AccessToken>> tokenSupplier) {
        this(tokenSupplier,
            t -> OffsetDateTime.now().isAfter(t.getExpiresAt().minus(TokenCredential.DEFAULT_TOKEN_REFRESH_OFFSET)));
    }

    /**
     * Creates an instance of RefreshableTokenCredential with default scheme "Bearer".
     *
     * @param tokenSupplier a method to get a new token
     * @param shouldRefresh a method to check if the cached token is expired
     */
    public SimpleTokenCache(Supplier<Mono<AccessToken>> tokenSupplier, Predicate<AccessToken> shouldRefresh) {
        this.wip = new AtomicBoolean(false);
        this.tokenSupplier = tokenSupplier;
        this.shouldRefresh = shouldRefresh;
    }

    /**
     * Asynchronously get a token from either the cache or replenish the cache with a new token.
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> getToken() {
        try {
            if (!wip.getAndSet(true)) {
                OffsetDateTime now = OffsetDateTime.now();
                Mono<AccessToken> tokenRefresh;
                Mono<AccessToken> fallback;
                if (cache != null && !shouldRefresh.test(cache)) {
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
                        tokenRefresh = Mono.delay(Duration.between(now, nextTokenRefresh))
                            .then(Mono.defer(tokenSupplier));
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
                    .doOnNext(accessToken -> {
                        logger.info(refreshLog(cache, now, "Acquired a new access token"));
                        sink.next(accessToken);
                        cache = accessToken;
                        nextTokenRefresh = OffsetDateTime.now().plus(REFRESH_TIMEOUT);
                    })
                    .onErrorResume(err -> {
                        logger.error(refreshLog(cache, now, "Failed to acquire a new access token"));
                        nextTokenRefresh = OffsetDateTime.now().plus(REFRESH_TIMEOUT);
                        return fallback.switchIfEmpty(Mono.error(err));
                    })
                    .switchIfEmpty(fallback)
                    .doOnError(sink::error)
                    .doOnTerminate(() -> {
                        wip.set(false);
                    });
            } else {
                return emitterProcessor.next().filter(t -> !t.isExpired());
            }
        } catch (Throwable t) {
            return Mono.error(t);
        }
    }

    private String refreshLog(AccessToken cache, OffsetDateTime now, String log) {
        StringBuilder info = new StringBuilder(log);
        if (cache == null) {
            info.append(".");
        } else {
            Duration tte = Duration.between(now, cache.getExpiresAt());
            info.append(" at ").append(tte.abs().getSeconds()).append(" seconds ")
                .append(tte.isNegative() ? "after" : "before").append(" expiry. ")
                .append("Retry may be attempted after ").append(REFRESH_TIMEOUT.getSeconds()).append(" seconds.");
            if (!tte.isNegative()) {
                info.append(" The token currently cached will be used.");
            }
        }
        return info.toString();
    }
}
