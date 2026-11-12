// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ISessionContainer;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.PartitionKeyRangeGoneException;
import com.azure.cosmos.implementation.ReadFeedKeyType;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.RxGatewayStoreModel;
import com.azure.cosmos.implementation.RxStoreModel;
import com.azure.cosmos.implementation.SessionTokenHelper;
import com.azure.cosmos.implementation.ThinClientStoreModel;
import com.azure.cosmos.implementation.ThinClientConnectivityConfig;
import com.azure.cosmos.implementation.UserAgentContainer;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.caches.RxClientCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.directconnectivity.GatewayServiceConfigurationReader;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.HexConvert;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import com.azure.cosmos.implementation.perPartitionAutomaticFailover.GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover;
import com.azure.cosmos.implementation.perPartitionCircuitBreaker.GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker;
import com.azure.cosmos.models.PartitionKeyDefinition;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.mockito.Mockito;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants.RntbdRequestHeader;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.ArgumentMatchers.same;

public class ThinClientCompoundSessionTokenTest {
    private static final String COMPOUND = "0:1#10#1=10,1:1#20#1=20";
    private static final String COLLECTION_RID = "ccZ1ANCszwk=";
    private DiagnosticsClientContext context;
    private ThinClientStoreModel model;
    private ISessionContainer ambient;
    private HttpClient httpClient;
    private HttpResponse response;
    private List<HttpRequest> sent;
    private RxDocumentClientImpl client;
    private RxGatewayStoreModel classic;
    private GlobalEndpointManager endpoints;
    private Method storeSelector;
    private MockedStatic<Configs> configs;
    private RxPartitionKeyRangeCache rangeCache;
    private CollectionRoutingMap routingMap;
    private PartitionKeyRange targetRange;
    private Method tokenPreparer;
    private GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker circuitBreaker;
    private GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover automaticFailover;

    @BeforeMethod(groups = "unit")
    public void setUp() throws Exception {
        configs = Mockito.mockStatic(Configs.class, Mockito.CALLS_REAL_METHODS);
        configs.when(Configs::isThinClientEnabled).thenReturn(null);
        context = mockDiagnosticsClientContext();
        ambient = Mockito.mock(ISessionContainer.class);
        httpClient = Mockito.mock(HttpClient.class);
        sent = new ArrayList<>();
        response = new HttpResponse() {
            @Override public int statusCode() { return 200; }
            @Override public String headerValue(String name) { return null; }
            @Override public HttpHeaders headers() { return new HttpHeaders(); }
            @Override public Mono<ByteBuf> body() { return Mono.just(Unpooled.EMPTY_BUFFER); }
            @Override public Mono<String> bodyAsString() { return Mono.just(""); }
        };
        Mockito.when(httpClient.send(any(), any())).thenAnswer(invocation -> {
            HttpRequest request = invocation.getArgument(0);
            sent.add(request);
            return Mono.just(response.withRequest(request));
        });
        endpoints = Mockito.mock(GlobalEndpointManager.class);
        DatabaseAccount account = Mockito.mock(DatabaseAccount.class);
        Mockito.when(account.getId()).thenReturn("test-account");
        Mockito.when(endpoints.getLatestDatabaseAccount()).thenReturn(account);
        RegionalRoutingContext location = new RegionalRoutingContext(URI.create("https://localhost"));
        location.setThinclientRegionalEndpoint(URI.create("https://localhost:10250"));
        Mockito.when(endpoints.resolveServiceEndpoint(any())).thenReturn(location);
        model = new ThinClientStoreModel(context, ambient, ConsistencyLevel.SESSION,
            new UserAgentContainer(), endpoints, httpClient, null);
        GatewayServiceConfigurationReader reader = Mockito.mock(GatewayServiceConfigurationReader.class);
        Mockito.when(reader.getDefaultConsistencyLevel()).thenReturn(ConsistencyLevel.SESSION);
        Field configuration = RxGatewayStoreModel.class.getDeclaredField("gatewayServiceConfigurationReader");
        configuration.setAccessible(true);
        configuration.set(model, reader);
        client = Mockito.mock(RxDocumentClientImpl.class, Mockito.CALLS_REAL_METHODS);
        classic = Mockito.mock(RxGatewayStoreModel.class);
        ThinClientConnectivityConfig connectivity = Mockito.mock(ThinClientConnectivityConfig.class);
        Mockito.when(connectivity.canThinClientBeUsed()).thenReturn(true);
        Mockito.when(endpoints.hasThinClientReadLocations()).thenReturn(true);
        Mockito.when(endpoints.getProxyProbeDecision()).thenReturn(true);
        setClientField("thinClientConnectivityConfig", connectivity);
        setClientField("globalEndpointManager", endpoints);
        setClientField("thinProxy", model);
        setClientField("gatewayProxy", classic);
        setClientField("storeModel", classic);
        circuitBreaker = Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.class);
        automaticFailover = Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover.class);
        setClientField("globalPartitionEndpointManagerForPerPartitionCircuitBreaker", circuitBreaker);
        setClientField("globalPartitionEndpointManagerForPerPartitionAutomaticFailover", automaticFailover);
        RxClientCollectionCache collections = Mockito.mock(RxClientCollectionCache.class);
        DocumentCollection collection = new DocumentCollection();
        collection.setResourceId(COLLECTION_RID);
        collection.setPartitionKey(new PartitionKeyDefinition().setPaths(Collections.singletonList("/pk")));
        Mockito.when(collections.resolveCollectionAsync(any(), any()))
            .thenReturn(Mono.just(new Utils.ValueHolder<>(collection)));
        rangeCache = Mockito.mock(RxPartitionKeyRangeCache.class);
        routingMap = Mockito.mock(CollectionRoutingMap.class);
        targetRange = new PartitionKeyRange("1", "", "FF");
        Mockito.when(routingMap.getRangeByEffectivePartitionKey(any())).thenReturn(targetRange);
        Mockito.when(routingMap.getRangeByPartitionKeyRangeId("1")).thenReturn(targetRange);
        Mockito.when(routingMap.tryGetRangeByPartitionKeyRangeId("1")).thenReturn(targetRange);
        Mockito.when(routingMap.tryGetRangeByPartitionKeyRangeId("0")).thenReturn(new PartitionKeyRange("0", "", "FF"));
        Mockito.when(rangeCache.tryLookupAsync(any(), eq(COLLECTION_RID), any(), any()))
            .thenReturn(Mono.just(new Utils.ValueHolder<>(routingMap)));
        setClientField("collectionCache", collections);
        setClientField("partitionKeyRangeCache", rangeCache);
        storeSelector = RxDocumentClientImpl.class.getDeclaredMethod("getStoreProxy", RxDocumentServiceRequest.class);
        storeSelector.setAccessible(true);
        tokenPreparer = RxDocumentClientImpl.class.getDeclaredMethod("prepareCompoundSessionToken",
            RxDocumentServiceRequest.class, String.class, PartitionKeyDefinition.class, CollectionRoutingMap.class);
        tokenPreparer.setAccessible(true);
    }

    @AfterMethod(groups = "unit", alwaysRun = true)
    public void tearDown() {
        if (configs != null) {
            configs.close();
            configs = null;
        }
    }

    @DataProvider
    public Object[][] operations() {
        return new Object[][] {
            {OperationType.Read}, {OperationType.Query}, {OperationType.Batch}, {OperationType.Create},
            {OperationType.Upsert}, {OperationType.Replace}, {OperationType.Delete}, {OperationType.Patch},
            {OperationType.ExecuteJavaScript}
        };
    }

    @Test(groups = "unit", dataProvider = "operations")
    public void sendsOnlyTargetProgressOverV2(OperationType operation) {
        model.setUseMultipleWriteLocations(true);
        RxDocumentServiceRequest request = request(operation, COMPOUND, true);

        send(request);

        assertSession(0, "1:1#20#1=20");
        assertThat(request.requestContext.sessionToken).isEqualTo(SessionTokenHelper.parse("1#20#1=20"));
        assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(COMPOUND);
        assertThat(request.getOriginalSessionToken()).isEqualTo(COMPOUND);
        assertThat(sent.get(0).uri().getPort()).isEqualTo(10250);
        Mockito.verify(rangeCache).tryLookupAsync(any(), eq(COLLECTION_RID), isNull(), isNull());
        Mockito.verify(rangeCache, Mockito.never()).tryLookupAsync(any(), any(), notNull(), any());
        verifyNoAmbientLookup();
    }

    @Test(groups = "unit")
    public void mergesParentAndTargetProgress() {
        RxDocumentServiceRequest request = request(OperationType.Read,
            "0:1#100#1=10#2=20,1:1#120#1=15#2=10", true);
        request.requestContext.resolvedPartitionKeyRange.setParents(Collections.singletonList("0"));

        send(request);

        assertSession(0, "1:1#120#1=15#2=20");
        verifyNoAmbientLookup();
    }

    @Test(groups = "unit")
    public void inheritsParentProgressForAChildWithoutItsOwnSegment() {
        RxDocumentServiceRequest request = request(OperationType.Read, COMPOUND, true);
        request.requestContext.resolvedPartitionKeyRange = new PartitionKeyRange(
            "2", "", "FF", Collections.singletonList("1"));
        Mockito.when(routingMap.getRangeByEffectivePartitionKey(any()))
            .thenReturn(request.requestContext.resolvedPartitionKeyRange);
        Mockito.when(routingMap.tryGetRangeByPartitionKeyRangeId("2"))
            .thenReturn(request.requestContext.resolvedPartitionKeyRange);

        send(request);

        assertSession(0, "2:1#20#1=20");
        verifyNoAmbientLookup();
    }

    @Test(groups = "unit")
    public void usesPhysicalRangeResolvedDuringDocumentClientPreparation() {
        RxDocumentServiceRequest request = request(OperationType.Batch, COMPOUND, false);
        request.setPartitionKeyInternal(null);
        request.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity(COLLECTION_RID, "1"));
        CollectionRoutingMap routingMap = Mockito.mock(CollectionRoutingMap.class);
        Mockito.when(routingMap.getRangeByPartitionKeyRangeId("1"))
            .thenReturn(new PartitionKeyRange("1", "", "FF"));
        RequestOptions options = new RequestOptions();
        options.setPartitionKeyDefinition(request.getPartitionKeyDefinition());
        client.addPartitionLevelUnavailableRegionsForPointOperationRequestForPerPartitionCircuitBreaker(
            request, options, routingMap, null, null);

        send(request);

        assertSession(0, "1:1#20#1=20");
        assertThat(request.requestContext.resolvedPartitionKeyRange.getId()).isEqualTo("1");
        Mockito.verify(routingMap).getRangeByPartitionKeyRangeId("1");
        verifyNoAmbientLookup();
    }

    @DataProvider
    public Object[][] unscopable() {
        return new Object[][] {{true}, {false}};
    }

    @Test(groups = "unit", dataProvider = "unscopable")
    public void omitsUnscopableProgressWithoutAmbientSubstitution(boolean resolved) {
        RxDocumentServiceRequest request = request(OperationType.Read, COMPOUND, resolved);
        if (resolved) {
            request.requestContext.resolvedPartitionKeyRange = new PartitionKeyRange("9", "", "FF");
            Mockito.when(routingMap.getRangeByEffectivePartitionKey(any()))
                .thenReturn(request.requestContext.resolvedPartitionKeyRange);
            Mockito.when(routingMap.tryGetRangeByPartitionKeyRangeId("9"))
                .thenReturn(request.requestContext.resolvedPartitionKeyRange);
        }
        Mockito.when(ambient.resolveGlobalSessionToken(any())).thenReturn("9:1#999#1=999");

        send(request);
        send(request);

        assertSession(0, null);
        assertSession(1, null);
        assertThat(request.requestContext.sessionToken).isNull();
        assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(COMPOUND);
        assertThat(request.requestContext.getSessionTokenEvaluationResults()).hasSize(1)
            .allSatisfy(warning -> assertThat(warning)
                .contains("could not be evaluated", "pkRangeId : " + (resolved ? "9" : ""),
                    "collectionRid : " + COLLECTION_RID)
                .doesNotContain("secret-key"));
        verifyNoAmbientLookup();
    }

    @Test(groups = "unit")
    public void retryRescopesOriginalProgressAfterAnOmission() {
        RxDocumentServiceRequest request = request(OperationType.Read, COMPOUND, false);
        Mockito.doAnswer(invocation -> {
            HttpRequest outgoing = invocation.getArgument(0);
            sent.add(outgoing);
            if (sent.size() == 1) {
                request.requestContext.resolvedPartitionKeyRange = targetRange;
                Mockito.when(routingMap.getRangeByEffectivePartitionKey(any())).thenReturn(targetRange);
                return Mono.error(new IllegalStateException("simulated transport failure"));
            }
            return Mono.just(response.withRequest(outgoing));
        }).when(httpClient).send(any(), any());

        StepVerifier.create(dispatch(request).retry(1)).expectNextCount(1).verifyComplete();

        assertSession(0, null);
        assertSession(1, "1:1#20#1=20");
        assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(COMPOUND);
        verifyNoAmbientLookup();
    }

    @Test(groups = "unit")
    public void clonesRescopeOriginalProgressForTheirOwnRange() {
        RxDocumentServiceRequest original = request(OperationType.Read, COMPOUND, true);
        send(original);
        RxDocumentServiceRequest clone = original.clone();
        clone.requestContext.resolvedPartitionKeyRange = routingMap.tryGetRangeByPartitionKeyRangeId("0");

        send(clone);

        assertSession(0, "1:1#20#1=20");
        assertSession(1, "0:1#10#1=10");
        assertThat(original.requestContext.sessionToken).isEqualTo(SessionTokenHelper.parse("1#20#1=20"));
        assertThat(clone.requestContext.sessionToken).isEqualTo(SessionTokenHelper.parse("1#10#1=10"));
        verifyNoAmbientLookup();
    }

    @Test(groups = "unit")
    public void cancellationRestoresCallerProgress() {
        RxDocumentServiceRequest request = request(OperationType.Read, COMPOUND, false);
        Mockito.doAnswer(invocation -> {
            sent.add(invocation.getArgument(0));
            return Mono.never();
        }).when(httpClient).send(any(), any());

        StepVerifier.create(dispatch(request))
            .then(() -> assertThat(sent).hasSize(1))
            .thenCancel().verify();

        assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(COMPOUND);
        assertThat(request.getOriginalSessionToken()).isEqualTo(COMPOUND);
        assertThat(request.requestContext.getSessionTokenEvaluationResults()).hasSize(1);
        verifyNoAmbientLookup();
    }

    @Test(groups = "unit")
    public void unresolvedMultiRangeFeedPreservesRoutingBounds() {
        RxDocumentServiceRequest request = request(OperationType.Query, COMPOUND, false);
        request.setPartitionKeyInternal(null);
        request.getHeaders().put(HttpConstants.HttpHeaders.READ_FEED_KEY_TYPE,
            ReadFeedKeyType.EffectivePartitionKeyRange.name());
        request.getHeaders().put(HttpConstants.HttpHeaders.START_EPK, "AA");
        request.getHeaders().put(HttpConstants.HttpHeaders.END_EPK, "BB");

        send(request);

        Map<Short, byte[]> headers = ThinClientRangeHeadersTest.decodeHeaders(sent.get(0));
        assertSession(0, null);
        assertThat(headers.get(RntbdRequestHeader.StartEpkHash.id())).containsExactly(HexConvert.hexToBytes("AA"));
        assertThat(headers.get(RntbdRequestHeader.EndEpkHash.id())).containsExactly(HexConvert.hexToBytes("BB"));
        assertThat(headers).doesNotContainKey(RntbdRequestHeader.EffectivePartitionKey.id());
        verifyNoAmbientLookup();
    }

    @DataProvider
    public Object[][] malformed() {
        return new Object[][] {
            {"1:1#20#1=20,invalid", true}, {"1:1#20#1=20,invalid", false},
            {"1:1#20#1=20,9:not-a-vector", true}, {"1:1#20#1=20,9:not-a-vector", false}
        };
    }

    @Test(groups = "unit", dataProvider = "malformed")
    public void malformedTokenFailsBeforeSending(String token, boolean resolved) {
        RxDocumentServiceRequest request = request(OperationType.Read, token, resolved);

        StepVerifier.create(dispatch(request)).expectError().verify();

        assertThat(sent).isEmpty();
        assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(token);
        assertThat(request.requestContext.getSessionTokenEvaluationResults()).isEmpty();
        Mockito.verify(rangeCache, Mockito.never()).tryLookupAsync(any(), any(), notNull(), any());
        verifyNoAmbientLookup();
    }

    @Test(groups = "unit")
    public void consistencyAndWriteEligibilityStillRemoveTokens() {
        RxDocumentServiceRequest eventual = request(OperationType.Read, COMPOUND, true);
        eventual.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.EVENTUAL.toString());
        send(eventual);
        RxDocumentServiceRequest singleWriter = request(OperationType.Create, COMPOUND, true);
        send(singleWriter);
        RxDocumentServiceRequest queryPlan = request(OperationType.QueryPlan, COMPOUND, false);
        send(queryPlan);

        assertSession(0, null);
        assertSession(1, null);
        assertSession(2, null);
        assertThat(eventual.requestContext.getSessionTokenEvaluationResults()).isEmpty();
        assertThat(singleWriter.requestContext.getSessionTokenEvaluationResults()).isEmpty();
        assertThat(queryPlan.requestContext.getSessionTokenEvaluationResults()).isEmpty();
        verifyNoAmbientLookup();
    }

    @Test(groups = "unit")
    public void singleAndAmbientTokensRemainUnchanged() {
        String single = "1:1#20#1=20";
        send(request(OperationType.Read, single, true));
        RxDocumentServiceRequest request = request(OperationType.Read, null, true);
        Mockito.when(ambient.resolveGlobalSessionToken(request)).thenReturn(single);
        send(request);
        send(request);

        assertSession(0, single);
        assertSession(1, single);
        assertSession(2, single);
        Mockito.verify(ambient, Mockito.times(2)).resolveGlobalSessionToken(request);
    }

    @Test(groups = "unit")
    public void documentClientScopesBeforeEnteringThinTransport() throws Exception {
        RxDocumentServiceRequest request = request(OperationType.Read, COMPOUND, true);

        assertThat(selectStore(request)).isSameAs(model);

        assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo("1:1#20#1=20");
        assertThat(request.requestContext.sessionToken).isEqualTo(SessionTokenHelper.parse("1#20#1=20"));
        assertThat(request.useThinClientMode).isTrue();
        assertThat(sent).isEmpty();
        verifyNoAmbientLookup();
    }

    @DataProvider
    public Object[][] classicRouting() {
        return new Object[][] {{true}, {false}};
    }

    @Test(groups = "unit", dataProvider = "classicRouting")
    public void documentClientRestoresOriginalTokenWhenRoutingAwayFromV2(boolean forcedGateway) throws Exception {
        RxDocumentServiceRequest request = request(OperationType.Read, COMPOUND, true);
        assertThat(selectStore(request)).isSameAs(model);
        request.useGatewayMode = forcedGateway;
        Mockito.when(endpoints.getProxyProbeDecision()).thenReturn(false);

        assertThat(RxStoreModel.class.cast(storeSelector.invoke(client, request))).isSameAs(classic);

        assertThat(request.useThinClientMode).isFalse();
        assertThat(request.requestContext.sessionToken).isNull();
        assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(COMPOUND);
        verifyNoAmbientLookup();
    }

    @Test(groups = "unit")
    public void ineligibleSessionPolicyDoesNotParseIgnoredCompoundToken() {
        RxDocumentServiceRequest request = request(OperationType.Read, "invalid,ignored", true);
        request.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.EVENTUAL.toString());

        send(request);

        assertSession(0, null);
        assertThat(request.requestContext.getSessionTokenEvaluationResults()).isEmpty();
        verifyNoAmbientLookup();
    }

    @DataProvider
    public Object[][] parentProgress() {
        return new Object[][] {{true}, {false}};
    }

    @Test(groups = "unit", dataProvider = "parentProgress")
    public void refreshesStaleTopologyBeforeScopingChildProgress(boolean includeParent) {
        String original = "1:1#1000020#1=1000020,2:1#30#1=30" + (includeParent ? ",0:1#10#1=10" : "");
        RxDocumentServiceRequest request = request(OperationType.Read, original, true);
        PartitionKeyRange parent = routingMap.tryGetRangeByPartitionKeyRangeId("0");
        request.requestContext.resolvedPartitionKeyRange = parent;
        request.requestContext.resolvedPartitionKeyRangeForCircuitBreaker = parent;
        request.requestContext.resolvedPartitionKeyRangeForPerPartitionAutomaticFailover = parent;
        Mockito.when(circuitBreaker.isPerPartitionLevelCircuitBreakingApplicable(any())).thenReturn(true);
        Mockito.when(automaticFailover.isPerPartitionAutomaticFailoverApplicable(any())).thenReturn(true);
        request.properties = Collections.singletonMap("test-property", "retained");
        Mockito.when(routingMap.getRangeByEffectivePartitionKey(any())).thenReturn(parent);
        Mockito.when(routingMap.tryGetRangeByPartitionKeyRangeId("1")).thenReturn(null);
        CollectionRoutingMap refreshed = Mockito.mock(CollectionRoutingMap.class);
        PartitionKeyRange child = new PartitionKeyRange("1", "", "80", Collections.singletonList("0"));
        Mockito.when(refreshed.getRangeByEffectivePartitionKey(any())).thenReturn(child);
        Mockito.when(refreshed.tryGetRangeByPartitionKeyRangeId("1")).thenReturn(child);
        Mockito.when(refreshed.tryGetRangeByPartitionKeyRangeId("2"))
            .thenReturn(new PartitionKeyRange("2", "80", "FF", Collections.singletonList("0")));
        Mockito.when(rangeCache.tryLookupAsync(any(), eq(COLLECTION_RID), same(routingMap), same(request.properties)))
            .thenReturn(Mono.just(new Utils.ValueHolder<>(refreshed)));

        send(request);

        assertSession(0, "1:1#1000020#1=1000020");
        assertThat(request.requestContext.resolvedPartitionKeyRange).isSameAs(child);
        assertThat(request.requestContext.resolvedPartitionKeyRangeForCircuitBreaker).isSameAs(child);
        assertThat(request.requestContext.resolvedPartitionKeyRangeForPerPartitionAutomaticFailover).isSameAs(child);
        assertThat(request.requestContext.getSessionTokenEvaluationResults()).isEmpty();
        Mockito.verify(rangeCache).tryLookupAsync(any(), eq(COLLECTION_RID), same(routingMap), same(request.properties));
        Mockito.verify(rangeCache, Mockito.times(2)).tryLookupAsync(any(), any(), any(), any());
        verifyNoAmbientLookup();
    }

    @Test(groups = "unit")
    public void omitsAllProgressWhenUnknownRangesRemainAfterOneRefresh() {
        RxDocumentServiceRequest request = request(OperationType.Read, COMPOUND + ",99:1#999#1=999", true);

        send(request);

        assertSession(0, null);
        assertThat(request.requestContext.sessionToken).isNull();
        assertThat(request.requestContext.getSessionTokenEvaluationResults()).hasSize(1);
        Mockito.verify(rangeCache).tryLookupAsync(any(), eq(COLLECTION_RID), same(routingMap), any());
        Mockito.verify(rangeCache, Mockito.times(2)).tryLookupAsync(any(), any(), any(), any());
        verifyNoAmbientLookup();
    }

    @DataProvider
    public Object[][] metadataFailureStage() {
        return new Object[][] {{true}, {false}};
    }

    @Test(groups = "unit", dataProvider = "metadataFailureStage")
    public void propagatesMetadataErrorsWithoutSendingWeakerProgress(boolean duringRefresh) {
        RxDocumentServiceRequest request = request(OperationType.Read, COMPOUND + ",99:1#999#1=999", true);
        IllegalStateException failure = new IllegalStateException("metadata unavailable");
        if (duringRefresh) {
            Mockito.when(rangeCache.tryLookupAsync(any(), eq(COLLECTION_RID), same(routingMap), any()))
                .thenReturn(Mono.error(failure));
        } else {
            Mockito.when(rangeCache.tryLookupAsync(any(), eq(COLLECTION_RID), isNull(), any()))
                .thenReturn(Mono.error(failure));
        }

        StepVerifier.create(dispatch(request)).expectErrorMatches(error -> error == failure).verify();

        assertThat(sent).isEmpty();
        assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN))
            .isEqualTo(COMPOUND + ",99:1#999#1=999");
        assertThat(request.requestContext.getSessionTokenEvaluationResults()).isEmpty();
        verifyNoAmbientLookup();
    }

    @Test(groups = "unit")
    public void cancellationDuringRefreshCancelsMetadataAndPreventsDispatch() {
        RxDocumentServiceRequest request = request(OperationType.Read, COMPOUND + ",99:1#999#1=999", true);
        AtomicBoolean cancelled = new AtomicBoolean();
        Mockito.when(rangeCache.tryLookupAsync(any(), eq(COLLECTION_RID), same(routingMap), any()))
            .thenReturn(Mono.<Utils.ValueHolder<CollectionRoutingMap>>never().doOnCancel(() -> cancelled.set(true)));

        StepVerifier.create(dispatch(request))
            .then(() -> Mockito.verify(rangeCache).tryLookupAsync(any(), eq(COLLECTION_RID), same(routingMap), any()))
            .thenCancel().verify();

        assertThat(cancelled).isTrue();
        assertThat(sent).isEmpty();
        assertThat(request.requestContext.getSessionTokenEvaluationResults()).isEmpty();
        verifyNoAmbientLookup();
    }

    @Test(groups = "unit")
    public void missingPinnedRangeRetainsRangeGoneErrorAfterRefresh() {
        RxDocumentServiceRequest request = request(OperationType.Batch, COMPOUND, false);
        request.setPartitionKeyInternal(null);
        request.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity(COLLECTION_RID, "gone"));

        StepVerifier.create(dispatch(request)).expectError(PartitionKeyRangeGoneException.class).verify();

        assertThat(sent).isEmpty();
        Mockito.verify(rangeCache).tryLookupAsync(any(), eq(COLLECTION_RID), same(routingMap), any());
        verifyNoAmbientLookup();
    }

    private RxDocumentServiceRequest request(OperationType operation, String token, boolean resolved) {
        boolean sproc = operation == OperationType.ExecuteJavaScript;
        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(context, operation,
            sproc ? "/dbs/db/colls/coll/sprocs/sproc" : "/dbs/db/colls/coll/docs/item",
            sproc ? ResourceType.StoredProcedure : ResourceType.Document);
        request.setResourceId(COLLECTION_RID);
        request.setPartitionKeyDefinition(new PartitionKeyDefinition().setPaths(Collections.singletonList("/pk")));
        request.setPartitionKeyInternal(PartitionKeyInternal.fromObjectArray(Collections.singletonList("secret-key"), true));
        request.setResponseTimeout(Duration.ofSeconds(5));
        request.requestContext.cosmosDiagnostics = context.createDiagnostics();
        request.requestContext.resolvedCollectionRid = COLLECTION_RID;
        if (token != null) {
            request.getHeaders().put(HttpConstants.HttpHeaders.SESSION_TOKEN, token);
        }
        if (resolved) {
            request.requestContext.resolvedPartitionKeyRange = targetRange;
        } else {
            Mockito.when(routingMap.getRangeByEffectivePartitionKey(any())).thenReturn(null);
        }
        return request;
    }

    private void send(RxDocumentServiceRequest request) {
        StepVerifier.create(dispatch(request)).expectNextCount(1).verifyComplete();
    }

    private Mono<RxDocumentServiceResponse> dispatch(RxDocumentServiceRequest request) {
        return selectStoreAsync(request).flatMap(store -> store.processMessage(request));
    }

    private RxStoreModel selectStore(RxDocumentServiceRequest request)
        throws InvocationTargetException, IllegalAccessException {
        return selectStoreAsync(request).block(Duration.ofSeconds(5));
    }

    private Mono<RxStoreModel> selectStoreAsync(RxDocumentServiceRequest request) {
        return Mono.defer(() -> rangeCache.tryLookupAsync(null, COLLECTION_RID, null, null))
            .flatMap(map -> {
                try {
                    return ((Mono<?>) tokenPreparer.invoke(client, request, COLLECTION_RID,
                        request.getPartitionKeyDefinition(), map.v)).cast(CollectionRoutingMap.class);
                } catch (InvocationTargetException e) {
                    return Mono.error(e.getCause());
                } catch (IllegalAccessException e) {
                    return Mono.error(e);
                }
            })
            .flatMap(finalMap -> {
                RequestOptions options = new RequestOptions();
                options.setPartitionKeyDefinition(request.getPartitionKeyDefinition());
                PartitionKeyRange prepared = request.useThinClientMode
                    && request.getOriginalSessionToken() != null ? request.requestContext.resolvedPartitionKeyRange : null;
                PartitionKeyRange preResolved = client
                    .setPartitionKeyRangeForPointOperationRequestForPerPartitionAutomaticFailover(
                        request, options, finalMap, null, !request.isReadOnlyRequest(), prepared);
                client.addPartitionLevelUnavailableRegionsForPointOperationRequestForPerPartitionCircuitBreaker(
                    request, options, finalMap, null, preResolved);
                return Mono.fromCallable(() -> RxStoreModel.class.cast(storeSelector.invoke(client, request)))
                    .onErrorMap(InvocationTargetException.class, InvocationTargetException::getCause);
            });
    }

    private void setClientField(String name, Object value) throws Exception {
        Field field = RxDocumentClientImpl.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(client, value);
    }

    private void assertSession(int index, String expected) {
        byte[] actual = ThinClientRangeHeadersTest.decodeHeaders(sent.get(index)).get(RntbdRequestHeader.SessionToken.id());
        if (expected == null) {
            assertThat(actual).isNull();
        } else {
            assertThat(actual).containsExactly(expected.getBytes(StandardCharsets.UTF_8));
        }
    }

    private void verifyNoAmbientLookup() {
        Mockito.verify(ambient, Mockito.never()).resolveGlobalSessionToken(any());
        Mockito.verify(ambient, Mockito.never()).resolvePartitionLocalSessionToken(any(), any());
    }
}
