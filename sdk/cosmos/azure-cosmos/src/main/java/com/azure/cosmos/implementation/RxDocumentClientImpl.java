// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.SimpleTokenCache;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.batch.BatchResponseParser;
import com.azure.cosmos.implementation.batch.PartitionKeyRangeServerBatchRequest;
import com.azure.cosmos.implementation.batch.ServerBatchRequest;
import com.azure.cosmos.implementation.batch.SinglePartitionKeyServerBatchRequest;
import com.azure.cosmos.implementation.caches.RxClientCollectionCache;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.clienttelemetry.ClientTelemetry;
import com.azure.cosmos.implementation.cpu.CpuMemoryListener;
import com.azure.cosmos.implementation.cpu.CpuMemoryMonitor;
import com.azure.cosmos.implementation.directconnectivity.GatewayServiceConfigurationReader;
import com.azure.cosmos.implementation.directconnectivity.GlobalAddressResolver;
import com.azure.cosmos.implementation.directconnectivity.ServerStoreModel;
import com.azure.cosmos.implementation.directconnectivity.StoreClient;
import com.azure.cosmos.implementation.directconnectivity.StoreClientFactory;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpClientConfig;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.SharedGatewayHttpClient;
import com.azure.cosmos.implementation.patch.PatchUtil;
import com.azure.cosmos.implementation.query.DocumentQueryExecutionContextFactory;
import com.azure.cosmos.implementation.query.IDocumentQueryClient;
import com.azure.cosmos.implementation.query.IDocumentQueryExecutionContext;
import com.azure.cosmos.implementation.query.Paginator;
import com.azure.cosmos.implementation.query.PartitionedQueryExecutionInfo;
import com.azure.cosmos.implementation.query.PipelinedDocumentQueryExecutionContext;
import com.azure.cosmos.implementation.query.QueryInfo;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.PartitionKeyAndResourceTokenPair;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.implementation.spark.OperationContext;
import com.azure.cosmos.implementation.spark.OperationListener;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.implementation.throughputControl.ThroughputControlStore;
import com.azure.cosmos.implementation.throughputControl.config.ThroughputControlGroupInternal;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.cosmos.BridgeInternal.documentFromObject;
import static com.azure.cosmos.BridgeInternal.getAltLink;
import static com.azure.cosmos.BridgeInternal.toFeedResponsePage;
import static com.azure.cosmos.BridgeInternal.toResourceResponse;
import static com.azure.cosmos.BridgeInternal.toStoredProcedureResponse;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static com.azure.cosmos.models.ModelBridgeInternal.serializeJsonToByteBuffer;

/**
 * While this class is public, it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class RxDocumentClientImpl implements AsyncDocumentClient, IAuthorizationTokenProvider, CpuMemoryListener,
    DiagnosticsClientContext {
    private static final AtomicInteger activeClientsCnt = new AtomicInteger(0);
    private static final AtomicInteger clientIdGenerator = new AtomicInteger(0);
    private static final Range<String> RANGE_INCLUDING_ALL_PARTITION_KEY_RANGES = new Range<>(
        PartitionKeyInternalHelper.MinimumInclusiveEffectivePartitionKey,
        PartitionKeyInternalHelper.MaximumExclusiveEffectivePartitionKey, true, false);

    private static final String DUMMY_SQL_QUERY = "this is dummy and only used in creating " +
        "ParallelDocumentQueryExecutioncontext, but not used";
    private final static ObjectMapper mapper = Utils.getSimpleObjectMapper();
    private final ItemDeserializer itemDeserializer = new ItemDeserializer.JsonDeserializer();
    private final Logger logger = LoggerFactory.getLogger(RxDocumentClientImpl.class);
    private final String masterKeyOrResourceToken;
    private final URI serviceEndpoint;
    private final ConnectionPolicy connectionPolicy;
    private final ConsistencyLevel consistencyLevel;
    private final BaseAuthorizationTokenProvider authorizationTokenProvider;
    private final UserAgentContainer userAgentContainer;
    private final boolean hasAuthKeyResourceToken;
    private final Configs configs;
    private final boolean connectionSharingAcrossClientsEnabled;
    private AzureKeyCredential credential;
    private final TokenCredential tokenCredential;
    private String[] tokenCredentialScopes;
    private SimpleTokenCache tokenCredentialCache;
    private CosmosAuthorizationTokenResolver cosmosAuthorizationTokenResolver;
    AuthorizationTokenType authorizationTokenType;
    private SessionContainer sessionContainer;
    private String firstResourceTokenFromPermissionFeed = StringUtils.EMPTY;
    private RxClientCollectionCache collectionCache;
    private RxStoreModel gatewayProxy;
    private RxStoreModel storeModel;
    private GlobalAddressResolver addressResolver;
    private RxPartitionKeyRangeCache partitionKeyRangeCache;
    private Map<String, List<PartitionKeyAndResourceTokenPair>> resourceTokensMap;
    private final boolean contentResponseOnWriteEnabled;
    private ConcurrentMap<String, PartitionedQueryExecutionInfo> queryPlanCache;

    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final int clientId;
    private ClientTelemetry clientTelemetry;

    // RetryPolicy retries a request when it encounters session unavailable (see ClientRetryPolicy).
    // Once it exhausts all write regions it clears the session container, then it uses RxClientCollectionCache
    // to resolves the request's collection name. If it differs from the session container's resource id it
    // explains the session unavailable exception: somebody removed and recreated the collection. In this
    // case we retry once again (with empty session token) otherwise we return the error to the client
    // (see RenameCollectionAwareClientRetryPolicy)
    private IRetryPolicyFactory resetSessionTokenRetryPolicy;
    /**
     * Compatibility mode: Allows to specify compatibility mode used by client when
     * making query requests. Should be removed when application/sql is no longer
     * supported.
     */
    private final QueryCompatibilityMode queryCompatibilityMode = QueryCompatibilityMode.Default;
    private final GlobalEndpointManager globalEndpointManager;
    private final RetryPolicy retryPolicy;
    private HttpClient reactorHttpClient;
    private Function<HttpClient, HttpClient> httpClientInterceptor;
    private volatile boolean useMultipleWriteLocations;

    // creator of TransportClient is responsible for disposing it.
    private StoreClientFactory storeClientFactory;

    private GatewayServiceConfigurationReader gatewayConfigurationReader;
    private final DiagnosticsClientConfig diagnosticsClientConfig;

    private final AtomicBoolean throughputControlEnabled;
    private ThroughputControlStore throughputControlStore;

    public RxDocumentClientImpl(URI serviceEndpoint,
                                String masterKeyOrResourceToken,
                                List<Permission> permissionFeed,
                                ConnectionPolicy connectionPolicy,
                                ConsistencyLevel consistencyLevel,
                                Configs configs,
                                CosmosAuthorizationTokenResolver cosmosAuthorizationTokenResolver,
                                AzureKeyCredential credential,
                                boolean sessionCapturingOverride,
                                boolean connectionSharingAcrossClientsEnabled,
                                boolean contentResponseOnWriteEnabled,
                                CosmosClientMetadataCachesSnapshot metadataCachesSnapshot) {
        this(serviceEndpoint, masterKeyOrResourceToken, permissionFeed, connectionPolicy, consistencyLevel, configs,
            credential, null, sessionCapturingOverride, connectionSharingAcrossClientsEnabled, contentResponseOnWriteEnabled, metadataCachesSnapshot);
        this.cosmosAuthorizationTokenResolver = cosmosAuthorizationTokenResolver;
    }

    public RxDocumentClientImpl(URI serviceEndpoint,
                                String masterKeyOrResourceToken,
                                List<Permission> permissionFeed,
                                ConnectionPolicy connectionPolicy,
                                ConsistencyLevel consistencyLevel,
                                Configs configs,
                                CosmosAuthorizationTokenResolver cosmosAuthorizationTokenResolver,
                                AzureKeyCredential credential,
                                TokenCredential tokenCredential,
                                boolean sessionCapturingOverride,
                                boolean connectionSharingAcrossClientsEnabled,
                                boolean contentResponseOnWriteEnabled,
                                CosmosClientMetadataCachesSnapshot metadataCachesSnapshot) {
        this(serviceEndpoint, masterKeyOrResourceToken, permissionFeed, connectionPolicy, consistencyLevel, configs,
            credential, tokenCredential, sessionCapturingOverride, connectionSharingAcrossClientsEnabled, contentResponseOnWriteEnabled, metadataCachesSnapshot);
        this.cosmosAuthorizationTokenResolver = cosmosAuthorizationTokenResolver;
    }

    private RxDocumentClientImpl(URI serviceEndpoint,
                                String masterKeyOrResourceToken,
                                List<Permission> permissionFeed,
                                ConnectionPolicy connectionPolicy,
                                ConsistencyLevel consistencyLevel,
                                Configs configs,
                                AzureKeyCredential credential,
                                TokenCredential tokenCredential,
                                boolean sessionCapturingOverrideEnabled,
                                boolean connectionSharingAcrossClientsEnabled,
                                boolean contentResponseOnWriteEnabled,
                                CosmosClientMetadataCachesSnapshot metadataCachesSnapshot) {
        this(serviceEndpoint, masterKeyOrResourceToken, connectionPolicy, consistencyLevel, configs,
            credential, tokenCredential, sessionCapturingOverrideEnabled, connectionSharingAcrossClientsEnabled, contentResponseOnWriteEnabled, metadataCachesSnapshot);

        if (permissionFeed != null && permissionFeed.size() > 0) {
            this.resourceTokensMap = new HashMap<>();
            for (Permission permission : permissionFeed) {
                String[] segments = StringUtils.split(permission.getResourceLink(),
                        Constants.Properties.PATH_SEPARATOR.charAt(0));

                if (segments.length <= 0) {
                    throw new IllegalArgumentException("resourceLink");
                }

                List<PartitionKeyAndResourceTokenPair> partitionKeyAndResourceTokenPairs = null;
                PathInfo pathInfo = new PathInfo(false, StringUtils.EMPTY, StringUtils.EMPTY, false);
                if (!PathsHelper.tryParsePathSegments(permission.getResourceLink(), pathInfo, null)) {
                    throw new IllegalArgumentException(permission.getResourceLink());
                }

                partitionKeyAndResourceTokenPairs = resourceTokensMap.get(pathInfo.resourceIdOrFullName);
                if (partitionKeyAndResourceTokenPairs == null) {
                    partitionKeyAndResourceTokenPairs = new ArrayList<>();
                    this.resourceTokensMap.put(pathInfo.resourceIdOrFullName, partitionKeyAndResourceTokenPairs);
                }

                PartitionKey partitionKey = permission.getResourcePartitionKey();
                partitionKeyAndResourceTokenPairs.add(new PartitionKeyAndResourceTokenPair(
                        partitionKey != null ? BridgeInternal.getPartitionKeyInternal(partitionKey) : PartitionKeyInternal.Empty,
                        permission.getToken()));
                logger.debug("Initializing resource token map  , with map key [{}] , partition key [{}] and resource token [{}]",
                        pathInfo.resourceIdOrFullName, partitionKey != null ? partitionKey.toString() : null, permission.getToken());

            }

            if(this.resourceTokensMap.isEmpty()) {
                throw new IllegalArgumentException("permissionFeed");
            }

            String firstToken = permissionFeed.get(0).getToken();
            if(ResourceTokenAuthorizationHelper.isResourceToken(firstToken)) {
                this.firstResourceTokenFromPermissionFeed = firstToken;
            }
        }
    }

    RxDocumentClientImpl(URI serviceEndpoint,
                         String masterKeyOrResourceToken,
                         ConnectionPolicy connectionPolicy,
                         ConsistencyLevel consistencyLevel,
                         Configs configs,
                         AzureKeyCredential credential,
                         TokenCredential tokenCredential,
                         boolean sessionCapturingOverrideEnabled,
                         boolean connectionSharingAcrossClientsEnabled,
                         boolean contentResponseOnWriteEnabled,
                         CosmosClientMetadataCachesSnapshot metadataCachesSnapshot) {

        activeClientsCnt.incrementAndGet();
        this.clientId = clientIdGenerator.getAndDecrement();
        this.diagnosticsClientConfig = new DiagnosticsClientConfig();
        this.diagnosticsClientConfig.withClientId(this.clientId);
        this.diagnosticsClientConfig.withActiveClientCounter(activeClientsCnt);

        this.diagnosticsClientConfig.withConnectionSharingAcrossClientsEnabled(connectionSharingAcrossClientsEnabled);
        this.diagnosticsClientConfig.withConsistency(consistencyLevel);
        this.throughputControlEnabled = new AtomicBoolean(false);

        logger.info(
            "Initializing DocumentClient [{}] with"
                + " serviceEndpoint [{}], connectionPolicy [{}], consistencyLevel [{}], directModeProtocol [{}]",
            this.clientId, serviceEndpoint, connectionPolicy, consistencyLevel, configs.getProtocol());

        try {
            this.connectionSharingAcrossClientsEnabled = connectionSharingAcrossClientsEnabled;
            this.configs = configs;
            this.masterKeyOrResourceToken = masterKeyOrResourceToken;
            this.serviceEndpoint = serviceEndpoint;
            this.credential = credential;
            this.tokenCredential = tokenCredential;
            this.contentResponseOnWriteEnabled = contentResponseOnWriteEnabled;
            this.authorizationTokenType = AuthorizationTokenType.Invalid;

            if (this.credential != null) {
                hasAuthKeyResourceToken = false;
                this.authorizationTokenType = AuthorizationTokenType.PrimaryMasterKey;
                this.authorizationTokenProvider = new BaseAuthorizationTokenProvider(this.credential);
            } else if (masterKeyOrResourceToken != null && ResourceTokenAuthorizationHelper.isResourceToken(masterKeyOrResourceToken)) {
                this.authorizationTokenProvider = null;
                hasAuthKeyResourceToken = true;
                this.authorizationTokenType = AuthorizationTokenType.ResourceToken;
            } else if(masterKeyOrResourceToken != null && !ResourceTokenAuthorizationHelper.isResourceToken(masterKeyOrResourceToken)) {
                this.credential = new AzureKeyCredential(this.masterKeyOrResourceToken);
                hasAuthKeyResourceToken = false;
                this.authorizationTokenType = AuthorizationTokenType.PrimaryMasterKey;
                this.authorizationTokenProvider = new BaseAuthorizationTokenProvider(this.credential);
            } else {
                hasAuthKeyResourceToken = false;
                this.authorizationTokenProvider = null;
                if (tokenCredential != null) {
                    this.tokenCredentialScopes = new String[] {
//                    AadTokenAuthorizationHelper.AAD_AUTH_TOKEN_COSMOS_WINDOWS_SCOPE,
                        serviceEndpoint.getScheme() + "://" + serviceEndpoint.getHost() + "/.default"
                    };
                    this.tokenCredentialCache = new SimpleTokenCache(() -> this.tokenCredential
                        .getToken(new TokenRequestContext().addScopes(this.tokenCredentialScopes)));
                    this.authorizationTokenType = AuthorizationTokenType.AadToken;
                }
            }

            if (connectionPolicy != null) {
                this.connectionPolicy = connectionPolicy;
            } else {
                this.connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
            }

            this.diagnosticsClientConfig.withConnectionMode(this.getConnectionPolicy().getConnectionMode());
            this.diagnosticsClientConfig.withMultipleWriteRegionsEnabled(this.connectionPolicy.isMultipleWriteRegionsEnabled());
            this.diagnosticsClientConfig.withEndpointDiscoveryEnabled(this.connectionPolicy.isEndpointDiscoveryEnabled());
            this.diagnosticsClientConfig.withPreferredRegions(this.connectionPolicy.getPreferredRegions());

            boolean disableSessionCapturing = (ConsistencyLevel.SESSION != consistencyLevel && !sessionCapturingOverrideEnabled);

            this.sessionContainer = new SessionContainer(this.serviceEndpoint.getHost(), disableSessionCapturing);
            this.consistencyLevel = consistencyLevel;

            this.userAgentContainer = new UserAgentContainer();

            String userAgentSuffix = this.connectionPolicy.getUserAgentSuffix();
            if (userAgentSuffix != null && userAgentSuffix.length() > 0) {
                userAgentContainer.setSuffix(userAgentSuffix);
            }

            this.httpClientInterceptor = null;
            this.reactorHttpClient = httpClient();

            this.globalEndpointManager = new GlobalEndpointManager(asDatabaseAccountManagerInternal(), this.connectionPolicy, /**/configs);
            this.retryPolicy = new RetryPolicy(this, this.globalEndpointManager, this.connectionPolicy);
            this.resetSessionTokenRetryPolicy = retryPolicy;
            CpuMemoryMonitor.register(this);
            this.queryPlanCache = new ConcurrentHashMap<>();
        } catch (RuntimeException e) {
            logger.error("unexpected failure in initializing client.", e);
            close();
            throw e;
        }
    }

    @Override
    public DiagnosticsClientConfig getConfig() {
        return diagnosticsClientConfig;
    }

    @Override
    public CosmosDiagnostics createDiagnostics() {
       return BridgeInternal.createCosmosDiagnostics(this);
    }

    private void initializeGatewayConfigurationReader() {
        this.gatewayConfigurationReader = new GatewayServiceConfigurationReader(this.globalEndpointManager);
        DatabaseAccount databaseAccount = this.globalEndpointManager.getLatestDatabaseAccount();
        //Database account should not be null here,
        // this.globalEndpointManager.init() must have been already called
        // hence asserting it
        if (databaseAccount == null) {
            logger.error("Client initialization failed."
                + " Check if the endpoint is reachable and if your auth token is valid. More info: https://aka.ms/cosmosdb-tsg-service-unavailable-java");
            throw new RuntimeException("Client initialization failed."
                + " Check if the endpoint is reachable and if your auth token is valid. More info: https://aka.ms/cosmosdb-tsg-service-unavailable-java");
        }

        this.useMultipleWriteLocations = this.connectionPolicy.isMultipleWriteRegionsEnabled() && BridgeInternal.isEnableMultipleWriteLocations(databaseAccount);

        // TODO: add support for openAsync
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/332589
    }

    public void init(CosmosClientMetadataCachesSnapshot metadataCachesSnapshot, Function<HttpClient, HttpClient> httpClientInterceptor) {
        try {
            // TODO: add support for openAsync
            // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/332589

            this.httpClientInterceptor = httpClientInterceptor;
            if (httpClientInterceptor != null) {
                this.reactorHttpClient = httpClientInterceptor.apply(httpClient());
            }

            this.gatewayProxy = createRxGatewayProxy(this.sessionContainer,
                this.consistencyLevel,
                this.queryCompatibilityMode,
                this.userAgentContainer,
                this.globalEndpointManager,
                this.reactorHttpClient);
            this.globalEndpointManager.init();
            this.initializeGatewayConfigurationReader();

            if (metadataCachesSnapshot != null) {
                this.collectionCache = new RxClientCollectionCache(this,
                    this.sessionContainer,
                    this.gatewayProxy,
                    this,
                    this.retryPolicy,
                    metadataCachesSnapshot.getCollectionInfoByNameCache(),
                    metadataCachesSnapshot.getCollectionInfoByIdCache()
                );
            } else {
                this.collectionCache = new RxClientCollectionCache(this,
                    this.sessionContainer,
                    this.gatewayProxy,
                    this,
                    this.retryPolicy);
            }
            this.resetSessionTokenRetryPolicy = new ResetSessionTokenRetryPolicyFactory(this.sessionContainer, this.collectionCache, this.retryPolicy);

            this.partitionKeyRangeCache = new RxPartitionKeyRangeCache(RxDocumentClientImpl.this,
                collectionCache);

            if (this.connectionPolicy.getConnectionMode() == ConnectionMode.GATEWAY) {
                this.storeModel = this.gatewayProxy;
            } else {
                this.initializeDirectConnectivity();
            }
            clientTelemetry = new ClientTelemetry(null, UUID.randomUUID().toString(),
                ManagementFactory.getRuntimeMXBean().getName(), userAgentContainer.getUserAgent(),
                connectionPolicy.getConnectionMode(), globalEndpointManager.getLatestDatabaseAccount().getId(),
                null, null, this.reactorHttpClient, connectionPolicy.isClientTelemetryEnabled(), this);
            clientTelemetry.init();
            this.queryPlanCache = new ConcurrentHashMap<>();
        } catch (Exception e) {
            logger.error("unexpected failure in initializing client.", e);
            close();
            throw e;
        }
    }

    public void serialize(CosmosClientMetadataCachesSnapshot state) {
        RxCollectionCache.serialize(state, this.collectionCache);
    }

    private void initializeDirectConnectivity() {

        this.addressResolver = new GlobalAddressResolver(this,
            this.reactorHttpClient,
            this.globalEndpointManager,
            this.configs.getProtocol(),
            this,
            this.collectionCache,
            this.partitionKeyRangeCache,
            userAgentContainer,
            // TODO: GATEWAY Configuration Reader
            //     this.gatewayConfigurationReader,
            null,
            this.connectionPolicy);

        this.storeClientFactory = new StoreClientFactory(
            this.addressResolver,
            this.diagnosticsClientConfig,
            this.configs,
            this.connectionPolicy,
            // this.maxConcurrentConnectionOpenRequests,
            this.userAgentContainer,
            this.connectionSharingAcrossClientsEnabled
        );

        this.createStoreModel(true);
    }

    DatabaseAccountManagerInternal asDatabaseAccountManagerInternal() {
        return new DatabaseAccountManagerInternal() {

            @Override
            public URI getServiceEndpoint() {
                return RxDocumentClientImpl.this.getServiceEndpoint();
            }

            @Override
            public Flux<DatabaseAccount> getDatabaseAccountFromEndpoint(URI endpoint) {
                logger.info("Getting database account endpoint from {}", endpoint);
                return RxDocumentClientImpl.this.getDatabaseAccountFromEndpoint(endpoint);
            }

            @Override
            public ConnectionPolicy getConnectionPolicy() {
                return RxDocumentClientImpl.this.getConnectionPolicy();
            }
        };
    }

    RxGatewayStoreModel createRxGatewayProxy(ISessionContainer sessionContainer,
                                             ConsistencyLevel consistencyLevel,
                                             QueryCompatibilityMode queryCompatibilityMode,
                                             UserAgentContainer userAgentContainer,
                                             GlobalEndpointManager globalEndpointManager,
                                             HttpClient httpClient) {
        return new RxGatewayStoreModel(
                this,
                sessionContainer,
                consistencyLevel,
                queryCompatibilityMode,
                userAgentContainer,
                globalEndpointManager,
                httpClient);
    }

    private HttpClient httpClient() {
        HttpClientConfig httpClientConfig = new HttpClientConfig(this.configs)
            .withMaxIdleConnectionTimeout(this.connectionPolicy.getIdleHttpConnectionTimeout())
            .withPoolSize(this.connectionPolicy.getMaxConnectionPoolSize())
            .withProxy(this.connectionPolicy.getProxy())
            .withRequestTimeout(this.connectionPolicy.getRequestTimeout());

        if (connectionSharingAcrossClientsEnabled) {
            return SharedGatewayHttpClient.getOrCreateInstance(httpClientConfig, diagnosticsClientConfig);
        } else {
            diagnosticsClientConfig.withGatewayHttpClientConfig(httpClientConfig);
            return HttpClient.createFixed(httpClientConfig);
        }
    }

    private void createStoreModel(boolean subscribeRntbdStatus) {
        // EnableReadRequestsFallback, if not explicitly set on the connection policy,
        // is false if the account's consistency is bounded staleness,
        // and true otherwise.

        StoreClient storeClient = this.storeClientFactory.createStoreClient(this,
                this.addressResolver,
                this.sessionContainer,
                this.gatewayConfigurationReader,
                this,
                false
        );

        this.storeModel = new ServerStoreModel(storeClient);
    }


    @Override
    public URI getServiceEndpoint() {
        return this.serviceEndpoint;
    }

    @Override
    public URI getWriteEndpoint() {
        return globalEndpointManager.getWriteEndpoints().stream().findFirst().orElse(null);
    }

    @Override
    public URI getReadEndpoint() {
        return globalEndpointManager.getReadEndpoints().stream().findFirst().orElse(null);
    }

    @Override
    public ConnectionPolicy getConnectionPolicy() {
        return this.connectionPolicy;
    }

    @Override
    public boolean isContentResponseOnWriteEnabled() {
        return contentResponseOnWriteEnabled;
    }

    @Override
    public ConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

    @Override
    public ClientTelemetry getClientTelemetry() {
        return this.clientTelemetry;
    }

    @Override
    public Mono<ResourceResponse<Database>> createDatabase(Database database, RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> createDatabaseInternal(database, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<Database>> createDatabaseInternal(Database database, RequestOptions options, DocumentClientRetryPolicy retryPolicyInstance) {
        try {

            if (database == null) {
                throw new IllegalArgumentException("Database");
            }

            logger.debug("Creating a Database. id: [{}]", database.getId());
            validateResource(database);

            Map<String, String> requestHeaders = this.getRequestHeaders(options, ResourceType.Database, OperationType.Create);
            Instant serializationStartTimeUTC = Instant.now();
            ByteBuffer byteBuffer = ModelBridgeInternal.serializeJsonToByteBuffer(database);
            Instant serializationEndTimeUTC = Instant.now();
            SerializationDiagnosticsContext.SerializationDiagnostics serializationDiagnostics = new SerializationDiagnosticsContext.SerializationDiagnostics(
                serializationStartTimeUTC,
                serializationEndTimeUTC,
                SerializationDiagnosticsContext.SerializationType.DATABASE_SERIALIZATION);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Create, ResourceType.Database, Paths.DATABASES_ROOT, byteBuffer, requestHeaders, options);

            if (retryPolicyInstance != null) {
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            SerializationDiagnosticsContext serializationDiagnosticsContext = BridgeInternal.getSerializationDiagnosticsContext(request.requestContext.cosmosDiagnostics);
            if (serializationDiagnosticsContext != null) {
                serializationDiagnosticsContext.addSerializationDiagnostics(serializationDiagnostics);
            }

            return this.create(request, retryPolicyInstance, getOperationContextAndListenerTuple(options))
                       .map(response -> toResourceResponse(response, Database.class));
        } catch (Exception e) {
            logger.debug("Failure in creating a database. due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<Database>> deleteDatabase(String databaseLink, RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> deleteDatabaseInternal(databaseLink, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<Database>> deleteDatabaseInternal(String databaseLink, RequestOptions options,
                                                                    DocumentClientRetryPolicy retryPolicyInstance) {
        try {
            if (StringUtils.isEmpty(databaseLink)) {
                throw new IllegalArgumentException("databaseLink");
            }

            logger.debug("Deleting a Database. databaseLink: [{}]", databaseLink);
            String path = Utils.joinPath(databaseLink, null);
            Map<String, String> requestHeaders = this.getRequestHeaders(options, ResourceType.Database, OperationType.Delete);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Delete, ResourceType.Database, path, requestHeaders, options);

            if (retryPolicyInstance != null) {
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            return this.delete(request, retryPolicyInstance, getOperationContextAndListenerTuple(options)).map(response -> toResourceResponse(response, Database.class));
        } catch (Exception e) {
            logger.debug("Failure in deleting a database. due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<Database>> readDatabase(String databaseLink, RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> readDatabaseInternal(databaseLink, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<Database>> readDatabaseInternal(String databaseLink, RequestOptions options, DocumentClientRetryPolicy retryPolicyInstance) {
        try {
            if (StringUtils.isEmpty(databaseLink)) {
                throw new IllegalArgumentException("databaseLink");
            }

            logger.debug("Reading a Database. databaseLink: [{}]", databaseLink);
            String path = Utils.joinPath(databaseLink, null);
            Map<String, String> requestHeaders = this.getRequestHeaders(options, ResourceType.Database, OperationType.Read);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Read, ResourceType.Database, path, requestHeaders, options);

            if (retryPolicyInstance != null) {
                retryPolicyInstance.onBeforeSendRequest(request);
            }
            return this.read(request, retryPolicyInstance).map(response -> toResourceResponse(response, Database.class));
        } catch (Exception e) {
            logger.debug("Failure in reading a database. due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Flux<FeedResponse<Database>> readDatabases(CosmosQueryRequestOptions options) {
        return readFeed(options, ResourceType.Database, Database.class, Paths.DATABASES_ROOT);
    }

    private String parentResourceLinkToQueryLink(String parentResourceLink, ResourceType resourceTypeEnum) {
        switch (resourceTypeEnum) {
            case Database:
                return Paths.DATABASES_ROOT;

            case DocumentCollection:
                return Utils.joinPath(parentResourceLink, Paths.COLLECTIONS_PATH_SEGMENT);

            case Document:
                return Utils.joinPath(parentResourceLink, Paths.DOCUMENTS_PATH_SEGMENT);

            case Offer:
                return Paths.OFFERS_ROOT;

            case User:
                return Utils.joinPath(parentResourceLink, Paths.USERS_PATH_SEGMENT);

            case ClientEncryptionKey:
                return Utils.joinPath(parentResourceLink, Paths.CLIENT_ENCRYPTION_KEY_PATH_SEGMENT);

            case Permission:
                return Utils.joinPath(parentResourceLink, Paths.PERMISSIONS_PATH_SEGMENT);

            case Attachment:
                return Utils.joinPath(parentResourceLink, Paths.ATTACHMENTS_PATH_SEGMENT);

            case StoredProcedure:
                return Utils.joinPath(parentResourceLink, Paths.STORED_PROCEDURES_PATH_SEGMENT);

            case Trigger:
                return Utils.joinPath(parentResourceLink, Paths.TRIGGERS_PATH_SEGMENT);

            case UserDefinedFunction:
                return Utils.joinPath(parentResourceLink, Paths.USER_DEFINED_FUNCTIONS_PATH_SEGMENT);

            case Conflict:
                return Utils.joinPath(parentResourceLink, Paths.CONFLICTS_PATH_SEGMENT);

            default:
                throw new IllegalArgumentException("resource type not supported");
        }
    }

    private OperationContextAndListenerTuple getOperationContextAndListenerTuple(CosmosQueryRequestOptions options) {
        if (options == null) {
            return null;
        }
        return ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.getCosmosQueryRequestOptionsAccessor().getOperationContext(options);
    }

    private OperationContextAndListenerTuple getOperationContextAndListenerTuple(RequestOptions options) {
        if (options == null) {
            return null;
        }
        return options.getOperationContextAndListenerTuple();
    }

    private <T extends Resource> Flux<FeedResponse<T>> createQuery(
        String parentResourceLink,
        SqlQuerySpec sqlQuery,
        CosmosQueryRequestOptions options,
        Class<T> klass,
        ResourceType resourceTypeEnum) {

        String resourceLink = parentResourceLinkToQueryLink(parentResourceLink, resourceTypeEnum);
        UUID activityId = Utils.randomUUID();
        IDocumentQueryClient queryClient = documentQueryClientImpl(RxDocumentClientImpl.this,
            getOperationContextAndListenerTuple(options));

        // Trying to put this logic as low as the query pipeline
        // Since for parallelQuery, each partition will have its own request, so at this point, there will be no request associate with this retry policy.
        // For default document context, it already wired up InvalidPartitionExceptionRetry, but there is no harm to wire it again here
        InvalidPartitionExceptionRetryPolicy invalidPartitionExceptionRetryPolicy = new InvalidPartitionExceptionRetryPolicy(
            this.collectionCache,
            null,
            resourceLink,
            ModelBridgeInternal.getPropertiesFromQueryRequestOptions(options));

        return ObservableHelper.fluxInlineIfPossibleAsObs(
            () -> createQueryInternal(resourceLink, sqlQuery, options, klass, resourceTypeEnum, queryClient, activityId),
            invalidPartitionExceptionRetryPolicy);
    }

    private <T extends Resource> Flux<FeedResponse<T>> createQueryInternal(
            String resourceLink,
            SqlQuerySpec sqlQuery,
            CosmosQueryRequestOptions options,
            Class<T> klass,
            ResourceType resourceTypeEnum,
            IDocumentQueryClient queryClient,
            UUID activityId) {

        Flux<? extends IDocumentQueryExecutionContext<T>> executionContext =
            DocumentQueryExecutionContextFactory
                .createDocumentQueryExecutionContextAsync(this, queryClient, resourceTypeEnum, klass, sqlQuery,
                                                          options, resourceLink, false, activityId,
                                                          Configs.isQueryPlanCachingEnabled(), queryPlanCache);

        AtomicBoolean isFirstResponse = new AtomicBoolean(true);
        return executionContext.flatMap(iDocumentQueryExecutionContext -> {
            QueryInfo queryInfo = null;
            if (iDocumentQueryExecutionContext instanceof PipelinedDocumentQueryExecutionContext) {
                queryInfo = ((PipelinedDocumentQueryExecutionContext<T>) iDocumentQueryExecutionContext).getQueryInfo();
            }

            QueryInfo finalQueryInfo = queryInfo;
            return iDocumentQueryExecutionContext.executeAsync()
                .map(tFeedResponse -> {
                    if (finalQueryInfo != null) {
                        if (finalQueryInfo.hasSelectValue()) {
                            ModelBridgeInternal
                                .addQueryInfoToFeedResponse(tFeedResponse, finalQueryInfo);
                        }

                        if (isFirstResponse.compareAndSet(true, false)) {
                            ModelBridgeInternal.addQueryPlanDiagnosticsContextToFeedResponse(tFeedResponse,
                                finalQueryInfo.getQueryPlanDiagnosticsContext());
                        }
                    }
                    return tFeedResponse;
                });
        });
    }

    @Override
    public Flux<FeedResponse<Database>> queryDatabases(String query, CosmosQueryRequestOptions options) {
        return queryDatabases(new SqlQuerySpec(query), options);
    }


    @Override
    public Flux<FeedResponse<Database>> queryDatabases(SqlQuerySpec querySpec, CosmosQueryRequestOptions options) {
        return createQuery(Paths.DATABASES_ROOT, querySpec, options, Database.class, ResourceType.Database);
    }

    @Override
    public Mono<ResourceResponse<DocumentCollection>> createCollection(String databaseLink,
                                                                       DocumentCollection collection, RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> this.createCollectionInternal(databaseLink, collection, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<DocumentCollection>> createCollectionInternal(String databaseLink,
                                                                                DocumentCollection collection, RequestOptions options, DocumentClientRetryPolicy retryPolicyInstance) {
        try {
            if (StringUtils.isEmpty(databaseLink)) {
                throw new IllegalArgumentException("databaseLink");
            }
            if (collection == null) {
                throw new IllegalArgumentException("collection");
            }

            logger.debug("Creating a Collection. databaseLink: [{}], Collection id: [{}]", databaseLink,
                collection.getId());
            validateResource(collection);

            String path = Utils.joinPath(databaseLink, Paths.COLLECTIONS_PATH_SEGMENT);
            Map<String, String> requestHeaders = this.getRequestHeaders(options, ResourceType.DocumentCollection, OperationType.Create);

            Instant serializationStartTimeUTC = Instant.now();
            ByteBuffer byteBuffer = ModelBridgeInternal.serializeJsonToByteBuffer(collection);
            Instant serializationEndTimeUTC = Instant.now();
            SerializationDiagnosticsContext.SerializationDiagnostics serializationDiagnostics = new SerializationDiagnosticsContext.SerializationDiagnostics(
                serializationStartTimeUTC,
                serializationEndTimeUTC,
                SerializationDiagnosticsContext.SerializationType.CONTAINER_SERIALIZATION);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Create, ResourceType.DocumentCollection, path, byteBuffer, requestHeaders, options);

            if (retryPolicyInstance != null){
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            SerializationDiagnosticsContext serializationDiagnosticsContext = BridgeInternal.getSerializationDiagnosticsContext(request.requestContext.cosmosDiagnostics);
            if (serializationDiagnosticsContext != null) {
                serializationDiagnosticsContext.addSerializationDiagnostics(serializationDiagnostics);
            }

            return this.create(request, retryPolicyInstance, getOperationContextAndListenerTuple(options)).map(response -> toResourceResponse(response, DocumentCollection.class))
                       .doOnNext(resourceResponse -> {
                    // set the session token
                    this.sessionContainer.setSessionToken(resourceResponse.getResource().getResourceId(),
                        getAltLink(resourceResponse.getResource()),
                        resourceResponse.getResponseHeaders());
                });
        } catch (Exception e) {
            logger.debug("Failure in creating a collection. due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<DocumentCollection>> replaceCollection(DocumentCollection collection,
                                                                        RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> replaceCollectionInternal(collection, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<DocumentCollection>> replaceCollectionInternal(DocumentCollection collection,
                                                                                 RequestOptions options, DocumentClientRetryPolicy retryPolicyInstance) {
        try {
            if (collection == null) {
                throw new IllegalArgumentException("collection");
            }

            logger.debug("Replacing a Collection. id: [{}]", collection.getId());
            validateResource(collection);

            String path = Utils.joinPath(collection.getSelfLink(), null);
            Map<String, String> requestHeaders = this.getRequestHeaders(options, ResourceType.DocumentCollection, OperationType.Replace);
            Instant serializationStartTimeUTC = Instant.now();
            ByteBuffer byteBuffer = ModelBridgeInternal.serializeJsonToByteBuffer(collection);
            Instant serializationEndTimeUTC = Instant.now();
            SerializationDiagnosticsContext.SerializationDiagnostics serializationDiagnostics = new SerializationDiagnosticsContext.SerializationDiagnostics(
                serializationStartTimeUTC,
                serializationEndTimeUTC,
                SerializationDiagnosticsContext.SerializationType.CONTAINER_SERIALIZATION);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Replace, ResourceType.DocumentCollection, path, byteBuffer, requestHeaders, options);

            // TODO: .Net has some logic for updating session token which we don't
            // have here
            if (retryPolicyInstance != null){
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            SerializationDiagnosticsContext serializationDiagnosticsContext = BridgeInternal.getSerializationDiagnosticsContext(request.requestContext.cosmosDiagnostics);
            if (serializationDiagnosticsContext != null) {
                serializationDiagnosticsContext.addSerializationDiagnostics(serializationDiagnostics);
            }

            return this.replace(request, retryPolicyInstance).map(response -> toResourceResponse(response, DocumentCollection.class))
                .doOnNext(resourceResponse -> {
                    if (resourceResponse.getResource() != null) {
                        // set the session token
                        this.sessionContainer.setSessionToken(resourceResponse.getResource().getResourceId(),
                            getAltLink(resourceResponse.getResource()),
                            resourceResponse.getResponseHeaders());
                    }
                });

        } catch (Exception e) {
            logger.debug("Failure in replacing a collection. due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<DocumentCollection>> deleteCollection(String collectionLink,
                                                                       RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> deleteCollectionInternal(collectionLink, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<DocumentCollection>> deleteCollectionInternal(String collectionLink,
                                                                                RequestOptions options, DocumentClientRetryPolicy retryPolicyInstance) {
        try {
            if (StringUtils.isEmpty(collectionLink)) {
                throw new IllegalArgumentException("collectionLink");
            }

            logger.debug("Deleting a Collection. collectionLink: [{}]", collectionLink);
            String path = Utils.joinPath(collectionLink, null);
            Map<String, String> requestHeaders = this.getRequestHeaders(options, ResourceType.DocumentCollection, OperationType.Delete);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Delete, ResourceType.DocumentCollection, path, requestHeaders, options);

            if (retryPolicyInstance != null){
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            return this.delete(request, retryPolicyInstance, getOperationContextAndListenerTuple(options)).map(response -> toResourceResponse(response, DocumentCollection.class));

        } catch (Exception e) {
            logger.debug("Failure in deleting a collection, due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    private Mono<RxDocumentServiceResponse> delete(RxDocumentServiceRequest request, DocumentClientRetryPolicy documentClientRetryPolicy, OperationContextAndListenerTuple operationContextAndListenerTuple) {
        return populateHeaders(request, RequestVerb.DELETE)
            .flatMap(requestPopulated -> {
                if (documentClientRetryPolicy.getRetryContext() != null && documentClientRetryPolicy.getRetryContext().getRetryCount() > 0) {
                    documentClientRetryPolicy.getRetryContext().updateEndTime();
                }

                return getStoreProxy(requestPopulated).processMessage(requestPopulated, operationContextAndListenerTuple);
            });
    }

    private Mono<RxDocumentServiceResponse> deleteAllItemsByPartitionKey(RxDocumentServiceRequest request, DocumentClientRetryPolicy documentClientRetryPolicy, OperationContextAndListenerTuple operationContextAndListenerTuple) {
        return populateHeaders(request, RequestVerb.POST)
            .flatMap(requestPopulated -> {
                RxStoreModel storeProxy = this.getStoreProxy(requestPopulated);
                if (documentClientRetryPolicy.getRetryContext() != null && documentClientRetryPolicy.getRetryContext().getRetryCount() > 0) {
                    documentClientRetryPolicy.getRetryContext().updateEndTime();
                }

                return storeProxy.processMessage(requestPopulated, operationContextAndListenerTuple);
            });
    }

    private Mono<RxDocumentServiceResponse> read(RxDocumentServiceRequest request, DocumentClientRetryPolicy documentClientRetryPolicy) {
        return populateHeaders(request, RequestVerb.GET)
            .flatMap(requestPopulated -> {
                if (documentClientRetryPolicy.getRetryContext() != null && documentClientRetryPolicy.getRetryContext().getRetryCount() > 0) {
                    documentClientRetryPolicy.getRetryContext().updateEndTime();
                }

                return getStoreProxy(requestPopulated).processMessage(requestPopulated);
                });
    }

    Mono<RxDocumentServiceResponse> readFeed(RxDocumentServiceRequest request) {
        return populateHeaders(request, RequestVerb.GET)
            .flatMap(requestPopulated -> getStoreProxy(requestPopulated).processMessage(requestPopulated));
    }

    private Mono<RxDocumentServiceResponse> query(RxDocumentServiceRequest request) {
        return populateHeaders(request, RequestVerb.POST)
            .flatMap(requestPopulated ->
                this.getStoreProxy(requestPopulated).processMessage(requestPopulated)
                    .map(response -> {
                            this.captureSessionToken(requestPopulated, response);
                            return response;
                        }
                    ));
    }

    @Override
    public Mono<ResourceResponse<DocumentCollection>> readCollection(String collectionLink,
                                                                     RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> readCollectionInternal(collectionLink, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<DocumentCollection>> readCollectionInternal(String collectionLink,
                                                                              RequestOptions options, DocumentClientRetryPolicy retryPolicyInstance) {

        // we are using an observable factory here
        // observable will be created fresh upon subscription
        // this is to ensure we capture most up to date information (e.g.,
        // session)
        try {
            if (StringUtils.isEmpty(collectionLink)) {
                throw new IllegalArgumentException("collectionLink");
            }

            logger.debug("Reading a Collection. collectionLink: [{}]", collectionLink);
            String path = Utils.joinPath(collectionLink, null);
            Map<String, String> requestHeaders = this.getRequestHeaders(options, ResourceType.DocumentCollection, OperationType.Read);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Read, ResourceType.DocumentCollection, path, requestHeaders, options);

            if (retryPolicyInstance != null){
                retryPolicyInstance.onBeforeSendRequest(request);
            }
            return this.read(request, retryPolicyInstance).map(response -> toResourceResponse(response, DocumentCollection.class));
        } catch (Exception e) {
            // this is only in trace level to capture what's going on
            logger.debug("Failure in reading a collection, due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Flux<FeedResponse<DocumentCollection>> readCollections(String databaseLink, CosmosQueryRequestOptions options) {

        if (StringUtils.isEmpty(databaseLink)) {
            throw new IllegalArgumentException("databaseLink");
        }

        return readFeed(options, ResourceType.DocumentCollection, DocumentCollection.class,
                Utils.joinPath(databaseLink, Paths.COLLECTIONS_PATH_SEGMENT));
    }

    @Override
    public Flux<FeedResponse<DocumentCollection>> queryCollections(String databaseLink, String query,
                                                                   CosmosQueryRequestOptions options) {
        return createQuery(databaseLink, new SqlQuerySpec(query), options, DocumentCollection.class, ResourceType.DocumentCollection);
    }

    @Override
    public Flux<FeedResponse<DocumentCollection>> queryCollections(String databaseLink,
                                                                         SqlQuerySpec querySpec, CosmosQueryRequestOptions options) {
        return createQuery(databaseLink, querySpec, options, DocumentCollection.class, ResourceType.DocumentCollection);
    }

    private static String serializeProcedureParams(List<Object> objectArray) {
        String[] stringArray = new String[objectArray.size()];

        for (int i = 0; i < objectArray.size(); ++i) {
            Object object = objectArray.get(i);
            if (object instanceof JsonSerializable) {
                stringArray[i] = ModelBridgeInternal.toJsonFromJsonSerializable((JsonSerializable) object);
            } else {

                // POJO, ObjectNode, number, STRING or Boolean
                try {
                    stringArray[i] = mapper.writeValueAsString(object);
                } catch (IOException e) {
                    throw new IllegalArgumentException("Can't serialize the object into the json string", e);
                }
            }
        }

        return String.format("[%s]", StringUtils.join(stringArray, ","));
    }

    private static void validateResource(Resource resource) {
        if (!StringUtils.isEmpty(resource.getId())) {
            if (resource.getId().indexOf('/') != -1 || resource.getId().indexOf('\\') != -1 ||
                    resource.getId().indexOf('?') != -1 || resource.getId().indexOf('#') != -1) {
                throw new IllegalArgumentException("Id contains illegal chars.");
            }

            if (resource.getId().endsWith(" ")) {
                throw new IllegalArgumentException("Id ends with a space.");
            }
        }
    }

    private Map<String, String> getRequestHeaders(RequestOptions options, ResourceType resourceType, OperationType operationType) {
        Map<String, String> headers = new HashMap<>();

        if (this.useMultipleWriteLocations) {
            headers.put(HttpConstants.HttpHeaders.ALLOW_TENTATIVE_WRITES, Boolean.TRUE.toString());
        }

        if (consistencyLevel != null) {
            headers.put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, consistencyLevel.toString());
        }

        if (options == null) {
            //  Corner case, if options are null, then just check this flag from CosmosClientBuilder
            //  If content response on write is not enabled, and operation is document write - then add minimal prefer header
            //  Otherwise, don't add this header, which means return the full response
            if (!this.contentResponseOnWriteEnabled && resourceType.equals(ResourceType.Document) && operationType.isWriteOperation()) {
                headers.put(HttpConstants.HttpHeaders.PREFER, HttpConstants.HeaderValues.PREFER_RETURN_MINIMAL);
            }
            return headers;
        }

        Map<String, String> customOptions = options.getHeaders();
        if (customOptions != null) {
            headers.putAll(customOptions);
        }

        boolean contentResponseOnWriteEnabled = this.contentResponseOnWriteEnabled;
        //  If options has contentResponseOnWriteEnabled set to true / false, override the value from CosmosClientBuilder
        if (options.isContentResponseOnWriteEnabled() != null) {
            contentResponseOnWriteEnabled = options.isContentResponseOnWriteEnabled();
        }

        //  If content response on write is not enabled, and operation is document write - then add minimal prefer header
        //  Otherwise, don't add this header, which means return the full response
        if (!contentResponseOnWriteEnabled && resourceType.equals(ResourceType.Document) && operationType.isWriteOperation()) {
            headers.put(HttpConstants.HttpHeaders.PREFER, HttpConstants.HeaderValues.PREFER_RETURN_MINIMAL);
        }

        if (options.getIfMatchETag() != null) {
                headers.put(HttpConstants.HttpHeaders.IF_MATCH, options.getIfMatchETag());
        }

        if(options.getIfNoneMatchETag() != null) {
            headers.put(HttpConstants.HttpHeaders.IF_NONE_MATCH, options.getIfNoneMatchETag());
        }

        if (options.getConsistencyLevel() != null) {
            headers.put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, options.getConsistencyLevel().toString());
        }

        if (options.getIndexingDirective() != null) {
            headers.put(HttpConstants.HttpHeaders.INDEXING_DIRECTIVE, options.getIndexingDirective().toString());
        }

        if (options.getPostTriggerInclude() != null && options.getPostTriggerInclude().size() > 0) {
            String postTriggerInclude = StringUtils.join(options.getPostTriggerInclude(), ",");
            headers.put(HttpConstants.HttpHeaders.POST_TRIGGER_INCLUDE, postTriggerInclude);
        }

        if (options.getPreTriggerInclude() != null && options.getPreTriggerInclude().size() > 0) {
            String preTriggerInclude = StringUtils.join(options.getPreTriggerInclude(), ",");
            headers.put(HttpConstants.HttpHeaders.PRE_TRIGGER_INCLUDE, preTriggerInclude);
        }

        if (!Strings.isNullOrEmpty(options.getSessionToken())) {
            headers.put(HttpConstants.HttpHeaders.SESSION_TOKEN, options.getSessionToken());
        }

        if (options.getResourceTokenExpirySeconds() != null) {
            headers.put(HttpConstants.HttpHeaders.RESOURCE_TOKEN_EXPIRY,
                    String.valueOf(options.getResourceTokenExpirySeconds()));
        }

        if (options.getOfferThroughput() != null && options.getOfferThroughput() >= 0) {
            headers.put(HttpConstants.HttpHeaders.OFFER_THROUGHPUT, options.getOfferThroughput().toString());
        } else if (options.getOfferType() != null) {
            headers.put(HttpConstants.HttpHeaders.OFFER_TYPE, options.getOfferType());
        }

        if (options.getOfferThroughput() == null) {
            if (options.getThroughputProperties() != null) {
                Offer offer = ModelBridgeInternal.getOfferFromThroughputProperties(options.getThroughputProperties());
                final OfferAutoscaleSettings offerAutoscaleSettings = offer.getOfferAutoScaleSettings();
                OfferAutoscaleAutoUpgradeProperties autoscaleAutoUpgradeProperties = null;
                if (offerAutoscaleSettings != null) {
                     autoscaleAutoUpgradeProperties
                        = offer.getOfferAutoScaleSettings().getAutoscaleAutoUpgradeProperties();
                }
                if (offer.hasOfferThroughput() &&
                        (offerAutoscaleSettings != null && offerAutoscaleSettings.getMaxThroughput() >= 0 ||
                             autoscaleAutoUpgradeProperties != null &&
                                 autoscaleAutoUpgradeProperties
                                     .getAutoscaleThroughputProperties()
                                     .getIncrementPercent() >= 0)) {
                    throw new IllegalArgumentException("Autoscale provisioned throughput can not be configured with "
                                                           + "fixed offer");
                }

                if (offer.hasOfferThroughput()) {
                    headers.put(HttpConstants.HttpHeaders.OFFER_THROUGHPUT, String.valueOf(offer.getThroughput()));
                } else if (offer.getOfferAutoScaleSettings() != null) {
                    headers.put(HttpConstants.HttpHeaders.OFFER_AUTOPILOT_SETTINGS,
                                ModelBridgeInternal.toJsonFromJsonSerializable(offer.getOfferAutoScaleSettings()));
                }
            }
        }

        if (options.isQuotaInfoEnabled()) {
            headers.put(HttpConstants.HttpHeaders.POPULATE_QUOTA_INFO, String.valueOf(true));
        }

        if (options.isScriptLoggingEnabled()) {
            headers.put(HttpConstants.HttpHeaders.SCRIPT_ENABLE_LOGGING, String.valueOf(true));
        }

        if (options.getDedicatedGatewayRequestOptions() != null &&
            options.getDedicatedGatewayRequestOptions().getMaxIntegratedCacheStaleness() != null) {
            headers.put(HttpConstants.HttpHeaders.DEDICATED_GATEWAY_PER_REQUEST_CACHE_STALENESS,
                String.valueOf(Utils.getMaxIntegratedCacheStalenessInMillis(options.getDedicatedGatewayRequestOptions())));
        }

        return headers;
    }

    public IRetryPolicyFactory getResetSessionTokenRetryPolicy() {
        return this.resetSessionTokenRetryPolicy;
    }

    private Mono<RxDocumentServiceRequest> addPartitionKeyInformation(RxDocumentServiceRequest request,
                                                                      ByteBuffer contentAsByteBuffer,
                                                                      Document document,
                                                                      RequestOptions options) {

        Mono<Utils.ValueHolder<DocumentCollection>> collectionObs = this.collectionCache.resolveCollectionAsync(BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics), request);
        return collectionObs
                .map(collectionValueHolder -> {
                    addPartitionKeyInformation(request, contentAsByteBuffer, document, options, collectionValueHolder.v);
                    return request;
                });
    }

    private Mono<RxDocumentServiceRequest> addPartitionKeyInformation(RxDocumentServiceRequest request,
                                                                      ByteBuffer contentAsByteBuffer,
                                                                      Object document,
                                                                      RequestOptions options,
                                                                      Mono<Utils.ValueHolder<DocumentCollection>> collectionObs) {

        return collectionObs.map(collectionValueHolder -> {
            addPartitionKeyInformation(request, contentAsByteBuffer, document, options, collectionValueHolder.v);
            return request;
        });
    }

    private void addPartitionKeyInformation(RxDocumentServiceRequest request,
                                            ByteBuffer contentAsByteBuffer,
                                            Object objectDoc, RequestOptions options,
                                            DocumentCollection collection) {
        PartitionKeyDefinition partitionKeyDefinition = collection.getPartitionKey();

        PartitionKeyInternal partitionKeyInternal = null;
        if (options != null && options.getPartitionKey() != null && options.getPartitionKey().equals(PartitionKey.NONE)){
            partitionKeyInternal = ModelBridgeInternal.getNonePartitionKey(partitionKeyDefinition);
        } else if (options != null && options.getPartitionKey() != null) {
            partitionKeyInternal = BridgeInternal.getPartitionKeyInternal(options.getPartitionKey());
        } else if (partitionKeyDefinition == null || partitionKeyDefinition.getPaths().size() == 0) {
            // For backward compatibility, if collection doesn't have partition key defined, we assume all documents
            // have empty value for it and user doesn't need to specify it explicitly.
            partitionKeyInternal = PartitionKeyInternal.getEmpty();
        } else if (contentAsByteBuffer != null || objectDoc != null) {
            InternalObjectNode internalObjectNode;
            if (objectDoc instanceof InternalObjectNode) {
                internalObjectNode = (InternalObjectNode) objectDoc;
            } else if (objectDoc instanceof ObjectNode) {
                internalObjectNode = new InternalObjectNode((ObjectNode)objectDoc);
            } else if (contentAsByteBuffer != null) {
                contentAsByteBuffer.rewind();
                internalObjectNode = new InternalObjectNode(contentAsByteBuffer);
            } else {
                //  This is a safety check, this should not happen ever.
                //  If it does, it is a SDK bug
                throw new IllegalStateException("ContentAsByteBuffer and objectDoc are null");
            }

            Instant serializationStartTime = Instant.now();
            partitionKeyInternal =  extractPartitionKeyValueFromDocument(internalObjectNode, partitionKeyDefinition);
            Instant serializationEndTime = Instant.now();
            SerializationDiagnosticsContext.SerializationDiagnostics serializationDiagnostics = new SerializationDiagnosticsContext.SerializationDiagnostics(
                serializationStartTime,
                serializationEndTime,
                SerializationDiagnosticsContext.SerializationType.PARTITION_KEY_FETCH_SERIALIZATION
            );
            SerializationDiagnosticsContext serializationDiagnosticsContext = BridgeInternal.getSerializationDiagnosticsContext(request.requestContext.cosmosDiagnostics);
            if (serializationDiagnosticsContext != null) {
                serializationDiagnosticsContext.addSerializationDiagnostics(serializationDiagnostics);
            }

        } else {
            throw new UnsupportedOperationException("PartitionKey value must be supplied for this operation.");
        }

        request.setPartitionKeyInternal(partitionKeyInternal);
        request.getHeaders().put(HttpConstants.HttpHeaders.PARTITION_KEY, Utils.escapeNonAscii(partitionKeyInternal.toJson()));
    }

    public static PartitionKeyInternal extractPartitionKeyValueFromDocument(
            InternalObjectNode document,
            PartitionKeyDefinition partitionKeyDefinition) {
        if (partitionKeyDefinition != null) {
            switch (partitionKeyDefinition.getKind()) {
                case HASH:
                    String path = partitionKeyDefinition.getPaths().iterator().next();
                    List<String> parts = PathParser.getPathParts(path);
                    if (parts.size() >= 1) {
                        Object value = ModelBridgeInternal.getObjectByPathFromJsonSerializable(document, parts);
                        if (value == null || value.getClass() == ObjectNode.class) {
                            value = ModelBridgeInternal.getNonePartitionKey(partitionKeyDefinition);
                        }

                        if (value instanceof PartitionKeyInternal) {
                            return (PartitionKeyInternal) value;
                        } else {
                            return PartitionKeyInternal.fromObjectArray(Collections.singletonList(value), false);
                        }
                    }
                    break;
                case MULTI_HASH:
                    Object[] partitionKeyValues = new Object[partitionKeyDefinition.getPaths().size()];
                    for(int pathIter = 0 ; pathIter < partitionKeyDefinition.getPaths().size(); pathIter++){
                        String partitionPath = partitionKeyDefinition.getPaths().get(pathIter);
                        List<String> partitionPathParts = PathParser.getPathParts(partitionPath);
                        partitionKeyValues[pathIter] = ModelBridgeInternal.getObjectByPathFromJsonSerializable(document, partitionPathParts);
                    }
                    return PartitionKeyInternal.fromObjectArray(partitionKeyValues, false);

                default:
                    throw new IllegalArgumentException("Unrecognized Partition kind: " + partitionKeyDefinition.getKind());
                }
        }

        return null;
    }

    private Mono<RxDocumentServiceRequest> getCreateDocumentRequest(DocumentClientRetryPolicy requestRetryPolicy,
                                                                    String documentCollectionLink,
                                                                    Object document,
                                                                    RequestOptions options,
                                                                    boolean disableAutomaticIdGeneration,
                                                                    OperationType operationType) {

        if (StringUtils.isEmpty(documentCollectionLink)) {
            throw new IllegalArgumentException("documentCollectionLink");
        }
        if (document == null) {
            throw new IllegalArgumentException("document");
        }

        Instant serializationStartTimeUTC = Instant.now();
        ByteBuffer content = BridgeInternal.serializeJsonToByteBuffer(document, mapper);
        Instant serializationEndTimeUTC = Instant.now();

        SerializationDiagnosticsContext.SerializationDiagnostics serializationDiagnostics = new SerializationDiagnosticsContext.SerializationDiagnostics(
            serializationStartTimeUTC,
            serializationEndTimeUTC,
            SerializationDiagnosticsContext.SerializationType.ITEM_SERIALIZATION);

        String path = Utils.joinPath(documentCollectionLink, Paths.DOCUMENTS_PATH_SEGMENT);
        Map<String, String> requestHeaders = this.getRequestHeaders(options, ResourceType.Document, operationType);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
            operationType, ResourceType.Document, path, requestHeaders, options, content);
        if (requestRetryPolicy != null) {
            requestRetryPolicy.onBeforeSendRequest(request);
        }

        SerializationDiagnosticsContext serializationDiagnosticsContext = BridgeInternal.getSerializationDiagnosticsContext(request.requestContext.cosmosDiagnostics);
        if (serializationDiagnosticsContext != null) {
            serializationDiagnosticsContext.addSerializationDiagnostics(serializationDiagnostics);
        }

        Mono<Utils.ValueHolder<DocumentCollection>> collectionObs = this.collectionCache.resolveCollectionAsync(BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics), request);
        return addPartitionKeyInformation(request, content, document, options, collectionObs);
    }

    private Mono<RxDocumentServiceRequest> getBatchDocumentRequest(DocumentClientRetryPolicy requestRetryPolicy,
                                                                   String documentCollectionLink,
                                                                   ServerBatchRequest serverBatchRequest,
                                                                   RequestOptions options,
                                                                   boolean disableAutomaticIdGeneration) {

        checkArgument(StringUtils.isNotEmpty(documentCollectionLink), "expected non empty documentCollectionLink");
        checkNotNull(serverBatchRequest, "expected non null serverBatchRequest");

        Instant serializationStartTimeUTC = Instant.now();
        ByteBuffer content = ByteBuffer.wrap(Utils.getUTF8Bytes(serverBatchRequest.getRequestBody()));
        Instant serializationEndTimeUTC = Instant.now();

        SerializationDiagnosticsContext.SerializationDiagnostics serializationDiagnostics = new SerializationDiagnosticsContext.SerializationDiagnostics(
            serializationStartTimeUTC,
            serializationEndTimeUTC,
            SerializationDiagnosticsContext.SerializationType.ITEM_SERIALIZATION);

        String path = Utils.joinPath(documentCollectionLink, Paths.DOCUMENTS_PATH_SEGMENT);
        Map<String, String> requestHeaders = this.getRequestHeaders(options, ResourceType.Document, OperationType.Batch);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            this,
            OperationType.Batch,
            ResourceType.Document,
            path,
            requestHeaders,
            options,
            content);

        if (requestRetryPolicy != null) {
            requestRetryPolicy.onBeforeSendRequest(request);
        }

        SerializationDiagnosticsContext serializationDiagnosticsContext = BridgeInternal.getSerializationDiagnosticsContext(request.requestContext.cosmosDiagnostics);
        if (serializationDiagnosticsContext != null) {
            serializationDiagnosticsContext.addSerializationDiagnostics(serializationDiagnostics);
        }

        Mono<Utils.ValueHolder<DocumentCollection>> collectionObs =
            this.collectionCache.resolveCollectionAsync(BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics), request);

        return collectionObs.map((Utils.ValueHolder<DocumentCollection> collectionValueHolder) -> {
            addBatchHeaders(request, serverBatchRequest, collectionValueHolder.v);
            return request;
        });
    }

    private RxDocumentServiceRequest addBatchHeaders(RxDocumentServiceRequest request,
                                                     ServerBatchRequest serverBatchRequest,
                                                     DocumentCollection collection) {

        if(serverBatchRequest instanceof SinglePartitionKeyServerBatchRequest) {

            PartitionKey partitionKey = ((SinglePartitionKeyServerBatchRequest) serverBatchRequest).getPartitionKeyValue();
            PartitionKeyInternal partitionKeyInternal;

            if (partitionKey.equals(PartitionKey.NONE)) {
                PartitionKeyDefinition partitionKeyDefinition = collection.getPartitionKey();
                partitionKeyInternal = ModelBridgeInternal.getNonePartitionKey(partitionKeyDefinition);
            } else {
                // Partition key is always non-null
                partitionKeyInternal = BridgeInternal.getPartitionKeyInternal(partitionKey);
            }

            request.setPartitionKeyInternal(partitionKeyInternal);
            request.getHeaders().put(HttpConstants.HttpHeaders.PARTITION_KEY, Utils.escapeNonAscii(partitionKeyInternal.toJson()));
        } else if(serverBatchRequest instanceof PartitionKeyRangeServerBatchRequest) {
            request.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity(((PartitionKeyRangeServerBatchRequest) serverBatchRequest).getPartitionKeyRangeId()));
        } else {
            throw new UnsupportedOperationException("Unknown Server request.");
        }

        request.getHeaders().put(HttpConstants.HttpHeaders.IS_BATCH_REQUEST, Boolean.TRUE.toString());
        request.getHeaders().put(HttpConstants.HttpHeaders.IS_BATCH_ATOMIC, String.valueOf(serverBatchRequest.isAtomicBatch()));
        request.getHeaders().put(HttpConstants.HttpHeaders.SHOULD_BATCH_CONTINUE_ON_ERROR, String.valueOf(serverBatchRequest.isShouldContinueOnError()));

        request.setNumberOfItemsInBatchRequest(serverBatchRequest.getOperations().size());

        return request;
    }

    private Mono<RxDocumentServiceRequest> populateHeaders(RxDocumentServiceRequest request, RequestVerb httpMethod) {
        request.getHeaders().put(HttpConstants.HttpHeaders.X_DATE, Utils.nowAsRFC1123());
        if (this.masterKeyOrResourceToken != null || this.resourceTokensMap != null
            || this.cosmosAuthorizationTokenResolver != null || this.credential != null) {
            String resourceName = request.getResourceAddress();

            String authorization = this.getUserAuthorizationToken(
                    resourceName, request.getResourceType(), httpMethod, request.getHeaders(),
                    AuthorizationTokenType.PrimaryMasterKey, request.properties);
            try {
                authorization = URLEncoder.encode(authorization, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("Failed to encode authtoken.", e);
            }
            request.getHeaders().put(HttpConstants.HttpHeaders.AUTHORIZATION, authorization);
        }

        if ((RequestVerb.POST.equals(httpMethod) || RequestVerb.PUT.equals(httpMethod))
                && !request.getHeaders().containsKey(HttpConstants.HttpHeaders.CONTENT_TYPE)) {
            request.getHeaders().put(HttpConstants.HttpHeaders.CONTENT_TYPE, RuntimeConstants.MediaTypes.JSON);
        }

        if (RequestVerb.PATCH.equals(httpMethod) &&
            !request.getHeaders().containsKey(HttpConstants.HttpHeaders.CONTENT_TYPE)) {
            request.getHeaders().put(HttpConstants.HttpHeaders.CONTENT_TYPE, RuntimeConstants.MediaTypes.JSON_PATCH);
        }

        if (!request.getHeaders().containsKey(HttpConstants.HttpHeaders.ACCEPT)) {
            request.getHeaders().put(HttpConstants.HttpHeaders.ACCEPT, RuntimeConstants.MediaTypes.JSON);
        }

        MetadataDiagnosticsContext metadataDiagnosticsCtx =
            BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics);

        if (this.requiresFeedRangeFiltering(request)) {
            return request.getFeedRange()
                          .populateFeedRangeFilteringHeaders(
                              this.getPartitionKeyRangeCache(),
                              request,
                              this.collectionCache.resolveCollectionAsync(metadataDiagnosticsCtx, request))
                          .flatMap(this::populateAuthorizationHeader);
        }

        return this.populateAuthorizationHeader(request);
    }

    private boolean requiresFeedRangeFiltering(RxDocumentServiceRequest request) {
        if (request.getResourceType() != ResourceType.Document &&
                request.getResourceType() != ResourceType.Conflict) {
            return false;
        }

        switch (request.getOperationType()) {
            case ReadFeed:
            case Query:
            case SqlQuery:
                return request.getFeedRange() != null;
            default:
                return false;
        }
    }

    @Override
    public Mono<RxDocumentServiceRequest> populateAuthorizationHeader(RxDocumentServiceRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request");
        }

        if (this.authorizationTokenType == AuthorizationTokenType.AadToken) {
            return AadTokenAuthorizationHelper.getAuthorizationToken(this.tokenCredentialCache)
                .map(authorization -> {
                    request.getHeaders().put(HttpConstants.HttpHeaders.AUTHORIZATION, authorization);
                    return request;
                });
        } else {
            return Mono.just(request);
        }
    }

    @Override
    public Mono<HttpHeaders> populateAuthorizationHeader(HttpHeaders httpHeaders) {
        if (httpHeaders == null) {
            throw new IllegalArgumentException("httpHeaders");
        }

        if (this.authorizationTokenType == AuthorizationTokenType.AadToken) {
            return AadTokenAuthorizationHelper.getAuthorizationToken(this.tokenCredentialCache)
                .map(authorization -> {
                    httpHeaders.set(HttpConstants.HttpHeaders.AUTHORIZATION, authorization);
                    return httpHeaders;
                });
        }

        return Mono.just(httpHeaders);
    }

    @Override
    public AuthorizationTokenType getAuthorizationTokenType() {
        return this.authorizationTokenType;
    }

    @Override
    public String getUserAuthorizationToken(String resourceName,
                                            ResourceType resourceType,
                                            RequestVerb requestVerb,
                                            Map<String, String> headers,
                                            AuthorizationTokenType tokenType,
                                            Map<String, Object> properties) {

        if (this.cosmosAuthorizationTokenResolver != null) {
            return this.cosmosAuthorizationTokenResolver.getAuthorizationToken(requestVerb, resourceName, this.resolveCosmosResourceType(resourceType),
                    properties != null ? Collections.unmodifiableMap(properties) : null);
        } else if (credential != null) {
            return this.authorizationTokenProvider.generateKeyAuthorizationSignature(requestVerb, resourceName,
                    resourceType, headers);
        } else if (masterKeyOrResourceToken != null && hasAuthKeyResourceToken && resourceTokensMap == null) {
            return masterKeyOrResourceToken;
        } else {
            assert resourceTokensMap != null;
            if(resourceType.equals(ResourceType.DatabaseAccount)) {
                return this.firstResourceTokenFromPermissionFeed;
            }

            return ResourceTokenAuthorizationHelper.getAuthorizationTokenUsingResourceTokens(resourceTokensMap, requestVerb, resourceName, headers);
        }
    }

    private CosmosResourceType resolveCosmosResourceType(ResourceType resourceType) {
        CosmosResourceType cosmosResourceType =
            ModelBridgeInternal.fromServiceSerializedFormat(resourceType.toString());
        if (cosmosResourceType == null) {
            return CosmosResourceType.SYSTEM;
        }
        return cosmosResourceType;
    }

    void captureSessionToken(RxDocumentServiceRequest request, RxDocumentServiceResponse response) {
        this.sessionContainer.setSessionToken(request, response.getResponseHeaders());
    }

    private Mono<RxDocumentServiceResponse> create(RxDocumentServiceRequest request,
                                                   DocumentClientRetryPolicy documentClientRetryPolicy,
                                                   OperationContextAndListenerTuple operationContextAndListenerTuple) {
        return populateHeaders(request, RequestVerb.POST)
            .flatMap(requestPopulated -> {
                RxStoreModel storeProxy = this.getStoreProxy(requestPopulated);
                if (documentClientRetryPolicy.getRetryContext() != null && documentClientRetryPolicy.getRetryContext().getRetryCount() > 0) {
                    documentClientRetryPolicy.getRetryContext().updateEndTime();
                }

                return storeProxy.processMessage(requestPopulated, operationContextAndListenerTuple);
            });
    }

    private Mono<RxDocumentServiceResponse> upsert(RxDocumentServiceRequest request,
                                                   DocumentClientRetryPolicy documentClientRetryPolicy,
                                                   OperationContextAndListenerTuple operationContextAndListenerTuple) {

        return populateHeaders(request, RequestVerb.POST)
            .flatMap(requestPopulated -> {
                Map<String, String> headers = requestPopulated.getHeaders();
                // headers can never be null, since it will be initialized even when no
                // request options are specified,
                // hence using assertion here instead of exception, being in the private
                // method
                assert (headers != null);
                headers.put(HttpConstants.HttpHeaders.IS_UPSERT, "true");
                if (documentClientRetryPolicy.getRetryContext() != null && documentClientRetryPolicy.getRetryContext().getRetryCount() > 0) {
                    documentClientRetryPolicy.getRetryContext().updateEndTime();
                }

                return getStoreProxy(requestPopulated).processMessage(requestPopulated, operationContextAndListenerTuple)
                    .map(response -> {
                            this.captureSessionToken(requestPopulated, response);
                            return response;
                        }
                    );
            });
    }

    private Mono<RxDocumentServiceResponse> replace(RxDocumentServiceRequest request, DocumentClientRetryPolicy documentClientRetryPolicy) {
        return populateHeaders(request, RequestVerb.PUT)
            .flatMap(requestPopulated -> {
                if (documentClientRetryPolicy.getRetryContext() != null && documentClientRetryPolicy.getRetryContext().getRetryCount() > 0) {
                    documentClientRetryPolicy.getRetryContext().updateEndTime();
                }

                return getStoreProxy(requestPopulated).processMessage(requestPopulated);
            });
    }

    private Mono<RxDocumentServiceResponse> patch(RxDocumentServiceRequest request, DocumentClientRetryPolicy documentClientRetryPolicy) {
        populateHeaders(request, RequestVerb.PATCH);
        if (documentClientRetryPolicy.getRetryContext() != null && documentClientRetryPolicy.getRetryContext().getRetryCount() > 0) {
            documentClientRetryPolicy.getRetryContext().updateEndTime();
        }

        return getStoreProxy(request).processMessage(request);
    }

    @Override
    public Mono<ResourceResponse<Document>> createDocument(String collectionLink, Object document,
                                                           RequestOptions options, boolean disableAutomaticIdGeneration) {
        DocumentClientRetryPolicy requestRetryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        if (options == null || options.getPartitionKey() == null) {
            requestRetryPolicy = new PartitionKeyMismatchRetryPolicy(collectionCache, requestRetryPolicy, collectionLink, options);
        }

        DocumentClientRetryPolicy finalRetryPolicyInstance = requestRetryPolicy;
        return ObservableHelper.inlineIfPossibleAsObs(() -> createDocumentInternal(collectionLink, document, options, disableAutomaticIdGeneration, finalRetryPolicyInstance), requestRetryPolicy);
    }

    private Mono<ResourceResponse<Document>> createDocumentInternal(String collectionLink, Object document,
                                                                    RequestOptions options, boolean disableAutomaticIdGeneration, DocumentClientRetryPolicy requestRetryPolicy) {
        try {
            logger.debug("Creating a Document. collectionLink: [{}]", collectionLink);

            Mono<RxDocumentServiceRequest> requestObs = getCreateDocumentRequest(requestRetryPolicy, collectionLink, document,
                options, disableAutomaticIdGeneration, OperationType.Create);

            Mono<RxDocumentServiceResponse> responseObservable =
                requestObs.flatMap(request -> create(request, requestRetryPolicy, getOperationContextAndListenerTuple(options)));

            return responseObservable
                    .map(serviceResponse -> toResourceResponse(serviceResponse, Document.class));

        } catch (Exception e) {
            logger.debug("Failure in creating a document due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<Document>> upsertDocument(String collectionLink, Object document,
                                                                 RequestOptions options, boolean disableAutomaticIdGeneration) {
        DocumentClientRetryPolicy requestRetryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        if (options == null || options.getPartitionKey() == null) {
            requestRetryPolicy = new PartitionKeyMismatchRetryPolicy(collectionCache, requestRetryPolicy, collectionLink, options);
        }
        DocumentClientRetryPolicy finalRetryPolicyInstance = requestRetryPolicy;
        return ObservableHelper.inlineIfPossibleAsObs(() -> upsertDocumentInternal(collectionLink, document, options, disableAutomaticIdGeneration, finalRetryPolicyInstance), finalRetryPolicyInstance);
    }

    private Mono<ResourceResponse<Document>> upsertDocumentInternal(String collectionLink, Object document,
                                                                    RequestOptions options, boolean disableAutomaticIdGeneration, DocumentClientRetryPolicy retryPolicyInstance) {
        try {
            logger.debug("Upserting a Document. collectionLink: [{}]", collectionLink);

            Mono<RxDocumentServiceRequest> reqObs = getCreateDocumentRequest(retryPolicyInstance, collectionLink, document,
                options, disableAutomaticIdGeneration, OperationType.Upsert);

            Mono<RxDocumentServiceResponse> responseObservable = reqObs.flatMap(request -> upsert(request, retryPolicyInstance, getOperationContextAndListenerTuple(options)));

            return responseObservable
                    .map(serviceResponse -> toResourceResponse(serviceResponse, Document.class));
        } catch (Exception e) {
            logger.debug("Failure in upserting a document due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<Document>> replaceDocument(String documentLink, Object document,
                                                            RequestOptions options) {

        DocumentClientRetryPolicy requestRetryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        if (options == null || options.getPartitionKey() == null) {
            String collectionLink = Utils.getCollectionName(documentLink);
            requestRetryPolicy = new PartitionKeyMismatchRetryPolicy(collectionCache, requestRetryPolicy, collectionLink, options);
        }
        DocumentClientRetryPolicy finalRequestRetryPolicy = requestRetryPolicy;
        return ObservableHelper.inlineIfPossibleAsObs(() -> replaceDocumentInternal(documentLink, document, options, finalRequestRetryPolicy), requestRetryPolicy);
    }

    private Mono<ResourceResponse<Document>> replaceDocumentInternal(String documentLink, Object document,
                                                                     RequestOptions options, DocumentClientRetryPolicy retryPolicyInstance) {
        try {
            if (StringUtils.isEmpty(documentLink)) {
                throw new IllegalArgumentException("documentLink");
            }

            if (document == null) {
                throw new IllegalArgumentException("document");
            }

            Document typedDocument = documentFromObject(document, mapper);

            return this.replaceDocumentInternal(documentLink, typedDocument, options, retryPolicyInstance);

        } catch (Exception e) {
            logger.debug("Failure in replacing a document due to [{}]", e.getMessage());
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<Document>> replaceDocument(Document document, RequestOptions options) {
        DocumentClientRetryPolicy requestRetryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        if (options == null || options.getPartitionKey() == null) {
            String collectionLink = document.getSelfLink();
            requestRetryPolicy = new PartitionKeyMismatchRetryPolicy(collectionCache, requestRetryPolicy, collectionLink, options);
        }
        DocumentClientRetryPolicy finalRequestRetryPolicy = requestRetryPolicy;
        return ObservableHelper.inlineIfPossibleAsObs(() -> replaceDocumentInternal(document, options, finalRequestRetryPolicy), requestRetryPolicy);
    }

    private Mono<ResourceResponse<Document>> replaceDocumentInternal(Document document, RequestOptions options, DocumentClientRetryPolicy retryPolicyInstance) {

        try {
            if (document == null) {
                throw new IllegalArgumentException("document");
            }

            return this.replaceDocumentInternal(document.getSelfLink(), document, options, retryPolicyInstance);

        } catch (Exception e) {
            logger.debug("Failure in replacing a database due to [{}]", e.getMessage());
            return Mono.error(e);
        }
    }

    private Mono<ResourceResponse<Document>> replaceDocumentInternal(String documentLink,
                                                                     Document document,
                                                                     RequestOptions options,
                                                                     DocumentClientRetryPolicy retryPolicyInstance) {

        if (document == null) {
            throw new IllegalArgumentException("document");
        }

        logger.debug("Replacing a Document. documentLink: [{}]", documentLink);
        final String path = Utils.joinPath(documentLink, null);
        final Map<String, String> requestHeaders = getRequestHeaders(options, ResourceType.Document, OperationType.Replace);
        Instant serializationStartTimeUTC = Instant.now();
        ByteBuffer content = serializeJsonToByteBuffer(document);
        Instant serializationEndTime = Instant.now();
        SerializationDiagnosticsContext.SerializationDiagnostics serializationDiagnostics = new SerializationDiagnosticsContext.SerializationDiagnostics(
            serializationStartTimeUTC,
            serializationEndTime,
            SerializationDiagnosticsContext.SerializationType.ITEM_SERIALIZATION);

        final RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
            OperationType.Replace, ResourceType.Document, path, requestHeaders, options, content);
        if (retryPolicyInstance != null) {
            retryPolicyInstance.onBeforeSendRequest(request);
        }

        SerializationDiagnosticsContext serializationDiagnosticsContext = BridgeInternal.getSerializationDiagnosticsContext(request.requestContext.cosmosDiagnostics);
        if (serializationDiagnosticsContext != null) {
            serializationDiagnosticsContext.addSerializationDiagnostics(serializationDiagnostics);
        }

        Mono<Utils.ValueHolder<DocumentCollection>> collectionObs = collectionCache.resolveCollectionAsync(BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics), request);
        Mono<RxDocumentServiceRequest> requestObs = addPartitionKeyInformation(request, content, document, options, collectionObs);

        return requestObs.flatMap(req -> replace(request, retryPolicyInstance)
            .map(resp -> toResourceResponse(resp, Document.class)));
    }

    @Override
    public Mono<ResourceResponse<Document>> patchDocument(String documentLink,
                                                          CosmosPatchOperations cosmosPatchOperations,
                                                          RequestOptions options) {
        DocumentClientRetryPolicy documentClientRetryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> patchDocumentInternal(documentLink, cosmosPatchOperations, options, documentClientRetryPolicy), documentClientRetryPolicy);
    }

    private Mono<ResourceResponse<Document>> patchDocumentInternal(String documentLink,
                                                                   CosmosPatchOperations cosmosPatchOperations,
                                                                   RequestOptions options,
                                                                   DocumentClientRetryPolicy retryPolicyInstance) {

        checkArgument(StringUtils.isNotEmpty(documentLink), "expected non empty documentLink");
        checkNotNull(cosmosPatchOperations, "expected non null cosmosPatchOperations");

        logger.debug("Running patch operations on Document. documentLink: [{}]", documentLink);

        final String path = Utils.joinPath(documentLink, null);

        final Map<String, String> requestHeaders = getRequestHeaders(options, ResourceType.Document, OperationType.Patch);
        Instant serializationStartTimeUTC = Instant.now();

        ByteBuffer content = ByteBuffer.wrap(PatchUtil.serializeCosmosPatchToByteArray(cosmosPatchOperations, options));

        Instant serializationEndTime = Instant.now();
        SerializationDiagnosticsContext.SerializationDiagnostics serializationDiagnostics = new SerializationDiagnosticsContext.SerializationDiagnostics(
            serializationStartTimeUTC,
            serializationEndTime,
            SerializationDiagnosticsContext.SerializationType.ITEM_SERIALIZATION);

        final RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            this,
            OperationType.Patch,
            ResourceType.Document,
            path,
            requestHeaders,
            options,
            content);

        if (retryPolicyInstance != null) {
            retryPolicyInstance.onBeforeSendRequest(request);
        }

        SerializationDiagnosticsContext serializationDiagnosticsContext = BridgeInternal.getSerializationDiagnosticsContext(request.requestContext.cosmosDiagnostics);
        if (serializationDiagnosticsContext != null) {
            serializationDiagnosticsContext.addSerializationDiagnostics(serializationDiagnostics);
        }

        Mono<Utils.ValueHolder<DocumentCollection>> collectionObs = collectionCache.resolveCollectionAsync(
            BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics), request);

        // options will always have partition key info, so contentAsByteBuffer can be null and is not needed.
        Mono<RxDocumentServiceRequest> requestObs = addPartitionKeyInformation(
            request,
            null,
            null,
            options,
            collectionObs);

        return requestObs.flatMap(req -> patch(request, retryPolicyInstance)
            .map(resp -> toResourceResponse(resp, Document.class)));
    }

    @Override
    public Mono<ResourceResponse<Document>> deleteDocument(String documentLink, RequestOptions options) {
        DocumentClientRetryPolicy requestRetryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> deleteDocumentInternal(documentLink, null, options, requestRetryPolicy), requestRetryPolicy);
    }

    @Override
    public Mono<ResourceResponse<Document>> deleteDocument(String documentLink, InternalObjectNode internalObjectNode, RequestOptions options) {
        DocumentClientRetryPolicy requestRetryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> deleteDocumentInternal(documentLink, internalObjectNode, options, requestRetryPolicy),
            requestRetryPolicy);
    }

    private Mono<ResourceResponse<Document>> deleteDocumentInternal(String documentLink, InternalObjectNode internalObjectNode, RequestOptions options,
                                                                    DocumentClientRetryPolicy retryPolicyInstance) {
        try {
            if (StringUtils.isEmpty(documentLink)) {
                throw new IllegalArgumentException("documentLink");
            }

            logger.debug("Deleting a Document. documentLink: [{}]", documentLink);
            String path = Utils.joinPath(documentLink, null);
            Map<String, String> requestHeaders = this.getRequestHeaders(options, ResourceType.Document, OperationType.Delete);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Delete, ResourceType.Document, path, requestHeaders, options);
            if (retryPolicyInstance != null) {
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            Mono<Utils.ValueHolder<DocumentCollection>> collectionObs = collectionCache.resolveCollectionAsync(BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics), request);

            Mono<RxDocumentServiceRequest> requestObs = addPartitionKeyInformation(request, null, internalObjectNode, options, collectionObs);

            return requestObs.flatMap(req -> this
                .delete(req, retryPolicyInstance, getOperationContextAndListenerTuple(options))
                .map(serviceResponse -> toResourceResponse(serviceResponse, Document.class)));

        } catch (Exception e) {
            logger.debug("Failure in deleting a document due to [{}]", e.getMessage());
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<Document>> deleteAllDocumentsByPartitionKey(String collectionLink, PartitionKey partitionKey, RequestOptions options) {
        DocumentClientRetryPolicy requestRetryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> deleteAllDocumentsByPartitionKeyInternal(collectionLink, options, requestRetryPolicy),
            requestRetryPolicy);
    }

    private Mono<ResourceResponse<Document>> deleteAllDocumentsByPartitionKeyInternal(String collectionLink, RequestOptions options,
                                                                                  DocumentClientRetryPolicy retryPolicyInstance) {
        try {
            if (StringUtils.isEmpty(collectionLink)) {
                throw new IllegalArgumentException("collectionLink");
            }

            logger.debug("Deleting all items by Partition Key. collectionLink: [{}]", collectionLink);
            String path = Utils.joinPath(collectionLink, null);
            Map<String, String> requestHeaders = this.getRequestHeaders(options, ResourceType.PartitionKey, OperationType.Delete);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Delete, ResourceType.PartitionKey, path, requestHeaders, options);
            if (retryPolicyInstance != null) {
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            Mono<Utils.ValueHolder<DocumentCollection>> collectionObs = collectionCache.resolveCollectionAsync(BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics), request);

            Mono<RxDocumentServiceRequest> requestObs = addPartitionKeyInformation(request, null, null, options, collectionObs);

            return requestObs.flatMap(req -> this
                .deleteAllItemsByPartitionKey(req, retryPolicyInstance, getOperationContextAndListenerTuple(options))
                .map(serviceResponse -> toResourceResponse(serviceResponse, Document.class)));
        } catch (Exception e) {
            logger.debug("Failure in deleting a document due to [{}]", e.getMessage());
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<Document>> readDocument(String documentLink, RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> readDocumentInternal(documentLink, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<Document>> readDocumentInternal(String documentLink, RequestOptions options,
                                                                  DocumentClientRetryPolicy retryPolicyInstance) {
        try {
            if (StringUtils.isEmpty(documentLink)) {
                throw new IllegalArgumentException("documentLink");
            }

            logger.debug("Reading a Document. documentLink: [{}]", documentLink);
            String path = Utils.joinPath(documentLink, null);
            Map<String, String> requestHeaders = this.getRequestHeaders(options, ResourceType.Document, OperationType.Read);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Read, ResourceType.Document, path, requestHeaders, options);
            if (retryPolicyInstance != null) {
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            Mono<Utils.ValueHolder<DocumentCollection>> collectionObs = this.collectionCache.resolveCollectionAsync(BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics), request);

            Mono<RxDocumentServiceRequest> requestObs = addPartitionKeyInformation(request, null, null, options, collectionObs);

            return requestObs.flatMap(req -> {
                return this.read(request, retryPolicyInstance).map(serviceResponse -> toResourceResponse(serviceResponse, Document.class));
            });

        } catch (Exception e) {
            logger.debug("Failure in reading a document due to [{}]", e.getMessage());
            return Mono.error(e);
        }
    }

    @Override
    public Flux<FeedResponse<Document>> readDocuments(String collectionLink, CosmosQueryRequestOptions options) {

        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }

        return queryDocuments(collectionLink, "SELECT * FROM r", options);
    }

    @Override
    public <T> Mono<FeedResponse<T>> readMany(
        List<CosmosItemIdentity> itemIdentityList,
        String collectionLink,
        CosmosQueryRequestOptions options,
        Class<T> klass) {

        String resourceLink = parentResourceLinkToQueryLink(collectionLink, ResourceType.Document);
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
            OperationType.Query,
            ResourceType.Document,
            collectionLink, null
        );

        // This should not got to backend
        Mono<Utils.ValueHolder<DocumentCollection>> collectionObs =
            collectionCache.resolveCollectionAsync(null, request);

        return collectionObs
            .flatMap(documentCollectionResourceResponse -> {
                    final DocumentCollection collection = documentCollectionResourceResponse.v;
                    if (collection == null) {
                        throw new IllegalStateException("Collection cannot be null");
                    }

                    final PartitionKeyDefinition pkDefinition = collection.getPartitionKey();

                    Mono<Utils.ValueHolder<CollectionRoutingMap>> valueHolderMono = partitionKeyRangeCache
                        .tryLookupAsync(BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics),
                            collection.getResourceId(),
                            null,
                            null);
                    return valueHolderMono.flatMap(collectionRoutingMapValueHolder -> {
                        Map<PartitionKeyRange, List<CosmosItemIdentity>> partitionRangeItemKeyMap =
                            new HashMap<>();
                        CollectionRoutingMap routingMap = collectionRoutingMapValueHolder.v;
                        if (routingMap == null) {
                            throw new IllegalStateException("Failed to get routing map.");
                        }
                        itemIdentityList
                            .forEach(itemIdentity -> {

                                String effectivePartitionKeyString =  PartitionKeyInternalHelper
                                    .getEffectivePartitionKeyString(
                                        BridgeInternal.getPartitionKeyInternal(
                                            itemIdentity.getPartitionKey()),
                                        pkDefinition);

                                //use routing map to find the partitionKeyRangeId of each
                                // effectivePartitionKey
                                PartitionKeyRange range =
                                    routingMap.getRangeByEffectivePartitionKey(effectivePartitionKeyString);

                                //group the itemKeyList based on partitionKeyRangeId
                                if (partitionRangeItemKeyMap.get(range) == null) {
                                    List<CosmosItemIdentity> list = new ArrayList<>();
                                    list.add(itemIdentity);
                                    partitionRangeItemKeyMap.put(range, list);
                                } else {
                                    List<CosmosItemIdentity> pairs =
                                        partitionRangeItemKeyMap.get(range);
                                    pairs.add(itemIdentity);
                                    partitionRangeItemKeyMap.put(range, pairs);
                                }

                            });

                        //Create the range query map that contains the query to be run for that
                        // partitionkeyrange
                        Map<PartitionKeyRange, SqlQuerySpec> rangeQueryMap;
                        rangeQueryMap = getRangeQueryMap(partitionRangeItemKeyMap,
                            collection.getPartitionKey());

                        // create the executable query
                        return createReadManyQuery(
                            resourceLink,
                            new SqlQuerySpec(DUMMY_SQL_QUERY),
                            options,
                            Document.class,
                            ResourceType.Document,
                            collection,
                            Collections.unmodifiableMap(rangeQueryMap))
                            .collectList() // aggregating the result construct a FeedResponse and
                            // aggregate RUs.
                            .map(feedList -> {
                                List<T> finalList = new ArrayList<>();
                                HashMap<String, String> headers = new HashMap<>();
                                ConcurrentMap<String, QueryMetrics> aggregatedQueryMetrics = new ConcurrentHashMap<>();
                                double requestCharge = 0;
                                for (FeedResponse<Document> page : feedList) {
                                    ConcurrentMap<String, QueryMetrics> pageQueryMetrics =
                                        ModelBridgeInternal.queryMetrics(page);
                                    if (pageQueryMetrics != null) {
                                        pageQueryMetrics.forEach(
                                            aggregatedQueryMetrics::putIfAbsent);
                                    }

                                    requestCharge += page.getRequestCharge();
                                    // TODO: this does double serialization: FIXME
                                    finalList.addAll(page.getResults().stream().map(document ->
                                        ModelBridgeInternal.toObjectFromJsonSerializable(document, klass)).collect(Collectors.toList()));
                                }
                                headers.put(HttpConstants.HttpHeaders.REQUEST_CHARGE, Double
                                    .toString(requestCharge));
                                FeedResponse<T> frp = BridgeInternal
                                    .createFeedResponse(finalList, headers);
                                return frp;
                            });
                    });
                }
            );

    }

    private Map<PartitionKeyRange, SqlQuerySpec> getRangeQueryMap(
        Map<PartitionKeyRange, List<CosmosItemIdentity>> partitionRangeItemKeyMap,
        PartitionKeyDefinition partitionKeyDefinition) {
        //TODO: Optimise this to include all types of partitionkeydefinitions. ex: c["prop1./ab"]["key1"]

        Map<PartitionKeyRange, SqlQuerySpec> rangeQueryMap = new HashMap<>();
        String partitionKeySelector = createPkSelector(partitionKeyDefinition);

        for(Map.Entry<PartitionKeyRange, List<CosmosItemIdentity>> entry: partitionRangeItemKeyMap.entrySet()) {

            SqlQuerySpec sqlQuerySpec;
            if (partitionKeySelector.equals("[\"id\"]")) {
                sqlQuerySpec = createReadManyQuerySpecPartitionKeyIdSame(entry.getValue(), partitionKeySelector);
            } else {
                sqlQuerySpec = createReadManyQuerySpec(entry.getValue(), partitionKeySelector);
            }
            // Add query for this partition to rangeQueryMap
            rangeQueryMap.put(entry.getKey(), sqlQuerySpec);

        }

        return rangeQueryMap;
    }

    private SqlQuerySpec createReadManyQuerySpecPartitionKeyIdSame(
        List<CosmosItemIdentity> idPartitionKeyPairList,
        String partitionKeySelector) {

        StringBuilder queryStringBuilder = new StringBuilder();
        List<SqlParameter> parameters = new ArrayList<>();

        queryStringBuilder.append("SELECT * FROM c WHERE c.id IN ( ");
        for (int i = 0; i < idPartitionKeyPairList.size(); i++) {
            CosmosItemIdentity itemIdentity = idPartitionKeyPairList.get(i);

            String idValue = itemIdentity.getId();
            String idParamName = "@param" + i;

            PartitionKey pkValueAsPartitionKey = itemIdentity.getPartitionKey();
            Object pkValue = ModelBridgeInternal.getPartitionKeyObject(pkValueAsPartitionKey);

            if (!Objects.equals(idValue, pkValue)) {
                // this is sanity check to ensure id and pk are the same
                continue;
            }

            parameters.add(new SqlParameter(idParamName, idValue));
            queryStringBuilder.append(idParamName);

            if (i < idPartitionKeyPairList.size() - 1) {
                queryStringBuilder.append(", ");
            }
        }
        queryStringBuilder.append(" )");

        return new SqlQuerySpec(queryStringBuilder.toString(), parameters);
    }

    private SqlQuerySpec createReadManyQuerySpec(List<CosmosItemIdentity> itemIdentities, String partitionKeySelector) {
        StringBuilder queryStringBuilder = new StringBuilder();
        List<SqlParameter> parameters = new ArrayList<>();

        queryStringBuilder.append("SELECT * FROM c WHERE ( ");
        for (int i = 0; i < itemIdentities.size(); i++) {
            CosmosItemIdentity itemIdentity = itemIdentities.get(i);

            PartitionKey pkValueAsPartitionKey = itemIdentity.getPartitionKey();
            Object pkValue = ModelBridgeInternal.getPartitionKeyObject(pkValueAsPartitionKey);
            String pkParamName = "@param" + (2 * i);
            parameters.add(new SqlParameter(pkParamName, pkValue));

            String idValue = itemIdentity.getId();
            String idParamName = "@param" + (2 * i + 1);
            parameters.add(new SqlParameter(idParamName, idValue));

            queryStringBuilder.append("(");
            queryStringBuilder.append("c.id = ");
            queryStringBuilder.append(idParamName);
            queryStringBuilder.append(" AND ");
            queryStringBuilder.append(" c");
            // partition key def
            queryStringBuilder.append(partitionKeySelector);
            queryStringBuilder.append((" = "));
            queryStringBuilder.append(pkParamName);
            queryStringBuilder.append(" )");

            if (i < itemIdentities.size() - 1) {
                queryStringBuilder.append(" OR ");
            }
        }
        queryStringBuilder.append(" )");

        return new SqlQuerySpec(queryStringBuilder.toString(), parameters);
    }

    private String createPkSelector(PartitionKeyDefinition partitionKeyDefinition) {
        return partitionKeyDefinition.getPaths()
            .stream()
            .map(pathPart -> StringUtils.substring(pathPart, 1)) // skip starting /
            .map(pathPart -> StringUtils.replace(pathPart, "\"", "\\")) // escape quote
            .map(part -> "[\"" + part + "\"]")
            .collect(Collectors.joining());
    }

    private <T extends Resource> Flux<FeedResponse<T>> createReadManyQuery(
        String parentResourceLink,
        SqlQuerySpec sqlQuery,
        CosmosQueryRequestOptions options,
        Class<T> klass,
        ResourceType resourceTypeEnum,
        DocumentCollection collection,
        Map<PartitionKeyRange, SqlQuerySpec> rangeQueryMap) {

        UUID activityId = Utils.randomUUID();
        IDocumentQueryClient queryClient = documentQueryClientImpl(RxDocumentClientImpl.this, getOperationContextAndListenerTuple(options));
        Flux<? extends IDocumentQueryExecutionContext<T>> executionContext =
            DocumentQueryExecutionContextFactory.createReadManyQueryAsync(this, queryClient, collection.getResourceId(),
                                                                          sqlQuery,
                                                                          rangeQueryMap,
                                                                          options,
                                                                          collection.getResourceId(),
                                                                          parentResourceLink,
                                                                          activityId,
                                                                          klass,
                                                                          resourceTypeEnum);
        return executionContext.flatMap(IDocumentQueryExecutionContext<T>::executeAsync);
    }

    @Override
    public Flux<FeedResponse<Document>> queryDocuments(String collectionLink, String query, CosmosQueryRequestOptions options) {
        return queryDocuments(collectionLink, new SqlQuerySpec(query), options);
    }

    private IDocumentQueryClient documentQueryClientImpl(RxDocumentClientImpl rxDocumentClientImpl, OperationContextAndListenerTuple operationContextAndListenerTuple) {

        return new IDocumentQueryClient () {

            @Override
            public RxCollectionCache getCollectionCache() {
                return RxDocumentClientImpl.this.collectionCache;
            }

            @Override
            public RxPartitionKeyRangeCache getPartitionKeyRangeCache() {
                return RxDocumentClientImpl.this.partitionKeyRangeCache;
            }

            @Override
            public IRetryPolicyFactory getResetSessionTokenRetryPolicy() {
                return RxDocumentClientImpl.this.resetSessionTokenRetryPolicy;
            }

            @Override
            public ConsistencyLevel getDefaultConsistencyLevelAsync() {
                return RxDocumentClientImpl.this.gatewayConfigurationReader.getDefaultConsistencyLevel();
            }

            @Override
            public ConsistencyLevel getDesiredConsistencyLevelAsync() {
                // TODO Auto-generated method stub
                return RxDocumentClientImpl.this.consistencyLevel;
            }

            @Override
            public Mono<RxDocumentServiceResponse> executeQueryAsync(RxDocumentServiceRequest request) {
                if (operationContextAndListenerTuple == null) {
                    return RxDocumentClientImpl.this.query(request).single();
                } else {
                    final OperationListener listener =
                        operationContextAndListenerTuple.getOperationListener();
                    final OperationContext operationContext = operationContextAndListenerTuple.getOperationContext();
                    request.getHeaders().put(HttpConstants.HttpHeaders.CORRELATED_ACTIVITY_ID, operationContext.getCorrelationActivityId());
                    listener.requestListener(operationContext, request);

                    return RxDocumentClientImpl.this.query(request).single().doOnNext(
                        response -> listener.responseListener(operationContext, response)
                    ).doOnError(
                        ex -> listener.exceptionListener(operationContext, ex)
                    );
                }
            }

            @Override
            public QueryCompatibilityMode getQueryCompatibilityMode() {
                // TODO Auto-generated method stub
                return QueryCompatibilityMode.Default;
            }

            @Override
            public Mono<RxDocumentServiceResponse> readFeedAsync(RxDocumentServiceRequest request) {
                // TODO Auto-generated method stub
                return null;
            }
        };
    }

    @Override
    public Flux<FeedResponse<Document>> queryDocuments(String collectionLink, SqlQuerySpec querySpec,
                                                             CosmosQueryRequestOptions options) {
        SqlQuerySpecLogger.getInstance().logQuery(querySpec);
        return createQuery(collectionLink, querySpec, options, Document.class, ResourceType.Document);
    }

    @Override
    public Flux<FeedResponse<Document>> queryDocumentChangeFeed(
        final DocumentCollection collection,
        final CosmosChangeFeedRequestOptions changeFeedOptions) {

        checkNotNull(collection, "Argument 'collection' must not be null.");

        ChangeFeedQueryImpl<Document> changeFeedQueryImpl = new ChangeFeedQueryImpl<>(
            this,
            ResourceType.Document,
            Document.class,
            collection.getAltLink(),
            collection.getResourceId(),
            changeFeedOptions);

        return changeFeedQueryImpl.executeAsync();
    }

    @Override
    public Flux<FeedResponse<Document>> readAllDocuments(
        String collectionLink,
        PartitionKey partitionKey,
        CosmosQueryRequestOptions options) {

        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }

        if (partitionKey == null) {
            throw new IllegalArgumentException("partitionKey");
        }

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
            OperationType.Query,
            ResourceType.Document,
            collectionLink,
            null
        );

        // This should not got to backend
        Flux<Utils.ValueHolder<DocumentCollection>> collectionObs =
            collectionCache.resolveCollectionAsync(null, request).flux();

        return collectionObs.flatMap(documentCollectionResourceResponse -> {

            DocumentCollection collection = documentCollectionResourceResponse.v;
            if (collection == null) {
                throw new IllegalStateException("Collection cannot be null");
            }

            PartitionKeyDefinition pkDefinition = collection.getPartitionKey();
            String pkSelector = createPkSelector(pkDefinition);
            SqlQuerySpec querySpec = createLogicalPartitionScanQuerySpec(partitionKey, pkSelector);

            String resourceLink = parentResourceLinkToQueryLink(collectionLink, ResourceType.Document);
            UUID activityId = Utils.randomUUID();
            IDocumentQueryClient queryClient = documentQueryClientImpl(RxDocumentClientImpl.this, getOperationContextAndListenerTuple(options));

            final CosmosQueryRequestOptions effectiveOptions =
                ModelBridgeInternal.createQueryRequestOptions(options);

            // Trying to put this logic as low as the query pipeline
            // Since for parallelQuery, each partition will have its own request, so at this point, there will be no request associate with this retry policy.
            // For default document context, it already wired up InvalidPartitionExceptionRetry, but there is no harm to wire it again here
            InvalidPartitionExceptionRetryPolicy invalidPartitionExceptionRetryPolicy = new InvalidPartitionExceptionRetryPolicy(
                this.collectionCache,
                null,
                resourceLink,
                ModelBridgeInternal.getPropertiesFromQueryRequestOptions(effectiveOptions));

            return ObservableHelper.fluxInlineIfPossibleAsObs(
                () -> {
                    Flux<Utils.ValueHolder<CollectionRoutingMap>> valueHolderMono = this.partitionKeyRangeCache
                        .tryLookupAsync(
                            BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics),
                            collection.getResourceId(),
                            null,
                            null).flux();
                    return valueHolderMono.flatMap(collectionRoutingMapValueHolder -> {

                        CollectionRoutingMap routingMap = collectionRoutingMapValueHolder.v;
                        if (routingMap == null) {
                            throw new IllegalStateException("Failed to get routing map.");
                        }

                        String effectivePartitionKeyString = PartitionKeyInternalHelper
                            .getEffectivePartitionKeyString(
                                BridgeInternal.getPartitionKeyInternal(partitionKey),
                                pkDefinition);

                        //use routing map to find the partitionKeyRangeId of each
                        // effectivePartitionKey
                        PartitionKeyRange range =
                            routingMap.getRangeByEffectivePartitionKey(effectivePartitionKeyString);

                        return createQueryInternal(
                            resourceLink,
                            querySpec,
                            ModelBridgeInternal.setPartitionKeyRangeIdInternal(effectiveOptions, range.getId()),
                            Document.class,
                            ResourceType.Document,
                            queryClient,
                            activityId);
                    });
                },
                invalidPartitionExceptionRetryPolicy);
        });
    }

    @Override
    public ConcurrentMap<String, PartitionedQueryExecutionInfo> getQueryPlanCache() {
        return queryPlanCache;
    }

    @Override
    public Flux<FeedResponse<PartitionKeyRange>> readPartitionKeyRanges(final String collectionLink,
                                                                              CosmosQueryRequestOptions options) {

        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }

        return readFeed(options, ResourceType.PartitionKeyRange, PartitionKeyRange.class,
                Utils.joinPath(collectionLink, Paths.PARTITION_KEY_RANGES_PATH_SEGMENT));
    }

    private RxDocumentServiceRequest getStoredProcedureRequest(String collectionLink, StoredProcedure storedProcedure,
                                                               RequestOptions options, OperationType operationType) {
        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }
        if (storedProcedure == null) {
            throw new IllegalArgumentException("storedProcedure");
        }

        validateResource(storedProcedure);

        String path = Utils.joinPath(collectionLink, Paths.STORED_PROCEDURES_PATH_SEGMENT);
        Map<String, String> requestHeaders = this.getRequestHeaders(options, ResourceType.StoredProcedure, operationType);
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this, operationType,
            ResourceType.StoredProcedure, path, storedProcedure, requestHeaders, options);

        return request;
    }

    private RxDocumentServiceRequest getUserDefinedFunctionRequest(String collectionLink, UserDefinedFunction udf,
                                                                   RequestOptions options, OperationType operationType) {
        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }
        if (udf == null) {
            throw new IllegalArgumentException("udf");
        }

        validateResource(udf);

        String path = Utils.joinPath(collectionLink, Paths.USER_DEFINED_FUNCTIONS_PATH_SEGMENT);
        Map<String, String> requestHeaders = this.getRequestHeaders(options, ResourceType.UserDefinedFunction, operationType);
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
            operationType, ResourceType.UserDefinedFunction, path, udf, requestHeaders, options);

        return request;
    }

    @Override
    public Mono<ResourceResponse<StoredProcedure>> createStoredProcedure(String collectionLink,
                                                                               StoredProcedure storedProcedure, RequestOptions options) {
        DocumentClientRetryPolicy requestRetryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> createStoredProcedureInternal(collectionLink, storedProcedure, options, requestRetryPolicy), requestRetryPolicy);
    }

    private Mono<ResourceResponse<StoredProcedure>> createStoredProcedureInternal(String collectionLink,
                                                                                        StoredProcedure storedProcedure, RequestOptions options, DocumentClientRetryPolicy retryPolicyInstance) {
        // we are using an observable factory here
        // observable will be created fresh upon subscription
        // this is to ensure we capture most up to date information (e.g.,
        // session)
        try {

            logger.debug("Creating a StoredProcedure. collectionLink: [{}], storedProcedure id [{}]",
                    collectionLink, storedProcedure.getId());
            RxDocumentServiceRequest request = getStoredProcedureRequest(collectionLink, storedProcedure, options,
                    OperationType.Create);
            if (retryPolicyInstance != null) {
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            return this.create(request, retryPolicyInstance, getOperationContextAndListenerTuple(options)).map(response -> toResourceResponse(response, StoredProcedure.class));

        } catch (Exception e) {
            // this is only in trace level to capture what's going on
            logger.debug("Failure in creating a StoredProcedure due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<StoredProcedure>> upsertStoredProcedure(String collectionLink,
                                                                               StoredProcedure storedProcedure, RequestOptions options) {
        DocumentClientRetryPolicy requestRetryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> upsertStoredProcedureInternal(collectionLink, storedProcedure, options, requestRetryPolicy), requestRetryPolicy);
    }

    private Mono<ResourceResponse<StoredProcedure>> upsertStoredProcedureInternal(String collectionLink,
                                                                                        StoredProcedure storedProcedure, RequestOptions options, DocumentClientRetryPolicy retryPolicyInstance) {
        // we are using an observable factory here
        // observable will be created fresh upon subscription
        // this is to ensure we capture most up to date information (e.g.,
        // session)
        try {

            logger.debug("Upserting a StoredProcedure. collectionLink: [{}], storedProcedure id [{}]",
                    collectionLink, storedProcedure.getId());
            RxDocumentServiceRequest request = getStoredProcedureRequest(collectionLink, storedProcedure, options,
                    OperationType.Upsert);
            if (retryPolicyInstance != null) {
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            return this.upsert(request, retryPolicyInstance, getOperationContextAndListenerTuple(options)).map(response -> toResourceResponse(response, StoredProcedure.class));

        } catch (Exception e) {
            // this is only in trace level to capture what's going on
            logger.debug("Failure in upserting a StoredProcedure due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<StoredProcedure>> replaceStoredProcedure(StoredProcedure storedProcedure,
                                                                                RequestOptions options) {
        DocumentClientRetryPolicy requestRetryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> replaceStoredProcedureInternal(storedProcedure, options, requestRetryPolicy), requestRetryPolicy);
    }

    private Mono<ResourceResponse<StoredProcedure>> replaceStoredProcedureInternal(StoredProcedure storedProcedure,
                                                                                         RequestOptions options, DocumentClientRetryPolicy retryPolicyInstance) {
        try {

            if (storedProcedure == null) {
                throw new IllegalArgumentException("storedProcedure");
            }
            logger.debug("Replacing a StoredProcedure. storedProcedure id [{}]", storedProcedure.getId());

            RxDocumentClientImpl.validateResource(storedProcedure);

            String path = Utils.joinPath(storedProcedure.getSelfLink(), null);
            Map<String, String> requestHeaders = getRequestHeaders(options, ResourceType.StoredProcedure, OperationType.Replace);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Replace, ResourceType.StoredProcedure, path, storedProcedure, requestHeaders, options);

            if (retryPolicyInstance != null) {
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            return this.replace(request, retryPolicyInstance).map(response -> toResourceResponse(response, StoredProcedure.class));

        } catch (Exception e) {
            logger.debug("Failure in replacing a StoredProcedure due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<StoredProcedure>> deleteStoredProcedure(String storedProcedureLink,
                                                                               RequestOptions options) {
        DocumentClientRetryPolicy requestRetryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> deleteStoredProcedureInternal(storedProcedureLink, options, requestRetryPolicy), requestRetryPolicy);
    }

    private Mono<ResourceResponse<StoredProcedure>> deleteStoredProcedureInternal(String storedProcedureLink,
                                                                                        RequestOptions options, DocumentClientRetryPolicy retryPolicyInstance) {
        // we are using an observable factory here
        // observable will be created fresh upon subscription
        // this is to ensure we capture most up to date information (e.g.,
        // session)
        try {

            if (StringUtils.isEmpty(storedProcedureLink)) {
                throw new IllegalArgumentException("storedProcedureLink");
            }

            logger.debug("Deleting a StoredProcedure. storedProcedureLink [{}]", storedProcedureLink);
            String path = Utils.joinPath(storedProcedureLink, null);
            Map<String, String> requestHeaders = this.getRequestHeaders(options, ResourceType.StoredProcedure, OperationType.Delete);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Delete, ResourceType.StoredProcedure, path, requestHeaders, options);

            if (retryPolicyInstance != null) {
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            return this.delete(request, retryPolicyInstance, getOperationContextAndListenerTuple(options)).map(response -> toResourceResponse(response, StoredProcedure.class));

        } catch (Exception e) {
            // this is only in trace level to capture what's going on
            logger.debug("Failure in deleting a StoredProcedure due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<StoredProcedure>> readStoredProcedure(String storedProcedureLink,
                                                                             RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> readStoredProcedureInternal(storedProcedureLink, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<StoredProcedure>> readStoredProcedureInternal(String storedProcedureLink,
                                                                                      RequestOptions options, DocumentClientRetryPolicy retryPolicyInstance) {

        // we are using an observable factory here
        // observable will be created fresh upon subscription
        // this is to ensure we capture most up to date information (e.g.,
        // session)
        try {

            if (StringUtils.isEmpty(storedProcedureLink)) {
                throw new IllegalArgumentException("storedProcedureLink");
            }

            logger.debug("Reading a StoredProcedure. storedProcedureLink [{}]", storedProcedureLink);
            String path = Utils.joinPath(storedProcedureLink, null);
            Map<String, String> requestHeaders = this.getRequestHeaders(options, ResourceType.StoredProcedure, OperationType.Read);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Read, ResourceType.StoredProcedure, path, requestHeaders, options);

            if (retryPolicyInstance != null){
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            return this.read(request, retryPolicyInstance).map(response -> toResourceResponse(response, StoredProcedure.class));

        } catch (Exception e) {
            // this is only in trace level to capture what's going on
            logger.debug("Failure in reading a StoredProcedure due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Flux<FeedResponse<StoredProcedure>> readStoredProcedures(String collectionLink,
                                                                          CosmosQueryRequestOptions options) {

        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }

        return readFeed(options, ResourceType.StoredProcedure, StoredProcedure.class,
                Utils.joinPath(collectionLink, Paths.STORED_PROCEDURES_PATH_SEGMENT));
    }

    @Override
    public Flux<FeedResponse<StoredProcedure>> queryStoredProcedures(String collectionLink, String query,
                                                                           CosmosQueryRequestOptions options) {
        return queryStoredProcedures(collectionLink, new SqlQuerySpec(query), options);
    }

    @Override
    public Flux<FeedResponse<StoredProcedure>> queryStoredProcedures(String collectionLink,
                                                                           SqlQuerySpec querySpec, CosmosQueryRequestOptions options) {
        return createQuery(collectionLink, querySpec, options, StoredProcedure.class, ResourceType.StoredProcedure);
    }

    @Override
    public Mono<StoredProcedureResponse> executeStoredProcedure(String storedProcedureLink,
                                                                      List<Object> procedureParams) {
        return this.executeStoredProcedure(storedProcedureLink, null, procedureParams);
    }

    @Override
    public Mono<StoredProcedureResponse> executeStoredProcedure(String storedProcedureLink,
                                                                      RequestOptions options, List<Object> procedureParams) {
        DocumentClientRetryPolicy documentClientRetryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> executeStoredProcedureInternal(storedProcedureLink, options, procedureParams, documentClientRetryPolicy), documentClientRetryPolicy);
    }

    @Override
    public Mono<CosmosBatchResponse> executeBatchRequest(String collectionLink,
                                                         ServerBatchRequest serverBatchRequest,
                                                         RequestOptions options,
                                                         boolean disableAutomaticIdGeneration) {
        DocumentClientRetryPolicy documentClientRetryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> executeBatchRequestInternal(collectionLink, serverBatchRequest, options, documentClientRetryPolicy, disableAutomaticIdGeneration), documentClientRetryPolicy);
    }

    private Mono<StoredProcedureResponse> executeStoredProcedureInternal(String storedProcedureLink,
                                                                               RequestOptions options, List<Object> procedureParams, DocumentClientRetryPolicy retryPolicy) {

        try {
            logger.debug("Executing a StoredProcedure. storedProcedureLink [{}]", storedProcedureLink);
            String path = Utils.joinPath(storedProcedureLink, null);

            Map<String, String> requestHeaders = getRequestHeaders(options, ResourceType.StoredProcedure, OperationType.ExecuteJavaScript);
            requestHeaders.put(HttpConstants.HttpHeaders.ACCEPT, RuntimeConstants.MediaTypes.JSON);

            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                    OperationType.ExecuteJavaScript,
                    ResourceType.StoredProcedure, path,
                    procedureParams != null && !procedureParams.isEmpty() ? RxDocumentClientImpl.serializeProcedureParams(procedureParams) : "",
                    requestHeaders, options);

            if (retryPolicy != null) {
                retryPolicy.onBeforeSendRequest(request);
            }

            Mono<RxDocumentServiceRequest> reqObs = addPartitionKeyInformation(request, null, null, options);
            return reqObs.flatMap(req -> create(request, retryPolicy, getOperationContextAndListenerTuple(options))
                    .map(response -> {
                        this.captureSessionToken(request, response);
                        return toStoredProcedureResponse(response);
                    }));

        } catch (Exception e) {
            logger.debug("Failure in executing a StoredProcedure due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    private Mono<CosmosBatchResponse> executeBatchRequestInternal(String collectionLink,
                                                                         ServerBatchRequest serverBatchRequest,
                                                                         RequestOptions options,
                                                                         DocumentClientRetryPolicy requestRetryPolicy,
                                                                         boolean disableAutomaticIdGeneration) {

        try {
            logger.debug("Executing a Batch request with number of operations {}", serverBatchRequest.getOperations().size());

            Mono<RxDocumentServiceRequest> requestObs = getBatchDocumentRequest(requestRetryPolicy, collectionLink, serverBatchRequest, options, disableAutomaticIdGeneration);
            Mono<RxDocumentServiceResponse> responseObservable =
                requestObs.flatMap(request -> create(request, requestRetryPolicy, getOperationContextAndListenerTuple(options)));

            return responseObservable
                .map(serviceResponse -> BatchResponseParser.fromDocumentServiceResponse(serviceResponse, serverBatchRequest, true));

        } catch (Exception ex) {
            logger.debug("Failure in executing a batch due to [{}]", ex.getMessage(), ex);
            return Mono.error(ex);
        }
    }

    @Override
    public Mono<ResourceResponse<Trigger>> createTrigger(String collectionLink, Trigger trigger,
                                                               RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> createTriggerInternal(collectionLink, trigger, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<Trigger>> createTriggerInternal(String collectionLink, Trigger trigger,
                                                                        RequestOptions options, DocumentClientRetryPolicy retryPolicyInstance) {
        try {

            logger.debug("Creating a Trigger. collectionLink [{}], trigger id [{}]", collectionLink,
                    trigger.getId());
            RxDocumentServiceRequest request = getTriggerRequest(collectionLink, trigger, options,
                    OperationType.Create);
            if (retryPolicyInstance != null){
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            return this.create(request, retryPolicyInstance, getOperationContextAndListenerTuple(options)).map(response -> toResourceResponse(response, Trigger.class));

        } catch (Exception e) {
            logger.debug("Failure in creating a Trigger due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<Trigger>> upsertTrigger(String collectionLink, Trigger trigger,
                                                               RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> upsertTriggerInternal(collectionLink, trigger, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<Trigger>> upsertTriggerInternal(String collectionLink, Trigger trigger,
                                                                        RequestOptions options, DocumentClientRetryPolicy retryPolicyInstance) {
        try {

            logger.debug("Upserting a Trigger. collectionLink [{}], trigger id [{}]", collectionLink,
                    trigger.getId());
            RxDocumentServiceRequest request = getTriggerRequest(collectionLink, trigger, options,
                    OperationType.Upsert);
            if (retryPolicyInstance != null){
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            return this.upsert(request, retryPolicyInstance, getOperationContextAndListenerTuple(options)).map(response -> toResourceResponse(response, Trigger.class));

        } catch (Exception e) {
            logger.debug("Failure in upserting a Trigger due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    private RxDocumentServiceRequest getTriggerRequest(String collectionLink, Trigger trigger, RequestOptions options,
                                                       OperationType operationType) {
        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }
        if (trigger == null) {
            throw new IllegalArgumentException("trigger");
        }

        RxDocumentClientImpl.validateResource(trigger);

        String path = Utils.joinPath(collectionLink, Paths.TRIGGERS_PATH_SEGMENT);
        Map<String, String> requestHeaders = getRequestHeaders(options, ResourceType.Trigger, operationType);
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this, operationType, ResourceType.Trigger, path,
                trigger, requestHeaders, options);

        return request;
    }

    @Override
    public Mono<ResourceResponse<Trigger>> replaceTrigger(Trigger trigger, RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> replaceTriggerInternal(trigger, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<Trigger>> replaceTriggerInternal(Trigger trigger, RequestOptions options,
                                                                         DocumentClientRetryPolicy retryPolicyInstance) {

        try {
            if (trigger == null) {
                throw new IllegalArgumentException("trigger");
            }

            logger.debug("Replacing a Trigger. trigger id [{}]", trigger.getId());
            RxDocumentClientImpl.validateResource(trigger);

            String path = Utils.joinPath(trigger.getSelfLink(), null);
            Map<String, String> requestHeaders = getRequestHeaders(options, ResourceType.Trigger, OperationType.Replace);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Replace, ResourceType.Trigger, path, trigger, requestHeaders, options);

            if (retryPolicyInstance != null){
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            return this.replace(request, retryPolicyInstance).map(response -> toResourceResponse(response, Trigger.class));

        } catch (Exception e) {
            logger.debug("Failure in replacing a Trigger due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<Trigger>> deleteTrigger(String triggerLink, RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> deleteTriggerInternal(triggerLink, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<Trigger>> deleteTriggerInternal(String triggerLink, RequestOptions options, DocumentClientRetryPolicy retryPolicyInstance) {
        try {
            if (StringUtils.isEmpty(triggerLink)) {
                throw new IllegalArgumentException("triggerLink");
            }

            logger.debug("Deleting a Trigger. triggerLink [{}]", triggerLink);
            String path = Utils.joinPath(triggerLink, null);
            Map<String, String> requestHeaders = getRequestHeaders(options, ResourceType.Trigger, OperationType.Delete);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Delete, ResourceType.Trigger, path, requestHeaders, options);

            if (retryPolicyInstance != null){
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            return this.delete(request, retryPolicyInstance, getOperationContextAndListenerTuple(options)).map(response -> toResourceResponse(response, Trigger.class));

        } catch (Exception e) {
            logger.debug("Failure in deleting a Trigger due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<Trigger>> readTrigger(String triggerLink, RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> readTriggerInternal(triggerLink, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<Trigger>> readTriggerInternal(String triggerLink, RequestOptions options,
                                                                      DocumentClientRetryPolicy retryPolicyInstance) {
        try {
            if (StringUtils.isEmpty(triggerLink)) {
                throw new IllegalArgumentException("triggerLink");
            }

            logger.debug("Reading a Trigger. triggerLink [{}]", triggerLink);
            String path = Utils.joinPath(triggerLink, null);
            Map<String, String> requestHeaders = getRequestHeaders(options, ResourceType.Trigger, OperationType.Read);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Read, ResourceType.Trigger, path, requestHeaders, options);

            if (retryPolicyInstance != null){
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            return this.read(request, retryPolicyInstance).map(response -> toResourceResponse(response, Trigger.class));

        } catch (Exception e) {
            logger.debug("Failure in reading a Trigger due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Flux<FeedResponse<Trigger>> readTriggers(String collectionLink, CosmosQueryRequestOptions options) {

        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }

        return readFeed(options, ResourceType.Trigger, Trigger.class,
                Utils.joinPath(collectionLink, Paths.TRIGGERS_PATH_SEGMENT));
    }

    @Override
    public Flux<FeedResponse<Trigger>> queryTriggers(String collectionLink, String query,
                                                           CosmosQueryRequestOptions options) {
        return queryTriggers(collectionLink, new SqlQuerySpec(query), options);
    }

    @Override
    public Flux<FeedResponse<Trigger>> queryTriggers(String collectionLink, SqlQuerySpec querySpec,
                                                           CosmosQueryRequestOptions options) {
        return createQuery(collectionLink, querySpec, options, Trigger.class, ResourceType.Trigger);
    }

    @Override
    public Mono<ResourceResponse<UserDefinedFunction>> createUserDefinedFunction(String collectionLink,
                                                                                       UserDefinedFunction udf, RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> createUserDefinedFunctionInternal(collectionLink, udf, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<UserDefinedFunction>> createUserDefinedFunctionInternal(String collectionLink,
                                                                                                UserDefinedFunction udf, RequestOptions options, DocumentClientRetryPolicy retryPolicyInstance) {
        // we are using an observable factory here
        // observable will be created fresh upon subscription
        // this is to ensure we capture most up to date information (e.g.,
        // session)
        try {
            logger.debug("Creating a UserDefinedFunction. collectionLink [{}], udf id [{}]", collectionLink,
                    udf.getId());
            RxDocumentServiceRequest request = getUserDefinedFunctionRequest(collectionLink, udf, options,
                    OperationType.Create);
            if (retryPolicyInstance != null){
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            return this.create(request, retryPolicyInstance, getOperationContextAndListenerTuple(options)).map(response -> toResourceResponse(response, UserDefinedFunction.class));

        } catch (Exception e) {
            // this is only in trace level to capture what's going on
            logger.debug("Failure in creating a UserDefinedFunction due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<UserDefinedFunction>> upsertUserDefinedFunction(String collectionLink,
                                                                                       UserDefinedFunction udf, RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> upsertUserDefinedFunctionInternal(collectionLink, udf, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<UserDefinedFunction>> upsertUserDefinedFunctionInternal(String collectionLink,
                                                                                                UserDefinedFunction udf, RequestOptions options, DocumentClientRetryPolicy retryPolicyInstance) {
        // we are using an observable factory here
        // observable will be created fresh upon subscription
        // this is to ensure we capture most up to date information (e.g.,
        // session)
        try {
            logger.debug("Upserting a UserDefinedFunction. collectionLink [{}], udf id [{}]", collectionLink,
                    udf.getId());
            RxDocumentServiceRequest request = getUserDefinedFunctionRequest(collectionLink, udf, options,
                    OperationType.Upsert);
            if (retryPolicyInstance != null){
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            return this.upsert(request, retryPolicyInstance, getOperationContextAndListenerTuple(options)).map(response -> toResourceResponse(response, UserDefinedFunction.class));

        } catch (Exception e) {
            // this is only in trace level to capture what's going on
            logger.debug("Failure in upserting a UserDefinedFunction due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<UserDefinedFunction>> replaceUserDefinedFunction(UserDefinedFunction udf,
                                                                                        RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> replaceUserDefinedFunctionInternal(udf, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<UserDefinedFunction>> replaceUserDefinedFunctionInternal(UserDefinedFunction udf,
                                                                                                 RequestOptions options, DocumentClientRetryPolicy retryPolicyInstance) {
        // we are using an observable factory here
        // observable will be created fresh upon subscription
        // this is to ensure we capture most up to date information (e.g.,
        // session)
        try {
            if (udf == null) {
                throw new IllegalArgumentException("udf");
            }

            logger.debug("Replacing a UserDefinedFunction. udf id [{}]", udf.getId());
            validateResource(udf);

            String path = Utils.joinPath(udf.getSelfLink(), null);
            Map<String, String> requestHeaders = this.getRequestHeaders(options, ResourceType.UserDefinedFunction, OperationType.Replace);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Replace, ResourceType.UserDefinedFunction, path, udf, requestHeaders, options);

            if (retryPolicyInstance != null){
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            return this.replace(request, retryPolicyInstance).map(response -> toResourceResponse(response, UserDefinedFunction.class));

        } catch (Exception e) {
            // this is only in trace level to capture what's going on
            logger.debug("Failure in replacing a UserDefinedFunction due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<UserDefinedFunction>> deleteUserDefinedFunction(String udfLink,
                                                                                       RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> deleteUserDefinedFunctionInternal(udfLink, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<UserDefinedFunction>> deleteUserDefinedFunctionInternal(String udfLink,
                                                                                                RequestOptions options, DocumentClientRetryPolicy retryPolicyInstance) {
        // we are using an observable factory here
        // observable will be created fresh upon subscription
        // this is to ensure we capture most up to date information (e.g.,
        // session)
        try {
            if (StringUtils.isEmpty(udfLink)) {
                throw new IllegalArgumentException("udfLink");
            }

            logger.debug("Deleting a UserDefinedFunction. udfLink [{}]", udfLink);
            String path = Utils.joinPath(udfLink, null);
            Map<String, String> requestHeaders = this.getRequestHeaders(options, ResourceType.UserDefinedFunction, OperationType.Delete);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Delete, ResourceType.UserDefinedFunction, path, requestHeaders, options);

            if (retryPolicyInstance != null){
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            return this.delete(request, retryPolicyInstance, getOperationContextAndListenerTuple(options)).map(response -> toResourceResponse(response, UserDefinedFunction.class));

        } catch (Exception e) {
            // this is only in trace level to capture what's going on
            logger.debug("Failure in deleting a UserDefinedFunction due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<UserDefinedFunction>> readUserDefinedFunction(String udfLink,
                                                                                     RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> readUserDefinedFunctionInternal(udfLink, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<UserDefinedFunction>> readUserDefinedFunctionInternal(String udfLink,
                                                                                              RequestOptions options, DocumentClientRetryPolicy retryPolicyInstance) {
        // we are using an observable factory here
        // observable will be created fresh upon subscription
        // this is to ensure we capture most up to date information (e.g.,
        // session)
        try {
            if (StringUtils.isEmpty(udfLink)) {
                throw new IllegalArgumentException("udfLink");
            }

            logger.debug("Reading a UserDefinedFunction. udfLink [{}]", udfLink);
            String path = Utils.joinPath(udfLink, null);
            Map<String, String> requestHeaders = this.getRequestHeaders(options, ResourceType.UserDefinedFunction, OperationType.Read);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Read, ResourceType.UserDefinedFunction, path, requestHeaders, options);

            if (retryPolicyInstance != null) {
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            return this.read(request, retryPolicyInstance).map(response -> toResourceResponse(response, UserDefinedFunction.class));

        } catch (Exception e) {
            // this is only in trace level to capture what's going on
            logger.debug("Failure in reading a UserDefinedFunction due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Flux<FeedResponse<UserDefinedFunction>> readUserDefinedFunctions(String collectionLink,
                                                                                  CosmosQueryRequestOptions options) {

        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }

        return readFeed(options, ResourceType.UserDefinedFunction, UserDefinedFunction.class,
                Utils.joinPath(collectionLink, Paths.USER_DEFINED_FUNCTIONS_PATH_SEGMENT));
    }

    @Override
    public Flux<FeedResponse<UserDefinedFunction>> queryUserDefinedFunctions(String collectionLink,
                                                                                   String query, CosmosQueryRequestOptions options) {
        return queryUserDefinedFunctions(collectionLink, new SqlQuerySpec(query), options);
    }

    @Override
    public Flux<FeedResponse<UserDefinedFunction>> queryUserDefinedFunctions(String collectionLink,
                                                                                   SqlQuerySpec querySpec, CosmosQueryRequestOptions options) {
        return createQuery(collectionLink, querySpec, options, UserDefinedFunction.class, ResourceType.UserDefinedFunction);
    }

    @Override
    public Mono<ResourceResponse<Conflict>> readConflict(String conflictLink, RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> readConflictInternal(conflictLink, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<Conflict>> readConflictInternal(String conflictLink, RequestOptions options, DocumentClientRetryPolicy retryPolicyInstance) {

        try {
            if (StringUtils.isEmpty(conflictLink)) {
                throw new IllegalArgumentException("conflictLink");
            }

            logger.debug("Reading a Conflict. conflictLink [{}]", conflictLink);
            String path = Utils.joinPath(conflictLink, null);
            Map<String, String> requestHeaders = getRequestHeaders(options, ResourceType.Conflict, OperationType.Read);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Read, ResourceType.Conflict, path, requestHeaders, options);

            Mono<RxDocumentServiceRequest> reqObs = addPartitionKeyInformation(request, null, null, options);

            return reqObs.flatMap(req -> {
                if (retryPolicyInstance != null) {
                    retryPolicyInstance.onBeforeSendRequest(request);
                }
                return this.read(request, retryPolicyInstance).map(response -> toResourceResponse(response, Conflict.class));
            });

        } catch (Exception e) {
            logger.debug("Failure in reading a Conflict due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Flux<FeedResponse<Conflict>> readConflicts(String collectionLink, CosmosQueryRequestOptions options) {

        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }

        return readFeed(options, ResourceType.Conflict, Conflict.class,
                Utils.joinPath(collectionLink, Paths.CONFLICTS_PATH_SEGMENT));
    }

    @Override
    public Flux<FeedResponse<Conflict>> queryConflicts(String collectionLink, String query,
                                                             CosmosQueryRequestOptions options) {
        return queryConflicts(collectionLink, new SqlQuerySpec(query), options);
    }

    @Override
    public Flux<FeedResponse<Conflict>> queryConflicts(String collectionLink, SqlQuerySpec querySpec,
                                                             CosmosQueryRequestOptions options) {
        return createQuery(collectionLink, querySpec, options, Conflict.class, ResourceType.Conflict);
    }

    @Override
    public Mono<ResourceResponse<Conflict>> deleteConflict(String conflictLink, RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> deleteConflictInternal(conflictLink, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<Conflict>> deleteConflictInternal(String conflictLink, RequestOptions options,
                                                                          DocumentClientRetryPolicy retryPolicyInstance) {

        try {
            if (StringUtils.isEmpty(conflictLink)) {
                throw new IllegalArgumentException("conflictLink");
            }

            logger.debug("Deleting a Conflict. conflictLink [{}]", conflictLink);
            String path = Utils.joinPath(conflictLink, null);
            Map<String, String> requestHeaders = getRequestHeaders(options, ResourceType.Conflict, OperationType.Delete);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Delete, ResourceType.Conflict, path, requestHeaders, options);

            Mono<RxDocumentServiceRequest> reqObs = addPartitionKeyInformation(request, null, null, options);
            return reqObs.flatMap(req -> {
                if (retryPolicyInstance != null) {
                    retryPolicyInstance.onBeforeSendRequest(request);
                }

                return this.delete(request, retryPolicyInstance, getOperationContextAndListenerTuple(options)).map(response -> toResourceResponse(response, Conflict.class));
            });

        } catch (Exception e) {
            logger.debug("Failure in deleting a Conflict due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<User>> createUser(String databaseLink, User user, RequestOptions options) {
        DocumentClientRetryPolicy documentClientRetryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> createUserInternal(databaseLink, user, options, documentClientRetryPolicy), documentClientRetryPolicy);
    }

    private Mono<ResourceResponse<User>> createUserInternal(String databaseLink, User user, RequestOptions options, DocumentClientRetryPolicy documentClientRetryPolicy) {
        try {
            logger.debug("Creating a User. databaseLink [{}], user id [{}]", databaseLink, user.getId());
            RxDocumentServiceRequest request = getUserRequest(databaseLink, user, options, OperationType.Create);
            return this.create(request, documentClientRetryPolicy, getOperationContextAndListenerTuple(options)).map(response -> toResourceResponse(response, User.class));

        } catch (Exception e) {
            logger.debug("Failure in creating a User due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<User>> upsertUser(String databaseLink, User user, RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> upsertUserInternal(databaseLink, user, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<User>> upsertUserInternal(String databaseLink, User user, RequestOptions options,
                                                                  DocumentClientRetryPolicy retryPolicyInstance) {
        try {
            logger.debug("Upserting a User. databaseLink [{}], user id [{}]", databaseLink, user.getId());
            RxDocumentServiceRequest request = getUserRequest(databaseLink, user, options, OperationType.Upsert);
            if (retryPolicyInstance != null) {
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            return this.upsert(request, retryPolicyInstance, getOperationContextAndListenerTuple(options)).map(response -> toResourceResponse(response, User.class));

        } catch (Exception e) {
            logger.debug("Failure in upserting a User due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    private RxDocumentServiceRequest getUserRequest(String databaseLink, User user, RequestOptions options,
                                                    OperationType operationType) {
        if (StringUtils.isEmpty(databaseLink)) {
            throw new IllegalArgumentException("databaseLink");
        }
        if (user == null) {
            throw new IllegalArgumentException("user");
        }

        RxDocumentClientImpl.validateResource(user);

        String path = Utils.joinPath(databaseLink, Paths.USERS_PATH_SEGMENT);
        Map<String, String> requestHeaders = getRequestHeaders(options, ResourceType.User, operationType);
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
            operationType, ResourceType.User, path, user, requestHeaders, options);

        return request;
    }

    @Override
    public Mono<ResourceResponse<User>> replaceUser(User user, RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> replaceUserInternal(user, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<User>> replaceUserInternal(User user, RequestOptions options, DocumentClientRetryPolicy retryPolicyInstance) {
        try {
            if (user == null) {
                throw new IllegalArgumentException("user");
            }
            logger.debug("Replacing a User. user id [{}]", user.getId());
            RxDocumentClientImpl.validateResource(user);

            String path = Utils.joinPath(user.getSelfLink(), null);
            Map<String, String> requestHeaders = getRequestHeaders(options, ResourceType.User, OperationType.Replace);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Replace, ResourceType.User, path, user, requestHeaders, options);
            if (retryPolicyInstance != null) {
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            return this.replace(request, retryPolicyInstance).map(response -> toResourceResponse(response, User.class));

        } catch (Exception e) {
            logger.debug("Failure in replacing a User due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }


    public Mono<ResourceResponse<User>> deleteUser(String userLink, RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance =  this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> deleteUserInternal(userLink, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<User>> deleteUserInternal(String userLink, RequestOptions options,
                                                                  DocumentClientRetryPolicy retryPolicyInstance) {

        try {
            if (StringUtils.isEmpty(userLink)) {
                throw new IllegalArgumentException("userLink");
            }
            logger.debug("Deleting a User. userLink [{}]", userLink);
            String path = Utils.joinPath(userLink, null);
            Map<String, String> requestHeaders = getRequestHeaders(options, ResourceType.User, OperationType.Delete);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Delete, ResourceType.User, path, requestHeaders, options);

            if (retryPolicyInstance != null) {
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            return this.delete(request, retryPolicyInstance, getOperationContextAndListenerTuple(options)).map(response -> toResourceResponse(response, User.class));

        } catch (Exception e) {
            logger.debug("Failure in deleting a User due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }
    @Override
    public Mono<ResourceResponse<User>> readUser(String userLink, RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> readUserInternal(userLink, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<User>> readUserInternal(String userLink, RequestOptions options, DocumentClientRetryPolicy retryPolicyInstance) {
        try {
            if (StringUtils.isEmpty(userLink)) {
                throw new IllegalArgumentException("userLink");
            }
            logger.debug("Reading a User. userLink [{}]", userLink);
            String path = Utils.joinPath(userLink, null);
            Map<String, String> requestHeaders = getRequestHeaders(options, ResourceType.User, OperationType.Read);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Read, ResourceType.User, path, requestHeaders, options);

            if (retryPolicyInstance != null) {
                retryPolicyInstance.onBeforeSendRequest(request);
            }
            return this.read(request, retryPolicyInstance).map(response -> toResourceResponse(response, User.class));

        } catch (Exception e) {
            logger.debug("Failure in reading a User due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Flux<FeedResponse<User>> readUsers(String databaseLink, CosmosQueryRequestOptions options) {

        if (StringUtils.isEmpty(databaseLink)) {
            throw new IllegalArgumentException("databaseLink");
        }

        return readFeed(options, ResourceType.User, User.class,
                Utils.joinPath(databaseLink, Paths.USERS_PATH_SEGMENT));
    }

    @Override
    public Flux<FeedResponse<User>> queryUsers(String databaseLink, String query, CosmosQueryRequestOptions options) {
        return queryUsers(databaseLink, new SqlQuerySpec(query), options);
    }

    @Override
    public Flux<FeedResponse<User>> queryUsers(String databaseLink, SqlQuerySpec querySpec,
                                                     CosmosQueryRequestOptions options) {
        return createQuery(databaseLink, querySpec, options, User.class, ResourceType.User);
    }

    @Override
    public Mono<ResourceResponse<ClientEncryptionKey>> readClientEncryptionKey(String clientEncryptionKeyLink,
                                                                RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> readClientEncryptionKeyInternal(clientEncryptionKeyLink, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<ClientEncryptionKey>> readClientEncryptionKeyInternal(String clientEncryptionKeyLink, RequestOptions options, DocumentClientRetryPolicy retryPolicyInstance) {
        try {
            if (StringUtils.isEmpty(clientEncryptionKeyLink)) {
                throw new IllegalArgumentException("clientEncryptionKeyLink");
            }
            logger.debug("Reading a client encryption key. clientEncryptionKeyLink [{}]", clientEncryptionKeyLink);
            String path = Utils.joinPath(clientEncryptionKeyLink, null);
            Map<String, String> requestHeaders = getRequestHeaders(options, ResourceType.ClientEncryptionKey, OperationType.Read);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Read, ResourceType.ClientEncryptionKey, path, requestHeaders, options);

            if (retryPolicyInstance != null) {
                retryPolicyInstance.onBeforeSendRequest(request);
            }
            return this.read(request, retryPolicyInstance).map(response -> toResourceResponse(response, ClientEncryptionKey.class));

        } catch (Exception e) {
            logger.debug("Failure in reading a client encryption key due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<ClientEncryptionKey>> createClientEncryptionKey(String databaseLink,
     ClientEncryptionKey clientEncryptionKey, RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> createClientEncryptionKeyInternal(databaseLink, clientEncryptionKey, options, retryPolicyInstance), retryPolicyInstance);

    }

    private Mono<ResourceResponse<ClientEncryptionKey>> createClientEncryptionKeyInternal(String databaseLink, ClientEncryptionKey clientEncryptionKey, RequestOptions options, DocumentClientRetryPolicy documentClientRetryPolicy) {
        try {
            logger.debug("Creating a client encryption key. databaseLink [{}], clientEncryptionKey id [{}]", databaseLink, clientEncryptionKey.getId());
            RxDocumentServiceRequest request = getClientEncryptionKeyRequest(databaseLink, clientEncryptionKey, options, OperationType.Create);
            return this.create(request, documentClientRetryPolicy, getOperationContextAndListenerTuple(options)).map(response -> toResourceResponse(response, ClientEncryptionKey.class));

        } catch (Exception e) {
            logger.debug("Failure in creating a client encryption key due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    private RxDocumentServiceRequest getClientEncryptionKeyRequest(String databaseLink, ClientEncryptionKey clientEncryptionKey, RequestOptions options,
                                                    OperationType operationType) {
        if (StringUtils.isEmpty(databaseLink)) {
            throw new IllegalArgumentException("databaseLink");
        }
        if (clientEncryptionKey == null) {
            throw new IllegalArgumentException("clientEncryptionKey");
        }

        RxDocumentClientImpl.validateResource(clientEncryptionKey);

        String path = Utils.joinPath(databaseLink, Paths.CLIENT_ENCRYPTION_KEY_PATH_SEGMENT);
        Map<String, String> requestHeaders = getRequestHeaders(options, ResourceType.ClientEncryptionKey, operationType);
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
            operationType, ResourceType.ClientEncryptionKey, path, clientEncryptionKey, requestHeaders, options);

        return request;
    }

    @Override
    public Mono<ResourceResponse<ClientEncryptionKey>> replaceClientEncryptionKey(ClientEncryptionKey clientEncryptionKey,
                                                                                  String nameBasedLink,
                                                                                  RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> replaceClientEncryptionKeyInternal(clientEncryptionKey,
            nameBasedLink, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<ClientEncryptionKey>> replaceClientEncryptionKeyInternal(ClientEncryptionKey clientEncryptionKey, String nameBasedLink, RequestOptions options, DocumentClientRetryPolicy retryPolicyInstance) {
        try {
            if (clientEncryptionKey == null) {
                throw new IllegalArgumentException("clientEncryptionKey");
            }
            logger.debug("Replacing a clientEncryptionKey. clientEncryptionKey id [{}]", clientEncryptionKey.getId());
            RxDocumentClientImpl.validateResource(clientEncryptionKey);

            String path = Utils.joinPath(nameBasedLink, null);
            //String path = Utils.joinPath(clientEncryptionKey.getSelfLink(), null); TODO need to check with BE service
            Map<String, String> requestHeaders = getRequestHeaders(options, ResourceType.ClientEncryptionKey,
             OperationType.Replace);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Replace, ResourceType.ClientEncryptionKey, path, clientEncryptionKey, requestHeaders,
                 options);
            if (retryPolicyInstance != null) {
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            return this.replace(request, retryPolicyInstance).map(response -> toResourceResponse(response, ClientEncryptionKey.class));

        } catch (Exception e) {
            logger.debug("Failure in replacing a clientEncryptionKey due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Flux<FeedResponse<ClientEncryptionKey>> readClientEncryptionKeys(String databaseLink, CosmosQueryRequestOptions options) {
        if (StringUtils.isEmpty(databaseLink)) {
            throw new IllegalArgumentException("databaseLink");
        }

        return readFeed(options, ResourceType.ClientEncryptionKey, ClientEncryptionKey.class,
            Utils.joinPath(databaseLink, Paths.CLIENT_ENCRYPTION_KEY_PATH_SEGMENT));
    }

    @Override
    public Flux<FeedResponse<ClientEncryptionKey>> queryClientEncryptionKeys(String databaseLink, String query,
     CosmosQueryRequestOptions options) {
        return queryClientEncryptionKeys(databaseLink, new SqlQuerySpec(query), options);
    }

    @Override
    public Flux<FeedResponse<ClientEncryptionKey>> queryClientEncryptionKeys(String databaseLink, SqlQuerySpec querySpec, CosmosQueryRequestOptions options) {
        return createQuery(databaseLink, querySpec, options, ClientEncryptionKey.class, ResourceType.ClientEncryptionKey);
    }

    @Override
    public Mono<ResourceResponse<Permission>> createPermission(String userLink, Permission permission,
                                                                     RequestOptions options) {
        DocumentClientRetryPolicy documentClientRetryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> createPermissionInternal(userLink, permission, options, documentClientRetryPolicy), this.resetSessionTokenRetryPolicy.getRequestPolicy());
    }

    private Mono<ResourceResponse<Permission>> createPermissionInternal(String userLink, Permission permission,
                                                                              RequestOptions options, DocumentClientRetryPolicy documentClientRetryPolicy) {

        try {
            logger.debug("Creating a Permission. userLink [{}], permission id [{}]", userLink, permission.getId());
            RxDocumentServiceRequest request = getPermissionRequest(userLink, permission, options,
                    OperationType.Create);
            return this.create(request, documentClientRetryPolicy, getOperationContextAndListenerTuple(options)).map(response -> toResourceResponse(response, Permission.class));

        } catch (Exception e) {
            logger.debug("Failure in creating a Permission due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<Permission>> upsertPermission(String userLink, Permission permission,
                                                                     RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> upsertPermissionInternal(userLink, permission, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<Permission>> upsertPermissionInternal(String userLink, Permission permission,
                                                                              RequestOptions options, DocumentClientRetryPolicy retryPolicyInstance) {

        try {
            logger.debug("Upserting a Permission. userLink [{}], permission id [{}]", userLink, permission.getId());
            RxDocumentServiceRequest request = getPermissionRequest(userLink, permission, options,
                    OperationType.Upsert);
            if (retryPolicyInstance != null) {
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            return this.upsert(request, retryPolicyInstance, getOperationContextAndListenerTuple(options)).map(response -> toResourceResponse(response, Permission.class));

        } catch (Exception e) {
            logger.debug("Failure in upserting a Permission due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    private RxDocumentServiceRequest getPermissionRequest(String userLink, Permission permission,
                                                          RequestOptions options, OperationType operationType) {
        if (StringUtils.isEmpty(userLink)) {
            throw new IllegalArgumentException("userLink");
        }
        if (permission == null) {
            throw new IllegalArgumentException("permission");
        }

        RxDocumentClientImpl.validateResource(permission);

        String path = Utils.joinPath(userLink, Paths.PERMISSIONS_PATH_SEGMENT);
        Map<String, String> requestHeaders = getRequestHeaders(options, ResourceType.Permission, operationType);
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
            operationType, ResourceType.Permission, path, permission, requestHeaders, options);

        return request;
    }

    @Override
    public Mono<ResourceResponse<Permission>> replacePermission(Permission permission, RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> replacePermissionInternal(permission, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<Permission>> replacePermissionInternal(Permission permission, RequestOptions options, DocumentClientRetryPolicy retryPolicyInstance) {
        try {
            if (permission == null) {
                throw new IllegalArgumentException("permission");
            }
            logger.debug("Replacing a Permission. permission id [{}]", permission.getId());
            RxDocumentClientImpl.validateResource(permission);

            String path = Utils.joinPath(permission.getSelfLink(), null);
            Map<String, String> requestHeaders = getRequestHeaders(options, ResourceType.Permission, OperationType.Replace);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Replace, ResourceType.Permission, path, permission, requestHeaders, options);

            if (retryPolicyInstance != null) {
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            return this.replace(request, retryPolicyInstance).map(response -> toResourceResponse(response, Permission.class));

        } catch (Exception e) {
            logger.debug("Failure in replacing a Permission due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<Permission>> deletePermission(String permissionLink, RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> deletePermissionInternal(permissionLink, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<Permission>> deletePermissionInternal(String permissionLink, RequestOptions options,
                                                                              DocumentClientRetryPolicy retryPolicyInstance) {

        try {
            if (StringUtils.isEmpty(permissionLink)) {
                throw new IllegalArgumentException("permissionLink");
            }
            logger.debug("Deleting a Permission. permissionLink [{}]", permissionLink);
            String path = Utils.joinPath(permissionLink, null);
            Map<String, String> requestHeaders = getRequestHeaders(options, ResourceType.Permission, OperationType.Delete);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Delete, ResourceType.Permission, path, requestHeaders, options);

            if (retryPolicyInstance != null) {
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            return this.delete(request, retryPolicyInstance, getOperationContextAndListenerTuple(options)).map(response -> toResourceResponse(response, Permission.class));

        } catch (Exception e) {
            logger.debug("Failure in deleting a Permission due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<Permission>> readPermission(String permissionLink, RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> readPermissionInternal(permissionLink, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<Permission>> readPermissionInternal(String permissionLink, RequestOptions options, DocumentClientRetryPolicy retryPolicyInstance ) {
        try {
            if (StringUtils.isEmpty(permissionLink)) {
                throw new IllegalArgumentException("permissionLink");
            }
            logger.debug("Reading a Permission. permissionLink [{}]", permissionLink);
            String path = Utils.joinPath(permissionLink, null);
            Map<String, String> requestHeaders = getRequestHeaders(options, ResourceType.Permission, OperationType.Read);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Read, ResourceType.Permission, path, requestHeaders, options);

            if (retryPolicyInstance != null) {
                retryPolicyInstance.onBeforeSendRequest(request);
            }
            return this.read(request, retryPolicyInstance).map(response -> toResourceResponse(response, Permission.class));

        } catch (Exception e) {
            logger.debug("Failure in reading a Permission due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Flux<FeedResponse<Permission>> readPermissions(String userLink, CosmosQueryRequestOptions options) {

        if (StringUtils.isEmpty(userLink)) {
            throw new IllegalArgumentException("userLink");
        }

        return readFeed(options, ResourceType.Permission, Permission.class,
                Utils.joinPath(userLink, Paths.PERMISSIONS_PATH_SEGMENT));
    }

    @Override
    public Flux<FeedResponse<Permission>> queryPermissions(String userLink, String query,
                                                                 CosmosQueryRequestOptions options) {
        return queryPermissions(userLink, new SqlQuerySpec(query), options);
    }

    @Override
    public Flux<FeedResponse<Permission>> queryPermissions(String userLink, SqlQuerySpec querySpec,
                                                                 CosmosQueryRequestOptions options) {
        return createQuery(userLink, querySpec, options, Permission.class, ResourceType.Permission);
    }

    @Override
    public Mono<ResourceResponse<Offer>> replaceOffer(Offer offer) {
        DocumentClientRetryPolicy documentClientRetryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> replaceOfferInternal(offer, documentClientRetryPolicy), documentClientRetryPolicy);
    }

    private Mono<ResourceResponse<Offer>> replaceOfferInternal(Offer offer, DocumentClientRetryPolicy documentClientRetryPolicy) {
        try {
            if (offer == null) {
                throw new IllegalArgumentException("offer");
            }
            logger.debug("Replacing an Offer. offer id [{}]", offer.getId());
            RxDocumentClientImpl.validateResource(offer);

            String path = Utils.joinPath(offer.getSelfLink(), null);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this, OperationType.Replace,
                    ResourceType.Offer, path, offer, null, null);
            return this.replace(request, documentClientRetryPolicy).map(response -> toResourceResponse(response, Offer.class));

        } catch (Exception e) {
            logger.debug("Failure in replacing an Offer due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<Offer>> readOffer(String offerLink) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> readOfferInternal(offerLink, retryPolicyInstance), retryPolicyInstance);
    }

    private Mono<ResourceResponse<Offer>> readOfferInternal(String offerLink, DocumentClientRetryPolicy retryPolicyInstance) {
        try {
            if (StringUtils.isEmpty(offerLink)) {
                throw new IllegalArgumentException("offerLink");
            }
            logger.debug("Reading an Offer. offerLink [{}]", offerLink);
            String path = Utils.joinPath(offerLink, null);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Read, ResourceType.Offer, path, (HashMap<String, String>)null, null);

            if (retryPolicyInstance != null) {
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            return this.read(request, retryPolicyInstance).map(response -> toResourceResponse(response, Offer.class));

        } catch (Exception e) {
            logger.debug("Failure in reading an Offer due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Flux<FeedResponse<Offer>> readOffers(CosmosQueryRequestOptions options) {
        return readFeed(options, ResourceType.Offer, Offer.class,
                Utils.joinPath(Paths.OFFERS_PATH_SEGMENT, null));
    }

//    private <T extends Resource> Flux<FeedResponse<T>> readFeedCollectionChild(FeedOptions options, ResourceType resourceType,
//                                                                               Class<T> klass, String resourceLink) {
//        if (options == null) {
//            options = new FeedOptions();
//        }
//
//        int maxPageSize = options.getMaxItemCount() != null ? options.getMaxItemCount() : -1;
//
//        final FeedOptions finalFeedOptions = options;
//        RequestOptions requestOptions = new RequestOptions();
//        requestOptions.setPartitionKey(options.getPartitionKey());
//        BiFunction<String, Integer, RxDocumentServiceRequest> createRequestFunc = (continuationToken, pageSize) -> {
//            Map<String, String> requestHeaders = new HashMap<>();
//            if (continuationToken != null) {
//                requestHeaders.put(HttpConstants.HttpHeaders.CONTINUATION, continuationToken);
//            }
//            requestHeaders.put(HttpConstants.HttpHeaders.PAGE_SIZE, Integer.toString(pageSize));
//            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.ReadFeed,
//                resourceType, resourceLink, requestHeaders, finalFeedOptions);
//            return request;
//        };
//
//        Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeFunc = request -> {
//            return ObservableHelper.inlineIfPossibleAsObs(() -> {
//                Mono<Utils.ValueHolder<DocumentCollection>> collectionObs = this.collectionCache.resolveCollectionAsync(request);
//                Mono<RxDocumentServiceRequest> requestObs = this.addPartitionKeyInformation(request, null, null, requestOptions, collectionObs);
//
//                return requestObs.flatMap(req -> this.readFeed(req)
//                    .map(response -> toFeedResponsePage(response, klass)));
//            }, this.resetSessionTokenRetryPolicy.getRequestPolicy());
//        };
//
//        return Paginator.getPaginatedQueryResultAsObservable(options, createRequestFunc, executeFunc, klass, maxPageSize);
//    }

    private <T extends Resource> Flux<FeedResponse<T>> readFeed(CosmosQueryRequestOptions options, ResourceType resourceType, Class<T> klass, String resourceLink) {
        if (options == null) {
            options = new CosmosQueryRequestOptions();
        }

        Integer maxItemCount = ModelBridgeInternal.getMaxItemCountFromQueryRequestOptions(options);
        int maxPageSize = maxItemCount != null ? maxItemCount : -1;
        final CosmosQueryRequestOptions finalCosmosQueryRequestOptions = options;
        BiFunction<String, Integer, RxDocumentServiceRequest> createRequestFunc = (continuationToken, pageSize) -> {
            Map<String, String> requestHeaders = new HashMap<>();
            if (continuationToken != null) {
                requestHeaders.put(HttpConstants.HttpHeaders.CONTINUATION, continuationToken);
            }
            requestHeaders.put(HttpConstants.HttpHeaders.PAGE_SIZE, Integer.toString(pageSize));
            RxDocumentServiceRequest request =  RxDocumentServiceRequest.create(this,
                OperationType.ReadFeed, resourceType, resourceLink, requestHeaders, finalCosmosQueryRequestOptions);
            return request;
        };

        Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeFunc = request -> ObservableHelper
            .inlineIfPossibleAsObs(() -> readFeed(request).map(response -> toFeedResponsePage(response, klass)),
                this.resetSessionTokenRetryPolicy.getRequestPolicy());

        return Paginator.getPaginatedQueryResultAsObservable(options, createRequestFunc, executeFunc, klass, maxPageSize);
    }

    @Override
    public Flux<FeedResponse<Offer>> queryOffers(String query, CosmosQueryRequestOptions options) {
        return queryOffers(new SqlQuerySpec(query), options);
    }

    @Override
    public Flux<FeedResponse<Offer>> queryOffers(SqlQuerySpec querySpec, CosmosQueryRequestOptions options) {
        return createQuery(null, querySpec, options, Offer.class, ResourceType.Offer);
    }

    @Override
    public Mono<DatabaseAccount> getDatabaseAccount() {
        DocumentClientRetryPolicy documentClientRetryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> getDatabaseAccountInternal(documentClientRetryPolicy),
         documentClientRetryPolicy);
    }

    @Override
    public DatabaseAccount getLatestDatabaseAccount() {
        return this.globalEndpointManager.getLatestDatabaseAccount();
    }

    private Mono<DatabaseAccount> getDatabaseAccountInternal(DocumentClientRetryPolicy documentClientRetryPolicy) {
        try {
            logger.debug("Getting Database Account");
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                    OperationType.Read,
                    ResourceType.DatabaseAccount, "", // path
                    (HashMap<String, String>) null,
                    null);
            return this.read(request, documentClientRetryPolicy).map(ModelBridgeInternal::toDatabaseAccount);

        } catch (Exception e) {
            logger.debug("Failure in getting Database Account due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    public Object getSession() {
        return this.sessionContainer;
    }

    public void setSession(Object sessionContainer) {
        this.sessionContainer = (SessionContainer) sessionContainer;
    }

    @Override
    public RxClientCollectionCache getCollectionCache() {
        return this.collectionCache;
    }

    @Override
    public RxPartitionKeyRangeCache getPartitionKeyRangeCache() {
        return partitionKeyRangeCache;
    }

    public Flux<DatabaseAccount> getDatabaseAccountFromEndpoint(URI endpoint) {
        return Flux.defer(() -> {
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Read, ResourceType.DatabaseAccount, "", null, (Object) null);
            return this.populateHeaders(request, RequestVerb.GET)
                .flatMap(requestPopulated -> {

                    requestPopulated.setEndpointOverride(endpoint);
                    return this.gatewayProxy.processMessage(requestPopulated).doOnError(e -> {
                        String message = String.format("Failed to retrieve database account information. %s",
                            e.getCause() != null
                                ? e.getCause().toString()
                                : e.toString());
                        logger.warn(message);
                    }).map(rsp -> rsp.getResource(DatabaseAccount.class))
                        .doOnNext(databaseAccount ->
                            this.useMultipleWriteLocations = this.connectionPolicy.isMultipleWriteRegionsEnabled()
                            && BridgeInternal.isEnableMultipleWriteLocations(databaseAccount));
                });
        });
    }

    /**
     * Certain requests must be routed through gateway even when the client connectivity mode is direct.
     *
     * @param request
     * @return RxStoreModel
     */
    private RxStoreModel getStoreProxy(RxDocumentServiceRequest request) {
        // If a request is configured to always use GATEWAY mode(in some cases when targeting .NET Core)
        // we return the GATEWAY store model
        if (request.UseGatewayMode) {
            return this.gatewayProxy;
        }

        ResourceType resourceType = request.getResourceType();
        OperationType operationType = request.getOperationType();

        if (resourceType == ResourceType.Offer ||
            resourceType == ResourceType.ClientEncryptionKey ||
            resourceType.isScript() && operationType != OperationType.ExecuteJavaScript ||
            resourceType == ResourceType.PartitionKeyRange ||
            resourceType == ResourceType.PartitionKey && operationType == OperationType.Delete) {
            return this.gatewayProxy;
        }

        if (operationType == OperationType.Create
                || operationType == OperationType.Upsert) {
            if (resourceType == ResourceType.Database ||
                    resourceType == ResourceType.User ||
                    resourceType == ResourceType.DocumentCollection ||
                    resourceType == ResourceType.Permission) {
                return this.gatewayProxy;
            } else {
                return this.storeModel;
            }
        } else if (operationType == OperationType.Delete) {
            if (resourceType == ResourceType.Database ||
                    resourceType == ResourceType.User ||
                    resourceType == ResourceType.DocumentCollection) {
                return this.gatewayProxy;
            } else {
                return this.storeModel;
            }
        } else if (operationType == OperationType.Replace) {
            if (resourceType == ResourceType.DocumentCollection) {
                return this.gatewayProxy;
            } else {
                return this.storeModel;
            }
        } else if (operationType == OperationType.Read) {
            if (resourceType == ResourceType.DocumentCollection) {
                return this.gatewayProxy;
            } else {
                return this.storeModel;
            }
        } else {
            if ((operationType == OperationType.Query ||
                operationType == OperationType.SqlQuery ||
                operationType == OperationType.ReadFeed) &&
                    Utils.isCollectionChild(request.getResourceType())) {
                // Go to gateway only when partition key range and partition key are not set. This should be very rare
                if (request.getPartitionKeyRangeIdentity() == null &&
                        request.getHeaders().get(HttpConstants.HttpHeaders.PARTITION_KEY) == null) {
                    return this.gatewayProxy;
                }
            }

            return this.storeModel;
        }
    }

    @Override
    public void close() {
        logger.info("Attempting to close client {}", this.clientId);
        if (!closed.getAndSet(true)) {
            logger.info("Shutting down ...");
            logger.info("Closing Global Endpoint Manager ...");
            LifeCycleUtils.closeQuietly(this.globalEndpointManager);
            logger.info("Closing StoreClientFactory ...");
            LifeCycleUtils.closeQuietly(this.storeClientFactory);
            logger.info("Shutting down reactorHttpClient ...");
            LifeCycleUtils.closeQuietly(this.reactorHttpClient);
            logger.info("Shutting down CpuMonitor ...");
            CpuMemoryMonitor.unregister(this);

            if (this.throughputControlEnabled.get()) {
                logger.info("Closing ThroughputControlStore ...");
                this.throughputControlStore.close();
            }

            logger.info("Shutting down completed.");
        } else {
            logger.warn("Already shutdown!");
        }
    }

    @Override
    public ItemDeserializer getItemDeserializer() {
        return this.itemDeserializer;
    }

    @Override
    public synchronized void enableThroughputControlGroup(ThroughputControlGroupInternal group) {
        checkNotNull(group, "Throughput control group can not be null");

        if (this.throughputControlEnabled.compareAndSet(false, true)) {
            this.throughputControlStore =
                new ThroughputControlStore(
                    this.collectionCache,
                    this.connectionPolicy.getConnectionMode(),
                    this.partitionKeyRangeCache);

            this.storeModel.enableThroughputControl(throughputControlStore);
        }

        this.throughputControlStore.enableThroughputControlGroup(group);
    }

    private static SqlQuerySpec createLogicalPartitionScanQuerySpec(
        PartitionKey partitionKey,
        String partitionKeySelector) {

        StringBuilder queryStringBuilder = new StringBuilder();
        List<SqlParameter> parameters = new ArrayList<>();

        queryStringBuilder.append("SELECT * FROM c WHERE");
        Object pkValue = ModelBridgeInternal.getPartitionKeyObject(partitionKey);
        String pkParamName = "@pkValue";
        parameters.add(new SqlParameter(pkParamName, pkValue));

        queryStringBuilder.append(" c");
        // partition key def
        queryStringBuilder.append(partitionKeySelector);
        queryStringBuilder.append((" = "));
        queryStringBuilder.append(pkParamName);

        return new SqlQuerySpec(queryStringBuilder.toString(), parameters);
    }

    @Override
    public Mono<List<FeedRange>> getFeedRanges(String collectionLink) {

        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            this,
            OperationType.Query,
            ResourceType.Document,
            collectionLink,
            null); // This should not go to backend
        Mono<Utils.ValueHolder<DocumentCollection>> collectionObs = collectionCache.resolveCollectionAsync(null,
            request);

        return collectionObs.flatMap(documentCollectionResourceResponse -> {
            final DocumentCollection collection = documentCollectionResourceResponse.v;
            if (collection == null) {
                throw new IllegalStateException("Collection cannot be null");
            }

            Mono<Utils.ValueHolder<List<PartitionKeyRange>>> valueHolderMono = partitionKeyRangeCache
                .tryGetOverlappingRangesAsync(
                    BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics),
                    collection.getResourceId(), RANGE_INCLUDING_ALL_PARTITION_KEY_RANGES, true, null);

            return valueHolderMono.map(RxDocumentClientImpl::toFeedRanges);
        });
    }

    private static List<FeedRange> toFeedRanges(
        Utils.ValueHolder<List<PartitionKeyRange>> partitionKeyRangeListValueHolder) {
        final List<PartitionKeyRange> partitionKeyRangeList = partitionKeyRangeListValueHolder.v;
        if (partitionKeyRangeList == null) {
            throw new IllegalStateException("PartitionKeyRange list cannot be null");
        }

        List<FeedRange> feedRanges = new ArrayList<>();
        partitionKeyRangeList.forEach(pkRange -> feedRanges.add(toFeedRange(pkRange)));

        return feedRanges;
    }

    private static FeedRange toFeedRange(PartitionKeyRange pkRange) {
        return new FeedRangeEpkImpl(pkRange.toRange());
    }
}
