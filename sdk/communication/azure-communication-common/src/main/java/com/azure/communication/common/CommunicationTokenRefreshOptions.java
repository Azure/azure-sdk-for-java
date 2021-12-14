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
    /**
     * This is a constant defined to represent the default time span before token expiry that causes the tokenRefresher
     * to be called if refreshProactively is true. The default value is 4.5 minutes to avoid MSAL compatibility issues.
     */
    private static final int DEFAULT_EXPIRING_OFFSET_SECONDS = 270;
    private final Supplier<Mono<String>> tokenRefresher;
    private final boolean refreshProactively;
    private final String initialToken;
    private final Duration refreshIntervalBeforeTokenExpiry;

    /**
     * Creates a CommunicationTokenRefreshOptions object
     *
     * @param tokenRefresher The callback function that acquires a fresh token from the Communication Identity API, e.g. by calling the CommunicationIdentityClient
     * @param refreshProactively Determines whether the token should be proactively renewed prior to its expiry or on demand.
     */
    public CommunicationTokenRefreshOptions(Supplier<Mono<String>> tokenRefresher, boolean refreshProactively) {
        this(tokenRefresher, refreshProactively, null, getDefaultRefreshIntervalBeforeTokenExpiry());
    }

    /**
     * Creates a CommunicationTokenRefreshOptions object
     *
     * @param tokenRefresher The callback function that acquires a fresh token from the Communication Identity API, e.g. by calling the CommunicationIdentityClient
     * @param refreshProactively Determines whether the token should be proactively renewed prior to its expiry or on demand.
     * @param initialToken The optional serialized JWT token
     */
    public CommunicationTokenRefreshOptions(Supplier<Mono<String>> tokenRefresher, boolean refreshProactively, String initialToken) {
        this(tokenRefresher, refreshProactively, initialToken, getDefaultRefreshIntervalBeforeTokenExpiry());
    }

    /**
     * Creates a CommunicationTokenRefreshOptions object
     *
     * @param tokenRefresher The callback function that acquires a fresh token from the Communication Identity API, e.g. by calling the CommunicationIdentityClient
     * @param refreshProactively Determines whether the token should be proactively renewed prior to its expiry or on demand.
     * @param initialToken The optional serialized JWT token
     * @param refreshIntervalBeforeTokenExpiry The time span before token expiry that causes the tokenRefresher to be called if refreshProactively is true. For example, setting it to 5 minutes means that 5 minutes before the cached token expires, the proactive refresh will request a new token. The default value is 4.5 minutes.
     */
    public CommunicationTokenRefreshOptions(Supplier<Mono<String>> tokenRefresher, boolean refreshProactively, String initialToken, Duration refreshIntervalBeforeTokenExpiry) {
        this.tokenRefresher = tokenRefresher;
        this.refreshProactively = refreshProactively;
        this.initialToken = initialToken;
        this.refreshIntervalBeforeTokenExpiry = refreshIntervalBeforeTokenExpiry;
    }

    /**
     * Creates a CommunicationTokenRefreshOptions object
     *
     * @param tokenRefresher The callback function that acquires a fresh token from the Communication Identity API, e.g. by calling the CommunicationIdentityClient
     * @param refreshProactively Determines whether the token should be proactively renewed prior to its expiry or on demand.
     * @param refreshIntervalBeforeTokenExpiry The time span before token expiry that causes the tokenRefresher to be called if refreshProactively is true. For example, setting it to 5 minutes means that 5 minutes before the cached token expires, the proactive refresh will request a new token. The default value is 4.5 minutes.
     */
    public CommunicationTokenRefreshOptions(Supplier<Mono<String>> tokenRefresher, boolean refreshProactively, Duration refreshIntervalBeforeTokenExpiry) {
        this(tokenRefresher, refreshProactively, null, refreshIntervalBeforeTokenExpiry);
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
    public Duration getRefreshIntervalBeforeTokenExpiry() {
        return refreshIntervalBeforeTokenExpiry;
    }

    /**
     * @return The default time span before token expiry that causes the tokenRefresher to be called if refreshProactively is true
     */
    private static Duration getDefaultRefreshIntervalBeforeTokenExpiry() {
        return Duration.ofSeconds(DEFAULT_EXPIRING_OFFSET_SECONDS);
    }

}
