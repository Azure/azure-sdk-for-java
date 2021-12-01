// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.cosmos;

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
import com.azure.spring.core.properties.util.PropertyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import static com.azure.spring.core.converter.AzureHttpProxyOptionsConverter.HTTP_PROXY_CONVERTER;

/**
 * Cosmos client builder factory, it builds the {@link CosmosClientBuilder} according the configuration context and
 * blob properties.
 */
public class CosmosClientBuilderFactory extends AbstractAzureServiceClientBuilderFactory<CosmosClientBuilder> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosClientBuilderFactory.class);

    private final CosmosProperties cosmosProperties;

    private ProxyOptions proxyOptions;
    private ThrottlingRetryOptions throttlingRetryOptions;

    public CosmosClientBuilderFactory(CosmosProperties cosmosProperties) {
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
    protected void configureProxy(CosmosClientBuilder builder) {
        ProxyAware.Proxy proxy = this.cosmosProperties.getProxy();
        this.proxyOptions = HTTP_PROXY_CONVERTER.convert(proxy);
        if (this.proxyOptions == null) {
            LOGGER.debug("No proxy properties available.");
        }
    }

    @Override
    protected void configureRetry(CosmosClientBuilder builder) {
        RetryAware.Retry retry = this.cosmosProperties.getRetry();
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

        map.from(this.cosmosProperties.getEndpoint()).to(builder::endpoint);
        map.from(this.cosmosProperties.getConsistencyLevel()).to(builder::consistencyLevel);
        map.from(this.cosmosProperties.getClientTelemetryEnabled()).to(builder::clientTelemetryEnabled);
        map.from(this.cosmosProperties.getConnectionSharingAcrossClientsEnabled()).to(builder::connectionSharingAcrossClientsEnabled);
        map.from(this.cosmosProperties.getContentResponseOnWriteEnabled()).to(builder::contentResponseOnWriteEnabled);
        map.from(this.cosmosProperties.getEndpointDiscoveryEnabled()).to(builder::endpointDiscoveryEnabled);
        map.from(this.cosmosProperties.getMultipleWriteRegionsEnabled()).to(builder::multipleWriteRegionsEnabled);
        map.from(this.cosmosProperties.getReadRequestsFallbackEnabled()).to(builder::readRequestsFallbackEnabled);
        map.from(this.cosmosProperties.getSessionCapturingOverrideEnabled()).to(builder::sessionCapturingOverrideEnabled);
        map.from(this.cosmosProperties.getPreferredRegions()).whenNot(List::isEmpty).to(builder::preferredRegions);

        ThrottlingRetryOptions retryOptions = this.cosmosProperties.getThrottlingRetryOptions();
        if (this.throttlingRetryOptions != null && isDefaultThrottlingRetryOptions(retryOptions)) {
            map.from(this.throttlingRetryOptions).to(builder::throttlingRetryOptions);
            LOGGER.debug("The throttling retry options is not configured, "
                + "then the Azure Spring Retry configuration will be applied to Cosmos service builder.");
        } else {
            map.from(retryOptions).to(builder::throttlingRetryOptions);
        }

        // TODO (xiada): should we count this as authentication
        map.from(this.cosmosProperties.getResourceToken()).to(builder::resourceToken);
        map.from(this.cosmosProperties.getPermissions()).whenNot(List::isEmpty).to(builder::permissions);
        GatewayConnectionConfig gatewayConnection = this.cosmosProperties.getGatewayConnection();
        if (proxyOptions != null && gatewayConnection.getProxy() == null) {
            gatewayConnection.setProxy(proxyOptions);
            LOGGER.debug("The proxy of the Gateway connection is not configured, "
                + "then the Azure Spring Proxy configuration will be applied to Cosmos gateway connection.");
        }
        if (ConnectionMode.DIRECT.equals(this.cosmosProperties.getConnectionMode())) {
            builder.directMode(this.cosmosProperties.getDirectConnection(), gatewayConnection);
        } else if (ConnectionMode.GATEWAY.equals(this.cosmosProperties.getConnectionMode())) {
            builder.gatewayMode(gatewayConnection);
        }
    }

    /**
     * Check if the retry option is the default value, which is defined in azure-cosmos SDK.
     * @param retryOptions retry options to be checked
     * @return result
     */
    private boolean isDefaultThrottlingRetryOptions(ThrottlingRetryOptions retryOptions) {
        if (retryOptions.getMaxRetryWaitTime().equals(Duration.ofSeconds(30))
            && retryOptions.getMaxRetryAttemptsOnThrottledRequests() == 9) {
            return true;
        }
        return false;
    }

    /**
     * Check if the properties of the retry is invalid value.
     * @param retry retry options to be checked
     * @return result
     */
    private boolean isInvalidRetry(RetryAware.Retry retry) {
        if (retry.getMaxAttempts() == null || retry.getTimeout() == null) {
            return true;
        }
        return false;
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
