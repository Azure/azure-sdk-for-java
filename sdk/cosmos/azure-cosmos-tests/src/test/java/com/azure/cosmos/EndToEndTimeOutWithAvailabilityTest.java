// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosReadManyRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.test.faultinjection.FaultInjectionCondition;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorResultBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import com.azure.cosmos.test.faultinjection.IFaultInjectionResult;
import com.azure.cosmos.test.implementation.faultinjection.FaultInjectorProvider;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;

public class EndToEndTimeOutWithAvailabilityTest extends TestSuiteBase {
    private static final int DEFAULT_NUM_DOCUMENTS = 100;
    private final Random random;
    private static final int TIMEOUT = 60000;
    private CosmosAsyncClient clientWithPreferredRegions;
    private CosmosAsyncContainer cosmosAsyncContainer;
    private List<String> preferredRegionList;


    // These regions should match the ones in test-resources.json
    private List<String> regions;

    @Factory(dataProvider = "clientBuildersWithDirectTcpSession")
    public EndToEndTimeOutWithAvailabilityTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        random = new Random();
    }

    @BeforeClass(groups = {"multi-master", "multi-region"}, timeOut = SETUP_TIMEOUT * 100)
    public void beforeClass() throws Exception {
        System.setProperty("COSMOS.DEFAULT_SESSION_TOKEN_MISMATCH_WAIT_TIME_IN_MILLISECONDS", "1000");
        System.setProperty("COSMOS.DEFAULT_SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_TIME_IN_MILLISECONDS", "500");

        CosmosAsyncClient dummyClient = null;
        try {
            dummyClient = this.getClientBuilder().buildAsyncClient();

            this.preferredRegionList = getPreferredRegionList(dummyClient);
            this.regions = this.preferredRegionList;
            this.clientWithPreferredRegions =
                this.getClientBuilder()
                    .contentResponseOnWriteEnabled(true)
                    .preferredRegions(this.preferredRegionList)
                    .multipleWriteRegionsEnabled(true)
                    .buildAsyncClient();

            this.cosmosAsyncContainer = getSharedSinglePartitionCosmosContainer(this.clientWithPreferredRegions);
        } finally {
            safeClose(dummyClient);
        }
    }

    @Test(groups = {"multi-master"}, dataProvider = "faultInjectionArgProvider", timeOut = TIMEOUT*100)
    public void testThresholdAvailabilityStrategy(
        OperationType operationType,
        FaultInjectionOperationType faultInjectionOperationType,
        boolean ignore) throws InterruptedException {

        if (this.preferredRegionList.size() <= 1) {
            throw new SkipException("excludeRegionTest_SkipFirstPreferredRegion can only be tested for multi-master with multi-regions");
        }

        ConnectionMode connectionMode = getClientBuilder().buildConnectionPolicy().getConnectionMode();
        FaultInjectionConnectionType faultInjectionConnectionType = connectionMode == ConnectionMode.DIRECT ?
            FaultInjectionConnectionType.DIRECT :
            FaultInjectionConnectionType.GATEWAY;

        TestObject createdItem = TestObject.create();
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        this.cosmosAsyncContainer.createItem(createdItem).block();

        // To run the test against the read all and read many variations of query
        int maxIterations = (operationType == OperationType.Query) ? 3 : 1;
        QueryFlavor[] queryFlavors = QueryFlavor.values();

        for (int i = 0; i < maxIterations; i++) {

            FaultInjectionRule rule = injectFailure(this.cosmosAsyncContainer, faultInjectionConnectionType);
            QueryFlavor queryFlavor = (operationType == OperationType.Query) ? queryFlavors[i] : null;

            try {
                // This is to wait for the item to be replicated to the secondary region
                Thread.sleep(2000);
                CosmosDiagnostics cosmosDiagnostics = performDocumentOperation(this.cosmosAsyncContainer, operationType, createdItem, options, false, queryFlavor);
                assertThat(cosmosDiagnostics).isNotNull();
                CosmosDiagnosticsContext diagnosticsContext = cosmosDiagnostics.getDiagnosticsContext();
                assertThat(diagnosticsContext).isNotNull();
                assertThat(diagnosticsContext.getContactedRegionNames().size()).isBetween(1, 2);
                assertThat(diagnosticsContext.getStatusCode()).isBetween(200, 204);
                assertThat(diagnosticsContext.getDuration()).isLessThanOrEqualTo(Duration.ofSeconds(3));

                // asserts response is obtained from the second preferred region
                assertThat(diagnosticsContext.getContactedRegionNames()).contains(regions.get(1).toLowerCase(Locale.ROOT));

            } catch (RuntimeException e) {
                fail("Operation should have succeeded from the second preferred region.", e);
            } finally {
                rule.disable();
            }
        }
    }

    @Test(groups = {"multi-region"}, dataProvider = "faultInjectionArgProvider", timeOut = TIMEOUT*100)
    public void testThresholdAvailabilityStrategyForReadsDefaultEnablementWithPpaf(
        OperationType operationType,
        FaultInjectionOperationType faultInjectionOperationType,
        boolean shouldPpafEnforcedReadAvailabilityStrategyBeEnforced) {

        if (this.preferredRegionList.size() <= 1) {
            throw new SkipException("excludeRegionTest_SkipFirstPreferredRegion can only be tested for multi-master with multi-regions");
        }

        if (faultInjectionOperationType != FaultInjectionOperationType.READ_ITEM && faultInjectionOperationType != FaultInjectionOperationType.QUERY_ITEM) {
            throw new SkipException("testThresholdAvailabilityStrategyForReadsDefaultEnablementWithPpaf only supported for READ and QUERY operations");
        }

        ConnectionMode connectionMode = getClientBuilder().buildConnectionPolicy().getConnectionMode();
        FaultInjectionConnectionType faultInjectionConnectionType = connectionMode == ConnectionMode.DIRECT ?
            FaultInjectionConnectionType.DIRECT :
            FaultInjectionConnectionType.GATEWAY;

        CosmosAsyncClient cosmosAsyncClient;
        FaultInjectionRule rule = null;

        // To run the test against the read all and read many variations of query
        int maxIterations = (operationType == OperationType.Query) ? 3 : 1;
        QueryFlavor[] queryFlavors = QueryFlavor.values();

        for (int i = 0; i < maxIterations; i++) {

            System.setProperty("COSMOS.IS_PER_PARTITION_AUTOMATIC_FAILOVER_ENABLED", "true");

            // test opt out behavior - opt in behavior is the default
            if (!shouldPpafEnforcedReadAvailabilityStrategyBeEnforced) {
                System.setProperty("COSMOS.IS_READ_AVAILABILITY_STRATEGY_ENABLED_WITH_PPAF", "false");
            }

            cosmosAsyncClient = getClientBuilder().preferredRegions(this.preferredRegionList).buildAsyncClient();

            QueryFlavor queryFlavor = (operationType == OperationType.Query) ? queryFlavors[i] : null;

            try {
                CosmosAsyncDatabase asyncDatabase = cosmosAsyncClient.getDatabase(this.cosmosAsyncContainer.getDatabase().getId());
                CosmosAsyncContainer asyncContainer = asyncDatabase.getContainer(this.cosmosAsyncContainer.getId());

                TestObject createdItem = TestObject.create();
                CosmosItemRequestOptions options = new CosmosItemRequestOptions();
                asyncContainer.createItem(createdItem).block();

                // This is to wait for the item to be replicated to the secondary region
                Thread.sleep(2000);
                rule = injectFailure(asyncContainer, faultInjectionConnectionType);

                Instant start = Instant.now();

                CosmosDiagnostics cosmosDiagnostics
                    = performDocumentOperation(asyncContainer, operationType, createdItem, options, true, queryFlavor);

                Instant end = Instant.now();

                assertThat(cosmosDiagnostics).isNotNull();
                CosmosDiagnosticsContext diagnosticsContext = cosmosDiagnostics.getDiagnosticsContext();
                assertThat(diagnosticsContext).isNotNull();
                assertThat(diagnosticsContext.getStatusCode()).isBetween(200, 204);

                if (!shouldPpafEnforcedReadAvailabilityStrategyBeEnforced) {

                    if (diagnosticsContext.isPointOperation()) {
                        assertThat(diagnosticsContext.getDuration()).isGreaterThanOrEqualTo(Duration.ofSeconds(30));
                    } else {
                        assertThat(Duration.between(start, end)).isGreaterThanOrEqualTo(Duration.ofSeconds(30));
                    }

                } else {
                    // Default enablement of PPAF-enforced read availability strategy should
                    // return a success ideally in 2s-3s
                    // keeping loose enough bounds to ensure test is not flaky
                    if (diagnosticsContext.isPointOperation()) {
                        assertThat(diagnosticsContext.getDuration()).isLessThanOrEqualTo(Duration.ofSeconds(10));
                    } else {
                        assertThat(Duration.between(start, end)).isLessThanOrEqualTo(Duration.ofSeconds(15));
                    }
                }

                // asserts response is obtained from the second preferred region
                assertThat(diagnosticsContext.getContactedRegionNames()).contains(regions.get(1).toLowerCase(Locale.ROOT));
            } catch (Exception e) {
                fail("Operation should have succeeded from the second preferred region.", e);
            } finally {

                System.clearProperty("COSMOS.IS_PER_PARTITION_AUTOMATIC_FAILOVER_ENABLED");
                System.clearProperty("COSMOS.IS_READ_AVAILABILITY_STRATEGY_ENABLED_WITH_PPAF");

                if (rule != null) {
                    rule.disable();
                }

                safeClose(cosmosAsyncClient);
            }
        }
    }

    @DataProvider(name = "faultInjectionArgProvider")
    public static Object[][] faultInjectionArgProvider() {
        return new Object[][] {
            // Operation type, Fault Injection Operation Type, Is PPAF-enforced read availability strategy enabled
            {OperationType.Read, FaultInjectionOperationType.READ_ITEM, true},
            {OperationType.Read, FaultInjectionOperationType.READ_ITEM, false},
            {OperationType.Replace, FaultInjectionOperationType.REPLACE_ITEM, false},
            {OperationType.Create, FaultInjectionOperationType.CREATE_ITEM, true},
            {OperationType.Delete, FaultInjectionOperationType.DELETE_ITEM, false},
            {OperationType.Query, FaultInjectionOperationType.QUERY_ITEM, true},
            {OperationType.Query, FaultInjectionOperationType.QUERY_ITEM, false},
            {OperationType.Patch, FaultInjectionOperationType.PATCH_ITEM, true}
        };
    }

    private FaultInjectionRule injectFailure(
        CosmosAsyncContainer container,
        FaultInjectionConnectionType faultInjectionConnectionType) {

        FaultInjectionServerErrorResultBuilder faultInjectionResultBuilder = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
            .delay(Duration.ofSeconds(10000))
            .times(10000);

        IFaultInjectionResult result = faultInjectionResultBuilder.build();
        logger.info("Injecting fault: {}", this.preferredRegionList.get(0));
        FaultInjectionCondition condition = new FaultInjectionConditionBuilder()
            .region(this.preferredRegionList.get(0))
            .connectionType(faultInjectionConnectionType)
            .build();

        FaultInjectionRule rule = new FaultInjectionRuleBuilder("InjectedResponseDelay")
            .condition(condition)
            .result(result)
            .build();

        FaultInjectorProvider injectorProvider = (FaultInjectorProvider) container
            .getOrConfigureFaultInjectorProvider(() -> new FaultInjectorProvider(container));

        injectorProvider.configureFaultInjectionRules(Arrays.asList(rule)).block();

        return rule;
    }

    private EndToEndTimeOutValidationTests.TestObject getDocumentDefinition(String documentId, String partitionKey) {
        // Doing NUM_DOCUMENTS/2 just to ensure there will be good number of repetetions for int value.
        int randInt = random.nextInt(DEFAULT_NUM_DOCUMENTS / 2);

        EndToEndTimeOutValidationTests.TestObject doc = new EndToEndTimeOutValidationTests.TestObject(documentId, "name" + randInt, randInt, partitionKey);
        return doc;
    }

    @AfterClass(groups = {"multi-master", "multi-region"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(this.clientWithPreferredRegions);
        System.clearProperty("COSMOS.DEFAULT_SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_TIME_IN_MILLISECONDS");
        System.clearProperty("COSMOS.DEFAULT_SESSION_TOKEN_MISMATCH_WAIT_TIME_IN_MILLISECONDS");
        System.clearProperty("COSMOS.IS_PER_PARTITION_AUTOMATIC_FAILOVER_ENABLED");
        System.clearProperty("COSMOS.IS_READ_AVAILABILITY_STRATEGY_ENABLED_WITH_PPAF");
    }

    private CosmosDiagnostics performDocumentOperation(
        CosmosAsyncContainer cosmosAsyncContainer,
        OperationType operationType,
        TestObject createdItem,
        CosmosItemRequestOptions cosmosItemRequestOptions,
        boolean ignoreE2E2LatencyCfgOnRequestOptions,
        QueryFlavor queryFlavor) {

        CosmosEndToEndOperationLatencyPolicyConfig config = null;

        if (!ignoreE2E2LatencyCfgOnRequestOptions) {
            config = new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(4))
                .availabilityStrategy(new ThresholdBasedAvailabilityStrategy(Duration.ofMillis(100), Duration.ofMillis(200)))
                .build();
            cosmosItemRequestOptions.setCosmosEndToEndOperationLatencyPolicyConfig(config);
            cosmosItemRequestOptions.setNonIdempotentWriteRetryPolicy(true, true);
        }

        if (operationType == OperationType.Query) {

            CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
            queryRequestOptions.setCosmosEndToEndOperationLatencyPolicyConfig(config);

            if (queryFlavor == QueryFlavor.ReadAll) {

                logger.info("Running readAllItems...");

                FeedResponse<TestObject> response = cosmosAsyncContainer
                    .readAllItems(queryRequestOptions, TestObject.class)
                    .byPage()
                    .blockFirst();

                assertThat(response).isNotNull();

                return response.getCosmosDiagnostics();
            }

            if (queryFlavor == QueryFlavor.ReadMany) {

                CosmosReadManyRequestOptions readManyRequestOptions = new CosmosReadManyRequestOptions();
                readManyRequestOptions.setCosmosEndToEndOperationLatencyPolicyConfig(config);

                logger.info("Running readMany...");

                FeedResponse<TestObject> response = cosmosAsyncContainer
                    .readMany(
                        Arrays.asList(new CosmosItemIdentity(new PartitionKey(createdItem.getMypk()), createdItem.getId())),
                        readManyRequestOptions,
                        TestObject.class)
                    .block();

                assertThat(response).isNotNull();
                return response.getCosmosDiagnostics();
            }

            logger.info("Running query ...");

            String query = String.format("SELECT * from c where c.id = '%s'", createdItem.getId());
            FeedResponse<TestObject> itemFeedResponse =
                cosmosAsyncContainer.queryItems(query, queryRequestOptions, TestObject.class).byPage().blockFirst();

            assertThat(itemFeedResponse).isNotNull();

            return itemFeedResponse.getCosmosDiagnostics();
        }

        if (operationType == OperationType.Read
            || operationType == OperationType.Delete
            || operationType == OperationType.Replace
            || operationType == OperationType.Create
            || operationType == OperationType.Patch
            || operationType == OperationType.Upsert) {

            if (operationType == OperationType.Read) {

                CosmosItemResponse<TestObject> response = cosmosAsyncContainer.readItem(
                        createdItem.getId(),
                        new PartitionKey(createdItem.getMypk()),
                        cosmosItemRequestOptions,
                        TestObject.class)
                    .block();

                assertThat(response).isNotNull();

                return response.getDiagnostics();
            }

            if (operationType == OperationType.Replace) {
                CosmosItemResponse<TestObject> response = cosmosAsyncContainer.replaceItem(
                        createdItem,
                        createdItem.getId(),
                        new PartitionKey(createdItem.getMypk()),
                        cosmosItemRequestOptions)
                    .block();

                assertThat(response).isNotNull();

                return response.getDiagnostics();
            }

            if (operationType == OperationType.Delete) {
                TestObject toBeDeletedItem = TestObject.create();
                cosmosAsyncContainer.createItem(toBeDeletedItem, cosmosItemRequestOptions).block();
                CosmosItemResponse<Object> response = cosmosAsyncContainer
                    .deleteItem(toBeDeletedItem, cosmosItemRequestOptions)
                    .block();

                assertThat(response).isNotNull();

                return response.getDiagnostics();
            }

            if (operationType == OperationType.Create) {
                CosmosItemResponse<TestObject> response = cosmosAsyncContainer
                    .createItem(TestObject.create(), cosmosItemRequestOptions)
                    .block();

                assertThat(response).isNotNull();

                return response.getDiagnostics();
            }

            if (operationType == OperationType.Upsert) {
                CosmosItemResponse<TestObject> response = cosmosAsyncContainer
                    .upsertItem(TestObject.create(), cosmosItemRequestOptions)
                    .block();

                assertThat(response).isNotNull();

                return response.getDiagnostics();
            }

            if (operationType == OperationType.Patch) {
                CosmosPatchOperations patchOperations =
                    CosmosPatchOperations
                        .create()
                        .add("/newPath", "newPath");

                CosmosPatchItemRequestOptions patchItemRequestOptions = new CosmosPatchItemRequestOptions();
                patchItemRequestOptions.setNonIdempotentWriteRetryPolicy(true, true);
                patchItemRequestOptions.setCosmosEndToEndOperationLatencyPolicyConfig(config);
                CosmosItemResponse<TestObject> response = cosmosAsyncContainer
                    .patchItem(createdItem.getId(), new PartitionKey(createdItem.getMypk()), patchOperations, patchItemRequestOptions, TestObject.class)
                    .block();

                assertThat(response).isNotNull();

                return response.getDiagnostics();
            }
        }

        throw new IllegalArgumentException("The operation type is not supported");
    }

    private List<String> getPreferredRegionList(CosmosAsyncClient client) {
        assertThat(client).isNotNull();

        AsyncDocumentClient asyncDocumentClient = ReflectionUtils.getAsyncDocumentClient(client);
        RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncDocumentClient;
        GlobalEndpointManager globalEndpointManager =
            ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
        DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();

        Iterator<DatabaseAccountLocation> locationIterator = databaseAccount.getWritableLocations().iterator();
        List<String> preferredRegionList = new ArrayList<>();

        while (locationIterator.hasNext()) {
            DatabaseAccountLocation accountLocation = locationIterator.next();
            preferredRegionList.add(accountLocation.getName().toLowerCase());
        }

        return preferredRegionList;
    }

    private enum QueryFlavor {
        Query, ReadAll, ReadMany
    }
}
