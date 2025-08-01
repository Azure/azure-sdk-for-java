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
import com.azure.cosmos.models.CosmosBatch;
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
import com.azure.cosmos.implementation.throughputControl.TestItem;
import org.assertj.core.api.AssertionsForClassTypes;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.fail;

public class ClientRetryPolicyE2ETestsWithGatewayV2 extends TestSuiteBase {
    private CosmosAsyncClient clientWithPreferredRegions;
    private CosmosAsyncContainer cosmosAsyncContainerFromClientWithPreferredRegions;
    private CosmosAsyncClient clientWithoutPreferredRegions;
    private CosmosAsyncContainer cosmosAsyncContainerFromClientWithoutPreferredRegions;
    private List<String> preferredRegions;

    @Factory(dataProvider = "clientBuildersWithGateway")
    public ClientRetryPolicyE2ETestsWithGatewayV2(CosmosClientBuilder clientBuilder) {
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

        this.preferredRegions = accountLevelReadableLocationContext.serviceOrderedReadableRegions
            .stream()
            .map(regionName -> regionName.toLowerCase(Locale.ROOT))
            .collect(Collectors.toList());

        System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");
        System.setProperty("COSMOS.HTTP2_ENABLED", "true");

        this.clientWithPreferredRegions = getClientBuilder()
            .preferredRegions(this.preferredRegions)
            .endpointDiscoveryEnabled(true)
            .multipleWriteRegionsEnabled(true)
            .buildAsyncClient();

        this.clientWithoutPreferredRegions = getClientBuilder()
            .endpointDiscoveryEnabled(true)
            .multipleWriteRegionsEnabled(true)
            .buildAsyncClient();

        this.cosmosAsyncContainerFromClientWithPreferredRegions = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(clientWithPreferredRegions);
        this.cosmosAsyncContainerFromClientWithoutPreferredRegions = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(clientWithoutPreferredRegions);
    }

    @AfterClass(groups = {"multi-master"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        System.clearProperty("COSMOS.THINCLIENT_ENABLED");
        System.clearProperty("COSMOS.HTTP2_ENABLED");
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

    @DataProvider(name = "serviceUnavailableTestInputProvider")
    public static Object[][] serviceUnavailableTestInputProvider() {
        return new Object[][]{
            // FaultInjectionOperationType, OperationType, shouldRetryCrossRegionForServiceUnavailableInSingleWriteMultiRegion, shouldUsePreferredRegionsOnClient
            {FaultInjectionOperationType.READ_ITEM, OperationType.Read, Boolean.TRUE, Boolean.TRUE},
            {FaultInjectionOperationType.READ_ITEM, OperationType.Read, Boolean.TRUE, Boolean.FALSE},
            {FaultInjectionOperationType.QUERY_ITEM, OperationType.Query, Boolean.TRUE, Boolean.TRUE},
            {FaultInjectionOperationType.QUERY_ITEM, OperationType.Query, Boolean.TRUE, Boolean.FALSE},
            {FaultInjectionOperationType.CREATE_ITEM, OperationType.Create, Boolean.FALSE, Boolean.TRUE},
            {FaultInjectionOperationType.CREATE_ITEM, OperationType.Create, Boolean.FALSE, Boolean.FALSE},
            {FaultInjectionOperationType.REPLACE_ITEM, OperationType.Replace, Boolean.FALSE, Boolean.TRUE},
            {FaultInjectionOperationType.REPLACE_ITEM, OperationType.Replace, Boolean.FALSE, Boolean.FALSE},
            {FaultInjectionOperationType.UPSERT_ITEM, OperationType.Upsert, Boolean.FALSE, Boolean.TRUE},
            {FaultInjectionOperationType.UPSERT_ITEM, OperationType.Upsert, Boolean.FALSE, Boolean.FALSE},
            {FaultInjectionOperationType.DELETE_ITEM, OperationType.Delete, Boolean.FALSE, Boolean.TRUE},
            {FaultInjectionOperationType.DELETE_ITEM, OperationType.Delete, Boolean.FALSE, Boolean.FALSE},
            {FaultInjectionOperationType.PATCH_ITEM, OperationType.Patch, Boolean.FALSE, Boolean.TRUE},
            {FaultInjectionOperationType.PATCH_ITEM, OperationType.Patch, Boolean.FALSE, Boolean.FALSE},
            {FaultInjectionOperationType.BATCH_ITEM, OperationType.Batch, Boolean.FALSE, Boolean.TRUE},
            {FaultInjectionOperationType.BATCH_ITEM, OperationType.Batch, Boolean.FALSE, Boolean.FALSE},
            {FaultInjectionOperationType.READ_FEED_ITEM, OperationType.ReadFeed, Boolean.TRUE, Boolean.TRUE},
            {FaultInjectionOperationType.READ_FEED_ITEM, OperationType.ReadFeed, Boolean.TRUE, Boolean.FALSE},
        };
    }

    @Test(groups = {"multi-master"}, dataProvider = "operationTypeProvider", timeOut = 8 * TIMEOUT)
    public void dataPlaneRequestHttpTimeoutWithGatewayV2(
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
                    .connectionType(FaultInjectionConnectionType.GATEWAY_V2)
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

                    assertThat(cosmosDiagnostics.getContactedRegionNames().size()).isEqualTo(this.preferredRegions.size());
                    assertThat(cosmosDiagnostics.getContactedRegionNames().containsAll(this.preferredRegions)).isTrue();
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

    @Test(groups = {"multi-master"}, dataProvider = "serviceUnavailableTestInputProvider", timeOut = TIMEOUT)
    public void serviceUnavailableWithGatewayV2(FaultInjectionOperationType faultInjectionOperationType,
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
        FaultInjectionRule serviceUnavailableRule = new FaultInjectionRuleBuilder("serviceUnavailableRule-" + UUID.randomUUID())
            .condition(
                new FaultInjectionConditionBuilder()
                    .operationType(faultInjectionOperationType)
                    .connectionType(FaultInjectionConnectionType.GATEWAY_V2)
                    .build())
            .result(
                FaultInjectionResultBuilders.getResultBuilder(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                    .times(1)
                    .build()
            )
            .build();

        CosmosFaultInjectionHelper.configureFaultInjectionRules(resultantCosmosAsyncContainer, Arrays.asList(serviceUnavailableRule)).block();

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

                    assertThat(cosmosDiagnostics.getContactedRegionNames().size()).isEqualTo(this.preferredRegions.size());
                    assertThat(cosmosDiagnostics.getContactedRegionNames().containsAll(this.preferredRegions)).isTrue();
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
                    assertThat(e.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.SERVICE_UNAVAILABLE);
                    assertThat(e.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.SERVER_GENERATED_503);
                }
            }
        } finally {
            serviceUnavailableRule.disable();
        }
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
            || operationType == OperationType.Upsert
            || operationType == OperationType.Batch) {

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
