package com.azure.identity;

import com.azure.identity.implementation.TokenCacheImpl;

/**
 * Represents In Memory Token Cache.
 */
class TokenCache {

    private TokenCacheImpl tokenCache;

    /**
     * Constructs an instance of {@link TokenCache}
     */
    TokenCache() {
        tokenCache = new TokenCacheImpl();
    }

    TokenCacheImpl getTokenCache() {
        return this.tokenCache;
    }
}
