// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.credential.AccessToken;

import java.time.OffsetDateTime;

public class AccessTokenCacheInfo {
    private AccessToken cache;
    private OffsetDateTime nextTokenRefresh;

    /**
     * Create an Instance of Access Token Cache Info.
     *
     * @param cache the cached token
     * @param nextTokenRefresh the next token refresh time
     */
    public AccessTokenCacheInfo(AccessToken cache, OffsetDateTime nextTokenRefresh) {
        this.cache = cache;
        this.nextTokenRefresh = nextTokenRefresh;
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
    public OffsetDateTime getNextTokenRefresh() {
        return nextTokenRefresh;
    }
}
