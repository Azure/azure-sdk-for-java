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
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchRequestOptions;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ExcludeRegionTests extends TestSuiteBase {
    private static final int TIMEOUT = 60000;
    private CosmosAsyncClient clientWithPreferredRegions;
    private CosmosAsyncContainer cosmosAsyncContainer;
    private List<String> preferredRegionList;

    private static final CosmosEndToEndOperationLatencyPolicyConfig INF_E2E_TIMEOUT
        = new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofDays(100)).build();

    @Factory(dataProvider = "clientBuildersWithSessionConsistency")
    public ExcludeRegionTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"multi-master"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        System.setProperty("COSMOS.DEFAULT_SESSION_TOKEN_MISMATCH_WAIT_TIME_IN_MILLISECONDS", "1000");
        System.setProperty("COSMOS.DEFAULT_SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_TIME_IN_MILLISECONDS", "500");

        CosmosAsyncClient dummyClient = null;
        try {
            dummyClient = this.getClientBuilder().buildAsyncClient();

            this.preferredRegionList = this.getPreferredRegionList(dummyClient);
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

    @AfterClass(groups = {"multi-master"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(this.clientWithPreferredRegions);
        System.clearProperty("COSMOS.DEFAULT_SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_TIME_IN_MILLISECONDS");
        System.clearProperty("COSMOS.DEFAULT_SESSION_TOKEN_MISMATCH_WAIT_TIME_IN_MILLISECONDS");
    }

    @DataProvider(name = "operationTypeArgProvider")
    public static Object[][] operationTypeArgProvider() {
        return new Object[][]{
            { OperationType.Read },
            { OperationType.Replace },
            { OperationType.Create },
            { OperationType.Delete },
            { OperationType.Query },
            { OperationType.Patch },
            { OperationType.Batch }
        };
    }

    @DataProvider(name = "faultInjectionArgProvider")
    public static Object[][] faultInjectionArgProvider() {
        return new Object[][] {
            { OperationType.Read, FaultInjectionOperationType.READ_ITEM },
            { OperationType.Replace, FaultInjectionOperationType.REPLACE_ITEM },
            { OperationType.Create, FaultInjectionOperationType.CREATE_ITEM },
            { OperationType.Delete, FaultInjectionOperationType.DELETE_ITEM },
            { OperationType.Query, FaultInjectionOperationType.QUERY_ITEM },
            { OperationType.Patch, FaultInjectionOperationType.PATCH_ITEM },
            { OperationType.Batch, FaultInjectionOperationType.BATCH_ITEM }
        };
    }

    @Test(groups = {"multi-master"}, dataProvider = "operationTypeArgProvider", timeOut = TIMEOUT)
    public void excludeRegionTest_SkipFirstPreferredRegion(OperationType operationType) throws InterruptedException {

        if (this.preferredRegionList.size() <= 1) {
            throw new SkipException("excludeRegionTest_SkipFirstPreferredRegion can only be tested for multi-master with multi-regions");
        }

        TestObject createdItem = TestObject.create();
        this.cosmosAsyncContainer.createItem(createdItem).block();

        Thread.sleep(1000);

        CosmosDiagnosticsContext cosmosDiagnosticsContextBeforeRegionExclusion
            = this.performDocumentOperation(cosmosAsyncContainer, operationType, createdItem, null, INF_E2E_TIMEOUT);

        validateRegionsContacted(cosmosDiagnosticsContextBeforeRegionExclusion, this.preferredRegionList.subList(0, 1));

        // now exclude the first preferred region
        CosmosDiagnosticsContext cosmosDiagnosticsContextPostRegionExclusion =
            this.performDocumentOperation(
                cosmosAsyncContainer,
                operationType,
                createdItem,
                Arrays.asList(this.preferredRegionList.get(0)),
                INF_E2E_TIMEOUT);

        validateRegionsContacted(cosmosDiagnosticsContextPostRegionExclusion, this.preferredRegionList.subList(1, 2));
    }

    @Test(groups = {"multi-master"}, dataProvider = "faultInjectionArgProvider", timeOut = TIMEOUT)
    public void excludeRegionTest_readSessionNotAvailable(
        OperationType operationType,
        FaultInjectionOperationType faultInjectionOperationType) {

        if (this.clientWithPreferredRegions.getConnectionPolicy().getConnectionMode() != ConnectionMode.DIRECT) {
            throw new SkipException("Fault injection can only be applied for direct model.");
        }

        if (this.preferredRegionList.size() <= 1) {
            throw new SkipException("excludeRegionTest_SkipFirstPreferredRegion can only be tested for multi-master with multi-regions");
        }

        TestObject createdItem = TestObject.create();
        this.cosmosAsyncContainer.createItem(createdItem).block();

        FaultInjectionRule serverErrorRule = new FaultInjectionRuleBuilder("excludeRegionTest-" + operationType)
            .condition(
                new FaultInjectionConditionBuilder()
                    .region(this.preferredRegionList.get(0))
                    .operationType(faultInjectionOperationType)
                    .build())
            .result(
                FaultInjectionResultBuilders
                    .getResultBuilder(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                    .build()
            ).build();

        try {
            CosmosFaultInjectionHelper.configureFaultInjectionRules(this.cosmosAsyncContainer, Arrays.asList(serverErrorRule)).block();
            try {
                CosmosDiagnosticsContext cosmosDiagnosticsContextBeforeRegionExclusion = this.performDocumentOperation(cosmosAsyncContainer, operationType, createdItem, null, INF_E2E_TIMEOUT);
                validateRegionsContacted(cosmosDiagnosticsContextBeforeRegionExclusion, this.preferredRegionList.subList(0, 2));
            } catch (CosmosException e) {
                fail("Request should succeeded in other regions");
            }

            // now exclude all the other regions except the first preferred region
            try {
                this.performDocumentOperation(
                    cosmosAsyncContainer,
                    operationType,
                    createdItem,
                    this.preferredRegionList.subList(1, this.preferredRegionList.size()),
                    INF_E2E_TIMEOUT);

                fail("Request should have failed");
            } catch (CosmosException exception) {
                CosmosDiagnostics cosmosDiagnostics = exception.getDiagnostics();

                assertThat(cosmosDiagnostics).isNotNull();

                CosmosDiagnosticsContext cosmosDiagnosticsContextPostRegionExclusion
                    = cosmosDiagnostics.getDiagnosticsContext();

                validateRegionsContacted(cosmosDiagnosticsContextPostRegionExclusion, this.preferredRegionList.subList(0, 1));
            }
        } finally {
            serverErrorRule.disable();
        }
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

    private CosmosDiagnosticsContext performDocumentOperation(
        CosmosAsyncContainer cosmosAsyncContainer,
        OperationType operationType,
        TestObject createdItem,
        List<String> excludeRegions,
        CosmosEndToEndOperationLatencyPolicyConfig cosmosEndToEndOperationLatencyPolicyConfig) {

        if (operationType == OperationType.Query) {
            CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();

            queryRequestOptions.setCosmosEndToEndOperationLatencyPolicyConfig(cosmosEndToEndOperationLatencyPolicyConfig);

            String query = String.format("SELECT * from c where c.id = '%s'", createdItem.getId());
            queryRequestOptions.setExcludedRegions(excludeRegions);
            FeedResponse<TestObject> itemFeedResponse =
                cosmosAsyncContainer.queryItems(query, queryRequestOptions, TestObject.class).byPage().blockFirst();

            assertThat(itemFeedResponse).isNotNull();
            CosmosDiagnostics cosmosDiagnostics = itemFeedResponse.getCosmosDiagnostics();

            assertThat(cosmosDiagnostics).isNotNull();

            return cosmosDiagnostics.getDiagnosticsContext();
        }

        if (operationType == OperationType.Read
            || operationType == OperationType.Delete
            || operationType == OperationType.Replace
            || operationType == OperationType.Create
            || operationType == OperationType.Patch
            || operationType == OperationType.Upsert) {

            CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();

            cosmosItemRequestOptions.setCosmosEndToEndOperationLatencyPolicyConfig(cosmosEndToEndOperationLatencyPolicyConfig);
            cosmosItemRequestOptions.setExcludedRegions(excludeRegions);

            if (operationType == OperationType.Read) {

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                CosmosItemResponse<TestObject> itemResponse = cosmosAsyncContainer.readItem(
                    createdItem.getId(),
                    new PartitionKey(createdItem.getMypk()),
                    cosmosItemRequestOptions,
                    TestObject.class).block();

                assertThat(itemResponse).isNotNull();
                assertThat(itemResponse.getDiagnostics()).isNotNull();

                CosmosDiagnostics cosmosDiagnostics = itemResponse.getDiagnostics();

                assertThat(cosmosDiagnostics).isNotNull();

                return cosmosDiagnostics.getDiagnosticsContext();
            }

            if (operationType == OperationType.Replace) {

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                CosmosItemResponse<TestObject> itemResponse = cosmosAsyncContainer.replaceItem(
                    createdItem,
                    createdItem.getId(),
                    new PartitionKey(createdItem.getMypk()),
                    cosmosItemRequestOptions).block();

                assertThat(itemResponse).isNotNull();
                assertThat(itemResponse.getDiagnostics()).isNotNull();

                CosmosDiagnostics cosmosDiagnostics = itemResponse.getDiagnostics();

                assertThat(cosmosDiagnostics).isNotNull();

                return cosmosDiagnostics.getDiagnosticsContext();
            }

            if (operationType == OperationType.Delete) {

                TestObject itemToBeDeleted = TestObject.create();

                cosmosAsyncContainer.createItem(itemToBeDeleted, cosmosItemRequestOptions).block();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                CosmosItemResponse<Object> itemResponse
                    = cosmosAsyncContainer.deleteItem(itemToBeDeleted, cosmosItemRequestOptions).block();

                assertThat(itemResponse).isNotNull();
                assertThat(itemResponse.getDiagnostics()).isNotNull();

                CosmosDiagnostics cosmosDiagnostics = itemResponse.getDiagnostics();

                assertThat(cosmosDiagnostics).isNotNull();

                return cosmosDiagnostics.getDiagnosticsContext();
            }

            if (operationType == OperationType.Create) {
                CosmosItemResponse<TestObject> itemResponse = cosmosAsyncContainer
                    .createItem(TestObject.create(), cosmosItemRequestOptions).block();

                assertThat(itemResponse).isNotNull();
                assertThat(itemResponse.getDiagnostics()).isNotNull();

                CosmosDiagnostics cosmosDiagnostics = itemResponse.getDiagnostics();

                assertThat(cosmosDiagnostics).isNotNull();

                return cosmosDiagnostics.getDiagnosticsContext();
            }

            if (operationType == OperationType.Upsert) {
                CosmosItemResponse<TestObject> itemResponse
                    = cosmosAsyncContainer.upsertItem(TestObject.create(), cosmosItemRequestOptions).block();

                assertThat(itemResponse).isNotNull();
                assertThat(itemResponse.getDiagnostics()).isNotNull();

                CosmosDiagnostics cosmosDiagnostics = itemResponse.getDiagnostics();

                assertThat(cosmosDiagnostics).isNotNull();

                return cosmosDiagnostics.getDiagnosticsContext();
            }

            if (operationType == OperationType.Patch) {
                CosmosPatchOperations patchOperations =
                    CosmosPatchOperations
                        .create()
                        .add("/newPath", "newPath");

                CosmosPatchItemRequestOptions patchItemRequestOptions = new CosmosPatchItemRequestOptions();

                patchItemRequestOptions.setCosmosEndToEndOperationLatencyPolicyConfig(cosmosEndToEndOperationLatencyPolicyConfig);
                patchItemRequestOptions.setExcludedRegions(excludeRegions);

                CosmosItemResponse<TestObject> itemResponse = cosmosAsyncContainer
                    .patchItem(createdItem.getId(), new PartitionKey(createdItem.getMypk()), patchOperations, patchItemRequestOptions, TestObject.class)
                    .block();

                assertThat(itemResponse).isNotNull();
                assertThat(itemResponse.getDiagnostics()).isNotNull();

                CosmosDiagnostics cosmosDiagnostics = itemResponse.getDiagnostics();

                assertThat(cosmosDiagnostics).isNotNull();

                return cosmosDiagnostics.getDiagnosticsContext();
            }
        }

        if (operationType == OperationType.Batch) {

            CosmosBatchRequestOptions cosmosBatchRequestOptions = new CosmosBatchRequestOptions();

            cosmosBatchRequestOptions.setExcludedRegions(excludeRegions);

            TestObject testItem = TestObject.create();
            PartitionKey partitionKey = new PartitionKey(testItem.getMypk());

            CosmosBatch cosmosBatch = CosmosBatch.createCosmosBatch(partitionKey);
            cosmosBatch.createItemOperation(testItem);

            CosmosBatchResponse cosmosBatchResponse
                = cosmosAsyncContainer.executeCosmosBatch(cosmosBatch, cosmosBatchRequestOptions).block();

            assertThat(cosmosBatchResponse).isNotNull();
            assertThat(cosmosBatchResponse.getDiagnostics()).isNotNull();

            CosmosDiagnostics cosmosDiagnostics = cosmosBatchResponse.getDiagnostics();

            assertThat(cosmosDiagnostics).isNotNull();

            return cosmosDiagnostics.getDiagnosticsContext();
        }

        throw new IllegalArgumentException("The operation type is not supported");
    }

    private static void validateRegionsContacted(CosmosDiagnosticsContext diagnosticsContext, List<String> expectedRegionsContacted) {
        assertThat(diagnosticsContext).isNotNull();
        assertThat(diagnosticsContext.getContactedRegionNames()).isNotNull();
        assertThat(diagnosticsContext.getContactedRegionNames().size()).isEqualTo(expectedRegionsContacted.size());
        assertThat(diagnosticsContext.getContactedRegionNames()).containsAll(expectedRegionsContacted);
    }
}
