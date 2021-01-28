// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

/**
 * Options for refreshing CommunicationTokenCredential
 */
public class CommunicationTokenRefreshOptions {
    private final TokenRefresher tokenRefresher;
    private final boolean refreshProactively;
    private final String token;

    /**
     * Creates a CommunicationTokenRefreshOptions object
     *
     * @param tokenRefresher the token refresher to provide capacity to fetch fresh token
     * @param refreshProactively when set to true, turn on proactive fetching to call
     *                           tokenRefresher before token expiry by minutes set
     *                           with setCallbackOffsetMinutes or default value of
     *                           two minutes
     */
    public CommunicationTokenRefreshOptions(TokenRefresher tokenRefresher, boolean refreshProactively) {
        this.tokenRefresher = tokenRefresher;
        this.refreshProactively = refreshProactively;
        this.token = null;
    }

     /**
     * Creates a CommunicationTokenRefreshOptions object
     *
     * @param tokenRefresher the token refresher to provide capacity to fetch fresh token
     * @param refreshProactively when set to true, turn on proactive fetching to call
     *                           tokenRefresher before token expiry by minutes set
     *                           with setCallbackOffsetMinutes or default value of
     *                           two minutes
     * @param token the optional serialized JWT token
     */
    public CommunicationTokenRefreshOptions(TokenRefresher tokenRefresher, boolean refreshProactively, String token) {
        this.tokenRefresher = tokenRefresher;
        this.refreshProactively = refreshProactively;
        this.token = token;
    }

    /**
     * @return the token refresher to provide capacity to fetch fresh token
     */
    public TokenRefresher getTokenRefresher() {
        return tokenRefresher;
    }

    /**
     * @return whether or not to refresh token proactively
     */
    public boolean getRefreshProactively() {
        return refreshProactively;
    }

    /**
     * @return the serialized JWT token
     */
    public String getToken() {
        return token;
    }
}
