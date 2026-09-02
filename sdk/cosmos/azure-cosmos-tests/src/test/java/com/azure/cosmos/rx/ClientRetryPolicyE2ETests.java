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
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
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

    @Factory(dataProvider = "clientBuildersWithSessionConsistency")
    public ClientRetryPolicyE2ETests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @BeforeClass(groups = {"multi-master"}, timeOut = TIMEOUT)
    public void beforeClass() {
        CosmosAsyncClient dummy = getClientBuilder().buildAsyncClient();
        AsyncDocumentClient asyncDocumentClient = BridgeInternal.getContextClient(dummy);
        GlobalEndpointManager globalEndpointManager = asyncDocumentClient.getGlobalEndpointManager();

        DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();

        AccountLevelLocationContext accountLevelReadableLocationContext
            = getAccountLevelLocationContext(databaseAccount, false);

        validate(accountLevelReadableLocationContext, false);

        this.serviceOrderedReadableRegions = accountLevelReadableLocationContext.serviceOrderedReadableRegions
                .stream()
                .map(regionName -> regionName.toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());
        this.serviceOrderedWriteableRegions = accountLevelReadableLocationContext.serviceOrderedWriteableRegions
            .stream()
            .map(regionName -> regionName.toLowerCase(Locale.ROOT))
            .collect(Collectors.toList());
        this.preferredRegions = new ArrayList<>(this.serviceOrderedReadableRegions);

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

    @AfterClass(groups = {"multi-master"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
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
        TestItem newItem = TestItem.createNewItem();

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
            FeedResponse<TestItem> firstPage = cosmosAsyncContainerFromClientWithPreferredRegions.queryItems(query, queryRequestOptions, TestItem.class)
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

        TestItem newItem = TestItem.createNewItem();
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
            CosmosItemResponse<TestItem> itemResponse = resultantCosmosAsyncContainer
                .readItem(newItem.getId(), new PartitionKey(newItem.getId()), TestItem.class)
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
            TestItem newItem = TestItem.createNewItem();
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

        TestItem newItem = TestItem.createNewItem();
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
                            (testItem) -> new PartitionKey(testItem.getId())
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
                try {
                    this.performDocumentOperation(
                        resultantCosmosAsyncContainer,
                        operationType,
                        newItem,
                        (testItem) -> new PartitionKey(testItem.getId())
                    ).block();
                    fail("dataPlaneRequestHttpTimeout() should have failed for operationType " + operationType);
                } catch (CosmosException e) {
                    System.out.println("dataPlaneRequestHttpTimeout() preferredRegions " + this.preferredRegions.toString() + " " + e.getDiagnostics());
                    assertThat(e.getDiagnostics().getContactedRegionNames().size()).isEqualTo(1);
                    assertThat(e.getDiagnostics().getContactedRegionNames()).contains(this.preferredRegions.get(0));
                    assertThat(e.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.REQUEST_TIMEOUT);
                    assertThat(e.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT);
                }
            }
        } finally {
            requestHttpTimeoutRule.disable();
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
            TestItem createdItem = TestItem.createNewItem();
            testContainer.createItem(createdItem).block();

            // using a higher concurrency to force channelAcquisitionException to happen
            AtomicBoolean channelAcquisitionExceptionTriggeredRetryExists = new AtomicBoolean(false);
            Flux.range(1, 10)
                .flatMap(index ->
                    this.performDocumentOperation(
                        testContainer,
                        operationType,
                        createdItem,
                        (testItem) -> new PartitionKey(testItem.getMypk())))
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
        TestItem createdItem,
        Function<TestItem, PartitionKey> extractPartitionKeyFunc) {
        if (operationType == OperationType.Query) {
            CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
            String query = String.format("SELECT * from c where c.id = '%s'", createdItem.getId());
            FeedResponse<TestItem> itemFeedResponse =
                cosmosAsyncContainer.queryItems(query, queryRequestOptions, TestItem.class).byPage().blockFirst();
            return Mono.just(itemFeedResponse.getCosmosDiagnostics());
        }

        if (operationType == OperationType.Read
            || operationType == OperationType.Delete
            || operationType == OperationType.Replace
            || operationType == OperationType.Create
            || operationType == OperationType.Patch
            || operationType == OperationType.Upsert) {

            if (operationType == OperationType.Read) {
                return cosmosAsyncContainer
                    .readItem(
                        createdItem.getId(),
                        extractPartitionKeyFunc.apply(createdItem),
                        TestItem.class
                    )
                    .map(itemResponse -> itemResponse.getDiagnostics());
            }

            if (operationType == OperationType.Replace) {
                return cosmosAsyncContainer
                    .replaceItem(
                        createdItem,
                        createdItem.getId(),
                        extractPartitionKeyFunc.apply(createdItem))
                    .map(itemResponse -> itemResponse.getDiagnostics());
            }

            if (operationType == OperationType.Delete) {
                return cosmosAsyncContainer.deleteItem(createdItem, null).map(itemResponse -> itemResponse.getDiagnostics());
            }

            if (operationType == OperationType.Create) {
                return cosmosAsyncContainer.createItem(TestItem.createNewItem()).map(itemResponse -> itemResponse.getDiagnostics());
            }

            if (operationType == OperationType.Upsert) {
                return cosmosAsyncContainer.upsertItem(TestItem.createNewItem()).map(itemResponse -> itemResponse.getDiagnostics());
            }

            if (operationType == OperationType.Patch) {
                CosmosPatchOperations patchOperations =
                    CosmosPatchOperations
                        .create()
                        .add("newPath", "newPath");
                return cosmosAsyncContainer
                    .patchItem(
                        createdItem.getId(),
                        extractPartitionKeyFunc.apply(createdItem),
                        patchOperations,
                        TestItem.class)
                    .map(itemResponse -> itemResponse.getDiagnostics());
            }
        }

            if (operationType == OperationType.ReadFeed) {
                List<FeedRange> feedRanges = getFeedRangesWithRetry(
                    cosmosAsyncContainer,
                    "get feed ranges for client retry policy setup");
                CosmosChangeFeedRequestOptions changeFeedRequestOptions =
                    CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(feedRanges.get(0));

            FeedResponse<TestItem> firstPage =  cosmosAsyncContainer
                .queryChangeFeed(changeFeedRequestOptions, TestItem.class)
                .byPage()
                .blockFirst();
            return Mono.just(firstPage.getCosmosDiagnostics());
        }

        throw new IllegalArgumentException("The operation type is not supported");
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
