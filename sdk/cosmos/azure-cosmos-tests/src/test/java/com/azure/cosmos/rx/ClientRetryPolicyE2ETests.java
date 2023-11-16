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
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
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
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.fail;

public class ClientRetryPolicyE2ETests extends TestSuiteBase {
    private CosmosAsyncClient clientWithPreferredRegions;
    private CosmosAsyncContainer cosmosAsyncContainer;
    private List<String> preferredRegions;

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
        Map<String, String> readRegionMap = this.getRegionMap(databaseAccount, false);
        this.preferredRegions =
            readRegionMap
                .keySet()
                .stream()
                .map(regionName -> regionName.toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());
        this.clientWithPreferredRegions = getClientBuilder()
            .preferredRegions(preferredRegions)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .endpointDiscoveryEnabled(true)
            .multipleWriteRegionsEnabled(true)
            .buildAsyncClient();
        this.cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(clientWithPreferredRegions);
    }

    @AfterClass(groups = {"multi-master"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(clientWithPreferredRegions);
    }

    @DataProvider(name = "operationTypeProvider")
    public static Object[][] operationTypeProvider() {
        return new Object[][]{
            // FaultInjectionOperationType, OperationType, shouldRetryCrossRegionForHttpTimeout
            { FaultInjectionOperationType.READ_ITEM, OperationType.Read, Boolean.TRUE },
            { FaultInjectionOperationType.CREATE_ITEM, OperationType.Create, Boolean.FALSE },
        };
    }

    @Test(groups = { "multi-master" }, timeOut = TIMEOUT)
    public void queryPlanHttpTimeoutWillNotMarkRegionUnavailable() {
        TestItem newItem = TestItem.createNewItem();
        this.cosmosAsyncContainer.createItem(newItem).block();

        // create fault injection rules for query plan
        FaultInjectionConditionBuilder conditionBuilder =
            new FaultInjectionConditionBuilder()
                .operationType(FaultInjectionOperationType.METADATA_REQUEST_QUERY_PLAN);
        if (BridgeInternal
            .getContextClient(this.clientWithPreferredRegions)
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

        CosmosFaultInjectionHelper.configureFaultInjectionRules(cosmosAsyncContainer, Arrays.asList(queryPlanDelayRule)).block();
        String query = "select * from c";
        CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
        queryRequestOptions.setPartitionKey(new PartitionKey(newItem.getId()));
        try {
            // validate the query plan will be retried in a different region and the final requests will be succeeded
            // TODO: Also capture all retries for metadata requests in the diagnostics
            FeedResponse<TestItem> firstPage = cosmosAsyncContainer.queryItems(query, queryRequestOptions, TestItem.class)
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

    @Test(groups = { "multi-master" }, timeOut = TIMEOUT)
    public void addressRefreshHttpTimeoutWillNotDoCrossRegionRetry() {
        if (BridgeInternal
            .getContextClient(this.clientWithPreferredRegions)
            .getConnectionPolicy()
            .getConnectionMode() != ConnectionMode.DIRECT) {
            throw new SkipException("queryPlanHttpTimeoutWillNotMarkRegionUnavailable() is only meant for DIRECT mode");
        }

        TestItem newItem = TestItem.createNewItem();
        this.cosmosAsyncContainer.createItem(newItem).block();

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
                cosmosAsyncContainer,
                Arrays.asList(addressRefreshDelayRule, serverGoneRule)).block();
        try {
            cosmosAsyncContainer
                .readItem(newItem.getId(), new PartitionKey(newItem.getId()), TestItem.class)
                .block();
            fail("addressRefreshHttpTimeoutWillNotDoCrossRegionRetry() should fail due to addressRefresh timeout");
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
        boolean shouldRetryCrossRegion) {

        if (BridgeInternal
            .getContextClient(this.clientWithPreferredRegions)
            .getConnectionPolicy()
            .getConnectionMode() != ConnectionMode.GATEWAY) {
            throw new SkipException("queryPlanHttpTimeoutWillNotMarkRegionUnavailable() is only meant for GATEWAY mode");
        }

        TestItem newItem = TestItem.createNewItem();
        this.cosmosAsyncContainer.createItem(newItem).block();
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

        CosmosFaultInjectionHelper.configureFaultInjectionRules(this.cosmosAsyncContainer, Arrays.asList(requestHttpTimeoutRule)).block();

        try {
            if (shouldRetryCrossRegion) {
                try {
                    CosmosDiagnostics cosmosDiagnostics =
                        this.performDocumentOperation(cosmosAsyncContainer, operationType, newItem).block();

                    assertThat(cosmosDiagnostics.getContactedRegionNames().size()).isEqualTo(this.preferredRegions.size());
                    assertThat(cosmosDiagnostics.getContactedRegionNames().containsAll(this.preferredRegions)).isTrue();
                } catch (Exception e) {
                    fail("dataPlaneRequestHttpTimeout() should succeed for operationType " + operationType, e);
                }
            } else {
                try {
                    this.performDocumentOperation(cosmosAsyncContainer, operationType, newItem).block();
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

    private Mono<CosmosDiagnostics> performDocumentOperation(
        CosmosAsyncContainer cosmosAsyncContainer,
        OperationType operationType,
        TestItem createdItem) {
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
                        new PartitionKey(createdItem.getId()),
                        TestItem.class
                    )
                    .map(itemResponse -> itemResponse.getDiagnostics());
            }

            if (operationType == OperationType.Replace) {
                return cosmosAsyncContainer
                    .replaceItem(
                        createdItem,
                        createdItem.getId(),
                        new PartitionKey(createdItem.getId()))
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
                    .patchItem(createdItem.getId(), new PartitionKey(createdItem.getId()), patchOperations, TestItem.class)
                    .map(itemResponse -> itemResponse.getDiagnostics());
            }
        }

        if (operationType == OperationType.ReadFeed) {
            List<FeedRange> feedRanges = cosmosAsyncContainer.getFeedRanges().block();
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
}
