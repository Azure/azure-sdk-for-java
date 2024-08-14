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
import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import static com.azure.cosmos.CosmosDiagnostics.OBJECT_MAPPER;
import static org.assertj.core.api.Assertions.assertThat;

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

    @BeforeClass(groups = {"multi-master", "multi-master-circuit-breaker"}, timeOut = SETUP_TIMEOUT * 100)
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

    @Test(groups = {"multi-master", "multi-master-circuit-breaker"}, dataProvider = "faultInjectionArgProvider", timeOut = TIMEOUT*100)
    public void testThresholdAvailabilityStrategy(OperationType operationType, FaultInjectionOperationType faultInjectionOperationType) throws InterruptedException {
        if (this.preferredRegionList.size() <= 1) {
            throw new SkipException("excludeRegionTest_SkipFirstPreferredRegion can only be tested for multi-master with multi-regions");
        }

        if (getClientBuilder().buildConnectionPolicy().getConnectionMode() != ConnectionMode.DIRECT) {
            throw new SkipException("Failure injection only supported for DIRECT mode");
        }

        TestItem createdItem = TestItem.createNewItem();
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        this.cosmosAsyncContainer.createItem(createdItem).block();

        // This is to wait for the item to be replicated to the secondary region
        Thread.sleep(2000);
        FaultInjectionRule rule = injectFailure(cosmosAsyncContainer, faultInjectionOperationType);
        CosmosDiagnostics cosmosDiagnostics = performDocumentOperation(cosmosAsyncContainer, operationType, createdItem, options);
        assertThat(cosmosDiagnostics).isNotNull();
        CosmosDiagnosticsContext diagnosticsContext = cosmosDiagnostics.getDiagnosticsContext();
        assertThat(diagnosticsContext).isNotNull();
        assertThat(diagnosticsContext.getContactedRegionNames().size()).isGreaterThan(1);
        ObjectNode diagnosticsNode;
        try {
            if (operationType == OperationType.Query) {
                assertThat(cosmosDiagnostics.getClientSideRequestStatistics().iterator().next().getResponseStatisticsList().iterator().next().getRegionName())
                    .isEqualTo(regions.get(1).toLowerCase(Locale.ROOT));
            } else {
                diagnosticsNode = (ObjectNode) OBJECT_MAPPER.readTree(cosmosDiagnostics.toString());
                assertResponseFromSpeculatedRegion(diagnosticsNode);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } finally {
            rule.disable();
        }
    }

    @DataProvider(name = "faultInjectionArgProvider")
    public static Object[][] faultInjectionArgProvider() {
        return new Object[][] {
            {OperationType.Read, FaultInjectionOperationType.READ_ITEM},
            {OperationType.Replace, FaultInjectionOperationType.REPLACE_ITEM},
            {OperationType.Create, FaultInjectionOperationType.CREATE_ITEM},
            {OperationType.Delete, FaultInjectionOperationType.DELETE_ITEM},
            {OperationType.Query, FaultInjectionOperationType.QUERY_ITEM},
            {OperationType.Patch, FaultInjectionOperationType.PATCH_ITEM}
        };
    }

    private void assertResponseFromSpeculatedRegion(ObjectNode diagnosticsNode) {
        JsonNode responseStatisticsList = diagnosticsNode.get("responseStatisticsList");
        assertThat(responseStatisticsList.isArray()).isTrue();
        assertThat(responseStatisticsList.size()).isGreaterThan(0);
        JsonNode storeResult = responseStatisticsList.get(0).get("storeResult");
        assertThat(storeResult.get("storePhysicalAddress").toString()).contains(StringUtils.deleteWhitespace(regions.get(1).toLowerCase(Locale.ROOT)));
    }

    private FaultInjectionRule injectFailure(
        CosmosAsyncContainer container,
        FaultInjectionOperationType operationType) {

        FaultInjectionServerErrorResultBuilder faultInjectionResultBuilder = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
            .delay(Duration.ofMillis(10000))
            .times(1);

        IFaultInjectionResult result = faultInjectionResultBuilder.build();
        logger.info("Injecting fault: {}", this.preferredRegionList.get(0));
        FaultInjectionCondition condition = new FaultInjectionConditionBuilder()
            .operationType(operationType)
            .region(this.preferredRegionList.get(0))
            .connectionType(FaultInjectionConnectionType.DIRECT)
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

    private List<EndToEndTimeOutValidationTests.TestObject> insertDocuments(int documentCount, List<String> partitionKeys, CosmosAsyncContainer container) {
        List<EndToEndTimeOutValidationTests.TestObject> documentsToInsert = new ArrayList<>();

        for (int i = 0; i < documentCount; i++) {
            documentsToInsert.add(
                getDocumentDefinition(
                    UUID.randomUUID().toString(),
                    partitionKeys == null ? UUID.randomUUID().toString() : partitionKeys.get(random.nextInt(partitionKeys.size()))));
        }

        List<EndToEndTimeOutValidationTests.TestObject> documentInserted = bulkInsertBlocking(container, documentsToInsert);

        waitIfNeededForReplicasToCatchUp(this.getClientBuilder());

        return documentInserted;
    }

    @AfterClass(groups = {"multi-master", "multi-master-circuit-breaker"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(this.clientWithPreferredRegions);
        System.clearProperty("COSMOS.DEFAULT_SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_TIME_IN_MILLISECONDS");
        System.clearProperty("COSMOS.DEFAULT_SESSION_TOKEN_MISMATCH_WAIT_TIME_IN_MILLISECONDS");
    }

    private CosmosDiagnostics performDocumentOperation(
        CosmosAsyncContainer cosmosAsyncContainer,
        OperationType operationType,
        TestItem createdItem,
        CosmosItemRequestOptions cosmosItemRequestOptions) {
        CosmosEndToEndOperationLatencyPolicyConfig config = new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(4))
            .availabilityStrategy(new ThresholdBasedAvailabilityStrategy(Duration.ofMillis(100), Duration.ofMillis(200)))
            .build();
        cosmosItemRequestOptions.setCosmosEndToEndOperationLatencyPolicyConfig(config);
        cosmosItemRequestOptions.setNonIdempotentWriteRetryPolicy(true, true);

        if (operationType == OperationType.Query) {
            CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
            queryRequestOptions.setCosmosEndToEndOperationLatencyPolicyConfig(config);
            String query = String.format("SELECT * from c where c.id = '%s'", createdItem.getId());
            FeedResponse<TestItem> itemFeedResponse =
                cosmosAsyncContainer.queryItems(query, queryRequestOptions, TestItem.class).byPage().blockFirst();

            return itemFeedResponse.getCosmosDiagnostics();
        }

        if (operationType == OperationType.Read
            || operationType == OperationType.Delete
            || operationType == OperationType.Replace
            || operationType == OperationType.Create
            || operationType == OperationType.Patch
            || operationType == OperationType.Upsert) {

            if (operationType == OperationType.Read) {

                return cosmosAsyncContainer.readItem(
                    createdItem.getId(),
                    new PartitionKey(createdItem.getMypk()),
                    cosmosItemRequestOptions,
                    TestItem.class).block().getDiagnostics();
            }

            if (operationType == OperationType.Replace) {
                return cosmosAsyncContainer.replaceItem(
                    createdItem,
                    createdItem.getId(),
                    new PartitionKey(createdItem.getMypk()),
                    cosmosItemRequestOptions).block().getDiagnostics();
            }

            if (operationType == OperationType.Delete) {
                TestItem toBeDeletedItem = TestItem.createNewItem();
                cosmosAsyncContainer.createItem(toBeDeletedItem).block();
                return cosmosAsyncContainer.deleteItem(toBeDeletedItem, cosmosItemRequestOptions).block().getDiagnostics();
            }

            if (operationType == OperationType.Create) {
                return cosmosAsyncContainer.createItem(TestItem.createNewItem(), cosmosItemRequestOptions).block().getDiagnostics();
            }

            if (operationType == OperationType.Upsert) {
                return cosmosAsyncContainer.upsertItem(TestItem.createNewItem(), cosmosItemRequestOptions).block().getDiagnostics();
            }

            if (operationType == OperationType.Patch) {
                CosmosPatchOperations patchOperations =
                    CosmosPatchOperations
                        .create()
                        .add("/newPath", "newPath");

                CosmosPatchItemRequestOptions patchItemRequestOptions = new CosmosPatchItemRequestOptions();
                patchItemRequestOptions.setNonIdempotentWriteRetryPolicy(true, true);
                patchItemRequestOptions.setCosmosEndToEndOperationLatencyPolicyConfig(config);
                return cosmosAsyncContainer
                    .patchItem(createdItem.getId(), new PartitionKey(createdItem.getMypk()), patchOperations, patchItemRequestOptions, TestItem.class)
                    .block().getDiagnostics();
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
}
