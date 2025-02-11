// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.jedis.implementation.authentication;

import com.azure.core.credential.AccessToken;

/**
 * The result of an access token.
 */
public final class AccessTokenResult {
    private final AccessToken accessToken;
    private final boolean refreshedToken;

    /**
     * Creates an instance of {@link AccessTokenResult}.
     *
     * @param accessToken The {@link AccessToken} value.
     * @param refreshedToken Whether the token was refreshed.
     */
    public AccessTokenResult(AccessToken accessToken, boolean refreshedToken) {
        this.accessToken = accessToken;
        this.refreshedToken = refreshedToken;
    }

    /**
     * Gets the {@link AccessToken} value.
     *
     * @return The {@link AccessToken} value.
     */
    public AccessToken getAccessToken() {
        return accessToken;
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
