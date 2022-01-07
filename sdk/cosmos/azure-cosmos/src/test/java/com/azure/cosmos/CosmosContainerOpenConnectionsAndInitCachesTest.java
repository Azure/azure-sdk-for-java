// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.caches.AsyncCache;
import com.azure.cosmos.implementation.caches.RxClientCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.directconnectivity.AddressInformation;
import com.azure.cosmos.implementation.directconnectivity.AddressResolver;
import com.azure.cosmos.implementation.directconnectivity.AddressSelector;
import com.azure.cosmos.implementation.directconnectivity.ConsistencyReader;
import com.azure.cosmos.implementation.directconnectivity.GatewayAddressCache;
import com.azure.cosmos.implementation.directconnectivity.GlobalAddressResolver;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.ReplicatedResourceClient;
import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import com.azure.cosmos.implementation.directconnectivity.StoreClient;
import com.azure.cosmos.implementation.directconnectivity.StoreReader;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpoint;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.rx.TestSuiteBase;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    public void beforeClass() throws InterruptedException {
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
        cosmosAsyncDatabase =
        getSharedCosmosDatabase(directCosmosAsyncClient);
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
        cosmosContainer = cosmosDatabase.getContainer(cosmosAsyncContainer.getId());
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
    public void loadCachesAndOpenConnectionsToServiceAsyncContainer() throws Exception {
        RntbdTransportClient rntbdTransportClient =
            (RntbdTransportClient) ReflectionUtils.getTransportClient(directCosmosAsyncClient);
        RntbdEndpoint.Provider provider = ReflectionUtils.getRntbdEndpointProvider(rntbdTransportClient);
        RxDocumentClientImpl rxDocumentClient =
            (RxDocumentClientImpl) directCosmosAsyncClient.getDocClientWrapper();

        ConcurrentHashMap<String, ?> routingMap = getRoutingMap(rxDocumentClient);
        ConcurrentHashMap<String, ?> collectionInfoByNameMap = getCollectionInfoByNameMap(rxDocumentClient);
        GatewayAddressCache spyGatewayAddressCache = createAndSetSpyGatewayAddressCacheOnConsistencyReader(rxDocumentClient);

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

        // Get all the unique physical addresses of physical partitions
        ConcurrentHashMap<PartitionKeyRangeIdentity, AddressInformation[]> addressCache =
            getServerPartitionAddressCache(rxDocumentClient);
        HashSet<String> endpointList = getUniqueEndpoint(addressCache);

        List<FeedRange> feedRanges =
            rxDocumentClient.getFeedRanges(cosmosAsyncContainer.getLink()).block();
        CollectionRoutingMap collectionRoutingMap = getCollectionRoutingMap(routingMap);

        // Verifying tcp open connection count
        assertThat(provider.count()).isEqualTo(endpointList.size());
        // Verifying address cache partition key range size
        assertThat(addressCache.keySet().size()).isEqualTo(feedRanges.size());
        // Verifying partitionKeyRanges list size
        assertThat(collectionRoutingMap.getOrderedPartitionKeyRanges().size()).isEqualTo(feedRanges.size());
        // Verifying we are not doing any extra address refresh call to gateway
        Mockito.verify(spyGatewayAddressCache, Mockito.times(feedRanges.size())).getServerAddressesViaGatewayAsync(ArgumentMatchers.any(), ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.anyBoolean());
        // Verifying isInitialized is true
        assertThat(ReflectionUtils.isInitialized(cosmosAsyncContainer).get()).isTrue();

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
    public void loadCachesAndOpenConnectionsToServiceSyncContainer() throws Exception {

        RntbdTransportClient rntbdTransportClient =
            (RntbdTransportClient) ReflectionUtils.getTransportClient(directCosmosClient);
        RntbdEndpoint.Provider provider = ReflectionUtils.getRntbdEndpointProvider(rntbdTransportClient);
        RxDocumentClientImpl rxDocumentClient =
            (RxDocumentClientImpl) directCosmosClient.asyncClient().getDocClientWrapper();

        ConcurrentHashMap<String, ?> routingMap = getRoutingMap(rxDocumentClient);
        ConcurrentHashMap<String, ?> collectionInfoByNameMap = getCollectionInfoByNameMap(rxDocumentClient);
        GatewayAddressCache spyGatewayAddressCache = createAndSetSpyGatewayAddressCacheOnConsistencyReader(rxDocumentClient);

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

        // Get all the unique physical addresses of physical partitions
        ConcurrentHashMap<PartitionKeyRangeIdentity, AddressInformation[]> addressCache =
            getServerPartitionAddressCache(rxDocumentClient);
        HashSet<String> endpointList = getUniqueEndpoint(addressCache);

        List<FeedRange> feedRanges =
            rxDocumentClient.getFeedRanges(BridgeInternal.extractContainerSelfLink(cosmosAsyncContainer)).block();
        CollectionRoutingMap collectionRoutingMap = getCollectionRoutingMap(routingMap);
        // Verifying tcp open connection count
        assertThat(provider.count()).isGreaterThanOrEqualTo(feedRanges.size());
        assertThat(provider.count()).isEqualTo(endpointList.size());
        // Verifying address cache partition key range size
        assertThat(addressCache.keySet().size()).isEqualTo(feedRanges.size());
        // Verifying partitionKeyRanges list size
        assertThat(collectionRoutingMap.getOrderedPartitionKeyRanges().size()).isEqualTo(feedRanges.size());
        // Verifying we are not doing any extra address refresh call to gateway
        Mockito.verify(spyGatewayAddressCache, Mockito.times(feedRanges.size())).getServerAddressesViaGatewayAsync(ArgumentMatchers.any(), ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.anyBoolean());
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

    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<PartitionKeyRangeIdentity, AddressInformation[]> getServerPartitionAddressCache(RxDocumentClientImpl rxDocumentClient) throws Exception {
        Map getAddressCacheByEndpoint = (Map) FieldUtils.readField(ReflectionUtils.getGlobalAddressResolver(rxDocumentClient), "addressCacheByEndpoint", true);
        Object endpointCache = getAddressCacheByEndpoint.values().toArray()[0];
        GatewayAddressCache gatewayAddressCache = (GatewayAddressCache) FieldUtils.readField(endpointCache, "addressCache", true);
        AsyncCache<PartitionKeyRangeIdentity, ?> serverPartitionAddressCacheAsync=  (AsyncCache<PartitionKeyRangeIdentity, ?>) FieldUtils.readField(gatewayAddressCache, "serverPartitionAddressCache", true);
        ConcurrentHashMap<PartitionKeyRangeIdentity, ?> serverPartitionAddressCacheLazy = ReflectionUtils.getValueMap(serverPartitionAddressCacheAsync);

        ConcurrentHashMap<PartitionKeyRangeIdentity, AddressInformation[]> addressCache = new ConcurrentHashMap<>();
        for(Map.Entry<PartitionKeyRangeIdentity, ?> partitionKeyRangeIdentityEntry : serverPartitionAddressCacheLazy.entrySet()) {
            addressCache.put(partitionKeyRangeIdentityEntry.getKey(), getAddressInformationFromAsyncLazy(partitionKeyRangeIdentityEntry.getValue()));
        }
        return addressCache;
    }

    private GatewayAddressCache createAndSetSpyGatewayAddressCacheOnConsistencyReader(RxDocumentClientImpl rxDocumentClient) throws Exception {
        // Get the existing GatewayAddressCache which was created on client creation and create the spy on it
        GlobalAddressResolver globalAddressResolver = ReflectionUtils.getGlobalAddressResolver(rxDocumentClient);
        Map getAddressCacheByEndpoint = (Map) FieldUtils.readField(globalAddressResolver, "addressCacheByEndpoint", true);
        Object endpointCache = getAddressCacheByEndpoint.values().toArray()[0];
        Class<?>  EndpointCacheClass = Class.forName("com.azure.cosmos.implementation.directconnectivity.GlobalAddressResolver$EndpointCache");
        Field gatewayAddressCacheField = EndpointCacheClass.getDeclaredField("addressCache");
        gatewayAddressCacheField.setAccessible(true);
        GatewayAddressCache gatewayAddressCache = (GatewayAddressCache) gatewayAddressCacheField.get(endpointCache);
        GatewayAddressCache spyGatewayAddressCache = Mockito.spy(gatewayAddressCache);

        // Get the address selector used during ConsistencyReader creation
        StoreClient storeClient = ReflectionUtils.getStoreClient(rxDocumentClient);
        ReplicatedResourceClient replicatedResourceClient =
            ReflectionUtils.getReplicatedResourceClient(storeClient);
        ConsistencyReader consistencyReader = ReflectionUtils.getConsistencyReader(replicatedResourceClient);
        StoreReader storeReader = ReflectionUtils.getStoreReader(consistencyReader);
        AddressSelector addressSelector = ReflectionUtils.getAddressSelector(storeReader);

        // Set the new spy gatewayAddressCache which will be used on every call of ConsistencyReader
        GlobalAddressResolver globalAddressResolverNew = ReflectionUtils.getGlobalAddressResolver(addressSelector);
        Map getAddressCacheByEndpointNew = (Map) FieldUtils.readField(globalAddressResolverNew, "addressCacheByEndpoint", true);
        Object endpointCacheNew = getAddressCacheByEndpointNew.values().toArray()[0];
        Field addressResolverField = EndpointCacheClass.getDeclaredField("addressResolver");
        addressResolverField.setAccessible(true);
        AddressResolver addressResolver = (AddressResolver) addressResolverField.get(endpointCacheNew);
        ReflectionUtils.setGatewayAddressCache(addressResolver, spyGatewayAddressCache);

        return spyGatewayAddressCache;
    }

    private AddressInformation[] getAddressInformationFromAsyncLazy(Object asyncLazyValue) throws Exception {
        Class<?>  AsyncLazyClass = Class.forName("com.azure.cosmos.implementation.caches.AsyncLazy");
        Field addressInformationMonoSingle = AsyncLazyClass.getDeclaredField("single");
        addressInformationMonoSingle.setAccessible(true);
        Mono<AddressInformation[]> addressInformationMono = (Mono<AddressInformation[]>)addressInformationMonoSingle.get(asyncLazyValue);
        return addressInformationMono.block();
    }

    private HashSet<String> getUniqueEndpoint(ConcurrentHashMap<PartitionKeyRangeIdentity, AddressInformation[]> addressMap) {
        HashSet<String> endpointList = new HashSet<>();
        for (AddressInformation[] addressInformations : addressMap.values()) {
            for (AddressInformation addressInformation : addressInformations) {
                endpointList.add(addressInformation.getPhysicalUri().getURI().getAuthority());
            }
        }
        return endpointList;
    }

}
