// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.cosmos;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.Configuration;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.spring.core.aware.ProxyAware;
import com.azure.spring.core.aware.RetryAware;
import com.azure.spring.core.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.KeyAuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.TokenAuthenticationDescriptor;
import com.azure.spring.core.factory.AbstractAzureServiceClientBuilderFactory;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.PropertyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import static com.azure.spring.core.implementation.converter.AzureHttpProxyOptionsConverter.HTTP_PROXY_CONVERTER;

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
    protected void configureProxy(CosmosClientBuilder builder) {
        ProxyAware.Proxy proxy = this.cosmosClientProperties.getProxy();
        this.proxyOptions = HTTP_PROXY_CONVERTER.convert(proxy);
        if (this.proxyOptions == null) {
            LOGGER.debug("No proxy properties available.");
        }
    }

    @Override
    protected void configureRetry(CosmosClientBuilder builder) {
        RetryAware.Retry retry = this.cosmosClientProperties.getRetry();
        if (isInvalidRetry(retry)) {
            return;
        }

        this.throttlingRetryOptions = new ThrottlingRetryOptions();
        this.throttlingRetryOptions.setMaxRetryWaitTime(retry.getTimeout());
        this.throttlingRetryOptions.setMaxRetryAttemptsOnThrottledRequests(retry.getMaxAttempts());
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
        map.from(this.cosmosClientProperties.getPermissions()).whenNot(List::isEmpty).to(builder::permissions);
        GatewayConnectionConfig gatewayConnection = this.cosmosClientProperties.getGatewayConnection();
        if (proxyOptions != null && gatewayConnection.getProxy() == null) {
            gatewayConnection.setProxy(proxyOptions);
            LOGGER.debug("The proxy of the Gateway connection is not configured, "
                + "then the Azure Spring Proxy configuration will be applied to Cosmos gateway connection.");
        }
        if (ConnectionMode.DIRECT.equals(this.cosmosClientProperties.getConnectionMode())) {
            builder.directMode(this.cosmosClientProperties.getDirectConnection(), gatewayConnection);
        } else if (ConnectionMode.GATEWAY.equals(this.cosmosClientProperties.getConnectionMode())) {
            builder.gatewayMode(gatewayConnection);
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

    /**
     * Check if the properties of the retry is invalid value.
     * @param retry retry options to be checked
     * @return result
     */
    private boolean isInvalidRetry(RetryAware.Retry retry) {
        return retry.getMaxAttempts() == null || retry.getTimeout() == null;
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
