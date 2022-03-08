// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.factory.credential;

import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.provider.authentication.TokenCredentialOptionsProvider;

/**
 * A credential builder factory for the {@link ManagedIdentityCredentialBuilder}.
 */
public class ManagedIdentityCredentialBuilderFactory extends AbstractAzureCredentialBuilderFactory<ManagedIdentityCredentialBuilder> {

    /**
     * Create a {@link ManagedIdentityCredentialBuilderFactory} instance with {@link AzureProperties}.
     * @param azureProperties The Azure properties.
     */
    public ManagedIdentityCredentialBuilderFactory(AzureProperties azureProperties) {
        super(azureProperties);
    }

    @Override
    protected ManagedIdentityCredentialBuilder createBuilderInstance() {
        return new ManagedIdentityCredentialBuilder();
    }

    @Override
    protected void configureService(ManagedIdentityCredentialBuilder builder) {
        super.configureService(builder);

        AzureProperties azureProperties = getAzureProperties();
        TokenCredentialOptionsProvider.TokenCredentialOptions credential = azureProperties.getCredential();
        PropertyMapper map = new PropertyMapper();

        map.from(credential.getClientId()).when(p -> credential.isManagedIdentityEnabled()).to(builder::clientId);
    }

}
