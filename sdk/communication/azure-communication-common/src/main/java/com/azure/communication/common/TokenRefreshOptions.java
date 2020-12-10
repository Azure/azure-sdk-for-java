// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

/**
 * Options for refreshing CommunicationTokenCredential
 */
public class TokenRefreshOptions {
    private final TokenRefresher tokenRefresher;
    private final boolean refreshProactively;

    /**
     * Creates a TokenRefreshOptions object
     * 
     * @param tokenRefresher the token refresher to provide capacity to fetch fresh token
     * @param refreshProactively when set to true, turn on proactive fetching to call
     *                           tokenRefresher before token expiry by minutes set
     *                           with setCallbackOffsetMinutes or default value of
     *                           two minutes
     */
    public TokenRefreshOptions(TokenRefresher tokenRefresher, boolean refreshProactively) {
        this.tokenRefresher = tokenRefresher;
        this.refreshProactively = refreshProactively;
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
}
