// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.extensions.implementation.credential.provider;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.extensions.implementation.cache.TokenCredentialCache;
import com.azure.identity.extensions.implementation.credential.TokenCredentialProviderOptions;

import java.util.function.Supplier;

/**
 * Interface to be implemented by classes that wish to provide the {@link TokenCredential}.
 */
@FunctionalInterface
public interface TokenCredentialProvider extends Supplier<TokenCredential> {

    ClientLogger LOGGER = new ClientLogger(TokenCredentialProvider.class);

    default TokenCredential get(TokenCredentialProviderOptions options) {
        return get();
    }

    default TokenCredentialCache getTokenCredentialCache() {
        return null;
    }

    default TokenCredentialProviderOptions getTokenCredentialProviderOptions() {
        return null;
    }

    default TokenCredential getFromCache() {
        TokenCredentialCache cache = getTokenCredentialCache();
        TokenCredentialProviderOptions options = getTokenCredentialProviderOptions();
        if (options != null && cache != null && options.isTokenCredentialCacheEnabled()) {
            TokenCredential cachedTokenCredential = cache.get(cache.getKey(options));
            if (cachedTokenCredential != null) {
                LOGGER.verbose("Returning token credential from cache.");
                return cachedTokenCredential;
            }
        }

        TokenCredential tokenCredential = get();
        if (options != null && cache != null && options.isTokenCredentialCacheEnabled()) {
            cache.put(cache.getKey(options), tokenCredential);
            LOGGER.verbose("The token credential cached.");
        }
        return tokenCredential;
    }

    /**
     * Create TokenCredentialProvider instance
     * @param options Used by {@link TokenCredentialProvider} to create {@link TokenCredentialProvider} instance.
     * @return TokenCredentialProvider instance.
     */
    static TokenCredentialProvider createDefault(TokenCredentialProviderOptions options) {
        return TokenCredentialProviders.createInstance(options);
    }
}
