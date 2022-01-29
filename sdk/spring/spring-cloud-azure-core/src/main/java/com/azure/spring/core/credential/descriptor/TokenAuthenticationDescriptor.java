// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.credential.descriptor;

import com.azure.spring.core.credential.AzureCredentialResolver;
import com.azure.spring.core.credential.AzureCredentialType;
import com.azure.spring.core.credential.provider.AzureTokenCredentialProvider;

import java.util.function.Consumer;

/**
 * A descriptor describes the token authentication.
 */
public final class TokenAuthenticationDescriptor implements AuthenticationDescriptor<AzureTokenCredentialProvider> {

    private final Consumer<AzureTokenCredentialProvider> consumer;
    private final AzureCredentialResolver<AzureTokenCredentialProvider> resolver;

    /**
     * Create a {@link TokenAuthenticationDescriptor} instance with the consumer to consume the resolved credential.
     * @param resolver A resolver to resolve token credential from properties.
     * @param consumer The consumer to consume the resolved credential.
     */
    public TokenAuthenticationDescriptor(AzureCredentialResolver<AzureTokenCredentialProvider> resolver,
                                         Consumer<AzureTokenCredentialProvider> consumer) {
        this.consumer = consumer;
        this.resolver = resolver;
    }

    @Override
    public AzureCredentialType getAzureCredentialType() {
        return AzureCredentialType.TOKEN_CREDENTIAL;
    }

    @Override
    public AzureCredentialResolver<AzureTokenCredentialProvider> getAzureCredentialResolver() {
        return this.resolver;
    }

    @Override
    public Consumer<AzureTokenCredentialProvider> getConsumer() {
        return consumer;
    }
}
