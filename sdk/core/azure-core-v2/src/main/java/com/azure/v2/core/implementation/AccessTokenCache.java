// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.implementation;

import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.credentials.TokenRequestContext;
import io.clientcore.core.credentials.AccessToken;
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
            throw LOGGER
                .logThrowableAsError(new IllegalArgumentException("The token request context input cannot be null."));
        }
        AccessTokenCacheInfo cache = this.cacheInfo;
        AccessToken cachedToken = cache.getCachedAccessToken();

        OffsetDateTime now = OffsetDateTime.now();
        boolean tokenRefresh;
        AccessToken fallback;

        // Check if the incoming token request context is different from the cached one. A different
        // token request context, requires to fetch a new token as the cached one won't work for the
        // passed in token request context.
        boolean forceRefresh = (forceFetchToken && checkIfForceRefreshRequired(tokenRequestContext))
            || this.tokenRequestContext == null;

        if (forceRefresh) {
            this.tokenRequestContext = tokenRequestContext;
            tokenRefresh = true;
            fallback = null;
        } else if (cachedToken != null && !checkIfRefreshIsNeeded(cachedToken)) {
            // fresh cache & no need to refresh
            tokenRefresh = false;
            fallback = cachedToken;
        } else if (cachedToken == null || cachedToken.isExpired()) {
            // no token to use
            // refresh immediately
            tokenRefresh = true;

            // cache doesn't exist or expired, no fallback
            fallback = null;
        } else {
            // token available, but close to expiry
            if (now.isAfter(cache.getNextTokenRefresh())) {
                // refresh immediately
                tokenRefresh = true;
            } else {
                // still in timeout, do not refresh
                tokenRefresh = false;
            }
            // cache hasn't expired, ignore refresh error this time
            fallback = cachedToken;
        }

        try {
            if (tokenRefresh) {
                AccessToken token = getToken();
                logTokenRefresh(LogLevel.VERBOSE, cachedToken, now, "Acquired a new access token.");
                OffsetDateTime nextTokenRefreshTime = OffsetDateTime.now().plus(REFRESH_DELAY);
                AccessTokenCacheInfo updatedInfo = new AccessTokenCacheInfo(token, nextTokenRefreshTime);
                this.cacheInfo = updatedInfo;
                return token;
            } else {
                return fallback;
            }
        } catch (Throwable error) {
            logTokenRefresh(LogLevel.ERROR, cachedToken, now, "Failed to acquire a new access token.");
            OffsetDateTime nextTokenRefreshTime = OffsetDateTime.now();
            AccessTokenCacheInfo updatedInfo = new AccessTokenCacheInfo(cachedToken, nextTokenRefreshTime);
            this.cacheInfo = updatedInfo;
            if (fallback != null) {
                return fallback;
            }
            throw error;
        }
    }

    private boolean checkIfForceRefreshRequired(TokenRequestContext tokenRequestContext) {
        return !(this.tokenRequestContext != null
            && (this.tokenRequestContext.getClaims() == null
                ? tokenRequestContext.getClaims() == null
                : (tokenRequestContext.getClaims() != null
                    && tokenRequestContext.getClaims().equals(this.tokenRequestContext.getClaims())))
            && this.tokenRequestContext.getScopes().equals(tokenRequestContext.getScopes()));
    }

    private static void logTokenRefresh(LogLevel level, AccessToken cache, OffsetDateTime now, String prefix) {
        if (cache == null || !LOGGER.canLogAtLevel(level)) {
            return;
        }

        Duration tte = getDurationUntilExpiration(cache);

        LOGGER.atLevel(level)
            .log(String.format("%s. expiresAt: %s, tteSeconds: %s, retryAfterSeconds: %s, expired: %s", prefix,
                cache.getExpiresAt(), tte.abs().getSeconds(), REFRESH_DELAY_STRING, tte.isNegative()));
    }

    private AccessToken getToken() {
        return this.tokenCredential.getToken(this.tokenRequestContext);
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

    ///**
    // * A token cache that supports caching a token and refreshing it.
    // * TODO: @g2vinay, make the implementation in client-core public API and remove it from this layer.
    // */
    //public final class AccessTokenCache {
    //    // The delay after a refresh to attempt another token refresh
    //    private static final Duration REFRESH_DELAY = Duration.ofSeconds(30);
    //    private static final String REFRESH_DELAY_STRING = String.valueOf(REFRESH_DELAY.getSeconds());
    //
    //    // the offset before token expiry to attempt proactive token refresh
    //    private static final Duration REFRESH_OFFSET = Duration.ofMinutes(5);
    //    // AccessTokenCache is a commonly used class, use a static logger.
    //    private static final ClientLogger LOGGER = new ClientLogger(AccessTokenCache.class);
    //    private final AtomicReference<AccessTokenCacheInfo> cacheInfo;
    //    // Stores the last authenticated token request context. The cached token is valid under this context.
    //    private TokenRequestContext tokenRequestContext;
    //    private final Supplier<AccessToken> tokenSupplierSync;
    //    private final Predicate<AccessToken> shouldRefresh;
    //    // Used for sync flow.
    //    private final Lock lock;
    //
    //    /**
    //     * Creates an instance of RefreshableTokenCredential with default scheme "Bearer".
    //     *
    //     * @param tokenCredential the token credential to be used to acquire the token.
    //     */
    //    public AccessTokenCache(TokenCredential tokenCredential) {
    //        Objects.requireNonNull(tokenCredential, "The token credential cannot be null");
    //        this.cacheInfo = new AtomicReference<>(new AccessTokenCacheInfo(null, OffsetDateTime.now()));
    //        this.shouldRefresh
    //            = accessToken -> OffsetDateTime.now().isAfter(accessToken.getExpiresAt().minus(REFRESH_OFFSET));
    //        this.tokenSupplierSync = () -> tokenCredential.getToken(this.tokenRequestContext);
    //        this.lock = new ReentrantLock();
    //    }
    //
    //    /**
    //     * Synchronously get a token from either the cache or replenish the cache with a new token.
    //     *
    //     * @param tokenRequestContext The request context for token acquisition.
    //     * @param checkToForceFetchToken The flag indicating whether to force fetch a new token or not.
    //     * @return The Publisher that emits an AccessToken
    //     */
    //    public AccessToken getTokenSync(TokenRequestContext tokenRequestContext, boolean checkToForceFetchToken) {
    //        lock.lock();
    //        try {
    //            return retrieveTokenSync(tokenRequestContext, checkToForceFetchToken).get();
    //        } finally {
    //            lock.unlock();
    //        }
    //    }
    //
    //    private Supplier<AccessToken> retrieveTokenSync(TokenRequestContext tokenRequestContext,
    //        boolean checkToForceFetchToken) {
    //        return () -> {
    //            if (tokenRequestContext == null) {
    //                throw LOGGER.logThrowableAsError(
    //                    new IllegalArgumentException("The token request context input cannot be null."));
    //            }
    //            AccessTokenCacheInfo cache = this.cacheInfo.get();
    //            AccessToken cachedToken = cache.getCachedAccessToken();
    //
    //            OffsetDateTime now = OffsetDateTime.now();
    //            Supplier<AccessToken> tokenRefresh;
    //            AccessToken fallback;
    //
    //            // Check if the incoming token request context is different from the cached one. A different
    //            // token request context, requires to fetch a new token as the cached one won't work for the
    //            // passed in token request context.
    //            boolean forceRefresh = (checkToForceFetchToken && checkIfForceRefreshRequired(tokenRequestContext))
    //                || this.tokenRequestContext == null;
    //
    //            if (forceRefresh) {
    //                this.tokenRequestContext = tokenRequestContext;
    //                tokenRefresh = tokenSupplierSync;
    //                fallback = null;
    //            } else if (cachedToken != null && !shouldRefresh.test(cachedToken)) {
    //                // fresh cache & no need to refresh
    //                tokenRefresh = null;
    //                fallback = cachedToken;
    //            } else if (cachedToken == null || cachedToken.isExpired()) {
    //                // no token to use
    //                // refresh immediately
    //                tokenRefresh = tokenSupplierSync;
    //
    //                // cache doesn't exist or expired, no fallback
    //                fallback = null;
    //            } else {
    //                // token available, but close to expiry
    //                if (now.isAfter(cache.getNextTokenRefresh())) {
    //                    // refresh immediately
    //                    tokenRefresh = tokenSupplierSync;
    //                } else {
    //                    // still in timeout, do not refresh
    //                    tokenRefresh = null;
    //                }
    //                // cache hasn't expired, ignore refresh error this time
    //                fallback = cachedToken;
    //            }
    //
    //            try {
    //                if (tokenRefresh != null) {
    //                    AccessToken token = tokenRefresh.get();
    //                    buildTokenRefreshLog(LogLevel.INFORMATIONAL, cachedToken, now).log("Acquired a new access token.");
    //                    OffsetDateTime nextTokenRefreshTime = OffsetDateTime.now().plus(REFRESH_DELAY);
    //                    AccessTokenCacheInfo updatedInfo = new AccessTokenCacheInfo(token, nextTokenRefreshTime);
    //                    this.cacheInfo.set(updatedInfo);
    //                    return token;
    //                } else {
    //                    return fallback;
    //                }
    //            } catch (Throwable error) {
    //                buildTokenRefreshLog(LogLevel.ERROR, cachedToken, now).log("Failed to acquire a new access token.",
    //                    error);
    //                OffsetDateTime nextTokenRefreshTime = OffsetDateTime.now();
    //                AccessTokenCacheInfo updatedInfo = new AccessTokenCacheInfo(cachedToken, nextTokenRefreshTime);
    //                this.cacheInfo.set(updatedInfo);
    //                if (fallback != null) {
    //                    return fallback;
    //                }
    //                throw error;
    //            }
    //        };
    //    }
    //
    //    private boolean checkIfForceRefreshRequired(TokenRequestContext tokenRequestContext) {
    //        return !(this.tokenRequestContext != null
    //            && (this.tokenRequestContext.getClaims() == null
    //                ? tokenRequestContext.getClaims() == null
    //                : (tokenRequestContext.getClaims() == null
    //                    ? false
    //                    : tokenRequestContext.getClaims().equals(this.tokenRequestContext.getClaims())))
    //            && this.tokenRequestContext.getScopes().equals(tokenRequestContext.getScopes()));
    //    }
    //
    //    private static LoggingEvent buildTokenRefreshLog(LogLevel level, AccessToken cache, OffsetDateTime now) {
    //        LoggingEvent logBuilder = LOGGER.atLevel(level);
    //        if (cache == null || !LOGGER.canLogAtLevel(level)) {
    //            return logBuilder;
    //        }
    //
    //        Duration tte = Duration.between(now, cache.getExpiresAt());
    //        return logBuilder.addKeyValue("expiresAt", cache.getExpiresAt())
    //            .addKeyValue("tteSeconds", String.valueOf(tte.abs().getSeconds()))
    //            .addKeyValue("retryAfterSeconds", REFRESH_DELAY_STRING)
    //            .addKeyValue("expired", tte.isNegative());
    //    }
    //}

}
