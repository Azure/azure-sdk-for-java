// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.credential.descriptor;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureKeyCredentialResolver;

import java.util.function.Consumer;

/**
 * A descriptor describes the key authentication.
 */
public final class KeyAuthenticationDescriptor implements AuthenticationDescriptor<AzureKeyCredential> {

    private final Consumer<AzureKeyCredential> consumer;

    /**
     * Create a {@link KeyAuthenticationDescriptor} instance with the consumer to consume the resolved credential.
     * @param consumer The consumer to consume the resolved credential.
     */
    public KeyAuthenticationDescriptor(Consumer<AzureKeyCredential> consumer) {
        this.consumer = consumer;
    }

    @Override
    public Class<AzureKeyCredential> getAzureCredentialType() {
        return AzureKeyCredential.class;
    }

    @Override
    public AzureCredentialResolver<AzureKeyCredential> getAzureCredentialResolver() {
        return new AzureKeyCredentialResolver();
    }

    @Override
    public Consumer<AzureKeyCredential> getConsumer() {
        return consumer;
    }
}
