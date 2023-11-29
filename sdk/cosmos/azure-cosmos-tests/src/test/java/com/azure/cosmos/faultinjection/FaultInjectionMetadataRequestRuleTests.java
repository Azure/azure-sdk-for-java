// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfigBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.MetadataDiagnosticsContext;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionEndpointBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.testng.AssertJUnit.fail;

public class FaultInjectionMetadataRequestRuleTests extends FaultInjectionTestBase {
    private CosmosAsyncClient client;
    private CosmosAsyncContainer cosmosAsyncContainer;
    private List<String> readPreferredLocations;
    private List<String> writePreferredLocations;

    @Factory(dataProvider = "simpleClientBuildersWithJustDirectTcp")
    public FaultInjectionMetadataRequestRuleTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @DataProvider(name = "operationTypeProvider")
    public static Object[][] operationTypeProvider() {
        return new Object[][]{
            // FaultInjectionOperationType, OperationType
            { FaultInjectionOperationType.READ_ITEM, OperationType.Read },
            { FaultInjectionOperationType.REPLACE_ITEM, OperationType.Replace },
            { FaultInjectionOperationType.CREATE_ITEM, OperationType.Create },
            { FaultInjectionOperationType.DELETE_ITEM, OperationType.Delete },
            { FaultInjectionOperationType.QUERY_ITEM, OperationType.Query },
            { FaultInjectionOperationType.PATCH_ITEM, OperationType.Patch }
        };
    }

    @DataProvider(name = "partitionKeyRangesArgProvider")
    public static Object[][] partitionKeyRangesArgProvider() {
        return new Object[][]{
            // FaultInjectionServerErrorType, delay duration, ruleApplyLimitPerOperation
            { FaultInjectionServerErrorType.CONNECTION_DELAY, Duration.ofSeconds(50), 1 },
            { FaultInjectionServerErrorType.CONNECTION_DELAY, Duration.ofSeconds(50), Integer.MAX_VALUE },
            { FaultInjectionServerErrorType.RESPONSE_DELAY, Duration.ofSeconds(11), 1 },
            { FaultInjectionServerErrorType.RESPONSE_DELAY, Duration.ofSeconds(11), Integer.MAX_VALUE }
        };
    }

    @BeforeClass(groups = { "multi-region", "multi-master" }, timeOut = TIMEOUT)
    public void beforeClass() {
        this.client = getClientBuilder().buildAsyncClient();
        AsyncDocumentClient asyncDocumentClient = BridgeInternal.getContextClient(this.client);
        GlobalEndpointManager globalEndpointManager = asyncDocumentClient.getGlobalEndpointManager();

        DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();

        Map<String, String> readRegionMap = this.getRegionMap(databaseAccount, false);
        Map<String, String> writeRegionMap = this.getRegionMap(databaseAccount, true);

        this.cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(this.client);

        // This test runs against a real account
        // Creating collections can take some time in the remote region
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // create a client with preferred regions
        this.readPreferredLocations = readRegionMap.keySet().stream().collect(Collectors.toList());
        this.writePreferredLocations = writeRegionMap.keySet().stream().collect(Collectors.toList());
    }

    @Test(groups = { "multi-region" }, timeOut = 20 * TIMEOUT)
    public void faultInjectionServerErrorRuleTests_AddressRefresh_ConnectionDelay() throws JsonProcessingException {

        // Test to validate if there is http connection exception for address refresh,
        // SDK will make the region unavailable and retry the request in another region.

        // We need to create a new client because client may have marked region unavailable in other tests
        // which can impact the test result
        CosmosAsyncClient testClient = getClientBuilder()
            .contentResponseOnWriteEnabled(true)
            .preferredRegions(readPreferredLocations)
            .buildAsyncClient();

        CosmosAsyncContainer container =
            testClient
                .getDatabase(this.cosmosAsyncContainer.getDatabase().getId())
                .getContainer(this.cosmosAsyncContainer.getId());

        String addressRefreshConnectionDelay = "AddressRefresh-connectionDelay-" + UUID.randomUUID();
        FaultInjectionRule addressRefreshConnectionDelayRule =
            new FaultInjectionRuleBuilder(addressRefreshConnectionDelay)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .region(this.readPreferredLocations.get(0))
                        .operationType(FaultInjectionOperationType.METADATA_REQUEST_ADDRESS_REFRESH)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.CONNECTION_DELAY)
                        .delay(Duration.ofSeconds(50)) // to simulate http connection timeout
                        .times(4) // make sure to exhaust local region retries
                        .build()
                )
                .duration(Duration.ofMinutes(10))
                .build();

        FaultInjectionRule dataOperationGoneRule =
            new FaultInjectionRuleBuilder("DataOperation-gone-" + UUID.randomUUID())
                .condition(
                    new FaultInjectionConditionBuilder()
                        .region(this.readPreferredLocations.get(0))
                        .operationType(FaultInjectionOperationType.READ_ITEM)
                        .build())
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.GONE)
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();
        try {
            TestItem createdItem = TestItem.createNewItem();
            container.createItem(createdItem).block();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(
                    container,
                    Arrays.asList(addressRefreshConnectionDelayRule, dataOperationGoneRule))
                .block();

            try {
                CosmosDiagnostics cosmosDiagnostics =
                    container
                        .readItem(createdItem.getId(), new PartitionKey(createdItem.getId()), JsonNode.class)
                        .block()
                        .getDiagnostics();
                assertThat(cosmosDiagnostics.getContactedRegionNames().size()).isEqualTo(2);
                if (this.readPreferredLocations.size() == 2) {
                    // when preferred regions is 2
                    // Due to issue https://github.com/Azure/azure-sdk-for-java/issues/35779, the request mark the region unavailable will retry
                    // in the unavailable region again, hence the addressRefresh fault injection will be happened 4 times
                    validateFaultInjectionRuleAppliedForAddressResolution(cosmosDiagnostics, addressRefreshConnectionDelay, 4);
                } else {
                    validateFaultInjectionRuleAppliedForAddressResolution(cosmosDiagnostics, addressRefreshConnectionDelay, 3);
                }
            } catch (CosmosException e) {
                fail("Request should be able to succeed by retrying in another region. " + e.getDiagnostics());
            }

            addressRefreshConnectionDelayRule.disable();
            dataOperationGoneRule.disable();

            // issue another request to verify SDK has marked the first region unavailable
            CosmosDiagnostics cosmosDiagnostics =
                container
                    .readItem(createdItem.getId(), new PartitionKey(createdItem.getId()), JsonNode.class)
                    .block()
                    .getDiagnostics();
            assertThat(cosmosDiagnostics.getContactedRegionNames().size()).isEqualTo(1);
            assertThat(
                cosmosDiagnostics
                    .getContactedRegionNames()
                    .containsAll(Arrays.asList(this.readPreferredLocations.get(1).toLowerCase())))
                .isTrue();
        } finally {
            addressRefreshConnectionDelayRule.disable();
            dataOperationGoneRule.disable();
            safeClose(testClient);
        }
    }

    @Test(groups = { "multi-master" }, dataProvider = "operationTypeProvider", timeOut = 4 * TIMEOUT)
    public void faultInjectionServerErrorRuleTests_AddressRefresh_ResponseDelay(
        FaultInjectionOperationType faultInjectionOperationType,
        OperationType operationType) throws JsonProcessingException {

        // Test to validate if there is http request timeout for address refresh,
        // SDK will retry at least 2 times, and SDK will fail the request as no cross region retry for addressRefresh on timeout

        // We need to create a new client because client may have marked region unavailable in other tests
        // which can impact the test result
        CosmosAsyncClient testClient = getClientBuilder()
            .contentResponseOnWriteEnabled(true)
            .preferredRegions(readPreferredLocations)
            .buildAsyncClient();

        CosmosAsyncContainer container =
            testClient
                .getDatabase(this.cosmosAsyncContainer.getDatabase().getId())
                .getContainer(this.cosmosAsyncContainer.getId());

        String addressRefreshResponseDelay = "AddressRefresh-responseDelay-" + UUID.randomUUID();
        FaultInjectionRule addressRefreshResponseDelayRule =
            new FaultInjectionRuleBuilder(addressRefreshResponseDelay)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .region(this.readPreferredLocations.get(0))
                        .operationType(FaultInjectionOperationType.METADATA_REQUEST_ADDRESS_REFRESH)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
                        .delay(Duration.ofSeconds(11)) // to simulate http request timeout
                        .times(3)
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        FaultInjectionRule dataOperationGoneRule =
            new FaultInjectionRuleBuilder("DataOperation-gone-" + UUID.randomUUID())
                .condition(
                    new FaultInjectionConditionBuilder()
                        .region(this.readPreferredLocations.get(0))
                        .operationType(faultInjectionOperationType)
                        .build())
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.GONE)
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();
        try {

            TestItem createdItem = TestItem.createNewItem();
            container.createItem(createdItem).block();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(
                    container,
                    Arrays.asList(addressRefreshResponseDelayRule, dataOperationGoneRule))
                .block();

            CosmosDiagnostics cosmosDiagnostics =
                this.performDocumentOperation(container, operationType, createdItem);

            assertThat(cosmosDiagnostics.getContactedRegionNames().size()).isEqualTo(1);
            assertThat(
                cosmosDiagnostics
                    .getContactedRegionNames()
                    .containsAll(Arrays.asList(this.readPreferredLocations.get(0).toLowerCase())))
                .isTrue();

            assertThat(cosmosDiagnostics.getDiagnosticsContext().getStatusCode())
                .isEqualTo(HttpConstants.StatusCodes.REQUEST_TIMEOUT);
            assertThat(cosmosDiagnostics.getDiagnosticsContext().getSubStatusCode())
                .isEqualTo(HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT);
            validateFaultInjectionRuleAppliedForAddressResolution(cosmosDiagnostics, addressRefreshResponseDelay, 3);
        } finally {
            addressRefreshResponseDelayRule.disable();
            dataOperationGoneRule.disable();
            safeClose(testClient);
        }
    }

    @Test(groups = { "multi-region" }, timeOut = 4 * TIMEOUT)
    public void faultInjectionServerErrorRuleTests_AddressRefresh_byPartition() {

        // We need to create a new client because client may have marked region unavailable in other tests
        // which can impact the test result
        CosmosAsyncClient testClient = getClientBuilder()
            .contentResponseOnWriteEnabled(true)
            .preferredRegions(readPreferredLocations)
            .buildAsyncClient();

        CosmosAsyncContainer container =
            testClient
                .getDatabase(this.cosmosAsyncContainer.getDatabase().getId())
                .getContainer(this.cosmosAsyncContainer.getId());

        // first create few documents
        for (int i = 0; i < 10; i++) {
            container.createItem(TestItem.createNewItem()).block();
        }

        List<FeedRange> feedRanges = container.getFeedRanges().block();
        assertThat(feedRanges.size()).isGreaterThan(1);

        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        cosmosQueryRequestOptions.setFeedRange(feedRanges.get(0));
        String query = "select * from c";
        TestItem itemOnFeedRange1 = container.queryItems(query, cosmosQueryRequestOptions, TestItem.class)
            .byPage(1)
            .blockFirst()
            .getResults()
            .get(0);

        cosmosQueryRequestOptions.setFeedRange(feedRanges.get(1));
        TestItem itemOnFeedRange2 = container.queryItems(query, cosmosQueryRequestOptions, TestItem.class)
            .byPage(1)
            .blockFirst()
            .getResults()
            .get(0);

        // Test to validate address refresh rule can be scoped to partition
        String addressRefreshResponseDelay = "AddressRefresh-responseDelay-" + UUID.randomUUID();
        FaultInjectionRule addressRefreshResponseDelayRule =
            new FaultInjectionRuleBuilder(addressRefreshResponseDelay)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(FaultInjectionOperationType.METADATA_REQUEST_ADDRESS_REFRESH)
                        .endpoints(new FaultInjectionEndpointBuilder(feedRanges.get(0)).build())
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
                        .delay(Duration.ofSeconds(11)) // to simulate http request timeout
                        .times(3)
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        FaultInjectionRule dataOperationGoneRule =
            new FaultInjectionRuleBuilder("DataOperation-gone-" + UUID.randomUUID())
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(FaultInjectionOperationType.READ_ITEM)
                        .build())
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.GONE)
                        .times(1)
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        try {
            CosmosFaultInjectionHelper.configureFaultInjectionRules(
                    container,
                    Arrays.asList(addressRefreshResponseDelayRule, dataOperationGoneRule))
                .block();

            // validate for request on feed range 0, it will fail
            try {
                CosmosDiagnostics cosmosDiagnostics =
                    container
                        .readItem(itemOnFeedRange1.getId(), new PartitionKey(itemOnFeedRange1.getId()), JsonNode.class)
                        .block()
                        .getDiagnostics();

                fail("Item on feed range 1 should have failed. " + cosmosDiagnostics);
            } catch (CosmosException e) {
                // no-op
            }

            try {
                container
                    .readItem(itemOnFeedRange2.getId(), new PartitionKey(itemOnFeedRange2.getId()), JsonNode.class)
                    .block()
                    .getDiagnostics();

            } catch (CosmosException e) {
                fail("Item on feed range 2 should have succeeded. " + e.getDiagnostics());
            }

        } finally {
            addressRefreshResponseDelayRule.disable();
            dataOperationGoneRule.disable();
            safeClose(testClient);
        }
    }

    @Test(groups = { "multi-region" }, timeOut = 4 * TIMEOUT)
    public void faultInjectionServerErrorRuleTests_AddressRefresh_TooManyRequest() throws JsonProcessingException {

        // We need to create a new client because client may have marked region unavailable in other tests
        // which can impact the test result
        CosmosAsyncClient testClient = getClientBuilder()
            .contentResponseOnWriteEnabled(true)
            .preferredRegions(readPreferredLocations)
            .buildAsyncClient();

        CosmosAsyncContainer container =
            testClient
                .getDatabase(this.cosmosAsyncContainer.getDatabase().getId())
                .getContainer(this.cosmosAsyncContainer.getId());

        // Test to SDK will retry 429 for address refresh
        String addressRefreshTooManyRequest = "AddressRefresh-tooManyRequest-" + UUID.randomUUID();
        FaultInjectionRule addressRefreshTooManyRequestRule =
            new FaultInjectionRuleBuilder(addressRefreshTooManyRequest)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .region(this.readPreferredLocations.get(0))
                        .operationType(FaultInjectionOperationType.METADATA_REQUEST_ADDRESS_REFRESH)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.TOO_MANY_REQUEST)
                        .times(1)
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        FaultInjectionRule dataOperationGoneRule =
            new FaultInjectionRuleBuilder("DataOperation-gone-" + UUID.randomUUID())
                .condition(
                    new FaultInjectionConditionBuilder()
                        .region(this.readPreferredLocations.get(0))
                        .operationType(FaultInjectionOperationType.READ_ITEM)
                        .build())
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.GONE)
                        .times(1) // to make sure the address refresh will be at least triggered once
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();
        try {
            TestItem createdItem = TestItem.createNewItem();
            container.createItem(createdItem).block();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(
                    container,
                    Arrays.asList(addressRefreshTooManyRequestRule, dataOperationGoneRule))
                .block();

            CosmosDiagnostics cosmosDiagnostics =
                container.readItem(createdItem.getId(), new PartitionKey(createdItem.getId()), JsonNode.class).block().getDiagnostics();

            assertThat(cosmosDiagnostics.getContactedRegionNames().size()).isEqualTo(1);
            assertThat(cosmosDiagnostics.getContactedRegionNames().containsAll(Arrays.asList(this.readPreferredLocations.get(0).toLowerCase()))).isTrue();
            validateFaultInjectionRuleAppliedForAddressResolution(cosmosDiagnostics, addressRefreshTooManyRequest, 1);
        } finally {
            addressRefreshTooManyRequestRule.disable();
            dataOperationGoneRule.disable();
            safeClose(testClient);
        }
    }

    @Test(groups = { "multi-master" }, dataProvider = "partitionKeyRangesArgProvider", timeOut = 40 * TIMEOUT)
    public void faultInjectionServerErrorRuleTests_PartitionKeyRanges_DelayError(
        FaultInjectionServerErrorType faultInjectionServerErrorType,
        Duration delay,
        int applyLimit) throws JsonProcessingException {

        // We need to create a new client because client may have marked region unavailable in other tests
        // which can impact the test result
        CosmosAsyncClient testClient = getClientBuilder()
            .contentResponseOnWriteEnabled(true)
            .preferredRegions(writePreferredLocations)
            .endToEndOperationLatencyPolicyConfig(
                new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofMinutes(10)).build())
            .buildAsyncClient();

        CosmosAsyncContainer container =
            testClient
                .getDatabase(this.cosmosAsyncContainer.getDatabase().getId())
                .getContainer(this.cosmosAsyncContainer.getId());

        // Test to validate partition key ranges request is being injected connection timeout
        String pkRangesConnectionDelay = "PkRanges-connectionDelay-" + UUID.randomUUID();
        FaultInjectionRule pkRangesConnectionDelayRule =
            new FaultInjectionRuleBuilder(pkRangesConnectionDelay)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .region(this.writePreferredLocations.get(0))
                        .operationType(FaultInjectionOperationType.METADATA_REQUEST_PARTITION_KEY_RANGES)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(faultInjectionServerErrorType)
                        .delay(delay) // to simulate http connection timeout
                        .times(applyLimit)
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        FaultInjectionRule dataOperationGoneRule =
            new FaultInjectionRuleBuilder("DataOperation-gone-" + UUID.randomUUID())
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(FaultInjectionOperationType.CREATE_ITEM)
                        .region(this.writePreferredLocations.get(0))
                        .build())
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.PARTITION_IS_SPLITTING)
                        .times(1)// using partition split to trigger routing map refresh flow
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();
        try {
            // create few items to first make sure the collection cache, pkRanges cache is being populated
            for (int i = 0; i < 10; i++) {
                container.createItem(TestItem.createNewItem()).block();
            }

            CosmosFaultInjectionHelper.configureFaultInjectionRules(
                    container,
                    Arrays.asList(pkRangesConnectionDelayRule, dataOperationGoneRule))
                .block();

            try {
                CosmosDiagnostics cosmosDiagnostics = container.createItem(TestItem.createNewItem()).block().getDiagnostics();
                // The PkRanges requests may have retried in another region,
                // but the create request will only be retried locally for PARTITION_IS_SPLITTING
                assertThat(cosmosDiagnostics.getContactedRegionNames().size()).isEqualTo(1);

                // validate PARTITION_KEY_RANGE_LOOK_UP
                ObjectNode diagnosticsNode = (ObjectNode) Utils.getSimpleObjectMapper().readTree(cosmosDiagnostics.toString());
                JsonNode metadataDiagnosticsContext = diagnosticsNode.get("metadataDiagnosticsContext");
                ArrayNode metadataDiagnosticList = (ArrayNode) metadataDiagnosticsContext.get("metadataDiagnosticList");

                assertThat(metadataDiagnosticList.size()).isGreaterThan(0);

                JsonNode pkRangesLookup = null;
                for (int i = 0; i < metadataDiagnosticList.size(); i++) {
                    if (metadataDiagnosticList
                        .get(i)
                        .get("metaDataName")
                        .asText()
                        .equalsIgnoreCase(MetadataDiagnosticsContext.MetadataType.PARTITION_KEY_RANGE_LOOK_UP.name())) {
                        pkRangesLookup = metadataDiagnosticList.get(i);
                        break;
                    }
                }

                assertThat(pkRangesLookup).isNotNull();
                if (faultInjectionServerErrorType == FaultInjectionServerErrorType.CONNECTION_DELAY) {
                    assertThat(pkRangesLookup.get("durationinMS").asLong()).isGreaterThanOrEqualTo(45 * 1000 * Math.min(applyLimit, 3)); // the duration will be at least one connection timeout
                }

                if (faultInjectionServerErrorType == FaultInjectionServerErrorType.RESPONSE_DELAY) {
                    assertThat(pkRangesLookup.get("durationinMS").asLong()).isGreaterThanOrEqualTo(500 * Math.min(applyLimit, 3)); // the duration will be at least one response timeout
                }

            } catch (CosmosException cosmosException) {
                fail("CreateItem should have succeeded. " + cosmosException.getDiagnostics());
            }
        } finally {
            pkRangesConnectionDelayRule.disable();
            dataOperationGoneRule.disable();
            safeClose(testClient);
        }
    }

    @Test(groups = { "multi-master" }, timeOut = 40 * TIMEOUT)
    public void faultInjectionServerErrorRuleTests_CollectionRead_ConnectionDelay() throws JsonProcessingException {

        // We need to create a new client because client may have marked region unavailable in other tests
        // which can impact the test result
        CosmosAsyncClient testClient = getClientBuilder()
            .contentResponseOnWriteEnabled(true)
            .preferredRegions(writePreferredLocations)
            .buildAsyncClient();

        CosmosAsyncContainer container =
            testClient
                .getDatabase(this.cosmosAsyncContainer.getDatabase().getId())
                .getContainer(this.cosmosAsyncContainer.getId());

        // Test to validate partition key ranges request is being injected connection timeout
        String collectionReadConnectionDelay = "CollectionRead-connectionDelay-" + UUID.randomUUID();
        FaultInjectionRule collectionReadConnectionDelayRule =
            new FaultInjectionRuleBuilder(collectionReadConnectionDelay)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .region(this.writePreferredLocations.get(0))
                        .operationType(FaultInjectionOperationType.METADATA_REQUEST_CONTAINER)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.CONNECTION_DELAY)
                        .delay(Duration.ofSeconds(50)) // to simulate http connection timeout
                        .times(1)
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        FaultInjectionRule dataOperationStaledCacheRule =
            new FaultInjectionRuleBuilder("DataOperation-staledCache-" + UUID.randomUUID())
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(FaultInjectionOperationType.CREATE_ITEM)
                        .region(this.writePreferredLocations.get(0))
                        .build())
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.NAME_CACHE_IS_STALE)
                        .times(1)// using staled cache to trigger collection cache refresh flow
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();
        try {
            // issue few requests to first populate all the necessary caches
            // as connection delay will impact all other operations, and in this test, we want to limit the scope to only collection read
            for (int i = 0; i < 10; i++) {
                container.createItem(TestItem.createNewItem()).block();
            }

            CosmosFaultInjectionHelper.configureFaultInjectionRules(
                    container,
                    Arrays.asList(collectionReadConnectionDelayRule, dataOperationStaledCacheRule))
                .block();

            try {
                CosmosDiagnostics cosmosDiagnostics = container.createItem(TestItem.createNewItem()).block().getDiagnostics();

                // validate CONTAINER_LOOK_UP
                ObjectNode diagnosticsNode = (ObjectNode) Utils.getSimpleObjectMapper().readTree(cosmosDiagnostics.toString());
                JsonNode metadataDiagnosticsContext = diagnosticsNode.get("metadataDiagnosticsContext");
                ArrayNode metadataDiagnosticList = (ArrayNode) metadataDiagnosticsContext.get("metadataDiagnosticList");

                assertThat(metadataDiagnosticList.size()).isGreaterThan(0);

                JsonNode containerLookup = null;
                for (int i = 0; i < metadataDiagnosticList.size(); i++) {
                    if (metadataDiagnosticList.get(i)
                        .get("metaDataName")
                        .asText()
                        .equalsIgnoreCase(MetadataDiagnosticsContext.MetadataType.CONTAINER_LOOK_UP.name())) {
                        containerLookup = metadataDiagnosticList.get(i);
                        break;
                    }
                }

                assertThat(containerLookup).isNotNull();
                assertThat(containerLookup.get("durationinMS").asLong()).isGreaterThanOrEqualTo(45000); // the duration will be at least one timeout
            } catch (CosmosException cosmosException) {
                fail("CreateItem should have succeeded. " + cosmosException.getDiagnostics());
            }
        } finally {
            collectionReadConnectionDelayRule.disable();
            dataOperationStaledCacheRule.disable();
            safeClose(testClient);
        }
    }

    @AfterClass(groups = {"multi-region"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(this.client);
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

    private void validateFaultInjectionRuleAppliedForAddressResolution(
        CosmosDiagnostics cosmosDiagnostics,
        String ruleId,
        int failureInjectedExpectedCount) throws JsonProcessingException {

        ObjectNode diagnosticsNode = (ObjectNode) Utils.getSimpleObjectMapper().readTree(cosmosDiagnostics.toString());
        JsonNode addressResolutionStatistics = diagnosticsNode.get("addressResolutionStatistics");
        Iterator<Map.Entry<String, JsonNode>> addressResolutionIterator = addressResolutionStatistics.fields();
        int failureInjectedCount = 0;
        while (addressResolutionIterator.hasNext()) {
            JsonNode addressResolutionSingleRequest = addressResolutionIterator.next().getValue();
            if (addressResolutionSingleRequest.has("faultInjectionRuleId")
                && addressResolutionSingleRequest.get("faultInjectionRuleId").asText().equalsIgnoreCase(ruleId)) {
                failureInjectedCount++;
            }
        }

        assertThat(failureInjectedCount).isEqualTo(failureInjectedExpectedCount);
    }
}
