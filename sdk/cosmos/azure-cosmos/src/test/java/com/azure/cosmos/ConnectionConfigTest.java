// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.core.http.ProxyOptions;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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
        assertThat(connectionPolicy.getConnectionMode()).isEqualTo(ConnectionMode.GATEWAY);
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
        assertThat(connectionPolicy.getConnectionMode()).isEqualTo(ConnectionMode.GATEWAY);
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
        assertThat(connectionPolicy.getConnectionMode()).isEqualTo(ConnectionMode.DIRECT);
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
        assertThat(connectionPolicy.getConnectionMode()).isEqualTo(ConnectionMode.DIRECT);
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
        assertThat(connectionPolicy.getConnectionMode()).isEqualTo(ConnectionMode.DIRECT);
        validateDirectAndGatewayConnectionConfig(connectionPolicy, cosmosClientBuilder, directConnectionConfig, gatewayConnectionConfig);
        safeCloseSyncClient(cosmosClient);
    }

    @Test(groups = { "unit" })
    public void buildConnectionPolicy_withDirectAndGatewayConnectionConfig() throws Exception {
        ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("127.0.0.0", 8080));
        DirectConnectionConfig directConnectionConfig = DirectConnectionConfig.getDefaultConfig();
        GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
        gatewayConnectionConfig.setMaxConnectionPoolSize(MAX_CONNECTION_POOL_SIZE);
        gatewayConnectionConfig.setRequestTimeout(REQUEST_TIME_OUT);
        gatewayConnectionConfig.setIdleConnectionTimeout(IDLE_CONNECTION_TIME_OUT);
        gatewayConnectionConfig.setProxy(proxyOptions);
        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .directMode(directConnectionConfig, gatewayConnectionConfig);
        ReflectionUtils.buildConnectionPolicy(cosmosClientBuilder);
        ConnectionPolicy connectionPolicy = ReflectionUtils.getConnectionPolicy(cosmosClientBuilder);
        assertThat(connectionPolicy.getConnectionMode()).isEqualTo(ConnectionMode.DIRECT);
        validateDirectAndGatewayConnectionConfig(connectionPolicy, cosmosClientBuilder, directConnectionConfig, gatewayConnectionConfig);
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
        assertThat(connectionPolicy.getConnectionMode()).isEqualTo(ConnectionMode.DIRECT);
        validateDirectConnectionConfig(connectionPolicy, cosmosClientBuilder, DirectConnectionConfig.getDefaultConfig());
        safeCloseSyncClient(cosmosClient);
    }

    private void validateDirectAndGatewayConnectionConfig(ConnectionPolicy connectionPolicy, CosmosClientBuilder cosmosClientBuilder,
                                                          DirectConnectionConfig directConnectionConfig, GatewayConnectionConfig gatewayConnectionConfig) {
        validateCommonConnectionConfig(connectionPolicy, cosmosClientBuilder);
        assertThat(connectionPolicy.getConnectionMode()).isEqualTo(ConnectionMode.DIRECT);
        validateDirectConfig(connectionPolicy, directConnectionConfig);
        validateGatewayConfig(connectionPolicy, gatewayConnectionConfig);
    }

    private void validateGatewayConnectionConfig(ConnectionPolicy connectionPolicy, CosmosClientBuilder cosmosClientBuilder, GatewayConnectionConfig gatewayConnectionConfig) {
        validateCommonConnectionConfig(connectionPolicy, cosmosClientBuilder);
        assertThat(connectionPolicy.getConnectionMode()).isEqualTo(ConnectionMode.GATEWAY);
        validateGatewayConfig(connectionPolicy, gatewayConnectionConfig);
    }

    private void validateDirectConnectionConfig(ConnectionPolicy connectionPolicy, CosmosClientBuilder cosmosClientBuilder, DirectConnectionConfig directConnectionConfig) {
        validateCommonConnectionConfig(connectionPolicy, cosmosClientBuilder);
        assertThat(connectionPolicy.getConnectionMode()).isEqualTo(ConnectionMode.DIRECT);
        validateDirectConfig(connectionPolicy, directConnectionConfig);
    }

    private void validateCommonConnectionConfig(ConnectionPolicy connectionPolicy, CosmosClientBuilder cosmosClientBuilder) {
        assertThat(connectionPolicy.isMultipleWriteRegionsEnabled()).isEqualTo(cosmosClientBuilder.isMultipleWriteRegionsEnabled());
        assertThat(connectionPolicy.isEndpointDiscoveryEnabled()).isEqualTo(cosmosClientBuilder.isEndpointDiscoveryEnabled());
        assertThat(connectionPolicy.isReadRequestsFallbackEnabled()).isEqualTo(cosmosClientBuilder.isReadRequestsFallbackEnabled());
        assertThat(connectionPolicy.getPreferredRegions()).isEqualTo(cosmosClientBuilder.getPreferredRegions());
        assertThat(connectionPolicy.getThrottlingRetryOptions()).isEqualTo(cosmosClientBuilder.getThrottlingRetryOptions());
        assertThat(connectionPolicy.getUserAgentSuffix()).isEqualTo(cosmosClientBuilder.getUserAgentSuffix());
    }

    private void validateGatewayConfig(ConnectionPolicy connectionPolicy, GatewayConnectionConfig gatewayConnectionConfig) {
        assertThat(connectionPolicy.getIdleHttpConnectionTimeout()).isEqualTo(gatewayConnectionConfig.getIdleConnectionTimeout());
        assertThat(connectionPolicy.getMaxConnectionPoolSize()).isEqualTo(gatewayConnectionConfig.getMaxConnectionPoolSize());
        assertThat(connectionPolicy.getRequestTimeout()).isEqualTo(gatewayConnectionConfig.getRequestTimeout());
        assertThat(connectionPolicy.getProxy()).isEqualTo(gatewayConnectionConfig.getProxy());
    }

    private void validateDirectConfig(ConnectionPolicy connectionPolicy, DirectConnectionConfig directConnectionConfig) {
        assertThat(connectionPolicy.getConnectTimeout()).isEqualTo(directConnectionConfig.getConnectTimeout());
        assertThat(connectionPolicy.getIdleTcpConnectionTimeout()).isEqualTo(directConnectionConfig.getIdleConnectionTimeout());
        assertThat(connectionPolicy.getIdleTcpEndpointTimeout()).isEqualTo(directConnectionConfig.getIdleEndpointTimeout());
        assertThat(connectionPolicy.getMaxConnectionsPerEndpoint()).isEqualTo(directConnectionConfig.getMaxConnectionsPerEndpoint());
        assertThat(connectionPolicy.getMaxRequestsPerConnection()).isEqualTo(directConnectionConfig.getMaxRequestsPerConnection());
    }
}
