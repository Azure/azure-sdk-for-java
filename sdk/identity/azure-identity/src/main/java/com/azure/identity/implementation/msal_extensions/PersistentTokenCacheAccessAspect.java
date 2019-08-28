// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.azure.identity.implementation.msal_extensions;

import com.azure.identity.implementation.msal_extensions.cachePersister.CachePersister;
import com.azure.identity.implementation.msal_extensions.cachePersister.PlatformNotSupportedException;
import com.microsoft.aad.msal4j.ITokenCacheAccessAspect;
import com.microsoft.aad.msal4j.ITokenCacheAccessContext;

import java.io.IOException;

/*
 * Access Aspect for accessing the cache
 * Allows for notifications for the cache before/after access so the lock can be used
 * */
public class PersistentTokenCacheAccessAspect implements ITokenCacheAccessAspect {

    private CachePersister cachePersister;

    public PersistentTokenCacheAccessAspect() throws IOException, PlatformNotSupportedException {
        cachePersister = new CachePersister.Builder().build();
    }

    public PersistentTokenCacheAccessAspect(CachePersister customCachePersister) {
        cachePersister = customCachePersister;
    }

    /*
     * Load token cache to memory - deserialize data in file to Token Cache class
     * */
    public void beforeCacheAccess(ITokenCacheAccessContext iTokenCacheAccessContext) {

        byte[] bytes = cachePersister.readCache();
        String data = new String(bytes);

        iTokenCacheAccessContext.tokenCache().deserialize(data);
    }

    /*
     * Read memory and write to token cache file
     * */
    public void afterCacheAccess(ITokenCacheAccessContext iTokenCacheAccessContext) {

        if (iTokenCacheAccessContext.hasCacheChanged()) {
            String newData = iTokenCacheAccessContext.tokenCache().serialize();
            cachePersister.writeCache(newData.getBytes());
        }
    }

    public void deleteCache() {
        cachePersister.deleteCache();
    }
}
