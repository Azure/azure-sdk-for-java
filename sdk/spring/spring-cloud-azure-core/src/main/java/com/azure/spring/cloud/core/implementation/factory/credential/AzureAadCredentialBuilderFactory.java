// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.factory.credential;

import com.azure.identity.AadCredentialBuilderBase;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;

import java.util.concurrent.ExecutorService;

/**
 * A credential builder factory for the {@link com.azure.identity.AadCredentialBuilderBase}.
 */
public abstract class AzureAadCredentialBuilderFactory<T extends AadCredentialBuilderBase<T>> extends AbstractAzureCredentialBuilderFactory<T> {

    private ExecutorService executorService = null;

    /**
     * Create a {@link AzureAadCredentialBuilderFactory} instance with {@link AzureProperties}.
     * @param azureProperties The Azure properties.
     */
    protected AzureAadCredentialBuilderFactory(AzureProperties azureProperties) {
        super(azureProperties);
    }

    @Override
    protected void configureService(T builder) {
        AzureProperties azureProperties = getAzureProperties();
        AzureProfileOptionsProvider.ProfileOptions profile = azureProperties.getProfile();
        PropertyMapper map = new PropertyMapper();
        map.from(azureProperties.getCredential().getClientId()).to(builder::clientId);
        map.from(profile.getTenantId()).to(builder::tenantId);
        map.from(profile.getEnvironment().getActiveDirectoryEndpoint()).to(builder::authorityHost);
        map.from(executorService).to(builder::executorService);
    }

    /**
     * Set the {@link ExecutorService}. The {@link ExecutorService} will be applied to the underneath identity client.
     * @param executorService The executor service.
     */
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }
}
