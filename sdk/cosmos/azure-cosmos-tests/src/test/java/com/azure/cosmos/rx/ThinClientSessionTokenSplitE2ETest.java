// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfigBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.CosmosRegionSwitchHint;
import com.azure.cosmos.SessionRetryOptionsBuilder;
import com.azure.cosmos.implementation.ConsistencyTestsBase;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ISessionToken;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxStoreModel;
import com.azure.cosmos.implementation.SessionTokenHelper;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants.RntbdRequestHeader;
import com.azure.cosmos.implementation.directconnectivity.rntbd.ThinClientRangeHeadersTest;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.ThroughputProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ThinClientSessionTokenSplitE2ETest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThinClientSessionTokenSplitE2ETest.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Duration WAIT = Duration.ofSeconds(60);
    private static final Duration SPLIT_WAIT = Duration.ofMinutes(15);
    private static final Duration SPLIT_POLL_INTERVAL = Duration.ofSeconds(10);
    private final CosmosClientBuilder builder;
    private CosmosAsyncClient staleReader;
    private CosmosAsyncClient observer;
    private CosmosAsyncClient negativeReader;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer staleContainer;
    private CosmosAsyncContainer currentContainer;
    private CosmosAsyncContainer negativeContainer;
    private String collectionRid;
    private PartitionKeyDefinition partitionKeyDefinition;
    private CollectionRoutingMap beforeSplit;
    private CollectionRoutingMap negativeBeforeSplit;
    private final Map<String, List<String>> parentTokens = new LinkedHashMap<>();

    @Factory(dataProvider = "clientBuildersWithGatewayAndHttp2", dataProviderClass = TestSuiteBase.class)
    public ThinClientSessionTokenSplitE2ETest(CosmosClientBuilder builder) {
        this.builder = builder.consistencyLevel(ConsistencyLevel.SESSION)
            .sessionRetryOptions(new SessionRetryOptionsBuilder()
                .regionSwitchHint(CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED)
                .maxRetriesPerRegion(1)
                .minTimeoutPerRegion(Duration.ofMillis(100))
                .build());
    }

    @BeforeClass(groups = "thinclientSplit", timeOut = 300000)
    public void setup() {
        staleReader = builder.buildAsyncClient();
        awaitThinClient(staleReader);
        DatabaseAccount account = internal(staleReader).getGlobalEndpointManager().getLatestDatabaseAccount();
        List<String> regions = account.getThinClientReadableLocations().stream()
            .map(location -> location.getName()).collect(Collectors.toList());
        assertThat(regions).as("Two Gateway V2 regions are needed to isolate the observer and both stale-reader caches")
            .hasSizeGreaterThanOrEqualTo(2);
        String region = regions.get(0);
        String regionalEndpoint = StreamSupport.stream(account.getReadableLocations().spliterator(), false)
            .filter(location -> region.equals(location.getName()))
            .findFirst().orElseThrow(() -> new IllegalStateException("No classic endpoint for " + region))
            .getEndpoint();
        assertThat(URI.create(regionalEndpoint).getHost())
            .as("Use a global ACCOUNT_HOST so the observer's regional endpoint has a separate cache")
            .isNotEqualTo(internal(staleReader).getServiceEndpoint().getHost());
        observer = builder.endpoint(regionalEndpoint).preferredRegions(Collections.singletonList(region)).buildAsyncClient();
        awaitThinClient(observer);
        assertThat(ReflectionUtils.getRoutingMapAsyncCacheNonBlocking(cache(staleReader)))
            .isNotSameAs(ReflectionUtils.getRoutingMapAsyncCacheNonBlocking(cache(observer)));
        String negativeEndpoint = StreamSupport.stream(account.getReadableLocations().spliterator(), false)
            .filter(location -> regions.get(1).equals(location.getName()))
            .findFirst().orElseThrow(() -> new IllegalStateException("No classic endpoint for " + regions.get(1)))
            .getEndpoint();
        negativeReader = builder.endpoint(negativeEndpoint)
            .preferredRegions(Collections.singletonList(regions.get(1))).buildAsyncClient();
        awaitThinClient(negativeReader);
        assertThat(ReflectionUtils.getRoutingMapAsyncCacheNonBlocking(cache(negativeReader)))
            .isNotSameAs(ReflectionUtils.getRoutingMapAsyncCacheNonBlocking(cache(staleReader)))
            .isNotSameAs(ReflectionUtils.getRoutingMapAsyncCacheNonBlocking(cache(observer)));

        database = TestSuiteBase.createTestDatabase(staleReader, "compound-session-split");
        CosmosContainerProperties properties = new CosmosContainerProperties("items", "/pk");
        database.createContainer(properties, ThroughputProperties.createManualThroughput(400)).block(WAIT);
        staleContainer = database.getContainer("items");
        currentContainer = observer.getDatabase(database.getId()).getContainer("items");
        negativeContainer = negativeReader.getDatabase(database.getId()).getContainer("items");
        CosmosContainerProperties actual = staleContainer.read().block(WAIT).getProperties();
        collectionRid = actual.getResourceId();
        partitionKeyDefinition = actual.getPartitionKeyDefinition();
        assertThat(awaitContainer(currentContainer).getResourceId()).isEqualTo(collectionRid);
        assertThat(awaitContainer(negativeContainer).getResourceId()).isEqualTo(collectionRid);
        assertThat(staleContainer.readThroughput().block(WAIT).getProperties().getManualThroughput()).isEqualTo(400);
        beforeSplit = routingMap(staleReader, null);
        assertThat(beforeSplit.getOrderedPartitionKeyRanges()).isNotEmpty();
        for (PartitionKeyRange parent : beforeSplit.getOrderedPartitionKeyRanges()) {
            List<String> tokens = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                ObjectNode item = itemInRange(beforeSplit, parent.getId(), "parent-" + parent.getId() + "-" + i);
                CosmosItemResponse<ObjectNode> created = staleContainer.createItem(item).block(WAIT);
                TestSuiteBase.assertThinClientEndpointUsed(created.getDiagnostics());
                assertThat(created.getSessionToken()).startsWith(parent.getId() + ":");
                tokens.add(created.getSessionToken());
            }
            parentTokens.put(parent.getId(), tokens);
        }
        assertThat(routingMap(staleReader, null)).isSameAs(beforeSplit);
        negativeBeforeSplit = routingMap(negativeReader, null);
        assertThat(negativeBeforeSplit.getOrderedPartitionKeyRanges())
            .containsExactlyElementsOf(beforeSplit.getOrderedPartitionKeyRanges());
    }

    @Test(groups = "thinclientSplit", timeOut = 1200000)
    public void pointReadsPreserveCompoundProgressAcrossRealSplit() throws Exception {
        currentContainer.replaceThroughput(ThroughputProperties.createManualThroughput(10100)).block(WAIT);
        CollectionRoutingMap afterSplit = awaitSplit();
        assertThat(currentContainer.readThroughput().block(WAIT).getProperties().getManualThroughput()).isEqualTo(10100);
        PartitionKeyRange child = afterSplit.getOrderedPartitionKeyRanges().stream()
            .filter(range -> range.getParents() != null
                && range.getParents().stream().anyMatch(parentTokens::containsKey))
            .findFirst().orElseThrow(() -> new AssertionError("Split did not expose a known parent-child relationship"));
        String parentId = child.getParents().stream().filter(parentTokens::containsKey).findFirst().get();
        List<PartitionKeyRange> children = afterSplit.getOrderedPartitionKeyRanges().stream()
            .filter(range -> range.getParents() != null && range.getParents().contains(parentId))
            .collect(Collectors.toList());
        PartitionKeyRange parent = beforeSplit.getRangeByPartitionKeyRangeId(parentId);
        assertThat(children).hasSize(2);
        assertThat(children.get(0).getMinInclusive()).isEqualTo(parent.getMinInclusive());
        assertThat(children.get(0).getMaxExclusive()).isEqualTo(children.get(1).getMinInclusive());
        assertThat(children.get(1).getMaxExclusive()).isEqualTo(parent.getMaxExclusive());
        assertThat(afterSplit.tryGetRangeByPartitionKeyRangeId(parentId)).isNull();

        // Two real responses from the parent keep this compound even when 400 RU/s starts with one range.
        String parentCompound = String.join(",", parentTokens.get(parentId));
        ISessionToken parentProgress = null;
        for (String token : parentTokens.get(parentId)) {
            ISessionToken progress = SessionTokenHelper.parse(token.substring(token.indexOf(':') + 1));
            parentProgress = parentProgress == null ? progress : parentProgress.merge(progress);
        }
        Map<String, ISessionToken> childProgress = new LinkedHashMap<>();
        Map<String, ObjectNode> childItems = new LinkedHashMap<>();
        for (PartitionKeyRange range : children) {
            ObjectNode item = itemInRange(afterSplit, range.getId(), "child-" + range.getId());
            CosmosItemResponse<ObjectNode> created = currentContainer.createItem(item).block(WAIT);
            TestSuiteBase.assertThinClientEndpointUsed(created.getDiagnostics());
            String[] parts = created.getSessionToken().split(":", 2);
            assertThat(parts[0]).isEqualTo(range.getId());
            childProgress.put(parts[0], SessionTokenHelper.parse(parts[1]));
            childItems.put(range.getId(), item);
        }
        String childCompound = combine(childProgress);
        ObjectNode item = childItems.get(child.getId());
        assertStale(staleReader, beforeSplit, parentId, child.getId());

        try (RequestRecorder freshRequests = new RequestRecorder(observer);
             RequestRecorder staleRequests = new RequestRecorder(staleReader);
             RequestRecorder negativeRequests = new RequestRecorder(negativeReader)) {
            readAndAssert(currentContainer, item, parentCompound,
                child.getId() + ":" + parentProgress.convertToString(), true, freshRequests);
            readAndAssert(currentContainer, item, childCompound,
                child.getId() + ":" + childProgress.get(child.getId()).convertToString(), true, freshRequests);
            assertStale(staleReader, beforeSplit, parentId, child.getId());
            readAndAssert(staleContainer, item, childCompound, childCompound, false, staleRequests);

            assertStale(negativeReader, negativeBeforeSplit, parentId, child.getId());
            Map<String, ISessionToken> advanced = new LinkedHashMap<>(childProgress);
            ISessionToken target = advanced.get(child.getId());
            advanced.put(child.getId(), ConsistencyTestsBase.createSessionToken(target, target.getLSN() + 1000000));
            String advancedCompound = combine(advanced);
            negativeRequests.clear();
            assertThatThrownBy(() -> negativeContainer.readItem(
                item.get("id").asText(), new PartitionKey(item.get("pk").asText()),
                options(advancedCompound), ObjectNode.class).block(WAIT))
                .isInstanceOfSatisfying(CosmosException.class, error -> {
                    assertThat(error.getStatusCode()).isEqualTo(404);
                    assertThat(error.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE);
                });
            assertThat(negativeRequests.requests).isNotEmpty();
            assertThat(negativeRequests.requests.get(0).getRight()).isEqualTo(advancedCompound);
            assertThat(negativeRequests.requests.get(0).getLeft().getPort()).isIn(-1, 443);
        }
    }

    private CollectionRoutingMap awaitSplit() {
        AtomicReference<CollectionRoutingMap> latest = new AtomicReference<>(routingMap(observer, null));
        CollectionRoutingMap split = Flux.interval(Duration.ZERO, SPLIT_POLL_INTERVAL)
            .concatMap(ignored -> currentContainer.readThroughput()
                .flatMap(throughput -> cache(observer).tryLookupAsync(null, collectionRid, latest.get(), null)
                    .map(holder -> {
                        assertThat(holder.v).as("Observer routing map").isNotNull();
                        latest.set(holder.v);
                        LOGGER.info(
                            "Waiting for real split: replacePending={}, manualThroughput={}, initialRangeCount={}, currentRanges={}",
                            throughput.isReplacePending(),
                            throughput.getProperties().getManualThroughput(),
                            beforeSplit.getOrderedPartitionKeyRanges().size(),
                            holder.v.getOrderedPartitionKeyRanges());
                        return holder.v;
                    })))
            .filter(map -> map.getOrderedPartitionKeyRanges().size() > beforeSplit.getOrderedPartitionKeyRanges().size()
                && map.getOrderedPartitionKeyRanges().stream().anyMatch(range -> range.getParents() != null
                    && range.getParents().stream().anyMatch(parentTokens::containsKey)))
            .next()
            .timeout(SPLIT_WAIT, Mono.defer(() -> Mono.error(new AssertionError(
                "No real parent-to-child split observed within " + SPLIT_WAIT.toMinutes()
                    + " minutes after manual throughput increase from 400 to 10100 RU/s; latest ranges: "
                    + latest.get().getOrderedPartitionKeyRanges()))))
            .block(SPLIT_WAIT.plus(WAIT));
        LOGGER.info("Observed real split: before={}, after={}",
            beforeSplit.getOrderedPartitionKeyRanges(), split.getOrderedPartitionKeyRanges());
        return split;
    }

    private void assertStale(CosmosAsyncClient reader, CollectionRoutingMap original, String parentId, String childId) {
        CollectionRoutingMap cached = routingMap(reader, null);
        assertThat(cached).as("Reader must retain its genuine pre-split cache; do not silently test a refreshed reader")
            .isSameAs(original);
        assertThat(cached.tryGetRangeByPartitionKeyRangeId(parentId)).isNotNull();
        assertThat(cached.tryGetRangeByPartitionKeyRangeId(childId)).isNull();
    }

    private CollectionRoutingMap routingMap(CosmosAsyncClient client, CollectionRoutingMap previous) {
        CollectionRoutingMap map = cache(client).tryLookupAsync(null, collectionRid, previous, null).block(WAIT).v;
        assertThat(map).isNotNull();
        return map;
    }

    private ObjectNode itemInRange(CollectionRoutingMap map, String rangeId, String prefix) {
        for (int i = 0; i < 10000; i++) {
            String key = prefix + "-" + i;
            PartitionKeyInternal internalKey = PartitionKeyInternal.fromObjectArray(new Object[] {key}, true);
            String epk = internalKey.getEffectivePartitionKeyString(internalKey, partitionKeyDefinition);
            if (rangeId.equals(map.getRangeByEffectivePartitionKey(epk).getId())) {
                return MAPPER.createObjectNode().put("id", key).put("pk", key).put("version", 1);
            }
        }
        throw new AssertionError("Could not find a representative partition key for real range " + rangeId);
    }

    private static void readAndAssert(CosmosAsyncContainer container, ObjectNode item, String original,
        String expected, boolean thin, RequestRecorder recorder) {
        recorder.clear();
        CosmosItemRequestOptions options = options(original);
        CosmosItemResponse<ObjectNode> response = container.readItem(
            item.get("id").asText(), new PartitionKey(item.get("pk").asText()), options, ObjectNode.class).block(WAIT);
        assertThat(response.getItem().get("id")).isEqualTo(item.get("id"));
        assertThat(response.getItem().get("pk")).isEqualTo(item.get("pk"));
        assertThat(response.getItem().get("version")).isEqualTo(item.get("version"));
        assertThat(options.getSessionToken()).isEqualTo(original);
        assertThat(recorder.requests).isNotEmpty().allSatisfy(attempt -> {
            assertThat(attempt.getRight()).isEqualTo(expected);
            assertThat(attempt.getLeft().getScheme()).isEqualTo("https");
            if (thin) {
                assertThat(attempt.getLeft().getPort()).isEqualTo(10250);
            } else {
                assertThat(attempt.getLeft().getPort()).isIn(-1, 443);
            }
        });
    }

    private static CosmosItemRequestOptions options(String token) {
        return new CosmosItemRequestOptions().setSessionToken(token)
            .setCosmosEndToEndOperationLatencyPolicyConfig(
                new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(20)).build());
    }

    private static CosmosContainerProperties awaitContainer(CosmosAsyncContainer container) {
        return container.read()
            .retryWhen(Retry.fixedDelay(30, Duration.ofSeconds(1))
                .filter(error -> error instanceof CosmosException && ((CosmosException) error).getStatusCode() == 404)
                .doBeforeRetry(ignored -> LOGGER.info("Waiting for container {} to become visible in the reader region", container.getId()))
                .onRetryExhaustedThrow((spec, signal) -> signal.failure()))
            .block(WAIT).getProperties();
    }

    private static String combine(Map<String, ISessionToken> tokens) {
        return tokens.entrySet().stream()
            .map(entry -> entry.getKey() + ":" + entry.getValue().convertToString()).collect(Collectors.joining(","));
    }

    private static RxDocumentClientImpl internal(CosmosAsyncClient client) {
        return (RxDocumentClientImpl) CosmosBridgeInternal.getAsyncDocumentClient(client);
    }

    private static RxPartitionKeyRangeCache cache(CosmosAsyncClient client) {
        return internal(client).getPartitionKeyRangeCache();
    }

    private static void awaitThinClient(CosmosAsyncClient client) {
        if (Configs.isThinClientEnabled() == null) {
            Flux.interval(Duration.ZERO, Duration.ofMillis(100))
                .filter(ignored -> Boolean.TRUE.equals(internal(client).getGlobalEndpointManager().getProxyProbeDecision()))
                .next().block(WAIT);
        }
        assertThat(internal(client).getGlobalEndpointManager().getLatestDatabaseAccount().getThinClientReadableLocations())
            .isNotEmpty();
    }

    @AfterClass(groups = "thinclientSplit", alwaysRun = true, timeOut = 120000)
    public void cleanup() {
        try {
            if (database != null) {
                TestSuiteBase.safeDeleteDatabase(database);
                assertThatThrownBy(() -> database.read().block(WAIT))
                    .isInstanceOfSatisfying(CosmosException.class, error -> assertThat(error.getStatusCode()).isEqualTo(404));
            }
        } finally {
            try {
                if (observer != null) {
                    observer.close();
                }
            } finally {
                try {
                    if (negativeReader != null) {
                        negativeReader.close();
                    }
                } finally {
                    if (staleReader != null) {
                        staleReader.close();
                    }
                }
            }
        }
    }

    private static final class RequestRecorder implements AutoCloseable {
        private final Map<RxStoreModel, HttpClient> originals = new LinkedHashMap<>();
        private final List<ImmutablePair<URI, String>> requests = new CopyOnWriteArrayList<>();

        private RequestRecorder(CosmosAsyncClient client) {
            RxDocumentClientImpl internal = internal(client);
            for (RxStoreModel store : new RxStoreModel[] {
                ReflectionUtils.getGatewayProxy(internal), ReflectionUtils.getThinProxy(internal)}) {
                HttpClient delegate = ReflectionUtils.getHttpClient(store);
                originals.put(store, delegate);
                ReflectionUtils.setGatewayHttpClient(store, new HttpClient() {
                    @Override
                    public Mono<HttpResponse> send(HttpRequest request) {
                        return observe(request).flatMap(delegate::send);
                    }

                    @Override
                    public Mono<HttpResponse> send(HttpRequest request, Duration timeout) {
                        return observe(request).flatMap(observed -> delegate.send(observed, timeout));
                    }

                    @Override
                    public void shutdown() {
                        delegate.shutdown();
                    }
                });
            }
        }

        private Mono<HttpRequest> observe(HttpRequest request) {
            if (!request.isThinClientRequest()) {
                if (request.uri().getPath().contains("/docs/")) {
                    requests.add(new ImmutablePair<>(request.uri(), request.headers().value(HttpConstants.HttpHeaders.SESSION_TOKEN)));
                }
                return Mono.just(request);
            }
            return request.body().collectList().map(chunks -> {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                for (byte[] chunk : chunks) {
                    bytes.write(chunk, 0, chunk.length);
                }
                byte[] body = bytes.toByteArray();
                byte[] token = ThinClientRangeHeadersTest.decodeTokens(body).get(RntbdRequestHeader.SessionToken.id());
                requests.add(new ImmutablePair<>(request.uri(),
                    token == null ? null : new String(token, StandardCharsets.UTF_8)));
                return request.withBody(Flux.just(body));
            });
        }

        private void clear() {
            requests.clear();
        }

        @Override
        public void close() {
            originals.forEach(ReflectionUtils::setGatewayHttpClient);
        }
    }
}
