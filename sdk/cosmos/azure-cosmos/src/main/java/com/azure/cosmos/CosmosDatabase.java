// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import com.azure.cosmos.models.CosmosClientEncryptionKeyResponse;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseRequestOptions;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosUserProperties;
import com.azure.cosmos.models.CosmosUserResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.ThroughputResponse;
import com.azure.cosmos.util.Beta;
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
     * Instantiates a new Cosmos database context client.
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
     * Get the id of the Cosmos database.
     *
     * @return the id of the database.
     */
    public String getId() {
        return id;
    }

    /**
     * Reads the current Cosmos database.
     *
     * @return the {@link CosmosDatabaseResponse}.
     */
    public CosmosDatabaseResponse read() {
        return client.blockDatabaseResponse((databaseWrapper.read()));
    }

    /**
     * Reads the current Cosmos database while specifying additional request options.
     *
     * @param options the {@link CosmosDatabaseRequestOptions} request options.
     * @return the {@link CosmosDatabaseResponse}
     */
    public CosmosDatabaseResponse read(CosmosDatabaseRequestOptions options) {
        return client.blockDatabaseResponse(databaseWrapper.read(options));
    }

    /**
     * Deletes the current Cosmos database.
     *
     * @return the {@link CosmosDatabaseResponse}.
     */
    public CosmosDatabaseResponse delete() {
        return client.blockDatabaseResponse(databaseWrapper.delete());
    }

    /**
     * Delete the current Cosmos database while specifying additional request options.
     *
     * @param options the {@link CosmosDatabaseRequestOptions} request options.
     * @return the {@link CosmosDatabaseResponse}.
     */
    public CosmosDatabaseResponse delete(CosmosDatabaseRequestOptions options) {
        return client.blockDatabaseResponse(databaseWrapper.delete(options));
    }

    /* Cosmos container operations */

    /**
     * Creates a Cosmos container.
     *
     * @param containerProperties the {@link CosmosContainerProperties}.
     * @return the {@link CosmosContainerResponse} with the created container.
     */
    public CosmosContainerResponse createContainer(CosmosContainerProperties containerProperties) {
        return this.blockContainerResponse(databaseWrapper.createContainer(containerProperties));
    }

    /**
     * Creates a Cosmos container with custom throughput setting.
     *
     * @param containerProperties the {@link CosmosContainerProperties}.
     * @param throughputProperties the throughput properties.
     * @return the {@link CosmosContainerResponse} with the created container.
     */
    public CosmosContainerResponse createContainer(
        CosmosContainerProperties containerProperties,
        ThroughputProperties throughputProperties) {
        return this.blockContainerResponse(databaseWrapper.createContainer(containerProperties, throughputProperties));
    }

    /**
     * Creates a Cosmos container while passing additional request options.
     *
     * @param containerProperties the {@link CosmosContainerProperties}.
     * @param options the {@link CosmosContainerProperties}.
     * @return the {@link CosmosContainerResponse} with the created container.
     */
    public CosmosContainerResponse createContainer(
        CosmosContainerProperties containerProperties,
        CosmosContainerRequestOptions options) {
        return this.blockContainerResponse(databaseWrapper.createContainer(containerProperties, options));
    }

    /**
     * Creates a Cosmos container.
     *
     * @param containerProperties the {@link CosmosContainerProperties}.
     * @param throughput the throughput.
     * @param options the {@link CosmosContainerProperties}.
     * @return the {@link CosmosContainerResponse} with the created container.
     */
    CosmosContainerResponse createContainer(
        CosmosContainerProperties containerProperties,
        int throughput,
        CosmosContainerRequestOptions options) {
        return this.blockContainerResponse(databaseWrapper.createContainer(containerProperties,
                                                                                 throughput,
                                                                                 options));
    }

    /**
     * Creates a Cosmos container.
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
        return this.blockContainerResponse(databaseWrapper.createContainer(containerProperties,
                                                                                 throughputProperties,
                                                                                 options));
    }

    /**
     * Create a Cosmos container.
     *
     * @param id the container id.
     * @param partitionKeyPath the partition key path.
     * @return the cosmos container response.
     */
    public CosmosContainerResponse createContainer(String id, String partitionKeyPath) {
        return this.blockContainerResponse(databaseWrapper.createContainer(id, partitionKeyPath));
    }

    /**
     * Create a Cosmos container.
     *
     * @param id the id.
     * @param partitionKeyPath the partition key path.
     * @param throughputProperties the throughput properties.
     * @return the cosmos container response.
     */
    public CosmosContainerResponse createContainer(String id, String partitionKeyPath, ThroughputProperties throughputProperties) {
        return this.blockContainerResponse(databaseWrapper.createContainer(id, partitionKeyPath, throughputProperties));
    }

    /**
     * Create container if one matching the id in the properties object does not exist.
     *
     * @param containerProperties the container properties.
     * @return the cosmos container response.
     */
    public CosmosContainerResponse createContainerIfNotExists(CosmosContainerProperties containerProperties) {
        return this.blockContainerResponse(databaseWrapper.createContainerIfNotExists(containerProperties));
    }

    /**
     * Create container if one does not exist.
     *
     * @param containerProperties the container properties.
     * @param throughput the throughput.
     * @return the cosmos container response.
     */
    CosmosContainerResponse createContainerIfNotExists(
        CosmosContainerProperties containerProperties,
        int throughput) {
        return this.blockContainerResponse(databaseWrapper.createContainerIfNotExists(containerProperties,
            throughput));
    }

    /**
     * Creates a Cosmos container if one matching the id in the properties object does not exist.
     * <p>
     * The throughput properties will only be used if the specified container
     * does not exist and therefor a new container will be created.
     *
     * @param containerProperties the container properties.
     * @param throughputProperties the throughput properties for the container.
     * @return the cosmos container response.
     */
    public CosmosContainerResponse createContainerIfNotExists(
        CosmosContainerProperties containerProperties,
        ThroughputProperties throughputProperties) {
        return this.blockContainerResponse(databaseWrapper.createContainerIfNotExists(containerProperties,
            throughputProperties));
    }

    /**
     * Creates a Cosmos container if one matching the id does not exist.
     *
     * @param id the id.
     * @param partitionKeyPath the partition key path.
     * @return the cosmos container response.
     */
    public CosmosContainerResponse createContainerIfNotExists(
        String id,
        String partitionKeyPath) {
        return this.blockContainerResponse(databaseWrapper.createContainerIfNotExists(id, partitionKeyPath));
    }

    /**
     * Creates a Cosmos container if one matching the id does not exist.
     * <p>
     * The throughput settings will only be used if the specified container
     * does not exist and therefor a new container will be created.
     *
     * @param id the id.
     * @param partitionKeyPath the partition key path.
     * @param throughput the throughput.
     * @return the cosmos container response.
     */
    CosmosContainerResponse createContainerIfNotExists(
        String id, String partitionKeyPath,
        int throughput) {
        return this.blockContainerResponse(databaseWrapper.createContainerIfNotExists(id,
            partitionKeyPath,
            throughput));
    }

    /**
     * Creates a Cosmos container if one matching the id does not exist.
     * <p>
     * The throughput properties will only be used if the specified container
     * does not exist and therefor a new container will be created.
     *
     * @param id the id.
     * @param partitionKeyPath the partition key path.
     * @param throughputProperties the throughput properties for the container.
     * @return the cosmos container response.
     */
    public CosmosContainerResponse createContainerIfNotExists(
        String id, String partitionKeyPath,
        ThroughputProperties throughputProperties) {
        return this.blockContainerResponse(databaseWrapper.createContainerIfNotExists(id,
            partitionKeyPath,
            throughputProperties));
    }

    /**
     * Block cosmos container response.
     *
     * @param containerMono the container mono.
     * @return the cosmos container response.
     */
    CosmosContainerResponse blockContainerResponse(Mono<CosmosContainerResponse> containerMono) {
        try {
            return containerMono.block();
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
     * Read all containers in the current database.
     *
     * @param options the options.
     * @return the {@link CosmosPagedIterable}.
     */
    CosmosPagedIterable<CosmosContainerProperties> readAllContainers(CosmosQueryRequestOptions options) {
        return getCosmosPagedIterable(databaseWrapper.readAllContainers(options));
    }

    /**
     * Read all containers in the current database.
     *
     @return the {@link CosmosPagedIterable}.
     */
    public CosmosPagedIterable<CosmosContainerProperties> readAllContainers() {
        return getCosmosPagedIterable(databaseWrapper.readAllContainers());
    }

    /**
     * Query containers in the current database.
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
    public CosmosPagedIterable<CosmosContainerProperties> queryContainers(String query, CosmosQueryRequestOptions options) {
        return getCosmosPagedIterable(databaseWrapper.queryContainers(query, options));
    }

    /**
     * Query containers in the current database.
     *
     * @param querySpec the query spec.
     * @return the {@link CosmosPagedIterable}.
     */
    public CosmosPagedIterable<CosmosContainerProperties> queryContainers(SqlQuerySpec querySpec) {
        return getCosmosPagedIterable(databaseWrapper.queryContainers(querySpec));
    }

    /**
     * Query containers in the current database.
     *
     * @param querySpec the query spec.
     * @param options the options.
     * @return the {@link CosmosPagedIterable}.
     */
    public CosmosPagedIterable<CosmosContainerProperties> queryContainers(
        SqlQuerySpec querySpec,
        CosmosQueryRequestOptions options) {
        return getCosmosPagedIterable(databaseWrapper.queryContainers(querySpec, options));
    }

    /**
     * Gets a Cosmos container instance without making a service call.
     * <p>
     * To get the actual object a read operation must be performed first.
     *
     * @param id id of the container.
     * @return Cosmos Container.
     */
    public CosmosContainer getContainer(String id) {
        return new CosmosContainer(id, this, databaseWrapper.getContainer(id));
    }

    /* Users */

    /**
     * Create Cosmos user instance without making a service call.
     * <p>
     * To get the actual object a read operation must be performed first.
     *
     *
     * @param userProperties the settings.
     * @return the cosmos user response.
     */
    public CosmosUserResponse createUser(CosmosUserProperties userProperties) {
        return blockUserResponse(databaseWrapper.createUser(userProperties));
    }

    /**
     * Upserts a Cosmos user.
     *
     * @param userProperties the settings.
     * @return the cosmos user response.
     */
    public CosmosUserResponse upsertUser(CosmosUserProperties userProperties) {
        return blockUserResponse(databaseWrapper.upsertUser(userProperties));
    }

    /**
     * Read all Cosmos users for the current database.
     *
     * @return the {@link CosmosPagedIterable}.
     */
    public CosmosPagedIterable<CosmosUserProperties> readAllUsers() {
        return getCosmosPagedIterable(databaseWrapper.readAllUsers());
    }

    /**
     * Read all Cosmos users for the current database.
     *
     * @param options the options.
     * @return the {@link CosmosPagedIterable}.
     */
    CosmosPagedIterable<CosmosUserProperties> readAllUsers(CosmosQueryRequestOptions options) {
        return getCosmosPagedIterable(databaseWrapper.readAllUsers(options));
    }

    /**
     * Query all Cosmos users for the current database.
     *
     * @param query the query.
     * @return the {@link CosmosPagedIterable}.
     */
    public CosmosPagedIterable<CosmosUserProperties> queryUsers(String query) {
        return getCosmosPagedIterable(databaseWrapper.queryUsers(query));
    }

    /**
     * Query all Cosmos users for the current database.
     *
     * @param query the query.
     * @param options the options.
     * @return the {@link CosmosPagedIterable}.
     */
    public CosmosPagedIterable<CosmosUserProperties> queryUsers(String query, CosmosQueryRequestOptions options) {
        return getCosmosPagedIterable(databaseWrapper.queryUsers(query, options));
    }

    /**
     * Query all Cosmos users for the current database.
     *
     * @param querySpec the query spec.
     * @return the {@link CosmosPagedIterable}.
     */
    public CosmosPagedIterable<CosmosUserProperties> queryUsers(SqlQuerySpec querySpec) {
        return getCosmosPagedIterable(databaseWrapper.queryUsers(querySpec));
    }

    /**
     * Query all Cosmos users for the current database.
     *
     * @param querySpec the query spec.
     * @param options the options.
     * @return the {@link CosmosPagedIterable}.
     */
    public CosmosPagedIterable<CosmosUserProperties> queryUsers(SqlQuerySpec querySpec, CosmosQueryRequestOptions options) {
        return getCosmosPagedIterable(databaseWrapper.queryUsers(querySpec, options));
    }

    /**
     * Gets a Cosmos user instance without making a service call.
     * <p>
     * To get the actual object a read operation must be performed first.
     *
     * @param id the id.
     * @return the user.
     */
    public CosmosUser getUser(String id) {
        return new CosmosUser(databaseWrapper.getUser(id), this, id);
    }

    CosmosUserResponse blockUserResponse(Mono<CosmosUserResponse> containerMono) {
        try {
            return containerMono.block();
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

    /**
     * Gets a CosmosClientEncryptionKey object without making a service call
     *
     * @param id id of the clientEncryptionKey
     * @return Cosmos ClientEncryptionKey
     */
    @Beta(value = Beta.SinceVersion.V4_15_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosClientEncryptionKey getClientEncryptionKey(String id) {
        return new CosmosClientEncryptionKey(id, this, this.databaseWrapper.getClientEncryptionKey(id));
    }

    /**
     * Reads all cosmos client encryption keys in a database.
     *
     * @return a {@link CosmosPagedIterable}.
     */
    @Beta(value = Beta.SinceVersion.V4_15_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosPagedIterable<CosmosClientEncryptionKeyProperties> readAllClientEncryptionKeys() {
        return getCosmosPagedIterable(this.databaseWrapper.readAllClientEncryptionKeys(new CosmosQueryRequestOptions()));
    }

    /**
     * Block cosmos clientEncryptionKey response
     *
     * @param cosmosClientEncryptionKeyResponseMono the clientEncryptionKey mono.
     * @return the cosmos clientEncryptionKey response.
     */
    CosmosClientEncryptionKeyResponse blockClientEncryptionKeyResponse(Mono<CosmosClientEncryptionKeyResponse> cosmosClientEncryptionKeyResponseMono) {
        try {
            return cosmosClientEncryptionKeyResponseMono.block();
        } catch (Exception ex) {
            final Throwable throwable = Exceptions.unwrap(ex);
            if (throwable instanceof CosmosException) {
                throw (CosmosException) throwable;
            } else {
                throw ex;
            }
        }
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
        return new CosmosPagedIterable<>(cosmosPagedFlux);
    }

}
