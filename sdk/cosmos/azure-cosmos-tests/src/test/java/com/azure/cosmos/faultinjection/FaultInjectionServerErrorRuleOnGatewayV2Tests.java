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
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.FlakyTestRetryAnalyzer;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.Fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.testng.AssertJUnit.fail;

public class FaultInjectionServerErrorRuleOnGatewayV2Tests extends FaultInjectionTestBase {

    private CosmosAsyncClient client;
    private CosmosAsyncContainer cosmosAsyncContainer;
    private List<String> preferredReadRegions;
    private Map<String, String> readRegionMap;
    private DatabaseAccount databaseAccount;

    private static final String thinClientEndpointIndicator = ":10250/";

    private static final String FAULT_INJECTION_RULE_NON_APPLICABLE_ADDRESS = "Addresses mismatch";
    private static final String FAULT_INJECTION_RULE_NON_APPLICABLE_HIT_LIMIT = "Hit Limit reached";
    private static final String FAULT_INJECTION_RULE_NON_APPLICABLE_REGION_ENDPOINT = "RegionalRoutingContext mismatch";

    @Factory(dataProvider = "clientBuildersWithGatewayAndHttp2")
    public FaultInjectionServerErrorRuleOnGatewayV2Tests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @BeforeClass(groups = {"fi-thinclient-multi-master"}, timeOut = TIMEOUT)
    public void beforeClass() {
        //Uncomment below line to enable thin client if running tests locally
        //System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");

        this.client = getClientBuilder().buildAsyncClient();

        AsyncDocumentClient asyncDocumentClient = BridgeInternal.getContextClient(this.client);
        GlobalEndpointManager globalEndpointManager = asyncDocumentClient.getGlobalEndpointManager();

        DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();

        this.databaseAccount = databaseAccount;
        this.cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(this.client);

        AccountLevelLocationContext accountLevelReadableRegionsLocationContext
            = getAccountLevelLocationContext(databaseAccount, false);

        validate(accountLevelReadableRegionsLocationContext, false);

        this.preferredReadRegions = accountLevelReadableRegionsLocationContext.serviceOrderedReadableRegions;
        this.readRegionMap = accountLevelReadableRegionsLocationContext.regionNameToEndpoint;
    }

    @AfterClass(groups = {"fi-thinclient-multi-master"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        //System.clearProperty("COSMOS.THINCLIENT_ENABLED");
        safeClose(this.client);
    }

    @DataProvider(name = "faultInjectionServerErrorResponseProvider")
    public static Object[][] faultInjectionServerErrorResponseProvider() {
        return new Object[][]{
            // faultInjectionServerError, will SDK retry, check for multi-region setup, errorStatusCode, errorSubStatusCode
            { FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR, false, false, 500, 0 },
            { FaultInjectionServerErrorType.RETRY_WITH, false, false, 449, 0 },
            { FaultInjectionServerErrorType.TOO_MANY_REQUEST, true, false, 429, HttpConstants.SubStatusCodes.USER_REQUEST_RATE_TOO_LARGE },
            { FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE, true, false, 404, 1002 },
            { FaultInjectionServerErrorType.SERVICE_UNAVAILABLE, true, true, 503, 21008 }
        };
    }

    @DataProvider(name = "operationTypeProvider")
    public static Object[][] operationTypeProvider() {
        return new Object[][]{
            // operationType, faultInjectionOperationType, isReadMany
            { OperationType.Upsert, FaultInjectionOperationType.UPSERT_ITEM, false },
            { OperationType.Read, FaultInjectionOperationType.READ_ITEM, false },
            { OperationType.Replace, FaultInjectionOperationType.REPLACE_ITEM, false },
            { OperationType.Create, FaultInjectionOperationType.CREATE_ITEM, false },
            { OperationType.Delete, FaultInjectionOperationType.DELETE_ITEM, false },
            { OperationType.Query, FaultInjectionOperationType.QUERY_ITEM, false },
            { OperationType.Patch, FaultInjectionOperationType.PATCH_ITEM, false },
            { OperationType.Batch, FaultInjectionOperationType.BATCH_ITEM, false },
            { OperationType.ReadFeed, FaultInjectionOperationType.READ_FEED_ITEM, false },
            { OperationType.Query, FaultInjectionOperationType.QUERY_ITEM, true }
        };
    }

    @DataProvider(name = "preferredRegionsConfigProvider")
    public static Object[] preferredRegionsConfigProvider() {
        // shouldInjectPreferredRegionsOnClient
        return new Object[] {true, false};
    }

    @Test(groups = {"fi-thinclient-multi-master"}, dataProvider = "faultInjectionServerErrorResponseProvider", timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_ServerErrorResponse(
        FaultInjectionServerErrorType serverErrorType,
        boolean canRetry,
        boolean isMultiRegionCheckRequired,
        int errorStatusCode,
        int errorSubStatusCode) throws JsonProcessingException {

        if (isMultiRegionCheckRequired) {
            if (this.preferredReadRegions.size() == 1) {
                canRetry = false;
            }
        }

        // simulate high channel acquisition/connectionTimeout
        String ruleId = "serverErrorRule-" + serverErrorType + "-" + UUID.randomUUID();
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
                        .getResultBuilder(serverErrorType)
                        .times(1)
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        try {
            TestItem createdItem = TestItem.createNewItem();
            cosmosAsyncContainer.createItem(createdItem).block();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(cosmosAsyncContainer, Arrays.asList(serverErrorRule)).block();

            CosmosDiagnostics cosmosDiagnostics = null;
            if (canRetry) {
                try {
                    cosmosDiagnostics =
                        cosmosAsyncContainer
                            .readItem(createdItem.getId(), new PartitionKey(createdItem.getId()), TestItem.class)
                            .block()
                            .getDiagnostics();
                    assertThinClientEndpointUsed(cosmosDiagnostics);
                } catch (Exception exception) {
                    fail("Request should succeeded, but failed with " + exception);
                }
            } else {
                try {
                    cosmosDiagnostics =
                        cosmosAsyncContainer
                            .readItem(createdItem.getId(), new PartitionKey(createdItem.getId()), TestItem.class)
                            .block()
                            .getDiagnostics();
                    fail("Request should fail, but succeeded");

                } catch (Exception e) {
                    cosmosDiagnostics = ((CosmosException)e).getDiagnostics();
                    assertThinClientEndpointUsed(cosmosDiagnostics);
                }
            }

            this.validateHitCount(serverErrorRule, 1, OperationType.Read, ResourceType.Document);
            this.validateFaultInjectionRuleApplied(
                cosmosDiagnostics,
                OperationType.Read,
                errorStatusCode,
                errorSubStatusCode,
                ruleId,
                canRetry
            );
            assertThinClientEndpointUsed(cosmosDiagnostics);
        } finally {
            serverErrorRule.disable();
        }
    }

    @Test(groups = {"fi-thinclient-multi-master"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_HitLimit(
        OperationType operationType,
        FaultInjectionOperationType faultInjectionOperationType,
        boolean isReadMany) throws JsonProcessingException {

        TestObject createdItem = TestObject.create();
        cosmosAsyncContainer.createItem(createdItem).block();

        // set rule by feed range
        String hitLimitRuleId = "ServerErrorRule-hitLimit-" + UUID.randomUUID();

        FaultInjectionRule hitLimitServerErrorRule =
            new FaultInjectionRuleBuilder(hitLimitRuleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .connectionType(FaultInjectionConnectionType.GATEWAY)
                        .operationType(faultInjectionOperationType)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.TOO_MANY_REQUEST)
                        .times(1)
                        .build()
                )
                .hitLimit(2)
                .build();

        try {
            CosmosFaultInjectionHelper.configureFaultInjectionRules(cosmosAsyncContainer, Arrays.asList(hitLimitServerErrorRule)).block();
            AssertionsForClassTypes.assertThat(
                hitLimitServerErrorRule.getRegionEndpoints().size() == this.readRegionMap.size()
                    && hitLimitServerErrorRule.getRegionEndpoints().containsAll(this.readRegionMap.keySet()));

            for (int i = 1; i <= 3; i++) {
                CosmosDiagnostics cosmosDiagnostics = this.performDocumentOperation(cosmosAsyncContainer, operationType, createdItem, isReadMany);
                if (i <= 2) {
                    this.validateFaultInjectionRuleApplied(
                        cosmosDiagnostics,
                        operationType,
                        HttpConstants.StatusCodes.TOO_MANY_REQUESTS,
                        HttpConstants.SubStatusCodes.USER_REQUEST_RATE_TOO_LARGE,
                        hitLimitRuleId,
                        true
                    );
                    assertThinClientEndpointUsed(cosmosDiagnostics);
                } else {
                    // the fault injection rule will not be applied due to hitLimit
                    cosmosDiagnostics = this.performDocumentOperation(cosmosAsyncContainer, operationType, createdItem, isReadMany);
                    this.validateNoFaultInjectionApplied(cosmosDiagnostics, operationType, FAULT_INJECTION_RULE_NON_APPLICABLE_HIT_LIMIT);
                    assertThinClientEndpointUsed(cosmosDiagnostics);
                }
            }

            this.validateHitCount(hitLimitServerErrorRule, 2, operationType, ResourceType.Document);
        } finally {
            hitLimitServerErrorRule.disable();
        }
    }

    @Test(groups = {"fi-thinclient-multi-master"}, dataProvider = "preferredRegionsConfigProvider", timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_Region(boolean shouldUsePreferredRegionsOnClient) throws JsonProcessingException {
        List<String> preferredLocations = this.preferredReadRegions;

        CosmosAsyncClient clientWithPreferredRegion = null;
        // set local region rule
        String localRegionRuleId = "ServerErrorRule-LocalRegion-" + UUID.randomUUID();
        FaultInjectionRule serverErrorRuleLocalRegion =
            new FaultInjectionRuleBuilder(localRegionRuleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .connectionType(FaultInjectionConnectionType.GATEWAY)
                        .region(preferredLocations.get(0))
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

        // set remote region rule
        String remoteRegionRuleId = "ServerErrorRule-RemoteRegion-" + UUID.randomUUID();
        FaultInjectionRule serverErrorRuleRemoteRegion =
            new FaultInjectionRuleBuilder(remoteRegionRuleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .connectionType(FaultInjectionConnectionType.GATEWAY)
                        .region(preferredLocations.get(1))
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
            clientWithPreferredRegion = getClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .consistencyLevel(BridgeInternal.getContextClient(this.client).getConsistencyLevel())
                .preferredRegions(shouldUsePreferredRegionsOnClient ? preferredLocations : Collections.emptyList())
                .buildAsyncClient();

            CosmosAsyncContainer container =
                clientWithPreferredRegion
                    .getDatabase(this.cosmosAsyncContainer.getDatabase().getId())
                    .getContainer(this.cosmosAsyncContainer.getId());

            TestObject createdItem = TestObject.create();
            container.createItem(createdItem).block();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(
                    container,
                    Arrays.asList(serverErrorRuleLocalRegion, serverErrorRuleRemoteRegion))
                .block();

            AssertionsForClassTypes.assertThat(
                serverErrorRuleLocalRegion.getRegionEndpoints().size() == 1
                    && serverErrorRuleLocalRegion.getRegionEndpoints().get(0).equals(this.readRegionMap.get(preferredLocations.get(0))));
            AssertionsForClassTypes.assertThat(
                serverErrorRuleRemoteRegion.getRegionEndpoints().size() == 1
                    && serverErrorRuleRemoteRegion.getRegionEndpoints().get(0).equals(this.readRegionMap.get(preferredLocations.get(1))));

            // Validate fault injection applied in the local region
            CosmosDiagnostics cosmosDiagnostics = this.performDocumentOperation(container, OperationType.Read, createdItem, false);
            logger.info("Cosmos Diagnostics: {}", cosmosDiagnostics);
            this.validateHitCount(serverErrorRuleLocalRegion, 1, OperationType.Read, ResourceType.Document);
            // 503/0 is retried in Client Retry policy
            this.validateHitCount(serverErrorRuleRemoteRegion, 1, OperationType.Read, ResourceType.Document);

            this.validateFaultInjectionRuleApplied(
                cosmosDiagnostics,
                OperationType.Read,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                localRegionRuleId,
                true
            );
            assertThinClientEndpointUsed(cosmosDiagnostics);

            serverErrorRuleLocalRegion.disable();

            cosmosDiagnostics = this.performDocumentOperation(container, OperationType.Read, createdItem, false);
            this.validateNoFaultInjectionApplied(cosmosDiagnostics, OperationType.Read, FAULT_INJECTION_RULE_NON_APPLICABLE_REGION_ENDPOINT);
            assertThinClientEndpointUsed(cosmosDiagnostics);
        } finally {
            serverErrorRuleLocalRegion.disable();
            serverErrorRuleRemoteRegion.disable();
            safeClose(clientWithPreferredRegion);
        }
    }

    @Test(groups = {"fi-thinclient-multi-master"}, timeOut = 4 * TIMEOUT)
    public void faultInjectionServerErrorRuleTests_Partition() throws JsonProcessingException {
        CosmosAsyncClient testClient = null;

        try {
            testClient = this.getClientBuilder()
                .consistencyLevel(this.databaseAccount.getConsistencyPolicy().getDefaultConsistencyLevel())
                .buildAsyncClient();

            CosmosAsyncContainer testContainer =
                testClient
                    .getDatabase(cosmosAsyncContainer.getDatabase().getId())
                    .getContainer(cosmosAsyncContainer.getId());

            for (int i = 0; i < 10; i++) {
                testContainer.createItem(TestItem.createNewItem()).block();
            }

            // getting one item from each feedRange
            List<FeedRange> feedRanges = testContainer.getFeedRanges().block();
            AssertionsForClassTypes.assertThat(feedRanges.size()).isGreaterThan(1);

            String query = "select * from c";
            CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
            cosmosQueryRequestOptions.setFeedRange(feedRanges.get(0));
            TestItem itemOnFeedRange0 = testContainer.queryItems(query, cosmosQueryRequestOptions, TestItem.class).blockFirst();

            cosmosQueryRequestOptions.setFeedRange(feedRanges.get(1));
            TestItem itemOnFeedRange1 = testContainer.queryItems(query, cosmosQueryRequestOptions, TestItem.class).blockFirst();

            // set rule by feed range
            String feedRangeRuleId = "ServerErrorRule-FeedRange-" + UUID.randomUUID();

            FaultInjectionRule serverErrorRuleByFeedRange =
                new FaultInjectionRuleBuilder(feedRangeRuleId)
                    .condition(
                        new FaultInjectionConditionBuilder()
                            .connectionType(FaultInjectionConnectionType.GATEWAY)
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

            CosmosFaultInjectionHelper.configureFaultInjectionRules(testContainer, Arrays.asList(serverErrorRuleByFeedRange)).block();
            AssertionsForClassTypes.assertThat(
                serverErrorRuleByFeedRange.getRegionEndpoints().size() == this.readRegionMap.size()
                    && serverErrorRuleByFeedRange.getRegionEndpoints().containsAll(this.readRegionMap.keySet()));

            // Issue a read item for the same feed range as configured in the fault injection rule
            CosmosDiagnostics cosmosDiagnostics =
                testContainer
                    .readItem(itemOnFeedRange0.getId(), new PartitionKey(itemOnFeedRange0.getId()), JsonNode.class)
                    .block()
                    .getDiagnostics();

            this.validateHitCount(serverErrorRuleByFeedRange, 1, OperationType.Read, ResourceType.Document);
            this.validateFaultInjectionRuleApplied(
                cosmosDiagnostics,
                OperationType.Read,
                HttpConstants.StatusCodes.TOO_MANY_REQUESTS,
                HttpConstants.SubStatusCodes.USER_REQUEST_RATE_TOO_LARGE,
                feedRangeRuleId,
                true
            );

            assertThinClientEndpointUsed(cosmosDiagnostics);

            // Issue a read item to different feed range
            try {
                cosmosDiagnostics = testContainer
                    .readItem(itemOnFeedRange1.getId(), new PartitionKey(itemOnFeedRange1.getId()), JsonNode.class)
                    .block()
                    .getDiagnostics();
                assertThinClientEndpointUsed(cosmosDiagnostics);
                this.validateNoFaultInjectionApplied(cosmosDiagnostics, OperationType.Read, FAULT_INJECTION_RULE_NON_APPLICABLE_ADDRESS);
            } finally {
                serverErrorRuleByFeedRange.disable();
            }
        } finally {
            safeClose(testClient);
        }

    }

    @Test(groups = {"fi-thinclient-multi-master"}, timeOut = 4 * TIMEOUT)
    public void faultInjectionServerErrorRuleTests_ServerResponseDelay() throws JsonProcessingException {
        // define another rule which can simulate timeout
        String timeoutRuleId = "serverErrorRule-responseDelay-" + UUID.randomUUID();
        FaultInjectionRule timeoutRule =
            new FaultInjectionRuleBuilder(timeoutRuleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .connectionType(FaultInjectionConnectionType.GATEWAY)
                        .operationType(FaultInjectionOperationType.READ_ITEM)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
                        .times(1)
                        .delay(Duration.ofSeconds(61)) // the default time out is 60s
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();
        try {
            DirectConnectionConfig directConnectionConfig = DirectConnectionConfig.getDefaultConfig();
            directConnectionConfig.setConnectTimeout(Duration.ofSeconds(1));

            // create a new item to be used by read operations
            TestItem createdItem = TestItem.createNewItem();
            this.cosmosAsyncContainer.createItem(createdItem).block();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(this.cosmosAsyncContainer, Arrays.asList(timeoutRule)).block();
            CosmosItemResponse<TestItem> itemResponse =
                this.cosmosAsyncContainer.readItem(createdItem.getId(), new PartitionKey(createdItem.getId()), TestItem.class).block();

            AssertionsForClassTypes.assertThat(timeoutRule.getHitCount()).isEqualTo(1);
            this.validateHitCount(timeoutRule, 1, OperationType.Read, ResourceType.Document);

            this.validateFaultInjectionRuleApplied(
                itemResponse.getDiagnostics(),
                OperationType.Read,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT,
                timeoutRuleId,
                true
            );

            assertThinClientEndpointUsed(itemResponse.getDiagnostics());

        } finally {
            timeoutRule.disable();
        }
    }

    @Test(groups = {"fi-thinclient-multi-master"}, timeOut = 4 * TIMEOUT, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void faultInjectionServerErrorRuleTests_ServerConnectionDelay() throws JsonProcessingException {
        // simulate high channel acquisition/connectionTimeout
        String ruleId = "serverErrorRule-serverConnectionDelay-" + UUID.randomUUID();
        FaultInjectionRule serverConnectionDelayRule =
            new FaultInjectionRuleBuilder(ruleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .connectionType(FaultInjectionConnectionType.GATEWAY)
                        .operationType(FaultInjectionOperationType.CREATE_ITEM)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.CONNECTION_DELAY)
                        .delay(Duration.ofSeconds(46)) // default value is 45s
                        .times(1)
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        try {
            CosmosFaultInjectionHelper.configureFaultInjectionRules(this.cosmosAsyncContainer, Arrays.asList(serverConnectionDelayRule)).block();
            CosmosItemResponse<TestItem> itemResponse = this.cosmosAsyncContainer.createItem(TestItem.createNewItem()).block();

            AssertionsForClassTypes.assertThat(serverConnectionDelayRule.getHitCount()).isEqualTo(1l);
            this.validateFaultInjectionRuleApplied(
                itemResponse.getDiagnostics(),
                OperationType.Create,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE,
                ruleId,
                true
            );
            assertThinClientEndpointUsed(itemResponse.getDiagnostics());
        } finally {
            serverConnectionDelayRule.disable();
        }
    }

    private void validateHitCount(
        FaultInjectionRule rule,
        long totalHitCount,
        OperationType operationType,
        ResourceType resourceType) {

        AssertionsForClassTypes.assertThat(rule.getHitCount()).isGreaterThanOrEqualTo(totalHitCount);
        if (totalHitCount > 0) {
            AssertionsForClassTypes.assertThat(rule.getHitCountDetails().size()).isGreaterThanOrEqualTo(1);
            AssertionsForClassTypes.assertThat(rule.getHitCountDetails().get(operationType.toString() + "-" + resourceType.toString())).isEqualTo(totalHitCount);
        }
    }

    private void validateNoFaultInjectionApplied(
        CosmosDiagnostics cosmosDiagnostics,
        OperationType operationType,
        String faultInjectionNonApplicableReason) throws JsonProcessingException {

        List<ObjectNode> diagnosticsNode = new ArrayList<>();
        if (operationType == OperationType.Query || operationType == OperationType.ReadFeed) {
            int clientSideDiagnosticsIndex = cosmosDiagnostics.toString().indexOf("[{\"userAgent\"");
            ArrayNode arrayNode =
                (ArrayNode) Utils.getSimpleObjectMapper().readTree(cosmosDiagnostics.toString().substring(clientSideDiagnosticsIndex));
            for (JsonNode node : arrayNode) {
                diagnosticsNode.add((ObjectNode) node);
            }
        } else {
            diagnosticsNode.add((ObjectNode) Utils.getSimpleObjectMapper().readTree(cosmosDiagnostics.toString()));
        }

        for (ObjectNode diagnosticNode : diagnosticsNode) {
            JsonNode gatewayStatisticsList = diagnosticNode.get("gatewayStatisticsList");
            AssertionsForClassTypes.assertThat(gatewayStatisticsList.isArray()).isTrue();

            for (int i = 0; i < gatewayStatisticsList.size(); i++) {
                JsonNode gatewayStatistics = gatewayStatisticsList.get(i);
                AssertionsForClassTypes.assertThat(gatewayStatistics.get("faultInjectionRuleId")).isNull();
                AssertionsForClassTypes.assertThat(gatewayStatistics.get("faultInjectionEvaluationResults")).isNotNull();
                AssertionsForClassTypes.assertThat(gatewayStatistics.get("faultInjectionEvaluationResults").toString().contains(faultInjectionNonApplicableReason));
            }
            AssertionsForClassTypes.assertThat(gatewayStatisticsList.size()).isOne();
        }
    }

    private void validateFaultInjectionRuleApplied(
        CosmosDiagnostics cosmosDiagnostics,
        OperationType operationType,
        int statusCode,
        int subStatusCode,
        String ruleId,
        boolean canRetryOnFaultInjectedError) throws JsonProcessingException {

        List<ObjectNode> diagnosticsNode = new ArrayList<>();
        if (operationType == OperationType.Query || operationType == OperationType.ReadFeed) {
            System.out.println(cosmosDiagnostics);
            int clientSideDiagnosticsIndex = cosmosDiagnostics.toString().indexOf("[{\"userAgent\"");
            ArrayNode arrayNode =
                (ArrayNode) Utils.getSimpleObjectMapper().readTree(cosmosDiagnostics.toString().substring(clientSideDiagnosticsIndex));
            for (JsonNode node : arrayNode) {
                diagnosticsNode.add((ObjectNode) node);
            }
        } else {
            diagnosticsNode.add((ObjectNode) Utils.getSimpleObjectMapper().readTree(cosmosDiagnostics.toString()));
        }

        for (ObjectNode diagnosticNode : diagnosticsNode) {
            JsonNode gatewayStatisticsList = diagnosticNode.get("gatewayStatisticsList");
            AssertionsForClassTypes.assertThat(gatewayStatisticsList.isArray()).isTrue();

            if (canRetryOnFaultInjectedError) {
                AssertionsForClassTypes.assertThat(gatewayStatisticsList.size()).isGreaterThanOrEqualTo(2);
            } else {
                AssertionsForClassTypes.assertThat(gatewayStatisticsList.size()).isEqualTo(1);
            }
            JsonNode gatewayStatistics = gatewayStatisticsList.get(0);
            AssertionsForClassTypes.assertThat(gatewayStatistics).isNotNull();
            AssertionsForClassTypes.assertThat(gatewayStatistics.get("statusCode").asInt()).isEqualTo(statusCode);
            AssertionsForClassTypes.assertThat(gatewayStatistics.get("subStatusCode").asInt()).isEqualTo(subStatusCode);
            AssertionsForClassTypes.assertThat(gatewayStatistics.get("faultInjectionRuleId").asText()).isEqualTo(ruleId);
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
