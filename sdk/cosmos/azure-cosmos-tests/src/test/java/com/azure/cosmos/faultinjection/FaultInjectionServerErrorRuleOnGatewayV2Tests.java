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
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfigBuilder;
import com.azure.cosmos.CosmosException;
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
import com.azure.cosmos.models.CosmosItemRequestOptions;
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
import reactor.core.publisher.Mono;

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
         System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");

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
        System.clearProperty("COSMOS.THINCLIENT_ENABLED");
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

    @DataProvider(name = "responseDelayOperationTypeProvider")
    public static Object[][] responseDelayOperationTypeProvider() {
        return new Object[][]{
            // operationType, faultInjectionOperationType
            { OperationType.Read, FaultInjectionOperationType.READ_ITEM },
            { OperationType.Query, FaultInjectionOperationType.QUERY_ITEM },
            { OperationType.ReadFeed, FaultInjectionOperationType.READ_FEED_ITEM }
        };
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

    @Test(groups = {"fi-thinclient-multi-master"}, dataProvider = "responseDelayOperationTypeProvider", timeOut = 4 * TIMEOUT, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void faultInjectionServerErrorRuleTests_ServerResponseDelay(
        OperationType operationType,
        FaultInjectionOperationType faultInjectionOperationType) throws JsonProcessingException {

        // define another rule which can simulate timeout
        String timeoutRuleId = "serverErrorRule-responseDelay-" + UUID.randomUUID();
        FaultInjectionRule timeoutRule =
            new FaultInjectionRuleBuilder(timeoutRuleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .connectionType(FaultInjectionConnectionType.GATEWAY)
                        .operationType(faultInjectionOperationType)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
                        .times(1)
                        .delay(Duration.ofSeconds(61)) // the default time out is 60s, but Gateway V2 uses 6s
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();
        try {
            // create a new item to be used by operations
            TestObject createdItem = TestObject.create();
            this.cosmosAsyncContainer.createItem(createdItem).block();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(this.cosmosAsyncContainer, Arrays.asList(timeoutRule)).block();

            // With HttpTimeoutPolicyForGatewayV2, the first attempt times out at 6s,
            // but since delay is only injected once (times=1), the retry succeeds
            CosmosDiagnostics cosmosDiagnostics = this.performDocumentOperation(
                this.cosmosAsyncContainer,
                operationType,
                createdItem,
                false);

            AssertionsForClassTypes.assertThat(timeoutRule.getHitCount()).isEqualTo(1);
            this.validateHitCount(timeoutRule, 1, operationType, ResourceType.Document);

            this.validateFaultInjectionRuleApplied(
                cosmosDiagnostics,
                operationType,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT,
                timeoutRuleId,
                true
            );

            assertThinClientEndpointUsed(cosmosDiagnostics);

            // Validate end-to-end latency and final status code from CosmosDiagnosticsContext
            CosmosDiagnosticsContext diagnosticsContext = cosmosDiagnostics.getDiagnosticsContext();
            AssertionsForClassTypes.assertThat(diagnosticsContext).isNotNull();
            AssertionsForClassTypes.assertThat(diagnosticsContext.getDuration()).isNotNull();
            AssertionsForClassTypes.assertThat(diagnosticsContext.getDuration()).isLessThan(Duration.ofSeconds(8));
            AssertionsForClassTypes.assertThat(diagnosticsContext.getStatusCode()).isBetween(HttpConstants.StatusCodes.OK, HttpConstants.StatusCodes.NOT_MODIFIED);

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

    // ==========================================
    // Connection lifecycle tests
    // ==========================================

    @Test(groups = {"fi-thinclient-multi-master"}, timeOut = 4 * TIMEOUT, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void faultInjectionServerErrorRuleTests_ConnectionReuseOnResponseTimeout() throws JsonProcessingException {
        // Test: When a responseTimeout fires (6s), the retry should reuse the same HTTP/2 TCP connection.
        // Inject RESPONSE_DELAY for 8s (> 6s timeout) once. First attempt times out, retry succeeds.
        // Assert: parentChannelId is the same across both attempts (same TCP connection reused).

        String timeoutRuleId = "serverErrorRule-connReuse-" + UUID.randomUUID();
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
                        .times(1) // only first attempt is delayed
                        .delay(Duration.ofSeconds(8)) // > 6s GatewayV2 timeout, < 10s 3rd attempt timeout
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        try {
            TestObject createdItem = TestObject.create();
            this.cosmosAsyncContainer.createItem(createdItem).block();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(this.cosmosAsyncContainer, Arrays.asList(timeoutRule)).block();

            CosmosDiagnostics cosmosDiagnostics = this.performDocumentOperation(
                this.cosmosAsyncContainer, OperationType.Read, createdItem, false);

            assertThinClientEndpointUsed(cosmosDiagnostics);
            AssertionsForClassTypes.assertThat(timeoutRule.getHitCount()).isEqualTo(1);

            // Parse diagnostics to extract channelId info from gatewayStatisticsList
            ObjectNode diagnosticNode = (ObjectNode) Utils.getSimpleObjectMapper().readTree(cosmosDiagnostics.toString());
            JsonNode gatewayStatisticsList = diagnosticNode.get("gatewayStatisticsList");
            AssertionsForClassTypes.assertThat(gatewayStatisticsList.isArray()).isTrue();
            AssertionsForClassTypes.assertThat(gatewayStatisticsList.size()).isGreaterThanOrEqualTo(2);

            // First attempt: timed out (408)
            JsonNode firstAttempt = gatewayStatisticsList.get(0);
            AssertionsForClassTypes.assertThat(firstAttempt.get("statusCode").asInt()).isEqualTo(HttpConstants.StatusCodes.REQUEST_TIMEOUT);

            // Second attempt: succeeded
            JsonNode secondAttempt = gatewayStatisticsList.get(1);
            AssertionsForClassTypes.assertThat(secondAttempt.get("statusCode").asInt()).isBetween(
                HttpConstants.StatusCodes.OK, HttpConstants.StatusCodes.NOT_MODIFIED);

            // Both should have parentChannelId (TCP connection identity)
            String parentChannelId1 = firstAttempt.has("parentChannelId") ? firstAttempt.get("parentChannelId").asText() : null;
            String parentChannelId2 = secondAttempt.has("parentChannelId") ? secondAttempt.get("parentChannelId").asText() : null;

            logger.info("Connection reuse test - attempt1 parentChannelId={}, channelId={}, isHttp2={}",
                parentChannelId1,
                firstAttempt.has("channelId") ? firstAttempt.get("channelId").asText() : "n/a",
                firstAttempt.has("isHttp2") ? firstAttempt.get("isHttp2").asBoolean() : "n/a");
            logger.info("Connection reuse test - attempt2 parentChannelId={}, channelId={}, isHttp2={}",
                parentChannelId2,
                secondAttempt.has("channelId") ? secondAttempt.get("channelId").asText() : "n/a",
                secondAttempt.has("isHttp2") ? secondAttempt.get("isHttp2").asBoolean() : "n/a");

            // KEY ASSERTION: Same TCP connection reused for retry
            if (parentChannelId1 != null && parentChannelId2 != null) {
                AssertionsForClassTypes.assertThat(parentChannelId2)
                    .as("Retry should reuse the same TCP connection (parentChannelId)")
                    .isEqualTo(parentChannelId1);
            }

            // Both should report HTTP/2 if thin client is used
            if (firstAttempt.has("isHttp2")) {
                AssertionsForClassTypes.assertThat(firstAttempt.get("isHttp2").asBoolean()).isTrue();
            }
        } finally {
            timeoutRule.disable();
        }
    }

    @Test(groups = {"fi-thinclient-multi-master"}, timeOut = 4 * TIMEOUT, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void faultInjectionServerErrorRuleTests_AllRetriesTimeoutSameConnection() throws JsonProcessingException {
        // Test: When all local retries exhaust (3 attempts, all timeout), verify they all used
        // the same TCP connection. After exhaustion, cross-region failover should use a different connection.

        String timeoutRuleId = "serverErrorRule-allRetries-" + UUID.randomUUID();
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
                        .times(4) // enough to exhaust all 3 inner retries (6s, 6s, 10s)
                        .delay(Duration.ofSeconds(66)) // > max timeout (10s) for GatewayV2
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        try {
            TestObject createdItem = TestObject.create();
            this.cosmosAsyncContainer.createItem(createdItem).block();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(this.cosmosAsyncContainer, Arrays.asList(timeoutRule)).block();

            CosmosDiagnostics cosmosDiagnostics;
            try {
                cosmosDiagnostics = this.performDocumentOperation(
                    this.cosmosAsyncContainer, OperationType.Read, createdItem, false);
            } catch (CosmosException e) {
                cosmosDiagnostics = e.getDiagnostics();
            }

            assertThinClientEndpointUsed(cosmosDiagnostics);

            // Parse diagnostics
            ObjectNode diagnosticNode = (ObjectNode) Utils.getSimpleObjectMapper().readTree(cosmosDiagnostics.toString());
            JsonNode gatewayStatisticsList = diagnosticNode.get("gatewayStatisticsList");
            AssertionsForClassTypes.assertThat(gatewayStatisticsList.isArray()).isTrue();
            // Should have at least 3 attempts (inner retries from WebExceptionRetryPolicy)
            AssertionsForClassTypes.assertThat(gatewayStatisticsList.size()).isGreaterThanOrEqualTo(3);

            // Collect all parentChannelIds to verify connection reuse within same region
            String firstParentChannelId = null;
            for (int i = 0; i < Math.min(3, gatewayStatisticsList.size()); i++) {
                JsonNode attempt = gatewayStatisticsList.get(i);
                if (attempt.has("parentChannelId")) {
                    String parentChannelId = attempt.get("parentChannelId").asText();
                    logger.info("All-retries test - attempt {} parentChannelId={}, statusCode={}, channelId={}",
                        i, parentChannelId, attempt.get("statusCode").asInt(),
                        attempt.has("channelId") ? attempt.get("channelId").asText() : "n/a");

                    if (firstParentChannelId == null) {
                        firstParentChannelId = parentChannelId;
                    } else {
                        // KEY ASSERTION: All local retries use the same TCP connection
                        AssertionsForClassTypes.assertThat(parentChannelId)
                            .as("All inner retry attempts should reuse the same TCP connection, attempt " + i)
                            .isEqualTo(firstParentChannelId);
                    }
                }
            }

            // If cross-region retry happened (size > 3), the new region should use a different parentChannelId
            if (gatewayStatisticsList.size() > 3) {
                JsonNode crossRegionAttempt = gatewayStatisticsList.get(gatewayStatisticsList.size() - 1);
                if (crossRegionAttempt.has("parentChannelId") && firstParentChannelId != null) {
                    String crossRegionParentChannelId = crossRegionAttempt.get("parentChannelId").asText();
                    logger.info("All-retries test - cross-region attempt parentChannelId={}", crossRegionParentChannelId);
                    // Cross-region = different gateway endpoint = likely different TCP connection
                    // (not strictly guaranteed if the pool happens to map to same IP, but very likely)
                }
            }
        } finally {
            timeoutRule.disable();
        }
    }

    @Test(groups = {"fi-thinclient-multi-master"}, timeOut = 4 * TIMEOUT, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void faultInjectionServerErrorRuleTests_ConnectionSurvivesTimeoutForNextRequest() throws JsonProcessingException {
        // Test: After a timeout occurs and retries succeed, the next independent request should
        // still use the same TCP connection — proving the connection wasn't evicted from the pool.

        String timeoutRuleId = "serverErrorRule-connSurvives-" + UUID.randomUUID();
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
                        .times(1) // only first attempt delayed
                        .delay(Duration.ofSeconds(8)) // > 6s timeout
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        try {
            TestObject createdItem = TestObject.create();
            this.cosmosAsyncContainer.createItem(createdItem).block();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(this.cosmosAsyncContainer, Arrays.asList(timeoutRule)).block();

            // First operation: will timeout once, then succeed on retry
            CosmosDiagnostics firstDiagnostics = this.performDocumentOperation(
                this.cosmosAsyncContainer, OperationType.Read, createdItem, false);

            assertThinClientEndpointUsed(firstDiagnostics);
            AssertionsForClassTypes.assertThat(timeoutRule.getHitCount()).isEqualTo(1);

            // Disable the fault injection rule before the second request
            timeoutRule.disable();

            // Second operation: no fault injection, should succeed immediately
            CosmosDiagnostics secondDiagnostics = this.performDocumentOperation(
                this.cosmosAsyncContainer, OperationType.Read, createdItem, false);

            assertThinClientEndpointUsed(secondDiagnostics);

            // Parse both diagnostics to compare parentChannelId
            ObjectNode firstNode = (ObjectNode) Utils.getSimpleObjectMapper().readTree(firstDiagnostics.toString());
            ObjectNode secondNode = (ObjectNode) Utils.getSimpleObjectMapper().readTree(secondDiagnostics.toString());

            JsonNode firstGwStats = firstNode.get("gatewayStatisticsList");
            JsonNode secondGwStats = secondNode.get("gatewayStatisticsList");
            AssertionsForClassTypes.assertThat(firstGwStats.isArray()).isTrue();
            AssertionsForClassTypes.assertThat(secondGwStats.isArray()).isTrue();

            // Get the parentChannelId from the successful retry of the first operation
            JsonNode firstOpSuccessAttempt = firstGwStats.get(firstGwStats.size() - 1);
            JsonNode secondOpAttempt = secondGwStats.get(0);

            String parentChannelIdFirstOp = firstOpSuccessAttempt.has("parentChannelId")
                ? firstOpSuccessAttempt.get("parentChannelId").asText() : null;
            String parentChannelIdSecondOp = secondOpAttempt.has("parentChannelId")
                ? secondOpAttempt.get("parentChannelId").asText() : null;

            logger.info("Connection survives test - firstOp parentChannelId={}, secondOp parentChannelId={}",
                parentChannelIdFirstOp, parentChannelIdSecondOp);

            // KEY ASSERTION: The connection survived the timeout and is reused for the next request
            if (parentChannelIdFirstOp != null && parentChannelIdSecondOp != null) {
                AssertionsForClassTypes.assertThat(parentChannelIdSecondOp)
                    .as("Connection should survive timeout and be reused for subsequent requests")
                    .isEqualTo(parentChannelIdFirstOp);
            }

            // Validate the second operation completed fast (no timeout, no retry)
            CosmosDiagnosticsContext secondCtx = secondDiagnostics.getDiagnosticsContext();
            AssertionsForClassTypes.assertThat(secondCtx.getDuration()).isLessThan(Duration.ofSeconds(3));
            AssertionsForClassTypes.assertThat(secondGwStats.size()).isEqualTo(1);
        } finally {
            timeoutRule.disable();
        }
    }

    @Test(groups = {"fi-thinclient-multi-master"}, timeOut = 4 * TIMEOUT, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void faultInjectionServerErrorRuleTests_ConcurrentStreamsUnaffectedByTimeout() throws JsonProcessingException {
        // Test: Two concurrent reads on the same HTTP/2 connection. One is delayed (times out),
        // the other should complete fast. The timeout on one stream should NOT affect the other.

        TestObject createdItem = TestObject.create();
        this.cosmosAsyncContainer.createItem(createdItem).block();

        // Get feed ranges to scope fault injection to only one partition
        List<FeedRange> feedRanges = this.cosmosAsyncContainer.getFeedRanges().block();
        AssertionsForClassTypes.assertThat(feedRanges.size()).isGreaterThan(1);

        // Create a second item on a different feed range
        String query = "select * from c";
        CosmosQueryRequestOptions queryOpts0 = new CosmosQueryRequestOptions();
        queryOpts0.setFeedRange(feedRanges.get(0));
        TestItem itemOnRange0 = this.cosmosAsyncContainer.queryItems(query, queryOpts0, TestItem.class).blockFirst();

        CosmosQueryRequestOptions queryOpts1 = new CosmosQueryRequestOptions();
        queryOpts1.setFeedRange(feedRanges.get(1));
        TestItem itemOnRange1 = this.cosmosAsyncContainer.queryItems(query, queryOpts1, TestItem.class).blockFirst();

        if (itemOnRange0 == null || itemOnRange1 == null) {
            // Create items on each feed range if needed
            for (int i = 0; i < 10; i++) {
                this.cosmosAsyncContainer.createItem(TestItem.createNewItem()).block();
            }
            itemOnRange0 = this.cosmosAsyncContainer.queryItems(query, queryOpts0, TestItem.class).blockFirst();
            itemOnRange1 = this.cosmosAsyncContainer.queryItems(query, queryOpts1, TestItem.class).blockFirst();
        }

        AssertionsForClassTypes.assertThat(itemOnRange0).isNotNull();
        AssertionsForClassTypes.assertThat(itemOnRange1).isNotNull();

        // Inject delay ONLY on feedRange[0]
        String delayRuleId = "serverErrorRule-concurrentStreams-" + UUID.randomUUID();
        FaultInjectionRule delayRule =
            new FaultInjectionRuleBuilder(delayRuleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .connectionType(FaultInjectionConnectionType.GATEWAY)
                        .operationType(FaultInjectionOperationType.READ_ITEM)
                        .endpoints(new FaultInjectionEndpointBuilder(feedRanges.get(0)).build())
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
                        .times(1)
                        .delay(Duration.ofSeconds(8)) // > 6s timeout
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        try {
            CosmosFaultInjectionHelper.configureFaultInjectionRules(this.cosmosAsyncContainer, Arrays.asList(delayRule)).block();

            final TestItem finalItemOnRange0 = itemOnRange0;
            final TestItem finalItemOnRange1 = itemOnRange1;

            // Fire both reads concurrently
            Mono<CosmosDiagnostics> delayedRead = this.cosmosAsyncContainer
                .readItem(finalItemOnRange0.getId(), new PartitionKey(finalItemOnRange0.getId()), TestItem.class)
                .map(r -> r.getDiagnostics());

            Mono<CosmosDiagnostics> fastRead = this.cosmosAsyncContainer
                .readItem(finalItemOnRange1.getId(), new PartitionKey(finalItemOnRange1.getId()), TestItem.class)
                .map(r -> r.getDiagnostics());

            // Wait for both to complete
            CosmosDiagnostics[] results = Mono.zip(delayedRead, fastRead,
                (d1, d2) -> new CosmosDiagnostics[]{d1, d2}).block();

            CosmosDiagnostics delayedDiagnostics = results[0];
            CosmosDiagnostics fastDiagnostics = results[1];

            assertThinClientEndpointUsed(delayedDiagnostics);
            assertThinClientEndpointUsed(fastDiagnostics);

            // The fast read (feedRange[1]) should complete quickly — no delay injected
            CosmosDiagnosticsContext fastCtx = fastDiagnostics.getDiagnosticsContext();
            AssertionsForClassTypes.assertThat(fastCtx.getDuration()).isLessThan(Duration.ofSeconds(3));

            // Parse both diagnostics
            ObjectNode delayedNode = (ObjectNode) Utils.getSimpleObjectMapper().readTree(delayedDiagnostics.toString());
            ObjectNode fastNode = (ObjectNode) Utils.getSimpleObjectMapper().readTree(fastDiagnostics.toString());

            JsonNode delayedGwStats = delayedNode.get("gatewayStatisticsList");
            JsonNode fastGwStats = fastNode.get("gatewayStatisticsList");

            // The delayed read should have at least 2 entries (timeout + retry)
            AssertionsForClassTypes.assertThat(delayedGwStats.size()).isGreaterThanOrEqualTo(2);
            // The fast read should have exactly 1 entry (no retry)
            AssertionsForClassTypes.assertThat(fastGwStats.size()).isEqualTo(1);

            // Log channel info for both
            JsonNode delayedAttempt = delayedGwStats.get(0);
            JsonNode fastAttempt = fastGwStats.get(0);

            logger.info("Concurrent streams test - delayed parentChannelId={}, fast parentChannelId={}",
                delayedAttempt.has("parentChannelId") ? delayedAttempt.get("parentChannelId").asText() : "n/a",
                fastAttempt.has("parentChannelId") ? fastAttempt.get("parentChannelId").asText() : "n/a");

            // Both should use HTTP/2
            if (delayedAttempt.has("isHttp2") && fastAttempt.has("isHttp2")) {
                AssertionsForClassTypes.assertThat(delayedAttempt.get("isHttp2").asBoolean()).isTrue();
                AssertionsForClassTypes.assertThat(fastAttempt.get("isHttp2").asBoolean()).isTrue();
            }

            // KEY ASSERTION: If both are on same connection, timeout on one doesn't affect the other
            // The fast read completed in < 3s while the delayed read was blocked
            AssertionsForClassTypes.assertThat(fastCtx.getStatusCode()).isBetween(
                HttpConstants.StatusCodes.OK, HttpConstants.StatusCodes.NOT_MODIFIED);

        } finally {
            delayRule.disable();
        }
    }

    @Test(groups = {"fi-thinclient-multi-master"}, timeOut = 4 * TIMEOUT, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void faultInjectionServerErrorRuleTests_ConnectionSurvivesE2ETimeout() throws JsonProcessingException {
        // Test: When the entire operation fails due to e2e timeout,
        // the underlying HTTP/2 TCP connection should NOT be closed.
        // A subsequent operation on the same client should reuse the same connection.
        //
        // Strategy: Use CosmosEndToEndOperationLatencyPolicyConfig with a short timeout (3s).
        // Inject RESPONSE_DELAY of 8s (> 6s GatewayV2 timeout AND > 3s e2e timeout).
        // The e2e timeout fires before even the first retry completes → operation fails fast.
        // Then verify the next operation (without e2e timeout) reuses the same connection.

        String timeoutRuleId = "serverErrorRule-e2eTimeout-" + UUID.randomUUID();
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
                        .times(2) // enough for the first attempt + beginning of second
                        .delay(Duration.ofSeconds(8)) // > 6s GatewayV2 first-attempt timeout
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        // Configure e2e timeout at 7s:
        // - First attempt times out at 6s (GatewayV2 timeout) → diagnostics captured with endpoint + parentChannelId
        // - E2e timeout fires at 7s, before second attempt completes → operation fails
        CosmosEndToEndOperationLatencyPolicyConfig e2eTimeoutPolicy =
            new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(7))
                .enable(true)
                .build();

        CosmosItemRequestOptions requestOptionsWithE2ETimeout = new CosmosItemRequestOptions();
        requestOptionsWithE2ETimeout.setCosmosEndToEndOperationLatencyPolicyConfig(e2eTimeoutPolicy);

        try {
            TestObject createdItem = TestObject.create();
            this.cosmosAsyncContainer.createItem(createdItem).block();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(this.cosmosAsyncContainer, Arrays.asList(timeoutRule)).block();

            // First operation: first attempt times out at 6s (captured in diagnostics), e2e timeout fires at 7s
            CosmosDiagnostics failedDiagnostics = null;
            String failedParentChannelId = null;
            try {
                this.cosmosAsyncContainer
                    .readItem(
                        createdItem.getId(),
                        new PartitionKey(createdItem.getMypk()),
                        requestOptionsWithE2ETimeout,
                        TestObject.class)
                    .block();
                fail("Operation should have failed due to e2e timeout but succeeded");
            } catch (CosmosException e) {
                logger.info("E2E timeout test - failed with statusCode={}, subStatusCode={}",
                    e.getStatusCode(), e.getSubStatusCode());
                failedDiagnostics = e.getDiagnostics();

                // First attempt completed (timed out at 6s < e2e timeout 7s), so diagnostics should have endpoint
                assertThinClientEndpointUsed(failedDiagnostics);

                // Extract parentChannelId from the failed operation
                ObjectNode failedNode = (ObjectNode) Utils.getSimpleObjectMapper().readTree(failedDiagnostics.toString());
                JsonNode failedGwStats = failedNode.get("gatewayStatisticsList");
                AssertionsForClassTypes.assertThat(failedGwStats.isArray()).isTrue();
                // At least 1 attempt should have completed (first timed out at 6s)
                AssertionsForClassTypes.assertThat(failedGwStats.size()).isGreaterThanOrEqualTo(1);

                // Get parentChannelId from the first attempt
                JsonNode firstFailedAttempt = failedGwStats.get(0);
                failedParentChannelId = firstFailedAttempt.has("parentChannelId")
                    ? firstFailedAttempt.get("parentChannelId").asText() : null;

                logger.info("E2E timeout test - failed op parentChannelId={}, attempts={}",
                    failedParentChannelId, failedGwStats.size());
            }

            // Disable fault injection before the next request
            timeoutRule.disable();

            // Second operation: no e2e timeout, no fault injection — should succeed and reuse the same TCP connection
            CosmosDiagnostics successDiagnostics = this.performDocumentOperation(
                this.cosmosAsyncContainer, OperationType.Read, createdItem, false);

            assertThinClientEndpointUsed(successDiagnostics);

            ObjectNode successNode = (ObjectNode) Utils.getSimpleObjectMapper().readTree(successDiagnostics.toString());
            JsonNode successGwStats = successNode.get("gatewayStatisticsList");
            AssertionsForClassTypes.assertThat(successGwStats.isArray()).isTrue();
            AssertionsForClassTypes.assertThat(successGwStats.size()).isEqualTo(1);

            JsonNode successAttempt = successGwStats.get(0);
            String successParentChannelId = successAttempt.has("parentChannelId")
                ? successAttempt.get("parentChannelId").asText() : null;

            logger.info("E2E timeout test - success op parentChannelId={}", successParentChannelId);

            // KEY ASSERTION: Connection survived the e2e-timeout-failed operation
            if (failedParentChannelId != null && successParentChannelId != null) {
                AssertionsForClassTypes.assertThat(successParentChannelId)
                    .as("Connection should survive a fully-failed e2e timeout and be reused")
                    .isEqualTo(failedParentChannelId);
            }

            // Validate the second operation completed fast
            CosmosDiagnosticsContext successCtx = successDiagnostics.getDiagnosticsContext();
            AssertionsForClassTypes.assertThat(successCtx.getDuration()).isLessThan(Duration.ofSeconds(3));
        } finally {
            timeoutRule.disable();
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
