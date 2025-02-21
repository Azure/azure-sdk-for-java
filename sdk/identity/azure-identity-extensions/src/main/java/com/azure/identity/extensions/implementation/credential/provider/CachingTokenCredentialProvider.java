// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.extensions.implementation.credential.provider;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.extensions.implementation.credential.TokenCredentialProviderOptions;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Caching tokenCredentialProvider implementation that provides tokenCredential instance.
 */
public class CachingTokenCredentialProvider implements TokenCredentialProvider {

    private static final ClientLogger LOGGER = new ClientLogger(CachingTokenCredentialProvider.class);

    private static final ConcurrentHashMap<String, TokenCredential> CACHE = new ConcurrentHashMap<>();

    private final TokenCredentialProviderOptions defaultOptions;

    private final TokenCredentialProvider delegate;

    /**
     * CachingTokenCredentialProvider constructor.
     * @param defaultOptions the {@link TokenCredentialProviderOptions} for the delegate {@link TokenCredentialProvider} initialization.
     * @param tokenCredentialProvider the delegate {@link TokenCredentialProvider}.
     */
    public CachingTokenCredentialProvider(TokenCredentialProviderOptions defaultOptions,
        TokenCredentialProvider tokenCredentialProvider) {
        this.defaultOptions = defaultOptions;
        this.delegate = tokenCredentialProvider;
    }

    @Override
    public TokenCredential get() {
        return getOrCreate(CACHE, this.defaultOptions, this.delegate,
            tokenCredentialProvider -> tokenCredentialProvider.get());
    }

    @Override
    public TokenCredential get(TokenCredentialProviderOptions options) {
        return getOrCreate(CACHE, options, this.delegate,
            tokenCredentialProvider -> tokenCredentialProvider.get(options));
    }

    private static TokenCredential getOrCreate(Map<String, TokenCredential> cache,
        TokenCredentialProviderOptions options, TokenCredentialProvider delegate,
        Function<TokenCredentialProvider, TokenCredential> fn) {
        String tokenCredentialCacheKey = convertToTokenCredentialCacheKey(options);

        if (cache.containsKey(tokenCredentialCacheKey)) {
            LOGGER.verbose("Retrieving token credential from cache.");
        } else {
            LOGGER.verbose("Caching token credential.");
            cache.put(tokenCredentialCacheKey, fn.apply(delegate));
        }

        return cache.get(tokenCredentialCacheKey);
    }

    private static String convertToTokenCredentialCacheKey(TokenCredentialProviderOptions options) {
        if (options == null) {
            return CachingTokenCredentialProvider.class.getSimpleName();
        }

        return Arrays
            .stream(new String[] {
                options.getTenantId(),
                options.getClientId(),
                options.getClientCertificatePath(),
                options.getUsername(),
                String.valueOf(options.isManagedIdentityEnabled()),
                options.getTokenCredentialProviderClassName(),
                options.getTokenCredentialBeanName() })
            .map(option -> option == null ? "" : option)
            .collect(Collectors.joining(","));
    }
}
