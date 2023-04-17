// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.faultinjection;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
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
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.test.TestItem;
import com.azure.cosmos.test.TestSuiteBase;
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

public class FaultInjectionServerErrorRuleTests extends TestSuiteBase {

    private static final int TIMEOUT = 60000;
    private CosmosAsyncClient client;
    private CosmosAsyncContainer cosmosAsyncContainer;
    private DatabaseAccount databaseAccount;

    private Map<String, String> readRegionMap;
    private Map<String, String> writeRegionMap;

    @Factory(dataProvider = "simpleClientBuildersWithJustDirectTcp")
    public FaultInjectionServerErrorRuleTests(CosmosClientBuilder clientBuilder) {
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
            { OperationType.Read },
            { OperationType.Replace },
            { OperationType.Create },
            { OperationType.Delete },
            { OperationType.Query },
            { OperationType.Patch }
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
            { FaultInjectionServerErrorType.RETRY_WITH, true, 449, 0 },
            { FaultInjectionServerErrorType.TOO_MANY_REQUEST, true, 429, 0 },
            { FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE, true, 404, 1002 },
            { FaultInjectionServerErrorType.TIMEOUT, false, 408, 0 },
            { FaultInjectionServerErrorType.PARTITION_IS_MIGRATING, true, 410, 1008 },
            { FaultInjectionServerErrorType.PARTITION_IS_SPLITTING, true, 410, 1007 }
        };
    }

    @Test(groups = {"multi-region", "simple"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_OperationType(OperationType operationType) throws JsonProcessingException {
        // Test for SERVER_GONE, the operation type will be ignored after getting the addresses
        String serverGoneRuleId = "serverErrorRule-serverGone-" + UUID.randomUUID();
        FaultInjectionRule serverGoneErrorRule =
            new FaultInjectionRuleBuilder(serverGoneRuleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(FaultInjectionOperationType.READ_ITEM)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.GONE)
                        .times(1)
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        String tooManyRequestsRuleId = "serverErrorRule-tooManyRequests-" + UUID.randomUUID();
        FaultInjectionRule serverTooManyRequestsErrorRule =
            new FaultInjectionRuleBuilder(tooManyRequestsRuleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(FaultInjectionOperationType.READ_ITEM)
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
            TestItem createdItem = TestItem.createNewItem();
            cosmosAsyncContainer.createItem(createdItem).block();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(cosmosAsyncContainer, Arrays.asList(serverGoneErrorRule)).block();

            assertThat(serverGoneErrorRule.getAddresses().size()).isZero();
            assertThat(serverGoneErrorRule.getRegionEndpoints().size() == this.readRegionMap.size() + 1
               && serverGoneErrorRule.getRegionEndpoints().containsAll(this.readRegionMap.values()));

            CosmosDiagnostics cosmosDiagnostics = this.performDocumentOperation(cosmosAsyncContainer, operationType, createdItem);
            this.validateFaultInjectionRuleApplied(
                cosmosDiagnostics,
                operationType,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.UNKNOWN,
                serverGoneRuleId,
                true);

            // Test for Server error TOO_MANY_REQUESTS, the rules will be applied by operation type
            serverGoneErrorRule.disable();
            CosmosFaultInjectionHelper.configureFaultInjectionRules(cosmosAsyncContainer, Arrays.asList(serverTooManyRequestsErrorRule)).block();
            assertThat(serverGoneErrorRule.getAddresses().size()).isZero();
            assertThat(serverGoneErrorRule.getRegionEndpoints().size() == this.readRegionMap.size() + 1
                && serverGoneErrorRule.getRegionEndpoints().containsAll(this.readRegionMap.values()));

            cosmosDiagnostics = this.performDocumentOperation(cosmosAsyncContainer, operationType, createdItem);
            if (operationType == OperationType.Read) {
                assertThat(serverTooManyRequestsErrorRule.getHitCount()).isEqualTo(1);
                this.validateFaultInjectionRuleApplied(
                    cosmosDiagnostics,
                    operationType,
                    HttpConstants.StatusCodes.TOO_MANY_REQUESTS,
                    HttpConstants.SubStatusCodes.UNKNOWN,
                    tooManyRequestsRuleId,
                    true);
            } else {
                this.validateNoFaultInjectionApplied(cosmosDiagnostics, operationType);
            }

        } finally {
            serverGoneErrorRule.disable();
            serverTooManyRequestsErrorRule.disable();
        }
    }

    @Test(groups = {"multi-region"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_OperationTypeImpactAddresses(OperationType operationType) throws JsonProcessingException {
        // Test the operation type can impact which region or replica the rule will be applicable
        TestItem createdItem = TestItem.createNewItem();
        this.cosmosAsyncContainer.createItem(createdItem).block();

        String writeRegionServerGoneRuleId = "serverErrorRule-writeRegionOnly-" + UUID.randomUUID();
        FaultInjectionRule writeRegionServerGoneErrorRule =
            new FaultInjectionRuleBuilder(writeRegionServerGoneRuleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(FaultInjectionOperationType.CREATE_ITEM)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.GONE)
                        .times(1)
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();


        String primaryReplicaServerGoneRuleId = "serverErrorRule-primaryReplicaOnly-" + UUID.randomUUID();
        FaultInjectionRule primaryReplicaServerGoneErrorRule =
            new FaultInjectionRuleBuilder(primaryReplicaServerGoneRuleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(FaultInjectionOperationType.CREATE_ITEM)
                        .endpoints(
                            new FaultInjectionEndpointBuilder(FeedRange.forLogicalPartition(new PartitionKey(createdItem.getId())))
                                .replicaCount(3)
                                .build())
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.GONE)
                        .times(1)
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        // Test the rule will limit to only write region
        // Setting the preferred regions in a way so that read and write operation will go to different regions
        List<String> preferredRegionList = new ArrayList<>();
        for (String region : this.readRegionMap.keySet()) {
            if (this.writeRegionMap.containsKey(region)) {
                preferredRegionList.add(region);
            } else {
                preferredRegionList.add(0, region);
            }
        }

        CosmosAsyncClient clientWithPreferredRegions;
        try {
            clientWithPreferredRegions = new CosmosClientBuilder()
                .key(TestConfigurations.MASTER_KEY)
                .endpoint(TestConfigurations.HOST)
                .preferredRegions(preferredRegionList)
                .consistencyLevel(ConsistencyLevel.EVENTUAL)
                .buildAsyncClient();

            CosmosAsyncContainer container =
                clientWithPreferredRegions
                    .getDatabase(this.cosmosAsyncContainer.getDatabase().getId())
                    .getContainer(this.cosmosAsyncContainer.getId());

            CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(writeRegionServerGoneErrorRule)).block();
            assertThat(writeRegionServerGoneErrorRule.getRegionEndpoints().size()).isEqualTo(2);

            CosmosDiagnostics cosmosDiagnostics = this.performDocumentOperation(container, operationType, createdItem);
            if (operationType.isWriteOperation()) {
                assertThat(writeRegionServerGoneErrorRule.getHitCount()).isEqualTo(1);
                this.validateFaultInjectionRuleApplied(
                    cosmosDiagnostics,
                    operationType,
                    HttpConstants.StatusCodes.GONE,
                    HttpConstants.SubStatusCodes.UNKNOWN,
                    writeRegionServerGoneRuleId,
                    true);
            } else {
                this.validateNoFaultInjectionApplied(cosmosDiagnostics, operationType);
            }

            writeRegionServerGoneErrorRule.disable();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(primaryReplicaServerGoneErrorRule)).block();
            assertThat(
                primaryReplicaServerGoneErrorRule.getRegionEndpoints().size() == this.writeRegionMap.size()
                        && primaryReplicaServerGoneErrorRule.getRegionEndpoints().containsAll(this.writeRegionMap.keySet()));
            assertThat(primaryReplicaServerGoneErrorRule.getAddresses().size() == this.writeRegionMap.size());
        } finally {
            writeRegionServerGoneErrorRule.disable();
            primaryReplicaServerGoneErrorRule.disable();
        }
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
                        .region(preferredLocations.get(0))
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.GONE)
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
                        .region(preferredLocations.get(1))
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.GONE)
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
                .directMode()
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
            assertThat(serverErrorRuleLocalRegion.getHitCount()).isEqualTo(1);
            assertThat(serverErrorRuleRemoteRegion.getHitCount()).isEqualTo(0);

            this.validateFaultInjectionRuleApplied(
                cosmosDiagnostics,
                OperationType.Read,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.UNKNOWN,
                localRegionRuleId,
                true
            );

            serverErrorRuleLocalRegion.disable();

            cosmosDiagnostics = this.performDocumentOperation(container, OperationType.Read, createdItem);
            this.validateNoFaultInjectionApplied(cosmosDiagnostics, OperationType.Read);
        } finally {
            serverErrorRuleLocalRegion.disable();
            serverErrorRuleRemoteRegion.disable();
            safeClose(clientWithPreferredRegion);
        }
    }

    @Test(groups = {"multi-region", "simple"}, timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_Partition() throws JsonProcessingException {
        TestItem createdItem = TestItem.createNewItem();
        cosmosAsyncContainer.createItem(createdItem).block();

        List<FeedRange> feedRanges = cosmosAsyncContainer.getFeedRanges().block();

        // set rule by feed range
        String feedRangeRuleId = "ServerErrorRule-FeedRange-" + UUID.randomUUID();

        FaultInjectionRule serverErrorRuleByFeedRange =
            new FaultInjectionRuleBuilder(feedRangeRuleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .endpoints(new FaultInjectionEndpointBuilder(feedRanges.get(0)).build()) // by default setting on all replicas
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.GONE)
                        .times(1)
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        CosmosFaultInjectionHelper.configureFaultInjectionRules(cosmosAsyncContainer, Arrays.asList(serverErrorRuleByFeedRange)).block();
        assertThat(
            serverErrorRuleByFeedRange.getRegionEndpoints().size() == this.readRegionMap.size()
            && serverErrorRuleByFeedRange.getRegionEndpoints().containsAll(this.readRegionMap.keySet()));
        assertThat(serverErrorRuleByFeedRange.getAddresses().size()).isBetween(
            this.readRegionMap.size() * 3, this.readRegionMap.size() * 5);

        // Issue a query to the feed range which configured fault injection rule and validate fault injection rule is applied
        String query = "select * from c";
        CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
        queryRequestOptions.setFeedRange(feedRanges.get(0));

        CosmosDiagnostics cosmosDiagnostics =
            cosmosAsyncContainer.queryItems(query, queryRequestOptions, TestItem.class).byPage().blockFirst().getCosmosDiagnostics();

        assertThat(serverErrorRuleByFeedRange.getHitCount()).isEqualTo(1);
        this.validateFaultInjectionRuleApplied(
            cosmosDiagnostics,
            OperationType.Query,
            HttpConstants.StatusCodes.GONE,
            HttpConstants.SubStatusCodes.UNKNOWN,
            feedRangeRuleId,
            true
        );

        // Issue a query to the feed range which is not configured fault injection rule and validate no fault injection is applied
        queryRequestOptions.setFeedRange(feedRanges.get(1));

        try {
            cosmosDiagnostics = cosmosAsyncContainer.queryItems(query, queryRequestOptions, TestItem.class).byPage().blockFirst().getCosmosDiagnostics();
            this.validateNoFaultInjectionApplied(cosmosDiagnostics, OperationType.Query);
        } finally {
            serverErrorRuleByFeedRange.disable();
        }
    }

    @Test(groups = {"multi-region", "simple"}, timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_ServerResponseDelay() throws JsonProcessingException {
        CosmosAsyncClient newClient = null; // creating new client to force creating new connections
        // define another rule which can simulate timeout
        String timeoutRuleId = "serverErrorRule-transitTimeout-" + UUID.randomUUID();
        FaultInjectionRule timeoutRule =
            new FaultInjectionRuleBuilder(timeoutRuleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(FaultInjectionOperationType.READ_ITEM)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
                        .times(1)
                        .delay(Duration.ofSeconds(6)) // the default time out is 5s
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();
        try {
            DirectConnectionConfig directConnectionConfig = DirectConnectionConfig.getDefaultConfig();
            directConnectionConfig.setConnectTimeout(Duration.ofSeconds(1));

            newClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .consistencyLevel(BridgeInternal.getContextClient(this.client).getConsistencyLevel())
                .directMode(directConnectionConfig)
                .buildAsyncClient();

            CosmosAsyncContainer container =
                newClient
                    .getDatabase(cosmosAsyncContainer.getDatabase().getId())
                    .getContainer(cosmosAsyncContainer.getId());

            // create a new item to be used by read operations
            TestItem createdItem = TestItem.createNewItem();
            container.createItem(createdItem).block();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(timeoutRule)).block();
            CosmosItemResponse<TestItem> itemResponse =
                container.readItem(createdItem.getId(), new PartitionKey(createdItem.getId()), TestItem.class).block();

            assertThat(timeoutRule.getHitCount()).isEqualTo(1);
            this.validateFaultInjectionRuleApplied(
                itemResponse.getDiagnostics(),
                OperationType.Read,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.UNKNOWN,
                timeoutRuleId,
                true
            );

        } finally {
            timeoutRule.disable();
            safeClose(newClient);
        }
    }

    @Test(groups = {"multi-region", "simple"}, timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_ServerConnectionDelay() throws JsonProcessingException {
        CosmosAsyncClient newClient = null; // creating new client to force creating new connections
        // simulate high channel acquisition/connectionTimeout
        String ruleId = "serverErrorRule-serverConnectionDelay-" + UUID.randomUUID();
        FaultInjectionRule serverConnectionDelayRule =
            new FaultInjectionRuleBuilder(ruleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(FaultInjectionOperationType.CREATE_ITEM)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.CONNECTION_DELAY)
                        .delay(Duration.ofSeconds(2))
                        .times(1)
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        try {
            DirectConnectionConfig directConnectionConfig = DirectConnectionConfig.getDefaultConfig();
            directConnectionConfig.setConnectTimeout(Duration.ofSeconds(1));

            newClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .consistencyLevel(BridgeInternal.getContextClient(this.client).getConsistencyLevel())
                .directMode(directConnectionConfig)
                .buildAsyncClient();

            CosmosAsyncContainer container =
                newClient
                    .getDatabase(cosmosAsyncContainer.getDatabase().getId())
                    .getContainer(cosmosAsyncContainer.getId());

            CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(serverConnectionDelayRule)).block();
            CosmosItemResponse<TestItem> itemResponse = container.createItem(TestItem.createNewItem()).block();

            // Due to the replica validation, there could be an extra open connection call flow, while the rule will also be applied on.
            assertThat(serverConnectionDelayRule.getHitCount()).isBetween(1l, 2l);
            this.validateFaultInjectionRuleApplied(
                itemResponse.getDiagnostics(),
                OperationType.Create,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.UNKNOWN,
                ruleId,
                true
            );

        } finally {
            serverConnectionDelayRule.disable();
            safeClose(newClient);
        }
    }

    @Test(groups = {"multi-region", "simple"}, dataProvider = "faultInjectionOperationTypeProvider", timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_ServerConnectionDelay_warmup(
        FaultInjectionOperationType operationType,
        boolean primaryAddressesOnly) {

        CosmosAsyncClient newClient = null; // creating new client to force creating new connections
        // simulate high channel acquisition/connectionTimeout during openConnection flow
        String ruleId = "serverErrorRule-serverConnectionDelay-warmup" + UUID.randomUUID();
        FaultInjectionRule serverConnectionDelayWarmupRule =
            new FaultInjectionRuleBuilder(ruleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(operationType)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.CONNECTION_DELAY)
                        .delay(Duration.ofSeconds(2))
                        .times(1)
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        try {
            DirectConnectionConfig directConnectionConfig = DirectConnectionConfig.getDefaultConfig();
            directConnectionConfig.setConnectTimeout(Duration.ofSeconds(1));

            newClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .consistencyLevel(BridgeInternal.getContextClient(this.client).getConsistencyLevel())
                .directMode(directConnectionConfig)
                .buildAsyncClient();

            CosmosAsyncContainer container =
                newClient
                    .getDatabase(cosmosAsyncContainer.getDatabase().getId())
                    .getContainer(cosmosAsyncContainer.getId());

            CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(serverConnectionDelayWarmupRule)).block();

            int partitionSize = container.getFeedRanges().block().size();
            container.openConnectionsAndInitCaches().block();

            if (primaryAddressesOnly) {
                assertThat(serverConnectionDelayWarmupRule.getHitCount()).isEqualTo(partitionSize);
            } else {
                assertThat(serverConnectionDelayWarmupRule.getHitCount()).isBetween(partitionSize * 3L, partitionSize * 5L);
            }
        } finally {
            serverConnectionDelayWarmupRule.disable();
            safeClose(newClient);
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

            assertThat(serverErrorRule.getHitCount()).isEqualTo(1);
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

    @Test(groups = {"multi-region", "simple"}, timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_HitLimit() throws JsonProcessingException {
        TestItem createdItem = TestItem.createNewItem();
        cosmosAsyncContainer.createItem(createdItem).block();

        // set rule by feed range
        String hitLimitRuleId = "ServerErrorRule-hitLimit-" + UUID.randomUUID();

        FaultInjectionRule hitLimitServerErrorRule =
            new FaultInjectionRuleBuilder(hitLimitRuleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.GONE)
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
            assertThat(hitLimitServerErrorRule.getAddresses().size() == 0);

            for (int i = 1; i <= 3; i++) {
                CosmosDiagnostics cosmosDiagnostics = this.performDocumentOperation(cosmosAsyncContainer, OperationType.Read, createdItem);
                if (i <= 2) {
                    this.validateFaultInjectionRuleApplied(
                        cosmosDiagnostics,
                        OperationType.Read,
                        HttpConstants.StatusCodes.GONE,
                        HttpConstants.SubStatusCodes.UNKNOWN,
                        hitLimitRuleId,
                        true
                    );
                } else {
                    // the fault injection rule will not be applied due to hitLimit
                    cosmosDiagnostics = this.performDocumentOperation(cosmosAsyncContainer, OperationType.Read, createdItem);
                    this.validateNoFaultInjectionApplied(cosmosDiagnostics, OperationType.Read);
                }
            }

            assertThat(hitLimitServerErrorRule.getHitCount()).isEqualTo(2);
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

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_includePrimary() throws JsonProcessingException {
        TestItem createdItem = TestItem.createNewItem();
        CosmosAsyncContainer singlePartitionContainer = getSharedSinglePartitionCosmosContainer(client);
        List<FeedRange> feedRanges = singlePartitionContainer.getFeedRanges().block();

        // Test if includePrimary=true, then primary replica address will always be returned
        String serverGoneIncludePrimaryRuleId = "serverErrorRule-includePrimary-" + UUID.randomUUID();
        FaultInjectionRule serverGoneIncludePrimaryErrorRule =
            new FaultInjectionRuleBuilder(serverGoneIncludePrimaryRuleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .endpoints(
                            new FaultInjectionEndpointBuilder(feedRanges.get(0))
                                .replicaCount(1)
                                .includePrimary(true)
                                .build()
                        )
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.GONE)
                        .times(1)
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        try {
            CosmosFaultInjectionHelper.configureFaultInjectionRules(singlePartitionContainer, Arrays.asList(serverGoneIncludePrimaryErrorRule)).block();

            // test for create item operation, the rule will be applied
            CosmosDiagnostics cosmosDiagnostics = this.performDocumentOperation(singlePartitionContainer, OperationType.Create, createdItem);
            this.validateFaultInjectionRuleApplied(
                cosmosDiagnostics,
                OperationType.Create,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.UNKNOWN,
                serverGoneIncludePrimaryRuleId,
                true);

            // test for upsert item operation, the rule will be applied
            cosmosDiagnostics = this.performDocumentOperation(singlePartitionContainer, OperationType.Upsert, createdItem);
            this.validateFaultInjectionRuleApplied(
                cosmosDiagnostics,
                OperationType.Upsert,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.UNKNOWN,
                serverGoneIncludePrimaryRuleId,
                true);
        } finally {
            serverGoneIncludePrimaryErrorRule.disable();
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
            JsonNode responseStatisticsList = diagnosticNode.get("responseStatisticsList");
            assertThat(responseStatisticsList.isArray()).isTrue();

            if (canRetryOnFaultInjectedError) {
                assertThat(responseStatisticsList.size()).isEqualTo(2);
            } else {
                assertThat(responseStatisticsList.size()).isOne();
            }
            JsonNode storeResult = responseStatisticsList.get(0).get("storeResult");
            assertThat(storeResult).isNotNull();
            assertThat(storeResult.get("statusCode").asInt()).isEqualTo(statusCode);
            assertThat(storeResult.get("subStatusCode").asInt()).isEqualTo(subStatusCode);
            assertThat(storeResult.get("faultInjectionRuleId").asText()).isEqualTo(ruleId);
        }
    }

    private void validateNoFaultInjectionApplied(
        CosmosDiagnostics cosmosDiagnostics,
        OperationType operationType) throws JsonProcessingException {

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
            JsonNode responseStatisticsList = diagnosticNode.get("responseStatisticsList");
            assertThat(responseStatisticsList.isArray()).isTrue();

            for (int i = 0; i < responseStatisticsList.size(); i++) {
                JsonNode storeResult = responseStatisticsList.get(i).get("storeResult");
                assertThat(storeResult.get("faultInjectionRuleId")).isNull();
            }
            assertThat(responseStatisticsList.size()).isOne();
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
}
