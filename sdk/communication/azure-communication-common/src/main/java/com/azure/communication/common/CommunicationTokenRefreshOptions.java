// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

import reactor.core.publisher.Mono;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * Options for refreshing CommunicationTokenCredential
 */
public final class CommunicationTokenRefreshOptions {
    private static final int DEFAULT_EXPIRING_OFFSET_MINUTES = 10;
    private final Supplier<Mono<String>> tokenRefresher;
    private final boolean refreshProactively;
    private final String initialToken;
    private final Duration refreshTimeBeforeTokenExpiry;

    /**
     * Creates a CommunicationTokenRefreshOptions object
     *
     * @param tokenRefresher the token refresher to provide capacity to fetch fresh token
     * @param refreshProactively when set to true, turn on proactive fetching to call
     *                           tokenRefresher before token expiry by minutes set
     *                           with setCallbackOffsetMinutes or default value of
     *                           two minutes
     */
    public CommunicationTokenRefreshOptions(Supplier<Mono<String>> tokenRefresher, boolean refreshProactively) {
        this.tokenRefresher = tokenRefresher;
        this.refreshProactively = refreshProactively;
        this.initialToken = null;
        this.refreshTimeBeforeTokenExpiry = getDefaultRefreshTimeBeforeTokenExpiry();
    }

    /**
     * Creates a CommunicationTokenRefreshOptions object
     *
     * @param tokenRefresher the token refresher to provide capacity to fetch fresh token
     * @param refreshProactively when set to true, turn on proactive fetching to call
     *                           tokenRefresher before token expiry by minutes set
     *                           with setCallbackOffsetMinutes or default value of
     *                           two minutes
     * @param initialToken the optional serialized JWT token
     */
    public CommunicationTokenRefreshOptions(Supplier<Mono<String>> tokenRefresher, boolean refreshProactively, String initialToken) {
        this.tokenRefresher = tokenRefresher;
        this.refreshProactively = refreshProactively;
        this.initialToken = initialToken;
        this.refreshTimeBeforeTokenExpiry = getDefaultRefreshTimeBeforeTokenExpiry();
    }

    /**
     * Creates a CommunicationTokenRefreshOptions object
     *
     * @param tokenRefresher the token refresher to provide capacity to fetch fresh token
     * @param refreshProactively when set to true, turn on proactive fetching to call
     *                           tokenRefresher before token expiry by minutes set
     *                           with setCallbackOffsetMinutes or default value of
     *                           two minutes
     * @param initialToken the optional serialized JWT token
     * @param refreshTimeBeforeTokenExpiry The time span before token expiry that tokenRefresher will be called if refreshProactively is true. For example, setting it to 5min means that 5min before the cached token expires, proactive refresh will request a new token. The default value is 10min.
     */
    public CommunicationTokenRefreshOptions(Supplier<Mono<String>> tokenRefresher, boolean refreshProactively, String initialToken, Duration refreshTimeBeforeTokenExpiry) {
        this.tokenRefresher = tokenRefresher;
        this.refreshProactively = refreshProactively;
        this.initialToken = initialToken;
        this.refreshTimeBeforeTokenExpiry = refreshTimeBeforeTokenExpiry;
    }

    /**
     * @return the token refresher to provide capacity to fetch fresh token
     */
    public Supplier<Mono<String>> getTokenRefresher() {
        return tokenRefresher;
    }

    /**
     * @return whether or not to refresh token proactively
     */
    public boolean isRefreshProactively() {
        return refreshProactively;
    }

    /**
     * @return the initial token
     */
    public String getInitialToken() {
        return initialToken;
    }

    /**
     * @return the time span before token expiry that tokenRefresher will be called if refreshProactively is true
     */
    public Duration getRefreshTimeBeforeTokenExpiry() {
        return refreshTimeBeforeTokenExpiry;
    }

    /**
     * @return default time span before token expiry that tokenRefresher will be called if refreshProactively is true
     */
    public static Duration getDefaultRefreshTimeBeforeTokenExpiry() {
        return Duration.ofMinutes(DEFAULT_EXPIRING_OFFSET_MINUTES);
    }

}
