// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A token cache that supports caching a token and refreshing it.
 */
public class SimpleTokenCache {
    // The delay after a refresh to attempt another token refresh
    private static final Duration REFRESH_DELAY = Duration.ofSeconds(30);
    private static final String REFRESH_DELAY_STRING = String.valueOf(REFRESH_DELAY.getSeconds());
    // the offset before token expiry to attempt proactive token refresh
    private static final Duration REFRESH_OFFSET = Duration.ofMinutes(5);
    // SimpleTokenCache is commonly used, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(SimpleTokenCache.class);

    private static final String NO_CACHE_ACQUIRED = "Acquired a new access token.";
    private static final String NO_CACHE_FAILED = "Failed to acquire a new access token.";

    private static final String NEGATIVE_TTE = " seconds after expiry. Retry may be attempted after "
        + REFRESH_DELAY_STRING
        + " seconds.";
    private static final String POSITIVE_TTE = " seconds before expiry. Retry may be attempted after "
        + REFRESH_DELAY_STRING
        + " seconds. The token currently cached will be used.";

    private final AtomicReference<Sinks.One<AccessToken>> wip;
    private volatile AccessToken cache;
    private volatile OffsetDateTime nextTokenRefresh = OffsetDateTime.now();
    private final Supplier<Mono<AccessToken>> tokenSupplier;
    private final Predicate<AccessToken> shouldRefresh;

    /**
     * Creates an instance of RefreshableTokenCredential with default scheme "Bearer".
     *
     * @param tokenSupplier a method to get a new token
     */
    public SimpleTokenCache(Supplier<Mono<AccessToken>> tokenSupplier) {
        this.wip = new AtomicReference<>();
        this.tokenSupplier = tokenSupplier;
        this.shouldRefresh = accessToken -> OffsetDateTime
            .now()
            .isAfter(accessToken.getExpiresAt().minus(REFRESH_OFFSET));
    }

    /**
     * Asynchronously get a token from either the cache or replenish the cache with a new token.
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> getToken() {
        return Mono.defer(() -> {
            try {
                if (wip.compareAndSet(null, Sinks.one())) {
                    final Sinks.One<AccessToken> sinksOne = wip.get();
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
                            tokenRefresh = Mono
                                .defer(tokenSupplier)
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
                    return tokenRefresh.materialize().flatMap(signal -> {
                        AccessToken accessToken = signal.get();
                        Throwable error = signal.getThrowable();
                        if (signal.isOnNext() && accessToken != null) { // SUCCESS
                            LOGGER
                                .log(LogLevel.INFORMATIONAL, () -> refreshLog(cache, now, "Acquired a new access token",
                                    true));
                            cache = accessToken;
                            sinksOne.tryEmitValue(accessToken);
                            nextTokenRefresh = OffsetDateTime.now().plus(REFRESH_DELAY);
                            return Mono.just(accessToken);
                        } else if (signal.isOnError() && error != null) { // ERROR
                            LOGGER
                                .log(LogLevel.ERROR, () -> refreshLog(cache, now,
                                    "Failed to acquire a new access token", false));
                            nextTokenRefresh = OffsetDateTime.now().plus(REFRESH_DELAY);
                            return fallback.switchIfEmpty(Mono.error(() -> error));
                        } else { // NO REFRESH
                            sinksOne.tryEmitEmpty();
                            return fallback;
                        }
                    }).doOnError(sinksOne::tryEmitError).doFinally(ignored -> wip.set(null));
                } else if (cache != null && !cache.isExpired()) {
                    // another thread might be refreshing the token proactively, but the current token is still valid
                    return Mono.just(cache);
                } else {
                    // another thread is definitely refreshing the expired token
                    Sinks.One<AccessToken> sinksOne = wip.get();
                    if (sinksOne == null) {
                        // the refreshing thread has finished
                        return Mono.just(cache);
                    } else {
                        // wait for refreshing thread to finish but defer to updated cache in case just missed onNext()
                        return sinksOne.asMono().switchIfEmpty(Mono.fromSupplier(() -> cache));
                    }
                }
            } catch (Exception t) {
                return Mono.error(t);
            }
        });
    }

    Sinks.One<AccessToken> getWipValue() {
        return wip.get();
    }

    private static String refreshLog(AccessToken cache, OffsetDateTime now, String log, boolean acquired) {
        if (cache == null) {
            return acquired ? NO_CACHE_ACQUIRED : NO_CACHE_FAILED;
        }

        Duration tte = Duration.between(now, cache.getExpiresAt());

        return log + " at " + tte.abs().getSeconds() + (tte.isNegative() ? NEGATIVE_TTE : POSITIVE_TTE);
    }
}
