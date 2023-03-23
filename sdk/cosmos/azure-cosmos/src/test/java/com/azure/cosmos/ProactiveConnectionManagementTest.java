// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
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
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ProactiveConnectionManagementTest extends TestSuiteBase {

    private CosmosClientBuilder clientBuilder;
    private DatabaseAccount databaseAccount;
    private CosmosAsyncDatabase cosmosAsyncDatabase;

    @BeforeClass(groups = {"multi-region"})
    public void beforeClass() {
        clientBuilder = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .directMode();

        CosmosAsyncClient dummyClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .directMode().buildAsyncClient();

        this.cosmosAsyncDatabase = getSharedCosmosDatabase(dummyClient);

        AsyncDocumentClient asyncDocumentClient = ReflectionUtils.getAsyncDocumentClient(dummyClient);
        RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncDocumentClient;
        GlobalEndpointManager globalEndpointManager =
                ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
        this.databaseAccount = globalEndpointManager.getLatestDatabaseAccount();

        safeClose(dummyClient);
    }

    @Test(groups = {"multi-region"}, dataProvider = "invalidProactiveContainerInitConfigs")
    public void openConnectionsAndInitCachesWithInvalidCosmosClientConfig(List<String> preferredRegions, int numProactiveConnectionRegions, int numContainers) {

        List<CosmosAsyncContainer> asyncContainers = new ArrayList<>();
        List<CosmosContainerIdentity> cosmosContainerIdentities = new ArrayList<>();

        for (int i = 1; i <= numContainers; i++) {
            String containerId = String.format("id%d", i);
            cosmosAsyncDatabase.createContainerIfNotExists(containerId, "/mypk").block();
            asyncContainers.add(cosmosAsyncDatabase.getContainer(containerId));
            cosmosContainerIdentities.add(new CosmosContainerIdentity(cosmosAsyncDatabase.getId(), containerId));
        }

        if (numProactiveConnectionRegions > 5) {
            try {
                new CosmosContainerProactiveInitConfigBuilder(cosmosContainerIdentities)
                    .setProactiveConnectionRegionsCount(numProactiveConnectionRegions)
                    .build();
                fail("Should have thrown exception");
            } catch (IllegalArgumentException illegalArgEx) {}

        } else {
            CosmosContainerProactiveInitConfig proactiveContainerInitConfig = new CosmosContainerProactiveInitConfigBuilder(cosmosContainerIdentities)
                .setProactiveConnectionRegionsCount(numProactiveConnectionRegions)
                .build();

            try {
                CosmosAsyncClient clientWithOpenConnections = new CosmosClientBuilder()
                    .endpoint(TestConfigurations.HOST)
                    .key(TestConfigurations.MASTER_KEY)
                    .endpointDiscoveryEnabled(true)
                    .preferredRegions(preferredRegions)
                    .openConnectionsAndInitCaches(proactiveContainerInitConfig)
                    .directMode()
                    .buildAsyncClient();
                fail("Should have thrown exception");
            } catch (IllegalArgumentException illegalArgEx) {}
        }
    }

    @Test(groups = {"multi-region"}, dataProvider = "proactiveContainerInitConfigs")
    public void openConnectionsAndInitCachesWithCosmosClient(List<String> preferredRegions, int numProactiveConnectionRegions, int numContainers) {

        CosmosAsyncClient clientWithOpenConnections = null;

        try {

            List<CosmosAsyncContainer> asyncContainers = new ArrayList<>();
            List<CosmosContainerIdentity> cosmosContainerIdentities = new ArrayList<>();

            for (int i = 1; i <= numContainers; i++) {
                String containerId = String.format("id%d", i);
                cosmosAsyncDatabase.createContainerIfNotExists(containerId, "/mypk").block();
                asyncContainers.add(cosmosAsyncDatabase.getContainer(containerId));
                cosmosContainerIdentities.add(new CosmosContainerIdentity(cosmosAsyncDatabase.getId(), containerId));
            }

            CosmosContainerProactiveInitConfigBuilder cosmosContainerProactiveInitConfigBuilder = new
                    CosmosContainerProactiveInitConfigBuilder(cosmosContainerIdentities)
                    .setProactiveConnectionRegionsCount(numProactiveConnectionRegions);

            for (int i = 1; i <= numContainers; i++) {
                cosmosContainerProactiveInitConfigBuilder = cosmosContainerProactiveInitConfigBuilder
                        .withMinConnectionsPerReplicaForContainer(cosmosContainerIdentities.get(i - 1), i);
            }

            CosmosContainerProactiveInitConfig proactiveContainerInitConfig = cosmosContainerProactiveInitConfigBuilder
                    .build();

            clientWithOpenConnections = new CosmosClientBuilder()
                    .endpoint(TestConfigurations.HOST)
                    .key(TestConfigurations.MASTER_KEY)
                    .endpointDiscoveryEnabled(true)
                    .preferredRegions(preferredRegions)
                    .openConnectionsAndInitCaches(proactiveContainerInitConfig)
                    .directMode()
                    .buildAsyncClient();

            RntbdTransportClient rntbdTransportClient = (RntbdTransportClient) ReflectionUtils.getTransportClient(clientWithOpenConnections);
            AsyncDocumentClient asyncDocumentClient = ReflectionUtils.getAsyncDocumentClient(clientWithOpenConnections);
            RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncDocumentClient;
            GlobalAddressResolver globalAddressResolver = ReflectionUtils.getGlobalAddressResolver(rxDocumentClient);
            GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
            RntbdEndpoint.Provider provider = ReflectionUtils.getRntbdEndpointProvider(rntbdTransportClient);

            ConcurrentHashMap<String, ?> routingMap = getRoutingMap(rxDocumentClient);
            ConcurrentHashMap<String, ?> collectionInfoByNameMap = getCollectionInfoByNameMap(rxDocumentClient);
            Set<String> endpoints = ConcurrentHashMap.newKeySet();
            UnmodifiableList<URI> readEndpoints = globalEndpointManager.getReadEndpoints();
            List<URI> proactiveConnectionEndpoints = readEndpoints.subList(
                0,
                Math.min(readEndpoints.size(), proactiveContainerInitConfig.getProactiveConnectionRegionsCount()));

            Flux<CosmosAsyncContainer> asyncContainerFlux = Flux.fromIterable(asyncContainers);

            Flux<Utils.ValueHolder<List<PartitionKeyRange>>> partitionKeyRangeFlux = Flux.fromIterable(asyncContainers)
                    .flatMap(CosmosAsyncContainer::read)
                    .flatMap(containerResponse -> rxDocumentClient
                            .getPartitionKeyRangeCache()
                            .tryGetOverlappingRangesAsync(
                                    null,
                                    containerResponse.getProperties().getResourceId(),
                                    PartitionKeyInternalHelper.FullRange,
                                    false,
                                    null));

            // 1. Extract all preferred read regions to proactively connect to.
            // 2. Obtain partition addresses for a container for one read region, then mark that read region as unavailable.
            // 3. This will force resolveAsync to use the next preferred read region in the next invocation.
            // 4. This way we can verify that connections have been opened to all replicas across all proactive connection regions.
            for (URI proactiveConnectionEndpoint : proactiveConnectionEndpoints) {
                Flux.zip(asyncContainerFlux, partitionKeyRangeFlux)
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
                        .delayElements(Duration.ofSeconds(3))
                        .doOnNext(addressInformations -> {
                            for (AddressInformation address : addressInformations) {
                                endpoints.add(address.getPhysicalUri().getURI().getAuthority());
                            }
                        })
                        .blockLast();

                globalEndpointManager.markEndpointUnavailableForRead(proactiveConnectionEndpoint);
            }

            assertThat(provider.count()).isEqualTo(endpoints.size());
            assertThat(collectionInfoByNameMap.size()).isEqualTo(cosmosContainerIdentities.size());
            assertThat(routingMap.size()).isEqualTo(cosmosContainerIdentities.size());

            for (RntbdEndpoint rntbdEndpoint : provider.list().collect(Collectors.toList())) {
                System.out.println("Endpoint name : " + rntbdEndpoint.id() + ";" + "Connections count : " + rntbdEndpoint.channelsMetrics());
            }

            for (CosmosAsyncContainer asyncContainer : asyncContainers) {
                asyncContainer.delete().block();
            }

        } finally {
            safeClose(clientWithOpenConnections);
        }
    }

    @Test(groups = {"multi-region"}, dataProvider = "proactiveContainerInitConfigs")
    public void openConnectionsAndInitCachesWithContainer(List<String> preferredRegions, int numProactiveConnectionRegions, int ignore) {
        CosmosAsyncClient asyncClient = null;

        try {

            asyncClient = new CosmosClientBuilder()
                    .endpoint(TestConfigurations.HOST)
                    .key(TestConfigurations.MASTER_KEY)
                    .endpointDiscoveryEnabled(true)
                    .preferredRegions(preferredRegions)
                    .directMode()
                    .buildAsyncClient();

            cosmosAsyncDatabase = getSharedCosmosDatabase(asyncClient);

            List<CosmosContainerIdentity> cosmosContainerIdentities = new ArrayList<>();

            String containerId = "id1";
            cosmosAsyncDatabase.createContainerIfNotExists(containerId, "/mypk").block();

            CosmosAsyncContainer cosmosAsyncContainer = cosmosAsyncDatabase.getContainer(containerId);

            cosmosContainerIdentities.add(new CosmosContainerIdentity(cosmosAsyncDatabase.getId(), containerId));

            CosmosContainerProactiveInitConfig proactiveContainerInitConfig = new CosmosContainerProactiveInitConfigBuilder(cosmosContainerIdentities)
                    .setProactiveConnectionRegionsCount(numProactiveConnectionRegions)
                    .build();

            RntbdTransportClient rntbdTransportClient = (RntbdTransportClient) ReflectionUtils.getTransportClient(asyncClient);
            AsyncDocumentClient asyncDocumentClient = ReflectionUtils.getAsyncDocumentClient(asyncClient);
            RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncDocumentClient;
            GlobalAddressResolver globalAddressResolver = ReflectionUtils.getGlobalAddressResolver(rxDocumentClient);
            GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
            RntbdEndpoint.Provider provider = ReflectionUtils.getRntbdEndpointProvider(rntbdTransportClient);

            ConcurrentHashMap<String, ?> routingMap = getRoutingMap(rxDocumentClient);
            ConcurrentHashMap<String, ?> collectionInfoByNameMap = getCollectionInfoByNameMap(rxDocumentClient);
            Set<String> endpoints = ConcurrentHashMap.newKeySet();

            cosmosAsyncContainer.openConnectionsAndInitCaches(numProactiveConnectionRegions).block();

            UnmodifiableList<URI> readEndpoints =
                globalEndpointManager.getReadEndpoints();
            List<URI> proactiveConnectionEndpoints = readEndpoints.subList(
                0,
                Math.min(readEndpoints.size(),proactiveContainerInitConfig.getProactiveConnectionRegionsCount()));

            Mono<CosmosAsyncContainer> asyncContainerMono = Mono.just(cosmosAsyncContainer);

            Mono<Utils.ValueHolder<List<PartitionKeyRange>>> partitionKeyRangeMono = Mono.just(cosmosAsyncContainer)
                    .flatMap(CosmosAsyncContainer::read)
                    .flatMap(containerResponse -> rxDocumentClient
                            .getPartitionKeyRangeCache()
                            .tryGetOverlappingRangesAsync(
                                    null,
                                    containerResponse.getProperties().getResourceId(),
                                    PartitionKeyInternalHelper.FullRange,
                                    false,
                                    null));

            // 1. Extract all preferred read regions to proactively connect to.
            // 2. Obtain partition addresses for a container for one read region, then mark that read region as unavailable.
            // 3. This will force resolveAsync to use the next preferred read region in the next invocation.
            // 4. This way we can verify that connections have been opened to all replicas across all proactive connection regions.
            for (URI proactiveConnectionEndpoint : proactiveConnectionEndpoints) {
                Mono.zip(asyncContainerMono, partitionKeyRangeMono)
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
                        .delayElements(Duration.ofSeconds(3))
                        .doOnNext(addressInformations -> {
                            for (AddressInformation address : addressInformations) {
                                endpoints.add(address.getPhysicalUri().getURI().getAuthority());
                            }
                        })
                        .blockLast();

                globalEndpointManager.markEndpointUnavailableForRead(proactiveConnectionEndpoint);
            }

            assertThat(provider.count()).isEqualTo(endpoints.size());
            assertThat(collectionInfoByNameMap.size()).isEqualTo(cosmosContainerIdentities.size());
            assertThat(routingMap.size()).isEqualTo(cosmosContainerIdentities.size());

            cosmosAsyncContainer.delete().block();
        } finally {
            safeClose(asyncClient);
        }
    }

    @DataProvider(name = "proactiveContainerInitConfigs")
    private Object[][] proactiveContainerInitConfigs() {
        Iterator<DatabaseAccountLocation> locationIterator = this.databaseAccount.getReadableLocations().iterator();
        List<String> preferredLocations = new ArrayList<>();

        while (locationIterator.hasNext()) {
            DatabaseAccountLocation accountLocation = locationIterator.next();
            preferredLocations.add(accountLocation.getName());
            preferredLocations.add("EastUS");
        }

        // configure preferredLocation, no of proactive connection regions, no of containers
        return new Object[][] {
                new Object[]{preferredLocations, 1, 3},
                new Object[]{preferredLocations, 2, 1}

        };
    }

    @DataProvider(name = "invalidProactiveContainerInitConfigs")
    private Object[][] invalidProactiveContainerInitConfigs() {
        Iterator<DatabaseAccountLocation> locationIterator = this.databaseAccount.getReadableLocations().iterator();
        List<String> preferredLocations = new ArrayList<>();

        while (locationIterator.hasNext()) {
            DatabaseAccountLocation accountLocation = locationIterator.next();
            preferredLocations.add(accountLocation.getName());
        }

        // configure preferredLocation, no of proactive connection regions, no of containers
        return new Object[][] {
            new Object[]{preferredLocations, preferredLocations.size() + 1, 1},
            new Object[]{
                Collections.unmodifiableList(
                    Arrays.asList("R1", "R2", "R3", "R4", "R5", "R6")),
                6,
                1
            }
        };
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
