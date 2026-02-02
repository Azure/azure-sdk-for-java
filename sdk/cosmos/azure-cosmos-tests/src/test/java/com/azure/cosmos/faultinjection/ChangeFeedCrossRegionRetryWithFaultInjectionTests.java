// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.ChangeFeedProcessorBuilder;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;

/**
 * Tests for verifying cross-region retry behavior for Change Feed Processor when:
 * 1. Replicas return 410-20001 (TRANSPORT_GENERATED_410)
 * 2. Follow-up Gateway address refresh calls also fail
 *    Strong Consistency Cosmos DB account with TWO regions</li>
 *    Reads initially routed to the read region (first preferred region)</li>
 *    CosmosClient configured with preferredRegions: [readRegion, writeRegion]</li>
 */
public class ChangeFeedCrossRegionRetryWithFaultInjectionTests extends FaultInjectionTestBase {

    private static final Logger logger = LoggerFactory.getLogger(ChangeFeedCrossRegionRetryWithFaultInjectionTests.class);

    // Test timeout: 10 minutes to allow for cross-region retry which can take time
    // due to GoneRetryPolicy's 60s timeout (Strong consistency) before 410->503 conversion
    private static final int TIMEOUT = 600000;

    // Poll delay for ChangeFeedProcessor - how often it checks for new changes
    private static final Duration FEED_POLL_DELAY = Duration.ofSeconds(1);

    private CosmosAsyncClient client;

    private CosmosAsyncDatabase cosmosAsyncDatabase;


    private CosmosAsyncContainer feedContainer;


    private CosmosAsyncContainer leaseContainer;

    // SDK will try regions in this order when cross-region retry occurs
    private List<String> preferredRegions;

    // First region where faults will be injected - reads initially routed here
    private String firstPreferredRegion;

    // Second region (healthy) - cross-region retry target when first region fails
    private String secondPreferredRegion;

    @Factory(dataProvider = "simpleClientBuildersWithJustDirectTcp")
    public ChangeFeedCrossRegionRetryWithFaultInjectionTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"multi-region"}, timeOut = TIMEOUT)
    public void beforeClass() {

        // STEP 1: Create client and discover account regions
        client = getClientBuilder().buildAsyncClient();

        // Access internal GlobalEndpointManager to get account topology
        AsyncDocumentClient asyncDocumentClient = BridgeInternal.getContextClient(client);
        GlobalEndpointManager globalEndpointManager = asyncDocumentClient.getGlobalEndpointManager();
        DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();

        // STEP 2: Extract readable regions from account configuration
        Iterator<DatabaseAccountLocation> locationIterator = databaseAccount.getReadableLocations().iterator();
        List<String> readRegions = new java.util.ArrayList<>();
        while (locationIterator.hasNext()) {
            readRegions.add(locationIterator.next().getName());
        }

        // This test requires at least 2 regions for cross-region retry validation
        assertThat(readRegions.size()).as("Account must have at least 2 regions").isGreaterThanOrEqualTo(2);

        // STEP 3: Configure preferred regions for cross-region retry testing

        this.firstPreferredRegion = readRegions.get(0);
        this.secondPreferredRegion = readRegions.get(1);
        this.preferredRegions = Arrays.asList(firstPreferredRegion, secondPreferredRegion);

        logger.info("DEBUG: Account regions discovered:");
        logger.info("DEBUG:   First preferred region (read region): {}", firstPreferredRegion);
        logger.info("DEBUG:   Second preferred region (fallback): {}", secondPreferredRegion);

        // STEP 4: Create test database
        String databaseId = "cfp-fault-injection-test-db-" + UUID.randomUUID();
        client.createDatabaseIfNotExists(databaseId).block();
        cosmosAsyncDatabase = client.getDatabase(databaseId);

        // STEP 5: Create feed container (where documents are stored)
        String feedContainerId = "feed-container-" + UUID.randomUUID();
        cosmosAsyncDatabase.createContainerIfNotExists(
            new CosmosContainerProperties(feedContainerId, "/pk"),
            ThroughputProperties.createManualThroughput(10000)
        ).block();
        feedContainer = cosmosAsyncDatabase.getContainer(feedContainerId);

        // STEP 6: Create lease container (for ChangeFeedProcessor checkpoints)
        String leaseContainerId = "lease-container-" + UUID.randomUUID();
        cosmosAsyncDatabase.createContainerIfNotExists(
            new CosmosContainerProperties(leaseContainerId, "/id"),
            ThroughputProperties.createManualThroughput(400)
        ).block();
        leaseContainer = cosmosAsyncDatabase.getContainer(leaseContainerId);

        logger.info("DEBUG: Test infrastructure created:");
        logger.info("DEBUG:   Database: {}", databaseId);
        logger.info("DEBUG:   Feed container: {}", feedContainerId);
        logger.info("DEBUG:   Lease container: {}", leaseContainerId);
    }

    @AfterClass(groups = {"multi-region"}, timeOut = TIMEOUT, alwaysRun = true)
    public void afterClass() {
        if (cosmosAsyncDatabase != null) {
            try {
                cosmosAsyncDatabase.delete().block();
            } catch (Exception e) {
                logger.warn("Failed to delete test database", e);
            }
        }
        safeClose(client);
    }


    /**
     * PRIMARY TEST: Validates cross-region retry when BOTH failures occur:
     * 1. Replicas return 410-20001 (TRANSPORT_GENERATED_410)
     * 2. Follow-up Gateway address refresh calls also fail
     */
    @Test(groups = {"multi-region"}, timeOut = TIMEOUT)
    public void changeFeedProcessor_replicaAndGatewayBothFail_shouldTriggerCrossRegionRetry() {

        // Test resources - declared outside try block for cleanup in finally
        CosmosAsyncClient testClient = null;
        ChangeFeedProcessor changeFeedProcessor = null;
        FaultInjectionRule dataPlaneResponseDelayRule = null;
        FaultInjectionRule addressRefreshDelayRule = null;

        // Tracking variables for test assertions
        AtomicInteger documentsReceived = new AtomicInteger(0);
        AtomicBoolean processingComplete = new AtomicBoolean(false);

        try {

            // STEP 1: Create test client with preferred regions
            testClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .consistencyLevel(ConsistencyLevel.STRONG)
                .preferredRegions(this.preferredRegions)
                .directMode(DirectConnectionConfig.getDefaultConfig())
                .contentResponseOnWriteEnabled(true)
                .buildAsyncClient();

            // Get container references through the test client
            CosmosAsyncContainer testFeedContainer = testClient
                .getDatabase(this.cosmosAsyncDatabase.getId())
                .getContainer(this.feedContainer.getId());

            CosmosAsyncContainer testLeaseContainer = testClient
                .getDatabase(this.cosmosAsyncDatabase.getId())
                .getContainer(this.leaseContainer.getId());


            // STEP 2: Get FeedRanges for fault injection targeting
            List<FeedRange> feedRanges = testFeedContainer.getFeedRanges().block();
            assertThat(feedRanges).isNotNull();


            // STEP 3a: Create FAULT RULE 1 - Data Plane Response Delay
            String dataPlaneRuleId = "dataPlane-responseDelay-" + UUID.randomUUID();
            dataPlaneResponseDelayRule = new FaultInjectionRuleBuilder(dataPlaneRuleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .region(this.firstPreferredRegion)  // Only affect first region
                        .operationType(FaultInjectionOperationType.READ_FEED_ITEM)  // Change Feed reads
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
                        .delay(Duration.ofSeconds(65))
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();


            // STEP 3b: Create FAULT RULE 2 - Gateway Address Refresh Delay
            String addressRefreshRuleId = "addressRefresh-connectionDelay-" + UUID.randomUUID();
            addressRefreshDelayRule = new FaultInjectionRuleBuilder(addressRefreshRuleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .region(this.firstPreferredRegion)  // Only affect first region
                        .operationType(FaultInjectionOperationType.METADATA_REQUEST_ADDRESS_REFRESH)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.CONNECTION_DELAY)
                        .delay(Duration.ofSeconds(50))  // Causes connection timeout
                        .times(4)  // Apply 4 times to exhaust WebExceptionRetryPolicy (3 retries)
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

            // NTE: Fault injection rules are created but NOT applied yet
            // We will apply them AFTER ChangeFeedProcessor starts and completes initialization
            logger.info("DEBUG: Fault injection rules created (not yet applied):");
            logger.info("DEBUG:   Rule 1: {} - RESPONSE_DELAY 65s on READ_FEED_ITEM in {}",
                dataPlaneRuleId, this.firstPreferredRegion);
            logger.info("DEBUG:   Rule 2: {} - CONNECTION_DELAY 50s on ADDRESS_REFRESH in {}",
                addressRefreshRuleId, this.firstPreferredRegion);


            // STEP 4: Create and start ChangeFeedProcessor (with NO documents yet)
            // Track documents received and test completion
            int documentCount = 5;

            ChangeFeedProcessorOptions options = new ChangeFeedProcessorOptions();
            options.setStartFromBeginning(true);
            options.setFeedPollDelay(FEED_POLL_DELAY);
            options.setLeasePrefix("primary-test-");


            changeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName("TestHost_" + UUID.randomUUID())
                .feedContainer(testFeedContainer)
                .leaseContainer(testLeaseContainer)
                .handleChanges((List<JsonNode> docs) -> {
                    // This callback is invoked when changes are detected
                    logger.info("DEBUG: ChangeFeedProcessor received {} documents", docs.size());
                    documentsReceived.addAndGet(docs.size());
                    if (documentsReceived.get() >= documentCount) {
                        processingComplete.set(true);
                    }
                })
                .options(options)
                .buildChangeFeedProcessor();

            logger.info("DEBUG: Starting ChangeFeedProcessor (with empty container)...");
            changeFeedProcessor.start().block();

            // Wait a few seconds for CFP to fully initialize (acquire leases, etc.)
            logger.info("DEBUG: Waiting for CFP to initialize...");
            Thread.sleep(5000);


            // STEP 5: Enable fault injection AFTER CFP initialization

            logger.info("DEBUG: Enabling fault injection rules NOW...");
            CosmosFaultInjectionHelper.configureFaultInjectionRules(
                testFeedContainer,
                Arrays.asList(dataPlaneResponseDelayRule, addressRefreshDelayRule)
            ).block();

            logger.info("DEBUG: Fault injection rules are now ACTIVE:");
            logger.info("DEBUG:   First region: {} (BOTH faults injected - will fail)", this.firstPreferredRegion);
            logger.info("DEBUG:   Second region: {} (healthy - cross-region target)", this.secondPreferredRegion);


            // STEP 6: Insert test documents AFTER fault injection is enabled
            logger.info("DEBUG: Inserting {} test documents using original client (no fault injection)...", documentCount);
            for (int i = 0; i < documentCount; i++) {
                String docId = "test-doc-" + UUID.randomUUID();
                TestDocument doc = new TestDocument(docId, docId, "Test document " + i);
                this.feedContainer.createItem(doc).block();  // Use original client without fault injection
            }
            logger.info("DEBUG: Documents inserted successfully. CFP will now poll and hit the faults.");


            // STEP 7: Wait for documents to be processed
            long startTime = System.currentTimeMillis();
            long maxWaitTime = 360000;  // 6 minutes max wait

            while (!processingComplete.get() &&
                   (System.currentTimeMillis() - startTime) < maxWaitTime) {
                Thread.sleep(2000);  // Check every 2 seconds
                logger.info("DEBUG: Waiting... Received: {}/{}, Data plane hits: {}, Address refresh hits: {}",
                    documentsReceived.get(), documentCount,
                    dataPlaneResponseDelayRule.getHitCount(),
                    addressRefreshDelayRule.getHitCount());
            }

            // STEP 8: Validate results - verify cross-region retry occurred
            logger.info("DEBUG: TEST RESULTS:");
            logger.info("DEBUG:   Documents received: {}/{}", documentsReceived.get(), documentCount);
            logger.info("DEBUG:   Data plane fault hit count: {}", dataPlaneResponseDelayRule.getHitCount());
            logger.info("DEBUG:   Address refresh fault hit count: {}", addressRefreshDelayRule.getHitCount());
            logger.info("DEBUG: =======================================================================");

            // VALIDATION 1: Data plane fault was triggered
            // This proves that replicas in first region returned 410/20001 (TRANSPORT_GENERATED_410)
            // The response delay (65s) exceeds the network timeout (60s), causing SDK to generate 410/20001
            assertThat(dataPlaneResponseDelayRule.getHitCount())
                .as("Data plane fault should have been triggered (replicas returning 410/20001)")
                .isGreaterThan(0);

            // VALIDATION 2: Address refresh fault was triggered
            // This proves that after receiving 410/20001, SDK tried to refresh addresses from Gateway
            // and those calls also failed due to the connection delay fault
            assertThat(addressRefreshDelayRule.getHitCount())
                .as("Address refresh fault should have been triggered (Gateway address refresh failed)")
                .isGreaterThan(0);

            // VALIDATION 3: Documents were processed
            // if documents were processed despite BOTH faults
            // being active in the first region, it means cross-region retry worked correctly
            // The SDK failed over from the faulty first region to the healthy second region
            assertThat(documentsReceived.get())
                .as("Documents should be processed via cross-region retry to second region")
                .isGreaterThanOrEqualTo(documentCount);


        } catch (Exception e) {
            logger.error("DEBUG: Test failed", e);
            fail("Test failed: " + e.getMessage());
        } finally {
            // ================================================================
            // CLEANUP: Stop processor and disable fault rules
            // ================================================================
            if (changeFeedProcessor != null) {
                try {
                    changeFeedProcessor.stop().block();
                } catch (Exception e) {
                    logger.warn("DEBUG: Error stopping ChangeFeedProcessor", e);
                }
            }
            if (dataPlaneResponseDelayRule != null) {
                dataPlaneResponseDelayRule.disable();
            }
            if (addressRefreshDelayRule != null) {
                addressRefreshDelayRule.disable();
            }
            safeClose(testClient);

        }
    }


    // HELPER CLASSES

    /**
     * Simple test document class with id and partition key.
     */
    public static class TestDocument {
        private String id;       // Document ID (unique within partition)
        private String pk;       // Partition key value
        private String message;  // Test payload

        // Required by Jackson for deserialization
        public TestDocument() {}

        public TestDocument(String id, String pk, String message) {
            this.id = id;
            this.pk = pk;
            this.message = message;
        }

        // Standard getters and setters required by Jackson
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getPk() { return pk; }
        public void setPk(String pk) { this.pk = pk; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
