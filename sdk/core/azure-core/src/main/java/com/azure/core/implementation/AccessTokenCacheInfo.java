package com.azure.core.implementation;

import com.azure.core.credential.AccessToken;

import java.time.OffsetDateTime;

public class AccessTokenCacheInfo {
    private AccessToken cache;
    private OffsetDateTime nextTokenRefresh;

    public AccessTokenCacheInfo(AccessToken cache, OffsetDateTime nextTokenRefresh) {
        this.cache = cache;
        this.nextTokenRefresh = nextTokenRefresh;
    }

    /**
     * Get the cached token.
     * @return the cached token.
     */
    public AccessToken getCache() {
        return cache;
    }

    /**
     * Get the time to refresh the token next.
     * @return
     */
    public OffsetDateTime getNextTokenRefresh() {
        return nextTokenRefresh;
    }
}
