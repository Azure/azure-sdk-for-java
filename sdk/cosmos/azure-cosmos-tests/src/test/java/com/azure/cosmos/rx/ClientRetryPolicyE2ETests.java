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
import com.azure.cosmos.TestObject;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.Utils;
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
import com.azure.cosmos.test.faultinjection.FaultInjectionEndpointBuilder;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.fail;

public class ClientRetryPolicyE2ETests extends TestSuiteBase {
    private CosmosAsyncClient clientWithPreferredRegions;
    private CosmosAsyncContainer cosmosAsyncContainerFromClientWithPreferredRegions;
    private CosmosAsyncClient clientWithoutPreferredRegions;
    private CosmosAsyncContainer cosmosAsyncContainerFromClientWithoutPreferredRegions;
    private List<String> preferredRegions;
    private List<String> serviceOrderedReadableRegions;
    private List<String> serviceOrderedWriteableRegions;

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
            // OperationType, FaultInjectionOperationType, shouldUsePreferredRegionsOnClient, isReadMany
            { OperationType.Read, FaultInjectionOperationType.READ_ITEM, true, false },
            { OperationType.Query, FaultInjectionOperationType.QUERY_ITEM, true, false },
            { OperationType.Create, FaultInjectionOperationType.CREATE_ITEM, true, false },
            { OperationType.Patch, FaultInjectionOperationType.PATCH_ITEM, true, false },
            { OperationType.Replace, FaultInjectionOperationType.REPLACE_ITEM, true, false },
            { OperationType.Delete, FaultInjectionOperationType.DELETE_ITEM, true, false },
            { OperationType.Upsert, FaultInjectionOperationType.UPSERT_ITEM, true, false },
            { OperationType.ReadFeed, FaultInjectionOperationType.READ_FEED_ITEM, true, false },
            { OperationType.Batch, FaultInjectionOperationType.BATCH_ITEM, true, false },
            { OperationType.Query, FaultInjectionOperationType.QUERY_ITEM, true, true },
            { OperationType.Read, FaultInjectionOperationType.READ_ITEM, false, false },
            { OperationType.Query, FaultInjectionOperationType.QUERY_ITEM, false, false },
            { OperationType.Create, FaultInjectionOperationType.CREATE_ITEM, false, false },
            { OperationType.Patch, FaultInjectionOperationType.PATCH_ITEM, false, false },
            { OperationType.Replace, FaultInjectionOperationType.REPLACE_ITEM, false, false },
            { OperationType.Delete, FaultInjectionOperationType.DELETE_ITEM, false, false },
            { OperationType.Upsert, FaultInjectionOperationType.UPSERT_ITEM, false, false },
            { OperationType.ReadFeed, FaultInjectionOperationType.READ_FEED_ITEM, false, false },
            { OperationType.Batch, FaultInjectionOperationType.BATCH_ITEM, false, false },
            { OperationType.Query, FaultInjectionOperationType.QUERY_ITEM, false, true }
        };
    }

    @Factory(dataProvider = "clientBuildersWithSessionConsistency")
    public ClientRetryPolicyE2ETests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @BeforeClass(groups = {"multi-master", "fast", "fi-multi-master", "multi-region"}, timeOut = TIMEOUT)
    public void beforeClass() {
        CosmosAsyncClient dummy = getClientBuilder().buildAsyncClient();
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
            .consistencyLevel(ConsistencyLevel.SESSION)
            .endpointDiscoveryEnabled(true)
            .multipleWriteRegionsEnabled(true)
            .buildAsyncClient();

        this.clientWithoutPreferredRegions = getClientBuilder()
            .consistencyLevel(ConsistencyLevel.SESSION)
            .endpointDiscoveryEnabled(true)
            .multipleWriteRegionsEnabled(true)
            .buildAsyncClient();

        this.cosmosAsyncContainerFromClientWithPreferredRegions = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(clientWithPreferredRegions);
        this.cosmosAsyncContainerFromClientWithoutPreferredRegions = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(clientWithoutPreferredRegions);
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

            assertThat(firstPage.getCosmosDiagnostics().getContactedRegionNames().size()).isEqualTo(1);
            // validate query plan timeout should not cause region failover
            assertThat(firstPage.getCosmosDiagnostics().getContactedRegionNames()).contains(this.preferredRegions.get(0));
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

            assertThat(diagnostics.getContactedRegionNames().size()).isEqualTo(2);
            assertThat(diagnostics.getContactedRegionNames()).contains(this.preferredRegions.get(0));
            assertThat(diagnostics.getContactedRegionNames()).contains(this.preferredRegions.get(1));
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
            assertThat(e.getDiagnostics().getContactedRegionNames().size()).isEqualTo(1);
            assertThat(e.getDiagnostics().getContactedRegionNames()).contains(this.preferredRegions.get(0));
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

                    assertThat(cosmosDiagnostics.getContactedRegionNames().size()).isEqualTo(this.preferredRegions.size());
                    assertThat(cosmosDiagnostics.getContactedRegionNames().containsAll(this.preferredRegions)).isTrue();
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

                assertThat(cosmosDiagnostics.getContactedRegionNames().size()).isEqualTo(1);
                assertThat(cosmosDiagnostics.getContactedRegionNames()).contains(this.preferredRegions.get(0));
                assertThat(cosmosDiagnostics.getDiagnosticsContext().getStatusCode()).isEqualTo(HttpConstants.StatusCodes.REQUEST_TIMEOUT);
                assertThat(cosmosDiagnostics.getDiagnosticsContext().getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT);
            }
        } finally {
            requestHttpTimeoutRule.disable();
        }
    }

    @Test(groups = { "fast", "fi-multi-master", "multi-region" }, dataProvider = "leaseNotFoundArgProvider", timeOut = TIMEOUT)
    public void dataPlaneRequestHitsLeaseNotFoundInFirstPreferredRegion(
        OperationType operationType,
        FaultInjectionOperationType faultInjectionOperationType,
        boolean shouldUsePreferredRegionsOnClient,
        boolean isReadMany) {

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
                    .build())
            .result(
                FaultInjectionResultBuilders.getResultBuilder(FaultInjectionServerErrorType.LEASE_NOT_FOUND)
                    .build()
            )
            .duration(Duration.ofMinutes(5))
            .hitLimit(1)
            .build();

        CosmosAsyncClient testClient = getClientBuilder()
            .preferredRegions(shouldUsePreferredRegionsOnClient ? this.preferredRegions : Collections.emptyList())
            .directMode()
            .buildAsyncClient();
        CosmosAsyncContainer testContainer = getSharedSinglePartitionCosmosContainer(testClient);

        try {

            testContainer.createItem(createdItem).block();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(testContainer, Arrays.asList(leaseNotFoundFaultRule)).block();

            Instant timeStart = Instant.now();

            CosmosDiagnostics cosmosDiagnostics
                = this.performDocumentOperation(testContainer, operationType, createdItem, testItem -> new PartitionKey(testItem.getMypk()), isReadMany).block();

            Instant timeEnd = Instant.now();

            if (shouldRetryCrossRegion) {
                assertThat(cosmosDiagnostics).isNotNull();
                assertThat(cosmosDiagnostics.getDiagnosticsContext()).isNotNull();

                CosmosDiagnosticsContext diagnosticsContext = cosmosDiagnostics.getDiagnosticsContext();

                assertThat(diagnosticsContext.getContactedRegionNames().size()).isEqualTo(2);
                assertThat(diagnosticsContext.getStatusCode()).isLessThan(HttpConstants.StatusCodes.BADREQUEST);
            } else {
                assertThat(cosmosDiagnostics).isNotNull();
                assertThat(cosmosDiagnostics.getDiagnosticsContext()).isNotNull();

                CosmosDiagnosticsContext diagnosticsContext = cosmosDiagnostics.getDiagnosticsContext();

                assertThat(diagnosticsContext.getContactedRegionNames().size()).isEqualTo(1);
                assertThat(diagnosticsContext.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.SERVICE_UNAVAILABLE);
                assertThat(diagnosticsContext.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.LEASE_NOT_FOUND);
            }

            assertThat(Duration.between(timeStart, timeEnd)).isLessThan(Duration.ofSeconds(5));

        } finally {
            leaseNotFoundFaultRule.disable();

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
                    // since we have only injected connection delay error in one region, so we should only see 2 regions being contacted eventually
                    assertThat(diagnostics.getContactedRegionNames().size()).isEqualTo(2);
                    assertThat(diagnostics.getContactedRegionNames().containsAll(this.preferredRegions.subList(0, 2))).isTrue();

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
                    return cosmosAsyncContainer.createItem(TestObject.create()).map(itemResponse -> itemResponse.getDiagnostics())                        .onErrorResume(throwable -> {
                        if (throwable instanceof CosmosException) {
                            CosmosException cosmosException = (CosmosException) throwable;

                            return Mono.just(cosmosException.getDiagnostics());
                        }
                        return Mono.error(throwable);
                    });
                }

                if (operationType == OperationType.Upsert) {
                    return cosmosAsyncContainer.upsertItem(TestObject.create()).map(itemResponse -> itemResponse.getDiagnostics())                        .onErrorResume(throwable -> {
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
                List<FeedRange> feedRanges = cosmosAsyncContainer.getFeedRanges().block();
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
