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
        TokenCredentialCache tokenCredentialCache = getTokenCredentialCache();
        TokenCredentialProviderOptions providerOptions = getTokenCredentialProviderOptions();
        if (providerOptions != null && tokenCredentialCache != null && providerOptions.isTokenCredentialCacheEnabled()) {
            TokenCredential cachedTokenCredential = tokenCredentialCache.get(providerOptions);
            if (cachedTokenCredential != null) {
                LOGGER.verbose("Returning TokenCredential from cache.");
                return cachedTokenCredential;
            }
        }

        TokenCredential tokenCredential = get();
        if (providerOptions != null && tokenCredentialCache != null && providerOptions.isTokenCredentialCacheEnabled()) {
            tokenCredentialCache.put(providerOptions, tokenCredential);
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
