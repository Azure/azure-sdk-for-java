// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.implementation.credential.provider;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.providers.jdbc.implementation.credential.TokenCredentialProviderOptions;

import java.util.function.Supplier;

/**
 * Interface to be implemented by classes that wish to provide the {@link TokenCredential}.
 */
@FunctionalInterface
public interface TokenCredentialProvider extends Supplier<TokenCredential> {

    default TokenCredential get(TokenCredentialProviderOptions options) {
        return get();
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
