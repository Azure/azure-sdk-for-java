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
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosContainerOpenConnectionsAndInitCachesTest extends TestSuiteBase {
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
        cosmosAsyncDatabase.createContainerIfNotExists(CONTAINER_ID, "/mypk",
            ThroughputProperties.createManualThroughput(20000)).block();
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
    public void loadCachesAndOpenConnectionsToServiceAsyncContainer() throws IllegalAccessException,
        NoSuchFieldException, ClassNotFoundException {

        System.setProperty("COSMOS.OPEN_ASYNC_RETIES_COUNT", "1");

        RntbdTransportClient rntbdTransportClient =
            (RntbdTransportClient) ReflectionUtils.getTransportClient(directCosmosAsyncClient);
        RntbdEndpoint.Provider provider = ReflectionUtils.getRntbdEndpointProvider(rntbdTransportClient);
        RxDocumentClientImpl rxDocumentClient =
            (RxDocumentClientImpl) directCosmosAsyncClient.getDocClientWrapper();

        GatewayServiceConfigurationReader configurationReader =
            ReflectionUtils.getServiceConfigurationReader(rxDocumentClient);

        ConcurrentHashMap<String, ?> routingMap = getRoutingMap(rxDocumentClient);
        ConcurrentHashMap<String, ?> collectionInfoByNameMap = getCollectionInfoByNameMap(rxDocumentClient);

        assertThat(provider.count()).isEqualTo(0);
        assertThat(collectionInfoByNameMap.size()).isEqualTo(0);
        assertThat(routingMap.size()).isEqualTo(0);
        assertThat(ReflectionUtils.isInitialized(cosmosAsyncContainer).get()).isFalse();

        // Calling it twice to make sure no side effect of second time no-op call
        cosmosAsyncContainer.openConnectionsAndInitCaches().block();
        cosmosAsyncContainer.openConnectionsAndInitCaches().block();

        // Verifying collectionInfoByNameMap size
        assertThat(collectionInfoByNameMap.size()).isEqualTo(1);
        // Verifying routingMap size
        assertThat(routingMap.size()).isEqualTo(1);
        // Verifying isInitialized is true
        assertThat(ReflectionUtils.isInitialized(cosmosAsyncContainer).get()).isTrue();

        List<FeedRange> feedRanges =
            rxDocumentClient.getFeedRanges(cosmosAsyncContainer.getLink()).block();
        int maxNumberOfConnection =
            feedRanges.size() * configurationReader.getUserReplicationPolicy().getMaxReplicaSetSize() / 2;
        CollectionRoutingMap collectionRoutingMap = getCollectionRoutingMap(routingMap);
        // Verifying tcp open connection count
        assertThat(provider.count()).isGreaterThanOrEqualTo(feedRanges.size());
        assertThat(provider.count()).isLessThanOrEqualTo(maxNumberOfConnection);
        // Verifying partitionKeyRanges list size
        assertThat(collectionRoutingMap.getOrderedPartitionKeyRanges().size()).isEqualTo(feedRanges.size());

        rxDocumentClient =
            (RxDocumentClientImpl) gatewayCosmosAsyncClient.getDocClientWrapper();
        routingMap = getRoutingMap(rxDocumentClient);
        collectionInfoByNameMap = getCollectionInfoByNameMap(rxDocumentClient);
        assertThat(collectionInfoByNameMap.size()).isEqualTo(0);
        assertThat(routingMap.size()).isEqualTo(0);
        CosmosAsyncContainer gatewayAsyncContainer =
            gatewayCosmosAsyncClient.getDatabase(cosmosDatabase.getId()).getContainer(cosmosContainer.getId());
        assertThat(ReflectionUtils.isInitialized(gatewayAsyncContainer).get()).isFalse();

        // Verifying no error when initializeContainer called on gateway mode
        // Calling it twice to make sure no side effect of second time no-op call
        gatewayAsyncContainer.openConnectionsAndInitCaches().block();
        gatewayAsyncContainer.openConnectionsAndInitCaches().block();

        // Verifying collectionInfoByNameMap size
        assertThat(collectionInfoByNameMap.size()).isEqualTo(1);
        // Verifying routingMap size
        assertThat(routingMap.size()).isEqualTo(1);
        // Verifying isInitialized is true
        assertThat(ReflectionUtils.isInitialized(gatewayAsyncContainer).get()).isTrue();


        feedRanges =
            rxDocumentClient.getFeedRanges(BridgeInternal.extractContainerSelfLink(cosmosAsyncContainer)).block();
        collectionRoutingMap = getCollectionRoutingMap(routingMap);
        // Verifying partitionKeyRanges list size
        assertThat(collectionRoutingMap.getOrderedPartitionKeyRanges().size()).isEqualTo(feedRanges.size());
    }

    @Test(groups = {"simple"})
    public void loadCachesAndOpenConnectionsToServiceSyncContainer() throws ClassNotFoundException,
        NoSuchFieldException, IllegalAccessException {

        System.setProperty("COSMOS.OPEN_ASYNC_RETIES_COUNT", "1");
        RntbdTransportClient rntbdTransportClient =
            (RntbdTransportClient) ReflectionUtils.getTransportClient(directCosmosClient);
        RntbdEndpoint.Provider provider = ReflectionUtils.getRntbdEndpointProvider(rntbdTransportClient);
        RxDocumentClientImpl rxDocumentClient =
            (RxDocumentClientImpl) directCosmosClient.asyncClient().getDocClientWrapper();

        GatewayServiceConfigurationReader configurationReader =
            ReflectionUtils.getServiceConfigurationReader(rxDocumentClient);

        ConcurrentHashMap<String, ?> routingMap = getRoutingMap(rxDocumentClient);
        ConcurrentHashMap<String, ?> collectionInfoByNameMap = getCollectionInfoByNameMap(rxDocumentClient);

        assertThat(provider.count()).isEqualTo(0);
        assertThat(collectionInfoByNameMap.size()).isEqualTo(0);
        assertThat(routingMap.size()).isEqualTo(0);
        assertThat(ReflectionUtils.isInitialized(cosmosContainer.asyncContainer).get()).isFalse();

        // Calling it twice to make sure no side effect of second time no-op call
        cosmosContainer.openConnectionsAndInitCaches();
        cosmosContainer.openConnectionsAndInitCaches();

        // Verifying collectionInfoByNameMap size
        assertThat(collectionInfoByNameMap.size()).isEqualTo(1);
        // Verifying routingMap size
        assertThat(routingMap.size()).isEqualTo(1);

        List<FeedRange> feedRanges =
            rxDocumentClient.getFeedRanges(BridgeInternal.extractContainerSelfLink(cosmosAsyncContainer)).block();
        int maxNumberOfConnection =
            feedRanges.size() * configurationReader.getUserReplicationPolicy().getMaxReplicaSetSize() / 2;
        CollectionRoutingMap collectionRoutingMap = getCollectionRoutingMap(routingMap);
        // Verifying tcp open connection count
        assertThat(provider.count()).isGreaterThanOrEqualTo(feedRanges.size());
        assertThat(provider.count()).isLessThanOrEqualTo(maxNumberOfConnection);
        // Verifying partitionKeyRanges list size
        assertThat(collectionRoutingMap.getOrderedPartitionKeyRanges().size()).isEqualTo(feedRanges.size());
        // Verifying isInitialized is true
        assertThat(ReflectionUtils.isInitialized(cosmosContainer.asyncContainer).get()).isTrue();

        rxDocumentClient =
            (RxDocumentClientImpl) gatewayCosmosClient.asyncClient().getDocClientWrapper();
        routingMap = getRoutingMap(rxDocumentClient);
        collectionInfoByNameMap = getCollectionInfoByNameMap(rxDocumentClient);
        assertThat(collectionInfoByNameMap.size()).isEqualTo(0);
        assertThat(routingMap.size()).isEqualTo(0);
        CosmosContainer gatewayContainer =
            gatewayCosmosClient.getDatabase(cosmosDatabase.getId()).getContainer(cosmosContainer.getId());
        assertThat(ReflectionUtils.isInitialized(gatewayContainer.asyncContainer).get()).isFalse();

        // Verifying no error when initializeContainer called on gateway mode
        // Calling it twice to make sure no side effect of second time no-op call
        gatewayContainer.openConnectionsAndInitCaches();
        gatewayContainer.openConnectionsAndInitCaches();

        // Verifying collectionInfoByNameMap size
        assertThat(collectionInfoByNameMap.size()).isEqualTo(1);
        // Verifying routingMap size
        assertThat(routingMap.size()).isEqualTo(1);
        // Verifying isInitialized is true
        assertThat(ReflectionUtils.isInitialized(gatewayContainer.asyncContainer).get()).isTrue();

        feedRanges =
            rxDocumentClient.getFeedRanges(BridgeInternal.extractContainerSelfLink(cosmosAsyncContainer)).block();
        collectionRoutingMap = getCollectionRoutingMap(routingMap);
        // Verifying partitionKeyRanges list size
        assertThat(collectionRoutingMap.getOrderedPartitionKeyRanges().size()).isEqualTo(feedRanges.size());
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

    @SuppressWarnings("unchecked")
    private CollectionRoutingMap getCollectionRoutingMap(ConcurrentHashMap<String, ?> routingMap) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Class<?> AsynLazyClass = Class.forName("com.azure.cosmos.implementation.caches.AsyncLazy");
        Field collectionRoutingMapField = AsynLazyClass.getDeclaredField("single");
        collectionRoutingMapField.setAccessible(true);
        CollectionRoutingMap collectionRoutingMap =
            ((Mono<CollectionRoutingMap>) collectionRoutingMapField.get(routingMap.values().toArray()[0])).block();
        return collectionRoutingMap;
    }
}
