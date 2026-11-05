// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
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
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

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
    public Object[][] fallbackOperations() {
        return new Object[][] {
            {OperationType.Read, false}, {OperationType.Read, true},
            {OperationType.Query, false}, {OperationType.Query, true},
            {OperationType.Batch, false}, {OperationType.Create, true}
        };
    }

    @Test(groups = "unit", dataProvider = "fallbackOperations")
    public void injectedScopeFailureRetriesClassicOnceInSameRegion(
        OperationType operation, boolean recreateRequest) throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.thin.setUseMultipleWriteLocations(true);
            fixture.classic.setUseMultipleWriteLocations(true);
            DocumentClientRetryPolicy policy = fixture.retryPolicy();
            RxDocumentClientImpl client = fixture.client();
            RxDocumentServiceRequest original = fixture.request(operation, ORIGINAL, "0");
            original.setContentBytes("{\"id\":\"item\",\"pk\":\"customer\"}".getBytes(StandardCharsets.UTF_8));
            original.setResponseTimeout(Duration.ofSeconds(5));
            original.requestContext.setExcludeRegions(Collections.singletonList("North Europe"));
            List<URI> attemptedEndpoints = new CopyOnWriteArrayList<>();
            AtomicInteger injected = new AtomicInteger();
            IServerErrorInjector injector = Mockito.mock(IServerErrorInjector.class);
            Mockito.when(injector.injectServerResponseError(any(), any())).thenAnswer(invocation -> {
                FaultInjectionRequestArgs args = invocation.getArgument(0);
                attemptedEndpoints.add(args.getRequestURI());
                if (args.getRequestURI().getPort() == 10250) {
                    injected.incrementAndGet();
                    Utils.ValueHolder<CosmosException> failure = invocation.getArgument(1);
                    failure.v = new GatewayV2SessionTokenRetryPolicy.GatewayFallbackRequiredException();
                    fixture.preferSecondary = true;
                    return true;
                }
                return false;
            });
            fixture.inject(injector);
            List<RxDocumentServiceRequest> attempts = new ArrayList<>();
            BackoffRetryUtility.executeRetry(() -> {
                RxDocumentServiceRequest request = recreateRequest ? original.clone() : original;
                Duration timeoutBeforePolicy = request.getResponseTimeout();
                policy.onBeforeSendRequest(request);
                assertThat(request.getResponseTimeout()).isEqualTo(timeoutBeforePolicy);
                attempts.add(request);
                return dispatch(client, request);
            }, policy).block(Duration.ofSeconds(5));

            assertThat(injected.get()).isEqualTo(1);
            assertThat(attempts).hasSize(2);
            assertThat(attemptedEndpoints).extracting(URI::getPort).containsExactly(10250, 443);
            assertThat(attemptedEndpoints).extracting(URI::getHost).containsExactly("localhost", "localhost");
            assertThat(fixture.wireTokens).containsExactly(ORIGINAL);
            assertThat(fixture.sentRequests).hasSize(1);
            assertThat(attempts.get(1).getContentAsByteArray()).containsExactly(original.getContentAsByteArray());
            assertThat(attempts.get(1).getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(ORIGINAL);
            assertThat(attempts.get(1).useThinClientMode).isFalse();
            assertThat(attempts.get(1).useGatewayMode).isTrue();
            assertThat(policy.getRetryContext().getRetryCount()).isEqualTo(1);
            verify(fixture.endpointManager, never()).markEndpointUnavailableForRead(any());
            verify(fixture.endpointManager, never()).markEndpointUnavailableForWrite(any());
            verify(fixture.endpointManager, never()).refreshLocationAsync(any(), Mockito.anyBoolean());
            verify(fixture.automaticFailover, never())
                .tryMarkEndpointAsUnavailableForPartitionKeyRange(any(), Mockito.anyBoolean());

            // A sibling operation's policy must not inherit the transport decision.
            fixture.preferSecondary = false;
            RxDocumentServiceRequest sibling = fixture.request(operation, ORIGINAL, "0");
            fixture.retryPolicy().onBeforeSendRequest(sibling);
            assertThat(sibling.useGatewayMode).isFalse();
        }
    }

    @Test(groups = "unit")
    public void unresolvedScopeSignalsPolicyWithoutSendingThinRequest() throws Exception {
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
        }
    }

    @Test(groups = "unit")
    public void injectedRepeatedFallbackStopsAndOrdinaryBadRequestDoesNotFallback() throws Exception {
        for (boolean fallback : new boolean[] {false, true}) {
            try (Fixture fixture = new Fixture()) {
                List<CosmosException> injectedErrors = new CopyOnWriteArrayList<>();
                AtomicInteger injected = new AtomicInteger();
                IServerErrorInjector injector = Mockito.mock(IServerErrorInjector.class);
                Mockito.when(injector.injectServerResponseError(any(), any())).thenAnswer(invocation -> {
                    injected.incrementAndGet();
                    Utils.ValueHolder<CosmosException> failure = invocation.getArgument(1);
                    failure.v = fallback
                        ? new GatewayV2SessionTokenRetryPolicy.GatewayFallbackRequiredException()
                        : new BadRequestException("Invalid caller input");
                    injectedErrors.add(failure.v);
                    return true;
                });
                fixture.inject(injector);
                DocumentClientRetryPolicy policy = fixture.retryPolicy();
                RxDocumentClientImpl client = fixture.client();
                RxDocumentServiceRequest request = fixture.request(OperationType.Read, ORIGINAL, "0");
                Throwable failure = catchThrowable(() -> BackoffRetryUtility.executeRetry(() -> {
                    policy.onBeforeSendRequest(request);
                    return dispatch(client, request);
                }, policy).block(Duration.ofSeconds(5)));
                assertThat(injected.get()).as("Injected errors; fallback=%s, failure=%s", fallback, failure)
                    .isEqualTo(fallback ? 2 : 1);
                assertThat(failure).isSameAs(injectedErrors.get(injectedErrors.size() - 1));
                assertThat(fixture.sentRequests).isEmpty();
                assertThat(request.useGatewayMode).isEqualTo(fallback);
            }
        }
    }

    @Test(groups = "unit")
    public void unrelatedErrorsStillReachExistingRetryHierarchy() throws Exception {
        try (Fixture fixture = new Fixture()) {
            DocumentClientRetryPolicy policy = fixture.retryPolicy();
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, ORIGINAL, "0");
            policy.onBeforeSendRequest(request);
            request.captureOriginalSessionToken();
            request.useThinClientMode = true;
            assertThat(policy.shouldRetry(new GatewayV2SessionTokenRetryPolicy.GatewayFallbackRequiredException())
                .block().shouldRetry).isTrue();
            fixture.preferSecondary = true;
            policy.onBeforeSendRequest(request);
            assertThat(request.requestContext.regionalRoutingContextToRoute.getGatewayRegionalEndpoint().getHost())
                .isEqualTo("localhost");
            assertThat(policy.shouldRetry(new RequestRateTooLargeException()).block().shouldRetry).isTrue();
            assertThat(request.useGatewayMode).isTrue();
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
        }
    }

    @DataProvider
    public Object[][] ineligibleFallbackRequests() {
        return new Object[][] {
            {null, true, false}, {"0:1#100", true, false}, {ORIGINAL, false, false}, {ORIGINAL, true, true}
        };
    }

    @Test(groups = "unit", dataProvider = "ineligibleFallbackRequests")
    public void fallbackSignalDoesNotRetryNonCompoundOrNonThinRequests(
        String token, boolean thin, boolean classic) {
        try (Fixture fixture = new Fixture()) {
            DocumentClientRetryPolicy policy = fixture.retryPolicy();
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, token, "0");
            policy.onBeforeSendRequest(request);
            request.captureOriginalSessionToken();
            request.useThinClientMode = thin;
            request.useGatewayMode = classic;
            assertThat(policy.shouldRetry(new GatewayV2SessionTokenRetryPolicy.GatewayFallbackRequiredException())
                .block().shouldRetry).isFalse();
        }
    }

    private static Mono<RxDocumentServiceResponse> dispatch(
        RxDocumentClientImpl client, RxDocumentServiceRequest request) throws Exception {
        Method method = RxDocumentClientImpl.class.getDeclaredMethod("getStoreProxy", RxDocumentServiceRequest.class);
        method.setAccessible(true);
        return ((RxStoreModel) method.invoke(client, request)).processMessage(request);
    }

    @DataProvider
    public Object[][] hedges() {
        return new Object[][] {
            {false, ORIGINAL, "0:1#100#1=90#2=-1", false},
            {true, ORIGINAL, "0:1#100#1=90#2=-1", false},
            {false, "0:1#100,unknown:1#200", "0:1#100", false},
            {true, "0:1#100,unknown:1#200", "0:1#100", false},
            {false, "1:1#100,unknown:1#200", null, false},
            {true, "1:1#100,unknown:1#200", null, false},
            {false, ORIGINAL, "0:1#100#1=90#2=-1", true},
            {true, ORIGINAL, "0:1#100#1=90#2=-1", true}
        };
    }

    @Test(groups = "unit", dataProvider = "hedges")
    public void actualAvailabilityStrategiesPreserveBothAttempts(
        boolean feed, String original, String expected, boolean injectFallback)
        throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.delayFirstResponse = true;
            RxDocumentClientImpl client = fixture.client();
            setField(client, "globalPartitionEndpointManagerForPerPartitionCircuitBreaker",
                Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.class));
            AtomicInteger injected = new AtomicInteger();
            if (injectFallback) {
                IServerErrorInjector injector = Mockito.mock(IServerErrorInjector.class);
                Mockito.when(injector.injectServerResponseError(any(), any())).thenAnswer(invocation -> {
                    FaultInjectionRequestArgs args = invocation.getArgument(0);
                    if ("localhost".equals(args.getRequestURI().getHost()) && args.getRequestURI().getPort() == 10250) {
                        injected.incrementAndGet();
                        Utils.ValueHolder<CosmosException> failure = invocation.getArgument(1);
                        failure.v = new GatewayV2SessionTokenRetryPolicy.GatewayFallbackRequiredException();
                        return true;
                    }
                    return false;
                });
                fixture.inject(injector);
            }
            CosmosEndToEndOperationLatencyPolicyConfig policy =
                new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(5))
                    .availabilityStrategy(new ThresholdBasedAvailabilityStrategy(
                        Duration.ofMillis(10), Duration.ofMillis(10))).build();
            List<RxDocumentServiceRequest> attempts = new CopyOnWriteArrayList<>();
            if (feed) {
                RxDocumentServiceRequest parent = fixture.request(OperationType.Query, original, "0");
                parent.requestContext.setEndToEndOperationLatencyPolicyConfig(policy);
                client.executeFeedOperationWithAvailabilityStrategy(
                    ResourceType.Document, OperationType.Query, fixture::retryPolicy,
                    parent, (retry, request) -> {
                        attempts.add(request);
                        DocumentClientRetryPolicy retryPolicy = retry.get();
                        return BackoffRetryUtility.executeRetry(() -> {
                            retryPolicy.onBeforeSendRequest(request);
                            return dispatch(client, request);
                        }, retryPolicy);
                    }, "/dbs/db/colls/coll").block(Duration.ofSeconds(5));
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
                        DocumentClientRetryPolicy retryPolicy = fixture.retryPolicy();
                        return BackoffRetryUtility.executeRetry(() -> {
                            retryPolicy.onBeforeSendRequest(request);
                            return dispatch(client, request);
                        }, retryPolicy)
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
            }
            assertThat(attempts.get(0).useGatewayMode).isEqualTo(injectFallback);
            assertThat(attempts.get(1).useGatewayMode).isFalse();
            assertThat(injected.get()).isEqualTo(injectFallback ? 1 : 0);
            assertThat(fixture.wireTokens).containsExactlyInAnyOrder(injectFallback ? original : expected, expected);
            assertThat(fixture.endpoints).extracting(URI::getHost).containsExactlyInAnyOrder("localhost", "secondary");
            assertThat(fixture.endpoints).extracting(URI::getPort)
                .containsExactlyInAnyOrder(injectFallback ? 443 : 10250, 10250);
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
    public Object[][] directModeTokens() {
        return new Object[][] {
            {"0:1#100,unknown:1#200", "0:1#100"}, {"0:1#100,0:1#200", "0:1#200"},
            {"1:1#100,1:1#200", null}, {"0:1#100,", "0:1#100"}
        };
    }

    @Test(groups = "unit", dataProvider = "directModeTokens")
    public void selectsOrOmitsTokenLikeDirectModeWithoutChangingTransport(String token, String expected) {
        try (Fixture fixture = new Fixture()) {
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, token, "0");
            fixture.thin.processMessage(request).block(Duration.ofSeconds(5));
            assertThat(fixture.wireTokens).containsExactly(expected);
            assertThat(fixture.endpoints).allMatch(uri -> uri.getPort() == 10250);
            assertThat(request.useThinClientMode).isTrue();
            assertThat(request.useGatewayMode).isFalse();
            assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(token);
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
            String original = "1:1#200,unknown:1#300";
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, original, "0");
            fixture.thin.processMessage(request).block(Duration.ofSeconds(5));
            request.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity(RID, "1"));
            fixture.thin.processMessage(request).block(Duration.ofSeconds(5));
            request.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity(RID, "0"));
            fixture.thin.processMessage(request).block(Duration.ofSeconds(5));
            assertThat(fixture.wireTokens).containsExactly(null, "1:1#200", null);
            assertThat(fixture.endpoints).extracting(URI::getPort).containsExactly(10250, 10250, 10250);
            assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(original);
        }
    }

    @Test(groups = "unit")
    public void malformedTokenFailsWithoutSendingToEitherTransport() {
        try (Fixture fixture = new Fixture()) {
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, "0:1#100,1:future", "0");
            assertThatThrownBy(() -> fixture.thin.processMessage(request).block(Duration.ofSeconds(5)))
                .hasRootCauseInstanceOf(BadRequestException.class);
            assertThat(fixture.sentRequests).isEmpty();
        }
    }

    @Test(groups = "unit")
    public void missingMetadataIsNotAnUnmatchedToken() {
        try (Fixture fixture = new Fixture()) {
            RxDocumentServiceRequest request = fixture.request(OperationType.Read, ORIGINAL, "0");
            fixture.map = null;
            assertThatThrownBy(() -> fixture.thin.processMessage(request).block(Duration.ofSeconds(5)))
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
    public void exactFeedScopeIsPreservedAndUnresolvedMultiRangeScopeFails() {
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
            assertThatThrownBy(() -> fixture.thin.processMessage(request).block(Duration.ofSeconds(5)))
                .isInstanceOf(BadRequestException.class);
            assertThat(fixture.wireTokens).containsExactly("0:1#100#1=90#2=-1");
        }
    }

    @Test(groups = "unit")
    public void unresolvedHierarchicalKeyAndConflictingIdentityFailWithoutTransportFallback() {
        try (Fixture fixture = new Fixture()) {
            fixture.definition.setKind(PartitionKind.MULTI_HASH);
            fixture.definition.setPaths(Arrays.asList("/tenant", "/user"));
            RxDocumentServiceRequest request = fixture.request(OperationType.Query, ORIGINAL, null);
            request.setPartitionKeyInternal(PartitionKeyInternal.fromObjectArray(new Object[] {"tenant"}, true));
            assertThatThrownBy(() -> fixture.thin.processMessage(request).block(Duration.ofSeconds(5)))
                .isInstanceOf(BadRequestException.class);
            RxDocumentServiceRequest conflicting = fixture.request(OperationType.Read, ORIGINAL, "0");
            conflicting.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity("other-collection", "0"));
            assertThatThrownBy(() -> fixture.thin.processMessage(conflicting).block(Duration.ofSeconds(5)))
                .isInstanceOf(BadRequestException.class);
            assertThat(fixture.sentRequests).isEmpty();
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
            fixture.thin.processMessage(single).block(Duration.ofSeconds(5));
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
            fixture.thin.processMessage(request).block(Duration.ofSeconds(5));
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
        boolean preferSecondary;
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
                return preferSecondary || excluded != null && excluded.contains("East US") ? secondary : region;
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

        RxDocumentClientImpl client() throws Exception {
            RxDocumentClientImpl client = Mockito.mock(RxDocumentClientImpl.class, Mockito.CALLS_REAL_METHODS);
            ThinClientConnectivityConfig connectivity = Mockito.mock(ThinClientConnectivityConfig.class);
            Mockito.when(connectivity.canThinClientBeUsed()).thenReturn(true);
            setField(client, "thinClientConnectivityConfig", connectivity);
            setField(client, "globalEndpointManager", endpointManager);
            setField(client, "thinProxy", thin);
            setField(client, "gatewayProxy", classic);
            return client;
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
