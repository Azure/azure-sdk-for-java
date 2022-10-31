// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.credential.resolver;

import com.azure.core.credential.TokenCredential;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import com.azure.spring.cloud.core.provider.authentication.TokenCredentialOptionsProvider;

import java.util.Optional;

public class DefaultTokenCredentialResolver implements AzureCredentialResolver<TokenCredential> {

    private TokenCredential defaultTokenCredential;
    private String defaultTokenCredentialKey;

    public DefaultTokenCredentialResolver(TokenCredential defaultTokenCredential,
                                          AzureProperties defaultTokenCredentialProperties) {
        this.defaultTokenCredential = defaultTokenCredential;
        this.defaultTokenCredentialKey = buildCredentialKey(defaultTokenCredentialProperties);
    }

    @Override
    public boolean isResolvable(AzureProperties properties) {
        return this.defaultTokenCredentialKey.equals(buildCredentialKey(properties));
    }

    @Override
    public TokenCredential resolve(AzureProperties properties) {
        return this.defaultTokenCredential;
    }

    private static String buildCredentialKey(AzureProperties azureProperties) {
        String clientId = Optional.ofNullable(azureProperties)
            .map(AzureProperties::getCredential)
            .map(TokenCredentialOptionsProvider.TokenCredentialOptions::getClientId)
            .orElse(null);

        String tenantId = Optional.ofNullable(azureProperties)
            .map(AzureProperties::getProfile)
            .map(AzureProfileOptionsProvider.ProfileOptions::getTenantId)
            .orElse(null);

        String authorityHost = Optional.ofNullable(azureProperties)
            .map(AzureProperties::getProfile)
            .map(AzureProfileOptionsProvider.ProfileOptions::getEnvironment)
            .map(AzureProfileOptionsProvider.AzureEnvironmentOptions::getActiveDirectoryEndpoint)
            .orElse(null);

        return String.join("_", clientId, tenantId, authorityHost);
    }
}
