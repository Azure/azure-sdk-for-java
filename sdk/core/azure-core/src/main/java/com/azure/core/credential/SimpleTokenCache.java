// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A token cache that supports caching a token and refreshing it.
 */
public class SimpleTokenCache {
    private static final Duration REFRESH_TIMEOUT = Duration.ofSeconds(30);
    private final AtomicReference<MonoProcessor<AccessToken>> wip;
    private volatile AccessToken cache;
    private volatile OffsetDateTime nextTokenRefresh = OffsetDateTime.now();
    private final Supplier<Mono<AccessToken>> tokenSupplier;
    private final Predicate<AccessToken> shouldRefresh;
    private final ClientLogger logger = new ClientLogger(SimpleTokenCache.class);

    /**
     * Creates an instance of RefreshableTokenCredential with default scheme "Bearer".
     *
     * @param tokenSupplier a method to get a new token
     */
    public SimpleTokenCache(Supplier<Mono<AccessToken>> tokenSupplier) {
        this(tokenSupplier, new TokenRefreshOptions());
    }

    /**
     * Creates an instance of RefreshableTokenCredential with default scheme "Bearer".
     *
     * @param tokenSupplier a method to get a new token
     * @param tokenRefreshOptions the options to configure the token refresh behavior
     */
    public SimpleTokenCache(Supplier<Mono<AccessToken>> tokenSupplier, TokenRefreshOptions tokenRefreshOptions) {
        this.wip = new AtomicReference<>();
        this.tokenSupplier = tokenSupplier;
        this.shouldRefresh = accessToken -> OffsetDateTime.now().isAfter(accessToken.getExpiresAt()
            .minus(tokenRefreshOptions.getOffset()));
    }

    /**
     * Asynchronously get a token from either the cache or replenish the cache with a new token.
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> getToken() {
        return Mono.defer(() -> {
            try {
                if (wip.compareAndSet(null, MonoProcessor.create())) {
                    final MonoProcessor<AccessToken> monoProcessor = wip.get();
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
                        .flatMap(signal -> {
                            AccessToken accessToken = signal.get();
                            Throwable error = signal.getThrowable();
                            if (signal.isOnNext() && accessToken != null) { // SUCCESS
                                logger.info(refreshLog(cache, now, "Acquired a new access token"));
                                cache = accessToken;
                                monoProcessor.onNext(accessToken);
                                monoProcessor.onComplete();
                                nextTokenRefresh = OffsetDateTime.now().plus(REFRESH_TIMEOUT);
                                return Mono.just(accessToken);
                            } else if (signal.isOnError() && error != null) { // ERROR
                                logger.error(refreshLog(cache, now, "Failed to acquire a new access token"));
                                nextTokenRefresh = OffsetDateTime.now().plus(REFRESH_TIMEOUT);
                                return fallback.switchIfEmpty(Mono.error(error));
                            } else { // NO REFRESH
                                monoProcessor.onComplete();
                                return fallback;
                            }
                        })
                        .doOnError(monoProcessor::onError)
                        .doOnTerminate(() -> wip.set(null));
                } else if (cache != null && !cache.isExpired()) {
                    // another thread might be refreshing the token proactively, but the current token is still valid
                    return Mono.just(cache);
                } else {
                    // another thread is definitely refreshing the expired token
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
        });
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
