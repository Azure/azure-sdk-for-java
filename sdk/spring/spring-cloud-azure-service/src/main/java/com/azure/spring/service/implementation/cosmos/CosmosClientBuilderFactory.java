// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.cosmos;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.Configuration;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.spring.core.aware.ProxyOptionsAware;
import com.azure.spring.core.implementation.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.core.implementation.credential.descriptor.KeyAuthenticationDescriptor;
import com.azure.spring.core.implementation.credential.descriptor.TokenAuthenticationDescriptor;
import com.azure.spring.core.implementation.factory.AbstractAzureServiceClientBuilderFactory;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.implementation.properties.PropertyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import static com.azure.spring.core.implementation.converter.AzureHttpProxyOptionsConverter.HTTP_PROXY_CONVERTER;
import static com.azure.spring.service.implementation.converter.DirectConnectionConfigConverter.DIRECT_CONNECTION_CONFIG_CONVERTER;
import static com.azure.spring.service.implementation.converter.GatewayConnectionConfigConverter.GATEWAY_CONNECTION_CONFIG_CONVERTER;

/**
 * Cosmos client builder factory, it builds the {@link CosmosClientBuilder} according the configuration context and
 * blob properties.
 */
public class CosmosClientBuilderFactory extends AbstractAzureServiceClientBuilderFactory<CosmosClientBuilder> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosClientBuilderFactory.class);

    private final CosmosClientProperties cosmosClientProperties;

    private ProxyOptions proxyOptions;
    private ThrottlingRetryOptions throttlingRetryOptions;

    /**
     * Create a {@link CosmosClientBuilderFactory} instance with a {@link CosmosClientProperties}.
     * @param cosmosClientProperties the properties for the cosmos client.
     */
    public CosmosClientBuilderFactory(CosmosClientProperties cosmosClientProperties) {
        this.cosmosClientProperties = cosmosClientProperties;
    }

    @Override
    protected CosmosClientBuilder createBuilderInstance() {
        return new CosmosClientBuilder();
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return this.cosmosClientProperties;
    }

    @Override
    protected List<AuthenticationDescriptor<?>> getAuthenticationDescriptors(CosmosClientBuilder builder) {
        return Arrays.asList(
            new KeyAuthenticationDescriptor(builder::credential),
            new TokenAuthenticationDescriptor(this.tokenCredentialResolver, builder::credential)
        );
    }

    @Override
    protected void configureProxy(CosmosClientBuilder builder) {
        ProxyOptionsAware.Proxy proxy = this.cosmosClientProperties.getProxy();
        this.proxyOptions = HTTP_PROXY_CONVERTER.convert(proxy);
        if (this.proxyOptions == null) {
            LOGGER.debug("No proxy properties available.");
        }
    }

    @Override
    protected void configureRetry(CosmosClientBuilder builder) {
        LOGGER.debug("CosmosClientBuilderFactory does not support common defined retry options");
    }

    @Override
    protected void configureService(CosmosClientBuilder builder) {
        PropertyMapper map = new PropertyMapper();

        map.from(this.cosmosClientProperties.getEndpoint()).to(builder::endpoint);
        map.from(this.cosmosClientProperties.getConsistencyLevel()).to(builder::consistencyLevel);
        map.from(this.cosmosClientProperties.getClientTelemetryEnabled()).to(builder::clientTelemetryEnabled);
        map.from(this.cosmosClientProperties.getConnectionSharingAcrossClientsEnabled()).to(builder::connectionSharingAcrossClientsEnabled);
        map.from(this.cosmosClientProperties.getContentResponseOnWriteEnabled()).to(builder::contentResponseOnWriteEnabled);
        map.from(this.cosmosClientProperties.getEndpointDiscoveryEnabled()).to(builder::endpointDiscoveryEnabled);
        map.from(this.cosmosClientProperties.getMultipleWriteRegionsEnabled()).to(builder::multipleWriteRegionsEnabled);
        map.from(this.cosmosClientProperties.getReadRequestsFallbackEnabled()).to(builder::readRequestsFallbackEnabled);
        map.from(this.cosmosClientProperties.getSessionCapturingOverrideEnabled()).to(builder::sessionCapturingOverrideEnabled);
        map.from(this.cosmosClientProperties.getPreferredRegions()).whenNot(List::isEmpty).to(builder::preferredRegions);
        configureThrottlingRetryOptions(builder, map);
        configureConnection(builder, map);
    }

    /**
     * Configure Cosmos connection.
     * If not configured the proxy of gateway connection, then will try to use the root proxy of Cosmos properties.
     * @param builder Cosmos client builder
     * @param map Property mapper
     */
    private void configureConnection(CosmosClientBuilder builder, PropertyMapper map) {
        // TODO (xiada): should we count this as authentication
        map.from(this.cosmosClientProperties.getResourceToken()).to(builder::resourceToken);

        GatewayConnectionConfig gatewayConnectionConfig = GATEWAY_CONNECTION_CONFIG_CONVERTER.convert(
            this.cosmosClientProperties.getGatewayConnection());
        if (proxyOptions != null) {
            gatewayConnectionConfig.setProxy(proxyOptions);
        }

        ConnectionMode connectionMode = this.cosmosClientProperties.getConnectionMode();
        if (ConnectionMode.DIRECT.equals(connectionMode)) {
            DirectConnectionConfig directConnectionConfig = DIRECT_CONNECTION_CONFIG_CONVERTER.convert(
                this.cosmosClientProperties.getDirectConnection());

            builder.directMode(directConnectionConfig, gatewayConnectionConfig);
        } else if (ConnectionMode.GATEWAY.equals(connectionMode)) {
            builder.gatewayMode(gatewayConnectionConfig);
        }
    }

    /**
     * Configure ThrottlingRetryOptions.
     * If not configured the retry options of ThrottlingRetryOptions, then will try to use the root retry options of Cosmos properties.
     * @param builder Cosmos client builder
     * @param map Property mapper
     */
    private void configureThrottlingRetryOptions(CosmosClientBuilder builder, PropertyMapper map) {
        ThrottlingRetryOptions retryOptions = this.cosmosClientProperties.getThrottlingRetryOptions();
        if (this.throttlingRetryOptions != null && isDefaultThrottlingRetryOptions(retryOptions)) {
            map.from(this.throttlingRetryOptions).to(builder::throttlingRetryOptions);
            LOGGER.debug("The throttling retry options is not configured, "
                + "then the Azure Spring Retry configuration will be applied to Cosmos service builder.");
        } else {
            map.from(retryOptions).to(builder::throttlingRetryOptions);
        }
    }

    /**
     * Check if the retry option is the default value, which is defined in azure-cosmos SDK.
     * @param retryOptions retry options to be checked
     * @return result
     */
    private boolean isDefaultThrottlingRetryOptions(ThrottlingRetryOptions retryOptions) {
        ThrottlingRetryOptions defaultOptions = new ThrottlingRetryOptions();
        return defaultOptions.getMaxRetryAttemptsOnThrottledRequests() == retryOptions.getMaxRetryAttemptsOnThrottledRequests()
            && defaultOptions.getMaxRetryWaitTime().equals(retryOptions.getMaxRetryWaitTime());
    }

    @Override
    protected BiConsumer<CosmosClientBuilder, String> consumeApplicationId() {
        return CosmosClientBuilder::userAgentSuffix;
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
