// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.core.annotation.ServiceClient;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosDatabaseRequestOptions;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.util.Beta;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.azure.cosmos.util.UtilBridgeInternal;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.Closeable;

/**
 * Provides a client-side logical representation of the Azure Cosmos DB service.
 * Calls to CosmosClient API's are blocked for completion.
 */
@ServiceClient(builder = CosmosClientBuilder.class)
public final class CosmosClient implements Closeable {
    private final CosmosAsyncClient asyncClientWrapper;

    CosmosClient(CosmosClientBuilder builder) {
        this.asyncClientWrapper = builder.buildAsyncClient();
    }

    /**
     * Create a Cosmos database if it does not already exist on the service.
     *
     * @param databaseProperties {@link CosmosDatabaseProperties} the database properties.
     * @return the {@link CosmosDatabaseResponse} with the created database.
     */
    CosmosDatabaseResponse createDatabaseIfNotExists(CosmosDatabaseProperties databaseProperties) {
        return blockDatabaseResponse(asyncClientWrapper.createDatabaseIfNotExists(databaseProperties));
    }

    /**
     * Create a Cosmos database if it does not already exist on the service.
     * <p>
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
     *
     * @param id the id of the database.
     * @return the {@link CosmosDatabaseResponse} with the created database.
     */
    public CosmosDatabaseResponse createDatabaseIfNotExists(String id) {
        return blockDatabaseResponse(asyncClientWrapper.createDatabaseIfNotExists(id));
    }

    /**
     * Creates a database.
     *
     * @param databaseProperties {@link CosmosDatabaseProperties} the database properties.
     * @param options the request options.
     * @return the {@link CosmosDatabaseResponse} with the created database.
     */
    public CosmosDatabaseResponse createDatabase(CosmosDatabaseProperties databaseProperties,
                                                 CosmosDatabaseRequestOptions options) {
        return blockDatabaseResponse(asyncClientWrapper.createDatabase(databaseProperties, options));
    }

    /**
     * Creates a Cosmos database.
     *
     * @param databaseProperties {@link CosmosDatabaseProperties} the database properties.
     * @return the {@link CosmosDatabaseResponse} with the created database.
     */
    public CosmosDatabaseResponse createDatabase(CosmosDatabaseProperties databaseProperties) {
        return blockDatabaseResponse(asyncClientWrapper.createDatabase(databaseProperties));
    }

    /**
     * Creates a Cosmos database.
     *
     * @param id the id of the database.
     * @return the {@link CosmosDatabaseResponse} with the created database.
     */
    public CosmosDatabaseResponse createDatabase(String id) {
        return blockDatabaseResponse(asyncClientWrapper.createDatabase(id));

    }

    /**
     * Creates a Cosmos database.
     *
     * @param databaseProperties {@link CosmosDatabaseProperties} the database properties.
     * @param throughputProperties the throughput properties.
     * @param options {@link CosmosDatabaseRequestOptions} the request options.
     * @return the {@link CosmosDatabaseResponse} with the created database.
     */
    public CosmosDatabaseResponse createDatabase(CosmosDatabaseProperties databaseProperties,
                                                 ThroughputProperties throughputProperties,
                                                 CosmosDatabaseRequestOptions options) {
        return blockDatabaseResponse(asyncClientWrapper.createDatabase(databaseProperties, throughputProperties, options));
    }

    /**
     * Creates a Cosmos database.
     *
     * @param databaseProperties {@link CosmosDatabaseProperties} the database properties.
     * @param throughputProperties the throughput properties.
     * @return the {@link CosmosDatabaseResponse} with the created database.
     */
    public CosmosDatabaseResponse createDatabase(CosmosDatabaseProperties databaseProperties,
                                                 ThroughputProperties throughputProperties) {
        return blockDatabaseResponse(asyncClientWrapper.createDatabase(databaseProperties, throughputProperties));
    }

    /**
     * Creates a Cosmos database.
     *
     * @param id the id of the database.
     * @param throughputProperties the throughput properties.
     * @return the {@link CosmosDatabaseResponse} with the created database.
     */
    public CosmosDatabaseResponse createDatabase(String id, ThroughputProperties throughputProperties) {
        return blockDatabaseResponse(asyncClientWrapper.createDatabase(id, throughputProperties));
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
     *
     * @param options {@link CosmosQueryRequestOptions}the feed options.
     * @return the {@link CosmosPagedIterable} for feed response with the read databases.
     */
    CosmosPagedIterable<CosmosDatabaseProperties> readAllDatabases(CosmosQueryRequestOptions options) {
        return getCosmosPagedIterable(asyncClientWrapper.readAllDatabases(options));
    }

    /**
     * Reads all Cosmos databases.
     *
     * @return the {@link CosmosPagedIterable} for feed response with the read databases.
     */
    public CosmosPagedIterable<CosmosDatabaseProperties> readAllDatabases() {
        return getCosmosPagedIterable(asyncClientWrapper.readAllDatabases());
    }

    /**
     * Query a Cosmos database.
     *
     * @param query the query.
     * @param options {@link CosmosQueryRequestOptions}the feed options.
     * @return the {@link CosmosPagedIterable} for feed response with the obtained databases.
     */
    public CosmosPagedIterable<CosmosDatabaseProperties> queryDatabases(String query, CosmosQueryRequestOptions options) {
        return getCosmosPagedIterable(asyncClientWrapper.queryDatabases(query, options));
    }

    /**
     * Query a Cosmos database.
     *
     * @param querySpec {@link SqlQuerySpec} the query spec.
     * @param options the query request options.
     * @return the {@link CosmosPagedIterable} for feed response with the obtained databases.
     */
    public CosmosPagedIterable<CosmosDatabaseProperties> queryDatabases(SqlQuerySpec querySpec,
                                                                        CosmosQueryRequestOptions options) {
        return getCosmosPagedIterable(asyncClientWrapper.queryDatabases(querySpec, options));
    }

    /**
     * Gets the Cosmos database client.
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
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public GlobalThroughputControlConfigBuilder createGlobalThroughputControlConfigBuilder(String databaseId, String containerId) {
        return new GlobalThroughputControlConfigBuilder(this.asyncClientWrapper, databaseId, containerId);
    }

    static {
        ImplementationBridgeHelpers.CosmosClientHelper.setCosmosClientAccessor(
            cosmosClient -> cosmosClient.asyncClient());
    }
}
