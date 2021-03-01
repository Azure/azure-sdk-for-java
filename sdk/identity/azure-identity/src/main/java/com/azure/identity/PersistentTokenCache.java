package com.azure.identity;

import com.azure.identity.implementation.PersistentTokenCacheImpl;

/**
 * Represents the Persistence Token Cache to persist the access tokens to disk.
 */
public final class PersistentTokenCache {
    private PersistentTokenCacheImpl tokenCache;

    public PersistentTokenCache() {
        tokenCache = new PersistentTokenCacheImpl();
    }

    /**
     * Allows to use an unprotected file specified by <code>cacheFileLocation()</code> instead of
     * Gnome keyring on Linux. This is restricted by default.
     *
     * @return An updated instance of the cache.
     */
    public void setAllowUnencryptedStorage(boolean allowUnencryptedStorage) {
        tokenCache.setAllowUnencryptedStorage(allowUnencryptedStorage);
    }

    /**
     * Set the name uniquely identifying the {@link PersistentTokenCache}.
     * @param name the name of the cache
     * @return the updated instance of the cache.
     */
    public PersistentTokenCache setName(String name) {
        tokenCache.setName(name);
        return this;
    }

    PersistentTokenCacheImpl getTokenCacheImpl() {
        return this.tokenCache;
    }
}
