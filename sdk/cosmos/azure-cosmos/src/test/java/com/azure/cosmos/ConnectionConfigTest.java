// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class ConnectionConfigTest extends TestSuiteBase {

    private static final Duration REQUEST_TIME_OUT = Duration.ofSeconds(15);
    private static final Duration IDLE_CONNECTION_TIME_OUT = Duration.ofSeconds(30);
    private static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(100);
    private static final Duration IDLE_CHANNEL_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration IDLE_ENDPOINT_TIMEOUT = Duration.ofSeconds(20);
    private static final int MAX_CONNECTION_POOL_SIZE = 500;

    @Test(groups = { "emulator" })
    public void buildClient_withDefaultGatewayConnectionConfig() {
        GatewayConnectionConfig gatewayConnectionConfig = GatewayConnectionConfig.getDefaultConfig();
        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .gatewayMode();
        CosmosClient cosmosClient = cosmosClientBuilder.buildClient();

        AsyncDocumentClient asyncDocumentClient =
            CosmosBridgeInternal.getAsyncDocumentClient(cosmosClient);
        ConnectionPolicy connectionPolicy = asyncDocumentClient.getConnectionPolicy();
        assertThat(connectionPolicy.getConnectionMode().equals(ConnectionMode.GATEWAY));
        validateGatewayConnectionConfig(connectionPolicy, cosmosClientBuilder, gatewayConnectionConfig);
        safeCloseSyncClient(cosmosClient);
    }

    @Test(groups = { "emulator" })
    public void buildClient_withCustomGatewayConnectionConfig() {
        GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
        gatewayConnectionConfig.setMaxConnectionPoolSize(100);
        final List<String> preferredRegions = new ArrayList<>();
        preferredRegions.add("West US");
        gatewayConnectionConfig.setIdleConnectionTimeout(IDLE_CONNECTION_TIME_OUT);
        gatewayConnectionConfig.setRequestTimeout(REQUEST_TIME_OUT);
        gatewayConnectionConfig.setMaxConnectionPoolSize(MAX_CONNECTION_POOL_SIZE);
        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .preferredRegions(preferredRegions)
            .userAgentSuffix("custom-gateway-client")
            .multipleWriteRegionsEnabled(false)
            .endpointDiscoveryEnabled(false)
            .readRequestsFallbackEnabled(true)
            .gatewayMode(gatewayConnectionConfig);

        CosmosClient cosmosClient = cosmosClientBuilder.buildClient();

        AsyncDocumentClient asyncDocumentClient =
            CosmosBridgeInternal.getAsyncDocumentClient(cosmosClient);
        ConnectionPolicy connectionPolicy = asyncDocumentClient.getConnectionPolicy();
        assertThat(connectionPolicy.getConnectionMode().equals(ConnectionMode.GATEWAY));
        validateGatewayConnectionConfig(connectionPolicy, cosmosClientBuilder, gatewayConnectionConfig);
        safeCloseSyncClient(cosmosClient);
    }

    @Test(groups = { "emulator" })
    public void buildClient_withDefaultDirectConnectionConfig() {
        DirectConnectionConfig directConnectionConfig = DirectConnectionConfig.getDefaultConfig();
        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .directMode();

        CosmosClient cosmosClient = cosmosClientBuilder.buildClient();

        AsyncDocumentClient asyncDocumentClient =
            CosmosBridgeInternal.getAsyncDocumentClient(cosmosClient);
        ConnectionPolicy connectionPolicy = asyncDocumentClient.getConnectionPolicy();
        assertThat(connectionPolicy.getConnectionMode().equals(ConnectionMode.DIRECT));
        validateDirectConnectionConfig(connectionPolicy, cosmosClientBuilder, directConnectionConfig);
        safeCloseSyncClient(cosmosClient);
    }

    @Test(groups = { "emulator" })
    public void buildClient_withCustomDirectConnectionConfig() {
        DirectConnectionConfig directConnectionConfig = DirectConnectionConfig.getDefaultConfig();
        directConnectionConfig.setConnectTimeout(CONNECTION_TIMEOUT);
        directConnectionConfig.setIdleConnectionTimeout(IDLE_CHANNEL_TIMEOUT);
        directConnectionConfig.setIdleEndpointTimeout(IDLE_ENDPOINT_TIMEOUT);
        directConnectionConfig.setMaxConnectionsPerEndpoint(100);
        directConnectionConfig.setMaxRequestsPerConnection(100);
        final List<String> preferredRegions = new ArrayList<>();
        preferredRegions.add("West US");
        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .directMode(directConnectionConfig)
            .preferredRegions(preferredRegions)
            .userAgentSuffix("custom-direct-client")
            .multipleWriteRegionsEnabled(false)
            .endpointDiscoveryEnabled(false)
            .readRequestsFallbackEnabled(true);

        CosmosClient cosmosClient = cosmosClientBuilder.buildClient();
        AsyncDocumentClient asyncDocumentClient =
            CosmosBridgeInternal.getAsyncDocumentClient(cosmosClient);
        ConnectionPolicy connectionPolicy = asyncDocumentClient.getConnectionPolicy();
        assertThat(connectionPolicy.getConnectionMode().equals(ConnectionMode.DIRECT));
        validateDirectConnectionConfig(connectionPolicy, cosmosClientBuilder, directConnectionConfig);
        safeCloseSyncClient(cosmosClient);
    }

    @Test(groups = { "emulator" })
    public void buildClient_withDirectAndGatewayConnectionConfig() {
        DirectConnectionConfig directConnectionConfig = DirectConnectionConfig.getDefaultConfig();
        GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
        gatewayConnectionConfig.setMaxConnectionPoolSize(MAX_CONNECTION_POOL_SIZE);
        gatewayConnectionConfig.setRequestTimeout(REQUEST_TIME_OUT);
        gatewayConnectionConfig.setIdleConnectionTimeout(IDLE_CONNECTION_TIME_OUT);
        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .directMode(directConnectionConfig, gatewayConnectionConfig);
        CosmosClient cosmosClient = cosmosClientBuilder.buildClient();
        AsyncDocumentClient asyncDocumentClient =
            CosmosBridgeInternal.getAsyncDocumentClient(cosmosClient);
        ConnectionPolicy connectionPolicy = asyncDocumentClient.getConnectionPolicy();
        assertThat(connectionPolicy.getConnectionMode().equals(ConnectionMode.DIRECT));
        validateDirectAndGatewayConnectionConfig(connectionPolicy, cosmosClientBuilder, directConnectionConfig, gatewayConnectionConfig);
        safeCloseSyncClient(cosmosClient);
    }

    @Test(groups = { "emulator" })
    public void buildClient_withNoConnectionConfig() {
        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY);

        CosmosClient cosmosClient = cosmosClientBuilder.buildClient();

        AsyncDocumentClient asyncDocumentClient =
            CosmosBridgeInternal.getAsyncDocumentClient(cosmosClient);
        ConnectionPolicy connectionPolicy = asyncDocumentClient.getConnectionPolicy();
        assertThat(connectionPolicy.getConnectionMode().equals(ConnectionMode.DIRECT));
        validateDirectConnectionConfig(connectionPolicy, cosmosClientBuilder, DirectConnectionConfig.getDefaultConfig());
        safeCloseSyncClient(cosmosClient);
    }

    private void validateDirectAndGatewayConnectionConfig(ConnectionPolicy connectionPolicy, CosmosClientBuilder cosmosClientBuilder,
                                                          DirectConnectionConfig directConnectionConfig, GatewayConnectionConfig gatewayConnectionConfig) {
        validateCommonConnectionConfig(connectionPolicy, cosmosClientBuilder);
        assertThat(Objects.equals(connectionPolicy.getConnectionMode(), ConnectionMode.DIRECT));
        validateDirectConfig(connectionPolicy, directConnectionConfig);
        validateGatewayConfig(connectionPolicy, gatewayConnectionConfig);
    }

    private void validateGatewayConnectionConfig(ConnectionPolicy connectionPolicy, CosmosClientBuilder cosmosClientBuilder, GatewayConnectionConfig gatewayConnectionConfig) {
        validateCommonConnectionConfig(connectionPolicy, cosmosClientBuilder);
        assertThat(Objects.equals(connectionPolicy.getConnectionMode(), ConnectionMode.GATEWAY));
        validateGatewayConfig(connectionPolicy, gatewayConnectionConfig);
    }

    private void validateDirectConnectionConfig(ConnectionPolicy connectionPolicy, CosmosClientBuilder cosmosClientBuilder, DirectConnectionConfig directConnectionConfig) {
        validateCommonConnectionConfig(connectionPolicy, cosmosClientBuilder);
        assertThat(Objects.equals(connectionPolicy.getConnectionMode(), ConnectionMode.DIRECT));
        validateDirectConfig(connectionPolicy, directConnectionConfig);
    }

    private void validateCommonConnectionConfig(ConnectionPolicy connectionPolicy, CosmosClientBuilder cosmosClientBuilder) {
        assertThat(Objects.equals(connectionPolicy.isMultipleWriteRegionsEnabled(), cosmosClientBuilder.isMultipleWriteRegionsEnabled()));
        assertThat(Objects.equals(connectionPolicy.isEndpointDiscoveryEnabled(), cosmosClientBuilder.isEndpointDiscoveryEnabled()));
        assertThat(Objects.equals(connectionPolicy.isReadRequestsFallbackEnabled(), cosmosClientBuilder.isReadRequestsFallbackEnabled()));
        assertThat(Objects.equals(connectionPolicy.getPreferredRegions(), cosmosClientBuilder.getPreferredRegions()));
        assertThat(Objects.equals(connectionPolicy.getThrottlingRetryOptions(), cosmosClientBuilder.getThrottlingRetryOptions()));
        assertThat(Objects.equals(connectionPolicy.getUserAgentSuffix(), cosmosClientBuilder.getUserAgentSuffix()));
    }

    private void validateGatewayConfig(ConnectionPolicy connectionPolicy, GatewayConnectionConfig gatewayConnectionConfig) {
        assertThat(Objects.equals(connectionPolicy.getIdleConnectionTimeout(), gatewayConnectionConfig.getIdleConnectionTimeout()));
        assertThat(Objects.equals(connectionPolicy.getMaxConnectionPoolSize(), gatewayConnectionConfig.getMaxConnectionPoolSize()));
        assertThat(Objects.equals(connectionPolicy.getRequestTimeout(), gatewayConnectionConfig.getRequestTimeout()));
        assertThat(Objects.equals(connectionPolicy.getProxy(), gatewayConnectionConfig.getProxy()));
    }

    private void validateDirectConfig(ConnectionPolicy connectionPolicy, DirectConnectionConfig directConnectionConfig) {
        assertThat(Objects.equals(connectionPolicy.getConnectTimeout(), directConnectionConfig.getConnectTimeout()));
        assertThat(Objects.equals(connectionPolicy.getIdleConnectionTimeout(), directConnectionConfig.getIdleConnectionTimeout()));
        assertThat(Objects.equals(connectionPolicy.getIdleEndpointTimeout(), directConnectionConfig.getIdleEndpointTimeout()));
        assertThat(Objects.equals(connectionPolicy.getMaxConnectionsPerEndpoint(), directConnectionConfig.getMaxConnectionsPerEndpoint()));
        assertThat(Objects.equals(connectionPolicy.getMaxRequestsPerConnection(), directConnectionConfig.getMaxRequestsPerConnection()));
    }
}
