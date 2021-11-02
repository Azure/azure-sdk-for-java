// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.*;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.google.common.collect.ImmutableList;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosClientBuilderTest {

    String hostName = "https://sample-account.documents.azure.com:443/";
    private static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(100);
    private static final Duration IDLE_CHANNEL_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration IDLE_ENDPOINT_TIMEOUT = Duration.ofSeconds(20);

    @Test(groups = "unit")
    public void validateBadPreferredRegions1() {
        try {
            CosmosAsyncClient client = new CosmosClientBuilder()
                .key(TestConfigurations.MASTER_KEY)
                .endpoint(hostName)
                .preferredRegions(ImmutableList.of("westus1,eastus1"))
                .buildAsyncClient();
            client.close();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
            assertThat(e).hasCauseExactlyInstanceOf(URISyntaxException.class);
            assertThat(e.getMessage()).isEqualTo("invalid location [westus1,eastus1] or serviceEndpoint [https://sample-account.documents.azure.com:443/]");
        }
    }

    @Test(groups = "unit")
    public void validateBadPreferredRegions2() {
        try {
            CosmosAsyncClient client = new CosmosClientBuilder()
                .key(TestConfigurations.MASTER_KEY)
                .endpoint(hostName)
                .preferredRegions(ImmutableList.of(" "))
                .buildAsyncClient();
            client.close();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
            assertThat(e.getMessage()).isEqualTo("preferredRegion can't be empty");
        }
    }

    @Test(groups = "emulator")
    public void validateApiTypePresent() {
        DirectConnectionConfig directConnectionConfig = DirectConnectionConfig.getDefaultConfig();
        directConnectionConfig.setConnectTimeout(CONNECTION_TIMEOUT);
        directConnectionConfig.setIdleConnectionTimeout(IDLE_CHANNEL_TIMEOUT);
        directConnectionConfig.setIdleEndpointTimeout(IDLE_ENDPOINT_TIMEOUT);
        directConnectionConfig.setMaxConnectionsPerEndpoint(100);
        directConnectionConfig.setMaxRequestsPerConnection(100);
        final List<String> preferredRegions = new ArrayList<>();
        preferredRegions.add("West US");
        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .endpoint(hostName)
            .key(TestConfigurations.MASTER_KEY)
            .directMode(directConnectionConfig)
            .preferredRegions(preferredRegions)
            .userAgentSuffix("custom-direct-client")
            .multipleWriteRegionsEnabled(false)
            .endpointDiscoveryEnabled(false)
            .readRequestsFallbackEnabled(true);

         ImplementationBridgeHelpers.CosmosClientBuilderHelper.CosmosClientBuilderAccessor accessor =
            ImplementationBridgeHelpers.CosmosClientBuilderHelper.getCosmosClientBuilderAccessor();
         accessor.setCosmosClientApiType(cosmosClientBuilder, ApiType.TABLE);

        RxDocumentClientImpl documentClient =
            (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(new CosmosAsyncClient(cosmosClientBuilder));
        Assert.assertEquals(ReflectionUtils.getApiType(documentClient), ApiType.TABLE);
    }
}
