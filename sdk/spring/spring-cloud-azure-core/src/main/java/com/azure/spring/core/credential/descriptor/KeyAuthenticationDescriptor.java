// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.credential.descriptor;

import com.azure.spring.core.credential.AzureCredentialType;
import com.azure.spring.core.credential.provider.AzureKeyCredentialProvider;
import com.azure.spring.core.credential.resolver.AzureCredentialResolver;
import com.azure.spring.core.credential.resolver.AzureKeyCredentialResolver;

import java.util.function.Consumer;

/**
 * A descriptor describes the key authentication.
 */
public class KeyAuthenticationDescriptor implements AuthenticationDescriptor<AzureKeyCredentialProvider> {

    private final Consumer<AzureKeyCredentialProvider> consumer;

    public KeyAuthenticationDescriptor(Consumer<AzureKeyCredentialProvider> consumer) {
        this.consumer = consumer;
    }

    @Override
    public AzureCredentialType azureCredentialType() {
        return AzureCredentialType.KEY_CREDENTIAL;
    }

    @Override
    public AzureCredentialResolver<AzureKeyCredentialProvider> azureCredentialResolver() {
        return new AzureKeyCredentialResolver();
    }

    @Override
    public Consumer<AzureKeyCredentialProvider> consumer() {
        return consumer;
    }
}
