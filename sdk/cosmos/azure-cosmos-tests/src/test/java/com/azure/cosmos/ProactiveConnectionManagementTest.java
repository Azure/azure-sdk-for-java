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
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import com.azure.cosmos.models.CosmosContainerIdentity;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ProactiveConnectionManagementTest extends TestSuiteBase {

    private CosmosClientBuilder clientBuilder;
    private DatabaseAccount databaseAccount;
    private CosmosAsyncDatabase cosmosAsyncDatabase;

    @BeforeClass(groups = {"multi-master", "flaky-multi-master"})
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

    @Test(groups = {"multi-master"}, dataProvider = "invalidProactiveContainerInitConfigs")
    public void openConnectionsAndInitCachesWithInvalidCosmosClientConfig(List<String> preferredRegions, int numProactiveConnectionRegions, int numContainers, Duration aggressiveWarmupDuration) {

        List<CosmosAsyncContainer> asyncContainers = new ArrayList<>();
        List<CosmosContainerIdentity> cosmosContainerIdentities = new ArrayList<>();

        for (int i = 1; i <= numContainers; i++) {
            String containerId = String.format("id%d", i);
            cosmosAsyncDatabase.createContainerIfNotExists(containerId, "/mypk").block();
            asyncContainers.add(cosmosAsyncDatabase.getContainer(containerId));
            cosmosContainerIdentities.add(new CosmosContainerIdentity(cosmosAsyncDatabase.getId(), containerId));
        }

        if (aggressiveWarmupDuration.compareTo(Duration.ZERO) <= 0) {
            try {
                new CosmosContainerProactiveInitConfigBuilder(cosmosContainerIdentities)
                        .setAggressiveWarmupDuration(aggressiveWarmupDuration)
                        .build();
                fail("Should have thrown exception");
            } catch (IllegalArgumentException illegalArgEx) {}
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

    @Test(groups = {"multi-master"}, dataProvider = "proactiveContainerInitConfigs", retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void openConnectionsAndInitCachesWithContainer(ProactiveConnectionManagementTestConfig proactiveConnectionManagementTestConfig) {
        CosmosAsyncClient asyncClient = null;

        List<String> preferredRegions = proactiveConnectionManagementTestConfig.preferredRegions;
        int proactiveConnectionRegionCount = proactiveConnectionManagementTestConfig.proactiveConnectionRegionsCount;

        CosmosAsyncContainer cosmosAsyncContainer = null;
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

            String containerId = "id1" + UUID.randomUUID();
            cosmosAsyncDatabase.createContainerIfNotExists(containerId, "/mypk").block();

            cosmosAsyncContainer = cosmosAsyncDatabase.getContainer(containerId);

            cosmosContainerIdentities.add(new CosmosContainerIdentity(cosmosAsyncDatabase.getId(), containerId));

            CosmosContainerProactiveInitConfig proactiveContainerInitConfig = new CosmosContainerProactiveInitConfigBuilder(cosmosContainerIdentities)
                .setProactiveConnectionRegionsCount(proactiveConnectionRegionCount)
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

            cosmosAsyncContainer.openConnectionsAndInitCaches(proactiveConnectionRegionCount).block();

            UnmodifiableList<RegionalRoutingContext> readEndpoints =
                globalEndpointManager.getReadEndpoints();

            List<URI> proactiveConnectionEndpoints = readEndpoints.subList(
                0,
                Math.min(readEndpoints.size(),proactiveContainerInitConfig.getProactiveConnectionRegionsCount()))
                .stream().map(RegionalRoutingContext::getGatewayRegionalEndpoint).collect(Collectors.toList());

            Mono<CosmosAsyncContainer> asyncContainerMono = Mono.just(cosmosAsyncContainer);

            Mono<Utils.ValueHolder<List<PartitionKeyRange>>> partitionKeyRangeMono = this.buildPartitionKeyRangeRequestFromAsyncContainerAsMono(cosmosAsyncContainer, rxDocumentClient);

            // 1. Extract all preferred read regions to proactively connect to.
            // 2. Obtain partition addresses for a container for one read region, then mark that read region as unavailable.
            // 3. This will force resolveAsync to use the next preferred read region in the next invocation.
            // 4. This way we can verify that connections have been opened to all replicas across all proactive connection regions.
            for (URI proactiveConnectionEndpoint : proactiveConnectionEndpoints) {
                Mono.zip(asyncContainerMono, partitionKeyRangeMono)
                        .flatMapIterable(containerToPartitionKeyRanges -> {
                            assertThat(containerToPartitionKeyRanges).isNotNull();
                            assertThat(containerToPartitionKeyRanges.getT2()).isNotNull();
                            assertThat(containerToPartitionKeyRanges.getT2().v).isNotNull();
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
        } finally {
            if (cosmosAsyncContainer != null) {
                safeDeleteCollection(cosmosAsyncContainer);
            }
            safeClose(asyncClient);
        }
    }

    @Test(groups = {"multi-master"}, dataProvider = "proactiveContainerInitConfigs")
    public void openConnectionsAndInitCachesWithCosmosClient_And_PerContainerConnectionPoolSize_ThroughSystemConfig(
        ProactiveConnectionManagementTestConfig proactiveConnectionManagementTestConfig) throws InterruptedException {

        CosmosAsyncClient asyncClientWithOpenConnections = null;
        CosmosClient syncClientWithOpenConnections = null;

        List<CosmosAsyncContainer> asyncContainers = new ArrayList<>();

        // test config parameters
        List<String> preferredRegions = proactiveConnectionManagementTestConfig.preferredRegions;

        int containerCount = proactiveConnectionManagementTestConfig.containerCount;
        int minConnectionPoolSizePerEndpoint = proactiveConnectionManagementTestConfig.minConnectionPoolSizePerEndpoint;
        int proactiveConnectionRegionsCount = proactiveConnectionManagementTestConfig.proactiveConnectionRegionsCount;

        boolean isSystemPropertySetBeforeDirectConnectionConfig = proactiveConnectionManagementTestConfig.isSystemPropertySetBeforeDirectConnectionConfig;

        try {
            List<CosmosContainerIdentity> cosmosContainerIdentities = new ArrayList<>();

            for (int i = 1; i <= containerCount; i++) {
                String containerId = String.format("id%d", i);
                cosmosAsyncDatabase.createContainerIfNotExists(containerId, "/mypk").block();
                asyncContainers.add(cosmosAsyncDatabase.getContainer(containerId));
                cosmosContainerIdentities.add(new CosmosContainerIdentity(cosmosAsyncDatabase.getId(), containerId));
            }

            CosmosContainerProactiveInitConfig proactiveContainerInitConfig = new
                    CosmosContainerProactiveInitConfigBuilder(cosmosContainerIdentities)
                    .setProactiveConnectionRegionsCount(proactiveConnectionRegionsCount)
                    .build();

            // allow enough time for the container to be available for reads
            Thread.sleep(5_000);

            if (isSystemPropertySetBeforeDirectConnectionConfig) {
                System.setProperty("COSMOS.MIN_CONNECTION_POOL_SIZE_PER_ENDPOINT", String.valueOf(minConnectionPoolSizePerEndpoint));

                if (proactiveConnectionManagementTestConfig.isSyncClient) {
                    syncClientWithOpenConnections = new CosmosClientBuilder()
                        .endpoint(TestConfigurations.HOST)
                        .key(TestConfigurations.MASTER_KEY)
                        .endpointDiscoveryEnabled(true)
                        .preferredRegions(preferredRegions)
                        .openConnectionsAndInitCaches(proactiveContainerInitConfig)
                        .directMode(DirectConnectionConfig.getDefaultConfig())
                        .buildClient();
                } else {
                    asyncClientWithOpenConnections = new CosmosClientBuilder()
                        .endpoint(TestConfigurations.HOST)
                        .key(TestConfigurations.MASTER_KEY)
                        .endpointDiscoveryEnabled(true)
                        .preferredRegions(preferredRegions)
                        .openConnectionsAndInitCaches(proactiveContainerInitConfig)
                        .directMode(DirectConnectionConfig.getDefaultConfig())
                        .buildAsyncClient();
                }
            } else {
                DirectConnectionConfig directConnectionConfig = DirectConnectionConfig.getDefaultConfig();

                System.setProperty("COSMOS.MIN_CONNECTION_POOL_SIZE_PER_ENDPOINT", String.valueOf(minConnectionPoolSizePerEndpoint));

                if (proactiveConnectionManagementTestConfig.isSyncClient) {
                    syncClientWithOpenConnections = new CosmosClientBuilder()
                        .endpoint(TestConfigurations.HOST)
                        .key(TestConfigurations.MASTER_KEY)
                        .endpointDiscoveryEnabled(true)
                        .preferredRegions(preferredRegions)
                        .openConnectionsAndInitCaches(proactiveContainerInitConfig)
                        .directMode(directConnectionConfig)
                        .buildClient();
                } else {
                    asyncClientWithOpenConnections = new CosmosClientBuilder()
                        .endpoint(TestConfigurations.HOST)
                        .key(TestConfigurations.MASTER_KEY)
                        .endpointDiscoveryEnabled(true)
                        .preferredRegions(preferredRegions)
                        .openConnectionsAndInitCaches(proactiveContainerInitConfig)
                        .directMode(directConnectionConfig)
                        .buildAsyncClient();
                }
            }

            RntbdTransportClient rntbdTransportClient;
            AsyncDocumentClient asyncDocumentClient;

            if (proactiveConnectionManagementTestConfig.isSyncClient) {
                rntbdTransportClient =
                    (RntbdTransportClient) ReflectionUtils.getTransportClient(syncClientWithOpenConnections);
                asyncDocumentClient = ReflectionUtils.getAsyncDocumentClient(syncClientWithOpenConnections.asyncClient());
            } else {
                rntbdTransportClient =
                    (RntbdTransportClient) ReflectionUtils.getTransportClient(asyncClientWithOpenConnections);
                asyncDocumentClient = ReflectionUtils.getAsyncDocumentClient(asyncClientWithOpenConnections);
            }

            RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncDocumentClient;
            GlobalAddressResolver globalAddressResolver = ReflectionUtils.getGlobalAddressResolver(rxDocumentClient);
            GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
            RntbdEndpoint.Provider provider = ReflectionUtils.getRntbdEndpointProvider(rntbdTransportClient);

            ConcurrentHashMap<String, ?> routingMap = getRoutingMap(rxDocumentClient);
            ConcurrentHashMap<String, ?> collectionInfoByNameMap = getCollectionInfoByNameMap(rxDocumentClient);
            Set<String> endpoints = ConcurrentHashMap.newKeySet();
            UnmodifiableList<RegionalRoutingContext> readEndpoints = globalEndpointManager.getReadEndpoints();

            List<URI> proactiveConnectionEndpoints = readEndpoints.subList(
                    0,
                    Math.min(readEndpoints.size(), proactiveContainerInitConfig.getProactiveConnectionRegionsCount()))
                .stream()
                .map(RegionalRoutingContext::getGatewayRegionalEndpoint)
                .collect(Collectors.toList());

            Flux<CosmosAsyncContainer> asyncContainerFlux = Flux.fromIterable(asyncContainers);
            Flux<Utils.ValueHolder<List<PartitionKeyRange>>> partitionKeyRangeFlux =
                    buildPartitionKeyRangeRequestFromAsyncContainersAsFlux(asyncContainers, rxDocumentClient);

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
                        .delayElements(Duration.ofMillis(300))
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

            int totalConnectionCountForAllEndpoints = 0;

            for (RntbdEndpoint endpoint : provider.list().collect(Collectors.toList())) {
                totalConnectionCountForAllEndpoints += endpoint.channelsMetrics();
            }

            assertThat(totalConnectionCountForAllEndpoints).isEqualTo(endpoints.size() * minConnectionPoolSizePerEndpoint);

        } finally {

            for (CosmosAsyncContainer asyncContainer : asyncContainers) {
                asyncContainer.delete().block();
            }
            System.clearProperty("COSMOS.MIN_CONNECTION_POOL_SIZE_PER_ENDPOINT");
            safeClose(asyncClientWithOpenConnections);
            safeCloseSyncClient(syncClientWithOpenConnections);
        }
    }

    @Test(groups = {"multi-master"}, dataProvider = "proactiveContainerInitConfigs")
    public void openConnectionsAndInitCachesWithCosmosClient_And_PerContainerConnectionPoolSize_ThroughProactiveContainerInitConfig(
        ProactiveConnectionManagementTestConfig proactiveConnectionManagementTestConfig) throws InterruptedException {

        CosmosAsyncClient asyncClientWithOpenConnections = null;
        CosmosClient syncClientWithOpenConnections = null;

        List<CosmosAsyncContainer> asyncContainers = new ArrayList<>();

        // test config parameters
        List<String> preferredRegions = proactiveConnectionManagementTestConfig.preferredRegions;

        int containerCount = proactiveConnectionManagementTestConfig.containerCount;
        int minConnectionPoolSizePerEndpoint = proactiveConnectionManagementTestConfig.minConnectionPoolSizePerEndpoint;
        int proactiveConnectionRegionsCount = proactiveConnectionManagementTestConfig.proactiveConnectionRegionsCount;

        try {

            List<CosmosContainerIdentity> cosmosContainerIdentities = new ArrayList<>();

            for (int i = 1; i <= containerCount; i++) {
                String containerId = String.format("id%d", i);
                cosmosAsyncDatabase.createContainerIfNotExists(containerId, "/mypk").block();
                asyncContainers.add(cosmosAsyncDatabase.getContainer(containerId));
                cosmosContainerIdentities.add(new CosmosContainerIdentity(cosmosAsyncDatabase.getId(), containerId));
            }

            CosmosContainerProactiveInitConfigBuilder proactiveContainerInitConfigBuilder = new
                    CosmosContainerProactiveInitConfigBuilder(cosmosContainerIdentities)
                    .setProactiveConnectionRegionsCount(proactiveConnectionRegionsCount);

            for (int i = 0; i < cosmosContainerIdentities.size(); i++) {
                proactiveContainerInitConfigBuilder = proactiveContainerInitConfigBuilder
                        .setMinConnectionPoolSizePerEndpointForContainer(cosmosContainerIdentities.get(i), minConnectionPoolSizePerEndpoint);
            }

            CosmosContainerProactiveInitConfig proactiveContainerInitConfig = proactiveContainerInitConfigBuilder
                    .build();

            RntbdTransportClient rntbdTransportClient;
            AsyncDocumentClient asyncDocumentClient;

            // allow enough time for the container to be available for reads
            Thread.sleep(5_000);

            if (proactiveConnectionManagementTestConfig.isSyncClient) {
                syncClientWithOpenConnections = new CosmosClientBuilder()
                    .endpoint(TestConfigurations.HOST)
                    .key(TestConfigurations.MASTER_KEY)
                    .endpointDiscoveryEnabled(true)
                    .preferredRegions(preferredRegions)
                    .openConnectionsAndInitCaches(proactiveContainerInitConfig)
                    .directMode()
                    .buildClient();

                rntbdTransportClient =
                    (RntbdTransportClient) ReflectionUtils.getTransportClient(syncClientWithOpenConnections);
                asyncDocumentClient = ReflectionUtils.getAsyncDocumentClient(syncClientWithOpenConnections.asyncClient());
            } else {
                asyncClientWithOpenConnections = new CosmosClientBuilder()
                    .endpoint(TestConfigurations.HOST)
                    .key(TestConfigurations.MASTER_KEY)
                    .endpointDiscoveryEnabled(true)
                    .preferredRegions(preferredRegions)
                    .openConnectionsAndInitCaches(proactiveContainerInitConfig)
                    .directMode()
                    .buildAsyncClient();

                rntbdTransportClient =
                    (RntbdTransportClient) ReflectionUtils.getTransportClient(asyncClientWithOpenConnections);
                asyncDocumentClient = ReflectionUtils.getAsyncDocumentClient(asyncClientWithOpenConnections);
            }

            RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncDocumentClient;
            GlobalAddressResolver globalAddressResolver = ReflectionUtils.getGlobalAddressResolver(rxDocumentClient);
            GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
            RntbdEndpoint.Provider provider = ReflectionUtils.getRntbdEndpointProvider(rntbdTransportClient);

            ConcurrentHashMap<String, ?> routingMap = getRoutingMap(rxDocumentClient);
            ConcurrentHashMap<String, ?> collectionInfoByNameMap = getCollectionInfoByNameMap(rxDocumentClient);
            Set<String> endpoints = ConcurrentHashMap.newKeySet();
            UnmodifiableList<RegionalRoutingContext> readEndpoints = globalEndpointManager.getReadEndpoints();
            List<URI> proactiveConnectionEndpoints = readEndpoints.subList(
                0,
                Math.min(readEndpoints.size(), proactiveContainerInitConfig.getProactiveConnectionRegionsCount()))
                .stream()
                .map(RegionalRoutingContext::getGatewayRegionalEndpoint)
                .collect(Collectors.toList());;

            Flux<CosmosAsyncContainer> asyncContainerFlux = Flux.fromIterable(asyncContainers);
            Flux<Utils.ValueHolder<List<PartitionKeyRange>>> partitionKeyRangeFlux =
                    buildPartitionKeyRangeRequestFromAsyncContainersAsFlux(asyncContainers, rxDocumentClient);

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
                        .delayElements(Duration.ofMillis(300))
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

            int totalConnectionCountForAllEndpoints = 0;

            for (RntbdEndpoint endpoint : provider.list().collect(Collectors.toList())) {
                totalConnectionCountForAllEndpoints += endpoint.channelsMetrics();
            }

            assertThat(totalConnectionCountForAllEndpoints).isEqualTo(endpoints.size() * minConnectionPoolSizePerEndpoint);

            provider.list().forEach(rntbdEndpoint -> assertThat(rntbdEndpoint.channelsMetrics()).isEqualTo(minConnectionPoolSizePerEndpoint));

        } finally {

            for (CosmosAsyncContainer asyncContainer : asyncContainers) {
                asyncContainer.delete().block();
            }

            safeClose(asyncClientWithOpenConnections);
            safeCloseSyncClient(syncClientWithOpenConnections);
        }
    }

    @Test(groups = {"flaky-multi-master"}, dataProvider = "proactiveContainerInitConfigs", retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void openConnectionsAndInitCachesWithCosmosClient_And_PerContainerConnectionPoolSize_ThroughProactiveContainerInitConfig_WithTimeout(
        ProactiveConnectionManagementTestConfig proactiveConnectionManagementTestConfig) {

        CosmosAsyncClient asyncClientWithOpenConnections = null;
        CosmosClient syncClientWithOpenConnections = null;

        List<CosmosAsyncContainer> asyncContainers = new ArrayList<>();

        // test config parameters
        List<String> preferredRegions = proactiveConnectionManagementTestConfig.preferredRegions;

        int containerCount = proactiveConnectionManagementTestConfig.containerCount;
        int minConnectionPoolSizePerEndpoint = proactiveConnectionManagementTestConfig.minConnectionPoolSizePerEndpoint;
        int proactiveConnectionRegionsCount = proactiveConnectionManagementTestConfig.proactiveConnectionRegionsCount;

        Duration aggressiveWarmupDuration = proactiveConnectionManagementTestConfig.aggressiveWarmupDuration;

        try {

            List<CosmosContainerIdentity> cosmosContainerIdentities = new ArrayList<>();

            for (int i = 0; i < containerCount; i++) {
                String containerId = String.format("id%d", i);
                cosmosAsyncDatabase.createContainerIfNotExists(containerId, "/mypk").block();
                asyncContainers.add(cosmosAsyncDatabase.getContainer(containerId));
                cosmosContainerIdentities.add(new CosmosContainerIdentity(cosmosAsyncDatabase.getId(), containerId));
            }

            CosmosContainerProactiveInitConfigBuilder proactiveContainerInitConfigBuilder = new
                CosmosContainerProactiveInitConfigBuilder(cosmosContainerIdentities)
                .setProactiveConnectionRegionsCount(proactiveConnectionRegionsCount);

            for (int i = 0; i < containerCount; i++) {
                proactiveContainerInitConfigBuilder = proactiveContainerInitConfigBuilder
                        .setMinConnectionPoolSizePerEndpointForContainer(cosmosContainerIdentities.get(i), minConnectionPoolSizePerEndpoint);
            }

            CosmosContainerProactiveInitConfig proactiveContainerInitConfig = proactiveContainerInitConfigBuilder
                    .setAggressiveWarmupDuration(aggressiveWarmupDuration)
                    .build();

            RntbdTransportClient rntbdTransportClient;
            AsyncDocumentClient asyncDocumentClient;

            // allow enough time for the container to be available for reads
            Thread.sleep(5_000);

            Instant clientBuildStartTime = Instant.now();

            if (proactiveConnectionManagementTestConfig.isSyncClient) {
                syncClientWithOpenConnections = new CosmosClientBuilder()
                    .endpoint(TestConfigurations.HOST)
                    .key(TestConfigurations.MASTER_KEY)
                    .endpointDiscoveryEnabled(true)
                    .preferredRegions(preferredRegions)
                    .openConnectionsAndInitCaches(proactiveContainerInitConfig)
                    .directMode()
                    .buildClient();

                rntbdTransportClient =
                    (RntbdTransportClient) ReflectionUtils.getTransportClient(syncClientWithOpenConnections);
                asyncDocumentClient = ReflectionUtils.getAsyncDocumentClient(syncClientWithOpenConnections.asyncClient());
            } else {
                asyncClientWithOpenConnections = new CosmosClientBuilder()
                    .endpoint(TestConfigurations.HOST)
                    .key(TestConfigurations.MASTER_KEY)
                    .endpointDiscoveryEnabled(true)
                    .preferredRegions(preferredRegions)
                    .openConnectionsAndInitCaches(proactiveContainerInitConfig)
                    .directMode()
                    .buildAsyncClient();

                rntbdTransportClient =
                    (RntbdTransportClient) ReflectionUtils.getTransportClient(asyncClientWithOpenConnections);
                asyncDocumentClient = ReflectionUtils.getAsyncDocumentClient(asyncClientWithOpenConnections);
            }

            Instant clientBuildEndTime = Instant.now();

            Duration approxClientBuildTime = Duration.between(clientBuildStartTime, clientBuildEndTime);

            // delta b/w connection warm up and everything else part of building
            // a client should ideally not be greater than 5s
            Duration delta = Duration.ofSeconds(5);

            boolean clientBuildTimeIsApproximatelyAggressiveWarmUpDuration = approxClientBuildTime.minus(delta).compareTo(aggressiveWarmupDuration) < 0;

            // case where the aggressive warmup duration is set too a high value than is required to warmup containers
            boolean clientBuildTimeIsLesserThanAggressiveWarmUpDuration = approxClientBuildTime.compareTo(aggressiveWarmupDuration) < 0;

            assertThat(clientBuildTimeIsApproximatelyAggressiveWarmUpDuration || clientBuildTimeIsLesserThanAggressiveWarmUpDuration).isTrue();

            RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncDocumentClient;
            GlobalAddressResolver globalAddressResolver = ReflectionUtils.getGlobalAddressResolver(rxDocumentClient);
            GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
            RntbdEndpoint.Provider provider = ReflectionUtils.getRntbdEndpointProvider(rntbdTransportClient);

            ConcurrentHashMap<String, ?> routingMap = getRoutingMap(rxDocumentClient);
            ConcurrentHashMap<String, ?> collectionInfoByNameMap = getCollectionInfoByNameMap(rxDocumentClient);
            Set<String> endpoints = ConcurrentHashMap.newKeySet();
            UnmodifiableList<RegionalRoutingContext> readEndpoints = globalEndpointManager.getReadEndpoints();
            List<URI> proactiveConnectionEndpoints = readEndpoints.subList(
                    0,
                    Math.min(readEndpoints.size(), proactiveContainerInitConfig.getProactiveConnectionRegionsCount()))
                .stream()
                .map(RegionalRoutingContext::getGatewayRegionalEndpoint)
                .collect(Collectors.toList());;

            Flux<CosmosAsyncContainer> asyncContainerFlux = Flux.fromIterable(asyncContainers);
            Flux<Utils.ValueHolder<List<PartitionKeyRange>>> partitionKeyRangeFlux =
                    buildPartitionKeyRangeRequestFromAsyncContainersAsFlux(asyncContainers, rxDocumentClient);

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
                        .delayElements(Duration.ofMillis(300))
                        .doOnNext(addressInformations -> {
                            for (AddressInformation address : addressInformations) {
                                endpoints.add(address.getPhysicalUri().getURI().getAuthority());
                            }
                        })
                        .blockLast();

                globalEndpointManager.markEndpointUnavailableForRead(proactiveConnectionEndpoint);
            }

            // let connection counts catch up
            Thread.sleep(50_000);

            assertThat(provider.count()).isEqualTo(endpoints.size());
            assertThat(collectionInfoByNameMap.size()).isEqualTo(cosmosContainerIdentities.size());
            assertThat(routingMap.size()).isEqualTo(cosmosContainerIdentities.size());

            int totalConnectionCountForAllEndpoints = 0;

            for (RntbdEndpoint endpoint : provider.list().collect(Collectors.toList())) {
                totalConnectionCountForAllEndpoints += endpoint.channelsMetrics();
            }

            assertThat(totalConnectionCountForAllEndpoints).isEqualTo(endpoints.size() * minConnectionPoolSizePerEndpoint);

            provider.list().forEach(rntbdEndpoint -> assertThat(rntbdEndpoint.channelsMetrics()).isEqualTo(minConnectionPoolSizePerEndpoint));

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {

            for (CosmosAsyncContainer asyncContainer : asyncContainers) {
                asyncContainer.delete().block();
            }

            safeClose(asyncClientWithOpenConnections);
            safeCloseSyncClient(syncClientWithOpenConnections);
        }
    }

    @DataProvider(name = "proactiveContainerInitConfigs")
    private Object[] proactiveContainerInitConfigs() {
        Iterator<DatabaseAccountLocation> locationIterator = this.databaseAccount.getReadableLocations().iterator();
        List<String> preferredRegions = new ArrayList<>();

        while (locationIterator.hasNext()) {
            DatabaseAccountLocation accountLocation = locationIterator.next();
            preferredRegions.add(accountLocation.getName());
        }

        return new Object[] {
            new ProactiveConnectionManagementTestConfig()
                .withPreferredRegions(preferredRegions)
                .withProactiveConnectionRegionsCount(2)
                .withContainerCount(3)
                .withMinConnectionPoolSizePerEndpoint(4)
                .withAggressiveWarmupDuration(Duration.ofMillis(250))
                .withIsSystemPropertySetBeforeDirectConnectionConfig(true)
                .withIsSyncClient(false),
            new ProactiveConnectionManagementTestConfig()
                .withPreferredRegions(preferredRegions)
                .withProactiveConnectionRegionsCount(2)
                .withContainerCount(3)
                .withMinConnectionPoolSizePerEndpoint(5)
                .withAggressiveWarmupDuration(Duration.ofMillis(1000))
                .withIsSystemPropertySetBeforeDirectConnectionConfig(false)
                .withIsSyncClient(false),
            // in this test config the aggressive warmup duration is set to an unreasonably high value
            // the client build should not block until this aggressive warm up duration time
            new ProactiveConnectionManagementTestConfig()
                .withPreferredRegions(preferredRegions)
                .withProactiveConnectionRegionsCount(2)
                .withContainerCount(3)
                .withMinConnectionPoolSizePerEndpoint(5)
                .withAggressiveWarmupDuration(Duration.ofMinutes(1000))
                .withIsSystemPropertySetBeforeDirectConnectionConfig(false)
                .withIsSyncClient(false),
            new ProactiveConnectionManagementTestConfig()
                .withPreferredRegions(preferredRegions)
                .withProactiveConnectionRegionsCount(2)
                .withContainerCount(3)
                .withMinConnectionPoolSizePerEndpoint(4)
                .withAggressiveWarmupDuration(Duration.ofMillis(250))
                .withIsSystemPropertySetBeforeDirectConnectionConfig(true)
                .withIsSyncClient(true),
            new ProactiveConnectionManagementTestConfig()
                .withPreferredRegions(preferredRegions)
                .withProactiveConnectionRegionsCount(2)
                .withContainerCount(3)
                .withMinConnectionPoolSizePerEndpoint(5)
                .withAggressiveWarmupDuration(Duration.ofMillis(1000))
                .withIsSystemPropertySetBeforeDirectConnectionConfig(false)
                .withIsSyncClient(true),
            new ProactiveConnectionManagementTestConfig()
                .withPreferredRegions(preferredRegions)
                .withProactiveConnectionRegionsCount(2)
                .withContainerCount(3)
                .withMinConnectionPoolSizePerEndpoint(5)
                .withAggressiveWarmupDuration(Duration.ofMinutes(1000))
                .withIsSystemPropertySetBeforeDirectConnectionConfig(false)
                .withIsSyncClient(true)
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
            new Object[]{preferredLocations, preferredLocations.size() + 1, 1, Duration.ofSeconds(2)},
            new Object[]{
                Collections.unmodifiableList(
                    Arrays.asList("R1", "R2", "R3", "R4", "R5", "R6")),
                6,
                1,
                    Duration.ofSeconds(2)
            },
                new Object[]{preferredLocations, preferredLocations.size() + 1, 1, Duration.ofSeconds(0)},
                new Object[]{preferredLocations, preferredLocations.size() + 1, 1, Duration.ofSeconds(-1)},

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

    private Flux<Utils.ValueHolder<List<PartitionKeyRange>>> buildPartitionKeyRangeRequestFromAsyncContainersAsFlux(
            List<CosmosAsyncContainer> cosmosAsyncContainers, RxDocumentClientImpl rxDocumentClient) {
        return Flux.fromIterable(cosmosAsyncContainers)
                .flatMap(CosmosAsyncContainer::read)
                .flatMap(containerResponse -> rxDocumentClient
                        .getPartitionKeyRangeCache()
                        .tryGetOverlappingRangesAsync(
                                null,
                                containerResponse.getProperties().getResourceId(),
                                PartitionKeyInternalHelper.FullRange,
                                false,
                                null,
                            new StringBuilder()));
    }

    private Mono<Utils.ValueHolder<List<PartitionKeyRange>>> buildPartitionKeyRangeRequestFromAsyncContainerAsMono(
            CosmosAsyncContainer cosmosAsyncContainer, RxDocumentClientImpl rxDocumentClient) {
        return Mono.just(cosmosAsyncContainer)
                .flatMap(CosmosAsyncContainer::read)
                .flatMap(containerResponse -> rxDocumentClient
                        .getPartitionKeyRangeCache()
                        .tryGetOverlappingRangesAsync(
                                null,
                                containerResponse.getProperties().getResourceId(),
                                PartitionKeyInternalHelper.FullRange,
                                false,
                                null,
                            new StringBuilder()));
    }

    private class ProactiveConnectionManagementTestConfig {
        private List<String> preferredRegions;
        private int proactiveConnectionRegionsCount;
        private int minConnectionPoolSizePerEndpoint;
        private int containerCount;
        private Duration aggressiveWarmupDuration;
        private boolean isSystemPropertySetBeforeDirectConnectionConfig;
        private boolean isSyncClient;

        public ProactiveConnectionManagementTestConfig() {
            this.preferredRegions = new ArrayList<>();
            this.proactiveConnectionRegionsCount = 0;
            this.minConnectionPoolSizePerEndpoint = 0;
            this.containerCount = 0;
            this.aggressiveWarmupDuration = Duration.ofHours(24);
            this.isSystemPropertySetBeforeDirectConnectionConfig = false;
            this.isSyncClient = false;
        }

        public ProactiveConnectionManagementTestConfig withPreferredRegions(List<String> preferredRegions) {
            this.preferredRegions = preferredRegions;
            return this;
        }

        public ProactiveConnectionManagementTestConfig withProactiveConnectionRegionsCount(int proactiveConnectionRegionsCount) {
            this.proactiveConnectionRegionsCount = proactiveConnectionRegionsCount;
            return this;
        }

        public ProactiveConnectionManagementTestConfig withMinConnectionPoolSizePerEndpoint(int minConnectionPoolSizePerEndpoint) {
            this.minConnectionPoolSizePerEndpoint = minConnectionPoolSizePerEndpoint;
            return this;
        }

        public ProactiveConnectionManagementTestConfig withContainerCount(int containerCount) {
            this.containerCount = containerCount;
            return this;
        }

        public ProactiveConnectionManagementTestConfig withAggressiveWarmupDuration(Duration aggressiveWarmupDuration) {
            this.aggressiveWarmupDuration = aggressiveWarmupDuration;
            return this;
        }

        public ProactiveConnectionManagementTestConfig withIsSystemPropertySetBeforeDirectConnectionConfig(
            boolean isSystemPropertySetBeforeDirectConnectionConfig) {
            this.isSystemPropertySetBeforeDirectConnectionConfig = isSystemPropertySetBeforeDirectConnectionConfig;
            return this;
        }

        public ProactiveConnectionManagementTestConfig withIsSyncClient(boolean isSyncClient) {
            this.isSyncClient = isSyncClient;
            return this;
        }
    }
}
