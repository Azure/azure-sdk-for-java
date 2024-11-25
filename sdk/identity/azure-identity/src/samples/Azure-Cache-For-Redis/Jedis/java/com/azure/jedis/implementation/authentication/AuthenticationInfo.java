// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.jedis.implementation.authentication;

/**
 * The authentication information.
 */
public final class AuthenticationInfo {
    private final String authToken;
    private final boolean shouldAuthenticate;
    private final boolean refreshedToken;

    /**
     * Creates an instance of {@link AuthenticationInfo}.
     *
     * @param authToken The authentication token.
     * @param shouldAuthenticate Whether authentication should happen.
     */
    public AuthenticationInfo(String authToken, boolean shouldAuthenticate) {
        this(authToken, shouldAuthenticate, false);
    }

    /**
     * Creates an instance of {@link AuthenticationInfo}.
     *
     * @param authToken The authentication token.
     * @param shouldAuthenticate Whether authentication should happen.
     * @param refreshedToken Whether the token was refreshed.
     */
    public AuthenticationInfo(String authToken, boolean shouldAuthenticate, boolean refreshedToken) {
        this.authToken = authToken;
        this.shouldAuthenticate = shouldAuthenticate;
        this.refreshedToken = refreshedToken;
    }

    /**
     * Gets the authentication token.
     *
     * @return The authentication token.
     */
    public String getAuthToken() {
        return authToken;
    }

    /**
     * Whether authentication should happen.
     *
     * @return Whether authentication should happen.
     */
    public boolean isShouldAuthenticate() {
        return shouldAuthenticate;
    }

    /**
     * Whether the token was refreshed.
     *
     * @return Whether the token was refreshed.
     */
    public boolean isRefreshedToken() {
        return refreshedToken;
    }
}
