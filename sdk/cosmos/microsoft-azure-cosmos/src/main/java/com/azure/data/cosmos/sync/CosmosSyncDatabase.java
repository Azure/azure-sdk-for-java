// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.sync;

import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosContainerProperties;
import com.azure.data.cosmos.CosmosContainerRequestOptions;
import com.azure.data.cosmos.CosmosContainerResponse;
import com.azure.data.cosmos.CosmosDatabase;
import com.azure.data.cosmos.CosmosDatabaseRequestOptions;
import com.azure.data.cosmos.CosmosUserProperties;
import com.azure.data.cosmos.CosmosUserResponse;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.SqlQuerySpec;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Iterator;

/**
 * Perform read and delete databases, update database throughput, and perform operations on child resources in
 * a synchronous way
 */
public class CosmosSyncDatabase {

    private final CosmosDatabase databaseWrapper;
    private final CosmosSyncClient client;
    private final String id;

    /**
     * Instantiates a new Cosmos sync database.
     *
     * @param id the id
     * @param client the client
     * @param database the database
     */
    CosmosSyncDatabase(String id, CosmosSyncClient client, CosmosDatabase database) {
        this.id = id;
        this.client = client;
        this.databaseWrapper = database;
    }

    /**
     * Get the id of the CosmosDatabase
     *
     * @return the id of the database
     */
    public String id() {
        return id;
    }

    /**
     * Reads a database
     *
     * @return the {@link CosmosSyncDatabaseResponse}
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncDatabaseResponse read() throws CosmosClientException {
        return client.mapDatabaseResponseAndBlock((databaseWrapper.read()));
    }

    /**
     * Reads a database.
     *
     * @param options the {@link CosmosDatabaseRequestOptions} request options.
     * @return the {@link CosmosSyncDatabaseResponse}
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncDatabaseResponse read(CosmosDatabaseRequestOptions options) throws CosmosClientException {
        return client.mapDatabaseResponseAndBlock(databaseWrapper.read(options));
    }

    /**
     * Delete a database.
     *
     * @return the {@link CosmosSyncDatabaseResponse}
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncDatabaseResponse delete() throws CosmosClientException {
        return client.mapDatabaseResponseAndBlock(databaseWrapper.delete());
    }

    /**
     * Delete a database.
     *
     * @param options the {@link CosmosDatabaseRequestOptions} request options.
     * @return the {@link CosmosSyncDatabaseResponse}
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncDatabaseResponse delete(CosmosDatabaseRequestOptions options) throws CosmosClientException {
        return client.mapDatabaseResponseAndBlock(databaseWrapper.delete(options));
    }

    /* CosmosContainer operations */

    /**
     * Creates a cosmos container.
     *
     * @param containerProperties the {@link CosmosContainerProperties}
     * @return the {@link CosmosSyncContainerResponse} with the created container.
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncContainerResponse createContainer(CosmosContainerProperties containerProperties) throws CosmosClientException {
        return this.mapContainerResponseAndBlock(databaseWrapper.createContainer(containerProperties));
    }

    /**
     * Creates a cosmos container.
     *
     * @param containerProperties the {@link CosmosContainerProperties}
     * @param throughput the throughput
     * @return the {@link CosmosSyncContainerResponse} with the created container.
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncContainerResponse createContainer(CosmosContainerProperties containerProperties,
                                                       int throughput) throws CosmosClientException {
        return this.mapContainerResponseAndBlock(databaseWrapper.createContainer(containerProperties, throughput));
    }

    /**
     * Creates a cosmos container.
     *
     * @param containerProperties the {@link CosmosContainerProperties}
     * @param options the {@link CosmosContainerProperties}
     * @return the {@link CosmosSyncContainerResponse} with the created container.
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncContainerResponse createContainer(CosmosContainerProperties containerProperties,
                                                       CosmosContainerRequestOptions options) throws CosmosClientException {
        return this.mapContainerResponseAndBlock(databaseWrapper.createContainer(containerProperties, options));
    }

    /**
     * Creates a cosmos container.
     *
     * @param containerProperties the {@link CosmosContainerProperties}
     * @param throughput the throughput
     * @param options the {@link CosmosContainerProperties}
     * @return the {@link CosmosSyncContainerResponse} with the created container.
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncContainerResponse createContainer(CosmosContainerProperties containerProperties,
                                                       int throughput,
                                                       CosmosContainerRequestOptions options) throws CosmosClientException {
        return this.mapContainerResponseAndBlock(databaseWrapper.createContainer(containerProperties,
                throughput,
                options));
    }

    /**
     * Create container cosmos sync container response.
     *
     * @param id the id
     * @param partitionKeyPath the partition key path
     * @return the cosmos sync container response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncContainerResponse createContainer(String id, String partitionKeyPath) throws CosmosClientException {
        return this.mapContainerResponseAndBlock(databaseWrapper.createContainer(id, partitionKeyPath));
    }

    /**
     * Create container cosmos sync container response.
     *
     * @param id the id
     * @param partitionKeyPath the partition key path
     * @param throughput the throughput
     * @return the cosmos sync container response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncContainerResponse createContainer(String id, String partitionKeyPath, int throughput) throws CosmosClientException {
        return this.mapContainerResponseAndBlock(databaseWrapper.createContainer(id, partitionKeyPath, throughput));
    }

    /**
     * Create container if not exists cosmos sync container response.
     *
     * @param containerProperties the container properties
     * @return the cosmos sync container response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncContainerResponse createContainerIfNotExists(CosmosContainerProperties containerProperties)
            throws CosmosClientException {
        return this.mapContainerResponseAndBlock(databaseWrapper.createContainerIfNotExists(containerProperties));
    }

    /**
     * Create container if not exists cosmos sync container response.
     *
     * @param containerProperties the container properties
     * @param throughput the throughput
     * @return the cosmos sync container response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncContainerResponse createContainerIfNotExists(CosmosContainerProperties containerProperties,
                                                                  int throughput) throws CosmosClientException {
        return this.mapContainerResponseAndBlock(databaseWrapper.createContainerIfNotExists(containerProperties, throughput));
    }

    /**
     * Create container if not exists cosmos sync container response.
     *
     * @param id the id
     * @param partitionKeyPath the partition key path
     * @return the cosmos sync container response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncContainerResponse createContainerIfNotExists(String id,
                                                                  String partitionKeyPath) throws CosmosClientException {
        return this.mapContainerResponseAndBlock(databaseWrapper.createContainerIfNotExists(id, partitionKeyPath));
    }

    /**
     * Create container if not exists cosmos sync container response.
     *
     * @param id the id
     * @param partitionKeyPath the partition key path
     * @param throughput the throughput
     * @return the cosmos sync container response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncContainerResponse createContainerIfNotExists(String id, String partitionKeyPath,
                                                                  int throughput) throws CosmosClientException {
        return this.mapContainerResponseAndBlock(databaseWrapper.createContainerIfNotExists(id,
                partitionKeyPath,
                throughput));
    }

    /**
     * Map container response and block cosmos sync container response.
     *
     * @param containerMono the container mono
     * @return the cosmos sync container response
     * @throws CosmosClientException the cosmos client exception
     */
    CosmosSyncContainerResponse mapContainerResponseAndBlock(Mono<CosmosContainerResponse> containerMono)
            throws CosmosClientException {
        try {
            return containerMono
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
     * Read all containers iterator.
     *
     * @param options the options
     * @return the iterator
     */
    public Iterator<FeedResponse<CosmosContainerProperties>> readAllContainers(FeedOptions options) {
        return databaseWrapper.readAllContainers(options)
                       .toIterable()
                       .iterator();
    }

    /**
     * Read all containers iterator.
     *
     * @return the iterator
     */
    public Iterator<FeedResponse<CosmosContainerProperties>> readAllContainers() {
        return databaseWrapper.readAllContainers()
                       .toIterable()
                       .iterator();
    }

    /**
     * Query containers iterator.
     *
     * @param query the query
     * @return the iterator
     */
    public Iterator<FeedResponse<CosmosContainerProperties>> queryContainers(String query) {
        return databaseWrapper.queryContainers(query)
                       .toIterable()
                       .iterator();
    }

    /**
     * Query containers iterator.
     *
     * @param query the query
     * @param options the options
     * @return the iterator
     */
    public Iterator<FeedResponse<CosmosContainerProperties>> queryContainers(String query, FeedOptions options) {
        return databaseWrapper.queryContainers(query, options)
                       .toIterable()
                       .iterator();
    }

    /**
     * Query containers iterator.
     *
     * @param querySpec the query spec
     * @return the iterator
     */
    public Iterator<FeedResponse<CosmosContainerProperties>> queryContainers(SqlQuerySpec querySpec) {
        return databaseWrapper.queryContainers(querySpec)
                       .toIterable()
                       .iterator();
    }

    /**
     * Query containers iterator.
     *
     * @param querySpec the query spec
     * @param options the options
     * @return the iterator
     */
    public Iterator<FeedResponse<CosmosContainerProperties>> queryContainers(SqlQuerySpec querySpec, FeedOptions options) {
        return databaseWrapper.queryContainers(querySpec, options)
                       .toIterable()
                       .iterator();
    }

    /**
     * Gets a CosmosSyncContainer object without making a service call
     *
     * @param id id of the container
     * @return Cosmos Container
     */
    public CosmosSyncContainer getContainer(String id) {
        return new CosmosSyncContainer(id, this, databaseWrapper.getContainer(id));
    }

    /**
     * Convert response cosmos sync container response.
     *
     * @param response the response
     * @return the cosmos sync container response
     */
    CosmosSyncContainerResponse convertResponse(CosmosContainerResponse response) {
        return new CosmosSyncContainerResponse(response, this, client);
    }

    /* Users */

    /**
     * Create user cosmos sync user response.
     *
     * @param settings the settings
     * @return the cosmos sync user response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncUserResponse createUser(CosmosUserProperties settings) throws CosmosClientException {
        return mapUserResponseAndBlock(databaseWrapper.createUser(settings));
    }

    /**
     * Upsert user cosmos sync user response.
     *
     * @param settings the settings
     * @return the cosmos sync user response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncUserResponse upsertUser(CosmosUserProperties settings) throws CosmosClientException {
        return mapUserResponseAndBlock(databaseWrapper.upsertUser(settings));
    }

    /**
     * Read all users iterator.
     *
     * @return the iterator
     */
    public Iterator<FeedResponse<CosmosUserProperties>> readAllUsers() {
        return getFeedIterator(databaseWrapper.readAllUsers());
    }

    /**
     * Read all users iterator.
     *
     * @param options the options
     * @return the iterator
     */
    public Iterator<FeedResponse<CosmosUserProperties>> readAllUsers(FeedOptions options) {
        return getFeedIterator(databaseWrapper.readAllUsers(options));
    }

    /**
     * Query users iterator.
     *
     * @param query the query
     * @return the iterator
     */
    public Iterator<FeedResponse<CosmosUserProperties>> queryUsers(String query) {
        return getFeedIterator(databaseWrapper.queryUsers(query));
    }

    /**
     * Query users iterator.
     *
     * @param query the query
     * @param options the options
     * @return the iterator
     */
    public Iterator<FeedResponse<CosmosUserProperties>> queryUsers(String query, FeedOptions options) {
        return getFeedIterator(databaseWrapper.queryUsers(query, options));
    }

    /**
     * Query users iterator.
     *
     * @param querySpec the query spec
     * @return the iterator
     */
    public Iterator<FeedResponse<CosmosUserProperties>> queryUsers(SqlQuerySpec querySpec) {
        return getFeedIterator(databaseWrapper.queryUsers(querySpec));
    }

    /**
     * Query users iterator.
     *
     * @param querySpec the query spec
     * @param options the options
     * @return the iterator
     */
    public Iterator<FeedResponse<CosmosUserProperties>> queryUsers(SqlQuerySpec querySpec, FeedOptions options) {
        return getFeedIterator(databaseWrapper.queryUsers(querySpec, options));
    }

    /**
     * Gets user.
     *
     * @param id the id
     * @return the user
     */
    public CosmosSyncUser getUser(String id) {
        return new CosmosSyncUser(databaseWrapper.getUser(id), this, id);
    }

    CosmosSyncUserResponse mapUserResponseAndBlock(Mono<CosmosUserResponse> containerMono)
            throws CosmosClientException {
        try {
            return containerMono.map(this::convertUserResponse).block();
        } catch (Exception ex) {
            final Throwable throwable = Exceptions.unwrap(ex);
            if (throwable instanceof CosmosClientException) {
                throw (CosmosClientException) throwable;
            } else {
                throw ex;
            }
        }
    }

    private CosmosSyncUserResponse convertUserResponse(CosmosUserResponse response) {
        return new CosmosSyncUserResponse(response, this);
    }

    /**
     * Read provisioned throughput integer.
     *
     * @return the integer. null response indicates database doesn't have any provisioned RUs
     * @throws CosmosClientException the cosmos client exception
     */
    public Integer readProvisionedThroughput() throws CosmosClientException {
        return throughputResponseToBlock(databaseWrapper.readProvisionedThroughput());
    }

    /**
     * Replace provisioned throughput integer.
     *
     * @param requestUnitsPerSecond the request units per second
     * @return the integer
     * @throws CosmosClientException the cosmos client exception
     */
    public Integer replaceProvisionedThroughput(int requestUnitsPerSecond) throws CosmosClientException {
        return throughputResponseToBlock(databaseWrapper.replaceProvisionedThroughput(requestUnitsPerSecond));
    }

    static Integer throughputResponseToBlock(Mono<Integer> throughputResponse) throws CosmosClientException {
        try {
            return throughputResponse.block();
        } catch (Exception ex) {
            final Throwable throwable = Exceptions.unwrap(ex);
            if (throwable instanceof CosmosClientException) {
                throw (CosmosClientException) throwable;
            } else {
                throw ex;
            }
        }
    }

    private <T> Iterator<FeedResponse<T>> getFeedIterator(Flux<FeedResponse<T>> itemFlux) {
        return itemFlux.toIterable(1).iterator();
    }

}
