// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.rx.TestSuiteBase;
import org.assertj.core.api.Assertions;
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

    @Test(groups = { "emulator" })
    public void buildClient_withDefaultGatewayConnectionConfig() {
        GatewayConnectionConfig gatewayConnectionConfig = GatewayConnectionConfig.getDefaultConfig();
        CosmosClient cosmosClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .connectionModeGateway(gatewayConnectionConfig)
            .buildClient();

        AsyncDocumentClient asyncDocumentClient =
            CosmosBridgeInternal.getAsyncDocumentClient(cosmosClient);
        ConnectionPolicy connectionPolicy = asyncDocumentClient.getConnectionPolicy();
        assertThat(connectionPolicy.getConnectionMode().equals(ConnectionMode.GATEWAY));
        validateGatewayConnectionConfig(connectionPolicy, gatewayConnectionConfig);
        safeCloseSyncClient(cosmosClient);
    }

    @Test(groups = { "emulator" })
    public void buildClient_withCustomGatewayConnectionConfig() {
        GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
        gatewayConnectionConfig.setMaxPoolSize(100);
        final List<String> preferredRegions = new ArrayList<>();
        preferredRegions.add("West US");
        gatewayConnectionConfig.setPreferredRegions(preferredRegions);
        gatewayConnectionConfig.setIdleConnectionTimeout(IDLE_CONNECTION_TIME_OUT);
        gatewayConnectionConfig.setRequestTimeout(REQUEST_TIME_OUT);
        CosmosClient cosmosClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .connectionModeGateway(gatewayConnectionConfig)
            .buildClient();

        AsyncDocumentClient asyncDocumentClient =
            CosmosBridgeInternal.getAsyncDocumentClient(cosmosClient);
        ConnectionPolicy connectionPolicy = asyncDocumentClient.getConnectionPolicy();
        assertThat(connectionPolicy.getConnectionMode().equals(ConnectionMode.GATEWAY));
        validateGatewayConnectionConfig(connectionPolicy, gatewayConnectionConfig);
        safeCloseSyncClient(cosmosClient);
    }

    @Test(groups = { "emulator" })
    public void buildClient_withDefaultDirectConnectionConfig() {
        DirectConnectionConfig directConnectionConfig = DirectConnectionConfig.getDefaultConfig();
        CosmosClient cosmosClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .connectionModeDirect(directConnectionConfig)
            .buildClient();

        AsyncDocumentClient asyncDocumentClient =
            CosmosBridgeInternal.getAsyncDocumentClient(cosmosClient);
        ConnectionPolicy connectionPolicy = asyncDocumentClient.getConnectionPolicy();
        assertThat(connectionPolicy.getConnectionMode().equals(ConnectionMode.DIRECT));
        validateDirectConnectionConfig(connectionPolicy, directConnectionConfig);
        safeCloseSyncClient(cosmosClient);
    }

    @Test(groups = { "emulator" })
    public void buildClient_withCustomDirectConnectionConfig() {
        DirectConnectionConfig directConnectionConfig = DirectConnectionConfig.getDefaultConfig();
        directConnectionConfig.setConnectionTimeout(CONNECTION_TIMEOUT);
        directConnectionConfig.setIdleChannelTimeout(IDLE_CHANNEL_TIMEOUT);
        directConnectionConfig.setIdleEndpointTimeout(IDLE_ENDPOINT_TIMEOUT);
        directConnectionConfig.setMaxChannelsPerEndpoint(100);
        directConnectionConfig.setMaxRequestsPerChannel(100);
        final List<String> preferredRegions = new ArrayList<>();
        preferredRegions.add("West US");
        directConnectionConfig.setPreferredRegions(preferredRegions);
        CosmosClient cosmosClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .connectionModeDirect(directConnectionConfig)
            .buildClient();

        AsyncDocumentClient asyncDocumentClient =
            CosmosBridgeInternal.getAsyncDocumentClient(cosmosClient);
        ConnectionPolicy connectionPolicy = asyncDocumentClient.getConnectionPolicy();
        assertThat(connectionPolicy.getConnectionMode().equals(ConnectionMode.DIRECT));
        validateDirectConnectionConfig(connectionPolicy, directConnectionConfig);
        safeCloseSyncClient(cosmosClient);
    }

    @Test(groups = { "emulator" })
    public void buildClient_withDirectAndGatewayConnectionConfig() {
        DirectConnectionConfig directConnectionConfig = DirectConnectionConfig.getDefaultConfig();
        GatewayConnectionConfig gatewayConnectionConfig = GatewayConnectionConfig.getDefaultConfig();
        CosmosClient cosmosClient = null;
        try {
            cosmosClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .connectionModeDirect(directConnectionConfig)
                .connectionModeGateway(gatewayConnectionConfig)
                .buildClient();
            Assertions.failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        } catch (Exception e) {
            assertThat(e instanceof IllegalArgumentException);
        } finally {
            safeCloseSyncClient(cosmosClient);
        }
    }

    @Test(groups = { "emulator" })
    public void buildClient_withNoConnectionConfig() {
        CosmosClient cosmosClient = null;
        try {
            cosmosClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .buildClient();
            Assertions.failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        } catch (Exception e) {
            assertThat(e instanceof IllegalArgumentException);
        } finally {
            safeCloseSyncClient(cosmosClient);
        }
    }

    private void validateGatewayConnectionConfig(ConnectionPolicy connectionPolicy, GatewayConnectionConfig gatewayConnectionConfig) {
        validateConnectionConfig(connectionPolicy, gatewayConnectionConfig);
        assertThat(Objects.equals(connectionPolicy.getIdleChannelTimeout(), gatewayConnectionConfig.getIdleConnectionTimeout()));
        assertThat(Objects.equals(connectionPolicy.getMaxPoolSize(), gatewayConnectionConfig.getMaxPoolSize()));
        assertThat(Objects.equals(connectionPolicy.getRequestTimeout(), gatewayConnectionConfig.getRequestTimeout()));
        assertThat(Objects.equals(connectionPolicy.getProxy(), gatewayConnectionConfig.getProxy()));
    }

    private void validateDirectConnectionConfig(ConnectionPolicy connectionPolicy, DirectConnectionConfig directConnectionConfig) {
        validateConnectionConfig(connectionPolicy, directConnectionConfig);
        assertThat(Objects.equals(connectionPolicy.getConnectionTimeout(), directConnectionConfig.getConnectionTimeout()));
        assertThat(Objects.equals(connectionPolicy.getIdleChannelTimeout(), directConnectionConfig.getIdleChannelTimeout()));
        assertThat(Objects.equals(connectionPolicy.getIdleConnectionTimeout(), directConnectionConfig.getIdleChannelTimeout()));
        assertThat(Objects.equals(connectionPolicy.getIdleEndpointTimeout(), directConnectionConfig.getIdleEndpointTimeout()));
        assertThat(Objects.equals(connectionPolicy.getMaxChannelsPerEndpoint(), directConnectionConfig.getMaxChannelsPerEndpoint()));
        assertThat(Objects.equals(connectionPolicy.getMaxRequestsPerChannel(), directConnectionConfig.getMaxRequestsPerChannel()));
    }

    private void validateConnectionConfig(ConnectionPolicy connectionPolicy, ConnectionConfig connectionConfig) {
        assertThat(Objects.equals(connectionPolicy.getConnectionMode(), connectionConfig.getConnectionMode()));
        assertThat(Objects.equals(connectionPolicy.isUsingMultipleWriteRegions(), connectionConfig.isUsingMultipleWriteRegions()));
        assertThat(Objects.equals(connectionPolicy.isEndpointDiscoveryEnabled(), connectionConfig.isEndpointDiscoveryEnabled()));
        assertThat(Objects.equals(connectionPolicy.isReadRequestsFallbackEnabled(), connectionConfig.isReadRequestsFallbackEnabled()));
        assertThat(Objects.equals(connectionPolicy.getPreferredRegions(), connectionConfig.getPreferredRegions()));
        assertThat(Objects.equals(connectionPolicy.getThrottlingRetryOptions(), connectionConfig.getThrottlingRetryOptions()));
        assertThat(Objects.equals(connectionPolicy.getUserAgentSuffix(), connectionConfig.getUserAgentSuffix()));
    }
}
