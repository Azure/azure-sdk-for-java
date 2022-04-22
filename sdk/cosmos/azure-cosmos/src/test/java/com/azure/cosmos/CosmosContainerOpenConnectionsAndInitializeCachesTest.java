// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.caches.AsyncCache;
import com.azure.cosmos.implementation.caches.RxClientCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.directconnectivity.GatewayServiceConfigurationReader;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpoint;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

/***
 * Please do not be confused by another test called {@link CosmosContainerOpenConnectionsAndInitCachesTest}.
 * This two classes each tests a different API relates to open connections.
 * {@link CosmosContainerOpenConnectionsAndInitCachesTest} -> openConnectionsAndInitCaches
 * {@link CosmosContainerOpenConnectionsAndInitializeCachesTest} -> openConnectionsAndInitializeCaches
 */
public class CosmosContainerOpenConnectionsAndInitializeCachesTest extends TestSuiteBase {
    private CosmosAsyncClient directCosmosAsyncClient;
    private CosmosAsyncDatabase directCosmosAsyncDatabase;
    private CosmosAsyncContainer directCosmosAsyncContainer;

    private CosmosAsyncClient gatewayCosmosAsyncClient;
    private CosmosAsyncDatabase gatewayCosmosAsyncDatabase;
    private CosmosAsyncContainer gatewayCosmosAsyncContainer;

    private CosmosClient directCosmosClient;
    private CosmosDatabase directCosmosDatabase;
    private CosmosContainer directCosmosContainer;

    private CosmosClient gatewayCosmosClient;
    private CosmosDatabase gatewayCosmosDatabase;
    private CosmosContainer gatewayCosmosContainer;

    private final static String CONTAINER_ID = "InitializedTestContainer";

    @BeforeClass(groups = {"simple"})
    public void beforeClass() {
        directCosmosAsyncClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .directMode()
                .buildAsyncClient();
        directCosmosAsyncDatabase = getSharedCosmosDatabase(directCosmosAsyncClient);
        directCosmosAsyncDatabase.createContainerIfNotExists(CONTAINER_ID, "/mypk",
                ThroughputProperties.createManualThroughput(20000)).block();
        directCosmosAsyncContainer = directCosmosAsyncDatabase.getContainer(CONTAINER_ID);

        gatewayCosmosAsyncClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .gatewayMode()
                .buildAsyncClient();
        gatewayCosmosAsyncDatabase = gatewayCosmosAsyncClient.getDatabase(directCosmosAsyncDatabase.getId());
        gatewayCosmosAsyncContainer = gatewayCosmosAsyncDatabase.getContainer(directCosmosAsyncContainer.getId());

        directCosmosClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .directMode()
                .buildClient();
        directCosmosDatabase = directCosmosClient.getDatabase(directCosmosAsyncDatabase.getId());
        directCosmosContainer = directCosmosDatabase.getContainer(CONTAINER_ID);

        gatewayCosmosClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .gatewayMode()
                .buildClient();
        gatewayCosmosDatabase = gatewayCosmosClient.getDatabase(directCosmosAsyncDatabase.getId());
        gatewayCosmosContainer = gatewayCosmosDatabase.getContainer(directCosmosAsyncContainer.getId());
    }

    @AfterClass(groups = {"simple"}, alwaysRun = true)
    public void afterClass() {
        if (this.directCosmosAsyncContainer != null) {
            this.directCosmosAsyncContainer.delete().block();
        }

        safeCloseAsync(directCosmosAsyncClient);
        safeCloseAsync(gatewayCosmosAsyncClient);
        safeCloseSyncClient(directCosmosClient);
        safeCloseSyncClient(gatewayCosmosClient);
    }

    @DataProvider(name = "useAsyncParameterProvider")
    public Object[][] useAsyncParameterProvider() {
        return new Object[][]{
                // flag to indicate whether it is sync or async call
                { true },
                { false }
        };
    }

    @Test(groups = {"simple"}, dataProvider = "useAsyncParameterProvider")
    public void openConnectionsAndInitCachesForDirectMode(boolean useAsync) {
        CosmosAsyncContainer asyncContainer = useAsync ? directCosmosAsyncContainer : directCosmosContainer.asyncContainer;
        CosmosAsyncClient asyncClient = useAsync ? directCosmosAsyncClient : directCosmosClient.asyncClient();

        RntbdTransportClient rntbdTransportClient = (RntbdTransportClient) ReflectionUtils.getTransportClient(asyncClient);
        RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncClient.getDocClientWrapper();
        RntbdEndpoint.Provider provider = ReflectionUtils.getRntbdEndpointProvider(rntbdTransportClient);
        String containerLink = asyncContainer.getLink();
        String containerId = asyncContainer.getId();

        GatewayServiceConfigurationReader configurationReader =
                ReflectionUtils.getServiceConfigurationReader(rxDocumentClient);

        ConcurrentHashMap<String, ?> routingMap = getRoutingMap(rxDocumentClient);
        ConcurrentHashMap<String, ?> collectionInfoByNameMap = getCollectionInfoByNameMap(rxDocumentClient);

        assertThat(provider.count()).isEqualTo(0);
        assertThat(collectionInfoByNameMap.size()).isEqualTo(0);
        assertThat(routingMap.size()).isEqualTo(0);
        assertThat(ReflectionUtils.isInitialized(asyncContainer).get()).isFalse();

        // Calling it twice to make sure no side effect of second time no-op call
        String resultForFirstCall;
        String resultForSecondCall;
        if (useAsync) {
            resultForFirstCall = directCosmosAsyncContainer.openConnectionsAndInitializeCaches().block();
            resultForSecondCall = directCosmosAsyncContainer.openConnectionsAndInitializeCaches().block();
        } else {
            resultForFirstCall = directCosmosContainer.openConnectionsAndInitializeCaches();
            resultForSecondCall = directCosmosContainer.openConnectionsAndInitializeCaches();
        }

        assertThat(collectionInfoByNameMap.size()).isEqualTo(1);
        assertThat(routingMap.size()).isEqualTo(1);
        assertThat(ReflectionUtils.isInitialized(asyncContainer).get()).isTrue();

        List<FeedRange> feedRanges = rxDocumentClient.getFeedRanges(containerLink).block();
        // The goal is to have at most 1 connection to each replica
        int numberOfReplicas = feedRanges.size() * configurationReader.getUserReplicationPolicy().getMaxReplicaSetSize();
        assertThat(provider.count()).isEqualTo(numberOfReplicas);

        // validate the openConnection call result
        assertThat(resultForFirstCall).isEqualTo(String.format("Established: %s, Failed: %s", numberOfReplicas, 0));
        assertThat(resultForSecondCall).isEqualTo(
                String.format(
                        "openConnectionsAndInitializeCaches is already called once on Container %s, no operation will take place in this call",
                        containerId));
    }

    @Test(groups = {"simple"}, dataProvider = "useAsyncParameterProvider")
    public void openConnectionsAndInitCachesForGatewayMode(boolean useAsync) {

        CosmosAsyncContainer asyncContainer = useAsync ? gatewayCosmosAsyncContainer : gatewayCosmosContainer.asyncContainer;
        CosmosAsyncClient asyncClient = useAsync ? gatewayCosmosAsyncClient : gatewayCosmosClient.asyncClient();

        RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncClient.getDocClientWrapper();
        String containerId = asyncContainer.getId();

        ConcurrentHashMap<String, ?> routingMap = getRoutingMap(rxDocumentClient);
        ConcurrentHashMap<String, ?> collectionInfoByNameMap = getCollectionInfoByNameMap(rxDocumentClient);

        assertThat(collectionInfoByNameMap.size()).isEqualTo(0);
        assertThat(routingMap.size()).isEqualTo(0);
        assertThat(ReflectionUtils.isInitialized(asyncContainer).get()).isFalse();

        // Verifying no error when initializeContainer called on gateway mode
        // Calling it twice to make sure no side effect of second time no-op call
        String resultForFirstCall;
        String resultForSecondCall;
        if (useAsync) {
            resultForFirstCall = gatewayCosmosAsyncContainer.openConnectionsAndInitializeCaches().block();
            resultForSecondCall = gatewayCosmosAsyncContainer.openConnectionsAndInitializeCaches().block();
        } else {
            resultForFirstCall = gatewayCosmosContainer.openConnectionsAndInitializeCaches();
            resultForSecondCall = gatewayCosmosContainer.openConnectionsAndInitializeCaches();
        }

        assertThat(collectionInfoByNameMap.size()).isEqualTo(0);
        assertThat(routingMap.size()).isEqualTo(0);
        assertThat(ReflectionUtils.isInitialized(asyncContainer).get()).isTrue();

        // validate the openConnection call result
        assertThat(resultForFirstCall).isEqualTo("Established: 0, Failed: 0");
        assertThat(resultForSecondCall).isEqualTo(
                String.format(
                        "openConnectionsAndInitializeCaches is already called once on Container %s, no operation will take place in this call",
                        containerId));
    }

    private ConcurrentHashMap<String, ?> getCollectionInfoByNameMap(RxDocumentClientImpl rxDocumentClient) {
        RxClientCollectionCache collectionCache =
                ReflectionUtils.getClientCollectionCache(rxDocumentClient);
        AsyncCache<String, DocumentCollection> collectionInfoByNameCache =
                ReflectionUtils.getCollectionInfoByNameCache(collectionCache);

        return ReflectionUtils.getValueMap(collectionInfoByNameCache);
    }

    private ConcurrentHashMap<String, ?> getRoutingMap(RxDocumentClientImpl rxDocumentClient) {
        RxPartitionKeyRangeCache partitionKeyRangeCache =
                ReflectionUtils.getPartitionKeyRangeCache(rxDocumentClient);
        AsyncCache<String, CollectionRoutingMap> routingMapAsyncCache =
                ReflectionUtils.getRoutingMapAsyncCache(partitionKeyRangeCache);

        return ReflectionUtils.getValueMap(routingMapAsyncCache);
    }
}
