// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.rx;


import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RequestTimeline;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.SkipException;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;

import java.util.Collection;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.fail;

public class WebExceptionRetryPolicyE2ETests extends TestSuiteBase {

    private final static Logger logger = LoggerFactory.getLogger(WebExceptionRetryPolicyE2ETests.class);
    private final static
    ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor diagnosticsAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor();

    private CosmosAsyncClient cosmosAsyncClient;
    private CosmosAsyncContainer cosmosAsyncContainer;

    @Factory(dataProvider = "clientBuildersWithSessionConsistency")
    public WebExceptionRetryPolicyE2ETests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @BeforeClass(groups = {"multi-master"}, timeOut = TIMEOUT)
    public void beforeClass() {
        this.cosmosAsyncClient = getClientBuilder().buildAsyncClient();
        this.cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(cosmosAsyncClient);
    }

    @AfterClass(groups = {"multi-master"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(cosmosAsyncClient);
    }

    @DataProvider(name = "operationTypeProvider")
    public static Object[][] operationTypeProvider() {
        return new Object[][]{
            // FaultInjectionOperationType, OperationType
            {FaultInjectionOperationType.READ_ITEM, OperationType.Read},
            {FaultInjectionOperationType.QUERY_ITEM, OperationType.Query}
        };
    }


    @Test(groups = {"multi-master"}, timeOut = TIMEOUT)
    public void addressRefreshHttpTimeout() {
        if (BridgeInternal
            .getContextClient(this.cosmosAsyncClient)
            .getConnectionPolicy()
            .getConnectionMode() != ConnectionMode.DIRECT) {
            throw new SkipException("addressRefreshHttpTimeout() is only meant for DIRECT mode");
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
                    .delay(Duration.ofSeconds(14))
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
                cosmosAsyncContainer,
                Arrays.asList(addressRefreshDelayRule, serverGoneRule)).block();
        try {
            cosmosAsyncContainer
                .createItem(newItem)
                .block();
            fail("addressRefreshHttpTimeout() should fail due to addressRefresh timeout");
        } catch (CosmosException e) {
            logger.info("dataPlaneRequestHttpTimeout() Diagnostics " + " " + e.getDiagnostics());
            assertThat(e.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.REQUEST_TIMEOUT);
            assertThat(e.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT);
            validateAddressRefreshRetryPolicyResponseTimeouts(e.getDiagnostics());
        } finally {
            addressRefreshDelayRule.disable();
            serverGoneRule.disable();
        }
    }

    @Test(groups = {"multi-master"}, dataProvider = "operationTypeProvider", timeOut = 8 * TIMEOUT)
    public void dataPlaneRequestHttpTimeout(
        FaultInjectionOperationType faultInjectionOperationType,
        OperationType operationType) {

        if (BridgeInternal
            .getContextClient(this.cosmosAsyncClient)
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
                    .times(3) // make sure it will exhaust the local region retry
                    .build()
            )
            .build();

        CosmosFaultInjectionHelper.configureFaultInjectionRules(this.cosmosAsyncContainer, Arrays.asList(requestHttpTimeoutRule)).block();


        try {
            CosmosDiagnostics cosmosDiagnostics =
                this.performDocumentOperation(cosmosAsyncContainer, operationType, newItem).block();
            logger.info("dataPlaneRequestHttpTimeout() Diagnostics " + " " + cosmosDiagnostics);
            validateDataPlaneRetryPolicyResponseTimeouts(cosmosDiagnostics);
        } catch (Exception e) {
            fail("dataPlaneRequestHttpTimeout() should succeed for operationType " + operationType, e);
        } finally {
            requestHttpTimeoutRule.disable();
        }
    }

    @Test(groups = {"multi-master"}, timeOut = 8 * TIMEOUT)
    public void writeOperationRequestHttpTimeout() {

        if (BridgeInternal
            .getContextClient(this.cosmosAsyncClient)
            .getConnectionPolicy()
            .getConnectionMode() != ConnectionMode.GATEWAY) {
            throw new SkipException("queryPlanHttpTimeoutWillNotMarkRegionUnavailable() is only meant for GATEWAY mode");
        }

        TestItem newItem = TestItem.createNewItem();
        this.cosmosAsyncContainer.createItem(newItem).block();
        FaultInjectionRule requestHttpTimeoutRule = new FaultInjectionRuleBuilder("requestHttpTimeoutRule" + UUID.randomUUID())
            .condition(
                new FaultInjectionConditionBuilder()
                    .operationType(FaultInjectionOperationType.CREATE_ITEM)
                    .connectionType(FaultInjectionConnectionType.GATEWAY)
                    .build())
            .result(
                FaultInjectionResultBuilders.getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
                    .delay(Duration.ofSeconds(66))
                    .times(2) // make sure it will exhaust the local region retry
                    .build()
            )
            .build();

        CosmosFaultInjectionHelper.configureFaultInjectionRules(this.cosmosAsyncContainer, Arrays.asList(requestHttpTimeoutRule)).block();


        try {
            CosmosDiagnostics cosmosDiagnostics =
                this.performDocumentOperation(cosmosAsyncContainer, OperationType.Create, newItem).block();
            fail("writeOperationRequestHttpTimeout() should fail for operationType " + OperationType.Create);
        } catch (CosmosException e) {
            logger.info("writeOperationRequestHttpTimeout() Diagnostics " + " " + e.getDiagnostics());
            assertThat(e.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.REQUEST_TIMEOUT);
        } finally {
            requestHttpTimeoutRule.disable();
        }
    }

    @Test(groups = {"multi-master"}, timeOut = 8 * TIMEOUT)
    public void writeOperationConnectionTimeout() {

        if (BridgeInternal
            .getContextClient(this.cosmosAsyncClient)
            .getConnectionPolicy()
            .getConnectionMode() != ConnectionMode.GATEWAY) {
            throw new SkipException("queryPlanHttpTimeoutWillNotMarkRegionUnavailable() is only meant for GATEWAY mode");
        }

        TestItem newItem = TestItem.createNewItem();
        this.cosmosAsyncContainer.createItem(newItem).block();
        FaultInjectionRule requestHttpTimeoutRule = new FaultInjectionRuleBuilder("requestHttpTimeoutRule" + UUID.randomUUID())
            .condition(
                new FaultInjectionConditionBuilder()
                    .operationType(FaultInjectionOperationType.CREATE_ITEM)
                    .connectionType(FaultInjectionConnectionType.GATEWAY)
                    .build())
            .result(
                FaultInjectionResultBuilders.getResultBuilder(FaultInjectionServerErrorType.CONNECTION_DELAY)
                    .delay(Duration.ofSeconds(66))
                    .times(3) // make sure it will exhaust the local region retry
                    .build()
            )
            .build();

        CosmosFaultInjectionHelper.configureFaultInjectionRules(this.cosmosAsyncContainer, Arrays.asList(requestHttpTimeoutRule)).block();


        try {
            CosmosDiagnostics cosmosDiagnostics =
                this.performDocumentOperation(cosmosAsyncContainer, OperationType.Create, newItem).block();
            logger.info("writeOperationConnectionTimeout() Diagnostics " + " " + cosmosDiagnostics);

        } catch (CosmosException e) {
            fail("writeOperationConnectionTimeout() should pass for operationType " + OperationType.Create);
            assertThat(e.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.SERVICE_UNAVAILABLE);
        } finally {
            requestHttpTimeoutRule.disable();
        }
    }

    private void validateDataPlaneRetryPolicyResponseTimeouts(CosmosDiagnostics cosmosDiagnostics) {
        List<ClientSideRequestStatistics.GatewayStatistics> gatewayStatisticsList = diagnosticsAccessor.getClientSideRequestStatistics(cosmosDiagnostics)
            .stream()
            .map(ClientSideRequestStatistics::getGatewayStatisticsList)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        for (ClientSideRequestStatistics.GatewayStatistics gs : gatewayStatisticsList) {
            if (gs.getStatusCode() == HttpConstants.StatusCodes.REQUEST_TIMEOUT) {
                for (RequestTimeline.Event event : gs.getRequestTimeline()) {
                    Duration durationInMillis = event.getDuration();
                    if (durationInMillis != null) {
                        assertThat(durationInMillis.getSeconds()).isLessThanOrEqualTo(62);
                        assertThat(durationInMillis.getSeconds()).isGreaterThanOrEqualTo(60);
                    }
                }
            }
        }
    }

    private void validateAddressRefreshRetryPolicyResponseTimeouts(CosmosDiagnostics cosmosDiagnostics) {

        List<ClientSideRequestStatistics.AddressResolutionStatistics> addressResolutionStatisticsList = diagnosticsAccessor.getClientSideRequestStatistics(cosmosDiagnostics)
            .stream()
            .map(ClientSideRequestStatistics::getAddressResolutionStatistics)
            .flatMap(m -> m.values().stream())
            .sorted(Comparator.comparing(ClientSideRequestStatistics.AddressResolutionStatistics::getStartTimeUTC))
            .collect(Collectors.toList());

        assertThat(MILLIS.between(addressResolutionStatisticsList.get(0).getStartTimeUTC(), addressResolutionStatisticsList.get(0).getEndTimeUTC())).isLessThanOrEqualTo(600);
        assertThat(SECONDS.between(addressResolutionStatisticsList.get(1).getStartTimeUTC(), addressResolutionStatisticsList.get(1).getEndTimeUTC())).isLessThanOrEqualTo(6);
        assertThat(SECONDS.between(addressResolutionStatisticsList.get(2).getStartTimeUTC(), addressResolutionStatisticsList.get(2).getEndTimeUTC())).isLessThanOrEqualTo(11);
    }

    private Mono<CosmosDiagnostics> performDocumentOperation(
        CosmosAsyncContainer cosmosAsyncContainer,
        OperationType operationType,
        TestItem createdItem) {

        switch(operationType) {
            case Query:
                CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
                String query = String.format("SELECT * from c where c.id = '%s'", createdItem.getId());
                FeedResponse<TestItem> itemFeedResponse =
                    cosmosAsyncContainer.queryItems(query, queryRequestOptions, TestItem.class).byPage().blockFirst();
                return Mono.just(itemFeedResponse.getCosmosDiagnostics());

            case Read:
                return cosmosAsyncContainer
                    .readItem(
                        createdItem.getId(),
                        new PartitionKey(createdItem.getId()),
                        TestItem.class
                    )
                    .map(itemResponse -> itemResponse.getDiagnostics());

            case Replace:
                return cosmosAsyncContainer
                    .replaceItem(
                        createdItem,
                        createdItem.getId(),
                        new PartitionKey(createdItem.getId()))
                    .map(itemResponse -> itemResponse.getDiagnostics());

            case Delete:
                return cosmosAsyncContainer.deleteItem(createdItem, null).map(itemResponse -> itemResponse.getDiagnostics());
            case Create:
                return cosmosAsyncContainer.createItem(TestItem.createNewItem()).map(itemResponse -> itemResponse.getDiagnostics());
            case Upsert:
                return cosmosAsyncContainer.upsertItem(TestItem.createNewItem()).map(itemResponse -> itemResponse.getDiagnostics());
            case Patch:
                CosmosPatchOperations patchOperations =
                    CosmosPatchOperations
                        .create()
                        .add("newPath", "newPath");
                return cosmosAsyncContainer
                    .patchItem(createdItem.getId(), new PartitionKey(createdItem.getId()), patchOperations, TestItem.class)
                    .map(itemResponse -> itemResponse.getDiagnostics());
            case ReadFeed:
                List<FeedRange> feedRanges = cosmosAsyncContainer.getFeedRanges().block();
                CosmosChangeFeedRequestOptions changeFeedRequestOptions =
                    CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(feedRanges.get(0));

                FeedResponse<TestItem> firstPage = cosmosAsyncContainer
                    .queryChangeFeed(changeFeedRequestOptions, TestItem.class)
                    .byPage()
                    .blockFirst();
                return Mono.just(firstPage.getCosmosDiagnostics());
        }
        throw new IllegalArgumentException("The operation type is not supported");
    }
}
