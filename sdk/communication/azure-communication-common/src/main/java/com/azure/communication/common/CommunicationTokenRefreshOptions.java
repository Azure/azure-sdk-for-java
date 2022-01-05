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
    private boolean refreshProactively;
    private String initialToken;
    private Duration refreshIntervalBeforeTokenExpiry;

    /**
     * Creates a CommunicationTokenRefreshOptions object
     *
     * @param tokenRefresher The callback function that acquires a fresh token from the Communication Identity API, e.g. by calling the CommunicationIdentityClient
     * @param refreshProactively Determines whether the token should be proactively renewed prior to its expiry or on demand.
     * @deprecated Use {@link #CommunicationTokenRefreshOptions(Supplier)} instead and chain fluent setter {@link #setRefreshProactively(boolean)}
     */
    @Deprecated
    public CommunicationTokenRefreshOptions(Supplier<Mono<String>> tokenRefresher, boolean refreshProactively) {
        this(tokenRefresher, refreshProactively, null);
    }

    /**
     * Creates a CommunicationTokenRefreshOptions object
     *
     * @param tokenRefresher The callback function that acquires a fresh token from the Communication Identity API, e.g. by calling the CommunicationIdentityClient
     * @param refreshProactively Determines whether the token should be proactively renewed prior to its expiry or on demand.
     * @param initialToken The optional serialized JWT token
     * @deprecated Use {@link #CommunicationTokenRefreshOptions(Supplier)} instead
     * and chain fluent setters {@link #setRefreshProactively(boolean)}, {@link #setInitialToken(String)}
     */
    @Deprecated
    public CommunicationTokenRefreshOptions(Supplier<Mono<String>> tokenRefresher, boolean refreshProactively, String initialToken) {
        this.tokenRefresher = tokenRefresher;
        this.refreshProactively = refreshProactively;
        this.initialToken = initialToken;
        this.refreshIntervalBeforeTokenExpiry = getDefaultRefreshIntervalBeforeTokenExpiry();
    }

    /**
     * Creates a CommunicationTokenRefreshOptions object
     *
     * @param tokenRefresher The callback function that acquires a fresh token from the Communication Identity API, e.g. by calling the CommunicationIdentityClient
     */
    public CommunicationTokenRefreshOptions(Supplier<Mono<String>> tokenRefresher) {
        this.tokenRefresher = tokenRefresher;
        this.refreshProactively = false;
        this.initialToken = null;
        this.refreshIntervalBeforeTokenExpiry = getDefaultRefreshIntervalBeforeTokenExpiry();
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
     * Set whether the token should be proactively renewed prior to its expiry or on demand.
     *
     * @param  refreshProactively the refreshProactively value to set.
     * @return the CommunicationTokenRefreshOptions object itself.
     */
    public CommunicationTokenRefreshOptions setRefreshProactively(boolean refreshProactively) {
        this.refreshProactively = refreshProactively;
        return this;
    }

    /**
     * @return The initial token
     */
    public String getInitialToken() {
        return initialToken;
    }

    /**
     * Set the optional serialized JWT token
     *
     * @param  initialToken the initialToken value to set.
     * @return the CommunicationTokenRefreshOptions object itself.
     */
    public CommunicationTokenRefreshOptions setInitialToken(String initialToken) {
        this.initialToken = initialToken;
        return this;
    }

    /**
     * @return The time span before token expiry that causes the tokenRefresher to be called if refreshProactively is true
     */
    public Duration getRefreshIntervalBeforeTokenExpiry() {
        return refreshIntervalBeforeTokenExpiry;
    }

    /**
     * Set the time span before token expiry that causes the tokenRefresher to be called if refreshProactively is true.
     * For example, setting it to 5 minutes means that 5 minutes before the cached token expires, the proactive refresh will request a new token.
     * The default value is 4.5 minutes.
     *
     * @param  refreshIntervalBeforeTokenExpiry the refreshIntervalBeforeTokenExpiry value to set.
     * @return the CommunicationTokenRefreshOptions object itself.
     */
    public CommunicationTokenRefreshOptions setRefreshIntervalBeforeTokenExpiry(Duration refreshIntervalBeforeTokenExpiry) {
        this.refreshIntervalBeforeTokenExpiry = refreshIntervalBeforeTokenExpiry;
        return this;
    }

    /**
     * @return The default time span before token expiry that causes the tokenRefresher to be called if refreshProactively is true
     */
    private static Duration getDefaultRefreshIntervalBeforeTokenExpiry() {
        return Duration.ofSeconds(DEFAULT_EXPIRING_OFFSET_SECONDS);
    }

}
