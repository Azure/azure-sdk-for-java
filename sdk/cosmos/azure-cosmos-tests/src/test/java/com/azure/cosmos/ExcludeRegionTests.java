// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.CosmosItemRequestOptions;
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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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

    @BeforeClass(groups = {"multi-master"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        CosmosAsyncClient dummyClient = null;
        try {
            dummyClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .directMode()
                .buildAsyncClient();

            this.preferredRegionList = this.getPreferredRegionList(dummyClient);
            this.clientWithPreferredRegions =
                new CosmosClientBuilder()
                    .endpoint(TestConfigurations.HOST)
                    .key(TestConfigurations.MASTER_KEY)
                    .contentResponseOnWriteEnabled(true)
                    .preferredRegions(this.preferredRegionList)
                    .buildAsyncClient();

            this.cosmosAsyncContainer = getSharedSinglePartitionCosmosContainer(this.clientWithPreferredRegions);
        } finally {
            safeClose(dummyClient);
        }
    }

    @AfterClass(groups = {"multi-master"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(this.clientWithPreferredRegions);
    }

    @DataProvider(name = "operationTypeArgProvider")
    public static Object[][] operationTypeArgProvider() {
        return new Object[][]{
            { OperationType.Read },
            { OperationType.Replace },
            { OperationType.Create },
            { OperationType.Delete },
            { OperationType.Query },
            { OperationType.Patch }
        };
    }

    @DataProvider(name = "faultInjectionArgProvider")
    public static Object[][] faultInjectionArgProvider() {
        return new Object[][]{
            { OperationType.Read, FaultInjectionOperationType.READ_ITEM },
            { OperationType.Replace, FaultInjectionOperationType.REPLACE_ITEM },
            { OperationType.Create, FaultInjectionOperationType.CREATE_ITEM },
            { OperationType.Delete, FaultInjectionOperationType.DELETE_ITEM },
            { OperationType.Query, FaultInjectionOperationType.QUERY_ITEM },
            { OperationType.Patch, FaultInjectionOperationType.PATCH_ITEM }
        };
    }

    @Test(groups = {"multi-master"}, dataProvider = "operationTypeArgProvider", timeOut = TIMEOUT)
    public void excludeRegionTest_SkipFirstPreferredRegion(OperationType operationType) {
        TestItem createdItem = TestItem.createNewItem();
        this.cosmosAsyncContainer.createItem(createdItem).block();

        CosmosDiagnostics cosmosDiagnostics = this.performDocumentOperation(cosmosAsyncContainer, operationType, createdItem, null);
        assertThat(cosmosDiagnostics.getContactedRegionNames().size()).isEqualTo(1);
        assertThat(cosmosDiagnostics.getContactedRegionNames()).containsAll(this.preferredRegionList.subList(0, 1));

        // now exclude the first preferred region
        cosmosDiagnostics =
            this.performDocumentOperation(
                cosmosAsyncContainer,
                operationType,
                createdItem,
                Arrays.asList(this.preferredRegionList.get(0)));

        assertThat(cosmosDiagnostics.getContactedRegionNames().size()).isEqualTo(1);
        assertThat(cosmosDiagnostics.getContactedRegionNames()).containsAll(this.preferredRegionList.subList(1, 2));
    }

    @Test(groups = {"multi-master"}, dataProvider = "faultInjectionArgProvider", timeOut = TIMEOUT)
    public void excludeRegionTest_readSessionNotAvailable(
        OperationType operationType,
        FaultInjectionOperationType faultInjectionOperationType) {
        TestItem createdItem = TestItem.createNewItem();
        this.cosmosAsyncContainer.createItem(createdItem).block();

        // TODO: reduce the max wait time
        // Configure 404/1002 fault injection rule, validate the request should succeed in other region
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

        CosmosFaultInjectionHelper.configureFaultInjectionRules(this.cosmosAsyncContainer, Arrays.asList(serverErrorRule)).block();
        try {
            CosmosDiagnostics cosmosDiagnostics = this.performDocumentOperation(cosmosAsyncContainer, operationType, createdItem, null);
            assertThat(cosmosDiagnostics.getContactedRegionNames().size()).isEqualTo(2);
            assertThat(cosmosDiagnostics.getContactedRegionNames().containsAll(this.preferredRegionList.subList(0, 2)));
        } catch (CosmosException e) {
            fail("Request should succeeded in other regions");
        }

        // now exclude all the other regions except the first preferred region
        try {
            CosmosDiagnostics cosmosDiagnostics = this.performDocumentOperation(
                cosmosAsyncContainer,
                operationType,
                createdItem,
                this.preferredRegionList.subList(1, this.preferredRegionList.size()));

            fail("Request should have failed");
        } catch (CosmosException exception) {
            CosmosDiagnostics cosmosDiagnostics = exception.getDiagnostics();
            assertThat(cosmosDiagnostics.getContactedRegionNames().size()).isEqualTo(1);
            assertThat(cosmosDiagnostics.getContactedRegionNames().containsAll(this.preferredRegionList.subList(0, 1)));
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
            preferredRegionList.add(accountLocation.getName());
        }

        return preferredRegionList;
    }

    private CosmosDiagnostics performDocumentOperation(
        CosmosAsyncContainer cosmosAsyncContainer,
        OperationType operationType,
        TestItem createdItem,
        List<String> excludeRegions) {
        if (operationType == OperationType.Query) {
            CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
            String query = String.format("SELECT * from c where c.id = '%s'", createdItem.getId());
            queryRequestOptions.setExcludedRegions(excludeRegions);
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

            CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
            cosmosItemRequestOptions.setExcludedRegions(excludeRegions);

            if (operationType == OperationType.Read) {

                return cosmosAsyncContainer.readItem(
                    createdItem.getId(),
                    new PartitionKey(createdItem.getId()),
                    cosmosItemRequestOptions,
                    TestItem.class).block().getDiagnostics();
            }

            if (operationType == OperationType.Replace) {
                return cosmosAsyncContainer.replaceItem(
                    createdItem,
                    createdItem.getId(),
                    new PartitionKey(createdItem.getId()),
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
                        .add("newPath", "newPath");

                CosmosPatchItemRequestOptions patchItemRequestOptions = new CosmosPatchItemRequestOptions();
                patchItemRequestOptions.setExcludedRegions(excludeRegions);
                return cosmosAsyncContainer
                    .patchItem(createdItem.getId(), new PartitionKey(createdItem.getId()), patchOperations, patchItemRequestOptions, TestItem.class)
                    .block().getDiagnostics();
            }
        }

        throw new IllegalArgumentException("The operation type is not supported");
    }
}
