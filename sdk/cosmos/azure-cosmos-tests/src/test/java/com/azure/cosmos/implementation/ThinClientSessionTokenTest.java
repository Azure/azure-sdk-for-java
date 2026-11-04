// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfigBuilder;
import com.azure.cosmos.ThresholdBasedAvailabilityStrategy;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.caches.RxClientCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.directconnectivity.GatewayServiceConfigurationReader;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants.RntbdRequestHeader;
import com.azure.cosmos.implementation.directconnectivity.rntbd.ThinClientRangeHeadersTest;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import com.azure.cosmos.implementation.perPartitionCircuitBreaker.GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKind;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.net.URI;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class ThinClientSessionTokenTest {
    private static final String ORIGINAL = "0:1#100#1=90#2=-1,1:1#200#1=180#2=150";
    private static final String RID = "ccZ1ANCszwk=";

    @DataProvider
    public Object[][] hedges() {
        return new Object[][] {{false, ORIGINAL}, {true, ORIGINAL},
            {false, "0:1#100,unknown:1#200"}, {true, "0:1#100,unknown:1#200"}};
    }

    @Test(groups = "unit", dataProvider = "hedges")
    public void actualAvailabilityStrategiesPreserveBothAttempts(boolean feed, String original) throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.delayFirstResponse = true;
            RxDocumentClientImpl client = Mockito.mock(RxDocumentClientImpl.class, Mockito.CALLS_REAL_METHODS);
            setField(client, "globalEndpointManager", fixture.endpointManager);
            setField(client, "globalPartitionEndpointManagerForPerPartitionCircuitBreaker",
                Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.class));
            CosmosEndToEndOperationLatencyPolicyConfig policy =
                new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(5))
                    .availabilityStrategy(new ThresholdBasedAvailabilityStrategy(
                        Duration.ofMillis(10), Duration.ofMillis(10))).build();
            List<RxDocumentServiceRequest> attempts = new CopyOnWriteArrayList<>();
            if (feed) {
                RxDocumentServiceRequest parent = fixture.request(OperationType.Query, original, "0");
                parent.requestContext.setEndToEndOperationLatencyPolicyConfig(policy);
                client.executeFeedOperationWithAvailabilityStrategy(
                    ResourceType.Document, OperationType.Query, () -> Mockito.mock(DocumentClientRetryPolicy.class),
                    parent, (retry, request) -> Mono.defer(() -> {
                        attempts.add(request);
                        return fixture.thin.processMessage(request);
                    }), "/dbs/db/colls/coll").block(Duration.ofSeconds(5));
                assertThat(parent.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(original);
                assertThat(parent.useGatewayMode).isFalse();
            } else {
                RequestOptions options = new RequestOptions();
                options.setSessionToken(original);
                options.setCosmosEndToEndLatencyPolicyConfig(policy);
                client.wrapPointOperationWithAvailabilityStrategy(
                    ResourceType.Document, OperationType.Read,
                    (copy, config, context, availability) -> Mono.defer(() -> {
                        RxDocumentServiceRequest request = fixture.request(
                            OperationType.Read, copy.getSessionToken(), "0");
                        request.requestContext.setCrossRegionAvailabilityContext(availability);
                        request.requestContext.setExcludeRegions(copy.getExcludedRegions());
                        attempts.add(request);
                        return fixture.thin.processMessage(request)
                            .map(response -> new ResourceResponse<>(response, Document.class));
                    }), options, false, fixture.context, "/dbs/db/colls/coll").block(Duration.ofSeconds(5));
                assertThat(options.getSessionToken()).isEqualTo(original);
                assertThat(options.getExcludedRegions()).isNullOrEmpty();
            }

            assertThat(attempts).hasSize(2);
            assertThat(attempts.get(0)).isNotSameAs(attempts.get(1));
            for (RxDocumentServiceRequest attempt : attempts) {
                assertThat(attempt.getOriginalSessionToken()).isEqualTo(original);
                assertThat(attempt.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(original);
                assertThat(attempt.useGatewayMode).isFalse();
            }
            String expected = original.equals(ORIGINAL) ? "0:1#100#1=90#2=-1" : original;
            assertThat(fixture.wireTokens).containsExactly(expected, expected);
            assertThat(fixture.endpoints).extracting(URI::getHost).containsExactly("localhost", "secondary");
            assertThat(fixture.endpoints).allMatch(uri -> uri.getPort() == (original.equals(ORIGINAL) ? 10250 : 443));
        }
    }

    private static void setField(RxDocumentClientImpl client, String name, Object value) throws Exception {
        Field field = RxDocumentClientImpl.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(client, value);
    }

    @DataProvider
    public Object[][] operations() {
        return new Object[][] {{OperationType.Read}, {OperationType.Query}, {OperationType.ReadFeed},
            {OperationType.Batch}};
    }

    @Test(groups = "unit", dataProvider = "operations")
    public void explicitCompoundIsScopedOnTheWireOnly(OperationType operation) {
        try (Fixture fixture = new Fixture()) {
            RxDocumentServiceRequest request = fixture.request(operation, ORIGINAL, "1");
            fixture.thin.processMessage(request).block(Duration.ofSeconds(5));
            assertThat(fixture.wireTokens).containsExactly("1:1#200#1=180#2=150");
            assertThat(fixture.endpoints).allMatch(uri -> uri.getPort() == 10250);
            assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(ORIGINAL);
            assertThat(request.getOriginalSessionToken()).isEqualTo(ORIGINAL);
            verify(fixture.sessions, never()).resolveGlobalSessionToken(any());
            verify(fixture.sessions, never()).resolvePartitionLocalSessionToken(any(), any());
            verify(fixture.sessions).setSessionToken(any(RxDocumentServiceRequest.class), any(Map.class));
        }
    }

    @Test(groups = "unit")
    public void retriesAndClonesAlwaysUseOriginalProgress() {
        try (Fixture fixture = new Fixture()) {
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, ORIGINAL, "0");
            Mono<RxDocumentServiceResponse> operation = fixture.thin.processMessage(request);
            operation.block(Duration.ofSeconds(5));
            request.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity(RID, "1"));
            operation.block(Duration.ofSeconds(5));
            RxDocumentServiceRequest clone = request.clone();
            clone.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity(RID, "0"));
            fixture.thin.processMessage(clone).block(Duration.ofSeconds(5));

            fixture.map = SessionTokenHelperTests.routingMap(
                new PartitionKeyRange("2", "", "80", Collections.singletonList("0")),
                new PartitionKeyRange("1", "80", "FF"));
            request.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity(RID, "2"));
            operation.block(Duration.ofSeconds(5));
            assertThat(fixture.wireTokens).containsExactly(
                "0:1#100#1=90#2=-1", "1:1#200#1=180#2=150",
                "0:1#100#1=90#2=-1", "2:1#100#1=90#2=-1");
            assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(ORIGINAL);
            assertThat(clone.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(ORIGINAL);
        }
    }

    @DataProvider
    public Object[][] fallbackTokens() {
        return new Object[][] {{"0:1#100,unknown:1#200"}, {"0:1#100,1:future"},
            {"0:1#100,0:1#200"}, {"1:1#100,1:1#200"}, {"0:1#100,"}};
    }

    @Test(groups = "unit", dataProvider = "fallbackTokens")
    public void unsafeTokenUsesClassicWithOriginalHeader(String token) {
        try (Fixture fixture = new Fixture()) {
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, token, "0");
            fixture.thin.processMessage(request).block(Duration.ofSeconds(5));
            assertThat(fixture.wireTokens).containsExactly(token);
            assertThat(fixture.endpoints).allMatch(uri -> uri.getPort() == 443);
            assertThat(request.useThinClientMode).isFalse();
            assertThat(request.useGatewayMode).isFalse();
        }
    }

    @Test(groups = "unit")
    public void fallbackDoesNotStickAcrossAttempts() {
        try (Fixture fixture = new Fixture()) {
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, ORIGINAL, "0");
            CollectionRoutingMap map = fixture.map;
            fixture.map = null;
            fixture.thin.processMessage(request).block(Duration.ofSeconds(5));
            fixture.map = map;
            fixture.thin.processMessage(request).block(Duration.ofSeconds(5));
            fixture.map = null;
            fixture.thin.processMessage(request).block(Duration.ofSeconds(5));
            assertThat(fixture.wireTokens).containsExactly(ORIGINAL, "0:1#100#1=90#2=-1", ORIGINAL);
            assertThat(fixture.endpoints).extracting(URI::getPort).containsExactly(443, 10250, 443);
        }
    }

    @Test(groups = "unit")
    public void waitsForNormalMetadataResolutionAndPropagatesFailures() {
        try (Fixture fixture = new Fixture()) {
            Sinks.One<Utils.ValueHolder<CollectionRoutingMap>> loading = Sinks.one();
            Mockito.when(fixture.ranges.tryLookupAsync(any(), any(), any(), any())).thenReturn(loading.asMono());
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, ORIGINAL, "0");
            List<Throwable> errors = new ArrayList<>();
            Disposable subscription = fixture.thin.processMessage(request).subscribe(ignored -> { }, errors::add);
            assertThat(fixture.wireTokens).isEmpty();
            loading.tryEmitValue(new Utils.ValueHolder<>(fixture.map));
            assertThat(errors).isEmpty();
            assertThat(fixture.wireTokens).containsExactly("0:1#100#1=90#2=-1");
            subscription.dispose();

            RuntimeException failure = new IllegalStateException("metadata failure");
            Mockito.when(fixture.ranges.tryLookupAsync(any(), any(), any(), any())).thenReturn(Mono.error(failure));
            assertThatThrownBy(() -> fixture.thin.processMessage(request).block(Duration.ofSeconds(5)))
                .isSameAs(failure);
            assertThat(fixture.wireTokens).hasSize(1);
        }
    }

    @Test(groups = "unit")
    public void cancelledMetadataDoesNotSendOrChangeSiblingProgress() {
        try (Fixture fixture = new Fixture()) {
            Sinks.One<Utils.ValueHolder<CollectionRoutingMap>> loading = Sinks.one();
            Mockito.when(fixture.ranges.tryLookupAsync(any(), any(), any(), any())).thenReturn(loading.asMono());
            RxDocumentServiceRequest original = fixture.request(OperationType.Query, ORIGINAL, "0");
            RxDocumentServiceRequest sibling = original.clone();
            sibling.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity(RID, "1"));
            Disposable cancelled = fixture.thin.processMessage(original).subscribe();
            cancelled.dispose();
            List<Throwable> errors = new ArrayList<>();
            Disposable active = fixture.thin.processMessage(sibling).subscribe(ignored -> { }, errors::add);
            loading.tryEmitValue(new Utils.ValueHolder<>(fixture.map));
            assertThat(errors).isEmpty();
            assertThat(fixture.wireTokens).containsExactly("1:1#200#1=180#2=150");
            assertThat(original.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(ORIGINAL);
            active.dispose();
        }
    }

    @Test(groups = "unit")
    public void fullLogicalKeyUsesResolvedMetadata() {
        try (Fixture fixture = new Fixture()) {
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, ORIGINAL, null);
            request.setPartitionKeyInternal(PartitionKeyInternal.fromObjectArray(new Object[] {"customer"}, true));
            fixture.thin.processMessage(request).block(Duration.ofSeconds(5));
            assertThat(fixture.wireTokens).containsExactly("0:1#100#1=90#2=-1");
            assertThat(fixture.endpoints).extracting(URI::getPort).containsExactly(10250);
        }
    }

    @Test(groups = "unit")
    public void exactFeedScopeIsPreservedAndMultiRangeScopeFallsBack() {
        try (Fixture fixture = new Fixture()) {
            RxDocumentServiceRequest request = fixture.request(OperationType.Query, ORIGINAL, null);
            request.getHeaders().put(HttpConstants.HttpHeaders.START_EPK, "10");
            request.getHeaders().put(HttpConstants.HttpHeaders.END_EPK, "20");
            request.getHeaders().put(HttpConstants.HttpHeaders.READ_FEED_KEY_TYPE,
                ReadFeedKeyType.EffectivePartitionKeyRange.name());
            fixture.thin.processMessage(request).block(Duration.ofSeconds(5));
            assertThat(fixture.encodedTokens.get(0).get(RntbdRequestHeader.StartEpk.id()))
                .containsExactly("10".getBytes(StandardCharsets.UTF_8));
            assertThat(fixture.encodedTokens.get(0).get(RntbdRequestHeader.EndEpk.id()))
                .containsExactly("20".getBytes(StandardCharsets.UTF_8));
            request.getHeaders().put(HttpConstants.HttpHeaders.END_EPK, "FF");
            fixture.thin.processMessage(request).block(Duration.ofSeconds(5));
            assertThat(fixture.wireTokens).containsExactly("0:1#100#1=90#2=-1", ORIGINAL);
            assertThat(fixture.sentRequests.get(1).headers().value(HttpConstants.HttpHeaders.START_EPK))
                .isEqualTo("10");
            assertThat(fixture.sentRequests.get(1).headers().value(HttpConstants.HttpHeaders.END_EPK))
                .isEqualTo("FF");
        }
    }

    @Test(groups = "unit")
    public void partialHierarchicalKeyAndConflictingIdentityFallBack() {
        try (Fixture fixture = new Fixture()) {
            fixture.definition.setKind(PartitionKind.MULTI_HASH);
            fixture.definition.setPaths(Arrays.asList("/tenant", "/user"));
            RxDocumentServiceRequest request = fixture.request(OperationType.Query, ORIGINAL, null);
            request.setPartitionKeyInternal(PartitionKeyInternal.fromObjectArray(new Object[] {"tenant"}, true));
            fixture.thin.processMessage(request).block(Duration.ofSeconds(5));
            RxDocumentServiceRequest conflicting = fixture.request(OperationType.Read, ORIGINAL, "0");
            conflicting.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity("other-collection", "0"));
            fixture.thin.processMessage(conflicting).block(Duration.ofSeconds(5));
            assertThat(fixture.wireTokens).containsExactly(ORIGINAL, ORIGINAL);
            assertThat(fixture.endpoints).allMatch(uri -> uri.getPort() == 443);
        }
    }

    @Test(groups = "unit")
    public void singleTokenAndClassicCompoundPassThroughWithoutRangeLookup() {
        try (Fixture fixture = new Fixture()) {
            RxDocumentServiceRequest single = fixture.request(OperationType.Read, "1:1#200", "1");
            fixture.thin.processMessage(single).block(Duration.ofSeconds(5));
            fixture.classic.processMessage(fixture.request(OperationType.Read, ORIGINAL, "0"))
                .block(Duration.ofSeconds(5));
            assertThat(fixture.wireTokens).containsExactly("1:1#200", ORIGINAL);
            verify(fixture.ranges, never()).tryLookupAsync(any(), any(), any(), any());
        }
    }

    @Test(groups = "unit")
    public void nonSessionConsistencyDoesNotResolveOrSendExplicitProgress() {
        try (Fixture fixture = new Fixture()) {
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, ORIGINAL, "0");
            request.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, "Eventual");
            fixture.thin.processMessage(request).block(Duration.ofSeconds(5));
            assertThat(fixture.wireTokens).containsExactly((String) null);
            verify(fixture.ranges, never()).tryLookupAsync(any(), any(), any(), any());
        }
    }

    private static final class Fixture implements AutoCloseable {
        final DiagnosticsClientContext context = mockDiagnosticsClientContext();
        final ISessionContainer sessions = Mockito.mock(ISessionContainer.class);
        final RxClientCollectionCache collections = Mockito.mock(RxClientCollectionCache.class);
        final RxPartitionKeyRangeCache ranges = Mockito.mock(RxPartitionKeyRangeCache.class);
        final GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        final PartitionKeyDefinition definition = new PartitionKeyDefinition();
        final ThinClientStoreModel thin;
        final RxGatewayStoreModel classic;
        final List<String> wireTokens = new CopyOnWriteArrayList<>();
        final List<URI> endpoints = new CopyOnWriteArrayList<>();
        final List<HttpRequest> sentRequests = new CopyOnWriteArrayList<>();
        final List<Map<Short, byte[]>> encodedTokens = new CopyOnWriteArrayList<>();
        CollectionRoutingMap map = SessionTokenHelperTests.routingMap(
            new PartitionKeyRange("0", "", "80"), new PartitionKeyRange("1", "80", "FF"));
        boolean delayFirstResponse;

        Fixture() {
            definition.setPaths(Collections.singletonList("/pk"));
            DocumentCollection collection = new DocumentCollection();
            collection.setResourceId(RID);
            collection.setPartitionKey(definition);
            Mockito.when(collections.resolveCollectionAsync(any(), any())).thenAnswer(invocation -> {
                RxDocumentServiceRequest request = invocation.getArgument(1);
                request.requestContext.resolvedCollectionRid = RID;
                request.setResourceId(RID);
                return Mono.just(new Utils.ValueHolder<>(collection));
            });
            Mockito.when(ranges.tryLookupAsync(any(), any(), any(), any()))
                .thenAnswer(invocation -> Mono.just(new Utils.ValueHolder<>(map)));
            GatewayServiceConfigurationReader configuration = Mockito.mock(GatewayServiceConfigurationReader.class);
            Mockito.when(configuration.getDefaultConsistencyLevel()).thenReturn(ConsistencyLevel.SESSION);
            RegionalRoutingContext region = new RegionalRoutingContext(URI.create("https://localhost:443"));
            region.setThinclientRegionalEndpoint(URI.create("https://localhost:10250"));
            RegionalRoutingContext secondary = new RegionalRoutingContext(URI.create("https://secondary:443"));
            secondary.setThinclientRegionalEndpoint(URI.create("https://secondary:10250"));
            Mockito.when(endpointManager.getApplicableReadRegionalRoutingContexts(Mockito.<List<String>>any()))
                .thenReturn(new UnmodifiableList<>(Arrays.asList(region, secondary)));
            Mockito.when(endpointManager.getRegionName(any(), any())).thenAnswer(invocation ->
                ((URI) invocation.getArgument(0)).getHost().equals("localhost") ? "East US" : "West US");
            Mockito.when(endpointManager.resolveServiceEndpoint(any())).thenAnswer(invocation -> {
                RxDocumentServiceRequest request = invocation.getArgument(0);
                List<String> excluded = request.requestContext.getExcludeRegions();
                return excluded != null && excluded.contains("East US") ? secondary : region;
            });
            DatabaseAccount account = Mockito.mock(DatabaseAccount.class);
            Mockito.when(account.getId()).thenReturn("test-account");
            Mockito.when(endpointManager.getLatestDatabaseAccount()).thenReturn(account);
            HttpClient http = Mockito.mock(HttpClient.class);
            Mockito.when(http.send(any(), any())).thenAnswer(invocation -> {
                HttpRequest request = invocation.getArgument(0);
                Flux<byte[]> body = request.body() == null ? Flux.empty() : request.body();
                return body.singleOrEmpty().defaultIfEmpty(new byte[0]).map(bytes -> {
                    sentRequests.add(request);
                    this.endpoints.add(request.uri());
                    if (request.uri().getPort() == 10250) {
                        Map<Short, byte[]> tokens = ThinClientRangeHeadersTest.decodeTokens(bytes);
                        encodedTokens.add(tokens);
                        byte[] token = tokens.get(RntbdRequestHeader.SessionToken.id());
                        wireTokens.add(token == null ? null : new String(token, StandardCharsets.UTF_8));
                    } else {
                        wireTokens.add(request.headers().value(HttpConstants.HttpHeaders.SESSION_TOKEN));
                    }
                    return new HttpResponse() {
                        @Override public int statusCode() { return 200; }
                        @Override public String headerValue(String name) { return null; }
                        @Override public HttpHeaders headers() { return new HttpHeaders(); }
                        @Override public Mono<ByteBuf> body() { return Mono.just(Unpooled.EMPTY_BUFFER); }
                        @Override public Mono<String> bodyAsString() { return Mono.just(""); }
                        @Override public void close() { }
                    }.withRequest(request);
                }).flatMap(response -> delayFirstResponse && this.endpoints.size() == 1
                    ? Mono.never() : Mono.just(response));
            });
            thin = new ThinClientStoreModel(context, sessions, ConsistencyLevel.SESSION,
                new UserAgentContainer(), endpointManager, http, null);
            classic = new RxGatewayStoreModel(context, sessions, ConsistencyLevel.SESSION,
                QueryCompatibilityMode.Default, new UserAgentContainer(), endpointManager, http, ApiType.SQL, null);
            for (RxGatewayStoreModel store : Arrays.asList(thin, classic)) {
                store.setGatewayServiceConfigurationReader(configuration);
                store.setCollectionCache(collections);
                store.setPartitionKeyRangeCache(ranges);
            }
            thin.setGatewayStoreModel(classic);
        }

        RxDocumentServiceRequest request(OperationType operation, String token, String rangeId) {
            RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(
                context, operation, "/dbs/db/colls/coll/docs/item", ResourceType.Document);
            request.setResourceId(RID);
            request.requestContext.resolvedCollectionRid = RID;
            request.requestContext.cosmosDiagnostics = context.createDiagnostics();
            request.setPartitionKeyDefinition(definition);
            request.getHeaders().put(HttpConstants.HttpHeaders.SESSION_TOKEN, token);
            if (rangeId != null) {
                request.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity(RID, rangeId));
                request.requestContext.resolvedPartitionKeyRange = map.tryGetRangeByPartitionKeyRangeId(rangeId);
            }
            return request;
        }

        @Override
        public void close() {
            thin.close();
            classic.close();
        }
    }
}
