// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfigBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.CosmosRegionSwitchHint;
import com.azure.cosmos.SessionRetryOptionsBuilder;
import com.azure.cosmos.ThresholdBasedAvailabilityStrategy;
import com.azure.cosmos.implementation.ConsistencyTestsBase;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ISessionToken;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.SessionTokenHelper;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.routing.RegionNameNormalizer;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ThinClientSessionTokenE2ETest {
    private static final Duration WAIT = Duration.ofSeconds(60);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final CosmosClientBuilder builder;
    private final Map<String, ISessionToken> tokens = new LinkedHashMap<>();
    private final Map<String, ObjectNode> representatives = new LinkedHashMap<>();
    private final List<ObjectNode> items = new ArrayList<>();
    private CosmosAsyncClient writer;
    private CosmosAsyncDatabase database;
    private List<String> regions;
    private String compoundToken;

    @Factory(dataProvider = "clientBuildersWithGatewayAndHttp2", dataProviderClass = TestSuiteBase.class)
    public ThinClientSessionTokenE2ETest(CosmosClientBuilder builder) {
        this.builder = builder.consistencyLevel(ConsistencyLevel.SESSION)
            .sessionRetryOptions(new SessionRetryOptionsBuilder()
                .regionSwitchHint(CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED)
                .maxRetriesPerRegion(1).minTimeoutPerRegion(Duration.ofMillis(100)).build());
    }

    @BeforeClass(groups = {"thinclient", "thinclientEndpointProbe"}, timeOut = 300000)
    public void setup() {
        writer = builder.buildAsyncClient();
        DatabaseAccount account = ReflectionUtils.getGlobalEndpointManager(
            (RxDocumentClientImpl) CosmosBridgeInternal.getAsyncDocumentClient(writer)).getLatestDatabaseAccount();
        assertThat(account.getThinClientReadableLocations()).as("Account must advertise Gateway V2").isNotEmpty();
        regions = account.getThinClientReadableLocations().stream()
            .map(DatabaseAccountLocation::getName).collect(Collectors.toList());
        builder.preferredRegions(regions);
        database = TestSuiteBase.createTestDatabase(writer, "compound-session");
        CosmosAsyncContainer container = TestSuiteBase.createCollection(database,
            new CosmosContainerProperties("items", "/pk"), new CosmosContainerRequestOptions(), 10100);
        assertThat(container.getFeedRanges().block(WAIT)).hasSizeGreaterThanOrEqualTo(2);

        for (int i = 0; i < 128 && (tokens.size() < 2 || items.size() < 6); i++) {
            ObjectNode item = MAPPER.createObjectNode().put("id", "item-" + i).put("pk", "partition-" + i)
                .put("version", i + 1);
            CosmosItemResponse<ObjectNode> response = container.createItem(item).block(WAIT);
            TestSuiteBase.assertThinClientEndpointUsed(response.getDiagnostics());
            items.add(item);
            assertThat(response.getSessionToken()).isNotBlank();
            for (String segment : response.getSessionToken().split(",")) {
                String[] parts = segment.split(":", 2);
                assertThat(parts).hasSize(2);
                tokens.merge(parts[0], SessionTokenHelper.parse(parts[1]), ISessionToken::merge);
                representatives.put(parts[0], item);
            }
        }
        assertThat(tokens).as("The token must cover distinct physical ranges").hasSizeGreaterThanOrEqualTo(2);
        compoundToken = combine(tokens);
    }

    @DataProvider
    public Object[][] metadataModes() {
        return new Object[][] {{false}, {true}};
    }

    @Test(groups = {"thinclient", "thinclientEndpointProbe"}, dataProvider = "metadataModes", timeOut = 180000)
    public void replayMapThroughPointReadsAndResumedQuery(boolean preloadMetadata) {
        try (CosmosAsyncClient reader = builder.buildAsyncClient()) {
            CosmosAsyncContainer container = reader.getDatabase(database.getId()).getContainer("items");
            if (preloadMetadata) {
                container.getFeedRanges().block(WAIT);
            }
            CosmosItemRequestOptions options = new CosmosItemRequestOptions().setSessionToken(compoundToken)
                .setCosmosEndToEndOperationLatencyPolicyConfig(policy(false));
            for (ObjectNode item : representatives.values()) {
                CosmosItemResponse<ObjectNode> response = container.readItem(
                    item.get("id").asText(), new PartitionKey(item.get("pk").asText()), options, ObjectNode.class)
                    .block(WAIT);
                assertThat(response.getItem().get("version")).isEqualTo(item.get("version"));
                TestSuiteBase.assertThinClientEndpointUsed(response.getDiagnostics());
            }
            assertThat(options.getSessionToken()).isEqualTo(compoundToken);

            List<String> ids = new ArrayList<>();
            String continuation = null;
            int pages = 0;
            do {
                CosmosQueryRequestOptions query = new CosmosQueryRequestOptions().setSessionToken(compoundToken)
                    .setCosmosEndToEndOperationLatencyPolicyConfig(policy(false));
                FeedResponse<ObjectNode> page = container.queryItems("SELECT * FROM c", query, ObjectNode.class)
                    .byPage(continuation, 2).next().block(WAIT);
                assertThat(page).isNotNull();
                for (ObjectNode item : page.getResults()) {
                    ids.add(item.get("id").asText());
                    assertThat(items).anySatisfy(expected -> {
                        assertThat(item.get("id")).isEqualTo(expected.get("id"));
                        assertThat(item.get("version")).isEqualTo(expected.get("version"));
                    });
                }
                TestSuiteBase.assertThinClientEndpointUsed(page.getCosmosDiagnostics());
                assertThat(query.getSessionToken()).isEqualTo(compoundToken);
                continuation = page.getContinuationToken();
                assertThat(++pages).isLessThan(100);
            } while (continuation != null);
            assertThat(ids).containsExactlyInAnyOrderElementsOf(
                items.stream().map(item -> item.get("id").asText()).collect(Collectors.toList()));
            assertThat(pages).isGreaterThan(1);
        }
    }

    @Test(groups = {"thinclient", "thinclientEndpointProbe"}, timeOut = 90000)
    public void unknownRangeUsesClassicGatewayWithoutDroppingProgress() {
        try (CosmosAsyncClient reader = builder.buildAsyncClient()) {
            ObjectNode item = representatives.values().iterator().next();
            String original = compoundToken + ",2147483647:1#0";
            CosmosItemRequestOptions options = new CosmosItemRequestOptions().setSessionToken(original)
                .setCosmosEndToEndOperationLatencyPolicyConfig(policy(false));
            CosmosItemResponse<ObjectNode> response = reader.getDatabase(database.getId()).getContainer("items")
                .readItem(item.get("id").asText(), new PartitionKey(item.get("pk").asText()), options, ObjectNode.class)
                .block(WAIT);
            assertThat(response.getItem().get("version")).isEqualTo(item.get("version"));
            ThinClientTestBase.assertGatewayEndpointUsed(response.getDiagnostics());
            assertThat(options.getSessionToken()).isEqualTo(original);
        }
    }

    @Test(groups = {"thinclient", "thinclientEndpointProbe"}, timeOut = 90000)
    public void advancedTargetProgressIsNotSilentlyDropped() throws Exception {
        try (CosmosAsyncClient reader = builder.buildAsyncClient()) {
            Map.Entry<String, ObjectNode> target = representatives.entrySet().iterator().next();
            Map<String, ISessionToken> advanced = new LinkedHashMap<>(tokens);
            ISessionToken token = advanced.get(target.getKey());
            advanced.put(target.getKey(), ConsistencyTestsBase.createSessionToken(token, token.getLSN() + 1000000));
            ObjectNode item = target.getValue();
            CosmosItemRequestOptions options = new CosmosItemRequestOptions().setSessionToken(combine(advanced))
                .setCosmosEndToEndOperationLatencyPolicyConfig(policy(false));
            assertThatThrownBy(() -> reader.getDatabase(database.getId()).getContainer("items")
                .readItem(item.get("id").asText(), new PartitionKey(item.get("pk").asText()), options, ObjectNode.class)
                .block(WAIT)).isInstanceOfSatisfying(CosmosException.class, error -> {
                    assertThat(error.getStatusCode()).isEqualTo(404);
                    assertThat(error.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE);
                    TestSuiteBase.assertThinClientEndpointUsed(error.getDiagnostics());
                });
        }
    }

    @Test(groups = {"thinclient", "thinclientEndpointProbe"}, timeOut = 180000)
    public void forcedPointAndQueryHedgesRetainCompoundProgress() {
        if (regions.size() < 2) {
            throw new SkipException("Forced live hedging requires two Gateway V2 regions");
        }
        try (CosmosAsyncClient reader = builder.buildAsyncClient()) {
            CosmosAsyncContainer container = reader.getDatabase(database.getId()).getContainer("items");
            ObjectNode item = representatives.values().iterator().next();
            for (FaultInjectionOperationType operation : new FaultInjectionOperationType[] {
                FaultInjectionOperationType.READ_ITEM, FaultInjectionOperationType.QUERY_ITEM}) {
                FaultInjectionRule rule = new FaultInjectionRuleBuilder("compound-session-" + operation)
                    .condition(new FaultInjectionConditionBuilder().region(regions.get(0))
                        .connectionType(FaultInjectionConnectionType.GATEWAY).operationType(operation).build())
                    .result(FaultInjectionResultBuilders.getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
                        .delay(Duration.ofSeconds(3)).build()).build();
                try {
                    CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Collections.singletonList(rule))
                        .block(WAIT);
                    CosmosDiagnostics diagnostics;
                    if (operation == FaultInjectionOperationType.READ_ITEM) {
                        CosmosItemRequestOptions options = new CosmosItemRequestOptions().setSessionToken(compoundToken)
                            .setCosmosEndToEndOperationLatencyPolicyConfig(policy(true));
                        CosmosItemResponse<ObjectNode> response = container.readItem(item.get("id").asText(),
                            new PartitionKey(item.get("pk").asText()), options, ObjectNode.class).block(WAIT);
                        assertThat(response.getItem().get("version")).isEqualTo(item.get("version"));
                        assertThat(options.getSessionToken()).isEqualTo(compoundToken);
                        diagnostics = response.getDiagnostics();
                    } else {
                        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions().setSessionToken(compoundToken)
                            .setPartitionKey(new PartitionKey(item.get("pk").asText()))
                            .setCosmosEndToEndOperationLatencyPolicyConfig(policy(true));
                        FeedResponse<ObjectNode> response = container.queryItems(
                            "SELECT * FROM c", options, ObjectNode.class).byPage().next().block(WAIT);
                        assertThat(response.getResults()).anySatisfy(actual ->
                            assertThat(actual.get("version")).isEqualTo(item.get("version")));
                        assertThat(options.getSessionToken()).isEqualTo(compoundToken);
                        diagnostics = response.getCosmosDiagnostics();
                    }
                    assertThat(rule.getHitCount()).as("Primary-region request must hit the delay rule").isPositive();
                    // The cancelled primary is proven by the rule; response diagnostics may contain only the winner.
                    assertThat(diagnostics.getContactedRegionNames().stream()
                        .map(RegionNameNormalizer::normalize).collect(Collectors.toList()))
                        .contains(RegionNameNormalizer.normalize(regions.get(1)));
                    TestSuiteBase.assertThinClientEndpointUsed(diagnostics);
                } finally {
                    rule.disable();
                    CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Collections.emptyList())
                        .block(WAIT);
                }
            }
        }
    }

    @AfterClass(groups = {"thinclient", "thinclientEndpointProbe"}, alwaysRun = true, timeOut = 120000)
    public void cleanup() {
        try {
            if (database != null) {
                TestSuiteBase.safeDeleteDatabase(database);
                assertThatThrownBy(() -> database.read().block(WAIT))
                    .isInstanceOfSatisfying(CosmosException.class, error ->
                        assertThat(error.getStatusCode()).isEqualTo(404));
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private static CosmosEndToEndOperationLatencyPolicyConfig policy(boolean hedge) {
        CosmosEndToEndOperationLatencyPolicyConfigBuilder builder =
            new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(20));
        if (hedge) {
            builder.availabilityStrategy(new ThresholdBasedAvailabilityStrategy(
                Duration.ofMillis(100), Duration.ofMillis(100)));
        }
        return builder.build();
    }

    private static String combine(Map<String, ISessionToken> progress) {
        return progress.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue().convertToString())
            .collect(Collectors.joining(","));
    }
}
