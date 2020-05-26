// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosAsyncContainerResponse;
import com.azure.cosmos.models.CosmosAsyncUserResponse;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseRequestOptions;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosUserProperties;
import com.azure.cosmos.models.CosmosUserResponse;
import com.azure.cosmos.models.FeedOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.ThroughputResponse;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.azure.cosmos.util.UtilBridgeInternal;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

/**
 * Perform read and delete databases, update database throughput, and perform operations on child resources in
 * a synchronous way
 */
public class CosmosDatabase {

    private final CosmosAsyncDatabase databaseWrapper;
    private final CosmosClient client;
    private final String id;

    /**
     * Instantiates a new Cosmos sync database.
     *
     * @param id the id.
     * @param client the client.
     * @param database the database.
     */
    CosmosDatabase(String id, CosmosClient client, CosmosAsyncDatabase database) {
        this.id = id;
        this.client = client;
        this.databaseWrapper = database;
    }

    /**
     * Get the id of the CosmosAsyncDatabase
     *
     * @return the id of the database.
     */
    public String getId() {
        return id;
    }

    /**
     * Reads a database.
     *
     * @return the {@link CosmosDatabaseResponse}.
     */
    public CosmosDatabaseResponse read() {
        return client.mapDatabaseResponseAndBlock((databaseWrapper.read()));
    }

    /**
     * Reads a database.
     *
     * @param options the {@link CosmosDatabaseRequestOptions} request options.
     * @return the {@link CosmosDatabaseResponse}
     */
    public CosmosDatabaseResponse read(CosmosDatabaseRequestOptions options) {
        return client.mapDatabaseResponseAndBlock(databaseWrapper.read(options));
    }

    /**
     * Delete a database.
     *
     * @return the {@link CosmosDatabaseResponse}.
     */
    public CosmosDatabaseResponse delete() {
        return client.mapDatabaseResponseAndBlock(databaseWrapper.delete());
    }

    /**
     * Delete a database.
     *
     * @param options the {@link CosmosDatabaseRequestOptions} request options.
     * @return the {@link CosmosDatabaseResponse}.
     */
    public CosmosDatabaseResponse delete(CosmosDatabaseRequestOptions options) {
        return client.mapDatabaseResponseAndBlock(databaseWrapper.delete(options));
    }

    /* CosmosAsyncContainer operations */

    /**
     * Creates a cosmos container.
     *
     * @param containerProperties the {@link CosmosContainerProperties}.
     * @return the {@link CosmosContainerResponse} with the created container.
     */
    public CosmosContainerResponse createContainer(CosmosContainerProperties containerProperties) {
        return this.mapContainerResponseAndBlock(databaseWrapper.createContainer(containerProperties));
    }

    /**
     * Creates a cosmos container.
     *
     * @param containerProperties the {@link CosmosContainerProperties}.
     * @param throughput the throughput.
     * @return the {@link CosmosContainerResponse} with the created container.
     */
    public CosmosContainerResponse createContainer(
        CosmosContainerProperties containerProperties,
        int throughput) {
        return this.mapContainerResponseAndBlock(databaseWrapper.createContainer(containerProperties, throughput));
    }

    /**
     * Creates a cosmos container.
     *
     * @param containerProperties the {@link CosmosContainerProperties}.
     * @param options the {@link CosmosContainerProperties}.
     * @return the {@link CosmosContainerResponse} with the created container.
     */
    public CosmosContainerResponse createContainer(
        CosmosContainerProperties containerProperties,
        CosmosContainerRequestOptions options) {
        return this.mapContainerResponseAndBlock(databaseWrapper.createContainer(containerProperties, options));
    }

    /**
     * Creates a cosmos container.
     *
     * @param containerProperties the {@link CosmosContainerProperties}.
     * @param throughput the throughput.
     * @param options the {@link CosmosContainerProperties}.
     * @return the {@link CosmosContainerResponse} with the created container.
     */
    public CosmosContainerResponse createContainer(
        CosmosContainerProperties containerProperties,
        int throughput,
        CosmosContainerRequestOptions options) {
        return this.mapContainerResponseAndBlock(databaseWrapper.createContainer(containerProperties,
                                                                                 throughput,
                                                                                 options));
    }

    /**
     * Creates a cosmos container.
     *
     * @param containerProperties the container properties.
     * @param throughputProperties the throughput properties.
     * @param options the options.
     * @return the cosmos container response.
     */
    public CosmosContainerResponse createContainer(
        CosmosContainerProperties containerProperties,
        ThroughputProperties throughputProperties,
        CosmosContainerRequestOptions options) {
        return this.mapContainerResponseAndBlock(databaseWrapper.createContainer(containerProperties,
                                                                                 throughputProperties,
                                                                                 options));
    }

    /**
     * Create container cosmos sync container response.
     *
     * @param id the id.
     * @param partitionKeyPath the partition key path.
     * @return the cosmos sync container response.
     */
    public CosmosContainerResponse createContainer(String id, String partitionKeyPath) {
        return this.mapContainerResponseAndBlock(databaseWrapper.createContainer(id, partitionKeyPath));
    }

    /**
     * Create container cosmos sync container response.
     *
     * @param id the id.
     * @param partitionKeyPath the partition key path.
     * @param throughput the throughput.
     * @return the cosmos sync container response.
     */
    public CosmosContainerResponse createContainer(String id, String partitionKeyPath, int throughput) {
        return this.mapContainerResponseAndBlock(databaseWrapper.createContainer(id, partitionKeyPath, throughput));
    }

    /**
     * Create container if not exists cosmos sync container response.
     *
     * @param containerProperties the container properties.
     * @return the cosmos sync container response.
     */
    public CosmosContainerResponse createContainerIfNotExists(CosmosContainerProperties containerProperties) {
        return this.mapContainerResponseAndBlock(databaseWrapper.createContainerIfNotExists(containerProperties));
    }

    /**
     * Create container if not exists cosmos sync container response.
     *
     * @param containerProperties the container properties.
     * @param throughput the throughput.
     * @return the cosmos sync container response.
     */
    public CosmosContainerResponse createContainerIfNotExists(
        CosmosContainerProperties containerProperties,
        int throughput) {
        return this.mapContainerResponseAndBlock(databaseWrapper.createContainerIfNotExists(containerProperties,
            throughput));
    }

    /**
     * Create container if not exists cosmos sync container response.
     * <p>
     * The throughput properties will only be used if the specified container
     * does not exist and therefor a new container will be created.
     *
     * @param containerProperties the container properties.
     * @param throughputProperties the throughput properties for the container.
     * @return the cosmos sync container response.
     */
    public CosmosContainerResponse createContainerIfNotExists(
        CosmosContainerProperties containerProperties,
        ThroughputProperties throughputProperties) {
        return this.mapContainerResponseAndBlock(databaseWrapper.createContainerIfNotExists(containerProperties,
            throughputProperties));
    }

    /**
     * Create container if not exists cosmos sync container response.
     *
     * @param id the id.
     * @param partitionKeyPath the partition key path.
     * @return the cosmos sync container response.
     */
    public CosmosContainerResponse createContainerIfNotExists(
        String id,
        String partitionKeyPath) {
        return this.mapContainerResponseAndBlock(databaseWrapper.createContainerIfNotExists(id, partitionKeyPath));
    }

    /**
     * Create container if not exists cosmos sync container response.
     * <p>
     * The throughput settings will only be used if the specified container
     * does not exist and therefor a new container will be created.
     *
     * @param id the id.
     * @param partitionKeyPath the partition key path.
     * @param throughput the throughput.
     * @return the cosmos sync container response.
     */
    public CosmosContainerResponse createContainerIfNotExists(
        String id, String partitionKeyPath,
        int throughput) {
        return this.mapContainerResponseAndBlock(databaseWrapper.createContainerIfNotExists(id,
            partitionKeyPath,
            throughput));
    }

    /**
     * Create container if not exists cosmos sync container response.
     * <p>
     * The throughput properties will only be used if the specified container
     * does not exist and therefor a new container will be created.
     *
     * @param id the id.
     * @param partitionKeyPath the partition key path.
     * @param throughputProperties the throughput properties for the container.
     * @return the cosmos sync container response.
     */
    public CosmosContainerResponse createContainerIfNotExists(
        String id, String partitionKeyPath,
        ThroughputProperties throughputProperties) {
        return this.mapContainerResponseAndBlock(databaseWrapper.createContainerIfNotExists(id,
            partitionKeyPath,
            throughputProperties));
    }

    /**
     * Map container response and block cosmos sync container response.
     *
     * @param containerMono the container mono.
     * @return the cosmos sync container response.
     */
    CosmosContainerResponse mapContainerResponseAndBlock(Mono<CosmosAsyncContainerResponse> containerMono) {
        try {
            return containerMono
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
     * Read all containers iterator.
     *
     * @param options the options.
     * @return the {@link CosmosPagedIterable}.
     */
    public CosmosPagedIterable<CosmosContainerProperties> readAllContainers(FeedOptions options) {
        return getCosmosPagedIterable(databaseWrapper.readAllContainers(options));
    }

    /**
     * Read all containers iterator.
     *
     @return the {@link CosmosPagedIterable}.
     */
    public CosmosPagedIterable<CosmosContainerProperties> readAllContainers() {
        return getCosmosPagedIterable(databaseWrapper.readAllContainers());
    }

    /**
     * Query containers iterator.
     *
     * @param query the query.
     * @return the {@link CosmosPagedIterable}.
     */
    public CosmosPagedIterable<CosmosContainerProperties> queryContainers(String query) {
        return getCosmosPagedIterable(databaseWrapper.queryContainers(query));
    }

    /**
     * Query containers iterator.
     *
     * @param query the query.
     * @param options the options.
     * @return the {@link CosmosPagedIterable}.
     */
    public CosmosPagedIterable<CosmosContainerProperties> queryContainers(String query, FeedOptions options) {
        return getCosmosPagedIterable(databaseWrapper.queryContainers(query, options));
    }

    /**
     * Query containers iterator.
     *
     * @param querySpec the query spec.
     * @return the {@link CosmosPagedIterable}.
     */
    public CosmosPagedIterable<CosmosContainerProperties> queryContainers(SqlQuerySpec querySpec) {
        return getCosmosPagedIterable(databaseWrapper.queryContainers(querySpec));
    }

    /**
     * Query containers iterator.
     *
     * @param querySpec the query spec.
     * @param options the options.
     * @return the {@link CosmosPagedIterable}.
     */
    public CosmosPagedIterable<CosmosContainerProperties> queryContainers(
        SqlQuerySpec querySpec,
        FeedOptions options) {
        return getCosmosPagedIterable(databaseWrapper.queryContainers(querySpec, options));
    }

    /**
     * Gets a CosmosContainer object without making a service call.
     *
     * @param id id of the container.
     * @return Cosmos Container.
     */
    public CosmosContainer getContainer(String id) {
        return new CosmosContainer(id, this, databaseWrapper.getContainer(id));
    }

    /**
     * Convert response cosmos sync container response.
     *
     * @param response the response.
     * @return the cosmos sync container response.
     */
    CosmosContainerResponse convertResponse(CosmosAsyncContainerResponse response) {
        return ModelBridgeInternal.createCosmosContainerResponse(response, this, client);
    }

    /* Users */

    /**
     * Create user cosmos sync user response.
     *
     * @param userProperties the settings.
     * @return the cosmos sync user response.
     */
    public CosmosUserResponse createUser(CosmosUserProperties userProperties) {
        return mapUserResponseAndBlock(databaseWrapper.createUser(userProperties));
    }

    /**
     * Upsert user cosmos sync user response.
     *
     * @param userProperties the settings.
     * @return the cosmos sync user response.
     */
    public CosmosUserResponse upsertUser(CosmosUserProperties userProperties) {
        return mapUserResponseAndBlock(databaseWrapper.upsertUser(userProperties));
    }

    /**
     * Read all users {@link CosmosPagedIterable}.
     *
     * @return the {@link CosmosPagedIterable}.
     */
    public CosmosPagedIterable<CosmosUserProperties> readAllUsers() {
        return getCosmosPagedIterable(databaseWrapper.readAllUsers());
    }

    /**
     * Read all users {@link CosmosPagedIterable}.
     *
     * @param options the options.
     * @return the {@link CosmosPagedIterable}.
     */
    public CosmosPagedIterable<CosmosUserProperties> readAllUsers(FeedOptions options) {
        return getCosmosPagedIterable(databaseWrapper.readAllUsers(options));
    }

    /**
     * Query users {@link CosmosPagedIterable}.
     *
     * @param query the query.
     * @return the {@link CosmosPagedIterable}.
     */
    public CosmosPagedIterable<CosmosUserProperties> queryUsers(String query) {
        return getCosmosPagedIterable(databaseWrapper.queryUsers(query));
    }

    /**
     * Query users {@link CosmosPagedIterable}.
     *
     * @param query the query.
     * @param options the options.
     * @return the {@link CosmosPagedIterable}.
     */
    public CosmosPagedIterable<CosmosUserProperties> queryUsers(String query, FeedOptions options) {
        return getCosmosPagedIterable(databaseWrapper.queryUsers(query, options));
    }

    /**
     * Query users {@link CosmosPagedIterable}.
     *
     * @param querySpec the query spec.
     * @return the {@link CosmosPagedIterable}.
     */
    public CosmosPagedIterable<CosmosUserProperties> queryUsers(SqlQuerySpec querySpec) {
        return getCosmosPagedIterable(databaseWrapper.queryUsers(querySpec));
    }

    /**
     * Query users {@link CosmosPagedIterable}.
     *
     * @param querySpec the query spec.
     * @param options the options.
     * @return the {@link CosmosPagedIterable}.
     */
    public CosmosPagedIterable<CosmosUserProperties> queryUsers(SqlQuerySpec querySpec, FeedOptions options) {
        return getCosmosPagedIterable(databaseWrapper.queryUsers(querySpec, options));
    }

    /**
     * Gets user.
     *
     * @param id the id.
     * @return the user.
     */
    public CosmosUser getUser(String id) {
        return new CosmosUser(databaseWrapper.getUser(id), this, id);
    }

    CosmosUserResponse mapUserResponseAndBlock(Mono<CosmosAsyncUserResponse> containerMono) {
        try {
            return containerMono.map(this::convertUserResponse).block();
        } catch (Exception ex) {
            final Throwable throwable = Exceptions.unwrap(ex);
            if (throwable instanceof CosmosException) {
                throw (CosmosException) throwable;
            } else {
                throw Exceptions.propagate(ex);
            }
        }
    }

    private CosmosUserResponse convertUserResponse(CosmosAsyncUserResponse response) {
        return ModelBridgeInternal.createCosmosUserResponse(response, this);
    }

    /**
     * Sets the throughput.
     *
     * @param throughputProperties the throughput properties.
     * @return the throughput response.
     */
    public ThroughputResponse replaceThroughput(ThroughputProperties throughputProperties) {
        return throughputResponseToBlock(databaseWrapper.replaceThroughput(throughputProperties));
    }

    /**
     * Gets the throughput of the database.
     *
     * @return the throughput response.
     */
    public ThroughputResponse readThroughput() {
        return throughputResponseToBlock(databaseWrapper.readThroughput());
    }

    <T> T throughputResponseToBlock(Mono<T> throughputResponse) {
        try {
            return throughputResponse.block();
        } catch (Exception ex) {
            final Throwable throwable = Exceptions.unwrap(ex);
            if (throwable instanceof CosmosException) {
                throw (CosmosException) throwable;
            } else {
                throw Exceptions.propagate(ex);
            }
        }
    }

    private <T> CosmosPagedIterable<T> getCosmosPagedIterable(CosmosPagedFlux<T> cosmosPagedFlux) {
        return UtilBridgeInternal.createCosmosPagedIterable(cosmosPagedFlux);
    }

}
