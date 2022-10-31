// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.credential.resolver;

import com.azure.core.credential.TokenCredential;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.core.implementation.factory.credential.ManagedIdentityCredentialBuilderFactory;
import com.azure.spring.cloud.core.properties.AzureProperties;

public class ManagedIdentityCredentialResolver implements AzureCredentialResolver<TokenCredential> {

    private final ManagedIdentityCredentialBuilderFactory builderFactory;

    public ManagedIdentityCredentialResolver(ManagedIdentityCredentialBuilderFactory builderFactory) {
        this.builderFactory = builderFactory;
    }

    ManagedIdentityCredentialResolver() {
        this(null);
    }


    @Override
    public boolean isResolvable(AzureProperties properties) {
        if (properties == null || properties.getCredential() == null) {
            return false;
        }

        return properties.getCredential().isManagedIdentityEnabled();
    }

    @Override
    public TokenCredential resolve(AzureProperties properties) {
        ManagedIdentityCredentialBuilderFactory factory = this.builderFactory == null
            ? new ManagedIdentityCredentialBuilderFactory(properties) : this.builderFactory;

        return factory
            .build()
            .clientId(properties.getCredential().getClientId())
            .build();
    }
}
