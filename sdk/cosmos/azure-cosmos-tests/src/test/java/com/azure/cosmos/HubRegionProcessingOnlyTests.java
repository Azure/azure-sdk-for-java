// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorResult;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for validating hub region processing behavior when using the hub region processing only header.
 * These tests verify that when a read operation encounters a 404-1002 error in a non-hub region,
 * the operation correctly fails over to the partition-set level hub region.
 */
public class HubRegionProcessingOnlyTests extends TestSuiteBase {

    private volatile CosmosAsyncDatabase database;
    private volatile CosmosAsyncContainer container;
    private List<String> preferredRegions;
    private Map<String, String> regionNameToEndpoint;
    private String partitionKeyValue = "12345";

    private static final ImplementationBridgeHelpers.CosmosAsyncClientHelper.CosmosAsyncClientAccessor cosmosAsyncClientAccessor
        = ImplementationBridgeHelpers.CosmosAsyncClientHelper.getCosmosAsyncClientAccessor();

    @Factory(dataProvider = "clientBuildersWithDirectSession")
    public HubRegionProcessingOnlyTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"multi-region"})
    public void beforeClass() {
        try (CosmosAsyncClient dummy = getClientBuilder().buildAsyncClient()) {
            AsyncDocumentClient asyncDocumentClient = BridgeInternal.getContextClient(dummy);
            GlobalEndpointManager globalEndpointManager = asyncDocumentClient.getGlobalEndpointManager();

            DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();

            AccountLevelLocationContext accountLevelContext = getAccountLevelLocationContext(databaseAccount, false);

            // Ensure we have at least 3 regions for this test
            if (accountLevelContext.serviceOrderedReadableRegions.size() < 3) {
                throw new SkipException("Test requires at least 3 readable regions");
            }

            // Set preferred regions - start with third region, then first, then second
            this.preferredRegions = new ArrayList<>();
            this.preferredRegions.add(accountLevelContext.serviceOrderedReadableRegions.get(0)); // Third region
            this.preferredRegions.add(accountLevelContext.serviceOrderedReadableRegions.get(2)); // First region
            this.preferredRegions.add(accountLevelContext.serviceOrderedReadableRegions.get(1)); // Second region

            this.regionNameToEndpoint = accountLevelContext.regionNameToEndpoint;
            this.database = getSharedCosmosDatabase(dummy);
            this.container = getSharedSinglePartitionCosmosContainer(dummy);
        }
    }

    @AfterClass(groups = {"multi-region"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
    }

    @DataProvider(name = "hubRegionProcessingScenarios")
    public static Object[][] hubRegionProcessingScenarios() {
        return new Object[][]{
            // Test read operation with 404-1002 injected in third preferred region (first in preferred list)
            { OperationType.Read }
        };
    }

    /**
     * Validates that when a read operation encounters a 404-1002 (READ_SESSION_NOT_AVAILABLE) error
     * in a non-hub region, the operation correctly identifies and contacts the partition-set level hub region.
     *
     * <p><b>Test Flow:</b></p>
     * <ol>
     *   <li>Pre-creates a container (expected to already exist)</li>
     *   <li>Injects 404-1002 fault on document reads from the third preferred region (first in preferred list)</li>
     *   <li>Performs a read operation and lets it complete</li>
     *   <li>Extracts the CosmosDiagnosticsContext to determine which regions were contacted</li>
     *   <li>Validates that the partition-set level hub region was contacted</li>
     *   <li>Determines the partition-set hub region as ground truth by:
     *       <ul>
     *         <li>Creating another CosmosClient instance</li>
     *         <li>Performing an Upsert on the same partition-set</li>
     *         <li>Extracting the region from which success is obtained</li>
     *         <li>Using that region as the base truth for validation in step 4</li>
     *       </ul>
     *   </li>
     * </ol>
     *
     * <p><b>Expected Behavior:</b></p>
     * <ul>
     *   <li>The read operation should fail in the third preferred region due to injected 404-1002</li>
     *   <li>The operation should automatically fail over to the hub region for that partition-set</li>
     *   <li>The hub region contacted should match the region determined via the Upsert ground truth test</li>
     *   <li>The CosmosDiagnosticsContext should show both the failed region and the hub region in contacted regions</li>
     * </ul>
     *
     * @param operationType the type of operation to perform (Read in this test)
     * @throws Exception if test setup fails or unexpected errors occur
     */
    @Test(groups = {"multi-region"}, dataProvider = "hubRegionProcessingScenarios", timeOut = 2 * TIMEOUT)
    public void validateHubRegionProcessingOnReadWith404_1002(OperationType operationType) throws Exception {

        // Skip if we don't have at least 3 regions
        if (this.preferredRegions.size() < 3) {
            throw new SkipException("Test requires at least 3 readable regions");
        }

        // Create test client with preferred regions (third region first)
        CosmosAsyncClient testClient = getClientBuilder()
            .preferredRegions(this.preferredRegions)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .sessionRetryOptions(new SessionRetryOptionsBuilder().regionSwitchHint(CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED).build())
            .buildAsyncClient();

        ConnectionMode connectionModeForTestClient
            = ConnectionMode.valueOf(cosmosAsyncClientAccessor.getConnectionMode(testClient));

        try {

            System.setProperty("COSMOS.IS_READ_AVAILABILITY_STRATEGY_ENABLED_WITH_PPAF", "false");

            String databaseId = "testDatabase";
            String containerId = "testContainer";

            // Step 1: Container is pre-created (using shared container)
            CosmosAsyncDatabase targetDatabase = testClient.getDatabase(databaseId);
            CosmosAsyncContainer targetContainer = targetDatabase.getContainer(containerId);

            TestObject testObject = TestObject.create(this.partitionKeyValue);

            // Create the document using the test client
            targetContainer.createItem(testObject).block();

            // Step 4: Determine partition-set hub region as base truth
            String hubRegion = determinePartitionSetHubRegion(this.partitionKeyValue, databaseId, containerId);

            logger.info("Determined hub region for partition '{}': {}", this.partitionKeyValue, hubRegion);

            // Step 2: Inject 404-1002 fault in the third preferred region (first in list)
            String thirdRegion = this.preferredRegions.get(0); // Third region is first in our preferred list
            injectReadSessionNotAvailableError(
                targetContainer,
                thirdRegion,
                FaultInjectionOperationType.READ_ITEM,
                ConnectionMode.DIRECT.equals(connectionModeForTestClient) ? FaultInjectionConnectionType.DIRECT : FaultInjectionConnectionType.GATEWAY);

            // Wait a bit for fault injection to be active
            Thread.sleep(1000);

            // Step 3: Perform read operation and let it complete
            CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
            CosmosItemResponse<TestObject> response = targetContainer
                .readItem(testObject.getId(), new PartitionKey(this.partitionKeyValue), requestOptions, TestObject.class)
                .block();

            // Validate operation succeeded
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);

            // Step 4: Extract CosmosDiagnosticsContext and validate contacted regions
            CosmosDiagnostics diagnostics = response.getDiagnostics();
            assertThat(diagnostics).isNotNull();

            CosmosDiagnosticsContext diagnosticsContext = diagnostics.getDiagnosticsContext();
            assertThat(diagnosticsContext).isNotNull();
            assertThat(diagnosticsContext.getContactedRegionNames()).isNotNull();
            assertThat(diagnosticsContext.getContactedRegionNames()).isNotEmpty();

            logger.info("Contacted regions: {}", diagnosticsContext.getContactedRegionNames());

            // Validate that hub region was contacted
            assertThat(diagnosticsContext.getContactedRegionNames())
                .as("Hub region should be contacted")
                .contains(hubRegion);

            // Validate that we contacted more than one region (due to failover from third region)
            assertThat(diagnosticsContext.getContactedRegionNames().size())
                .as("Should contact multiple regions due to failover")
                .isGreaterThan(1);

        } finally {
            safeClose(testClient);
        }
    }

    /**
     * Determines the partition-set hub region by creating a separate client and performing an upsert operation.
     * The region that successfully processes the upsert is considered the hub region for that partition-set.
     *
     * @param partitionKeyValue the partition key value to test
     * @return the name of the hub region
     */
    private String determinePartitionSetHubRegion(String partitionKeyValue, String databaseId, String containerId) {
        // Create a client with a different preferred region order to find the hub
        List<String> hubDiscoveryRegions = new ArrayList<>(this.preferredRegions);

        CosmosAsyncClient hubDiscoveryClient = getClientBuilder()
            .preferredRegions(hubDiscoveryRegions)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .buildAsyncClient();

        try {
            CosmosAsyncDatabase targetDatabase = hubDiscoveryClient.getDatabase(databaseId);
            CosmosAsyncContainer targetContainer = targetDatabase.getContainer(containerId);

            // Perform an upsert on the same partition-set
            String testDocId = UUID.randomUUID().toString();
            TestObject testObject = TestObject.create(partitionKeyValue);

            CosmosItemResponse<TestObject> upsertResponse = targetContainer
                .upsertItem(testObject)
                .block();

            assertThat(upsertResponse).isNotNull();
            assertThat(upsertResponse.getStatusCode()).isIn(
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.CREATED);

            // Extract the region from diagnostics
            CosmosDiagnostics diagnostics = upsertResponse.getDiagnostics();
            assertThat(diagnostics).isNotNull();

            CosmosDiagnosticsContext diagnosticsContext = diagnostics.getDiagnosticsContext();

            assertThat(diagnosticsContext).isNotNull();
            assertThat(diagnosticsContext.getContactedRegionNames()).isNotNull();
            assertThat(diagnosticsContext.getContactedRegionNames()).isNotEmpty();

            TreeSet<String> contactedRegionNames = (TreeSet<String>) diagnosticsContext.getContactedRegionNames();

            return contactedRegionNames.pollLast();
        } finally {
            safeClose(hubDiscoveryClient);
        }
    }

    /**
     * Injects READ_SESSION_NOT_AVAILABLE (404-1002) errors into a specific region for read operations.
     *
     * @param container the container to inject faults into
     * @param region the region where faults should be injected
     * @param faultInjectionOperationType the operation type to inject faults for
     */
    private void injectReadSessionNotAvailableError(
        CosmosAsyncContainer container,
        String region,
        FaultInjectionOperationType faultInjectionOperationType,
        FaultInjectionConnectionType faultInjectionConnectionType) {

        String ruleName = "serverErrorRule-read-session-unavailable-" + UUID.randomUUID();

        FaultInjectionServerErrorResult errorResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
            .times(FaultInjectionConnectionType.DIRECT.equals(faultInjectionConnectionType) ? 8 : 1)
            .build();

        FaultInjectionRule rule = new FaultInjectionRuleBuilder(ruleName)
            .condition(
                new FaultInjectionConditionBuilder()
                    .operationType(faultInjectionOperationType)
                    .connectionType(faultInjectionConnectionType)
                    .region(region)
                    .build()
            )
            .result(errorResult)
            .duration(Duration.ofSeconds(60))
            .build();

        List<FaultInjectionRule> rules = new ArrayList<>();
        rules.add(rule);

        CosmosFaultInjectionHelper
            .configureFaultInjectionRules(container, rules)
            .block();

        logger.info("Injected 404-1002 fault for region '{}', operationType '{}'", region, faultInjectionOperationType);
    }

    /**
     * Retrieves account-level location context including readable/writable regions and their endpoints.
     *
     * @param databaseAccount the database account
     * @param writeOnly whether to get write regions only
     * @return account level location context
     */
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
}
