// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.credential.descriptor;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureNamedKeyCredentialResolver;

import java.util.function.Consumer;

/**
 * A descriptor describes the named key authentication.
 */
public final class NamedKeyAuthenticationDescriptor implements AuthenticationDescriptor<AzureNamedKeyCredential> {

    private final Consumer<AzureNamedKeyCredential> consumer;

    /**
     * Create a {@link NamedKeyAuthenticationDescriptor} instance with the consumer to consume the resolved credential.
     * @param consumer The consumer to consume the resolved credential.
     */
    public NamedKeyAuthenticationDescriptor(Consumer<AzureNamedKeyCredential> consumer) {
        this.consumer = consumer;
    }


    @Override
    public Class<AzureNamedKeyCredential> getAzureCredentialType() {
        return AzureNamedKeyCredential.class;
    }

    @Override
    public AzureCredentialResolver<AzureNamedKeyCredential> getAzureCredentialResolver() {
        return new AzureNamedKeyCredentialResolver();
    }

    @Override
    public Consumer<AzureNamedKeyCredential> getConsumer() {
        return consumer;
    }
}
