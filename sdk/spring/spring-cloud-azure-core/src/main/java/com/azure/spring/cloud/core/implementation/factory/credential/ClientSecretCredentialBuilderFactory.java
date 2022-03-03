// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.factory.credential;

import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.spring.cloud.core.aware.authentication.TokenCredentialOptionsAware;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.core.properties.AzureProperties;

/**
 * A credential builder factory for the {@link ClientSecretCredentialBuilder}.
 */
public class ClientSecretCredentialBuilderFactory extends AzureAadCredentialBuilderFactory<ClientSecretCredentialBuilder> {

    /**
     * Create a {@link ClientSecretCredentialBuilderFactory} instance with {@link AzureProperties}.
     * @param azureProperties The Azure properties.
     */
    public ClientSecretCredentialBuilderFactory(AzureProperties azureProperties) {
        super(azureProperties);
    }

    @Override
    protected ClientSecretCredentialBuilder createBuilderInstance() {
        return new ClientSecretCredentialBuilder();
    }

    @Override
    protected void configureService(ClientSecretCredentialBuilder builder) {
        super.configureService(builder);

        AzureProperties azureProperties = getAzureProperties();
        TokenCredentialOptionsAware.TokenCredential credential = azureProperties.getCredential();
        PropertyMapper map = new PropertyMapper();

        map.from(credential.getClientSecret()).to(builder::clientSecret);
    }

}
