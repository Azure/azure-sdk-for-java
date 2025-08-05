// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.CosmosDiagnosticsRequestInfo;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.FeedRange;
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
import com.azure.cosmos.implementation.throughputControl.TestItem;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.Fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class FaultInjectionServerErrorRuleOnGatewayV2Tests extends FaultInjectionTestBase {

    private CosmosAsyncClient client;
    private CosmosAsyncContainer cosmosAsyncContainer;
    private List<String> preferredReadRegions;

    private static final String thinClientEndpointIndicator = ":10250/";

    @Factory(dataProvider = "clientBuildersWithGateway")
    public FaultInjectionServerErrorRuleOnGatewayV2Tests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @BeforeClass(groups = {"fi-thin-client-multi-region"}, timeOut = TIMEOUT)
    public void beforeClass() {
        System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");
        System.setProperty("COSMOS.HTTP2_ENABLED", "true");

        this.client = getClientBuilder().buildAsyncClient();

        AsyncDocumentClient asyncDocumentClient = BridgeInternal.getContextClient(this.client);
        GlobalEndpointManager globalEndpointManager = asyncDocumentClient.getGlobalEndpointManager();

        DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();

        this.cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(this.client);

        AccountLevelLocationContext accountLevelReadableRegionsLocationContext
            = getAccountLevelLocationContext(databaseAccount, false);

        validate(accountLevelReadableRegionsLocationContext, false);

        this.preferredReadRegions = accountLevelReadableRegionsLocationContext.serviceOrderedReadableRegions;
    }

    @AfterClass(groups = {"fi-thin-client-multi-region"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        System.clearProperty("COSMOS.THINCLIENT_ENABLED");
        System.clearProperty("COSMOS.HTTP2_ENABLED");
        safeClose(this.client);
    }

    @Test(groups = {"fi-thin-client-multi-region"}, timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_GatewayV2() {

        TestItem createdItem = TestItem.createNewItem();
        cosmosAsyncContainer.createItem(createdItem).block();

        String ruleId = "ServerErrorRule-GatewayV2-" + UUID.randomUUID();
        FaultInjectionRule serverErrorRule =
            new FaultInjectionRuleBuilder(ruleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .connectionType(FaultInjectionConnectionType.GATEWAY)
                        .operationType(FaultInjectionOperationType.READ_ITEM)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .times(1)
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        try {
            CosmosFaultInjectionHelper.configureFaultInjectionRules(cosmosAsyncContainer, Collections.singletonList(serverErrorRule)).block();

            // Perform the operation and validate fault injection
            CosmosItemResponse<TestItem> response = cosmosAsyncContainer.readItem(createdItem.getId(), new PartitionKey(createdItem.getId()), TestItem.class).block();

            assertThat(response).isNotNull();
            assertThat(response.getDiagnostics()).isNotNull();
            assertThinClientEndpointUsed(response.getDiagnostics());
            assertThat(serverErrorRule.getHitCount()).isEqualTo(1);
        } finally {
            serverErrorRule.disable();
        }
    }

    @Test(groups = {"fi-thin-client-multi-region"}, timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_Region() {
        String regionRuleId = "ServerErrorRule-RegionSpecific-" + UUID.randomUUID();
        FaultInjectionRule regionSpecificRule =
            new FaultInjectionRuleBuilder(regionRuleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .connectionType(FaultInjectionConnectionType.GATEWAY)
                        .operationType(FaultInjectionOperationType.READ_ITEM)
                        .region(this.preferredReadRegions.get(0))
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .times(1)
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        try {
            CosmosFaultInjectionHelper.configureFaultInjectionRules(cosmosAsyncContainer, Collections.singletonList(regionSpecificRule)).block();

            // Perform an operation to validate fault injection
            TestItem createdItem = TestItem.createNewItem();
            cosmosAsyncContainer.createItem(createdItem).block();
            CosmosItemResponse<TestItem> response = cosmosAsyncContainer.readItem(createdItem.getId(), new PartitionKey(createdItem.getId()), TestItem.class).block();

            assertThat(response).isNotNull();
            assertThat(response.getDiagnostics()).isNotNull();
            assertThinClientEndpointUsed(response.getDiagnostics());
            assertThat(regionSpecificRule.getHitCount()).isEqualTo(1);
        } finally {
            regionSpecificRule.disable();
        }
    }

    @Test(groups = {"fi-thin-client-multi-region"}, timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_FeedRange() {
        List<FeedRange> feedRanges = cosmosAsyncContainer.getFeedRanges().block();
        assertThat(feedRanges).isNotEmpty();

        String feedRangeRuleId = "ServerErrorRule-FeedRange-" + UUID.randomUUID();
        FaultInjectionRule feedRangeRule =
            new FaultInjectionRuleBuilder(feedRangeRuleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .connectionType(FaultInjectionConnectionType.GATEWAY)
                        .operationType(FaultInjectionOperationType.READ_ITEM)
                        .endpoints(new FaultInjectionEndpointBuilder(feedRanges.get(0)).build())
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

        try {
            CosmosFaultInjectionHelper.configureFaultInjectionRules(cosmosAsyncContainer, Collections.singletonList(feedRangeRule)).block();

            // Perform an operation on the specific feed range
            TestItem createdItem = TestItem.createNewItem();
            cosmosAsyncContainer.createItem(createdItem).block();
            CosmosItemResponse<TestItem> response
                = cosmosAsyncContainer.readItem(createdItem.getId(), new PartitionKey(createdItem.getId()), TestItem.class).block();

            assertThat(feedRangeRule.getHitCount()).isEqualTo(1);
            assertThat(response).isNotNull();
            assertThat(response.getDiagnostics()).isNotNull();
            assertThinClientEndpointUsed(response.getDiagnostics());
        } finally {
            feedRangeRule.disable();
        }
    }

    @Test(groups = {"fi-thin-client-multi-region"}, timeOut = 3 * TIMEOUT)
    public void faultInjectionServerErrorRuleTests_ResponseDelay() {
        String delayRuleId = "ServerErrorRule-ResponseDelay-" + UUID.randomUUID();
        FaultInjectionRule responseDelayRule =
            new FaultInjectionRuleBuilder(delayRuleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .connectionType(FaultInjectionConnectionType.GATEWAY)
                        .operationType(FaultInjectionOperationType.READ_ITEM)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
                        .delay(Duration.ofSeconds(65)) // Simulate delay beyond default timeout
                        .times(1)
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        try {
            CosmosFaultInjectionHelper.configureFaultInjectionRules(cosmosAsyncContainer, Collections.singletonList(responseDelayRule)).block();

            // Perform a read operation to validate timeout behavior
            TestItem createdItem = TestItem.createNewItem();
            cosmosAsyncContainer.createItem(createdItem).block();

            try {
                cosmosAsyncContainer.readItem(createdItem.getId(), new PartitionKey(createdItem.getId()), TestItem.class).block();
            } catch (Exception e) {
                assertThat(e).isInstanceOf(CosmosException.class);
                assertThat(((CosmosException) e).getStatusCode()).isEqualTo(408); // Request timeout
                assertThinClientEndpointUsed(((CosmosException) e).getDiagnostics());
            }

            assertThat(responseDelayRule.getHitCount()).isEqualTo(1);
        } finally {
            responseDelayRule.disable();
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

        AssertionsForClassTypes.assertThat(accountLevelLocationContext).isNotNull();

        if (isWriteOnly) {
            AssertionsForClassTypes.assertThat(accountLevelLocationContext.serviceOrderedWriteableRegions).isNotNull();
            AssertionsForClassTypes.assertThat(accountLevelLocationContext.serviceOrderedWriteableRegions.size()).isGreaterThanOrEqualTo(1);
        } else {
            AssertionsForClassTypes.assertThat(accountLevelLocationContext.serviceOrderedReadableRegions).isNotNull();
            AssertionsForClassTypes.assertThat(accountLevelLocationContext.serviceOrderedReadableRegions.size()).isGreaterThanOrEqualTo(1);
        }
    }

    private static void assertThinClientEndpointUsed(CosmosDiagnostics diagnostics) {
        AssertionsForClassTypes.assertThat(diagnostics).isNotNull();

        CosmosDiagnosticsContext ctx = diagnostics.getDiagnosticsContext();
        AssertionsForClassTypes.assertThat(ctx).isNotNull();

        Collection<CosmosDiagnosticsRequestInfo> requests = ctx.getRequestInfo();
        AssertionsForClassTypes.assertThat(requests).isNotNull();
        AssertionsForClassTypes.assertThat(requests.size()).isPositive();

        for (CosmosDiagnosticsRequestInfo requestInfo : requests) {
            logger.info(
                "Endpoint: {}, RequestType: {}, Partition: {}/{}, ActivityId: {}",
                requestInfo.getEndpoint(),
                requestInfo.getRequestType(),
                requestInfo.getPartitionId(),
                requestInfo.getPartitionKeyRangeId(),
                requestInfo.getActivityId());
            if (requestInfo.getEndpoint().contains(thinClientEndpointIndicator)) {
                return;
            }
        }

        Fail.fail("No request targeting thin client proxy endpoint.");
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
