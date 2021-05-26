// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.impl;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.spring.cloud.context.core.api.CredentialsProvider;
import com.azure.spring.cloud.context.core.config.AzureProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link CredentialsProvider} implementation that provides credentials based on user-provided properties and
 * defaults.
 *
 * @author Warren Zhu
 */
public class DefaultCredentialsProvider implements CredentialsProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCredentialsProvider.class);

    private final TokenCredential credentials;

    public DefaultCredentialsProvider(AzureProperties azureProperties) {
        this.credentials = initCredentials(azureProperties);
    }

    private TokenCredential initCredentials(AzureProperties azureProperties) {
        final String clientId = azureProperties.getClientId();
        final String clientSecret = azureProperties.getClientSecret();
        final String tenantId = azureProperties.getTenantId();

        if (clientId != null && clientSecret != null && tenantId != null) {
            LOGGER.debug("Will use ClientSecretCredential");
            return new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tenantId(tenantId)
                .authorityHost(azureProperties.getEnvironment().getAzureEnvironment().getActiveDirectoryEndpoint())
                .build();
        }

        if (azureProperties.isMsiEnabled()) {
            final String managedIdentityClientId = azureProperties.getClientId();

            if (managedIdentityClientId != null) {
                LOGGER.debug("Will use MSI credentials with specified clientId");
                return new ManagedIdentityCredentialBuilder().clientId(managedIdentityClientId).build();
            }
        }

        LOGGER.debug("Will use MSI credentials");
        return new ManagedIdentityCredentialBuilder().build();
    }

    @Override
    public TokenCredential getCredential() {
        return this.credentials;
    }
}
