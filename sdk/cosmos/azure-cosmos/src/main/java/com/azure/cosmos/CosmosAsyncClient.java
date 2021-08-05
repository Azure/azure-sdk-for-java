// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.Context;
import com.azure.core.util.tracing.Tracer;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.CosmosAuthorizationTokenResolver;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.Permission;
import com.azure.cosmos.implementation.TracerProvider;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdMetrics;
import com.azure.cosmos.implementation.throughputControl.config.ThroughputControlGroupInternal;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosDatabaseRequestOptions;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosPermissionProperties;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.util.Beta;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.UtilBridgeInternal;
import io.micrometer.core.instrument.MeterRegistry;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.cosmos.implementation.Utils.setContinuationTokenAndMaxItemCount;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Provides a client-side logical representation of the Azure Cosmos DB service.
 * This asynchronous client is used to configure and execute requests against the service.
 */
@ServiceClient(
    builder = CosmosClientBuilder.class,
    isAsync = true)
public final class CosmosAsyncClient implements Closeable {

    // Async Cosmos client wrapper
    private final Configs configs;
    private final AsyncDocumentClient asyncDocumentClient;
    private final String serviceEndpoint;
    private final String keyOrResourceToken;
    private final ConnectionPolicy connectionPolicy;
    private final ConsistencyLevel desiredConsistencyLevel;
    private final List<CosmosPermissionProperties> permissions;
    private final CosmosAuthorizationTokenResolver cosmosAuthorizationTokenResolver;
    private final AzureKeyCredential credential;
    private final TokenCredential tokenCredential;
    private final boolean sessionCapturingOverride;
    private final boolean enableTransportClientSharing;
    private final boolean clientTelemetryEnabled;
    private final TracerProvider tracerProvider;
    private final boolean contentResponseOnWriteEnabled;
    private static final Tracer TRACER;

    static {
        ServiceLoader<Tracer> serviceLoader = ServiceLoader.load(Tracer.class);
        Iterator<?> iterator = serviceLoader.iterator();
        if (iterator.hasNext()) {
            TRACER = serviceLoader.iterator().next();
        } else {
            TRACER = null;
        }
    }

    CosmosAsyncClient(CosmosClientBuilder builder) {
        this.configs = builder.configs();
        this.serviceEndpoint = builder.getEndpoint();
        this.keyOrResourceToken = builder.getKey();
        this.connectionPolicy = builder.getConnectionPolicy();
        this.desiredConsistencyLevel = builder.getConsistencyLevel();
        this.permissions = builder.getPermissions();
        this.cosmosAuthorizationTokenResolver = builder.getAuthorizationTokenResolver();
        this.credential = builder.getCredential();
        this.tokenCredential = builder.getTokenCredential();
        this.sessionCapturingOverride = builder.isSessionCapturingOverrideEnabled();
        this.enableTransportClientSharing = builder.isConnectionSharingAcrossClientsEnabled();
        this.clientTelemetryEnabled = builder.isClientTelemetryEnabled();
        this.contentResponseOnWriteEnabled = builder.isContentResponseOnWriteEnabled();
        this.tracerProvider = new TracerProvider(TRACER);

        List<Permission> permissionList = new ArrayList<>();
        if (this.permissions != null) {
            permissionList =
                this.permissions
                    .stream()
                    .map(permissionProperties -> ModelBridgeInternal.getPermission(permissionProperties))
                    .filter(permission -> permission != null)
                    .collect(Collectors.toList());
        }

        this.asyncDocumentClient = new AsyncDocumentClient.Builder()
                                       .withServiceEndpoint(this.serviceEndpoint)
                                       .withMasterKeyOrResourceToken(this.keyOrResourceToken)
                                       .withConnectionPolicy(this.connectionPolicy)
                                       .withConsistencyLevel(this.desiredConsistencyLevel)
                                       .withSessionCapturingOverride(this.sessionCapturingOverride)
                                       .withConfigs(this.configs)
                                       .withTokenResolver(this.cosmosAuthorizationTokenResolver)
                                       .withCredential(this.credential)
                                       .withTransportClientSharing(this.enableTransportClientSharing)
                                       .withContentResponseOnWriteEnabled(this.contentResponseOnWriteEnabled)
                                       .withTokenCredential(this.tokenCredential)
                                       .withState(builder.metadataCaches())
                                       .withPermissionFeed(permissionList)
                                       .build();
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
     * Gets the key or resource token.
     *
     * @return get the key or resource token.
     */
    String getKeyOrResourceToken() {
        return keyOrResourceToken;
    }

    /**
     * Get the connection policy.
     *
     * @return {@link ConnectionPolicy}.
     */
    ConnectionPolicy getConnectionPolicy() {
        return connectionPolicy;
    }

    /**
     * Gets the consistency level.
     *
     * @return the {@link ConsistencyLevel}.
     */
    ConsistencyLevel getDesiredConsistencyLevel() {
        return desiredConsistencyLevel;
    }

    /**
     * Gets the permission list.
     *
     * @return the permission list.
     */
    List<CosmosPermissionProperties> getPermissions() {
        return permissions;
    }

    AsyncDocumentClient getDocClientWrapper() {
        return asyncDocumentClient;
    }

    /**
     * Gets the configs.
     *
     * @return the configs.
     */
    Configs getConfigs() {
        return configs;
    }

    /**
     * Gets the token resolver.
     *
     * @return the token resolver.
     */
    CosmosAuthorizationTokenResolver getCosmosAuthorizationTokenResolver() {
        return cosmosAuthorizationTokenResolver;
    }

    /**
     * Gets the azure key credential.
     *
     * @return azure key credential.
     */
    AzureKeyCredential credential() {
        return credential;
    }

    /**
     * Gets the boolean which indicates whether to only return the headers and status code in Cosmos DB response
     * in case of Create, Update and Delete operations on CosmosItem.
     *
     * If set to false (which is by default), this removes the resource from response. It reduces networking
     * and CPU load by not sending the resource back over the network and serializing it
     * on the client.
     *
     * By-default, this is false.
     *
     * @return a boolean indicating whether resource will be included in the response or not.
     */
    boolean isContentResponseOnWriteEnabled() {
        return contentResponseOnWriteEnabled;
    }

    boolean isClientTelemetryEnabled() {
        return clientTelemetryEnabled;
    }

    /**
     * CREATE a Database if it does not already exist on the service.
     * <p>
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
     * <p>
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
     * <p>
     * The throughputProperties will only be used if the specified database
     * does not exist and therefor a new database will be created with throughputProperties.
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
     * After subscription the operation will be performed.
     * The {@link CosmosPagedFlux} will contain one or several feed response of the read databases.
     * In case of failure the {@link CosmosPagedFlux} will error.
     *
     * @param options {@link CosmosQueryRequestOptions}
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of read databases or an error.
     */
    CosmosPagedFlux<CosmosDatabaseProperties> readAllDatabases(CosmosQueryRequestOptions options) {
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            pagedFluxOptions.setTracerInformation(this.tracerProvider, "readAllDatabases", this.serviceEndpoint, null);
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, options);
            return getDocClientWrapper().readDatabases(options)
                .map(response ->
                    BridgeInternal.createFeedResponse(
                        ModelBridgeInternal.getCosmosDatabasePropertiesFromV2Results(response.getResults()),
                        response.getResponseHeaders()));
        });
    }

    /**
     * Reads all databases.
     * <p>
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
     * <p>
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
     * <p>
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
        asyncDocumentClient.close();
    }

    TracerProvider getTracerProvider(){
        return this.tracerProvider;
    }

    /**
     * Enable throughput control group.
     *
     * @param group Throughput control group going to be enabled.
     */
    void enableThroughputControlGroup(ThroughputControlGroupInternal group) {
        checkNotNull(group, "Throughput control group cannot be null");
        this.asyncDocumentClient.enableThroughputControlGroup(group);
    }

    /**
     * Create global throughput control config builder which will be used to build {@link GlobalThroughputControlConfig}.
     *
     * @param databaseId The database id of the control container.
     * @param containerId The container id of the control container.
     * @return A {@link GlobalThroughputControlConfigBuilder}.
     */
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public GlobalThroughputControlConfigBuilder createGlobalThroughputControlConfigBuilder(String databaseId, String containerId) {
        return new GlobalThroughputControlConfigBuilder(this, databaseId, containerId);
    }

    private CosmosPagedFlux<CosmosDatabaseProperties> queryDatabasesInternal(SqlQuerySpec querySpec, CosmosQueryRequestOptions options){
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            pagedFluxOptions.setTracerInformation(this.tracerProvider, "queryDatabases", this.serviceEndpoint, null);
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, options);
            return getDocClientWrapper().queryDatabases(querySpec, options)
                .map(response -> BridgeInternal.createFeedResponse(
                    ModelBridgeInternal.getCosmosDatabasePropertiesFromV2Results(response.getResults()),
                    response.getResponseHeaders()));
        });
    }


    private Mono<CosmosDatabaseResponse> createDatabaseIfNotExistsInternal(CosmosAsyncDatabase database,
                                                                           ThroughputProperties throughputProperties, Context context) {
        String spanName = "createDatabaseIfNotExists." + database.getId();
        Context nestedContext = context.addData(TracerProvider.COSMOS_CALL_DEPTH, TracerProvider.COSMOS_CALL_DEPTH_VAL);
        Mono<CosmosDatabaseResponse> responseMono = database.readInternal(new CosmosDatabaseRequestOptions(),
            nestedContext).onErrorResume(exception -> {
            final Throwable unwrappedException = Exceptions.unwrap(exception);
            if (unwrappedException instanceof CosmosException) {
                final CosmosException cosmosException = (CosmosException) unwrappedException;
                if (cosmosException.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                    CosmosDatabaseRequestOptions requestOptions = new CosmosDatabaseRequestOptions();
                    if (throughputProperties != null) {
                        ModelBridgeInternal.setThroughputProperties(requestOptions, throughputProperties);
                    }

                    Database wrappedDatabase = new Database();
                    wrappedDatabase.setId(database.getId());
                    return createDatabaseInternal(wrappedDatabase,
                        requestOptions, nestedContext);
                }
            }
            return Mono.error(unwrappedException);
        });
        return tracerProvider.traceEnabledCosmosResponsePublisher(responseMono,
            context,
            spanName,
            database.getId(),
            this.serviceEndpoint);
    }

    private Mono<CosmosDatabaseResponse> createDatabaseInternal(Database database, CosmosDatabaseRequestOptions options,
                                                             Context context) {
        String spanName = "createDatabase." + database.getId();
        Mono<CosmosDatabaseResponse> responseMono = asyncDocumentClient.createDatabase(database, ModelBridgeInternal.toRequestOptions(options))
            .map(databaseResourceResponse -> ModelBridgeInternal.createCosmosDatabaseResponse(databaseResourceResponse))
            .single();
        return tracerProvider.traceEnabledCosmosResponsePublisher(responseMono,
            context,
            spanName,
            database.getId(),
            this.serviceEndpoint);
    }
}
