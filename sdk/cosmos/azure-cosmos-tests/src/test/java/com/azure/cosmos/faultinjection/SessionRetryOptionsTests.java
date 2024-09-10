// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosRegionSwitchHint;
import com.azure.cosmos.SessionRetryOptions;
import com.azure.cosmos.SessionRetryOptionsBuilder;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.testng.AssertJUnit.fail;

public class SessionRetryOptionsTests extends FaultInjectionTestBase {
    private final static ImplementationBridgeHelpers.CosmosSessionRetryOptionsHelper.CosmosSessionRetryOptionsAccessor
        sessionRetryOptionsAccessor = ImplementationBridgeHelpers
        .CosmosSessionRetryOptionsHelper
        .getCosmosSessionRetryOptionsAccessor();

    private CosmosAsyncClient cosmosAsyncClient;
    private CosmosAsyncContainer cosmosAsyncContainer;
    private List<String> preferredRegions;

    @Factory(dataProvider = "clientBuilderSolelyDirectWithSessionConsistency")
    public SessionRetryOptionsTests(CosmosClientBuilder cosmosClientBuilder) {
        super(cosmosClientBuilder);
    }

    @BeforeClass(groups = {"multi-master"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        cosmosAsyncClient = getClientBuilder().buildAsyncClient();
        AsyncDocumentClient asyncDocumentClient = BridgeInternal.getContextClient(cosmosAsyncClient);
        GlobalEndpointManager globalEndpointManager = asyncDocumentClient.getGlobalEndpointManager();

        DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();
        this.cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(cosmosAsyncClient);

        AccountLevelLocationContext accountLevelWriteableLocationContext
            = getAccountLevelLocationContext(databaseAccount, true);
        validate(accountLevelWriteableLocationContext, true);

        this.preferredRegions = accountLevelWriteableLocationContext.serviceOrderedWriteableRegions;
    }

    @DataProvider(name = "nonWriteOperationContextProvider")
    public Object[][] nonWriteOperationContextProvider() {
        return new Object[][]{
            {OperationType.Read, FaultInjectionOperationType.READ_ITEM, CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED, 2, true},
            {OperationType.Query, FaultInjectionOperationType.QUERY_ITEM, CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED, 1, true},
            {OperationType.Read, FaultInjectionOperationType.READ_ITEM, CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED, 2, true},
            {OperationType.Query, FaultInjectionOperationType.QUERY_ITEM, CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED, 2, true},

            {OperationType.Read, FaultInjectionOperationType.READ_ITEM, CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED, 2, false},
            {OperationType.Query, FaultInjectionOperationType.QUERY_ITEM, CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED, 1, false},
            {OperationType.Read, FaultInjectionOperationType.READ_ITEM, CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED, 2, false},
            {OperationType.Query, FaultInjectionOperationType.QUERY_ITEM, CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED, 2, false}
        };
    }

    @DataProvider(name = "writeOperationContextProvider")
    public Object[][] writeOperationContextProvider() {
        return new Object[][]{
            {OperationType.Create, FaultInjectionOperationType.CREATE_ITEM, CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED, 2, true},
            {OperationType.Replace, FaultInjectionOperationType.REPLACE_ITEM, CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED, 1, true},
            {OperationType.Delete, FaultInjectionOperationType.DELETE_ITEM, CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED, 2, true},
            {OperationType.Upsert, FaultInjectionOperationType.UPSERT_ITEM, CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED, 3, true},
            {OperationType.Patch, FaultInjectionOperationType.PATCH_ITEM, CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED, 1, true},
            {OperationType.Create, FaultInjectionOperationType.CREATE_ITEM, CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED, 3, true},
            {OperationType.Replace, FaultInjectionOperationType.REPLACE_ITEM, CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED, 1, true},
            {OperationType.Delete, FaultInjectionOperationType.DELETE_ITEM, CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED, 2, true},
            {OperationType.Upsert, FaultInjectionOperationType.UPSERT_ITEM, CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED, 1, true},
            {OperationType.Patch, FaultInjectionOperationType.PATCH_ITEM, CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED, 1, true},

            {OperationType.Create, FaultInjectionOperationType.CREATE_ITEM, CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED, 2, false},
            {OperationType.Replace, FaultInjectionOperationType.REPLACE_ITEM, CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED, 1, false},
            {OperationType.Delete, FaultInjectionOperationType.DELETE_ITEM, CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED, 2, false},
            {OperationType.Upsert, FaultInjectionOperationType.UPSERT_ITEM, CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED, 3, false},
            {OperationType.Patch, FaultInjectionOperationType.PATCH_ITEM, CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED, 1, false},
            {OperationType.Create, FaultInjectionOperationType.CREATE_ITEM, CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED, 3, false},
            {OperationType.Replace, FaultInjectionOperationType.REPLACE_ITEM, CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED, 1, false},
            {OperationType.Delete, FaultInjectionOperationType.DELETE_ITEM, CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED, 2, false},
            {OperationType.Upsert, FaultInjectionOperationType.UPSERT_ITEM, CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED, 1, false},
            {OperationType.Patch, FaultInjectionOperationType.PATCH_ITEM, CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED, 1, false}

        };
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void SessionRetryOptionsBuilder_defaultValues() {
        SessionRetryOptions optionsWithDefaultValues = new SessionRetryOptionsBuilder()
            .regionSwitchHint(CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED)
            .build();

        assertThat(sessionRetryOptionsAccessor.getMaxInRegionRetryCount(optionsWithDefaultValues))
            .isEqualTo(Configs.getMaxRetriesInLocalRegionWhenRemoteRegionPreferred());

        assertThat(sessionRetryOptionsAccessor.getMinInRegionRetryTime(optionsWithDefaultValues))
            .isEqualTo(Configs.getMinRetryTimeInLocalRegionWhenRemoteRegionPreferred());
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void SessionRetryOptionsBuilder_customValues() {
        SessionRetryOptions optionsWithDefaultValues = new SessionRetryOptionsBuilder()
            .regionSwitchHint(CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED)
            .minTimeoutPerRegion(Duration.ofSeconds(1))
            .maxRetriesPerRegion(3)
            .build();

        assertThat(sessionRetryOptionsAccessor.getMaxInRegionRetryCount(optionsWithDefaultValues))
            .isEqualTo(3);

        assertThat(sessionRetryOptionsAccessor.getMinInRegionRetryTime(optionsWithDefaultValues))
            .isEqualTo(Duration.ofSeconds(1));
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void SessionRetryOptionsBuilder_minimum_maxRetryCountEnforced() {
        SessionRetryOptionsBuilder builder = new SessionRetryOptionsBuilder()
            .regionSwitchHint(CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED)
            .maxRetriesPerRegion(0);

        try {
            builder.build();

            fail("Building the session retry options should have failed");
        } catch (IllegalArgumentException illegalArgumentException) {
            logger.info("Expected IllegalArgumentException", illegalArgumentException);
        }
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void SessionRetryOptionsBuilder_minimum_minRetryTimeEnforced() {
        SessionRetryOptionsBuilder builder = new SessionRetryOptionsBuilder()
            .regionSwitchHint(CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED)
            .minTimeoutPerRegion(Duration.ofMillis(99));

        try {
            builder.build();

            fail("Building the session retry options should have failed");
        } catch (IllegalArgumentException illegalArgumentException) {
            logger.info("Expected IllegalArgumentException", illegalArgumentException);
        }
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void SessionRetryOptionsBuilder_minRetryTimeRequired() {
        SessionRetryOptionsBuilder builder = new SessionRetryOptionsBuilder()
            .regionSwitchHint(CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED)
            .minTimeoutPerRegion(null);

        try {
            builder.build();

            fail("Building the session retry options should have failed");
        } catch (IllegalArgumentException illegalArgumentException) {
            logger.info("Expected IllegalArgumentException", illegalArgumentException);
        }
    }

    @Test(groups = {"multi-master"}, dataProvider = "nonWriteOperationContextProvider", timeOut = TIMEOUT)
    public void nonWriteOperation_WithReadSessionUnavailable_test(
        OperationType operationType,
        FaultInjectionOperationType faultInjectionOperationType,
        CosmosRegionSwitchHint regionSwitchHint,
        int sessionTokenMismatchRetryAttempts,
        boolean shouldInjectPreferredRegionsOnClient) {

        System.setProperty("COSMOS.MAX_RETRIES_IN_LOCAL_REGION_WHEN_REMOTE_REGION_PREFERRED", String.valueOf(sessionTokenMismatchRetryAttempts));

        Duration sessionTokenMismatchDefaultWaitTime = Duration.ofMillis(Configs.getSessionTokenMismatchDefaultWaitTimeInMs());

        assertThat(this.preferredRegions).isNotNull();
        assertThat(this.preferredRegions.size()).isEqualTo(2);

        CosmosAsyncClient clientWithPreferredRegions = null;

        try {
            clientWithPreferredRegions = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .consistencyLevel(BridgeInternal.getContextClient(this.cosmosAsyncClient).getConsistencyLevel())
                .preferredRegions(shouldInjectPreferredRegionsOnClient ? this.preferredRegions : Collections.emptyList())
                .sessionRetryOptions(new SessionRetryOptionsBuilder().regionSwitchHint(regionSwitchHint).build())
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
                .region(this.preferredRegions.get(0))
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

            // For a non-write operation, the request can go to multiple replicas (upto 4 replicas)
            // Check if the SessionTokenMismatchRetryPolicy retries on the bad / lagging region
            // for sessionTokenMismatchRetryAttempts by tracking the badSessionTokenRule hit count
            if (regionSwitchHint == CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED) {
                assertThat(badSessionTokenRule.getHitCount()).isBetween((long) sessionTokenMismatchRetryAttempts, (1 + sessionTokenMismatchRetryAttempts) * 4L);
            }
        } finally {
            System.clearProperty("COSMOS.MAX_RETRIES_IN_LOCAL_REGION_WHEN_REMOTE_REGION_PREFERRED");
            safeCloseAsync(clientWithPreferredRegions);
        }
    }

    @Test(groups = {"multi-master"}, dataProvider = "writeOperationContextProvider", timeOut = TIMEOUT)
    public void writeOperation_withReadSessionUnavailable_test(
        OperationType operationType,
        FaultInjectionOperationType faultInjectionOperationType,
        CosmosRegionSwitchHint regionSwitchHint,
        int sessionTokenMismatchRetryAttempts,
        boolean shouldInjectPreferredRegionsOnClient) {

        System.setProperty("COSMOS.MAX_RETRIES_IN_LOCAL_REGION_WHEN_REMOTE_REGION_PREFERRED", String.valueOf(sessionTokenMismatchRetryAttempts));

        Duration sessionTokenMismatchDefaultWaitTime = Duration.ofMillis(Configs.getSessionTokenMismatchDefaultWaitTimeInMs());

        assertThat(this.preferredRegions).isNotNull();
        assertThat(this.preferredRegions.size()).isEqualTo(2);

        CosmosAsyncClient clientWithPreferredRegions = null;

        try {
            clientWithPreferredRegions = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .preferredRegions(shouldInjectPreferredRegionsOnClient ? this.preferredRegions : Collections.emptyList())
                .sessionRetryOptions(new SessionRetryOptionsBuilder().regionSwitchHint(regionSwitchHint).build())
                .buildAsyncClient();

            CosmosAsyncContainer asyncContainerFromClientWithPreferredRegions = clientWithPreferredRegions
                .getDatabase(cosmosAsyncContainer.getDatabase().getId())
                .getContainer(cosmosAsyncContainer.getId());

            FaultInjectionCondition faultInjectionCondition = new FaultInjectionConditionBuilder()
                .operationType(faultInjectionOperationType)
                .connectionType(FaultInjectionConnectionType.DIRECT)
                .region(this.preferredRegions.get(0))
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

            // For a write operation, the request can just go to the primary replica
            // Check if the SessionTokenMismatchRetryPolicy retries on the bad / lagging region
            // for sessionTokenMismatchRetryAttempts by tracking the badSessionTokenRule hit count
            if (regionSwitchHint == CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED) {
                // higher hit count is possible while in MinRetryWaitTimeWithinRegion
                assertThat(badSessionTokenRule.getHitCount()).isGreaterThanOrEqualTo(
                    sessionTokenMismatchRetryAttempts);
            }
        } finally {
            System.clearProperty("COSMOS.MAX_RETRIES_IN_LOCAL_REGION_WHEN_REMOTE_REGION_PREFERRED");
            safeCloseAsync(clientWithPreferredRegions);
        }
    }

    @AfterClass(groups = {"multi-master"}, timeOut = SHUTDOWN_TIMEOUT)
    public void afterClass() {
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

        if (regionSwitchHint == CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED) {
            assertThat(executionDuration).isLessThan(sessionTokenMismatchDefaultWaitTimePerRegion);
        } else if (regionSwitchHint == CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED || regionSwitchHint == null) {
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

    private static AccountLevelLocationContext getAccountLevelLocationContext(DatabaseAccount databaseAccount, boolean writeOnly) {
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

    private static void validate(AccountLevelLocationContext accountLevelLocationContext, boolean isWriteOnly) {

        assertThat(accountLevelLocationContext).isNotNull();

        if (isWriteOnly) {
            assertThat(accountLevelLocationContext.serviceOrderedWriteableRegions).isNotNull();
            assertThat(accountLevelLocationContext.serviceOrderedWriteableRegions.size()).isGreaterThanOrEqualTo(1);
        } else {
            assertThat(accountLevelLocationContext.serviceOrderedReadableRegions).isNotNull();
            assertThat(accountLevelLocationContext.serviceOrderedReadableRegions.size()).isGreaterThanOrEqualTo(1);
        }
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
