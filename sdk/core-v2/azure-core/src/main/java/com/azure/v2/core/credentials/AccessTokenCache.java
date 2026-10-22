// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.credentials;

import com.azure.v2.core.implementation.AccessTokenCacheInfo;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.logging.LogLevel;
import io.clientcore.core.instrumentation.logging.LoggingEvent;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>
 * {@code AccessTokenCache} is a thread-safe token cache that wraps a {@link TokenCredential} and manages proactive
 * token refresh. Tokens are retrieved synchronously via {@link #getToken(TokenRequestContext, boolean)}.
 * </p>
 *
 * <p>
 * The cache maintains a single cached {@link AccessToken} per instance and proactively refreshes it before expiry
 * (by default five minutes before the expiry time, or at the {@code refreshAt} time provided by the credential).
 * If a refresh fails while a non-expired token is still available, the cached token continues to be returned until
 * it expires.
 * </p>
 *
 * <p>
 * When the {@code refreshOnContextChange} flag is {@code true}, the cache compares the incoming
 * {@link TokenRequestContext} (scopes, tenant ID, and claims) against the context used to acquire the current cached
 * token. A mismatch causes an immediate refresh without falling back to the previously cached token. This supports
 * Continuous Access Evaluation (CAE) claims challenges.
 * </p>
 *
 * <p>
 * <strong>Note:</strong> Each instance caches exactly one {@link AccessToken} associated with one
 * {@link TokenRequestContext}. Do not share a single {@code AccessTokenCache} instance across calls that require
 * different scopes or tenants simultaneously, as each new context will evict the previously cached token.
 * </p>
 *
 * <p><strong>Sample: Caching tokens from an existing TokenCredential</strong></p>
 *
 * <!-- src_embed com.azure.v2.core.credentials.accessTokenCache -->
 * <pre>
 * AccessTokenCache tokenCache = new AccessTokenCache&#40;credential&#41;;
 * TokenRequestContext requestContext = new TokenRequestContext&#40;&#41;.addScopes&#40;&quot;https:&#47;&#47;management.azure.com&#47;.default&quot;&#41;;
 * AccessToken token = tokenCache.getToken&#40;requestContext, false&#41;;
 * </pre>
 * <!-- end com.azure.v2.core.credentials.accessTokenCache -->
 *
 * @see TokenCredential
 * @see AccessToken
 * @see TokenRequestContext
 */
public final class AccessTokenCache {
    // The delay after a refresh to attempt another token refresh
    private static final Duration REFRESH_DELAY = Duration.ofSeconds(30);
    private static final String REFRESH_DELAY_STRING = String.valueOf(REFRESH_DELAY.getSeconds());

    // the offset before token expiry to attempt proactive token refresh
    private static final Duration REFRESH_OFFSET = Duration.ofMinutes(5);
    // AccessTokenCache is a commonly used class, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(AccessTokenCache.class);
    private volatile AccessTokenCacheInfo cacheInfo;
    private final TokenCredential tokenCredential;
    // Stores the last authenticated token request context. The cached token is valid under this context.
    private TokenRequestContext tokenRequestContext;

    // Used for sync flow.
    private final Lock lock;

    /**
     * Creates an instance of {@code AccessTokenCache} that wraps the given {@link TokenCredential}.
     *
     * @param tokenCredential the token credential to be used to acquire the token.
     * @throws NullPointerException if {@code tokenCredential} is {@code null}.
     */
    public AccessTokenCache(TokenCredential tokenCredential) {
        Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null.");
        this.tokenCredential = tokenCredential;
        this.cacheInfo = new AccessTokenCacheInfo(null, OffsetDateTime.now());
        this.lock = new ReentrantLock();
    }

    private boolean checkIfRefreshIsNeeded(AccessToken accessToken) {
        return OffsetDateTime.now()
            .isAfter(accessToken.getRefreshAt() == null
                ? accessToken.getExpiresAt().minus(REFRESH_OFFSET)
                : accessToken.getRefreshAt());
    }

    /**
     * Synchronously gets a token from either the cache or replenishes the cache with a new token.
     *
     * @param tokenRequestContext The request context for token acquisition.
     * @param refreshOnContextChange When {@code true}, a change in scopes, tenant ID, or claims causes an immediate token
     * refresh regardless of expiry. When {@code false}, the cached token's context is retained for subsequent refreshes.
     * @return The cached or newly acquired access token.
     * @throws IllegalArgumentException if {@code tokenRequestContext} is {@code null}.
     * @throws RuntimeException if token acquisition fails and no cached token can be used.
     */
    public AccessToken getToken(TokenRequestContext tokenRequestContext, boolean refreshOnContextChange) {
        lock.lock();
        try {
            return retrieveToken(tokenRequestContext, refreshOnContextChange);
        } finally {
            lock.unlock();
        }
    }

    private AccessToken retrieveToken(TokenRequestContext tokenRequestContext, boolean refreshOnContextChange) {
        if (tokenRequestContext == null) {
            throw LOGGER.throwableAtError().log("'tokenRequestContext' cannot be null.", IllegalArgumentException::new);
        }

        AccessTokenCacheInfo cache = this.cacheInfo;
        AccessToken cachedToken = cache.getCachedAccessToken();
        OffsetDateTime now = OffsetDateTime.now();
        AccessToken fallback;

        boolean forceRefresh = (refreshOnContextChange && checkIfForceRefreshRequired(tokenRequestContext))
            || this.tokenRequestContext == null;
        if (forceRefresh) {
            this.tokenRequestContext = tokenRequestContext;
            fallback = null;
        } else if (cachedToken != null && !checkIfRefreshIsNeeded(cachedToken)) {
            return cachedToken;
        } else if (cachedToken == null || cachedToken.isExpired()) {
            fallback = null;
        } else {
            if (!now.isAfter(cache.getNextTokenRefreshAt())) {
                return cachedToken;
            }
            fallback = cachedToken;
        }

        return attemptTokenRefresh(cachedToken, fallback);
    }

    private AccessToken attemptTokenRefresh(AccessToken cachedToken, AccessToken fallback) {
        try {
            AccessToken newToken = this.tokenCredential.getToken(this.tokenRequestContext);
            buildTokenRefreshLog(LogLevel.VERBOSE, cachedToken).log("Acquired a new access token.");

            this.cacheInfo = new AccessTokenCacheInfo(newToken, OffsetDateTime.now().plus(REFRESH_DELAY));
            return newToken;

        } catch (Throwable error) {
            buildTokenRefreshLog(LogLevel.ERROR, cachedToken).setThrowable(error)
                .log("Failed to acquire a new access token.");
            this.cacheInfo = new AccessTokenCacheInfo(cachedToken, OffsetDateTime.now());

            if (fallback != null) {
                return fallback;
            }
            if (error instanceof RuntimeException) {
                throw (RuntimeException) error;
            }
            throw LOGGER.throwableAtError().log(error, RuntimeException::new);
        }
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

    private static LoggingEvent buildTokenRefreshLog(LogLevel level, AccessToken cache) {
        LoggingEvent logBuilder = LOGGER.atLevel(level);
        if (cache == null || !LOGGER.canLogAtLevel(level)) {
            return logBuilder;
        }

        Duration tte = getDurationUntilExpiration(cache);

        return logBuilder.addKeyValue("expiresAt", cache.getExpiresAt())
            .addKeyValue("tteSeconds", String.valueOf(tte.abs().getSeconds()))
            .addKeyValue("retryAfterSeconds", REFRESH_DELAY_STRING)
            .addKeyValue("expired", tte.isNegative());
    }

    /**
     * Gets the {@link Duration} until the {@link AccessToken} expires.
     * <p>
     * The {@link Duration} is based on the {@link OffsetDateTime#now() current time} and may return a negative
     * {@link Duration}, indicating that the {@link AccessToken} has expired.
     *
     * @return The {@link Duration} until the {@link AccessToken} expires.
     */
    private static Duration getDurationUntilExpiration(AccessToken accessToken) {
        // Call Duration.between with the 'cache.getExpiresAt' as the start Temporal and 'now' as the end Temporal as
        // some TokenCredential implementations may use 'OffsetDateTime.MAX' as the expiration time. When comparing the
        // time between now and 'OffsetDateTime.MAX', depending on the Java version, it may attempt to change the end
        // Temporal's time zone to match the start Temporal's time zone. Since 'OffsetDateTime.MAX' uses the most
        // minimal time zone offset, if the now time is using anything before that it will result in
        // 'OffsetDateTime.MAX' needing to roll over its time to the next day which results in the 'year' value being
        // incremented to a value outside the 'year' bounds allowed by OffsetDateTime.
        //
        // Changing to having the 'cache.getExpiresAt' means it is impossible for this rollover to occur as the start
        // Temporal doesn't have its time zone modified. But it now means the time between value is inverted, so the
        // result is 'negated()' to maintain current behaviors.
        return Duration.between(accessToken.getExpiresAt(), OffsetDateTime.now()).negated();
    }
}
