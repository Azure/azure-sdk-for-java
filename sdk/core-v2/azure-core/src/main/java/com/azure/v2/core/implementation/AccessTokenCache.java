// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.implementation;

import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.credentials.TokenRequestContext;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.logging.LogLevel;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
    private volatile AccessTokenCacheInfo cacheInfo;
    private final TokenCredential tokenCredential;
    // Stores the last authenticated token request context. The cached token is valid under this context.
    private TokenRequestContext tokenRequestContext;

    // Used for sync flow.
    private final Lock lock;

    /**
     * Creates an instance of AccessTokenCache.
     *
     * @param tokenCredential the token credential to be used to acquire the token.
     */
    public AccessTokenCache(TokenCredential tokenCredential) {
        Objects.requireNonNull(tokenCredential, "The token credential cannot be null");
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
     * Get a token from either the cache or replenish the cache with a new token.
     *
     * @param tokenRequestContext The request context for token acquisition.
     * @param checkToForceFetchToken The flag indicating whether to force fetch a new token or not.
     * @return The Publisher that emits an AccessToken
     */
    public AccessToken getToken(TokenRequestContext tokenRequestContext, boolean checkToForceFetchToken) {
        lock.lock();
        try {
            return retrieveToken(tokenRequestContext, checkToForceFetchToken);
        } finally {
            lock.unlock();
        }
    }

    private AccessToken retrieveToken(TokenRequestContext tokenRequestContext, boolean forceFetchToken) {
        if (tokenRequestContext == null) {
            throw LOGGER.throwableAtError()
                .log("The token request context input cannot be null.", IllegalArgumentException::new);
        }

        AccessTokenCacheInfo cache = this.cacheInfo;
        AccessToken cachedToken = cache.getCachedAccessToken();

        boolean needsRefresh = determineRefreshRequirement(tokenRequestContext, forceFetchToken, cachedToken);

        if (!needsRefresh) {
            return cachedToken;
        }

        return attemptTokenRefresh(cachedToken, tokenRequestContext);
    }

    private boolean determineRefreshRequirement(TokenRequestContext tokenRequestContext, boolean forceFetchToken,
        AccessToken cachedToken) {

        if (cachedToken == null || cachedToken.isExpired()) {
            return true;
        }

        if (forceFetchToken && checkIfTokenRequestsAreDifferent(tokenRequestContext)) {
            this.tokenRequestContext = tokenRequestContext;
            return true;
        }

        if (!checkIfRefreshIsNeeded(cachedToken)) {
            return false;
        }

        return OffsetDateTime.now().isAfter(this.cacheInfo.getNextTokenRefreshAt());
    }

    private AccessToken attemptTokenRefresh(AccessToken cachedToken, TokenRequestContext tokenRequestContext) {
        try {
            AccessToken newToken = getToken(tokenRequestContext);
            logTokenRefresh(LogLevel.VERBOSE, cachedToken, "Acquired a new access token.");

            this.cacheInfo = new AccessTokenCacheInfo(newToken, OffsetDateTime.now().plus(REFRESH_DELAY));
            return newToken;

        } catch (Throwable error) {
            logTokenRefresh(LogLevel.ERROR, cachedToken, "Failed to acquire a new access token.");
            this.cacheInfo = new AccessTokenCacheInfo(cachedToken, OffsetDateTime.now());

            if (cachedToken != null) {
                return cachedToken;
            }
            throw error;
        }
    }

    private boolean checkIfTokenRequestsAreDifferent(TokenRequestContext tokenRequestContext) {
        return !(this.tokenRequestContext != null
            && (this.tokenRequestContext.getClaims() == null
                ? tokenRequestContext.getClaims() == null
                : (tokenRequestContext.getClaims() != null
                    && tokenRequestContext.getClaims().equals(this.tokenRequestContext.getClaims())))
            && this.tokenRequestContext.getScopes().equals(tokenRequestContext.getScopes()));
    }

    private static void logTokenRefresh(LogLevel level, AccessToken cache, String prefix) {
        if (cache == null || !LOGGER.canLogAtLevel(level)) {
            return;
        }

        Duration tte = getDurationUntilExpiration(cache);

        LOGGER.atLevel(level)
            .log(String.format("%s. expiresAt: %s, tteSeconds: %s, retryAfterSeconds: %s, expired: %s", prefix,
                cache.getExpiresAt(), tte.abs().getSeconds(), REFRESH_DELAY_STRING, tte.isNegative()));
    }

    private AccessToken getToken(TokenRequestContext tokenRequestContext) {
        return this.tokenCredential.getToken(tokenRequestContext);
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
