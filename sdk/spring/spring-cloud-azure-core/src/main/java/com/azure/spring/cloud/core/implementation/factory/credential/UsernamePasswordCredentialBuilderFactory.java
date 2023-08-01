// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.factory.credential;

import com.azure.identity.UsernamePasswordCredentialBuilder;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.provider.authentication.TokenCredentialOptionsProvider;

/**
 * A credential builder factory for the {@link UsernamePasswordCredentialBuilder}.
 */
public class UsernamePasswordCredentialBuilderFactory extends AzureAadCredentialBuilderFactory<UsernamePasswordCredentialBuilder> {

    /**
     * Create a {@link UsernamePasswordCredentialBuilderFactory} instance with {@link AzureProperties}.
     * @param azureProperties The Azure properties.
     */
    public UsernamePasswordCredentialBuilderFactory(AzureProperties azureProperties) {
        super(azureProperties);
    }

    @Override
    protected UsernamePasswordCredentialBuilder createBuilderInstance() {
        return new UsernamePasswordCredentialBuilder();
    }

    @Override
    protected void configureService(UsernamePasswordCredentialBuilder builder) {
        super.configureService(builder);

        AzureProperties azureProperties = getAzureProperties();
        TokenCredentialOptionsProvider.TokenCredentialOptions credential = azureProperties.getCredential();
        PropertyMapper map = new PropertyMapper();

        map.from(credential.getUsername()).to(builder::username);
        map.from(credential.getPassword()).to(builder::password);
    }

}
