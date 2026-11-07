// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfigBuilder;
import com.azure.cosmos.ThresholdBasedAvailabilityStrategy;
import com.azure.cosmos.ThrottlingRetryOptions;
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
import com.azure.cosmos.implementation.faultinjection.FaultInjectionRequestArgs;
import com.azure.cosmos.implementation.faultinjection.IFaultInjectorProvider;
import com.azure.cosmos.implementation.faultinjection.IServerErrorInjector;
import com.azure.cosmos.implementation.perPartitionAutomaticFailover.GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover;
import com.azure.cosmos.implementation.perPartitionCircuitBreaker.GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import com.azure.cosmos.implementation.spark.OperationContext;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.implementation.spark.OperationListener;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKind;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.CosmosPatchOperations;
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
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class ThinClientSessionTokenTest {
    private static final String ORIGINAL = "0:1#100#1=90#2=-1,1:1#200#1=180#2=150";
    private static final String RID = "ccZ1ANCszwk=";

    @DataProvider
    public Object[][] pointReadSessionTokenScopes() {
        return new Object[][] {
            {"parent-token-child-metadata", "1:1#100,0:1#900", true, "2:1#100"},
            {"child-token-child-metadata", "2:1#200,0:1#900", true, "2:1#200"},
            {"child-token-parent-metadata", "2:1#200,0:1#100", false, "2:1#200"},
            {"parent-and-child-token-parent-metadata", "1:1#100,2:1#200", false, "2:1#200"}
        };
    }

    @Test(groups = "unit", dataProvider = "pointReadSessionTokenScopes")
    public void pointReadScopesCompoundSessionTokenAcrossSplits(
        String scenario, String original, boolean metadataHasChildren, String expectedScopedToken) throws Exception {
        try (Fixture fixture = new Fixture()) {
            PartitionKeyInternal key = PartitionKeyInternal.fromObjectArray(new Object[] {"customer"}, true);
            String epk = key.getEffectivePartitionKeyString(key, fixture.definition);
            CollectionRoutingMap childMap = SessionTokenHelperTests.routingMap(
                new PartitionKeyRange("0", "", epk),
                new PartitionKeyRange("2", epk, epk + "80", Collections.singletonList("1")),
                new PartitionKeyRange("3", epk + "80", "FF", Collections.singletonList("1")));
            fixture.map = metadataHasChildren ? childMap : SessionTokenHelperTests.routingMap(
                new PartitionKeyRange("0", "", epk), new PartitionKeyRange("1", epk, "FF"));
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, original, null);
            request.setPartitionKeyInternal(key);
            request.getHeaders().put(HttpConstants.HttpHeaders.PARTITION_KEY, key.toJson());
            RxDocumentClientImpl client = fixture.client();
            GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover failover =
                Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover.class);
            Mockito.when(failover.isPerPartitionAutomaticFailoverApplicable(request)).thenReturn(true);
            setField(client, "globalPartitionEndpointManagerForPerPartitionAutomaticFailover", failover);
            setField(client, "globalPartitionEndpointManagerForPerPartitionCircuitBreaker",
                Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.class));
            RequestOptions options = new RequestOptions();
            options.setPartitionKeyDefinition(fixture.definition);
            client.setPartitionKeyRangeForPointOperationRequestForPerPartitionAutomaticFailover(
                request, options, fixture.map, null, false, null);
            assertThat(request.requestContext.resolvedPartitionKeyRange.getId()).as(scenario)
                .isEqualTo(metadataHasChildren ? "2" : "1");
            assertThat(request.getPartitionKeyRangeIdentity()).isNull();

            dispatch(client, request).block(Duration.ofSeconds(5));

            String expectedInitialToken = metadataHasChildren ? expectedScopedToken : original;
            int expectedInitialPort = metadataHasChildren ? 10250 : 443;
            assertThat(fixture.wireTokens).as(scenario).containsExactly(expectedInitialToken);
            assertThat(fixture.endpoints).extracting(URI::getPort).containsExactly(expectedInitialPort);
            assertThat(fixture.endpoints).extracting(URI::getHost).containsExactly("localhost");
            assertThat(request.useThinClientMode).isEqualTo(metadataHasChildren);
            assertThat(request.getOriginalSessionToken()).isEqualTo(original);
            assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(expectedInitialToken);
            assertThat(request.requestContext.getSessionTokenEvaluationResults()).isEmpty();
            verify(fixture.ranges).tryLookupAsync(any(), any(), any(), any());
            ISessionToken initialScopedToken = request.requestContext.sessionToken;

            fixture.map = childMap;
            client.setPartitionKeyRangeForPointOperationRequestForPerPartitionAutomaticFailover(
                request, options, fixture.map, null, false, null);
            dispatch(client, request).block(Duration.ofSeconds(5));
            assertThat(fixture.wireTokens).as(scenario).containsExactly(expectedInitialToken, expectedScopedToken);
            assertThat(fixture.endpoints).extracting(URI::getPort).containsExactly(expectedInitialPort, 10250);
            assertThat(fixture.endpoints).extracting(URI::getHost).containsExactly("localhost", "localhost");
            assertThat(request.requestContext.resolvedPartitionKeyRange.getId()).isEqualTo("2");
            assertThat(request.useThinClientMode).isTrue();
            assertThat(request.getOriginalSessionToken()).isEqualTo(original);
            assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(expectedScopedToken);
            assertThat(request.requestContext.getSessionTokenEvaluationResults()).isEmpty();
            if (metadataHasChildren) {
                assertThat(initialScopedToken).isNotNull();
                assertThat(request.requestContext.sessionToken).isSameAs(initialScopedToken);
            }
            verify(fixture.ranges, Mockito.times(metadataHasChildren ? 1 : 2))
                .tryLookupAsync(any(), any(), any(), any());
            verify(fixture.sessions, never()).resolveGlobalSessionToken(any());
            verify(fixture.sessions, never()).resolvePartitionLocalSessionToken(any(), any());
        }
    }

    @DataProvider
    public Object[][] preparedPointReads() {
        return new Object[][] {
            {ORIGINAL, "0:1#100#1=90#2=-1", 10250},
            {"1:1#200,1:1#100", null, 10250},
            {"2:1#200,0:1#100", "2:1#200,0:1#100", 443}
        };
    }

    @Test(groups = "unit", dataProvider = "preparedPointReads")
    public void readDocumentPreparesOnceBeforeStartingHedges(String original, String expected, int port) throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.delayFirstResponse = true;
            RxDocumentClientImpl client = fixture.fullClient();
            Sinks.One<Utils.ValueHolder<CollectionRoutingMap>> metadata = Sinks.one();
            Mockito.when(fixture.ranges.tryLookupAsync(any(), any(), any(), any())).thenReturn(metadata.asMono());
            List<RxDocumentServiceRequest> attempts = new CopyOnWriteArrayList<>();
            IServerErrorInjector injector = Mockito.mock(IServerErrorInjector.class);
            Mockito.when(injector.injectServerResponseError(any(), any())).thenAnswer(invocation -> {
                attempts.add(((FaultInjectionRequestArgs) invocation.getArgument(0)).getServiceRequest());
                return false;
            });
            fixture.inject(injector);
            RequestOptions options = new RequestOptions();
            options.setPartitionKey(new PartitionKey("customer"));
            options.setSessionToken(original);
            options.setCosmosEndToEndLatencyPolicyConfig(
                new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(5))
                    .availabilityStrategy(new ThresholdBasedAvailabilityStrategy(
                        Duration.ofMillis(10), Duration.ofMillis(10))).build());
            Method read = RxDocumentClientImpl.class.getDeclaredMethod(
                "readDocument", String.class, String.class, RequestOptions.class, DiagnosticsClientContext.class);
            read.setAccessible(true);
            Mono<?> operation = (Mono<?>) read.invoke(client, "/dbs/db/colls/coll/docs/item", "item", options, fixture.context);
            CompletableFuture<?> result = operation.toFuture();
            assertThat(attempts).isEmpty();
            assertThat(fixture.sentRequests).isEmpty();
            verify(fixture.collections).resolveCollectionAsync(any(), any());
            metadata.tryEmitValue(new Utils.ValueHolder<>(fixture.map));
            assertThat(result.get(5, TimeUnit.SECONDS)).isNotNull();
            assertThat(attempts).hasSize(2);
            assertThat(attempts.get(0).requestContext).isNotSameAs(attempts.get(1).requestContext);
            assertThat(attempts.get(0).requestContext.getSessionTokenState()).isNotNull()
                .isSameAs(attempts.get(1).requestContext.getSessionTokenState());
            assertThat(fixture.wireTokens).containsExactly(expected, expected);
            assertThat(fixture.endpoints).extracting(URI::getPort).containsExactly(port, port);
            verify(fixture.collections, Mockito.times(3)).resolveCollectionAsync(any(), any());
            verify(fixture.ranges).tryLookupAsync(any(), any(), any(), any());
            assertThat(options.getSessionToken()).isEqualTo(original);
        }
    }

    @DataProvider
    public Object[][] pointWrites() {
        return new Object[][] {
            {OperationType.Create}, {OperationType.Upsert}, {OperationType.Replace},
            {OperationType.Patch}, {OperationType.Delete}
        };
    }

    @Test(groups = "unit", dataProvider = "scopedOutcomes", dataProviderClass = SessionTokenHelperTests.class)
    public void pointPreparationFailureOrTimeoutNeverStartsAHedge(boolean timeout) throws Exception {
        try (Fixture fixture = new Fixture()) {
            RxDocumentClientImpl client = fixture.fullClient();
            RuntimeException metadataFailure = new IllegalStateException("metadata failure before hedging");
            Mockito.when(fixture.ranges.tryLookupAsync(any(), any(), any(), any()))
                .thenReturn(timeout ? Mono.never() : Mono.error(metadataFailure));
            RequestOptions options = new RequestOptions();
            options.setPartitionKey(new PartitionKey("customer"));
            options.setSessionToken(ORIGINAL);
            options.setCosmosEndToEndLatencyPolicyConfig(
                new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofMillis(200))
                    .availabilityStrategy(new ThresholdBasedAvailabilityStrategy(
                        Duration.ofMillis(10), Duration.ofMillis(10))).build());
            Method read = RxDocumentClientImpl.class.getDeclaredMethod(
                "readDocument", String.class, String.class, RequestOptions.class, DiagnosticsClientContext.class);
            read.setAccessible(true);
            Mono<?> operation = (Mono<?>) read.invoke(client,
                "/dbs/db/colls/coll/docs/item", "item", options, fixture.context);
            Throwable failure = catchThrowable(() -> operation.block(Duration.ofSeconds(5)));
            if (timeout) {
                assertThat(failure).isInstanceOf(OperationCancelledException.class);
            } else {
                assertThat(failure).isSameAs(metadataFailure);
            }
            assertThat(fixture.sentRequests).isEmpty();
            verify(fixture.ranges).tryLookupAsync(any(), any(), any(), any());
        }
    }

    @DataProvider
    public Object[][] pointReadRetryScopes() {
        return new Object[][] {
            {ORIGINAL, false, "0:1#100#1=90#2=-1", "0:1#100#1=90#2=-1", 10250},
            {ORIGINAL, true, "0:1#100#1=90#2=-1", "2:1#100#1=90#2=-1", 10250},
            {"2:1#200,1:1#100", true, "2:1#200,1:1#100", "2:1#200", 443}
        };
    }

    @Test(groups = "unit", dataProvider = "pointReadRetryScopes")
    public void readDocumentRetryReusesOrReplacesPreparedScope(
        String original, boolean split, String firstToken, String secondToken, int firstPort) throws Exception {
        try (Fixture fixture = new Fixture()) {
            RxDocumentClientImpl client = fixture.fullClient();
            List<RxDocumentServiceRequest> attempts = new ArrayList<>();
            List<String> attemptTokens = new ArrayList<>();
            List<URI> attemptEndpoints = new ArrayList<>();
            IServerErrorInjector injector = Mockito.mock(IServerErrorInjector.class);
            Mockito.when(injector.injectServerResponseError(any(), any())).thenAnswer(invocation -> {
                FaultInjectionRequestArgs args = invocation.getArgument(0);
                attempts.add(args.getServiceRequest());
                attemptTokens.add(args.getServiceRequest().getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN));
                attemptEndpoints.add(args.getRequestURI());
                if (attempts.size() == 1) {
                    if (split) {
                        fixture.map = SessionTokenHelperTests.routingMap(
                            new PartitionKeyRange("2", "", "40", Collections.singletonList("0")),
                            new PartitionKeyRange("3", "40", "80", Collections.singletonList("0")),
                            new PartitionKeyRange("1", "80", "FF"));
                    }
                    Utils.ValueHolder<CosmosException> failure = invocation.getArgument(1);
                    failure.v = new RequestRateTooLargeException();
                    return true;
                }
                return false;
            });
            fixture.inject(injector);
            RequestOptions options = new RequestOptions();
            options.setPartitionKey(new PartitionKey("customer"));
            options.setSessionToken(original);
            options.setCosmosEndToEndLatencyPolicyConfig(
                new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(5)).build());
            Method read = RxDocumentClientImpl.class.getDeclaredMethod(
                "readDocument", String.class, String.class, RequestOptions.class, DiagnosticsClientContext.class);
            read.setAccessible(true);
            assertThat(((Mono<?>) read.invoke(client,
                "/dbs/db/colls/coll/docs/item", "item", options, fixture.context)).block(Duration.ofSeconds(5))).isNotNull();
            assertThat(attemptTokens).containsExactly(firstToken, secondToken);
            assertThat(attemptEndpoints).extracting(URI::getPort).containsExactly(firstPort, 10250);
            assertThat(attemptEndpoints).extracting(URI::getHost).containsExactly("localhost", "localhost");
            if (split) {
                assertThat(attempts.get(1).requestContext.getSessionTokenState())
                    .isNotSameAs(attempts.get(0).requestContext.getSessionTokenState());
            } else {
                assertThat(attempts.get(1).requestContext.getSessionTokenState())
                    .isSameAs(attempts.get(0).requestContext.getSessionTokenState());
            }
            verify(fixture.ranges, Mockito.times(2)).tryLookupAsync(any(), any(), any(), any());
            assertThat(options.getSessionToken()).isEqualTo(original);
        }
    }

    @Test(groups = "unit", dataProvider = "pointWrites")
    public void pointWritesPrepareCompoundProgressBeforeDispatch(OperationType operationType) throws Exception {
        try (Fixture fixture = new Fixture()) {
            RxDocumentClientImpl client = fixture.fullClient();
            fixture.thin.setUseMultipleWriteLocations(true);
            fixture.classic.setUseMultipleWriteLocations(true);
            RequestOptions options = new RequestOptions();
            options.setPartitionKey(new PartitionKey("customer"));
            options.setSessionToken(ORIGINAL);
            options.setCosmosEndToEndLatencyPolicyConfig(
                new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(5)).build());
            Document document = new Document("{\"id\":\"item\",\"pk\":\"customer\"}");
            Mono<ResourceResponse<Document>> result;
            switch (operationType) {
                case Create:
                    result = client.createDocument("/dbs/db/colls/coll", document, options, true);
                    break;
                case Upsert:
                    result = client.upsertDocument("/dbs/db/colls/coll", document, options, true);
                    break;
                case Replace:
                    result = client.replaceDocument("/dbs/db/colls/coll/docs/item", "item", document, options);
                    break;
                case Patch:
                    result = client.patchDocument("/dbs/db/colls/coll/docs/item", "item",
                        CosmosPatchOperations.create().set("/value", 1), options);
                    break;
                case Delete:
                    result = client.deleteDocument("/dbs/db/colls/coll/docs/item", "item", options);
                    break;
                default:
                    throw new AssertionError(operationType);
            }
            assertThat(result.block(Duration.ofSeconds(5))).isNotNull();
            assertThat(fixture.wireTokens).containsExactly("0:1#100#1=90#2=-1");
            verify(fixture.ranges).tryLookupAsync(any(), any(), any(), any());
            assertThat(options.getSessionToken()).isEqualTo(ORIGINAL);
        }
    }

    @DataProvider
    public Object[][] fallbackOperations() {
        return new Object[][] {
            {OperationType.Read, false}, {OperationType.Read, true},
            {OperationType.Query, false}, {OperationType.Query, true},
            {OperationType.Batch, false}, {OperationType.Create, true}
        };
    }

    @Test(groups = "unit", dataProvider = "fallbackOperations")
    public void unresolvedScopeSelectsClassicBeforeSendingInSameRegion(
        OperationType operation, boolean recreateRequest) throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.thin.setUseMultipleWriteLocations(true);
            fixture.classic.setUseMultipleWriteLocations(true);
            DocumentClientRetryPolicy policy = fixture.retryPolicy();
            RxDocumentClientImpl client = fixture.client();
            RxDocumentServiceRequest original = fixture.request(operation, ORIGINAL, null);
            original.setContentBytes("{\"id\":\"item\",\"pk\":\"customer\"}".getBytes(StandardCharsets.UTF_8));
            original.setResponseTimeout(Duration.ofSeconds(5));
            original.requestContext.setExcludeRegions(Collections.singletonList("North Europe"));
            List<URI> attemptedEndpoints = new CopyOnWriteArrayList<>();
            IServerErrorInjector injector = Mockito.mock(IServerErrorInjector.class);
            Mockito.when(injector.injectServerResponseError(any(), any())).thenAnswer(invocation -> {
                FaultInjectionRequestArgs args = invocation.getArgument(0);
                attemptedEndpoints.add(args.getRequestURI());
                return false;
            });
            fixture.inject(injector);
            List<RxDocumentServiceRequest> attempts = new ArrayList<>();
            BackoffRetryUtility.executeRetry(() -> {
                RxDocumentServiceRequest request = recreateRequest ? original.clone() : original;
                request.requestContext.setExcludeRegions(original.requestContext.getExcludeRegions());
                Duration timeoutBeforePolicy = request.getResponseTimeout();
                policy.onBeforeSendRequest(request);
                assertThat(request.getResponseTimeout()).isEqualTo(timeoutBeforePolicy);
                attempts.add(request);
                return dispatch(client, request);
            }, policy).block(Duration.ofSeconds(5));

            assertThat(attempts).hasSize(1);
            assertThat(attemptedEndpoints).extracting(URI::getPort).containsExactly(443);
            assertThat(attemptedEndpoints).extracting(URI::getHost).containsExactly("localhost");
            assertThat(fixture.wireTokens).containsExactly(ORIGINAL);
            assertThat(fixture.sentRequests).hasSize(1);
            assertThat(attempts.get(0).getContentAsByteArray()).containsExactly(original.getContentAsByteArray());
            assertThat(attempts.get(0).getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(ORIGINAL);
            assertThat(attempts.get(0).requestContext.getExcludeRegions())
                .containsExactly("North Europe");
            assertThat(attempts.get(0).useThinClientMode).isFalse();
            assertThat(attempts.get(0).useGatewayMode).isFalse();
            assertThat(policy.getRetryContext().getRetryCount()).isZero();
            verify(fixture.endpointManager, never()).markEndpointUnavailableForRead(any());
            verify(fixture.endpointManager, never()).markEndpointUnavailableForWrite(any());
            verify(fixture.endpointManager, never()).refreshLocationAsync(any(), Mockito.anyBoolean());
            verify(fixture.automaticFailover, never())
                .tryMarkEndpointAsUnavailableForPartitionKeyRange(any(), Mockito.anyBoolean());

            // A sibling operation's policy must not inherit the transport decision.
            RxDocumentServiceRequest sibling = fixture.request(operation, ORIGINAL, "0");
            fixture.retryPolicy().onBeforeSendRequest(sibling);
            assertThat(sibling.useGatewayMode).isFalse();
        }
    }

    @Test(groups = "unit")
    public void unresolvedScopePreservesFilterWithoutSendingThinRequest() throws Exception {
        try (Fixture fixture = new Fixture()) {
            RxDocumentServiceRequest request = fixture.request(OperationType.Query, ORIGINAL, null);
            request.getHeaders().put(HttpConstants.HttpHeaders.START_EPK, "10");
            request.getHeaders().put(HttpConstants.HttpHeaders.END_EPK, "FF");
            request.getHeaders().put(HttpConstants.HttpHeaders.READ_FEED_KEY_TYPE,
                ReadFeedKeyType.EffectivePartitionKeyRange.name());
            DocumentClientRetryPolicy policy = fixture.retryPolicy();
            RxDocumentClientImpl client = fixture.client();
            BackoffRetryUtility.executeRetry(() -> {
                policy.onBeforeSendRequest(request);
                return dispatch(client, request);
            }, policy).block(Duration.ofSeconds(5));
            assertThat(fixture.wireTokens).containsExactly(ORIGINAL);
            assertThat(fixture.endpoints).extracting(URI::getPort).containsExactly(443);
            assertThat(fixture.sentRequests.get(0).headers().value(HttpConstants.HttpHeaders.START_EPK)).isEqualTo("10");
            assertThat(fixture.sentRequests.get(0).headers().value(HttpConstants.HttpHeaders.END_EPK)).isEqualTo("FF");
            assertThat(policy.getRetryContext().getRetryCount()).isZero();
        }
    }

    @Test(groups = "unit")
    public void injectedBadRequestIsNotRetriedOnEitherTransport() throws Exception {
        for (boolean fallback : new boolean[] {false, true}) {
            try (Fixture fixture = new Fixture()) {
                List<CosmosException> injectedErrors = new CopyOnWriteArrayList<>();
                AtomicInteger injected = new AtomicInteger();
                IServerErrorInjector injector = Mockito.mock(IServerErrorInjector.class);
                Mockito.when(injector.injectServerResponseError(any(), any())).thenAnswer(invocation -> {
                    injected.incrementAndGet();
                    Utils.ValueHolder<CosmosException> failure = invocation.getArgument(1);
                    failure.v = new BadRequestException("Invalid caller input");
                    injectedErrors.add(failure.v);
                    return true;
                });
                fixture.inject(injector);
                DocumentClientRetryPolicy policy = fixture.retryPolicy();
                RxDocumentClientImpl client = fixture.client();
                RxDocumentServiceRequest request = fixture.request(OperationType.Read, ORIGINAL, fallback ? null : "0");
                Throwable failure = catchThrowable(() -> BackoffRetryUtility.executeRetry(() -> {
                    policy.onBeforeSendRequest(request);
                    return dispatch(client, request);
                }, policy).block(Duration.ofSeconds(5)));
                assertThat(injected.get()).as("Injected errors; fallback=%s, failure=%s", fallback, failure)
                    .isEqualTo(1);
                assertThat(failure).isSameAs(injectedErrors.get(injectedErrors.size() - 1));
                assertThat(fixture.sentRequests).isEmpty();
                assertThat(request.useThinClientMode).isEqualTo(!fallback);
                assertThat(request.useGatewayMode).isFalse();
            }
        }
    }

    @Test(groups = "unit")
    public void injectedThrottleRetriesAndReevaluatesTransportWithoutLosingProgress() throws Exception {
        try (Fixture fixture = new Fixture()) {
            DocumentClientRetryPolicy policy = fixture.retryPolicy();
            RxDocumentServiceRequest request = fixture.request(OperationType.Query, ORIGINAL, null);
            request.getHeaders().put(HttpConstants.HttpHeaders.START_EPK, "10");
            request.getHeaders().put(HttpConstants.HttpHeaders.END_EPK, "F0");
            request.getHeaders().put(HttpConstants.HttpHeaders.READ_FEED_KEY_TYPE,
                ReadFeedKeyType.EffectivePartitionKeyRange.name());
            List<URI> attemptedEndpoints = new CopyOnWriteArrayList<>();
            IServerErrorInjector injector = Mockito.mock(IServerErrorInjector.class);
            Mockito.when(injector.injectServerResponseError(any(), any())).thenAnswer(invocation -> {
                FaultInjectionRequestArgs args = invocation.getArgument(0);
                attemptedEndpoints.add(args.getRequestURI());
                if (attemptedEndpoints.size() == 1) {
                    fixture.map = SessionTokenHelperTests.routingMap(
                        new PartitionKeyRange("2", "", "FF", Arrays.asList("0", "1")));
                    request.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity(RID, "2"));
                    request.requestContext.resolvedPartitionKeyRange = fixture.map.getRangeByPartitionKeyRangeId("2");
                    Utils.ValueHolder<CosmosException> failure = invocation.getArgument(1);
                    failure.v = new RequestRateTooLargeException();
                    return true;
                }
                return false;
            });
            fixture.inject(injector);
            RxDocumentClientImpl client = fixture.client();
            BackoffRetryUtility.executeRetry(() -> {
                policy.onBeforeSendRequest(request);
                return dispatch(client, request);
            }, policy).block(Duration.ofSeconds(5));
            assertThat(attemptedEndpoints).extracting(URI::getPort).containsExactly(443, 10250);
            assertThat(attemptedEndpoints).extracting(URI::getHost).containsExactly("localhost", "localhost");
            assertThat(fixture.wireTokens).containsExactly("2:1#200#1=180#2=150");
            assertThat(policy.getRetryContext().getRetryCount()).isEqualTo(1);
            assertThat(policy.getRetryContext().getStatusAndSubStatusCodes().get(0)[0]).isEqualTo(429);
            assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN))
                .isEqualTo("2:1#200#1=180#2=150");
            assertThat(request.getOriginalSessionToken()).isEqualTo(ORIGINAL);
            assertThat(request.useGatewayMode).isFalse();
        }
    }

    @Test(groups = "unit")
    public void cancellationStopsClassicFallbackWithoutReplayingAgain() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.delayFirstResponse = true;
            RxDocumentServiceRequest request = fixture.request(OperationType.Query, ORIGINAL, null);
            DocumentClientRetryPolicy policy = fixture.retryPolicy();
            RxDocumentClientImpl client = fixture.client();
            List<Throwable> errors = new CopyOnWriteArrayList<>();
            Disposable subscription = BackoffRetryUtility.executeRetry(() -> {
                policy.onBeforeSendRequest(request);
                return dispatch(client, request);
            }, policy).subscribe(ignored -> { }, errors::add);
            fixture.primaryRequestSent.asMono().block(Duration.ofSeconds(5));
            subscription.dispose();
            assertThat(errors).isEmpty();
            assertThat(fixture.wireTokens).containsExactly(ORIGINAL);
            assertThat(fixture.endpoints).extracting(URI::getPort).containsExactly(443);
            assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(ORIGINAL);
            assertThat(policy.getRetryContext().getRetryCount()).isZero();
        }
    }

    private static Mono<RxDocumentServiceResponse> dispatch(
        RxDocumentClientImpl client, RxDocumentServiceRequest request) throws ReflectiveOperationException {
        return dispatch(client, request, null);
    }

    private static Mono<RxDocumentServiceResponse> dispatch(
        RxDocumentClientImpl client, RxDocumentServiceRequest request, OperationContextAndListenerTuple listener)
        throws ReflectiveOperationException {
        Method method = RxDocumentClientImpl.class.getDeclaredMethod("getStoreProxy", RxDocumentServiceRequest.class);
        method.setAccessible(true);
        RequestVerb verb;
        switch (request.getOperationType()) {
            case Read:
            case ReadFeed:
                verb = RequestVerb.GET;
                break;
            case Delete:
                verb = RequestVerb.DELETE;
                break;
            case Replace:
                verb = RequestVerb.PUT;
                break;
            case Patch:
                verb = RequestVerb.PATCH;
                break;
            default:
                verb = RequestVerb.POST;
        }
        return client.populateHeadersAsync(request, verb).flatMap(prepared -> {
            try {
                return ((RxStoreModel) method.invoke(client, prepared)).processMessage(prepared, listener);
            } catch (ReflectiveOperationException e) {
                return Mono.error(e);
            }
        });
    }

    @DataProvider
    public Object[][] operationListenerScenarios() {
        return new Object[][] {{"thin"}, {"thin-unresolved"}, {"classic"}, {"metadata-error"}};
    }

    @Test(groups = "unit", dataProvider = "operationListenerScenarios")
    public void operationListenerRemainsAtFinalDispatchBoundary(String scenario) throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.thin.setUseMultipleWriteLocations(true);
            fixture.classic.setUseMultipleWriteLocations(true);
            RxDocumentClientImpl client = fixture.client();
            RxDocumentServiceRequest request = fixture.request(
                OperationType.Create, ORIGINAL, "classic".equals(scenario) ? null : "0");
            if ("thin-unresolved".equals(scenario) || "metadata-error".equals(scenario)) {
                request.requestContext.resolvedPartitionKeyRange = null;
            } else if ("thin".equals(scenario)) {
                request.captureOriginalSessionToken();
                request.useThinClientMode = true;
                fixture.thin.tryPrepareExplicitSessionToken(request).block(Duration.ofSeconds(5));
            }
            OperationContext context = Mockito.mock(OperationContext.class);
            String correlationId = "00000000-0000-0000-0000-000000000001";
            Mockito.when(context.getCorrelationActivityId()).thenReturn(correlationId);
            OperationListener listener = Mockito.mock(OperationListener.class);
            OperationContextAndListenerTuple tuple = new OperationContextAndListenerTuple(context, listener);
            List<String> events = new CopyOnWriteArrayList<>();
            RuntimeException metadataError = new IllegalStateException("metadata failure");
            Mockito.doAnswer(invocation -> { events.add("request"); return null; })
                .when(listener).requestListener(context, request);
            Mockito.doAnswer(invocation -> { events.add("response"); return null; })
                .when(listener).responseListener(Mockito.eq(context), any());
            Mockito.doAnswer(invocation -> { events.add("error"); return null; })
                .when(listener).exceptionListener(Mockito.eq(context), any());
            Mockito.when(fixture.ranges.tryLookupAsync(any(), any(), any(), any())).thenAnswer(invocation -> {
                events.add("prepare");
                return "metadata-error".equals(scenario)
                    ? Mono.error(metadataError) : Mono.just(new Utils.ValueHolder<>(fixture.map));
            });

            if ("metadata-error".equals(scenario)) {
                assertThatThrownBy(() -> dispatch(client, request, tuple).block(Duration.ofSeconds(5)))
                    .isSameAs(metadataError);
                assertThat(events).containsExactly("prepare");
                verify(listener, never()).requestListener(any(), any());
                verify(listener, never()).exceptionListener(any(), any());
                verify(listener, never()).responseListener(any(), any());
                assertThat(fixture.sentRequests).isEmpty();
            } else {
                RxDocumentServiceResponse response = dispatch(client, request, tuple).block(Duration.ofSeconds(5));
                assertThat(events).containsExactly("thin".equals(scenario)
                    ? new String[] {"request", "response"} : new String[] {"prepare", "request", "response"});
                verify(listener).requestListener(context, request);
                verify(listener).responseListener(context, response);
                verify(listener, never()).exceptionListener(any(), any());
                assertThat(fixture.endpoints).extracting(URI::getPort)
                    .containsExactly("classic".equals(scenario) ? 443 : 10250);
                assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.CORRELATED_ACTIVITY_ID))
                    .isEqualTo(correlationId);
            }
        }
    }

    private static Mono<?> executeFeedWithAvailability(
        RxDocumentClientImpl client, RxDocumentServiceRequest request,
        Supplier<DocumentClientRetryPolicy> retryFactory,
        BiFunction<Supplier<DocumentClientRetryPolicy>, RxDocumentServiceRequest, Mono<RxDocumentServiceResponse>> operation)
        throws Exception {
        Method method = RxDocumentClientImpl.class.getDeclaredMethod("executeFeedOperationWithAvailabilityStrategy",
            ResourceType.class, OperationType.class, Supplier.class, RxDocumentServiceRequest.class,
            BiFunction.class, String.class);
        method.setAccessible(true);
        return (Mono<?>) method.invoke(client, ResourceType.Document, OperationType.Query, retryFactory, request,
            operation, "/dbs/db/colls/coll");
    }

    private static Mono<?> executePointWithAvailability(
        RxDocumentClientImpl client, RequestOptions options, DiagnosticsClientContext context,
        BiFunction<RequestOptions, CrossRegionAvailabilityContextForRxDocumentServiceRequest,
            Mono<ResourceResponse<Document>>> operation) throws Exception {
        Class<?> callbackType = Class.forName(RxDocumentClientImpl.class.getName() + "$DocumentPointOperation");
        Object callback = Proxy.newProxyInstance(callbackType.getClassLoader(), new Class<?>[] {callbackType},
            (proxy, method, args) -> {
                if (!"apply".equals(method.getName())) {
                    throw new UnsupportedOperationException("Unexpected callback method: " + method.getName());
                }
                return operation.apply((RequestOptions) args[0],
                    (CrossRegionAvailabilityContextForRxDocumentServiceRequest) args[3]);
            });
        Method method = RxDocumentClientImpl.class.getDeclaredMethod("wrapPointOperationWithAvailabilityStrategy",
            ResourceType.class, OperationType.class, callbackType, RequestOptions.class, boolean.class,
            DiagnosticsClientContext.class, String.class);
        method.setAccessible(true);
        return (Mono<?>) method.invoke(client, ResourceType.Document, OperationType.Read, callback, options, false,
            context, "/dbs/db/colls/coll");
    }

    @DataProvider
    public Object[][] hedges() {
        return new Object[][] {
            {false, ORIGINAL, "0:1#100#1=90#2=-1", false},
            {true, ORIGINAL, "0:1#100#1=90#2=-1", false},
            {false, "0:1#100,1:1#200", "0:1#100", false},
            {true, "0:1#100,1:1#200", "0:1#100", false},
            {false, "1:1#100,1:1#200", null, false},
            {true, "1:1#100,1:1#200", null, false},
            {false, ORIGINAL, "0:1#100#1=90#2=-1", true},
            {true, ORIGINAL, "0:1#100#1=90#2=-1", true}
        };
    }

    @Test(groups = "unit", dataProvider = "hedges")
    public void actualAvailabilityStrategiesPreserveBothAttempts(
        boolean feed, String original, String expected, boolean fallbackPrimary)
        throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.delayFirstResponse = true;
            RxDocumentClientImpl client = fixture.client();
            setField(client, "globalPartitionEndpointManagerForPerPartitionCircuitBreaker",
                Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.class));
            CosmosEndToEndOperationLatencyPolicyConfig policy =
                new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(5))
                    .availabilityStrategy(new ThresholdBasedAvailabilityStrategy(
                        Duration.ofMillis(10), Duration.ofMillis(10))).build();
            List<RxDocumentServiceRequest> attempts = new CopyOnWriteArrayList<>();
            List<DocumentClientRetryPolicy> policies = new CopyOnWriteArrayList<>();
            if (feed) {
                RxDocumentServiceRequest parent = fixture.request(OperationType.Query, original, "0");
                parent.requestContext.setEndToEndOperationLatencyPolicyConfig(policy);
                executeFeedWithAvailability(client, parent, fixture::retryPolicy, (retry, request) -> {
                    attempts.add(request);
                    setHedgeScope(request, fallbackPrimary);
                    DocumentClientRetryPolicy retryPolicy = retry.get();
                    policies.add(retryPolicy);
                    return BackoffRetryUtility.executeRetry(() -> {
                        retryPolicy.onBeforeSendRequest(request);
                        return dispatch(client, request);
                    }, retryPolicy);
                }).block(Duration.ofSeconds(5));
                assertThat(parent.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(original);
                assertThat(parent.useGatewayMode).isFalse();
            } else {
                RequestOptions options = new RequestOptions();
                options.setSessionToken(original);
                options.setCosmosEndToEndLatencyPolicyConfig(policy);
                RxDocumentServiceRequest prepared = fixture.request(OperationType.Read, original, "0");
                prepared.captureOriginalSessionToken();
                prepared.useThinClientMode = true;
                fixture.thin.tryPrepareExplicitSessionToken(prepared).block(Duration.ofSeconds(5));
                executePointWithAvailability(client, options, fixture.context,
                    (copy, availability) -> Mono.defer(() -> {
                        RxDocumentServiceRequest request = fixture.request(
                            OperationType.Read, copy.getSessionToken(), "0");
                        request.requestContext.setCrossRegionAvailabilityContext(availability);
                        request.requestContext.setSessionTokenState(prepared.requestContext.getSessionTokenState());
                        request.requestContext.setExcludeRegions(copy.getExcludedRegions());
                        setHedgeScope(request, fallbackPrimary);
                        attempts.add(request);
                        DocumentClientRetryPolicy retryPolicy = fixture.retryPolicy();
                        policies.add(retryPolicy);
                        return BackoffRetryUtility.executeRetry(() -> {
                            retryPolicy.onBeforeSendRequest(request);
                            return dispatch(client, request);
                        }, retryPolicy)
                            .map(response -> new ResourceResponse<>(response, Document.class));
                    })).block(Duration.ofSeconds(5));
                assertThat(options.getSessionToken()).isEqualTo(original);
                assertThat(options.getExcludedRegions()).isNullOrEmpty();
            }

            assertThat(attempts).hasSize(2);
            assertThat(attempts.get(0)).isNotSameAs(attempts.get(1));
            if (!fallbackPrimary) {
                assertThat(attempts.get(0).requestContext.getSessionTokenState())
                    .isSameAs(attempts.get(1).requestContext.getSessionTokenState());
            }
            assertThat(attempts.get(0).requestContext).isNotSameAs(attempts.get(1).requestContext);
            if (!fallbackPrimary && expected != null) {
                assertThat(attempts.get(0).requestContext.sessionToken)
                    .isSameAs(attempts.get(1).requestContext.sessionToken);
            }
            for (RxDocumentServiceRequest attempt : attempts) {
                assertThat(attempt.getOriginalSessionToken()).isEqualTo(original);
                if (expected == null) {
                    assertThat(attempt.requestContext.getSessionTokenEvaluationResults()).hasSize(1);
                }
            }
            assertThat(attempts.get(0).getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN))
                .isEqualTo(fallbackPrimary ? original : expected);
            assertThat(attempts.get(1).getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(expected);
            assertThat(attempts.get(0).useGatewayMode).isFalse();
            assertThat(attempts.get(1).useGatewayMode).isFalse();
            assertThat(attempts.get(0).useThinClientMode).isEqualTo(!fallbackPrimary);
            assertThat(attempts.get(1).useThinClientMode).isTrue();
            assertThat(policies).allSatisfy(retry -> assertThat(retry.getRetryContext().getRetryCount()).isZero());
            assertThat(fixture.wireTokens).containsExactlyInAnyOrder(fallbackPrimary ? original : expected, expected);
            assertThat(fixture.endpoints).extracting(URI::getHost).containsExactlyInAnyOrder("localhost", "secondary");
            assertThat(fixture.endpoints).extracting(URI::getPort)
                .containsExactlyInAnyOrder(fallbackPrimary ? 443 : 10250, 10250);
        }
    }

    private static void setHedgeScope(RxDocumentServiceRequest request, boolean fallbackPrimary) {
        List<String> excluded = request.requestContext.getExcludeRegions();
        if (fallbackPrimary && (excluded == null || !excluded.contains("East US"))) {
            request.setPartitionKeyRangeIdentity(null);
            request.requestContext.resolvedPartitionKeyRange = null;
        }
    }

    private static void setField(RxDocumentClientImpl client, String name, Object value) throws ReflectiveOperationException {
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
    public void explicitCompoundPreparesOutgoingHeaderAndPreservesOriginal(OperationType operation) {
        try (Fixture fixture = new Fixture()) {
            RxDocumentServiceRequest request = fixture.request(operation, ORIGINAL, "1");
            fixture.processMessage(request).block(Duration.ofSeconds(5));
            assertThat(fixture.wireTokens).containsExactly("1:1#200#1=180#2=150");
            assertThat(fixture.endpoints).allMatch(uri -> uri.getPort() == 10250);
            assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN))
                .isEqualTo("1:1#200#1=180#2=150");
            assertThat(request.getOriginalSessionToken()).isEqualTo(ORIGINAL);
            verify(fixture.sessions, never()).resolveGlobalSessionToken(any());
            verify(fixture.sessions, never()).resolvePartitionLocalSessionToken(any(), any());
            verify(fixture.sessions).setSessionToken(any(RxDocumentServiceRequest.class), any(Map.class));
            verify(fixture.ranges).tryLookupAsync(any(), any(), any(), any());
        }
    }

    @Test(groups = "unit")
    public void verifiedRangeReusesParentProgressWithoutRepeatedMetadataOrHashing() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.map = SessionTokenHelperTests.routingMap(
                new PartitionKeyRange("2", "", "FF", Arrays.asList("0", "1")));
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, ORIGINAL, "2");
            PartitionKeyInternal key = Mockito.mock(PartitionKeyInternal.class);
            request.setPartitionKeyInternal(key);
            request.captureOriginalSessionToken();
            request.useThinClientMode = true;
            prepareSessionToken(fixture.thin, request).block(Duration.ofSeconds(5));
            fixture.thin.setCollectionCache(null);
            fixture.thin.setPartitionKeyRangeCache(null);
            prepareSessionToken(fixture.thin, request).block(Duration.ofSeconds(5));

            assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN))
                .isEqualTo("2:1#200#1=180#2=150");
            verify(fixture.collections).resolveCollectionAsync(any(), any());
            verify(fixture.ranges).tryLookupAsync(any(), any(), any(), any());
            Mockito.verifyNoMoreInteractions(key);
        }
    }

    @Test(groups = "unit", dataProvider = "transportChanges")
    public void repeatedAttemptsAndClonesReusePreparedProgress(String original, String expected) {
        try (Fixture fixture = new Fixture()) {
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, original, "0");
            Mono<RxDocumentServiceResponse> operation = fixture.processMessage(request);
            operation.block(Duration.ofSeconds(5));
            ISessionToken prepared = request.requestContext.sessionToken;
            operation.block(Duration.ofSeconds(5));
            RxDocumentServiceRequest clone = request.clone();
            fixture.processMessage(clone).block(Duration.ofSeconds(5));
            assertThat(fixture.wireTokens).containsExactly(expected, expected, expected);
            if (expected != null) {
                assertThat(request.requestContext.sessionToken).isSameAs(prepared);
                assertThat(clone.requestContext.sessionToken).isSameAs(prepared);
            } else {
                assertThat(request.requestContext.getSessionTokenEvaluationResults()).hasSize(1);
                assertThat(clone.requestContext.getSessionTokenEvaluationResults()).hasSize(1);
            }
            assertThat(request.requestContext.getSessionTokenState()).isSameAs(clone.requestContext.getSessionTokenState());
            verify(fixture.sessions, never()).resolveGlobalSessionToken(any());
            verify(fixture.sessions, never()).resolvePartitionLocalSessionToken(any(), any());
        }
    }

    @Test(groups = "unit")
    public void changedParentLineageRescopesOriginalProgress() {
        try (Fixture fixture = new Fixture()) {
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, ORIGINAL, "0");
            fixture.processMessage(request).block(Duration.ofSeconds(5));
            ISessionToken first = request.requestContext.sessionToken;
            fixture.map = SessionTokenHelperTests.routingMap(
                new PartitionKeyRange("0", "", "FF", Collections.singletonList("1")));
            request.requestContext.resolvedPartitionKeyRange = fixture.map.getRangeByPartitionKeyRangeId("0");
            fixture.processMessage(request).block(Duration.ofSeconds(5));
            assertThat(request.requestContext.sessionToken).isNotSameAs(first);
            assertThat(fixture.wireTokens).containsExactly("0:1#100#1=90#2=-1", "0:1#200#1=180#2=150");
            assertThat(request.getOriginalSessionToken()).isEqualTo(ORIGINAL);
        }
    }

    @Test(groups = "unit", dataProvider = "transportChanges")
    public void recreatedPointRetriesShareTheOperationScope(String original, String expected) throws Exception {
        try (Fixture fixture = new Fixture()) {
            RxDocumentClientImpl client = fixture.client();
            setField(client, "globalPartitionEndpointManagerForPerPartitionCircuitBreaker",
                Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.class));
            RegionalRoutingContext region =
                fixture.endpointManager.getApplicableReadRegionalRoutingContexts(Collections.emptyList()).get(0);
            Mockito.when(fixture.endpointManager.getApplicableReadRegionalRoutingContexts(Mockito.<List<String>>any()))
                .thenReturn(new UnmodifiableList<>(Collections.singletonList(region)));
            AtomicInteger sends = new AtomicInteger();
            IServerErrorInjector injector = Mockito.mock(IServerErrorInjector.class);
            Mockito.when(injector.injectServerResponseError(any(), any())).thenAnswer(invocation -> {
                if (sends.getAndIncrement() == 0) {
                    Utils.ValueHolder<CosmosException> failure = invocation.getArgument(1);
                    failure.v = new RequestRateTooLargeException();
                    return true;
                }
                return false;
            });
            fixture.inject(injector);
            RequestOptions options = new RequestOptions();
            options.setSessionToken(original);
            options.setCosmosEndToEndLatencyPolicyConfig(
                new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(5))
                    .availabilityStrategy(new ThresholdBasedAvailabilityStrategy(
                        Duration.ofMillis(10), Duration.ofMillis(10))).build());
            List<RxDocumentServiceRequest> attempts = new ArrayList<>();
            RxDocumentServiceRequest prepared = fixture.request(OperationType.Read, original, "0");
            prepared.captureOriginalSessionToken();
            prepared.useThinClientMode = true;
            fixture.thin.tryPrepareExplicitSessionToken(prepared).block(Duration.ofSeconds(5));
            executePointWithAvailability(client, options, fixture.context, (copy, availability) -> {
                DocumentClientRetryPolicy policy = fixture.retryPolicy();
                return BackoffRetryUtility.executeRetry(() -> {
                    RxDocumentServiceRequest request = fixture.request(OperationType.Read, copy.getSessionToken(), "0");
                    request.requestContext.setCrossRegionAvailabilityContext(availability);
                    request.requestContext.setSessionTokenState(prepared.requestContext.getSessionTokenState());
                    attempts.add(request);
                    policy.onBeforeSendRequest(request);
                    return dispatch(client, request);
                }, policy).map(response -> new ResourceResponse<>(response, Document.class));
            }).block(Duration.ofSeconds(5));

            assertThat(attempts).hasSize(2);
            assertThat(attempts.get(0)).isNotSameAs(attempts.get(1));
            assertThat(attempts.get(0).requestContext.getSessionTokenState())
                .isSameAs(attempts.get(1).requestContext.getSessionTokenState());
            if (expected != null) {
                assertThat(attempts.get(1).requestContext.sessionToken)
                    .isSameAs(attempts.get(0).requestContext.sessionToken);
            } else {
                assertThat(attempts).allSatisfy(attempt ->
                    assertThat(attempt.requestContext.getSessionTokenEvaluationResults()).hasSize(1));
            }
            assertThat(fixture.wireTokens).containsExactly(expected);
            assertThat(sends.get()).isEqualTo(2);
        }
    }

    @Test(groups = "unit", dataProvider = "transportChanges")
    public void concurrentClonesReuseAlreadyPreparedSessionState(String original, String expected) throws Exception {
        try (Fixture fixture = new Fixture()) {
            RxDocumentServiceRequest parent = fixture.request(OperationType.Query, original, "0");
            parent.captureOriginalSessionToken();
            parent.useThinClientMode = true;
            fixture.thin.tryPrepareExplicitSessionToken(parent).block(Duration.ofSeconds(5));
            SessionTokenHelper.setOriginalSessionToken(parent, original);
            List<RxDocumentServiceRequest> attempts = new ArrayList<>();
            List<Future<Boolean>> results = new ArrayList<>();
            ExecutorService executor = Executors.newFixedThreadPool(8);
            CyclicBarrier start = new CyclicBarrier(8);
            try {
                for (int i = 0; i < 8; i++) {
                    RxDocumentServiceRequest request = parent.clone();
                    request.captureOriginalSessionToken();
                    request.useThinClientMode = true;
                    attempts.add(request);
                    results.add(executor.submit(() -> {
                        start.await(5, TimeUnit.SECONDS);
                        return fixture.thin.tryPrepareExplicitSessionToken(request).block(Duration.ofSeconds(5));
                    }));
                }
                for (Future<Boolean> result : results) {
                    assertThat(result.get(5, TimeUnit.SECONDS)).isTrue();
                }
                DocumentServiceRequestContext.SessionTokenState state = parent.requestContext.getSessionTokenState();
                assertThat(state).isNotNull();
                for (RxDocumentServiceRequest attempt : attempts) {
                    assertThat(attempt.requestContext).isNotSameAs(parent.requestContext);
                    assertThat(attempt.requestContext.getSessionTokenState()).isSameAs(parent.requestContext.getSessionTokenState());
                    assertThat(attempt.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(expected);
                    if (expected != null) {
                        assertThat(attempt.requestContext.sessionToken).isSameAs(attempts.get(0).requestContext.sessionToken);
                    } else {
                        assertThat(attempt.requestContext.getSessionTokenEvaluationResults()).hasSize(1);
                    }
                }
                assertThat(parent.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(original);
                assertThat(fixture.sentRequests).isEmpty();
            } finally {
                executor.shutdownNow();
            }
        }
    }

    @Test(groups = "unit")
    public void retriesAndClonesAlwaysUseOriginalProgress() {
        try (Fixture fixture = new Fixture()) {
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, ORIGINAL, "0");
            Mono<RxDocumentServiceResponse> operation = fixture.processMessage(request);
            operation.block(Duration.ofSeconds(5));
            request.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity(RID, "1"));
            operation.block(Duration.ofSeconds(5));
            RxDocumentServiceRequest clone = request.clone();
            clone.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity(RID, "0"));
            fixture.processMessage(clone).block(Duration.ofSeconds(5));

            fixture.map = SessionTokenHelperTests.routingMap(
                new PartitionKeyRange("2", "", "80", Collections.singletonList("0")),
                new PartitionKeyRange("1", "80", "FF"));
            request.requestContext.resolvedPartitionKeyRangeForCircuitBreaker =
                request.requestContext.resolvedPartitionKeyRange;
            request.requestContext.resolvedPartitionKeyRange = null;
            request.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity(RID, "2"));
            operation.block(Duration.ofSeconds(5));
            assertThat(fixture.wireTokens).containsExactly(
                "0:1#100#1=90#2=-1", "1:1#200#1=180#2=150",
                "0:1#100#1=90#2=-1", "2:1#100#1=90#2=-1");
            assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN))
                .isEqualTo("2:1#100#1=90#2=-1");
            assertThat(clone.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN))
                .isEqualTo("0:1#100#1=90#2=-1");
            assertThat(request.getOriginalSessionToken()).isEqualTo(ORIGINAL);
            assertThat(clone.getOriginalSessionToken()).isEqualTo(ORIGINAL);
        }
    }

    @DataProvider
    public Object[][] directModeTokens() {
        return new Object[][] {
            {"0:1#100,1:1#200", "0:1#100"}, {"0:1#100,0:1#200", "0:1#200"},
            {"1:1#100,1:1#200", null}, {"0:1#100,", "0:1#100"}
        };
    }

    @Test(groups = "unit", dataProvider = "directModeTokens")
    public void selectsOrOmitsTokenLikeDirectModeWithoutChangingTransport(String token, String expected) {
        try (Fixture fixture = new Fixture()) {
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, token, "0");
            fixture.processMessage(request).block(Duration.ofSeconds(5));
            assertThat(fixture.wireTokens).containsExactly(expected);
            assertThat(fixture.endpoints).allMatch(uri -> uri.getPort() == 10250);
            assertThat(request.useThinClientMode).isTrue();
            assertThat(request.useGatewayMode).isFalse();
            assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(expected);
            assertThat(request.getOriginalSessionToken()).isEqualTo(token);
            verify(fixture.sessions, never()).resolveGlobalSessionToken(any());
            verify(fixture.sessions, never()).resolvePartitionLocalSessionToken(any(), any());
            if (expected == null) {
                assertThat(request.requestContext.getSessionTokenEvaluationResults()).hasSize(1);
                assertThat(fixture.encodedTokens.get(0)).doesNotContainKey(RntbdRequestHeader.SessionToken.id());
            }
        }
    }

    @Test(groups = "unit")
    public void unmatchedAttemptsDoNotLoseOriginalProgressOnRetry() {
        try (Fixture fixture = new Fixture()) {
            String original = "1:1#200,1:1#100";
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, original, "0");
            Mono<RxDocumentServiceResponse> operation = fixture.processMessage(request);
            operation.block(Duration.ofSeconds(5));
            RxDocumentServiceRequest clone = request.clone();
            clone.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity(RID, "1"));
            fixture.processMessage(clone).block(Duration.ofSeconds(5));
            request.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity(RID, "1"));
            operation.block(Duration.ofSeconds(5));
            request.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity(RID, "0"));
            operation.block(Duration.ofSeconds(5));
            assertThat(fixture.wireTokens).containsExactly(null, "1:1#200", "1:1#200", null);
            assertThat(fixture.endpoints).extracting(URI::getPort).containsExactly(10250, 10250, 10250, 10250);
            assertThat(request.getHeaders()).doesNotContainKey(HttpConstants.HttpHeaders.SESSION_TOKEN);
            assertThat(request.getOriginalSessionToken()).isEqualTo(original);
            assertThat(clone.getOriginalSessionToken()).isEqualTo(original);
            verify(fixture.sessions, never()).resolveGlobalSessionToken(any());
            verify(fixture.sessions, never()).resolvePartitionLocalSessionToken(any(), any());
        }
    }

    @DataProvider
    public Object[][] transportChanges() {
        return new Object[][] {{ORIGINAL, "0:1#100#1=90#2=-1"}, {"1:1#200,1:1#100", null}};
    }

    @Test(groups = "unit", dataProvider = "transportChanges")
    public void transportChangesRestoreOriginalRatherThanReplayingPreparedHeader(String original, String local)
        throws Exception {
        try (Fixture fixture = new Fixture()) {
            RxDocumentClientImpl client = fixture.client();
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, original, "0");
            dispatch(client, request).block(Duration.ofSeconds(5));
            Mockito.when(fixture.endpointManager.hasThinClientReadLocations()).thenReturn(false);
            dispatch(client, request).block(Duration.ofSeconds(5));
            assertThat(request.useThinClientMode).isFalse();
            Mockito.when(fixture.endpointManager.hasThinClientReadLocations()).thenReturn(true);
            dispatch(client, request).block(Duration.ofSeconds(5));
            assertThat(fixture.wireTokens).containsExactly(local, original, local);
            assertThat(fixture.endpoints).extracting(URI::getPort).containsExactly(10250, 443, 10250);
            assertThat(request.getOriginalSessionToken()).isEqualTo(original);
            verify(fixture.sessions, never()).resolveGlobalSessionToken(any());
            verify(fixture.sessions, never()).resolvePartitionLocalSessionToken(any(), any());
        }
    }

    @DataProvider
    public Object[][] malformedTokens() {
        return new Object[][] {{"0:1#100,1:future"}};
    }

    @Test(groups = "unit", dataProvider = "malformedTokens")
    public void malformedTokenFailsWithoutSendingToEitherTransport(String token) {
        try (Fixture fixture = new Fixture()) {
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, token, "0");
            assertThatThrownBy(() -> fixture.processMessage(request).block(Duration.ofSeconds(5)))
                .hasRootCauseInstanceOf(BadRequestException.class);
            assertThat(fixture.sentRequests).isEmpty();
        }
    }

    @Test(groups = "unit", dataProvider = "scopedOutcomes", dataProviderClass = SessionTokenHelperTests.class)
    public void unknownRangePreservesFullTokenWithoutReportingOmission(boolean unmatched) {
        try (Fixture fixture = new Fixture()) {
            String original = (unmatched ? "1:1#100" : "0:1#100") + ",unknown:1#200";
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, original, "0");
            fixture.processMessage(request).block(Duration.ofSeconds(5));
            assertThat(fixture.wireTokens).containsExactly(original);
            assertThat(fixture.endpoints).extracting(URI::getPort).containsExactly(443);
            assertThat(request.requestContext.getSessionTokenEvaluationResults()).isEmpty();
            verify(fixture.ranges).tryLookupAsync(any(), any(), any(), any());
        }
    }

    @Test(groups = "unit")
    public void missingMetadataIsNotAnUnmatchedToken() {
        try (Fixture fixture = new Fixture()) {
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, ORIGINAL, "0");
            request.requestContext.resolvedPartitionKeyRange = null;
            fixture.map = null;
            assertThatThrownBy(() -> fixture.processMessage(request).block(Duration.ofSeconds(5)))
                .isInstanceOf(NotFoundException.class);
            assertThat(fixture.sentRequests).isEmpty();
        }
    }

    @Test(groups = "unit")
    public void waitsForNormalMetadataResolutionAndPropagatesFailures() {
        try (Fixture fixture = new Fixture()) {
            Sinks.One<Utils.ValueHolder<CollectionRoutingMap>> loading = Sinks.one();
            Mockito.when(fixture.ranges.tryLookupAsync(any(), any(), any(), any())).thenReturn(loading.asMono());
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, ORIGINAL, "0");
            request.requestContext.resolvedPartitionKeyRange = null;
            List<Throwable> errors = new ArrayList<>();
            Disposable subscription = fixture.processMessage(request).subscribe(ignored -> { }, errors::add);
            assertThat(fixture.wireTokens).isEmpty();
            loading.tryEmitValue(new Utils.ValueHolder<>(fixture.map));
            assertThat(errors).isEmpty();
            assertThat(fixture.wireTokens).containsExactly("0:1#100#1=90#2=-1");
            subscription.dispose();

            RuntimeException failure = new IllegalStateException("metadata failure");
            request.requestContext.resolvedPartitionKeyRange = null;
            Mockito.when(fixture.ranges.tryLookupAsync(any(), any(), any(), any())).thenReturn(Mono.error(failure));
            assertThatThrownBy(() -> fixture.processMessage(request).block(Duration.ofSeconds(5)))
                .isSameAs(failure);
            assertThat(fixture.wireTokens).hasSize(1);
        }
    }

    @DataProvider
    public Object[][] diagnosticsForSessionResolution() {
        return new Object[][] {
            {null}, {Mockito.mock(CosmosDiagnostics.class)}, {mockDiagnosticsClientContext().createDiagnostics()}
        };
    }

    @Test(groups = "unit", dataProvider = "diagnosticsForSessionResolution")
    public void sessionResolutionPreservesMetadataDiagnostics(CosmosDiagnostics cosmosDiagnostics) throws Exception {
        try (Fixture fixture = new Fixture()) {
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, ORIGINAL, "1");
            request.requestContext.resolvedPartitionKeyRange = null;
            request.captureOriginalSessionToken();
            request.useThinClientMode = true;
            request.requestContext.cosmosDiagnostics = cosmosDiagnostics;
            ClientSideRequestStatistics statistics = ImplementationBridgeHelpers.CosmosDiagnosticsHelper
                .getCosmosDiagnosticsAccessor().getClientSideRequestStatisticsRaw(cosmosDiagnostics);
            MetadataDiagnosticsContext expected = statistics == null ? null : statistics.getMetadataDiagnosticsContext();

            prepareSessionToken(fixture.thin, request).block(Duration.ofSeconds(5));
            verify(fixture.collections).resolveCollectionAsync(expected, request);
            verify(fixture.ranges).tryLookupAsync(expected, RID, null, null);
            assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN))
                .isEqualTo("1:1#200#1=180#2=150");
        }
    }

    private static Mono<?> prepareSessionToken(RxGatewayStoreModel store, RxDocumentServiceRequest request)
        throws Exception {
        if (request.useThinClientMode && SessionTokenHelper.isCompoundSessionToken(request.getOriginalSessionToken())) {
            return store.tryPrepareExplicitSessionToken(request);
        }
        Method method = RxGatewayStoreModel.class.getDeclaredMethod("applySessionToken", RxDocumentServiceRequest.class);
        method.setAccessible(true);
        return (Mono<?>) method.invoke(store, request);
    }

    @DataProvider
    public Object[][] missingSessionMetadata() {
        return new Object[][] {
            {"collection", false}, {"collection", true}, {"ranges", false}, {"ranges", true},
            {"caches", false}, {"caches", true}
        };
    }

    @Test(groups = "unit", dataProvider = "missingSessionMetadata")
    public void missingMetadataPreservesAutomaticFallbackButNeverSubstitutesExplicitProgress(
        String missing, boolean explicit) throws Exception {
        try (Fixture fixture = new Fixture()) {
            Mockito.when(fixture.sessions.resolveGlobalSessionToken(any())).thenReturn("1:1#900");
            if ("collection".equals(missing)) {
                Mockito.doReturn(Mono.just(new Utils.ValueHolder<>(null)))
                    .when(fixture.collections).resolveCollectionAsync(any(), any());
            } else if ("ranges".equals(missing)) {
                fixture.map = null;
            } else {
                fixture.thin.setCollectionCache(null);
                fixture.thin.setPartitionKeyRangeCache(null);
            }
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, explicit ? ORIGINAL : null, null);
            request.captureOriginalSessionToken();
            request.useThinClientMode = true;
            Mono<?> preparation = prepareSessionToken(fixture.thin, request);
            if (explicit) {
                assertThatThrownBy(() -> preparation.block(Duration.ofSeconds(5)))
                    .isInstanceOf("caches".equals(missing) ? IllegalStateException.class : NotFoundException.class);
                assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(ORIGINAL);
                verify(fixture.sessions, never()).resolveGlobalSessionToken(any());
            } else {
                preparation.block(Duration.ofSeconds(5));
                assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo("1:1#900");
                verify(fixture.sessions).resolveGlobalSessionToken(request);
            }
            verify(fixture.sessions, never()).resolvePartitionLocalSessionToken(any(), any());
        }
    }

    @DataProvider
    public Object[][] automaticRangeSelectors() {
        return new Object[][] {
            {"range", "1"}, {"missingRange", "missing"}, {"logicalKey", "0"}, {"cachedEpk", "1"}, {"noTarget", null}
        };
    }

    @Test(groups = "unit", dataProvider = "automaticRangeSelectors")
    public void automaticResolutionRetainsExistingRangeSelection(String selector, String expectedRange) throws Exception {
        try (Fixture fixture = new Fixture()) {
            Mockito.when(fixture.sessions.resolveGlobalSessionToken(any())).thenReturn("0:1#900,1:1#800");
            Mockito.when(fixture.sessions.resolvePartitionLocalSessionToken(any(), any()))
                .thenReturn(SessionTokenHelper.parse("1#300"));
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, null, null);
            request.useThinClientMode = true;
            if ("range".equals(selector) || "missingRange".equals(selector)) {
                request.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity(expectedRange));
                request.setPartitionKeyInternal(PartitionKeyInternal.fromObjectArray(new Object[] {"customer"}, true));
            } else if ("logicalKey".equals(selector) || "cachedEpk".equals(selector)) {
                request.setPartitionKeyInternal(PartitionKeyInternal.fromObjectArray(new Object[] {"customer"}, true));
                if ("cachedEpk".equals(selector)) {
                    request.setEffectivePartitionKey("90");
                }
            } else {
                request.requestContext.resolvedPartitionKeyRange = fixture.map.tryGetRangeByPartitionKeyRangeId("1");
            }
            prepareSessionToken(fixture.thin, request).block(Duration.ofSeconds(5));
            if (expectedRange == null) {
                assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN))
                    .isEqualTo("0:1#900,1:1#800");
                verify(fixture.sessions).resolveGlobalSessionToken(request);
                verify(fixture.sessions, never()).resolvePartitionLocalSessionToken(any(), any());
            } else {
                assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN))
                    .isEqualTo(expectedRange + ":1#300");
                verify(fixture.sessions).resolvePartitionLocalSessionToken(request, expectedRange);
                verify(fixture.sessions, never()).resolveGlobalSessionToken(any());
            }
            verify(fixture.collections).resolveCollectionAsync(any(), Mockito.eq(request));
            verify(fixture.ranges).tryLookupAsync(any(), Mockito.eq(RID), Mockito.isNull(), Mockito.isNull());
        }
    }

    @Test(groups = "unit")
    public void cancelledMetadataDoesNotSendOrChangeSiblingProgress() {
        try (Fixture fixture = new Fixture()) {
            Sinks.One<Utils.ValueHolder<CollectionRoutingMap>> loading = Sinks.one();
            Mockito.when(fixture.ranges.tryLookupAsync(any(), any(), any(), any())).thenReturn(loading.asMono());
            RxDocumentServiceRequest original = fixture.request(OperationType.Query, ORIGINAL, "0");
            original.requestContext.resolvedPartitionKeyRange = null;
            RxDocumentServiceRequest sibling = original.clone();
            sibling.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity(RID, "1"));
            Disposable cancelled = fixture.processMessage(original).subscribe();
            cancelled.dispose();
            List<Throwable> errors = new ArrayList<>();
            Disposable active = fixture.processMessage(sibling).subscribe(ignored -> { }, errors::add);
            loading.tryEmitValue(new Utils.ValueHolder<>(fixture.map));
            assertThat(errors).isEmpty();
            assertThat(fixture.wireTokens).containsExactly("1:1#200#1=180#2=150");
            assertThat(original.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(ORIGINAL);
            active.dispose();
        }
    }

    @DataProvider
    public Object[][] cachedEffectiveKeys() {
        return new Object[][] {{null, "0"}, {"90", "1"}};
    }

    @Test(groups = "unit", dataProvider = "cachedEffectiveKeys")
    public void fullLogicalKeyReusesOperationComputedEffectivePartitionKey(String effectiveKey, String rangeId) {
        try (Fixture fixture = new Fixture()) {
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, ORIGINAL, null);
            request.setPartitionKeyInternal(PartitionKeyInternal.fromObjectArray(new Object[] {"customer"}, true));
            request.setEffectivePartitionKey(effectiveKey);
            fixture.processMessage(request).block(Duration.ofSeconds(5));
            assertThat(fixture.wireTokens).containsExactly(
                "0".equals(rangeId) ? "0:1#100#1=90#2=-1" : "1:1#200#1=180#2=150");
            assertThat(fixture.endpoints).extracting(URI::getPort).containsExactly(10250);
        }
    }

    @Test(groups = "unit")
    public void explicitHierarchicalPrefixUsesOperationResolvedRange() {
        try (Fixture fixture = new Fixture()) {
            fixture.definition.setKind(PartitionKind.MULTI_HASH);
            fixture.definition.setPaths(Arrays.asList("/tenant", "/user"));
            RxDocumentServiceRequest request = fixture.request(OperationType.Query, ORIGINAL, "1");
            request.setPartitionKeyInternal(PartitionKeyInternal.fromObjectArray(new Object[] {"tenant"}, true));
            request.getHeaders().put(HttpConstants.HttpHeaders.START_EPK, "80");
            request.getHeaders().put(HttpConstants.HttpHeaders.END_EPK, "90");
            request.getHeaders().put(HttpConstants.HttpHeaders.READ_FEED_KEY_TYPE,
                ReadFeedKeyType.EffectivePartitionKeyRange.name());
            fixture.processMessage(request).block(Duration.ofSeconds(5));
            assertThat(fixture.wireTokens).containsExactly("1:1#200#1=180#2=150");
            assertThat(fixture.encodedTokens.get(0).get(RntbdRequestHeader.StartEpk.id()))
                .containsExactly("80".getBytes(StandardCharsets.UTF_8));
            assertThat(fixture.encodedTokens.get(0).get(RntbdRequestHeader.EndEpk.id()))
                .containsExactly("90".getBytes(StandardCharsets.UTF_8));
        }
    }

    @Test(groups = "unit")
    public void eachSubscriptionUsesOperationResolvedRangeAndTransport() {
        try (Fixture fixture = new Fixture()) {
            RxDocumentServiceRequest request = fixture.request(OperationType.Query, ORIGINAL, "0");
            request.getHeaders().put(HttpConstants.HttpHeaders.START_EPK, "10");
            request.getHeaders().put(HttpConstants.HttpHeaders.END_EPK, "20");
            request.getHeaders().put(HttpConstants.HttpHeaders.READ_FEED_KEY_TYPE,
                ReadFeedKeyType.EffectivePartitionKeyRange.name());
            Mono<RxDocumentServiceResponse> operation = fixture.processMessage(request);
            operation.block(Duration.ofSeconds(5));
            assertThat(fixture.encodedTokens.get(0).get(RntbdRequestHeader.StartEpk.id()))
                .containsExactly("10".getBytes(StandardCharsets.UTF_8));
            assertThat(fixture.encodedTokens.get(0).get(RntbdRequestHeader.EndEpk.id()))
                .containsExactly("20".getBytes(StandardCharsets.UTF_8));
            request.getHeaders().put(HttpConstants.HttpHeaders.END_EPK, "FF");
            request.setPartitionKeyRangeIdentity(null);
            request.requestContext.resolvedPartitionKeyRange = null;
            operation.block(Duration.ofSeconds(5));
            assertThat(fixture.sentRequests.get(1).headers().value(HttpConstants.HttpHeaders.START_EPK)).isEqualTo("10");
            assertThat(fixture.sentRequests.get(1).headers().value(HttpConstants.HttpHeaders.END_EPK)).isEqualTo("FF");
            request.getHeaders().put(HttpConstants.HttpHeaders.END_EPK, "20");
            request.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity(RID, "0"));
            request.requestContext.resolvedPartitionKeyRange = fixture.map.getRangeByPartitionKeyRangeId("0");
            operation.block(Duration.ofSeconds(5));
            assertThat(fixture.wireTokens).containsExactly("0:1#100#1=90#2=-1", ORIGINAL, "0:1#100#1=90#2=-1");
            assertThat(fixture.endpoints).extracting(URI::getPort).containsExactly(10250, 443, 10250);
            assertThat(request.useGatewayMode).isFalse();
        }
    }

    @Test(groups = "unit")
    public void unresolvedHierarchicalKeyAndConflictingIdentitySelectClassicBeforeDispatch() {
        try (Fixture fixture = new Fixture()) {
            fixture.definition.setKind(PartitionKind.MULTI_HASH);
            fixture.definition.setPaths(Arrays.asList("/tenant", "/user"));
            RxDocumentServiceRequest request = fixture.request(OperationType.Query, ORIGINAL, null);
            request.setPartitionKeyInternal(PartitionKeyInternal.fromObjectArray(new Object[] {"tenant"}, true));
            fixture.processMessage(request).block(Duration.ofSeconds(5));
            RxDocumentServiceRequest conflicting = fixture.request(OperationType.Read, ORIGINAL, "0");
            conflicting.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity("other-collection", "0"));
            fixture.processMessage(conflicting).block(Duration.ofSeconds(5));
            assertThat(fixture.wireTokens).containsExactly(ORIGINAL, ORIGINAL);
            assertThat(fixture.endpoints).extracting(URI::getPort).containsExactly(443, 443);
        }
    }

    @DataProvider
    public Object[][] singleTokens() {
        return new Object[][] {{"1:1#200"}, {"1:1#200#1=195#2=180"}, {"1:future-format"}};
    }

    @Test(groups = "unit", dataProvider = "singleTokens")
    public void singleTokenAndClassicCompoundPassThroughWithoutRangeLookup(String token) {
        try (Fixture fixture = new Fixture()) {
            RxDocumentServiceRequest single = fixture.request(OperationType.Read, token, "1");
            fixture.processMessage(single).block(Duration.ofSeconds(5));
            fixture.classic.processMessage(fixture.request(OperationType.Read, ORIGINAL, "0"))
                .block(Duration.ofSeconds(5));
            assertThat(fixture.wireTokens).containsExactly(token, ORIGINAL);
            verify(fixture.ranges, never()).tryLookupAsync(any(), any(), any(), any());
        }
    }

    @DataProvider
    public Object[][] absentTokens() {
        return new Object[][] {{null}, {""}};
    }

    @Test(groups = "unit", dataProvider = "absentTokens")
    public void absentCallerTokenStillUsesAutomaticSessionResolution(String token) {
        try (Fixture fixture = new Fixture()) {
            ISessionToken ambient = SessionTokenHelper.parse("1#300#1=290#2=280");
            Mockito.when(fixture.sessions.resolvePartitionLocalSessionToken(any(), any())).thenReturn(ambient);
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, token, null);
            request.setPartitionKeyInternal(PartitionKeyInternal.fromObjectArray(new Object[] {"customer"}, true));
            fixture.processMessage(request).block(Duration.ofSeconds(5));
            assertThat(fixture.wireTokens).containsExactly("0:1#300#1=290#2=280");
            assertThat(request.getOriginalSessionToken()).isEqualTo(token);
            verify(fixture.sessions).resolvePartitionLocalSessionToken(request, "0");
            verify(fixture.ranges).tryLookupAsync(any(), any(), any(), any());
        }
    }

    @Test(groups = "unit")
    public void nonSessionConsistencyDoesNotResolveOrSendExplicitProgress() {
        try (Fixture fixture = new Fixture()) {
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, ORIGINAL, "0");
            request.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, "Eventual");
            fixture.processMessage(request).block(Duration.ofSeconds(5));
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
        final GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover automaticFailover =
            Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover.class);
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
        final Sinks.Empty<Void> primaryRequestSent = Sinks.empty();

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
                if (request.requestContext.regionalRoutingContextToRoute != null) {
                    return request.requestContext.regionalRoutingContextToRoute;
                }
                List<String> excluded = request.requestContext.getExcludeRegions();
                return excluded != null && excluded.contains("East US") ? secondary : region;
            });
            Mockito.when(endpointManager.hasThinClientReadLocations()).thenReturn(true);
            Mockito.when(endpointManager.getProxyProbeDecision()).thenReturn(true);
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
                    if ("localhost".equals(request.uri().getHost())) {
                        primaryRequestSent.tryEmitEmpty();
                    }
                    return new HttpResponse() {
                        @Override public int statusCode() { return 200; }
                        @Override public String headerValue(String name) { return null; }
                        @Override public HttpHeaders headers() { return new HttpHeaders(); }
                        @Override public Mono<ByteBuf> body() { return Mono.just(Unpooled.EMPTY_BUFFER); }
                        @Override public Mono<String> bodyAsString() { return Mono.just(""); }
                        @Override public void close() { }
                    }.withRequest(request);
                }).flatMap(response -> {
                    if (!delayFirstResponse) {
                        return Mono.just(response);
                    }
                    return "localhost".equals(request.uri().getHost())
                        ? Mono.never() : primaryRequestSent.asMono().thenReturn(response);
                });
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
        }

        DocumentClientRetryPolicy retryPolicy() {
            ConnectionPolicy connection = Mockito.mock(ConnectionPolicy.class);
            Mockito.when(connection.getThrottlingRetryOptions()).thenReturn(new ThrottlingRetryOptions());
            Mockito.when(connection.isEndpointDiscoveryEnabled()).thenReturn(true);
            return new ResetSessionTokenRetryPolicyFactory(sessions, collections,
                new RetryPolicy(context, endpointManager, connection,
                    Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.class), automaticFailover))
                .getRequestPolicy(null);
        }

        RxDocumentClientImpl client() throws ReflectiveOperationException {
            RxDocumentClientImpl client = Mockito.mock(RxDocumentClientImpl.class, Mockito.CALLS_REAL_METHODS);
            ThinClientConnectivityConfig connectivity = Mockito.mock(ThinClientConnectivityConfig.class);
            Mockito.when(connectivity.canThinClientBeUsed()).thenReturn(true);
            setField(client, "thinClientConnectivityConfig", connectivity);
            setField(client, "globalEndpointManager", endpointManager);
            setField(client, "thinProxy", thin);
            setField(client, "gatewayProxy", classic);
            setField(client, "storeModel", classic);
            return client;
        }

        RxDocumentClientImpl fullClient() throws ReflectiveOperationException {
            RxDocumentClientImpl client = client();
            Mockito.doAnswer(ignored -> context.createDiagnostics()).when(client).createDiagnostics();
            ConnectionPolicy connection = Mockito.mock(ConnectionPolicy.class);
            Mockito.when(connection.getThrottlingRetryOptions()).thenReturn(new ThrottlingRetryOptions());
            Mockito.when(connection.isEndpointDiscoveryEnabled()).thenReturn(true);
            GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker circuitBreaker =
                Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.class);
            setField(client, "collectionCache", collections);
            setField(client, "partitionKeyRangeCache", ranges);
            setField(client, "sessionContainer", sessions);
            setField(client, "globalPartitionEndpointManagerForPerPartitionCircuitBreaker", circuitBreaker);
            setField(client, "globalPartitionEndpointManagerForPerPartitionAutomaticFailover", automaticFailover);
            setField(client, "connectionPolicy", connection);
            setField(client, "resetSessionTokenRetryPolicy", new ResetSessionTokenRetryPolicyFactory(
                sessions, collections, new RetryPolicy(context, endpointManager, connection, circuitBreaker, automaticFailover)));
            return client;
        }

        private Mono<RxDocumentServiceResponse> processMessage(RxDocumentServiceRequest request) {
            return Mono.defer(() -> {
                try {
                    return dispatch(client(), request);
                } catch (ReflectiveOperationException e) {
                    return Mono.error(e);
                }
            });
        }

        void inject(IServerErrorInjector injector) {
            IFaultInjectorProvider provider = Mockito.mock(IFaultInjectorProvider.class);
            Mockito.when(provider.getServerErrorInjector()).thenReturn(injector);
            thin.configureFaultInjectorProvider(provider, new Configs());
            classic.configureFaultInjectorProvider(provider, new Configs());
        }

        RxDocumentServiceRequest request(OperationType operation, String token, String rangeId) {
            RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(
                context, operation, "/dbs/db/colls/coll/docs/item", ResourceType.Document);
            request.setResourceId(RID);
            request.requestContext.resolvedCollectionRid = RID;
            request.requestContext.cosmosDiagnostics = context.createDiagnostics();
            request.setPartitionKeyDefinition(definition);
            request.setResponseTimeout(Duration.ofSeconds(5));
            request.getHeaders().put(HttpConstants.HttpHeaders.SESSION_TOKEN, token);
            if (operation == OperationType.ReadFeed) {
                request.getHeaders().put(HttpConstants.HttpHeaders.A_IM, HttpConstants.A_IMHeaderValues.INCREMENTAL_FEED);
            }
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
