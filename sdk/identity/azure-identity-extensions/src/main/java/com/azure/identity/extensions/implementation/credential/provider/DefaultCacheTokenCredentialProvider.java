// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.extensions.implementation.credential.provider;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.extensions.implementation.cache.IdentityCache;
import com.azure.identity.extensions.implementation.cache.IdentityCacheHelper;
import com.azure.identity.extensions.implementation.credential.TokenCredentialProviderOptions;

import static com.azure.identity.extensions.implementation.utils.StringUtils.getTokenCredentialCacheKey;

/**
 * Default cache tokenCredentialProvider implementation that provides tokenCredential instance.
 */
public class DefaultCacheTokenCredentialProvider implements TokenCredentialProvider {

    private static final ClientLogger LOGGER = new ClientLogger(DefaultCacheTokenCredentialProvider.class);

    private final TokenCredentialProviderOptions options;

    private final IdentityCache<String, TokenCredential> tokenCredentialCache;

    private final DefaultTokenCredentialProvider defaultProvider;

    DefaultCacheTokenCredentialProvider() {
        this(new TokenCredentialProviderOptions());
    }

    DefaultCacheTokenCredentialProvider(TokenCredentialProviderOptions options) {
        this(options, null);
    }

    DefaultCacheTokenCredentialProvider(TokenCredentialProviderOptions options, IdentityCache<String, TokenCredential> tokenCredentialCache) {
        this.options = options;
        if (tokenCredentialCache == null) {
            this.tokenCredentialCache = IdentityCacheHelper.createTokenCredentialCacheInstance(options.getTokenCredentialCacheClassName());
        } else {
            this.tokenCredentialCache = tokenCredentialCache;
        }
        this.defaultProvider = new DefaultTokenCredentialProvider(this.options);
    }

    @Override
    public TokenCredential get() {
        String tokenCredentialCacheKey = getTokenCredentialCacheKey(options);
        TokenCredential cachedTokenCredential = tokenCredentialCache.get(tokenCredentialCacheKey);
        if (cachedTokenCredential != null) {
            LOGGER.verbose("Returning token credential from cache.");
            return cachedTokenCredential;
        }

        TokenCredential tokenCredential = defaultProvider.get();
        tokenCredentialCache.put(tokenCredentialCacheKey, tokenCredential);
        LOGGER.verbose("The token credential cached.");
        return tokenCredential;
    }
}
