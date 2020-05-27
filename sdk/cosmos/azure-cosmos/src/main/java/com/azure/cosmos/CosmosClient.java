// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.core.annotation.ServiceClient;
import com.azure.cosmos.models.CosmosAsyncDatabaseResponse;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosDatabaseRequestOptions;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.FeedOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.azure.cosmos.util.UtilBridgeInternal;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.Closeable;

/**
 * Provides a client-side logical representation of the Azure Cosmos DB service.
 * SyncClient is used to perform operations in a synchronous way
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
    public CosmosDatabaseResponse createDatabaseIfNotExists(CosmosDatabaseProperties databaseProperties) {
        return mapDatabaseResponseAndBlock(asyncClientWrapper.createDatabaseIfNotExists(databaseProperties));
    }

    /**
     * Create a Database if it does not already exist on the service.
     *
     * @param id the id of the database.
     * @param throughputProperties the throughputProperties.
     * @return the {@link CosmosDatabaseResponse} with the created database.
     */
    public CosmosDatabaseResponse createDatabaseIfNotExists(String id, ThroughputProperties throughputProperties) {
        return mapDatabaseResponseAndBlock(asyncClientWrapper.createDatabaseIfNotExists(id, throughputProperties));
    }

    /**
     * Create a Database if it does not already exist on the service.
     *
     * @param id the id of the database.
     * @return the {@link CosmosDatabaseResponse} with the created database.
     */
    public CosmosDatabaseResponse createDatabaseIfNotExists(String id) {
        return mapDatabaseResponseAndBlock(asyncClientWrapper.createDatabaseIfNotExists(id));
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
        return mapDatabaseResponseAndBlock(asyncClientWrapper.createDatabase(databaseProperties, options));
    }

    /**
     * Creates a database.
     *
     * @param databaseProperties {@link CosmosDatabaseProperties} the database properties.
     * @return the {@link CosmosDatabaseResponse} with the created database.
     */
    public CosmosDatabaseResponse createDatabase(CosmosDatabaseProperties databaseProperties) {
        return mapDatabaseResponseAndBlock(asyncClientWrapper.createDatabase(databaseProperties));
    }

    /**
     * Creates a database.
     *
     * @param id the id of the database.
     * @return the {@link CosmosDatabaseResponse} with the created database.
     */
    public CosmosDatabaseResponse createDatabase(String id) {
        return mapDatabaseResponseAndBlock(asyncClientWrapper.createDatabase(id));

    }

    /**
     * Creates a database.
     *
     * @param databaseProperties {@link CosmosDatabaseProperties} the database properties.
     * @param throughputProperties the throughput properties.
     * @param options {@link CosmosDatabaseRequestOptions} the request options.
     * @return the {@link CosmosDatabaseResponse} with the created database.
     */
    public CosmosDatabaseResponse createDatabase(CosmosDatabaseProperties databaseProperties,
                                                 ThroughputProperties throughputProperties,
                                                 CosmosDatabaseRequestOptions options) {
        return mapDatabaseResponseAndBlock(asyncClientWrapper.createDatabase(databaseProperties, throughputProperties, options));
    }

    /**
     * Creates a database.
     *
     * @param databaseProperties {@link CosmosDatabaseProperties} the database properties.
     * @param throughputProperties the throughput properties.
     * @return the {@link CosmosDatabaseResponse} with the created database.
     */
    public CosmosDatabaseResponse createDatabase(CosmosDatabaseProperties databaseProperties,
                                                 ThroughputProperties throughputProperties) {
        return mapDatabaseResponseAndBlock(asyncClientWrapper.createDatabase(databaseProperties, throughputProperties));
    }

    /**
     * Creates a database.
     *
     * @param id the id of the database.
     * @param throughputProperties the throughput properties.
     * @return the {@link CosmosDatabaseResponse} with the created database.
     */
    public CosmosDatabaseResponse createDatabase(String id, ThroughputProperties throughputProperties) {
        return mapDatabaseResponseAndBlock(asyncClientWrapper.createDatabase(id, throughputProperties));
    }

    CosmosDatabaseResponse mapDatabaseResponseAndBlock(Mono<CosmosAsyncDatabaseResponse> databaseMono) {
        try {
            return databaseMono
                       .map(this::convertResponse)
                       .block();
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
     * Reads all databases.
     *
     * @param options {@link FeedOptions}the feed options.
     * @return the {@link CosmosPagedIterable} for feed response with the read databases.
     */
    public CosmosPagedIterable<CosmosDatabaseProperties> readAllDatabases(FeedOptions options) {
        return getCosmosPagedIterable(asyncClientWrapper.readAllDatabases(options));
    }

    /**
     * Reads all databases.
     *
     * @return the {@link CosmosPagedIterable} for feed response with the read databases.
     */
    public CosmosPagedIterable<CosmosDatabaseProperties> readAllDatabases() {
        return getCosmosPagedIterable(asyncClientWrapper.readAllDatabases());
    }

    /**
     * Query a database.
     *
     * @param query the query.
     * @param options {@link FeedOptions}the feed options.
     * @return the {@link CosmosPagedIterable} for feed response with the obtained databases.
     */
    public CosmosPagedIterable<CosmosDatabaseProperties> queryDatabases(String query, FeedOptions options) {
        return getCosmosPagedIterable(asyncClientWrapper.queryDatabases(query, options));
    }

    /**
     * Query a database.
     *
     * @param querySpec {@link SqlQuerySpec} the query spec.
     * @param options the query.
     * @return the {@link CosmosPagedIterable} for feed response with the obtained databases.
     */
    public CosmosPagedIterable<CosmosDatabaseProperties> queryDatabases(SqlQuerySpec querySpec,
                                                                        FeedOptions options) {
        return getCosmosPagedIterable(asyncClientWrapper.queryDatabases(querySpec, options));
    }

    /**
     * Gets the database client.
     *
     * @param id the id of the database.
     * @return {@link CosmosDatabase} the cosmos sync database.
     */
    public CosmosDatabase getDatabase(String id) {
        return new CosmosDatabase(id, this, asyncClientWrapper.getDatabase(id));
    }

    CosmosDatabaseResponse convertResponse(CosmosAsyncDatabaseResponse response) {
        return ModelBridgeInternal.createCosmosDatabaseResponse(response, this);
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
        return UtilBridgeInternal.createCosmosPagedIterable(cosmosPagedFlux);
    }

}
