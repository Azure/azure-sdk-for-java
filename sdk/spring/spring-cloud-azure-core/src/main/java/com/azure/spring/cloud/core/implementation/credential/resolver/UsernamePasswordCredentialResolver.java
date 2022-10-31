// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.credential.resolver;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.core.implementation.factory.credential.UsernamePasswordCredentialBuilderFactory;
import com.azure.spring.cloud.core.properties.AzureProperties;
import org.springframework.util.StringUtils;

public class UsernamePasswordCredentialResolver implements AzureCredentialResolver<TokenCredential> {

    private final UsernamePasswordCredentialBuilderFactory builderFactory;

    public UsernamePasswordCredentialResolver(UsernamePasswordCredentialBuilderFactory builderFactory) {
        this.builderFactory = builderFactory;
    }

    UsernamePasswordCredentialResolver() {
        this(null);
    }


    @Override
    public boolean isResolvable(AzureProperties properties) {
        if (properties == null || properties.getCredential() == null) {
            return false;
        }

        return StringUtils.hasText(properties.getCredential().getClientId())
            && StringUtils.hasText(properties.getCredential().getUsername())
            && StringUtils.hasText(properties.getCredential().getPassword());
    }

    @Override
    public TokenCredential resolve(AzureProperties properties) {
        String authorityHost = null;
        String tenantId = null;
        if (properties.getProfile() != null) {
            authorityHost = properties.getProfile().getEnvironment().getActiveDirectoryEndpoint();
            tenantId = properties.getProfile().getTenantId();
        }

        if (authorityHost == null) {
            authorityHost = AzureEnvironment.AZURE.getActiveDirectoryEndpoint();
        }

        UsernamePasswordCredentialBuilderFactory factory = this.builderFactory == null
            ? new UsernamePasswordCredentialBuilderFactory(properties) : this.builderFactory;

        return factory
            .build()
            .authorityHost(authorityHost)
            .username(properties.getCredential().getUsername())
            .password(properties.getCredential().getPassword())
            .clientId(properties.getCredential().getClientId())
            .tenantId(tenantId)
            .build();
    }
}
