// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.sync;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosDatabaseProperties;
import com.azure.data.cosmos.CosmosDatabaseRequestOptions;
import com.azure.data.cosmos.CosmosDatabaseResponse;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.SqlQuerySpec;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.util.Iterator;

/**
 * Provides a client-side logical representation of the Azure Cosmos database service.
 * SyncClient is used to perform operations in a synchronous way
 */
public class CosmosSyncClient implements AutoCloseable {
    private final CosmosClient asyncClientWrapper;

    public CosmosSyncClient(CosmosClientBuilder builder) {
        this.asyncClientWrapper = builder.build();
    }
    
    /**
     * Instantiate the cosmos client builder to build cosmos client
     * @return {@link CosmosClientBuilder}
     */
    public static CosmosClientBuilder builder(){
        return new CosmosClientBuilder();
    }

    /**
     * Create a Database if it does not already exist on the service
     *
     * @param databaseProperties {@link CosmosDatabaseProperties} the database properties
     * @return the {@link CosmosSyncDatabaseResponse} with the created database.
     * @throws CosmosClientException the cosmos client exception.
     */
    public CosmosSyncDatabaseResponse createDatabaseIfNotExists(CosmosDatabaseProperties databaseProperties)
            throws CosmosClientException {
        return mapDatabaseResponseAndBlock(asyncClientWrapper.createDatabaseIfNotExists(databaseProperties));
    }

    /**
     * Create a Database if it does not already exist on the service
     *
     * @param id the id of the database
     * @return the {@link CosmosSyncDatabaseResponse} with the created database.
     * @throws CosmosClientException the cosmos client exception.
     */
    public CosmosSyncDatabaseResponse createDatabaseIfNotExists(String id) throws CosmosClientException {
        return mapDatabaseResponseAndBlock(asyncClientWrapper.createDatabaseIfNotExists(id));
    }


    /**
     * Creates a database.
     *
     * @param databaseProperties {@link CosmosDatabaseProperties} the database properties.
     * @param options the request options.
     * @return the {@link CosmosSyncDatabaseResponse} with the created database.
     * @throws CosmosClientException the cosmos client exception.
     */
    public CosmosSyncDatabaseResponse createDatabase(CosmosDatabaseProperties databaseProperties,
                                                     CosmosDatabaseRequestOptions options) throws CosmosClientException {
        return mapDatabaseResponseAndBlock(asyncClientWrapper.createDatabase(databaseProperties, options));
    }

    /**
     * Creates a database.
     *
     * @param databaseProperties {@link CosmosDatabaseProperties} the database properties.
     * @return the {@link CosmosSyncDatabaseResponse} with the created database.
     * @throws CosmosClientException the cosmos client exception.
     */
    public CosmosSyncDatabaseResponse createDatabase(CosmosDatabaseProperties databaseProperties) throws CosmosClientException {
        return mapDatabaseResponseAndBlock(asyncClientWrapper.createDatabase(databaseProperties));
    }

    /**
     * Creates a database.
     *
     * @param id the id of the database
     * @return the {@link CosmosSyncDatabaseResponse} with the created database.
     * @throws CosmosClientException the cosmos client exception.
     */
    public CosmosSyncDatabaseResponse createDatabase(String id) throws CosmosClientException {
        return mapDatabaseResponseAndBlock(asyncClientWrapper.createDatabase(id));

    }

    /**
     * Creates a database.
     *
     * @param databaseProperties {@link CosmosDatabaseProperties} the database properties.
     * @param throughput the throughput
     * @param options {@link CosmosDatabaseRequestOptions} the request options
     * @return the {@link CosmosSyncDatabaseResponse} with the created database.
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncDatabaseResponse createDatabase(CosmosDatabaseProperties databaseProperties,
                                                     int throughput,
                                                     CosmosDatabaseRequestOptions options) throws CosmosClientException {
        return mapDatabaseResponseAndBlock(asyncClientWrapper.createDatabase(databaseProperties, throughput, options));
    }

    /**
     * Creates a database.
     *
     * @param databaseProperties {@link CosmosDatabaseProperties} the database properties.
     * @param throughput the throughput
     * @return the {@link CosmosSyncDatabaseResponse} with the created database.
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncDatabaseResponse createDatabase(CosmosDatabaseProperties databaseProperties,
                                                     int throughput) throws CosmosClientException {
        return mapDatabaseResponseAndBlock(asyncClientWrapper.createDatabase(databaseProperties, throughput));
    }


    /**
     * Creates a database.
     *
     * @param id the id of the database
     * @param throughput the throughput
     * @return the {@link CosmosSyncDatabaseResponse} with the created database.
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncDatabaseResponse createDatabase(String id, int throughput) throws CosmosClientException {
        return mapDatabaseResponseAndBlock(asyncClientWrapper.createDatabase(id, throughput));
    }

    CosmosSyncDatabaseResponse mapDatabaseResponseAndBlock(Mono<CosmosDatabaseResponse> databaseMono)
            throws CosmosClientException {
        try {
            return databaseMono
                           .map(this::convertResponse)
                           .block();
        } catch (Exception ex) {
            final Throwable throwable = Exceptions.unwrap(ex);
            if (throwable instanceof CosmosClientException) {
                throw (CosmosClientException) throwable;
            } else {
                throw ex;
            }
        }
    }

    /**
     * Reads all databases.
     *
     * @param options {@link FeedOptions}the feed options.
     * @return the iterator for feed response with the read databases.
     */
    public Iterator<FeedResponse<CosmosDatabaseProperties>> readAllDatabases(FeedOptions options) {
        return asyncClientWrapper.readAllDatabases(options)
                       .toIterable()
                       .iterator();
    }

    /**
     * Reads all databases.
     *
     * @return the iterator for feed response with the read databases.
     */
    public Iterator<FeedResponse<CosmosDatabaseProperties>> readAllDatabases() {
        return asyncClientWrapper.readAllDatabases()
                       .toIterable()
                       .iterator();
    }

    /**
     * Query a database
     *
     * @param query the query
     * @param options {@link FeedOptions}the feed options.
     * @return the iterator for feed response with the obtained databases.
     */
    public Iterator<FeedResponse<CosmosDatabaseProperties>> queryDatabases(String query, FeedOptions options) {
        return asyncClientWrapper.queryDatabases(query, options)
                       .toIterable()
                       .iterator();
    }

    /**
     * Query a database
     *
     * @param querySpec {@link SqlQuerySpec} the query spec
     * @param options the query
     * @return the iterator for feed response with the obtained databases.
     */
    public Iterator<FeedResponse<CosmosDatabaseProperties>> queryDatabases(SqlQuerySpec querySpec, FeedOptions options) {
        return asyncClientWrapper.queryDatabases(querySpec, options)
                       .toIterable()
                       .iterator();
    }

    /**
     * Gets the database client
     *
     * @param id the id of the database
     * @return {@link CosmosSyncDatabase} the cosmos sync database
     */
    public CosmosSyncDatabase getDatabase(String id) {
        return new CosmosSyncDatabase(id, this, asyncClientWrapper.getDatabase(id));
    }

    CosmosSyncDatabaseResponse convertResponse(CosmosDatabaseResponse response) {
        return new CosmosSyncDatabaseResponse(response, this);
    }

    CosmosClient asyncClient() {
        return this.asyncClientWrapper;
    }

    /**
     * Close this {@link CosmosSyncClient} instance
     */
    public void close() {
        asyncClientWrapper.close();
    }

}
