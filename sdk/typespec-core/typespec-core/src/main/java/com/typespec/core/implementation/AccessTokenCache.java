// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation;

import com.typespec.core.credential.AccessToken;
import com.typespec.core.credential.TokenCredential;
import com.typespec.core.credential.TokenRequestContext;
import com.typespec.core.util.logging.ClientLogger;
import com.typespec.core.util.logging.LogLevel;
import com.typespec.core.util.logging.LoggingEventBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A token cache that supports caching a token and refreshing it.
 */
public final class AccessTokenCache {
    // The delay after a refresh to attempt another token refresh
    private static final Duration REFRESH_DELAY = Duration.ofSeconds(30);
    private static final String REFRESH_DELAY_STRING = String.valueOf(REFRESH_DELAY.getSeconds());

    // the offset before token expiry to attempt proactive token refresh
    private static final Duration REFRESH_OFFSET = Duration.ofMinutes(5);
    // AccessTokenCache is a commonly used class, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(AccessTokenCache.class);
    private final AtomicReference<Sinks.One<AccessToken>> wip;
    private final AtomicReference<AccessTokenCacheInfo> cacheInfo;
    private final TokenCredential tokenCredential;
    // Stores the last authenticated token request context. The cached token is valid under this context.
    private TokenRequestContext tokenRequestContext;
    private Supplier<Mono<AccessToken>> tokenSupplierAsync;
    private Supplier<AccessToken> tokenSupplierSync;
    private final Predicate<AccessToken> shouldRefresh;
    // Used for sync flow.
    private Lock lock;

    /**
     * Creates an instance of RefreshableTokenCredential with default scheme "Bearer".
     *
     */
    public AccessTokenCache(TokenCredential tokenCredential) {
        Objects.requireNonNull(tokenCredential, "The token credential cannot be null");
        this.wip = new AtomicReference<>();
        this.tokenCredential = tokenCredential;
        this.cacheInfo = new AtomicReference<>(new AccessTokenCacheInfo(null, OffsetDateTime.now()));
        this.shouldRefresh = accessToken -> OffsetDateTime.now()
            .isAfter(accessToken.getExpiresAt().minus(REFRESH_OFFSET));
        this.tokenSupplierAsync = () -> tokenCredential.getToken(this.tokenRequestContext);
        this.tokenSupplierSync = () -> tokenCredential.getTokenSync(this.tokenRequestContext);
        this.lock = new ReentrantLock();
    }

    /**
     * Asynchronously get a token from either the cache or replenish the cache with a new token.
     *
     * @param tokenRequestContext The request context for token acquisition.
     * @return The Publisher that emits an AccessToken
     */
    public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext, boolean checkToForceFetchToken) {
        return Mono.defer(retrieveToken(tokenRequestContext, checkToForceFetchToken))
            // Keep resubscribing as long as Mono.defer [token acquisition] emits empty().
            .repeatWhenEmpty((Flux<Long> longFlux) ->
                longFlux.concatMap(ignored -> Flux.just(true).delayElements(Duration.ofMillis(500))));
    }

    /**
     * Synchronously get a token from either the cache or replenish the cache with a new token.
     *
     * @param tokenRequestContext The request context for token acquisition.
     * @return The Publisher that emits an AccessToken
     */
    public AccessToken getTokenSync(TokenRequestContext tokenRequestContext, boolean checkToForceFetchToken) {
        lock.lock();
        try {
            return retrieveTokenSync(tokenRequestContext, checkToForceFetchToken).get();
        } finally {
            lock.unlock();
        }
    }

    private Supplier<Mono<? extends AccessToken>> retrieveToken(TokenRequestContext tokenRequestContext,
                                                                boolean checkToForceFetchToken) {
        return () -> {
            try {
                if (tokenRequestContext == null) {
                    return Mono.error(LOGGER.logExceptionAsError(
                        new IllegalArgumentException("The token request context input cannot be null.")));
                }

                AccessTokenCacheInfo cache = this.cacheInfo.get();
                AccessToken cachedToken = cache.getCachedAccessToken();

                if (wip.compareAndSet(null, Sinks.one())) {
                    final Sinks.One<AccessToken> sinksOne = wip.get();
                    OffsetDateTime now = OffsetDateTime.now();
                    Mono<AccessToken> tokenRefresh;
                    Mono<AccessToken> fallback;

                    // Check if the incoming token request context is different from the cached one. A different
                    // token request context, requires to fetch a new token as the cached one won't work for the
                    // passed in token request context.
                    boolean forceRefresh = (checkToForceFetchToken && checkIfForceRefreshRequired(tokenRequestContext))
                        || this.tokenRequestContext == null;

                    if (forceRefresh) {
                        this.tokenRequestContext = tokenRequestContext;
                        tokenRefresh = Mono.defer(() -> tokenCredential.getToken(this.tokenRequestContext));
                        fallback = Mono.empty();
                    } else if (cachedToken != null && !shouldRefresh.test(cachedToken)) {
                        // fresh cache & no need to refresh
                        tokenRefresh = Mono.empty();
                        fallback = Mono.just(cachedToken);
                    } else if (cachedToken == null || cachedToken.isExpired()) {
                        // no token to use
                        // refresh immediately
                        tokenRefresh = Mono.defer(tokenSupplierAsync);

                        // cache doesn't exist or expired, no fallback
                        fallback = Mono.empty();
                    } else {
                        // token available, but close to expiry
                        if (now.isAfter(cache.getNextTokenRefresh())) {
                            // refresh immediately
                            tokenRefresh = Mono.defer(tokenSupplierAsync);
                        } else {
                            // still in timeout, do not refresh
                            tokenRefresh = Mono.empty();
                        }
                        // cache hasn't expired, ignore refresh error this time
                        fallback = Mono.just(cachedToken);
                    }
                    return tokenRefresh
                        .materialize()
                        .flatMap(processTokenRefreshResult(sinksOne, now, fallback))
                        .doOnError(sinksOne::tryEmitError)
                        .doFinally(ignored -> wip.set(null));
                } else if (cachedToken != null && !cachedToken.isExpired() && !checkToForceFetchToken) {
                    // another thread might be refreshing the token proactively, but the current token is still valid
                    return Mono.just(cachedToken);
                } else {
                    // if a force refresh is possible, then exit and retry.
                    if (checkToForceFetchToken) {
                        return Mono.empty();
                    }
                    // another thread is definitely refreshing the expired token
                    Sinks.One<AccessToken> sinksOne = wip.get();
                    if (sinksOne == null) {
                        // the refreshing thread has finished
                        return Mono.just(cachedToken);
                    } else {
                        // wait for refreshing thread to finish but defer to updated cache in case just missed onNext()
                        return sinksOne.asMono().switchIfEmpty(Mono.fromSupplier(() -> cachedToken));
                    }
                }
            } catch (Exception ex) {
                return Mono.error(ex);
            }
        };
    }

    private Supplier<AccessToken> retrieveTokenSync(TokenRequestContext tokenRequestContext,
                                                                boolean checkToForceFetchToken) {
        return () -> {
            if (tokenRequestContext == null) {
                throw LOGGER.logExceptionAsError(
                    new IllegalArgumentException("The token request context input cannot be null."));
            }
            AccessTokenCacheInfo cache = this.cacheInfo.get();
            AccessToken cachedToken = cache.getCachedAccessToken();

            OffsetDateTime now = OffsetDateTime.now();
            Supplier<AccessToken> tokenRefresh;
            AccessToken fallback;

            // Check if the incoming token request context is different from the cached one. A different
            // token request context, requires to fetch a new token as the cached one won't work for the
            // passed in token request context.
            boolean forceRefresh = (checkToForceFetchToken && checkIfForceRefreshRequired(tokenRequestContext))
                || this.tokenRequestContext == null;

            if (forceRefresh) {
                this.tokenRequestContext = tokenRequestContext;
                tokenRefresh = tokenSupplierSync;
                fallback = null;
            } else if (cachedToken != null && !shouldRefresh.test(cachedToken)) {
                // fresh cache & no need to refresh
                tokenRefresh = null;
                fallback = cachedToken;
            } else if (cachedToken == null || cachedToken.isExpired()) {
                // no token to use
                // refresh immediately
                tokenRefresh = tokenSupplierSync;

                // cache doesn't exist or expired, no fallback
                fallback = null;
            } else {
                // token available, but close to expiry
                if (now.isAfter(cache.getNextTokenRefresh())) {
                    // refresh immediately
                    tokenRefresh = tokenSupplierSync;
                } else {
                    // still in timeout, do not refresh
                    tokenRefresh = null;
                }
                // cache hasn't expired, ignore refresh error this time
                fallback = cachedToken;
            }

            try {
                if (tokenRefresh != null) {
                    AccessToken token = tokenRefresh.get();
                    buildTokenRefreshLog(LogLevel.INFORMATIONAL, cachedToken, now)
                        .log("Acquired a new access token.");
                    OffsetDateTime nextTokenRefreshTime = OffsetDateTime.now().plus(REFRESH_DELAY);
                    AccessTokenCacheInfo updatedInfo = new AccessTokenCacheInfo(token, nextTokenRefreshTime);
                    this.cacheInfo.set(updatedInfo);
                    return token;
                } else {
                    return fallback;
                }
            } catch (Throwable error) {
                buildTokenRefreshLog(LogLevel.ERROR, cachedToken, now)
                    .log("Failed to acquire a new access token.", error);
                OffsetDateTime nextTokenRefreshTime = OffsetDateTime.now();
                AccessTokenCacheInfo updatedInfo = new AccessTokenCacheInfo(cachedToken, nextTokenRefreshTime);
                this.cacheInfo.set(updatedInfo);
                if (fallback != null) {
                    return fallback;
                }
                throw error;
            }
        };
    }

    private boolean checkIfForceRefreshRequired(TokenRequestContext tokenRequestContext) {
        return !(this.tokenRequestContext != null
            && (this.tokenRequestContext.getClaims() == null ? tokenRequestContext.getClaims() == null
            : (tokenRequestContext.getClaims() == null ? false
            : tokenRequestContext.getClaims().equals(this.tokenRequestContext.getClaims())))
            && this.tokenRequestContext.getScopes().equals(tokenRequestContext.getScopes()));
    }

    private Function<Signal<AccessToken>, Mono<? extends AccessToken>> processTokenRefreshResult(
        Sinks.One<AccessToken> sinksOne, OffsetDateTime now, Mono<AccessToken> fallback) {
        return signal -> {
            AccessToken accessToken = signal.get();
            Throwable error = signal.getThrowable();
            AccessToken cache = cacheInfo.get().getCachedAccessToken();
            if (signal.isOnNext() && accessToken != null) { // SUCCESS
                buildTokenRefreshLog(LogLevel.INFORMATIONAL, cache, now)
                    .log("Acquired a new access token.");
                sinksOne.tryEmitValue(accessToken);
                OffsetDateTime nextTokenRefresh = OffsetDateTime.now().plus(REFRESH_DELAY);
                cacheInfo.set(new AccessTokenCacheInfo(accessToken, nextTokenRefresh));
                return Mono.just(accessToken);
            } else if (signal.isOnError() && error != null) { // ERROR
                buildTokenRefreshLog(LogLevel.ERROR, cache, now)
                    .log("Failed to acquire a new access token.", error);
                OffsetDateTime nextTokenRefresh = OffsetDateTime.now();
                cacheInfo.set(new AccessTokenCacheInfo(cache, nextTokenRefresh));
                return fallback.switchIfEmpty(Mono.error(error));
            } else { // NO REFRESH
                sinksOne.tryEmitEmpty();
                return fallback;
            }
        };
    }

    private static LoggingEventBuilder buildTokenRefreshLog(LogLevel level, AccessToken cache, OffsetDateTime now) {
        LoggingEventBuilder logBuilder = LOGGER.atLevel(level);
        if (cache == null || !LOGGER.canLogAtLevel(level)) {
            return logBuilder;
        }

        Duration tte = Duration.between(now, cache.getExpiresAt());
        return logBuilder
            .addKeyValue("expiresAt", cache.getExpiresAt())
            .addKeyValue("tteSeconds", String.valueOf(tte.abs().getSeconds()))
            .addKeyValue("retryAfterSeconds", REFRESH_DELAY_STRING)
            .addKeyValue("expired", tte.isNegative());
    }
}
