// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.ProxyOptions;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosContainerProactiveInitConfig;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfigBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.Http2ConnectionConfig;
import com.azure.cosmos.SessionRetryOptions;
import com.azure.cosmos.ThresholdBasedAvailabilityStrategy;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
import com.azure.cosmos.implementation.caches.RxClientCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.GatewayServiceConfigurationReader;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpClientConfig;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import com.azure.cosmos.implementation.perPartitionAutomaticFailover.GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover;
import com.azure.cosmos.implementation.perPartitionCircuitBreaker.GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker;
import com.azure.cosmos.implementation.query.DocumentQueryExecutionContextFactory;
import com.azure.cosmos.implementation.query.IDocumentQueryExecutionContext;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.IServerIdentity;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import com.azure.cosmos.models.CosmosAuthorizationTokenResolver;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Supplier;
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
    public void readMany() {

        // setup static method mocks
        MockedStatic<HttpClient> httpClientMock = Mockito.mockStatic(HttpClient.class);
        MockedStatic<PartitionKeyInternalHelper> partitionKeyInternalHelperMock = Mockito.mockStatic(PartitionKeyInternalHelper.class);
        MockedStatic<DocumentQueryExecutionContextFactory> documentQueryExecutionFactoryMock = Mockito.mockStatic(DocumentQueryExecutionContextFactory.class);
//        MockedStatic<ObservableHelper> observableHelperMock = Mockito.mockStatic(ObservableHelper.class);

        // setup mocks
        DocumentClientRetryPolicy documentClientRetryPolicyMock = Mockito.mock(DocumentClientRetryPolicy.class);
        GatewayServiceConfigurationReader gatewayServiceConfigurationReaderMock = Mockito.mock(GatewayServiceConfigurationReader.class);
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
            .when(this.partitionKeyRangeCacheMock.tryLookupAsync(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(Mono.just(dummyCollectionRoutingMap(epksPartitionKeyRangeMap)));

        RetryContext retryContext = new RetryContext();

        Mockito.when(this.resetSessionTokenRetryPolicyMock.getRequestPolicy(null)).thenReturn(dummyDocumentClientRetryPolicy());
        Mockito.when(this.cosmosAuthorizationTokenResolverMock.getAuthorizationToken(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any())).thenReturn("abcdefgh");
        Mockito.when(this.resetSessionTokenRetryPolicyMock.getRequestPolicy(Mockito.any())).thenReturn(documentClientRetryPolicyMock);
        Mockito.when(documentClientRetryPolicyMock.getRetryContext()).thenReturn(retryContext);
        Mockito.when(documentClientRetryPolicyMock.shouldRetry(Mockito.any(Exception.class)))
            .thenReturn(Mono.just(ShouldRetryResult.noRetry()));
        Mockito.when(gatewayServiceConfigurationReaderMock.getDefaultConsistencyLevel())
            .thenReturn(ConsistencyLevel.SESSION);
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
            ReflectionUtils.setGatewayServiceConfigurationReader(rxDocumentClient, gatewayServiceConfigurationReaderMock);
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

    @Test(groups = {"unit"})
    public void lookupCollectionRoutingMapWithRetryRetriesNullRoutingMap() {
        RxClientCollectionCache collectionCache = Mockito.mock(RxClientCollectionCache.class);
        RxPartitionKeyRangeCache partitionKeyRangeCache = Mockito.mock(RxPartitionKeyRangeCache.class);

        Map<String, PartitionKeyRange> epksPartitionKeyRangeMap = new HashMap<>();
        PartitionKeyRange partitionKeyRange = new PartitionKeyRange()
            .setId("0")
            .setMinInclusive("AA")
            .setMaxExclusive("FF");
        epksPartitionKeyRangeMap.put("AAA", partitionKeyRange);

        Mockito
            .when(partitionKeyRangeCache.tryLookupAsync(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(Mono.just(dummyNullCollectionRoutingMap()))
            .thenReturn(Mono.just(dummyCollectionRoutingMap(epksPartitionKeyRangeMap)));

        Mockito.when(this.connectionPolicyMock.getIdleHttpConnectionTimeout()).thenReturn(Duration.ZERO);
        Mockito.when(this.connectionPolicyMock.getMaxConnectionPoolSize()).thenReturn(1);
        Mockito.when(this.connectionPolicyMock.getProxy()).thenReturn(null);
        Mockito.when(this.connectionPolicyMock.getHttpNetworkRequestTimeout()).thenReturn(Duration.ZERO);
        Mockito.when(this.connectionPolicyMock.getHttp2ConnectionConfig()).thenReturn(new Http2ConnectionConfig());

        MockedStatic<HttpClient> httpClientMock = Mockito.mockStatic(HttpClient.class);
        httpClientMock
            .when(() -> HttpClient.createFixed(Mockito.any(HttpClientConfig.class)))
            .thenReturn(dummyHttpClient());

        RxDocumentClientImpl rxDocumentClient = null;

        try {
            rxDocumentClient = new RxDocumentClientImpl(
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

            ReflectionUtils.setCollectionCache(rxDocumentClient, collectionCache);
            ReflectionUtils.setPartitionKeyRangeCache(rxDocumentClient, partitionKeyRangeCache);

            DocumentCollection documentCollection = dummyCollectionObs().v;
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
                rxDocumentClient,
                OperationType.Query,
                ResourceType.Document,
                "dbs/db1/colls/coll1",
                (byte[]) null,
                new HashMap<>());
            MetadataDiagnosticsContext metadataDiagnosticsContext = new MetadataDiagnosticsContext();

            StepVerifier.create(rxDocumentClient.lookupCollectionRoutingMapWithRetry(
                    metadataDiagnosticsContext,
                    request,
                    documentCollection))
                .expectNextMatches(routingMapHolder -> routingMapHolder != null && routingMapHolder.v != null)
                .verifyComplete();

            Mockito.verify(partitionKeyRangeCache, Mockito.times(2))
                .tryLookupAsync(Mockito.same(metadataDiagnosticsContext), Mockito.eq(documentCollection.getResourceId()), Mockito.isNull(), Mockito.isNull());
            Mockito.verify(collectionCache, Mockito.atLeastOnce())
                .refresh(Mockito.same(metadataDiagnosticsContext), Mockito.eq(request.getResourceAddress()), Mockito.any());
        } finally {
            if (rxDocumentClient != null) {
                rxDocumentClient.close();
            }
            httpClientMock.close();
        }
    }

    @Test(groups = {"unit"})
    public void lookupCollectionRoutingMapWithRetryStopsAfterBoundedAttempts() {
        RxClientCollectionCache collectionCache = Mockito.mock(RxClientCollectionCache.class);
        RxPartitionKeyRangeCache partitionKeyRangeCache = Mockito.mock(RxPartitionKeyRangeCache.class);

        // Always return an empty (null) routing map so the lookup can never succeed. The underlying partition key
        // range read is already retried with backoff by InCompleteRoutingMapRetryPolicy; this test guards that the
        // outer lookup retry stays bounded (does not multiply into a large/compounding number of attempts) and that
        // exhaustion surfaces a CollectionRoutingMapNotFoundException (404 / INCORRECT_CONTAINER_RID).
        Mockito
            .when(partitionKeyRangeCache.tryLookupAsync(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(Mono.just(dummyNullCollectionRoutingMap()));

        Mockito.when(this.connectionPolicyMock.getIdleHttpConnectionTimeout()).thenReturn(Duration.ZERO);
        Mockito.when(this.connectionPolicyMock.getMaxConnectionPoolSize()).thenReturn(1);
        Mockito.when(this.connectionPolicyMock.getProxy()).thenReturn(null);
        Mockito.when(this.connectionPolicyMock.getHttpNetworkRequestTimeout()).thenReturn(Duration.ZERO);
        Mockito.when(this.connectionPolicyMock.getHttp2ConnectionConfig()).thenReturn(new Http2ConnectionConfig());

        MockedStatic<HttpClient> httpClientMock = Mockito.mockStatic(HttpClient.class);
        httpClientMock
            .when(() -> HttpClient.createFixed(Mockito.any(HttpClientConfig.class)))
            .thenReturn(dummyHttpClient());

        RxDocumentClientImpl rxDocumentClient = null;

        try {
            rxDocumentClient = new RxDocumentClientImpl(
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

            ReflectionUtils.setCollectionCache(rxDocumentClient, collectionCache);
            ReflectionUtils.setPartitionKeyRangeCache(rxDocumentClient, partitionKeyRangeCache);

            DocumentCollection documentCollection = dummyCollectionObs().v;
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
                rxDocumentClient,
                OperationType.Query,
                ResourceType.Document,
                "dbs/db1/colls/coll1",
                (byte[]) null,
                new HashMap<>());
            MetadataDiagnosticsContext metadataDiagnosticsContext = new MetadataDiagnosticsContext();

            StepVerifier.create(rxDocumentClient.lookupCollectionRoutingMapWithRetry(
                    metadataDiagnosticsContext,
                    request,
                    documentCollection))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(CollectionRoutingMapNotFoundException.class);
                    CollectionRoutingMapNotFoundException notFound = (CollectionRoutingMapNotFoundException) error;
                    assertThat(notFound.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
                    assertThat(notFound.getSubStatusCode())
                        .isEqualTo(HttpConstants.SubStatusCodes.INCORRECT_CONTAINER_RID_SUB_STATUS);
                })
                .verify();

            // One initial attempt plus exactly one bounded retry. If the outer retry budget ever regresses to a
            // large value (compounding with the inner InCompleteRoutingMapRetryPolicy backoff), this fails fast.
            Mockito.verify(partitionKeyRangeCache, Mockito.times(2))
                .tryLookupAsync(
                    Mockito.same(metadataDiagnosticsContext),
                    Mockito.eq(documentCollection.getResourceId()),
                    Mockito.isNull(),
                    Mockito.isNull());
        } finally {
            if (rxDocumentClient != null) {
                rxDocumentClient.close();
            }
            httpClientMock.close();
        }
    }

    // Regression test for the "partitionLevelCircuitBreakerCfg" diagnostics field silently disappearing from the
    // CosmosDiagnostics "clientCfgs" section. Prior to the fix, the field was only written on the PPAF
    // (service-mandated) path, so a client that did not have PPAF-mandated PPCB never surfaced it. The field must
    // now be present for every client regardless of any PPCB configuration. This test constructs a real
    // RxDocumentClientImpl (exercising the actual constructor wiring), drives the private
    // initializePerPartitionCircuitBreaker() init path without setting any PPCB configuration, serializes the
    // resulting DiagnosticsClientConfig, and asserts that every expected "clientCfgs" key - including
    // partitionLevelCircuitBreakerCfg - is present (guarding against future serialization truncation as well as
    // the specific regression).
    @Test(groups = {"unit"})
    public void diagnosticsClientConfigContainsAllClientCfgKeysIncludingPartitionLevelCircuitBreaker() throws Exception {
        Mockito.when(this.connectionPolicyMock.getIdleHttpConnectionTimeout()).thenReturn(Duration.ZERO);
        Mockito.when(this.connectionPolicyMock.getMaxConnectionPoolSize()).thenReturn(1);
        Mockito.when(this.connectionPolicyMock.getProxy()).thenReturn(null);
        Mockito.when(this.connectionPolicyMock.getHttpNetworkRequestTimeout()).thenReturn(Duration.ZERO);
        Mockito.when(this.connectionPolicyMock.getHttp2ConnectionConfig()).thenReturn(new Http2ConnectionConfig());
        // The serializer eagerly calls getConnectionMode().toString() for the very first "connectionMode" key; if this
        // returns null (Mockito default), serialization would NPE and silently drop every subsequent key.
        Mockito.when(this.connectionPolicyMock.getConnectionMode()).thenReturn(ConnectionMode.DIRECT);

        MockedStatic<HttpClient> httpClientMock = Mockito.mockStatic(HttpClient.class);
        httpClientMock
            .when(() -> HttpClient.createFixed(Mockito.any(HttpClientConfig.class)))
            .thenReturn(dummyHttpClient());

        RxDocumentClientImpl rxDocumentClient = null;

        try {
            rxDocumentClient = new RxDocumentClientImpl(
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

            // Drive the exact wiring that regressed: explicit (client-side) Per-Partition Circuit Breaker
            // initialization. The constructor does not invoke init() (which would require network), so invoke the
            // private no-arg initializer reflectively.
            Method initPpcb = RxDocumentClientImpl.class.getDeclaredMethod("initializePerPartitionCircuitBreaker");
            initPpcb.setAccessible(true);
            initPpcb.invoke(rxDocumentClient);

            ObjectMapper objectMapper = new ObjectMapper();
            StringWriter jsonWriter = new StringWriter();
            JsonGenerator jsonGenerator = new JsonFactory().createGenerator(jsonWriter);
            SerializerProvider serializerProvider = objectMapper.getSerializerProvider();
            DiagnosticsClientContext.DiagnosticsClientConfigSerializer.INSTANCE
                .serialize(rxDocumentClient.getConfig(), jsonGenerator, serializerProvider);
            jsonGenerator.flush();
            ObjectNode clientCfgs = (ObjectNode) objectMapper.readTree(jsonWriter.toString());

            String serializedJson = clientCfgs.toString();

            // Every key the serializer unconditionally writes, including the (previously regressed)
            // partitionLevelCircuitBreakerCfg which must be present for every client regardless of PPCB config.
            String[] expectedKeys = new String[] {
                "id",
                "machineId",
                "connectionMode",
                "numberOfClients",
                "isPpafEnabled",
                "isCrossRegionalHedgingDisabledByAccount",
                "isFalseProgSessionTokenMergeEnabled",
                "excrgns",
                "clientEndpoints",
                "connCfg",
                "consistencyCfg",
                "proactiveInitCfg",
                "e2ePolicyCfg",
                "sessionRetryCfg",
                "partitionLevelCircuitBreakerCfg"
            };

            for (String expectedKey : expectedKeys) {
                assertThat(clientCfgs.has(expectedKey))
                    .withFailMessage("Expected clientCfgs key '%s' to be present. Serialized clientCfgs: %s",
                        expectedKey, serializedJson)
                    .isTrue();
            }
        } finally {
            if (rxDocumentClient != null) {
                rxDocumentClient.close();
            }
            httpClientMock.close();
        }
    }

    @Test(groups = "unit")
    public void accountHedgingOverrideAppliesOnlyWithPpaf() throws Exception {
        RxDocumentClientImpl client = Mockito.mock(RxDocumentClientImpl.class, Mockito.CALLS_REAL_METHODS);
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        AtomicBoolean disabledByAccount = new AtomicBoolean();
        Mockito.when(endpointManager.getCrossRegionalHedgingDisabledByAccount()).thenReturn(disabledByAccount);
        GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover ppafManager =
            Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover.class);
        Field endpointManagerField = RxDocumentClientImpl.class.getDeclaredField("globalEndpointManager");
        endpointManagerField.setAccessible(true);
        endpointManagerField.set(client, endpointManager);
        Field ppafManagerField = RxDocumentClientImpl.class
            .getDeclaredField("globalPartitionEndpointManagerForPerPartitionAutomaticFailover");
        ppafManagerField.setAccessible(true);
        ppafManagerField.set(client, ppafManager);

        Method applicableRegions = RxDocumentClientImpl.class.getDeclaredMethod("getApplicableRegionsForSpeculation",
            CosmosEndToEndOperationLatencyPolicyConfig.class, ResourceType.class, OperationType.class,
            boolean.class, List.class);
        applicableRegions.setAccessible(true);
        CosmosEndToEndOperationLatencyPolicyConfig policy =
            new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(5))
                .availabilityStrategy(new ThresholdBasedAvailabilityStrategy())
                .build();

        DatabaseAccount[] snapshots = {
            null,
            new DatabaseAccount("{\"disableCrossRegionalHedging\":true}"),
            new DatabaseAccount("{\"disableCrossRegionalHedging\":false}"),
            new DatabaseAccount("{\"disableCrossRegionalHedging\":true}"),
            new DatabaseAccount("{}"),
            new DatabaseAccount("{\"disableCrossRegionalHedging\":null}")
        };

        for (boolean ppafEnabled : new boolean[] {true, false}) {
            Mockito.when(ppafManager.isPerPartitionAutomaticFailoverEnabled()).thenReturn(ppafEnabled);
            for (DatabaseAccount snapshot : snapshots) {
                disabledByAccount.set(snapshot != null && snapshot.isCrossRegionalHedgingDisabled());
                for (OperationType operation : new OperationType[] {OperationType.Read, OperationType.Query}) {
                    Mockito.clearInvocations(endpointManager);
                    applicableRegions.invoke(client, policy, ResourceType.Document, operation, false, null);
                    boolean disabled = ppafEnabled && snapshot != null
                        && Boolean.TRUE.equals(snapshot.getBoolean("disableCrossRegionalHedging"));
                    Mockito.verify(endpointManager, Mockito.times(disabled ? 0 : 1))
                        .getApplicableReadRegionalRoutingContexts((List<String>) null);
                }
            }
        }
    }

    @Test(groups = "unit")
    public void accountHedgingOverrideIsInitializedAndRefreshedWithoutResettingPpcb() throws Exception {
        AtomicReference<DatabaseAccount> account = new AtomicReference<>(hedgingAccount(true, true));
        try (MockedStatic<HttpClient> httpClientMock = Mockito.mockStatic(HttpClient.class)) {
            httpClientMock.when(() -> HttpClient.createFixed(Mockito.any(HttpClientConfig.class)))
                .thenReturn(dummyHttpClient());
            RxDocumentClientImpl client = createClientWithAccount(account);
            try {
                client.init(null, null);
                GlobalEndpointManager endpointManager = client.getGlobalEndpointManager();
                Object circuitBreakerConfig = client.getGlobalPartitionEndpointManagerForCircuitBreaker()
                    .getCircuitBreakerConfig();
                Method applicableRegions = RxDocumentClientImpl.class.getDeclaredMethod("getApplicableRegionsForSpeculation",
                    CosmosEndToEndOperationLatencyPolicyConfig.class, ResourceType.class, OperationType.class,
                    boolean.class, List.class);
                applicableRegions.setAccessible(true);
                CosmosEndToEndOperationLatencyPolicyConfig policy =
                    new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(5))
                        .availabilityStrategy(new ThresholdBasedAvailabilityStrategy())
                        .build();

                for (Boolean disabled : new Boolean[] {true, false, true, null, true}) {
                    if (disabled != Boolean.TRUE || account.get().isCrossRegionalHedgingDisabled() != disabled) {
                        account.set(hedgingAccount(true, disabled));
                        endpointManager.refreshLocationAsync(null, true).block(Duration.ofSeconds(5));
                    }
                    ObjectNode clientCfg = serializeClientConfig(client);
                    assertThat(clientCfg.get("isPpafEnabled").asBoolean()).isTrue();
                    assertThat(clientCfg.get("isCrossRegionalHedgingDisabledByAccount").asBoolean())
                        .isEqualTo(Boolean.TRUE.equals(disabled));
                    assertThat(clientCfg.get("partitionLevelCircuitBreakerCfg").asText()).isNotEmpty();
                    assertThat(client.getGlobalPartitionEndpointManagerForCircuitBreaker().getCircuitBreakerConfig())
                        .isSameAs(circuitBreakerConfig);
                    List<?> regions = (List<?>) applicableRegions.invoke(client, policy, ResourceType.Document,
                        OperationType.Read, false, Collections.emptyList());
                    assertThat(regions).hasSize(Boolean.TRUE.equals(disabled) ? 0 : 2);
                }

                account.set(hedgingAccount(false, true));
                endpointManager.refreshLocationAsync(null, true).block(Duration.ofSeconds(5));
                assertThat(serializeClientConfig(client).get("isCrossRegionalHedgingDisabledByAccount").asBoolean()).isFalse();
                assertThat((List<?>) applicableRegions.invoke(client, policy, ResourceType.Document,
                    OperationType.Read, false, Collections.emptyList())).hasSize(2);
            } finally {
                client.close();
            }
        }
    }

    @DataProvider(name = "accountHedgingDisabled")
    public Object[][] accountHedgingDisabled() {
        return new Object[][] {{false}, {true}};
    }

    @Test(groups = "unit", dataProvider = "accountHedgingDisabled")
    public void accountDisabledHedgingWaitsForPpcbFailover(boolean disabled) throws Exception {
        AtomicReference<DatabaseAccount> account = new AtomicReference<>(hedgingAccount(true, disabled));
        try (MockedStatic<HttpClient> httpClientMock = Mockito.mockStatic(HttpClient.class)) {
            httpClientMock.when(() -> HttpClient.createFixed(Mockito.any(HttpClientConfig.class)))
                .thenReturn(dummyHttpClient());
            RxDocumentClientImpl client = createClientWithAccount(account);
            try {
                client.init(null, null);
                GlobalEndpointManager endpointManager = client.getGlobalEndpointManager();
                GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker ppcb =
                    client.getGlobalPartitionEndpointManagerForCircuitBreaker();
                PartitionKeyRange partition = new PartitionKeyRange("0", "", "FF");
                String collectionId = "collectionRid";
                AtomicReference<RxDocumentServiceRequest> lastRequest = new AtomicReference<>();
                AtomicInteger reportedFailures = new AtomicInteger();
                List<String> contactedRegions = new ArrayList<>();
                ResourceResponse<Document> success = Mockito.mock(ResourceResponse.class);
                Class<?> callbackType = ReflectionUtils.getClassBySimpleName(
                    RxDocumentClientImpl.class.getDeclaredClasses(), "DocumentPointOperation");
                Object callback = java.lang.reflect.Proxy.newProxyInstance(callbackType.getClassLoader(),
                    new Class<?>[] {callbackType}, (proxy, method, arguments) -> Mono.defer(() -> {
                        RequestOptions options = (RequestOptions) arguments[0];
                        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
                            client, OperationType.Read, ResourceType.Document);
                        request.setResourceId(collectionId);
                        request.requestContext.resolvedPartitionKeyRange = partition;
                        request.requestContext.resolvedPartitionKeyRangeForCircuitBreaker = partition;
                        request.requestContext.setExcludeRegions(options.getExcludedRegions());
                        request.requestContext.setCrossRegionAvailabilityContext(
                            (CrossRegionAvailabilityContextForRxDocumentServiceRequest) arguments[3]);
                        lastRequest.set(request);

                        List<String> unavailable = ppcb.getUnavailableRegionsForPartitionKeyRange(
                            request, collectionId, partition);
                        RegionalRoutingContext target = endpointManager
                            .getApplicableReadRegionalRoutingContexts(options.getExcludedRegions()).stream()
                            .filter(region -> !unavailable.contains(endpointManager.getRegionName(
                                region.getGatewayRegionalEndpoint(), OperationType.Read)))
                            .findFirst().get();
                        String regionName = endpointManager.getRegionName(target.getGatewayRegionalEndpoint(), OperationType.Read);
                        contactedRegions.add(regionName);
                        request.requestContext.regionalRoutingContextToRoute = target;

                        if (regionName.equals("east us")) {
                            return Mono.delay(Duration.ofSeconds(2))
                                .then(Mono.<ResourceResponse<Document>>error(
                                    BridgeInternal.createCosmosException(408, "Primary region timeout")))
                                .doOnError(error -> {
                                    reportedFailures.incrementAndGet();
                                    ppcb.handleLocationExceptionForPartitionKeyRange(request, target, false);
                                });
                        }
                        return Mono.just(success);
                    }));
                Method wrap = RxDocumentClientImpl.class.getDeclaredMethod("wrapPointOperationWithAvailabilityStrategy",
                    ResourceType.class, OperationType.class, callbackType, RequestOptions.class, boolean.class,
                    DiagnosticsClientContext.class, String.class);
                wrap.setAccessible(true);
                Supplier<Mono<ResourceResponse<Document>>> operation = () -> {
                    try {
                        return (Mono<ResourceResponse<Document>>) wrap.invoke(client, ResourceType.Document,
                            OperationType.Read, callback, new RequestOptions(), false, client, collectionId);
                    } catch (ReflectiveOperationException error) {
                        throw new IllegalStateException(error);
                    }
                };

                if (disabled) {
                    do {
                        StepVerifier.withVirtualTime(operation)
                            .thenAwait(Duration.ofSeconds(2))
                            .expectErrorMatches(error -> error instanceof CosmosException
                                && ((CosmosException) error).getStatusCode() == 408)
                            .verify(Duration.ofSeconds(5));
                        assertThat(contactedRegions).containsOnly("east us");
                        assertThat(reportedFailures.get()).isLessThan(100);
                    } while (ppcb.getUnavailableRegionsForPartitionKeyRange(lastRequest.get(), collectionId, partition).isEmpty());
                }

                StepVerifier.withVirtualTime(operation)
                    .thenAwait(Duration.ofSeconds(2))
                    .expectNext(success)
                    .expectComplete()
                    .verify(Duration.ofSeconds(5));
                assertThat(contactedRegions.get(contactedRegions.size() - 1)).isEqualTo("west us");
                if (disabled) {
                    assertThat(reportedFailures.get()).isPositive();
                    assertThat(ppcb.getUnavailableRegionsForPartitionKeyRange(lastRequest.get(), collectionId, partition))
                        .containsExactly("east us");
                } else {
                    assertThat(reportedFailures.get()).isZero();
                    assertThat(ppcb.getUnavailableRegionsForPartitionKeyRange(lastRequest.get(), collectionId, partition)).isEmpty();
                }
                ObjectNode clientCfg = serializeClientConfig(client);
                assertThat(clientCfg.get("isCrossRegionalHedgingDisabledByAccount").asBoolean()).isEqualTo(disabled);
                assertThat(clientCfg.get("partitionLevelCircuitBreakerCfg").asText()).isNotEmpty();
            } finally {
                client.close();
            }
        }
    }

    @Test(groups = "unit", dataProvider = "accountHedgingDisabled")
    public void accountHedgingOverrideControlsFeedSpeculation(boolean disabled) throws Exception {
        AtomicReference<DatabaseAccount> account = new AtomicReference<>(hedgingAccount(true, disabled));
        try (MockedStatic<HttpClient> httpClientMock = Mockito.mockStatic(HttpClient.class)) {
            httpClientMock.when(() -> HttpClient.createFixed(Mockito.any(HttpClientConfig.class)))
                .thenReturn(dummyHttpClient());
            RxDocumentClientImpl client = createClientWithAccount(account);
            try {
                client.init(null, null);
                Method execute = RxDocumentClientImpl.class.getDeclaredMethod("executeFeedOperationWithAvailabilityStrategy",
                    ResourceType.class, OperationType.class, Supplier.class, RxDocumentServiceRequest.class,
                    BiFunction.class, String.class);
                execute.setAccessible(true);
                for (OperationType operationType : new OperationType[] {OperationType.Query, OperationType.ReadFeed}) {
                    List<String> contactedRegions = new ArrayList<>();
                    BiFunction<Supplier<DocumentClientRetryPolicy>, RxDocumentServiceRequest, Mono<String>> transport =
                        (retryPolicy, request) -> Mono.defer(() -> {
                            GlobalEndpointManager endpointManager = client.getGlobalEndpointManager();
                            RegionalRoutingContext target = endpointManager.getApplicableReadRegionalRoutingContexts(
                                request.requestContext.getExcludeRegions()).get(0);
                            String region = endpointManager.getRegionName(target.getGatewayRegionalEndpoint(), operationType);
                            request.requestContext.regionalRoutingContextToRoute = target;
                            request.requestContext.resolvedPartitionKeyRange = new PartitionKeyRange("0", "", "FF");
                            request.requestContext.resolvedPartitionKeyRangeForCircuitBreaker = request.requestContext.resolvedPartitionKeyRange;
                            contactedRegions.add(region);
                            return region.equals("east us") ? Mono.never() : Mono.just(region);
                        });
                    Supplier<Mono<String>> operation = () -> {
                        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(client, operationType, ResourceType.Document);
                        request.setResourceId("collectionRid");
                        request.requestContext.setExcludeRegions(Collections.emptyList());
                        try {
                            return (Mono<String>) execute.invoke(client, ResourceType.Document, operationType,
                                (Supplier<DocumentClientRetryPolicy>) () -> Mockito.mock(DocumentClientRetryPolicy.class),
                                request, transport, "collectionRid");
                        } catch (ReflectiveOperationException error) {
                            throw new IllegalStateException(error);
                        }
                    };
                    if (disabled) {
                        StepVerifier.withVirtualTime(operation).expectSubscription()
                            .expectNoEvent(Duration.ofSeconds(2)).thenCancel().verify(Duration.ofSeconds(5));
                        assertThat(contactedRegions).containsExactly("east us");
                    } else {
                        StepVerifier.withVirtualTime(operation).thenAwait(Duration.ofSeconds(2))
                            .expectNext("west us").expectComplete().verify(Duration.ofSeconds(5));
                        assertThat(contactedRegions).containsExactly("east us", "west us");
                    }
                }
            } finally {
                client.close();
            }
        }
    }

    private RxDocumentClientImpl createClientWithAccount(AtomicReference<DatabaseAccount> account) {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy(GatewayConnectionConfig.getDefaultConfig());
        connectionPolicy.setPreferredRegions(Arrays.asList("East US", "West US"));
        return new RxDocumentClientImpl(
            URI.create("https://testaccount.documents.azure.com"), "ZmFrZQ==", null, connectionPolicy,
            ConsistencyLevel.SESSION, null, new Configs(), null, null, false, false, false,
            null, ApiType.SQL, new CosmosClientTelemetryConfig(), null, null, null, null,
            this.defaultItemSerializer, false) {
            @Override
            public Flux<DatabaseAccount> getDatabaseAccountFromEndpoint(URI endpoint) {
                return Flux.defer(() -> Flux.just(account.get()));
            }
        };
    }

    private static DatabaseAccount hedgingAccount(boolean ppafEnabled, Boolean disabled) {
        DatabaseAccount account = new DatabaseAccount("{\"id\":\"testaccount\","
            + "\"writableLocations\":[{\"name\":\"East US\","
            + "\"databaseAccountEndpoint\":\"https://testaccount-eastus.documents.azure.com\"}],"
            + "\"readableLocations\":[{\"name\":\"East US\","
            + "\"databaseAccountEndpoint\":\"https://testaccount-eastus.documents.azure.com\"},"
            + "{\"name\":\"West US\",\"databaseAccountEndpoint\":\"https://testaccount-westus.documents.azure.com\"}],"
            + "\"userConsistencyPolicy\":{\"defaultConsistencyLevel\":\"Session\"}}");
        account.set(Constants.Properties.ENABLE_PER_PARTITION_FAILOVER_BEHAVIOR, ppafEnabled);
        if (disabled != null) {
            account.set(Constants.Properties.DISABLE_CROSS_REGIONAL_HEDGING, disabled);
        }
        return account;
    }

    private static ObjectNode serializeClientConfig(RxDocumentClientImpl client) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return (ObjectNode) objectMapper.readTree(objectMapper.writeValueAsString(client.getConfig()));
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
        collectionObs.v.setResourceId("collectionRid");
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
            public CollectionRoutingMap tryCombine(
                List<ImmutablePair<PartitionKeyRange, IServerIdentity>> ranges,
                String changeFeedIfNoneMatch,
                String collectionRid) {
                return null;
            }

            @Override
            public String getChangeFeedNextIfNoneMatch() {
                return null;
            }
        };
        return routingMap;
    }

    private static Utils.ValueHolder<CollectionRoutingMap> dummyNullCollectionRoutingMap() {
        return new Utils.ValueHolder<>();
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
                return Mono.just(ShouldRetryResult.noRetry());
            }

            @Override
            public RetryContext getRetryContext() {
                return null;
            }
        };
    }

    private static RxDocumentServiceResponse mockRxDocumentServiceResponse(String content, Map<String, String> headers) {
        byte[] blob = content.getBytes(StandardCharsets.UTF_8);
        StoreResponse storeResponse = new StoreResponse(
            null,
            HttpResponseStatus.OK.code(),
            headers,
            new ByteBufInputStream(Unpooled.wrappedBuffer(blob), true),
            blob.length);

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
