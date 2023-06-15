// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosSessionRetryOptionsBuilder;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.CosmosRegionSwitchHint;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionCondition;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorResult;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SessionRetryOptionsTests extends TestSuiteBase {

    private CosmosAsyncClient cosmosAsyncClient;
    private CosmosAsyncContainer cosmosAsyncContainer;
    private Map<String, String> writeRegionMap;

    @Factory(dataProvider = "clientBuilderSolelyDirectWithSessionConsistency")
    public SessionRetryOptionsTests(CosmosClientBuilder cosmosClientBuilder) {
        super(cosmosClientBuilder);
    }

    @BeforeClass(groups = {"multi-region", "multi-master"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        cosmosAsyncClient = getClientBuilder().buildAsyncClient();
        AsyncDocumentClient asyncDocumentClient = BridgeInternal.getContextClient(cosmosAsyncClient);
        GlobalEndpointManager globalEndpointManager = asyncDocumentClient.getGlobalEndpointManager();

        DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();
        this.cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(cosmosAsyncClient);
        this.writeRegionMap = this.getRegionMap(databaseAccount, true);
    }

    @DataProvider(name = "nonWriteOperationContextProvider")
    public Object[][] nonWriteOperationContextProvider() {
        return new Object[][]{
            {OperationType.Read, FaultInjectionOperationType.READ_ITEM, CosmosRegionSwitchHint.DIFFERENT_REGION_PREFERRED},
            {OperationType.Query, FaultInjectionOperationType.QUERY_ITEM, CosmosRegionSwitchHint.DIFFERENT_REGION_PREFERRED},
            {OperationType.Read, FaultInjectionOperationType.READ_ITEM, CosmosRegionSwitchHint.CURRENT_REGION_PREFERRED},
            {OperationType.Query, FaultInjectionOperationType.QUERY_ITEM, CosmosRegionSwitchHint.CURRENT_REGION_PREFERRED},
            {OperationType.Read, FaultInjectionOperationType.READ_ITEM, null},
            {OperationType.Query, FaultInjectionOperationType.QUERY_ITEM, null}
        };
    }

    @DataProvider(name = "writeOperationContextProvider")
    public Object[][] writeOperationContextProvider() {
        return new Object[][]{
            {OperationType.Create, FaultInjectionOperationType.CREATE_ITEM, CosmosRegionSwitchHint.DIFFERENT_REGION_PREFERRED},
            {OperationType.Replace, FaultInjectionOperationType.REPLACE_ITEM, CosmosRegionSwitchHint.DIFFERENT_REGION_PREFERRED},
            {OperationType.Delete, FaultInjectionOperationType.DELETE_ITEM, CosmosRegionSwitchHint.DIFFERENT_REGION_PREFERRED},
            {OperationType.Upsert, FaultInjectionOperationType.UPSERT_ITEM, CosmosRegionSwitchHint.DIFFERENT_REGION_PREFERRED},
            {OperationType.Patch, FaultInjectionOperationType.PATCH_ITEM, CosmosRegionSwitchHint.DIFFERENT_REGION_PREFERRED},
            {OperationType.Create, FaultInjectionOperationType.CREATE_ITEM, CosmosRegionSwitchHint.CURRENT_REGION_PREFERRED},
            {OperationType.Replace, FaultInjectionOperationType.REPLACE_ITEM, CosmosRegionSwitchHint.CURRENT_REGION_PREFERRED},
            {OperationType.Delete, FaultInjectionOperationType.DELETE_ITEM, CosmosRegionSwitchHint.CURRENT_REGION_PREFERRED},
            {OperationType.Upsert, FaultInjectionOperationType.UPSERT_ITEM, CosmosRegionSwitchHint.CURRENT_REGION_PREFERRED},
            {OperationType.Patch, FaultInjectionOperationType.PATCH_ITEM, CosmosRegionSwitchHint.CURRENT_REGION_PREFERRED},
            {OperationType.Create, FaultInjectionOperationType.CREATE_ITEM, null},
            {OperationType.Replace, FaultInjectionOperationType.REPLACE_ITEM, null},
            {OperationType.Delete, FaultInjectionOperationType.DELETE_ITEM, null},
            {OperationType.Upsert, FaultInjectionOperationType.UPSERT_ITEM, null},
            {OperationType.Patch, FaultInjectionOperationType.PATCH_ITEM, null},

        };
    }

    @Test(groups = {"multi-region"}, dataProvider = "nonWriteOperationContextProvider", timeOut = TIMEOUT)
    public void nonWriteOperation_WithReadSessionUnavailable_test(OperationType operationType, FaultInjectionOperationType faultInjectionOperationType, CosmosRegionSwitchHint regionSwitchHint) {
        List<String> preferredLocations = this.writeRegionMap.keySet().stream().collect(Collectors.toList());
        Duration sessionTokenMismatchDefaultWaitTime = Duration.ofMillis(Configs.getSessionTokenMismatchDefaultWaitTimeInMs());

        assertThat(preferredLocations).isNotNull();
        assertThat(preferredLocations.size()).isEqualTo(2);

        CosmosAsyncClient clientWithPreferredRegions = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .consistencyLevel(BridgeInternal.getContextClient(this.cosmosAsyncClient).getConsistencyLevel())
            .preferredRegions(preferredLocations)
            .sessionRetryOptions(new CosmosSessionRetryOptionsBuilder()
                .setRegionSwitchHint(regionSwitchHint)
                .build()
            )
            .directMode()
            .buildAsyncClient();

        CosmosAsyncContainer containerForClientWithPreferredRegions = clientWithPreferredRegions
            .getDatabase(this.cosmosAsyncContainer.getDatabase().getId())
            .getContainer(this.cosmosAsyncContainer.getId());

        TestItem createdItem = TestItem.createNewItem();
        containerForClientWithPreferredRegions.createItem(createdItem).block();

        FaultInjectionRuleBuilder badSessionTokenRuleBuilder = new FaultInjectionRuleBuilder("serverErrorRule-bad-session-token-" + UUID.randomUUID());

        FaultInjectionCondition faultInjectionConditionForReadsInPrimaryRegion = new FaultInjectionConditionBuilder()
            .operationType(faultInjectionOperationType)
            .connectionType(FaultInjectionConnectionType.DIRECT)
            .region(preferredLocations.get(0))
            .build();

        FaultInjectionServerErrorResult badSessionTokenServerErrorResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
            .build();

        FaultInjectionRule badSessionTokenRule = badSessionTokenRuleBuilder
            .condition(faultInjectionConditionForReadsInPrimaryRegion)
            .result(badSessionTokenServerErrorResult)
            .duration(Duration.ofSeconds(10))
            .build();

        CosmosFaultInjectionHelper
            .configureFaultInjectionRules(containerForClientWithPreferredRegions, Arrays.asList(badSessionTokenRule))
            .block();

        validateOperationExecutionResult(
            performDocumentOperation(
                containerForClientWithPreferredRegions,
                createdItem,
                operationType
            ),
            sessionTokenMismatchDefaultWaitTime,
            regionSwitchHint);

        safeCloseAsync(clientWithPreferredRegions);
    }

    @Test(groups = {"multi-master"}, dataProvider = "writeOperationContextProvider", timeOut = TIMEOUT)
    public void writeOperation_withReadSessionUnavailable_test(
        OperationType operationType,
        FaultInjectionOperationType faultInjectionOperationType,
        CosmosRegionSwitchHint regionSwitchHint) {

        List<String> preferredRegions = this.writeRegionMap.keySet().stream().collect(Collectors.toList());

        Duration sessionTokenMismatchDefaultWaitTime = Duration.ofMillis(Configs.getSessionTokenMismatchDefaultWaitTimeInMs());

        assertThat(preferredRegions).isNotNull();
        assertThat(preferredRegions.size()).isEqualTo(2);

        CosmosAsyncClient clientWithPreferredRegions = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .preferredRegions(preferredRegions)
            .sessionRetryOptions(new CosmosSessionRetryOptionsBuilder().setRegionSwitchHint(regionSwitchHint).build())
            .buildAsyncClient();

        CosmosAsyncContainer asyncContainerFromClientWithPreferredRegions = clientWithPreferredRegions
            .getDatabase(cosmosAsyncContainer.getDatabase().getId())
            .getContainer(cosmosAsyncContainer.getId());

        FaultInjectionCondition faultInjectionCondition = new FaultInjectionConditionBuilder()
            .operationType(faultInjectionOperationType)
            .connectionType(FaultInjectionConnectionType.DIRECT)
            .region(preferredRegions.get(0))
            .build();

        FaultInjectionServerErrorResult faultInjectionServerErrorResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
            .build();

        FaultInjectionRule badSessionTokenRule = new FaultInjectionRuleBuilder("bad-session-token-rule-" + UUID.randomUUID())
            .condition(faultInjectionCondition)
            .result(faultInjectionServerErrorResult)
            .duration(Duration.ofSeconds(10))
            .build();

        CosmosFaultInjectionHelper
            .configureFaultInjectionRules(asyncContainerFromClientWithPreferredRegions, Arrays.asList(badSessionTokenRule))
            .block();

        TestItem testItem = TestItem.createNewItem();

        validateOperationExecutionResult(
            performDocumentOperation(
                asyncContainerFromClientWithPreferredRegions,
                testItem,
                operationType
            ),
            sessionTokenMismatchDefaultWaitTime,
            regionSwitchHint);

        safeCloseAsync(clientWithPreferredRegions);
    }

    @AfterClass(groups = {"multi-region", "multi-master"}, timeOut = SHUTDOWN_TIMEOUT)
    public void afterClass() {
        safeDeleteCollection(cosmosAsyncContainer);
        safeCloseAsync(cosmosAsyncClient);
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

    private OperationExecutionResult performDocumentOperation(
        CosmosAsyncContainer faultInjectedContainer,
        TestItem testItem,
        OperationType operationType) {

        AtomicReference<Instant> operationStart = new AtomicReference<>(Instant.now());
        AtomicReference<Instant> operationEnd = new AtomicReference<>(Instant.now());

        if (operationType == OperationType.Query) {
            String query = String.format("SELECT * FROM c WHERE c.id = '%s'", testItem.getId());
            CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();

            SqlQuerySpec sqlQuerySpec = new SqlQuerySpec(query);
            FeedResponse<TestItem> feedResponse = faultInjectedContainer
                .queryItems(sqlQuerySpec, queryRequestOptions, TestItem.class)
                .byPage()
                .doOnSubscribe(ignore -> operationStart.set(Instant.now()))
                .doOnCancel(() -> operationEnd.set(Instant.now()))
                .blockFirst();

            assertThat(feedResponse).isNotNull();
            assertThat(feedResponse.getResults()).isNotNull();
            assertThat(feedResponse.getResults().size()).isEqualTo(1);

            return new OperationExecutionResult(
                feedResponse.getCosmosDiagnostics(),
                Duration.between(operationStart.get(), operationEnd.get()),
                HttpConstants.StatusCodes.OK,
                operationType);
        }

        if (operationType == OperationType.Read) {

            CosmosItemResponse<TestItem> itemResponse = faultInjectedContainer
                .readItem(testItem.getId(), new PartitionKey(testItem.getId()), TestItem.class)
                .doOnSubscribe(ignore -> operationStart.set(Instant.now()))
                .doOnSuccess(ignore -> operationEnd.set(Instant.now()))
                .block();

            return new OperationExecutionResult(
                itemResponse.getDiagnostics(),
                Duration.between(operationStart.get(), operationEnd.get()),
                itemResponse.getStatusCode(),
                operationType);
        }

        if (operationType == OperationType.Create) {

            CosmosItemRequestOptions itemRequestOptions = new CosmosItemRequestOptions();

            CosmosItemResponse<TestItem> itemResponse = faultInjectedContainer
                .createItem(testItem, new PartitionKey(testItem.getId()), itemRequestOptions)
                .doOnSubscribe(ignore -> operationStart.set(Instant.now()))
                .doOnSuccess(ignore -> operationEnd.set(Instant.now()))
                .block();

            return new OperationExecutionResult(
                itemResponse.getDiagnostics(),
                Duration.between(operationStart.get(), operationEnd.get()),
                itemResponse.getStatusCode(),
                operationType);
        }

        if (operationType == OperationType.Replace) {

            CosmosItemRequestOptions itemRequestOptions = new CosmosItemRequestOptions();

            faultInjectedContainer
                .createItem(testItem, new PartitionKey(testItem.getId()), itemRequestOptions)
                .block();

            CosmosItemResponse<TestItem> itemResponse = faultInjectedContainer
                .replaceItem(testItem, testItem.getId(), new PartitionKey(testItem.getId()), itemRequestOptions)
                .doOnSubscribe(ignore -> operationStart.set(Instant.now()))
                .doOnSuccess(ignore -> operationEnd.set(Instant.now()))
                .block();

            return new OperationExecutionResult(
                itemResponse.getDiagnostics(),
                Duration.between(operationStart.get(), operationEnd.get()),
                itemResponse.getStatusCode(),
                operationType);
        }

        if (operationType == OperationType.Delete) {
            CosmosItemRequestOptions itemRequestOptions = new CosmosItemRequestOptions();

            faultInjectedContainer
                .createItem(testItem, new PartitionKey(testItem.getId()), itemRequestOptions)
                .block();

            CosmosItemResponse<Object> itemResponse = faultInjectedContainer
                .deleteItem(testItem.getId(), new PartitionKey(testItem.getId()), itemRequestOptions)
                .doOnSubscribe(ignore -> operationStart.set(Instant.now()))
                .doOnSuccess(ignore -> operationEnd.set(Instant.now()))
                .block();

            return new OperationExecutionResult(
                itemResponse.getDiagnostics(),
                Duration.between(operationStart.get(), operationEnd.get()),
                itemResponse.getStatusCode(),
                operationType);
        }

        if (operationType == OperationType.Upsert) {
            CosmosItemRequestOptions itemRequestOptions = new CosmosItemRequestOptions();

            CosmosItemResponse<TestItem> itemResponse = faultInjectedContainer
                .upsertItem(testItem, new PartitionKey(testItem.getId()), itemRequestOptions)
                .doOnSubscribe(ignore -> operationStart.set(Instant.now()))
                .doOnSuccess(ignore -> operationEnd.set(Instant.now()))
                .block();

            return new OperationExecutionResult(
                itemResponse.getDiagnostics(),
                Duration.between(operationStart.get(), operationEnd.get()),
                itemResponse.getStatusCode(),
                operationType);
        }

        if (operationType == OperationType.Patch) {
            CosmosPatchOperations patchOperations = CosmosPatchOperations.create().add("/" + "newProperty", "newVal");
            CosmosItemRequestOptions itemRequestOptions = new CosmosItemRequestOptions();

            faultInjectedContainer
                .createItem(testItem, new PartitionKey(testItem.getId()), itemRequestOptions)
                .block();

            CosmosItemResponse<TestItem> itemResponse = faultInjectedContainer
                .patchItem(testItem.getId(), new PartitionKey(testItem.getId()), patchOperations, TestItem.class)
                .doOnSubscribe(ignore -> operationStart.set(Instant.now()))
                .doOnSuccess(ignore -> operationEnd.set(Instant.now()))
                .block();

            return new OperationExecutionResult(
                itemResponse.getDiagnostics(),
                Duration.between(operationStart.get(), operationEnd.get()),
                itemResponse.getStatusCode(),
                operationType);
        }

        return null;
    }

    private void validateOperationExecutionResult(
        OperationExecutionResult operationExecutionResult,
        Duration sessionTokenMismatchDefaultWaitTimePerRegion,
        CosmosRegionSwitchHint regionSwitchHint) {

        OperationType executionOpType = operationExecutionResult.operationType;
        int statusCode = operationExecutionResult.statusCode;
        Duration executionDuration = operationExecutionResult.duration;

        if (executionOpType == OperationType.Create) {
            assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.CREATED);
        } else if (executionOpType == OperationType.Delete) {
            assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.NO_CONTENT);
        } else if (executionOpType == OperationType.Upsert) {
            assertThat(statusCode == HttpConstants.StatusCodes.OK || statusCode == HttpConstants.StatusCodes.CREATED).isTrue();
        } else {
            assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.OK);
        }

        if (regionSwitchHint == CosmosRegionSwitchHint.DIFFERENT_REGION_PREFERRED) {
            assertThat(executionDuration).isLessThan(sessionTokenMismatchDefaultWaitTimePerRegion);
        } else if (regionSwitchHint == CosmosRegionSwitchHint.CURRENT_REGION_PREFERRED || regionSwitchHint == null) {
            assertThat(executionDuration).isGreaterThan(sessionTokenMismatchDefaultWaitTimePerRegion);
        }
    }

    private class OperationExecutionResult {
        private CosmosDiagnostics diagnostics;
        private Duration duration;
        private int statusCode;
        private OperationType operationType;

        OperationExecutionResult(CosmosDiagnostics diagnostics, Duration duration, int statusCode, OperationType operationType) {
            this.diagnostics = diagnostics;
            this.duration = duration;
            this.statusCode = statusCode;
            this.operationType = operationType;
        }
    }
}
