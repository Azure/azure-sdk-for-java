package com.azure.cosmos.implementation;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.ProxyOptions;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
import com.azure.cosmos.implementation.caches.RxClientCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
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
import com.azure.cosmos.models.*;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

public class RxDocumentClientImplTest {

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

    @BeforeTest
    public void setUp() {
        // create mocks
        this.serviceEndpointMock = Mockito.mock(URI.class);
        this.masterKeyOrResourceTokenMock = "";
        this.permissionFeedMock = new ArrayList<>();
        this.connectionPolicyMock = Mockito.mock(ConnectionPolicy.class);
        this.consistencyLevelMock = Mockito.mock(ConsistencyLevel.class);
        this.configsMock = Mockito.mock(Configs.class);
        this.cosmosAuthorizationTokenResolverMock = Mockito.mock(CosmosAuthorizationTokenResolver.class);
        this.azureKeyCredentialMock = Mockito.mock(AzureKeyCredential.class);
        this.metadataCachesSnapshotMock = Mockito.mock(CosmosClientMetadataCachesSnapshot.class);
        this.apiTypeMock = Mockito.mock(ApiType.class);
        this.cosmosClientTelemetryConfigMock = Mockito.mock(CosmosClientTelemetryConfig.class);
        this.clientCorrelationIdMock = "";
        this.collectionCacheMock = Mockito.mock(RxClientCollectionCache.class);
        this.partitionKeyRangeCacheMock = Mockito.mock(RxPartitionKeyRangeCache.class);
        this.resetSessionTokenRetryPolicyMock = Mockito.mock(IRetryPolicyFactory.class);
    }

    @Test(groups = "unit")
    public void readMany() {

        // setup static method mocks
        MockedStatic<HttpClient> httpClientMock = Mockito.mockStatic(HttpClient.class);
        MockedStatic<PartitionKeyInternalHelper> partitionKeyInternalHelperMock = Mockito.mockStatic(PartitionKeyInternalHelper.class);
        MockedStatic<DocumentQueryExecutionContextFactory> documentQueryExecutionFactoryMock = Mockito.mockStatic(DocumentQueryExecutionContextFactory.class);
        MockedStatic<ObservableHelper> observableHelperMock = Mockito.mockStatic(ObservableHelper.class);

        PartitionKeyRange partitionKeyRange1 = new PartitionKeyRange()
            .setId(UUID.randomUUID().toString())
            .setMinInclusive("AA")
            .setMaxExclusive("FF");

        PartitionKeyRange partitionKeyRange2 = new PartitionKeyRange()
            .setId(UUID.randomUUID().toString())
            .setMinInclusive("BB")
            .setMaxExclusive("CCC");

        // dummy partition key ranges
        List<PartitionKeyRange> partitionKeyRanges = Arrays.asList(partitionKeyRange1, partitionKeyRange2);


        // set up mock behavior
        Mockito.when(this.connectionPolicyMock.getIdleHttpConnectionTimeout()).thenReturn(dummyDuration());
        Mockito.when(this.connectionPolicyMock.getMaxConnectionPoolSize()).thenReturn(dummyInt());
        Mockito.when(this.connectionPolicyMock.getProxy()).thenReturn(dummyProxyClass());
        Mockito.when(this.connectionPolicyMock.getHttpNetworkRequestTimeout()).thenReturn(dummyDuration());

        httpClientMock
            .when(() -> HttpClient.createFixed(Mockito.any(HttpClientConfig.class)))
            .thenReturn(dummyHttpClient());
        partitionKeyInternalHelperMock
            .when(() -> PartitionKeyInternalHelper.getEffectivePartitionKeyString(Mockito.any(), Mockito.any()))
            .thenReturn("AAA", "BBB", "CCC");
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
                Mockito.any()
            ))
            .thenReturn(Flux.just(dummyExecutionContextForQuery()));
        observableHelperMock
            .when(() -> ObservableHelper.inlineIfPossibleAsObs(Mockito.any(), Mockito.any()))
            .thenReturn(Mono.just(dummyResourceResponse()));

        Mockito
            .when(this.collectionCacheMock.resolveCollectionAsync(Mockito.isNull(), Mockito.any(RxDocumentServiceRequest.class)))
            .thenReturn(Mono.just(dummyCollectionObs()));
        Mockito
            .when(this.partitionKeyRangeCacheMock.tryLookupAsync(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(Mono.just(dummyCollectionRoutingMap(partitionKeyRanges)));

        Mockito.when(this.resetSessionTokenRetryPolicyMock.getRequestPolicy()).thenReturn(dummyDocumentClientRetryPolicy());


        // initialize object to be tested
        RxDocumentClientImpl rxDocumentClient = new RxDocumentClientImpl(
            this.serviceEndpointMock,
            this.masterKeyOrResourceTokenMock,
            this.permissionFeedMock,
            this.connectionPolicyMock,
            this.consistencyLevelMock,
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
            null
        );

        ReflectionTestUtils.setField(rxDocumentClient, "collectionCache", this.collectionCacheMock);
        ReflectionTestUtils.setField(rxDocumentClient, "partitionKeyRangeCache", this.partitionKeyRangeCacheMock);
        ReflectionTestUtils.setField(rxDocumentClient, "resetSessionTokenRetryPolicy", this.resetSessionTokenRetryPolicyMock);

        ArrayList<CosmosItemIdentity> cosmosItemIdentities = new ArrayList<CosmosItemIdentity>();

        cosmosItemIdentities.add(new CosmosItemIdentity(new PartitionKey("1"), "1"));
        cosmosItemIdentities.add(new CosmosItemIdentity(new PartitionKey("2"), "2"));
        cosmosItemIdentities.add(new CosmosItemIdentity(new PartitionKey("3"), "3"));

        String collectionLink = "";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        Class<InternalObjectNode> klass = InternalObjectNode.class;

        StepVerifier.create(
                rxDocumentClient.readMany(
                    cosmosItemIdentities,
                    collectionLink,
                    options,
                    klass
                )
            ).expectNextCount(1)
            .expectComplete()
            .verify();
    }

    private static Duration dummyDuration() {
        return Duration.ZERO;
    }

    private static int dummyInt() {
        return Integer.MAX_VALUE;
    }

    private static ProxyOptions dummyProxyClass() {
        return new ProxyOptions(null, null);
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
        partitionKeyDefinition.setPaths(List.of("/id"));
        Utils.ValueHolder<DocumentCollection> collectionObs = new Utils.ValueHolder<DocumentCollection>();
        collectionObs.v = new DocumentCollection();
        collectionObs.v.setPartitionKey(partitionKeyDefinition);

        return collectionObs;
    }

    private static Utils.ValueHolder<CollectionRoutingMap> dummyCollectionRoutingMap(List<PartitionKeyRange> partitionKeyRanges) {
        Utils.ValueHolder<CollectionRoutingMap> routingMap = new Utils.ValueHolder<CollectionRoutingMap>();
        routingMap.v = new CollectionRoutingMap() {
            @Override
            public List<PartitionKeyRange> getOrderedPartitionKeyRanges() {
                return null;
            }

            @Override
            public PartitionKeyRange getRangeByEffectivePartitionKey(String effectivePartitionKeyValue) {
                if ("AAA".equals(effectivePartitionKeyValue)) return partitionKeyRanges.get(0);
                return partitionKeyRanges.get(1);
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

    private static <T> IDocumentQueryExecutionContext<T> dummyExecutionContextForQuery() {
        return () -> Flux.just(ModelBridgeInternal.createFeedResponse(new ArrayList<>(), new HashMap<>()));
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

    private static ResourceResponse<Document> dummyResourceResponse() {
        String content = "{\"id\": \"1\"}";

        Document document = new Document(content);

        String activityId = UUID.randomUUID().toString();
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.HttpHeaders.ACTIVITY_ID, activityId);
        headers.put(HttpConstants.HttpHeaders.REQUEST_CHARGE, "4.5");


        StoreResponse storeResponse = new StoreResponse(
            HttpResponseStatus.OK.code(),
            headers,
            content.getBytes(StandardCharsets.UTF_8));

        RxDocumentServiceResponse documentServiceResponse = new RxDocumentServiceResponse(new DiagnosticsClientContext() {

            @Override
            public DiagnosticsClientConfig getConfig() {
                return null;
            }

            @Override
            public CosmosDiagnostics createDiagnostics() {
                return BridgeInternal.createCosmosDiagnostics(this) ;
            }
        }, storeResponse);

        documentServiceResponse.setCosmosDiagnostics(dummyCosmosDiagnostics());

        return new ResourceResponse<>(documentServiceResponse, Document.class);
    }

    private static CosmosDiagnostics dummyCosmosDiagnostics() {
        return BridgeInternal.createCosmosDiagnostics(new DiagnosticsClientContext() {
            @Override
            public DiagnosticsClientConfig getConfig() {
                return new DiagnosticsClientConfig();
            }

            @Override
            public CosmosDiagnostics createDiagnostics() {
                return null;
            }
        });
    }
}
