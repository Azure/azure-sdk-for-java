/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
package com.azure.cosmos;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.RxGatewayStoreModel;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.faultinjection.IFaultInjectorProvider;
import com.azure.cosmos.implementation.throughputControl.ThroughputControlStore;
import com.azure.cosmos.models.CosmosContainerIdentity;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
public class FeedRangeTest extends TestSuiteBase {
    private CosmosClientBuilder cosmosClientBuilderUnderTest;
    private CosmosClient houseKeepingClient;
    private String preExistingDatabaseId = CosmosDatabaseForTest.generateId();

    @Factory(dataProvider = "clientBuilders")
    public FeedRangeTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosContainerTest() {
        cosmosClientBuilderUnderTest = getClientBuilder();
        houseKeepingClient = createGatewayHouseKeepingDocumentClient(false).buildClient();
        houseKeepingClient.createDatabase(preExistingDatabaseId);
    }

    @AfterClass(groups = {"emulator"}, timeOut = 3 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        logger.info("starting ....");
        safeDeleteSyncDatabase(houseKeepingClient.getDatabase(preExistingDatabaseId));
        safeCloseSyncClient(houseKeepingClient);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void feedRange_RecreateContainerWithSameName() {
        String containerName = UUID.randomUUID().toString();
        String databaseName = preExistingDatabaseId;
        try(CosmosAsyncClient clientUnderTest = cosmosClientBuilderUnderTest.buildAsyncClient()) {
            for (int i = 0; i < 2; i++) {
                CosmosContainerProperties cosmosContainerProperties = new CosmosContainerProperties(containerName, "/PE_Name");
                houseKeepingClient.getDatabase(databaseName).createContainerIfNotExists(cosmosContainerProperties);

                List<FeedRange> rsp =
                    clientUnderTest.getDatabase(databaseName).getContainer(containerName).getFeedRanges().block();
                assertThat(rsp).isNotNull();
                assertThat(rsp).hasSize(1);

                houseKeepingClient.getDatabase(databaseName).getContainer(containerName).delete();
            }
        }
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void feedRange_withForceRefresh() {
        String containerName = UUID.randomUUID().toString();
        String databaseName = preExistingDatabaseId;
        CosmosContainerProperties cosmosContainerProperties = new CosmosContainerProperties(containerName, "/PE_Name");
        houseKeepingClient.getDatabase(databaseName).createContainerIfNotExists(cosmosContainerProperties);
        try(CosmosAsyncClient clientUnderTest = cosmosClientBuilderUnderTest.buildAsyncClient()) {
            RxDocumentClientImpl rxClient = (RxDocumentClientImpl)clientUnderTest.getContextClient();
            RxGatewayStoreModel rxGatewayStoreModel = (RxGatewayStoreModel)ReflectionUtils.getGatewayProxy(rxClient);
            DelegatingRxStoreModel pkRangeFeedTrackingGatewayStoreModel = new DelegatingRxStoreModel(rxGatewayStoreModel);
            ReflectionUtils.setGatewayProxy(rxClient, pkRangeFeedTrackingGatewayStoreModel);
            for (int i = 0; i < 10; i++) {
                List<FeedRange> rsp =
                    clientUnderTest.getDatabase(databaseName).getContainer(containerName).getFeedRanges().block();
                assertThat(rsp).isNotNull();
                assertThat(rsp).hasSize(1);
            }
            assertThat(pkRangeFeedTrackingGatewayStoreModel.getPartitionKeyRangeFeedCount()).isGreaterThanOrEqualTo(10);
        }

        houseKeepingClient.getDatabase(databaseName).getContainer(containerName).delete();
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void feedRange_noForceRefresh() {
        String containerName = UUID.randomUUID().toString();
        String databaseName = preExistingDatabaseId;
        CosmosContainerProperties cosmosContainerProperties = new CosmosContainerProperties(containerName, "/PE_Name");
        houseKeepingClient.getDatabase(databaseName).createContainerIfNotExists(cosmosContainerProperties);
        try(CosmosAsyncClient clientUnderTest = cosmosClientBuilderUnderTest.buildAsyncClient()) {
            RxDocumentClientImpl rxClient = (RxDocumentClientImpl)clientUnderTest.getContextClient();
            RxGatewayStoreModel rxGatewayStoreModel = (RxGatewayStoreModel)ReflectionUtils.getGatewayProxy(rxClient);
            DelegatingRxStoreModel pkRangeFeedTrackingGatewayStoreModel = new DelegatingRxStoreModel(rxGatewayStoreModel);
            ReflectionUtils.setGatewayProxy(rxClient, pkRangeFeedTrackingGatewayStoreModel);
            clientUnderTest.getDatabase(databaseName).getContainer(containerName).getFeedRanges().block();

            long baselinePkRangeFeedCount = pkRangeFeedTrackingGatewayStoreModel.getPartitionKeyRangeFeedCount();
            logger.info("Baseline PKRangeFeedCount: {}", baselinePkRangeFeedCount);

            for (int i = 0; i < 10; i++) {
                List<FeedRange> rsp =
                    ImplementationBridgeHelpers
                        .CosmosAsyncContainerHelper
                        .getCosmosAsyncContainerAccessor()
                        .getFeedRanges(
                            clientUnderTest.getDatabase(databaseName).getContainer(containerName),
                            false).block();
                assertThat(rsp).isNotNull();
                assertThat(rsp).hasSize(1);
            }
            assertThat(pkRangeFeedTrackingGatewayStoreModel.getPartitionKeyRangeFeedCount())
                .isEqualTo(baselinePkRangeFeedCount);
        }

        houseKeepingClient.getDatabase(databaseName).getContainer(containerName).delete();
    }

    static class DelegatingRxStoreModel extends RxGatewayStoreModel {

        private final AtomicLong partitionKeyRangeFeedCounter= new AtomicLong(0);

        private final RxGatewayStoreModel inner;

        public DelegatingRxStoreModel(RxGatewayStoreModel inner) {
            super(inner);

            this.inner = inner;
        }

        @Override
        public Mono<RxDocumentServiceResponse> processMessage(RxDocumentServiceRequest request) {

            if (request.getResourceType() == ResourceType.PartitionKeyRange
                && request.getOperationType() == OperationType.ReadFeed) {
                partitionKeyRangeFeedCounter.incrementAndGet();
            }

            return inner.processMessage(request);
        }

        @Override
        public void enableThroughputControl(ThroughputControlStore throughputControlStore) {
            inner.enableThroughputControl(throughputControlStore);
        }

        @Override
        public Flux<Void> submitOpenConnectionTasksAndInitCaches(
            CosmosContainerProactiveInitConfig proactiveContainerInitConfig) {

            return inner.submitOpenConnectionTasksAndInitCaches(proactiveContainerInitConfig);
        }

        @Override
        public void configureFaultInjectorProvider(IFaultInjectorProvider injectorProvider, Configs configs) {
            inner.configureFaultInjectorProvider(injectorProvider, configs);
        }

        @Override
        public void recordOpenConnectionsAndInitCachesCompleted(List<CosmosContainerIdentity> cosmosContainerIdentities) {
            inner.recordOpenConnectionsAndInitCachesCompleted(cosmosContainerIdentities);
        }

        @Override
        public void recordOpenConnectionsAndInitCachesStarted(List<CosmosContainerIdentity> cosmosContainerIdentities) {
            inner.recordOpenConnectionsAndInitCachesStarted(cosmosContainerIdentities);
        }

        public long getPartitionKeyRangeFeedCount() {
            return this.partitionKeyRangeFeedCounter.get();
        }
    }
}
