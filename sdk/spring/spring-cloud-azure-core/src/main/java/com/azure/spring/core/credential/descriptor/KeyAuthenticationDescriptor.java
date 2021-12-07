// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.credential.descriptor;

import com.azure.spring.core.credential.AzureCredentialType;
import com.azure.spring.core.credential.provider.AzureKeyCredentialProvider;
import com.azure.spring.core.credential.AzureCredentialResolver;
import com.azure.spring.core.implementation.credential.resolver.AzureKeyCredentialResolver;

import java.util.function.Consumer;

/**
 * A descriptor describes the key authentication.
 */
public final class KeyAuthenticationDescriptor implements AuthenticationDescriptor<AzureKeyCredentialProvider> {

    private final Consumer<AzureKeyCredentialProvider> consumer;

    /**
     * Create a {@link KeyAuthenticationDescriptor} instance with the consumer to consume the resolved credential.
     * @param consumer The consumer to consume the resolved credential.
     */
    public KeyAuthenticationDescriptor(Consumer<AzureKeyCredentialProvider> consumer) {
        this.consumer = consumer;
    }

    @Override
    public AzureCredentialType getAzureCredentialType() {
        return AzureCredentialType.KEY_CREDENTIAL;
    }

    @Override
    public AzureCredentialResolver<AzureKeyCredentialProvider> getAzureCredentialResolver() {
        return new AzureKeyCredentialResolver();
    }

    @Override
    public Consumer<AzureKeyCredentialProvider> getConsumer() {
        return consumer;
    }
}
