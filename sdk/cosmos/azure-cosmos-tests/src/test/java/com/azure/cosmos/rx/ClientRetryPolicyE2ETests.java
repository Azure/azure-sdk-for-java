// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.rx;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.FlakyTestRetryAnalyzer;
import com.azure.cosmos.ReadConsistencyStrategy;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.directconnectivity.StoreResponseDiagnostics;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.assertj.core.api.AssertionsForClassTypes;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.fail;

public class ClientRetryPolicyE2ETests extends TestSuiteBase {

    private static final ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor cosmosDiagnosticsAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor();

    private CosmosAsyncClient clientWithPreferredRegions;
    private CosmosAsyncContainer cosmosAsyncContainerFromClientWithPreferredRegions;
    private CosmosAsyncClient clientWithoutPreferredRegions;
    private CosmosAsyncContainer cosmosAsyncContainerFromClientWithoutPreferredRegions;
    private List<String> preferredRegions;
    private List<String> serviceOrderedReadableRegions;
    private List<String> serviceOrderedWriteableRegions;

    private void assertContactedRegionCount(
        CosmosDiagnostics cosmosDiagnostics,
        int expectedCount,
        String expectation) {

        Set<String> contactedRegionNames = getContactedRegionNamesOrFail(cosmosDiagnostics, expectation);
        if (contactedRegionNames.size() != expectedCount) {
            fail(formatContactedRegionsAssertionMessage(
                expectation,
                String.format("contacted region count <%d>", expectedCount),
                contactedRegionNames,
                cosmosDiagnostics,
                cosmosDiagnostics == null ? null : cosmosDiagnostics.getDiagnosticsContext()));
        }
    }

    private void assertContactedRegionCount(
        CosmosDiagnosticsContext diagnosticsContext,
        int expectedCount,
        String expectation) {

        Set<String> contactedRegionNames = getContactedRegionNamesOrFail(diagnosticsContext, expectation);
        if (contactedRegionNames.size() != expectedCount) {
            fail(formatContactedRegionsAssertionMessage(
                expectation,
                String.format("contacted region count <%d>", expectedCount),
                contactedRegionNames,
                null,
                diagnosticsContext));
        }
    }

    private void assertContactedRegionCountBetween(
        CosmosDiagnostics cosmosDiagnostics,
        int minCount,
        int maxCount,
        String expectation) {

        Set<String> contactedRegionNames = getContactedRegionNamesOrFail(cosmosDiagnostics, expectation);
        if (contactedRegionNames.size() < minCount || contactedRegionNames.size() > maxCount) {
            fail(formatContactedRegionsAssertionMessage(
                expectation,
                String.format("contacted region count between <%d> and <%d>", minCount, maxCount),
                contactedRegionNames,
                cosmosDiagnostics,
                cosmosDiagnostics == null ? null : cosmosDiagnostics.getDiagnosticsContext()));
        }
    }

    private void assertContactedRegionsContain(
        CosmosDiagnostics cosmosDiagnostics,
        String expectedRegion,
        String expectation) {

        Set<String> contactedRegionNames = getContactedRegionNamesOrFail(cosmosDiagnostics, expectation);
        if (!contactedRegionNames.contains(expectedRegion)) {
            fail(formatContactedRegionsAssertionMessage(
                expectation,
                String.format("contacted regions to contain <%s>", expectedRegion),
                contactedRegionNames,
                cosmosDiagnostics,
                cosmosDiagnostics == null ? null : cosmosDiagnostics.getDiagnosticsContext()));
        }
    }

    private void assertContactedRegionsContainAll(
        CosmosDiagnostics cosmosDiagnostics,
        List<String> expectedRegions,
        String expectation) {

        Set<String> contactedRegionNames = getContactedRegionNamesOrFail(cosmosDiagnostics, expectation);
        if (!contactedRegionNames.containsAll(expectedRegions)) {
            fail(formatContactedRegionsAssertionMessage(
                expectation,
                String.format("contacted regions to contain all <%s>", expectedRegions),
                contactedRegionNames,
                cosmosDiagnostics,
                cosmosDiagnostics == null ? null : cosmosDiagnostics.getDiagnosticsContext()));
        }
    }

    private Set<String> getContactedRegionNamesOrFail(CosmosDiagnostics cosmosDiagnostics, String expectation) {
        if (cosmosDiagnostics == null) {
            fail(expectation + ". Cosmos diagnostics were null.");
        }

        Set<String> contactedRegionNames = cosmosDiagnostics.getContactedRegionNames();
        if (contactedRegionNames == null) {
            fail(formatContactedRegionsAssertionMessage(
                expectation,
                "non-null contacted region names",
                null,
                cosmosDiagnostics,
                cosmosDiagnostics.getDiagnosticsContext()));
        }

        return contactedRegionNames;
    }

    private Set<String> getContactedRegionNamesOrFail(CosmosDiagnosticsContext diagnosticsContext, String expectation) {
        if (diagnosticsContext == null) {
            fail(expectation + ". Diagnostics context was null.");
        }

        Set<String> contactedRegionNames = diagnosticsContext.getContactedRegionNames();
        if (contactedRegionNames == null) {
            fail(formatContactedRegionsAssertionMessage(
                expectation,
                "non-null contacted region names",
                null,
                null,
                diagnosticsContext));
        }

        return contactedRegionNames;
    }

    private String formatContactedRegionsAssertionMessage(
        String expectation,
        String expected,
        Set<String> contactedRegionNames,
        CosmosDiagnostics cosmosDiagnostics,
        CosmosDiagnosticsContext diagnosticsContext) {

        return String.format(
            "%s. Expected %s but actual contacted regions were <%s>. "
                + "preferredRegions=<%s>, serviceOrderedReadableRegions=<%s>, "
                + "serviceOrderedWriteableRegions=<%s>, diagnosticsContext=<%s>, diagnostics=<%s>",
            expectation,
            expected,
            contactedRegionNames,
            this.preferredRegions,
            this.serviceOrderedReadableRegions,
            this.serviceOrderedWriteableRegions,
            diagnosticsContext == null ? null : diagnosticsContext.toJson(),
            cosmosDiagnostics == null ? null : cosmosDiagnostics.toString());
    }

    private List<String> getServiceOrderedRegionsForOperation(OperationType operationType) {
        return Utils.isWriteOperation(operationType)
            ? this.serviceOrderedWriteableRegions
            : this.serviceOrderedReadableRegions;
    }

    private List<String> getExpectedServiceOrderedRegionsForMessage(OperationType operationType, int maxRegionCount) {
        List<String> serviceOrderedRegions = getServiceOrderedRegionsForOperation(operationType);
        if (serviceOrderedRegions == null) {
            return Collections.emptyList();
        }

        return serviceOrderedRegions.subList(0, Math.min(maxRegionCount, serviceOrderedRegions.size()));
    }

    private List<String> getExpectedPreferredRegionsForMessage(int maxRegionCount) {
        if (this.preferredRegions == null) {
            return Collections.emptyList();
        }

        return this.preferredRegions.subList(0, Math.min(maxRegionCount, this.preferredRegions.size()));
    }

    @DataProvider(name = "channelAcquisitionExceptionArgProvider")
    public static Object[][] channelAcquisitionExceptionArgProvider() {
        return new Object[][]{
            // OperationType, FaultInjectionOperationType, shouldUsePreferredRegionsOnClient
            { OperationType.Read, FaultInjectionOperationType.READ_ITEM, true },
            { OperationType.Read, FaultInjectionOperationType.READ_ITEM, false },
            { OperationType.Create, FaultInjectionOperationType.CREATE_ITEM, true },
            { OperationType.Create, FaultInjectionOperationType.CREATE_ITEM, false }
        };
    }

    @DataProvider(name = "leaseNotFoundArgProvider")
    public static Object[][] leaseNotFoundArgProvider() {
        return new Object[][]{
            // OperationType, FaultInjectionOperationType, shouldUsePreferredRegionsOnClient, isReadMany, hitLimit (1 or 2) for lease not found
            { OperationType.Read, FaultInjectionOperationType.READ_ITEM, true, false, 1 },
            { OperationType.Query, FaultInjectionOperationType.QUERY_ITEM, true, false, 1 },
            { OperationType.Create, FaultInjectionOperationType.CREATE_ITEM, true, false, 1 },
            { OperationType.Patch, FaultInjectionOperationType.PATCH_ITEM, true, false, 1 },
            { OperationType.Replace, FaultInjectionOperationType.REPLACE_ITEM, true, false, 1 },
            { OperationType.Delete, FaultInjectionOperationType.DELETE_ITEM, true, false, 1 },
            { OperationType.Upsert, FaultInjectionOperationType.UPSERT_ITEM, true, false, 1 },
            { OperationType.ReadFeed, FaultInjectionOperationType.READ_FEED_ITEM, true, false, 1 },
            { OperationType.Batch, FaultInjectionOperationType.BATCH_ITEM, true, false, 1 },
            { OperationType.Query, FaultInjectionOperationType.QUERY_ITEM, true, true, 1 },
            { OperationType.Read, FaultInjectionOperationType.READ_ITEM, false, false, 1 },
            { OperationType.Query, FaultInjectionOperationType.QUERY_ITEM, false, false, 1 },
            { OperationType.Create, FaultInjectionOperationType.CREATE_ITEM, false, false, 1 },
            { OperationType.Patch, FaultInjectionOperationType.PATCH_ITEM, false, false, 1 },
            { OperationType.Replace, FaultInjectionOperationType.REPLACE_ITEM, false, false, 1 },
            { OperationType.Delete, FaultInjectionOperationType.DELETE_ITEM, false, false, 1 },
            { OperationType.Upsert, FaultInjectionOperationType.UPSERT_ITEM, false, false, 1 },
            { OperationType.ReadFeed, FaultInjectionOperationType.READ_FEED_ITEM, false, false, 1 },
            { OperationType.Batch, FaultInjectionOperationType.BATCH_ITEM, false, false, 1 },
            { OperationType.Query, FaultInjectionOperationType.QUERY_ITEM, false, true, 1 },
            { OperationType.Read, FaultInjectionOperationType.READ_ITEM, true, false, 2 },
            { OperationType.Query, FaultInjectionOperationType.QUERY_ITEM, true, false, 2 },
            { OperationType.Create, FaultInjectionOperationType.CREATE_ITEM, true, false, 2 },
            { OperationType.Patch, FaultInjectionOperationType.PATCH_ITEM, true, false, 2 },
            { OperationType.Replace, FaultInjectionOperationType.REPLACE_ITEM, true, false, 2 },
            { OperationType.Delete, FaultInjectionOperationType.DELETE_ITEM, true, false, 2 },
            { OperationType.Upsert, FaultInjectionOperationType.UPSERT_ITEM, true, false, 2 },
            { OperationType.ReadFeed, FaultInjectionOperationType.READ_FEED_ITEM, true, false, 2 },
            { OperationType.Batch, FaultInjectionOperationType.BATCH_ITEM, true, false, 2 },
            { OperationType.Query, FaultInjectionOperationType.QUERY_ITEM, true, true, 2 },
            { OperationType.Read, FaultInjectionOperationType.READ_ITEM, false, false, 2 },
            { OperationType.Query, FaultInjectionOperationType.QUERY_ITEM, false, false, 2 },
            { OperationType.Create, FaultInjectionOperationType.CREATE_ITEM, false, false, 2 },
            { OperationType.Patch, FaultInjectionOperationType.PATCH_ITEM, false, false, 2 },
            { OperationType.Replace, FaultInjectionOperationType.REPLACE_ITEM, false, false, 2 },
            { OperationType.Delete, FaultInjectionOperationType.DELETE_ITEM, false, false, 2 },
            { OperationType.Upsert, FaultInjectionOperationType.UPSERT_ITEM, false, false, 2 },
            { OperationType.ReadFeed, FaultInjectionOperationType.READ_FEED_ITEM, false, false, 2 },
            { OperationType.Batch, FaultInjectionOperationType.BATCH_ITEM, false, false, 2 },
            { OperationType.Query, FaultInjectionOperationType.QUERY_ITEM, false, true, 2 }
        };
    }

    @Factory(dataProvider = "clientBuildersWithSessionConsistency")
    public ClientRetryPolicyE2ETests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @BeforeClass(groups = {"multi-master", "fast", "fi-multi-master", "multi-region"}, timeOut = TIMEOUT)
    public void beforeClass() {
        try(CosmosAsyncClient dummy = getClientBuilder().buildAsyncClient()) {
            AsyncDocumentClient asyncDocumentClient = BridgeInternal.getContextClient(dummy);
            GlobalEndpointManager globalEndpointManager = asyncDocumentClient.getGlobalEndpointManager();

            DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();

            AccountLevelLocationContext accountLevelReadableLocationContext
                = getAccountLevelLocationContext(databaseAccount, false);

            AccountLevelLocationContext accountLevelWriteableLocationContext
                = getAccountLevelLocationContext(databaseAccount, true);

            validate(accountLevelReadableLocationContext, false);
            validate(accountLevelWriteableLocationContext, true);

            this.preferredRegions = accountLevelReadableLocationContext.serviceOrderedReadableRegions
                .stream()
                .map(regionName -> regionName.toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());

            this.serviceOrderedReadableRegions = this.preferredRegions;

            this.serviceOrderedWriteableRegions = accountLevelWriteableLocationContext.serviceOrderedWriteableRegions
                .stream()
                .map(regionName -> regionName.toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());

            this.clientWithPreferredRegions = getClientBuilder()
                .preferredRegions(this.preferredRegions)
                .endpointDiscoveryEnabled(true)
                .multipleWriteRegionsEnabled(true)
                .buildAsyncClient();

            this.clientWithoutPreferredRegions = getClientBuilder()
                .endpointDiscoveryEnabled(true)
                .multipleWriteRegionsEnabled(true)
                .buildAsyncClient();
        }

        this.cosmosAsyncContainerFromClientWithPreferredRegions =
            getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(clientWithPreferredRegions);
        this.cosmosAsyncContainerFromClientWithoutPreferredRegions =
            getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(clientWithoutPreferredRegions);
    }

    @AfterClass(groups = {"multi-master", "fast", "fi-multi-master", "multi-region"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(this.clientWithPreferredRegions);
        safeClose(this.clientWithoutPreferredRegions);
    }

    @DataProvider(name = "operationTypeProvider")
    public static Object[][] operationTypeProvider() {
        return new Object[][]{
            // FaultInjectionOperationType, OperationType, shouldRetryCrossRegionForHttpTimeout, shouldUsePreferredRegionsOnClient
            { FaultInjectionOperationType.READ_ITEM, OperationType.Read, Boolean.TRUE, Boolean.TRUE },
            { FaultInjectionOperationType.READ_ITEM, OperationType.Read, Boolean.TRUE, Boolean.FALSE },
            { FaultInjectionOperationType.CREATE_ITEM, OperationType.Create, Boolean.FALSE, Boolean.TRUE },
            { FaultInjectionOperationType.CREATE_ITEM, OperationType.Create, Boolean.FALSE, Boolean.FALSE }
        };
    }

    @DataProvider(name = "preferredRegionsConfigProvider")
    public static Object[] preferredRegionsConfigProvider() {

        // shouldUsePreferredRegionsOnClient
        return new Object[] {Boolean.TRUE, Boolean.FALSE};
    }

    @Test(groups = { "multi-master" }, dataProvider = "preferredRegionsConfigProvider", timeOut = TIMEOUT)
    public void queryPlanHttpTimeoutWillNotMarkRegionUnavailable(boolean shouldUsePreferredRegionsOnClient) {
        TestObject newItem = TestObject.create();

        CosmosAsyncContainer resultantCosmosAsyncContainer;
        CosmosAsyncClient resultantCosmosAsyncClient;

        if (shouldUsePreferredRegionsOnClient) {
            resultantCosmosAsyncClient = this.clientWithPreferredRegions;
            resultantCosmosAsyncContainer = this.cosmosAsyncContainerFromClientWithPreferredRegions;
        } else {
            resultantCosmosAsyncClient = this.clientWithoutPreferredRegions;
            resultantCosmosAsyncContainer = this.cosmosAsyncContainerFromClientWithoutPreferredRegions;
        }

        resultantCosmosAsyncContainer.createItem(newItem).block();

        // create fault injection rules for query plan
        FaultInjectionConditionBuilder conditionBuilder =
            new FaultInjectionConditionBuilder()
                .operationType(FaultInjectionOperationType.METADATA_REQUEST_QUERY_PLAN);
        if (BridgeInternal
            .getContextClient(resultantCosmosAsyncClient)
            .getConnectionPolicy()
            .getConnectionMode() == ConnectionMode.GATEWAY) {
            conditionBuilder.connectionType(FaultInjectionConnectionType.GATEWAY);
        }

        FaultInjectionRule queryPlanDelayRule = new FaultInjectionRuleBuilder("queryPlanRule")
            .condition(conditionBuilder.build())
            .result(
                FaultInjectionResultBuilders.getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
                    .delay(Duration.ofSeconds(11))
                    .times(4) // make sure it will exhaust the local region retry
                    .build()
            )
            .build();

        CosmosFaultInjectionHelper.configureFaultInjectionRules(resultantCosmosAsyncContainer, Arrays.asList(queryPlanDelayRule)).block();
        String query = "select * from c";
        CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
        queryRequestOptions.setPartitionKey(new PartitionKey(newItem.getId()));
        try {
            // validate the query plan will be retried in a different region and the final requests will be succeeded
            // TODO: Also capture all retries for metadata requests in the diagnostics
            FeedResponse<TestObject> firstPage = cosmosAsyncContainerFromClientWithPreferredRegions.queryItems(query, queryRequestOptions, TestObject.class)
                .byPage()
                .blockFirst();

            CosmosDiagnostics diagnostics = firstPage.getCosmosDiagnostics();
            assertContactedRegionCount(
                diagnostics,
                1,
                String.format(
                    "Expected query plan timeout to keep the data plane request in first preferred region <%s>",
                    this.preferredRegions.get(0)));
            // validate query plan timeout should not cause region failover
            assertContactedRegionsContain(
                diagnostics,
                this.preferredRegions.get(0),
                String.format(
                    "Expected query plan timeout diagnostics to include first preferred region <%s>",
                    this.preferredRegions.get(0)));
        } catch (Exception e) {
            fail("Except test to succeeded, " + e);
        } finally {
            queryPlanDelayRule.disable();
        }
    }

    @Test(groups = { "multi-master" }, dataProvider = "preferredRegionsConfigProvider", timeOut = TIMEOUT)
    public void addressRefreshHttpTimeoutWillDoCrossRegionRetryForReads(boolean shouldUsePreferredRegionsOnClient) {

        CosmosAsyncContainer resultantCosmosAsyncContainer;
        CosmosAsyncClient resultantCosmosAsyncClient;

        if (shouldUsePreferredRegionsOnClient) {
            resultantCosmosAsyncClient = this.clientWithPreferredRegions;
            resultantCosmosAsyncContainer = this.cosmosAsyncContainerFromClientWithPreferredRegions;
        } else {
            resultantCosmosAsyncClient = this.clientWithoutPreferredRegions;
            resultantCosmosAsyncContainer = this.cosmosAsyncContainerFromClientWithoutPreferredRegions;
        }

        if (BridgeInternal
            .getContextClient(resultantCosmosAsyncClient)
            .getConnectionPolicy()
            .getConnectionMode() != ConnectionMode.DIRECT) {
            throw new SkipException("queryPlanHttpTimeoutWillNotMarkRegionUnavailable() is only meant for DIRECT mode");
        }

        TestObject newItem = TestObject.create();
        resultantCosmosAsyncContainer.createItem(newItem).block();

        // create fault injection rules for address refresh
        FaultInjectionRule addressRefreshDelayRule = new FaultInjectionRuleBuilder("addressRefreshDelayRule")
            .condition(
                new FaultInjectionConditionBuilder()
                    .operationType(FaultInjectionOperationType.METADATA_REQUEST_ADDRESS_REFRESH)
                    .build())
            .result(
                FaultInjectionResultBuilders.getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
                    .delay(Duration.ofSeconds(11))
                    .times(4) // make sure it will exhaust the local region retry
                    .build()
            )
            .build();

        // Create gone rules to force address refresh will bound to happen
        FaultInjectionRule serverGoneRule = new FaultInjectionRuleBuilder("serverGoneRule")
            .condition(
                new FaultInjectionConditionBuilder()
                    .operationType(FaultInjectionOperationType.READ_ITEM)
                    .build())
            .result(
                FaultInjectionResultBuilders.getResultBuilder(FaultInjectionServerErrorType.GONE)
                    .times(4) // client is on session consistency, make sure exception will be bubbled up to goneAndRetry policy
                    .build()
            )
            .build();

        CosmosFaultInjectionHelper
            .configureFaultInjectionRules(
                resultantCosmosAsyncContainer,
                Arrays.asList(addressRefreshDelayRule, serverGoneRule)).block();
        try {
            CosmosItemResponse<TestObject> itemResponse = resultantCosmosAsyncContainer
                .readItem(newItem.getId(), new PartitionKey(newItem.getId()), TestObject.class)
                .block();

            assertThat(itemResponse).isNotNull();

            CosmosDiagnostics diagnostics = itemResponse.getDiagnostics();

            assertContactedRegionCount(
                diagnostics,
                2,
                String.format(
                    "Expected address refresh read retry diagnostics to include first two preferred regions <%s>",
                    getExpectedPreferredRegionsForMessage(2)));
            assertContactedRegionsContain(
                diagnostics,
                this.preferredRegions.get(0),
                String.format(
                    "Expected address refresh read retry diagnostics to include first preferred region <%s>",
                    this.preferredRegions.get(0)));
            assertContactedRegionsContain(
                diagnostics,
                this.preferredRegions.get(1),
                String.format(
                    "Expected address refresh read retry diagnostics to include second preferred region <%s>",
                    this.preferredRegions.get(1)));
        } finally {
            addressRefreshDelayRule.disable();
            serverGoneRule.disable();
        }
    }

    @Test(groups = { "multi-master" }, dataProvider = "preferredRegionsConfigProvider", timeOut = TIMEOUT)
    public void addressRefreshHttpTimeoutWillNotDoCrossRegionRetryForWrites(boolean shouldUsePreferredRegionsOnClient) {

        CosmosAsyncContainer resultantCosmosAsyncContainer;
        CosmosAsyncClient resultantCosmosAsyncClient;

        if (shouldUsePreferredRegionsOnClient) {
            resultantCosmosAsyncClient = this.clientWithPreferredRegions;
            resultantCosmosAsyncContainer = this.cosmosAsyncContainerFromClientWithPreferredRegions;
        } else {
            resultantCosmosAsyncClient = this.clientWithoutPreferredRegions;
            resultantCosmosAsyncContainer = this.cosmosAsyncContainerFromClientWithoutPreferredRegions;
        }

        if (BridgeInternal
            .getContextClient(resultantCosmosAsyncClient)
            .getConnectionPolicy()
            .getConnectionMode() != ConnectionMode.DIRECT) {
            throw new SkipException("queryPlanHttpTimeoutWillNotMarkRegionUnavailable() is only meant for DIRECT mode");
        }

        // create fault injection rules for address refresh
        FaultInjectionRule addressRefreshDelayRule = new FaultInjectionRuleBuilder("addressRefreshDelayRule")
            .condition(
                new FaultInjectionConditionBuilder()
                    .operationType(FaultInjectionOperationType.METADATA_REQUEST_ADDRESS_REFRESH)
                    .build())
            .result(
                FaultInjectionResultBuilders.getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
                    .delay(Duration.ofSeconds(11))
                    .times(4) // make sure it will exhaust the local region retry
                    .build()
            )
            .build();

        // Create gone rules to force address refresh will bound to happen
        FaultInjectionRule serverGoneRule = new FaultInjectionRuleBuilder("serverGoneRule")
            .condition(
                new FaultInjectionConditionBuilder()
                    .operationType(FaultInjectionOperationType.CREATE_ITEM)
                    .build())
            .result(
                FaultInjectionResultBuilders.getResultBuilder(FaultInjectionServerErrorType.GONE)
                    .times(4) // client is on session consistency, make sure exception will be bubbled up to goneAndRetry policy
                    .build()
            )
            .build();

        CosmosFaultInjectionHelper
            .configureFaultInjectionRules(
                resultantCosmosAsyncContainer,
                Arrays.asList(addressRefreshDelayRule, serverGoneRule)).block();
        try {
            TestObject newItem = TestObject.create();
            resultantCosmosAsyncContainer.createItem(newItem).block();
        } catch (CosmosException e) {
            CosmosDiagnostics diagnostics = e.getDiagnostics();
            assertContactedRegionCount(
                diagnostics,
                1,
                String.format(
                    "Expected address refresh write retry diagnostics to include only first preferred region <%s>",
                    this.preferredRegions.get(0)));
            assertContactedRegionsContain(
                diagnostics,
                this.preferredRegions.get(0),
                String.format(
                    "Expected address refresh write retry diagnostics to include first preferred region <%s>",
                    this.preferredRegions.get(0)));
            assertThat(e.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.REQUEST_TIMEOUT);
            assertThat(e.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT);
        } finally {
            addressRefreshDelayRule.disable();
            serverGoneRule.disable();
        }
    }

    @Test(groups = { "multi-master" }, dataProvider = "operationTypeProvider", timeOut = 8 * TIMEOUT)
    public void dataPlaneRequestHttpTimeout(
        FaultInjectionOperationType faultInjectionOperationType,
        OperationType operationType,
        boolean shouldRetryCrossRegion,
        boolean shouldUsePreferredRegionsOnClient) {

        CosmosAsyncContainer resultantCosmosAsyncContainer;
        CosmosAsyncClient resultantCosmosAsyncClient;

        if (shouldUsePreferredRegionsOnClient) {
            resultantCosmosAsyncClient = this.clientWithPreferredRegions;
            resultantCosmosAsyncContainer = this.cosmosAsyncContainerFromClientWithPreferredRegions;
        } else {
            resultantCosmosAsyncClient = this.clientWithoutPreferredRegions;
            resultantCosmosAsyncContainer = this.cosmosAsyncContainerFromClientWithoutPreferredRegions;
        }

        if (BridgeInternal
            .getContextClient(resultantCosmosAsyncClient)
            .getConnectionPolicy()
            .getConnectionMode() != ConnectionMode.GATEWAY) {
            throw new SkipException("queryPlanHttpTimeoutWillNotMarkRegionUnavailable() is only meant for GATEWAY mode");
        }

        TestObject newItem = TestObject.create();
        resultantCosmosAsyncContainer.createItem(newItem).block();
        FaultInjectionRule requestHttpTimeoutRule = new FaultInjectionRuleBuilder("requestHttpTimeoutRule" + UUID.randomUUID())
            .condition(
                new FaultInjectionConditionBuilder()
                    .operationType(faultInjectionOperationType)
                    .connectionType(FaultInjectionConnectionType.GATEWAY)
                    .build())
            .result(
                FaultInjectionResultBuilders.getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
                    .delay(Duration.ofSeconds(66))
                    .times(4) // make sure it will exhaust the local region retry
                    .build()
            )
            .build();

        CosmosFaultInjectionHelper.configureFaultInjectionRules(resultantCosmosAsyncContainer, Arrays.asList(requestHttpTimeoutRule)).block();

        try {
            if (shouldRetryCrossRegion) {
                try {
                    CosmosDiagnostics cosmosDiagnostics =
                        this.performDocumentOperation(
                            resultantCosmosAsyncContainer,
                            operationType,
                            newItem,
                            (testItem) -> new PartitionKey(testItem.getId()),
                            false
                        ).block();

                    assertContactedRegionCount(
                        cosmosDiagnostics,
                        this.preferredRegions.size(),
                        String.format(
                            "Expected data plane request timeout diagnostics to include all preferred regions <%s>",
                            this.preferredRegions));
                    assertContactedRegionsContainAll(
                        cosmosDiagnostics,
                        this.preferredRegions,
                        String.format(
                            "Expected data plane request timeout diagnostics to include all preferred regions <%s>",
                            this.preferredRegions));
                } catch (Exception e) {
                    fail("dataPlaneRequestHttpTimeout() should succeed for operationType " + operationType, e);
                }
            } else {
                CosmosDiagnostics cosmosDiagnostics = this.performDocumentOperation(
                    resultantCosmosAsyncContainer,
                    operationType,
                    newItem,
                    (testItem) -> new PartitionKey(testItem.getId()),
                    false
                ).block();

                assertThat(cosmosDiagnostics).isNotNull();
                assertThat(cosmosDiagnostics.getDiagnosticsContext()).isNotNull();

                System.out.println("dataPlaneRequestHttpTimeout() preferredRegions " + this.preferredRegions.toString() + " " + cosmosDiagnostics.getDiagnosticsContext().toJson());

                assertContactedRegionCount(
                    cosmosDiagnostics,
                    1,
                    String.format(
                        "Expected data plane write timeout diagnostics to include only first preferred region <%s>",
                        this.preferredRegions.get(0)));
                assertContactedRegionsContain(
                    cosmosDiagnostics,
                    this.preferredRegions.get(0),
                    String.format(
                        "Expected data plane write timeout diagnostics to include first preferred region <%s>",
                        this.preferredRegions.get(0)));
                assertThat(cosmosDiagnostics.getDiagnosticsContext().getStatusCode()).isEqualTo(HttpConstants.StatusCodes.REQUEST_TIMEOUT);
                assertThat(cosmosDiagnostics.getDiagnosticsContext().getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT);
            }
        } finally {
            requestHttpTimeoutRule.disable();
        }
    }

    @Test(groups = { "fast", "fi-multi-master", "multi-region" }, dataProvider = "leaseNotFoundArgProvider", timeOut = TIMEOUT * 2, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void dataPlaneRequestHitsLeaseNotFoundInFirstPreferredRegion(
        OperationType operationType,
        FaultInjectionOperationType faultInjectionOperationType,
        boolean shouldUsePreferredRegionsOnClient,
        boolean isReadMany,
        int hitLimit) {

        boolean shouldRetryCrossRegion = false;

        if (Utils.isWriteOperation(operationType) && this.serviceOrderedWriteableRegions.size() > 1) {
            shouldRetryCrossRegion = true;
        } else if (!Utils.isWriteOperation(operationType) && this.serviceOrderedReadableRegions.size() > 1) {
            shouldRetryCrossRegion = true;
        }

        CosmosAsyncClient resultantCosmosAsyncClient;

        if (shouldUsePreferredRegionsOnClient) {
            resultantCosmosAsyncClient = this.clientWithPreferredRegions;
        } else {
            resultantCosmosAsyncClient = this.clientWithoutPreferredRegions;
        }

        if (BridgeInternal
            .getContextClient(resultantCosmosAsyncClient)
            .getConnectionPolicy()
            .getConnectionMode() == ConnectionMode.GATEWAY) {
            throw new SkipException("leaseNotFound is only meant for Direct mode");
        }

        TestObject createdItem = TestObject.create();

        FaultInjectionRule leaseNotFoundFaultRule = new FaultInjectionRuleBuilder("leaseNotFound-" + UUID.randomUUID())
            .condition(
                new FaultInjectionConditionBuilder()
                    .operationType(faultInjectionOperationType)
                    .connectionType(FaultInjectionConnectionType.DIRECT)
                    .region(this.preferredRegions.get(0))
                    .build())
            .result(
                FaultInjectionResultBuilders.getResultBuilder(FaultInjectionServerErrorType.LEASE_NOT_FOUND)
                    .build()
            )
            .duration(Duration.ofMinutes(5))
            .hitLimit(hitLimit)
            .build();

        try (CosmosAsyncClient testClient = getClientBuilder()
            .preferredRegions(shouldUsePreferredRegionsOnClient ? this.preferredRegions : Collections.emptyList())
            .directMode()
            // required to force a quorum read irrespective of account consistency level
            .readConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED)
            .buildAsyncClient()) {

            CosmosAsyncContainer testContainer = getSharedSinglePartitionCosmosContainer(testClient);

            try {

                testContainer.createItem(createdItem).block();

                CosmosFaultInjectionHelper.configureFaultInjectionRules(testContainer, Arrays.asList(leaseNotFoundFaultRule)).block();

                CosmosDiagnostics cosmosDiagnostics
                    = this.performDocumentOperation(testContainer, operationType, createdItem, testItem -> new PartitionKey(testItem.getMypk()), isReadMany)
                          .block();

                if (shouldRetryCrossRegion) {
                    assertThat(cosmosDiagnostics).isNotNull();
                    assertThat(cosmosDiagnostics.getDiagnosticsContext()).isNotNull();

                    CosmosDiagnosticsContext diagnosticsContext = cosmosDiagnostics.getDiagnosticsContext();
                    List<String> expectedRegions = getExpectedServiceOrderedRegionsForMessage(operationType, 2);

                    assertContactedRegionCount(
                        diagnosticsContext,
                        2,
                        String.format(
                            "Expected lease not found retry diagnostics for operationType <%s> to include regions <%s>",
                            operationType,
                            expectedRegions));
                    assertThat(diagnosticsContext.getStatusCode()).isLessThan(HttpConstants.StatusCodes.BADREQUEST);
                    assertThat(diagnosticsContext.getDuration()).isLessThan(Duration.ofSeconds(10));
                } else {
                    assertThat(cosmosDiagnostics).isNotNull();
                    assertThat(cosmosDiagnostics.getDiagnosticsContext()).isNotNull();

                    CosmosDiagnosticsContext diagnosticsContext = cosmosDiagnostics.getDiagnosticsContext();

                    assertContactedRegionCount(
                        diagnosticsContext,
                        1,
                        String.format(
                            "Expected lease not found diagnostics for operationType <%s> to include only first preferred region <%s>",
                            operationType,
                            this.preferredRegions.get(0)));
                    assertThat(diagnosticsContext.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.SERVICE_UNAVAILABLE);
                    assertThat(diagnosticsContext.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.LEASE_NOT_FOUND);
                    assertThat(diagnosticsContext.getDuration()).isLessThan(Duration.ofSeconds(10));
                }

            } finally {
                leaseNotFoundFaultRule.disable();
                cleanUpContainer(testContainer);
            }
        }
    }

    @Test(groups = { "fast", "fi-multi-master", "multi-region" }, dataProvider = "leaseNotFoundArgProvider", timeOut = TIMEOUT * 2, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    // Inject 410-1022 and 429-3200 into the 2 replicas participating in quorum read
    // Validate that the client fails fast in the first preferred region and retries in the next region if possible (in a window <<60s)
    public void dataPlaneRequestHitsLeaseNotFoundAndResourceThrottleFirstPreferredRegion(
        OperationType operationType,
        FaultInjectionOperationType faultInjectionOperationType,
        boolean shouldUsePreferredRegionsOnClient,
        boolean isReadMany,
        int hitLimit) throws InterruptedException {

        boolean shouldRetryCrossRegion = false;

        if (Utils.isWriteOperation(operationType) && this.serviceOrderedWriteableRegions.size() > 1) {
            shouldRetryCrossRegion = true;
        } else if (!Utils.isWriteOperation(operationType) && this.serviceOrderedReadableRegions.size() > 1) {
            shouldRetryCrossRegion = true;
        }

        CosmosAsyncClient resultantCosmosAsyncClient;

        if (shouldUsePreferredRegionsOnClient) {
            resultantCosmosAsyncClient = this.clientWithPreferredRegions;
        } else {
            resultantCosmosAsyncClient = this.clientWithoutPreferredRegions;
        }

        if (BridgeInternal
            .getContextClient(resultantCosmosAsyncClient)
            .getConnectionPolicy()
            .getConnectionMode() == ConnectionMode.GATEWAY) {
            throw new SkipException("leaseNotFound is only meant for Direct mode");
        }

        TestObject createdItem = TestObject.create();

        FaultInjectionRule leaseNotFoundFaultRule = new FaultInjectionRuleBuilder("leaseNotFound-" + UUID.randomUUID())
            .condition(
                new FaultInjectionConditionBuilder()
                    .operationType(faultInjectionOperationType)
                    .connectionType(FaultInjectionConnectionType.DIRECT)
                    .region(this.preferredRegions.get(0))
                    .build())
            .result(
                FaultInjectionResultBuilders.getResultBuilder(FaultInjectionServerErrorType.LEASE_NOT_FOUND)
                    .build()
            )
            .duration(Duration.ofMinutes(5))
            .hitLimit(1)
            .build();

        FaultInjectionRule tooManyRequestsRule = new FaultInjectionRuleBuilder("too-many-requests-" + UUID.randomUUID())
            .condition(
                new FaultInjectionConditionBuilder()
                    .operationType(faultInjectionOperationType)
                    .connectionType(FaultInjectionConnectionType.DIRECT)
                    .region(this.preferredRegions.get(0))
                    .build())
            .result(
                FaultInjectionResultBuilders.getResultBuilder(FaultInjectionServerErrorType.TOO_MANY_REQUEST)
                    .build()
            )
            .duration(Duration.ofMinutes(5))
            .hitLimit(1)
            .build();

        CosmosAsyncClient testClient = getClientBuilder()
            .preferredRegions(shouldUsePreferredRegionsOnClient ? this.preferredRegions : Collections.emptyList())
            // required to force a quorum read irrespective of account consistency level
            .readConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED)
            .directMode()
            .buildAsyncClient();

        CosmosAsyncContainer testContainer = getSharedSinglePartitionCosmosContainer(testClient);

        try {

            testContainer.createItem(createdItem).block();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(testContainer, Arrays.asList(tooManyRequestsRule, leaseNotFoundFaultRule)).block();

            if (shouldRetryCrossRegion) {
                // add some delay so to allow the data can be replicated cross regions
                Thread.sleep(200);
            }

            CosmosDiagnostics cosmosDiagnostics
                = this.performDocumentOperation(testContainer, operationType, createdItem, testItem -> new PartitionKey(testItem.getMypk()), isReadMany)
                .block();

            if (shouldRetryCrossRegion) {
                assertThat(cosmosDiagnostics).isNotNull();
                assertThat(cosmosDiagnostics.getDiagnosticsContext()).isNotNull();

                CosmosDiagnosticsContext diagnosticsContext = cosmosDiagnostics.getDiagnosticsContext();
                List<String> expectedRegions = getExpectedServiceOrderedRegionsForMessage(operationType, 2);

                assertContactedRegionCount(
                    diagnosticsContext,
                    2,
                    String.format(
                        "Expected lease not found/resource throttle retry diagnostics for operationType <%s> to include regions <%s>",
                        operationType,
                        expectedRegions));
                assertThat(diagnosticsContext.getStatusCode()).isLessThan(HttpConstants.StatusCodes.BADREQUEST);

                if (operationType.isReadOnlyOperation()) {
                    validateCosmosDiagnosticsForMultiErrorCodesInQuorumRead(diagnosticsContext, Arrays.asList(410, 429));
                }
            } else {
                assertThat(cosmosDiagnostics).isNotNull();
                assertThat(cosmosDiagnostics.getDiagnosticsContext()).isNotNull();

                CosmosDiagnosticsContext diagnosticsContext = cosmosDiagnostics.getDiagnosticsContext();

                assertContactedRegionCount(
                    diagnosticsContext,
                    1,
                    String.format(
                        "Expected lease not found/resource throttle diagnostics for operationType <%s> to include only first preferred region <%s>",
                        operationType,
                        this.preferredRegions.get(0)));
                assertThat(diagnosticsContext.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.SERVICE_UNAVAILABLE);
                assertThat(diagnosticsContext.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.LEASE_NOT_FOUND);

                if (operationType.isReadOnlyOperation()) {
                    validateCosmosDiagnosticsForMultiErrorCodesInQuorumRead(diagnosticsContext, Arrays.asList(410, 429));
                }
            }

        } finally {
            leaseNotFoundFaultRule.disable();
            tooManyRequestsRule.disable();

            if (testClient != null) {
                cleanUpContainer(testContainer);
                testClient.close();
            }
        }
    }

    @Test(groups = { "multi-master" }, dataProvider = "channelAcquisitionExceptionArgProvider", timeOut = 8 * TIMEOUT, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void channelAcquisitionExceptionOnWrites(
        OperationType operationType,
        FaultInjectionOperationType faultInjectionOperationType,
        boolean shouldUsePreferredRegionsOnClient) {

        CosmosAsyncClient resultantCosmosAsyncClient;

        if (shouldUsePreferredRegionsOnClient) {
            resultantCosmosAsyncClient = this.clientWithPreferredRegions;
        } else {
            resultantCosmosAsyncClient = this.clientWithoutPreferredRegions;
        }

        if (BridgeInternal
            .getContextClient(resultantCosmosAsyncClient)
            .getConnectionPolicy()
            .getConnectionMode() == ConnectionMode.GATEWAY) {
            throw new SkipException("channelAcquisitionExceptionOnWrites() is only meant for Direct mode");
        }

        FaultInjectionRule channelAcquisitionExceptionRule = new FaultInjectionRuleBuilder("channelAcquisitionException" + UUID.randomUUID())
            .condition(
                new FaultInjectionConditionBuilder()
                    .operationType(faultInjectionOperationType)
                    .region(this.preferredRegions.get(0))
                    .build())
            .result(
                FaultInjectionResultBuilders.getResultBuilder(FaultInjectionServerErrorType.CONNECTION_DELAY)
                    .delay(Duration.ofSeconds(2))
                    .build()
            )
            .build();

        DirectConnectionConfig directConnectionConfig = DirectConnectionConfig.getDefaultConfig();
        directConnectionConfig.setConnectTimeout(Duration.ofSeconds(1));
        CosmosAsyncClient testClient = getClientBuilder()
            .preferredRegions(shouldUsePreferredRegionsOnClient ? this.preferredRegions : Collections.emptyList())
            .consistencyLevel(ConsistencyLevel.SESSION)
            .endpointDiscoveryEnabled(true)
            .multipleWriteRegionsEnabled(true)
            .directMode(directConnectionConfig)
            .buildAsyncClient();
        CosmosAsyncContainer testContainer = getSharedSinglePartitionCosmosContainer(testClient);
        CosmosFaultInjectionHelper.configureFaultInjectionRules(testContainer, Arrays.asList(channelAcquisitionExceptionRule)).block();

        try {
            TestObject createdItem = TestObject.create();
            testContainer.createItem(createdItem).block();

            // using a higher concurrency to force channelAcquisitionException to happen
            AtomicBoolean channelAcquisitionExceptionTriggeredRetryExists = new AtomicBoolean(false);
            Flux.range(1, 10)
                .flatMap(index ->
                    this.performDocumentOperation(
                        testContainer,
                        operationType,
                        createdItem,
                        (testItem) -> new PartitionKey(testItem.getMypk()),
                        false))
                .doOnNext(diagnostics -> {
                    // Validate that the first 2 preferred regions are contacted.
                    // If fewer than 2 preferred regions are configured, skip the test to avoid hiding misconfiguration.
                    if (this.preferredRegions == null || this.preferredRegions.size() < 2) {
                        throw new SkipException(
                            "Test requires at least 2 preferred regions but found: " + this.preferredRegions);
                    }
                    // since we have only injected connection delay error in one region, so we should eventually see
                    // 2-3 regions being contacted (at least 2, but not an excessive number during failover/retry)
                    // Using a range instead of strict equality to handle timing variations in CI environments
                    assertContactedRegionCountBetween(
                        diagnostics,
                        2,
                        3,
                        String.format(
                            "Expected channel acquisition diagnostics to include between 2 and 3 regions, including first two preferred regions <%s>",
                            getExpectedPreferredRegionsForMessage(2)));
                    assertContactedRegionsContainAll(
                        diagnostics,
                        getExpectedPreferredRegionsForMessage(2),
                        String.format(
                            "Expected channel acquisition diagnostics to include first two preferred regions <%s>",
                            getExpectedPreferredRegionsForMessage(2)));

                    if (isChannelAcquisitionExceptionTriggeredRegionRetryExists(diagnostics.toString())) {
                        channelAcquisitionExceptionTriggeredRetryExists.compareAndSet(false, true);
                    }
                })
                .doOnError(throwable -> {
                    if (throwable instanceof CosmosException) {
                        fail(
                            "All the requests should succeeded by retrying on another region. Diagnostics: "
                                + ((CosmosException)throwable).getDiagnostics());
                    }
                })
                .blockLast();

            assertThat(channelAcquisitionExceptionTriggeredRetryExists.get()).isTrue();
        } finally {
            channelAcquisitionExceptionRule.disable();

            if (testClient != null) {
                cleanUpContainer(testContainer);
                testClient.close();
            }
        }
    }

    private boolean isChannelAcquisitionExceptionTriggeredRegionRetryExists(String cosmosDiagnostics) {
        ObjectNode diagnosticsNode;
        try {
            diagnosticsNode = (ObjectNode) Utils.getSimpleObjectMapper().readTree(cosmosDiagnostics);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        JsonNode responseStatisticsList = diagnosticsNode.get("responseStatisticsList");
        assertThat(responseStatisticsList.isArray()).isTrue();
        assertThat(responseStatisticsList.size()).isGreaterThan(2);

        JsonNode lastStoreResultFromFailedRegion = responseStatisticsList.get(responseStatisticsList.size()-2).get("storeResult");
        assertThat(lastStoreResultFromFailedRegion).isNotNull();
        JsonNode exceptionMessageNode = lastStoreResultFromFailedRegion.get("exceptionMessage");
        assertThat(exceptionMessageNode).isNotNull();

        return exceptionMessageNode.asText().contains("ChannelAcquisitionException");
    }

    private Mono<CosmosDiagnostics> performDocumentOperation(
        CosmosAsyncContainer cosmosAsyncContainer,
        OperationType operationType,
        TestObject createdItem,
        Function<TestObject, PartitionKey> extractPartitionKeyFunc,
        boolean isReadMany) {

        try {
            if (operationType == OperationType.Query && isReadMany) {
                CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
                String query = String.format("SELECT * from c where c.id = '%s'", createdItem.getId());
                FeedResponse<TestObject> itemFeedResponse =
                    cosmosAsyncContainer.queryItems(query, queryRequestOptions, TestObject.class).byPage().blockFirst();
                return Mono.just(itemFeedResponse.getCosmosDiagnostics())
                    .onErrorResume(throwable -> {
                        if (throwable instanceof CosmosException) {
                            CosmosException cosmosException = (CosmosException) throwable;

                            return Mono.just(cosmosException.getDiagnostics());
                        }
                        return Mono.error(throwable);
                    });
            }

            if (operationType == OperationType.Read
                || operationType == OperationType.Delete
                || operationType == OperationType.Replace
                || operationType == OperationType.Create
                || operationType == OperationType.Patch
                || operationType == OperationType.Upsert
                || operationType == OperationType.Batch) {

                if (operationType == OperationType.Read) {
                    return cosmosAsyncContainer
                        .readItem(
                            createdItem.getId(),
                            extractPartitionKeyFunc.apply(createdItem),
                            TestObject.class
                        )
                        .map(itemResponse -> itemResponse.getDiagnostics())
                        .onErrorResume(throwable -> {
                            if (throwable instanceof CosmosException) {
                                CosmosException cosmosException = (CosmosException) throwable;

                                return Mono.just(cosmosException.getDiagnostics());
                            }
                            return Mono.error(throwable);
                        });
                }

                if (operationType == OperationType.Replace) {
                    return cosmosAsyncContainer
                        .replaceItem(
                            createdItem,
                            createdItem.getId(),
                            extractPartitionKeyFunc.apply(createdItem))
                        .map(itemResponse -> itemResponse.getDiagnostics())
                        .onErrorResume(throwable -> {
                            if (throwable instanceof CosmosException) {
                                CosmosException cosmosException = (CosmosException) throwable;

                                return Mono.just(cosmosException.getDiagnostics());
                            }
                            return Mono.error(throwable);
                        });
                }

                if (operationType == OperationType.Delete) {
                    return cosmosAsyncContainer.deleteItem(createdItem, null).map(itemResponse -> itemResponse.getDiagnostics())                        .onErrorResume(throwable -> {
                        if (throwable instanceof CosmosException) {
                            CosmosException cosmosException = (CosmosException) throwable;

                            return Mono.just(cosmosException.getDiagnostics());
                        }
                        return Mono.error(throwable);
                    });
                }

                if (operationType == OperationType.Create) {
                    return cosmosAsyncContainer.createItem(TestObject.create())
                        .map(itemResponse -> itemResponse.getDiagnostics())
                        .onErrorResume(throwable -> {
                        if (throwable instanceof CosmosException) {
                            CosmosException cosmosException = (CosmosException) throwable;

                            return Mono.just(cosmosException.getDiagnostics());
                        }
                        return Mono.error(throwable);
                    });
                }

                if (operationType == OperationType.Upsert) {
                    return cosmosAsyncContainer.upsertItem(TestObject.create())
                        .map(itemResponse -> itemResponse.getDiagnostics())
                        .onErrorResume(throwable -> {
                        if (throwable instanceof CosmosException) {
                            CosmosException cosmosException = (CosmosException) throwable;

                            return Mono.just(cosmosException.getDiagnostics());
                        }
                        return Mono.error(throwable);
                    });
                }

                if (operationType == OperationType.Patch) {
                    CosmosPatchOperations patchOperations = CosmosPatchOperations.create();
                    patchOperations.add("/newPath", "newPath");
                    return cosmosAsyncContainer
                        .patchItem(
                            createdItem.getId(),
                            extractPartitionKeyFunc.apply(createdItem),
                            patchOperations,
                            TestObject.class)
                        .map(itemResponse -> itemResponse.getDiagnostics())
                        .onErrorResume(throwable -> {
                            if (throwable instanceof CosmosException) {
                                CosmosException cosmosException = (CosmosException) throwable;

                                return Mono.just(cosmosException.getDiagnostics());
                            }
                            return Mono.error(throwable);
                        });
                }

                if (operationType == OperationType.Batch) {
                    CosmosBatch batch = CosmosBatch.createCosmosBatch(extractPartitionKeyFunc.apply(createdItem));

                    batch.upsertItemOperation(createdItem);
                    batch.readItemOperation(createdItem.getId());

                    return cosmosAsyncContainer.executeCosmosBatch(batch).map(cosmosBatchResponse -> cosmosBatchResponse.getDiagnostics())
                        .onErrorResume(throwable -> {
                            if (throwable instanceof CosmosException) {
                                CosmosException cosmosException = (CosmosException) throwable;

                                return Mono.just(cosmosException.getDiagnostics());
                            }
                            return Mono.error(throwable);
                        });
                }
            }

            if (operationType == OperationType.ReadFeed) {
                List<FeedRange> feedRanges = getFeedRangesWithRetry(
                    cosmosAsyncContainer,
                    "get feed ranges for client retry policy setup");
                CosmosChangeFeedRequestOptions changeFeedRequestOptions =
                    CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(feedRanges.get(0));

            FeedResponse<TestObject> firstPage =  cosmosAsyncContainer
                .queryChangeFeed(changeFeedRequestOptions, TestObject.class)
                .byPage()
                .blockFirst();
            return Mono.just(firstPage.getCosmosDiagnostics());
        }

            if (operationType == OperationType.Query) {
                return cosmosAsyncContainer
                    .readMany(
                        Arrays.asList(new CosmosItemIdentity(extractPartitionKeyFunc.apply(createdItem), createdItem.getId()), new CosmosItemIdentity(extractPartitionKeyFunc.apply(createdItem), createdItem.getId())),
                        TestObject.class)
                    .map(itemResponse -> itemResponse.getCosmosDiagnostics())
                    .onErrorResume(throwable -> {
                        if (throwable instanceof CosmosException) {
                            CosmosException cosmosException = (CosmosException) throwable;

                            return Mono.just(cosmosException.getDiagnostics());
                        }
                        return Mono.error(throwable);
                    });
            }

            throw new IllegalArgumentException("The operation type is not supported");
        } catch (CosmosException e) {
            return Mono.just(e.getDiagnostics());
        }
    }

    private void validateCosmosDiagnosticsForMultiErrorCodesInQuorumRead(
        CosmosDiagnosticsContext cosmosDiagnosticsContext,
        List<Integer> expectedStatusCodes) {

        assertThat(cosmosDiagnosticsContext).isNotNull();

        List<Integer> actualStatusCodes = cosmosDiagnosticsContext.getDiagnostics()
            .stream()
            .map(diagnostics -> cosmosDiagnosticsAccessor.getClientSideRequestStatistics(diagnostics))
            .flatMap(clientSideRequestStatisticsCollection -> clientSideRequestStatisticsCollection.stream().map(ClientSideRequestStatistics::getResponseStatisticsList))
            .map(storeResponseStatisticsCollection -> storeResponseStatisticsCollection.stream().map(storeResponseStatistics -> storeResponseStatistics.getStoreResult().getStoreResponseDiagnostics()).collect(Collectors.toCollection(ArrayList::new)))
            .map(storeResponseDiagnosticsCollection -> storeResponseDiagnosticsCollection.stream().map(StoreResponseDiagnostics::getStatusCode).collect(Collectors.toCollection(ArrayList::new)))
            .flatMap(ArrayList::stream)
            .collect(Collectors.toCollection(ArrayList::new));

        assertThat(actualStatusCodes).containsAll(expectedStatusCodes);
    }

    private AccountLevelLocationContext getAccountLevelLocationContext(DatabaseAccount databaseAccount, boolean writeOnly) {
        Iterator<DatabaseAccountLocation> locationIterator =
            writeOnly ? databaseAccount.getWritableLocations().iterator() : databaseAccount.getReadableLocations().iterator();

        List<String> serviceOrderedReadableRegions = new ArrayList<>();
        List<String> serviceOrderedWriteableRegions = new ArrayList<>();

        while (locationIterator.hasNext()) {
            DatabaseAccountLocation accountLocation = locationIterator.next();

            if (writeOnly) {
                serviceOrderedWriteableRegions.add(accountLocation.getName());
            } else {
                serviceOrderedReadableRegions.add(accountLocation.getName());
            }
        }

        return new AccountLevelLocationContext(
            serviceOrderedReadableRegions,
            serviceOrderedWriteableRegions);
    }

    private static void validate(AccountLevelLocationContext accountLevelLocationContext, boolean isWriteOnly) {

        AssertionsForClassTypes.assertThat(accountLevelLocationContext).isNotNull();

        if (isWriteOnly) {
            AssertionsForClassTypes.assertThat(accountLevelLocationContext.serviceOrderedWriteableRegions).isNotNull();
            AssertionsForClassTypes.assertThat(accountLevelLocationContext.serviceOrderedWriteableRegions.size()).isGreaterThanOrEqualTo(1);
        } else {
            AssertionsForClassTypes.assertThat(accountLevelLocationContext.serviceOrderedReadableRegions).isNotNull();
            AssertionsForClassTypes.assertThat(accountLevelLocationContext.serviceOrderedReadableRegions.size()).isGreaterThanOrEqualTo(1);
        }
    }

    private static class AccountLevelLocationContext {
        private final List<String> serviceOrderedReadableRegions;
        private final List<String> serviceOrderedWriteableRegions;

        public AccountLevelLocationContext(
            List<String> serviceOrderedReadableRegions,
            List<String> serviceOrderedWriteableRegions) {

            this.serviceOrderedReadableRegions = serviceOrderedReadableRegions;
            this.serviceOrderedWriteableRegions = serviceOrderedWriteableRegions;
        }
    }
}
