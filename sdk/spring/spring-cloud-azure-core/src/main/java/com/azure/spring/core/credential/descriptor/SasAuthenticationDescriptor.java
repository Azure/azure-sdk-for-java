// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.credential.descriptor;

import com.azure.spring.core.credential.AzureCredentialResolver;
import com.azure.spring.core.credential.AzureCredentialType;
import com.azure.spring.core.credential.provider.AzureSasCredentialProvider;
import com.azure.spring.core.implementation.credential.resolver.AzureSasCredentialResolver;

import java.util.function.Consumer;

/**
 * A descriptor describes the SAS authentication.
 */
public final class SasAuthenticationDescriptor implements AuthenticationDescriptor<AzureSasCredentialProvider> {

    private final Consumer<AzureSasCredentialProvider> consumer;

    /**
     * Create a {@link SasAuthenticationDescriptor} instance with the consumer to consume the resolved credential.
     * @param consumer The consumer to consume the resolved credential.
     */
    public SasAuthenticationDescriptor(Consumer<AzureSasCredentialProvider> consumer) {
        this.consumer = consumer;
    }

    @Override
    public AzureCredentialType getAzureCredentialType() {
        return AzureCredentialType.SAS_CREDENTIAL;
    }

    @Override
    public AzureCredentialResolver<AzureSasCredentialProvider> getAzureCredentialResolver() {
        return new AzureSasCredentialResolver();
    }

    @Override
    public Consumer<AzureSasCredentialProvider> getConsumer() {
        return consumer;
    }
}
