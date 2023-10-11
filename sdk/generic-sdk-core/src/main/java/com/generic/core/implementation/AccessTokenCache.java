// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation;

import com.generic.core.credential.AccessToken;
import com.generic.core.credential.TokenCredential;
import com.generic.core.credential.TokenRequestContext;
import com.generic.core.util.logging.ClientLogger;
import com.generic.core.util.logging.LogLevel;
import com.generic.core.util.logging.LoggingEventBuilder;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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
    private final AtomicReference<AccessTokenCacheInfo> cacheInfo;
    private final TokenCredential tokenCredential;
    // Stores the last authenticated token request context. The cached token is valid under this context.
    private TokenRequestContext tokenRequestContext;
    private Supplier<AccessToken> tokenSupplierAsync;
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
        this.tokenCredential = tokenCredential;
        this.cacheInfo = new AtomicReference<>(new AccessTokenCacheInfo(null, OffsetDateTime.now()));
        this.shouldRefresh = accessToken -> OffsetDateTime.now()
            .isAfter(accessToken.getExpiresAt().minus(REFRESH_OFFSET));
        this.tokenSupplierSync = () -> tokenCredential.getTokenSync(this.tokenRequestContext);
        this.lock = new ReentrantLock();
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
