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
        LOGGER.debug("No configureProxy for CosmosClientBuilder.");
    }

    @Override
    protected void configureRetry(CosmosClientBuilder builder) {
        LOGGER.debug("No configureRetry for CosmosClientBuilder.");
    }

    @Override
    protected void configureService(CosmosClientBuilder builder) {
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();

        map.from(this.cosmosProperties.getUri()).to(builder::endpoint);
        map.from(this.cosmosProperties.getConsistencyLevel()).to(builder::consistencyLevel);
        map.from(this.cosmosProperties.getClientTelemetryEnabled()).to(builder::clientTelemetryEnabled);
        map.from(this.cosmosProperties.getConnectionSharingAcrossClientsEnabled()).to(builder::connectionSharingAcrossClientsEnabled);
        map.from(this.cosmosProperties.getContentResponseOnWriteEnabled()).to(builder::contentResponseOnWriteEnabled);
        map.from(this.cosmosProperties.getEndpointDiscoveryEnabled()).to(builder::endpointDiscoveryEnabled);
        map.from(this.cosmosProperties.getMultipleWriteRegionsEnabled()).to(builder::multipleWriteRegionsEnabled);
        map.from(this.cosmosProperties.getReadRequestsFallbackEnabled()).to(builder::readRequestsFallbackEnabled);
        map.from(this.cosmosProperties.getSessionCapturingOverrideEnabled()).to(builder::sessionCapturingOverrideEnabled);
        map.from(this.cosmosProperties.getPreferredRegions()).whenNot(List::isEmpty).to(builder::preferredRegions);
        map.from(this.cosmosProperties.getThrottlingRetryOptions()).to(builder::throttlingRetryOptions);

        // TODO (xiada): should we count this as authentication
        map.from(this.cosmosProperties.getResourceToken()).to(builder::resourceToken);
        map.from(this.cosmosProperties.getPermissions()).whenNot(List::isEmpty).to(builder::permissions);

        if (ConnectionMode.GATEWAY.equals(this.cosmosProperties.getConnectionMode())) {
            builder.gatewayMode(this.cosmosProperties.getGatewayConnection());
        } else if (ConnectionMode.DIRECT.equals(this.cosmosProperties.getConnectionMode())) {
            // TODO (xiada): public CosmosClientBuilder directMode(DirectConnectionConfig directConnectionConfig, GatewayConnectionConfig gatewayConnectionConfig) {
            builder.directMode(this.cosmosProperties.getDirectConnection());
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
