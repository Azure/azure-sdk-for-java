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
    private static final int DEFAULT_EXPIRING_OFFSET_MINUTES = 5;
    private final Supplier<Mono<String>> tokenRefresher;
    private final boolean refreshProactively;
    private final String initialToken;
    private final Duration refreshOffsetTime;

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
        this.refreshOffsetTime = getDefaultRefreshOffsetTime();
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
        this.refreshOffsetTime = getDefaultRefreshOffsetTime();
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
     * @param refreshOffsetTime proactive refresh interval.
     */
    public CommunicationTokenRefreshOptions(Supplier<Mono<String>> tokenRefresher, boolean refreshProactively, String initialToken, Duration refreshOffsetTime) {
        this.tokenRefresher = tokenRefresher;
        this.refreshProactively = refreshProactively;
        this.initialToken = initialToken;
        this.refreshOffsetTime = refreshOffsetTime;
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
     * @return the proactive refresh interval
     */
    public Duration getRefreshOffsetTime() {
        return refreshOffsetTime;
    }

    /**
     * @return default proactive refresh interval
     */
    public static Duration getDefaultRefreshOffsetTime() {
        return Duration.ofMinutes(DEFAULT_EXPIRING_OFFSET_MINUTES);
    }

}
