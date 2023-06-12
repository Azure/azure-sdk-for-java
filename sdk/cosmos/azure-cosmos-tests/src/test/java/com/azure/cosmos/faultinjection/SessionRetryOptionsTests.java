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
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosRegionSwitchHint;
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

    @BeforeClass(groups = {"multi-region"})
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
            {OperationType.Read, FaultInjectionOperationType.READ_ITEM, CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED},
            {OperationType.Query, FaultInjectionOperationType.QUERY_ITEM, CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED}
        };
    }

    @Test(groups = {"multi-region"}, dataProvider = "nonWriteOperationContextProvider", timeOut = 180000)
    public void nonWriteOperation_WithReadSessionUnavailable_test(OperationType operationType, FaultInjectionOperationType faultInjectionOperationType, CosmosRegionSwitchHint regionSwitchHint) {
        List<String> preferredLocations = this.writeRegionMap.keySet().stream().collect(Collectors.toList());
        Duration sessionTokenMismatchDefaultWaitTime = Duration.ofMillis(Configs.getSessionTokenMismatchDefaultWaitTimeInMs());

        assertThat(preferredLocations).isNotNull();
        assertThat(preferredLocations.size()).isEqualTo(2);

        CosmosAsyncClient clientWithPreferredRegions = null;

        clientWithPreferredRegions = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
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
            performDocumentOperation(containerForClientWithPreferredRegions, createdItem, operationType),
            sessionTokenMismatchDefaultWaitTime);

        safeCloseAsync(clientWithPreferredRegions);
    }

    @Test(groups = {"multi-region"}, timeOut = 60000)
    public void readSessionUnavailable_oneReplica_test() {

        List<String> preferredLocations = this.writeRegionMap.keySet().stream().collect(Collectors.toList());

        assertThat(preferredLocations).isNotNull();
        assertThat(preferredLocations.size()).isEqualTo(2);

        CosmosAsyncClient clientWithPreferredRegions = null;

        clientWithPreferredRegions = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .consistencyLevel(BridgeInternal.getContextClient(this.cosmosAsyncClient).getConsistencyLevel())
                .preferredRegions(preferredLocations)
                .directMode()
                .buildAsyncClient();

        CosmosAsyncContainer containerForClientWithPreferredRegions = clientWithPreferredRegions
                .getDatabase(this.cosmosAsyncContainer.getDatabase().getId())
                .getContainer(this.cosmosAsyncContainer.getId());

        TestItem createdItem = TestItem.createNewItem();
        containerForClientWithPreferredRegions.createItem(createdItem).block();

        FaultInjectionRuleBuilder badSessionTokenRuleBuilder = new FaultInjectionRuleBuilder("serverErrorRule-bad-session-token-" + UUID.randomUUID());

        FaultInjectionCondition faultInjectionConditionForReadsInPrimaryRegion = new FaultInjectionConditionBuilder()
                .operationType(FaultInjectionOperationType.READ_ITEM)
                .connectionType(FaultInjectionConnectionType.DIRECT)
                .region(preferredLocations.get(0))
                .build();

        // a lagging replica can be simulated by fault-injecting once
        // internally a replica will be chosen at random for which fault
        // is injected but no more fault injections thereafter
        FaultInjectionServerErrorResult badSessionTokenServerErrorResult = FaultInjectionResultBuilders
                .getResultBuilder(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                .times(1)
                .build();

        FaultInjectionRule badSessionTokenRule = badSessionTokenRuleBuilder
                .condition(faultInjectionConditionForReadsInPrimaryRegion)
                .result(badSessionTokenServerErrorResult)
                .duration(Duration.ofSeconds(10))
                .build();

        CosmosFaultInjectionHelper
                .configureFaultInjectionRules(containerForClientWithPreferredRegions, Arrays.asList(badSessionTokenRule))
                .block();

        CosmosItemResponse<TestItem> itemResponse = containerForClientWithPreferredRegions.readItem(
                createdItem.getId(),
                new PartitionKey(createdItem.getId()),
                TestItem.class).block();

        assertThat(itemResponse.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);

        safeCloseAsync(clientWithPreferredRegions);
    }

    @AfterClass(groups = {"multi-region"})
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

    private OperationExecutionResult performDocumentOperation(CosmosAsyncContainer faultInjectedContainer, TestItem testItem, OperationType operationType) {

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
                HttpConstants.StatusCodes.OK);
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
                itemResponse.getStatusCode());
        }

        if (operationType == OperationType.Create) {
            CosmosItemRequestOptions itemRequestOptions = new CosmosItemRequestOptions();

            CosmosItemResponse<TestItem> itemResponse = faultInjectedContainer
                .createItem(testItem, new PartitionKey(testItem.getId()), itemRequestOptions)
                .doOnSubscribe(ignore -> operationStart.set(Instant.now()))
                .doOnSuccess(ignore -> operationEnd.set(Instant.now()))
                .block();

            return new OperationExecutionResult(itemResponse.getDiagnostics(), Duration.between(operationStart.get(), operationEnd.get()), itemResponse.getStatusCode());
        }

        if (operationType == OperationType.Replace) {
            CosmosItemRequestOptions itemRequestOptions = new CosmosItemRequestOptions();

            CosmosItemResponse<TestItem> itemResponse = faultInjectedContainer
                .replaceItem(testItem, testItem.getId(), new PartitionKey(testItem.getId()), itemRequestOptions)
                .doOnSubscribe(ignore -> operationStart.set(Instant.now()))
                .doOnSuccess(ignore -> operationEnd.set(Instant.now()))
                .block();

            return new OperationExecutionResult(
                itemResponse.getDiagnostics(),
                Duration.between(operationStart.get(), operationEnd.get()),
                itemResponse.getStatusCode());
        }

        if (operationType == OperationType.Delete) {
            CosmosItemRequestOptions itemRequestOptions = new CosmosItemRequestOptions();

            CosmosItemResponse<Object> itemResponse = faultInjectedContainer
                .deleteItem(testItem.getId(), new PartitionKey(testItem.getId()), itemRequestOptions)
                .doOnSubscribe(ignore -> operationStart.set(Instant.now()))
                .doOnSuccess(ignore -> operationEnd.set(Instant.now()))
                .block();

            return new OperationExecutionResult(
                itemResponse.getDiagnostics(),
                Duration.between(operationStart.get(), operationEnd.get()),
                itemResponse.getStatusCode());
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
                itemResponse.getStatusCode());
        }

        return null;
    }

    private void validateOperationExecutionResult(
        OperationExecutionResult operationExecutionResult,
        Duration maxSessionTokenMismatchRetryWaitTime) {
        assertThat(operationExecutionResult.statusCode).isEqualTo(HttpConstants.StatusCodes.OK);
        assertThat(operationExecutionResult.duration).isLessThan(maxSessionTokenMismatchRetryWaitTime);
    }

    private class OperationExecutionResult {
        private CosmosDiagnostics diagnostics;
        private Duration duration;
        private int statusCode;

        OperationExecutionResult(CosmosDiagnostics diagnostics, Duration duration, int statusCode) {
            this.diagnostics = diagnostics;
            this.duration = duration;
            this.statusCode = statusCode;
        }
    }
}
