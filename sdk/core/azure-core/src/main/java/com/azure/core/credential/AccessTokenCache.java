// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import com.azure.core.implementation.AccessTokenCacheInfo;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.core.util.logging.LoggingEventBuilder;
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
 * <p>
 * {@code AccessTokenCache} is a thread-safe token cache that wraps a {@link TokenCredential} and manages proactive
 * token refresh. It supports both asynchronous and synchronous token retrieval via
 * {@link #getToken(TokenRequestContext, boolean)} and {@link #getTokenSync(TokenRequestContext, boolean)}.
 * </p>
 *
 * <p>
 * The cache maintains a single cached {@link AccessToken} per instance and proactively refreshes it before expiry
 * (by default 5 minutes before the expiry time, or at the {@code refreshAt} time if provided by the credential).
 * If a refresh fails while a non-expired token is still available, the cached token continues to be returned until
 * it expires.
 * </p>
 *
 * <p>
 * When the {@code refreshOnContextChange} flag is {@code true}, the cache compares the incoming
 * {@link TokenRequestContext} (scopes, tenant ID, claims) against the context used to acquire the current cached
 * token. A mismatch causes an immediate token refresh regardless of expiry. This is the mechanism used to support
 * Continuous Access Evaluation (CAE) claims challenges.
 * </p>
 *
 * <p>
 * <strong>Note:</strong> Each instance caches exactly one {@link AccessToken} associated with one
 * {@link TokenRequestContext}. Do not share a single {@code AccessTokenCache} instance across calls that require
 * different scopes or tenants simultaneously, as each new context will evict the previously cached token.
 * </p>
 *
 * <p>
 * This class is thread-safe. Multiple threads may call {@link #getToken(TokenRequestContext, boolean)} and
 * {@link #getTokenSync(TokenRequestContext, boolean)} concurrently.
 * </p>
 *
 * <p>
 * <strong>Sample: Wrapping a TokenCredential with AccessTokenCache</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.credential.accessTokenCache -->
 * <pre>
 * TokenCredential credential = new BasicAuthenticationCredential&#40;&quot;username&quot;, &quot;password&quot;&#41;;
 * AccessTokenCache tokenCache = new AccessTokenCache&#40;credential&#41;;
 * TokenRequestContext requestContext = new TokenRequestContext&#40;&#41;.addScopes&#40;&quot;https:&#47;&#47;management.azure.com&#47;.default&quot;&#41;;
 * &#47;&#47; Async usage
 * Mono&lt;AccessToken&gt; tokenMono = tokenCache.getToken&#40;requestContext, false&#41;;
 * &#47;&#47; Sync usage
 * AccessToken token = tokenCache.getTokenSync&#40;requestContext, false&#41;;
 * </pre>
 * <!-- end com.azure.core.credential.accessTokenCache -->
 *
 * @see TokenCredential
 * @see AccessToken
 * @see TokenRequestContext
 * @see SimpleTokenCache
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
    private final Supplier<Mono<AccessToken>> tokenSupplierAsync;
    private final Supplier<AccessToken> tokenSupplierSync;
    private final Predicate<AccessToken> shouldRefresh;
    // Used for sync flow.
    private final Lock lock;

    /**
     * Creates an instance of {@code AccessTokenCache} that wraps the given {@link TokenCredential}.
     *
     * @param tokenCredential the token credential to be used to acquire the token.
     */
    public AccessTokenCache(TokenCredential tokenCredential) {
        Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null.");
        this.wip = new AtomicReference<>();
        this.tokenCredential = tokenCredential;
        this.cacheInfo = new AtomicReference<>(new AccessTokenCacheInfo(null, OffsetDateTime.now()));
        this.shouldRefresh = accessToken -> OffsetDateTime.now()
            .isAfter(accessToken.getRefreshAt() == null
                ? accessToken.getExpiresAt().minus(REFRESH_OFFSET)
                : accessToken.getRefreshAt());
        this.tokenSupplierAsync = () -> tokenCredential.getToken(this.tokenRequestContext);
        this.tokenSupplierSync = () -> tokenCredential.getTokenSync(this.tokenRequestContext);
        this.lock = new ReentrantLock();
    }

    /**
     * Asynchronously get a token from either the cache or replenish the cache with a new token.
     *
     * @param tokenRequestContext The request context for token acquisition.
     * @param refreshOnContextChange When {@code true}, compares the incoming {@link TokenRequestContext} against the
     *     one used to acquire the current cached token. If the scopes, tenant ID, or claims differ, a fresh token is
     *     fetched immediately regardless of expiry. Pass {@code false} to always use the cached token when it is
     *     still valid.
     * @return a {@link Mono} that emits the cached or newly acquired {@link AccessToken}.
     * @throws IllegalArgumentException if {@code tokenRequestContext} is {@code null}.
     */
    public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext, boolean refreshOnContextChange) {
        return Mono.defer(retrieveToken(tokenRequestContext, refreshOnContextChange))
            // Keep resubscribing as long as Mono.defer [token acquisition] emits empty().
            .repeatWhenEmpty((Flux<Long> longFlux) -> longFlux
                .concatMap(ignored -> Flux.just(true).delayElements(Duration.ofMillis(500))));
    }

    /**
     * Synchronously get a token from either the cache or replenish the cache with a new token.
     *
     * @param tokenRequestContext The request context for token acquisition.
     * @param refreshOnContextChange When {@code true}, compares the incoming {@link TokenRequestContext} against the
     *     one used to acquire the current cached token. If the scopes, tenant ID, or claims differ, a fresh token is
     *     fetched immediately regardless of expiry. Pass {@code false} to always use the cached token when it is
     *     still valid.
     * @return the cached or newly acquired {@link AccessToken}.
     * @throws IllegalArgumentException if {@code tokenRequestContext} is {@code null}.
     */
    public AccessToken getTokenSync(TokenRequestContext tokenRequestContext, boolean refreshOnContextChange) {
        lock.lock();
        try {
            return retrieveTokenSync(tokenRequestContext, refreshOnContextChange).get();
        } finally {
            lock.unlock();
        }
    }

    private Supplier<Mono<? extends AccessToken>> retrieveToken(TokenRequestContext tokenRequestContext,
        boolean refreshOnContextChange) {
        return () -> {
            try {
                if (tokenRequestContext == null) {
                    return Mono.error(LOGGER
                        .logExceptionAsError(new IllegalArgumentException("'tokenRequestContext' cannot be null.")));
                }

                AccessTokenCacheInfo cache = this.cacheInfo.get();
                AccessToken cachedToken = cache.getCachedAccessToken();

                if (wip.compareAndSet(null, Sinks.one())) {
                    final Sinks.One<AccessToken> sinksOne = wip.get();
                    OffsetDateTime now = OffsetDateTime.now();
                    Mono<AccessToken> tokenRefresh;
                    Mono<AccessToken> fallback;

                    // Check if the incoming token request context is different from the cached one. A different
                    // token request context requires fetching a new token as the cached one won't work for the
                    // passed in token request context.
                    boolean forceRefresh = (refreshOnContextChange && checkIfForceRefreshRequired(tokenRequestContext))
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

                    return Mono.using(() -> wip,
                        ignored -> tokenRefresh.materialize()
                            .flatMap(processTokenRefreshResult(sinksOne, now, fallback))
                            .doOnError(sinksOne::tryEmitError),
                        w -> w.set(null));
                } else if (cachedToken != null && !cachedToken.isExpired() && !refreshOnContextChange) {
                    // another thread might be refreshing the token proactively, but the current token is still valid
                    return Mono.just(cachedToken);
                } else {
                    // if a context-change refresh is pending, exit and retry.
                    if (refreshOnContextChange) {
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
        boolean refreshOnContextChange) {
        return () -> {
            if (tokenRequestContext == null) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("'tokenRequestContext' cannot be null."));
            }
            AccessTokenCacheInfo cache = this.cacheInfo.get();
            AccessToken cachedToken = cache.getCachedAccessToken();

            OffsetDateTime now = OffsetDateTime.now();
            Supplier<AccessToken> tokenRefresh;
            AccessToken fallback;

            // Check if the incoming token request context is different from the cached one. A different
            // token request context requires fetching a new token as the cached one won't work for the
            // passed in token request context.
            boolean forceRefresh = (refreshOnContextChange && checkIfForceRefreshRequired(tokenRequestContext))
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
                    buildTokenRefreshLog(LogLevel.VERBOSE, cachedToken, now).log("Acquired a new access token.");
                    OffsetDateTime nextTokenRefreshTime = OffsetDateTime.now().plus(REFRESH_DELAY);
                    AccessTokenCacheInfo updatedInfo = new AccessTokenCacheInfo(token, nextTokenRefreshTime);
                    this.cacheInfo.set(updatedInfo);
                    return token;
                } else {
                    return fallback;
                }
            } catch (Throwable error) {
                buildTokenRefreshLog(LogLevel.ERROR, cachedToken, now).log("Failed to acquire a new access token.",
                    error);
                OffsetDateTime nextTokenRefreshTime = OffsetDateTime.now();
                AccessTokenCacheInfo updatedInfo = new AccessTokenCacheInfo(cachedToken, nextTokenRefreshTime);
                this.cacheInfo.set(updatedInfo);
                if (fallback != null) {
                    return fallback;
                }
                if (error instanceof RuntimeException) {
                    throw LOGGER.logExceptionAsError((RuntimeException) error);
                }
                throw LOGGER.logExceptionAsError(new RuntimeException(error));
            }
        };
    }

    private boolean checkIfForceRefreshRequired(TokenRequestContext tokenRequestContext) {
        return !(this.tokenRequestContext != null
            && (this.tokenRequestContext.getClaims() == null
                ? tokenRequestContext.getClaims() == null
                : (tokenRequestContext.getClaims() != null
                    && tokenRequestContext.getClaims().equals(this.tokenRequestContext.getClaims())))
            && (this.tokenRequestContext.getTenantId() == null
                ? tokenRequestContext.getTenantId() == null
                : (tokenRequestContext.getTenantId() != null
                    && tokenRequestContext.getTenantId().equals(this.tokenRequestContext.getTenantId())))
            && this.tokenRequestContext.getScopes().equals(tokenRequestContext.getScopes()));
    }

    private Function<Signal<AccessToken>, Mono<? extends AccessToken>>
        processTokenRefreshResult(Sinks.One<AccessToken> sinksOne, OffsetDateTime now, Mono<AccessToken> fallback) {
        return signal -> {
            AccessToken accessToken = signal.get();
            Throwable error = signal.getThrowable();
            AccessToken cache = cacheInfo.get().getCachedAccessToken();
            if (signal.isOnNext() && accessToken != null) { // SUCCESS
                buildTokenRefreshLog(LogLevel.VERBOSE, cache, now).log("Acquired a new access token.");
                sinksOne.tryEmitValue(accessToken);
                OffsetDateTime nextTokenRefresh = OffsetDateTime.now().plus(REFRESH_DELAY);
                cacheInfo.set(new AccessTokenCacheInfo(accessToken, nextTokenRefresh));
                return Mono.just(accessToken);
            } else if (signal.isOnError() && error != null) { // ERROR
                buildTokenRefreshLog(LogLevel.ERROR, cache, now).log("Failed to acquire a new access token.", error);
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

        Duration tte = cache.getDurationUntilExpiration();
        return logBuilder.addKeyValue("expiresAt", cache.getExpiresAt())
            .addKeyValue("tteSeconds", String.valueOf(tte.abs().getSeconds()))
            .addKeyValue("retryAfterSeconds", REFRESH_DELAY_STRING)
            .addKeyValue("expired", tte.isNegative());
    }
}
