/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.directconnectivity.GatewayServiceConfigurationReader;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpoint;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosContainerInitializationTest extends TestSuiteBase {
    private CosmosAsyncClient directCosmosAsyncClient;
    private CosmosAsyncClient gatewayCosmosAsyncClient;
    private CosmosAsyncDatabase cosmosAsyncDatabase;
    private CosmosAsyncContainer cosmosAsyncContainer;

    private CosmosClient directCosmosClient;
    private CosmosClient gatewayCosmosClient;
    private CosmosDatabase cosmosDatabase;
    private CosmosContainer cosmosContainer;

    private final static String CONTAINER_ID = "InitializedTestContainer";

    @BeforeClass(groups = {"simple"})
    public void beforeClass() {
        directCosmosAsyncClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .directMode()
            .buildAsyncClient();
        gatewayCosmosAsyncClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .gatewayMode()
            .buildAsyncClient();
        cosmosAsyncDatabase = getSharedCosmosDatabase(directCosmosAsyncClient);
        cosmosAsyncDatabase.createContainerIfNotExists(CONTAINER_ID, "/mypk", 20000).block();
        cosmosAsyncContainer = cosmosAsyncDatabase.getContainer(CONTAINER_ID);

        directCosmosClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .directMode()
            .buildClient();
        gatewayCosmosClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .gatewayMode()
            .buildClient();
        cosmosDatabase = directCosmosClient.getDatabase(cosmosAsyncDatabase.getId());
        cosmosContainer = cosmosDatabase.getContainer(CONTAINER_ID);
    }

    @AfterClass(groups = {"simple"}, alwaysRun = true)
    public void afterClass() {
        if (this.cosmosAsyncContainer != null) {
            this.cosmosAsyncContainer.delete().block();
        }

        safeCloseAsync(directCosmosAsyncClient);
        safeCloseAsync(gatewayCosmosAsyncClient);
        safeCloseSyncClient(directCosmosClient);
        safeCloseSyncClient(gatewayCosmosClient);
    }

    @Test(groups = {"simple"})
    public void initializeContainerAsync() {
        RntbdTransportClient rntbdTransportClient =
            (RntbdTransportClient) ReflectionUtils.getTransportClient(directCosmosAsyncClient);
        RntbdEndpoint.Provider provider = ReflectionUtils.getRntbdEndpointProvider(rntbdTransportClient);
        GatewayServiceConfigurationReader configurationReader =
            ReflectionUtils.getServiceConfigurationReader((RxDocumentClientImpl) directCosmosAsyncClient.getDocClientWrapper());
        List<FeedRange> feedRanges =
            directCosmosAsyncClient.getDocClientWrapper().getFeedRanges(cosmosAsyncContainer.getLink()).block();

        assertThat(provider.count()).isEqualTo(0);
        cosmosAsyncContainer.initializeContainerAsync().block();

        int maxNumberOfConnection =
            feedRanges.size() * configurationReader.getUserReplicationPolicy().getMaxReplicaSetSize() / 2;
        // Verifying tcp open connection count
        assertThat(provider.count()).isGreaterThanOrEqualTo(feedRanges.size());
        assertThat(provider.count()).isLessThanOrEqualTo(maxNumberOfConnection);

        // Verifying no error when initializeContainerAsync called on gateway mode
        gatewayCosmosAsyncClient.getDatabase(cosmosAsyncDatabase.getId()).getContainer(cosmosAsyncContainer.getId()).initializeContainerAsync();
    }

    @Test(groups = {"simple"})
    public void initializeContainer() {
        RntbdTransportClient rntbdTransportClient =
            (RntbdTransportClient) ReflectionUtils.getTransportClient(directCosmosClient);
        RntbdEndpoint.Provider provider = ReflectionUtils.getRntbdEndpointProvider(rntbdTransportClient);
        GatewayServiceConfigurationReader configurationReader =
            ReflectionUtils.getServiceConfigurationReader((RxDocumentClientImpl) directCosmosClient.asyncClient().getDocClientWrapper());
        List<FeedRange> feedRanges =
            directCosmosClient.asyncClient().getDocClientWrapper().getFeedRanges(cosmosAsyncContainer.getLink()).block();

        assertThat(provider.count()).isEqualTo(0);
        cosmosContainer.initializeContainer();

        int maxNumberOfConnection =
            feedRanges.size() * configurationReader.getUserReplicationPolicy().getMaxReplicaSetSize() / 2;
        // Verifying tcp open connection count
        assertThat(provider.count()).isGreaterThanOrEqualTo(feedRanges.size());
        assertThat(provider.count()).isLessThanOrEqualTo(maxNumberOfConnection);

        // Verifying no error when initializeContainer called on gateway mode
        gatewayCosmosClient.getDatabase(cosmosDatabase.getId()).getContainer(cosmosContainer.getId()).initializeContainer();
    }
}
