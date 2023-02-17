// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation.authentication;

import com.azure.core.credential.AccessToken;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.core.publisher.Sinks;

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
public class AccessTokenCacheImpl {
    // The delay after a refresh to attempt another token refresh
    private static final Duration REFRESH_DELAY = Duration.ofSeconds(30);
    // the offset before token expiry to attempt proactive token refresh
    private static final Duration REFRESH_OFFSET = Duration.ofMinutes(5);
    private volatile AccessToken cache;
    private volatile OffsetDateTime nextTokenRefresh = OffsetDateTime.now();
    private final AtomicReference<Sinks.One<AccessToken>> wip;
    private final ContainerRegistryRefreshTokenCredential tokenCredential;
    private ContainerRegistryTokenRequestContext tokenRequestContext;
    private final Predicate<AccessToken> shouldRefresh;
    private final ClientLogger logger = new ClientLogger(AccessTokenCacheImpl.class);

    /**
     * Creates an instance of AccessTokenCacheImpl with default scheme "Bearer".
     *
     * @param tokenCredential the credential to be used to acquire token from.
     */
    public AccessTokenCacheImpl(ContainerRegistryRefreshTokenCredential tokenCredential) {
        Objects.requireNonNull(tokenCredential, "The token credential cannot be null");
        this.wip = new AtomicReference<>();
        this.tokenCredential = tokenCredential;
        this.shouldRefresh = accessToken -> OffsetDateTime.now()
            .isAfter(accessToken.getExpiresAt().minus(REFRESH_OFFSET));
    }

    /**
     * Asynchronously get a token from either the cache or replenish the cache with a new token.
     *
     * @param tokenRequestContext The request context for token acquisition.
     * @return The Publisher that emits an AccessToken
     */
    public Mono<AccessToken> getToken(ContainerRegistryTokenRequestContext tokenRequestContext) {
        return Mono.defer(retrieveToken(tokenRequestContext))
            // Keep resubscribing as long as Mono.defer [token acquisition] emits empty().
            .repeatWhenEmpty((Flux<Long> longFlux) -> longFlux.concatMap(ignored -> Flux.just(true)));
    }

    private Supplier<Mono<? extends AccessToken>> retrieveToken(ContainerRegistryTokenRequestContext tokenRequestContext) {
        return () -> {
            try {
                if (wip.compareAndSet(null, Sinks.one())) {
                    final Sinks.One<AccessToken> sinksOne = wip.get();
                    OffsetDateTime now = OffsetDateTime.now();
                    Mono<AccessToken> tokenRefresh;
                    Mono<AccessToken> fallback;

                    Supplier<Mono<AccessToken>> tokenSupplier = () ->
                        tokenCredential.getToken(this.tokenRequestContext);

                    boolean forceRefresh = checkIfWeShouldForceRefresh(tokenRequestContext);

                    if (forceRefresh) {
                        this.tokenRequestContext = tokenRequestContext;
                        tokenRefresh = Mono.defer(() -> tokenCredential.getToken(this.tokenRequestContext));
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
                        .flatMap(processTokenRefreshResult(sinksOne, now, fallback))
                        .doOnError(sinksOne::tryEmitError)
                        .doFinally(ignored -> wip.set(null));
                } else {
                    return Mono.empty();
                }
            } catch (Throwable t) {
                return Mono.error(t);
            }
        };
    }

    private boolean checkIfWeShouldForceRefresh(ContainerRegistryTokenRequestContext tokenRequestContext) {
        return !(this.tokenRequestContext != null
            && (this.tokenRequestContext.getServiceName() == null ? tokenRequestContext.getServiceName() == null
            : this.tokenRequestContext.getServiceName().equals(tokenRequestContext.getServiceName())));
    }

    private Function<Signal<AccessToken>, Mono<? extends AccessToken>> processTokenRefreshResult(
        Sinks.One<AccessToken> sinksOne, OffsetDateTime now, Mono<AccessToken> fallback) {
        return signal -> {
            AccessToken accessToken = signal.get();
            Throwable error = signal.getThrowable();
            if (signal.isOnNext() && accessToken != null) { // SUCCESS
                logger.info(refreshLog(cache, now, "Acquired a new access token"));
                cache = accessToken;
                sinksOne.tryEmitValue(accessToken);
                nextTokenRefresh = OffsetDateTime.now().plus(REFRESH_DELAY);
                return Mono.just(accessToken);
            } else if (signal.isOnError() && error != null) { // ERROR
                logger.error(refreshLog(cache, now, "Failed to acquire a new access token"));
                nextTokenRefresh = OffsetDateTime.now().plus(REFRESH_DELAY);
                return fallback.switchIfEmpty(Mono.error(error));
            } else { // NO REFRESH
                sinksOne.tryEmitEmpty();
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
