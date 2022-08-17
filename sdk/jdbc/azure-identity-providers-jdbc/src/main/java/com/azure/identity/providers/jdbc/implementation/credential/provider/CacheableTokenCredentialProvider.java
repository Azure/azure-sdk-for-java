// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.implementation.credential.provider;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.providers.jdbc.implementation.cache.StaticAccessTokenCache;
import com.azure.identity.providers.jdbc.api.credential.provider.TokenCredentialProvider;
import com.azure.identity.providers.jdbc.implementation.credential.CacheableTokenCredential;
import com.azure.identity.providers.jdbc.implementation.credential.TokenCredentialProviderOptions;

/**
 * TokenCredentialProvider implementation that provides CacheableTokenCredential .
 */
public class CacheableTokenCredentialProvider implements TokenCredentialProvider {

    private final StaticAccessTokenCache cache = StaticAccessTokenCache.getInstance();
    private final TokenCredentialProvider delegate;
    private final TokenCredentialProviderOptions options;

    public CacheableTokenCredentialProvider(TokenCredentialProvider delegate,
                                            TokenCredentialProviderOptions options) {
        this.delegate = delegate;
        this.options = options;
    }

    @Override
    public TokenCredential get() {
        return new CacheableTokenCredential(cache, delegate.get(), options);
    }
}
