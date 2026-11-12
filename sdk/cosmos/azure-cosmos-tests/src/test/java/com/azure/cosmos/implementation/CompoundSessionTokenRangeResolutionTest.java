// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.caches.RxClientCollectionCache;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.perPartitionAutomaticFailover.GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover;
import com.azure.cosmos.implementation.perPartitionCircuitBreaker.GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker;
import com.azure.cosmos.implementation.query.IDocumentQueryClient;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKind;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

public class CompoundSessionTokenRangeResolutionTest {
    private static final String COMPOUND_TOKEN = "0:1#100#1=100,1:1#200#1=200";
    private RxDocumentClientImpl client;
    private ThinClientConnectivityConfig connectivity;
    private GlobalEndpointManager endpointManager;
    private GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker circuitBreaker;
    private GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover automaticFailover;
    private CollectionRoutingMap routingMap;
    private RxPartitionKeyRangeCache rangeCache;
    private PartitionKeyDefinition definition;
    private PartitionKeyRange range;
    private MockedStatic<Configs> configs;

    @BeforeMethod(groups = "unit")
    public void setUp() throws Exception {
        configs = Mockito.mockStatic(Configs.class, Mockito.CALLS_REAL_METHODS);
        configs.when(Configs::isThinClientEnabled).thenReturn(null);
        client = Mockito.mock(RxDocumentClientImpl.class, Mockito.CALLS_REAL_METHODS);
        connectivity = Mockito.mock(ThinClientConnectivityConfig.class);
        endpointManager = Mockito.mock(GlobalEndpointManager.class);
        circuitBreaker = Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.class);
        automaticFailover = Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover.class);
        rangeCache = Mockito.mock(RxPartitionKeyRangeCache.class);
        setClientField("thinClientConnectivityConfig", connectivity);
        setClientField("globalEndpointManager", endpointManager);
        setClientField("globalPartitionEndpointManagerForPerPartitionCircuitBreaker", circuitBreaker);
        setClientField("globalPartitionEndpointManagerForPerPartitionAutomaticFailover", automaticFailover);
        setClientField("partitionKeyRangeCache", rangeCache);
        RxGatewayStoreModel thinProxy = Mockito.mock(RxGatewayStoreModel.class);
        Mockito.when(thinProxy.shouldApplySessionToken(any())).thenReturn(true);
        setClientField("thinProxy", thinProxy);
        Mockito.when(connectivity.canThinClientBeUsed()).thenReturn(true);
        Mockito.when(endpointManager.hasThinClientReadLocations()).thenReturn(true);
        Mockito.when(endpointManager.getProxyProbeDecision()).thenReturn(true);
        Mockito.doReturn(new DiagnosticsClientContext.DiagnosticsClientConfig()).when(client).getConfig();
        Mockito.doReturn("unit-test").when(client).getUserAgent();
        Mockito.doReturn(ImplementationBridgeHelpers.CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor()
            .create(client, 1d)).when(client).createDiagnostics();
        routingMap = Mockito.mock(CollectionRoutingMap.class);
        definition = new PartitionKeyDefinition().setPaths(Collections.singletonList("/pk"));
        range = new PartitionKeyRange().setId("1").setMinInclusive("").setMaxExclusive("FF");
        Mockito.when(routingMap.getRangeByEffectivePartitionKey(any())).thenReturn(range);
        Mockito.when(routingMap.getRangeByPartitionKeyRangeId("1")).thenReturn(range);
        Mockito.when(routingMap.tryGetRangeByPartitionKeyRangeId("1")).thenReturn(range);
        Mockito.when(routingMap.tryGetRangeByPartitionKeyRangeId("0"))
            .thenReturn(new PartitionKeyRange("0", "", "FF"));
    }

    @AfterMethod(groups = "unit", alwaysRun = true)
    public void tearDown() {
        if (configs != null) {
            configs.close();
            configs = null;
        }
    }

    @DataProvider
    public Object[][] pointOperations() {
        return new Object[][] {
            {OperationType.Create}, {OperationType.Upsert}, {OperationType.Read}, {OperationType.Replace},
            {OperationType.Delete}, {OperationType.Patch}, {OperationType.Batch}, {OperationType.ExecuteJavaScript}
        };
    }

    @Test(groups = "unit", dataProvider = "pointOperations")
    public void resolvesLogicalKeyWithoutEnablingFailover(OperationType operation) {
        RxDocumentServiceRequest request = request(operation, COMPOUND_TOKEN);
        request.setPartitionKeyInternal(PartitionKeyInternal.fromJsonString("[\"value\"]"));

        populatePointRange(request, null);

        String epk = PartitionKeyInternalHelper.getEffectivePartitionKeyString(
            request.getPartitionKeyInternal(), definition);
        assertThat(request.requestContext.resolvedPartitionKeyRange).isSameAs(range);
        assertThat(request.getEffectivePartitionKey()).isEqualTo(epk);
        assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(COMPOUND_TOKEN);
        assertThat(request.requestContext.sessionToken).isNull();
        assertThat(request.useThinClientMode).isFalse();
        assertThat(request.requestContext.resolvedPartitionKeyRangeForCircuitBreaker).isNull();
        assertThat(request.requestContext.resolvedPartitionKeyRangeForPerPartitionAutomaticFailover).isNull();
        assertThat(request.isPerPartitionAutomaticFailoverEnabledAndWriteRequest).isFalse();
        Mockito.verify(routingMap).getRangeByEffectivePartitionKey(epk);
        Mockito.verifyNoInteractions(rangeCache);
    }

    @Test(groups = "unit")
    public void resolvesPhysicalRangeAndPreservesItsIdentity() {
        RxDocumentServiceRequest request = request(OperationType.Batch, COMPOUND_TOKEN);
        PartitionKeyRangeIdentity identity = new PartitionKeyRangeIdentity("1");
        request.setPartitionKeyRangeIdentity(identity);

        populatePointRange(request, null);

        assertThat(request.requestContext.resolvedPartitionKeyRange).isSameAs(range);
        assertThat(request.getPartitionKeyRangeIdentity()).isSameAs(identity);
        Mockito.verify(routingMap).getRangeByPartitionKeyRangeId("1");
        Mockito.verify(routingMap, Mockito.never()).getRangeByEffectivePartitionKey(any());
    }

    @Test(groups = "unit")
    public void resolvesRangeFromPreservedCallerTokenAfterHeaderOmission() {
        RxDocumentServiceRequest request = request(OperationType.Read, null);
        request.setOriginalSessionToken(COMPOUND_TOKEN);
        request.setPartitionKeyInternal(PartitionKeyInternal.fromJsonString("[\"value\"]"));

        populatePointRange(request, null);

        assertThat(request.requestContext.resolvedPartitionKeyRange).isSameAs(range);
        assertThat(request.getHeaders()).doesNotContainKey(HttpConstants.HttpHeaders.SESSION_TOKEN);
        Mockito.verifyNoInteractions(rangeCache);
    }

    @DataProvider
    public Object[][] bypasses() {
        return new Object[][] {
            {"absent"}, {"single"}, {"empty"}, {"gateway-v1"}, {"direct"}, {"no-locations"},
            {"forced-gateway"}, {"resource-token"}, {"query-plan"}
        };
    }

    @Test(groups = "unit", dataProvider = "bypasses")
    public void doesNotResolveOutsideCompoundGatewayV2Requests(String scenario) throws Exception {
        RxDocumentServiceRequest request = request(
            "query-plan".equals(scenario) ? OperationType.QueryPlan : OperationType.Read, COMPOUND_TOKEN);
        request.setPartitionKeyInternal(PartitionKeyInternal.fromJsonString("[\"value\"]"));
        switch (scenario) {
            case "absent":
                request.getHeaders().remove(HttpConstants.HttpHeaders.SESSION_TOKEN);
                break;
            case "single":
                request.getHeaders().put(HttpConstants.HttpHeaders.SESSION_TOKEN, "1:1#200#1=200");
                break;
            case "empty":
                request.getHeaders().put(HttpConstants.HttpHeaders.SESSION_TOKEN, "");
                break;
            case "gateway-v1":
                Mockito.when(endpointManager.getProxyProbeDecision()).thenReturn(false);
                break;
            case "direct":
                Mockito.when(connectivity.canThinClientBeUsed()).thenReturn(false);
                break;
            case "no-locations":
                Mockito.when(endpointManager.hasThinClientReadLocations()).thenReturn(false);
                break;
            case "forced-gateway":
                request.useGatewayMode = true;
                break;
            case "resource-token":
                setClientField("authorizationTokenType", AuthorizationTokenType.ResourceToken);
                break;
            default:
                break;
        }

        populatePointRange(request, null);

        assertThat(request.requestContext.resolvedPartitionKeyRange).isNull();
        Mockito.verifyNoInteractions(routingMap, rangeCache);
    }

    @Test(groups = "unit")
    public void reusesPreviouslyResolvedRangeAndCachedEpk() {
        RxDocumentServiceRequest request = request(OperationType.Read, COMPOUND_TOKEN);
        request.requestContext.resolvedPartitionKeyRange = range;
        populatePointRange(request, null);
        Mockito.verifyNoInteractions(routingMap);

        request.requestContext.resolvedPartitionKeyRange = null;
        request.setPartitionKeyInternal(PartitionKeyInternal.fromJsonString("[\"value\"]"));
        request.setEffectivePartitionKey("cached-epk");
        populatePointRange(request, null);
        Mockito.verify(routingMap).getRangeByEffectivePartitionKey("cached-epk");
        assertThat(request.requestContext.resolvedPartitionKeyRange).isSameAs(range);
    }

    @Test(groups = "unit")
    public void reusesPpafRangeWithoutAnotherLookup() {
        RxDocumentServiceRequest request = request(OperationType.Batch, COMPOUND_TOKEN);
        request.setPartitionKeyInternal(PartitionKeyInternal.fromJsonString("[\"value\"]"));
        RequestOptions options = new RequestOptions();
        options.setPartitionKeyDefinition(definition);
        Mockito.when(automaticFailover.isPerPartitionAutomaticFailoverApplicable(request)).thenReturn(true);
        Mockito.when(automaticFailover.isPerPartitionAutomaticFailoverEnabled()).thenReturn(true);
        PartitionKeyRange preResolved = client.setPartitionKeyRangeForPointOperationRequestForPerPartitionAutomaticFailover(
            request, options, routingMap, null, true, null);

        populatePointRange(request, preResolved);

        assertThat(request.requestContext.resolvedPartitionKeyRange).isSameAs(range);
        assertThat(request.requestContext.resolvedPartitionKeyRangeForPerPartitionAutomaticFailover).isSameAs(range);
        assertThat(request.isPerPartitionAutomaticFailoverEnabledAndWriteRequest).isTrue();
        Mockito.verify(routingMap).getRangeByEffectivePartitionKey(any());
    }

    @Test(groups = "unit")
    public void preservesPpcbBookkeeping() {
        RxDocumentServiceRequest request = request(OperationType.Read, COMPOUND_TOKEN);
        request.setPartitionKeyInternal(PartitionKeyInternal.fromJsonString("[\"value\"]"));
        Mockito.when(circuitBreaker.isPerPartitionLevelCircuitBreakingApplicable(request)).thenReturn(true);

        populatePointRange(request, null);

        assertThat(request.requestContext.resolvedPartitionKeyRange).isSameAs(range);
        assertThat(request.requestContext.resolvedPartitionKeyRangeForCircuitBreaker).isSameAs(range);
        Mockito.verify(circuitBreaker).getUnavailableRegionsForPartitionKeyRange(
            request, request.getResourceId(), range);
        Mockito.verify(routingMap).getRangeByEffectivePartitionKey(any());
    }

    @Test(groups = "unit")
    public void unresolvedPhysicalRangeRemainsUnsetWithoutChangingTokenOrRouting() {
        RxDocumentServiceRequest request = request(OperationType.Batch, COMPOUND_TOKEN);
        request.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity("gone"));

        populatePointRange(request, null);

        assertThat(request.requestContext.resolvedPartitionKeyRange).isNull();
        assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(COMPOUND_TOKEN);
        assertThat(client.shouldResolvePartitionKeyRangeForCompoundSessionToken(request)).isTrue();
        Mockito.verifyNoInteractions(rangeCache);
    }

    @DataProvider
    public Object[][] epkRangeCounts() {
        return new Object[][] {{0}, {1}, {2}};
    }

    @Test(groups = "unit", dataProvider = "epkRangeCounts")
    public void resolvesEpkIntervalOnlyWhenItHasOnePhysicalRange(int count) {
        definition = new PartitionKeyDefinition().setPaths(Arrays.asList("/tenant", "/user"))
            .setKind(PartitionKind.MULTI_HASH);
        RxDocumentServiceRequest request = request(OperationType.Query, COMPOUND_TOKEN);
        request.setPartitionKeyInternal(PartitionKeyInternal.fromJsonString("[\"tenant\"]"));
        request.getHeaders().put(HttpConstants.HttpHeaders.READ_FEED_KEY_TYPE,
            ReadFeedKeyType.EffectivePartitionKeyRange.name());
        request.getHeaders().put(HttpConstants.HttpHeaders.START_EPK, "");
        request.getHeaders().put(HttpConstants.HttpHeaders.END_EPK, "AA");
        Mockito.when(routingMap.getOverlappingRanges(Mockito.<Range<String>>any()))
            .thenReturn(Collections.nCopies(count, range));

        client.addPartitionLevelUnavailableRegionsForFeedRequestForPerPartitionCircuitBreaker(
            request, queryOptions(), routingMap, null);

        assertThat(request.requestContext.resolvedPartitionKeyRange).isSameAs(count == 1 ? range : null);
        assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.START_EPK)).isEmpty();
        assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.END_EPK)).isEqualTo("AA");
        assertThat(request.getEffectivePartitionKey()).isNull();
        Mockito.verify(routingMap, Mockito.never()).getRangeByEffectivePartitionKey(any());
    }

    @Test(groups = "unit")
    public void doesNotTreatPrefixKeyOrUnscopedFeedAsOnePhysicalRange() {
        definition = new PartitionKeyDefinition().setPaths(Arrays.asList("/tenant", "/user"))
            .setKind(PartitionKind.MULTI_HASH);
        RxDocumentServiceRequest request = request(OperationType.Query, COMPOUND_TOKEN);
        request.setPartitionKeyInternal(PartitionKeyInternal.fromJsonString("[\"tenant\"]"));
        client.addPartitionLevelUnavailableRegionsForFeedRequestForPerPartitionCircuitBreaker(
            request, queryOptions(), routingMap, null);
        assertThat(request.requestContext.resolvedPartitionKeyRange).isNull();

        request.setPartitionKeyInternal(null);
        client.addPartitionLevelUnavailableRegionsForFeedRequestForPerPartitionCircuitBreaker(
            request, queryOptions(), routingMap, null);
        assertThat(request.requestContext.resolvedPartitionKeyRange).isNull();
        Mockito.verifyNoInteractions(routingMap);
    }

    @Test(groups = "unit")
    public void queryPreparationLoadsRoutingMapWithFailoverDisabled() throws Exception {
        RxDocumentServiceRequest request = request(OperationType.Query, COMPOUND_TOKEN);
        request.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity("1"));
        Mockito.when(rangeCache.tryLookupAsync(any(), eq("rid"), isNull(), isNull()))
            .thenReturn(Mono.just(new Utils.ValueHolder<>(routingMap)));

        StepVerifier.create(queryClient().addPartitionLevelUnavailableRegionsOnRequest(request, queryOptions(), null))
            .expectNext(request).verifyComplete();

        assertThat(request.requestContext.resolvedPartitionKeyRange).isSameAs(range);
        Mockito.verify(rangeCache).tryLookupAsync(any(), eq("rid"), isNull(), isNull());
    }

    @Test(groups = "unit")
    public void queryPreparationPropagatesMetadataFailure() throws Exception {
        RxDocumentServiceRequest request = request(OperationType.Query, COMPOUND_TOKEN);
        IllegalStateException failure = new IllegalStateException("metadata unavailable");
        Mockito.when(rangeCache.tryLookupAsync(any(), eq("rid"), isNull(), isNull()))
            .thenReturn(Mono.error(failure));

        StepVerifier.create(queryClient().addPartitionLevelUnavailableRegionsOnRequest(request, queryOptions(), null))
            .expectErrorMatches(error -> error == failure).verify();
        assertThat(request.requestContext.resolvedPartitionKeyRange).isNull();
    }

    @Test(groups = "unit")
    public void queryWithoutCompoundTokenDoesNotLoadRoutingMap() throws Exception {
        RxDocumentServiceRequest request = request(OperationType.Query, "1:1#200#1=200");
        StepVerifier.create(queryClient().addPartitionLevelUnavailableRegionsOnRequest(request, queryOptions(), null))
            .expectNext(request).verifyComplete();
        Mockito.verifyNoInteractions(rangeCache, routingMap);
    }

    @Test(groups = "unit")
    public void doesNotSuppressRangeResolutionErrors() {
        RxDocumentServiceRequest request = request(OperationType.Read, COMPOUND_TOKEN);
        request.setPartitionKeyInternal(PartitionKeyInternal.fromJsonString("[\"value\"]"));
        IllegalStateException failure = new IllegalStateException("invalid routing map");
        Mockito.when(routingMap.getRangeByEffectivePartitionKey(any())).thenThrow(failure);

        assertThatThrownBy(() -> populatePointRange(request, null)).isSameAs(failure);
        assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(COMPOUND_TOKEN);
    }

    @DataProvider
    public Object[][] feedTokens() {
        return new Object[][] {{COMPOUND_TOKEN, true}, {"1:1#200#1=200", false}};
    }

    @Test(groups = "unit", dataProvider = "feedTokens")
    public void changeFeedPreparationResolvesOnlyForCompoundTokens(String token, boolean resolves) throws Exception {
        RxDocumentServiceRequest request = request(OperationType.ReadFeed, token);
        request.getHeaders().put(HttpConstants.HttpHeaders.A_IM, "Incremental feed");
        request.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity("1"));
        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(
            FeedRangeEpkImpl.forFullRange());
        ChangeFeedQueryImpl<Document> query = new ChangeFeedQueryImpl<>(client, ResourceType.Document, Document.class,
            "dbs/db/colls/coll", "rid", options, client, null);
        RxClientCollectionCache collectionCache = Mockito.mock(RxClientCollectionCache.class);
        setClientField("collectionCache", collectionCache);
        DocumentCollection collection = new DocumentCollection();
        collection.setResourceId("rid");
        collection.setPartitionKey(definition);
        Mockito.when(collectionCache.resolveCollectionAsync(isNull(), eq(request)))
            .thenReturn(Mono.just(new Utils.ValueHolder<>(collection)));
        Mockito.when(rangeCache.tryLookupAsync(isNull(), eq("rid"), isNull(), isNull()))
            .thenReturn(Mono.just(new Utils.ValueHolder<>(routingMap)));
        Mockito.doReturn(Mono.just(request)).when(client).populateHeadersAsync(request, RequestVerb.GET);
        Method prerequisites = ChangeFeedQueryImpl.class.getDeclaredMethod(
            "handlePerPartitionFailoverPrerequisites", RxDocumentServiceRequest.class);
        prerequisites.setAccessible(true);

        Mono<?> prepared = (Mono<?>) prerequisites.invoke(query, request);
        StepVerifier.create(prepared.cast(RxDocumentServiceRequest.class)).expectNext(request).verifyComplete();

        assertThat(request.requestContext.resolvedPartitionKeyRange).isSameAs(resolves ? range : null);
        assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN))
            .isEqualTo(resolves ? "1:1#200#1=200" : token);
        if (resolves) {
            assertThat(request.getOriginalSessionToken()).isEqualTo(token);
        }
        if (resolves) {
            Mockito.verify(rangeCache).tryLookupAsync(isNull(), eq("rid"), isNull(), isNull());
        } else {
            Mockito.verifyNoInteractions(collectionCache, rangeCache, routingMap);
        }
    }

    private RxDocumentServiceRequest request(OperationType operation, String token) {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(client, operation,
            "dbs/db/colls/coll/docs/item",
            operation == OperationType.ExecuteJavaScript ? ResourceType.StoredProcedure : ResourceType.Document);
        if (token != null) {
            request.getHeaders().put(HttpConstants.HttpHeaders.SESSION_TOKEN, token);
        }
        request.requestContext.cosmosDiagnostics = client.createDiagnostics();
        return request;
    }

    private void populatePointRange(RxDocumentServiceRequest request, PartitionKeyRange preResolved) {
        RequestOptions options = new RequestOptions();
        options.setPartitionKeyDefinition(definition);
        client.addPartitionLevelUnavailableRegionsForPointOperationRequestForPerPartitionCircuitBreaker(
            request, options, routingMap, null, preResolved);
    }

    private CosmosQueryRequestOptions queryOptions() {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.getCosmosQueryRequestOptionsAccessor()
            .setPartitionKeyDefinition(options, definition);
        ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.getCosmosQueryRequestOptionsAccessor()
            .setCollectionRid(options, "rid");
        return options;
    }

    private IDocumentQueryClient queryClient() throws Exception {
        Method factory = RxDocumentClientImpl.class.getDeclaredMethod(
            "documentQueryClientImpl", RxDocumentClientImpl.class, OperationContextAndListenerTuple.class);
        factory.setAccessible(true);
        return (IDocumentQueryClient) factory.invoke(client, client, null);
    }

    private void setClientField(String name, Object value) throws Exception {
        Field field = RxDocumentClientImpl.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(client, value);
    }
}
