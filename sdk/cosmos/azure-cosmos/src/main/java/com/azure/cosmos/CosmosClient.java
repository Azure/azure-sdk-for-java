// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.core.annotation.ServiceClient;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.models.CosmosContainerIdentity;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosDatabaseRequestOptions;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.CosmosPagedIterable;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.time.Duration;
import java.util.List;

/**
 * Provides a client-side logical representation of the Azure Cosmos DB service.
 * Calls to CosmosClient API's are blocked for completion.
 * <p>
 * CosmosClient is thread-safe.
 * It's recommended to maintain a single instance of CosmosClient per lifetime of the application which enables efficient connection management and performance.
 * CosmosClient initialization is a heavy operation - don't use initialization CosmosClient instances as credentials or network connectivity validations.
 */
@ServiceClient(builder = CosmosClientBuilder.class)
public final class CosmosClient implements Closeable {
    private final CosmosAsyncClient asyncClientWrapper;

    CosmosClient(CosmosClientBuilder builder) {
        this.asyncClientWrapper = builder.buildAsyncClient(false);
    }

    /**
     * Create a Cosmos database if it does not already exist on the service.
     * <!-- src_embed com.azure.cosmos.CosmosClient.createDatabaseIfNotExists -->
     * <pre>
     * CosmosDatabaseProperties databaseProperties = new CosmosDatabaseProperties&#40;databaseName&#41;;
     * cosmosClient.createDatabaseIfNotExists&#40;databaseProperties&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosClient.createDatabaseIfNotExists -->
     * @param databaseProperties {@link CosmosDatabaseProperties} the database properties.
     * @return the {@link CosmosDatabaseResponse} with the created database.
     */
    CosmosDatabaseResponse createDatabaseIfNotExists(CosmosDatabaseProperties databaseProperties) {
        return blockDatabaseResponse(asyncClientWrapper.createDatabaseIfNotExists(databaseProperties));
    }

    /**
     * Create a Cosmos database if it does not already exist on the service.
     * <!-- src_embed com.azure.cosmos.CosmosClient.createDatabaseIfNotExistsThroughput -->
     * <pre>
     * ThroughputProperties throughputProperties = ThroughputProperties
     *     .createAutoscaledThroughput&#40;autoScaleMaxThroughput&#41;;
     * cosmosClient.createDatabaseIfNotExists&#40;databaseName, throughputProperties&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosClient.createDatabaseIfNotExistsThroughput -->
     * The throughputProperties will only be used if the specified database
     * does not exist and therefor a new database will be created with throughputProperties.
     *
     * @param id the id of the database.
     * @param throughputProperties the throughputProperties.
     * @return the {@link CosmosDatabaseResponse} with the created database.
     */
    public CosmosDatabaseResponse createDatabaseIfNotExists(String id, ThroughputProperties throughputProperties) {
        return blockDatabaseResponse(asyncClientWrapper.createDatabaseIfNotExists(id, throughputProperties));
    }

    /**
     * Create a Cosmos database if it does not already exist on the service.
     * <!-- src_embed com.azure.cosmos.CosmosClient.createDatabaseIfNotExists -->
     * <pre>
     * CosmosDatabaseProperties databaseProperties = new CosmosDatabaseProperties&#40;databaseName&#41;;
     * cosmosClient.createDatabaseIfNotExists&#40;databaseProperties&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosClient.createDatabaseIfNotExists -->
     * @param id the id of the database.
     * @return the {@link CosmosDatabaseResponse} with the created database.
     */
    public CosmosDatabaseResponse createDatabaseIfNotExists(String id) {
        return blockDatabaseResponse(asyncClientWrapper.createDatabaseIfNotExists(id));
    }

    /**
     * Creates a database.
     * <!-- src_embed com.azure.cosmos.CosmosClient.createDatabase -->
     * <pre>
     * CosmosDatabaseProperties databaseProperties = new CosmosDatabaseProperties&#40;databaseName&#41;;
     * cosmosClient.createDatabase&#40;databaseProperties&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosClient.createDatabase -->
     * @param databaseProperties {@link CosmosDatabaseProperties} the database properties.
     * @param options the request options.
     * @return the {@link CosmosDatabaseResponse} with the created database.
     * @throws CosmosException if resource with specified id already exists
     */
    public CosmosDatabaseResponse createDatabase(CosmosDatabaseProperties databaseProperties,
                                                 CosmosDatabaseRequestOptions options) {
        return blockDatabaseResponse(asyncClientWrapper.createDatabase(databaseProperties, options));
    }

    /**
     * Creates a Cosmos database.
     * <!-- src_embed com.azure.cosmos.CosmosClient.createDatabase -->
     * <pre>
     * CosmosDatabaseProperties databaseProperties = new CosmosDatabaseProperties&#40;databaseName&#41;;
     * cosmosClient.createDatabase&#40;databaseProperties&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosClient.createDatabase -->
     * @param databaseProperties {@link CosmosDatabaseProperties} the database properties.
     * @return the {@link CosmosDatabaseResponse} with the created database.
     * @throws CosmosException if resource with specified id already exists
     */
    public CosmosDatabaseResponse createDatabase(CosmosDatabaseProperties databaseProperties) {
        return blockDatabaseResponse(asyncClientWrapper.createDatabase(databaseProperties));
    }

    /**
     * Creates a Cosmos database.
     * <!-- src_embed com.azure.cosmos.CosmosClient.createDatabase -->
     * <pre>
     * CosmosDatabaseProperties databaseProperties = new CosmosDatabaseProperties&#40;databaseName&#41;;
     * cosmosClient.createDatabase&#40;databaseProperties&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosClient.createDatabase -->
     * @param id the id of the database.
     * @return the {@link CosmosDatabaseResponse} with the created database.
     * @throws CosmosException if resource with specified id already exists
     */
    public CosmosDatabaseResponse createDatabase(String id) {
        return blockDatabaseResponse(asyncClientWrapper.createDatabase(id));

    }

    /**
     * Creates a Cosmos database.
     * <!-- src_embed com.azure.cosmos.CosmosClient.createDatabaseThroughput -->
     * <pre>
     * ThroughputProperties throughputProperties = ThroughputProperties
     *     .createAutoscaledThroughput&#40;autoScaleMaxThroughput&#41;;
     * cosmosClient.createDatabase&#40;databaseName, throughputProperties&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosClient.createDatabaseThroughput -->
     * @param databaseProperties {@link CosmosDatabaseProperties} the database properties.
     * @param throughputProperties the throughput properties.
     * @param options {@link CosmosDatabaseRequestOptions} the request options.
     * @return the {@link CosmosDatabaseResponse} with the created database.
     * @throws CosmosException if resource with specified id already exists
     */
    public CosmosDatabaseResponse createDatabase(CosmosDatabaseProperties databaseProperties,
                                                 ThroughputProperties throughputProperties,
                                                 CosmosDatabaseRequestOptions options) {
        return blockDatabaseResponse(asyncClientWrapper.createDatabase(databaseProperties, throughputProperties, options));
    }

    /**
     * Creates a Cosmos database.
     * <!-- src_embed com.azure.cosmos.CosmosClient.createDatabaseThroughput -->
     * <pre>
     * ThroughputProperties throughputProperties = ThroughputProperties
     *     .createAutoscaledThroughput&#40;autoScaleMaxThroughput&#41;;
     * cosmosClient.createDatabase&#40;databaseName, throughputProperties&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosClient.createDatabaseThroughput -->
     * @param databaseProperties {@link CosmosDatabaseProperties} the database properties.
     * @param throughputProperties the throughput properties.
     * @return the {@link CosmosDatabaseResponse} with the created database.
     * @throws CosmosException if resource with specified id already exists
     */
    public CosmosDatabaseResponse createDatabase(CosmosDatabaseProperties databaseProperties,
                                                 ThroughputProperties throughputProperties) {
        return blockDatabaseResponse(asyncClientWrapper.createDatabase(databaseProperties, throughputProperties));
    }

    /**
     * Creates a Cosmos database.
     * <!-- src_embed com.azure.cosmos.CosmosClient.createDatabaseThroughput -->
     * <pre>
     * ThroughputProperties throughputProperties = ThroughputProperties
     *     .createAutoscaledThroughput&#40;autoScaleMaxThroughput&#41;;
     * cosmosClient.createDatabase&#40;databaseName, throughputProperties&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosClient.createDatabaseThroughput -->
     * @param id the id of the database.
     * @param throughputProperties the throughput properties.
     * @return the {@link CosmosDatabaseResponse} with the created database.
     * @throws CosmosException if resource with specified id already exists
     */
    public CosmosDatabaseResponse createDatabase(String id, ThroughputProperties throughputProperties) {
        return blockDatabaseResponse(asyncClientWrapper.createDatabase(id, throughputProperties));
    }

    void openConnectionsAndInitCaches() {
        asyncClientWrapper.openConnectionsAndInitCaches();
    }

    void openConnectionsAndInitCaches(Duration aggressiveWarmupDuration) {
        asyncClientWrapper.openConnectionsAndInitCaches(aggressiveWarmupDuration);
    }

    void recordOpenConnectionsAndInitCachesCompleted(List<CosmosContainerIdentity> cosmosContainerIdentities) {
        this.asyncClientWrapper.recordOpenConnectionsAndInitCachesCompleted(cosmosContainerIdentities);
    }

    void recordOpenConnectionsAndInitCachesStarted(List<CosmosContainerIdentity> cosmosContainerIdentities) {
        this.asyncClientWrapper.recordOpenConnectionsAndInitCachesStarted(cosmosContainerIdentities);
    }

    CosmosDatabaseResponse blockDatabaseResponse(Mono<CosmosDatabaseResponse> databaseMono) {
        try {
            return databaseMono.block();
        } catch (Exception ex) {
            final Throwable throwable = Exceptions.unwrap(ex);
            if (throwable instanceof CosmosException) {
                throw (CosmosException) throwable;
            } else {
                throw Exceptions.propagate(ex);
            }
        }
    }

    /**
     * Reads all Cosmos databases.
     * <!-- src_embed com.azure.cosmos.CosmosClient.readAllDatabases -->
     * <pre>
     * CosmosPagedIterable&lt;CosmosDatabaseProperties&gt; cosmosDatabaseProperties =
     *     cosmosClient.readAllDatabases&#40;&#41;;
     * cosmosDatabaseProperties.forEach&#40;databaseProperties -&gt; &#123;
     *     System.out.println&#40;databaseProperties&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosClient.readAllDatabases -->
     * @param options {@link CosmosQueryRequestOptions}the feed options.
     * @return the {@link CosmosPagedIterable} for feed response with the read databases.
     */
    CosmosPagedIterable<CosmosDatabaseProperties> readAllDatabases(CosmosQueryRequestOptions options) {
        return getCosmosPagedIterable(asyncClientWrapper.readAllDatabases(options));
    }

    /**
     * Reads all Cosmos databases.
     * <!-- src_embed com.azure.cosmos.CosmosClient.readAllDatabases -->
     * <pre>
     * CosmosPagedIterable&lt;CosmosDatabaseProperties&gt; cosmosDatabaseProperties =
     *     cosmosClient.readAllDatabases&#40;&#41;;
     * cosmosDatabaseProperties.forEach&#40;databaseProperties -&gt; &#123;
     *     System.out.println&#40;databaseProperties&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosClient.readAllDatabases -->
     * @return the {@link CosmosPagedIterable} for feed response with the read databases.
     */
    public CosmosPagedIterable<CosmosDatabaseProperties> readAllDatabases() {
        return getCosmosPagedIterable(asyncClientWrapper.readAllDatabases());
    }

    /**
     * Query a Cosmos database.
     * <!-- src_embed com.azure.cosmos.CosmosClient.queryDatabases -->
     * <pre>
     * CosmosQueryRequestOptions options = new CosmosQueryRequestOptions&#40;&#41;;
     * CosmosPagedIterable&lt;CosmosDatabaseProperties&gt; databaseProperties =
     *     cosmosClient.queryDatabases&#40;&quot;select * from d&quot;, options&#41;;
     * databaseProperties.forEach&#40;properties -&gt; &#123;
     *     System.out.println&#40;properties.getId&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosClient.queryDatabases -->
     * @param query the query.
     * @param options {@link CosmosQueryRequestOptions}the feed options.
     * @return the {@link CosmosPagedIterable} for feed response with the obtained databases.
     */
    public CosmosPagedIterable<CosmosDatabaseProperties> queryDatabases(String query, CosmosQueryRequestOptions options) {
        return getCosmosPagedIterable(asyncClientWrapper.queryDatabases(query, options));
    }

    /**
     * Query a Cosmos database.
     * <!-- src_embed com.azure.cosmos.CosmosClient.queryDatabases -->
     * <pre>
     * CosmosQueryRequestOptions options = new CosmosQueryRequestOptions&#40;&#41;;
     * CosmosPagedIterable&lt;CosmosDatabaseProperties&gt; databaseProperties =
     *     cosmosClient.queryDatabases&#40;&quot;select * from d&quot;, options&#41;;
     * databaseProperties.forEach&#40;properties -&gt; &#123;
     *     System.out.println&#40;properties.getId&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosClient.queryDatabases -->
     * @param querySpec {@link SqlQuerySpec} the query spec.
     * @param options the query request options.
     * @return the {@link CosmosPagedIterable} for feed response with the obtained databases.
     */
    public CosmosPagedIterable<CosmosDatabaseProperties> queryDatabases(SqlQuerySpec querySpec,
                                                                        CosmosQueryRequestOptions options) {
        return getCosmosPagedIterable(asyncClientWrapper.queryDatabases(querySpec, options));
    }

    /**
     * Gets the Cosmos database instance without making a service call.
     *
     * @param id the id of the database.
     * @return {@link CosmosDatabase} the cosmos sync database.
     */
    public CosmosDatabase getDatabase(String id) {
        return new CosmosDatabase(id, this, asyncClientWrapper.getDatabase(id));
    }

    CosmosAsyncClient asyncClient() {
        return this.asyncClientWrapper;
    }

    /**
     * Close this {@link CosmosClient} instance.
     */
    public void close() {
        asyncClientWrapper.close();
    }

    private <T> CosmosPagedIterable<T> getCosmosPagedIterable(CosmosPagedFlux<T> cosmosPagedFlux) {
        return new CosmosPagedIterable<>(cosmosPagedFlux);
    }

    /**
     * Create global throughput control config builder which will be used to build {@link GlobalThroughputControlConfig}.
     *
     * @param databaseId The database id of the control container.
     * @param containerId The container id of the control container.
     * @return A {@link GlobalThroughputControlConfigBuilder}.
     */
    public GlobalThroughputControlConfigBuilder createGlobalThroughputControlConfigBuilder(String databaseId, String containerId) {
        return new GlobalThroughputControlConfigBuilder(this.asyncClientWrapper, databaseId, containerId);
    }

    static void initialize() {
        ImplementationBridgeHelpers.CosmosClientHelper.setCosmosClientAccessor(
            cosmosClient -> cosmosClient.asyncClient());
    }

    static { initialize(); }
}
