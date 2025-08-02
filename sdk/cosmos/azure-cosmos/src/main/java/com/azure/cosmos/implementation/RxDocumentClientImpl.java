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
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosContainerProactiveInitConfig;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfigBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.CosmosOperationPolicy;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.ReadConsistencyStrategy;
import com.azure.cosmos.SessionRetryOptions;
import com.azure.cosmos.ThresholdBasedAvailabilityStrategy;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
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
import com.azure.cosmos.implementation.directconnectivity.AddressSelector;
import com.azure.cosmos.implementation.directconnectivity.GatewayServiceConfigurationReader;
import com.azure.cosmos.implementation.directconnectivity.GlobalAddressResolver;
import com.azure.cosmos.implementation.directconnectivity.ServerStoreModel;
import com.azure.cosmos.implementation.directconnectivity.StoreClient;
import com.azure.cosmos.implementation.directconnectivity.StoreClientFactory;
import com.azure.cosmos.implementation.faultinjection.IFaultInjectorProvider;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpClientConfig;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.SharedGatewayHttpClient;
import com.azure.cosmos.implementation.patch.PatchUtil;
import com.azure.cosmos.implementation.perPartitionAutomaticFailover.GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover;
import com.azure.cosmos.implementation.perPartitionCircuitBreaker.GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker;
import com.azure.cosmos.implementation.perPartitionCircuitBreaker.PartitionLevelCircuitBreakerConfig;
import com.azure.cosmos.implementation.query.DocumentQueryExecutionContextFactory;
import com.azure.cosmos.implementation.query.IDocumentQueryClient;
import com.azure.cosmos.implementation.query.IDocumentQueryExecutionContext;
import com.azure.cosmos.implementation.query.Paginator;
import com.azure.cosmos.implementation.query.PartitionedQueryExecutionInfo;
import com.azure.cosmos.implementation.query.PipelinedQueryExecutionContextBase;
import com.azure.cosmos.implementation.query.QueryInfo;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.PartitionKeyAndResourceTokenPair;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.implementation.routing.RegionNameToRegionIdMap;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import com.azure.cosmos.implementation.spark.OperationContext;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.implementation.spark.OperationListener;
import com.azure.cosmos.implementation.throughputControl.ThroughputControlStore;
import com.azure.cosmos.implementation.throughputControl.config.ThroughputControlGroupInternal;
import com.azure.cosmos.models.CosmosAuthorizationTokenResolver;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosContainerIdentity;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosOperationDetails;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKind;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.util.concurrent.Queues;
import reactor.util.function.Tuple2;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.azure.cosmos.BridgeInternal.getAltLink;
import static com.azure.cosmos.BridgeInternal.toResourceResponse;
import static com.azure.cosmos.BridgeInternal.toStoredProcedureResponse;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * While this class is public, it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class RxDocumentClientImpl implements AsyncDocumentClient, IAuthorizationTokenProvider, CpuMemoryListener,
    DiagnosticsClientContext {

    private final static List<String> EMPTY_REGION_LIST = Collections.emptyList();

    private final static List<RegionalRoutingContext> EMPTY_ENDPOINT_LIST = Collections.emptyList();

    private final static
    ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor diagnosticsAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor();

    private final static
    ImplementationBridgeHelpers.FeedResponseHelper.FeedResponseAccessor feedResponseAccessor =
        ImplementationBridgeHelpers.FeedResponseHelper.getFeedResponseAccessor();

    private final static
    ImplementationBridgeHelpers.CosmosClientTelemetryConfigHelper.CosmosClientTelemetryConfigAccessor telemetryCfgAccessor =
        ImplementationBridgeHelpers.CosmosClientTelemetryConfigHelper.getCosmosClientTelemetryConfigAccessor();

    private final static
    ImplementationBridgeHelpers.CosmosDiagnosticsContextHelper.CosmosDiagnosticsContextAccessor ctxAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsContextHelper.getCosmosDiagnosticsContextAccessor();

    private final static
    ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.CosmosQueryRequestOptionsAccessor qryOptAccessor =
        ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.getCosmosQueryRequestOptionsAccessor();

    private final static
    ImplementationBridgeHelpers.CosmosItemResponseHelper.CosmosItemResponseBuilderAccessor itemResponseAccessor =
        ImplementationBridgeHelpers.CosmosItemResponseHelper.getCosmosItemResponseBuilderAccessor();

    private static final ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper.CosmosChangeFeedRequestOptionsAccessor changeFeedOptionsAccessor =
        ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper.getCosmosChangeFeedRequestOptionsAccessor();

    private static final ImplementationBridgeHelpers.CosmosOperationDetailsHelper.CosmosOperationDetailsAccessor operationDetailsAccessor =
        ImplementationBridgeHelpers.CosmosOperationDetailsHelper.getCosmosOperationDetailsAccessor();

    private static final ImplementationBridgeHelpers.ReadConsistencyStrategyHelper.ReadConsistencyStrategyAccessor readConsistencyStrategyAccessor =
        ImplementationBridgeHelpers.ReadConsistencyStrategyHelper.getReadConsistencyStrategyAccessor();

    private static final ImplementationBridgeHelpers.CosmosBulkExecutionOptionsHelper.CosmosBulkExecutionOptionsAccessor bulkExecutionOptionsAccessor =
        ImplementationBridgeHelpers.CosmosBulkExecutionOptionsHelper.getCosmosBulkExecutionOptionsAccessor();

    private static final String tempMachineId = "uuid:" + UUIDs.nonBlockingRandomUUID();
    private static final AtomicInteger activeClientsCnt = new AtomicInteger(0);
    private static final Map<String, Integer> clientMap = new ConcurrentHashMap<>();
    private static final AtomicInteger clientIdGenerator = new AtomicInteger(0);
    private static final Range<String> RANGE_INCLUDING_ALL_PARTITION_KEY_RANGES = new Range<>(
        PartitionKeyInternalHelper.MinimumInclusiveEffectivePartitionKey,
        PartitionKeyInternalHelper.MaximumExclusiveEffectivePartitionKey, true, false);

    private static final String DUMMY_SQL_QUERY = "this is dummy and only used in creating " +
        "ParallelDocumentQueryExecutioncontext, but not used";

    private final static ObjectMapper mapper = Utils.getSimpleObjectMapper();
    private final CosmosItemSerializer defaultCustomSerializer;
    private final static Logger logger = LoggerFactory.getLogger(RxDocumentClientImpl.class);
    private final String masterKeyOrResourceToken;
    private final URI serviceEndpoint;
    private final ConnectionPolicy connectionPolicy;
    private final ConsistencyLevel consistencyLevel;
    private final ReadConsistencyStrategy readConsistencyStrategy;
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
    private ISessionContainer sessionContainer;
    private String firstResourceTokenFromPermissionFeed = StringUtils.EMPTY;
    private RxClientCollectionCache collectionCache;
    private RxGatewayStoreModel gatewayProxy;
    private RxGatewayStoreModel thinProxy;
    private RxStoreModel storeModel;
    private GlobalAddressResolver addressResolver;
    private RxPartitionKeyRangeCache partitionKeyRangeCache;
    private Map<String, List<PartitionKeyAndResourceTokenPair>> resourceTokensMap;
    private final boolean contentResponseOnWriteEnabled;
    private final Map<String, PartitionedQueryExecutionInfo> queryPlanCache;

    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final int clientId;
    private ClientTelemetry clientTelemetry;
    private final ApiType apiType;
    private final CosmosEndToEndOperationLatencyPolicyConfig cosmosEndToEndOperationLatencyPolicyConfig;

    private final AtomicReference<CosmosDiagnostics> mostRecentlyCreatedDiagnostics = new AtomicReference<>(null);

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
    private final GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker globalPartitionEndpointManagerForPerPartitionCircuitBreaker;
    private final GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover globalPartitionEndpointManagerForPerPartitionAutomaticFailover;
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
    private final CosmosClientTelemetryConfig clientTelemetryConfig;
    private final String clientCorrelationId;
    private final SessionRetryOptions sessionRetryOptions;
    private final boolean sessionCapturingOverrideEnabled;
    private final boolean sessionCapturingDisabled;
    private final boolean isRegionScopedSessionCapturingEnabledOnClientOrSystemConfig;
    private final boolean useThinClient;
    private List<CosmosOperationPolicy> operationPolicies;
    private final AtomicReference<CosmosAsyncClient> cachedCosmosAsyncClientSnapshot;
    private CosmosEndToEndOperationLatencyPolicyConfig ppafEnforcedE2ELatencyPolicyConfigForReads;

    public RxDocumentClientImpl(URI serviceEndpoint,
                                String masterKeyOrResourceToken,
                                List<Permission> permissionFeed,
                                ConnectionPolicy connectionPolicy,
                                ConsistencyLevel consistencyLevel,
                                ReadConsistencyStrategy readConsistencyStrategy,
                                Configs configs,
                                CosmosAuthorizationTokenResolver cosmosAuthorizationTokenResolver,
                                AzureKeyCredential credential,
                                boolean sessionCapturingOverride,
                                boolean connectionSharingAcrossClientsEnabled,
                                boolean contentResponseOnWriteEnabled,
                                CosmosClientMetadataCachesSnapshot metadataCachesSnapshot,
                                ApiType apiType,
                                CosmosClientTelemetryConfig clientTelemetryConfig,
                                String clientCorrelationId,
                                CosmosEndToEndOperationLatencyPolicyConfig cosmosEndToEndOperationLatencyPolicyConfig,
                                SessionRetryOptions sessionRetryOptions,
                                CosmosContainerProactiveInitConfig containerProactiveInitConfig,
                                CosmosItemSerializer defaultCustomSerializer,
                                boolean isRegionScopedSessionCapturingEnabled) {
        this(
                serviceEndpoint,
                masterKeyOrResourceToken,
                permissionFeed,
                connectionPolicy,
                consistencyLevel,
                readConsistencyStrategy,
                configs,
                credential,
                null,
                sessionCapturingOverride,
                connectionSharingAcrossClientsEnabled,
                contentResponseOnWriteEnabled,
                metadataCachesSnapshot,
                apiType,
                clientTelemetryConfig,
                clientCorrelationId,
                cosmosEndToEndOperationLatencyPolicyConfig,
                sessionRetryOptions,
                containerProactiveInitConfig,
                defaultCustomSerializer,
                isRegionScopedSessionCapturingEnabled
        );

        this.cosmosAuthorizationTokenResolver = cosmosAuthorizationTokenResolver;
    }

    public RxDocumentClientImpl(URI serviceEndpoint,
                                String masterKeyOrResourceToken,
                                List<Permission> permissionFeed,
                                ConnectionPolicy connectionPolicy,
                                ConsistencyLevel consistencyLevel,
                                ReadConsistencyStrategy readConsistencyStrategy,
                                Configs configs,
                                CosmosAuthorizationTokenResolver cosmosAuthorizationTokenResolver,
                                AzureKeyCredential credential,
                                TokenCredential tokenCredential,
                                boolean sessionCapturingOverride,
                                boolean connectionSharingAcrossClientsEnabled,
                                boolean contentResponseOnWriteEnabled,
                                CosmosClientMetadataCachesSnapshot metadataCachesSnapshot,
                                ApiType apiType,
                                CosmosClientTelemetryConfig clientTelemetryConfig,
                                String clientCorrelationId,
                                CosmosEndToEndOperationLatencyPolicyConfig cosmosEndToEndOperationLatencyPolicyConfig,
                                SessionRetryOptions sessionRetryOptions,
                                CosmosContainerProactiveInitConfig containerProactiveInitConfig,
                                CosmosItemSerializer defaultCustomSerializer,
                                boolean isRegionScopedSessionCapturingEnabled,
                                List<CosmosOperationPolicy> operationPolicies,
                                boolean isPerPartitionAutomaticFailoverEnabled) {
        this(
                serviceEndpoint,
                masterKeyOrResourceToken,
                permissionFeed,
                connectionPolicy,
                consistencyLevel,
                readConsistencyStrategy,
                configs,
                credential,
                tokenCredential,
                sessionCapturingOverride,
                connectionSharingAcrossClientsEnabled,
                contentResponseOnWriteEnabled,
                metadataCachesSnapshot,
                apiType,
                clientTelemetryConfig,
                clientCorrelationId,
                cosmosEndToEndOperationLatencyPolicyConfig,
                sessionRetryOptions,
                containerProactiveInitConfig,
                defaultCustomSerializer,
                isRegionScopedSessionCapturingEnabled
        );

        this.cosmosAuthorizationTokenResolver = cosmosAuthorizationTokenResolver;
        this.operationPolicies = operationPolicies;
    }

    private RxDocumentClientImpl(URI serviceEndpoint,
                                String masterKeyOrResourceToken,
                                List<Permission> permissionFeed,
                                ConnectionPolicy connectionPolicy,
                                ConsistencyLevel consistencyLevel,
                                ReadConsistencyStrategy readConsistencyStrategy,
                                Configs configs,
                                AzureKeyCredential credential,
                                TokenCredential tokenCredential,
                                boolean sessionCapturingOverrideEnabled,
                                boolean connectionSharingAcrossClientsEnabled,
                                boolean contentResponseOnWriteEnabled,
                                CosmosClientMetadataCachesSnapshot metadataCachesSnapshot,
                                ApiType apiType,
                                CosmosClientTelemetryConfig clientTelemetryConfig,
                                String clientCorrelationId,
                                CosmosEndToEndOperationLatencyPolicyConfig cosmosEndToEndOperationLatencyPolicyConfig,
                                SessionRetryOptions sessionRetryOptions,
                                CosmosContainerProactiveInitConfig containerProactiveInitConfig,
                                CosmosItemSerializer defaultCustomSerializer,
                                boolean isRegionScopedSessionCapturingEnabled) {
        this(
                serviceEndpoint,
                masterKeyOrResourceToken,
                connectionPolicy,
                consistencyLevel,
                readConsistencyStrategy,
                configs,
                credential,
                tokenCredential,
                sessionCapturingOverrideEnabled,
                connectionSharingAcrossClientsEnabled,
                contentResponseOnWriteEnabled,
                metadataCachesSnapshot,
                apiType,
                clientTelemetryConfig,
                clientCorrelationId,
                cosmosEndToEndOperationLatencyPolicyConfig,
                sessionRetryOptions,
                containerProactiveInitConfig,
                defaultCustomSerializer,
                isRegionScopedSessionCapturingEnabled
        );

        if (permissionFeed != null && permissionFeed.size() > 0) {
            this.resourceTokensMap = new HashMap<>();
            for (Permission permission : permissionFeed) {
                String[] segments = StringUtils.split(permission.getResourceLink(),
                        Constants.Properties.PATH_SEPARATOR.charAt(0));

                if (segments.length == 0) {
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
                         ReadConsistencyStrategy readConsistencyStrategy,
                         Configs configs,
                         AzureKeyCredential credential,
                         TokenCredential tokenCredential,
                         boolean sessionCapturingOverrideEnabled,
                         boolean connectionSharingAcrossClientsEnabled,
                         boolean contentResponseOnWriteEnabled,
                         CosmosClientMetadataCachesSnapshot metadataCachesSnapshot,
                         ApiType apiType,
                         CosmosClientTelemetryConfig clientTelemetryConfig,
                         String clientCorrelationId,
                         CosmosEndToEndOperationLatencyPolicyConfig cosmosEndToEndOperationLatencyPolicyConfig,
                         SessionRetryOptions sessionRetryOptions,
                         CosmosContainerProactiveInitConfig containerProactiveInitConfig,
                         CosmosItemSerializer defaultCustomSerializer,
                         boolean isRegionScopedSessionCapturingEnabled) {

        assert(clientTelemetryConfig != null);
        activeClientsCnt.incrementAndGet();
        this.clientId = clientIdGenerator.incrementAndGet();
        this.clientCorrelationId = Strings.isNullOrWhiteSpace(clientCorrelationId) ?
            String.format("%05d",this.clientId): clientCorrelationId;
        clientMap.put(serviceEndpoint.toString(), clientMap.getOrDefault(serviceEndpoint.toString(), 0) + 1);
        this.diagnosticsClientConfig = new DiagnosticsClientConfig();
        this.diagnosticsClientConfig.withClientId(this.clientId);
        this.diagnosticsClientConfig.withActiveClientCounter(activeClientsCnt);
        this.diagnosticsClientConfig.withClientMap(clientMap);

        this.diagnosticsClientConfig.withConnectionSharingAcrossClientsEnabled(connectionSharingAcrossClientsEnabled);
        this.diagnosticsClientConfig.withConsistency(consistencyLevel).withReadConsistencyStrategy(readConsistencyStrategy);
        this.throughputControlEnabled = new AtomicBoolean(false);
        this.cosmosEndToEndOperationLatencyPolicyConfig = cosmosEndToEndOperationLatencyPolicyConfig;
        this.diagnosticsClientConfig.withEndToEndOperationLatencyPolicy(cosmosEndToEndOperationLatencyPolicyConfig);
        this.sessionRetryOptions = sessionRetryOptions;
        this.defaultCustomSerializer = defaultCustomSerializer;

        logger.info(
            "Initializing DocumentClient [{}] with"
                + " serviceEndpoint [{}], connectionPolicy [{}], consistencyLevel [{}], readConsistencyStrategy [{}]",
            this.clientId, serviceEndpoint, connectionPolicy, consistencyLevel, readConsistencyStrategy);

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
                    String scopeOverride = Configs.getAadScopeOverride();
                    String defaultScope = serviceEndpoint.getScheme() + "://" + serviceEndpoint.getHost() + "/.default";
                    String scopeToUse = (scopeOverride != null && !scopeOverride.isEmpty()) ? scopeOverride : defaultScope;

                    this.tokenCredentialScopes = new String[] { scopeToUse };
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
            this.diagnosticsClientConfig.withConnectionPolicy(this.connectionPolicy);
            this.diagnosticsClientConfig.withMultipleWriteRegionsEnabled(this.connectionPolicy.isMultipleWriteRegionsEnabled());
            this.diagnosticsClientConfig.withEndpointDiscoveryEnabled(this.connectionPolicy.isEndpointDiscoveryEnabled());
            this.diagnosticsClientConfig.withPreferredRegions(this.connectionPolicy.getPreferredRegions());
            this.diagnosticsClientConfig.withMachineId(tempMachineId);
            this.diagnosticsClientConfig.withProactiveContainerInitConfig(containerProactiveInitConfig);
            this.diagnosticsClientConfig.withSessionRetryOptions(sessionRetryOptions);

            this.sessionCapturingOverrideEnabled = sessionCapturingOverrideEnabled;
            boolean disableSessionCapturing = (ConsistencyLevel.SESSION != consistencyLevel
                && ReadConsistencyStrategy.SESSION != readConsistencyStrategy
                && !sessionCapturingOverrideEnabled);
            this.sessionCapturingDisabled = disableSessionCapturing;

            this.consistencyLevel = consistencyLevel;
            this.readConsistencyStrategy = readConsistencyStrategy;

            this.userAgentContainer = new UserAgentContainer();

            String userAgentSuffix = this.connectionPolicy.getUserAgentSuffix();

            if (userAgentSuffix != null && !userAgentSuffix.isEmpty()) {
                userAgentContainer.setSuffix(userAgentSuffix);
            }

            this.httpClientInterceptor = null;
            this.reactorHttpClient = httpClient();

            this.globalEndpointManager = new GlobalEndpointManager(asDatabaseAccountManagerInternal(), this.connectionPolicy, configs);
            this.isRegionScopedSessionCapturingEnabledOnClientOrSystemConfig = isRegionScopedSessionCapturingEnabled;

            this.sessionContainer = new SessionContainer(this.serviceEndpoint.getHost(), disableSessionCapturing);

            this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker
                = new GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker(this.globalEndpointManager);

            // enablement of PPAF is revaluated in RxDocumentClientImpl#init
            this.globalPartitionEndpointManagerForPerPartitionAutomaticFailover
                = new GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover(this.globalEndpointManager, false);

            this.cachedCosmosAsyncClientSnapshot = new AtomicReference<>();

            this.retryPolicy = new RetryPolicy(
                this,
                this.globalEndpointManager,
                this.connectionPolicy,
                this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker,
                this.globalPartitionEndpointManagerForPerPartitionAutomaticFailover);
            this.resetSessionTokenRetryPolicy = retryPolicy;
            CpuMemoryMonitor.register(this);
            this.queryPlanCache = new ConcurrentHashMap<>();
            this.apiType = apiType;
            this.clientTelemetryConfig = clientTelemetryConfig;
            this.useThinClient = Configs.isThinClientEnabled()
                && this.connectionPolicy.getConnectionMode() == ConnectionMode.GATEWAY
                && this.connectionPolicy.getHttp2ConnectionConfig() != null
                && ImplementationBridgeHelpers
                    .Http2ConnectionConfigHelper
                    .getHttp2ConnectionConfigAccessor()
                    .isEffectivelyEnabled(
                        this.connectionPolicy.getHttp2ConnectionConfig()
                    );
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
       CosmosDiagnostics diagnostics =
           diagnosticsAccessor.create(this, telemetryCfgAccessor.getSamplingRate(this.clientTelemetryConfig));

       this.mostRecentlyCreatedDiagnostics.set(diagnostics);

       return diagnostics;
    }
    private DatabaseAccount initializeGatewayConfigurationReader() {
        this.gatewayConfigurationReader = new GatewayServiceConfigurationReader(this.globalEndpointManager);
        DatabaseAccount databaseAccount = this.globalEndpointManager.getLatestDatabaseAccount();
        //Database account should not be null here,
        // this.globalEndpointManager.init() must have been already called
        // hence asserting it
        if (databaseAccount == null) {
            Throwable databaseRefreshErrorSnapshot = this.globalEndpointManager.getLatestDatabaseRefreshError();
            if (databaseRefreshErrorSnapshot != null) {
                logger.error("Client initialization failed. Check if the endpoint is reachable and if your auth token "
                        + "is valid. More info: https://aka.ms/cosmosdb-tsg-service-unavailable-java. More details: "+ databaseRefreshErrorSnapshot.getMessage(),
                    databaseRefreshErrorSnapshot
                );

                throw new RuntimeException("Client initialization failed. Check if the endpoint is reachable and if your auth token "
                    + "is valid. More info: https://aka.ms/cosmosdb-tsg-service-unavailable-java. More details: "+ databaseRefreshErrorSnapshot.getMessage(),
                    databaseRefreshErrorSnapshot);
            } else {
                logger.error("Client initialization failed."
                    + " Check if the endpoint is reachable and if your auth token is valid. More info: https://aka.ms/cosmosdb-tsg-service-unavailable-java");

                throw new RuntimeException("Client initialization failed. Check if the endpoint is reachable and if your auth token "
                    + "is valid. More info: https://aka.ms/cosmosdb-tsg-service-unavailable-java.");
            }
        }

        this.useMultipleWriteLocations = this.connectionPolicy.isMultipleWriteRegionsEnabled() && BridgeInternal.isEnableMultipleWriteLocations(databaseAccount);
        return databaseAccount;
    }

    private void resetSessionContainerIfNeeded(DatabaseAccount databaseAccount) {
        boolean isRegionScopingOfSessionTokensPossible = this.isRegionScopingOfSessionTokensPossible(databaseAccount, this.useMultipleWriteLocations, this.isRegionScopedSessionCapturingEnabledOnClientOrSystemConfig);

        if (isRegionScopingOfSessionTokensPossible) {
            this.sessionContainer = new RegionScopedSessionContainer(this.serviceEndpoint.getHost(), this.sessionCapturingDisabled, this.globalEndpointManager);
            this.diagnosticsClientConfig.withRegionScopedSessionContainerOptions((RegionScopedSessionContainer) this.sessionContainer);
        }
    }

    private boolean isRegionScopingOfSessionTokensPossible(DatabaseAccount databaseAccount, boolean useMultipleWriteLocations, boolean isRegionScopedSessionCapturingEnabled) {

        if (!isRegionScopedSessionCapturingEnabled) {
            return false;
        }

        if (!useMultipleWriteLocations) {
            return false;
        }

        Iterable<DatabaseAccountLocation> readableLocationsIterable = databaseAccount.getReadableLocations();
        Iterator<DatabaseAccountLocation> readableLocationsIterator = readableLocationsIterable.iterator();

        while (readableLocationsIterator.hasNext()) {
            DatabaseAccountLocation readableLocation = readableLocationsIterator.next();

            String normalizedReadableRegion = readableLocation.getName().toLowerCase(Locale.ROOT).trim().replace(" ", "");

            if (RegionNameToRegionIdMap.getRegionId(normalizedReadableRegion) == -1) {
                return false;
            }
        }

        return true;
    }

    private void updateGatewayProxy() {
        (this.gatewayProxy).setGatewayServiceConfigurationReader(this.gatewayConfigurationReader);
        (this.gatewayProxy).setCollectionCache(this.collectionCache);
        (this.gatewayProxy).setPartitionKeyRangeCache(this.partitionKeyRangeCache);
        (this.gatewayProxy).setUseMultipleWriteLocations(this.useMultipleWriteLocations);
        (this.gatewayProxy).setSessionContainer(this.sessionContainer);
    }

    private void updateThinProxy() {
        (this.thinProxy).setGatewayServiceConfigurationReader(this.gatewayConfigurationReader);
        (this.thinProxy).setCollectionCache(this.collectionCache);
        (this.thinProxy).setPartitionKeyRangeCache(this.partitionKeyRangeCache);
        (this.thinProxy).setUseMultipleWriteLocations(this.useMultipleWriteLocations);
        (this.thinProxy).setSessionContainer(this.sessionContainer);
    }

    public void init(CosmosClientMetadataCachesSnapshot metadataCachesSnapshot, Function<HttpClient, HttpClient> httpClientInterceptor) {
        try {

            this.httpClientInterceptor = httpClientInterceptor;
            if (httpClientInterceptor != null) {
                this.reactorHttpClient = httpClientInterceptor.apply(httpClient());
            }

            this.gatewayProxy = createRxGatewayProxy(this.sessionContainer,
                this.consistencyLevel,
                this.queryCompatibilityMode,
                this.userAgentContainer,
                this.globalEndpointManager,
                this.reactorHttpClient,
                this.apiType);

            this.thinProxy = createThinProxy(this.sessionContainer,
                this.consistencyLevel,
                this.userAgentContainer,
                this.globalEndpointManager,
                this.reactorHttpClient);

            this.globalEndpointManager.init();

            DatabaseAccount databaseAccountSnapshot = this.initializeGatewayConfigurationReader();
            this.resetSessionContainerIfNeeded(databaseAccountSnapshot);

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

            updateGatewayProxy();
            updateThinProxy();
            clientTelemetry = new ClientTelemetry(
                    this,
                    null,
                    UUIDs.nonBlockingRandomUUID().toString(),
                    ManagementFactory.getRuntimeMXBean().getName(),
                    connectionPolicy.getConnectionMode(),
                    globalEndpointManager.getLatestDatabaseAccount().getId(),
                    null,
                    null,
                    this.configs,
                    this.clientTelemetryConfig,
                    this.connectionPolicy.getPreferredRegions());
            clientTelemetry.init().thenEmpty((publisher) -> {
                logger.warn(
                    "Initialized DocumentClient [{}] with machineId[{}]"
                        + " serviceEndpoint [{}], connectionPolicy [{}], consistencyLevel [{}], readConsistencyStrategy [{}]",
                    clientId,
                    ClientTelemetry.getMachineId(diagnosticsClientConfig),
                    serviceEndpoint,
                    connectionPolicy,
                    consistencyLevel,
                    readConsistencyStrategy);
            }).subscribe();
            if (this.connectionPolicy.getConnectionMode() == ConnectionMode.GATEWAY) {
                this.storeModel = this.gatewayProxy;
            } else {
                this.initializeDirectConnectivity();
            }
            this.retryPolicy.setRxCollectionCache(this.collectionCache);
            ConsistencyLevel effectiveConsistencyLevel = consistencyLevel != null
                ? consistencyLevel
                : this.getDefaultConsistencyLevelOfAccount();
            boolean updatedDisableSessionCapturing =
                (ConsistencyLevel.SESSION != effectiveConsistencyLevel
                    && readConsistencyStrategy != ReadConsistencyStrategy.SESSION
                    && !sessionCapturingOverrideEnabled);
            this.sessionContainer.setDisableSessionCapturing(updatedDisableSessionCapturing);
            this.initializePerPartitionFailover(databaseAccountSnapshot);
            this.addUserAgentSuffix(this.userAgentContainer, EnumSet.allOf(UserAgentFeatureFlags.class));
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
            this.connectionPolicy,
            this.apiType);

        this.storeClientFactory = new StoreClientFactory(
            this.addressResolver,
            this.diagnosticsClientConfig,
            this.configs,
            this.connectionPolicy,
            // this.maxConcurrentConnectionOpenRequests,
            this.userAgentContainer,
            this.connectionSharingAcrossClientsEnabled,
            this.clientTelemetry,
            this.globalEndpointManager);

        this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker.setGlobalAddressResolver(this.addressResolver);
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
                logger.info("Getting database account endpoint from {} - useThinClient: {}", endpoint, useThinClient);
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
                                             HttpClient httpClient,
                                             ApiType apiType) {
        return new RxGatewayStoreModel(
                this,
                sessionContainer,
                consistencyLevel,
                queryCompatibilityMode,
                userAgentContainer,
                globalEndpointManager,
                httpClient,
                apiType);
    }

    ThinClientStoreModel createThinProxy(ISessionContainer sessionContainer,
                                         ConsistencyLevel consistencyLevel,
                                         UserAgentContainer userAgentContainer,
                                         GlobalEndpointManager globalEndpointManager,
                                         HttpClient httpClient) {
        return new ThinClientStoreModel(
            this,
            sessionContainer,
            consistencyLevel,
            userAgentContainer,
            globalEndpointManager,
            httpClient);
    }

    private HttpClient httpClient() {
        HttpClientConfig httpClientConfig = new HttpClientConfig(this.configs)
            .withMaxIdleConnectionTimeout(this.connectionPolicy.getIdleHttpConnectionTimeout())
            .withPoolSize(this.connectionPolicy.getMaxConnectionPoolSize())
            .withProxy(this.connectionPolicy.getProxy())
            .withNetworkRequestTimeout(this.connectionPolicy.getHttpNetworkRequestTimeout())
            .withServerCertValidationDisabled(this.connectionPolicy.isServerCertValidationDisabled())
            .withHttp2ConnectionConfig(this.connectionPolicy.getHttp2ConnectionConfig());

        if (connectionSharingAcrossClientsEnabled) {
            return SharedGatewayHttpClient.getOrCreateInstance(httpClientConfig, diagnosticsClientConfig);
        } else {
            diagnosticsClientConfig.withGatewayHttpClientConfig(httpClientConfig.toDiagnosticsString());
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
                this.useMultipleWriteLocations,
                this.sessionRetryOptions);

        this.storeModel = new ServerStoreModel(storeClient);
    }


    @Override
    public URI getServiceEndpoint() {
        return this.serviceEndpoint;
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
    public ReadConsistencyStrategy getReadConsistencyStrategy() {
        return readConsistencyStrategy;
    }

    @Override
    public ClientTelemetry getClientTelemetry() {
        return this.clientTelemetry;
    }

    @Override
    public String getClientCorrelationId() {
        return this.clientCorrelationId;
    }

    @Override
    public String getMachineId() {
        if (this.diagnosticsClientConfig == null) {
            return null;
        }

        return ClientTelemetry.getMachineId(diagnosticsClientConfig);
    }

    @Override
    public String getUserAgent() {
        return this.userAgentContainer.getUserAgent();
    }

    @Override
    public CosmosDiagnostics getMostRecentlyCreatedDiagnostics() {
        return mostRecentlyCreatedDiagnostics.get();
    }

    @Override
    public Mono<ResourceResponse<Database>> createDatabase(Database database, RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
            ByteBuffer byteBuffer = database.serializeJsonToByteBuffer(
                DefaultCosmosItemSerializer.INTERNAL_DEFAULT_SERIALIZER,
                null,
                false);
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
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
    public Flux<FeedResponse<Database>> readDatabases(QueryFeedOperationState state) {
        return nonDocumentReadFeed(state, ResourceType.Database, Database.class, Paths.DATABASES_ROOT);
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
        return qryOptAccessor.getImpl(options).getOperationContextAndListenerTuple();
    }

    private OperationContextAndListenerTuple getOperationContextAndListenerTuple(RequestOptions options) {
        if (options == null) {
            return null;
        }
        return options.getOperationContextAndListenerTuple();
    }

    private <T> Flux<FeedResponse<T>> createQuery(
        String parentResourceLink,
        SqlQuerySpec sqlQuery,
        QueryFeedOperationState state,
        Class<T> klass,
        ResourceType resourceTypeEnum) {

        return createQuery(parentResourceLink, sqlQuery, state, klass, resourceTypeEnum, this);
    }

    private <T> Flux<FeedResponse<T>> createQuery(
        String parentResourceLink,
        SqlQuerySpec sqlQuery,
        QueryFeedOperationState state,
        Class<T> klass,
        ResourceType resourceTypeEnum,
        DiagnosticsClientContext innerDiagnosticsFactory) {

        String resourceLink = parentResourceLinkToQueryLink(parentResourceLink, resourceTypeEnum);

        CosmosQueryRequestOptions nonNullQueryOptions = state.getQueryOptions();

        UUID correlationActivityIdOfRequestOptions = qryOptAccessor
            .getImpl(nonNullQueryOptions)
            .getCorrelationActivityId();
        UUID correlationActivityId = correlationActivityIdOfRequestOptions != null ?
            correlationActivityIdOfRequestOptions : UUIDs.nonBlockingRandomUUID();

        final AtomicBoolean isQueryCancelledOnTimeout = new AtomicBoolean(false);

        IDocumentQueryClient queryClient = documentQueryClientImpl(RxDocumentClientImpl.this, getOperationContextAndListenerTuple(nonNullQueryOptions));

        final ScopedDiagnosticsFactory diagnosticsFactory = new ScopedDiagnosticsFactory(innerDiagnosticsFactory, false);
        state.registerDiagnosticsFactory(
            diagnosticsFactory::reset,
            diagnosticsFactory::merge);

        // Trying to put this logic as low as the query pipeline
        // Since for parallelQuery, each partition will have its own request, so at this point, there will be no request associate with this retry policy.
        StaleResourceRetryPolicy staleResourceRetryPolicy = new StaleResourceRetryPolicy(
            this.collectionCache,
            null,
            resourceLink,
            qryOptAccessor.getProperties(nonNullQueryOptions),
            qryOptAccessor.getHeaders(nonNullQueryOptions),
            this.sessionContainer,
            diagnosticsFactory);

        return
            ObservableHelper.fluxInlineIfPossibleAsObs(
                                () -> createQueryInternal(
                                    diagnosticsFactory,
                                    resourceLink,
                                    sqlQuery,
                                    state.getQueryOptions(),
                                    klass,
                                    resourceTypeEnum,
                                    queryClient,
                                    correlationActivityId,
                                    isQueryCancelledOnTimeout),
                    staleResourceRetryPolicy
                            ).flatMap(result -> {
                                diagnosticsFactory.merge(state.getDiagnosticsContextSnapshot());
                                return Mono.just(result);
                            })
                            .onErrorMap(throwable -> {
                                diagnosticsFactory.merge(state.getDiagnosticsContextSnapshot());
                                return throwable;
                            })
                            .doOnCancel(() -> diagnosticsFactory.merge(state.getDiagnosticsContextSnapshot()));
    }

    private <T> Flux<FeedResponse<T>> createQueryInternal(
            DiagnosticsClientContext diagnosticsClientContext,
            String resourceLink,
            SqlQuerySpec sqlQuery,
            CosmosQueryRequestOptions options,
            Class<T> klass,
            ResourceType resourceTypeEnum,
            IDocumentQueryClient queryClient,
            UUID activityId,
            final AtomicBoolean isQueryCancelledOnTimeout) {

        Flux<? extends IDocumentQueryExecutionContext<T>> executionContext =
            DocumentQueryExecutionContextFactory
                .createDocumentQueryExecutionContextAsync(diagnosticsClientContext, queryClient, resourceTypeEnum, klass, sqlQuery,
                                                          options, resourceLink, false, activityId,
                                                          Configs.isQueryPlanCachingEnabled(), queryPlanCache, isQueryCancelledOnTimeout);

        AtomicBoolean isFirstResponse = new AtomicBoolean(true);
        return executionContext.flatMap(iDocumentQueryExecutionContext -> {
            QueryInfo queryInfo = null;
            if (iDocumentQueryExecutionContext instanceof PipelinedQueryExecutionContextBase) {
                queryInfo = ((PipelinedQueryExecutionContextBase<T>) iDocumentQueryExecutionContext).getQueryInfo();
            }

            QueryInfo finalQueryInfo = queryInfo;
            Flux<FeedResponse<T>> feedResponseFlux = iDocumentQueryExecutionContext.executeAsync()
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

            RequestOptions requestOptions = options == null? null : ImplementationBridgeHelpers
                .CosmosQueryRequestOptionsHelper
                .getCosmosQueryRequestOptionsAccessor()
                .toRequestOptions(options);

            CosmosEndToEndOperationLatencyPolicyConfig endToEndPolicyConfig =
                getEndToEndOperationLatencyPolicyConfig(requestOptions, resourceTypeEnum, OperationType.Query);

            if (endToEndPolicyConfig != null && endToEndPolicyConfig.isEnabled()) {
                return getFeedResponseFluxWithTimeout(
                    feedResponseFlux,
                    endToEndPolicyConfig,
                    options,
                    isQueryCancelledOnTimeout,
                    diagnosticsClientContext);
            }

            return feedResponseFlux;
            // concurrency is set to Queues.SMALL_BUFFER_SIZE to
            // maximize the IDocumentQueryExecutionContext publisher instances to subscribe to concurrently
            // prefetch is set to 1 to minimize the no. prefetched pages (result of merged executeAsync invocations)
        }, Queues.SMALL_BUFFER_SIZE, 1);
    }

    private static void applyExceptionToMergedDiagnosticsForQuery(
        CosmosQueryRequestOptions requestOptions,
        CosmosException exception,
        DiagnosticsClientContext diagnosticsClientContext) {

         CosmosDiagnostics mostRecentlyCreatedDiagnostics =
            diagnosticsClientContext.getMostRecentlyCreatedDiagnostics();

        if (mostRecentlyCreatedDiagnostics != null) {
            // When reaching here, it means the query(s) has timed out based on the e2e timeout config policy
            // Since all the underlying ongoing query requests will all timed out
            // We just use the last cosmosDiagnostics in the scoped diagnostics factory to populate the exception
            BridgeInternal.setCosmosDiagnostics(
                exception,
                mostRecentlyCreatedDiagnostics);
        } else {
            List<CosmosDiagnostics> cancelledRequestDiagnostics =
                qryOptAccessor
                    .getCancelledRequestDiagnosticsTracker(requestOptions);
            // if there is any cancelled requests, collect cosmos diagnostics
            if (cancelledRequestDiagnostics != null && !cancelledRequestDiagnostics.isEmpty()) {
                // combine all the cosmos diagnostics
                CosmosDiagnostics aggregratedCosmosDiagnostics =
                    cancelledRequestDiagnostics
                        .stream()
                        .reduce((first, toBeMerged) -> {
                            ClientSideRequestStatistics clientSideRequestStatistics =
                                ImplementationBridgeHelpers
                                    .CosmosDiagnosticsHelper
                                    .getCosmosDiagnosticsAccessor()
                                    .getClientSideRequestStatisticsRaw(first);

                            ClientSideRequestStatistics toBeMergedClientSideRequestStatistics =
                                ImplementationBridgeHelpers
                                    .CosmosDiagnosticsHelper
                                    .getCosmosDiagnosticsAccessor()
                                    .getClientSideRequestStatisticsRaw(first);

                            if (clientSideRequestStatistics == null) {
                                return toBeMerged;
                            } else {
                                clientSideRequestStatistics.mergeClientSideRequestStatistics(toBeMergedClientSideRequestStatistics);
                                return first;
                            }
                        })
                        .get();

                BridgeInternal.setCosmosDiagnostics(exception, aggregratedCosmosDiagnostics);
            }
        }
    }

    private static <T> Flux<FeedResponse<T>> getFeedResponseFluxWithTimeout(
        Flux<FeedResponse<T>> feedResponseFlux,
        CosmosEndToEndOperationLatencyPolicyConfig endToEndPolicyConfig,
        CosmosQueryRequestOptions requestOptions,
        final AtomicBoolean isQueryCancelledOnTimeout,
        DiagnosticsClientContext diagnosticsClientContext) {

        Duration endToEndTimeout = endToEndPolicyConfig.getEndToEndOperationTimeout();

        if (endToEndTimeout.isNegative()) {
            return feedResponseFlux
                .timeout(endToEndTimeout)
                .onErrorMap(throwable -> {
                    if (throwable instanceof TimeoutException) {
                        CosmosException cancellationException = getNegativeTimeoutException(null, endToEndTimeout);
                        cancellationException.setStackTrace(throwable.getStackTrace());

                        isQueryCancelledOnTimeout.set(true);

                        applyExceptionToMergedDiagnosticsForQuery(
                            requestOptions, cancellationException, diagnosticsClientContext);

                        return cancellationException;
                    }
                    return throwable;
                });
        }

        return feedResponseFlux
            .timeout(endToEndTimeout)
            .onErrorMap(throwable -> {
                if (throwable instanceof TimeoutException) {
                    CosmosException exception = new OperationCancelledException();
                    exception.setStackTrace(throwable.getStackTrace());

                    isQueryCancelledOnTimeout.set(true);

                    applyExceptionToMergedDiagnosticsForQuery(requestOptions, exception, diagnosticsClientContext);

                    return exception;
                }
                return throwable;
            });
    }

    private void addUserAgentSuffix(UserAgentContainer userAgentContainer, Set<UserAgentFeatureFlags> userAgentFeatureFlags) {

        if (!this.globalPartitionEndpointManagerForPerPartitionAutomaticFailover.isPerPartitionAutomaticFailoverEnabled()) {
            userAgentFeatureFlags.remove(UserAgentFeatureFlags.PerPartitionAutomaticFailover);
        }

        if (!this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker.getCircuitBreakerConfig().isPartitionLevelCircuitBreakerEnabled()) {
            userAgentFeatureFlags.remove(UserAgentFeatureFlags.PerPartitionCircuitBreaker);
        }

        userAgentContainer.setFeatureEnabledFlagsAsSuffix(userAgentFeatureFlags);
    }

    @Override
    public Flux<FeedResponse<Database>> queryDatabases(String query, QueryFeedOperationState state) {
        return queryDatabases(new SqlQuerySpec(query), state);
    }


    @Override
    public Flux<FeedResponse<Database>> queryDatabases(SqlQuerySpec querySpec, QueryFeedOperationState state) {
        return createQuery(Paths.DATABASES_ROOT, querySpec, state, Database.class, ResourceType.Database);
    }

    @Override
    public Mono<ResourceResponse<DocumentCollection>> createCollection(String databaseLink,
                                                                       DocumentCollection collection, RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
            ByteBuffer byteBuffer = collection.serializeJsonToByteBuffer(
                DefaultCosmosItemSerializer.INTERNAL_DEFAULT_SERIALIZER,
                null,
                false);
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
                    this.sessionContainer.setSessionToken(
                        request,
                        resourceResponse.getResource().getResourceId(),
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
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
            ByteBuffer byteBuffer = collection.serializeJsonToByteBuffer(
                DefaultCosmosItemSerializer.INTERNAL_DEFAULT_SERIALIZER,
                null,
                false);
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
                        this.sessionContainer.setSessionToken(
                            request,
                            resourceResponse.getResource().getResourceId(),
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
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
        return populateHeadersAsync(request, RequestVerb.DELETE)
            .flatMap(requestPopulated -> {
                if (documentClientRetryPolicy.getRetryContext() != null && documentClientRetryPolicy.getRetryContext().getRetryCount() > 0) {
                    documentClientRetryPolicy.getRetryContext().updateEndTime();
                }

                return getStoreProxy(requestPopulated).processMessage(requestPopulated, operationContextAndListenerTuple);
            });
    }

    private Mono<RxDocumentServiceResponse> deleteAllItemsByPartitionKey(RxDocumentServiceRequest request, DocumentClientRetryPolicy documentClientRetryPolicy, OperationContextAndListenerTuple operationContextAndListenerTuple) {
        return populateHeadersAsync(request, RequestVerb.POST)
            .flatMap(requestPopulated -> {
                RxStoreModel storeProxy = this.getStoreProxy(requestPopulated);
                if (documentClientRetryPolicy.getRetryContext() != null && documentClientRetryPolicy.getRetryContext().getRetryCount() > 0) {
                    documentClientRetryPolicy.getRetryContext().updateEndTime();
                }

                return storeProxy.processMessage(requestPopulated, operationContextAndListenerTuple);
            });
    }

    private Mono<RxDocumentServiceResponse> read(RxDocumentServiceRequest request, DocumentClientRetryPolicy documentClientRetryPolicy) {
        return populateHeadersAsync(request, RequestVerb.GET)
            .flatMap(requestPopulated -> {
                if (documentClientRetryPolicy.getRetryContext() != null && documentClientRetryPolicy.getRetryContext().getRetryCount() > 0) {
                    documentClientRetryPolicy.getRetryContext().updateEndTime();
                }

                return getStoreProxy(requestPopulated).processMessage(requestPopulated);
                });
    }

    Mono<RxDocumentServiceResponse> readFeed(RxDocumentServiceRequest request) {
        return populateHeadersAsync(request, RequestVerb.GET)
            .flatMap(requestPopulated -> getStoreProxy(requestPopulated).processMessage(requestPopulated));
    }

    private Mono<RxDocumentServiceResponse> query(RxDocumentServiceRequest request) {
        // If endToEndOperationLatencyPolicy is set in the query request options,
        // then it should have been populated into request context already
        // otherwise set them here with the client level policy

        return populateHeadersAsync(request, RequestVerb.POST)
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
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
    public Flux<FeedResponse<DocumentCollection>> readCollections(String databaseLink, QueryFeedOperationState state) {

        if (StringUtils.isEmpty(databaseLink)) {
            throw new IllegalArgumentException("databaseLink");
        }

        return nonDocumentReadFeed(state, ResourceType.DocumentCollection, DocumentCollection.class,
                Utils.joinPath(databaseLink, Paths.COLLECTIONS_PATH_SEGMENT));
    }

    @Override
    public Flux<FeedResponse<DocumentCollection>> queryCollections(String databaseLink, String query,
                                                                   QueryFeedOperationState state) {
        return createQuery(databaseLink, new SqlQuerySpec(query), state, DocumentCollection.class, ResourceType.DocumentCollection);
    }

    @Override
    public Flux<FeedResponse<DocumentCollection>> queryCollections(String databaseLink,
                                                                         SqlQuerySpec querySpec, QueryFeedOperationState state) {
        return createQuery(databaseLink, querySpec, state, DocumentCollection.class, ResourceType.DocumentCollection);
    }

    private static String serializeProcedureParams(List<Object> objectArray) {
        String[] stringArray = new String[objectArray.size()];

        for (int i = 0; i < objectArray.size(); ++i) {
            Object object = objectArray.get(i);
            if (object instanceof JsonSerializable) {
                stringArray[i] = ((JsonSerializable) object).toJson();
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

    public void validateAndLogNonDefaultReadConsistencyStrategy(String readConsistencyStrategyName) {
        if (this.connectionPolicy.getConnectionMode() != ConnectionMode.DIRECT
            && readConsistencyStrategyName != null
            && ! readConsistencyStrategyName.equalsIgnoreCase(ReadConsistencyStrategy.DEFAULT.toString())) {

            logger.warn(
                "ReadConsistencyStrategy {} defined in Gateway mode. "
                    + "This version of the SDK only supports ReadConsistencyStrategy in DIRECT mode. "
                    + "This setting will be ignored.",
                readConsistencyStrategyName);
        }
    }

    private Map<String, String> getRequestHeaders(RequestOptions options, ResourceType resourceType, OperationType operationType) {
        Map<String, String> headers = new HashMap<>();

        if (this.useMultipleWriteLocations) {
            headers.put(HttpConstants.HttpHeaders.ALLOW_TENTATIVE_WRITES, Boolean.TRUE.toString());
        }

        if (consistencyLevel != null) {
            // adding the "x-ms-consistency-level" header with consistency level stricter than the
            // account's default consistency level in Compute Gateway will result in a 400 Bad Request
            // even when it is done for resource types / operations where this header should simply be ignored
            // making the change here to restrict adding the header to when it is relevant.
            if ((operationType.isReadOnlyOperation() || operationType == OperationType.Batch) && (resourceType.isMasterResource() || resourceType == ResourceType.Document)) {
                headers.put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, consistencyLevel.toString());
            }
        }

        if (readConsistencyStrategy != null
            && readConsistencyStrategy != ReadConsistencyStrategy.DEFAULT
            && resourceType == ResourceType.Document
            && operationType.isReadOnlyOperation()) {

            String readConsistencyStrategyName = readConsistencyStrategy.toString();
            this.validateAndLogNonDefaultReadConsistencyStrategy(readConsistencyStrategyName);
            headers.put(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY, readConsistencyStrategyName);
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

        if (options.getIfNoneMatchETag() != null) {
            headers.put(HttpConstants.HttpHeaders.IF_NONE_MATCH, options.getIfNoneMatchETag());
        }

        if (options.getReadConsistencyStrategy() != null
            && options.getReadConsistencyStrategy() != ReadConsistencyStrategy.DEFAULT
            && resourceType == ResourceType.Document
            && operationType.isReadOnlyOperation()) {

            String readConsistencyStrategyName = options.getReadConsistencyStrategy().toString();
            this.validateAndLogNonDefaultReadConsistencyStrategy(readConsistencyStrategyName);
            headers.put(
                HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY,
                readConsistencyStrategyName);
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
                        offer.getOfferAutoScaleSettings().toJson());
                }
            }
        }

        if (options.isQuotaInfoEnabled()) {
            headers.put(HttpConstants.HttpHeaders.POPULATE_QUOTA_INFO, String.valueOf(true));
        }

        if (options.isScriptLoggingEnabled()) {
            headers.put(HttpConstants.HttpHeaders.SCRIPT_ENABLE_LOGGING, String.valueOf(true));
        }

        if (options.getDedicatedGatewayRequestOptions() != null) {
            if (options.getDedicatedGatewayRequestOptions().getMaxIntegratedCacheStaleness() != null) {
                headers.put(HttpConstants.HttpHeaders.DEDICATED_GATEWAY_PER_REQUEST_CACHE_STALENESS,
                    String.valueOf(Utils.getMaxIntegratedCacheStalenessInMillis(options.getDedicatedGatewayRequestOptions())));
            }
            if (options.getDedicatedGatewayRequestOptions().isIntegratedCacheBypassed()) {
                headers.put(HttpConstants.HttpHeaders.DEDICATED_GATEWAY_PER_REQUEST_BYPASS_CACHE,
                    String.valueOf(options.getDedicatedGatewayRequestOptions().isIntegratedCacheBypassed()));
            }
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
                    addPartitionKeyInformation(request, contentAsByteBuffer, document, options, collectionValueHolder.v, null);
                    return request;
                });
    }

    private Mono<RxDocumentServiceRequest> addPartitionKeyInformation(RxDocumentServiceRequest request,
                                                                      ByteBuffer contentAsByteBuffer,
                                                                      Object document,
                                                                      RequestOptions options,
                                                                      Mono<Utils.ValueHolder<DocumentCollection>> collectionObs,
                                                                      CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionContextForRequest) {

        return collectionObs.map(collectionValueHolder -> {
            addPartitionKeyInformation(request, contentAsByteBuffer, document, options, collectionValueHolder.v, crossRegionContextForRequest);
            return request;
        });
    }

    private void addPartitionKeyInformation(RxDocumentServiceRequest request,
                                            ByteBuffer contentAsByteBuffer,
                                            Object objectDoc, RequestOptions options,
                                            DocumentCollection collection,
                                            CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContextForRequest) {

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
            partitionKeyInternal =  PartitionKeyHelper.extractPartitionKeyValueFromDocument(internalObjectNode, partitionKeyDefinition);
            Instant serializationEndTime = Instant.now();
            SerializationDiagnosticsContext.SerializationDiagnostics serializationDiagnostics = new SerializationDiagnosticsContext.SerializationDiagnostics(
                serializationStartTime,
                serializationEndTime,
                SerializationDiagnosticsContext.SerializationType.PARTITION_KEY_FETCH_SERIALIZATION
            );

            SerializationDiagnosticsContext serializationDiagnosticsContext = BridgeInternal.getSerializationDiagnosticsContext(request.requestContext.cosmosDiagnostics);
            if (serializationDiagnosticsContext != null) {
                serializationDiagnosticsContext.addSerializationDiagnostics(serializationDiagnostics);
            } else if (crossRegionAvailabilityContextForRequest != null) {

                PointOperationContextForCircuitBreaker pointOperationContextForCircuitBreaker
                    = crossRegionAvailabilityContextForRequest.getPointOperationContextForCircuitBreaker();

                if (pointOperationContextForCircuitBreaker != null) {
                    serializationDiagnosticsContext = pointOperationContextForCircuitBreaker.getSerializationDiagnosticsContext();

                    if (serializationDiagnosticsContext != null) {
                        serializationDiagnosticsContext.addSerializationDiagnostics(serializationDiagnostics);
                    }
                }
            }

        } else {
            throw new UnsupportedOperationException("PartitionKey value must be supplied for this operation.");
        }

        request.setPartitionKeyInternal(partitionKeyInternal);
        request.setPartitionKeyDefinition(partitionKeyDefinition);
        request.getHeaders().put(HttpConstants.HttpHeaders.PARTITION_KEY, Utils.escapeNonAscii(partitionKeyInternal.toJson()));
    }

    private Mono<Tuple2<RxDocumentServiceRequest, Utils.ValueHolder<DocumentCollection>>> getCreateDocumentRequest(DocumentClientRetryPolicy requestRetryPolicy,
                                                                           String documentCollectionLink,
                                                                           Object document,
                                                                           RequestOptions options,
                                                                           boolean disableAutomaticIdGeneration,
                                                                           OperationType operationType,
                                                                           DiagnosticsClientContext clientContextOverride,
                                                                           CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionContextForRequest) {

        if (StringUtils.isEmpty(documentCollectionLink)) {
            throw new IllegalArgumentException("documentCollectionLink");
        }
        if (document == null) {
            throw new IllegalArgumentException("document");
        }

        Instant serializationStartTimeUTC = Instant.now();
        String trackingId = null;
        if (options != null) {
            trackingId = options.getTrackingId();
        }
        ByteBuffer content = InternalObjectNode.serializeJsonToByteBuffer(document, options.getEffectiveItemSerializer(), trackingId, true);
        Instant serializationEndTimeUTC = Instant.now();

        SerializationDiagnosticsContext.SerializationDiagnostics serializationDiagnostics = new SerializationDiagnosticsContext.SerializationDiagnostics(
            serializationStartTimeUTC,
            serializationEndTimeUTC,
            SerializationDiagnosticsContext.SerializationType.ITEM_SERIALIZATION);

        String path = Utils.joinPath(documentCollectionLink, Paths.DOCUMENTS_PATH_SEGMENT);
        Map<String, String> requestHeaders = this.getRequestHeaders(options, ResourceType.Document, operationType);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            getEffectiveClientContext(clientContextOverride),
            operationType, ResourceType.Document, path, requestHeaders, options, content);

        if (operationType.isWriteOperation() &&  options != null && options.getNonIdempotentWriteRetriesEnabled() != null && options.getNonIdempotentWriteRetriesEnabled()) {
            request.setNonIdempotentWriteRetriesEnabled(true);
        }

        if( options != null) {

            DocumentServiceRequestContext requestContext = request.requestContext;

            options.getMarkE2ETimeoutInRequestContextCallbackHook().set(
                () -> requestContext.setIsRequestCancelledOnTimeout(new AtomicBoolean(true)));
            requestContext.setExcludeRegions(options.getExcludedRegions());
            requestContext.setKeywordIdentifiers(options.getKeywordIdentifiers());
        }

        SerializationDiagnosticsContext serializationDiagnosticsContext = BridgeInternal.getSerializationDiagnosticsContext(request.requestContext.cosmosDiagnostics);
        if (serializationDiagnosticsContext != null) {
            serializationDiagnosticsContext.addSerializationDiagnostics(serializationDiagnostics);
        }

        if (requestRetryPolicy != null) {
            requestRetryPolicy.onBeforeSendRequest(request);
        }

        Mono<Utils.ValueHolder<DocumentCollection>> collectionObs = this.collectionCache.resolveCollectionAsync(BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics), request);
        return addPartitionKeyInformation(request, content, document, options, collectionObs, crossRegionContextForRequest)
            .zipWith(collectionObs);
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

        if (options != null) {

            DocumentServiceRequestContext requestContext = request.requestContext;

            options.getMarkE2ETimeoutInRequestContextCallbackHook().set(
                () -> requestContext.setIsRequestCancelledOnTimeout(new AtomicBoolean(true)));
            requestContext.setExcludeRegions(options.getExcludedRegions());
            requestContext.setKeywordIdentifiers(options.getKeywordIdentifiers());
        }

        SerializationDiagnosticsContext serializationDiagnosticsContext = BridgeInternal.getSerializationDiagnosticsContext(request.requestContext.cosmosDiagnostics);

        if (serializationDiagnosticsContext != null) {
            serializationDiagnosticsContext.addSerializationDiagnostics(serializationDiagnostics);
        }

        if (options != null) {
            request.requestContext.setExcludeRegions(options.getExcludedRegions());
            request.requestContext.setKeywordIdentifiers(options.getKeywordIdentifiers());
        }

        // note: calling onBeforeSendRequest is a cheap operation which injects a CosmosDiagnostics
        // instance into 'request' amongst other things - this way metadataDiagnosticsContext is not
        // null and can be used for metadata-related telemetry (partition key range, container and server address lookups)
        if (requestRetryPolicy != null) {
            requestRetryPolicy.onBeforeSendRequest(request);
        }

        MetadataDiagnosticsContext metadataDiagnosticsContext = BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics);

        request.requestContext.setCrossRegionAvailabilityContext(

            new CrossRegionAvailabilityContextForRxDocumentServiceRequest(
                null,
                new PointOperationContextForCircuitBreaker(
                    new AtomicBoolean(false),
                    false,
                    documentCollectionLink,
                    serializationDiagnosticsContext),
                null));

        return this.collectionCache.resolveCollectionAsync(metadataDiagnosticsContext, request)
            .flatMap(documentCollectionValueHolder -> {

                if (documentCollectionValueHolder == null || documentCollectionValueHolder.v == null) {
                    return Mono.error(new IllegalStateException("documentCollectionValueHolder or documentCollectionValueHolder.v cannot be null"));
                }

                return this.partitionKeyRangeCache.tryLookupAsync(metadataDiagnosticsContext, documentCollectionValueHolder.v.getResourceId(), null, null)
                    .flatMap(collectionRoutingMapValueHolder -> {

                        if (collectionRoutingMapValueHolder == null || collectionRoutingMapValueHolder.v == null) {
                            return Mono.error(new IllegalStateException("collectionRoutingMapValueHolder or collectionRoutingMapValueHolder.v cannot be null"));
                        }

                        addBatchHeaders(request, serverBatchRequest, documentCollectionValueHolder.v);

                        checkNotNull(options, "Argument 'options' cannot be null!");

                        options.setPartitionKeyDefinition(documentCollectionValueHolder.v.getPartitionKey());

                        PartitionKeyRange preResolvePartitionKeyRangeIfAny
                            = setPartitionKeyRangeForPointOperationRequestForPerPartitionAutomaticFailover(
                            request,
                            options,
                            collectionRoutingMapValueHolder.v,
                            requestRetryPolicy,
                            true,
                            null);

                        addPartitionLevelUnavailableRegionsForPointOperationRequestForPerPartitionCircuitBreaker(
                            request,
                            options,
                            collectionRoutingMapValueHolder.v,
                            requestRetryPolicy,
                            preResolvePartitionKeyRangeIfAny);

                        return Mono.just(request);
                    });
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

        request.setPartitionKeyDefinition(collection.getPartitionKey());
        request.setNumberOfItemsInBatchRequest(serverBatchRequest.getOperations().size());

        return request;
    }

    /**
     * NOTE: Caller needs to consume it by subscribing to this Mono in order for the request to populate headers
     * @param request request to populate headers to
     * @param httpMethod http method
     * @return Mono, which on subscription will populate the headers in the request passed in the argument.
     */
    public Mono<RxDocumentServiceRequest> populateHeadersAsync(RxDocumentServiceRequest request, RequestVerb httpMethod) {
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

        if (this.apiType != null)   {
            request.getHeaders().put(HttpConstants.HttpHeaders.API_TYPE, this.apiType.toString());
        }

        this.populateCapabilitiesHeader(request);

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
                              this.collectionCache
                                  .resolveCollectionAsync(metadataDiagnosticsCtx, request)
                                  .flatMap(documentCollectionValueHolder -> {

                                      if (documentCollectionValueHolder.v != null) {
                                          request.setPartitionKeyDefinition(documentCollectionValueHolder.v.getPartitionKey());
                                      }

                                      return Mono.just(documentCollectionValueHolder);
                                  })
                          )
                          .flatMap(this::populateAuthorizationHeader);
        }

        return this.populateAuthorizationHeader(request);
    }

    private void populateCapabilitiesHeader(RxDocumentServiceRequest request) {
        if (!request.getHeaders().containsKey(HttpConstants.HttpHeaders.SDK_SUPPORTED_CAPABILITIES)) {
            request
                .getHeaders()
                .put(HttpConstants.HttpHeaders.SDK_SUPPORTED_CAPABILITIES, HttpConstants.SDKSupportedCapabilities.SUPPORTED_CAPABILITIES);
        }
    }

    private boolean requiresFeedRangeFiltering(RxDocumentServiceRequest request) {
        if (request.getResourceType() != ResourceType.Document &&
                request.getResourceType() != ResourceType.Conflict) {
            return false;
        }

        if (request.hasFeedRangeFilteringBeenApplied()) {
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
            return this.cosmosAuthorizationTokenResolver.getAuthorizationToken(requestVerb.toUpperCase(), resourceName, this.resolveCosmosResourceType(resourceType).toString(),
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
        return populateHeadersAsync(request, RequestVerb.POST)
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

        return populateHeadersAsync(request, RequestVerb.POST)
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
        return populateHeadersAsync(request, RequestVerb.PUT)
            .flatMap(requestPopulated -> {
                if (documentClientRetryPolicy.getRetryContext() != null && documentClientRetryPolicy.getRetryContext().getRetryCount() > 0) {
                    documentClientRetryPolicy.getRetryContext().updateEndTime();
                }

                return getStoreProxy(requestPopulated).processMessage(requestPopulated);
            });
    }

    private Mono<RxDocumentServiceResponse> patch(RxDocumentServiceRequest request, DocumentClientRetryPolicy documentClientRetryPolicy) {
        return populateHeadersAsync(request, RequestVerb.PATCH)
            .flatMap(requestPopulated -> {
                if (documentClientRetryPolicy.getRetryContext() != null && documentClientRetryPolicy.getRetryContext().getRetryCount() > 0) {
                    documentClientRetryPolicy.getRetryContext().updateEndTime();
                }
                return getStoreProxy(requestPopulated).processMessage(requestPopulated);
        });
    }

    @Override
    public Mono<ResourceResponse<Document>> createDocument(
        String collectionLink,
        Object document,
        RequestOptions options,
        boolean disableAutomaticIdGeneration) {

        return wrapPointOperationWithAvailabilityStrategy(
            ResourceType.Document,
            OperationType.Create,
            (opt, e2ecfg, clientCtxOverride, crossRegionAvailabilityContextForRequest) -> createDocumentCore(
                collectionLink,
                document,
                opt,
                disableAutomaticIdGeneration,
                e2ecfg,
                clientCtxOverride,
                crossRegionAvailabilityContextForRequest
            ),
            options,
            options != null && options.getNonIdempotentWriteRetriesEnabled() != null && options.getNonIdempotentWriteRetriesEnabled(),
            collectionLink
        );
    }

    private Mono<ResourceResponse<Document>> createDocumentCore(
        String collectionLink,
        Object document,
        RequestOptions options,
        boolean disableAutomaticIdGeneration,
        CosmosEndToEndOperationLatencyPolicyConfig endToEndPolicyConfig,
        DiagnosticsClientContext clientContextOverride,
        CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContextForRxDocumentServiceRequest) {

        ScopedDiagnosticsFactory scopedDiagnosticsFactory = new ScopedDiagnosticsFactory(clientContextOverride, false);

        RequestOptions nonNullRequestOptions = options != null ? options : new RequestOptions();

        DocumentClientRetryPolicy finalRetryPolicyInstance =
            this.getRetryPolicyForPointOperation(
                scopedDiagnosticsFactory,
                nonNullRequestOptions,
                collectionLink);

        AtomicReference<RxDocumentServiceRequest> requestReference = new AtomicReference<>();

        Consumer<CosmosException> gwModeE2ETimeoutDiagnosticHandler
            = (operationCancelledException) -> {

            RxDocumentServiceRequest request = requestReference.get();
            this.addCancelledGatewayModeDiagnosticsIntoCosmosException(operationCancelledException, request);
        };

        scopedDiagnosticsFactory.setGwModeE2ETimeoutDiagnosticsHandler(gwModeE2ETimeoutDiagnosticHandler);

        return handleCircuitBreakingFeedbackForPointOperation(getPointOperationResponseMonoWithE2ETimeout(
            nonNullRequestOptions,
            endToEndPolicyConfig,
            ObservableHelper.inlineIfPossibleAsObs(() ->
                    createDocumentInternal(
                        collectionLink,
                        document,
                        nonNullRequestOptions,
                        disableAutomaticIdGeneration,
                        finalRetryPolicyInstance,
                        scopedDiagnosticsFactory,
                        requestReference,
                        crossRegionAvailabilityContextForRxDocumentServiceRequest),
                finalRetryPolicyInstance),
            scopedDiagnosticsFactory
        ), requestReference, endToEndPolicyConfig);
    }

    private Mono<ResourceResponse<Document>> createDocumentInternal(
        String collectionLink,
        Object document,
        RequestOptions options,
        boolean disableAutomaticIdGeneration,
        DocumentClientRetryPolicy requestRetryPolicy,
        DiagnosticsClientContext clientContextOverride,
        AtomicReference<RxDocumentServiceRequest> documentServiceRequestReference,
        CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContextForRequest) {

        try {
            logger.debug("Creating a Document. collectionLink: [{}]", collectionLink);

            Mono<Tuple2<RxDocumentServiceRequest, Utils.ValueHolder<DocumentCollection>>> requestToDocumentCollectionObs = getCreateDocumentRequest(
                requestRetryPolicy,
                collectionLink,
                document,
                options,
                disableAutomaticIdGeneration,
                OperationType.Create,
                clientContextOverride,
                crossRegionAvailabilityContextForRequest);

            return requestToDocumentCollectionObs
                .flatMap(requestToDocumentCollection -> {

                    RxDocumentServiceRequest request = requestToDocumentCollection.getT1();
                    Utils.ValueHolder<DocumentCollection> documentCollectionValueHolder = requestToDocumentCollection.getT2();

                    if (documentCollectionValueHolder == null || documentCollectionValueHolder.v == null) {
                        return Mono.error(new IllegalStateException("documentCollectionValueHolder or documentCollectionValueHolder.v cannot be null"));
                    }

                    return this.partitionKeyRangeCache.tryLookupAsync(BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics), documentCollectionValueHolder.v.getResourceId(), null, null)
                        .flatMap(collectionRoutingMapValueHolder -> {

                            if (collectionRoutingMapValueHolder == null || collectionRoutingMapValueHolder.v == null) {
                                return Mono.error(new IllegalStateException("collectionRoutingMapValueHolder or collectionRoutingMapValueHolder.v cannot be null"));
                            }

                            options.setPartitionKeyDefinition(documentCollectionValueHolder.v.getPartitionKey());

                            request.requestContext.setCrossRegionAvailabilityContext(crossRegionAvailabilityContextForRequest);

                            PartitionKeyRange preResolvedPartitionKeyRangeIfAny = setPartitionKeyRangeForPointOperationRequestForPerPartitionAutomaticFailover(
                                request,
                                options,
                                collectionRoutingMapValueHolder.v,
                                requestRetryPolicy,
                                true,
                                null);

                            addPartitionLevelUnavailableRegionsForPointOperationRequestForPerPartitionCircuitBreaker(
                                request,
                                options,
                                collectionRoutingMapValueHolder.v,
                                requestRetryPolicy,
                                preResolvedPartitionKeyRangeIfAny);

                            documentServiceRequestReference.set(request);

                            // needs to be after onBeforeSendRequest since CosmosDiagnostics instance needs to be wired
                            // to the RxDocumentServiceRequest instance
                            mergeContextInformationIntoDiagnosticsForPointRequest(request, crossRegionAvailabilityContextForRequest);

                            return create(request, requestRetryPolicy, getOperationContextAndListenerTuple(options));

                        })
                        .map(serviceResponse -> toResourceResponse(serviceResponse, Document.class));
                });
        } catch (Exception e) {
            logger.debug("Failure in creating a document due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    private static <T> Mono<T> getPointOperationResponseMonoWithE2ETimeout(
        RequestOptions requestOptions,
        CosmosEndToEndOperationLatencyPolicyConfig endToEndPolicyConfig,
        Mono<T> rxDocumentServiceResponseMono,
        ScopedDiagnosticsFactory scopedDiagnosticsFactory) {

        requestOptions.setCosmosEndToEndLatencyPolicyConfig(endToEndPolicyConfig);

        if (endToEndPolicyConfig != null && endToEndPolicyConfig.isEnabled()) {

            Duration endToEndTimeout = endToEndPolicyConfig.getEndToEndOperationTimeout();
            if (endToEndTimeout.isNegative()) {
                CosmosDiagnostics latestCosmosDiagnosticsSnapshot = scopedDiagnosticsFactory.getMostRecentlyCreatedDiagnostics();
                if (latestCosmosDiagnosticsSnapshot == null) {
                    scopedDiagnosticsFactory.createDiagnostics();
                }
                return Mono.error(getNegativeTimeoutException(scopedDiagnosticsFactory.getMostRecentlyCreatedDiagnostics(), endToEndTimeout));
            }

            return rxDocumentServiceResponseMono
                .timeout(endToEndTimeout)
                .onErrorMap(throwable -> getCancellationExceptionForPointOperations(
                    scopedDiagnosticsFactory,
                    throwable,
                    requestOptions.getMarkE2ETimeoutInRequestContextCallbackHook()));
        }
        return rxDocumentServiceResponseMono;
    }

    private <T> Mono<T> handleCircuitBreakingFeedbackForPointOperation(
        Mono<T> response,
        AtomicReference<RxDocumentServiceRequest> requestReference,
        CosmosEndToEndOperationLatencyPolicyConfig effectiveEndToEndPolicyConfig) {

        applyEndToEndLatencyPolicyCfgToRequestContext(requestReference.get(), effectiveEndToEndPolicyConfig);

        return response
            .doOnSuccess(ignore -> {

                RxDocumentServiceRequest succeededRequest = requestReference.get();

                if (this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker.isPerPartitionLevelCircuitBreakingApplicable(requestReference.get())) {

                    checkNotNull(succeededRequest.requestContext, "Argument 'succeededRequest.requestContext' must not be null!");

                    CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContext = succeededRequest.requestContext.getCrossRegionAvailabilityContext();

                    checkNotNull(crossRegionAvailabilityContext, "Argument 'crossRegionAvailabilityContext' cannot be null!");

                    PointOperationContextForCircuitBreaker pointOperationContextForCircuitBreaker = crossRegionAvailabilityContext.getPointOperationContextForCircuitBreaker();

                    checkNotNull(pointOperationContextForCircuitBreaker, "Argument 'pointOperationContextForCircuitBreaker' must not be null!");
                    pointOperationContextForCircuitBreaker.setHasOperationSeenSuccess();

                    this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker.handleLocationSuccessForPartitionKeyRange(succeededRequest);
                }

                this.globalPartitionEndpointManagerForPerPartitionAutomaticFailover.resetEndToEndTimeoutErrorCountIfPossible(succeededRequest);
            })
            .doOnError(throwable -> {
                if (throwable instanceof OperationCancelledException) {

                    RxDocumentServiceRequest failedRequest = requestReference.get();

                    if (this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker.isPerPartitionLevelCircuitBreakingApplicable(requestReference.get())) {

                        checkNotNull(failedRequest.requestContext, "Argument 'failedRequest.requestContext' must not be null!");

                        CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContext = failedRequest.requestContext.getCrossRegionAvailabilityContext();

                        PointOperationContextForCircuitBreaker pointOperationContextForCircuitBreaker = crossRegionAvailabilityContext.getPointOperationContextForCircuitBreaker();

                        checkNotNull(pointOperationContextForCircuitBreaker, "Argument 'pointOperationContextForCircuitBreaker' must not be null!");

                        if (pointOperationContextForCircuitBreaker.isThresholdBasedAvailabilityStrategyEnabled()) {

                            if (!pointOperationContextForCircuitBreaker.isRequestHedged() && pointOperationContextForCircuitBreaker.getHasOperationSeenSuccess()) {
                                this.handleLocationCancellationExceptionForPartitionKeyRange(failedRequest);
                            }
                        } else {
                            this.handleLocationCancellationExceptionForPartitionKeyRange(failedRequest);
                        }
                    }

                    // Trigger PPAF flow iff an OperationCanceledException is intercepted here
                    this.globalPartitionEndpointManagerForPerPartitionAutomaticFailover
                        .tryMarkEndpointAsUnavailableForPartitionKeyRange(failedRequest, true);
                }
            })
            .doFinally(signalType -> {
                if (signalType != SignalType.CANCEL) {
                    return;
                }

                if (this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker.isPerPartitionLevelCircuitBreakingApplicable(requestReference.get())) {
                    RxDocumentServiceRequest failedRequest = requestReference.get();
                    checkNotNull(failedRequest.requestContext, "Argument 'failedRequest.requestContext' must not be null!");

                    CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContext = failedRequest.requestContext.getCrossRegionAvailabilityContext();

                    checkNotNull(crossRegionAvailabilityContext, "Argument 'crossRegionAvailabilityContext' must not be null!");

                    PointOperationContextForCircuitBreaker pointOperationContextForCircuitBreaker = crossRegionAvailabilityContext.getPointOperationContextForCircuitBreaker();

                    checkNotNull(pointOperationContextForCircuitBreaker, "Argument 'pointOperationContextForCircuitBreaker' must not be null!");

                    // scoping the handling of CANCEL signal handling for reasons outside of end-to-end operation timeout
                    // to purely operations which have end-to-end operation timeout enabled
                    if (pointOperationContextForCircuitBreaker.isThresholdBasedAvailabilityStrategyEnabled()) {

                        if (!pointOperationContextForCircuitBreaker.isRequestHedged() && pointOperationContextForCircuitBreaker.getHasOperationSeenSuccess()) {
                            this.handleLocationCancellationExceptionForPartitionKeyRange(failedRequest);
                        }
                    }
                }
            });
    }

    private <T> Mono<NonTransientFeedOperationResult<T>> handleCircuitBreakingFeedbackForFeedOperationWithAvailabilityStrategy(Mono<NonTransientFeedOperationResult<T>> response, RxDocumentServiceRequest request) {

        return response
            .doOnSuccess(nonTransientFeedOperationResult -> {

                if (this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker.isPerPartitionLevelCircuitBreakingApplicable(request)) {
                    if (!nonTransientFeedOperationResult.isError()) {
                        checkNotNull(request, "Argument 'request' cannot be null!");
                        checkNotNull(request.requestContext, "Argument 'request.requestContext' cannot be null!");

                        CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContext
                            = request.requestContext.getCrossRegionAvailabilityContext();

                        checkNotNull(crossRegionAvailabilityContext, "Argument 'crossRegionAvailabilityContext' cannot be null!");

                        FeedOperationContextForCircuitBreaker feedOperationContextForCircuitBreaker
                            = crossRegionAvailabilityContext.getFeedOperationContextForCircuitBreaker();

                        checkNotNull(feedOperationContextForCircuitBreaker, "Argument 'feedOperationContextForCircuitBreaker' cannot be null!");

                        feedOperationContextForCircuitBreaker.addPartitionKeyRangeWithSuccess(request.requestContext.resolvedPartitionKeyRangeForCircuitBreaker, request.getResourceId());
                        this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker.handleLocationSuccessForPartitionKeyRange(request);
                    }
                }
            })
            .doFinally(signalType -> {
                if (signalType != SignalType.CANCEL) {
                    return;
                }

                if (this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker.isPerPartitionLevelCircuitBreakingApplicable(request)) {
                    checkNotNull(request, "Argument 'request' cannot be null!");
                    checkNotNull(request.requestContext, "Argument 'request.requestContext' cannot be null!");

                    CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContext
                        = request.requestContext.getCrossRegionAvailabilityContext();

                    checkNotNull(crossRegionAvailabilityContext, "Argument 'crossRegionAvailabilityContext' cannot be null!");

                    FeedOperationContextForCircuitBreaker feedOperationContextForCircuitBreaker
                        = crossRegionAvailabilityContext.getFeedOperationContextForCircuitBreaker();

                    checkNotNull(feedOperationContextForCircuitBreaker, "Argument 'feedOperationContextForCircuitBreaker' cannot be null!");

                    if (!feedOperationContextForCircuitBreaker.getIsRequestHedged()
                        && feedOperationContextForCircuitBreaker.isThresholdBasedAvailabilityStrategyEnabled()
                        && feedOperationContextForCircuitBreaker.hasPartitionKeyRangeSeenSuccess(request.requestContext.resolvedPartitionKeyRange, request.getResourceId())) {
                        this.handleLocationCancellationExceptionForPartitionKeyRange(request);
                    }
                }
            });
    }

    private static Throwable getCancellationExceptionForPointOperations(
        ScopedDiagnosticsFactory scopedDiagnosticsFactory,
        Throwable throwable,
        AtomicReference<Runnable> markE2ETimeoutInRequestContextCallbackHook) {
        Throwable unwrappedException = reactor.core.Exceptions.unwrap(throwable);
        if (unwrappedException instanceof TimeoutException) {

            CosmosException exception = new OperationCancelledException();
            exception.setStackTrace(throwable.getStackTrace());

            Runnable actualCallback = markE2ETimeoutInRequestContextCallbackHook.get();
            if (actualCallback != null) {
                logger.trace("Calling actual Mark E2E timeout callback");
                actualCallback.run();
            }

            Consumer<CosmosException> gatewayCancelledDiagnosticsHandler
                = scopedDiagnosticsFactory.getGwModeE2ETimeoutDiagnosticsHandler();

            if (gatewayCancelledDiagnosticsHandler != null) {
                gatewayCancelledDiagnosticsHandler.accept(exception);
            }

            // For point operations
            // availabilityStrategy sits on top of e2eTimeoutPolicy
            // e2eTimeoutPolicy sits on top of client retry policy
            // for each e2eTimeoutPolicy wrap, we are going to create one distinct ScopedDiagnosticsFactory
            // so for each scopedDiagnosticsFactory being used here, there will only be max one CosmosDiagnostics being tracked
            CosmosDiagnostics lastDiagnosticsSnapshot = scopedDiagnosticsFactory.getMostRecentlyCreatedDiagnostics();
            if (lastDiagnosticsSnapshot == null) {
                scopedDiagnosticsFactory.createDiagnostics();
            }
            BridgeInternal.setCosmosDiagnostics(exception, scopedDiagnosticsFactory.getMostRecentlyCreatedDiagnostics());

            return exception;
        }
        return throwable;
    }

    private static CosmosException getNegativeTimeoutException(CosmosDiagnostics cosmosDiagnostics, Duration negativeTimeout) {
        checkNotNull(negativeTimeout, "Argument 'negativeTimeout' must not be null");
        checkArgument(
            negativeTimeout.isNegative(),
            "This exception should only be used for negative timeouts");

        String message = String.format("Negative timeout '%s' provided.",  negativeTimeout);
        CosmosException exception = new OperationCancelledException(message, null);
        BridgeInternal.setSubStatusCode(exception, HttpConstants.SubStatusCodes.NEGATIVE_TIMEOUT_PROVIDED);

        if (cosmosDiagnostics != null) {
            BridgeInternal.setCosmosDiagnostics(exception, cosmosDiagnostics);
        }

        return exception;
    }

    private static void applyEndToEndLatencyPolicyCfgToRequestContext(RxDocumentServiceRequest rxDocumentServiceRequest, CosmosEndToEndOperationLatencyPolicyConfig effectiveEndToEndPolicyConfig) {

        if (rxDocumentServiceRequest == null) {
            return;
        }

        if (rxDocumentServiceRequest.requestContext == null) {
            return;
        }

        if (effectiveEndToEndPolicyConfig == null) {
            return;
        }

        rxDocumentServiceRequest.requestContext.setEndToEndOperationLatencyPolicyConfig(effectiveEndToEndPolicyConfig);
    }

    @Override
    public Mono<ResourceResponse<Document>> upsertDocument(String collectionLink, Object document,
                                                                 RequestOptions options, boolean disableAutomaticIdGeneration) {
        return wrapPointOperationWithAvailabilityStrategy(
            ResourceType.Document,
            OperationType.Upsert,
            (opt, e2ecfg, clientCtxOverride, crossRegionAvailabilityContextForRequest) -> upsertDocumentCore(
                collectionLink,
                document,
                opt,
                disableAutomaticIdGeneration,
                e2ecfg,
                clientCtxOverride,
                crossRegionAvailabilityContextForRequest),
            options,
            options != null && options.getNonIdempotentWriteRetriesEnabled() != null && options.getNonIdempotentWriteRetriesEnabled(),
            collectionLink
        );
    }

    private Mono<ResourceResponse<Document>> upsertDocumentCore(
        String collectionLink,
        Object document,
        RequestOptions options,
        boolean disableAutomaticIdGeneration,
        CosmosEndToEndOperationLatencyPolicyConfig endToEndPolicyConfig,
        DiagnosticsClientContext clientContextOverride,
        CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContextForRequest) {

        RequestOptions nonNullRequestOptions = options != null ? options : new RequestOptions();
        ScopedDiagnosticsFactory scopedDiagnosticsFactory = new ScopedDiagnosticsFactory(clientContextOverride, false);

        DocumentClientRetryPolicy finalRetryPolicyInstance =
            this.getRetryPolicyForPointOperation(
                scopedDiagnosticsFactory,
                nonNullRequestOptions,
                collectionLink);;
        AtomicReference<RxDocumentServiceRequest> requestReference = new AtomicReference<>();

        Consumer<CosmosException> gwModeE2ETimeoutDiagnosticHandler
            = (operationCancelledException) -> {

            RxDocumentServiceRequest request = requestReference.get();
            this.addCancelledGatewayModeDiagnosticsIntoCosmosException(operationCancelledException, request);
        };

        scopedDiagnosticsFactory.setGwModeE2ETimeoutDiagnosticsHandler(gwModeE2ETimeoutDiagnosticHandler);

        return handleCircuitBreakingFeedbackForPointOperation(getPointOperationResponseMonoWithE2ETimeout(
                nonNullRequestOptions,
                endToEndPolicyConfig,
                ObservableHelper.inlineIfPossibleAsObs(
                    () -> upsertDocumentInternal(
                        collectionLink,
                        document,
                        nonNullRequestOptions,
                        disableAutomaticIdGeneration,
                        finalRetryPolicyInstance,
                        scopedDiagnosticsFactory,
                        requestReference,
                        crossRegionAvailabilityContextForRequest),
                    finalRetryPolicyInstance),
                scopedDiagnosticsFactory), requestReference, endToEndPolicyConfig);
    }

    private Mono<ResourceResponse<Document>> upsertDocumentInternal(
        String collectionLink,
        Object document,
        RequestOptions options,
        boolean disableAutomaticIdGeneration,
        DocumentClientRetryPolicy retryPolicyInstance,
        DiagnosticsClientContext clientContextOverride,
        AtomicReference<RxDocumentServiceRequest> requestReference,
        CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContextForRequest) {

        try {
            logger.debug("Upserting a Document. collectionLink: [{}]", collectionLink);

            Mono<Tuple2<RxDocumentServiceRequest, Utils.ValueHolder<DocumentCollection>>> requestToDocumentCollectionObs =
                getCreateDocumentRequest(
                    retryPolicyInstance,
                    collectionLink,
                    document,
                    options,
                    disableAutomaticIdGeneration,
                    OperationType.Upsert,
                    clientContextOverride,
                    crossRegionAvailabilityContextForRequest);

            return requestToDocumentCollectionObs
                .flatMap(requestToDocumentCollection -> {
                    RxDocumentServiceRequest request = requestToDocumentCollection.getT1();
                    Utils.ValueHolder<DocumentCollection> documentCollectionValueHolder = requestToDocumentCollection.getT2();

                    if (documentCollectionValueHolder == null || documentCollectionValueHolder.v == null) {
                        return Mono.error(new IllegalStateException("documentCollectionValueHolder or documentCollectionValueHolder.v cannot be null"));
                    }

                    return this.partitionKeyRangeCache.tryLookupAsync(BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics), documentCollectionValueHolder.v.getResourceId(), null, null)
                        .flatMap(collectionRoutingMapValueHolder -> {

                            if (collectionRoutingMapValueHolder == null || collectionRoutingMapValueHolder.v == null) {
                                return Mono.error(new IllegalStateException("collectionRoutingMapValueHolder or collectionRoutingMapValueHolder.v cannot be null"));
                            }

                            options.setPartitionKeyDefinition(documentCollectionValueHolder.v.getPartitionKey());
                            request.requestContext.setCrossRegionAvailabilityContext(crossRegionAvailabilityContextForRequest);

                            PartitionKeyRange preResolvedPartitionKeyRangeIfAny = setPartitionKeyRangeForPointOperationRequestForPerPartitionAutomaticFailover(
                                request,
                                options,
                                collectionRoutingMapValueHolder.v,
                                retryPolicyInstance,
                                true,
                                null);

                            addPartitionLevelUnavailableRegionsForPointOperationRequestForPerPartitionCircuitBreaker(
                                request,
                                options,
                                collectionRoutingMapValueHolder.v,
                                retryPolicyInstance,
                                preResolvedPartitionKeyRangeIfAny);

                            requestReference.set(request);

                            // needs to be after onBeforeSendRequest since CosmosDiagnostics instance needs to be wired
                            // to the RxDocumentServiceRequest instance
                            mergeContextInformationIntoDiagnosticsForPointRequest(request, crossRegionAvailabilityContextForRequest);

                            return upsert(request, retryPolicyInstance, getOperationContextAndListenerTuple(options));
                        })
                        .map(serviceResponse -> toResourceResponse(serviceResponse, Document.class));

                });

        } catch (Exception e) {
            logger.debug("Failure in upserting a document due to [{}]", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<Document>> replaceDocument(String documentLink, Object document,
                                                            RequestOptions options) {

        String collectionLink = Utils.getCollectionName(documentLink);

        return wrapPointOperationWithAvailabilityStrategy(
            ResourceType.Document,
            OperationType.Replace,
            (opt, e2ecfg, clientCtxOverride, crossRegionAvailabilityContextForRequest) -> replaceDocumentCore(
                documentLink,
                document,
                opt,
                e2ecfg,
                clientCtxOverride,
                crossRegionAvailabilityContextForRequest),
            options,
            options != null && options.getNonIdempotentWriteRetriesEnabled() != null && options.getNonIdempotentWriteRetriesEnabled(),
            collectionLink
        );
    }

    private Mono<ResourceResponse<Document>> replaceDocumentCore(
        String documentLink,
        Object document,
        RequestOptions options,
        CosmosEndToEndOperationLatencyPolicyConfig endToEndPolicyConfig,
        DiagnosticsClientContext clientContextOverride,
        CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContextForRequest) {

        RequestOptions nonNullRequestOptions = options != null ? options : new RequestOptions();
        ScopedDiagnosticsFactory scopedDiagnosticsFactory = new ScopedDiagnosticsFactory(clientContextOverride, false);

        DocumentClientRetryPolicy finalRequestRetryPolicy =
            this.getRetryPolicyForPointOperation(
                scopedDiagnosticsFactory,
                nonNullRequestOptions,
                Utils.getCollectionName(documentLink));
        AtomicReference<RxDocumentServiceRequest> requestReference = new AtomicReference<>();

        Consumer<CosmosException> gwModeE2ETimeoutDiagnosticHandler
            = (operationCancelledException) -> {

            RxDocumentServiceRequest request = requestReference.get();
            this.addCancelledGatewayModeDiagnosticsIntoCosmosException(operationCancelledException, request);
        };

        scopedDiagnosticsFactory.setGwModeE2ETimeoutDiagnosticsHandler(gwModeE2ETimeoutDiagnosticHandler);

        return handleCircuitBreakingFeedbackForPointOperation(getPointOperationResponseMonoWithE2ETimeout(
                nonNullRequestOptions,
                endToEndPolicyConfig,
                ObservableHelper.inlineIfPossibleAsObs(
                    () -> replaceDocumentInternal(
                        documentLink,
                        document,
                        nonNullRequestOptions,
                        finalRequestRetryPolicy,
                        scopedDiagnosticsFactory,
                        requestReference,
                        crossRegionAvailabilityContextForRequest),
                    finalRequestRetryPolicy),
                scopedDiagnosticsFactory), requestReference, endToEndPolicyConfig);
    }

    private Mono<ResourceResponse<Document>> replaceDocumentInternal(
        String documentLink,
        Object document,
        RequestOptions options,
        DocumentClientRetryPolicy retryPolicyInstance,
        DiagnosticsClientContext clientContextOverride,
        AtomicReference<RxDocumentServiceRequest> requestReference,
        CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContextForRequest) {

        try {
            if (StringUtils.isEmpty(documentLink)) {
                throw new IllegalArgumentException("documentLink");
            }

            if (document == null) {
                throw new IllegalArgumentException("document");
            }

            Document typedDocument = Document.fromObject(document, options.getEffectiveItemSerializer());

            return this.replaceDocumentInternal(
                documentLink,
                typedDocument,
                options,
                retryPolicyInstance,
                clientContextOverride,
                requestReference,
                crossRegionAvailabilityContextForRequest);

        } catch (Exception e) {
            logger.debug("Failure in replacing a document due to [{}]", e.getMessage());
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<Document>> replaceDocument(Document document, RequestOptions options) {

        String collectionLink = Utils.getCollectionName(document.getSelfLink());

        return wrapPointOperationWithAvailabilityStrategy(
            ResourceType.Document,
            OperationType.Replace,
            (opt, e2ecfg, clientCtxOverride, pointOperationContextForCircuitBreaker) -> replaceDocumentCore(
                document,
                opt,
                clientCtxOverride,
                pointOperationContextForCircuitBreaker
            ),
            options,
            options != null && options.getNonIdempotentWriteRetriesEnabled() != null && options.getNonIdempotentWriteRetriesEnabled(),
            collectionLink
        );
    }

    private Mono<ResourceResponse<Document>> replaceDocumentCore(
        Document document,
        RequestOptions options,
        DiagnosticsClientContext clientContextOverride,
        CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContextForRequest) {

        DocumentClientRetryPolicy requestRetryPolicy =
            this.resetSessionTokenRetryPolicy.getRequestPolicy(clientContextOverride);
        if (options == null || options.getPartitionKey() == null) {
            String collectionLink = document.getSelfLink();
            requestRetryPolicy = new PartitionKeyMismatchRetryPolicy(
                collectionCache, requestRetryPolicy, collectionLink, options);
        }
        DocumentClientRetryPolicy finalRequestRetryPolicy = requestRetryPolicy;
        AtomicReference<RxDocumentServiceRequest> requestReference = new AtomicReference<>();

        return handleCircuitBreakingFeedbackForPointOperation(ObservableHelper.inlineIfPossibleAsObs(
            () -> replaceDocumentInternal(
                document,
                options,
                finalRequestRetryPolicy,
                clientContextOverride,
                requestReference,
                crossRegionAvailabilityContextForRequest),
            requestRetryPolicy), requestReference, cosmosEndToEndOperationLatencyPolicyConfig);
    }

    private Mono<ResourceResponse<Document>> replaceDocumentInternal(
        Document document,
        RequestOptions options,
        DocumentClientRetryPolicy retryPolicyInstance,
        DiagnosticsClientContext clientContextOverride,
        AtomicReference<RxDocumentServiceRequest> requestReference,
        CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContextForRequest) {

        try {
            if (document == null) {
                throw new IllegalArgumentException("document");
            }

            return this.replaceDocumentInternal(
                document.getSelfLink(),
                document,
                options,
                retryPolicyInstance,
                clientContextOverride,
                requestReference,
                crossRegionAvailabilityContextForRequest);

        } catch (Exception e) {
            logger.debug("Failure in replacing a database due to [{}]", e.getMessage());
            return Mono.error(e);
        }
    }

    private Mono<ResourceResponse<Document>> replaceDocumentInternal(
        String documentLink,
        Document document,
        RequestOptions options,
        DocumentClientRetryPolicy retryPolicyInstance,
        DiagnosticsClientContext clientContextOverride,
        AtomicReference<RxDocumentServiceRequest> requestReference,
        CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContextForRequest) {

        if (document == null) {
            throw new IllegalArgumentException("document");
        }

        logger.debug("Replacing a Document. documentLink: [{}]", documentLink);
        final String path = Utils.joinPath(documentLink, null);
        final Map<String, String> requestHeaders =
            getRequestHeaders(options, ResourceType.Document, OperationType.Replace);
        Instant serializationStartTimeUTC = Instant.now();
        Consumer<Map<String, Object>> onAfterSerialization = null;
        if (options != null) {
            String trackingId = options.getTrackingId();

            if (trackingId != null && !trackingId.isEmpty()) {
                onAfterSerialization = (node) -> node.put(Constants.Properties.TRACKING_ID, trackingId);
            }
        }

        ByteBuffer content = document.serializeJsonToByteBuffer(options.getEffectiveItemSerializer(), onAfterSerialization, false);
        Instant serializationEndTime = Instant.now();
        SerializationDiagnosticsContext.SerializationDiagnostics serializationDiagnostics =
            new SerializationDiagnosticsContext.SerializationDiagnostics(
                serializationStartTimeUTC,
                serializationEndTime,
                SerializationDiagnosticsContext.SerializationType.ITEM_SERIALIZATION);

        final RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            getEffectiveClientContext(clientContextOverride),
            OperationType.Replace, ResourceType.Document, path, requestHeaders, options, content);

        if (options != null && options.getNonIdempotentWriteRetriesEnabled() != null && options.getNonIdempotentWriteRetriesEnabled()) {
            request.setNonIdempotentWriteRetriesEnabled(true);
        }

        if (options != null) {

            DocumentServiceRequestContext requestContext = request.requestContext;

            options.getMarkE2ETimeoutInRequestContextCallbackHook().set(
                () -> requestContext.setIsRequestCancelledOnTimeout(new AtomicBoolean(true)));
            requestContext.setExcludeRegions(options.getExcludedRegions());
            requestContext.setKeywordIdentifiers(options.getKeywordIdentifiers());
        }

        SerializationDiagnosticsContext serializationDiagnosticsContext =
            BridgeInternal.getSerializationDiagnosticsContext(request.requestContext.cosmosDiagnostics);

        if (serializationDiagnosticsContext != null) {
            serializationDiagnosticsContext.addSerializationDiagnostics(serializationDiagnostics);
        }

        if (retryPolicyInstance != null) {
            retryPolicyInstance.onBeforeSendRequest(request);
        }

        Mono<Utils.ValueHolder<DocumentCollection>> collectionObs =
            collectionCache.resolveCollectionAsync(
                BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics),
                request);
        Mono<RxDocumentServiceRequest> requestObs =
            addPartitionKeyInformation(request, content, document, options, collectionObs, crossRegionAvailabilityContextForRequest);

        return collectionObs
            .flatMap(documentCollectionValueHolder -> {

                if (documentCollectionValueHolder == null || documentCollectionValueHolder.v == null) {
                    return Mono.error(new IllegalStateException("documentCollectionValueHolder or documentCollectionValueHolder.v cannot be null"));
                }

                return this.partitionKeyRangeCache.tryLookupAsync(BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics), documentCollectionValueHolder.v.getResourceId(), null, null)
                    .flatMap(collectionRoutingMapValueHolder -> {

                        if (collectionRoutingMapValueHolder == null || collectionRoutingMapValueHolder.v == null) {
                            return Mono.error(new IllegalStateException("collectionRoutingMapValueHolder or collectionRoutingMapValueHolder.v cannot be null"));
                        }

                        return requestObs.flatMap(req -> {

                                options.setPartitionKeyDefinition(documentCollectionValueHolder.v.getPartitionKey());

                                req.requestContext.setCrossRegionAvailabilityContext(crossRegionAvailabilityContextForRequest);

                                PartitionKeyRange preResolvedPartitionKeyRangeIfAny = setPartitionKeyRangeForPointOperationRequestForPerPartitionAutomaticFailover(
                                    req,
                                    options,
                                    collectionRoutingMapValueHolder.v,
                                    retryPolicyInstance,
                                    true,
                                    null);

                                addPartitionLevelUnavailableRegionsForPointOperationRequestForPerPartitionCircuitBreaker(
                                    req,
                                    options,
                                    collectionRoutingMapValueHolder.v,
                                    retryPolicyInstance,
                                    preResolvedPartitionKeyRangeIfAny);

                                requestReference.set(req);

                                // needs to be after onBeforeSendRequest since CosmosDiagnostics instance needs to be wired
                                // to the RxDocumentServiceRequest instance
                                mergeContextInformationIntoDiagnosticsForPointRequest(request, crossRegionAvailabilityContextForRequest);

                                return replace(request, retryPolicyInstance);
                            })
                            .map(resp -> toResourceResponse(resp, Document.class));
                    });
            });
    }

    private CosmosEndToEndOperationLatencyPolicyConfig getEndToEndOperationLatencyPolicyConfig(
        RequestOptions options,
        ResourceType resourceType,
        OperationType operationType) {
        return this.getEffectiveEndToEndOperationLatencyPolicyConfig(
            options != null ? options.getCosmosEndToEndLatencyPolicyConfig() : null,
            resourceType,
            operationType);
    }

    private CosmosEndToEndOperationLatencyPolicyConfig getEffectiveEndToEndOperationLatencyPolicyConfig(
        CosmosEndToEndOperationLatencyPolicyConfig policyConfig,
        ResourceType resourceType,
        OperationType operationType) {
        if (policyConfig != null) {
            return policyConfig;
        }

        if (resourceType != ResourceType.Document) {
            return null;
        }

        if (!operationType.isPointOperation() && Configs.isDefaultE2ETimeoutDisabledForNonPointOperations()) {
            return null;
        }

        if (this.cosmosEndToEndOperationLatencyPolicyConfig != null) {
            return this.cosmosEndToEndOperationLatencyPolicyConfig;
        }

        // If request options level and client-level e2e latency policy config,
        // rely on PPAF enforced defaults
        if (operationType.isReadOnlyOperation()) {
            return this.ppafEnforcedE2ELatencyPolicyConfigForReads;
        }

        return null;
    }

    @Override
    public Mono<ResourceResponse<Document>> patchDocument(String documentLink,
                                                          CosmosPatchOperations cosmosPatchOperations,
                                                          RequestOptions options) {

        String collectionLink = Utils.getCollectionName(documentLink);

        return wrapPointOperationWithAvailabilityStrategy(
            ResourceType.Document,
            OperationType.Patch,
            (opt, e2ecfg, clientCtxOverride, crossRegionAvailabilityContextForRequest) -> patchDocumentCore(
                documentLink,
                cosmosPatchOperations,
                opt,
                e2ecfg,
                clientCtxOverride,
                crossRegionAvailabilityContextForRequest),
            options,
            options != null && options.getNonIdempotentWriteRetriesEnabled() != null && options.getNonIdempotentWriteRetriesEnabled(),
            collectionLink
        );
    }

    private Mono<ResourceResponse<Document>> patchDocumentCore(
        String documentLink,
        CosmosPatchOperations cosmosPatchOperations,
        RequestOptions options,
        CosmosEndToEndOperationLatencyPolicyConfig endToEndPolicyConfig,
        DiagnosticsClientContext clientContextOverride,
        CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContextForRequest) {

        RequestOptions nonNullRequestOptions = options != null ? options : new RequestOptions();
        ScopedDiagnosticsFactory scopedDiagnosticsFactory = new ScopedDiagnosticsFactory(clientContextOverride, false);
        DocumentClientRetryPolicy documentClientRetryPolicy =
            this.getRetryPolicyForPointOperation(
                scopedDiagnosticsFactory,
                nonNullRequestOptions,
                Utils.getCollectionName(documentLink));

        AtomicReference<RxDocumentServiceRequest> requestReference = new AtomicReference<>();

        Consumer<CosmosException> gwModeE2ETimeoutDiagnosticHandler
            = (operationCancelledException) -> {

            RxDocumentServiceRequest request = requestReference.get();
            this.addCancelledGatewayModeDiagnosticsIntoCosmosException(operationCancelledException, request);
        };

        scopedDiagnosticsFactory.setGwModeE2ETimeoutDiagnosticsHandler(gwModeE2ETimeoutDiagnosticHandler);

        return handleCircuitBreakingFeedbackForPointOperation(
            getPointOperationResponseMonoWithE2ETimeout(
                nonNullRequestOptions,
                endToEndPolicyConfig,
                ObservableHelper.inlineIfPossibleAsObs(
                    () -> patchDocumentInternal(
                        documentLink,
                        cosmosPatchOperations,
                        nonNullRequestOptions,
                        documentClientRetryPolicy,
                        scopedDiagnosticsFactory,
                        requestReference,
                        crossRegionAvailabilityContextForRequest),
                    documentClientRetryPolicy),
                scopedDiagnosticsFactory), requestReference, cosmosEndToEndOperationLatencyPolicyConfig);
    }

    private Mono<ResourceResponse<Document>> patchDocumentInternal(
        String documentLink,
        CosmosPatchOperations cosmosPatchOperations,
        RequestOptions options,
        DocumentClientRetryPolicy retryPolicyInstance,
        DiagnosticsClientContext clientContextOverride,
        AtomicReference<RxDocumentServiceRequest> requestReference,
        CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContextForRequest) {

        checkArgument(StringUtils.isNotEmpty(documentLink), "expected non empty documentLink");
        checkNotNull(cosmosPatchOperations, "expected non null cosmosPatchOperations");

        logger.debug("Running patch operations on Document. documentLink: [{}]", documentLink);

        final String path = Utils.joinPath(documentLink, null);

        final Map<String, String> requestHeaders =
            getRequestHeaders(options, ResourceType.Document, OperationType.Patch);
        Instant serializationStartTimeUTC = Instant.now();

        ByteBuffer content = ByteBuffer.wrap(
            PatchUtil.serializeCosmosPatchToByteArray(cosmosPatchOperations, options));

        Instant serializationEndTime = Instant.now();
        SerializationDiagnosticsContext.SerializationDiagnostics serializationDiagnostics =
            new SerializationDiagnosticsContext.SerializationDiagnostics(
                serializationStartTimeUTC,
                serializationEndTime,
                SerializationDiagnosticsContext.SerializationType.ITEM_SERIALIZATION);

        final RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            clientContextOverride,
            OperationType.Patch,
            ResourceType.Document,
            path,
            requestHeaders,
            options,
            content);

        if (options != null && options.getNonIdempotentWriteRetriesEnabled() != null && options.getNonIdempotentWriteRetriesEnabled()) {
            request.setNonIdempotentWriteRetriesEnabled(true);
        }
        if (options != null) {

            DocumentServiceRequestContext requestContext = request.requestContext;

            options.getMarkE2ETimeoutInRequestContextCallbackHook().set(
                () -> requestContext.setIsRequestCancelledOnTimeout(new AtomicBoolean(true)));
            requestContext.setExcludeRegions(options.getExcludedRegions());
            requestContext.setKeywordIdentifiers(options.getKeywordIdentifiers());
        }

        if (retryPolicyInstance != null) {
            retryPolicyInstance.onBeforeSendRequest(request);
        }

        SerializationDiagnosticsContext serializationDiagnosticsContext =
            BridgeInternal.getSerializationDiagnosticsContext(request.requestContext.cosmosDiagnostics);

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
            collectionObs,
            crossRegionAvailabilityContextForRequest);

        return collectionObs
            .flatMap(documentCollectionValueHolder -> {

                if (documentCollectionValueHolder == null || documentCollectionValueHolder.v == null) {
                    return Mono.error(new IllegalStateException("documentCollectionValueHolder or documentCollectionValueHolder.v cannot be null"));
                }

                return this.partitionKeyRangeCache.tryLookupAsync(BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics), documentCollectionValueHolder.v.getResourceId(), null, null)
                    .flatMap(collectionRoutingMapValueHolder -> {

                        if (collectionRoutingMapValueHolder == null || collectionRoutingMapValueHolder.v == null) {
                            return Mono.error(new IllegalStateException("collectionRoutingMapValueHolder or collectionRoutingMapValueHolder.v cannot be null"));
                        }

                        return requestObs
                            .flatMap(req -> {

                                checkNotNull(options, "Argument 'options' cannot be null!");

                                options.setPartitionKeyDefinition(documentCollectionValueHolder.v.getPartitionKey());

                                req.requestContext.setCrossRegionAvailabilityContext(crossRegionAvailabilityContextForRequest);

                                PartitionKeyRange preResolvedPartitionKeyRangeIfAny = setPartitionKeyRangeForPointOperationRequestForPerPartitionAutomaticFailover(
                                    req,
                                    options,
                                    collectionRoutingMapValueHolder.v,
                                    retryPolicyInstance,
                                    true,
                                    null);

                                addPartitionLevelUnavailableRegionsForPointOperationRequestForPerPartitionCircuitBreaker(
                                    req,
                                    options,
                                    collectionRoutingMapValueHolder.v,
                                    retryPolicyInstance,
                                    preResolvedPartitionKeyRangeIfAny);

                                requestReference.set(req);

                                // needs to be after onBeforeSendRequest since CosmosDiagnostics instance needs to be wired
                                // to the RxDocumentServiceRequest instance
                                mergeContextInformationIntoDiagnosticsForPointRequest(request, crossRegionAvailabilityContextForRequest);

                                return patch(request, retryPolicyInstance);
                            })
                            .map(resp -> toResourceResponse(resp, Document.class));
                    });
            });
    }

    @Override
    public Mono<ResourceResponse<Document>> deleteDocument(String documentLink, RequestOptions options) {

        String collectionLink = Utils.getCollectionName(documentLink);

        return wrapPointOperationWithAvailabilityStrategy(
            ResourceType.Document,
            OperationType.Delete,
            (opt, e2ecfg, clientCtxOverride, crossRegionAvailabilityContextForRequest) -> deleteDocumentCore(
                documentLink,
                null,
                opt,
                e2ecfg,
                clientCtxOverride,
                crossRegionAvailabilityContextForRequest
            ),
            options,
            options != null && options.getNonIdempotentWriteRetriesEnabled() != null && options.getNonIdempotentWriteRetriesEnabled(),
            collectionLink
        );
    }

    @Override
    public Mono<ResourceResponse<Document>> deleteDocument(String documentLink, InternalObjectNode internalObjectNode, RequestOptions options) {

        String collectionLink = Utils.getCollectionName(documentLink);

        return wrapPointOperationWithAvailabilityStrategy(
            ResourceType.Document,
            OperationType.Delete,
            (opt, e2ecfg, clientCtxOverride, pointOperationContextForCircuitBreaker) -> deleteDocumentCore(
                documentLink,
                internalObjectNode,
                opt,
                e2ecfg,
                clientCtxOverride,
                pointOperationContextForCircuitBreaker),
            options,
            options != null && options.getNonIdempotentWriteRetriesEnabled() != null && options.getNonIdempotentWriteRetriesEnabled(),
            collectionLink
        );
    }

    private Mono<ResourceResponse<Document>> deleteDocumentCore(
        String documentLink,
        InternalObjectNode internalObjectNode,
        RequestOptions options,
        CosmosEndToEndOperationLatencyPolicyConfig endToEndPolicyConfig,
        DiagnosticsClientContext clientContextOverride,
        CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContextForRequest) {

        RequestOptions nonNullRequestOptions = options != null ? options : new RequestOptions();
        ScopedDiagnosticsFactory scopedDiagnosticsFactory = new ScopedDiagnosticsFactory(clientContextOverride, false);
        DocumentClientRetryPolicy requestRetryPolicy =
            this.getRetryPolicyForPointOperation(
                scopedDiagnosticsFactory,
                nonNullRequestOptions,
                Utils.getCollectionName(documentLink));

        AtomicReference<RxDocumentServiceRequest> requestReference = new AtomicReference<>();

        Consumer<CosmosException> gwModeE2ETimeoutDiagnosticHandler
            = (operationCancelledException) -> {

            RxDocumentServiceRequest request = requestReference.get();
            this.addCancelledGatewayModeDiagnosticsIntoCosmosException(operationCancelledException, request);
        };

        scopedDiagnosticsFactory.setGwModeE2ETimeoutDiagnosticsHandler(gwModeE2ETimeoutDiagnosticHandler);

        return handleCircuitBreakingFeedbackForPointOperation(getPointOperationResponseMonoWithE2ETimeout(
                nonNullRequestOptions,
                endToEndPolicyConfig,
                ObservableHelper.inlineIfPossibleAsObs(
                    () -> deleteDocumentInternal(
                        documentLink,
                        internalObjectNode,
                        nonNullRequestOptions,
                        requestRetryPolicy,
                        scopedDiagnosticsFactory,
                        requestReference,
                        crossRegionAvailabilityContextForRequest),
                    requestRetryPolicy),
                scopedDiagnosticsFactory), requestReference, endToEndPolicyConfig);
    }

    private Mono<ResourceResponse<Document>> deleteDocumentInternal(
        String documentLink,
        InternalObjectNode internalObjectNode,
        RequestOptions options,
        DocumentClientRetryPolicy retryPolicyInstance,
        DiagnosticsClientContext clientContextOverride,
        AtomicReference<RxDocumentServiceRequest> requestReference,
        CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContextForRequest) {

        try {
            if (StringUtils.isEmpty(documentLink)) {
                throw new IllegalArgumentException("documentLink");
            }

            logger.debug("Deleting a Document. documentLink: [{}]", documentLink);
            String path = Utils.joinPath(documentLink, null);
            Map<String, String> requestHeaders = this.getRequestHeaders(options, ResourceType.Document, OperationType.Delete);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
                getEffectiveClientContext(clientContextOverride),
                OperationType.Delete, ResourceType.Document, path, requestHeaders, options);

            if (options != null && options.getNonIdempotentWriteRetriesEnabled() != null && options.getNonIdempotentWriteRetriesEnabled()) {
                request.setNonIdempotentWriteRetriesEnabled(true);
            }

            if (options != null) {

                DocumentServiceRequestContext requestContext = request.requestContext;

                options.getMarkE2ETimeoutInRequestContextCallbackHook().set(
                    () -> requestContext.setIsRequestCancelledOnTimeout(new AtomicBoolean(true)));
                requestContext.setExcludeRegions(options.getExcludedRegions());
                requestContext.setKeywordIdentifiers(options.getKeywordIdentifiers());
            }

            if (retryPolicyInstance != null) {
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            Mono<Utils.ValueHolder<DocumentCollection>> collectionObs = collectionCache.resolveCollectionAsync(
                BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics),
                request);

            Mono<RxDocumentServiceRequest> requestObs = addPartitionKeyInformation(
                request, null, internalObjectNode, options, collectionObs, crossRegionAvailabilityContextForRequest);

            return collectionObs
                .flatMap(documentCollectionValueHolder -> this.partitionKeyRangeCache.tryLookupAsync(BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics), documentCollectionValueHolder.v.getResourceId(), null, null)
                    .flatMap(collectionRoutingMapValueHolder -> {
                        return requestObs
                            .flatMap(req -> {

                                checkNotNull(options, "Argument 'options' cannot be null!");

                                options.setPartitionKeyDefinition(documentCollectionValueHolder.v.getPartitionKey());

                                req.requestContext.setCrossRegionAvailabilityContext(crossRegionAvailabilityContextForRequest);

                                PartitionKeyRange preResolvedPartitionKeyRangeIfAny = setPartitionKeyRangeForPointOperationRequestForPerPartitionAutomaticFailover(
                                    req,
                                    options,
                                    collectionRoutingMapValueHolder.v,
                                    retryPolicyInstance,
                                    true,
                                    null);

                                addPartitionLevelUnavailableRegionsForPointOperationRequestForPerPartitionCircuitBreaker(
                                    req,
                                    options,
                                    collectionRoutingMapValueHolder.v,
                                    retryPolicyInstance,
                                    preResolvedPartitionKeyRangeIfAny);

                                requestReference.set(req);

                                // needs to be after onBeforeSendRequest since CosmosDiagnostics instance needs to be wired
                                // to the RxDocumentServiceRequest instance
                                mergeContextInformationIntoDiagnosticsForPointRequest(req, crossRegionAvailabilityContextForRequest);

                                return this.delete(req, retryPolicyInstance, getOperationContextAndListenerTuple(options));
                            })
                            .map(serviceResponse -> toResourceResponse(serviceResponse, Document.class));

                    }));
        } catch (Exception e) {
            logger.debug("Failure in deleting a document due to [{}]", e.getMessage());
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<Document>> deleteAllDocumentsByPartitionKey(String collectionLink, PartitionKey partitionKey, RequestOptions options) {
        // No ned-to-end policy / availability strategy applicable because PK Delete is a Gateway/Control-Plane operation
        DocumentClientRetryPolicy requestRetryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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

            Mono<RxDocumentServiceRequest> requestObs = addPartitionKeyInformation(request, null, null, options, collectionObs, null);

            return requestObs.flatMap(req -> this
                .deleteAllItemsByPartitionKey(req, retryPolicyInstance, getOperationContextAndListenerTuple(options))
                .map(serviceResponse -> toResourceResponse(serviceResponse, Document.class)));
        } catch (Exception e) {
            logger.debug("Failure in deleting documents due to [{}]", e.getMessage());
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResourceResponse<Document>> readDocument(String documentLink, RequestOptions options) {
        return readDocument(documentLink, options, this);
    }

    private Mono<ResourceResponse<Document>> readDocument(
        String documentLink,
        RequestOptions options,
        DiagnosticsClientContext innerDiagnosticsFactory) {

        String collectionLink = Utils.getCollectionName(documentLink);

        return wrapPointOperationWithAvailabilityStrategy(
            ResourceType.Document,
            OperationType.Read,
            (opt, e2ecfg, clientCtxOverride, crossRegionAvailabilityContextForRequest) -> readDocumentCore(documentLink, opt, e2ecfg, clientCtxOverride, crossRegionAvailabilityContextForRequest),
            options,
            false,
            innerDiagnosticsFactory,
            collectionLink
        );
    }

    private Mono<ResourceResponse<Document>> readDocumentCore(
        String documentLink,
        RequestOptions options,
        CosmosEndToEndOperationLatencyPolicyConfig endToEndPolicyConfig,
        DiagnosticsClientContext clientContextOverride,
        CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContextForRequest) {

        RequestOptions nonNullRequestOptions = options != null ? options : new RequestOptions();
        ScopedDiagnosticsFactory scopedDiagnosticsFactory = new ScopedDiagnosticsFactory(clientContextOverride, false);
        DocumentClientRetryPolicy retryPolicyInstance =
            this.getRetryPolicyForPointOperation(
                scopedDiagnosticsFactory,
                nonNullRequestOptions,
                Utils.getCollectionName(documentLink));

        AtomicReference<RxDocumentServiceRequest> requestReference = new AtomicReference<>();

        Consumer<CosmosException> gwModeE2ETimeoutDiagnosticHandler
            = (operationCancelledException) -> {

            RxDocumentServiceRequest request = requestReference.get();
            this.addCancelledGatewayModeDiagnosticsIntoCosmosException(operationCancelledException, request);
        };

        scopedDiagnosticsFactory.setGwModeE2ETimeoutDiagnosticsHandler(gwModeE2ETimeoutDiagnosticHandler);

        return handleCircuitBreakingFeedbackForPointOperation(getPointOperationResponseMonoWithE2ETimeout(
            nonNullRequestOptions,
            endToEndPolicyConfig,
            ObservableHelper.inlineIfPossibleAsObs(
                () -> readDocumentInternal(
                    documentLink,
                    nonNullRequestOptions,
                    retryPolicyInstance,
                    scopedDiagnosticsFactory,
                    requestReference,
                    crossRegionAvailabilityContextForRequest),
                retryPolicyInstance),
            scopedDiagnosticsFactory
        ), requestReference, endToEndPolicyConfig);
    }

    private Mono<ResourceResponse<Document>> readDocumentInternal(
        String documentLink,
        RequestOptions options,
        DocumentClientRetryPolicy retryPolicyInstance,
        DiagnosticsClientContext clientContextOverride,
        AtomicReference<RxDocumentServiceRequest> requestReference,
        CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContextForRequest) {

        try {
            if (StringUtils.isEmpty(documentLink)) {
                throw new IllegalArgumentException("documentLink");
            }

            logger.debug("Reading a Document. documentLink: [{}]", documentLink);
            String path = Utils.joinPath(documentLink, null);
            Map<String, String> requestHeaders = this.getRequestHeaders(options, ResourceType.Document, OperationType.Read);

            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
                getEffectiveClientContext(clientContextOverride),
                OperationType.Read, ResourceType.Document, path, requestHeaders, options);

            DocumentServiceRequestContext requestContext = request.requestContext;

            options.getMarkE2ETimeoutInRequestContextCallbackHook().set(
                () -> requestContext.setIsRequestCancelledOnTimeout(new AtomicBoolean(true)));
            requestContext.setExcludeRegions(options.getExcludedRegions());
            requestContext.setKeywordIdentifiers(options.getKeywordIdentifiers());

            if (retryPolicyInstance != null) {
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            Mono<Utils.ValueHolder<DocumentCollection>> collectionObs = this.collectionCache.resolveCollectionAsync(BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics), request);
            return collectionObs.flatMap(documentCollectionValueHolder -> {

                    if (documentCollectionValueHolder == null || documentCollectionValueHolder.v == null) {
                        return Mono.error(new IllegalStateException("documentCollectionValueHolder or documentCollectionValueHolder.v cannot be null"));
                    }

                    DocumentCollection documentCollection = documentCollectionValueHolder.v;
                    return this.partitionKeyRangeCache.tryLookupAsync(BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics), documentCollection.getResourceId(), null, null)
                        .flatMap(collectionRoutingMapValueHolder -> {

                            if (collectionRoutingMapValueHolder == null || collectionRoutingMapValueHolder.v == null) {
                                return Mono.error(new IllegalStateException("collectionRoutingMapValueHolder or collectionRoutingMapValueHolder.v cannot be null"));
                            }

                            Mono<RxDocumentServiceRequest> requestObs = addPartitionKeyInformation(request, null, null, options, collectionObs, crossRegionAvailabilityContextForRequest);

                            return requestObs.flatMap(req -> {

                                options.setPartitionKeyDefinition(documentCollection.getPartitionKey());
                                req.requestContext.setCrossRegionAvailabilityContext(crossRegionAvailabilityContextForRequest);

                                PartitionKeyRange preResolvedPartionKeyRangeIfAny = setPartitionKeyRangeForPointOperationRequestForPerPartitionAutomaticFailover(
                                    req,
                                    options,
                                    collectionRoutingMapValueHolder.v,
                                    retryPolicyInstance,
                                    false,
                                    null);

                                addPartitionLevelUnavailableRegionsForPointOperationRequestForPerPartitionCircuitBreaker(
                                    req,
                                    options,
                                    collectionRoutingMapValueHolder.v,
                                    retryPolicyInstance,
                                    preResolvedPartionKeyRangeIfAny);

                                requestReference.set(req);

                                // needs to be after onBeforeSendRequest since CosmosDiagnostics instance needs to be wired
                                // to the RxDocumentServiceRequest instance
                                mergeContextInformationIntoDiagnosticsForPointRequest(req, crossRegionAvailabilityContextForRequest);

                                return this.read(req, retryPolicyInstance)
                                    .map(serviceResponse -> toResourceResponse(serviceResponse, Document.class));
                            });

                        });

                }
            );
        } catch (Exception e) {
            logger.debug("Failure in reading a document due to [{}]", e.getMessage());
            return Mono.error(e);
        }
    }

    @Override
    public <T> Flux<FeedResponse<T>>  readDocuments(
        String collectionLink, QueryFeedOperationState state, Class<T> classOfT) {

        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }

        return queryDocuments(collectionLink, "SELECT * FROM r", state, classOfT);
    }

    @Override
    public <T> Mono<FeedResponse<T>> readMany(
        List<CosmosItemIdentity> itemIdentityList,
        String collectionLink,
        QueryFeedOperationState state,
        Class<T> klass) {

        final ScopedDiagnosticsFactory diagnosticsFactory = new ScopedDiagnosticsFactory(this, true);
        state.registerDiagnosticsFactory(
            () -> {}, // we never want to reset in readMany
            (ctx) -> diagnosticsFactory.merge(ctx)
        );

        StaleResourceRetryPolicy staleResourceRetryPolicy = new StaleResourceRetryPolicy(
            this.collectionCache,
            null,
            collectionLink,
            qryOptAccessor.getProperties(state.getQueryOptions()),
            qryOptAccessor.getHeaders(state.getQueryOptions()),
            this.sessionContainer,
            diagnosticsFactory);

        return ObservableHelper
            .inlineIfPossibleAsObs(
                () -> readMany(itemIdentityList, collectionLink, state, diagnosticsFactory, klass),
                staleResourceRetryPolicy
            )
            .map(feedList -> {
                // aggregating the result to construct a FeedResponse and aggregate RUs.
                List<T> finalList = new ArrayList<>();
                HashMap<String, String> headers = new HashMap<>();
                ConcurrentMap<String, QueryMetrics> aggregatedQueryMetrics = new ConcurrentHashMap<>();
                Collection<ClientSideRequestStatistics> aggregateRequestStatistics = new DistinctClientSideRequestStatisticsCollection();
                double requestCharge = 0;
                for (FeedResponse<T> page : feedList) {
                    ConcurrentMap<String, QueryMetrics> pageQueryMetrics =
                        ModelBridgeInternal.queryMetrics(page);
                    if (pageQueryMetrics != null) {
                        pageQueryMetrics.forEach(
                            aggregatedQueryMetrics::putIfAbsent);
                    }

                    requestCharge += page.getRequestCharge();
                    finalList.addAll(page.getResults());
                    aggregateRequestStatistics.addAll(diagnosticsAccessor.getClientSideRequestStatistics(page.getCosmosDiagnostics()));
                }

                // NOTE: This CosmosDiagnostics instance intentionally isn't captured in the
                // ScopedDiagnosticsFactory - and a such won't be included in the diagnostics of the
                // CosmosDiagnosticsContext - which is fine, because the CosmosDiagnosticsContext
                // contains the "real" CosmosDiagnostics instances (which will also be used
                // for diagnostics purposes - like metrics, logging etc.
                // this artificial CosmosDiagnostics with the aggregated RU/s etc. is simply
                // to maintain the API contract that a FeedResponse returns one CosmosDiagnostics
                CosmosDiagnostics aggregatedDiagnostics = BridgeInternal.createCosmosDiagnostics(aggregatedQueryMetrics);
                diagnosticsAccessor.addClientSideDiagnosticsToFeed(
                    aggregatedDiagnostics, aggregateRequestStatistics);

                state.mergeDiagnosticsContext();
                CosmosDiagnosticsContext ctx = state.getDiagnosticsContextSnapshot();
                if (ctx != null) {
                    ctxAccessor.recordOperation(
                        ctx,
                        200,
                        0,
                        finalList.size(),
                        requestCharge,
                        aggregatedDiagnostics,
                        null
                    );
                    diagnosticsAccessor
                        .setDiagnosticsContext(
                            aggregatedDiagnostics,
                            ctx);
                }

                headers.put(HttpConstants.HttpHeaders.REQUEST_CHARGE, Double
                    .toString(requestCharge));
                FeedResponse<T> frp = BridgeInternal
                    .createFeedResponseWithQueryMetrics(
                        finalList,
                        headers,
                        aggregatedQueryMetrics,
                        null,
                        false,
                        false,
                        aggregatedDiagnostics);
                return frp;
            })
            .onErrorMap(throwable -> {
                if (throwable instanceof CosmosException) {
                    CosmosException cosmosException = (CosmosException)throwable;
                    CosmosDiagnostics diagnostics = cosmosException.getDiagnostics();
                    if (diagnostics != null) {
                        state.mergeDiagnosticsContext();
                        CosmosDiagnosticsContext ctx = state.getDiagnosticsContextSnapshot();
                        if (ctx != null) {
                            ctxAccessor.recordOperation(
                                ctx,
                                cosmosException.getStatusCode(),
                                cosmosException.getSubStatusCode(),
                                0,
                                cosmosException.getRequestCharge(),
                                diagnostics,
                                throwable
                            );
                            diagnosticsAccessor
                                .setDiagnosticsContext(
                                    diagnostics,
                                    state.getDiagnosticsContextSnapshot());
                        }
                    }

                    return cosmosException;
                }

                return throwable;
            });
    }


    private <T> Mono<List<FeedResponse<T>>> readMany(
        List<CosmosItemIdentity> itemIdentityList,
        String collectionLink,
        QueryFeedOperationState state,
        ScopedDiagnosticsFactory diagnosticsFactory,
        Class<T> klass) {

        String resourceLink = parentResourceLinkToQueryLink(collectionLink, ResourceType.Document);
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(diagnosticsFactory,
            OperationType.Query,
            ResourceType.Document,
            collectionLink, null
        );

        // This should not get to backend
        Mono<Utils.ValueHolder<DocumentCollection>> collectionObs =
            collectionCache.resolveCollectionAsync(null, request);

        return collectionObs
            .flatMap(documentCollectionResourceResponse -> {
                    final DocumentCollection collection = documentCollectionResourceResponse.v;
                    if (collection == null) {
                        return Mono.error(new IllegalStateException("Collection cannot be null"));
                    }

                    final PartitionKeyDefinition pkDefinition = collection.getPartitionKey();

                    Mono<Utils.ValueHolder<CollectionRoutingMap>> valueHolderMono = partitionKeyRangeCache
                        .tryLookupAsync(BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics),
                            collection.getResourceId(),
                            null,
                            null);

                    return valueHolderMono
                        .flatMap(collectionRoutingMapValueHolder -> {
                            Map<PartitionKeyRange, List<CosmosItemIdentity>> partitionRangeItemKeyMap = new HashMap<>();
                            CollectionRoutingMap routingMap = collectionRoutingMapValueHolder.v;
                            if (routingMap == null) {
                                return Mono.error(new IllegalStateException("Failed to get routing map."));
                            }
                            itemIdentityList
                                .forEach(itemIdentity -> {
                                    //Check no partial partition keys are being used
                                    if (pkDefinition.getKind().equals(PartitionKind.MULTI_HASH) &&
                                        ModelBridgeInternal.getPartitionKeyInternal(itemIdentity.getPartitionKey())
                                                           .getComponents().size() != pkDefinition.getPaths().size()) {
                                        throw new IllegalArgumentException(RMResources.PartitionKeyMismatch);
                                    }
                                    String effectivePartitionKeyString = PartitionKeyInternalHelper
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
                            Map<PartitionKeyRange, SqlQuerySpec> rangeQueryMap = getRangeQueryMap(partitionRangeItemKeyMap, collection.getPartitionKey());

                            // create point reads
                            Flux<FeedResponse<T>> pointReads = pointReadsForReadMany(
                                diagnosticsFactory,
                                partitionRangeItemKeyMap,
                                resourceLink,
                                state.getQueryOptions(),
                                klass);

                            // create the executable query
                            Flux<FeedResponse<T>> queries = queryForReadMany(
                                diagnosticsFactory,
                                resourceLink,
                                new SqlQuerySpec(DUMMY_SQL_QUERY),
                                state.getQueryOptions(),
                                klass,
                                ResourceType.Document,
                                collection,
                                Collections.unmodifiableMap(rangeQueryMap));

                            // merge results from point reads and queries
                            return Flux.merge(pointReads, queries).collectList();
                        });
                }
            );
    }

    private Map<PartitionKeyRange, SqlQuerySpec> getRangeQueryMap(
        Map<PartitionKeyRange, List<CosmosItemIdentity>> partitionRangeItemKeyMap,
        PartitionKeyDefinition partitionKeyDefinition) {
        //TODO: Optimise this to include all types of partitionkeydefinitions. ex: c["prop1./ab"]["key1"]

        Map<PartitionKeyRange, SqlQuerySpec> rangeQueryMap = new HashMap<>();
        List<String> partitionKeySelectors = createPkSelectors(partitionKeyDefinition);

        for(Map.Entry<PartitionKeyRange, List<CosmosItemIdentity>> entry: partitionRangeItemKeyMap.entrySet()) {
            SqlQuerySpec sqlQuerySpec;
            List<CosmosItemIdentity> cosmosItemIdentityList = entry.getValue();
            if (cosmosItemIdentityList.size() > 1) {
                if (partitionKeySelectors.size() == 1 && partitionKeySelectors.get(0).equals("[\"id\"]")) {
                    sqlQuerySpec = createReadManyQuerySpecPartitionKeyIdSame(cosmosItemIdentityList);
                } else {
                    sqlQuerySpec = createReadManyQuerySpec(entry.getValue(), partitionKeySelectors);
                }
                // Add query for this partition to rangeQueryMap
                rangeQueryMap.put(entry.getKey(), sqlQuerySpec);
            }
        }

        return rangeQueryMap;
    }

    private SqlQuerySpec createReadManyQuerySpecPartitionKeyIdSame(List<CosmosItemIdentity> idPartitionKeyPairList) {

        StringBuilder queryStringBuilder = new StringBuilder();
        List<SqlParameter> parameters = new ArrayList<>();

        queryStringBuilder.append("SELECT * FROM c WHERE c.id IN ( ");
        for (int i = 0; i < idPartitionKeyPairList.size(); i++) {
            CosmosItemIdentity itemIdentity = idPartitionKeyPairList.get(i);

            String idValue = itemIdentity.getId();
            String idParamName = "@param" + i;

            parameters.add(new SqlParameter(idParamName, idValue));
            queryStringBuilder.append(idParamName);

            if (i < idPartitionKeyPairList.size() - 1) {
                queryStringBuilder.append(", ");
            }
        }
        queryStringBuilder.append(" )");

        return new SqlQuerySpec(queryStringBuilder.toString(), parameters);
    }

    private SqlQuerySpec createReadManyQuerySpec(
        List<CosmosItemIdentity> itemIdentities,
        List<String> partitionKeySelectors) {
        StringBuilder queryStringBuilder = new StringBuilder();
        List<SqlParameter> parameters = new ArrayList<>();

        queryStringBuilder.append("SELECT * FROM c WHERE ( ");
        int paramCount = 0;
        for (int i = 0; i < itemIdentities.size(); i++) {
            CosmosItemIdentity itemIdentity = itemIdentities.get(i);

            PartitionKey pkValueAsPartitionKey = itemIdentity.getPartitionKey();
            Object[] pkValues = ModelBridgeInternal.getPartitionKeyInternal(pkValueAsPartitionKey).toObjectArray();
            List<List<String>> partitionKeyParams = new ArrayList<>();
            int pathCount = 0;
            for (Object pkComponentValue : pkValues) {
                String pkParamName = "@param" + paramCount;
                partitionKeyParams.add(Arrays.asList(partitionKeySelectors.get(pathCount), pkParamName));
                parameters.add(new SqlParameter(pkParamName, pkComponentValue));
                paramCount++;
                pathCount++;
            }

            String idValue = itemIdentity.getId();
            String idParamName = "@param" + paramCount;
            paramCount++;
            parameters.add(new SqlParameter(idParamName, idValue));

            queryStringBuilder.append("(");
            queryStringBuilder.append("c.id = ");
            queryStringBuilder.append(idParamName);

            // partition key def
            for (List<String> pkParam: partitionKeyParams) {
                queryStringBuilder.append(" AND ");
                queryStringBuilder.append(" c");
                queryStringBuilder.append(pkParam.get(0));
                queryStringBuilder.append((" = "));
                queryStringBuilder.append(pkParam.get(1));
            }
            queryStringBuilder.append(" )");

            if (i < itemIdentities.size() - 1) {
                queryStringBuilder.append(" OR ");
            }
        }
        queryStringBuilder.append(" )");

        return new SqlQuerySpec(queryStringBuilder.toString(), parameters);
    }

    private List<String> createPkSelectors(PartitionKeyDefinition partitionKeyDefinition) {
        return partitionKeyDefinition.getPaths()
            .stream()
            .map(pathPart -> StringUtils.substring(pathPart, 1)) // skip starting /
            .map(pathPart -> StringUtils.replace(pathPart, "\"", "\\")) // escape quote
            .map(part -> "[\"" + part + "\"]")
            .collect(Collectors.toList());
    }

    private <T> Flux<FeedResponse<T>> queryForReadMany(
        ScopedDiagnosticsFactory diagnosticsFactory,
        String parentResourceLink,
        SqlQuerySpec sqlQuery,
        CosmosQueryRequestOptions options,
        Class<T> klass,
        ResourceType resourceTypeEnum,
        DocumentCollection collection,
        Map<PartitionKeyRange, SqlQuerySpec> rangeQueryMap) {

        if (rangeQueryMap.isEmpty()) {
            return Flux.empty();
        }

        UUID activityId = UUIDs.nonBlockingRandomUUID();

        final AtomicBoolean isQueryCancelledOnTimeout = new AtomicBoolean(false);

        IDocumentQueryClient queryClient = documentQueryClientImpl(RxDocumentClientImpl.this, getOperationContextAndListenerTuple(options));
        Flux<? extends IDocumentQueryExecutionContext<T>> executionContext =
            DocumentQueryExecutionContextFactory.createReadManyQueryAsync(
                diagnosticsFactory,
                queryClient,
                collection.getResourceId(),
                sqlQuery,
                rangeQueryMap,
                options,
                collection,
                parentResourceLink,
                activityId,
                klass,
                resourceTypeEnum,
                isQueryCancelledOnTimeout);

        Flux<FeedResponse<T>> feedResponseFlux = executionContext.flatMap(IDocumentQueryExecutionContext<T>::executeAsync);

        RequestOptions requestOptions = options == null? null : ImplementationBridgeHelpers
            .CosmosQueryRequestOptionsHelper
            .getCosmosQueryRequestOptionsAccessor()
            .toRequestOptions(options);

        CosmosEndToEndOperationLatencyPolicyConfig endToEndPolicyConfig =
            getEndToEndOperationLatencyPolicyConfig(requestOptions, ResourceType.Document, OperationType.Query);

        if (endToEndPolicyConfig != null && endToEndPolicyConfig.isEnabled()) {
            return getFeedResponseFluxWithTimeout(
                feedResponseFlux,
                endToEndPolicyConfig,
                options,
                isQueryCancelledOnTimeout,
                diagnosticsFactory);
        }

        return feedResponseFlux;
    }

    private <T> Flux<FeedResponse<T>> pointReadsForReadMany(
        ScopedDiagnosticsFactory diagnosticsFactory,
        Map<PartitionKeyRange,
        List<CosmosItemIdentity>> singleItemPartitionRequestMap,
        String resourceLink,
        CosmosQueryRequestOptions queryRequestOptions,
        Class<T> klass) {

        // if there is any factory method being passed in, use the factory method to deserializ the object
        // else fallback to use the original way
        // typically used by spark trying to convert into SparkRowItem
        CosmosItemSerializer effectiveItemSerializer = getEffectiveItemSerializer(queryRequestOptions);

        return Flux.fromIterable(singleItemPartitionRequestMap.values())
            .flatMap(cosmosItemIdentityList -> {
                if (cosmosItemIdentityList.size() == 1) {
                    CosmosItemIdentity firstIdentity = cosmosItemIdentityList.get(0);
                    RequestOptions requestOptions = ImplementationBridgeHelpers
                        .CosmosQueryRequestOptionsHelper
                        .getCosmosQueryRequestOptionsAccessor()
                        .toRequestOptions(queryRequestOptions);
                    requestOptions.setPartitionKey(firstIdentity.getPartitionKey());
                    return this.readDocument((resourceLink + firstIdentity.getId()), requestOptions, diagnosticsFactory)
                        .flatMap(resourceResponse -> Mono.just(
                            new ImmutablePair<ResourceResponse<Document>, CosmosException>(resourceResponse, null)
                        ))
                        .onErrorResume(throwable -> {
                            Throwable unwrappedThrowable = Exceptions.unwrap(throwable);

                            if (unwrappedThrowable instanceof CosmosException) {

                                CosmosException cosmosException = (CosmosException) unwrappedThrowable;

                                int statusCode = cosmosException.getStatusCode();
                                int subStatusCode = cosmosException.getSubStatusCode();

                                if (statusCode == HttpConstants.StatusCodes.NOTFOUND && subStatusCode == HttpConstants.SubStatusCodes.UNKNOWN) {
                                    return Mono.just(new ImmutablePair<ResourceResponse<Document>, CosmosException>(null, cosmosException));
                                }
                            }

                            return Mono.error(unwrappedThrowable);
                        });
                }
                return Mono.empty();
            })
            .flatMap(resourceResponseToExceptionPair -> {

                ResourceResponse<Document> resourceResponse = resourceResponseToExceptionPair.getLeft();
                CosmosException cosmosException = resourceResponseToExceptionPair.getRight();
                FeedResponse<T> feedResponse;

                if (cosmosException != null) {
                    feedResponse = ModelBridgeInternal.createFeedResponse(new ArrayList<>(), cosmosException.getResponseHeaders());
                    diagnosticsAccessor.addClientSideDiagnosticsToFeed(
                        feedResponse.getCosmosDiagnostics(),
                        Collections.singleton(
                            BridgeInternal.getClientSideRequestStatics(cosmosException.getDiagnostics())));
                } else {
                    CosmosItemResponse<T> cosmosItemResponse =
                        itemResponseAccessor.createCosmosItemResponse(resourceResponse, klass, effectiveItemSerializer);

                    feedResponse = ModelBridgeInternal.createFeedResponse(
                            Arrays.asList(cosmosItemResponse.getItem()),
                            cosmosItemResponse.getResponseHeaders());

                    diagnosticsAccessor.addClientSideDiagnosticsToFeed(
                        feedResponse.getCosmosDiagnostics(),
                        Collections.singleton(
                            BridgeInternal.getClientSideRequestStatics(cosmosItemResponse.getDiagnostics())));
                }

                return Mono.just(feedResponse);
            });
    }

    @Override
    public <T> Flux<FeedResponse<T>> queryDocuments(
        String collectionLink, String query, QueryFeedOperationState state, Class<T> classOfT) {

        return queryDocuments(collectionLink, new SqlQuerySpec(query), state, classOfT);
    }

    @Override
    public CosmosItemSerializer getEffectiveItemSerializer(CosmosItemSerializer requestOptionsItemSerializer) {
        if (requestOptionsItemSerializer != null) {
            return requestOptionsItemSerializer;
        }

        if (this.defaultCustomSerializer != null) {
            return this.defaultCustomSerializer;
        }

        return CosmosItemSerializer.DEFAULT_SERIALIZER;
    }

    private <T> CosmosItemSerializer getEffectiveItemSerializer(CosmosQueryRequestOptions queryRequestOptions) {

        CosmosItemSerializer requestOptionsItemSerializer =
            queryRequestOptions != null ? queryRequestOptions.getCustomItemSerializer() :  null;

        return this.getEffectiveItemSerializer(requestOptionsItemSerializer);
    }

    private <T> CosmosItemSerializer getEffectiveItemSerializer(CosmosItemRequestOptions itemRequestOptions) {

        CosmosItemSerializer requestOptionsItemSerializer =
            itemRequestOptions != null ? itemRequestOptions.getCustomItemSerializer() :  null;

        return this.getEffectiveItemSerializer(requestOptionsItemSerializer);
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
            public <T> Mono<T> executeFeedOperationWithAvailabilityStrategy(
                ResourceType resourceType,
                OperationType operationType,
                Supplier<DocumentClientRetryPolicy> retryPolicyFactory,
                RxDocumentServiceRequest req,
                BiFunction<Supplier<DocumentClientRetryPolicy>, RxDocumentServiceRequest, Mono<T>> feedOperation,
                String collectionLink) {

                return RxDocumentClientImpl.this.executeFeedOperationWithAvailabilityStrategy(
                    resourceType,
                    operationType,
                    retryPolicyFactory,
                    req,
                    feedOperation,
                    collectionLink);
            }

            @Override
            public <T> CosmosItemSerializer getEffectiveItemSerializer(CosmosQueryRequestOptions queryRequestOptions) {
                return RxDocumentClientImpl.this.getEffectiveItemSerializer(queryRequestOptions);
            }

            @Override
            public ReadConsistencyStrategy getReadConsistencyStrategy() {
                return RxDocumentClientImpl.this.getReadConsistencyStrategy();
            }

            @Override
            public ConsistencyLevel getConsistencyLevel() {
                return RxDocumentClientImpl.this.getConsistencyLevel();
            }

            @Override
            public void validateAndLogNonDefaultReadConsistencyStrategy(String readConsistencyStrategyName) {
                RxDocumentClientImpl.this.validateAndLogNonDefaultReadConsistencyStrategy(readConsistencyStrategyName);
            }

            @Override
            public Mono<RxDocumentServiceResponse> readFeedAsync(RxDocumentServiceRequest request) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Mono<RxDocumentServiceRequest> populateFeedRangeHeader(RxDocumentServiceRequest request) {

                if (RxDocumentClientImpl.this.requiresFeedRangeFiltering(request)) {
                    return request
                        .getFeedRange()
                        .populateFeedRangeFilteringHeaders(RxDocumentClientImpl.this.partitionKeyRangeCache, request, RxDocumentClientImpl.this.collectionCache.resolveCollectionAsync(BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics), request))
                        .flatMap(ignore -> Mono.just(request));
                } else {
                    return Mono.just(request);
                }
            }

            @Override
            public Mono<RxDocumentServiceRequest> addPartitionLevelUnavailableRegionsOnRequest(RxDocumentServiceRequest request, CosmosQueryRequestOptions queryRequestOptions, DocumentClientRetryPolicy documentClientRetryPolicy) {

                if (RxDocumentClientImpl.this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker.isPerPartitionLevelCircuitBreakingApplicable(request) || RxDocumentClientImpl.this.globalPartitionEndpointManagerForPerPartitionAutomaticFailover.isPerPartitionAutomaticFailoverApplicable(request)) {

                    String collectionRid = RxDocumentClientImpl.qryOptAccessor.getCollectionRid(queryRequestOptions);

                    checkNotNull(collectionRid, "Argument 'collectionRid' cannot be null!");

                    return RxDocumentClientImpl.this.partitionKeyRangeCache.tryLookupAsync(BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics), collectionRid, null, null)
                        .flatMap(collectionRoutingMapValueHolder -> {

                            if (collectionRoutingMapValueHolder.v == null) {
                                return Mono.error(new CollectionRoutingMapNotFoundException("Argument 'collectionRoutingMapValueHolder.v' cannot be null!"));
                            }

                            PartitionKeyRange preResolvedPartitionKeyRangeIfAny
                                = RxDocumentClientImpl.this.setPartitionKeyRangeForFeedRequestForPerPartitionAutomaticFailover(
                                request,
                                queryRequestOptions,
                                collectionRoutingMapValueHolder.v,
                                null);

                            RxDocumentClientImpl.this.addPartitionLevelUnavailableRegionsForFeedRequestForPerPartitionCircuitBreaker(
                                request,
                                queryRequestOptions,
                                collectionRoutingMapValueHolder.v,
                                preResolvedPartitionKeyRangeIfAny);

                            // onBeforeSendRequest uses excluded regions to know the next location endpoint
                            // to route the request to unavailable regions are effectively excluded regions for this request
                            if (documentClientRetryPolicy != null) {
                                documentClientRetryPolicy.onBeforeSendRequest(request);
                            }

                            return Mono.just(request);
                        });
                } else {
                    return Mono.just(request);
                }
            }

            @Override
            public GlobalEndpointManager getGlobalEndpointManager() {
                return RxDocumentClientImpl.this.getGlobalEndpointManager();
            }

            @Override
            public GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker getGlobalPartitionEndpointManagerForCircuitBreaker() {
                return RxDocumentClientImpl.this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker;
            }
        };
    }

    @Override
    public <T> Flux<FeedResponse<T>> queryDocuments(
        String collectionLink,
        SqlQuerySpec querySpec,
        QueryFeedOperationState state,
        Class<T> classOfT) {
        SqlQuerySpecLogger.getInstance().logQuery(querySpec);
        return createQuery(collectionLink, querySpec, state, classOfT, ResourceType.Document);
    }

    @Override
    public <T> Flux<FeedResponse<T>> queryDocumentChangeFeed(
        final DocumentCollection collection,
        final CosmosChangeFeedRequestOptions requestOptions,
        Class<T> classOfT,
        DiagnosticsClientContext diagnosticsClientContext) {

        checkNotNull(collection, "Argument 'collection' must not be null.");


        ChangeFeedQueryImpl<T> changeFeedQueryImpl = new ChangeFeedQueryImpl<>(
            this,
            ResourceType.Document,
            classOfT,
            collection.getAltLink(),
            collection.getResourceId(),
            requestOptions,
            diagnosticsClientContext);

        return changeFeedQueryImpl.executeAsync();
    }

    @Override
    public <T> Flux<FeedResponse<T>> queryDocumentChangeFeedFromPagedFlux(
        String collectionLink,
        ChangeFeedOperationState state,
        Class<T> classOfT) {

        final ScopedDiagnosticsFactory diagnosticsFactory = new ScopedDiagnosticsFactory(this, false);
        state.registerDiagnosticsFactory(
            diagnosticsFactory::reset,
            diagnosticsFactory::merge);

        StaleResourceRetryPolicy staleResourceRetryPolicy = new StaleResourceRetryPolicy(
            this.collectionCache,
            null,
            collectionLink,
            changeFeedOptionsAccessor.getProperties(state.getChangeFeedOptions()),
            changeFeedOptionsAccessor.getHeaders(state.getChangeFeedOptions()),
            this.sessionContainer,
            diagnosticsFactory);

        return ObservableHelper
            .fluxInlineIfPossibleAsObs(
                () -> this.queryDocumentChangeFeedFromPagedFluxInternal(collectionLink, state, classOfT, diagnosticsFactory),
                staleResourceRetryPolicy)
            .flatMap(result -> {
                diagnosticsFactory.merge(state.getDiagnosticsContextSnapshot());
                return Mono.just(result);
            })
            .onErrorMap(throwable -> {
                diagnosticsFactory.merge(state.getDiagnosticsContextSnapshot());
                return throwable;
            })
            .doOnCancel(() -> diagnosticsFactory.merge(state.getDiagnosticsContextSnapshot()));
    }

    private <T> Flux<FeedResponse<T>> queryDocumentChangeFeedFromPagedFluxInternal(
        String collectionLink,
        ChangeFeedOperationState state,
        Class<T> classOfT,
        DiagnosticsClientContext diagnosticsClientContext) {

        return this.getCollectionCache()
            .resolveByNameAsync(null, collectionLink, null)
            .flatMapMany(collection -> {
                if (collection == null) {
                    throw new IllegalStateException("Collection can not be null");
                }

                CosmosChangeFeedRequestOptions clonedOptions = changeFeedOptionsAccessor.clone(state.getChangeFeedOptions());

                CosmosChangeFeedRequestOptionsImpl optionsImpl = changeFeedOptionsAccessor.getImpl(clonedOptions);

                CosmosOperationDetails operationDetails = operationDetailsAccessor.create(optionsImpl, state.getDiagnosticsContextSnapshot());
                this.operationPolicies.forEach(policy -> {
                    try {
                        policy.process(operationDetails);
                    } catch (RuntimeException exception) {
                        logger.info("The following exception was thrown by a custom policy on changeFeed operation" + exception.getMessage());
                        throw(exception);
                    }
                });

                ReadConsistencyStrategy requestLevelReadConsistencyStrategy = optionsImpl != null
                    ? optionsImpl.getReadConsistencyStrategy()
                    : null;

                ReadConsistencyStrategy effectiveReadConsistencyStrategy = readConsistencyStrategyAccessor
                    .getEffectiveReadConsistencyStrategy(
                        ResourceType.Document,
                        OperationType.ReadFeed,
                        requestLevelReadConsistencyStrategy,
                        this.readConsistencyStrategy);

                ctxAccessor.setRequestOptions(
                    state.getDiagnosticsContextSnapshot(),
                    optionsImpl,
                    effectiveReadConsistencyStrategy);

                return queryDocumentChangeFeed(collection, clonedOptions, classOfT, diagnosticsClientContext);
            });
    }

    @Override
    public <T> Flux<FeedResponse<T>> readAllDocuments(
        String collectionLink,
        PartitionKey partitionKey,
        QueryFeedOperationState state,
        Class<T> classOfT) {

        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }

        if (partitionKey == null) {
            throw new IllegalArgumentException("partitionKey");
        }

        final CosmosQueryRequestOptions effectiveOptions =
            qryOptAccessor.clone(state.getQueryOptions());

        RequestOptions nonNullRequestOptions = qryOptAccessor.toRequestOptions(effectiveOptions);

        CosmosEndToEndOperationLatencyPolicyConfig endToEndPolicyConfig =
            nonNullRequestOptions.getCosmosEndToEndLatencyPolicyConfig();

        List<String> orderedApplicableRegionsForSpeculation = getApplicableRegionsForSpeculation(
            endToEndPolicyConfig,
            ResourceType.Document,
            OperationType.Query,
            false,
            nonNullRequestOptions);

        ScopedDiagnosticsFactory diagnosticsFactory = new ScopedDiagnosticsFactory(this, false);

        if (orderedApplicableRegionsForSpeculation.size() < 2) {
            state.registerDiagnosticsFactory(
                () -> {},
                (ctx) -> diagnosticsFactory.merge(ctx));
        } else {
            state.registerDiagnosticsFactory(
                () -> diagnosticsFactory.reset(),
                (ctx) -> diagnosticsFactory.merge(ctx));
        }

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            diagnosticsFactory,
            OperationType.Query,
            ResourceType.Document,
            collectionLink,
            null
        );

        // This should not get to backend
        Flux<Utils.ValueHolder<DocumentCollection>> collectionObs =
            collectionCache.resolveCollectionAsync(null, request).flux();

        return collectionObs.flatMap(documentCollectionResourceResponse -> {

            DocumentCollection collection = documentCollectionResourceResponse.v;
            if (collection == null) {
                return Mono.error(new IllegalStateException("Collection cannot be null"));
            }

            PartitionKeyDefinition pkDefinition = collection.getPartitionKey();
            List<String> partitionKeySelectors = createPkSelectors(pkDefinition);
            SqlQuerySpec querySpec = createLogicalPartitionScanQuerySpec(partitionKey, partitionKeySelectors);

            String resourceLink = parentResourceLinkToQueryLink(collectionLink, ResourceType.Document);
            UUID activityId = UUIDs.nonBlockingRandomUUID();

            final AtomicBoolean isQueryCancelledOnTimeout = new AtomicBoolean(false);

            IDocumentQueryClient queryClient = documentQueryClientImpl(RxDocumentClientImpl.this, getOperationContextAndListenerTuple(state.getQueryOptions()));

            // Trying to put this logic as low as the query pipeline
            // Since for parallelQuery, each partition will have its own request, so at this point, there will be no request associate with this retry policy.
            StaleResourceRetryPolicy staleResourceRetryPolicy = new StaleResourceRetryPolicy(
                this.collectionCache,
                null,
                resourceLink,
                qryOptAccessor.getProperties(effectiveOptions),
                qryOptAccessor.getHeaders(effectiveOptions),
                this.sessionContainer,
                diagnosticsFactory);

            Flux<FeedResponse<T>> innerFlux = ObservableHelper.fluxInlineIfPossibleAsObs(
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
                            return Mono.error(new IllegalStateException("Failed to get routing map."));
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
                            diagnosticsFactory,
                            resourceLink,
                            querySpec,
                            ModelBridgeInternal.setPartitionKeyRangeIdInternal(effectiveOptions, range.getId()),
                            classOfT, //Document.class
                            ResourceType.Document,
                            queryClient,
                            activityId,
                            isQueryCancelledOnTimeout);
                    });
                },
                staleResourceRetryPolicy);

            if (orderedApplicableRegionsForSpeculation.size() < 2) {
                return innerFlux;
            }

            return innerFlux
                .flatMap(result -> {
                    diagnosticsFactory.merge(nonNullRequestOptions);
                    return Mono.just(result);
                })
                .onErrorMap(throwable -> {
                    diagnosticsFactory.merge(nonNullRequestOptions);
                    return throwable;
                })
                .doOnCancel(() -> diagnosticsFactory.merge(nonNullRequestOptions));
        });
    }

    @Override
    public Map<String, PartitionedQueryExecutionInfo> getQueryPlanCache() {
        return queryPlanCache;
    }

    @Override
    public Flux<FeedResponse<PartitionKeyRange>> readPartitionKeyRanges(final String collectionLink,
                                                                        QueryFeedOperationState state) {

        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }

        return nonDocumentReadFeed(state, ResourceType.PartitionKeyRange, PartitionKeyRange.class,
                Utils.joinPath(collectionLink, Paths.PARTITION_KEY_RANGES_PATH_SEGMENT));
    }

    @Override
    public Flux<FeedResponse<PartitionKeyRange>> readPartitionKeyRanges(String collectionLink, CosmosQueryRequestOptions options) {
        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }

        return nonDocumentReadFeed(options, ResourceType.PartitionKeyRange, PartitionKeyRange.class,
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
        return RxDocumentServiceRequest.create(this, operationType,
            ResourceType.StoredProcedure, path, storedProcedure, requestHeaders, options);
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
        return RxDocumentServiceRequest.create(this,
            operationType, ResourceType.UserDefinedFunction, path, udf, requestHeaders, options);
    }

    @Override
    public Mono<ResourceResponse<StoredProcedure>> createStoredProcedure(String collectionLink,
                                                                               StoredProcedure storedProcedure, RequestOptions options) {
        DocumentClientRetryPolicy requestRetryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
    public Mono<ResourceResponse<StoredProcedure>> replaceStoredProcedure(StoredProcedure storedProcedure,
                                                                                RequestOptions options) {
        DocumentClientRetryPolicy requestRetryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
        DocumentClientRetryPolicy requestRetryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
                                                                    QueryFeedOperationState state) {

        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }

        return nonDocumentReadFeed(state, ResourceType.StoredProcedure, StoredProcedure.class,
                Utils.joinPath(collectionLink, Paths.STORED_PROCEDURES_PATH_SEGMENT));
    }

    @Override
    public Flux<FeedResponse<StoredProcedure>> queryStoredProcedures(String collectionLink, String query,
                                                                     QueryFeedOperationState state) {
        return queryStoredProcedures(collectionLink, new SqlQuerySpec(query), state);
    }

    @Override
    public Flux<FeedResponse<StoredProcedure>> queryStoredProcedures(String collectionLink,
                                                                           SqlQuerySpec querySpec, QueryFeedOperationState state) {
        return createQuery(collectionLink, querySpec, state, StoredProcedure.class, ResourceType.StoredProcedure);
    }

    @Override
    public Mono<StoredProcedureResponse> executeStoredProcedure(String storedProcedureLink,
                                                                      RequestOptions options, List<Object> procedureParams) {
        DocumentClientRetryPolicy documentClientRetryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
        return ObservableHelper.inlineIfPossibleAsObs(() -> executeStoredProcedureInternal(storedProcedureLink, options, procedureParams, documentClientRetryPolicy), documentClientRetryPolicy);
    }

    @Override
    public Mono<CosmosBatchResponse> executeBatchRequest(String collectionLink,
                                                         ServerBatchRequest serverBatchRequest,
                                                         RequestOptions options,
                                                         boolean disableAutomaticIdGeneration,
                                                         boolean disableStaledResourceExceptionHandling) {
        AtomicReference<RxDocumentServiceRequest> requestReference = new AtomicReference<>();

        Consumer<CosmosException> gwModeE2ETimeoutDiagnosticHandler
            = (operationCancelledException) -> {

            RxDocumentServiceRequest request = requestReference.get();
            this.addCancelledGatewayModeDiagnosticsIntoCosmosException(operationCancelledException, request);
        };

        RequestOptions nonNullRequestOptions = options != null ? options : new RequestOptions();
        CosmosEndToEndOperationLatencyPolicyConfig endToEndPolicyConfig =
            getEndToEndOperationLatencyPolicyConfig(nonNullRequestOptions, ResourceType.Document, OperationType.Batch);
        ScopedDiagnosticsFactory scopedDiagnosticsFactory = new ScopedDiagnosticsFactory(this, false);
        scopedDiagnosticsFactory.setGwModeE2ETimeoutDiagnosticsHandler(gwModeE2ETimeoutDiagnosticHandler);

        DocumentClientRetryPolicy documentClientRetryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy(scopedDiagnosticsFactory);
        if (!disableStaledResourceExceptionHandling) {
            documentClientRetryPolicy = new StaleResourceRetryPolicy(
                this.collectionCache,
                documentClientRetryPolicy,
                collectionLink,
                nonNullRequestOptions.getProperties(),
                nonNullRequestOptions.getHeaders(),
                this.sessionContainer,
                scopedDiagnosticsFactory);
        }

        final DocumentClientRetryPolicy finalRetryPolicy = documentClientRetryPolicy;

        return handleCircuitBreakingFeedbackForPointOperation(
            getPointOperationResponseMonoWithE2ETimeout(
                nonNullRequestOptions,
                endToEndPolicyConfig,
                ObservableHelper
                    .inlineIfPossibleAsObs(() -> executeBatchRequestInternal(
                        collectionLink,
                        serverBatchRequest,
                        options,
                        finalRetryPolicy,
                        disableAutomaticIdGeneration,
                        requestReference), documentClientRetryPolicy),
                scopedDiagnosticsFactory
            ),
            requestReference, endToEndPolicyConfig);
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

            if (options != null) {
                request.requestContext.setExcludeRegions(options.getExcludedRegions());
                request.requestContext.setKeywordIdentifiers(options.getKeywordIdentifiers());
            }

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
                                                                         boolean disableAutomaticIdGeneration,
                                                                         AtomicReference<RxDocumentServiceRequest> requestReference) {

        try {
            logger.debug("Executing a Batch request with number of operations {}", serverBatchRequest.getOperations().size());

            Mono<RxDocumentServiceRequest> requestObs = getBatchDocumentRequest(requestRetryPolicy, collectionLink, serverBatchRequest, options, disableAutomaticIdGeneration);

            Mono<RxDocumentServiceResponse> responseObservable =
                requestObs.flatMap(request -> {
                    requestReference.set(request);
                    return create(request, requestRetryPolicy, getOperationContextAndListenerTuple(options));
                });

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
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
        return RxDocumentServiceRequest.create(this, operationType, ResourceType.Trigger, path,
                trigger, requestHeaders, options);
    }

    @Override
    public Mono<ResourceResponse<Trigger>> replaceTrigger(Trigger trigger, RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
    public Flux<FeedResponse<Trigger>> readTriggers(String collectionLink, QueryFeedOperationState state) {

        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }

        return nonDocumentReadFeed(state, ResourceType.Trigger, Trigger.class,
                Utils.joinPath(collectionLink, Paths.TRIGGERS_PATH_SEGMENT));
    }

    @Override
    public Flux<FeedResponse<Trigger>> queryTriggers(String collectionLink, String query,
                                                     QueryFeedOperationState state) {
        return queryTriggers(collectionLink, new SqlQuerySpec(query), state);
    }

    @Override
    public Flux<FeedResponse<Trigger>> queryTriggers(String collectionLink, SqlQuerySpec querySpec,
                                                     QueryFeedOperationState state) {
        return createQuery(collectionLink, querySpec, state, Trigger.class, ResourceType.Trigger);
    }

    @Override
    public Mono<ResourceResponse<UserDefinedFunction>> createUserDefinedFunction(String collectionLink,
                                                                                       UserDefinedFunction udf, RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
    public Mono<ResourceResponse<UserDefinedFunction>> replaceUserDefinedFunction(UserDefinedFunction udf,
                                                                                        RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
                                                                                  QueryFeedOperationState state) {

        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }

        return nonDocumentReadFeed(state, ResourceType.UserDefinedFunction, UserDefinedFunction.class,
                Utils.joinPath(collectionLink, Paths.USER_DEFINED_FUNCTIONS_PATH_SEGMENT));
    }

    @Override
    public Flux<FeedResponse<UserDefinedFunction>> queryUserDefinedFunctions(
        String collectionLink,
        String query,
        QueryFeedOperationState state) {

        return queryUserDefinedFunctions(collectionLink, new SqlQuerySpec(query), state);
    }

    @Override
    public Flux<FeedResponse<UserDefinedFunction>> queryUserDefinedFunctions(
        String collectionLink,
        SqlQuerySpec querySpec,
        QueryFeedOperationState state) {

        return createQuery(collectionLink, querySpec, state, UserDefinedFunction.class, ResourceType.UserDefinedFunction);
    }

    @Override
    public Mono<ResourceResponse<Conflict>> readConflict(String conflictLink, RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
    public Flux<FeedResponse<Conflict>> readConflicts(String collectionLink, QueryFeedOperationState state) {

        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }

        return nonDocumentReadFeed(state, ResourceType.Conflict, Conflict.class,
                Utils.joinPath(collectionLink, Paths.CONFLICTS_PATH_SEGMENT));
    }

    @Override
    public Flux<FeedResponse<Conflict>> queryConflicts(String collectionLink, String query,
                                                       QueryFeedOperationState state) {
        return queryConflicts(collectionLink, new SqlQuerySpec(query), state);
    }

    @Override
    public Flux<FeedResponse<Conflict>> queryConflicts(String collectionLink, SqlQuerySpec querySpec,
                                                       QueryFeedOperationState state) {
        return createQuery(collectionLink, querySpec, state, Conflict.class, ResourceType.Conflict);
    }

    @Override
    public Mono<ResourceResponse<Conflict>> deleteConflict(String conflictLink, RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
        DocumentClientRetryPolicy documentClientRetryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
        return RxDocumentServiceRequest.create(this,
            operationType, ResourceType.User, path, user, requestHeaders, options);
    }

    @Override
    public Mono<ResourceResponse<User>> replaceUser(User user, RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
        DocumentClientRetryPolicy retryPolicyInstance =  this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
    public Flux<FeedResponse<User>> readUsers(String databaseLink, QueryFeedOperationState state) {

        if (StringUtils.isEmpty(databaseLink)) {
            throw new IllegalArgumentException("databaseLink");
        }

        return nonDocumentReadFeed(state, ResourceType.User, User.class,
                Utils.joinPath(databaseLink, Paths.USERS_PATH_SEGMENT));
    }

    @Override
    public Flux<FeedResponse<User>> queryUsers(String databaseLink, String query, QueryFeedOperationState state) {
        return queryUsers(databaseLink, new SqlQuerySpec(query), state);
    }

    @Override
    public Flux<FeedResponse<User>> queryUsers(String databaseLink, SqlQuerySpec querySpec,
                                               QueryFeedOperationState state) {
        return createQuery(databaseLink, querySpec, state, User.class, ResourceType.User);
    }

    @Override
    public Mono<ResourceResponse<ClientEncryptionKey>> readClientEncryptionKey(String clientEncryptionKeyLink,
                                                                RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
        return RxDocumentServiceRequest.create(this,
            operationType, ResourceType.ClientEncryptionKey, path, clientEncryptionKey, requestHeaders, options);
    }

    @Override
    public Mono<ResourceResponse<ClientEncryptionKey>> replaceClientEncryptionKey(ClientEncryptionKey clientEncryptionKey,
                                                                                  String nameBasedLink,
                                                                                  RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
    public Flux<FeedResponse<ClientEncryptionKey>> readClientEncryptionKeys(
        String databaseLink,
        QueryFeedOperationState state) {
        if (StringUtils.isEmpty(databaseLink)) {
            throw new IllegalArgumentException("databaseLink");
        }

        return nonDocumentReadFeed(state, ResourceType.ClientEncryptionKey, ClientEncryptionKey.class,
            Utils.joinPath(databaseLink, Paths.CLIENT_ENCRYPTION_KEY_PATH_SEGMENT));
    }

    @Override
    public Flux<FeedResponse<ClientEncryptionKey>> queryClientEncryptionKeys(
        String databaseLink,
        SqlQuerySpec querySpec,
        QueryFeedOperationState state) {
        return createQuery(databaseLink, querySpec, state, ClientEncryptionKey.class, ResourceType.ClientEncryptionKey);
    }

    @Override
    public Mono<ResourceResponse<Permission>> createPermission(String userLink, Permission permission,
                                                                     RequestOptions options) {
        DocumentClientRetryPolicy documentClientRetryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
        return ObservableHelper.inlineIfPossibleAsObs(() -> createPermissionInternal(userLink, permission, options, documentClientRetryPolicy), this.resetSessionTokenRetryPolicy.getRequestPolicy(null));
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
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
        return RxDocumentServiceRequest.create(this,
            operationType, ResourceType.Permission, path, permission, requestHeaders, options);
    }

    @Override
    public Mono<ResourceResponse<Permission>> replacePermission(Permission permission, RequestOptions options) {
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
    public Flux<FeedResponse<Permission>> readPermissions(String userLink, QueryFeedOperationState state) {

        if (StringUtils.isEmpty(userLink)) {
            throw new IllegalArgumentException("userLink");
        }

        return nonDocumentReadFeed(state, ResourceType.Permission, Permission.class,
                Utils.joinPath(userLink, Paths.PERMISSIONS_PATH_SEGMENT));
    }

    @Override
    public Flux<FeedResponse<Permission>> queryPermissions(String userLink, String query,
                                                           QueryFeedOperationState state) {
        return queryPermissions(userLink, new SqlQuerySpec(query), state);
    }

    @Override
    public Flux<FeedResponse<Permission>> queryPermissions(String userLink, SqlQuerySpec querySpec,
                                                           QueryFeedOperationState state) {
        return createQuery(userLink, querySpec, state, Permission.class, ResourceType.Permission);
    }

    @Override
    public Mono<ResourceResponse<Offer>> replaceOffer(Offer offer) {
        DocumentClientRetryPolicy documentClientRetryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
        DocumentClientRetryPolicy retryPolicyInstance = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
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
    public Flux<FeedResponse<Offer>> readOffers(QueryFeedOperationState state) {
        return nonDocumentReadFeed(state, ResourceType.Offer, Offer.class,
                Utils.joinPath(Paths.OFFERS_PATH_SEGMENT, null));
    }

    private <T> Flux<FeedResponse<T>> nonDocumentReadFeed(
        QueryFeedOperationState state,
        ResourceType resourceType,
        Class<T> klass,
        String resourceLink) {

        return nonDocumentReadFeed(state.getQueryOptions(), resourceType, klass, resourceLink);
    }

    private <T> Flux<FeedResponse<T>> nonDocumentReadFeed(
        CosmosQueryRequestOptions options,
        ResourceType resourceType,
        Class<T> klass,
        String resourceLink) {
        DocumentClientRetryPolicy retryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
        return ObservableHelper.fluxInlineIfPossibleAsObs(
            () -> nonDocumentReadFeedInternal(options, resourceType, klass, resourceLink, retryPolicy),
            retryPolicy);
    }

    private <T> Flux<FeedResponse<T>> nonDocumentReadFeedInternal(
        CosmosQueryRequestOptions options,
        ResourceType resourceType,
        Class<T> klass,
        String resourceLink,
        DocumentClientRetryPolicy retryPolicy) {

        final CosmosQueryRequestOptions nonNullOptions = options != null ? options : new CosmosQueryRequestOptions();
        Integer maxItemCount = ModelBridgeInternal.getMaxItemCountFromQueryRequestOptions(nonNullOptions);
        int maxPageSize = maxItemCount != null ? maxItemCount : -1;

        assert(resourceType != ResourceType.Document);
        // readFeed is only used for non-document operations - no need to wire up hedging
        BiFunction<String, Integer, RxDocumentServiceRequest> createRequestFunc = (continuationToken, pageSize) -> {
            Map<String, String> requestHeaders = new HashMap<>();
            if (continuationToken != null) {
                requestHeaders.put(HttpConstants.HttpHeaders.CONTINUATION, continuationToken);
            }
            requestHeaders.put(HttpConstants.HttpHeaders.PAGE_SIZE, Integer.toString(pageSize));
            RxDocumentServiceRequest request =  RxDocumentServiceRequest.create(this,
                OperationType.ReadFeed, resourceType, resourceLink, requestHeaders, nonNullOptions);
            retryPolicy.onBeforeSendRequest(request);
            return request;
        };

        Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeFunc =
            request -> readFeed(request)
                .map(response -> feedResponseAccessor.createFeedResponse(
                                    response,
                                    DefaultCosmosItemSerializer.INTERNAL_DEFAULT_SERIALIZER,
                                    klass));

        return Paginator
            .getPaginatedQueryResultAsObservable(
                nonNullOptions,
                createRequestFunc,
                executeFunc,
                maxPageSize,
                this.globalEndpointManager,
                this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker);
    }

    @Override
    public Flux<FeedResponse<Offer>> queryOffers(String query, QueryFeedOperationState state) {
        return queryOffers(new SqlQuerySpec(query), state);
    }

    @Override
    public Flux<FeedResponse<Offer>> queryOffers(SqlQuerySpec querySpec, QueryFeedOperationState state) {
        return createQuery(null, querySpec, state, Offer.class, ResourceType.Offer);
    }

    @Override
    public Mono<DatabaseAccount> getDatabaseAccount() {
        DocumentClientRetryPolicy documentClientRetryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy(null);
        return ObservableHelper.inlineIfPossibleAsObs(() -> getDatabaseAccountInternal(documentClientRetryPolicy),
         documentClientRetryPolicy);
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

    public ISessionContainer getSession() {
        return this.sessionContainer;
    }

    public void setSession(ISessionContainer sessionContainer) {
        this.sessionContainer = sessionContainer;
    }

    public CosmosAsyncClient getCachedCosmosAsyncClientSnapshot() {
        return cachedCosmosAsyncClientSnapshot.get();
    }

    @Override
    public RxClientCollectionCache getCollectionCache() {
        return this.collectionCache;
    }

    @Override
    public RxPartitionKeyRangeCache getPartitionKeyRangeCache() {
        return partitionKeyRangeCache;
    }

    @Override
    public GlobalEndpointManager getGlobalEndpointManager() {
        return this.globalEndpointManager;
    }

    @Override
    public GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker getGlobalPartitionEndpointManagerForCircuitBreaker() {
        return this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker;
    }

    @Override
    public GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover getGlobalPartitionEndpointManagerForPerPartitionAutomaticFailover() {
        return this.globalPartitionEndpointManagerForPerPartitionAutomaticFailover;
    }

    @Override
    public AddressSelector getAddressSelector() {
        return new AddressSelector(this.addressResolver, this.configs.getProtocol());
    }

    public Flux<DatabaseAccount> getDatabaseAccountFromEndpoint(URI endpoint) {
        return Flux.defer(() -> {
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this,
                OperationType.Read, ResourceType.DatabaseAccount, "", null, (Object) null);
            // if thin client enabled, populate thin client header so we can get thin client read and writeable locations
            if (useThinClient) {
                request.getHeaders().put(HttpConstants.HttpHeaders.THINCLIENT_OPT_IN, "true");
            }
            return this.populateHeadersAsync(request, RequestVerb.GET)
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
        if (request.useGatewayMode) {
            return this.gatewayProxy;
        }

        if (useThinClientStoreModel(request)) {
            return this.thinProxy;
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
            activeClientsCnt.decrementAndGet();
            logger.info("Shutting down ...");

            if (this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker != null) {
                logger.info("Closing globalPartitionEndpointManagerForPerPartitionCircuitBreaker...");
                LifeCycleUtils.closeQuietly(this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker);
            }

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
    public synchronized void enableThroughputControlGroup(ThroughputControlGroupInternal group, Mono<Integer> throughputQueryMono) {
        checkNotNull(group, "Throughput control group can not be null");

        if (this.throughputControlEnabled.compareAndSet(false, true)) {
            this.throughputControlStore =
                new ThroughputControlStore(
                    this.collectionCache,
                    this.connectionPolicy.getConnectionMode(),
                    this.partitionKeyRangeCache);

            if (ConnectionMode.DIRECT == this.connectionPolicy.getConnectionMode()) {
                this.storeModel.enableThroughputControl(throughputControlStore);
            } else {
                this.gatewayProxy.enableThroughputControl(throughputControlStore);
            }
        }

        this.throughputControlStore.enableThroughputControlGroup(group, throughputQueryMono);
    }

    @Override
    public Flux<Void> submitOpenConnectionTasksAndInitCaches(CosmosContainerProactiveInitConfig proactiveContainerInitConfig) {
        return this.storeModel.submitOpenConnectionTasksAndInitCaches(proactiveContainerInitConfig);
    }

    @Override
    public ConsistencyLevel getDefaultConsistencyLevelOfAccount() {
        return this.gatewayConfigurationReader.getDefaultConsistencyLevel();
    }

    /***
     * Configure fault injector provider.
     *
     * @param injectorProvider the fault injector provider.
     */
    @Override
    public void configureFaultInjectorProvider(IFaultInjectorProvider injectorProvider) {
        checkNotNull(injectorProvider, "Argument 'injectorProvider' can not be null");

        if (this.connectionPolicy.getConnectionMode() == ConnectionMode.DIRECT) {
            this.storeModel.configureFaultInjectorProvider(injectorProvider, this.configs);
            this.addressResolver.configureFaultInjectorProvider(injectorProvider, this.configs);
        }

        this.gatewayProxy.configureFaultInjectorProvider(injectorProvider, this.configs);
    }

    @Override
    public void recordOpenConnectionsAndInitCachesCompleted(List<CosmosContainerIdentity> cosmosContainerIdentities) {
        this.storeModel.recordOpenConnectionsAndInitCachesCompleted(cosmosContainerIdentities);
    }

    @Override
    public void recordOpenConnectionsAndInitCachesStarted(List<CosmosContainerIdentity> cosmosContainerIdentities) {
        this.storeModel.recordOpenConnectionsAndInitCachesStarted(cosmosContainerIdentities);
    }

    @Override
    public String getMasterKeyOrResourceToken() {
        return this.masterKeyOrResourceToken;
    }

    private static SqlQuerySpec createLogicalPartitionScanQuerySpec(
        PartitionKey partitionKey,
        List<String> partitionKeySelectors) {

        StringBuilder queryStringBuilder = new StringBuilder();
        List<SqlParameter> parameters = new ArrayList<>();

        queryStringBuilder.append("SELECT * FROM c WHERE");
        Object[] pkValues = ModelBridgeInternal.getPartitionKeyInternal(partitionKey).toObjectArray();
        String pkParamNamePrefix = "@pkValue";
        for (int i = 0; i < pkValues.length; i++) {
            StringBuilder subQueryStringBuilder = new StringBuilder();
            String sqlParameterName = pkParamNamePrefix + i;

            if (i > 0) {
                subQueryStringBuilder.append(" AND ");
            }
            subQueryStringBuilder.append(" c");
            subQueryStringBuilder.append(partitionKeySelectors.get(i));
            subQueryStringBuilder.append((" = "));
            subQueryStringBuilder.append(sqlParameterName);

            parameters.add(new SqlParameter(sqlParameterName, pkValues[i]));
            queryStringBuilder.append(subQueryStringBuilder);
        }

        return new SqlQuerySpec(queryStringBuilder.toString(), parameters);
    }

    @Override
    public Mono<List<FeedRange>> getFeedRanges(String collectionLink, boolean forceRefresh) {
        StaleResourceRetryPolicy staleResourceRetryPolicy = new StaleResourceRetryPolicy(
            this.collectionCache,
            null,
            collectionLink,
            new HashMap<>(),
            new HashMap<>(),
            this.sessionContainer,
            null);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            this,
            OperationType.Query,
            ResourceType.Document,
            collectionLink,
            null);

        staleResourceRetryPolicy.onBeforeSendRequest(request);

        return ObservableHelper.inlineIfPossibleAsObs(
            () -> getFeedRangesInternal(request, collectionLink, forceRefresh, staleResourceRetryPolicy),
            staleResourceRetryPolicy);
    }

    private Mono<List<FeedRange>> getFeedRangesInternal(
        RxDocumentServiceRequest request,
        String collectionLink,
        boolean forceRefresh,
        DocumentClientRetryPolicy retryPolicy) {

        logger.debug("getFeedRange collectionLink=[{}] - forceRefresh={}", collectionLink, forceRefresh);

        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }

        if (request != null) {
            retryPolicy.onBeforeSendRequest(request);
        }

        Mono<Utils.ValueHolder<DocumentCollection>> collectionObs = collectionCache.resolveCollectionAsync(null,
            request);

        return collectionObs.flatMap(documentCollectionResourceResponse -> {
            final DocumentCollection collection = documentCollectionResourceResponse.v;
            if (collection == null) {
                return Mono.error(new IllegalStateException("Collection cannot be null"));
            }

            Mono<Utils.ValueHolder<List<PartitionKeyRange>>> valueHolderMono = partitionKeyRangeCache
                .tryGetOverlappingRangesAsync(
                    BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics),
                    collection.getResourceId(),
                    RANGE_INCLUDING_ALL_PARTITION_KEY_RANGES,
                    forceRefresh,
                    null);

            return valueHolderMono.map(partitionKeyRangeList -> toFeedRanges(partitionKeyRangeList, request));
        });
    }

    private static List<FeedRange> toFeedRanges(
        Utils.ValueHolder<List<PartitionKeyRange>> partitionKeyRangeListValueHolder, RxDocumentServiceRequest request) {
        final List<PartitionKeyRange> partitionKeyRangeList = partitionKeyRangeListValueHolder.v;
        if (partitionKeyRangeList == null) {
            request.forceNameCacheRefresh = true;
            throw new InvalidPartitionException();
        }

        List<FeedRange> feedRanges = new ArrayList<>();
        partitionKeyRangeList.forEach(pkRange -> feedRanges.add(toFeedRange(pkRange)));

        return feedRanges;
    }

    private static FeedRange toFeedRange(PartitionKeyRange pkRange) {
        return new FeedRangeEpkImpl(pkRange.toRange());
    }

    public PartitionKeyRange addPartitionLevelUnavailableRegionsForPointOperationRequestForPerPartitionCircuitBreaker(
        RxDocumentServiceRequest request,
        RequestOptions options,
        CollectionRoutingMap collectionRoutingMap,
        DocumentClientRetryPolicy documentClientRetryPolicy,
        PartitionKeyRange preResolvedPartitionKeyRangeIfAny) {

        checkNotNull(request, "Argument 'request' cannot be null!");
        checkNotNull(this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker, "globalPartitionEndpointManagerForCircuitBreaker cannot be null!");

        if (this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker.isPerPartitionLevelCircuitBreakingApplicable(request)) {

            if (preResolvedPartitionKeyRangeIfAny != null) {

                // setting it here in case request.requestContext.resolvedPartitionKeyRange
                // is not assigned in either GlobalAddressResolver / RxGatewayStoreModel (possible if there are Gateway timeouts)
                // and circuit breaker also kicks in to mark a failure resolvedPartitionKeyRange (will result in NullPointerException and will
                // help failover as well)
                // also resolvedPartitionKeyRange will be overridden in GlobalAddressResolver / RxGatewayStoreModel irrespective
                // so staleness is not an issue (after doing a validation of parent-child relationship b/w initial and new partitionKeyRange)
                request.requestContext.resolvedPartitionKeyRange = preResolvedPartitionKeyRangeIfAny;

                // maintaining a separate copy - request.requestContext.resolvedPartitionKeyRange can be set to null
                // when the CosmosClient instance has to "reset" the request.requestContext.resolvedPartitionKeyRange
                // in partition split / merge and invalid partition scenarios - the separate copy will help identify
                // such scenarios and circuit breaking in general
                request.requestContext.resolvedPartitionKeyRangeForCircuitBreaker = preResolvedPartitionKeyRangeIfAny;

                List<String> unavailableRegionsForPartition
                    = this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker.getUnavailableRegionsForPartitionKeyRange(
                    request,
                    request.getResourceId(),
                    preResolvedPartitionKeyRangeIfAny
                );

                request.requestContext.setUnavailableRegionsForPerPartitionCircuitBreaker(unavailableRegionsForPartition);

                // onBeforeSendRequest uses excluded regions to know the next location endpoint
                // to route the request to unavailable regions are effectively excluded regions for this request
                if (documentClientRetryPolicy != null) {
                    documentClientRetryPolicy.onBeforeSendRequest(request);
                }

                return preResolvedPartitionKeyRangeIfAny;
            }
            else {
                checkNotNull(options, "Argument 'options' cannot be null!");
                checkNotNull(options.getPartitionKeyDefinition(), "Argument 'partitionKeyDefinition' within options cannot be null!");
                checkNotNull(collectionRoutingMap, "Argument 'collectionRoutingMap' cannot be null!");

                PartitionKeyRange resolvedPartitionKeyRange = null;

                PartitionKeyDefinition partitionKeyDefinition = options.getPartitionKeyDefinition();
                PartitionKeyInternal partitionKeyInternal = request.getPartitionKeyInternal();

                if (partitionKeyInternal != null) {
                    String effectivePartitionKeyString = PartitionKeyInternalHelper.getEffectivePartitionKeyString(partitionKeyInternal, partitionKeyDefinition);
                    resolvedPartitionKeyRange = collectionRoutingMap.getRangeByEffectivePartitionKey(effectivePartitionKeyString);

                    // cache the effective partition key if possible - can be a bottleneck,
                    // since it is also recomputed in AddressResolver
                    request.setEffectivePartitionKey(effectivePartitionKeyString);
                } else if (request.getPartitionKeyRangeIdentity() != null) {
                    resolvedPartitionKeyRange = collectionRoutingMap.getRangeByPartitionKeyRangeId(request.getPartitionKeyRangeIdentity().getPartitionKeyRangeId());
                }

                checkNotNull(resolvedPartitionKeyRange, "resolvedPartitionKeyRange cannot be null!");
                checkNotNull(this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker, "globalPartitionEndpointManagerForCircuitBreaker cannot be null!");

                // setting it here in case request.requestContext.resolvedPartitionKeyRange
                // is not assigned in either GlobalAddressResolver / RxGatewayStoreModel (possible if there are Gateway timeouts)
                // and circuit breaker also kicks in to mark a failure resolvedPartitionKeyRange (will result in NullPointerException and will
                // help failover as well)
                // also resolvedPartitionKeyRange will be overridden in GlobalAddressResolver / RxGatewayStoreModel irrespective
                // so staleness is not an issue (after doing a validation of parent-child relationship b/w initial and new partitionKeyRange)
                request.requestContext.resolvedPartitionKeyRange = resolvedPartitionKeyRange;

                // maintaining a separate copy - request.requestContext.resolvedPartitionKeyRange can be set to null
                // when the CosmosClient instance has to "reset" the request.requestContext.resolvedPartitionKeyRange
                // in partition split / merge and invalid partition scenarios - the separate copy will help identify
                // such scenarios and circuit breaking in general
                request.requestContext.resolvedPartitionKeyRangeForCircuitBreaker = resolvedPartitionKeyRange;

                List<String> unavailableRegionsForPartition
                    = this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker.getUnavailableRegionsForPartitionKeyRange(
                    request,
                    request.getResourceId(),
                    resolvedPartitionKeyRange
                );

                request.requestContext.setUnavailableRegionsForPerPartitionCircuitBreaker(unavailableRegionsForPartition);

                // onBeforeSendRequest uses excluded regions to know the next location endpoint
                // to route the request to unavailable regions are effectively excluded regions for this request
                if (documentClientRetryPolicy != null) {
                    documentClientRetryPolicy.onBeforeSendRequest(request);
                }

                return resolvedPartitionKeyRange;
            }
        }

        return null;
    }

    public PartitionKeyRange setPartitionKeyRangeForPointOperationRequestForPerPartitionAutomaticFailover(
        RxDocumentServiceRequest request,
        RequestOptions options,
        CollectionRoutingMap collectionRoutingMap,
        DocumentClientRetryPolicy documentClientRetryPolicy,
        boolean isWriteRequest,
        PartitionKeyRange preResolvedPartitionKeyRangeIfAny) {

        checkNotNull(request, "Argument 'request' cannot be null!");

        if (this.globalPartitionEndpointManagerForPerPartitionAutomaticFailover.isPerPartitionAutomaticFailoverApplicable(request)) {

            if (preResolvedPartitionKeyRangeIfAny != null) {

                checkNotNull(this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker, "globalPartitionEndpointManagerForCircuitBreaker cannot be null!");

                // setting it here in case request.requestContext.resolvedPartitionKeyRange
                // is not assigned in either GlobalAddressResolver / RxGatewayStoreModel (possible if there are Gateway timeouts)
                // and circuit breaker also kicks in to mark a failure resolvedPartitionKeyRange (will result in NullPointerException and will
                // help failover as well)
                // also resolvedPartitionKeyRange will be overridden in GlobalAddressResolver / RxGatewayStoreModel irrespective
                // so staleness is not an issue (after doing a validation of parent-child relationship b/w initial and new partitionKeyRange)
                request.requestContext.resolvedPartitionKeyRange = preResolvedPartitionKeyRangeIfAny;

                // maintaining a separate copy - request.requestContext.resolvedPartitionKeyRange can be set to null
                // when the CosmosClient instance has to "reset" the request.requestContext.resolvedPartitionKeyRange
                // in partition split / merge and invalid partition scenarios - the separate copy will help identify
                // such scenarios and PPAF in general
                request.requestContext.resolvedPartitionKeyRangeForPerPartitionAutomaticFailover = preResolvedPartitionKeyRangeIfAny;

                request.isPerPartitionAutomaticFailoverEnabledAndWriteRequest
                    = isWriteRequest && this.globalPartitionEndpointManagerForPerPartitionAutomaticFailover.isPerPartitionAutomaticFailoverEnabled();

                // onBeforeSendRequest uses excluded regions to know the next location endpoint
                // to route the request to unavailable regions are effectively excluded regions for this request
                if (documentClientRetryPolicy != null) {
                    documentClientRetryPolicy.onBeforeSendRequest(request);
                }

                return preResolvedPartitionKeyRangeIfAny;
            } else {
                checkNotNull(options, "Argument 'options' cannot be null!");
                checkNotNull(options.getPartitionKeyDefinition(), "Argument 'partitionKeyDefinition' within options cannot be null!");
                checkNotNull(collectionRoutingMap, "Argument 'collectionRoutingMap' cannot be null!");

                PartitionKeyRange resolvedPartitionKeyRange = null;

                PartitionKeyDefinition partitionKeyDefinition = options.getPartitionKeyDefinition();
                PartitionKeyInternal partitionKeyInternal = request.getPartitionKeyInternal();

                if (partitionKeyInternal != null) {
                    String effectivePartitionKeyString = PartitionKeyInternalHelper.getEffectivePartitionKeyString(partitionKeyInternal, partitionKeyDefinition);
                    resolvedPartitionKeyRange = collectionRoutingMap.getRangeByEffectivePartitionKey(effectivePartitionKeyString);

                    // cache the effective partition key if possible - can be a bottleneck,
                    // since it is also recomputed in AddressResolver
                    request.setEffectivePartitionKey(effectivePartitionKeyString);
                } else if (request.getPartitionKeyRangeIdentity() != null) {
                    resolvedPartitionKeyRange = collectionRoutingMap.getRangeByPartitionKeyRangeId(request.getPartitionKeyRangeIdentity().getPartitionKeyRangeId());
                }

                checkNotNull(resolvedPartitionKeyRange, "resolvedPartitionKeyRange cannot be null!");
                checkNotNull(this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker, "globalPartitionEndpointManagerForCircuitBreaker cannot be null!");

                // setting it here in case request.requestContext.resolvedPartitionKeyRange
                // is not assigned in either GlobalAddressResolver / RxGatewayStoreModel (possible if there are Gateway timeouts)
                // and circuit breaker also kicks in to mark a failure resolvedPartitionKeyRange (will result in NullPointerException and will
                // help failover as well)
                // also resolvedPartitionKeyRange will be overridden in GlobalAddressResolver / RxGatewayStoreModel irrespective
                // so staleness is not an issue (after doing a validation of parent-child relationship b/w initial and new partitionKeyRange)
                request.requestContext.resolvedPartitionKeyRange = resolvedPartitionKeyRange;

                // maintaining a separate copy - request.requestContext.resolvedPartitionKeyRange can be set to null
                // when the CosmosClient instance has to "reset" the request.requestContext.resolvedPartitionKeyRange
                // in partition split / merge and invalid partition scenarios - the separate copy will help identify
                // such scenarios and PPAF in general
                request.requestContext.resolvedPartitionKeyRangeForPerPartitionAutomaticFailover = resolvedPartitionKeyRange;

                request.isPerPartitionAutomaticFailoverEnabledAndWriteRequest
                    = isWriteRequest && this.globalPartitionEndpointManagerForPerPartitionAutomaticFailover.isPerPartitionAutomaticFailoverEnabled();

                // onBeforeSendRequest uses excluded regions to know the next location endpoint
                // to route the request to unavailable regions are effectively excluded regions for this request
                if (documentClientRetryPolicy != null) {
                    documentClientRetryPolicy.onBeforeSendRequest(request);
                }

                return resolvedPartitionKeyRange;
            }
        }

        return null;
    }

    public void mergeContextInformationIntoDiagnosticsForPointRequest(
        RxDocumentServiceRequest request,
        CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContextForRequest) {

        if (crossRegionAvailabilityContextForRequest != null) {
            PointOperationContextForCircuitBreaker pointOperationContextForCircuitBreaker
                = crossRegionAvailabilityContextForRequest.getPointOperationContextForCircuitBreaker();

            if (pointOperationContextForCircuitBreaker != null) {
                diagnosticsAccessor.mergeSerializationDiagnosticContext(request.requestContext.cosmosDiagnostics, pointOperationContextForCircuitBreaker.getSerializationDiagnosticsContext());
            }
        }
    }

    public PartitionKeyRange addPartitionLevelUnavailableRegionsForFeedRequestForPerPartitionCircuitBreaker(
        RxDocumentServiceRequest request,
        CosmosQueryRequestOptions options,
        CollectionRoutingMap collectionRoutingMap,
        PartitionKeyRange preResolvedPartitionKeyRangeIfAny) {

        if (this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker.isPerPartitionLevelCircuitBreakingApplicable(request)) {

            checkNotNull(collectionRoutingMap, "collectionRoutingMap cannot be null!");

            if (preResolvedPartitionKeyRangeIfAny != null) {
                // setting it here in case request.requestContext.resolvedPartitionKeyRange
                // is not assigned in either GlobalAddressResolver / RxGatewayStoreModel (possible if there are Gateway timeouts)
                // and circuit breaker also kicks in to mark a failure resolvedPartitionKeyRange (will result in NullPointerException and will
                // help failover as well)
                // also resolvedPartitionKeyRange will be overridden in GlobalAddressResolver / RxGatewayStoreModel irrespective
                // so staleness is not an issue (after doing a validation of parent-child relationship b/w initial and new partitionKeyRange)
                request.requestContext.resolvedPartitionKeyRange = preResolvedPartitionKeyRangeIfAny;

                // maintaining a separate copy - request.requestContext.resolvedPartitionKeyRange can be set to null
                // when the GoneAndRetryWithRetryPolicy has to "reset" the request.requestContext.resolvedPartitionKeyRange
                // in partition split / merge and invalid partition scenarios - the separate copy will help identify
                // such scenarios and circuit breaking in general
                request.requestContext.resolvedPartitionKeyRangeForCircuitBreaker = preResolvedPartitionKeyRangeIfAny;

                checkNotNull(this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker, "globalPartitionEndpointManagerForCircuitBreaker cannot be null!");

                List<String> unavailableRegionsForPartition
                    = this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker.getUnavailableRegionsForPartitionKeyRange(
                    request,
                    request.getResourceId(),
                    preResolvedPartitionKeyRangeIfAny
                );

                request.requestContext.setUnavailableRegionsForPerPartitionCircuitBreaker(unavailableRegionsForPartition);

                return preResolvedPartitionKeyRangeIfAny;

            } else {

                PartitionKeyRange resolvedPartitionKeyRange = null;

                if (request.getPartitionKeyRangeIdentity() != null) {
                    resolvedPartitionKeyRange = collectionRoutingMap.getRangeByPartitionKeyRangeId(request.getPartitionKeyRangeIdentity().getPartitionKeyRangeId());
                } else if (request.getPartitionKeyInternal() != null) {
                    String effectivePartitionKeyString = PartitionKeyInternalHelper.getEffectivePartitionKeyString(request.getPartitionKeyInternal(), ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.getCosmosQueryRequestOptionsAccessor().getPartitionKeyDefinition(options));
                    resolvedPartitionKeyRange = collectionRoutingMap.getRangeByEffectivePartitionKey(effectivePartitionKeyString);
                }

                checkNotNull(resolvedPartitionKeyRange, "resolvedPartitionKeyRange cannot be null!");

                // setting it here in case request.requestContext.resolvedPartitionKeyRange
                // is not assigned in either GlobalAddressResolver / RxGatewayStoreModel (possible if there are Gateway timeouts)
                // and circuit breaker also kicks in to mark a failure resolvedPartitionKeyRange (will result in NullPointerException and will
                // help failover as well)
                // also resolvedPartitionKeyRange will be overridden in GlobalAddressResolver / RxGatewayStoreModel irrespective
                // so staleness is not an issue (after doing a validation of parent-child relationship b/w initial and new partitionKeyRange)
                request.requestContext.resolvedPartitionKeyRange = resolvedPartitionKeyRange;

                // maintaining a separate copy - request.requestContext.resolvedPartitionKeyRange can be set to null
                // when the GoneAndRetryWithRetryPolicy has to "reset" the request.requestContext.resolvedPartitionKeyRange
                // in partition split / merge and invalid partition scenarios - the separate copy will help identify
                // such scenarios and circuit breaking in general
                request.requestContext.resolvedPartitionKeyRangeForCircuitBreaker = resolvedPartitionKeyRange;

                checkNotNull(this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker, "globalPartitionEndpointManagerForCircuitBreaker cannot be null!");

                List<String> unavailableRegionsForPartition
                    = this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker.getUnavailableRegionsForPartitionKeyRange(
                    request,
                    request.getResourceId(),
                    resolvedPartitionKeyRange
                );

                request.requestContext.setUnavailableRegionsForPerPartitionCircuitBreaker(unavailableRegionsForPartition);

                return resolvedPartitionKeyRange;
            }
        }

        return null;
    }

    public PartitionKeyRange setPartitionKeyRangeForFeedRequestForPerPartitionAutomaticFailover(
        RxDocumentServiceRequest request,
        CosmosQueryRequestOptions options,
        CollectionRoutingMap collectionRoutingMap,
        PartitionKeyRange preResolvedPartitionKeyRangeIfAny) {

        if (this.globalPartitionEndpointManagerForPerPartitionAutomaticFailover.isPerPartitionAutomaticFailoverApplicable(request)) {
            checkNotNull(collectionRoutingMap, "collectionRoutingMap cannot be null!");

            if (preResolvedPartitionKeyRangeIfAny != null) {

                request.requestContext.resolvedPartitionKeyRange = preResolvedPartitionKeyRangeIfAny;
                request.requestContext.resolvedPartitionKeyRangeForPerPartitionAutomaticFailover = preResolvedPartitionKeyRangeIfAny;

                return preResolvedPartitionKeyRangeIfAny;
            } else {
                PartitionKeyRange resolvedPartitionKeyRange = null;

                if (request.getPartitionKeyRangeIdentity() != null) {
                    resolvedPartitionKeyRange = collectionRoutingMap.getRangeByPartitionKeyRangeId(request.getPartitionKeyRangeIdentity().getPartitionKeyRangeId());
                } else if (request.getPartitionKeyInternal() != null) {
                    String effectivePartitionKeyString = PartitionKeyInternalHelper.getEffectivePartitionKeyString(request.getPartitionKeyInternal(), ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.getCosmosQueryRequestOptionsAccessor().getPartitionKeyDefinition(options));
                    resolvedPartitionKeyRange = collectionRoutingMap.getRangeByEffectivePartitionKey(effectivePartitionKeyString);
                }

                checkNotNull(resolvedPartitionKeyRange, "resolvedPartitionKeyRange cannot be null!");

                // setting it here in case request.requestContext.resolvedPartitionKeyRange
                // is not assigned in either GlobalAddressResolver / RxGatewayStoreModel (possible if there are Gateway timeouts)
                // and circuit breaker also kicks in to mark a failure resolvedPartitionKeyRange (will result in NullPointerException and will
                // help failover as well)
                // also resolvedPartitionKeyRange will be overridden in GlobalAddressResolver / RxGatewayStoreModel irrespective
                // so staleness is not an issue (after doing a validation of parent-child relationship b/w initial and new partitionKeyRange)
                request.requestContext.resolvedPartitionKeyRange = resolvedPartitionKeyRange;

                // maintaining a separate copy - request.requestContext.resolvedPartitionKeyRange can be set to null
                // when the GoneAndRetryWithRetryPolicy has to "reset" the request.requestContext.resolvedPartitionKeyRange
                // in partition split / merge and invalid partition scenarios - the separate copy will help identify
                // such scenarios and circuit breaking in general
                request.requestContext.resolvedPartitionKeyRangeForPerPartitionAutomaticFailover = resolvedPartitionKeyRange;

                return resolvedPartitionKeyRange;
            }
        }

        return null;
    }

    public void addPartitionLevelUnavailableRegionsForChangeFeedOperationRequestForPerPartitionCircuitBreaker(
        RxDocumentServiceRequest request,
        CosmosChangeFeedRequestOptions options,
        CollectionRoutingMap collectionRoutingMap,
        PartitionKeyRange preResolvedPartitionKeyRangeIfAny) {

        if (this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker.isPerPartitionLevelCircuitBreakingApplicable(request)) {

            if (preResolvedPartitionKeyRangeIfAny != null) {

                // setting it here in case request.requestContext.resolvedPartitionKeyRange
                // is not assigned in either GlobalAddressResolver / RxGatewayStoreModel (possible if there are Gateway timeouts)
                // and circuit breaker also kicks in to mark a failure resolvedPartitionKeyRange (will result in NullPointerException and will
                // help failover as well)
                // also resolvedPartitionKeyRange will be overridden in GlobalAddressResolver / RxGatewayStoreModel irrespective
                // so staleness is not an issue (after doing a validation of parent-child relationship b/w initial and new partitionKeyRange)
                request.requestContext.resolvedPartitionKeyRange = preResolvedPartitionKeyRangeIfAny;

                // maintaining a separate copy - request.requestContext.resolvedPartitionKeyRange can be set to null
                // when the GoneAndRetryWithRetryPolicy has to "reset" the request.requestContext.resolvedPartitionKeyRange
                // in partition split / merge and invalid partition scenarios - the separate copy will help identify
                // such scenarios and circuit breaking in general
                request.requestContext.resolvedPartitionKeyRangeForCircuitBreaker = preResolvedPartitionKeyRangeIfAny;

                List<String> unavailableRegionsForPartition
                    = this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker.getUnavailableRegionsForPartitionKeyRange(
                    request,
                    request.getResourceId(),
                    preResolvedPartitionKeyRangeIfAny
                );

                request.requestContext.setUnavailableRegionsForPerPartitionCircuitBreaker(unavailableRegionsForPartition);
            } else {
                checkNotNull(collectionRoutingMap, "collectionRoutingMap cannot be null!");

                PartitionKeyRange resolvedPartitionKeyRange = null;

                if (request.getPartitionKeyRangeIdentity() != null) {
                    resolvedPartitionKeyRange = collectionRoutingMap.getRangeByPartitionKeyRangeId(request.getPartitionKeyRangeIdentity().getPartitionKeyRangeId());
                } else if (request.getPartitionKeyInternal() != null) {
                    String effectivePartitionKeyString = PartitionKeyInternalHelper.getEffectivePartitionKeyString(request.getPartitionKeyInternal(), ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper.getCosmosChangeFeedRequestOptionsAccessor().getPartitionKeyDefinition(options));
                    resolvedPartitionKeyRange = collectionRoutingMap.getRangeByEffectivePartitionKey(effectivePartitionKeyString);
                }

                checkNotNull(resolvedPartitionKeyRange, "resolvedPartitionKeyRange cannot be null!");

                // setting it here in case request.requestContext.resolvedPartitionKeyRange
                // is not assigned in either GlobalAddressResolver / RxGatewayStoreModel (possible if there are Gateway timeouts)
                // and circuit breaker also kicks in to mark a failure resolvedPartitionKeyRange (will result in NullPointerException and will
                // help failover as well)
                // also resolvedPartitionKeyRange will be overridden in GlobalAddressResolver / RxGatewayStoreModel irrespective
                // so staleness is not an issue (after doing a validation of parent-child relationship b/w initial and new partitionKeyRange)
                request.requestContext.resolvedPartitionKeyRange = resolvedPartitionKeyRange;

                // maintaining a separate copy - request.requestContext.resolvedPartitionKeyRange can be set to null
                // when the GoneAndRetryWithRetryPolicy has to "reset" the request.requestContext.resolvedPartitionKeyRange
                // in partition split / merge and invalid partition scenarios - the separate copy will help identify
                // such scenarios and circuit breaking in general
                request.requestContext.resolvedPartitionKeyRangeForCircuitBreaker = resolvedPartitionKeyRange;

                List<String> unavailableRegionsForPartition
                    = this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker.getUnavailableRegionsForPartitionKeyRange(
                    request,
                    request.getResourceId(),
                    resolvedPartitionKeyRange
                );

                request.requestContext.setUnavailableRegionsForPerPartitionCircuitBreaker(unavailableRegionsForPartition);
            }
        }
    }

    public PartitionKeyRange setPartitionKeyRangeForChangeFeedOperationRequestForPerPartitionAutomaticFailover(
        RxDocumentServiceRequest request,
        CosmosChangeFeedRequestOptions options,
        CollectionRoutingMap collectionRoutingMap,
        PartitionKeyRange preResolvedPartitionKeyRangeIfAny) {

        if (this.globalPartitionEndpointManagerForPerPartitionAutomaticFailover.isPerPartitionAutomaticFailoverApplicable(request)) {

            if (preResolvedPartitionKeyRangeIfAny != null) {

                // setting it here in case request.requestContext.resolvedPartitionKeyRange
                // is not assigned in either GlobalAddressResolver / RxGatewayStoreModel (possible if there are Gateway timeouts)
                // and circuit breaker also kicks in to mark a failure resolvedPartitionKeyRange (will result in NullPointerException and will
                // help failover as well)
                // also resolvedPartitionKeyRange will be overridden in GlobalAddressResolver / RxGatewayStoreModel irrespective
                // so staleness is not an issue (after doing a validation of parent-child relationship b/w initial and new partitionKeyRange)
                request.requestContext.resolvedPartitionKeyRange = preResolvedPartitionKeyRangeIfAny;

                // maintaining a separate copy - request.requestContext.resolvedPartitionKeyRange can be set to null
                // when the GoneAndRetryWithRetryPolicy has to "reset" the request.requestContext.resolvedPartitionKeyRange
                // in partition split / merge and invalid partition scenarios - the separate copy will help identify
                // such scenarios and PPAF in general
                request.requestContext.resolvedPartitionKeyRangeForPerPartitionAutomaticFailover = preResolvedPartitionKeyRangeIfAny;

                return preResolvedPartitionKeyRangeIfAny;
            } else {
                checkNotNull(collectionRoutingMap, "collectionRoutingMap cannot be null!");

                PartitionKeyRange resolvedPartitionKeyRange = null;

                if (request.getPartitionKeyRangeIdentity() != null) {
                    resolvedPartitionKeyRange = collectionRoutingMap.getRangeByPartitionKeyRangeId(request.getPartitionKeyRangeIdentity().getPartitionKeyRangeId());
                } else if (request.getPartitionKeyInternal() != null) {
                    String effectivePartitionKeyString = PartitionKeyInternalHelper.getEffectivePartitionKeyString(request.getPartitionKeyInternal(), ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper.getCosmosChangeFeedRequestOptionsAccessor().getPartitionKeyDefinition(options));
                    resolvedPartitionKeyRange = collectionRoutingMap.getRangeByEffectivePartitionKey(effectivePartitionKeyString);
                }

                checkNotNull(resolvedPartitionKeyRange, "resolvedPartitionKeyRange cannot be null!");

                // setting it here in case request.requestContext.resolvedPartitionKeyRange
                // is not assigned in either GlobalAddressResolver / RxGatewayStoreModel (possible if there are Gateway timeouts)
                // and circuit breaker also kicks in to mark a failure resolvedPartitionKeyRange (will result in NullPointerException and will
                // help failover as well)
                // also resolvedPartitionKeyRange will be overridden in GlobalAddressResolver / RxGatewayStoreModel irrespective
                // so staleness is not an issue (after doing a validation of parent-child relationship b/w initial and new partitionKeyRange)
                request.requestContext.resolvedPartitionKeyRange = resolvedPartitionKeyRange;

                // maintaining a separate copy - request.requestContext.resolvedPartitionKeyRange can be set to null
                // when the GoneAndRetryWithRetryPolicy has to "reset" the request.requestContext.resolvedPartitionKeyRange
                // in partition split / merge and invalid partition scenarios - the separate copy will help identify
                // such scenarios and PPAF in general
                request.requestContext.resolvedPartitionKeyRangeForPerPartitionAutomaticFailover = resolvedPartitionKeyRange;

                return resolvedPartitionKeyRange;
            }
        }

        return null;
    }

    private Mono<ResourceResponse<Document>> wrapPointOperationWithAvailabilityStrategy(
        ResourceType resourceType,
        OperationType operationType,
        DocumentPointOperation callback,
        RequestOptions initialRequestOptions,
        boolean idempotentWriteRetriesEnabled,
        String collectionLink) {

        return wrapPointOperationWithAvailabilityStrategy(
            resourceType,
            operationType,
            callback,
            initialRequestOptions,
            idempotentWriteRetriesEnabled,
            this,
            collectionLink
        );
    }

    private Mono<ResourceResponse<Document>> wrapPointOperationWithAvailabilityStrategy(
        ResourceType resourceType,
        OperationType operationType,
        DocumentPointOperation callback,
        RequestOptions initialRequestOptions,
        boolean idempotentWriteRetriesEnabled,
        DiagnosticsClientContext innerDiagnosticsFactory,
        String collectionLink) {

        checkNotNull(resourceType, "Argument 'resourceType' must not be null.");
        checkNotNull(operationType, "Argument 'operationType' must not be null.");
        checkNotNull(callback, "Argument 'callback' must not be null.");

        final RequestOptions nonNullRequestOptions =
            initialRequestOptions != null ? initialRequestOptions : new RequestOptions();

        checkArgument(
            resourceType == ResourceType.Document,
            "This method can only be used for document point operations.");

        CosmosEndToEndOperationLatencyPolicyConfig endToEndPolicyConfig =
            getEndToEndOperationLatencyPolicyConfig(nonNullRequestOptions, resourceType, operationType);

        List<String> orderedApplicableRegionsForSpeculation = getApplicableRegionsForSpeculation(
            endToEndPolicyConfig,
            resourceType,
            operationType,
            idempotentWriteRetriesEnabled,
            nonNullRequestOptions);

        AtomicBoolean isOperationSuccessful = new AtomicBoolean(false);

        if (orderedApplicableRegionsForSpeculation.size() < 2) {
            // There is at most one applicable region - no hedging possible
            PointOperationContextForCircuitBreaker pointOperationContextForCircuitBreakerForMainRequest = new PointOperationContextForCircuitBreaker(
                isOperationSuccessful,
                false,
                collectionLink,
                new SerializationDiagnosticsContext());

            pointOperationContextForCircuitBreakerForMainRequest.setIsRequestHedged(false);

            AvailabilityStrategyContext availabilityStrategyContextForMainRequest = new AvailabilityStrategyContext(false, false);

            CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContextForMainRequest
                = new CrossRegionAvailabilityContextForRxDocumentServiceRequest(
                null,
                pointOperationContextForCircuitBreakerForMainRequest,
                availabilityStrategyContextForMainRequest);

            return callback.apply(nonNullRequestOptions, endToEndPolicyConfig, innerDiagnosticsFactory, crossRegionAvailabilityContextForMainRequest);
        }

        ThresholdBasedAvailabilityStrategy availabilityStrategy =
            (ThresholdBasedAvailabilityStrategy) endToEndPolicyConfig.getAvailabilityStrategy();
        List<Mono<NonTransientPointOperationResult>> monoList = new ArrayList<>();

        final ScopedDiagnosticsFactory diagnosticsFactory = new ScopedDiagnosticsFactory(innerDiagnosticsFactory, false);

        orderedApplicableRegionsForSpeculation
            .forEach(region -> {
                RequestOptions clonedOptions = new RequestOptions(nonNullRequestOptions);

                if (monoList.isEmpty()) {
                    // no special error handling for transient errors to suppress them here
                    // because any cross-regional retries are expected to be processed
                    // by the ClientRetryPolicy for the initial request - so, any outcome of the
                    // initial Mono should be treated as non-transient error - even when
                    // the error would otherwise be treated as transient
                    PointOperationContextForCircuitBreaker pointOperationContextForCircuitBreakerForMainRequest
                        = new PointOperationContextForCircuitBreaker(
                        isOperationSuccessful,
                        true,
                        collectionLink,
                        new SerializationDiagnosticsContext());

                    pointOperationContextForCircuitBreakerForMainRequest.setIsRequestHedged(false);

                    AvailabilityStrategyContext availabilityStrategyContextForMainRequest = new AvailabilityStrategyContext(true, false);

                    CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContextForMainRequest
                        = new CrossRegionAvailabilityContextForRxDocumentServiceRequest(
                        null,
                        pointOperationContextForCircuitBreakerForMainRequest,
                        availabilityStrategyContextForMainRequest);

                    Mono<NonTransientPointOperationResult> initialMonoAcrossAllRegions =
                        callback.apply(clonedOptions, endToEndPolicyConfig, diagnosticsFactory, crossRegionAvailabilityContextForMainRequest)
                            .map(NonTransientPointOperationResult::new)
                            .onErrorResume(
                                RxDocumentClientImpl::isCosmosException,
                                t -> Mono.just(
                                    new NonTransientPointOperationResult(
                                        Utils.as(Exceptions.unwrap(t), CosmosException.class))));

                    if (logger.isDebugEnabled()) {
                        monoList.add(initialMonoAcrossAllRegions.doOnSubscribe(c -> logger.debug(
                            "STARTING to process {} operation in region '{}'",
                            operationType,
                            region)));
                    } else {
                        monoList.add(initialMonoAcrossAllRegions);
                    }
                } else {
                    clonedOptions.setExcludedRegions(
                        getEffectiveExcludedRegionsForHedging(
                            nonNullRequestOptions.getExcludedRegions(),
                            orderedApplicableRegionsForSpeculation,
                            region)
                    );

                    // Non-Transient errors are mapped to a value - this ensures the firstWithValue
                    // operator below will complete the composite Mono for both successful values
                    // and non-transient errors
                    PointOperationContextForCircuitBreaker pointOperationContextForCircuitBreakerForHedgedRequest
                        = new PointOperationContextForCircuitBreaker(
                        isOperationSuccessful,
                        true,
                        collectionLink,
                        new SerializationDiagnosticsContext());

                    pointOperationContextForCircuitBreakerForHedgedRequest.setIsRequestHedged(true);

                    AvailabilityStrategyContext availabilityStrategyContextForHedgedRequest = new AvailabilityStrategyContext(true, true);

                    CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContextForHedgedRequest = new CrossRegionAvailabilityContextForRxDocumentServiceRequest(
                        null,
                        pointOperationContextForCircuitBreakerForHedgedRequest,
                        availabilityStrategyContextForHedgedRequest);

                    Mono<NonTransientPointOperationResult> regionalCrossRegionRetryMono =
                        callback.apply(clonedOptions, endToEndPolicyConfig, diagnosticsFactory, crossRegionAvailabilityContextForHedgedRequest)
                            .map(NonTransientPointOperationResult::new)
                            .onErrorResume(
                                RxDocumentClientImpl::isNonTransientCosmosException,
                                t -> Mono.just(
                                    new NonTransientPointOperationResult(
                                        Utils.as(Exceptions.unwrap(t), CosmosException.class))));

                    Duration delayForCrossRegionalRetry = (availabilityStrategy)
                        .getThreshold()
                        .plus((availabilityStrategy)
                            .getThresholdStep()
                            .multipliedBy(monoList.size() - 1));

                    if (logger.isDebugEnabled()) {
                        monoList.add(
                            regionalCrossRegionRetryMono
                                .doOnSubscribe(c -> logger.debug("STARTING to process {} operation in region '{}'", operationType, region))
                                .delaySubscription(delayForCrossRegionalRetry));
                    } else {
                        monoList.add(
                            regionalCrossRegionRetryMono
                                .delaySubscription(delayForCrossRegionalRetry));
                    }
                }
            });

        // NOTE - merging diagnosticsFactory cannot only happen in
        // doFinally operator because the doFinally operator is a side effect method -
        // meaning it executes concurrently with firing the onComplete/onError signal
        // doFinally is also triggered by cancellation
        // So, to make sure merging the Context happens synchronously in line we
        // have to ensure merging is happening on error/completion
        // and also in doOnCancel.
        return Mono
            .firstWithValue(monoList)
            .flatMap(nonTransientResult -> {
                diagnosticsFactory.merge(nonNullRequestOptions);
                if (nonTransientResult.isError()) {
                    return Mono.error(nonTransientResult.exception);
                }

                return Mono.just(nonTransientResult.response);
            })
            .onErrorMap(throwable -> {
                Throwable exception = Exceptions.unwrap(throwable);

                if (exception instanceof NoSuchElementException) {

                    List<Throwable> innerThrowables = Exceptions
                        .unwrapMultiple(exception.getCause());

                    int index = 0;
                    for (Throwable innerThrowable : innerThrowables) {
                        Throwable innerException = Exceptions.unwrap(innerThrowable);

                        // collect latest CosmosException instance bubbling up for a region
                        if (innerException instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(innerException, CosmosException.class);
                            diagnosticsFactory.merge(nonNullRequestOptions);
                            return cosmosException;
                        } else if (innerException instanceof NoSuchElementException) {
                            logger.trace(
                                "Operation in {} completed with empty result because it was cancelled.",
                                orderedApplicableRegionsForSpeculation.get(index));
                        } else if (logger.isWarnEnabled()) {
                            String message = "Unexpected Non-CosmosException when processing operation in '"
                                + orderedApplicableRegionsForSpeculation.get(index)
                                + "'.";
                            logger.warn(
                                message,
                                innerException
                            );
                        }

                        index++;
                    }
                }

                diagnosticsFactory.merge(nonNullRequestOptions);

                return exception;
            })
            .doOnCancel(() -> diagnosticsFactory.merge(nonNullRequestOptions));
    }

    private static boolean isCosmosException(Throwable t) {
        final Throwable unwrappedException = Exceptions.unwrap(t);
        return unwrappedException instanceof CosmosException;
    }

    private static boolean isNonTransientCosmosException(Throwable t) {
        final Throwable unwrappedException = Exceptions.unwrap(t);
        if (!(unwrappedException instanceof CosmosException)) {
            return false;
        }
        CosmosException cosmosException = Utils.as(unwrappedException, CosmosException.class);
        return isNonTransientResultForHedging(
            cosmosException.getStatusCode(),
            cosmosException.getSubStatusCode());
    }

    private List<String> getEffectiveExcludedRegionsForHedging(
        List<String> initialExcludedRegions,
        List<String> applicableRegions,
        String currentRegion) {

        // For hedging operations execution should only happen in the targeted region - no cross-regional
        // fail-overs should happen
        List<String> effectiveExcludedRegions = new ArrayList<>();
        if (initialExcludedRegions != null) {
            effectiveExcludedRegions.addAll(initialExcludedRegions);
        }

        for (String applicableRegion: applicableRegions) {
            if (!applicableRegion.equals(currentRegion)) {
                effectiveExcludedRegions.add(applicableRegion);
            }
        }

        return effectiveExcludedRegions;
    }

    private static boolean isNonTransientResultForHedging(int statusCode, int subStatusCode) {
        // All 1xx, 2xx and 3xx status codes should be treated as final result
        if (statusCode < HttpConstants.StatusCodes.BADREQUEST) {
            return true;
        }

        // Treat OperationCancelledException as non-transient timeout
        if (statusCode == HttpConstants.StatusCodes.REQUEST_TIMEOUT &&
            subStatusCode == HttpConstants.SubStatusCodes.CLIENT_OPERATION_TIMEOUT) {
            return true;
        }

        // Status codes below indicate non-transient errors
        if (statusCode == HttpConstants.StatusCodes.BADREQUEST
            || statusCode == HttpConstants.StatusCodes.CONFLICT
            || statusCode == HttpConstants.StatusCodes.METHOD_NOT_ALLOWED
            || statusCode == HttpConstants.StatusCodes.PRECONDITION_FAILED
            || statusCode == HttpConstants.StatusCodes.REQUEST_ENTITY_TOO_LARGE
            || statusCode == HttpConstants.StatusCodes.UNAUTHORIZED) {

            return true;
        }

        // 404 - NotFound is also a final result - it means document was not yet available
        // after enforcing whatever the consistency model is
        if (statusCode == HttpConstants.StatusCodes.NOTFOUND
            && subStatusCode == HttpConstants.SubStatusCodes.UNKNOWN) {

            return true;
        }

        // All other errors should be treated as possibly transient
        return false;
    }

    private CosmosEndToEndOperationLatencyPolicyConfig evaluatePpafEnforcedE2eLatencyPolicyCfgForReads(
        GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover globalPartitionEndpointManagerForPerPartitionAutomaticFailover,
        ConnectionPolicy connectionPolicy) {

        if (!globalPartitionEndpointManagerForPerPartitionAutomaticFailover.isPerPartitionAutomaticFailoverEnabled()) {
            return null;
        }

        if (Configs.isReadAvailabilityStrategyEnabledWithPpaf()) {

            logger.warn("Availability strategy for reads, queries, read all and read many" +
                " is enabled when PerPartitionAutomaticFailover is enabled.");

            if (connectionPolicy.getConnectionMode() == ConnectionMode.DIRECT) {
                Duration networkRequestTimeout = connectionPolicy.getTcpNetworkRequestTimeout();

                checkNotNull(networkRequestTimeout, "Argument 'networkRequestTimeout' cannot be null!");

                Duration overallE2eLatencyTimeout = networkRequestTimeout.plus(Utils.ONE_SECOND);
                Duration threshold = Utils.min(networkRequestTimeout.dividedBy(2), Utils.ONE_SECOND);
                Duration thresholdStep = Utils.min(threshold.dividedBy(2), Utils.HALF_SECOND);

                return new CosmosEndToEndOperationLatencyPolicyConfigBuilder(overallE2eLatencyTimeout)
                .availabilityStrategy(new ThresholdBasedAvailabilityStrategy(threshold, thresholdStep))
                .build();
            } else {

                Duration httpNetworkRequestTimeout = connectionPolicy.getHttpNetworkRequestTimeout();

                checkNotNull(httpNetworkRequestTimeout, "Argument 'httpNetworkRequestTimeout' cannot be null!");

                // 6s was chosen to accommodate for control-plane hot path read timeout retries (like QueryPlan / PartitionKeyRange)
                Duration overallE2eLatencyTimeout = Utils.min(Utils.SIX_SECONDS, httpNetworkRequestTimeout);

                Duration threshold = Utils.min(overallE2eLatencyTimeout.dividedBy(2), Utils.ONE_SECOND);
                Duration thresholdStep = Utils.min(threshold.dividedBy(2), Utils.HALF_SECOND);

                return new CosmosEndToEndOperationLatencyPolicyConfigBuilder(overallE2eLatencyTimeout)
                .availabilityStrategy(new ThresholdBasedAvailabilityStrategy(threshold, thresholdStep))
                .build();
            }
        }

        return null;
    }

    private DiagnosticsClientContext getEffectiveClientContext(DiagnosticsClientContext clientContextOverride) {
        if (clientContextOverride != null) {
            return clientContextOverride;
        }

        return this;
    }

    /**
     * Returns the applicable endpoints ordered by preference list if any
     * @param operationType - the operationT
     * @return the applicable endpoints ordered by preference list if any
     */
    private List<RegionalRoutingContext> getApplicableEndPoints(OperationType operationType, List<String> excludedRegions) {
        if (operationType.isReadOnlyOperation()) {
            return withoutNulls(this.globalEndpointManager.getApplicableReadRegionalRoutingContexts(excludedRegions));
        } else if (operationType.isWriteOperation()) {
            return withoutNulls(this.globalEndpointManager.getApplicableWriteRegionalRoutingContexts(excludedRegions));
        }

        return EMPTY_ENDPOINT_LIST;
    }

    private static List<RegionalRoutingContext> withoutNulls(List<RegionalRoutingContext> orderedEffectiveEndpointsList) {
        if (orderedEffectiveEndpointsList == null) {
            return EMPTY_ENDPOINT_LIST;
        }

        int i = 0;
        while (i < orderedEffectiveEndpointsList.size()) {
            if (orderedEffectiveEndpointsList.get(i) == null) {
                orderedEffectiveEndpointsList.remove(i);
            } else {
                i++;
            }
        }

        return orderedEffectiveEndpointsList;
    }

    private List<String> getApplicableRegionsForSpeculation(
        CosmosEndToEndOperationLatencyPolicyConfig endToEndPolicyConfig,
        ResourceType resourceType,
        OperationType operationType,
        boolean isIdempotentWriteRetriesEnabled,
        RequestOptions options) {

        return getApplicableRegionsForSpeculation(
            endToEndPolicyConfig,
            resourceType,
            operationType,
            isIdempotentWriteRetriesEnabled,
            options.getExcludedRegions());
    }

    private List<String> getApplicableRegionsForSpeculation(
        CosmosEndToEndOperationLatencyPolicyConfig endToEndPolicyConfig,
        ResourceType resourceType,
        OperationType operationType,
        boolean isIdempotentWriteRetriesEnabled,
        List<String> excludedRegions) {

        if (endToEndPolicyConfig == null || !endToEndPolicyConfig.isEnabled()) {
            return EMPTY_REGION_LIST;
        }

        if (resourceType != ResourceType.Document) {
            return EMPTY_REGION_LIST;
        }

        if (operationType.isWriteOperation() && !isIdempotentWriteRetriesEnabled) {
            return EMPTY_REGION_LIST;
        }

        if (operationType.isWriteOperation() && !this.globalEndpointManager.canUseMultipleWriteLocations()) {
            return EMPTY_REGION_LIST;
        }

        if (!(endToEndPolicyConfig.getAvailabilityStrategy() instanceof ThresholdBasedAvailabilityStrategy)) {
            return EMPTY_REGION_LIST;
        }

        List<RegionalRoutingContext> regionalRoutingContextList = getApplicableEndPoints(operationType, excludedRegions);

        HashSet<String> normalizedExcludedRegions = new HashSet<>();
        if (excludedRegions != null) {
            excludedRegions.forEach(r -> normalizedExcludedRegions.add(r.toLowerCase(Locale.ROOT)));
        }

        List<String> orderedRegionsForSpeculation = new ArrayList<>();
        regionalRoutingContextList.forEach(consolidatedLocationEndpoints -> {
            String regionName = this.globalEndpointManager.getRegionName(consolidatedLocationEndpoints.getGatewayRegionalEndpoint(), operationType);
            if (!normalizedExcludedRegions.contains(regionName.toLowerCase(Locale.ROOT))) {
                orderedRegionsForSpeculation.add(regionName);
            }
        });

        return orderedRegionsForSpeculation;
    }

    private <T> Mono<T> executeFeedOperationWithAvailabilityStrategy(
        final ResourceType resourceType,
        final OperationType operationType,
        final Supplier<DocumentClientRetryPolicy> retryPolicyFactory,
        final RxDocumentServiceRequest req,
        final BiFunction<Supplier<DocumentClientRetryPolicy>, RxDocumentServiceRequest, Mono<T>> feedOperation,
        final String collectionLink) {

        checkNotNull(retryPolicyFactory, "Argument 'retryPolicyFactory' must not be null.");
        checkNotNull(req, "Argument 'req' must not be null.");
        assert(resourceType == ResourceType.Document);

        CosmosEndToEndOperationLatencyPolicyConfig endToEndPolicyConfig =
            this.getEffectiveEndToEndOperationLatencyPolicyConfig(
                req.requestContext.getEndToEndOperationLatencyPolicyConfig(), resourceType, operationType);

        req.requestContext.setEndToEndOperationLatencyPolicyConfig(endToEndPolicyConfig);

        List<String> initialExcludedRegions = req.requestContext.getExcludeRegions();
        List<String> orderedApplicableRegionsForSpeculation = this.getApplicableRegionsForSpeculation(
            endToEndPolicyConfig,
            resourceType,
            operationType,
            false,
            initialExcludedRegions);

        Map<PartitionKeyRangeWrapper, PartitionKeyRangeWrapper> partitionKeyRangesWithSuccess = new ConcurrentHashMap<>();

        if (orderedApplicableRegionsForSpeculation.size() < 2) {
            FeedOperationContextForCircuitBreaker feedOperationContextForCircuitBreakerForRequestOutsideOfAvailabilityStrategyFlow
                = new FeedOperationContextForCircuitBreaker(
                partitionKeyRangesWithSuccess,
                false,
                collectionLink);

            feedOperationContextForCircuitBreakerForRequestOutsideOfAvailabilityStrategyFlow.setIsRequestHedged(false);

            AvailabilityStrategyContext availabilityStrategyContext = new AvailabilityStrategyContext(false, false);

            CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContextForRequest
                = new CrossRegionAvailabilityContextForRxDocumentServiceRequest(
                    feedOperationContextForCircuitBreakerForRequestOutsideOfAvailabilityStrategyFlow,
                null,
                availabilityStrategyContext);

            req.requestContext.setCrossRegionAvailabilityContext(crossRegionAvailabilityContextForRequest);

            // There is at most one applicable region - no hedging possible
            return feedOperation.apply(retryPolicyFactory, req);
        }

        FeedOperationContextForCircuitBreaker feedOperationContextForCircuitBreakerForParentRequestInAvailabilityStrategyFlow
            = new FeedOperationContextForCircuitBreaker(
            partitionKeyRangesWithSuccess,
            true,
            collectionLink);

        feedOperationContextForCircuitBreakerForParentRequestInAvailabilityStrategyFlow.setIsRequestHedged(false);

        AvailabilityStrategyContext availabilityStrategyContext = new AvailabilityStrategyContext(true, false);

        CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContextForRequest = new CrossRegionAvailabilityContextForRxDocumentServiceRequest(
            feedOperationContextForCircuitBreakerForParentRequestInAvailabilityStrategyFlow,
            null,
            availabilityStrategyContext);

        req.requestContext.setCrossRegionAvailabilityContext(crossRegionAvailabilityContextForRequest);

        ThresholdBasedAvailabilityStrategy availabilityStrategy =
            (ThresholdBasedAvailabilityStrategy)endToEndPolicyConfig.getAvailabilityStrategy();
        List<Mono<NonTransientFeedOperationResult<T>>> monoList = new ArrayList<>();

        orderedApplicableRegionsForSpeculation
            .forEach(region -> {
                RxDocumentServiceRequest clonedRequest = req.clone();

                if (monoList.isEmpty()) {
                    // no special error handling for transient errors to suppress them here
                    // because any cross-regional retries are expected to be processed
                    // by the ClientRetryPolicy for the initial request - so, any outcome of the
                    // initial Mono should be treated as non-transient error - even when
                    // the error would otherwise be treated as transient
                    FeedOperationContextForCircuitBreaker feedOperationContextForCircuitBreakerForNonHedgedRequest
                        = new FeedOperationContextForCircuitBreaker(
                        partitionKeyRangesWithSuccess,
                        true,
                        collectionLink);

                    feedOperationContextForCircuitBreakerForNonHedgedRequest.setIsRequestHedged(false);

                    AvailabilityStrategyContext availabilityStrategyContextForNonHedgedRequest = new AvailabilityStrategyContext(true, false);

                    CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContextForRequestForNonHedgedRequest = new CrossRegionAvailabilityContextForRxDocumentServiceRequest(
                        feedOperationContextForCircuitBreakerForNonHedgedRequest,
                        null,
                        availabilityStrategyContextForNonHedgedRequest);

                    clonedRequest.requestContext.setCrossRegionAvailabilityContext(crossRegionAvailabilityContextForRequestForNonHedgedRequest);

                    Mono<NonTransientFeedOperationResult<T>> initialMonoAcrossAllRegions =
                        handleCircuitBreakingFeedbackForFeedOperationWithAvailabilityStrategy(feedOperation.apply(retryPolicyFactory, clonedRequest)
                            .map(NonTransientFeedOperationResult::new)
                            .onErrorResume(
                                RxDocumentClientImpl::isCosmosException,
                                t -> Mono.just(
                                    new NonTransientFeedOperationResult<>(
                                        Utils.as(Exceptions.unwrap(t), CosmosException.class)))), clonedRequest);

                    if (logger.isDebugEnabled()) {
                        monoList.add(initialMonoAcrossAllRegions.doOnSubscribe(c -> logger.debug(
                            "STARTING to process {} operation in region '{}'",
                            operationType,
                            region)));
                    } else {
                        monoList.add(initialMonoAcrossAllRegions);
                    }
                } else {
                    clonedRequest.requestContext.setExcludeRegions(
                        getEffectiveExcludedRegionsForHedging(
                            initialExcludedRegions,
                            orderedApplicableRegionsForSpeculation,
                            region)
                    );

                    FeedOperationContextForCircuitBreaker feedOperationContextForCircuitBreakerForHedgedRequest
                        = new FeedOperationContextForCircuitBreaker(
                        partitionKeyRangesWithSuccess,
                        true,
                        collectionLink);

                    feedOperationContextForCircuitBreakerForHedgedRequest.setIsRequestHedged(true);

                    AvailabilityStrategyContext availabilityStrategyContextForHedgedRequest = new AvailabilityStrategyContext(true, true);

                    CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContextForRequestForHedgedRequest
                        = new CrossRegionAvailabilityContextForRxDocumentServiceRequest(
                            feedOperationContextForCircuitBreakerForHedgedRequest,
                        null,
                        availabilityStrategyContextForHedgedRequest
                    );

                    clonedRequest.requestContext.setCrossRegionAvailabilityContext(crossRegionAvailabilityContextForRequestForHedgedRequest);

                    clonedRequest.requestContext.setKeywordIdentifiers(req.requestContext.getKeywordIdentifiers());

                    // Non-Transient errors are mapped to a value - this ensures the firstWithValue
                    // operator below will complete the composite Mono for both successful values
                    // and non-transient errors
                    Mono<NonTransientFeedOperationResult<T>> regionalCrossRegionRetryMono =
                        handleCircuitBreakingFeedbackForFeedOperationWithAvailabilityStrategy(feedOperation.apply(retryPolicyFactory, clonedRequest)
                            .map(NonTransientFeedOperationResult::new)
                            .onErrorResume(
                                RxDocumentClientImpl::isNonTransientCosmosException,
                                t -> Mono.just(
                                    new NonTransientFeedOperationResult<>(
                                        Utils.as(Exceptions.unwrap(t), CosmosException.class)))), clonedRequest);

                    Duration delayForCrossRegionalRetry = (availabilityStrategy)
                        .getThreshold()
                        .plus((availabilityStrategy)
                            .getThresholdStep()
                            .multipliedBy(monoList.size() - 1));

                    if (logger.isDebugEnabled()) {
                        monoList.add(
                            regionalCrossRegionRetryMono
                                .doOnSubscribe(c -> logger.debug("STARTING to process {} operation in region '{}'", operationType, region))
                                .delaySubscription(delayForCrossRegionalRetry));
                    } else {
                        monoList.add(
                            regionalCrossRegionRetryMono
                                .delaySubscription(delayForCrossRegionalRetry));
                    }
                }
            });

        // NOTE - merging diagnosticsFactory cannot only happen in
        // doFinally operator because the doFinally operator is a side effect method -
        // meaning it executes concurrently with firing the onComplete/onError signal
        // doFinally is also triggered by cancellation
        // So, to make sure merging the Context happens synchronously in line we
        // have to ensure merging is happening on error/completion
        // and also in doOnCancel.
        return Mono
            .firstWithValue(monoList)
            .flatMap(nonTransientResult -> {
                if (nonTransientResult.isError()) {
                    return Mono.error(nonTransientResult.exception);
                }

                return Mono.just(nonTransientResult.response);
            })
            .onErrorMap(throwable -> {
                Throwable exception = Exceptions.unwrap(throwable);

                if (exception instanceof NoSuchElementException) {

                    List<Throwable> innerThrowables = Exceptions
                        .unwrapMultiple(exception.getCause());

                    int index = 0;
                    for (Throwable innerThrowable : innerThrowables) {
                        Throwable innerException = Exceptions.unwrap(innerThrowable);

                        // collect latest CosmosException instance bubbling up for a region
                        if (innerException instanceof CosmosException) {
                            return Utils.as(innerException, CosmosException.class);
                        } else if (innerException instanceof NoSuchElementException) {
                            logger.trace(
                                "Operation in {} completed with empty result because it was cancelled.",
                                orderedApplicableRegionsForSpeculation.get(index));
                        } else if (logger.isWarnEnabled()) {
                            String message = "Unexpected Non-CosmosException when processing operation in '"
                                + orderedApplicableRegionsForSpeculation.get(index)
                                + "'.";
                            logger.warn(
                                message,
                                innerException
                            );
                        }

                        index++;
                    }
                }

                return exception;
            });
    }

    private void handleLocationCancellationExceptionForPartitionKeyRange(RxDocumentServiceRequest failedRequest) {

        RegionalRoutingContext firstContactedLocationEndpoint = diagnosticsAccessor
            .getFirstContactedLocationEndpoint(failedRequest.requestContext.cosmosDiagnostics);

        if (firstContactedLocationEndpoint != null) {
            this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker
                .handleLocationExceptionForPartitionKeyRange(failedRequest, firstContactedLocationEndpoint);
        } else {
            this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker
                .handleLocationExceptionForPartitionKeyRange(failedRequest, failedRequest.requestContext.regionalRoutingContextToRoute);
        }
    }

    private void addCancelledGatewayModeDiagnosticsIntoCosmosException(CosmosException cosmosException, RxDocumentServiceRequest request) {
        if (request == null) {
            return;
        }

        if (request.requestContext == null) {
            return;
        }

        if (request.requestContext.cosmosDiagnostics == null) {
            return;
        }

        if (cosmosException == null) {
            return;
        }

        if (!(cosmosException instanceof OperationCancelledException)) {
            return;
        }

        List<GatewayRequestTimelineContext> cancelledGatewayRequestTimelineContexts
            = request.requestContext.cancelledGatewayRequestTimelineContexts;

        for (GatewayRequestTimelineContext cancelledGatewayRequestTimelineContext : cancelledGatewayRequestTimelineContexts) {

            RequestTimeline requestTimeline = cancelledGatewayRequestTimelineContext.getRequestTimeline();
            long transportRequestId = cancelledGatewayRequestTimelineContext.getTransportRequestId();

            BridgeInternal.setRequestTimeline(cosmosException, requestTimeline);

            ImplementationBridgeHelpers
                .CosmosExceptionHelper
                .getCosmosExceptionAccessor()
                .setFaultInjectionRuleId(
                    cosmosException,
                    request.faultInjectionRequestContext
                        .getFaultInjectionRuleId(transportRequestId));

            ImplementationBridgeHelpers
                .CosmosExceptionHelper
                .getCosmosExceptionAccessor()
                .setFaultInjectionEvaluationResults(
                    cosmosException,
                    request.faultInjectionRequestContext
                        .getFaultInjectionRuleEvaluationResults(transportRequestId));

            BridgeInternal.recordGatewayResponse(request.requestContext.cosmosDiagnostics, request, cosmosException, globalEndpointManager);
        }
    }

    // this is a one time call, so we can afford to synchronize as the benefit is now all PPAF and PPCB related dependencies are visible
    // if initializePerPartitionFailover has been invoked prior
    private synchronized void initializePerPartitionFailover(DatabaseAccount databaseAccountSnapshot) {
        initializePerPartitionAutomaticFailover(databaseAccountSnapshot);
        initializePerPartitionCircuitBreaker();
        enableAvailabilityStrategyForReads();

        checkNotNull(this.globalPartitionEndpointManagerForPerPartitionAutomaticFailover, "Argument 'globalPartitionEndpointManagerForPerPartitionAutomaticFailover' cannot be null.");
        checkNotNull(this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker, "Argument 'globalPartitionEndpointManagerForPerPartitionCircuitBreaker' cannot be null.");

        this.diagnosticsClientConfig.withPartitionLevelCircuitBreakerConfig(this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker.getCircuitBreakerConfig());
    }

    private void initializePerPartitionAutomaticFailover(DatabaseAccount databaseAccountSnapshot) {

        Boolean isPerPartitionAutomaticFailoverEnabledAsMandatedByService
            = databaseAccountSnapshot.isPerPartitionFailoverBehaviorEnabled();

        if (isPerPartitionAutomaticFailoverEnabledAsMandatedByService != null) {
            this.globalPartitionEndpointManagerForPerPartitionAutomaticFailover.resetPerPartitionAutomaticFailoverEnabled(isPerPartitionAutomaticFailoverEnabledAsMandatedByService);
        } else {
            boolean isPerPartitionAutomaticFailoverOptedIntoByClient
                = Configs.isPerPartitionAutomaticFailoverEnabled().equalsIgnoreCase("true");
            this.globalPartitionEndpointManagerForPerPartitionAutomaticFailover.resetPerPartitionAutomaticFailoverEnabled(isPerPartitionAutomaticFailoverOptedIntoByClient);
        }
    }

    private void initializePerPartitionCircuitBreaker() {
        if (this.globalPartitionEndpointManagerForPerPartitionAutomaticFailover.isPerPartitionAutomaticFailoverEnabled()) {

            PartitionLevelCircuitBreakerConfig partitionLevelCircuitBreakerConfig = Configs.getPartitionLevelCircuitBreakerConfig();

            if (partitionLevelCircuitBreakerConfig != null && !partitionLevelCircuitBreakerConfig.isPartitionLevelCircuitBreakerEnabled()) {
                logger.warn("Per-Partition Circuit Breaker is enabled by default when Per-Partition Automatic Failover is enabled.");
                System.setProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG", "{\"isPartitionLevelCircuitBreakerEnabled\": true}");
            }
        }

        this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker.resetCircuitBreakerConfig();
        this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker.init();
    }

    private void enableAvailabilityStrategyForReads() {
        if (this.globalPartitionEndpointManagerForPerPartitionAutomaticFailover.isPerPartitionAutomaticFailoverEnabled()) {
            this.ppafEnforcedE2ELatencyPolicyConfigForReads = this.evaluatePpafEnforcedE2eLatencyPolicyCfgForReads(
                this.globalPartitionEndpointManagerForPerPartitionAutomaticFailover,
                this.connectionPolicy
            );
        }
    }

    public boolean useThinClient() {
        return useThinClient;
    }

    private boolean useThinClientStoreModel(RxDocumentServiceRequest request) {
        if (!useThinClient
            || !this.globalEndpointManager.hasThinClientReadLocations()
            || request.getResourceType() != ResourceType.Document) {

            return false;
        }

        OperationType operationType = request.getOperationType();

        return operationType.isPointOperation()
                    || operationType == OperationType.Query
                    || operationType == OperationType.Batch;
    }

    private DocumentClientRetryPolicy getRetryPolicyForPointOperation(
        DiagnosticsClientContext diagnosticsClientContext,
        RequestOptions requestOptions,
        String collectionLink) {

        checkNotNull(requestOptions, "Argument 'requestOptions' can not be null");

        DocumentClientRetryPolicy requestRetryPolicy = this.resetSessionTokenRetryPolicy.getRequestPolicy(diagnosticsClientContext);
        if (requestOptions.getPartitionKey() == null) {
            requestRetryPolicy = new PartitionKeyMismatchRetryPolicy(collectionCache, requestRetryPolicy, collectionLink, requestOptions);
        }

        requestRetryPolicy = new StaleResourceRetryPolicy(
            this.collectionCache,
            requestRetryPolicy,
            collectionLink,
            requestOptions.getProperties(),
            requestOptions.getHeaders(),
            this.sessionContainer,
            diagnosticsClientContext);

        return requestRetryPolicy;
    }

    @FunctionalInterface
    private interface DocumentPointOperation {
        Mono<ResourceResponse<Document>> apply(
            RequestOptions requestOptions,
            CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig,
            DiagnosticsClientContext clientContextOverride,
            CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContext);
    }

    public boolean isClosed() {
        return this.closed.get();
    }

    private static class NonTransientPointOperationResult {
        private final ResourceResponse<Document> response;
        private final CosmosException exception;

        public NonTransientPointOperationResult(CosmosException exception) {
            checkNotNull(exception, "Argument 'exception' must not be null.");
            this.exception = exception;
            this.response = null;
        }

        public NonTransientPointOperationResult(ResourceResponse<Document> response) {
            checkNotNull(response, "Argument 'response' must not be null.");
            this.exception = null;
            this.response = response;
        }

        public boolean isError() {
            return this.exception != null;
        }

        public CosmosException getException() {
            return this.exception;
        }

        public ResourceResponse<Document> getResponse() {
            return this.response;
        }
    }

    private static class NonTransientFeedOperationResult<T> {
        private final T response;
        private final CosmosException exception;

        public NonTransientFeedOperationResult(CosmosException exception) {
            checkNotNull(exception, "Argument 'exception' must not be null.");
            this.exception = exception;
            this.response = null;
        }

        public NonTransientFeedOperationResult(T response) {
            checkNotNull(response, "Argument 'response' must not be null.");
            this.exception = null;
            this.response = response;
        }

        public boolean isError() {
            return this.exception != null;
        }

        public CosmosException getException() {
            return this.exception;
        }

        public T getResponse() {
            return this.response;
        }
    }

    private static class ScopedDiagnosticsFactory implements DiagnosticsClientContext {

        private final AtomicBoolean isMerged = new AtomicBoolean(false);
        private final DiagnosticsClientContext inner;
        private final ConcurrentLinkedQueue<CosmosDiagnostics> createdDiagnostics;
        private final boolean shouldCaptureAllFeedDiagnostics;
        private final AtomicReference<CosmosDiagnostics> mostRecentlyCreatedDiagnostics = new AtomicReference<>(null);
        private final AtomicReference<Consumer<CosmosException>> gatewayCancelledDiagnosticsHandler
            = new AtomicReference<>(null);

        public ScopedDiagnosticsFactory(DiagnosticsClientContext inner, boolean shouldCaptureAllFeedDiagnostics) {
            checkNotNull(inner, "Argument 'inner' must not be null.");
            this.inner = inner;
            this.createdDiagnostics = new ConcurrentLinkedQueue<>();
            this.shouldCaptureAllFeedDiagnostics = shouldCaptureAllFeedDiagnostics;
        }

        @Override
        public DiagnosticsClientConfig getConfig() {
            return inner.getConfig();
        }

        @Override
        public CosmosDiagnostics createDiagnostics() {
            CosmosDiagnostics diagnostics = inner.createDiagnostics();
            createdDiagnostics.add(diagnostics);
            mostRecentlyCreatedDiagnostics.set(diagnostics);
            return diagnostics;
        }

        @Override
        public String getUserAgent() {
            return inner.getUserAgent();
        }

        @Override
        public CosmosDiagnostics getMostRecentlyCreatedDiagnostics() {
            return this.mostRecentlyCreatedDiagnostics.get();
        }

        public void merge(RequestOptions requestOptions) {
            CosmosDiagnosticsContext knownCtx = null;

            if (requestOptions != null) {
                CosmosDiagnosticsContext ctxSnapshot = requestOptions.getDiagnosticsContextSnapshot();
                if (ctxSnapshot != null) {
                    knownCtx = requestOptions.getDiagnosticsContextSnapshot();
                }
            }

            merge(knownCtx);
        }

        public void merge(CosmosDiagnosticsContext knownCtx) {
            if (!isMerged.compareAndSet(false, true)) {
                return;
            }

            CosmosDiagnosticsContext ctx = null;

            if (knownCtx != null) {
                ctx = knownCtx;
            } else {
                for (CosmosDiagnostics diagnostics : this.createdDiagnostics) {
                    if (diagnostics.getDiagnosticsContext() != null) {
                        ctx = diagnostics.getDiagnosticsContext();
                        break;
                    }
                }
            }

            if (ctx == null) {
                return;
            }

            for (CosmosDiagnostics diagnostics : this.createdDiagnostics) {
                if (diagnostics.getDiagnosticsContext() == null && diagnosticsAccessor.isNotEmpty(diagnostics)) {
                    if (this.shouldCaptureAllFeedDiagnostics &&
                        diagnosticsAccessor.getFeedResponseDiagnostics(diagnostics) != null) {

                        AtomicBoolean isCaptured = diagnosticsAccessor.isDiagnosticsCapturedInPagedFlux(diagnostics);
                        if (isCaptured != null) {
                            // Diagnostics captured in the ScopedDiagnosticsFactory should always be kept
                            isCaptured.set(true);
                        }
                    }
                    ctxAccessor.addDiagnostics(ctx, diagnostics);
                }
            }
        }

        public void reset() {
            this.createdDiagnostics.clear();
            this.isMerged.set(false);
        }

        public void setGwModeE2ETimeoutDiagnosticsHandler(Consumer<CosmosException> gwModeE2ETimeoutDiagnosticsHandler) {
            this.gatewayCancelledDiagnosticsHandler.set(gwModeE2ETimeoutDiagnosticsHandler);
        }

        public Consumer<CosmosException> getGwModeE2ETimeoutDiagnosticsHandler() {
            return this.gatewayCancelledDiagnosticsHandler.get();
        }
    }
}
