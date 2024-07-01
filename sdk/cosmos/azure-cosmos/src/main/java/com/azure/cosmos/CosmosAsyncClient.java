// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.Context;
import com.azure.cosmos.implementation.ApiType;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.DiagnosticsProvider;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.Permission;
import com.azure.cosmos.implementation.QueryFeedOperationState;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.WriteRetryPolicy;
import com.azure.cosmos.implementation.clienttelemetry.ClientMetricsDiagnosticsHandler;
import com.azure.cosmos.implementation.clienttelemetry.ClientTelemetry;
import com.azure.cosmos.implementation.clienttelemetry.ClientTelemetryDiagnosticsHandler;
import com.azure.cosmos.implementation.clienttelemetry.ClientTelemetryMetrics;
import com.azure.cosmos.implementation.clienttelemetry.CosmosMeterOptions;
import com.azure.cosmos.implementation.clienttelemetry.MetricCategory;
import com.azure.cosmos.implementation.clienttelemetry.TagName;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdMetrics;
import com.azure.cosmos.implementation.faultinjection.IFaultInjectorProvider;
import com.azure.cosmos.implementation.throughputControl.config.ThroughputControlGroupInternal;
import com.azure.cosmos.models.CosmosAuthorizationTokenResolver;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosContainerIdentity;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosDatabaseRequestOptions;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosMetricName;
import com.azure.cosmos.models.CosmosPermissionProperties;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.UtilBridgeInternal;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Provides a client-side logical representation of the Azure Cosmos DB service.
 * This asynchronous client is used to configure and execute requests against the service.
 * <p>
 * CosmosAsyncClient is thread-safe.
 * It's recommended to maintain a single instance of CosmosAsyncClient per lifetime of the application which enables efficient connection management and performance.
 * CosmosAsyncClient initialization is a heavy operation - don't use initialization CosmosAsyncClient instances as credentials or network connectivity validations.
 */
@ServiceClient(
    builder = CosmosClientBuilder.class,
    isAsync = true)
public final class CosmosAsyncClient implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(CosmosAsyncClient.class);

    private static final CosmosClientTelemetryConfig DEFAULT_TELEMETRY_CONFIG = new CosmosClientTelemetryConfig();
    private static final ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.CosmosQueryRequestOptionsAccessor queryOptionsAccessor =
        ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.getCosmosQueryRequestOptionsAccessor();
    private static final ImplementationBridgeHelpers.FeedResponseHelper.FeedResponseAccessor feedResponseAccessor =
        ImplementationBridgeHelpers.FeedResponseHelper.getFeedResponseAccessor();
    private static final ImplementationBridgeHelpers.CosmosClientTelemetryConfigHelper.CosmosClientTelemetryConfigAccessor
        telemetryConfigAccessor = ImplementationBridgeHelpers
        .CosmosClientTelemetryConfigHelper
        .getCosmosClientTelemetryConfigAccessor();

    private final AsyncDocumentClient asyncDocumentClient;
    private final String serviceEndpoint;
    private final ConnectionPolicy connectionPolicy;
    private final ConsistencyLevel desiredConsistencyLevel;
    private final AzureKeyCredential credential;
    private final CosmosClientTelemetryConfig clientTelemetryConfig;
    private final DiagnosticsProvider diagnosticsProvider;
    private final Tag clientCorrelationTag;
    private final String accountTagValue;
    private final boolean isSendClientTelemetryToServiceEnabled;
    private final MeterRegistry clientMetricRegistrySnapshot;
    private final CosmosContainerProactiveInitConfig proactiveContainerInitConfig;
    private static final ImplementationBridgeHelpers.CosmosContainerIdentityHelper.CosmosContainerIdentityAccessor containerIdentityAccessor =
            ImplementationBridgeHelpers.CosmosContainerIdentityHelper.getCosmosContainerIdentityAccessor();
    private final ConsistencyLevel accountConsistencyLevel;
    private final WriteRetryPolicy nonIdempotentWriteRetryPolicy;
    private final List<CosmosOperationPolicy> requestPolicies;
    private final CosmosItemSerializer defaultCustomSerializer;

    CosmosAsyncClient(CosmosClientBuilder builder) {
        // Async Cosmos client wrapper
        Configs configs = builder.configs();
        this.serviceEndpoint = builder.getEndpoint();
        String keyOrResourceToken = builder.getKey();
        this.connectionPolicy = builder.getConnectionPolicy();
        this.desiredConsistencyLevel = builder.getConsistencyLevel();
        List<CosmosPermissionProperties> permissions = builder.getPermissions();
        CosmosAuthorizationTokenResolver cosmosAuthorizationTokenResolver = builder.getAuthorizationTokenResolver();
        this.credential = builder.getCredential();
        TokenCredential tokenCredential = builder.getTokenCredential();
        boolean sessionCapturingOverride = builder.isSessionCapturingOverrideEnabled();
        boolean enableTransportClientSharing = builder.isConnectionSharingAcrossClientsEnabled();
        this.proactiveContainerInitConfig = builder.getProactiveContainerInitConfig();
        this.nonIdempotentWriteRetryPolicy = builder.getNonIdempotentWriteRetryPolicy();
        this.requestPolicies = builder.getOperationPolicies();
        this.defaultCustomSerializer = builder.getCustomItemSerializer();
        CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig = builder.getEndToEndOperationConfig();
        SessionRetryOptions sessionRetryOptions = builder.getSessionRetryOptions();

        CosmosClientTelemetryConfig effectiveTelemetryConfig = telemetryConfigAccessor
            .createSnapshot(
                builder.getClientTelemetryConfig(),
                builder.isClientTelemetryEnabled());

        this.clientTelemetryConfig = effectiveTelemetryConfig;
        this.isSendClientTelemetryToServiceEnabled = telemetryConfigAccessor
            .isSendClientTelemetryToServiceEnabled(effectiveTelemetryConfig);
        boolean contentResponseOnWriteEnabled = builder.isContentResponseOnWriteEnabled();
        ApiType apiType = builder.apiType();
        String clientCorrelationId = telemetryConfigAccessor
            .getClientCorrelationId(effectiveTelemetryConfig);

        List<Permission> permissionList = new ArrayList<>();
        if (permissions != null) {
            permissionList =
                permissions
                    .stream()
                    .map(ModelBridgeInternal::getPermission)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        this.asyncDocumentClient = new AsyncDocumentClient.Builder()
                                       .withOperationPolicies(this.requestPolicies)
                                       .withServiceEndpoint(this.serviceEndpoint)
                                       .withMasterKeyOrResourceToken(keyOrResourceToken)
                                       .withConnectionPolicy(this.connectionPolicy)
                                       .withConsistencyLevel(this.desiredConsistencyLevel)
                                       .withSessionCapturingOverride(sessionCapturingOverride)
                                       .withConfigs(configs)
                                       .withTokenResolver(cosmosAuthorizationTokenResolver)
                                       .withCredential(this.credential)
                                       .withTransportClientSharing(enableTransportClientSharing)
                                       .withContentResponseOnWriteEnabled(contentResponseOnWriteEnabled)
                                       .withTokenCredential(tokenCredential)
                                       .withState(builder.metadataCaches())
                                       .withPermissionFeed(permissionList)
                                       .withApiType(apiType)
                                       .withClientTelemetryConfig(this.clientTelemetryConfig)
                                       .withClientCorrelationId(clientCorrelationId)
                                       .withEndToEndOperationLatencyPolicyConfig(endToEndOperationLatencyPolicyConfig)
                                       .withSessionRetryOptions(sessionRetryOptions)
                                       .withContainerProactiveInitConfig(this.proactiveContainerInitConfig)
                                       .withDefaultSerializer(this.defaultCustomSerializer)
                                       .withRegionScopedSessionCapturingEnabled(builder.isRegionScopedSessionCapturingEnabled())
                                       .build();

        this.accountConsistencyLevel = this.asyncDocumentClient.getDefaultConsistencyLevelOfAccount();

        String effectiveClientCorrelationId = this.asyncDocumentClient.getClientCorrelationId();
        String machineId = this.asyncDocumentClient.getMachineId();
        if (!Strings.isNullOrWhiteSpace(machineId) && machineId.startsWith(ClientTelemetry.VM_ID_PREFIX)) {
            machineId = machineId.replace(ClientTelemetry.VM_ID_PREFIX, "vmId_");
            if (Strings.isNullOrWhiteSpace(effectiveClientCorrelationId)) {
                effectiveClientCorrelationId = machineId;
            } else {
                effectiveClientCorrelationId = String.format(
                    "%s_%s",
                    machineId,
                    effectiveClientCorrelationId);
            }
        }
        this.clientCorrelationTag = Tag.of(
            TagName.ClientCorrelationId.toString(),
            ClientTelemetryMetrics.escape(effectiveClientCorrelationId));

        this.clientMetricRegistrySnapshot = telemetryConfigAccessor
            .getClientMetricRegistry(effectiveTelemetryConfig);

        CosmosMeterOptions cpuMeterOptions = telemetryConfigAccessor
            .getMeterOptions(effectiveTelemetryConfig, CosmosMetricName.SYSTEM_CPU);
        CosmosMeterOptions memoryMeterOptions = telemetryConfigAccessor
            .getMeterOptions(effectiveTelemetryConfig, CosmosMetricName.SYSTEM_MEMORY_FREE);


        if (clientMetricRegistrySnapshot != null) {
            ClientTelemetryMetrics.add(clientMetricRegistrySnapshot, cpuMeterOptions, memoryMeterOptions);
        }
        this.accountTagValue = URI.create(this.serviceEndpoint).getHost().replace(
            ".documents.azure.com", ""
        );

        if (this.clientMetricRegistrySnapshot != null) {
            telemetryConfigAccessor.setClientCorrelationTag(
                effectiveTelemetryConfig,
                this.clientCorrelationTag );
            telemetryConfigAccessor.setAccountName(
                effectiveTelemetryConfig,
                this.accountTagValue
            );

            telemetryConfigAccessor.addDiagnosticsHandler(
                effectiveTelemetryConfig,
                new ClientMetricsDiagnosticsHandler(this)
            );
        }

        if (this.isSendClientTelemetryToServiceEnabled) {
            telemetryConfigAccessor.setClientTelemetry(
                effectiveTelemetryConfig,
                asyncDocumentClient.getClientTelemetry()
            );

            telemetryConfigAccessor.addDiagnosticsHandler(
                effectiveTelemetryConfig,
                new ClientTelemetryDiagnosticsHandler(effectiveTelemetryConfig)
            );
        }

        this.diagnosticsProvider = new DiagnosticsProvider(
            effectiveTelemetryConfig,
            effectiveClientCorrelationId,
            this.getUserAgent(),
            this.connectionPolicy.getConnectionMode());
    }

    AsyncDocumentClient getContextClient() {
        return this.asyncDocumentClient;
    }

    /**
     * Monitor Cosmos client performance and resource utilization using the specified meter registry.
     *
     * @param registry meter registry to use for performance monitoring.
     */
    static void setMonitorTelemetry(MeterRegistry registry) {
        RntbdMetrics.add(registry);
    }

    /**
     * Get the service endpoint.
     *
     * @return the service endpoint.
     */
    String getServiceEndpoint() {
        return serviceEndpoint;
    }

    /**
     * Get the connection policy.
     *
     * @return {@link ConnectionPolicy}.
     */
    ConnectionPolicy getConnectionPolicy() {
        return connectionPolicy;
    }

    AsyncDocumentClient getDocClientWrapper() {
        return asyncDocumentClient;
    }

    /**
     * Gets the azure key credential.
     *
     * @return azure key credential.
     */
    AzureKeyCredential credential() {
        return credential;
    }

    /***
     * Get the client telemetry config.
     *
     * @return the {@link CosmosClientTelemetryConfig}.
     */
    CosmosClientTelemetryConfig getClientTelemetryConfig() {
        return this.clientTelemetryConfig;
    }

    /**
     * CREATE a Database if it does not already exist on the service.
     * <br/>
     * The {@link Mono} upon successful completion will contain a single cosmos database response with the
     * created or existing database.
     *
     * @param databaseProperties CosmosDatabaseProperties.
     * @return a {@link Mono} containing the cosmos database response with the created or existing database or
     * an error.
     */
    public Mono<CosmosDatabaseResponse> createDatabaseIfNotExists(CosmosDatabaseProperties databaseProperties) {
        return withContext(context -> createDatabaseIfNotExistsInternal(getDatabase(databaseProperties.getId()),
            null, context));
    }

    /**
     * Create a Database if it does not already exist on the service.
     * <br/>
     * The {@link Mono} upon successful completion will contain a single cosmos database response with the
     * created or existing database.
     *
     * @param id the id of the database.
     * @return a {@link Mono} containing the cosmos database response with the created or existing database or
     * an error.
     */
    public Mono<CosmosDatabaseResponse> createDatabaseIfNotExists(String id) {
        return withContext(context -> createDatabaseIfNotExistsInternal(getDatabase(id), null, context));
    }

    /**
     * Create a Database if it does not already exist on the service.
     * <br/>
     * The throughputProperties will only be used if the specified database
     * does not exist and therefor a new database will be created with throughputProperties.
     * <br/>
     * The {@link Mono} upon successful completion will contain a single cosmos database response with the
     * created or existing database.
     *
     * @param id the id.
     * @param throughputProperties the throughputProperties.
     * @return the mono.
     */
    public Mono<CosmosDatabaseResponse> createDatabaseIfNotExists(String id, ThroughputProperties throughputProperties) {
        return withContext(context -> createDatabaseIfNotExistsInternal(getDatabase(id),
            throughputProperties, context));
    }

    /**
     * Creates a database.
     * <br/>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the
     * created database.
     * In case of failure the {@link Mono} will error.
     *
     * @param databaseProperties {@link CosmosDatabaseProperties}.
     * @param options {@link CosmosDatabaseRequestOptions}.
     * @return an {@link Mono} containing the single cosmos database response with the created database or an error.
     */
    public Mono<CosmosDatabaseResponse> createDatabase(CosmosDatabaseProperties databaseProperties,
                                                       CosmosDatabaseRequestOptions options) {
        final CosmosDatabaseRequestOptions requestOptions = options == null ? new CosmosDatabaseRequestOptions() : options;
        Database wrappedDatabase = new Database();
        wrappedDatabase.setId(databaseProperties.getId());
        return withContext(context -> createDatabaseInternal(wrappedDatabase, requestOptions, context));
    }

    /**
     * Creates a database.
     * <br/>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the
     * created database.
     * In case of failure the {@link Mono} will error.
     *
     * @param databaseProperties {@link CosmosDatabaseProperties}.
     * @return an {@link Mono} containing the single cosmos database response with the created database or an error.
     */
    public Mono<CosmosDatabaseResponse> createDatabase(CosmosDatabaseProperties databaseProperties) {
        return createDatabase(databaseProperties, new CosmosDatabaseRequestOptions());
    }

    /**
     * Creates a database.
     * <br/>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the
     * created database.
     * In case of failure the {@link Mono} will error.
     *
     * @param id id of the database.
     * @return a {@link Mono} containing the single cosmos database response with the created database or an error.
     */
    public Mono<CosmosDatabaseResponse> createDatabase(String id) {
        return createDatabase(new CosmosDatabaseProperties(id), new CosmosDatabaseRequestOptions());
    }

    /**
     * Creates a database.
     * <br/>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the
     * created database.
     * In case of failure the {@link Mono} will error.
     *
     * @param databaseProperties {@link CosmosDatabaseProperties}.
     * @param throughputProperties the throughput properties for the database.
     * @param options {@link CosmosDatabaseRequestOptions}.
     * @return an {@link Mono} containing the single cosmos database response with the created database or an error.
     */
    public Mono<CosmosDatabaseResponse> createDatabase(CosmosDatabaseProperties databaseProperties,
                                                       ThroughputProperties throughputProperties,
                                                       CosmosDatabaseRequestOptions options) {
        if (options == null) {
            options = new CosmosDatabaseRequestOptions();
        }

        ModelBridgeInternal.setThroughputProperties(options, throughputProperties);
        Database wrappedDatabase = new Database();
        wrappedDatabase.setId(databaseProperties.getId());
        final CosmosDatabaseRequestOptions requestOptions = options;
        return withContext(context -> createDatabaseInternal(wrappedDatabase, requestOptions, context));
    }

    /**
     * Creates a database.
     * <br/>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the
     * created database.
     * In case of failure the {@link Mono} will error.
     *
     * @param databaseProperties {@link CosmosDatabaseProperties}.
     * @param throughputProperties the throughput properties for the database.
     * @return an {@link Mono} containing the single cosmos database response with the created database or an error.
     */
    public Mono<CosmosDatabaseResponse> createDatabase(CosmosDatabaseProperties databaseProperties, ThroughputProperties throughputProperties) {
        CosmosDatabaseRequestOptions options = new CosmosDatabaseRequestOptions();
        ModelBridgeInternal.setThroughputProperties(options, throughputProperties);
        return createDatabase(databaseProperties, options);
    }

    /**
     * Creates a database.
     *
     * @param id the id.
     * @param throughputProperties the throughputProperties.
     * @return the mono.
     */
    public Mono<CosmosDatabaseResponse> createDatabase(String id, ThroughputProperties throughputProperties) {
        CosmosDatabaseRequestOptions options = new CosmosDatabaseRequestOptions();
        ModelBridgeInternal.setThroughputProperties(options, throughputProperties);
        return createDatabase(new CosmosDatabaseProperties(id), options);
    }

    /**
     * Reads all databases.
     * <br/>
     * After subscription the operation will be performed.
     * The {@link CosmosPagedFlux} will contain one or several feed response of the read databases.
     * In case of failure the {@link CosmosPagedFlux} will error.
     *
     * @param options {@link CosmosQueryRequestOptions}
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of read databases or an error.
     */
    CosmosPagedFlux<CosmosDatabaseProperties> readAllDatabases(CosmosQueryRequestOptions options) {
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            String spanName = "readAllDatabases";
            CosmosQueryRequestOptions nonNullOptions = options != null ? options : new CosmosQueryRequestOptions();

            QueryFeedOperationState state = new QueryFeedOperationState(
                this,
                spanName,
                null,
                null,
                ResourceType.Database,
                OperationType.ReadFeed,
                queryOptionsAccessor.getQueryNameOrDefault(nonNullOptions, spanName),
                nonNullOptions,
                pagedFluxOptions
            );

            pagedFluxOptions.setFeedOperationState(state);

            return getDocClientWrapper().readDatabases(state)
                .map(response ->
                    feedResponseAccessor.createFeedResponse(
                        ModelBridgeInternal.getCosmosDatabasePropertiesFromV2Results(response.getResults()),
                        response.getResponseHeaders(),
                        response.getCosmosDiagnostics()));
        });
    }

    /**
     * Reads all databases.
     * <br/>
     * After subscription the operation will be performed.
     * The {@link CosmosPagedFlux} will contain one or several feed response of the read databases.
     * In case of failure the {@link CosmosPagedFlux} will error.
     *
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of read databases or an error.
     */
    public CosmosPagedFlux<CosmosDatabaseProperties> readAllDatabases() {
        return readAllDatabases(new CosmosQueryRequestOptions());
    }


    /**
     * Query for databases.
     * <br/>
     * After subscription the operation will be performed.
     * The {@link CosmosPagedFlux} will contain one or several feed response of the read databases.
     * In case of failure the {@link CosmosPagedFlux} will error.
     *
     * @param query the query.
     * @param options the feed options.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of read databases or an error.
     */
    public CosmosPagedFlux<CosmosDatabaseProperties> queryDatabases(String query, CosmosQueryRequestOptions options) {
        if (options == null) {
            options = new CosmosQueryRequestOptions();
        }

        return queryDatabasesInternal(new SqlQuerySpec(query), options);
    }

    /**
     * Query for databases.
     * <br/>
     * After subscription the operation will be performed.
     * The {@link CosmosPagedFlux} will contain one or several feed response of the read databases.
     * In case of failure the {@link CosmosPagedFlux} will error.
     *
     * @param querySpec the SQL query specification.
     * @param options the feed options.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of read databases or an error.
     */
    public CosmosPagedFlux<CosmosDatabaseProperties> queryDatabases(SqlQuerySpec querySpec, CosmosQueryRequestOptions options) {
        if (options == null) {
            options = new CosmosQueryRequestOptions();
        }

        return queryDatabasesInternal(querySpec, options);
    }

    /**
     * Gets a database object without making a service call.
     *
     * @param id name of the database.
     * @return {@link CosmosAsyncDatabase}.
     */
    public CosmosAsyncDatabase getDatabase(String id) {
        return new CosmosAsyncDatabase(id, this);
    }

    /**
     * Close this {@link CosmosAsyncClient} instance and cleans up the resources.
     */
    @Override
    public void close() {
        if (this.clientMetricRegistrySnapshot != null) {
            ClientTelemetryMetrics.remove(this.clientMetricRegistrySnapshot);
        }
        asyncDocumentClient.close();
    }

    DiagnosticsProvider getDiagnosticsProvider() {
        return this.diagnosticsProvider;
    }

    /**
     * Enable throughput control group.
     *
     * @param group Throughput control group going to be enabled.
     * @param throughputQueryMono The throughput query mono.
     */
    void enableThroughputControlGroup(ThroughputControlGroupInternal group, Mono<Integer> throughputQueryMono) {
        checkNotNull(group, "Throughput control group cannot be null");
        this.asyncDocumentClient.enableThroughputControlGroup(group, throughputQueryMono);
    }

    /***
     * Configure fault injector provider.
     *
     * @param injectorProvider the injector provider.
     */
    void configureFaultInjectorProvider(IFaultInjectorProvider injectorProvider) {
        checkNotNull(injectorProvider, "Argument 'injectorProvider' can not be null");
        this.asyncDocumentClient.configureFaultInjectorProvider(injectorProvider);
    }

    /**
     * Create global throughput control config builder which will be used to build {@link GlobalThroughputControlConfig}.
     *
     * @param databaseId The database id of the control container.
     * @param containerId The container id of the control container.
     * @return A {@link GlobalThroughputControlConfigBuilder}.
     */
    public GlobalThroughputControlConfigBuilder createGlobalThroughputControlConfigBuilder(String databaseId, String containerId) {
        return new GlobalThroughputControlConfigBuilder(this, databaseId, containerId);
    }

    WriteRetryPolicy getNonIdempotentWriteRetryPolicy() {
        return this.nonIdempotentWriteRetryPolicy;
    }

    void openConnectionsAndInitCaches() {
        blockVoidFlux(asyncDocumentClient.submitOpenConnectionTasksAndInitCaches(proactiveContainerInitConfig));
    }

    void openConnectionsAndInitCaches(Duration aggressiveWarmupDuration) {
        Flux<Void> submitOpenConnectionTasksFlux = asyncDocumentClient.submitOpenConnectionTasksAndInitCaches(proactiveContainerInitConfig);
        blockVoidFlux(wrapSourceFluxAndSoftCompleteAfterTimeout(submitOpenConnectionTasksFlux, aggressiveWarmupDuration));
    }

    // this method is currently used to open connections when the client is being built
    // the goal is to switch b/w a blocking flow to non-blocking flow when it comes
    // to opening connections and at the same time to only block for some specified duration
    // the below method allows the original flux to continue opening connections
    // by not issuing a cancel on it, instead we wrap around the original flux
    // with a sink and block on the wrapping flux for the specified duration
    private Flux<Void> wrapSourceFluxAndSoftCompleteAfterTimeout(Flux<Void> source, Duration timeout) {
        return Flux.<Void>create(sink -> {
                    source
                        .doFinally(signalType -> sink.complete())
                        .subscribe(t -> sink.next(t));
                })
                .take(timeout);
    }

    private void blockVoidFlux(Flux<Void> voidFlux) {
        try {
            voidFlux.blockLast();
        } catch (Exception ex) {
            // swallow exceptions here
            logger.warn("The void flux did not complete successfully", ex);
        }
    }

    private CosmosPagedFlux<CosmosDatabaseProperties> queryDatabasesInternal(
        SqlQuerySpec querySpec,
        CosmosQueryRequestOptions options){

        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            String spanName = "queryDatabases";
            CosmosQueryRequestOptions nonNullOptions = options != null ? options : new CosmosQueryRequestOptions();

            QueryFeedOperationState state = new QueryFeedOperationState(
                this,
                spanName,
                null,
                null,
                ResourceType.Database,
                OperationType.Query,
                queryOptionsAccessor.getQueryNameOrDefault(nonNullOptions, spanName),
                nonNullOptions,
                pagedFluxOptions
            );

            pagedFluxOptions.setFeedOperationState(state);

            return getDocClientWrapper().queryDatabases(querySpec, state)
                .map(response -> feedResponseAccessor.createFeedResponse(
                    ModelBridgeInternal.getCosmosDatabasePropertiesFromV2Results(response.getResults()),
                    response.getResponseHeaders(),
                    response.getCosmosDiagnostics()));
        });
    }


    private Mono<CosmosDatabaseResponse> createDatabaseIfNotExistsInternal(CosmosAsyncDatabase database,
                                                                           ThroughputProperties throughputProperties, Context context) {
        String spanName = "createDatabaseIfNotExists." + database.getId();
        Context nestedContext = context.addData(
            DiagnosticsProvider.COSMOS_CALL_DEPTH,
            DiagnosticsProvider.COSMOS_CALL_DEPTH_VAL);
        CosmosDatabaseRequestOptions options = new CosmosDatabaseRequestOptions();
        Mono<CosmosDatabaseResponse> responseMono = database.readInternal(new CosmosDatabaseRequestOptions(),
            nestedContext).onErrorResume(exception -> {
            final Throwable unwrappedException = Exceptions.unwrap(exception);
            if (unwrappedException instanceof CosmosException) {
                final CosmosException cosmosException = (CosmosException) unwrappedException;
                if (cosmosException.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {

                    if (throughputProperties != null) {
                        ModelBridgeInternal.setThroughputProperties(options, throughputProperties);
                    }

                    Database wrappedDatabase = new Database();
                    wrappedDatabase.setId(database.getId());
                    return createDatabaseInternal(wrappedDatabase,
                        options, nestedContext);
                }
            }
            return Mono.error(unwrappedException);
        });

        RequestOptions requestOptions = ModelBridgeInternal.toRequestOptions(options);

        return this.diagnosticsProvider.traceEnabledCosmosResponsePublisher(
            responseMono,
            context,
            spanName,
            database.getId(),
            null,
            this,
            null,
            OperationType.Create,
            ResourceType.Database,
            requestOptions);
    }

    private Mono<CosmosDatabaseResponse> createDatabaseInternal(Database database, CosmosDatabaseRequestOptions options,
                                                             Context context) {
        String spanName = "createDatabase." + database.getId();
        RequestOptions requestOptions = ModelBridgeInternal.toRequestOptions(options);
        Mono<CosmosDatabaseResponse> responseMono = asyncDocumentClient.createDatabase(database, requestOptions)
            .map(ModelBridgeInternal::createCosmosDatabaseResponse)
            .single();
        return this.diagnosticsProvider
            .traceEnabledCosmosResponsePublisher(
                responseMono,
                context,
                spanName,
                database.getId(),
                null,
                this,
                null,
                OperationType.Create,
                ResourceType.Database,
                requestOptions);
    }

    private ConsistencyLevel getEffectiveConsistencyLevel(
        OperationType operationType,
        ConsistencyLevel desiredConsistencyLevelOfOperation) {

        if (operationType.isWriteOperation()) {
            return this.accountConsistencyLevel;
        }

        if (desiredConsistencyLevelOfOperation != null) {
            return desiredConsistencyLevelOfOperation;
        }

        if (this.desiredConsistencyLevel != null) {
            return desiredConsistencyLevel;
        }

        return this.accountConsistencyLevel;
    }

    CosmosDiagnosticsThresholds getEffectiveDiagnosticsThresholds(
        CosmosDiagnosticsThresholds operationLevelThresholds) {

        if (operationLevelThresholds != null) {
            return operationLevelThresholds;
        }


        if (this.clientTelemetryConfig == null) {
            return new CosmosDiagnosticsThresholds();
        }

        CosmosDiagnosticsThresholds clientLevelThresholds =
            telemetryConfigAccessor.getDiagnosticsThresholds(this.clientTelemetryConfig);

        return clientLevelThresholds != null ? clientLevelThresholds : new CosmosDiagnosticsThresholds();
    }

    CosmosItemSerializer getEffectiveItemSerializer(CosmosItemSerializer requestOptionsItemSerializer) {
        return this.asyncDocumentClient.getEffectiveItemSerializer(requestOptionsItemSerializer);
    }

    boolean isTransportLevelTracingEnabled() {

        CosmosClientTelemetryConfig effectiveConfig = this.clientTelemetryConfig != null ?
            this.clientTelemetryConfig
            : DEFAULT_TELEMETRY_CONFIG;

        if (telemetryConfigAccessor.isLegacyTracingEnabled(effectiveConfig)) {
            return false;
        }

        if (this.getConnectionPolicy().getConnectionMode() != ConnectionMode.DIRECT) {
            return false;
        }

        return telemetryConfigAccessor.isTransportLevelTracingEnabled(effectiveConfig);
    }

    void recordOpenConnectionsAndInitCachesCompleted(List<CosmosContainerIdentity> cosmosContainerIdentities) {
        this.asyncDocumentClient.recordOpenConnectionsAndInitCachesCompleted(cosmosContainerIdentities);
    }

    void recordOpenConnectionsAndInitCachesStarted(List<CosmosContainerIdentity> cosmosContainerIdentities) {
        this.asyncDocumentClient.recordOpenConnectionsAndInitCachesStarted(cosmosContainerIdentities);
    }

    String getAccountTagValue() {
        return this.accountTagValue;
    }

    Tag getClientCorrelationTag() {
        return this.clientCorrelationTag;
    }

    String getUserAgent() {
        return this.asyncDocumentClient.getUserAgent();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.CosmosAsyncClientHelper.setCosmosAsyncClientAccessor(
            new ImplementationBridgeHelpers.CosmosAsyncClientHelper.CosmosAsyncClientAccessor() {

                @Override
                public Tag getClientCorrelationTag(CosmosAsyncClient client) {
                    return client.getClientCorrelationTag();
                }

                @Override
                public String getAccountTagValue(CosmosAsyncClient client) {
                    return client.getAccountTagValue();
                }

                @Override
                public EnumSet<TagName> getMetricTagNames(CosmosAsyncClient client) {
                    return  telemetryConfigAccessor
                        .getMetricTagNames(client.clientTelemetryConfig);
                }

                @Override
                public EnumSet<MetricCategory> getMetricCategories(CosmosAsyncClient client) {
                    return  telemetryConfigAccessor
                        .getMetricCategories(client.clientTelemetryConfig);
                }

                @Override
                public boolean shouldEnableEmptyPageDiagnostics(CosmosAsyncClient client) {
                    return client.clientMetricRegistrySnapshot != null || client.isTransportLevelTracingEnabled();
                }

                @Override
                public boolean isSendClientTelemetryToServiceEnabled(CosmosAsyncClient client) {
                    return client.isSendClientTelemetryToServiceEnabled;
                }

                @Override
                public List<String> getPreferredRegions(CosmosAsyncClient client) {
                    return client.connectionPolicy.getPreferredRegions();
                }

                @Override
                public boolean isEndpointDiscoveryEnabled(CosmosAsyncClient client) {
                    return client.connectionPolicy.isEndpointDiscoveryEnabled();
                }

                @Override
                public String getConnectionMode(CosmosAsyncClient client) {
                    return client.connectionPolicy.getConnectionMode().toString();
                }

                @Override
                public String getUserAgent(CosmosAsyncClient client) {
                    return client.getUserAgent();
                }

                @Override
                public CosmosMeterOptions getMeterOptions(CosmosAsyncClient client, CosmosMetricName name) {
                    return  telemetryConfigAccessor
                        .getMeterOptions(client.clientTelemetryConfig, name);
                }

                @Override
                public boolean isEffectiveContentResponseOnWriteEnabled(CosmosAsyncClient client,
                                                                        Boolean requestOptionsContentResponseEnabled) {
                    if (requestOptionsContentResponseEnabled != null) {
                        return requestOptionsContentResponseEnabled;
                    }

                    return client.asyncDocumentClient.isContentResponseOnWriteEnabled();
                }

                @Override
                public ConsistencyLevel getEffectiveConsistencyLevel(
                    CosmosAsyncClient client,
                    OperationType operationType,
                    ConsistencyLevel desiredConsistencyLevelOfOperation) {

                    return client.getEffectiveConsistencyLevel(operationType, desiredConsistencyLevelOfOperation);
                }

                @Override
                public CosmosDiagnosticsThresholds getEffectiveDiagnosticsThresholds(
                    CosmosAsyncClient client,
                    CosmosDiagnosticsThresholds operationLevelThresholds) {

                    return client.getEffectiveDiagnosticsThresholds(operationLevelThresholds);
                }

                @Override
                public DiagnosticsProvider getDiagnosticsProvider(CosmosAsyncClient client) {
                    return client.getDiagnosticsProvider();
                }

                @Override
                public List<CosmosOperationPolicy> getOperationPolicies(CosmosAsyncClient client) {
                    return client.requestPolicies;
                }

                @Override
                public CosmosItemSerializer getEffectiveItemSerializer(CosmosAsyncClient client, CosmosItemSerializer requestOptionsItemSerializer) {
                    return client.getEffectiveItemSerializer(requestOptionsItemSerializer);
                }
            }
        );
    }

    static { initialize(); }
}
