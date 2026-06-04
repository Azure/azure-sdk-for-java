// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.ReadConsistencyStrategy;
import com.azure.cosmos.implementation.directconnectivity.GatewayServiceConfigurationReader;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ConnectTimeoutException;
import io.netty.handler.timeout.ReadTimeoutException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.lang.reflect.Method;
import java.net.SocketException;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;

public class RxGatewayStoreModelTest {
    private final static int TIMEOUT = 10000;

    @DataProvider(name = "sessionTokenConfigProvider")
    public Object[][] sessionTokenConfigProvider() {
        return new Object[][]{
            // defaultConsistencyLevel, requestConsistencyLevel,requestOperationType, requestResourceType, sessionTokenFromUser, finalSessionTokenType

            // Skip applying session token for master operation
            {ConsistencyLevel.SESSION, null, OperationType.Read, ResourceType.Offer, true, SessionTokenType.NONE},
            {ConsistencyLevel.SESSION, null, OperationType.Read, ResourceType.Database, true, SessionTokenType.NONE},
            {ConsistencyLevel.SESSION, null, OperationType.Read, ResourceType.User, true, SessionTokenType.NONE},
            {ConsistencyLevel.SESSION, null, OperationType.Read, ResourceType.UserDefinedType, true, SessionTokenType.NONE},
            {ConsistencyLevel.SESSION, null, OperationType.Read, ResourceType.Permission, true, SessionTokenType.NONE},
            {ConsistencyLevel.SESSION, null, OperationType.Read, ResourceType.Topology, true, SessionTokenType.NONE},
            {ConsistencyLevel.SESSION, null, OperationType.Read, ResourceType.DatabaseAccount, true, SessionTokenType.NONE},
            {ConsistencyLevel.SESSION, null, OperationType.ReadFeed, ResourceType.PartitionKeyRange, false, SessionTokenType.NONE},
            {ConsistencyLevel.SESSION, null, OperationType.Read, ResourceType.DocumentCollection, false, SessionTokenType.NONE},
            {ConsistencyLevel.SESSION, null, OperationType.Read, ResourceType.Trigger, false, SessionTokenType.NONE},
            {ConsistencyLevel.SESSION, null, OperationType.Read, ResourceType.UserDefinedFunction, false, SessionTokenType.NONE},
            {ConsistencyLevel.SESSION, null, OperationType.Create, ResourceType.StoredProcedure, false, SessionTokenType.NONE},
            {ConsistencyLevel.SESSION, null, OperationType.QueryPlan, ResourceType.Document, false, SessionTokenType.NONE},

            // skip applying the session token when Eventual Consistency is explicitly requested
            // on request-level for data plane operations.
            {ConsistencyLevel.SESSION, ConsistencyLevel.EVENTUAL, OperationType.Read, ResourceType.Document, false, SessionTokenType.NONE},
            {ConsistencyLevel.SESSION, ConsistencyLevel.EVENTUAL, OperationType.Query, ResourceType.Document, true, SessionTokenType.NONE},

            // skip applying the session token if default and request consistency level is not session
            {ConsistencyLevel.EVENTUAL, null, OperationType.Read, ResourceType.Document, false, SessionTokenType.NONE},
            {ConsistencyLevel.BOUNDED_STALENESS, null, OperationType.Create, ResourceType.Document, true, SessionTokenType.NONE},
            {ConsistencyLevel.STRONG, null, OperationType.Query, ResourceType.Document, false, SessionTokenType.NONE},
            {ConsistencyLevel.CONSISTENT_PREFIX, null, OperationType.Delete, ResourceType.Document, true, SessionTokenType.NONE},

            // Apply session token for other scenarios
            {ConsistencyLevel.SESSION, null, OperationType.Read, ResourceType.Document, true, SessionTokenType.USER},
            {ConsistencyLevel.BOUNDED_STALENESS, ConsistencyLevel.SESSION, OperationType.Create, ResourceType.Document, true, SessionTokenType.USER},
            {ConsistencyLevel.SESSION, ConsistencyLevel.SESSION, OperationType.Query, ResourceType.Document, false, SessionTokenType.SDK},
            {ConsistencyLevel.STRONG, ConsistencyLevel.SESSION, OperationType.ExecuteJavaScript, ResourceType.StoredProcedure, false, SessionTokenType.SDK}
        };
    }

    @Test(groups = "unit")
    public void readTimeout() throws Exception {
        DiagnosticsClientContext clientContext = mockDiagnosticsClientContext();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        QueryCompatibilityMode queryCompatibilityMode = QueryCompatibilityMode.Default;
        UserAgentContainer userAgentContainer = new UserAgentContainer();
        GlobalEndpointManager globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);

        RegionalRoutingContext regionalRoutingContext = new RegionalRoutingContext(new URI("https://localhost"));

        Mockito.doReturn(regionalRoutingContext)
                .when(globalEndpointManager).resolveServiceEndpoint(any());
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        Mockito.doReturn(Mono.error(ReadTimeoutException.INSTANCE))
                .when(httpClient).send(any(HttpRequest.class), any(Duration.class));

        GatewayServiceConfigurationReader gatewayServiceConfigurationReader = Mockito.mock(GatewayServiceConfigurationReader.class);
        Mockito.doReturn(ConsistencyLevel.SESSION)
            .when(gatewayServiceConfigurationReader).getDefaultConsistencyLevel();
        RxGatewayStoreModel storeModel = new RxGatewayStoreModel(clientContext,
                sessionContainer,
                ConsistencyLevel.SESSION,
                queryCompatibilityMode,
                userAgentContainer,
                globalEndpointManager,
                httpClient,
            null,
            null);
        storeModel.setGatewayServiceConfigurationReader(gatewayServiceConfigurationReader);

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(clientContext,
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.getHeaders().put("key", "value");
        dsr.requestContext = new DocumentServiceRequestContext();
        dsr.requestContext.regionalRoutingContextToRoute = regionalRoutingContext;

        Mono<RxDocumentServiceResponse> resp = storeModel.processMessage(dsr);
        validateFailure(resp, FailureValidator.builder()
                .instanceOf(CosmosException.class)
                .causeInstanceOf(ReadTimeoutException.class)
                .documentClientExceptionHeaderRequestContainsEntry("key", "value")
                .statusCode(HttpConstants.StatusCodes.REQUEST_TIMEOUT)
                .subStatusCode(HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT)
                .build());
    }

    @Test(groups = "unit")
    public void serviceUnavailable() throws Exception {
        DiagnosticsClientContext clientContext = mockDiagnosticsClientContext();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        QueryCompatibilityMode queryCompatibilityMode = QueryCompatibilityMode.Default;
        UserAgentContainer userAgentContainer = new UserAgentContainer();
        GlobalEndpointManager globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);

        RegionalRoutingContext regionalRoutingContext = new RegionalRoutingContext(new URI("https://localhost"));
        Mockito.doReturn(regionalRoutingContext)
               .when(globalEndpointManager).resolveServiceEndpoint(any());
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        Mockito.doReturn(Mono.error(new SocketException("Dummy SocketException")))
               .when(httpClient).send(any(HttpRequest.class), any(Duration.class));

        GatewayServiceConfigurationReader gatewayServiceConfigurationReader = Mockito.mock(GatewayServiceConfigurationReader.class);
        Mockito.doReturn(ConsistencyLevel.SESSION)
               .when(gatewayServiceConfigurationReader).getDefaultConsistencyLevel();
        RxGatewayStoreModel storeModel = new RxGatewayStoreModel(clientContext,
            sessionContainer,
            ConsistencyLevel.SESSION,
            queryCompatibilityMode,
            userAgentContainer,
            globalEndpointManager,
            httpClient,
            null,
            null);
        storeModel.setGatewayServiceConfigurationReader(gatewayServiceConfigurationReader);

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(clientContext,
            OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.getHeaders().put("key", "value");
        dsr.requestContext = new DocumentServiceRequestContext();
        dsr.requestContext.regionalRoutingContextToRoute = regionalRoutingContext;

        Mono<RxDocumentServiceResponse> resp = storeModel.processMessage(dsr);
        validateFailure(resp, FailureValidator.builder()
                                              .instanceOf(CosmosException.class)
                                              .causeInstanceOf(SocketException.class)
                                              .documentClientExceptionHeaderRequestContainsEntry("key", "value")
                                              .statusCode(HttpConstants.StatusCodes.SERVICE_UNAVAILABLE)
                                              .subStatusCode(HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE)
                                              .build());
    }

    @Test(groups = "unit", dataProvider = "sessionTokenConfigProvider")
    public void applySessionToken(
        ConsistencyLevel defaultConsistency,
        ConsistencyLevel requestConsistency,
        OperationType operationType,
        ResourceType resourceType,
        boolean sessionTokenFromUser,
        SessionTokenType finalSessionTokenType) throws Exception {

        String sdkGlobalSessionToken = "1#100#1=20#2=5#3=30";
        String userControlledSessionToken = "1#99";
        ApiType apiType = ApiType.SQL;
        DiagnosticsClientContext clientContext = mockDiagnosticsClientContext();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        Mockito.doReturn(sdkGlobalSessionToken).when(sessionContainer).resolveGlobalSessionToken(any());

        GlobalEndpointManager globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);

        URI locationEndpointToRoute = new URI("https://localhost");
        RegionalRoutingContext regionalRoutingContext = new RegionalRoutingContext(locationEndpointToRoute);

        Mockito.doReturn(regionalRoutingContext)
            .when(globalEndpointManager).resolveServiceEndpoint(any());

        HttpClient httpClient = Mockito.mock(HttpClient.class);
        Mockito.doReturn(Mono.error(ReadTimeoutException.INSTANCE))
            .when(httpClient).send(any(HttpRequest.class), any(Duration.class));

        GatewayServiceConfigurationReader gatewayServiceConfigurationReader = Mockito.mock(GatewayServiceConfigurationReader.class);
        Mockito.doReturn(defaultConsistency)
            .when(gatewayServiceConfigurationReader).getDefaultConsistencyLevel();

        RxGatewayStoreModel storeModel = new RxGatewayStoreModel(
            clientContext,
            sessionContainer,
            defaultConsistency,
            QueryCompatibilityMode.Default,
            new UserAgentContainer(),
            globalEndpointManager,
            httpClient,
            apiType,
            null);
        storeModel.setGatewayServiceConfigurationReader(gatewayServiceConfigurationReader);

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(
            clientContext,
            operationType,
            "/fakeResourceFullName",
            resourceType);

        if (resourceType != ResourceType.DatabaseAccount) {
            dsr.requestContext.regionalRoutingContextToRoute = regionalRoutingContext;
        } else {
            dsr.setEndpointOverride(locationEndpointToRoute);
        }

        if (sessionTokenFromUser) {
            dsr.getHeaders().put(HttpConstants.HttpHeaders.SESSION_TOKEN, userControlledSessionToken);
        }
        if (requestConsistency != null) {
            dsr.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, requestConsistency.toString());
        }

        Mono<RxDocumentServiceResponse> resp = storeModel.processMessage(dsr);
        validateFailure(resp, FailureValidator.builder()
            .instanceOf(CosmosException.class)
            .causeInstanceOf(ReadTimeoutException.class)
            .statusCode(HttpConstants.StatusCodes.REQUEST_TIMEOUT).build());

        if (finalSessionTokenType == SessionTokenType.USER) {
            // Session token is passed only for read request, unless its batch operation, or its multi master create
            if(!dsr.isReadOnlyRequest() && dsr.getOperationType() != OperationType.Batch){
                assertThat(dsr.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isNull();
            } else {
                assertThat(dsr.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(userControlledSessionToken);
            }
        } else if(finalSessionTokenType == SessionTokenType.SDK) {
            // Session token is passed only for read request, unless its batch operation, or its multi master create
            if(!dsr.isReadOnlyRequest() && dsr.getOperationType() != OperationType.Batch){
                assertThat(dsr.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isNull();
            } else {
                assertThat(dsr.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(sdkGlobalSessionToken);
            }
        } else {
            assertThat(dsr.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isNull();
        }
    }

    @Test(groups = "unit")
    public void validateApiType() throws Exception {
        String sdkGlobalSessionToken = "1#100#1=20#2=5#3=30";
        ApiType apiType = ApiType.SQL;
        DiagnosticsClientContext clientContext = mockDiagnosticsClientContext();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        Mockito.doReturn(sdkGlobalSessionToken).when(sessionContainer).resolveGlobalSessionToken(any());

        GlobalEndpointManager globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);

        Mockito.doReturn(new RegionalRoutingContext(new URI("https://localhost")))
            .when(globalEndpointManager).resolveServiceEndpoint(any());

        HttpClient httpClient = Mockito.mock(HttpClient.class);
        ArgumentCaptor<HttpRequest> httpClientRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        Mockito.when(httpClient.send(any(), any())).thenReturn(Mono.error(new ConnectTimeoutException()));

        RxGatewayStoreModel storeModel = new RxGatewayStoreModel(
            clientContext,
            sessionContainer,
            ConsistencyLevel.SESSION,
            QueryCompatibilityMode.Default,
            new UserAgentContainer(),
            globalEndpointManager,
            httpClient,
            apiType,
            null);

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(
            clientContext,
            OperationType.Query,
            "/fakeResourceFullName",
            ResourceType.Document);

        try {
            storeModel.performRequest(dsr).block();
            fail("Request should fail");
        } catch (Exception e) {
            //no-op
        }

        Mockito.verify(httpClient).send(httpClientRequestCaptor.capture(), any());
        HttpRequest httpRequest = httpClientRequestCaptor.getValue();
        HttpHeaders headers = ReflectionUtils.getHttpHeaders(httpRequest);
        assertThat(headers.toMap().get(HttpConstants.HttpHeaders.API_TYPE)).isEqualTo(apiType.toString());
    }

    public void validateFailure(Mono<RxDocumentServiceResponse> observable,
                                FailureValidator validator) {
        validateFailure(observable, validator, TIMEOUT);
    }

    public static void validateFailure(Mono<RxDocumentServiceResponse> observable,
                                       FailureValidator validator,
                                       long timeout) {
        StepVerifier.create(observable)
            .expectErrorSatisfies(validator::validate)
            .verify(Duration.ofMillis(timeout));
    }

    /**
     * Verifies that when a request is cancelled while the retained ByteBuf is queued in
     * publishOn's async boundary, the doFinally safety net properly releases the buffer.
     *
     * Uses a Sinks.One to control body emission timing and a concrete HttpResponse subclass
     * to avoid Mockito final-method interception issues with withRequest().
     *
     * The body's doFinally simulates ByteBufFlux.aggregate()'s auto-release behavior
     * (one release for the aggregate's reference), while the production code's retain()
     * adds a second reference that must be released by our safety net on cancellation.
     */
    @Test(groups = "unit")
    public void cancelledRequestReleasesRetainedByteBuf() throws Exception {
        int leakCount = 0;
        int iterations = 20;

        for (int i = 0; i < iterations; i++) {
            if (runCancelAfterRetainIteration()) {
                leakCount++;
            }
        }

        assertThat(leakCount)
            .as("ByteBuf should not leak on cancellation (leaked in %d of %d iterations)", leakCount, iterations)
            .isEqualTo(0);
    }

    private boolean runCancelAfterRetainIteration() throws Exception {
        DiagnosticsClientContext clientContext = mockDiagnosticsClientContext();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        GlobalEndpointManager globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);

        RegionalRoutingContext regionalRoutingContext = new RegionalRoutingContext(new URI("https://localhost"));
        Mockito.doReturn(regionalRoutingContext)
               .when(globalEndpointManager).resolveServiceEndpoint(any());

        // Use pooled buffer to detect leaks in production-like conditions
        ByteBuf trackedBuf = PooledByteBufAllocator.DEFAULT.buffer(64);
        trackedBuf.writeBytes("{\"id\":\"test\"}".getBytes());

        Sinks.One<ByteBuf> bodySink = Sinks.one();
        AtomicBoolean aggregateReleased = new AtomicBoolean(false);

        // Simulate ByteBufFlux.aggregate() behavior: emit via Sink, auto-release in doFinally
        Mono<ByteBuf> bodyMono = bodySink.asMono()
            .doFinally(signal -> {
                if (!aggregateReleased.getAndSet(true) && trackedBuf.refCnt() > 0) {
                    trackedBuf.release();
                }
            });

        // Use a concrete HttpResponse to avoid Mockito intercepting final withRequest() method
        HttpResponse httpResponse = new HttpResponse() {
            @Override public int statusCode() { return 200; }
            @Override public String headerValue(String name) { return null; }
            @Override public HttpHeaders headers() { return new HttpHeaders(); }
            @Override public Mono<ByteBuf> body() { return bodyMono; }
            @Override public Mono<String> bodyAsString() { return Mono.just(""); }
            @Override public void close() {}
        };

        HttpClient httpClient = Mockito.mock(HttpClient.class);
        Mockito.doAnswer(invocation -> {
            HttpRequest req = invocation.getArgument(0);
            httpResponse.withRequest(req);
            return Mono.just(httpResponse);
        }).when(httpClient).send(any(HttpRequest.class), any(Duration.class));

        GatewayServiceConfigurationReader gatewayServiceConfigurationReader
            = Mockito.mock(GatewayServiceConfigurationReader.class);
        Mockito.doReturn(ConsistencyLevel.SESSION)
               .when(gatewayServiceConfigurationReader).getDefaultConsistencyLevel();

        RxGatewayStoreModel storeModel = new RxGatewayStoreModel(clientContext,
            sessionContainer,
            ConsistencyLevel.SESSION,
            QueryCompatibilityMode.Default,
            new UserAgentContainer(),
            globalEndpointManager,
            httpClient,
            null,
            null);
        storeModel.setGatewayServiceConfigurationReader(gatewayServiceConfigurationReader);

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(clientContext,
            OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.requestContext = new DocumentServiceRequestContext();
        dsr.requestContext.regionalRoutingContextToRoute = regionalRoutingContext;

        // Subscribe to processMessage
        reactor.core.Disposable disposable = storeModel.processMessage(dsr)
            .subscribe(r -> {}, e -> {}, () -> {});

        // Wait for the subscription chain to establish (flatMap subscribes to body())
        Thread.sleep(50);

        // Emit the body buffer. This triggers retain() synchronously in the map operator
        // (refCnt goes from 1 to 2). The retained buffer then enters publishOn's async queue.
        bodySink.tryEmitValue(trackedBuf);

        // Cancel immediately - the element is likely in publishOn's queue, creating the race
        // condition where doOnDiscard may not fire
        disposable.dispose();

        // Allow time for doFinally safety net and aggregate cleanup to execute
        Thread.sleep(500);

        int finalRefCnt = trackedBuf.refCnt();
        if (finalRefCnt > 0) {
            // Clean up to avoid poisoning the allocator
            while (trackedBuf.refCnt() > 0) {
                trackedBuf.release();
            }
            return true; // leaked
        }
        return false;
    }

    /**
     * Verifies that client-level additionalHeaders (e.g., workload-id) are injected into
     * outgoing HTTP requests by performRequest(). This covers metadata requests
     * (collection cache, partition key range) that don't go through getRequestHeaders().
     */
    @Test(groups = "unit")
    public void additionalHeadersInjectedInPerformRequest() throws Exception {
        DiagnosticsClientContext clientContext = mockDiagnosticsClientContext();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        GlobalEndpointManager globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);

        Mockito.doReturn(new RegionalRoutingContext(new URI("https://localhost")))
            .when(globalEndpointManager).resolveServiceEndpoint(any());

        HttpClient httpClient = Mockito.mock(HttpClient.class);
        ArgumentCaptor<HttpRequest> httpClientRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        Mockito.when(httpClient.send(any(), any())).thenReturn(Mono.error(new ConnectTimeoutException()));

        Map<String, String> additionalHeaders = new HashMap<>();
        additionalHeaders.put(HttpConstants.HttpHeaders.WORKLOAD_ID, "25");

        RxGatewayStoreModel storeModel = new RxGatewayStoreModel(
            clientContext,
            sessionContainer,
            ConsistencyLevel.SESSION,
            QueryCompatibilityMode.Default,
            new UserAgentContainer(),
            globalEndpointManager,
            httpClient,
            null,
            additionalHeaders);

        // Simulate a metadata request (e.g., collection cache lookup) — no additionalHeaders on the request itself
        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(
            clientContext,
            OperationType.Read,
            "/dbs/db/colls/col",
            ResourceType.DocumentCollection);
        dsr.requestContext = new DocumentServiceRequestContext();
        dsr.requestContext.regionalRoutingContextToRoute = new RegionalRoutingContext(new URI("https://localhost"));

        try {
            storeModel.performRequest(dsr).block();
            fail("Request should fail");
        } catch (Exception e) {
            // expected
        }

        Mockito.verify(httpClient).send(httpClientRequestCaptor.capture(), any());
        HttpRequest httpRequest = httpClientRequestCaptor.getValue();
        HttpHeaders headers = ReflectionUtils.getHttpHeaders(httpRequest);
        assertThat(headers.toMap().get(HttpConstants.HttpHeaders.WORKLOAD_ID)).isEqualTo("25");
    }

    /**
     * Verifies that request-level headers take precedence over client-level additionalHeaders.
     * If a request already has workload-id set (e.g., via getRequestHeaders()), performRequest()
     * should NOT overwrite it.
     */
    @Test(groups = "unit")
    public void requestLevelHeadersTakePrecedenceOverAdditionalHeaders() throws Exception {
        DiagnosticsClientContext clientContext = mockDiagnosticsClientContext();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        GlobalEndpointManager globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);

        Mockito.doReturn(new RegionalRoutingContext(new URI("https://localhost")))
            .when(globalEndpointManager).resolveServiceEndpoint(any());

        HttpClient httpClient = Mockito.mock(HttpClient.class);
        ArgumentCaptor<HttpRequest> httpClientRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        Mockito.when(httpClient.send(any(), any())).thenReturn(Mono.error(new ConnectTimeoutException()));

        Map<String, String> additionalHeaders = new HashMap<>();
        additionalHeaders.put(HttpConstants.HttpHeaders.WORKLOAD_ID, "10");

        RxGatewayStoreModel storeModel = new RxGatewayStoreModel(
            clientContext,
            sessionContainer,
            ConsistencyLevel.SESSION,
            QueryCompatibilityMode.Default,
            new UserAgentContainer(),
            globalEndpointManager,
            httpClient,
            null,
            additionalHeaders);

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(
            clientContext,
            OperationType.Read,
            "/dbs/db/colls/col/docs/doc1",
            ResourceType.Document);
        dsr.requestContext = new DocumentServiceRequestContext();
        dsr.requestContext.regionalRoutingContextToRoute = new RegionalRoutingContext(new URI("https://localhost"));

        // Simulate request-level header already set (e.g., by getRequestHeaders())
        dsr.getHeaders().put(HttpConstants.HttpHeaders.WORKLOAD_ID, "42");

        try {
            storeModel.performRequest(dsr).block();
            fail("Request should fail");
        } catch (Exception e) {
            // expected
        }

        Mockito.verify(httpClient).send(httpClientRequestCaptor.capture(), any());
        HttpRequest httpRequest = httpClientRequestCaptor.getValue();
        HttpHeaders headers = ReflectionUtils.getHttpHeaders(httpRequest);
        // Request-level header "42" should win over client-level "10"
        assertThat(headers.toMap().get(HttpConstants.HttpHeaders.WORKLOAD_ID)).isEqualTo("42");
    }

    /**
     * Verifies that when additionalHeaders is null, performRequest() still works normally
     * without injecting any extra headers.
     */
    @Test(groups = "unit")
    public void nullAdditionalHeadersDoesNotAffectPerformRequest() throws Exception {
        DiagnosticsClientContext clientContext = mockDiagnosticsClientContext();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        GlobalEndpointManager globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);

        Mockito.doReturn(new RegionalRoutingContext(new URI("https://localhost")))
            .when(globalEndpointManager).resolveServiceEndpoint(any());

        HttpClient httpClient = Mockito.mock(HttpClient.class);
        ArgumentCaptor<HttpRequest> httpClientRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        Mockito.when(httpClient.send(any(), any())).thenReturn(Mono.error(new ConnectTimeoutException()));

        RxGatewayStoreModel storeModel = new RxGatewayStoreModel(
            clientContext,
            sessionContainer,
            ConsistencyLevel.SESSION,
            QueryCompatibilityMode.Default,
            new UserAgentContainer(),
            globalEndpointManager,
            httpClient,
            null,
            null);

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(
            clientContext,
            OperationType.Read,
            "/dbs/db/colls/col",
            ResourceType.DocumentCollection);
        dsr.requestContext = new DocumentServiceRequestContext();
        dsr.requestContext.regionalRoutingContextToRoute = new RegionalRoutingContext(new URI("https://localhost"));

        try {
            storeModel.performRequest(dsr).block();
            fail("Request should fail");
        } catch (Exception e) {
            // expected
        }

        Mockito.verify(httpClient).send(httpClientRequestCaptor.capture(), any());
        HttpRequest httpRequest = httpClientRequestCaptor.getValue();
        HttpHeaders headers = ReflectionUtils.getHttpHeaders(httpRequest);
        // No workload-id header should be present
        assertThat(headers.toMap().get(HttpConstants.HttpHeaders.WORKLOAD_ID)).isNull();
    }

    // ───────────────────────────────────────────────────────────────────────────
    // ReadConsistencyStrategy × isEffectiveSessionConsistency tests
    // ───────────────────────────────────────────────────────────────────────────

    /**
     * Data provider for testing session-token application through the
     * {@code isEffectiveSessionConsistency} priority chain:
     *   request-level RCS > client-level RCS (header) > request CL > account default.
     *
     * Every row uses OperationType.Read + ResourceType.Document (data-plane read)
     * with no user-supplied session token, so the outcome depends solely on whether
     * {@code isEffectiveSessionConsistency} returns true (SDK token applied) or false
     * (no token).
     *
     * Parameters:
     *   defaultConsistencyLevel       – account-level default
     *   requestConsistencyLevel       – request-level ConsistencyLevel header (nullable)
     *   requestContextRCS             – request-level ReadConsistencyStrategy on requestContext (nullable)
     *   clientHeaderRCS               – client-level ReadConsistencyStrategy header string (nullable)
     *   expectSessionTokenApplied     – true ⇒ SDK session token expected; false ⇒ no token
     */
    @DataProvider(name = "readConsistencyStrategySessionTokenProvider")
    public Object[][] readConsistencyStrategySessionTokenProvider() {
        return new Object[][]{
            // ── Request-level RCS (highest priority) ──

            // RCS=SESSION overrides everything → session token applied
            {ConsistencyLevel.EVENTUAL, null, ReadConsistencyStrategy.SESSION, null, true},

            // RCS=EVENTUAL overrides even account-default=SESSION → no token
            {ConsistencyLevel.SESSION, null, ReadConsistencyStrategy.EVENTUAL, null, false},

            // RCS=DEFAULT is transparent → falls through to account default=SESSION → applied
            {ConsistencyLevel.SESSION, null, ReadConsistencyStrategy.DEFAULT, null, true},

            // RCS=DEFAULT is transparent → falls through to account default=EVENTUAL → no token
            {ConsistencyLevel.EVENTUAL, null, ReadConsistencyStrategy.DEFAULT, null, false},

            // ── Client-level RCS header (second priority) ──

            // Client header RCS=Session → session token applied
            {ConsistencyLevel.EVENTUAL, null, null, ReadConsistencyStrategy.SESSION.toString(), true},

            // Client header RCS=Eventual → no token
            {ConsistencyLevel.SESSION, null, null, ReadConsistencyStrategy.EVENTUAL.toString(), false},

            // Client header RCS=Default is transparent → falls through to account default=SESSION
            {ConsistencyLevel.SESSION, null, null, ReadConsistencyStrategy.DEFAULT.toString(), true},

            // ── No RCS, request-level ConsistencyLevel (third priority) ──

            // CL=SESSION → session token applied
            {ConsistencyLevel.EVENTUAL, ConsistencyLevel.SESSION, null, null, true},

            // CL=EVENTUAL → no token
            {ConsistencyLevel.SESSION, ConsistencyLevel.EVENTUAL, null, null, false},

            // ── No RCS, no request CL → account default (lowest priority) ──

            // Account default=SESSION → applied
            {ConsistencyLevel.SESSION, null, null, null, true},

            // Account default=EVENTUAL → no token
            {ConsistencyLevel.EVENTUAL, null, null, null, false},

            // ── Request-level RCS overrides client-level RCS ──

            // Request RCS=SESSION beats client RCS=Eventual → applied
            {ConsistencyLevel.EVENTUAL, null, ReadConsistencyStrategy.SESSION, ReadConsistencyStrategy.EVENTUAL.toString(), true},

            // Request RCS=EVENTUAL beats client RCS=Session → no token
            {ConsistencyLevel.SESSION, null, ReadConsistencyStrategy.EVENTUAL, ReadConsistencyStrategy.SESSION.toString(), false},

            // ── Quorum-read RCS (LATEST_COMMITTED / GLOBAL_STRONG) must NEVER attach a session token ──
            // These strategies execute quorum reads server-side; sending a session token would mask
            // any future regression that weakens them back to session reads.

            // Request RCS=LATEST_COMMITTED on SESSION-default account → no token
            {ConsistencyLevel.SESSION, null, ReadConsistencyStrategy.LATEST_COMMITTED, null, false},

            // Request RCS=LATEST_COMMITTED on EVENTUAL-default account → no token
            {ConsistencyLevel.EVENTUAL, null, ReadConsistencyStrategy.LATEST_COMMITTED, null, false},

            // Request RCS=LATEST_COMMITTED on STRONG-default account → no token
            {ConsistencyLevel.STRONG, null, ReadConsistencyStrategy.LATEST_COMMITTED, null, false},

            // Request RCS=GLOBAL_STRONG on STRONG-default account → no token
            {ConsistencyLevel.STRONG, null, ReadConsistencyStrategy.GLOBAL_STRONG, null, false},

            // Client header RCS=LatestCommitted on SESSION-default account → no token
            {ConsistencyLevel.SESSION, null, null, ReadConsistencyStrategy.LATEST_COMMITTED.toString(), false},

            // Client header RCS=GlobalStrong on STRONG-default account → no token
            {ConsistencyLevel.STRONG, null, null, ReadConsistencyStrategy.GLOBAL_STRONG.toString(), false},

            // Request RCS=LATEST_COMMITTED beats client header RCS=Session → no token (quorum wins)
            {ConsistencyLevel.SESSION, null, ReadConsistencyStrategy.LATEST_COMMITTED, ReadConsistencyStrategy.SESSION.toString(), false},

            // Request RCS=SESSION beats client header RCS=LatestCommitted → token applied (session wins by request-level priority)
            {ConsistencyLevel.EVENTUAL, null, ReadConsistencyStrategy.SESSION, ReadConsistencyStrategy.LATEST_COMMITTED.toString(), true},

            // ── Account default=STRONG without any RCS override → quorum read, no token ──
            {ConsistencyLevel.STRONG, null, null, null, false},

            // Account default=STRONG with request CL=SESSION → request CL wins → token applied
            {ConsistencyLevel.STRONG, ConsistencyLevel.SESSION, null, null, true},
        };
    }

    /**
     * Validates that {@code isEffectiveSessionConsistency} (private, tested indirectly
     * via {@code processMessage → applySessionToken}) correctly walks the 4-branch
     * priority chain when ReadConsistencyStrategy is involved.
     *
     * Uses a data-plane read (Read/Document) with no user session token, so the only
     * variable is the consistency resolution. If session consistency is effective the
     * SDK-maintained global session token must be present; otherwise it must be absent.
     */
    @Test(groups = "unit", dataProvider = "readConsistencyStrategySessionTokenProvider")
    public void applySessionTokenWithReadConsistencyStrategy(
        ConsistencyLevel defaultConsistency,
        ConsistencyLevel requestConsistency,
        ReadConsistencyStrategy requestContextRCS,
        String clientHeaderRCS,
        boolean expectSessionTokenApplied) throws Exception {

        String sdkGlobalSessionToken = "1#100#1=20#2=5#3=30";
        DiagnosticsClientContext clientContext = mockDiagnosticsClientContext();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        Mockito.doReturn(sdkGlobalSessionToken).when(sessionContainer).resolveGlobalSessionToken(any());

        GlobalEndpointManager globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);
        URI locationEndpointToRoute = new URI("https://localhost");
        RegionalRoutingContext regionalRoutingContext = new RegionalRoutingContext(locationEndpointToRoute);
        Mockito.doReturn(regionalRoutingContext)
            .when(globalEndpointManager).resolveServiceEndpoint(any());

        HttpClient httpClient = Mockito.mock(HttpClient.class);
        Mockito.doReturn(Mono.error(ReadTimeoutException.INSTANCE))
            .when(httpClient).send(any(HttpRequest.class), any(Duration.class));

        GatewayServiceConfigurationReader gatewayServiceConfigurationReader =
            Mockito.mock(GatewayServiceConfigurationReader.class);
        Mockito.doReturn(defaultConsistency)
            .when(gatewayServiceConfigurationReader).getDefaultConsistencyLevel();

        RxGatewayStoreModel storeModel = new RxGatewayStoreModel(
            clientContext,
            sessionContainer,
            defaultConsistency,
            QueryCompatibilityMode.Default,
            new UserAgentContainer(),
            globalEndpointManager,
            httpClient,
            ApiType.SQL,
            null);
        storeModel.setGatewayServiceConfigurationReader(gatewayServiceConfigurationReader);

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(
            clientContext,
            OperationType.Read,
            "/fakeResourceFullName",
            ResourceType.Document);
        dsr.requestContext.regionalRoutingContextToRoute = regionalRoutingContext;

        // Set request-level ReadConsistencyStrategy on requestContext (highest priority)
        if (requestContextRCS != null) {
            dsr.requestContext.readConsistencyStrategy = requestContextRCS;
        }

        // Set client-level ReadConsistencyStrategy header (second priority)
        if (clientHeaderRCS != null) {
            dsr.getHeaders().put(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY, clientHeaderRCS);
        }

        // Set request-level ConsistencyLevel header (third priority)
        if (requestConsistency != null) {
            dsr.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, requestConsistency.toString());
        }

        // Drive the request through processMessage → applySessionToken → isEffectiveSessionConsistency
        Mono<RxDocumentServiceResponse> resp = storeModel.processMessage(dsr);
        validateFailure(resp, FailureValidator.builder()
            .instanceOf(CosmosException.class)
            .causeInstanceOf(ReadTimeoutException.class)
            .statusCode(HttpConstants.StatusCodes.REQUEST_TIMEOUT).build());

        if (expectSessionTokenApplied) {
            assertThat(dsr.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN))
                .as("Session token should be applied (isEffectiveSessionConsistency=true)")
                .isEqualTo(sdkGlobalSessionToken);
        } else {
            assertThat(dsr.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN))
                .as("Session token should NOT be applied (isEffectiveSessionConsistency=false)")
                .isNull();
        }
    }

    /**
     * Matrix for {@link RxGatewayStoreModel#resolveEffectiveConsistencyHeaders(Map, ReadConsistencyStrategy)}.
     *
     * Columns:
     *   requestContextRCS    – request-level ReadConsistencyStrategy (nullable)
     *   initialHeaderRCS     – existing x-ms-cosmos-read-consistency-strategy header value (nullable = absent)
     *   initialHeaderCL      – existing x-ms-consistency-level header value (nullable = absent)
     *   expectedHeaderRCS    – expected RCS header value after the call (null = must be absent)
     *   expectedHeaderCL     – expected CL header value after the call (null = must be absent)
     */
    @DataProvider(name = "resolveEffectiveConsistencyHeadersProvider")
    public Object[][] resolveEffectiveConsistencyHeadersProvider() {
        return new Object[][]{
            // 1. Request-level non-DEFAULT wins → CL stripped, RCS set from requestContext
            {ReadConsistencyStrategy.LATEST_COMMITTED, null, "Session",
                ReadConsistencyStrategy.LATEST_COMMITTED.toString(), null},

            // 2. Request-level DEFAULT is transparent → header RCS (Eventual) wins → CL stripped
            {ReadConsistencyStrategy.DEFAULT, ReadConsistencyStrategy.EVENTUAL.toString(), "Session",
                ReadConsistencyStrategy.EVENTUAL.toString(), null},

            // 3. Both null → CL governs, RCS stays absent
            {null, null, "Session",
                null, "Session"},

            // 4. Both null/DEFAULT → strip stale DEFAULT sentinel from header, CL untouched
            {ReadConsistencyStrategy.DEFAULT, ReadConsistencyStrategy.DEFAULT.toString(), "Session",
                null, "Session"},

            // 5. Header-only non-DEFAULT (GlobalStrong) → CL stripped, RCS preserved
            {null, ReadConsistencyStrategy.GLOBAL_STRONG.toString(), "Session",
                ReadConsistencyStrategy.GLOBAL_STRONG.toString(), null},

            // 6. Request-level beats header on conflict → request RCS overwrites header, CL stripped
            {ReadConsistencyStrategy.EVENTUAL, ReadConsistencyStrategy.LATEST_COMMITTED.toString(), "Strong",
                ReadConsistencyStrategy.EVENTUAL.toString(), null},

            // 7. Empty header value treated as absent (Strings.isNullOrEmpty) → no-op on headers
            {null, "", "Session",
                "", "Session"},

            // 8. Unknown header value (not a known RCS) → helper returns null → stale header stripped
            {null, "Bogus", "Session",
                null, "Session"},

            // 9. Request-level SESSION wins → CL stripped, RCS set to Session
            {ReadConsistencyStrategy.SESSION, null, "Eventual",
                ReadConsistencyStrategy.SESSION.toString(), null},

            // 10. No CL present, request-level non-DEFAULT → RCS set, CL stays absent
            {ReadConsistencyStrategy.GLOBAL_STRONG, null, null,
                ReadConsistencyStrategy.GLOBAL_STRONG.toString(), null},
        };
    }

    /**
     * Validates that {@link RxGatewayStoreModel#resolveEffectiveConsistencyHeaders(Map, ReadConsistencyStrategy)}:
     * <ul>
     *   <li>strips {@code x-ms-consistency-level} when an effective non-DEFAULT RCS is resolved,</li>
     *   <li>strips a stale {@code x-ms-cosmos-read-consistency-strategy} header (including the
     *       {@code DEFAULT} sentinel) when no effective non-DEFAULT RCS is resolved,</li>
     *   <li>preserves {@code x-ms-consistency-level} when no RCS wins.</li>
     * </ul>
     *
     * This is the cross-cutting safety net that protects both GW V1 (HTTP) and GW V2 (RNTBD via
     * ThinClientStoreModel) from emitting a stale {@code DEFAULT} header on the wire.
     */
    @Test(groups = "unit", dataProvider = "resolveEffectiveConsistencyHeadersProvider")
    public void resolveEffectiveConsistencyHeaders_stripsDefaultAndCanonicalizes(
        ReadConsistencyStrategy requestContextRCS,
        String initialHeaderRCS,
        String initialHeaderCL,
        String expectedHeaderRCS,
        String expectedHeaderCL) {

        Map<String, String> headers = new HashMap<>();
        if (initialHeaderRCS != null) {
            headers.put(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY, initialHeaderRCS);
        }
        if (initialHeaderCL != null) {
            headers.put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, initialHeaderCL);
        }

        RxGatewayStoreModel.resolveEffectiveConsistencyHeaders(headers, requestContextRCS);

        if (expectedHeaderRCS == null) {
            assertThat(headers.get(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY))
                .as("x-ms-cosmos-read-consistency-strategy should be absent")
                .isNull();
        } else {
            assertThat(headers.get(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY))
                .as("x-ms-cosmos-read-consistency-strategy should equal expected value")
                .isEqualTo(expectedHeaderRCS);
        }

        if (expectedHeaderCL == null) {
            assertThat(headers.get(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL))
                .as("x-ms-consistency-level should be absent")
                .isNull();
        } else {
            assertThat(headers.get(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL))
                .as("x-ms-consistency-level should equal expected value")
                .isEqualTo(expectedHeaderCL);
        }
    }

    @Test(groups = "unit")
    public void gatewayAddsNoRetry449Header() throws Exception {
        DiagnosticsClientContext clientContext = mockDiagnosticsClientContext();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        GlobalEndpointManager globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);

        Mockito.doReturn(new RegionalRoutingContext(new URI("https://localhost")))
            .when(globalEndpointManager).resolveServiceEndpoint(any());

        HttpClient httpClient = Mockito.mock(HttpClient.class);
        ArgumentCaptor<HttpRequest> httpClientRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        Mockito.when(httpClient.send(any(), any())).thenReturn(Mono.error(new ConnectTimeoutException()));

        RxGatewayStoreModel storeModel = new RxGatewayStoreModel(
            clientContext,
            sessionContainer,
            ConsistencyLevel.SESSION,
            QueryCompatibilityMode.Default,
            new UserAgentContainer(),
            globalEndpointManager,
            httpClient,
            null,
            null);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(
            clientContext,
            OperationType.Read,
            "/dbs/db/colls/col/docs/doc1",
            ResourceType.Document);
        request.requestContext = new DocumentServiceRequestContext();
        request.requestContext.regionalRoutingContextToRoute = new RegionalRoutingContext(new URI("https://localhost"));

        try {
            storeModel.performRequest(request).block();
            fail("Request should fail");
        } catch (Exception expectedException) {
            // expected
        }

        Mockito.verify(httpClient).send(httpClientRequestCaptor.capture(), any());
        HttpHeaders headers = ReflectionUtils.getHttpHeaders(httpClientRequestCaptor.getValue());
        assertThat(headers.toMap().get(HttpConstants.HttpHeaders.NO_RETRY_449)).isEqualTo("true");
    }

    @Test(groups = "unit")
    public void gatewayOverridesNoRetry449Header() throws Exception {
        DiagnosticsClientContext clientContext = mockDiagnosticsClientContext();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        GlobalEndpointManager globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);

        Mockito.doReturn(new RegionalRoutingContext(new URI("https://localhost")))
            .when(globalEndpointManager).resolveServiceEndpoint(any());

        HttpClient httpClient = Mockito.mock(HttpClient.class);
        ArgumentCaptor<HttpRequest> httpClientRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        Mockito.when(httpClient.send(any(), any())).thenReturn(Mono.error(new ConnectTimeoutException()));

        Map<String, String> additionalHeaders = new HashMap<>();
        additionalHeaders.put(HttpConstants.HttpHeaders.NO_RETRY_449, "false");
        RxGatewayStoreModel storeModel = new RxGatewayStoreModel(
            clientContext,
            sessionContainer,
            ConsistencyLevel.SESSION,
            QueryCompatibilityMode.Default,
            new UserAgentContainer(),
            globalEndpointManager,
            httpClient,
            null,
            additionalHeaders);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(
            clientContext,
            OperationType.Read,
            "/dbs/db/colls/col/docs/doc1",
            ResourceType.Document);
        request.requestContext = new DocumentServiceRequestContext();
        request.requestContext.regionalRoutingContextToRoute = new RegionalRoutingContext(new URI("https://localhost"));
        request.getHeaders().put(HttpConstants.HttpHeaders.NO_RETRY_449, "false");

        try {
            storeModel.performRequest(request).block();
            fail("Request should fail");
        } catch (Exception expectedException) {
            // expected
        }

        Mockito.verify(httpClient).send(httpClientRequestCaptor.capture(), any());
        HttpHeaders headers = ReflectionUtils.getHttpHeaders(httpClientRequestCaptor.getValue());
        assertThat(headers.toMap().get(HttpConstants.HttpHeaders.NO_RETRY_449)).isEqualTo("true");
    }

    @Test(groups = "unit")
    public void gatewayRetryWithTimeoutUsesStrongConsistencyFromGatewayServiceConfigurationReader() throws Exception {
        DiagnosticsClientContext clientContext = mockDiagnosticsClientContext();
        GatewayServiceConfigurationReader gatewayServiceConfigurationReader = Mockito.mock(GatewayServiceConfigurationReader.class);
        Mockito.doReturn(ConsistencyLevel.STRONG)
            .when(gatewayServiceConfigurationReader).getDefaultConsistencyLevel();

        RxGatewayStoreModel storeModel = new RxGatewayStoreModel(
            clientContext,
            Mockito.mock(ISessionContainer.class),
            ConsistencyLevel.SESSION,
            QueryCompatibilityMode.Default,
            new UserAgentContainer(),
            Mockito.mock(GlobalEndpointManager.class),
            Mockito.mock(HttpClient.class),
            null,
            null);
        storeModel.setGatewayServiceConfigurationReader(gatewayServiceConfigurationReader);

        assertThat(getGatewayRetryWithTimeoutInSeconds(storeModel)).isEqualTo(60);
    }

    @Test(groups = "unit")
    public void gatewayRetryWithTimeoutFallsBackToDefaultConsistencyWhenGatewayServiceConfigurationReaderIsNull()
        throws Exception {

        RxGatewayStoreModel storeModel = new RxGatewayStoreModel(
            mockDiagnosticsClientContext(),
            Mockito.mock(ISessionContainer.class),
            ConsistencyLevel.STRONG,
            QueryCompatibilityMode.Default,
            new UserAgentContainer(),
            Mockito.mock(GlobalEndpointManager.class),
            Mockito.mock(HttpClient.class),
            null,
            null);

        assertThat(getGatewayRetryWithTimeoutInSeconds(storeModel)).isEqualTo(60);
    }

    private static int getGatewayRetryWithTimeoutInSeconds(RxGatewayStoreModel storeModel) throws Exception {
        Method getGatewayRetryWithTimeoutInSeconds = RxGatewayStoreModel.class
            .getDeclaredMethod("getGatewayRetryWithTimeoutInSeconds");
        getGatewayRetryWithTimeoutInSeconds.setAccessible(true);

        return (int) getGatewayRetryWithTimeoutInSeconds.invoke(storeModel);
    }

    enum SessionTokenType {
        NONE, // no session token applied
        USER, // userControlled session token
        SDK  // SDK maintained session token
    }
}
