// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.directconnectivity.ConsistencyReader;
import com.azure.cosmos.implementation.directconnectivity.ConsistencyWriter;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.ReplicatedResourceClient;
import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import com.azure.cosmos.implementation.directconnectivity.StoreClient;
import com.azure.cosmos.implementation.directconnectivity.StoreReader;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ExitFromConsistencyLayerTests extends TestSuiteBase {

    private CosmosAsyncClient client;
    private volatile CosmosAsyncDatabase database;
    private volatile CosmosAsyncContainer container;
    private List<String> preferredRegions;
    private Map<String, String> regionNameToEndpoint;

    @BeforeClass(groups = {"multi-region"})
    public void beforeClass() {

        try (CosmosAsyncClient dummy = getClientBuilder().buildAsyncClient()) {
            AsyncDocumentClient asyncDocumentClient = BridgeInternal.getContextClient(dummy);
            GlobalEndpointManager globalEndpointManager = asyncDocumentClient.getGlobalEndpointManager();

            DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();

            AccountLevelLocationContext accountLevelContext = getAccountLevelLocationContext(databaseAccount, false);

            // Set preferred regions starting with secondary region
            this.preferredRegions = new ArrayList<>(accountLevelContext.serviceOrderedReadableRegions);
            if (this.preferredRegions.size() > 1) {
                // Swap first and second to make secondary region preferred
                String temp = this.preferredRegions.get(0);
                this.preferredRegions.set(0, this.preferredRegions.get(1));
                this.preferredRegions.set(1, temp);
            }

            this.regionNameToEndpoint = accountLevelContext.regionNameToEndpoint;
            this.database = getSharedCosmosDatabase(dummy);
            this.container = getSharedSinglePartitionCosmosContainer(dummy);
        }
    }

    @Factory(dataProvider = "clientBuildersWithDirectTcp")
    public ExitFromConsistencyLayerTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @DataProvider(name = "headFailureScenarios")
    public static Object[][] headFailureScenarios() {
        return new Object[][]{
            // headFailureCount, operationType
            { 1, OperationType.Create },
            { 2, OperationType.Create },
            { 3, OperationType.Create },
            { 4, OperationType.Create },
            { 1, OperationType.Read },
            { 2, OperationType.Read },
            { 4, OperationType.Read },
        };
    }

    @Test(groups = {"multi-region"}, dataProvider = "headFailureScenarios" /*, timeOut = 2 * TIMEOUT*/)
    public void validateGCLSNBarrierWithHeadFailures(int headFailureCount, OperationType operationTypeForWhichBarrierFlowIsTriggered) throws Exception {

        CosmosAsyncClient targetClient = getClientBuilder()
            .preferredRegions(this.preferredRegions)
            .directMode()
            .consistencyLevel(ConsistencyLevel.STRONG)
            .buildAsyncClient();

        AtomicInteger failureCountTracker = new AtomicInteger();
        Utils.ValueHolder<RntbdTransportClient> originalRntbdTransportClientHolder = new Utils.ValueHolder<>();

        RntbdTransportClientWithStoreResponseInterceptor interceptorClient = createClientWithInterceptor(targetClient, originalRntbdTransportClientHolder);

        try {

            // Setup test data
            TestObject testObject = TestObject.create();

            if (operationTypeForWhichBarrierFlowIsTriggered == OperationType.Create) {
                interceptorClient
                    .setResponseInterceptor(
                        StoreResponseInterceptorUtils.forceBarrierFollowedByBarrierFailure(
                            operationTypeForWhichBarrierFlowIsTriggered,
                            this.regionNameToEndpoint.get(this.preferredRegions.get(1)),
                            headFailureCount,
                            failureCountTracker,
                            HttpConstants.StatusCodes.GONE,
                            HttpConstants.SubStatusCodes.LEASE_NOT_FOUND
                        ));
            } else if (operationTypeForWhichBarrierFlowIsTriggered == OperationType.Read) {
                interceptorClient
                    .setResponseInterceptor(
                        StoreResponseInterceptorUtils.forceBarrierFollowedByBarrierFailure(
                            operationTypeForWhichBarrierFlowIsTriggered,
                            this.regionNameToEndpoint.get(this.preferredRegions.get(0)),
                            headFailureCount,
                            failureCountTracker,
                            HttpConstants.StatusCodes.GONE,
                            HttpConstants.SubStatusCodes.LEASE_NOT_FOUND
                        ));
            }

            try {
                CosmosAsyncDatabase targetAsyncDatabase = targetClient.getDatabase(this.database.getId());
                CosmosAsyncContainer targetContainer = targetAsyncDatabase.getContainer(this.container.getId());

                Thread.sleep(5000); // Wait for collection to be available to be read

                // Assert based on operation type and failure count
                if (operationTypeForWhichBarrierFlowIsTriggered == OperationType.Create) {

                    // Perform the operation
                    CosmosItemResponse<?> response = targetContainer.createItem(testObject).block();

                    // For Create, Head can only fail up to 2 times
                    if (headFailureCount <= 1) {
                        // Should eventually succeed
                        assertThat(response).isNotNull();
                        assertThat(response.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CREATED);

                        // Check diagnostics - should have contacted only one region for create
                        CosmosDiagnostics diagnostics = response.getDiagnostics();
                        assertThat(diagnostics).isNotNull();

                        validateContactedRegions(diagnostics, 1);

                    } else {
                        // Should timeout with 408
                        fail("Should have thrown timeout exception");
                    }
                } else {

                    targetContainer.createItem(testObject).block();

                    CosmosItemResponse<TestObject> response
                        = targetContainer.readItem(testObject.getId(), new PartitionKey(testObject.getMypk()), TestObject.class).block();

                    if (headFailureCount <= 2) {
                        // Should eventually succeed
                        assertThat(response).isNotNull();
                        assertThat(response.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);

                        // Check diagnostics - should have contacted only one region for create
                        CosmosDiagnostics diagnostics = response.getDiagnostics();
                        assertThat(diagnostics).isNotNull();

                        validateContactedRegions(diagnostics, 1);
                    } else {
                        // Should eventually succeed
                        assertThat(response).isNotNull();
                        assertThat(response.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);

                        // Check diagnostics - should have contacted only one region for create
                        CosmosDiagnostics diagnostics = response.getDiagnostics();
                        assertThat(diagnostics).isNotNull();

                        validateContactedRegions(diagnostics, 2);
                    }
                }

            } catch (CosmosException e) {
                // Expected for some scenarios
                if (operationTypeForWhichBarrierFlowIsTriggered == OperationType.Create) {

                    if (headFailureCount <= 1) {
                        fail("Should have succeeded for create with head failures less than or equal to 2");
                    } else {
                        // Should get 408 timeout
                        assertThat(e.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.REQUEST_TIMEOUT);

                        CosmosDiagnostics diagnostics = e.getDiagnostics();

                        validateContactedRegions(diagnostics, 1);
                    }
                }

                if (operationTypeForWhichBarrierFlowIsTriggered == OperationType.Read) {
                    fail("Read operation should have succeeded even with head failures");
                }
            }

        } finally {

            if (originalRntbdTransportClientHolder.v != null) {
                originalRntbdTransportClientHolder.v.close();
            }

            interceptorClient.close();
            safeClose(targetClient);
        }
    }

    private RntbdTransportClientWithStoreResponseInterceptor createClientWithInterceptor(
        CosmosAsyncClient targetClient,
        Utils.ValueHolder<RntbdTransportClient> originalRntbdTransportClientHolder) {

        // Get internal client
        AsyncDocumentClient asyncDocumentClient = ReflectionUtils.getAsyncDocumentClient(targetClient);
        RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncDocumentClient;

        // Get store client and components
        StoreClient storeClient = ReflectionUtils.getStoreClient(rxDocumentClient);
        ReplicatedResourceClient replicatedResourceClient = ReflectionUtils.getReplicatedResourceClient(storeClient);
        ConsistencyReader consistencyReader = ReflectionUtils.getConsistencyReader(replicatedResourceClient);
        ConsistencyWriter consistencyWriter = ReflectionUtils.getConsistencyWriter(replicatedResourceClient);
        StoreReader storeReaderFromConsistencyReader = ReflectionUtils.getStoreReader(consistencyReader);
        StoreReader storeReaderFromConsistencyWriter = ReflectionUtils.getStoreReader(consistencyWriter);

        // Get the original transport client
        RntbdTransportClient originalTransportClient = (RntbdTransportClient) ReflectionUtils.getTransportClient(replicatedResourceClient);
        originalRntbdTransportClientHolder.v = originalTransportClient;

        // Create interceptor client
        RntbdTransportClientWithStoreResponseInterceptor interceptorClient =
            new RntbdTransportClientWithStoreResponseInterceptor(originalTransportClient);

        // Set the interceptor client on both reader and writer
        ReflectionUtils.setTransportClient(storeReaderFromConsistencyReader, interceptorClient);
        ReflectionUtils.setTransportClient(storeReaderFromConsistencyWriter, interceptorClient);
        ReflectionUtils.setTransportClient(consistencyWriter, interceptorClient);

        return interceptorClient;
    }

    private void validateContactedRegions(CosmosDiagnostics diagnostics, int expectedRegionsContactedCount) {

        CosmosDiagnosticsContext cosmosDiagnosticsContext = diagnostics.getDiagnosticsContext();

        assertThat(cosmosDiagnosticsContext).isNotNull();
        assertThat(cosmosDiagnosticsContext.getContactedRegionNames()).isNotNull();
        assertThat(cosmosDiagnosticsContext.getContactedRegionNames()).isNotEmpty();
        assertThat(cosmosDiagnosticsContext.getContactedRegionNames().size()).isEqualTo(expectedRegionsContactedCount);
    }

    private AccountLevelLocationContext getAccountLevelLocationContext(DatabaseAccount databaseAccount, boolean writeOnly) {
        Iterator<DatabaseAccountLocation> locationIterator =
            writeOnly ? databaseAccount.getWritableLocations().iterator() : databaseAccount.getReadableLocations().iterator();

        List<String> serviceOrderedReadableRegions = new ArrayList<>();
        List<String> serviceOrderedWriteableRegions = new ArrayList<>();
        Map<String, String> regionMap = new ConcurrentHashMap<>();

        while (locationIterator.hasNext()) {
            DatabaseAccountLocation accountLocation = locationIterator.next();
            regionMap.put(accountLocation.getName(), accountLocation.getEndpoint());

            if (writeOnly) {
                serviceOrderedWriteableRegions.add(accountLocation.getName());
            } else {
                serviceOrderedReadableRegions.add(accountLocation.getName());
            }
        }

        return new AccountLevelLocationContext(
            serviceOrderedReadableRegions,
            serviceOrderedWriteableRegions,
            regionMap);
    }

    private static class AccountLevelLocationContext {
        private final List<String> serviceOrderedReadableRegions;
        private final List<String> serviceOrderedWriteableRegions;
        private final Map<String, String> regionNameToEndpoint;

        public AccountLevelLocationContext(
            List<String> serviceOrderedReadableRegions,
            List<String> serviceOrderedWriteableRegions,
            Map<String, String> regionNameToEndpoint) {

            this.serviceOrderedReadableRegions = serviceOrderedReadableRegions;
            this.serviceOrderedWriteableRegions = serviceOrderedWriteableRegions;
            this.regionNameToEndpoint = regionNameToEndpoint;
        }
    }

    @AfterClass(groups = {"multi-region"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }
}
