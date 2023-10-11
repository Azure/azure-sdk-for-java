// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.credential;

import com.generic.core.util.logging.ClientLogger;
import com.generic.core.util.logging.LogLevel;
import com.generic.core.util.logging.LoggingEventBuilder;

import java.time.Duration;
import java.time.OffsetDateTime;
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
    private final Supplier<AccessToken> tokenSupplier;

    private volatile AccessToken cache;
    private volatile OffsetDateTime nextTokenRefresh = OffsetDateTime.now();
    private final Predicate<AccessToken> shouldRefresh;

    /**
     * Creates an instance of RefreshableTokenCredential with default scheme "Bearer".
     *
     * @param tokenSupplier a method to get a new token
     */
    public SimpleTokenCache(Supplier<AccessToken> tokenSupplier) {
        this.tokenSupplier = tokenSupplier;
        this.shouldRefresh = accessToken -> OffsetDateTime.now()
            .isAfter(accessToken.getExpiresAt().minus(REFRESH_OFFSET));
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
