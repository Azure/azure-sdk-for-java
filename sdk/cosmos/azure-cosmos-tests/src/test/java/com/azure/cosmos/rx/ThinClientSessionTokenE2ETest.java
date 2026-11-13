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
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ISessionToken;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxStoreModel;
import com.azure.cosmos.implementation.SessionTokenHelper;
import com.azure.cosmos.implementation.ThinClientStoreModel;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants.RntbdRequestHeader;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequest;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.HexConvert;
import com.azure.cosmos.implementation.routing.RegionNameNormalizer;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyBuilder;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKeyDefinitionVersion;
import com.azure.cosmos.models.PartitionKind;
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
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.mockito.Mockito;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
    private String databaseId;
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
        try (CosmosAsyncClient writer = builder.buildAsyncClient()) {
            awaitThinClientProbe(writer);
            DatabaseAccount account = ReflectionUtils.getGlobalEndpointManager(
                (RxDocumentClientImpl) CosmosBridgeInternal.getAsyncDocumentClient(writer)).getLatestDatabaseAccount();
            assertThat(account.getThinClientReadableLocations()).as("Account must advertise Gateway V2").isNotEmpty();
            regions = account.getThinClientReadableLocations().stream()
                .map(DatabaseAccountLocation::getName).collect(Collectors.toList());
            builder.preferredRegions(regions);
            CosmosAsyncDatabase database = TestSuiteBase.createTestDatabase(writer, "compound-session");
            databaseId = database.getId();
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
    }

    @DataProvider
    public Object[][] metadataModes() {
        return new Object[][] {{false}, {true}};
    }

    // Verify compound progress is replayed through point reads and resumed queries with cold or warm metadata.
    @Test(groups = {"thinclient", "thinclientEndpointProbe"}, dataProvider = "metadataModes", timeOut = 180000)
    public void replayMapThroughPointReadsAndResumedQuery(boolean preloadMetadata) {
        try (CosmosAsyncClient reader = builder.buildAsyncClient()) {
            awaitThinClientProbe(reader);
            CosmosAsyncContainer container = reader.getDatabase(databaseId).getContainer("items");
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

    @DataProvider
    public Object[][] crossPartitionTokenScopes() {
        return new Object[][] {{false}, {true}};
    }

    @Test(groups = {"thinclient", "thinclientEndpointProbe"}, dataProvider = "crossPartitionTokenScopes", timeOut = 180000)
    public void crossPartitionQueryScopesEachTransportRequestToOneOfTwoPhysicalRanges(boolean duplicateTarget)
        throws Exception {

        assertThat(tokens).as("The fixture must contain progress from exactly two physical partitions").hasSize(2);
        Map<String, ISessionToken> expectedProgress = new LinkedHashMap<>(tokens);
        String originalToken = compoundToken;
        if (duplicateTarget) {
            Map.Entry<String, ISessionToken> target = tokens.entrySet().iterator().next();
            originalToken += "," + target.getKey() + ":" + target.getValue().convertToString();
            expectedProgress.put(target.getKey(), null);
        }
        try (CosmosAsyncClient reader = builder.buildAsyncClient()) {
            awaitThinClientProbe(reader);
            CosmosAsyncContainer container = reader.getDatabase(databaseId).getContainer("items");
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions().setSessionToken(originalToken)
                .setCosmosEndToEndOperationLatencyPolicyConfig(policy(false));

            assertQueryTransportScopes(reader, container, expectedProgress, options,
                items.stream().map(item -> item.get("id").asText()).collect(Collectors.toList()), 2);
        }
    }

    @Test(groups = {"thinclient", "thinclientEndpointProbe"}, timeOut = 300000)
    public void hierarchicalPrefixQueryScopesCompoundProgressWithoutWideningItsEpkRange() throws Exception {
        Map<String, ISessionToken> hierarchicalTokens = new LinkedHashMap<>();
        List<String> expectedIds = new ArrayList<>();
        try (CosmosAsyncClient writer = builder.buildAsyncClient()) {
            awaitThinClientProbe(writer);
            PartitionKeyDefinition definition = new PartitionKeyDefinition();
            definition.setKind(PartitionKind.MULTI_HASH);
            definition.setVersion(PartitionKeyDefinitionVersion.V2);
            definition.setPaths(Arrays.asList("/tenantId", "/userId"));
            CosmosAsyncContainer container = TestSuiteBase.createCollection(writer.getDatabase(databaseId),
                new CosmosContainerProperties("hpk-items", definition), new CosmosContainerRequestOptions(), 10100);
            assertThat(container.getFeedRanges().block(WAIT)).hasSize(2);
            for (int tenant = 0; tenant < 16; tenant++) {
                for (int user = 0; user < 3; user++) {
                    String id = "tenant-" + tenant + "-user-" + user;
                    ObjectNode item = MAPPER.createObjectNode().put("id", id)
                        .put("tenantId", "tenant-" + tenant).put("userId", "user-" + user);
                    CosmosItemResponse<ObjectNode> created = container.createItem(item).block(WAIT);
                    TestSuiteBase.assertThinClientEndpointUsed(created.getDiagnostics());
                    String[] parts = created.getSessionToken().split(":", 2);
                    hierarchicalTokens.merge(parts[0], SessionTokenHelper.parse(parts[1]), ISessionToken::merge);
                    if (tenant == 0) {
                        expectedIds.add(id);
                    }
                }
            }
        }
        assertThat(hierarchicalTokens).as("Seed HPK documents in both physical partitions").hasSize(2);
        String originalToken = combine(hierarchicalTokens);
        try (CosmosAsyncClient reader = builder.buildAsyncClient()) {
            awaitThinClientProbe(reader);
            CosmosAsyncContainer container = reader.getDatabase(databaseId).getContainer("hpk-items");
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions().setSessionToken(originalToken)
                .setPartitionKey(new PartitionKeyBuilder().add("tenant-0").build())
                .setCosmosEndToEndOperationLatencyPolicyConfig(policy(false));

            assertQueryTransportScopes(reader, container, hierarchicalTokens, options, expectedIds, 1);
        }
    }

    private static void assertQueryTransportScopes(
        CosmosAsyncClient reader, CosmosAsyncContainer container, Map<String, ISessionToken> progress,
        CosmosQueryRequestOptions options, List<String> expectedIds, int expectedRangeCount) throws Exception {

        RxDocumentClientImpl client = (RxDocumentClientImpl) CosmosBridgeInternal.getAsyncDocumentClient(reader);
        String collectionRid = container.read().block(WAIT).getProperties().getResourceId();
        CollectionRoutingMap topology = client.getPartitionKeyRangeCache()
            .tryLookupAsync(null, collectionRid, null, null).block(WAIT).v;
        assertThat(topology.getOrderedPartitionKeyRanges()).hasSize(2);
        String originalToken = options.getSessionToken();
        try (QueryScopeRecorder recorder = new QueryScopeRecorder(client, topology, progress, originalToken)) {
            List<FeedResponse<ObjectNode>> pages = container.queryItems("SELECT * FROM c", options, ObjectNode.class)
                .byPage(2).collectList().block(WAIT);
            List<String> actualIds = new ArrayList<>();
            boolean sawFallback = false;
            for (FeedResponse<ObjectNode> page : pages) {
                TestSuiteBase.assertThinClientEndpointUsed(page.getCosmosDiagnostics());
                sawFallback |= page.getCosmosDiagnostics().toString().contains("could not be evaluated");
                page.getResults().forEach(item -> actualIds.add(item.get("id").asText()));
            }
            assertThat(sawFallback).as("Only an intentionally unscopable range should report fallback")
                .isEqualTo(progress.containsValue(null));
            assertThat(actualIds).containsExactlyInAnyOrderElementsOf(expectedIds);
            assertThat(recorder.rangeIds).as("Distinct physical targets entering ThinClientStoreModel")
                .hasSize(expectedRangeCount);
            assertThat(recorder.wireRangeIds).containsExactlyInAnyOrderElementsOf(recorder.rangeIds);
            assertThat(options.getSessionToken()).isEqualTo(originalToken);
        }
    }

    @DataProvider
    public Object[][] targetProgress() {
        return new Object[][] {{true, false}, {false, false}, {true, true}, {false, true}};
    }

    // Verify unmatched progress is omitted with helper diagnostics without switching away from Gateway V2.
    @Test(groups = {"thinclient", "thinclientEndpointProbe"}, dataProvider = "targetProgress", timeOut = 90000)
    public void unrelatedProgressStaysOnV2(boolean includeTargetProgress, boolean unknownRange) {
        try (CosmosAsyncClient reader = builder.buildAsyncClient()) {
            awaitThinClientProbe(reader);
            Map.Entry<String, ObjectNode> target = representatives.entrySet().iterator().next();
            ObjectNode item = target.getValue();
            Map<String, ISessionToken> progress = new LinkedHashMap<>(tokens);
            if (!includeTargetProgress) {
                progress.remove(target.getKey());
            }
            String original = combine(progress);
            if (unknownRange) {
                original += ",2147483647:1#0";
            } else if (progress.size() == 1) {
                original += "," + original;
            }
            CosmosItemRequestOptions options = new CosmosItemRequestOptions().setSessionToken(original)
                .setCosmosEndToEndOperationLatencyPolicyConfig(policy(false));
            CosmosItemResponse<ObjectNode> response = reader.getDatabase(databaseId).getContainer("items")
                .readItem(item.get("id").asText(), new PartitionKey(item.get("pk").asText()), options, ObjectNode.class)
                .block(WAIT);
            assertThat(response.getItem().get("version")).isEqualTo(item.get("version"));
            TestSuiteBase.assertThinClientEndpointUsed(response.getDiagnostics());
            if (!includeTargetProgress) {
                assertThat(response.getDiagnostics().toString())
                    .contains("could not be evaluated");
            } else {
                assertThat(response.getDiagnostics().toString()).doesNotContain("could not be evaluated");
            }
            assertThat(options.getSessionToken()).isEqualTo(original);
        }
    }

    // Verify an unattainable session position fails with 404/1002 instead of silently dropping required progress.
    @Test(groups = {"thinclient", "thinclientEndpointProbe"}, timeOut = 90000)
    public void advancedTargetProgressIsNotSilentlyDropped() throws Exception {
        try (CosmosAsyncClient reader = builder.buildAsyncClient()) {
            awaitThinClientProbe(reader);
            Map.Entry<String, ObjectNode> target = representatives.entrySet().iterator().next();
            Map<String, ISessionToken> advanced = new LinkedHashMap<>(tokens);
            ISessionToken token = advanced.get(target.getKey());
            advanced.put(target.getKey(), ConsistencyTestsBase.createSessionToken(token, token.getLSN() + 1000000));
            ObjectNode item = target.getValue();
            CosmosItemRequestOptions options = new CosmosItemRequestOptions().setSessionToken(combine(advanced))
                .setCosmosEndToEndOperationLatencyPolicyConfig(policy(false));
            assertThatThrownBy(() -> reader.getDatabase(databaseId).getContainer("items")
                .readItem(item.get("id").asText(), new PartitionKey(item.get("pk").asText()), options, ObjectNode.class)
                .block(WAIT)).isInstanceOfSatisfying(CosmosException.class, error -> {
                    assertThat(error.getStatusCode()).isEqualTo(404);
                    assertThat(error.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE);
                    TestSuiteBase.assertThinClientEndpointUsed(error.getDiagnostics());
                });
        }
    }

    // Verify delayed-primary point and query hedges preserve the caller's compound token across regions.
    @Test(groups = {"thinclient", "thinclientEndpointProbe"}, timeOut = 180000)
    public void forcedPointAndQueryHedgesRetainCompoundProgress() {
        if (regions.size() < 2) {
            throw new SkipException("Forced live hedging requires two Gateway V2 regions");
        }
        try (CosmosAsyncClient reader = builder.buildAsyncClient()) {
            awaitThinClientProbe(reader);
            CosmosAsyncContainer container = reader.getDatabase(databaseId).getContainer("items");
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

    @AfterClass(groups = {"thinclient", "thinclientEndpointProbe"}, alwaysRun = true, timeOut = 240000)
    public void cleanup() {
        if (databaseId != null) {
            try (CosmosAsyncClient cleanupClient = builder.buildAsyncClient()) {
                CosmosAsyncDatabase database = cleanupClient.getDatabase(databaseId);
                TestSuiteBase.safeDeleteDatabase(database);
                assertThatThrownBy(() -> database.read()
                    .retryWhen(Retry.fixedDelay(5, Duration.ofSeconds(2))
                        .filter(error -> error instanceof CosmosException
                            && ((CosmosException) error).getStatusCode() == 429)
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()))
                    .block(Duration.ofSeconds(120)))
                    .isInstanceOfSatisfying(CosmosException.class, error ->
                        assertThat(error.getStatusCode()).isEqualTo(404));
            }
        }
    }

    private static void awaitThinClientProbe(CosmosAsyncClient client) {
        if (Configs.isThinClientEnabled() == null) {
            Flux.interval(Duration.ZERO, Duration.ofMillis(100))
                .filter(ignored -> Boolean.TRUE.equals(ReflectionUtils.getGlobalEndpointManager(
                    (RxDocumentClientImpl) CosmosBridgeInternal.getAsyncDocumentClient(client)).getProxyProbeDecision()))
                .next().block(WAIT);
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

    private static final class QueryScopeRecorder implements AutoCloseable {
        private final RxDocumentClientImpl client;
        private final RxStoreModel original;
        private final Set<String> rangeIds = ConcurrentHashMap.newKeySet();
        private final Set<String> wireRangeIds = ConcurrentHashMap.newKeySet();

        private QueryScopeRecorder(
            RxDocumentClientImpl client, CollectionRoutingMap topology,
            Map<String, ISessionToken> progress, String originalToken) throws Exception {

            this.client = client;
            this.original = ReflectionUtils.getThinProxy(client);
            assertThat(original).isInstanceOf(ThinClientStoreModel.class);
            ThinClientStoreModel transport = Mockito.spy((ThinClientStoreModel) original);
            Mockito.doAnswer(invocation -> {
                RxDocumentServiceRequest request = invocation.getArgument(0);
                if (isDocumentQuery(request)) {
                    assertThat(request.getEffectiveRange()).as("Each query attempt must have an EPK scope").isNotNull();
                    List<PartitionKeyRange> ranges = topology.getOverlappingRanges(request.getEffectiveRange());
                    assertThat(ranges).as("A data query entering the transport must overlap exactly one physical range")
                        .hasSize(1);
                    assertThat(request.getPartitionKeyRangeIdentity()).isNotNull();
                    String rangeId = request.getPartitionKeyRangeIdentity().getPartitionKeyRangeId();
                    assertThat(rangeId).isEqualTo(ranges.get(0).getId());
                    assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(originalToken);
                    rangeIds.add(rangeId);
                }
                return invocation.callRealMethod();
            }).when(transport).performRequestInternal(Mockito.any(), Mockito.any());
            Mockito.doAnswer(invocation -> {
                RxDocumentServiceRequest request = invocation.getArgument(0);
                HttpRequest wireRequest = (HttpRequest) invocation.callRealMethod();
                if (!isDocumentQuery(request)) {
                    return wireRequest;
                }
                String rangeId = request.getPartitionKeyRangeIdentity().getPartitionKeyRangeId();
                assertThat(request.requestContext.resolvedPartitionKeyRange.getId()).isEqualTo(rangeId);
                assertThat(progress).containsKey(rangeId);
                ISessionToken expectedProgress = progress.get(rangeId);
                assertThat(request.requestContext.sessionToken).isEqualTo(expectedProgress);
                if (expectedProgress == null) {
                    assertThat(request.requestContext.getSessionTokenEvaluationResults())
                        .anySatisfy(result -> assertThat(result).contains(originalToken, "pkRangeId : " + rangeId));
                }
                String expectedToken = expectedProgress == null ? "" : rangeId + ":" + expectedProgress.convertToString();
                return wireRequest.withBody(wireRequest.body().doOnNext(body -> {
                    ByteBuf buffer = Unpooled.wrappedBuffer(body);
                    try {
                        buffer.writerIndex(buffer.getIntLE(0));
                        RntbdRequest decoded = RntbdRequest.decode(buffer);
                        assertThat(decoded.<String>getHeader(RntbdRequestHeader.SessionToken)).isEqualTo(expectedToken);
                        ByteBuf start = decoded.getHeader(RntbdRequestHeader.StartEpkHash);
                        try {
                            assertThat(ByteBufUtil.getBytes(start))
                                .containsExactly(HexConvert.hexToBytes(request.getEffectiveRange().getMin()));
                        } finally {
                            start.release();
                        }
                        ByteBuf end = decoded.getHeader(RntbdRequestHeader.EndEpkHash);
                        try {
                            assertThat(ByteBufUtil.getBytes(end))
                                .containsExactly(HexConvert.hexToBytes(request.getEffectiveRange().getMax()));
                        } finally {
                            end.release();
                        }
                        assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(originalToken);
                        wireRangeIds.add(rangeId);
                    } finally {
                        buffer.release();
                    }
                }));
            }).when(transport).wrapInHttpRequest(Mockito.any(), Mockito.any());
            ReflectionUtils.setThinProxy(client, transport);
        }

        private static boolean isDocumentQuery(RxDocumentServiceRequest request) {
            return request.getOperationType() == OperationType.Query && request.getResourceType() == ResourceType.Document;
        }

        @Override
        public void close() {
            ReflectionUtils.setThinProxy(client, original);
        }
    }
}
