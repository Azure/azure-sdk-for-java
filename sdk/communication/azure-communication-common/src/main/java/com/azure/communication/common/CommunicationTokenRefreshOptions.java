// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

import reactor.core.publisher.Mono;

import java.util.function.Supplier;

/**
 * Options for refreshing CommunicationTokenCredential
 */
public final class CommunicationTokenRefreshOptions {

    private final Supplier<Mono<String>> tokenRefresher;
    private boolean refreshProactively;
    private String initialToken;

    /**
     * Creates a CommunicationTokenRefreshOptions object
     *
     * @param tokenRefresher     The callback function that acquires a fresh token
     *                           from the Communication Identity API, e.g. by
     *                           calling the CommunicationIdentityClient
     * @param refreshProactively Determines whether the token should be proactively
     *                           renewed prior to its expiry or on demand.
     * @deprecated Use {@link #CommunicationTokenRefreshOptions(Supplier)} instead
     *             and chain fluent setter {@link #setRefreshProactively(boolean)}
     */
    @Deprecated
    public CommunicationTokenRefreshOptions(Supplier<Mono<String>> tokenRefresher, boolean refreshProactively) {
        this(tokenRefresher, refreshProactively, null);
    }

    /**
     * Creates a CommunicationTokenRefreshOptions object
     *
     * @param tokenRefresher     The callback function that acquires a fresh token
     *                           from the Communication Identity API, e.g. by
     *                           calling the CommunicationIdentityClient
     * @param refreshProactively Determines whether the token should be proactively
     *                           renewed prior to its expiry or on demand.
     * @param initialToken       The optional serialized JWT token
     * @deprecated Use {@link #CommunicationTokenRefreshOptions(Supplier)} instead
     *             and chain fluent setters {@link #setRefreshProactively(boolean)},
     *             {@link #setInitialToken(String)}
     */
    @Deprecated
    public CommunicationTokenRefreshOptions(Supplier<Mono<String>> tokenRefresher, boolean refreshProactively,
            String initialToken) {
        this.tokenRefresher = tokenRefresher;
        this.refreshProactively = refreshProactively;
        this.initialToken = initialToken;
    }

    /**
     * Creates a CommunicationTokenRefreshOptions object
     *
     * @param tokenRefresher The callback function that acquires a fresh token from
     *                       the Communication Identity API, e.g. by calling the
     *                       CommunicationIdentityClient
     *                       The returned token must be valid (its expiration date
     *                       must be set in the future).
     */
    public CommunicationTokenRefreshOptions(Supplier<Mono<String>> tokenRefresher) {
        this.tokenRefresher = tokenRefresher;
        this.refreshProactively = false;
        this.initialToken = null;
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
     * Set whether the token should be proactively renewed prior to its expiry or on
     * demand.
     *
     * @param refreshProactively the refreshProactively value to set.
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
     * @param initialToken the initialToken value to set.
     * @return the CommunicationTokenRefreshOptions object itself.
     */
    public CommunicationTokenRefreshOptions setInitialToken(String initialToken) {
        this.initialToken = initialToken;
        return this;
    }

}
