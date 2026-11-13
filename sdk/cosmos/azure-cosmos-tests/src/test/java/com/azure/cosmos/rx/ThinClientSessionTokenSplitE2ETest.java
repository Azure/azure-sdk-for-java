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
import com.azure.cosmos.SplitTimeoutException;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ISessionToken;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxStoreModel;
import com.azure.cosmos.implementation.SessionTokenHelper;
import com.azure.cosmos.implementation.ThinClientStoreModel;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants.RntbdRequestHeader;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequest;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.ThroughputProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
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
    private String accountEndpoint;
    private String databaseId;
    private CosmosAsyncContainer staleContainer;
    private CosmosAsyncContainer currentContainer;
    private String collectionRid;
    private PartitionKeyDefinition partitionKeyDefinition;
    private CollectionRoutingMap beforeSplit;

    @Factory(dataProvider = "clientBuildersWithGatewayAndHttp2", dataProviderClass = TestSuiteBase.class)
    public ThinClientSessionTokenSplitE2ETest(CosmosClientBuilder builder) {
        this.builder = builder.consistencyLevel(ConsistencyLevel.SESSION)
            .sessionRetryOptions(new SessionRetryOptionsBuilder()
                .regionSwitchHint(CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED)
                .maxRetriesPerRegion(1)
                .minTimeoutPerRegion(Duration.ofMillis(100))
                .build());
    }

    @Test(groups = "thinclientEndpointProbe", timeOut = 1200000)
    public void staleReaderRefreshesForChildCompoundTokenAfterRealSplit() throws Exception {
        assertThat(Configs.isThinClientEnabled()).as("Endpoint-probe coverage must not force Gateway V2 on or off").isNull();
        try (CosmosAsyncClient staleReader = builder.buildAsyncClient()) {
            accountEndpoint = internal(staleReader).getServiceEndpoint().toString();
            awaitThinClient(staleReader);
            DatabaseAccount account = internal(staleReader).getGlobalEndpointManager().getLatestDatabaseAccount();
            List<String> regions = account.getThinClientReadableLocations().stream()
                .map(location -> location.getName()).collect(Collectors.toList());
            assertThat(regions).as("The account must advertise a Gateway V2 region").isNotEmpty();
            String region = regions.get(0);
            String regionalEndpoint = StreamSupport.stream(account.getReadableLocations().spliterator(), false)
                .filter(location -> region.equals(location.getName()))
                .findFirst().orElseThrow(() -> new IllegalStateException("No classic endpoint for " + region))
                .getEndpoint();
            assertThat(URI.create(regionalEndpoint).getHost())
                .as("Use a global ACCOUNT_HOST so the observer's regional endpoint has a separate cache")
                .isNotEqualTo(internal(staleReader).getServiceEndpoint().getHost());
            try (CosmosAsyncClient observer = builder.endpoint(regionalEndpoint)
                     .preferredRegions(Collections.singletonList(region)).buildAsyncClient()) {
                setup(staleReader, observer);
                verifySplit(staleReader, observer);
            }
        }
    }

    private void setup(CosmosAsyncClient staleReader, CosmosAsyncClient observer) {
        awaitThinClient(observer);
        assertThat(ReflectionUtils.getRoutingMapAsyncCacheNonBlocking(cache(staleReader)))
            .isNotSameAs(ReflectionUtils.getRoutingMapAsyncCacheNonBlocking(cache(observer)));
        CosmosAsyncDatabase database = TestSuiteBase.createTestDatabase(staleReader, "compound-session-split");
        databaseId = database.getId();
        CosmosContainerProperties properties = new CosmosContainerProperties("items", "/pk");
        staleContainer = TestSuiteBase.createCollection(
            database, properties, new CosmosContainerRequestOptions(), 400, observer);
        currentContainer = observer.getDatabase(database.getId()).getContainer("items");
        CosmosContainerProperties actual = staleContainer.read().block(WAIT).getProperties();
        collectionRid = actual.getResourceId();
        partitionKeyDefinition = actual.getPartitionKeyDefinition();
        assertThat(awaitContainer(currentContainer).getResourceId()).isEqualTo(collectionRid);
        beforeSplit = routingMap(staleReader, null);
        assertThat(beforeSplit.getOrderedPartitionKeyRanges()).hasSize(1);
        String parentId = beforeSplit.getOrderedPartitionKeyRanges().get(0).getId();
        ObjectNode item = itemInRange(beforeSplit, parentId, "before-split");
        CosmosItemResponse<ObjectNode> created = currentContainer.createItem(item).block(WAIT);
        TestSuiteBase.assertThinClientEndpointUsed(created.getDiagnostics());
        CosmosItemResponse<ObjectNode> read = staleContainer.readItem(
            item.get("id").asText(), new PartitionKey(item.get("pk").asText()), ObjectNode.class).block(WAIT);
        TestSuiteBase.assertThinClientEndpointUsed(read.getDiagnostics());
        assertThat(routingMap(staleReader, null)).isSameAs(beforeSplit);
    }

    private void verifySplit(CosmosAsyncClient staleReader, CosmosAsyncClient observer) throws InterruptedException {
        TestSuiteBase.executeControlPlaneWithRetry(
            () -> currentContainer.replaceThroughput(ThroughputProperties.createManualThroughput(10100)).block(WAIT));
        CollectionRoutingMap afterSplit = awaitSplit(observer);
        PartitionKeyRange parent = beforeSplit.getOrderedPartitionKeyRanges().get(0);
        String parentId = parent.getId();
        List<PartitionKeyRange> children = afterSplit.getOrderedPartitionKeyRanges();
        assertThat(children).hasSize(2).allSatisfy(child -> assertThat(child.getParents()).contains(parentId));
        assertThat(children.get(0).getMinInclusive()).isEqualTo(parent.getMinInclusive());
        assertThat(children.get(0).getMaxExclusive()).isEqualTo(children.get(1).getMinInclusive());
        assertThat(children.get(1).getMaxExclusive()).isEqualTo(parent.getMaxExclusive());
        assertThat(afterSplit.tryGetRangeByPartitionKeyRangeId(parentId)).isNull();

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
        PartitionKeyRange child = children.get(0);
        ObjectNode item = childItems.get(child.getId());
        assertStale(staleReader, beforeSplit, parentId, child.getId());

        try (RequestRecorder recorder = new RequestRecorder(staleReader)) {
            CosmosItemRequestOptions options = new CosmosItemRequestOptions().setSessionToken(childCompound)
                .setCosmosEndToEndOperationLatencyPolicyConfig(
                    new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(20)).build());
            CosmosItemResponse<ObjectNode> response = staleContainer.readItem(
                item.get("id").asText(), new PartitionKey(item.get("pk").asText()), options, ObjectNode.class).block(WAIT);
            LOGGER.info("Stale-reader request order: {}", recorder.requestOrder);
            assertThat(recorder.requestOrder)
                .as("Only the second logical lookup should fetch metadata before the document request")
                .startsWith("lookup(false)", "lookup(true)", "pkranges").contains("document");
            assertThat(response.getItem().get("id")).isEqualTo(item.get("id"));
            assertThat(response.getItem().get("pk")).isEqualTo(item.get("pk"));
            assertThat(response.getItem().get("version")).isEqualTo(item.get("version"));
            TestSuiteBase.assertThinClientEndpointUsed(response.getDiagnostics());
            assertThat(response.getDiagnostics().toString()).doesNotContain("could not be evaluated");
            assertThat(options.getSessionToken()).isEqualTo(childCompound);
            assertThat(routingMap(staleReader, null).tryGetRangeByPartitionKeyRangeId(child.getId())).isNotNull();
            String expected = child.getId() + ":" + childProgress.get(child.getId()).convertToString();
            assertThat(recorder.requests).isNotEmpty().allSatisfy(attempt -> {
                assertThat(attempt.getRight()).isEqualTo(expected);
                assertThat(attempt.getLeft().getScheme()).isEqualTo("https");
                assertThat(attempt.getLeft().getPort()).isEqualTo(10250);
            });
        }
    }

    private CollectionRoutingMap awaitSplit(CosmosAsyncClient observer) throws InterruptedException {
        long deadlineNanos = System.nanoTime() + SPLIT_WAIT.toNanos();
        int initialPartitionCount = beforeSplit.getOrderedPartitionKeyRanges().size();
        CollectionRoutingMap latest = routingMap(observer, null);
        while (latest.getOrderedPartitionKeyRanges().size() <= initialPartitionCount
            && System.nanoTime() < deadlineNanos) {
            Thread.sleep(SPLIT_POLL_INTERVAL.toMillis());
            latest = routingMap(observer, latest);
            LOGGER.info(
                "Waiting for real split: initialRangeCount={}, currentRanges={}",
                initialPartitionCount,
                latest.getOrderedPartitionKeyRanges());
        }

        if (latest.getOrderedPartitionKeyRanges().size() <= initialPartitionCount) {
            throw new SplitTimeoutException(String.format(
                "Partition count for container '%s' did not increase beyond %d within %s; latest ranges: %s",
                currentContainer.getId(),
                initialPartitionCount,
                SPLIT_WAIT,
                latest.getOrderedPartitionKeyRanges()));
        }

        LOGGER.info("Observed real split: before={}, after={}",
            beforeSplit.getOrderedPartitionKeyRanges(), latest.getOrderedPartitionKeyRanges());
        return latest;
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

    @AfterClass(groups = "thinclientEndpointProbe", alwaysRun = true, timeOut = 240000)
    public void cleanup() {
        if (databaseId != null) {
            try (CosmosAsyncClient cleanupClient = builder.endpoint(accountEndpoint).buildAsyncClient()) {
                CosmosAsyncDatabase database = cleanupClient.getDatabase(databaseId);
                TestSuiteBase.safeDeleteDatabase(database);
                assertThatThrownBy(() -> database.read()
                    .retryWhen(Retry.fixedDelay(5, Duration.ofSeconds(2))
                        .filter(error -> error instanceof CosmosException
                            && ((CosmosException) error).getStatusCode() == 429)
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()))
                    .block(Duration.ofSeconds(120)))
                    .isInstanceOfSatisfying(CosmosException.class, error -> assertThat(error.getStatusCode()).isEqualTo(404));
            }
        }
    }

    private static final class RequestRecorder implements AutoCloseable {
        private final Map<RxStoreModel, HttpClient> originals = new LinkedHashMap<>();
        private final List<ImmutablePair<URI, String>> requests = new CopyOnWriteArrayList<>();
        private final List<String> requestOrder = new CopyOnWriteArrayList<>();
        private final ThinClientStoreModel thinClient;
        private final RxPartitionKeyRangeCache originalCache;

        private RequestRecorder(CosmosAsyncClient client) {
            RxDocumentClientImpl internal = internal(client);
            thinClient = (ThinClientStoreModel) ReflectionUtils.getThinProxy(internal);
            originalCache = internal.getPartitionKeyRangeCache();
            RxPartitionKeyRangeCache cacheSpy = Mockito.spy(originalCache);
            Mockito.doAnswer(invocation -> {
                boolean forceRefresh = invocation.getArgument(3);
                return Mono.defer(() -> {
                    requestOrder.add("lookup(" + forceRefresh + ")");
                    return originalCache.tryLookupAsync(
                        invocation.getArgument(0), invocation.getArgument(1), invocation.getArgument(2),
                        forceRefresh, invocation.getArgument(4));
                });
            }).when(cacheSpy).tryLookupAsync(
                Mockito.any(), Mockito.anyString(), Mockito.any(), Mockito.anyBoolean(), Mockito.any());
            thinClient.setPartitionKeyRangeCache(cacheSpy);
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
            if (request.uri().getPath().contains("/pkranges")) {
                requestOrder.add("pkranges");
            } else if (request.uri().getPath().contains("/docs/")) {
                requestOrder.add("document");
            }
            if (!request.isThinClientRequest()) {
                if (request.uri().getPath().contains("/docs/")) {
                    requests.add(new ImmutablePair<>(request.uri(), request.headers().value(HttpConstants.HttpHeaders.SESSION_TOKEN)));
                }
                return Mono.just(request);
            }
            return request.body().single().map(body -> {
                ByteBuf buffer = Unpooled.wrappedBuffer(body);
                try {
                    buffer.writerIndex(buffer.getIntLE(0));
                    RntbdRequest decoded = RntbdRequest.decode(buffer);
                    requests.add(new ImmutablePair<>(request.uri(), decoded.getHeader(RntbdRequestHeader.SessionToken)));
                } finally {
                    buffer.release();
                }
                return request.withBody(Flux.just(body));
            });
        }

        @Override
        public void close() {
            thinClient.setPartitionKeyRangeCache(originalCache);
            originals.forEach(ReflectionUtils::setGatewayHttpClient);
        }
    }
}
