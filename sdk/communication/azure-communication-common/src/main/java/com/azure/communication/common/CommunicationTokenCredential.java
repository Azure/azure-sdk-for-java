// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

import com.azure.communication.common.implementation.TokenParser;
import com.azure.core.credential.AccessToken;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Provide user credential for Communication service user
 */
public final class CommunicationTokenCredential implements AutoCloseable {
    private static final int DEFAULT_EXPIRING_OFFSET_MINUTES = 10;
    private static final int DEFAULT_REFRESH_AFTER_TTL_DIVIDER = 2;

    private final ClientLogger logger = new ClientLogger(CommunicationTokenCredential.class);

    private AccessToken accessToken;
    private final TokenParser tokenParser = new TokenParser();
    private Supplier<Mono<String>> refresher;
    private FetchingTask fetchingTask;
    private boolean isClosed = false;

    /**
     * Create with serialized JWT token
     *
     * @param token serialized JWT token
     */
    public CommunicationTokenCredential(String token) {
        Objects.requireNonNull(token, "'token' cannot be null.");
        setToken(token);
    }

    /**
     * Create with tokenRefreshOptions, which includes a token supplier and optional serialized JWT token.
     * If refresh proactively is true, callback function tokenRefresher will be called
     * ahead of the token expiry by the number of minutes specified by
     * CallbackOffsetMinutes defaulted to ten minutes.
     *
     * @param tokenRefreshOptions implementation to supply fresh token when reqested
     */
    public CommunicationTokenCredential(CommunicationTokenRefreshOptions tokenRefreshOptions) {
        Supplier<String> tokenRefresher = tokenRefreshOptions.getTokenRefresherSync();
        refresher = tokenRefresher != null
                ? () -> Mono.fromSupplier(tokenRefresher)
                : tokenRefreshOptions.getTokenRefresher();
        Objects.requireNonNull(refresher, "'tokenRefresher' cannot be null.");
        if (tokenRefreshOptions.getInitialToken() != null) {
            setToken(tokenRefreshOptions.getInitialToken());
        }
        if (tokenRefreshOptions.isRefreshProactively()) {
            scheduleRefresher();
        }
    }

    private void scheduleRefresher() {
        OffsetDateTime nextFetchTime;
        if (isTokenExpired(accessToken)) {
            nextFetchTime = OffsetDateTime.now();
        } else {
            OffsetDateTime now = OffsetDateTime.now();
            long tokenTtlMs = accessToken.getExpiresAt().toInstant().toEpochMilli() - now.toInstant().toEpochMilli();
            long nextFetchTimeMs = isTokenExpiringSoon()
                    ? tokenTtlMs / DEFAULT_REFRESH_AFTER_TTL_DIVIDER
                    : tokenTtlMs - TimeUnit.MILLISECONDS.convert(DEFAULT_EXPIRING_OFFSET_MINUTES, TimeUnit.MINUTES);
            nextFetchTime = now.plusNanos(TimeUnit.NANOSECONDS.convert(nextFetchTimeMs, TimeUnit.MILLISECONDS));
        }
        fetchingTask = new FetchingTask(this, nextFetchTime);
    }

    private boolean isTokenExpired(AccessToken accessToken) {
        return accessToken == null || accessToken.isExpired();
    }

    private boolean isTokenExpiringSoon() {
        return accessToken == null || OffsetDateTime.now().compareTo(accessToken.getExpiresAt().minusMinutes(DEFAULT_EXPIRING_OFFSET_MINUTES)) > 0;
    }

    /**
     * Get Azure core access token from credential
     *
     * @return Asynchronous call to fetch actual token
     */
    public Mono<AccessToken> getToken() {
        if (isClosed) {
            return FluxUtil.monoError(logger,
                    new RuntimeException("getToken called on closed CommunicationTokenCredential object"));
        }
        if (isTokenExpired(accessToken) && refresher != null) {
            synchronized (this) {
                // no valid token to return and can refresh
                if (isTokenExpired(accessToken) && refresher != null) {
                    return fetchFreshToken()
                            .flatMap(token -> {
                                accessToken = tokenParser.parseJWTToken(token);
                                if (isTokenExpired(accessToken)) {
                                    return FluxUtil.monoError(logger,
                                            new IllegalArgumentException("The token returned from the tokenRefresher is expired."));
                                }
                                return Mono.just(accessToken);
                            });
                }
            }
        }
        return Mono.just(accessToken);
    }

    @Override
    public void close() throws IOException {
        isClosed = true;
        if (fetchingTask != null) {
            fetchingTask.stopTimer();
            fetchingTask = null;
        }
        refresher = null;
    }

    // For test verification usage only
    boolean hasProactiveFetcher() {
        return fetchingTask != null;
    }

    private void setToken(String freshToken) {
        accessToken = tokenParser.parseJWTToken(freshToken);
        if (hasProactiveFetcher()) {
            scheduleRefresher();
        }
    }

    private Mono<String> fetchFreshToken() {
        Mono<String> tokenAsync = refresher.get();
        if (tokenAsync == null) {
            return FluxUtil.monoError(logger,
                    new RuntimeException("get() function of the token refresher should not return null."));
        }
        return tokenAsync;
    }

    private static class FetchingTask {
        private final CommunicationTokenCredential host;
        private Timer expiringTimer;
        private OffsetDateTime nextFetchTime;

        FetchingTask(CommunicationTokenCredential tokenHost,
                     OffsetDateTime nextFetchAt) {
            host = tokenHost;
            nextFetchTime = nextFetchAt;
            stopTimer();
            startTimer();
        }

        private synchronized void startTimer() {
            expiringTimer = new Timer();
            Date expiring = Date.from(nextFetchTime.toInstant());
            expiringTimer.schedule(new TokenExpiringTask(this), expiring);
        }

        private synchronized void stopTimer() {
            if (expiringTimer == null) {
                return;
            }

            expiringTimer.cancel();
            expiringTimer.purge();
            expiringTimer = null;
        }

        private Mono<String> fetchFreshToken() {
            return host.fetchFreshToken();
        }

        private void setToken(String freshTokenString) {
            host.setToken(freshTokenString);
        }

        private boolean isTokenExpired(String freshTokenString) {
            return host.tokenParser.parseJWTToken(freshTokenString).isExpired();
        }

        private class TokenExpiringTask extends TimerTask {
            private final ClientLogger logger = new ClientLogger(TokenExpiringTask.class);
            private final FetchingTask tokenCache;

            TokenExpiringTask(FetchingTask host) {
                tokenCache = host;
            }

            @Override
            public void run() {
                try {
                    Mono<String> tokenAsync = tokenCache.fetchFreshToken();
                    tokenAsync.subscribe(token -> {
                        if (!tokenCache.isTokenExpired(token)) {
                            tokenCache.setToken(token);
                        } else {
                            logger.logExceptionAsError(new IllegalArgumentException("The token returned from the tokenRefresher is expired."));
                        }
                    });
                } catch (Exception exception) {
                    logger.logExceptionAsError(new RuntimeException(exception));
                }

            }
        }
    }
}
