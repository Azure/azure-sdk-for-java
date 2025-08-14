// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.AsyncCacheNonBlockingIntegrationTest;
import com.azure.cosmos.BatchTestBase;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainerProactiveInitConfigBuilder;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.caches.AsyncCacheNonBlocking;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.directconnectivity.rntbd.ProactiveOpenConnectionsProcessor;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpoint;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkItemResponse;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosContainerIdentity;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.ThroughputResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


public class ProactiveOpenConnectionsProcessorTest extends BatchTestBase {

    private DatabaseAccount databaseAccount;
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase cosmosAsyncDatabase;
    private Map<String, String> writeRegionMap;

    @DataProvider(name = "sinkEmissionHandlingParams")
    public Object[][] sinkEmissionHandlingParams() {
        return new Object[][] {
                {5, 500, 8, Duration.ofNanos(10), 50},
                {50, 500, 8, Duration.ofNanos(10), 50},
                {500, 500, 8, Duration.ofNanos(10), 50},
                {1000, 500, 8, Duration.ofNanos(10), 50}
        };
    }

    @Factory(dataProvider = "simpleClientBuildersWithJustDirectTcp")
    public ProactiveOpenConnectionsProcessorTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"multi-region"})
    public void beforeClass() {
        try {
            this.client = getClientBuilder().buildAsyncClient();
            this.cosmosAsyncDatabase = getSharedCosmosDatabase(this.client);

            AsyncDocumentClient asyncDocumentClient = BridgeInternal.getContextClient(client);
            GlobalEndpointManager globalEndpointManager = asyncDocumentClient.getGlobalEndpointManager();

            this.databaseAccount = globalEndpointManager.getLatestDatabaseAccount();
            this.writeRegionMap = getRegionMap(this.databaseAccount, true);
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"multi-region"})
    public void recordNewAddressesAfterSplitTest() {
        String containerId = "containerForWarmup" + UUID.randomUUID();
        String databaseId = cosmosAsyncDatabase.getId();

        client = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .directMode()
                .buildAsyncClient();

        CosmosContainerProperties containerProperties = new CosmosContainerProperties(containerId, "/mypk");
        cosmosAsyncDatabase.createContainer(containerProperties).block();

        CosmosAsyncContainer containerUnderOpenConnectionsAndInitCaches = cosmosAsyncDatabase.getContainer(containerId);

        CosmosAsyncClient connectionWarmupClient = null;

        try {

            int totalRequests = 200;

            List<String> preferredRegions = this.writeRegionMap.keySet().stream().collect(Collectors.toList());
            List<CosmosContainerIdentity> cosmosContainerIdentities = new ArrayList<>();

            cosmosContainerIdentities.add(new CosmosContainerIdentity(databaseId, containerId));

            connectionWarmupClient = new CosmosClientBuilder()
                    .endpoint(TestConfigurations.HOST)
                    .key(TestConfigurations.MASTER_KEY)
                    .preferredRegions(preferredRegions)
                    .directMode()
                    .openConnectionsAndInitCaches(new CosmosContainerProactiveInitConfigBuilder(cosmosContainerIdentities)
                            .setProactiveConnectionRegionsCount(1)
                            .build()
                    )
                    .endpointDiscoveryEnabled(true)
                    .buildAsyncClient();

            containerUnderOpenConnectionsAndInitCaches = connectionWarmupClient
                    .getDatabase(databaseId)
                    .getContainer(containerId);

            RntbdTransportClient rntbdTransportClient =
                    (RntbdTransportClient) ReflectionUtils.getTransportClient(connectionWarmupClient);
            RntbdEndpoint.Provider endpointProvider = ReflectionUtils.getRntbdEndpointProvider(rntbdTransportClient);
            ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessor =
                    ReflectionUtils.getProactiveOpenConnectionsProcessor(rntbdTransportClient);

            Set<String> uriStringsUnderOpenConnectionsAndInitCaches =
                    ReflectionUtils.getAddressUrisAsStringUnderOpenConnectionsAndInitCachesFlow(proactiveOpenConnectionsProcessor);

            int uriCountUnderOpenConnectionsAndInitCachesBeforeSplit = uriStringsUnderOpenConnectionsAndInitCaches.size();

            Flux<CosmosItemOperation> cosmosItemOperationFlux1 = Flux.range(0, totalRequests).map(i -> {
                String partitionKey = UUID.randomUUID().toString();
                BatchTestBase.TestDoc testDoc = this.populateTestDoc(partitionKey);

                return CosmosBulkOperations.getCreateItemOperation(testDoc, new PartitionKey(partitionKey));
            });

            Flux<CosmosItemOperation> cosmosItemOperationFlux2 = Flux.range(0, totalRequests).map(i -> {
                String partitionKey = UUID.randomUUID().toString();
                BatchTestBase.EventDoc eventDoc = new BatchTestBase.EventDoc(UUID.randomUUID().toString(), 2, 4, "type1", partitionKey);

                return CosmosBulkOperations.getCreateItemOperation(eventDoc, new PartitionKey(partitionKey));
            });

            CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();

            Flux<CosmosBulkOperationResponse<AsyncCacheNonBlockingIntegrationTest>> responseFlux =
                    containerUnderOpenConnectionsAndInitCaches.executeBulkOperations(cosmosItemOperationFlux1, cosmosBulkExecutionOptions);

            AtomicInteger processedDoc = new AtomicInteger(0);
            responseFlux
                    .flatMap(cosmosBulkOperationResponse -> {

                        processedDoc.incrementAndGet();

                        CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
                        assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
                        assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
                        assertThat(cosmosBulkItemResponse.getSessionToken()).isNotNull();
                        assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();

                        return Mono.just(cosmosBulkItemResponse);
                    }).blockLast();

            assertThat(processedDoc.get()).isEqualTo(totalRequests);
            RxDocumentClientImpl rxDocumentClient =
                    (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(connectionWarmupClient);
            ConcurrentHashMap<String, ?> routingMap = getRoutingMap(rxDocumentClient);
            String cacheKeyBeforePartition = routingMap.keys().nextElement();

            // introduce a split and continue bulk operations after split. The partition key range cache has to be
            // refreshed and bulk processing should complete without errors
            List<PartitionKeyRange> partitionKeyRanges = getPartitionKeyRanges(containerId, connectionWarmupClient);
            // Scale up the throughput for a split
            logger.info("Scaling up throughput for split");
            ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(16000);
            ThroughputResponse throughputResponse = containerUnderOpenConnectionsAndInitCaches
                    .replaceThroughput(throughputProperties).block();
            logger.info("Throughput replace request submitted for {} ",
                    throughputResponse.getProperties().getManualThroughput());
            throughputResponse = containerUnderOpenConnectionsAndInitCaches.readThroughput().block();

            // wait for the throughput update to complete so that we get the partition split
            logger.info("Waiting for split to complete...");

            while (true) {
                assert throughputResponse != null;
                if (!throughputResponse.isReplacePending()) {
                    break;
                }
                throughputResponse = containerUnderOpenConnectionsAndInitCaches.readThroughput().block();
            }

            // Read number of partitions. Should be greater than one
            List<PartitionKeyRange> partitionKeyRangesAfterSplit = getPartitionKeyRanges(containerId,
                    connectionWarmupClient);
            assertThat(partitionKeyRangesAfterSplit.size()).isGreaterThan(partitionKeyRanges.size())
                    .as("Partition ranges should increase after split");
            logger.info("After split num partitions = {}", partitionKeyRangesAfterSplit.size());

            routingMap = getRoutingMap(rxDocumentClient);
            String cacheKeyAfterPartition = routingMap.keys().nextElement();

            assertThat(cacheKeyBeforePartition).isEqualTo(cacheKeyAfterPartition);

            responseFlux = containerUnderOpenConnectionsAndInitCaches.executeBulkOperations(cosmosItemOperationFlux2, cosmosBulkExecutionOptions);

            AtomicInteger processedDoc2 = new AtomicInteger(0);
            responseFlux
                    .flatMap(cosmosBulkOperationResponse -> {

                        processedDoc2.incrementAndGet();

                        CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
                        assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
                        assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
                        assertThat(cosmosBulkItemResponse.getSessionToken()).isNotNull();
                        assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();

                        return Mono.just(cosmosBulkItemResponse);
                    }).blockLast();

            int uriCountUnderOpenConnectionsAndInitCachesAfterSplit = uriStringsUnderOpenConnectionsAndInitCaches.size();

            // block here so connections can be opened to new endpoints
            Thread.sleep(1000);

            assertThat(uriCountUnderOpenConnectionsAndInitCachesAfterSplit)
                    .isGreaterThan(uriCountUnderOpenConnectionsAndInitCachesBeforeSplit);
            assertThat(endpointProvider.count()).isEqualTo(uriCountUnderOpenConnectionsAndInitCachesAfterSplit);
            assertThat(processedDoc.get()).isEqualTo(totalRequests);
            containerUnderOpenConnectionsAndInitCaches.delete().block();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(client);
            safeClose(connectionWarmupClient);
        }
    }

    private ConcurrentHashMap<String, ?> getRoutingMap(RxDocumentClientImpl rxDocumentClient) {
        RxPartitionKeyRangeCache partitionKeyRangeCache =
                ReflectionUtils.getPartitionKeyRangeCache(rxDocumentClient);
        AsyncCacheNonBlocking<String, CollectionRoutingMap> routingMapAsyncCache =
                ReflectionUtils.getRoutingMapAsyncCacheNonBlocking(partitionKeyRangeCache);

        return ReflectionUtils.getValueMapNonBlockingCache(routingMapAsyncCache);
    }

    private List<PartitionKeyRange> getPartitionKeyRanges(
            String containerId, CosmosAsyncClient asyncClient) {
        List<PartitionKeyRange> partitionKeyRanges = new ArrayList<>();
        AsyncDocumentClient asyncDocumentClient = BridgeInternal.getContextClient(asyncClient);
        List<FeedResponse<PartitionKeyRange>> partitionFeedResponseList = asyncDocumentClient
                .readPartitionKeyRanges("/dbs/" + cosmosAsyncDatabase.getId()
                                + "/colls/" + containerId,
                        new CosmosQueryRequestOptions())
                .collectList().block();
        partitionFeedResponseList.forEach(f -> partitionKeyRanges.addAll(f.getResults()));
        return partitionKeyRanges;
    }

    private Map<String, String> getRegionMap(DatabaseAccount databaseAccount, boolean writeOnly) {
        Iterator<DatabaseAccountLocation> locationIterator =
                writeOnly ? databaseAccount.getWritableLocations().iterator() : databaseAccount.getReadableLocations().iterator();
        Map<String, String> regionMap = new ConcurrentHashMap<>();

        while (locationIterator.hasNext()) {
            DatabaseAccountLocation accountLocation = locationIterator.next();
            regionMap.put(accountLocation.getName(), accountLocation.getEndpoint());
        }

        return regionMap;
    }
}
