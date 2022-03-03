// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.credential.descriptor;

import com.azure.core.credential.AzureSasCredential;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureSasCredentialResolver;

import java.util.function.Consumer;

/**
 * A descriptor describes the SAS authentication.
 */
public final class SasAuthenticationDescriptor implements AuthenticationDescriptor<AzureSasCredential> {

    private final Consumer<AzureSasCredential> consumer;

    /**
     * Create a {@link SasAuthenticationDescriptor} instance with the consumer to consume the resolved credential.
     * @param consumer The consumer to consume the resolved credential.
     */
    public SasAuthenticationDescriptor(Consumer<AzureSasCredential> consumer) {
        this.consumer = consumer;
    }

    @Override
    public Class<AzureSasCredential> getAzureCredentialType() {
        return AzureSasCredential.class;
    }

    @Override
    public AzureCredentialResolver<AzureSasCredential> getAzureCredentialResolver() {
        return new AzureSasCredentialResolver();
    }

    @Override
    public Consumer<AzureSasCredential> getConsumer() {
        return consumer;
    }
}
