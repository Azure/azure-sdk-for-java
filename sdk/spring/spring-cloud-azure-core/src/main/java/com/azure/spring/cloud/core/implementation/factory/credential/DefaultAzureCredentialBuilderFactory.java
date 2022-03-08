// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.factory.credential;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;

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
        AzureProfileOptionsProvider.ProfileOptions profile = azureProperties.getProfile();
        PropertyMapper mapper = new PropertyMapper();
        mapper.from(profile.getTenantId()).to(builder::tenantId);
        mapper.from(profile.getEnvironment().getActiveDirectoryEndpoint()).to(builder::authorityHost);
        mapper.from(azureProperties.getCredential().getClientId())
              .when(p -> azureProperties.getCredential().isManagedIdentityEnabled())
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
