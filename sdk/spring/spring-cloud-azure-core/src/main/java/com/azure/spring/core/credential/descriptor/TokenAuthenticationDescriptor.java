// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.credential.descriptor;

import com.azure.spring.core.credential.AzureCredentialResolver;
import com.azure.spring.core.credential.AzureCredentialType;
import com.azure.spring.core.credential.provider.AzureTokenCredentialProvider;
import com.azure.spring.core.implementation.credential.resolver.AzureTokenCredentialResolver;

import java.util.function.Consumer;

/**
 * A descriptor describes the token authentication.
 */
public final class TokenAuthenticationDescriptor implements AuthenticationDescriptor<AzureTokenCredentialProvider> {

    private final Consumer<AzureTokenCredentialProvider> consumer;

    /**
     * Create a {@link TokenAuthenticationDescriptor} instance with the consumer to consume the resolved credential.
     * @param consumer The consumer to consume the resolved credential.
     */
    public TokenAuthenticationDescriptor(Consumer<AzureTokenCredentialProvider> consumer) {
        this.consumer = consumer;
    }

    @Override
    public AzureCredentialType getAzureCredentialType() {
        return AzureCredentialType.TOKEN_CREDENTIAL;
    }

    @Override
    public AzureCredentialResolver<AzureTokenCredentialProvider> getAzureCredentialResolver() {
        return new AzureTokenCredentialResolver();
    }

    @Override
    public Consumer<AzureTokenCredentialProvider> getConsumer() {
        return consumer;
    }
}
