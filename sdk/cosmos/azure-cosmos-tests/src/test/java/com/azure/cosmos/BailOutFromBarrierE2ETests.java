// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.directconnectivity.ConsistencyReader;
import com.azure.cosmos.implementation.directconnectivity.ConsistencyWriter;
import com.azure.cosmos.implementation.directconnectivity.StoreResponseDiagnostics;
import com.azure.cosmos.implementation.directconnectivity.StoreResultDiagnostics;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.test.implementation.interceptor.CosmosInterceptorHelper;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class BailOutFromBarrierE2ETests extends TestSuiteBase {

    private static final ImplementationBridgeHelpers.CosmosAsyncClientHelper.CosmosAsyncClientAccessor cosmosAsyncClientAccessor
        = ImplementationBridgeHelpers.CosmosAsyncClientHelper.getCosmosAsyncClientAccessor();

    private volatile CosmosAsyncDatabase database;
    private volatile CosmosAsyncContainer container;
    private List<String> preferredRegions;
    private Map<String, String> regionNameToEndpoint;

    @BeforeClass(groups = {"multi-region-strong"})
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
    public BailOutFromBarrierE2ETests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }


    /**
     * Provides test scenarios for head request lease not found (410/1022) error handling.
     *
     * <p>Each scenario is defined by the following parameters:</p>
     * <ul>
     *   <li><b>headFailureCount</b>: Number of Head requests that should fail with 410/1022 status codes</li>
     *   <li><b>operationTypeForWhichBarrierFlowIsTriggered</b>: The operation type (Create or Read) that triggers the barrier flow</li>
     *   <li><b>enterPostQuorumSelectionOnlyBarrierLoop</b>: Whether to enter the post quorum selection only barrier loop</li>
     *   <li><b>successfulHeadRequestCountWhichDontMeetBarrier</b>: Number of successful Head requests (204 status code)
     *       that don't meet barrier requirements before failures start</li>
     *   <li><b>isCrossRegionRetryExpected</b>: Whether a cross-region retry is expected for the scenario</li>
     *   <li><b>desiredClientLevelConsistency</b>: The desired client-level consistency (STRONG or BOUNDED_STALENESS)</li>
     * </ul>
     *
     * <p><b>Important Notes:</b></p>
     * <ul>
     *   <li>Scenarios are scoped to cases where document requests succeeded but barrier flows were triggered
     *       (excludes QuorumNotMet scenarios which scope all barriers to primary)</li>
     *   <li>Create operations tolerate up to 2 Head failures before failing with timeout</li>
     *   <li>Read operations tolerate 3-5 Head failures before requiring cross-region retry</li>
     *   <li>In the QuorumSelected phase, successful Head requests that don't meet barrier requirements are tolerated:
     *       <ul>
     *         <li>Up to 18 successful Head requests for BOUNDED_STALENESS consistency</li>
     *         <li>Up to 111 successful Head requests for STRONG consistency</li>
     *       </ul>
     *       (See QuorumReader - maxNumberOfReadBarrierReadRetries and maxBarrierRetriesForMultiRegion)</li>
     * </ul>
     *
     * @return array of test scenario parameters for head request lease not found error handling
     */
    @DataProvider(name = "headRequestLeaseNotFoundScenarios")
    public static Object[][] headRequestLeaseNotFoundScenarios() {

        return new Object[][]{
            { 1, OperationType.Create, false, 0, false, null },
            { 2, OperationType.Create, false, 0, false, null },
            { 3, OperationType.Create, false, 0, false, null },
            { 4, OperationType.Create, false, 0, false, null },
            { 400, OperationType.Create, false, 0, false, null },
            { 1, OperationType.Read, false, 0, false, null },
            { 2, OperationType.Read, false, 0, false, null },
            { 3, OperationType.Read, false, 0, false, null },
            { 4, OperationType.Read, false, 0, true, null },
            { 400, OperationType.Read, false, 0, true, null },
            { 1, OperationType.Read, true, 18, false, ConsistencyLevel.BOUNDED_STALENESS },
            { 2, OperationType.Read, true, 18, false, ConsistencyLevel.BOUNDED_STALENESS },
            { 3, OperationType.Read, true, 18, false, ConsistencyLevel.BOUNDED_STALENESS },
            { 4, OperationType.Read, true, 18, false, ConsistencyLevel.BOUNDED_STALENESS },
            { 5, OperationType.Read, true, 18, true, ConsistencyLevel.BOUNDED_STALENESS },
            { 1, OperationType.Read, true, 18, false, ConsistencyLevel.STRONG },
            { 2, OperationType.Read, true, 18, false, ConsistencyLevel.STRONG },
            { 3, OperationType.Read, true, 18, false, ConsistencyLevel.STRONG },
            { 4, OperationType.Read, true, 18, true, ConsistencyLevel.STRONG },
            { 1, OperationType.Read, true, 111, false, ConsistencyLevel.STRONG },
            { 2, OperationType.Read, true, 111, false, ConsistencyLevel.STRONG },
            { 3, OperationType.Read, true, 111, false, ConsistencyLevel.STRONG },
            { 4, OperationType.Read, true, 111, false, ConsistencyLevel.STRONG },
            { 5, OperationType.Read, true, 111, true, ConsistencyLevel.STRONG }
        };
    }

    /**
     * Validates that the consistency layer properly handles lease not found (410/1022) errors during
     * barrier head requests and implements appropriate bailout/retry strategies based on the failure count,
     * operation type, and consistency level.
     *
     * <p>This test verifies the resilience and fault tolerance of the barrier flow mechanism in the
     * consistency layer when head requests fail with lease not found errors. The barrier flow is triggered
     * when documents requests succeed but additional head requests are needed to ensure consistency guarantees
     * are met across replicas.</p>
     *
     * <p><b>Test Scenarios:</b></p>
     * <ul>
     *   <li><b>Create Operations:</b> Can tolerate up to 2 head failures before timing out. Beyond this threshold,
     *       the operation fails with a 408 timeout status code with 1022 substatus.</li>
     *   <li><b>Read Operations:</b> Can tolerate 3-4 head failures within the same region. After 4 failures,
     *       the operation triggers a cross-region retry to ensure eventual consistency.</li>
     *   <li><b>Post-Quorum Selection Barrier Loop:</b> Tests scenarios where initial barriers pass but subsequent
     *       barriers in the quorum-selected phase encounter failures. The system tolerates different numbers of
     *       successful head requests that don't meet barrier requirements:
     *       <ul>
     *         <li>Up to 18 for BOUNDED_STALENESS consistency</li>
     *         <li>Up to 111 for STRONG consistency</li>
     *       </ul>
     *   </li>
     * </ul>
     *
     * <p><b>Validation Steps:</b></p>
     * <ol>
     *   <li>Creates a CosmosAsyncClient with the specified consistency level and preferred regions</li>
     *   <li>Intercepts store responses to inject controlled head request failures (410/1022)</li>
     *   <li>Executes the specified operation (Create or Read) that triggers the barrier flow</li>
     *   <li>Validates the operation completes successfully or fails appropriately based on failure count</li>
     *   <li>Examines diagnostics to verify:
     *       <ul>
     *         <li>Correct number of head requests were attempted</li>
     *         <li>Expected number of regions were contacted</li>
     *         <li>Primary replica was contacted when failures occurred</li>
     *         <li>No 410 errors reached the Create/Read operations themselves</li>
     *       </ul>
     *   </li>
     * </ol>
     *
     * <p><b>Important Notes:</b></p>
     * <ul>
     *   <li>Test scenarios are scoped to cases where document requests succeeded but barrier flows were triggered,
     *       excluding QuorumNotMet scenarios which scope all barriers to the primary region</li>
     *   <li>The test uses an interceptor client to inject failures at precise points in the barrier flow</li>
     *   <li>Cross-region retry is only expected for Read operations exceeding the failure threshold</li>
     *   <li>The test validates that the system never exposes 410/1022 errors to the application layer for
     *       the primary Create/Read operations - only head requests should encounter these errors</li>
     * </ul>
     *
     * @param headFailureCount the number of head requests that should fail with 410/1022 status codes
     *                         before the system either succeeds, times out, or retries in another region
     * @param operationTypeForWhichBarrierFlowIsTriggered the type of operation (Create or Read) that triggers
     *                                                     the barrier flow and determines retry behavior
     * @param enterPostQuorumSelectionOnlyBarrierLoop if true, allows initial barriers to pass and only injects
     *                                                 failures during the post-quorum selection barrier loop phase
     * @param successfulHeadRequestCountWhichDontMeetBarrier the number of successful head requests (204 status)
     *                                                        that don't meet barrier requirements before failures
     *                                                        start being injected (only relevant when in post-quorum
     *                                                        selection barrier loop)
     * @param isCrossRegionRetryExpected whether the test expects a cross-region retry to occur based on the
     *                                    failure count and operation type
     * @param consistencyLevelApplicableForTest the consistency level to configure on the test client (STRONG or
     *                                       BOUNDED_STALENESS), which affects barrier retry thresholds
     *
     * @throws Exception if the test setup fails or unexpected errors occur during test execution
     *
     * @see ConsistencyReader for barrier flow implementation in read path
     * @see ConsistencyWriter for barrier flow implementation in write path
     * @see StoreResponseInterceptorUtils for failure injection mechanisms
     */
    @Test(groups = {"multi-region-strong"}, dataProvider = "headRequestLeaseNotFoundScenarios", timeOut = 2 * TIMEOUT, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void validateHeadRequestLeaseNotFoundBailout(
        int headFailureCount,
        OperationType operationTypeForWhichBarrierFlowIsTriggered,
        boolean enterPostQuorumSelectionOnlyBarrierLoop,
        int successfulHeadRequestCountWhichDontMeetBarrier,
        boolean isCrossRegionRetryExpected,
        ConsistencyLevel consistencyLevelApplicableForTest) throws Exception {

        CosmosAsyncClient targetClient = getClientBuilder()
            .preferredRegions(this.preferredRegions)
            .buildAsyncClient();

        ConsistencyLevel effectiveConsistencyLevel
            = cosmosAsyncClientAccessor.getEffectiveConsistencyLevel(targetClient, operationTypeForWhichBarrierFlowIsTriggered, null);

        ConnectionMode connectionModeOfClientUnderTest
            = ConnectionMode.valueOf(
                cosmosAsyncClientAccessor.getConnectionMode(
                    targetClient).toUpperCase());

        if (!shouldTestExecutionHappen(
            effectiveConsistencyLevel,
            OperationType.Create.equals(operationTypeForWhichBarrierFlowIsTriggered) ? ConsistencyLevel.STRONG : ConsistencyLevel.BOUNDED_STALENESS,
            consistencyLevelApplicableForTest,
            connectionModeOfClientUnderTest)) {

            safeClose(targetClient);

            throw new SkipException("Skipping test for arguments: " +
                " OperationType: " + operationTypeForWhichBarrierFlowIsTriggered +
                " ConsistencyLevel: " + effectiveConsistencyLevel +
                " ConnectionMode: " + connectionModeOfClientUnderTest +
                " DesiredConsistencyLevel: " + consistencyLevelApplicableForTest);
        }

        AtomicInteger successfulHeadCountTracker = new AtomicInteger();
        AtomicInteger failedHeadCountTracker = new AtomicInteger();

        try {

            TestObject testObject = TestObject.create();

            if (enterPostQuorumSelectionOnlyBarrierLoop) {
                if (OperationType.Read.equals(operationTypeForWhichBarrierFlowIsTriggered)) {
                    CosmosInterceptorHelper.registerTransportClientInterceptor(targetClient, StoreResponseInterceptorUtils.forceSuccessfulBarriersOnReadUntilQuorumSelectionThenForceBarrierFailures(
                            effectiveConsistencyLevel,
                            this.regionNameToEndpoint.get(this.preferredRegions.get(0)),
                            successfulHeadRequestCountWhichDontMeetBarrier,
                            successfulHeadCountTracker,
                            headFailureCount,
                            failedHeadCountTracker,
                            HttpConstants.StatusCodes.GONE,
                            HttpConstants.SubStatusCodes.LEASE_NOT_FOUND
                        ));
                }
            } else {
                if (OperationType.Create.equals(operationTypeForWhichBarrierFlowIsTriggered)) {
                    CosmosInterceptorHelper.registerTransportClientInterceptor(targetClient, StoreResponseInterceptorUtils.forceBarrierFollowedByBarrierFailure(
                            effectiveConsistencyLevel,
                            this.regionNameToEndpoint.get(this.preferredRegions.get(1)),
                            headFailureCount,
                            failedHeadCountTracker,
                            HttpConstants.StatusCodes.GONE,
                            HttpConstants.SubStatusCodes.LEASE_NOT_FOUND
                        ));
                } else if (OperationType.Read.equals(operationTypeForWhichBarrierFlowIsTriggered)) {
                    CosmosInterceptorHelper.registerTransportClientInterceptor(targetClient, StoreResponseInterceptorUtils.forceBarrierFollowedByBarrierFailure(
                            effectiveConsistencyLevel,
                            this.regionNameToEndpoint.get(this.preferredRegions.get(0)),
                            headFailureCount,
                            failedHeadCountTracker,
                            HttpConstants.StatusCodes.GONE,
                            HttpConstants.SubStatusCodes.LEASE_NOT_FOUND
                        ));
                }
            }

            try {
                CosmosAsyncDatabase targetAsyncDatabase = targetClient.getDatabase(this.database.getId());
                CosmosAsyncContainer targetContainer = targetAsyncDatabase.getContainer(this.container.getId());

                Thread.sleep(5000); // Wait for collection to be available to be read

                // Assert based on operation type and failure count
                if (operationTypeForWhichBarrierFlowIsTriggered == OperationType.Create) {

                    // Perform the operation
                    CosmosItemResponse<?> response = targetContainer.createItem(testObject).block();

                    // For Create, Head can only fail up to 2 times before Create fails with a timeout
                    if (headFailureCount <= 1) {
                        // Should eventually succeed
                        assertThat(response).isNotNull();
                        assertThat(response.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CREATED);

                        // Check diagnostics - should have contacted only one region for create
                        CosmosDiagnostics diagnostics = response.getDiagnostics();
                        assertThat(diagnostics).isNotNull();

                        validateHeadRequestsInCosmosDiagnostics(diagnostics, 2, (2 + successfulHeadRequestCountWhichDontMeetBarrier));
                        validateContactedRegions(diagnostics, 1);
                    } else {
                        // Should timeout with 408
                        fail("Should have thrown timeout exception");
                    }
                } else {

                    targetContainer.createItem(testObject).block();

                    CosmosItemResponse<TestObject> response
                        = targetContainer.readItem(testObject.getId(), new PartitionKey(testObject.getMypk()), TestObject.class).block();

                    // for Read, Head can fail up to 3 times and still succeed from the same region after which read has to go to another region
                    if (!isCrossRegionRetryExpected) {
                        // Should eventually succeed
                        assertThat(response).isNotNull();
                        assertThat(response.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);

                        // Check diagnostics - should have contacted only one region for create
                        CosmosDiagnostics diagnostics = response.getDiagnostics();
                        assertThat(diagnostics).isNotNull();

                        validateHeadRequestsInCosmosDiagnostics(diagnostics, 5, (5 + successfulHeadRequestCountWhichDontMeetBarrier));
                        validateContactedRegions(diagnostics, 1);
                    } else {
                        // Should eventually succeed
                        assertThat(response).isNotNull();
                        assertThat(response.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);

                        CosmosDiagnostics diagnostics = response.getDiagnostics();
                        assertThat(diagnostics).isNotNull();

                        validateHeadRequestsInCosmosDiagnostics(diagnostics, 5, (5 + successfulHeadRequestCountWhichDontMeetBarrier));
                        validateContactedRegions(diagnostics, 2);
                    }
                }

            } catch (CosmosException e) {
                // Expected for some scenarios
                if (operationTypeForWhichBarrierFlowIsTriggered == OperationType.Create) {

                    if (headFailureCount <= 1) {
                        fail("Should have succeeded for create with head failures less than or equal to 2");
                    } else {
                        // Should get 408-1022 timeout
                        assertThat(e.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.REQUEST_TIMEOUT);
                        assertThat(e.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.LEASE_NOT_FOUND);

                        CosmosDiagnostics diagnostics = e.getDiagnostics();

                        validateHeadRequestsInCosmosDiagnostics(diagnostics, 2, (2 + successfulHeadRequestCountWhichDontMeetBarrier));
                        validateContactedRegions(diagnostics, 1);
                    }
                }

                if (operationTypeForWhichBarrierFlowIsTriggered == OperationType.Read) {
                    fail("Read operation should have succeeded even with head failures through cross region retry.");
                }
            }

        } finally {
            safeClose(targetClient);
        }
    }

    private void validateContactedRegions(CosmosDiagnostics diagnostics, int expectedRegionsContactedCount) {

        CosmosDiagnosticsContext cosmosDiagnosticsContext = diagnostics.getDiagnosticsContext();
        Collection<ClientSideRequestStatistics> clientSideRequestStatisticsCollection
            = diagnostics.getClientSideRequestStatistics();

        for (ClientSideRequestStatistics clientSideRequestStatistics : clientSideRequestStatisticsCollection) {

            Collection<ClientSideRequestStatistics.StoreResponseStatistics> storeResponseDiagnosticsList
                = clientSideRequestStatistics.getResponseStatisticsList();

            for (ClientSideRequestStatistics.StoreResponseStatistics storeResponseStatistics : storeResponseDiagnosticsList) {
                if (storeResponseStatistics.getRequestOperationType() == OperationType.Create || storeResponseStatistics.getRequestOperationType() == OperationType.Read) {

                    if (storeResponseStatistics.getStoreResult().getStoreResponseDiagnostics().getStatusCode() == 410) {
                        fail("Should not have encountered 410 for Create/Read operation");
                    }
                }
            }
        }

        assertThat(cosmosDiagnosticsContext).isNotNull();
        assertThat(cosmosDiagnosticsContext.getContactedRegionNames()).isNotNull();
        assertThat(cosmosDiagnosticsContext.getContactedRegionNames()).isNotEmpty();
        assertThat(cosmosDiagnosticsContext.getContactedRegionNames().size()).as("Mismatch in regions contacted.").isEqualTo(expectedRegionsContactedCount);
    }

    private void validateHeadRequestsInCosmosDiagnostics(
        CosmosDiagnostics diagnostics,
        int maxExpectedHeadRequestCountWithLeaseNotFoundErrors,
        int maxExpectedHeadRequestCount) {

        int actualHeadRequestCountWithLeaseNotFoundErrors = 0;
        int actualHeadRequestCount = 0;
        boolean primaryReplicaContacted = false;

        Collection<ClientSideRequestStatistics> clientSideRequestStatisticsCollection
            = diagnostics.getClientSideRequestStatistics();

        for (ClientSideRequestStatistics clientSideRequestStatistics : clientSideRequestStatisticsCollection) {

            Collection<ClientSideRequestStatistics.StoreResponseStatistics> storeResponseDiagnosticsList
                = clientSideRequestStatistics.getResponseStatisticsList();

            for (ClientSideRequestStatistics.StoreResponseStatistics storeResponseStatistics : storeResponseDiagnosticsList) {
                if (storeResponseStatistics.getRequestOperationType() == OperationType.Create || storeResponseStatistics.getRequestOperationType() == OperationType.Read) {

                    if (storeResponseStatistics.getStoreResult().getStoreResponseDiagnostics().getStatusCode() == 410) {
                        fail("Should not have encountered 410 for Create/Read operation");
                    }
                }
            }

            Collection<ClientSideRequestStatistics.StoreResponseStatistics> supplementalResponseStatisticsList
                = clientSideRequestStatistics.getSupplementalResponseStatisticsList();

            for (ClientSideRequestStatistics.StoreResponseStatistics storeResponseStatistics : supplementalResponseStatisticsList) {
                if (storeResponseStatistics.getRequestOperationType() == OperationType.Head) {

                    StoreResultDiagnostics storeResultDiagnostics = storeResponseStatistics.getStoreResult();

                    assertThat(storeResultDiagnostics).isNotNull();

                    String storePhysicalAddressContacted
                        = storeResultDiagnostics.getStorePhysicalAddressAsString();

                    StoreResponseDiagnostics storeResponseDiagnostics
                        = storeResultDiagnostics.getStoreResponseDiagnostics();

                    assertThat(storeResponseDiagnostics).isNotNull();

                    actualHeadRequestCount++;

                    if (storeResponseDiagnostics.getStatusCode() == HttpConstants.StatusCodes.GONE && storeResponseDiagnostics.getSubStatusCode() == HttpConstants.SubStatusCodes.LEASE_NOT_FOUND) {
                        actualHeadRequestCountWithLeaseNotFoundErrors++;
                    }

                    if (isStorePhysicalAddressThatOfPrimaryReplica(storePhysicalAddressContacted)) {
                        primaryReplicaContacted = true;
                    }
                }
            }
        }

        assertThat(actualHeadRequestCountWithLeaseNotFoundErrors).as("Head request failed count with 410/1022 should be greater than 1.").isGreaterThan(0);
        assertThat(actualHeadRequestCountWithLeaseNotFoundErrors).as("Too many head request failed.").isLessThanOrEqualTo(maxExpectedHeadRequestCountWithLeaseNotFoundErrors);
        assertThat(actualHeadRequestCount).as("Too many Head requests made perhaps due to real replication lag.").isLessThanOrEqualTo(maxExpectedHeadRequestCount + 10);
        assertThat(primaryReplicaContacted).as("Primary replica should be contacted even when a single Head request sees a 410/1022").isTrue();
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

    private boolean shouldTestExecutionHappen(
        ConsistencyLevel effectiveConsistencyLevel,
        ConsistencyLevel minimumConsistencyLevel,
        ConsistencyLevel consistencyLevelApplicableForTestScenario,
        ConnectionMode connectionModeOfClientUnderTest) {

        if (!connectionModeOfClientUnderTest.name().equalsIgnoreCase(ConnectionMode.DIRECT.name())) {
            return false;
        }

        if (consistencyLevelApplicableForTestScenario != null) {
            return consistencyLevelApplicableForTestScenario.equals(effectiveConsistencyLevel);
        }

        return effectiveConsistencyLevel.compareTo(minimumConsistencyLevel) <= 0;
    }

    private static boolean isStorePhysicalAddressThatOfPrimaryReplica(String storePhysicalAddress) {

        if (Strings.isNullOrEmpty(storePhysicalAddress)) {
            return false;
        }

        return storePhysicalAddress.endsWith("p/");
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

    @AfterClass(groups = {"multi-region-strong"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {}
}
