// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.ProxyOptions;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosContainerProactiveInitConfig;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.Http2ConnectionConfig;
import com.azure.cosmos.SessionRetryOptions;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
import com.azure.cosmos.implementation.caches.RxClientCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpClientConfig;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import com.azure.cosmos.implementation.query.DocumentQueryExecutionContextFactory;
import com.azure.cosmos.implementation.query.IDocumentQueryExecutionContext;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.IServerIdentity;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.CosmosAuthorizationTokenResolver;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class RxDocumentClientImplTest {
    private final static
    ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor diagnosticsAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor();

    private URI serviceEndpointMock;
    private String masterKeyOrResourceTokenMock;
    private List<Permission> permissionFeedMock;
    private ConnectionPolicy connectionPolicyMock;
    private ConsistencyLevel consistencyLevelMock;
    private Configs configsMock;
    private CosmosAuthorizationTokenResolver cosmosAuthorizationTokenResolverMock;
    private AzureKeyCredential azureKeyCredentialMock;
    private CosmosClientMetadataCachesSnapshot metadataCachesSnapshotMock;
    private ApiType apiTypeMock;
    private CosmosClientTelemetryConfig cosmosClientTelemetryConfigMock;
    private String clientCorrelationIdMock;
    private RxClientCollectionCache collectionCacheMock;
    private RxPartitionKeyRangeCache partitionKeyRangeCacheMock;
    private IRetryPolicyFactory resetSessionTokenRetryPolicyMock;
    private CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig;
    private SessionRetryOptions sessionRetryOptionsMock;
    private CosmosContainerProactiveInitConfig containerProactiveInitConfigMock;
    private CosmosItemSerializer defaultItemSerializer;

    @BeforeClass(groups = "unit")
    public void setUp() {
        // create mocks
        this.serviceEndpointMock = Mockito.mock(URI.class);
        this.masterKeyOrResourceTokenMock = "";
        this.permissionFeedMock = new ArrayList<>();
        this.connectionPolicyMock = Mockito.mock(ConnectionPolicy.class);
        this.consistencyLevelMock = Mockito.mock(ConsistencyLevel.class);
        this.configsMock = Mockito.mock(Configs.class);
        this.cosmosAuthorizationTokenResolverMock = Mockito.mock(CosmosAuthorizationTokenResolver.class);
        this.azureKeyCredentialMock = new AzureKeyCredential("fakeKey");
        this.metadataCachesSnapshotMock = Mockito.mock(CosmosClientMetadataCachesSnapshot.class);
        this.apiTypeMock = Mockito.mock(ApiType.class);
        this.cosmosClientTelemetryConfigMock = Mockito.mock(CosmosClientTelemetryConfig.class);
        this.clientCorrelationIdMock = "";
        this.collectionCacheMock = Mockito.mock(RxClientCollectionCache.class);
        this.partitionKeyRangeCacheMock = Mockito.mock(RxPartitionKeyRangeCache.class);
        this.resetSessionTokenRetryPolicyMock = Mockito.mock(IRetryPolicyFactory.class);
        this.endToEndOperationLatencyPolicyConfig = Mockito.mock(CosmosEndToEndOperationLatencyPolicyConfig.class);
        this.sessionRetryOptionsMock = Mockito.mock(SessionRetryOptions.class);
        this.containerProactiveInitConfigMock = Mockito.mock(CosmosContainerProactiveInitConfig.class);
        this.defaultItemSerializer = Mockito.mock(CosmosItemSerializer.class);
    }

    // todo: fix and revert enabled = false when circuit breaker is enabled
    @Test(groups = {"unit"}, enabled = true)
    public void readMany() throws Exception {

        // setup static method mocks
        MockedStatic<HttpClient> httpClientMock = Mockito.mockStatic(HttpClient.class);
        MockedStatic<PartitionKeyInternalHelper> partitionKeyInternalHelperMock = Mockito.mockStatic(PartitionKeyInternalHelper.class);
        MockedStatic<DocumentQueryExecutionContextFactory> documentQueryExecutionFactoryMock = Mockito.mockStatic(DocumentQueryExecutionContextFactory.class);
//        MockedStatic<ObservableHelper> observableHelperMock = Mockito.mockStatic(ObservableHelper.class);

        // setup mocks
        DocumentClientRetryPolicy documentClientRetryPolicyMock = Mockito.mock(DocumentClientRetryPolicy.class);
        RxGatewayStoreModel gatewayStoreModelMock = Mockito.mock(RxGatewayStoreModel.class);
        RxStoreModel serverStoreModelMock = Mockito.mock(RxStoreModel.class);

        // dummy values
        PartitionKeyRange dummyPartitionKeyRange1 = new PartitionKeyRange()
            .setId(UUID.randomUUID().toString())
            .setMinInclusive("AA")
            .setMaxExclusive("FF");

        PartitionKeyRange dummyPartitionKeyRange2 = new PartitionKeyRange()
            .setId(UUID.randomUUID().toString())
            .setMinInclusive("BB")
            .setMaxExclusive("CCC");

        PartitionKeyRange dummyPartitionKeyRange3 = new PartitionKeyRange()
            .setId(UUID.randomUUID().toString())
            .setMinInclusive("DD")
            .setMaxExclusive("FFF");

        Duration dummyDuration = Duration.ZERO;
        ProxyOptions dummyProxyOptions = new ProxyOptions(null, null);
        int dummyInt = 1;

        // dummy point read result
        String pointReadResult = "{\"id\": \"1\"}";

        // dummy query results
        List<String> queryResults = new ArrayList<>();

        queryResults.add("{\"id\": \"2\"}");
        queryResults.add("{\"id\": \"3\"}");

        // dummy headers
        Map<String, String> headersForPointReads = new HashMap<>();

        String activityIdPointRead = UUID.randomUUID().toString();
        headersForPointReads.put(HttpConstants.HttpHeaders.ACTIVITY_ID, activityIdPointRead);
        headersForPointReads.put(HttpConstants.HttpHeaders.REQUEST_CHARGE, "1");

        Map<String, String> headersForQueries = new HashMap<>();

        String activityIdQuery = UUID.randomUUID().toString();
        headersForQueries.put(HttpConstants.HttpHeaders.ACTIVITY_ID, activityIdQuery);
        headersForQueries.put(HttpConstants.HttpHeaders.REQUEST_CHARGE, "2.7");

        // map effective partition key string to partition key range
        Map<String, PartitionKeyRange> epksPartitionKeyRangeMap = new HashMap<>();

        epksPartitionKeyRangeMap.put("AAA", dummyPartitionKeyRange1);
        epksPartitionKeyRangeMap.put("BBB", dummyPartitionKeyRange2);
        epksPartitionKeyRangeMap.put("CCC", dummyPartitionKeyRange3);

        // set up mock behavior
        Mockito.when(this.connectionPolicyMock.getIdleHttpConnectionTimeout()).thenReturn(dummyDuration);
        Mockito.when(this.connectionPolicyMock.getMaxConnectionPoolSize()).thenReturn(dummyInt);
        Mockito.when(this.connectionPolicyMock.getProxy()).thenReturn(dummyProxyOptions);
        Mockito.when(this.connectionPolicyMock.getHttpNetworkRequestTimeout()).thenReturn(dummyDuration);
        Mockito.when(this.connectionPolicyMock.getHttp2ConnectionConfig()).thenReturn(new Http2ConnectionConfig());

        httpClientMock
            .when(() -> HttpClient.createFixed(Mockito.any(HttpClientConfig.class)))
            .thenReturn(dummyHttpClient());
        partitionKeyInternalHelperMock
            .when(() -> PartitionKeyInternalHelper.getEffectivePartitionKeyString(Mockito.any(), Mockito.any()))
            .thenReturn("AAA", "BBB", "BBB");
        documentQueryExecutionFactoryMock
            .when(() -> DocumentQueryExecutionContextFactory.createReadManyQueryAsync(
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any()
            ))
            .thenReturn(Flux.just(dummyExecutionContextForQuery(queryResults, headersForQueries, InternalObjectNode.class)));

        Mockito
            .when(this.collectionCacheMock.resolveCollectionAsync(Mockito.isNull(), Mockito.any(RxDocumentServiceRequest.class)))
            .thenReturn(Mono.just(dummyCollectionObs()));

        Mockito
            .when(this.collectionCacheMock.resolveByNameAsync(Mockito.any(), Mockito.anyString(), Mockito.isNull()))
            .thenReturn(Mono.just(dummyCollectionObs().v));

        Mockito
            .when(this.partitionKeyRangeCacheMock.tryLookupAsync(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(Mono.just(dummyCollectionRoutingMap(epksPartitionKeyRangeMap)));

        RetryContext retryContext = new RetryContext();

        Mockito.when(this.resetSessionTokenRetryPolicyMock.getRequestPolicy(null)).thenReturn(dummyDocumentClientRetryPolicy());
        Mockito.when(this.cosmosAuthorizationTokenResolverMock.getAuthorizationToken(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any())).thenReturn("abcdefgh");
        Mockito.when(this.resetSessionTokenRetryPolicyMock.getRequestPolicy(Mockito.any())).thenReturn(documentClientRetryPolicyMock);
        Mockito.when(documentClientRetryPolicyMock.getRetryContext()).thenReturn(retryContext);
        Mockito
            .when(serverStoreModelMock.processMessage(Mockito.any(RxDocumentServiceRequest.class)))
            .thenReturn(Mono.just(mockRxDocumentServiceResponse(pointReadResult, headersForPointReads)));

        // initialize object to be tested
        RxDocumentClientImpl rxDocumentClient = new RxDocumentClientImpl(
            this.serviceEndpointMock,
            this.masterKeyOrResourceTokenMock,
            this.permissionFeedMock,
            this.connectionPolicyMock,
            this.consistencyLevelMock,
            null,
            this.configsMock,
            this.cosmosAuthorizationTokenResolverMock,
            this.azureKeyCredentialMock,
            false,
            false,
            false,
            this.metadataCachesSnapshotMock,
            this.apiTypeMock,
            this.cosmosClientTelemetryConfigMock,
            this.clientCorrelationIdMock,
            this.endToEndOperationLatencyPolicyConfig,
            this.sessionRetryOptionsMock,
            this.containerProactiveInitConfigMock,
            this.defaultItemSerializer,
            false
        );

        try {
            ReflectionUtils.setCollectionCache(rxDocumentClient, this.collectionCacheMock);
            ReflectionUtils.setPartitionKeyRangeCache(rxDocumentClient, this.partitionKeyRangeCacheMock);
            ReflectionUtils.setResetSessionTokenRetryPolicy(rxDocumentClient, this.resetSessionTokenRetryPolicyMock);
            ReflectionUtils.setGatewayProxy(rxDocumentClient, gatewayStoreModelMock);
            ReflectionUtils.setServerStoreModel(rxDocumentClient, serverStoreModelMock);

            ArrayList<CosmosItemIdentity> cosmosItemIdentities = new ArrayList<CosmosItemIdentity>();

            cosmosItemIdentities.add(new CosmosItemIdentity(new PartitionKey("1"), "1"));
            cosmosItemIdentities.add(new CosmosItemIdentity(new PartitionKey("2"), "2"));
            cosmosItemIdentities.add(new CosmosItemIdentity(new PartitionKey("3"), "3"));

            String collectionLink = "";
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
            Class<InternalObjectNode> klass = InternalObjectNode.class;

        QueryFeedOperationState stateMock = Mockito.mock(QueryFeedOperationState.class);
        httpClientMock
            .when(() -> stateMock.getQueryOptions())
            .thenReturn(new CosmosQueryRequestOptions());

        StepVerifier.create(
                rxDocumentClient.readMany(
                    cosmosItemIdentities,
                    collectionLink,
                    stateMock,
                    klass
                )
            )
            .consumeNextWith(feedResponse -> {

                            int expectedResultSize = 3;
                            int expectedClientSideRequestStatisticsSize = 1;
                            double expectedRequestCharge = 3.7;

                            assertThat(feedResponse.getResults()).isNotNull();
                            assertThat(feedResponse.getResults().size()).isEqualTo(expectedResultSize);
                            assertThat(feedResponse.getRequestCharge()).isEqualTo(expectedRequestCharge);

                            assertThat(diagnosticsAccessor.getClientSideRequestStatistics(feedResponse.getCosmosDiagnostics())).isNotNull();
                            assertThat(diagnosticsAccessor.getClientSideRequestStatistics(feedResponse.getCosmosDiagnostics()).size()).isEqualTo(expectedClientSideRequestStatisticsSize);
                            assertThat(BridgeInternal.queryMetricsFromFeedResponse(feedResponse)).isNotNull();

                            List<InternalObjectNode> readManyResults = feedResponse.getResults();
                            Set<String> idSet = new HashSet<>(Arrays.asList("1", "2", "3"));

                            for (InternalObjectNode result : readManyResults) {
                                assertThat(idSet.contains(result.getId())).isTrue();
                            }

                        })
                        .expectComplete()
                        .verify();
        } finally {
            // release static mocks
            httpClientMock.close();
            partitionKeyInternalHelperMock.close();
            documentQueryExecutionFactoryMock.close();

            // de-register client
            rxDocumentClient.close();
        }
    }

    private static HttpClient dummyHttpClient() {
        return new HttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return null;
            }

            @Override
            public Mono<HttpResponse> send(HttpRequest request, Duration responseTimeout) {
                return null;
            }

            @Override
            public void shutdown() {
            }
        };
    }

    private static Utils.ValueHolder<DocumentCollection> dummyCollectionObs() {
        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        partitionKeyDefinition.setPaths(Arrays.asList("/id"));
        Utils.ValueHolder<DocumentCollection> collectionObs = new Utils.ValueHolder<>();
        collectionObs.v = new DocumentCollection();
        collectionObs.v.setPartitionKey(partitionKeyDefinition);

        return collectionObs;
    }

    private static Utils.ValueHolder<CollectionRoutingMap> dummyCollectionRoutingMap(Map<String, PartitionKeyRange> epksPartitionKeyRangeMap) {
        Utils.ValueHolder<CollectionRoutingMap> routingMap = new Utils.ValueHolder<>();
        routingMap.v = new CollectionRoutingMap() {
            @Override
            public List<PartitionKeyRange> getOrderedPartitionKeyRanges() {
                return null;
            }

            @Override
            public PartitionKeyRange getRangeByEffectivePartitionKey(String effectivePartitionKeyValue) {
                return epksPartitionKeyRangeMap.get(effectivePartitionKeyValue);
            }

            @Override
            public PartitionKeyRange getRangeByPartitionKeyRangeId(String partitionKeyRangeId) {
                return null;
            }

            @Override
            public List<PartitionKeyRange> getOverlappingRanges(Range<String> range) {
                return null;
            }

            @Override
            public List<PartitionKeyRange> getOverlappingRanges(Collection<Range<String>> providedPartitionKeyRanges) {
                return null;
            }

            @Override
            public PartitionKeyRange tryGetRangeByPartitionKeyRangeId(String partitionKeyRangeId) {
                return null;
            }

            @Override
            public IServerIdentity tryGetInfoByPartitionKeyRangeId(String partitionKeyRangeId) {
                return null;
            }

            @Override
            public boolean isGone(String partitionKeyRangeId) {
                return false;
            }

            @Override
            public String getCollectionUniqueId() {
                return null;
            }

            @Override
            public CollectionRoutingMap tryCombine(List<ImmutablePair<PartitionKeyRange, IServerIdentity>> ranges) {
                return null;
            }
        };
        return routingMap;
    }

    @SuppressWarnings("unchecked")
    private static <T> IDocumentQueryExecutionContext<T> dummyExecutionContextForQuery(
            List<String> results,
            Map<String, String> headers,
            Class<T> klass) {
        List<T> documentResults =
                results
                        .stream()
                        .map(str -> new Document(str))
                        .map(document -> document.toObject(klass))
                        .collect(Collectors.toList());

        return () -> Flux.just(ModelBridgeInternal.createFeedResponse(documentResults, headers));
    }

    private static DocumentClientRetryPolicy dummyDocumentClientRetryPolicy() {
        return new DocumentClientRetryPolicy() {
            @Override
            public void onBeforeSendRequest(RxDocumentServiceRequest request) {}

            @Override
            public Mono<ShouldRetryResult> shouldRetry(Exception e) {
                return null;
            }

            @Override
            public RetryContext getRetryContext() {
                return null;
            }
        };
    }

    private static RxDocumentServiceResponse mockRxDocumentServiceResponse(String content, Map<String, String> headers) throws Exception {
        byte[] blob = content.getBytes(StandardCharsets.UTF_8);
        StoreResponse storeResponse = new StoreResponse(
            null,
            HttpResponseStatus.OK.code(),
            headers,
            new ByteBufInputStream(Unpooled.wrappedBuffer(blob), true),
            blob.length,
            null);

        RxDocumentServiceResponse documentServiceResponse = new RxDocumentServiceResponse(new DiagnosticsClientContext() {

            private final AtomicReference<CosmosDiagnostics> mostRecentlyCreatedDiagnostics = new AtomicReference<>(null);

            @Override
            public DiagnosticsClientConfig getConfig() {
                return null;
            }

            @Override
            public CosmosDiagnostics createDiagnostics() {
                CosmosDiagnostics diagnostics = diagnosticsAccessor.create(this, 1d) ;
                mostRecentlyCreatedDiagnostics.set(diagnostics);
                return diagnostics;
            }

            @Override
            public String getUserAgent() {
                return Utils.getUserAgent();
            }

            @Override
            public CosmosDiagnostics getMostRecentlyCreatedDiagnostics() {
                return mostRecentlyCreatedDiagnostics.get();
            }

        }, storeResponse);

        documentServiceResponse.setCosmosDiagnostics(dummyCosmosDiagnostics());

        return documentServiceResponse;
    }

    private static CosmosDiagnostics dummyCosmosDiagnostics() {
        return diagnosticsAccessor.create(new DiagnosticsClientContext() {

            @Override
            public DiagnosticsClientConfig getConfig() {
                return new DiagnosticsClientConfig();
            }

            @Override
            public CosmosDiagnostics createDiagnostics() {
                return null;
            }

            @Override
            public String getUserAgent() {
                return Utils.getUserAgent();
            }

            @Override
            public CosmosDiagnostics getMostRecentlyCreatedDiagnostics() {
                return null;
            }
        }, 1d);
    }
}
