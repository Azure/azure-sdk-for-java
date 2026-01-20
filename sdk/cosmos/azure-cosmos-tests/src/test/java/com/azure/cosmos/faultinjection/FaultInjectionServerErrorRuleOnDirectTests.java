// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RequestTimeline;
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
import com.azure.cosmos.test.implementation.interceptor.CosmosInterceptorHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class FaultInjectionServerErrorRuleOnDirectTests extends FaultInjectionTestBase {
    private static final int TIMEOUT = 60000;
    private static final String FAULT_INJECTION_RULE_NON_APPLICABLE_ADDRESS = "Addresses mismatch";
    private static final String FAULT_INJECTION_RULE_NON_APPLICABLE_OPERATION_TYPE = "OperationType mismatch";
    private static final String FAULT_INJECTION_RULE_NON_APPLICABLE_REGION_ENDPOINT = "RegionEndpoint mismatch";
    private static final String FAULT_INJECTION_RULE_NON_APPLICABLE_HIT_LIMIT = "Hit Limit reached";

    private CosmosAsyncClient clientWithoutPreferredRegions;
    private CosmosAsyncContainer cosmosAsyncContainer;

    private DatabaseAccount databaseAccount;
    private List<String> accountLevelReadRegions;
    private List<String> accountLevelWriteRegions;
    private Map<String, String> readRegionMap;
    private Map<String, String> writeRegionMap;
    private List<String> preferredRegions;

    @Factory(dataProvider = "simpleClientBuildersWithJustDirectTcp")
    public FaultInjectionServerErrorRuleOnDirectTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @BeforeClass(groups = {"multi-region", "long", "fast", "fi-multi-master", "multi-region-strong"}, timeOut = TIMEOUT)
    public void beforeClass() {
        clientWithoutPreferredRegions = getClientBuilder()
            .preferredRegions(new ArrayList<>())
            .buildAsyncClient();

        AsyncDocumentClient asyncDocumentClient = BridgeInternal.getContextClient(clientWithoutPreferredRegions);
        GlobalEndpointManager globalEndpointManager = asyncDocumentClient.getGlobalEndpointManager();

        this.databaseAccount = globalEndpointManager.getLatestDatabaseAccount();
        this.cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(clientWithoutPreferredRegions);

        AccountLevelLocationContext accountLevelReadableLocationContext
            = getAccountLevelLocationContext(databaseAccount, false);
        AccountLevelLocationContext accountLevelWriteableLocationContext
            = getAccountLevelLocationContext(databaseAccount, true);

        validate(accountLevelReadableLocationContext, false);
        validate(accountLevelWriteableLocationContext, true);

        this.readRegionMap = accountLevelReadableLocationContext.regionNameToEndpoint;
        this.writeRegionMap = accountLevelWriteableLocationContext.regionNameToEndpoint;
        this.accountLevelReadRegions = accountLevelReadableLocationContext.serviceOrderedReadableRegions;
        this.accountLevelWriteRegions = accountLevelWriteableLocationContext.serviceOrderedWriteableRegions;

        this.preferredRegions = accountLevelReadableLocationContext.serviceOrderedReadableRegions
            .stream()
            .map(regionName -> regionName.toLowerCase(Locale.ROOT))
            .collect(Collectors.toList());
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
            { FaultInjectionOperationType.DELETE_ITEM, true },
            { FaultInjectionOperationType.QUERY_ITEM, false },
            { FaultInjectionOperationType.PATCH_ITEM, true }
        };
    }

    @DataProvider(name = "faultInjectionOperationTypeProviderForLeaseNotFound")
    public static Object[][] faultInjectionOperationTypeProviderForLeaseNotFound() {
        return new Object[][]{
            // fault injection operation type, primaryAddressOnly, isReadMany
            { OperationType.Read, FaultInjectionOperationType.READ_ITEM, false, false },
            { OperationType.Replace, FaultInjectionOperationType.REPLACE_ITEM, true, false },
            { OperationType.Create, FaultInjectionOperationType.CREATE_ITEM, true, false },
            { OperationType.Delete, FaultInjectionOperationType.DELETE_ITEM, true, false },
            { OperationType.Query, FaultInjectionOperationType.QUERY_ITEM, false, false },
            { OperationType.Patch, FaultInjectionOperationType.PATCH_ITEM, true, false },
            { OperationType.Batch, FaultInjectionOperationType.BATCH_ITEM, true, false },
            { OperationType.ReadFeed, FaultInjectionOperationType.READ_FEED_ITEM, false, false },
            { OperationType.Query, FaultInjectionOperationType.QUERY_ITEM, false, true }
        };
    }

    @DataProvider(name = "faultInjectionServerErrorResponseProvider")
    public static Object[][] faultInjectionServerErrorResponseProvider() {
        return new Object[][]{
            // Test retry situation within local region  - there is no preferred regions being configured on the client
            // operationType, faultInjectionOperationType, faultInjectionServerError, will SDK retry within local region or retry cross-region, is multi-region / multi-write check required, errorStatusCode, errorSubStatusCode
            { OperationType.Read, FaultInjectionOperationType.READ_ITEM, FaultInjectionServerErrorType.GONE, true, false, 410, HttpConstants.SubStatusCodes.SERVER_GENERATED_410 },
            { OperationType.Read, FaultInjectionOperationType.READ_ITEM, FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR, false, false, 500, 0 },
            { OperationType.Read, FaultInjectionOperationType.READ_ITEM, FaultInjectionServerErrorType.RETRY_WITH, true, false, 449, 0 },
            { OperationType.Read, FaultInjectionOperationType.READ_ITEM, FaultInjectionServerErrorType.TOO_MANY_REQUEST, true, false, 429, HttpConstants.SubStatusCodes.USER_REQUEST_RATE_TOO_LARGE },
            { OperationType.Read, FaultInjectionOperationType.READ_ITEM, FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE, true, false, 404, 1002 },
            { OperationType.Read, FaultInjectionOperationType.READ_ITEM, FaultInjectionServerErrorType.TIMEOUT, true, false, 410, HttpConstants.SubStatusCodes.SERVER_GENERATED_408 }, // for server return 408, SDK will wrap into 410/21010
            { OperationType.Read, FaultInjectionOperationType.READ_ITEM, FaultInjectionServerErrorType.PARTITION_IS_MIGRATING, true, false, 410, 1008 },
            { OperationType.Read, FaultInjectionOperationType.READ_ITEM, FaultInjectionServerErrorType.PARTITION_IS_SPLITTING, true, false, 410, 1007 },
            { OperationType.Read, FaultInjectionOperationType.READ_ITEM, FaultInjectionServerErrorType.SERVICE_UNAVAILABLE, true, true, 503, 21008 },
            { OperationType.Read, FaultInjectionOperationType.READ_ITEM, FaultInjectionServerErrorType.NAME_CACHE_IS_STALE, true, false, 410, 1000 },
            { OperationType.ReadFeed, FaultInjectionOperationType.READ_FEED_ITEM, FaultInjectionServerErrorType.GONE, true, false, 410, HttpConstants.SubStatusCodes.SERVER_GENERATED_410 },
            { OperationType.ReadFeed, FaultInjectionOperationType.READ_FEED_ITEM, FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR, false, false, 500, 0 },
            { OperationType.ReadFeed, FaultInjectionOperationType.READ_FEED_ITEM, FaultInjectionServerErrorType.RETRY_WITH, true, false, 449, 0 },
            { OperationType.ReadFeed, FaultInjectionOperationType.READ_FEED_ITEM, FaultInjectionServerErrorType.TOO_MANY_REQUEST, true, false, 429, HttpConstants.SubStatusCodes.USER_REQUEST_RATE_TOO_LARGE },
            { OperationType.ReadFeed, FaultInjectionOperationType.READ_FEED_ITEM, FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE, true, false, 404, 1002 },
            { OperationType.ReadFeed, FaultInjectionOperationType.READ_FEED_ITEM, FaultInjectionServerErrorType.TIMEOUT, true, false, 410, HttpConstants.SubStatusCodes.SERVER_GENERATED_408 }, // for server return 408, SDK will wrap into 410/21010
            { OperationType.ReadFeed, FaultInjectionOperationType.READ_FEED_ITEM, FaultInjectionServerErrorType.PARTITION_IS_MIGRATING, true, false, 410, 1008 },
            { OperationType.ReadFeed, FaultInjectionOperationType.READ_FEED_ITEM, FaultInjectionServerErrorType.PARTITION_IS_SPLITTING, true, false, 410, 1007 },
            { OperationType.ReadFeed, FaultInjectionOperationType.READ_FEED_ITEM, FaultInjectionServerErrorType.SERVICE_UNAVAILABLE, true, true, 503, 21008 },
            { OperationType.ReadFeed, FaultInjectionOperationType.READ_FEED_ITEM, FaultInjectionServerErrorType.NAME_CACHE_IS_STALE, true, false, 410, 1000 },
            { OperationType.Create, FaultInjectionOperationType.CREATE_ITEM, FaultInjectionServerErrorType.GONE, true, false, 410, HttpConstants.SubStatusCodes.SERVER_GENERATED_410 },
            { OperationType.Create, FaultInjectionOperationType.CREATE_ITEM, FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR, false, false, 500, 0 },
            { OperationType.Create, FaultInjectionOperationType.CREATE_ITEM, FaultInjectionServerErrorType.RETRY_WITH, true, false, 449, 0 },
            { OperationType.Create, FaultInjectionOperationType.CREATE_ITEM, FaultInjectionServerErrorType.TOO_MANY_REQUEST, true, false, 429, HttpConstants.SubStatusCodes.USER_REQUEST_RATE_TOO_LARGE },
            { OperationType.Create, FaultInjectionOperationType.CREATE_ITEM, FaultInjectionServerErrorType.TIMEOUT, false, false, 410, HttpConstants.SubStatusCodes.SERVER_GENERATED_408 }, // for server return 408, SDK will wrap into 410/21010
            { OperationType.Create, FaultInjectionOperationType.CREATE_ITEM, FaultInjectionServerErrorType.PARTITION_IS_MIGRATING, true, false, 410, 1008 },
            { OperationType.Create, FaultInjectionOperationType.CREATE_ITEM, FaultInjectionServerErrorType.PARTITION_IS_SPLITTING, true, false, 410, 1007 },
            { OperationType.Create, FaultInjectionOperationType.CREATE_ITEM, FaultInjectionServerErrorType.SERVICE_UNAVAILABLE, true, true, 503, 21008 },
            { OperationType.Create, FaultInjectionOperationType.CREATE_ITEM, FaultInjectionServerErrorType.NAME_CACHE_IS_STALE, true, false, 410, 1000 }
        };
    }

    @DataProvider(name = "preferredRegionsConfigProvider")
    public static Object[] preferredRegionsConfigProvider() {
        // shouldInjectPreferredRegionsOnClient
        return new Object[] {false, true};
    }

    @DataProvider(name = "barrierRequestServerErrorResponseProvider")
    public static Object[][] barrierRequestServerErrorResponseProvider() {
        // OperationType, FaultInjectionErrorType, ErrorStatusCode, ErrorSubStatusCode
        return new Object[][] {
            // only include exceptions which can be applied by operation type
            { OperationType.Create, FaultInjectionServerErrorType.LEASE_NOT_FOUND, HttpConstants.StatusCodes.GONE, HttpConstants.SubStatusCodes.LEASE_NOT_FOUND },
            { OperationType.Create, FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR, HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, HttpConstants.SubStatusCodes.UNKNOWN },
            { OperationType.Create, FaultInjectionServerErrorType.RETRY_WITH, HttpConstants.StatusCodes.RETRY_WITH, HttpConstants.SubStatusCodes.UNKNOWN },
            { OperationType.Create, FaultInjectionServerErrorType.TOO_MANY_REQUEST, HttpConstants.StatusCodes.TOO_MANY_REQUESTS, HttpConstants.SubStatusCodes.USER_REQUEST_RATE_TOO_LARGE },
            { OperationType.Create, FaultInjectionServerErrorType.TIMEOUT, HttpConstants.StatusCodes.GONE, HttpConstants.SubStatusCodes.SERVER_GENERATED_408 },
            { OperationType.Create, FaultInjectionServerErrorType.PARTITION_IS_MIGRATING, HttpConstants.StatusCodes.GONE, HttpConstants.SubStatusCodes.COMPLETING_PARTITION_MIGRATION },
            { OperationType.Create, FaultInjectionServerErrorType.PARTITION_IS_SPLITTING, HttpConstants.StatusCodes.GONE, HttpConstants.SubStatusCodes.COMPLETING_SPLIT_OR_MERGE },
            { OperationType.Create, FaultInjectionServerErrorType.SERVICE_UNAVAILABLE, HttpConstants.StatusCodes.SERVICE_UNAVAILABLE, HttpConstants.SubStatusCodes.SERVER_GENERATED_503 },
            { OperationType.Create, FaultInjectionServerErrorType.NAME_CACHE_IS_STALE, HttpConstants.StatusCodes.GONE, HttpConstants.SubStatusCodes.NAME_CACHE_IS_STALE },
            { OperationType.Read, FaultInjectionServerErrorType.LEASE_NOT_FOUND, HttpConstants.StatusCodes.GONE, HttpConstants.SubStatusCodes.LEASE_NOT_FOUND },
            { OperationType.Read, FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR, HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, HttpConstants.SubStatusCodes.UNKNOWN },
            { OperationType.Read, FaultInjectionServerErrorType.RETRY_WITH, HttpConstants.StatusCodes.RETRY_WITH, HttpConstants.SubStatusCodes.UNKNOWN },
            { OperationType.Read, FaultInjectionServerErrorType.TOO_MANY_REQUEST, HttpConstants.StatusCodes.TOO_MANY_REQUESTS, HttpConstants.SubStatusCodes.USER_REQUEST_RATE_TOO_LARGE },
            { OperationType.Read, FaultInjectionServerErrorType.TIMEOUT, HttpConstants.StatusCodes.GONE, HttpConstants.SubStatusCodes.SERVER_GENERATED_408 },
            { OperationType.Read, FaultInjectionServerErrorType.PARTITION_IS_MIGRATING, HttpConstants.StatusCodes.GONE, HttpConstants.SubStatusCodes.COMPLETING_PARTITION_MIGRATION },
            { OperationType.Read, FaultInjectionServerErrorType.PARTITION_IS_SPLITTING, HttpConstants.StatusCodes.GONE, HttpConstants.SubStatusCodes.COMPLETING_SPLIT_OR_MERGE },
            { OperationType.Read, FaultInjectionServerErrorType.SERVICE_UNAVAILABLE, HttpConstants.StatusCodes.SERVICE_UNAVAILABLE, HttpConstants.SubStatusCodes.SERVER_GENERATED_503 },
            { OperationType.Read, FaultInjectionServerErrorType.NAME_CACHE_IS_STALE, HttpConstants.StatusCodes.GONE, HttpConstants.SubStatusCodes.NAME_CACHE_IS_STALE },
            { OperationType.Query, FaultInjectionServerErrorType.LEASE_NOT_FOUND, HttpConstants.StatusCodes.GONE, HttpConstants.SubStatusCodes.LEASE_NOT_FOUND },
            { OperationType.Query, FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR, HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, HttpConstants.SubStatusCodes.UNKNOWN },
            { OperationType.Query, FaultInjectionServerErrorType.RETRY_WITH, HttpConstants.StatusCodes.RETRY_WITH, HttpConstants.SubStatusCodes.UNKNOWN },
            { OperationType.Query, FaultInjectionServerErrorType.TOO_MANY_REQUEST, HttpConstants.StatusCodes.TOO_MANY_REQUESTS, HttpConstants.SubStatusCodes.USER_REQUEST_RATE_TOO_LARGE },
            { OperationType.Query, FaultInjectionServerErrorType.TIMEOUT, HttpConstants.StatusCodes.GONE, HttpConstants.SubStatusCodes.SERVER_GENERATED_408 },
            { OperationType.Query, FaultInjectionServerErrorType.PARTITION_IS_MIGRATING, HttpConstants.StatusCodes.GONE, HttpConstants.SubStatusCodes.COMPLETING_PARTITION_MIGRATION },
            { OperationType.Query, FaultInjectionServerErrorType.PARTITION_IS_SPLITTING, HttpConstants.StatusCodes.GONE, HttpConstants.SubStatusCodes.COMPLETING_SPLIT_OR_MERGE },
            { OperationType.Query, FaultInjectionServerErrorType.SERVICE_UNAVAILABLE, HttpConstants.StatusCodes.SERVICE_UNAVAILABLE, HttpConstants.SubStatusCodes.SERVER_GENERATED_503 },
            { OperationType.Query, FaultInjectionServerErrorType.NAME_CACHE_IS_STALE, HttpConstants.StatusCodes.GONE, HttpConstants.SubStatusCodes.NAME_CACHE_IS_STALE }
        };
    }

    @Test(groups = {"multi-region", "long"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
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
            TestObject createdItem = TestObject.create();
            cosmosAsyncContainer.createItem(createdItem).block();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(cosmosAsyncContainer, Arrays.asList(serverGoneErrorRule)).block();

            assertThat(serverGoneErrorRule.getAddresses().size()).isZero();
            assertThat(serverGoneErrorRule.getRegionEndpoints().size() == this.readRegionMap.size() + 1
               && serverGoneErrorRule.getRegionEndpoints().containsAll(this.readRegionMap.values()));

            CosmosDiagnostics cosmosDiagnostics = this.performDocumentOperation(cosmosAsyncContainer, operationType, createdItem, false);
            this.validateFaultInjectionRuleApplied(
                cosmosDiagnostics,
                operationType,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                serverGoneRuleId,
                true);

            // Test for Server error TOO_MANY_REQUESTS, the rules will be applied by operation type
            serverGoneErrorRule.disable();
            CosmosFaultInjectionHelper.configureFaultInjectionRules(cosmosAsyncContainer, Arrays.asList(serverTooManyRequestsErrorRule)).block();
            assertThat(serverGoneErrorRule.getAddresses().size()).isZero();
            assertThat(serverGoneErrorRule.getRegionEndpoints().size() == this.readRegionMap.size() + 1
                && serverGoneErrorRule.getRegionEndpoints().containsAll(this.readRegionMap.values()));

            cosmosDiagnostics = this.performDocumentOperation(cosmosAsyncContainer, operationType, createdItem, false);
            if (operationType == OperationType.Read) {
                this.validateHitCount(serverTooManyRequestsErrorRule, 1, OperationType.Read, ResourceType.Document);
                this.validateFaultInjectionRuleApplied(
                    cosmosDiagnostics,
                    operationType,
                    HttpConstants.StatusCodes.TOO_MANY_REQUESTS,
                    HttpConstants.SubStatusCodes.USER_REQUEST_RATE_TOO_LARGE,
                    tooManyRequestsRuleId,
                    true);
            } else {
                this.validateNoFaultInjectionApplied(cosmosDiagnostics, operationType, FAULT_INJECTION_RULE_NON_APPLICABLE_OPERATION_TYPE);
            }

        } finally {
            serverGoneErrorRule.disable();
            serverTooManyRequestsErrorRule.disable();
        }
    }

    @Test(groups = {"multi-region"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_OperationTypeImpactAddresses(OperationType operationType) throws JsonProcessingException {
        // Test the operation type can impact which region or replica the rule will be applicable
        TestObject createdItem = TestObject.create();
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

        logger.info(
            "Inside fault injection test, OperationType {}, write region {}, read region",
            operationType,
            this.writeRegionMap.values(),
            this.readRegionMap.values());
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

            // when the fault injection is not configured with any region, internally, the default endpoint will also be included
            // So the size will be writeRegionCount + 1
            assertThat(writeRegionServerGoneErrorRule.getRegionEndpoints().size()).isEqualTo(this.writeRegionMap.size() + 1);

            CosmosDiagnostics cosmosDiagnostics = this.performDocumentOperation(container, operationType, createdItem, false);
            if (operationType.isWriteOperation()) {
                this.validateHitCount(writeRegionServerGoneErrorRule, 1, operationType, ResourceType.Document);
                this.validateFaultInjectionRuleApplied(
                    cosmosDiagnostics,
                    operationType,
                    HttpConstants.StatusCodes.GONE,
                    HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                    writeRegionServerGoneRuleId,
                    true);
            } else {
                this.validateNoFaultInjectionApplied(cosmosDiagnostics, operationType, FAULT_INJECTION_RULE_NON_APPLICABLE_ADDRESS);
            }

            writeRegionServerGoneErrorRule.disable();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(primaryReplicaServerGoneErrorRule)).block();
            assertThat(primaryReplicaServerGoneErrorRule.getRegionEndpoints().size()).isEqualTo(this.writeRegionMap.size() + 1);
            assertThat(primaryReplicaServerGoneErrorRule.getRegionEndpoints().containsAll(this.writeRegionMap.values())).isTrue();
            assertThat(primaryReplicaServerGoneErrorRule.getAddresses().size()).isEqualTo(this.writeRegionMap.size());
        } finally {
            writeRegionServerGoneErrorRule.disable();
            primaryReplicaServerGoneErrorRule.disable();
        }
    }

    @Test(groups = {"multi-region"}, dataProvider = "preferredRegionsConfigProvider", timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_Region(boolean shouldInjectPreferredRegionsOnClient) throws JsonProcessingException {
        List<String> preferredLocations = this.accountLevelReadRegions;

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
                .consistencyLevel(BridgeInternal.getContextClient(this.clientWithoutPreferredRegions).getConsistencyLevel())
                .preferredRegions(shouldInjectPreferredRegionsOnClient ? preferredLocations : Collections.emptyList())
                .directMode()
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

            assertThat(
                serverErrorRuleLocalRegion.getRegionEndpoints().size() == 1
                && serverErrorRuleLocalRegion.getRegionEndpoints().get(0).equals(this.readRegionMap.get(preferredLocations.get(0))));
            assertThat(
                serverErrorRuleRemoteRegion.getRegionEndpoints().size() == 1
                    && serverErrorRuleRemoteRegion.getRegionEndpoints().get(0).equals(this.readRegionMap.get(preferredLocations.get(1))));

            // Validate fault injection applied in the local region
            CosmosDiagnostics cosmosDiagnostics = this.performDocumentOperation(container, OperationType.Read, createdItem, false);

            this.validateHitCount(serverErrorRuleLocalRegion, 1, OperationType.Read, ResourceType.Document);
            this.validateHitCount(serverErrorRuleRemoteRegion, 0, OperationType.Read, ResourceType.Document);

            this.validateFaultInjectionRuleApplied(
                cosmosDiagnostics,
                OperationType.Read,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                localRegionRuleId,
                true
            );

            serverErrorRuleLocalRegion.disable();

            cosmosDiagnostics = this.performDocumentOperation(container, OperationType.Read, createdItem, false);
            this.validateNoFaultInjectionApplied(cosmosDiagnostics, OperationType.Read, FAULT_INJECTION_RULE_NON_APPLICABLE_REGION_ENDPOINT);
        } finally {
            serverErrorRuleLocalRegion.disable();
            serverErrorRuleRemoteRegion.disable();
            safeClose(clientWithPreferredRegion);
        }
    }

    @Test(groups = {"multi-region", "long"}, timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_Partition() throws JsonProcessingException {
        for (int i = 0; i < 10; i++) {
            cosmosAsyncContainer.createItem(TestObject.create()).block();
        }

        // getting one item from each feedRange
        List<FeedRange> feedRanges = cosmosAsyncContainer.getFeedRanges().block();
        assertThat(feedRanges.size()).isGreaterThan(1);

        String query = "select * from c";
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        cosmosQueryRequestOptions.setFeedRange(feedRanges.get(0));
        TestObject itemOnFeedRange0 = cosmosAsyncContainer.queryItems(query, cosmosQueryRequestOptions, TestObject.class).blockFirst();

        cosmosQueryRequestOptions.setFeedRange(feedRanges.get(1));
        TestObject itemOnFeedRange1 = cosmosAsyncContainer.queryItems(query, cosmosQueryRequestOptions, TestObject.class).blockFirst();

        // set rule by feed range
        String feedRangeRuleId = "ServerErrorRule-FeedRange-" + UUID.randomUUID();

        FaultInjectionRule serverErrorRuleByFeedRange =
            new FaultInjectionRuleBuilder(feedRangeRuleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .endpoints(
                            new FaultInjectionEndpointBuilder(feedRanges.get(0))
                                .build()) // by default setting on all replicas
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.TOO_MANY_REQUEST) // using a server error which will be applied on the full replica path
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
            HttpConstants.SubStatusCodes.USER_REQUEST_RATE_TOO_LARGE,
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

    @Test(groups = {"multi-region", "long"}, timeOut = TIMEOUT)
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
                .consistencyLevel(BridgeInternal.getContextClient(this.clientWithoutPreferredRegions).getConsistencyLevel())
                .directMode(directConnectionConfig)
                .buildAsyncClient();

            CosmosAsyncContainer container =
                newClient
                    .getDatabase(cosmosAsyncContainer.getDatabase().getId())
                    .getContainer(cosmosAsyncContainer.getId());

            // create a new item to be used by read operations
            TestObject createdItem = TestObject.create();
            container.createItem(createdItem).block();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(timeoutRule)).block();
            CosmosItemResponse<TestObject> itemResponse =
                container.readItem(createdItem.getId(), new PartitionKey(createdItem.getId()), TestObject.class).block();

            assertThat(timeoutRule.getHitCount()).isEqualTo(1);
            this.validateHitCount(timeoutRule, 1, OperationType.Read, ResourceType.Document);

            this.validateFaultInjectionRuleApplied(
                itemResponse.getDiagnostics(),
                OperationType.Read,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.TRANSPORT_GENERATED_410,
                timeoutRuleId,
                true
            );

        } finally {
            timeoutRule.disable();
            safeClose(newClient);
        }
    }

    @Test(groups = {"multi-region", "long"}, timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_ServerConnectionTimeout() throws JsonProcessingException {
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
                .consistencyLevel(BridgeInternal.getContextClient(this.clientWithoutPreferredRegions).getConsistencyLevel())
                .directMode(directConnectionConfig)
                .buildAsyncClient();

            CosmosAsyncContainer container =
                newClient
                    .getDatabase(cosmosAsyncContainer.getDatabase().getId())
                    .getContainer(cosmosAsyncContainer.getId());

            CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(serverConnectionDelayRule)).block();
            CosmosItemResponse<TestObject> itemResponse = container.createItem(TestObject.create()).block();

            // Due to the replica validation, there could be an extra open connection call flow, while the rule will also be applied on.
            assertThat(serverConnectionDelayRule.getHitCount()).isBetween(1l, 2l);
            this.validateFaultInjectionRuleApplied(
                itemResponse.getDiagnostics(),
                OperationType.Create,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.TRANSPORT_GENERATED_410,
                ruleId,
                true
            );

        } finally {
            serverConnectionDelayRule.disable();
            safeClose(newClient);
        }
    }

    @Test(groups = {"multi-region", "long"}, timeOut = TIMEOUT)
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
                        .delay(Duration.ofMillis(100))
                        .times(1)
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        try {
            newClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .consistencyLevel(BridgeInternal.getContextClient(this.clientWithoutPreferredRegions).getConsistencyLevel())
                .buildAsyncClient();

            CosmosAsyncContainer container =
                newClient
                    .getDatabase(cosmosAsyncContainer.getDatabase().getId())
                    .getContainer(cosmosAsyncContainer.getId());

            CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(serverConnectionDelayRule)).block();
            CosmosDiagnostics cosmosDiagnostics = container.createItem(TestObject.create()).block().getDiagnostics();

            // verify the request succeeded and the rule has applied
            List<ObjectNode> diagnosticsNode = new ArrayList<>();
            diagnosticsNode.add((ObjectNode) Utils.getSimpleObjectMapper().readTree(cosmosDiagnostics.toString()));
            assertThat(diagnosticsNode.size()).isEqualTo(1);
            JsonNode responseStatisticsList = diagnosticsNode.get(0).get("responseStatisticsList");
            assertThat(responseStatisticsList.isArray()).isTrue();
            assertThat(responseStatisticsList.size()).isEqualTo(1);
            JsonNode storeResult = responseStatisticsList.get(0).get("storeResult");
            assertThat(storeResult.get("faultInjectionRuleId").asText()).isEqualTo(ruleId);
            assertThat(storeResult.get("statusCode").asInt()).isEqualTo(201);
        } finally {
            serverConnectionDelayRule.disable();
            safeClose(newClient);
        }
    }

    @Test(groups = {"multi-region", "long"}, dataProvider = "faultInjectionOperationTypeProvider", timeOut = TIMEOUT)
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
                .consistencyLevel(BridgeInternal.getContextClient(this.clientWithoutPreferredRegions).getConsistencyLevel())
                .directMode(directConnectionConfig)
                .buildAsyncClient();

            CosmosAsyncContainer container =
                newClient
                    .getDatabase(cosmosAsyncContainer.getDatabase().getId())
                    .getContainer(cosmosAsyncContainer.getId());

            logger.info("serverConnectionDelayWarmupRule: get all the addresses");
            List<FeedRange> feedRanges = container.getFeedRanges().block();
            for (FeedRange feedRange : feedRanges) {
                String feedRangeRuleId = "serverErrorRule-test-feedRang" + feedRange.toString();
                FaultInjectionRule feedRangeRule =
                    new FaultInjectionRuleBuilder(feedRangeRuleId)
                        .condition(
                            new FaultInjectionConditionBuilder()
                                .endpoints(
                                    new FaultInjectionEndpointBuilder(feedRange).build()
                                )
                                .build()
                        )
                        .result(
                            FaultInjectionResultBuilders
                                .getResultBuilder(FaultInjectionServerErrorType.TOO_MANY_REQUEST)
                                .build()
                        )
                        .duration(Duration.ofMinutes(1))
                        .build();

                CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(feedRangeRule)).block();
                logger.info("serverConnectionDelayWarmupRul. FeedRange {}, Addresses {}", feedRange, feedRangeRule.getAddresses());
                feedRangeRule.disable();
            }

            CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(serverConnectionDelayWarmupRule)).block();

            int partitionSize = container.getFeedRanges().block().size();
            container.openConnectionsAndInitCaches().block();

            if (primaryAddressesOnly) {
                logger.info(
                    "serverConnectionDelayWarmupRule. PartitionSize {}, hitCount{}, hitDetails {}",
                    partitionSize,
                    serverConnectionDelayWarmupRule.getHitCount(),
                    serverConnectionDelayWarmupRule.getHitCountDetails());

                // proactive connection management will try to establish one connection per primary
                // and retry failed connection attempts at most twice per primary
                int primaryAddressCount = partitionSize;
                int maxConnectionRetriesPerPrimary = primaryAddressCount * 2;

                assertThat(serverConnectionDelayWarmupRule.getHitCount()).isLessThanOrEqualTo(primaryAddressCount + maxConnectionRetriesPerPrimary);

                this.validateHitCount(
                        serverConnectionDelayWarmupRule,
                        serverConnectionDelayWarmupRule.getHitCount(),
                        OperationType.Create,
                        ResourceType.Connection);
            } else {

                // proactive connection management will try to establish one connection per replica
                // and retry failed connection attempts at most twice per replica
                long minSecondaryAddressesCount = 3L * partitionSize;
                long maxAddressesCount = 5L * partitionSize;
                long minTotalConnectionEstablishmentAttempts = minSecondaryAddressesCount + 2 * minSecondaryAddressesCount;
                long maxTotalConnectionEstablishmentAttempts = maxAddressesCount + 2 * maxAddressesCount;

                assertThat(serverConnectionDelayWarmupRule.getHitCount()).isBetween(minTotalConnectionEstablishmentAttempts, maxTotalConnectionEstablishmentAttempts);

                this.validateHitCount(
                    serverConnectionDelayWarmupRule,
                    serverConnectionDelayWarmupRule.getHitCount(),
                    OperationType.Create,
                    ResourceType.Connection);
            }
        } finally {
            serverConnectionDelayWarmupRule.disable();
            safeClose(newClient);
        }
    }

    @Test(groups = {"multi-region", "long"}, dataProvider = "faultInjectionServerErrorResponseProvider", timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_ServerErrorResponse(
        OperationType operationType,
        FaultInjectionOperationType faultInjectionOperationType,
        FaultInjectionServerErrorType serverErrorType,
        boolean canRetry,
        boolean isMultiRegionCheckRequired,
        int errorStatusCode,
        int errorSubStatusCode) throws JsonProcessingException {

        if (isMultiRegionCheckRequired) {
            if (isOperationAWriteOperation(operationType) && this.accountLevelWriteRegions.size() == 1) {
                canRetry = false;
            } else if (!isOperationAWriteOperation(operationType) && this.accountLevelReadRegions.size() == 1) {
                canRetry = false;
            }
        }

        // simulate high channel acquisition/connectionTimeout for read/query
        TestObject createdItem = TestObject.create();
        cosmosAsyncContainer.createItem(createdItem).block();

        String ruleId = "serverErrorRule-" + serverErrorType + "-" + UUID.randomUUID();
        FaultInjectionRule serverErrorRule =
            new FaultInjectionRuleBuilder(ruleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(faultInjectionOperationType)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(serverErrorType)
                        .times(1)
                        .build()
                )
                .hitLimit(1) // for read feed staled resource exception, need to also configure this to limit the total injection count
                .duration(Duration.ofMinutes(5))
                .build();

        try {
            CosmosFaultInjectionHelper.configureFaultInjectionRules(cosmosAsyncContainer, Arrays.asList(serverErrorRule)).block();

            CosmosDiagnostics cosmosDiagnostics = performDocumentOperation(cosmosAsyncContainer, operationType, createdItem, false);
            this.validateHitCount(serverErrorRule, 1, operationType, ResourceType.Document);
            this.validateFaultInjectionRuleApplied(
                cosmosDiagnostics,
                operationType,
                errorStatusCode,
                errorSubStatusCode,
                ruleId,
                canRetry
            );

        } finally {
            serverErrorRule.disable();
        }

    }

    @Test(groups = { "fast", "fi-multi-master", "multi-region" }, dataProvider = "faultInjectionOperationTypeProviderForLeaseNotFound", timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_LeaseNotFound(OperationType operationType, FaultInjectionOperationType faultInjectionOperationType, boolean primaryAddressOnly, boolean isReadMany) throws JsonProcessingException {

        boolean shouldRetryCrossRegion = false;

        if (Utils.isWriteOperation(operationType) && this.accountLevelWriteRegions.size() > 1) {
            shouldRetryCrossRegion = true;
        } else if (!Utils.isWriteOperation(operationType) && this.accountLevelReadRegions.size() > 1) {
            shouldRetryCrossRegion = true;
        }

        TestObject createdItem = TestObject.create();

        String ruleId = "serverErrorRule-" + FaultInjectionServerErrorType.LEASE_NOT_FOUND + "-" + UUID.randomUUID();
        FaultInjectionRule serverErrorRule =
            new FaultInjectionRuleBuilder(ruleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(faultInjectionOperationType)
                        .connectionType(FaultInjectionConnectionType.DIRECT)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.LEASE_NOT_FOUND)
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .hitLimit(1)
                .build();

        try {

            this.cosmosAsyncContainer.createItem(createdItem).block();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(this.cosmosAsyncContainer, Arrays.asList(serverErrorRule)).block();
            CosmosDiagnostics cosmosDiagnostics = performDocumentOperation(this.cosmosAsyncContainer, operationType, createdItem, isReadMany);
            logger.warn("Preferred regions : {}", this.preferredRegions.stream().collect(Collectors.joining(", ")));
            logger.warn("Injected error details : {}", serverErrorRule.toString());
            logger.warn("Op Type : {} ; Diagnostics : {}", operationType, cosmosDiagnostics.getDiagnosticsContext().toJson());
            this.validateHitCount(serverErrorRule, 1, operationType, ResourceType.Document);
            this.validateFaultInjectionRuleApplied(
                cosmosDiagnostics,
                operationType,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.LEASE_NOT_FOUND,
                ruleId,
                shouldRetryCrossRegion
            );
            this.validateAddressRefreshWithForceRefresh(cosmosDiagnostics, (operationType == OperationType.ReadFeed || operationType == OperationType.Query));

        } finally {
            serverErrorRule.disable();
        }
    }

    @Test(groups = {"multi-region", "long"}, timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_HitLimit() throws JsonProcessingException {
        TestObject createdItem = TestObject.create();
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
                CosmosDiagnostics cosmosDiagnostics = this.performDocumentOperation(cosmosAsyncContainer, OperationType.Read, createdItem, false);
                if (i <= 2) {
                    this.validateFaultInjectionRuleApplied(
                        cosmosDiagnostics,
                        OperationType.Read,
                        HttpConstants.StatusCodes.GONE,
                        HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                        hitLimitRuleId,
                        true
                    );
                } else {
                    // the fault injection rule will not be applied due to hitLimit
                    cosmosDiagnostics = this.performDocumentOperation(cosmosAsyncContainer, OperationType.Read, createdItem, false);
                    this.validateNoFaultInjectionApplied(cosmosDiagnostics, OperationType.Read, FAULT_INJECTION_RULE_NON_APPLICABLE_HIT_LIMIT);
                }
            }

            this.validateHitCount(hitLimitServerErrorRule, 2, OperationType.Read, ResourceType.Document);
        } finally {
            hitLimitServerErrorRule.disable();
        }
    }

    @AfterClass(groups = {"multi-region", "long", "fast", "fi-multi-master", "multi-region-strong"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(clientWithoutPreferredRegions);
    }

    @Test(groups = {"long"}, timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_includePrimary() throws JsonProcessingException {
        TestObject createdItem = TestObject.create();
        CosmosAsyncContainer singlePartitionContainer = getSharedSinglePartitionCosmosContainer(clientWithoutPreferredRegions);
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
            CosmosDiagnostics cosmosDiagnostics = this.performDocumentOperation(singlePartitionContainer, OperationType.Create, createdItem, false);
            this.validateFaultInjectionRuleApplied(
                cosmosDiagnostics,
                OperationType.Create,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                serverGoneIncludePrimaryRuleId,
                true);

            // test for upsert item operation, the rule will be applied
            cosmosDiagnostics = this.performDocumentOperation(singlePartitionContainer, OperationType.Upsert, createdItem, false);
            this.validateFaultInjectionRuleApplied(
                cosmosDiagnostics,
                OperationType.Upsert,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                serverGoneIncludePrimaryRuleId,
                true);
        } finally {
            serverGoneIncludePrimaryErrorRule.disable();
        }
    }

    @Test(groups = {"long"}, timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_StaledAddressesServerGone() throws JsonProcessingException {
        // Test gone error being injected and forceRefresh address refresh happens
        String staledAddressesServerGoneRuleId = "staledAddressesServerGone-" + UUID.randomUUID();
        FaultInjectionRule staledAddressesServerGoneRule =
            new FaultInjectionRuleBuilder(staledAddressesServerGoneRuleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(FaultInjectionOperationType.CREATE_ITEM)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.STALED_ADDRESSES_SERVER_GONE)
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        try {
            TestObject testItem = this.cosmosAsyncContainer.createItem(TestObject.create()).block().getItem();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(this.cosmosAsyncContainer, Arrays.asList(staledAddressesServerGoneRule)).block();

            // test for create item operation, the rule will be applied
            CosmosDiagnostics cosmosDiagnostics = this.performDocumentOperation(this.cosmosAsyncContainer, OperationType.Create, testItem, false);
            this.validateFaultInjectionRuleApplied(
                cosmosDiagnostics,
                OperationType.Create,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                staledAddressesServerGoneRuleId,
                true);

            this.validateAddressRefreshWithForceRefresh(cosmosDiagnostics, false);
        } finally {
            staledAddressesServerGoneRule.disable();
        }
    }

    @Test(groups = {"long"}, timeOut = TIMEOUT)
    public void connectionAcquisitionTimeoutAlignConnectionTimeout() throws JsonProcessingException {
        // validate the acquisitionTimeout will be <= 2 * connectionTimeout
        String connectionDelayRuleId = "connectionDelay-" + UUID.randomUUID();

        FaultInjectionRule connectionDelayRule =
            new FaultInjectionRuleBuilder(connectionDelayRuleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(FaultInjectionOperationType.CREATE_ITEM)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.CONNECTION_DELAY)
                        .delay(Duration.ofSeconds(3))
                        .times(1) // for each operation, only apply the rule one time
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .hitLimit(3)
                .build();

        CosmosAsyncClient client = null;

        try {
            DirectConnectionConfig directConnectionConfig = DirectConnectionConfig.getDefaultConfig();
            directConnectionConfig.setConnectTimeout(Duration.ofSeconds(2));

            client =
                this.getClientBuilder().directMode(directConnectionConfig).buildAsyncClient();
            CosmosAsyncContainer containerWithSinglePartition = getSharedSinglePartitionCosmosContainer(client);

            CosmosFaultInjectionHelper
                .configureFaultInjectionRules(
                    containerWithSinglePartition,
                    Arrays.asList(connectionDelayRule))
                .block();

            // Creating 6 items concurrently, few of them will enter the pendingAcquisition queue
            // validate none of the channelAcquisition stage take more than 2s
            List<CosmosDiagnostics> results = new ArrayList<>();
            Flux.range(1, 6)
                .flatMap(t -> containerWithSinglePartition.createItem(TestObject.create()))
                .doOnNext(response -> results.add(response.getDiagnostics()))
                .blockLast();

            // assert the rule is applied once for each request
            for (CosmosDiagnostics cosmosDiagnostics : results) {
                this.validateTransportTimelineLatency(
                    RequestTimeline.EventName.CHANNEL_ACQUISITION_STARTED,
                    2 * 2000,
                    cosmosDiagnostics);
            }
        } finally {
            connectionDelayRule.disable();
            safeClose(client);
        }
    }

    @Test(groups = {"long"}, timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_InjectionRate50Percent() throws JsonProcessingException {
        String applyPercentageRuleId = "applyPercentage-"+UUID.randomUUID();

        FaultInjectionRule applyPercentageRule =
            new FaultInjectionRuleBuilder(applyPercentageRuleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(FaultInjectionOperationType.READ_ITEM)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.GONE)
                        .injectionRate(.5)
                        .times(1)//for each operation, only apply the rule one time
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        try {
            TestObject createdItem = TestObject.create();
            cosmosAsyncContainer.createItem(createdItem).block();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(
                cosmosAsyncContainer,
                Arrays.asList(applyPercentageRule)).block();

            for(int i = 0; i < 100; i++){
                this.performDocumentOperation(cosmosAsyncContainer, OperationType.Read, createdItem, false);
            }

            //Because applyPercentage is based on Random probability,
            //we expect that this assert will fail 0.66% of the time.
            assertThat(applyPercentageRule.getHitCount()).isBetween(37L, 63L);

        } finally {
            applyPercentageRule.disable();
        }
    }

    @Test(groups = {"long"}, timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_InjectionRate100Percent() throws JsonProcessingException {
        String applyPercentageRuleId = "applyPercentage-"+UUID.randomUUID();

        FaultInjectionRule applyPercentageRule =
            new FaultInjectionRuleBuilder(applyPercentageRuleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(FaultInjectionOperationType.READ_ITEM)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.GONE)
                        .injectionRate(1)
                        .times(1)//for each operation, only apply the rule one time
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        try {
            TestObject createdItem = TestObject.create();
            cosmosAsyncContainer.createItem(createdItem).block();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(
                cosmosAsyncContainer,
                Arrays.asList(applyPercentageRule)).block();

            for(int i = 0; i < 100; i++){
                this.performDocumentOperation(cosmosAsyncContainer, OperationType.Read, createdItem, false);
            }

            assertThat(applyPercentageRule.getHitCount()).isEqualTo(100L);

        } finally {
            applyPercentageRule.disable();
        }
    }

    @Test(groups = {"long"}, timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_InjectionRate0Percent() throws JsonProcessingException {
        String applyPercentageRuleId = "applyPercentage-"+UUID.randomUUID();

        try {
            FaultInjectionRule applyPercentageRule =
                new FaultInjectionRuleBuilder(applyPercentageRuleId)
                    .condition(
                        new FaultInjectionConditionBuilder()
                            .operationType(FaultInjectionOperationType.READ_ITEM)
                            .build()
                    )
                    .result(
                        FaultInjectionResultBuilders
                            .getResultBuilder(FaultInjectionServerErrorType.GONE)
                            .injectionRate(0)
                            .times(1)//for each operation, only apply the rule one time
                            .build()
                    )
                    .duration(Duration.ofMinutes(5))
                    .build();

        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage().contains("Argument 'injectionRate' should be between (0, 1]"));
        }
    }

    @Test(groups = {"long"}, timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_InjectionRate25Percent() throws JsonProcessingException {
        String applyPercentageRuleId = "applyPercentage-" + UUID.randomUUID();

        FaultInjectionRule applyPercentageRule =
            new FaultInjectionRuleBuilder(applyPercentageRuleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(FaultInjectionOperationType.READ_ITEM)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.GONE)
                        .injectionRate(.25)
                        .times(1)//for each operation, only apply the rule one time
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        try {
            TestObject createdItem = TestObject.create();
            cosmosAsyncContainer.createItem(createdItem).block();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(
                cosmosAsyncContainer,
                Arrays.asList(applyPercentageRule)).block();

            for (int i = 0; i < 100; i++) {
                this.performDocumentOperation(cosmosAsyncContainer, OperationType.Read, createdItem, false);
            }

            //Because applyPercentage is based on Random probability,
            //we expect that this assert will fail 0.53% of the time.
            assertThat(applyPercentageRule.getHitCount()).isBetween(14L, 37L);

        } finally {
            applyPercentageRule.disable();
        }
    }

    @Test(groups = {"long"}, timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_InjectionRate75Percent() throws JsonProcessingException {
        String applyPercentageRuleId = "applyPercentage-" + UUID.randomUUID();

        FaultInjectionRule applyPercentageRule =
            new FaultInjectionRuleBuilder(applyPercentageRuleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(FaultInjectionOperationType.READ_ITEM)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.GONE)
                        .injectionRate(.75)
                        .times(1)//for each operation, only apply the rule one time
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        try {
            TestObject createdItem = TestObject.create();
            cosmosAsyncContainer.createItem(createdItem).block();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(
                cosmosAsyncContainer,
                Arrays.asList(applyPercentageRule)).block();

            for (int i = 0; i < 100; i++) {
                this.performDocumentOperation(cosmosAsyncContainer, OperationType.Read, createdItem, false);
            }

            //Because applyPercentage is based on Random probability,
            //we expect that this assert will fail 0.53% of the time.
            assertThat(applyPercentageRule.getHitCount()).isBetween(63L, 86L);

        } finally {
            applyPercentageRule.disable();
        }
    }

    @Test(groups = {"long"}, timeOut = TIMEOUT)
    public void faultInjectionInjectTcpResponseDelay() throws JsonProcessingException {
        CosmosAsyncClient newClient = null; // creating new client to force creating new connections
        // define another rule which can simulate timeout
        String timeoutRuleId = "serverErrorRule-transitTimeout-" + UUID.randomUUID();
        FaultInjectionRule timeoutRule =
            new FaultInjectionRuleBuilder(timeoutRuleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(FaultInjectionOperationType.CREATE_ITEM)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
                        .times(1)
                        .delay(Duration.ofSeconds(4))
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();
        try {
            DirectConnectionConfig directConnectionConfig = DirectConnectionConfig.getDefaultConfig();

            // set networkRequestTimeout to 1.1s but CosmosClient internally sets networkRequestTimeout back to 5s, so the injected delay (4s) will not cause failures
            directConnectionConfig.setNetworkRequestTimeout(Duration.ofMillis(1100));

            newClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .consistencyLevel(BridgeInternal.getContextClient(this.clientWithoutPreferredRegions).getConsistencyLevel())
                .directMode(directConnectionConfig)
                .buildAsyncClient();

            CosmosAsyncContainer container =
                newClient
                    .getDatabase(cosmosAsyncContainer.getDatabase().getId())
                    .getContainer(cosmosAsyncContainer.getId());

            CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(timeoutRule)).block();

            // create a new item to be used by read operations
            TestObject createdItem = TestObject.create();
            CosmosItemResponse<TestObject> itemResponse = container.createItem(createdItem).block();

            assertThat(timeoutRule.getHitCount()).isEqualTo(1);
            this.validateHitCount(timeoutRule, 1, OperationType.Create, ResourceType.Document);

            this.validateFaultInjectionRuleApplied(
                itemResponse.getDiagnostics(),
                OperationType.Create,
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.SubStatusCodes.UNKNOWN,
                timeoutRuleId,
                false
            );

        } finally {
            timeoutRule.disable();
            safeClose(newClient);
        }
    }

    @Test(groups = {"multi-region-strong"}, dataProvider = "barrierRequestServerErrorResponseProvider", timeOut = 2 * TIMEOUT)
    public void faultInjection_serverError_barrierRequest(
        OperationType operationType,
        FaultInjectionServerErrorType serverErrorType,
        int statusCode,
        int subStatusCode) throws JsonProcessingException {

        // Test to verify server error type can be injected to barrier requests

        // for barrier request flow, only test on strong consistency
        if (this.databaseAccount.getConsistencyPolicy().getDefaultConsistencyLevel() != ConsistencyLevel.STRONG) {
            throw new SkipException(
                String.format(
                    "Test is not applicable to %s consistency level!",
                    this.databaseAccount.getConsistencyPolicy().getDefaultConsistencyLevel()));
        }

        CosmosAsyncClient newClient = null;
        String faultInjectionRuleId = "barrier-" + serverErrorType + "-" + UUID.randomUUID();
        FaultInjectionRule faultInjectionRule =
            new FaultInjectionRuleBuilder(faultInjectionRuleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(FaultInjectionOperationType.HEAD_COLLECTION)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(serverErrorType)
                        .times(2)
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        try {
            newClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .buildAsyncClient();

            CosmosAsyncContainer container =
                newClient
                    .getDatabase(cosmosAsyncContainer.getDatabase().getId())
                    .getContainer(cosmosAsyncContainer.getId());

            TestObject testItem = TestObject.create();
            container.createItem(testItem).block();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(faultInjectionRule)).block();

            // in order to trigger barrier request, we will need to also modify the store response of the original read/write operation so that GCLSN < LSN
            CosmosInterceptorHelper.registerTransportClientInterceptor(
                newClient,
                (request, storeResponse) -> {
                    if (request.getResourceType() == ResourceType.Document && request.getOperationType() == operationType) {
                        // Decrement so that GCLSN < LSN to simulate the replication lag
                        logger.info("faultInjection_serverError_barrierRequest reducing gclsn");
                        storeResponse.setGCLSN(storeResponse.getLSN() - 2L);
                    }
                    return storeResponse;
                }
            );

            CosmosDiagnostics cosmosDiagnostics = this.performDocumentOperation(container, operationType, testItem, false);
            validateFaultInjectionRuleAppliedForBarrier(
                cosmosDiagnostics,
                operationType,
                statusCode,
                subStatusCode,
                faultInjectionRule.getId());

        } finally {
            faultInjectionRule.disable();
            safeClose(newClient);
        }
    }

    private void validateFaultInjectionRuleApplied(
        CosmosDiagnostics cosmosDiagnostics,
        OperationType operationType,
        int statusCode,
        int subStatusCode,
        String ruleId,
        boolean canRetryOnFaultInjectedError) throws JsonProcessingException {

        validateFaultInjectionRuleApplied(
            cosmosDiagnostics,
            operationType,
            statusCode,
            subStatusCode,
            ruleId,
            canRetryOnFaultInjectedError,
            false);
    }

    private void validateFaultInjectionRuleAppliedForBarrier(
        CosmosDiagnostics cosmosDiagnostics,
        OperationType operationType,
        int statusCode,
        int subStatusCode,
        String ruleId) throws JsonProcessingException {

        validateFaultInjectionRuleApplied(
            cosmosDiagnostics,
            operationType,
            statusCode,
            subStatusCode,
            ruleId,
            true,
            true);
    }

    private void validateFaultInjectionRuleApplied(
        CosmosDiagnostics cosmosDiagnostics,
        OperationType operationType,
        int statusCode,
        int subStatusCode,
        String ruleId,
        boolean canRetryOnFaultInjectedError,
        boolean validateForBarrier) throws JsonProcessingException {

        List<ObjectNode> clientSideRequestStatisticsNodes = new ArrayList<>();
        assertThat(cosmosDiagnostics.getDiagnosticsContext()).isNotNull();

        for (CosmosDiagnostics diagnostics : cosmosDiagnostics.getDiagnosticsContext().getDiagnostics()) {
            if (operationType == OperationType.Query && canRetryOnFaultInjectedError) {
                ObjectNode cosmosDiagnosticsNode = (ObjectNode) Utils.getSimpleObjectMapper().readTree(diagnostics.toString());
                if (cosmosDiagnosticsNode.has("clientSideRequestStatistics")) { // query plan diagnostics will not have clientSideRequestStatistics
                    for (JsonNode node : cosmosDiagnosticsNode.get("clientSideRequestStatistics")) {
                        clientSideRequestStatisticsNodes.add((ObjectNode) node);
                    }
                }
            } else {
                clientSideRequestStatisticsNodes.add((ObjectNode) Utils.getSimpleObjectMapper().readTree(diagnostics.toString()));
            }
        }

        List<JsonNode> responseStatisticsNodes = new ArrayList<>();

        String diagnosticsNodeName = validateForBarrier ? "supplementalResponseStatisticsList" : "responseStatisticsList";
        for (ObjectNode diagnosticNode : clientSideRequestStatisticsNodes) {
            JsonNode responseStatisticsList = diagnosticNode.get(diagnosticsNodeName);
            assertThat(responseStatisticsList.isArray()).isTrue();

            for (JsonNode responseStatisticsNode : responseStatisticsList) {
                responseStatisticsNodes.add(responseStatisticsNode);
            }
        }

        if (canRetryOnFaultInjectedError) {
            assertThat(responseStatisticsNodes.size()).isGreaterThanOrEqualTo(2);
        } else {
            assertThat(responseStatisticsNodes.size()).isOne();
        }

        assertThat(responseStatisticsNodes.stream().anyMatch(responseStatisticsNode -> {
            JsonNode storeResultNode = responseStatisticsNode.get("storeResult");
            assertThat(storeResultNode).isNotNull();

            return (storeResultNode.get("statusCode").asInt() == statusCode)
                && (storeResultNode.get("subStatusCode").asInt() == subStatusCode)
                && (storeResultNode.has("faultInjectionRuleId") && storeResultNode.get("faultInjectionRuleId").asText().equals(ruleId));
        })).isTrue();
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
            JsonNode responseStatisticsList = diagnosticNode.get("responseStatisticsList");
            assertThat(responseStatisticsList.isArray()).isTrue();

            for (int i = 0; i < responseStatisticsList.size(); i++) {
                JsonNode storeResult = responseStatisticsList.get(i).get("storeResult");
                assertThat(storeResult.get("faultInjectionRuleId")).isNull();
                assertThat(storeResult.get("faultInjectionEvaluationResults")).isNotNull();
                assertThat(storeResult.get("faultInjectionEvaluationResults").toString().contains(faultInjectionNonApplicableReason));
            }
            assertThat(responseStatisticsList.size()).isOne();
        }
    }

    private void validateTransportTimelineLatency(
        RequestTimeline.EventName eventName,
        double maxLatency,
        CosmosDiagnostics cosmosDiagnostics) throws JsonProcessingException {

        ObjectNode cosmosDiagnosticsNode = (ObjectNode) Utils.getSimpleObjectMapper().readTree(cosmosDiagnostics.toString());
        JsonNode responseStatisticsList = cosmosDiagnosticsNode.get("responseStatisticsList");
        assertThat(responseStatisticsList.isArray()).isTrue();
        for (int i = 0; i < responseStatisticsList.size(); i++) {
            JsonNode storeResult = responseStatisticsList.get(i).get("storeResult");
            JsonNode transportRequestTimeline = storeResult.get("transportRequestTimeline");
            assertThat(transportRequestTimeline.isArray()).isTrue();

            // loop through the even
            JsonNode event = null;
            for (int j = 0; j < transportRequestTimeline.size(); j++) {
                if (transportRequestTimeline.get(j).get("eventName").asText().equals(eventName.getEventName())) {
                    event = transportRequestTimeline.get(j);
                    break;
                }
            }

            assertThat(event).isNotNull();
            assertThat(event.get("durationInMilliSecs").asDouble()).isLessThanOrEqualTo(maxLatency + 500);
        }
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

    private void validateAddressRefreshWithForceRefresh(CosmosDiagnostics cosmosDiagnostics, boolean isQueryOperation) throws JsonProcessingException {
        ObjectNode diagnosticsNode = (ObjectNode) Utils.getSimpleObjectMapper().readTree(cosmosDiagnostics.toString());
        JsonNode addressResolutionStatistics;

        // optimistically resolve the addressResolutionStatistics

        if (isQueryOperation) {

            ArrayNode arrayNode = (ArrayNode) diagnosticsNode.get("clientSideRequestStatistics");

            if (arrayNode != null) {
                assertThat(arrayNode).isNotNull();
                assertThat(arrayNode.get(0)).isNotNull();

                addressResolutionStatistics = arrayNode.get(0).get("addressResolutionStatistics");
            } else {
                // if the addressResolutionStatistics is not present in the clientSideRequestStatistics, then fallback to the diagnosticsNode
                addressResolutionStatistics = diagnosticsNode.get("addressResolutionStatistics");
            }
        } else {
            addressResolutionStatistics = diagnosticsNode.get("addressResolutionStatistics");
        }

        Iterator<Map.Entry<String, JsonNode>> addressResolutionIterator = addressResolutionStatistics.fields();
        int addressRefreshWithForceRefreshCount = 0;

        while (addressResolutionIterator.hasNext()) {
            JsonNode addressResolutionSingleRequest = addressResolutionIterator.next().getValue();
            if (addressResolutionSingleRequest.get("forceRefresh").asBoolean()) {
                addressRefreshWithForceRefreshCount++;
            }
        }

        assertThat(addressRefreshWithForceRefreshCount).isGreaterThanOrEqualTo(1);
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

        assertThat(accountLevelLocationContext).isNotNull();

        if (isWriteOnly) {
            assertThat(accountLevelLocationContext.serviceOrderedWriteableRegions).isNotNull();
            assertThat(accountLevelLocationContext.serviceOrderedWriteableRegions.size()).isGreaterThanOrEqualTo(1);
        } else {
            assertThat(accountLevelLocationContext.serviceOrderedReadableRegions).isNotNull();
            assertThat(accountLevelLocationContext.serviceOrderedReadableRegions.size()).isGreaterThanOrEqualTo(1);
        }
    }

    private static boolean isOperationAWriteOperation(OperationType operationType) {
        return operationType == OperationType.Create
            || operationType == OperationType.Upsert
            || operationType == OperationType.Replace
            || operationType == OperationType.Delete
            || operationType == OperationType.Patch
            || operationType == OperationType.Batch;
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
