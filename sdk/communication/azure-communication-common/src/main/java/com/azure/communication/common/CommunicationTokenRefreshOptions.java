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
     * @param tokenRefresher The callback function that acquires a fresh token from the Communication Identity API, e.g. by calling the CommunicationIdentityClient
     * @param refreshProactively Determines whether the token should be proactively renewed prior to its expiry or on demand.
     */
    public CommunicationTokenRefreshOptions(Supplier<Mono<String>> tokenRefresher, boolean refreshProactively) {
        this(tokenRefresher, refreshProactively, null, getDefaultRefreshTimeBeforeTokenExpiry());
    }

    /**
     * Creates a CommunicationTokenRefreshOptions object
     *
     * @param tokenRefresher The callback function that acquires a fresh token from the Communication Identity API, e.g. by calling the CommunicationIdentityClient
     * @param refreshProactively Determines whether the token should be proactively renewed prior to its expiry or on demand.
     * @param initialToken The optional serialized JWT token
     */
    public CommunicationTokenRefreshOptions(Supplier<Mono<String>> tokenRefresher, boolean refreshProactively, String initialToken) {
        this(tokenRefresher, refreshProactively, initialToken, getDefaultRefreshTimeBeforeTokenExpiry());
    }

    /**
     * Creates a CommunicationTokenRefreshOptions object
     *
     * @param tokenRefresher The callback function that acquires a fresh token from the Communication Identity API, e.g. by calling the CommunicationIdentityClient
     * @param refreshProactively Determines whether the token should be proactively renewed prior to its expiry or on demand.
     * @param initialToken The optional serialized JWT token
     * @param refreshTimeBeforeTokenExpiry The time span before token expiry that causes the tokenRefresher to be called if refreshProactively is true. For example, setting it to 5 minutes means that 5 minutes before the cached token expires, the proactive refresh will request a new token. The default value is 10 minutes.
     */
    public CommunicationTokenRefreshOptions(Supplier<Mono<String>> tokenRefresher, boolean refreshProactively, String initialToken, Duration refreshTimeBeforeTokenExpiry) {
        this.tokenRefresher = tokenRefresher;
        this.refreshProactively = refreshProactively;
        this.initialToken = initialToken;
        this.refreshTimeBeforeTokenExpiry = refreshTimeBeforeTokenExpiry;
    }

    /**
     * @return The token refresher to provide capacity to fetch fresh token
     */
    public Supplier<Mono<String>> getTokenRefresher() {
        return tokenRefresher;
    }

    /**
     * @return Whether or not to refresh token proactively
     */
    public boolean isRefreshProactively() {
        return refreshProactively;
    }

    /**
     * @return The initial token
     */
    public String getInitialToken() {
        return initialToken;
    }

    /**
     * @return The time span before token expiry that causes the tokenRefresher to be called if refreshProactively is true
     */
    public Duration getRefreshTimeBeforeTokenExpiry() {
        return refreshTimeBeforeTokenExpiry;
    }

    /**
     * @return The default time span before token expiry that causes the tokenRefresher to be called if refreshProactively is true
     */
    private static Duration getDefaultRefreshTimeBeforeTokenExpiry() {
        return Duration.ofMinutes(DEFAULT_EXPIRING_OFFSET_MINUTES);
    }

}
