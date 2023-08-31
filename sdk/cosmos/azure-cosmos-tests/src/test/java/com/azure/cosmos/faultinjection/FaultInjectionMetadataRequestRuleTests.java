// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
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

public class FaultInjectionMetadataRequestRuleTests extends TestSuiteBase {
    private CosmosAsyncClient client;
    private CosmosAsyncContainer cosmosAsyncContainer;
    private List<String> readPreferredLocations;
    private List<String> writePreferredLocations;

    @Factory(dataProvider = "simpleClientBuildersWithJustDirectTcp")
    public FaultInjectionMetadataRequestRuleTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
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
                        // changed from 2 to 8
                        // 6 more address refresh request retries have been introduced by WebExceptionRetryPolicy
                        .times(8)
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
                validateFaultInjectionRuleAppliedForAddressResolution(cosmosDiagnostics, addressRefreshConnectionDelay, 8);
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

    @Test(groups = { "multi-region" }, timeOut = 4 * TIMEOUT)
    public void faultInjectionServerErrorRuleTests_AddressRefresh_ResponseDelay() throws JsonProcessingException {

        // Test to validate if there is http request timeout for address refresh,
        // SDK will retry 3 times before fail the request

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
                        .delay(Duration.ofSeconds(6)) // to simulate http request timeout
                        .times(4)
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

            try {
                CosmosDiagnostics cosmosDiagnostics =
                    container
                        .readItem(createdItem.getId(), new PartitionKey(createdItem.getId()), JsonNode.class)
                        .block()
                        .getDiagnostics();

                fail("request should have failed due to http request timeout on address resolution. " + cosmosDiagnostics);
            } catch (CosmosException e) {
                CosmosDiagnostics cosmosDiagnostics = e.getDiagnostics();
                assertThat(cosmosDiagnostics.getContactedRegionNames().size()).isEqualTo(1);
                assertThat(cosmosDiagnostics.getContactedRegionNames().containsAll(Arrays.asList(this.readPreferredLocations.get(0).toLowerCase()))).isTrue();
                validateFaultInjectionRuleAppliedForAddressResolution(cosmosDiagnostics, addressRefreshResponseDelay, 4);
            }
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
                        .delay(Duration.ofSeconds(6)) // to simulate http request timeout
                        .times(4)
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

    @Test(groups = { "multi-master" }, timeOut = 40 * TIMEOUT)
    public void faultInjectionServerErrorRuleTests_PartitionKeyRanges_ConnectionDelay() throws JsonProcessingException {

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
                        .getResultBuilder(FaultInjectionServerErrorType.CONNECTION_DELAY)
                        .delay(Duration.ofSeconds(50)) // to simulate http connection timeout
                        .times(1)
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
                        .getResultBuilder(FaultInjectionServerErrorType.PARTITION_IS_SPLITTING) // using partition split to trigger routing map refresh flow
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
                fail("CreateItem should have failed. " + cosmosDiagnostics);
            } catch (CosmosException cosmosException) {
                CosmosDiagnostics cosmosDiagnostics = cosmosException.getDiagnostics();

                // validate PARTITION_KEY_RANGE_LOOK_UP
                ObjectNode diagnosticsNode = (ObjectNode) Utils.getSimpleObjectMapper().readTree(cosmosDiagnostics.toString());
                JsonNode metadataDiagnosticsContext = diagnosticsNode.get("metadataDiagnosticsContext");
                ArrayNode metadataDiagnosticList = (ArrayNode) metadataDiagnosticsContext.get("metadataDiagnosticList");

                assertThat(metadataDiagnosticList.size()).isGreaterThan(0);

                JsonNode pkRangesLookup = null;
                for (int i = 0; i < metadataDiagnosticList.size(); i++) {
                    if (metadataDiagnosticList.get(i).get("metaDataName").asText().equalsIgnoreCase("PARTITION_KEY_RANGE_LOOK_UP")) {
                        pkRangesLookup = metadataDiagnosticList.get(i);
                        break;
                    }
                }

                assertThat(pkRangesLookup).isNotNull();
                assertThat(pkRangesLookup.get("durationinMS").asLong()).isGreaterThanOrEqualTo(45000); // the duration will be at least one timeout
            }
        } finally {
            pkRangesConnectionDelayRule.disable();
            dataOperationGoneRule.disable();
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
