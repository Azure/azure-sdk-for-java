// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.extensions.implementation.credential.provider;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.extensions.implementation.credential.TokenCredentialProviderOptions;

import java.util.concurrent.ConcurrentHashMap;

import static com.azure.identity.extensions.implementation.utils.StringUtils.getTokenCredentialCacheKey;

/**
 * Default cache tokenCredentialProvider implementation that provides tokenCredential instance.
 */
public class DefaultCacheTokenCredentialProvider implements TokenCredentialProvider {

    private static final ClientLogger LOGGER = new ClientLogger(DefaultCacheTokenCredentialProvider.class);

    private static final ConcurrentHashMap<String, TokenCredential> CACHE = new ConcurrentHashMap<>();

    private final TokenCredentialProviderOptions options;

    private final DefaultTokenCredentialProvider defaultProvider;

    DefaultCacheTokenCredentialProvider() {
        this(new TokenCredentialProviderOptions());
    }

    DefaultCacheTokenCredentialProvider(TokenCredentialProviderOptions options) {
        this.options = options;
        this.defaultProvider = new DefaultTokenCredentialProvider(this.options);
    }

    @Override
    public TokenCredential get() {
        String tokenCredentialCacheKey = getTokenCredentialCacheKey(options);
        TokenCredential cachedTokenCredential = CACHE.get(tokenCredentialCacheKey);
        if (cachedTokenCredential != null) {
            LOGGER.verbose("Returning token credential from cache.");
            return cachedTokenCredential;
        }

        TokenCredential tokenCredential = defaultProvider.get();
        CACHE.put(tokenCredentialCacheKey, tokenCredential);
        LOGGER.verbose("The token credential cached.");
        return tokenCredential;
    }
}
