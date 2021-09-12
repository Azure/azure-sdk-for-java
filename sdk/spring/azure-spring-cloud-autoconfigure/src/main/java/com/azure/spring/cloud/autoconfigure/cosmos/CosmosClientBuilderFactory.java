// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.cosmos;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.Configuration;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.core.ApplicationId;
import com.azure.spring.core.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.KeyAuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.TokenAuthenticationDescriptor;
import com.azure.spring.core.factory.AbstractAzureServiceClientBuilderFactory;
import com.azure.spring.core.properties.AzureProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.PropertyMapper;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Cosmos client builder factory, it builds the {@link CosmosClientBuilder} according the configuration context and
 * blob properties.
 */
public class CosmosClientBuilderFactory extends AbstractAzureServiceClientBuilderFactory<CosmosClientBuilder> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosClientBuilderFactory.class);

    private final AzureCosmosProperties cosmosProperties;

    public CosmosClientBuilderFactory(AzureCosmosProperties cosmosProperties) {
        this.cosmosProperties = cosmosProperties;
    }

    @Override
    protected CosmosClientBuilder createBuilderInstance() {
        return new CosmosClientBuilder();
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return this.cosmosProperties;
    }

    @Override
    protected List<AuthenticationDescriptor<?>> getAuthenticationDescriptors(CosmosClientBuilder builder) {
        return Arrays.asList(
            new KeyAuthenticationDescriptor(provider -> builder.credential(provider.getCredential())),
            new TokenAuthenticationDescriptor(provider -> builder.credential(provider.getCredential()))
        );
    }

    @Override
    protected void configureApplicationId(CosmosClientBuilder builder) {
        builder.userAgentSuffix(ApplicationId.AZURE_SPRING_COSMOS);
    }

    @Override
    protected void configureProxy(CosmosClientBuilder builder) {
        // TODO (xiada) configure the proxy
    }

    @Override
    protected void configureRetry(CosmosClientBuilder builder) {
        // TODO (xiada) configure the retry
    }

    @Override
    protected void configureService(CosmosClientBuilder builder) {
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(this.cosmosProperties.getUri()).to(builder::endpoint);
        map.from(this.cosmosProperties.getConsistencyLevel()).to(builder::consistencyLevel);

        if (ConnectionMode.GATEWAY == cosmosProperties.getConnectionMode()) {
            builder.gatewayMode();
        }
    }

    @Override
    protected BiConsumer<CosmosClientBuilder, Configuration> consumeConfiguration() {
        LOGGER.warn("Configuration instance is not supported to configure in CosmosClientBuilder");
        return (a, b) -> { };
    }

    @Override
    protected BiConsumer<CosmosClientBuilder, TokenCredential> consumeDefaultTokenCredential() {
        return CosmosClientBuilder::credential;
    }

    @Override
    protected BiConsumer<CosmosClientBuilder, String> consumeConnectionString() {
        LOGGER.debug("Connection string is not supported to configure in CosmosClientBuilder");
        return (a, b) -> { };
    }
}
