// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.factory.credential;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.core.aware.AzureProfileAware;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.util.PropertyMapper;

import java.util.concurrent.ExecutorService;

/**
 *
 */
public class DefaultAzureCredentialBuilderFactory extends AbstractAzureCredentialBuilderFactory<DefaultAzureCredentialBuilder> {

    private ExecutorService executorService = null;

    public DefaultAzureCredentialBuilderFactory(AzureProperties azureProperties) {
        super(azureProperties);
    }

    @Override
    protected DefaultAzureCredentialBuilder createBuilderInstance() {
        return new DefaultAzureCredentialBuilder();
    }

    @Override
    protected void configureService(DefaultAzureCredentialBuilder builder) {
        AzureProperties azureProperties = getAzureProperties();
        AzureProfileAware.Profile profile = azureProperties.getProfile();
        PropertyMapper map = new PropertyMapper();
        map.from(profile.getTenantId()).to(builder::tenantId);
        map.from(profile.getEnvironment().getActiveDirectoryEndpoint()).to(builder::authorityHost);
        map.from(azureProperties.getCredential().getManagedIdentityClientId()).to(builder::managedIdentityClientId);
        map.from(executorService).to(builder::executorService);
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }
}
