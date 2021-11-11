// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.credential;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.core.aware.AzureProfileAware;
import com.azure.spring.core.factory.AzureCredentialBuilderFactory;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.service.core.PropertyMapper;

import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

/**
 *
 */
public class AzureDefaultAzureCredentialBuilderFactory extends AzureCredentialBuilderFactory<DefaultAzureCredentialBuilder> {

    private final ExecutorService executorService;

    public AzureDefaultAzureCredentialBuilderFactory(AzureProperties azureProperties,
                                                     DefaultAzureCredentialBuilder builder,
                                                     ExecutorService executorService) {
        super(azureProperties, builder);
        this.executorService = executorService;
    }

    @Override
    protected BiConsumer<DefaultAzureCredentialBuilder, HttpPipelinePolicy> consumeHttpPipelinePolicy() {
        return (a, b) -> { };
    }

    @Override
    protected BiConsumer<DefaultAzureCredentialBuilder, HttpLogOptions> consumeHttpLogOptions() {
        return (a, b) -> { };
    }

    @Override
    protected void configureService(DefaultAzureCredentialBuilder builder) {
        super.configureService(builder);
        AzureProperties azureProperties = getAzureProperties();
        AzureProfileAware.Profile profile = azureProperties.getProfile();
        PropertyMapper map = new PropertyMapper();
        map.from(profile.getTenantId()).to(builder::tenantId);
        map.from(profile.getEnvironment().getActiveDirectoryEndpoint()).to(builder::authorityHost);
        map.from(azureProperties.getCredential().getManagedIdentityClientId()).to(builder::managedIdentityClientId);
        builder.executorService(executorService);
    }

    @Override
    protected BiConsumer<DefaultAzureCredentialBuilder, TokenCredential> consumeDefaultTokenCredential() {
        return (a, b) -> { };
    }

    @Override
    protected BiConsumer<DefaultAzureCredentialBuilder, String> consumeConnectionString() {
        return (a, b) -> { };
    }
}
