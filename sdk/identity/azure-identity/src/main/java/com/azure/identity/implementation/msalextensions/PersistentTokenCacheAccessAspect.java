// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.msalextensions;

import com.azure.identity.implementation.msalextensions.cachepersister.CachePersister;
import com.azure.identity.implementation.msalextensions.cachepersister.PlatformNotSupportedException;
import com.microsoft.aad.msal4j.ITokenCacheAccessAspect;
import com.microsoft.aad.msal4j.ITokenCacheAccessContext;

import java.io.IOException;

/**
 * Access Aspect for accessing the token cache
 * Allows for notifications for the cache before/after access so the lock can be used
 * */
public class PersistentTokenCacheAccessAspect implements ITokenCacheAccessAspect {

    private CachePersister cachePersister;

    /**
     * Default constructor, creates a CachePersister object
     *
     * @throws IOException from errors in creating the CachePersister
     * @throws PlatformNotSupportedException  from errors in creating the CachePersister
     * */
    public PersistentTokenCacheAccessAspect() throws IOException, PlatformNotSupportedException {
        cachePersister = new CachePersister.Builder().build();
    }

    /**
     * Constructor with a custom CachePersister object
     *
     * @param customCachePersister
     * */
    public PersistentTokenCacheAccessAspect(CachePersister customCachePersister) {
        cachePersister = customCachePersister;
    }

    /**
     * Loads token cache to memory using CachePersister - deserialize data in file to Token Cache class
     *
     * @param  iTokenCacheAccessContext
     * */
    public void beforeCacheAccess(ITokenCacheAccessContext iTokenCacheAccessContext) {

        byte[] bytes = cachePersister.readCache();
        String data = new String(bytes);

        iTokenCacheAccessContext.tokenCache().deserialize(data);
    }

    /**
     * Reads memory and writes to token cache file using CachePersister
     *
     * @param iTokenCacheAccessContext
     * */
    public void afterCacheAccess(ITokenCacheAccessContext iTokenCacheAccessContext) {

        if (iTokenCacheAccessContext.hasCacheChanged()) {
            String newData = iTokenCacheAccessContext.tokenCache().serialize();
            cachePersister.writeCache(newData.getBytes());
        }
    }

    /**
     * Wrapper method to delete cache
     * */
    public void deleteCache() {
        cachePersister.deleteCache();
    }
}
