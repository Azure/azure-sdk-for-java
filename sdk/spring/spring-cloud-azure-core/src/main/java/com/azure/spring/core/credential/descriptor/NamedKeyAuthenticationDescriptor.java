// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.credential.descriptor;

import com.azure.spring.core.credential.AzureCredentialResolver;
import com.azure.spring.core.credential.AzureCredentialType;
import com.azure.spring.core.credential.provider.AzureNamedKeyCredentialProvider;
import com.azure.spring.core.implementation.credential.resolver.AzureNamedKeyCredentialResolver;

import java.util.function.Consumer;

/**
 * A descriptor describes the named key authentication.
 */
public final class NamedKeyAuthenticationDescriptor implements AuthenticationDescriptor<AzureNamedKeyCredentialProvider> {

    private final Consumer<AzureNamedKeyCredentialProvider> consumer;

    /**
     * Create a {@link NamedKeyAuthenticationDescriptor} instance with the consumer to consume the resolved credential.
     * @param consumer The consumer to consume the resolved credential.
     */
    public NamedKeyAuthenticationDescriptor(Consumer<AzureNamedKeyCredentialProvider> consumer) {
        this.consumer = consumer;
    }

    @Override
    public AzureCredentialType getAzureCredentialType() {
        return AzureCredentialType.NAMED_KEY_CREDENTIAL;
    }

    @Override
    public AzureCredentialResolver<AzureNamedKeyCredentialProvider> getAzureCredentialResolver() {
        return new AzureNamedKeyCredentialResolver();
    }

    @Override
    public Consumer<AzureNamedKeyCredentialProvider> getConsumer() {
        return consumer;
    }
}
