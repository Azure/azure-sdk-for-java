// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.implementation;

import io.clientcore.core.credentials.oauth.AccessToken;

import java.time.OffsetDateTime;

/**
 * The Access Token Cache Info holds the metadata used by {@link AccessTokenCache} to proactively refresh the token.
 */
public class AccessTokenCacheInfo {
    private final AccessToken cache;
    private final OffsetDateTime nextTokenRefreshAt;

    /**
     * Create an Instance of Access Token Cache Info.
     *
     * @param cache the cached token
     * @param nextTokenRefresh the next token refresh time
     */
    public AccessTokenCacheInfo(AccessToken cache, OffsetDateTime nextTokenRefresh) {
        this.cache = cache;
        this.nextTokenRefreshAt = nextTokenRefresh;
    }

    /**
     * Get the cached token.
     *
     * @return the cached token.
     */
    public AccessToken getCachedAccessToken() {
        return cache;
    }

    /**
     * Get the next refresh time for the token.
     *
     * @return the token refresh time.
     */
    public OffsetDateTime getNextTokenRefreshAt() {
        return nextTokenRefreshAt;
    }
}
