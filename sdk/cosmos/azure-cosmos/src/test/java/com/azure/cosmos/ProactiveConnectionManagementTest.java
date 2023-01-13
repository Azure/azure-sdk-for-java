// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.*;
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

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;

public class ProactiveConnectionManagementTest extends TestSuiteBase {

    private CosmosClientBuilder clientBuilder;
    private DatabaseAccount databaseAccount;

    @BeforeClass(groups = {"multi-master"})
    public void beforeClass() {
        clientBuilder = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .directMode();

        CosmosAsyncClient dummyClient = clientBuilder.buildAsyncClient();

        AsyncDocumentClient asyncDocumentClient = ReflectionUtils.getAsyncDocumentClient(dummyClient);
        RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncDocumentClient;
        GlobalEndpointManager globalEndpointManager =
                ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
        this.databaseAccount = globalEndpointManager.getLatestDatabaseAccount();

        safeClose(dummyClient);
    }

    @Test(groups = {"multi-master"}, dataProvider = "proactiveContainerInitConfigs")
    public void openConnectionsAndInitCachesWithCosmosClient(List<String> preferredRegions, int numProactiveConnectionRegions) {

        CosmosAsyncClient asyncClient = null;
        CosmosAsyncClient clientWithOpenConnections = null;

        try {
            asyncClient = clientBuilder.buildAsyncClient();

            CosmosAsyncContainer cosmosAsyncMultiPartitionContainer = getSharedMultiPartitionCosmosContainer(asyncClient);
            CosmosAsyncContainer cosmosAsyncSinglePartitionContainer = getSharedSinglePartitionCosmosContainer(asyncClient);

            List<CosmosContainerIdentity> cosmosContainerIdentities = new ArrayList<>();
            cosmosContainerIdentities.add(new CosmosContainerIdentity(cosmosAsyncMultiPartitionContainer.getDatabase().getId(),
                    cosmosAsyncMultiPartitionContainer.getId()));
            cosmosContainerIdentities.add(new CosmosContainerIdentity(cosmosAsyncSinglePartitionContainer.getDatabase().getId(),
                    cosmosAsyncSinglePartitionContainer.getId()));

            ProactiveContainerInitConfig proactiveContainerInitConfig = new ProactiveContainerInitConfigBuilder(cosmosContainerIdentities)
                    .setProactiveConnectionRegions(numProactiveConnectionRegions)
                    .build();

            clientWithOpenConnections = clientBuilder
                    .preferredRegions(preferredRegions)
                    .openConnectionsAndInitCaches(proactiveContainerInitConfig)
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
            List<URI> proactiveConnectionEndpoints = globalEndpointManager.getReadEndpoints().subList(0, proactiveContainerInitConfig.getNumProactiveConnectionRegions());


            Flux<CosmosAsyncContainer> asyncContainerFlux = Flux.fromArray(new CosmosAsyncContainer[]{cosmosAsyncSinglePartitionContainer,
                    cosmosAsyncMultiPartitionContainer});

            Flux<Utils.ValueHolder<List<PartitionKeyRange>>> partitionKeyRangeFlux = Flux.fromArray(new CosmosAsyncContainer[]{cosmosAsyncSinglePartitionContainer,
                            cosmosAsyncMultiPartitionContainer})
                    .flatMap(CosmosAsyncContainer::read)
                    .flatMap(containerResponse -> rxDocumentClient
                            .getPartitionKeyRangeCache()
                            .tryGetOverlappingRangesAsync(
                                    null,
                                    containerResponse.getProperties().getResourceId(),
                                    PartitionKeyInternalHelper.FullRange,
                                    true,
                                    null));

            // 1. Extract all preferred read regions to proactively connect to
            // 2. Resolve partition addresses for one read region, then mark read region as unavailable
            // 3. This will force resolveAsync to use the next preferred read region
            // 4. This way we can verify that connections have been opened to all replicas across all proactive
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
            safeClose(asyncClient);
            safeClose(clientWithOpenConnections);
        }
    }

    @DataProvider(name = "proactiveContainerInitConfigs")
    private Object[][] proactiveContainerInitConfigs() {
        Iterator<DatabaseAccountLocation> locationIterator = this.databaseAccount.getReadableLocations().iterator();
        List<String> preferredLocations = new ArrayList<>();

        while (locationIterator.hasNext()) {
            DatabaseAccountLocation accountLocation = locationIterator.next();
            preferredLocations.add(accountLocation.getName());
        }

        return new Object[][] {
                new Object[]{preferredLocations, 1},
                new Object[]{preferredLocations, 2}
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
