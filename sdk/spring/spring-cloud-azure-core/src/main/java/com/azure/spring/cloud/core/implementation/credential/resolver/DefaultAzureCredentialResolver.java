// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.credential.resolver;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.core.implementation.factory.credential.DefaultAzureCredentialBuilderFactory;
import com.azure.spring.cloud.core.properties.AzureProperties;

public class DefaultAzureCredentialResolver implements AzureCredentialResolver<TokenCredential> {

    private final DefaultAzureCredentialBuilderFactory builderFactory;

    public DefaultAzureCredentialResolver(DefaultAzureCredentialBuilderFactory builderFactory) {
        this.builderFactory = builderFactory;
    }

    DefaultAzureCredentialResolver() {
        this(null);
    }


    @Override
    public boolean isResolvable(AzureProperties properties) {
        return true;
    }

    @Override
    public TokenCredential resolve(AzureProperties properties) {
        String authorityHost = null;
        String tenantId = null;
        String clientId = properties.getCredential() == null ? null : properties.getCredential().getClientId();
        if (properties.getProfile() != null) {
            authorityHost = properties.getProfile().getEnvironment().getActiveDirectoryEndpoint();
            tenantId = properties.getProfile().getTenantId();
        }

        if (authorityHost == null) {
            authorityHost = AzureEnvironment.AZURE.getActiveDirectoryEndpoint();
        }

        DefaultAzureCredentialBuilderFactory factory = this.builderFactory == null
            ? new DefaultAzureCredentialBuilderFactory(properties) : this.builderFactory;

        return factory
            .build()
            .authorityHost(authorityHost)
            .tenantId(tenantId)
            .managedIdentityClientId(clientId)
            .build();


    }
}
