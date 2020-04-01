package com.azure.identity;

import com.microsoft.aad.msal4j.ITokenCacheAccessContext;
import com.microsoft.aad.msal4jextensions.PersistenceSettings;
import com.microsoft.aad.msal4jextensions.PersistenceTokenCacheAccessAspect;

import java.io.IOException;

public class ReadOnlyPersistenceTokenCacheAccessAspect extends PersistenceTokenCacheAccessAspect {
    public ReadOnlyPersistenceTokenCacheAccessAspect(PersistenceSettings persistenceSettings) throws IOException {
        super(persistenceSettings);
    }

    @Override
    public void afterCacheAccess(ITokenCacheAccessContext iTokenCacheAccessContext) {
        // do nothing
    }
}
