package com.azure.identity;

import com.azure.identity.implementation.PersistentTokenCacheImpl;

/**
 * Represents the Persistence Token Cache options used to setup the persistent access token cache.
 */
public final class TokenCachePersistenceOptions {
    private boolean allowUnencryptedStorage;
    private String name;

    public TokenCachePersistenceOptions() { }

    /**
     * Allows to use an unprotected file specified by <code>cacheFileLocation()</code> instead of
     * Gnome keyring on Linux. This is restricted by default.
     *
     * @return An updated instance of the options bag.
     */
    public TokenCachePersistenceOptions setAllowUnencryptedStorage(boolean allowUnencryptedStorage) {
        this.allowUnencryptedStorage = allowUnencryptedStorage;
        return this;
    }

    /**
     * Gets the status whether unencrypted storage is allowed for the persistent token cache.
     *
     * @return The status indicating if unencrypted storage is allowed for the persistent token cache.
     */
    public boolean isUnencryptedStorageAllowed() {
        return this.allowUnencryptedStorage;
    }

    /**
     * Set the name uniquely identifying the cache.
     *
     * @param name the name of the cache
     * @return the updated instance of the cache.
     */
    public TokenCachePersistenceOptions setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the name uniquely identifying the cache.
     *
     * @return the name of the cache.
     */
    public String getName() {
        return this.name;
    }
}
