// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.credential.resolver;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.core.implementation.factory.credential.ClientSecretCredentialBuilderFactory;
import com.azure.spring.cloud.core.properties.AzureProperties;
import org.springframework.util.StringUtils;

public class ClientSecretCredentialResolver implements AzureCredentialResolver<TokenCredential> {

    private final ClientSecretCredentialBuilderFactory builderFactory;

    public ClientSecretCredentialResolver(ClientSecretCredentialBuilderFactory builderFactory) {
        this.builderFactory = builderFactory;
    }

    ClientSecretCredentialResolver() {
        this(null);
    }


    @Override
    public boolean isResolvable(AzureProperties properties) {
        if (properties == null || properties.getCredential() == null || properties.getProfile() == null) {
            return false;
        }

        return StringUtils.hasText(properties.getProfile().getTenantId())
            && StringUtils.hasText(properties.getCredential().getClientId())
            && StringUtils.hasText(properties.getCredential().getClientSecret());
    }

    @Override
    public TokenCredential resolve(AzureProperties properties) {
        String authorityHost = properties.getProfile().getEnvironment().getActiveDirectoryEndpoint();

        if (authorityHost == null) {
            authorityHost = AzureEnvironment.AZURE.getActiveDirectoryEndpoint();
        }

        ClientSecretCredentialBuilderFactory factory = this.builderFactory == null
            ? new ClientSecretCredentialBuilderFactory(properties) : this.builderFactory;

        return factory
            .build()
            .authorityHost(authorityHost)
            .clientId(properties.getCredential().getClientId())
            .clientSecret(properties.getCredential().getClientSecret())
            .tenantId(properties.getProfile().getTenantId())
            .build();
    }
}
