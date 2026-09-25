// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.credentials;

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
 * A thread-safe token cache that wraps a {@link TokenCredential} and proactively refreshes its access token.
 *
 * <p>The cache stores one token and one associated {@link TokenRequestContext} per instance. A token is refreshed five
 * minutes before expiration by default, or at its configured {@link AccessToken#getRefreshAt() refresh time}. If a
 * proactive refresh fails while the cached token remains valid, the valid token is returned.</p>
 *
 * <p>When {@code refreshOnContextChange} is {@code true}, changes to scopes, tenant ID, or claims trigger an immediate
 * refresh. This supports Continuous Access Evaluation claims challenges. A cache shouldn't be shared concurrently by
 * operations that require different token request contexts because each new context replaces the previously cached
 * context and token. Concurrent callers are serialized while a token is acquired so that only one refresh is in
 * flight for this single-token cache.</p>
 */
public final class AccessTokenCache {
    private static final Duration REFRESH_DELAY = Duration.ofSeconds(30);
    private static final Duration REFRESH_OFFSET = Duration.ofMinutes(5);
    private static final ClientLogger LOGGER = new ClientLogger(AccessTokenCache.class);

    private final TokenCredential tokenCredential;
    private final Lock lock = new ReentrantLock();
    private CacheInfo cacheInfo = new CacheInfo(null, OffsetDateTime.now());
    private TokenRequestContext tokenRequestContext;

    /**
     * Creates a token cache backed by the given credential.
     *
     * @param tokenCredential The credential used to acquire tokens.
     * @throws NullPointerException If {@code tokenCredential} is {@code null}.
     */
    public AccessTokenCache(TokenCredential tokenCredential) {
        this.tokenCredential = Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null.");
    }

    /**
     * Gets a cached token or acquires and caches a new token.
     *
     * @param tokenRequestContext The token request context.
     * @param refreshOnContextChange Whether a change in scopes, tenant ID, or claims should force token acquisition.
     * @return The cached or newly acquired token.
     * @throws IllegalArgumentException If {@code tokenRequestContext} is {@code null}.
     */
    public AccessToken getToken(TokenRequestContext tokenRequestContext, boolean refreshOnContextChange) {
        if (tokenRequestContext == null) {
            throw LOGGER.throwableAtError().log("'tokenRequestContext' cannot be null.", IllegalArgumentException::new);
        }

        lock.lock();
        try {
            AccessToken cachedToken = cacheInfo.token;
            OffsetDateTime now = OffsetDateTime.now();
            boolean contextChanged = this.tokenRequestContext == null
                || (refreshOnContextChange && !hasSameContext(this.tokenRequestContext, tokenRequestContext));

            if (!contextChanged && cachedToken != null && !shouldRefresh(cachedToken, now)) {
                return cachedToken;
            }

            if (!contextChanged
                && cachedToken != null
                && !cachedToken.isExpired()
                && now.isBefore(cacheInfo.nextRefreshAt)) {
                return cachedToken;
            }

            return refreshToken(cachedToken, tokenRequestContext, !contextChanged);
        } finally {
            lock.unlock();
        }
    }

    private AccessToken refreshToken(AccessToken cachedToken, TokenRequestContext requestContext,
        boolean allowFallback) {
        try {
            AccessToken newToken = Objects.requireNonNull(tokenCredential.getToken(requestContext),
                "TokenCredential returned a null access token.");
            logTokenRefresh(LogLevel.VERBOSE, cachedToken, "Acquired a new access token.", null);
            tokenRequestContext = requestContext;
            cacheInfo = new CacheInfo(newToken, OffsetDateTime.now().plus(REFRESH_DELAY));
            return newToken;
        } catch (RuntimeException | Error error) {
            logTokenRefresh(LogLevel.ERROR, cachedToken, "Failed to acquire a new access token.", error);
            cacheInfo = new CacheInfo(cachedToken, OffsetDateTime.now());
            if (allowFallback && cachedToken != null && !cachedToken.isExpired()) {
                return cachedToken;
            }
            throw error;
        }
    }

    private static boolean shouldRefresh(AccessToken token, OffsetDateTime now) {
        OffsetDateTime refreshAt
            = token.getRefreshAt() == null ? token.getExpiresAt().minus(REFRESH_OFFSET) : token.getRefreshAt();
        return now.isAfter(refreshAt);
    }

    private static boolean hasSameContext(TokenRequestContext first, TokenRequestContext second) {
        return Objects.equals(first.getScopes(), second.getScopes())
            && Objects.equals(first.getTenantId(), second.getTenantId())
            && Objects.equals(first.getClaims(), second.getClaims())
            && Objects.equals(first.getProofOfPossessionOptions(), second.getProofOfPossessionOptions());
    }

    private static void logTokenRefresh(LogLevel level, AccessToken token, String message, Throwable error) {
        if (!LOGGER.canLogAtLevel(level)) {
            return;
        }

        LoggingEvent event = LOGGER.atLevel(level);
        if (token != null) {
            Duration timeToExpiration = Duration.between(token.getExpiresAt(), OffsetDateTime.now()).negated();
            event.addKeyValue("expiresAt", token.getExpiresAt())
                .addKeyValue("tteSeconds", timeToExpiration.abs().getSeconds())
                .addKeyValue("retryAfterSeconds", REFRESH_DELAY.getSeconds())
                .addKeyValue("expired", timeToExpiration.isNegative());
        }
        if (error != null) {
            event.setThrowable(error);
        }
        event.log(message);
    }

    private static final class CacheInfo {
        private final AccessToken token;
        private final OffsetDateTime nextRefreshAt;

        private CacheInfo(AccessToken token, OffsetDateTime nextRefreshAt) {
            this.token = token;
            this.nextRefreshAt = nextRefreshAt;
        }
    }
}
