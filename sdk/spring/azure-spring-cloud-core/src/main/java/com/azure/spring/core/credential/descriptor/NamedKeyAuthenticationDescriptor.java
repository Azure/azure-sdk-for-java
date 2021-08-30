// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.credential.descriptor;

import com.azure.spring.core.credential.AzureCredentialType;
import com.azure.spring.core.credential.provider.AzureNamedKeyCredentialProvider;
import com.azure.spring.core.credential.resolver.AzureCredentialResolver;
import com.azure.spring.core.credential.resolver.AzureNamedKeyCredentialResolver;

import java.util.function.Consumer;

/**
 * A descriptor describes the named key authentication.
 */
public class NamedKeyAuthenticationDescriptor implements AuthenticationDescriptor<AzureNamedKeyCredentialProvider> {

    private final Consumer<AzureNamedKeyCredentialProvider> consumer;

    public NamedKeyAuthenticationDescriptor(Consumer<AzureNamedKeyCredentialProvider> consumer) {
        this.consumer = consumer;
    }

    @Override
    public AzureCredentialType azureCredentialType() {
        return AzureCredentialType.NAMED_KEY_CREDENTIAL;
    }

    @Override
    public AzureCredentialResolver<AzureNamedKeyCredentialProvider> azureCredentialResolver() {
        return new AzureNamedKeyCredentialResolver();
    }

    @Override
    public Consumer<AzureNamedKeyCredentialProvider> consumer() {
        return consumer;
    }
}
