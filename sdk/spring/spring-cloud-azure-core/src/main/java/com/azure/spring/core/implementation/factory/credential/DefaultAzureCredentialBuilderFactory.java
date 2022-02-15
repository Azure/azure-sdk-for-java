// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.implementation.factory.credential;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.core.aware.AzureProfileAware;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.PropertyMapper;

import java.util.concurrent.ExecutorService;

/**
 * A credential builder factory for the {@link DefaultAzureCredentialBuilder}.
 */
public class DefaultAzureCredentialBuilderFactory extends AbstractAzureCredentialBuilderFactory<DefaultAzureCredentialBuilder> {

    private ExecutorService executorService = null;

    /**
     * Create a {@link DefaultAzureCredentialBuilderFactory} instance with {@link AzureProperties}.
     * @param azureProperties The Azure properties.
     */
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
        PropertyMapper mapper = new PropertyMapper();
        mapper.from(profile.getTenantId()).to(builder::tenantId);
        mapper.from(profile.getEnvironment().getActiveDirectoryEndpoint()).to(builder::authorityHost);
        mapper.from(azureProperties.getCredential().getClientId())
              .when(p -> azureProperties.getCredential().isEnableManagedIdentity())
              .to(builder::managedIdentityClientId);
        mapper.from(executorService).to(builder::executorService);
    }

    /**
     * Set the {@link ExecutorService}. The {@link ExecutorService} will be applied to the underneath identity client.
     * @param executorService The executor service.
     */
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }
}
