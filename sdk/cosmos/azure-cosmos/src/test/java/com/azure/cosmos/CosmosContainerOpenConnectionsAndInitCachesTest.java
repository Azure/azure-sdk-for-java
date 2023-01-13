// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
import com.azure.cosmos.implementation.caches.AsyncCache;
import com.azure.cosmos.implementation.caches.AsyncCacheNonBlocking;
import com.azure.cosmos.implementation.caches.RxClientCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.directconnectivity.AddressInformation;
import com.azure.cosmos.implementation.directconnectivity.GlobalAddressResolver;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpoint;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import com.azure.cosmos.models.CosmosContainerIdentity;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;

public class CosmosContainerOpenConnectionsAndInitCachesTest extends TestSuiteBase {
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

    @Test(enabled = false)
    public void openConnectionThroughClientBuilder() {
        directCosmosAsyncDatabase.createContainerIfNotExists("id1", "/mypk").block();
        directCosmosAsyncDatabase.createContainerIfNotExists("id2", "/mypk").block();

        CosmosAsyncContainer cosmosContainer1 = directCosmosAsyncDatabase.getContainer("id1");
        CosmosAsyncContainer cosmosContainer2 = directCosmosAsyncDatabase.getContainer("id2");

        List<String> regions = new ArrayList<>();
        regions.add("East US");

        List<CosmosContainerIdentity> cosmosContainerIdentities = new ArrayList<>();
        cosmosContainerIdentities.add(new CosmosContainerIdentity(cosmosContainer1.getDatabase().getId(), cosmosContainer1.getId()));
        cosmosContainerIdentities.add(new CosmosContainerIdentity(cosmosContainer2.getDatabase().getId(), cosmosContainer2.getId()));

        ProactiveContainerInitConfig proactiveContainerInitConfig = new ProactiveContainerInitConfigBuilder(cosmosContainerIdentities)
                .setProactiveConnectionRegions(1)
                .build();

        CosmosAsyncClient cosmosAsyncClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .openConnectionsAndInitCaches(proactiveContainerInitConfig)
                .contentResponseOnWriteEnabled(true)
                .preferredRegions(Arrays.asList("East US"))
                .endpointDiscoveryEnabled(true)
                .directMode()
                .buildAsyncClient();

        RntbdTransportClient rntbdTransportClient = (RntbdTransportClient) ReflectionUtils.getTransportClient(cosmosAsyncClient);
        RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) cosmosAsyncClient.getDocClientWrapper();
        RntbdEndpoint.Provider provider = ReflectionUtils.getRntbdEndpointProvider(rntbdTransportClient);
        GlobalAddressResolver globalAddressResolver = ReflectionUtils.getGlobalAddressResolver(rxDocumentClient);
        GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);

        ConcurrentHashMap<String, ?> routingMap = getRoutingMap(rxDocumentClient);
        ConcurrentHashMap<String, ?> collectionInfoByNameMap = getCollectionInfoByNameMap(rxDocumentClient);
        Set<String> endpoints = ConcurrentHashMap.newKeySet();
        int count = provider.count();
        List<URI> proactiveConnectionEndpoints = globalEndpointManager.getReadEndpoints().subList(0, proactiveContainerInitConfig.getNumProactiveConnectionRegions());

        Flux<CosmosAsyncContainer> containerFlux = Flux.fromArray(new CosmosAsyncContainer[]{cosmosContainer1, cosmosContainer2});
        Flux<Utils.ValueHolder<List<PartitionKeyRange>>> partitionKeyRangeFlux = Flux.fromArray(new CosmosAsyncContainer[]{cosmosContainer1, cosmosContainer2})
                        .flatMap(CosmosAsyncContainer::read)
                        .flatMap(containerResponse -> {
                            return rxDocumentClient
                                    .getPartitionKeyRangeCache()
                                    .tryGetOverlappingRangesAsync(
                                            null,
                                            containerResponse.getProperties().getResourceId(),
                                            PartitionKeyInternalHelper.FullRange,
                                            true,
                                            null);
                        });

        // 1. Extract all preferred read regions to proactively connect to
        // 2. Resolve partition addresses for one read region, then mark read region as unavailable
        // 3. This will force resolveAsync to use the next preferred read region
        // 4. This way we can verify that connections have been opened to all replicas across all proactive
        // connection regions
        for (URI readEndpoint : proactiveConnectionEndpoints) {
            Flux.zip(containerFlux, partitionKeyRangeFlux)
                    .flatMapIterable(containerToPartitionKeyRanges -> {
                        List<ImmutablePair<PartitionKeyRange, CosmosAsyncContainer>> pkrToContainer = new ArrayList<>();
                        for (PartitionKeyRange pkr : containerToPartitionKeyRanges.getT2().v) {
                            pkrToContainer.add(new ImmutablePair<>(pkr, containerToPartitionKeyRanges.getT1()));
                        }
                        return pkrToContainer;
                    })
                    .flatMap(partitionKeyRangeToContainer -> {
                        RxDocumentServiceRequest dummyRequest = RxDocumentServiceRequest.createFromName(
                                mockDiagnosticsClientContext(),
                                OperationType.Read,
                                partitionKeyRangeToContainer.getRight().getLink() + "/docId",
                                ResourceType.Document);
                        dummyRequest.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity(partitionKeyRangeToContainer.getLeft().getId()));
                        return globalAddressResolver.resolveAsync(dummyRequest, false);
                    })
                    .doOnNext(addressInformations -> {
                        for (AddressInformation address : addressInformations) {
                            endpoints.add(address.getPhysicalUri().getURI().getAuthority());
                        }
                    })
                    .blockLast();

            globalEndpointManager.markEndpointUnavailableForRead(readEndpoint);
        }

        assertThat(provider.count()).isEqualTo(endpoints.size());
        assertThat(collectionInfoByNameMap.size()).isEqualTo(cosmosContainerIdentities.size());
        assertThat(routingMap.size()).isEqualTo(cosmosContainerIdentities.size());

        cosmosContainer1.delete().block();
        cosmosContainer2.delete().block();
        safeCloseAsync(cosmosAsyncClient);
    }

    @Test(groups = {"simple"}, dataProvider = "useAsyncParameterProvider")
    public void openConnectionsAndInitCachesForDirectMode(boolean useAsync) {
        CosmosAsyncContainer asyncContainer = useAsync ? directCosmosAsyncContainer : directCosmosContainer.asyncContainer;
        CosmosAsyncClient asyncClient = useAsync ? directCosmosAsyncClient : directCosmosClient.asyncClient();

        RntbdTransportClient rntbdTransportClient = (RntbdTransportClient) ReflectionUtils.getTransportClient(asyncClient);
        RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncClient.getDocClientWrapper();
        RntbdEndpoint.Provider provider = ReflectionUtils.getRntbdEndpointProvider(rntbdTransportClient);

        ConcurrentHashMap<String, ?> routingMap = getRoutingMap(rxDocumentClient);
        ConcurrentHashMap<String, ?> collectionInfoByNameMap = getCollectionInfoByNameMap(rxDocumentClient);

        assertThat(provider.count()).isEqualTo(0);
        assertThat(collectionInfoByNameMap.size()).isEqualTo(0);
        assertThat(routingMap.size()).isEqualTo(0);
        assertThat(ReflectionUtils.isInitialized(asyncContainer).get()).isFalse();

        // Calling it twice to make sure no side effect of second time no-op call
        if (useAsync) {
            directCosmosAsyncContainer.openConnectionsAndInitCaches().block();
            directCosmosAsyncContainer.openConnectionsAndInitCaches().block();
        } else {
            directCosmosContainer.openConnectionsAndInitCaches();
            directCosmosContainer.openConnectionsAndInitCaches();
        }

        assertThat(collectionInfoByNameMap.size()).isEqualTo(1);
        assertThat(routingMap.size()).isEqualTo(1);
        assertThat(ReflectionUtils.isInitialized(asyncContainer).get()).isTrue();

        GlobalAddressResolver globalAddressResolver = ReflectionUtils.getGlobalAddressResolver(rxDocumentClient);
        Set<String> endpoints = ConcurrentHashMap.newKeySet();

        // Using the following way to get all the unique service endpoints
        // For openConnectionsAndInitCaches, the global is open at most 1 connection for each service endpoint
        asyncContainer.read()
                .flatMap(containerResponse -> {
                    return rxDocumentClient
                            .getPartitionKeyRangeCache()
                            .tryGetOverlappingRangesAsync(
                                    null,
                                    containerResponse.getProperties().getResourceId(),
                                    PartitionKeyInternalHelper.FullRange,
                                    true,
                                    null);
                })
                .flatMapIterable(pkRangesValueHolder -> pkRangesValueHolder.v)
                .flatMap(partitionKeyRange -> {
                    RxDocumentServiceRequest dummyRequest = RxDocumentServiceRequest.createFromName(
                            mockDiagnosticsClientContext(),
                            OperationType.Read,
                            asyncContainer.getLink() + "/docId",
                            ResourceType.Document);
                    dummyRequest.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity(partitionKeyRange.getId()));
                    return globalAddressResolver.resolveAsync(dummyRequest, false);
                })
                .doOnNext(addressInformations -> {
                    for (AddressInformation address : addressInformations) {
                        endpoints.add(address.getPhysicalUri().getURI().getAuthority());
                    }
                })
                .blockLast();

        assertThat(provider.count()).isEqualTo(endpoints.size());

        // Validate for each RntbdServiceEndpoint, one channel is being opened
        provider.list().forEach(rntbdEndpoint -> assertThat(rntbdEndpoint.channelsMetrics()).isEqualTo(1));

        // Test for real document requests, it will not open new channels
        for (int i = 0; i < 5; i++) {
            if (useAsync) {
                directCosmosAsyncContainer.createItem(TestObject.create()).block();
            } else {
                directCosmosContainer.createItem(TestObject.create());
            }
        }
        provider.list().forEach(rntbdEndpoint -> assertThat(rntbdEndpoint.channelsMetrics()).isEqualTo(1));
    }

    @Test(groups = {"simple"}, dataProvider = "useAsyncParameterProvider")
    public void openConnectionsAndInitCachesForGatewayMode(boolean useAsync) {

        CosmosAsyncContainer asyncContainer = useAsync ? gatewayCosmosAsyncContainer : gatewayCosmosContainer.asyncContainer;
        CosmosAsyncClient asyncClient = useAsync ? gatewayCosmosAsyncClient : gatewayCosmosClient.asyncClient();

        RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncClient.getDocClientWrapper();

        ConcurrentHashMap<String, ?> routingMap = getRoutingMap(rxDocumentClient);
        ConcurrentHashMap<String, ?> collectionInfoByNameMap = getCollectionInfoByNameMap(rxDocumentClient);

        assertThat(collectionInfoByNameMap.size()).isEqualTo(0);
        assertThat(routingMap.size()).isEqualTo(0);
        assertThat(ReflectionUtils.isInitialized(asyncContainer).get()).isFalse();

        // Verifying no error when initializeContainer called on gateway mode
        // Calling it twice to make sure no side effect of second time no-op call
        if (useAsync) {
            gatewayCosmosAsyncContainer.openConnectionsAndInitCaches().block();
            gatewayCosmosAsyncContainer.openConnectionsAndInitCaches().block();
        } else {
            gatewayCosmosContainer.openConnectionsAndInitCaches();
            gatewayCosmosContainer.openConnectionsAndInitCaches();
        }

        assertThat(collectionInfoByNameMap.size()).isEqualTo(0);
        assertThat(routingMap.size()).isEqualTo(0);
        assertThat(ReflectionUtils.isInitialized(asyncContainer).get()).isTrue();
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
        AsyncCacheNonBlocking<String, CollectionRoutingMap> routingMapAsyncCache =
                ReflectionUtils.getRoutingMapAsyncCacheNonBlocking(partitionKeyRangeCache);

        return ReflectionUtils.getValueMapNonBlockingCache(routingMapAsyncCache);
    }
}
