// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.testng.AssertJUnit.fail;

public class FaultInjectionServerErrorRuleOnGatewayTests extends TestSuiteBase {

    private static final String FAULT_INJECTION_RULE_NON_APPLICABLE_ADDRESS = "Addresses mismatch";
    private static final String FAULT_INJECTION_RULE_NON_APPLICABLE_REGION_ENDPOINT = "RegionEndpoint mismatch";
    private static final String FAULT_INJECTION_RULE_NON_APPLICABLE_HIT_LIMIT = "Hit Limit reached";

    private CosmosAsyncClient client;
    private CosmosAsyncContainer cosmosAsyncContainer;
    private DatabaseAccount databaseAccount;

    private Map<String, String> readRegionMap;
    private Map<String, String> writeRegionMap;

    @Factory(dataProvider = "clientBuildersWithGateway")
    public FaultInjectionServerErrorRuleOnGatewayTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @BeforeClass(groups = {"multi-region", "simple"}, timeOut = TIMEOUT)
    public void beforeClass() {
        client = getClientBuilder().buildAsyncClient();
        AsyncDocumentClient asyncDocumentClient = BridgeInternal.getContextClient(client);
        GlobalEndpointManager globalEndpointManager = asyncDocumentClient.getGlobalEndpointManager();

        DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();
        this.databaseAccount = databaseAccount;
        this.cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(client);
        this.readRegionMap = this.getRegionMap(databaseAccount, false);
        this.writeRegionMap = this.getRegionMap(databaseAccount, true);
    }

    @DataProvider(name = "operationTypeProvider")
    public static Object[][] operationTypeProvider() {
        return new Object[][]{
            { OperationType.Read, FaultInjectionOperationType.READ_ITEM },
            { OperationType.Replace, FaultInjectionOperationType.REPLACE_ITEM },
            { OperationType.Create, FaultInjectionOperationType.CREATE_ITEM },
            { OperationType.Delete, FaultInjectionOperationType.DELETE_ITEM },
            { OperationType.Query, FaultInjectionOperationType.QUERY_ITEM },
            { OperationType.Patch, FaultInjectionOperationType.PATCH_ITEM }
        };
    }

    @DataProvider(name = "faultInjectionOperationTypeProvider")
    public static Object[][] faultInjectionOperationTypeProvider() {
        return new Object[][]{
            // fault injection operation type, primaryAddressOnly
            { FaultInjectionOperationType.READ_ITEM, false },
            { FaultInjectionOperationType.REPLACE_ITEM, true },
            { FaultInjectionOperationType.CREATE_ITEM, true },
            { FaultInjectionOperationType.DELETE_ITEM, true},
            { FaultInjectionOperationType.QUERY_ITEM, false },
            { FaultInjectionOperationType.PATCH_ITEM, true }
        };
    }

    @DataProvider(name = "faultInjectionServerErrorResponseProvider")
    public static Object[][] faultInjectionServerErrorResponseProvider() {
        return new Object[][]{
            // faultInjectionServerError, will SDK retry, errorStatusCode, errorSubStatusCode
            { FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR, false, 500, 0 },
            { FaultInjectionServerErrorType.RETRY_WITH, false, 449, 0 },
            { FaultInjectionServerErrorType.TOO_MANY_REQUEST, true, 429, 0 },
            { FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE, true, 404, 1002 },
            { FaultInjectionServerErrorType.SERVICE_UNAVAILABLE, false, 503, 21008 }
        };
    }

    @Test(groups = {"multi-region"}, timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_Region() throws JsonProcessingException {
        List<String> preferredLocations = this.readRegionMap.keySet().stream().collect(Collectors.toList());

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
            clientWithPreferredRegion = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .consistencyLevel(BridgeInternal.getContextClient(this.client).getConsistencyLevel())
                .preferredRegions(preferredLocations)
                .gatewayMode()
                .buildAsyncClient();

            CosmosAsyncContainer container =
                clientWithPreferredRegion
                    .getDatabase(this.cosmosAsyncContainer.getDatabase().getId())
                    .getContainer(this.cosmosAsyncContainer.getId());

            TestItem createdItem = TestItem.createNewItem();
            container.createItem(createdItem).block();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(
                    container,
                    Arrays.asList(serverErrorRuleLocalRegion, serverErrorRuleRemoteRegion))
                .block();

            assertThat(
                serverErrorRuleLocalRegion.getRegionEndpoints().size() == 1
                    && serverErrorRuleLocalRegion.getRegionEndpoints().get(0).equals(this.readRegionMap.get(preferredLocations.get(0))));
            assertThat(
                serverErrorRuleRemoteRegion.getRegionEndpoints().size() == 1
                    && serverErrorRuleRemoteRegion.getRegionEndpoints().get(0).equals(this.readRegionMap.get(preferredLocations.get(1))));

            // Validate fault injection applied in the local region
            CosmosDiagnostics cosmosDiagnostics = this.performDocumentOperation(container, OperationType.Read, createdItem);

            this.validateHitCount(serverErrorRuleLocalRegion, 1, OperationType.Read, ResourceType.Document);
            this.validateHitCount(serverErrorRuleRemoteRegion, 0, OperationType.Read, ResourceType.Document);

            this.validateFaultInjectionRuleApplied(
                cosmosDiagnostics,
                OperationType.Read,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                localRegionRuleId,
                false
            );

            serverErrorRuleLocalRegion.disable();

            cosmosDiagnostics = this.performDocumentOperation(container, OperationType.Read, createdItem);
            this.validateNoFaultInjectionApplied(cosmosDiagnostics, OperationType.Read, FAULT_INJECTION_RULE_NON_APPLICABLE_REGION_ENDPOINT);
        } finally {
            serverErrorRuleLocalRegion.disable();
            serverErrorRuleRemoteRegion.disable();
            safeClose(clientWithPreferredRegion);
        }
    }

    @Test(groups = {"multi-region", "simple"}, timeOut = 4 * TIMEOUT)
    public void faultInjectionServerErrorRuleTests_Partition() throws JsonProcessingException {
        for (int i = 0; i < 10; i++) {
            cosmosAsyncContainer.createItem(TestItem.createNewItem()).block();
        }

        // getting one item from each feedRange
        List<FeedRange> feedRanges = cosmosAsyncContainer.getFeedRanges().block();
        assertThat(feedRanges.size()).isGreaterThan(1);

        String query = "select * from c";
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        cosmosQueryRequestOptions.setFeedRange(feedRanges.get(0));
        TestItem itemOnFeedRange0 = cosmosAsyncContainer.queryItems(query, cosmosQueryRequestOptions, TestItem.class).blockFirst();

        cosmosQueryRequestOptions.setFeedRange(feedRanges.get(1));
        TestItem itemOnFeedRange1 = cosmosAsyncContainer.queryItems(query, cosmosQueryRequestOptions, TestItem.class).blockFirst();

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

        CosmosFaultInjectionHelper.configureFaultInjectionRules(cosmosAsyncContainer, Arrays.asList(serverErrorRuleByFeedRange)).block();
        assertThat(
            serverErrorRuleByFeedRange.getRegionEndpoints().size() == this.readRegionMap.size()
                && serverErrorRuleByFeedRange.getRegionEndpoints().containsAll(this.readRegionMap.keySet()));

        // Issue a read item for the same feed range as configured in the fault injection rule
        CosmosDiagnostics cosmosDiagnostics =
            cosmosAsyncContainer
                .readItem(itemOnFeedRange0.getId(), new PartitionKey(itemOnFeedRange0.getId()), JsonNode.class)
                .block()
                .getDiagnostics();

        this.validateHitCount(serverErrorRuleByFeedRange, 1, OperationType.Read, ResourceType.Document);
        this.validateFaultInjectionRuleApplied(
            cosmosDiagnostics,
            OperationType.Read,
            HttpConstants.StatusCodes.TOO_MANY_REQUESTS,
            HttpConstants.SubStatusCodes.UNKNOWN,
            feedRangeRuleId,
            true
        );

        // Issue a read item to different feed range
        try {
            cosmosDiagnostics = cosmosAsyncContainer
                .readItem(itemOnFeedRange1.getId(), new PartitionKey(itemOnFeedRange1.getId()), JsonNode.class)
                .block()
                .getDiagnostics();
            this.validateNoFaultInjectionApplied(cosmosDiagnostics, OperationType.Read, FAULT_INJECTION_RULE_NON_APPLICABLE_ADDRESS);
        } finally {
            serverErrorRuleByFeedRange.disable();
        }
    }

    @Test(groups = {"multi-region", "simple"}, timeOut = 4 * TIMEOUT)
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

            assertThat(timeoutRule.getHitCount()).isEqualTo(1);
            this.validateHitCount(timeoutRule, 1, OperationType.Read, ResourceType.Document);

            this.validateFaultInjectionRuleApplied(
                itemResponse.getDiagnostics(),
                OperationType.Read,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT,
                timeoutRuleId,
                true
            );

        } finally {
            timeoutRule.disable();
        }
    }

    @Test(groups = {"multi-region", "simple"}, timeOut = 4 * TIMEOUT)
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

            assertThat(serverConnectionDelayRule.getHitCount()).isEqualTo(1l);
            this.validateFaultInjectionRuleApplied(
                itemResponse.getDiagnostics(),
                OperationType.Create,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE,
                ruleId,
                true
            );

        } finally {
            serverConnectionDelayRule.disable();
        }
    }

    @Test(groups = {"multi-region", "simple"}, dataProvider = "faultInjectionServerErrorResponseProvider", timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_ServerErrorResponse(
        FaultInjectionServerErrorType serverErrorType,
        boolean canRetry,
        int errorStatusCode,
        int errorSubStatusCode) throws JsonProcessingException {

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

        } finally {
            serverErrorRule.disable();
        }
    }

    @Test(groups = {"multi-region", "simple"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_HitLimit(
        OperationType operationType,
        FaultInjectionOperationType faultInjectionOperationType) throws JsonProcessingException {

        TestItem createdItem = TestItem.createNewItem();
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
            assertThat(
                hitLimitServerErrorRule.getRegionEndpoints().size() == this.readRegionMap.size()
                    && hitLimitServerErrorRule.getRegionEndpoints().containsAll(this.readRegionMap.keySet()));

            for (int i = 1; i <= 3; i++) {
                CosmosDiagnostics cosmosDiagnostics = this.performDocumentOperation(cosmosAsyncContainer, operationType, createdItem);
                if (i <= 2) {
                    this.validateFaultInjectionRuleApplied(
                        cosmosDiagnostics,
                        operationType,
                        HttpConstants.StatusCodes.TOO_MANY_REQUESTS,
                        HttpConstants.SubStatusCodes.UNKNOWN,
                        hitLimitRuleId,
                        true
                    );
                } else {
                    // the fault injection rule will not be applied due to hitLimit
                    cosmosDiagnostics = this.performDocumentOperation(cosmosAsyncContainer,operationType, createdItem);
                    this.validateNoFaultInjectionApplied(cosmosDiagnostics, operationType, FAULT_INJECTION_RULE_NON_APPLICABLE_HIT_LIMIT);
                }
            }

            this.validateHitCount(hitLimitServerErrorRule, 2, operationType, ResourceType.Document);
        } finally {
            hitLimitServerErrorRule.disable();
        }
    }

    @AfterClass(groups = {"multi-region", "simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    private CosmosDiagnostics performDocumentOperation(
        CosmosAsyncContainer cosmosAsyncContainer,
        OperationType operationType,
        TestItem createdItem) {
        try {
            if (operationType == OperationType.Query) {
                CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
                String query = String.format("SELECT * from c where c.id = '%s'", createdItem.getId());
                FeedResponse<TestItem> itemFeedResponse =
                    cosmosAsyncContainer.queryItems(query, queryRequestOptions, TestItem.class).byPage().blockFirst();

                return itemFeedResponse.getCosmosDiagnostics();
            }

            if (operationType == OperationType.Read
                || operationType == OperationType.Delete
                || operationType == OperationType.Replace
                || operationType == OperationType.Create
                || operationType == OperationType.Patch
                || operationType == OperationType.Upsert) {

                if (operationType == OperationType.Read) {
                    return cosmosAsyncContainer.readItem(
                        createdItem.getId(),
                        new PartitionKey(createdItem.getId()),
                        TestItem.class).block().getDiagnostics();
                }

                if (operationType == OperationType.Replace) {
                    return cosmosAsyncContainer.replaceItem(
                        createdItem,
                        createdItem.getId(),
                        new PartitionKey(createdItem.getId())).block().getDiagnostics();
                }

                if (operationType == OperationType.Delete) {
                    return cosmosAsyncContainer.deleteItem(createdItem, null).block().getDiagnostics();
                }

                if (operationType == OperationType.Create) {
                    return cosmosAsyncContainer.createItem(TestItem.createNewItem()).block().getDiagnostics();
                }

                if (operationType == OperationType.Upsert) {
                    return cosmosAsyncContainer.upsertItem(TestItem.createNewItem()).block().getDiagnostics();
                }

                if (operationType == OperationType.Patch) {
                    CosmosPatchOperations patchOperations =
                        CosmosPatchOperations
                            .create()
                            .add("newPath", "newPath");
                    return cosmosAsyncContainer
                        .patchItem(createdItem.getId(), new PartitionKey(createdItem.getId()), patchOperations, TestItem.class)
                        .block().getDiagnostics();
                }
            }

            throw new IllegalArgumentException("The operation type is not supported");
        } catch (CosmosException cosmosException) {
            return cosmosException.getDiagnostics();
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
        if (operationType == OperationType.Query) {
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
            assertThat(gatewayStatisticsList.isArray()).isTrue();

            if (canRetryOnFaultInjectedError) {
                if (gatewayStatisticsList.size() != 2) {
                    System.out.println("FaultInjectionGatewayStatisticsList is wrong " + cosmosDiagnostics);
                }
                assertThat(gatewayStatisticsList.size()).isEqualTo(2);
            } else {
                assertThat(gatewayStatisticsList.size()).isOne();
            }
            JsonNode gatewayStatistics = gatewayStatisticsList.get(0);
            assertThat(gatewayStatistics).isNotNull();
            assertThat(gatewayStatistics.get("statusCode").asInt()).isEqualTo(statusCode);
            assertThat(gatewayStatistics.get("subStatusCode").asInt()).isEqualTo(subStatusCode);
            assertThat(gatewayStatistics.get("faultInjectionRuleId").asText()).isEqualTo(ruleId);
        }
    }

    private void validateNoFaultInjectionApplied(
        CosmosDiagnostics cosmosDiagnostics,
        OperationType operationType,
        String faultInjectionNonApplicableReason) throws JsonProcessingException {

        List<ObjectNode> diagnosticsNode = new ArrayList<>();
        if (operationType == OperationType.Query) {
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
            assertThat(gatewayStatisticsList.isArray()).isTrue();

            for (int i = 0; i < gatewayStatisticsList.size(); i++) {
                JsonNode gatewayStatistics = gatewayStatisticsList.get(i);
                assertThat(gatewayStatistics.get("faultInjectionRuleId")).isNull();
                assertThat(gatewayStatistics.get("faultInjectionEvaluationResults")).isNotNull();
                assertThat(gatewayStatistics.get("faultInjectionEvaluationResults").toString().contains(faultInjectionNonApplicableReason));
            }
            assertThat(gatewayStatisticsList.size()).isOne();
        }
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

    private void validateHitCount(
        FaultInjectionRule rule,
        long totalHitCount,
        OperationType operationType,
        ResourceType resourceType) {

        assertThat(rule.getHitCount()).isEqualTo(totalHitCount);
        if (totalHitCount > 0) {
            assertThat(rule.getHitCountDetails().size()).isEqualTo(1);
            assertThat(rule.getHitCountDetails().get(operationType.toString() + "-" + resourceType.toString())).isEqualTo(totalHitCount);
        }
    }
}
