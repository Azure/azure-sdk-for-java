// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.core.implementation.credential.provider;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import com.azure.spring.cloud.core.properties.AzureProperties;

/**
 * A provider for {@link TokenCredential} to provide token credential from the bean of the default global
 * {@link DefaultAzureCredential} or {@link AzureTokenCredentialResolver}.
 */
public class TokenCredentialProvider {

    private final TokenCredential defaultTokenCredential;

    private final AzureTokenCredentialResolver azureTokenCredentialResolver;

    private final AzureProperties azureGlobalProperties;

    private final AzureProperties azureServiceProperties;

    private final boolean useDefaultCredential;

    public TokenCredentialProvider(TokenCredential defaultTokenCredential,
                                   AzureTokenCredentialResolver azureTokenCredentialResolver,
                                   AzureProperties azureGlobalProperties, AzureProperties azureServiceProperties) {
        this.defaultTokenCredential = defaultTokenCredential;
        this.azureTokenCredentialResolver = azureTokenCredentialResolver;
        this.azureGlobalProperties = azureGlobalProperties;
        this.azureServiceProperties = azureServiceProperties;
        this.useDefaultCredential = ifToUseDefaultCredential();
    }

    /**
     * Get the {@link TokenCredential} from default token credential built from azure global properties when the global
     * properties are equal to a service specific properties in credential and profile. Otherwise, build the token
     * credential from the service specific properties.
     *
     * @return the {@link TokenCredential}
     */
    public TokenCredential getTokenCredential() {
        if (useDefaultCredential) {
            return defaultTokenCredential;
        }
        return azureTokenCredentialResolver.resolve(azureServiceProperties);
    }

    private boolean ifToUseDefaultCredential() {
        return azureGlobalProperties.getCredential().equals(azureServiceProperties.getCredential())
                && azureGlobalProperties.getProfile().equals(azureServiceProperties.getProfile());
    }

}
