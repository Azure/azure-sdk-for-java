// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosAsyncItemResponse;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.FeedOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.ThroughputResponse;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.azure.cosmos.util.UtilBridgeInternal;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

/**
 * Provides synchronous methods for reading, deleting, and replacing existing Containers
 * Provides methods for interacting with child resources (Items, Scripts, Conflicts)
 */
public class CosmosContainer {

    final CosmosAsyncContainer asyncContainer;
    private final CosmosDatabase database;
    private final String id;
    private CosmosScripts scripts;

    /**
     * Instantiates a new Cosmos sync container.
     *
     * @param id the id
     * @param database the database
     * @param container the container
     */
    CosmosContainer(String id, CosmosDatabase database, CosmosAsyncContainer container) {
        this.id = id;
        this.database = database;
        this.asyncContainer = container;
    }

    /**
     * Id string.
     *
     * @return the string
     */
    public String getId() {
        return id;
    }

    /**
     * Read cosmos sync container response.
     *
     * @return the cosmos sync container response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosContainerResponse read() throws CosmosClientException {
        return database.mapContainerResponseAndBlock(this.asyncContainer.read());
    }

    /**
     * Read cosmos sync container response.
     *
     * @param options the options
     * @return the cosmos sync container response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosContainerResponse read(CosmosContainerRequestOptions options) throws CosmosClientException {
        return database.mapContainerResponseAndBlock(this.asyncContainer.read(options));
    }

    /**
     * Delete cosmos sync container response.
     *
     * @param options the options
     * @return the cosmos sync container response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosContainerResponse delete(CosmosContainerRequestOptions options) throws CosmosClientException {
        return database.mapContainerResponseAndBlock(this.asyncContainer.delete(options));
    }

    /**
     * Delete cosmos sync container response.
     *
     * @return the cosmos sync container response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosContainerResponse delete() throws CosmosClientException {
        return database.mapContainerResponseAndBlock(this.asyncContainer.delete());
    }

    /**
     * Replace cosmos sync container response.
     *
     * @param containerProperties the container properties
     * @return the cosmos sync container response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosContainerResponse replace(CosmosContainerProperties containerProperties) throws CosmosClientException {
        return database.mapContainerResponseAndBlock(this.asyncContainer.replace(containerProperties));
    }

    /**
     * Replace cosmos sync container response.
     *
     * @param containerProperties the container properties
     * @param options the options
     * @return the cosmos sync container response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosContainerResponse replace(CosmosContainerProperties containerProperties,
                                           CosmosContainerRequestOptions options) throws CosmosClientException {
        return database.mapContainerResponseAndBlock(this.asyncContainer.replace(containerProperties, options));
    }

    /**
     * Read provisioned throughput integer.
     *
     * @return the integer. null response indicates database doesn't have any provisioned RUs
     * @throws CosmosClientException the cosmos client exception
     */
    public Integer readProvisionedThroughput() throws CosmosClientException {
        return database.throughputResponseToBlock(this.asyncContainer.readProvisionedThroughput());
    }

    /**
     * Replace provisioned throughput integer.
     *
     * @param requestUnitsPerSecond the request units per second
     * @return the integer
     * @throws CosmosClientException the cosmos client exception
     */
    public Integer replaceProvisionedThroughput(int requestUnitsPerSecond) throws CosmosClientException {
        return database.throughputResponseToBlock(this.asyncContainer
                                                      .replaceProvisionedThroughput(requestUnitsPerSecond));
    }

    /**
     * Sets the throughput.
     *
     * @param throughputProperties the throughput properties
     * @return the throughput response
     */
    public ThroughputResponse replaceThroughput(ThroughputProperties throughputProperties) {
        return database.throughputResponseToBlock(this.asyncContainer.replaceThroughput(throughputProperties));
    }

    /**
     * Gets the throughput.
     *
     * @return the throughput response
     */
    public ThroughputResponse readThroughput() {
        return database.throughputResponseToBlock(this.asyncContainer.readThroughput());
    }

    /* CosmosAsyncItem operations */

    /**
     * Create item cosmos sync item response.
     *
     * @param <T> the type parameter
     * @param item the item
     * @return the cosmos sync item response
     * @throws CosmosClientException the cosmos client exception
     */
    public <T> CosmosItemResponse<T> createItem(T item) throws CosmosClientException {
        return this.mapItemResponseAndBlock(this.asyncContainer.createItem(item));
    }

    /**
     * Create a cosmos item synchronously.
     *
     * @param <T> the type parameter
     * @param item the item
     * @param partitionKey the partition key
     * @param options the options
     * @return the cosmos sync item response
     * @throws CosmosClientException the cosmos client exception
     */
    public <T> CosmosItemResponse<T> createItem(T item,
                                                PartitionKey partitionKey,
                                                CosmosItemRequestOptions options) throws CosmosClientException {
        return this.mapItemResponseAndBlock(this.asyncContainer.createItem(item, partitionKey, options));
    }

    /**
     * Create a cosmos item.
     *
     * @param <T> the type parameter
     * @param item the item
     * @param options the options
     * @return the cosmos item response
     * @throws CosmosClientException the cosmos client exception
     */

    public <T> CosmosItemResponse<T> createItem(T item, CosmosItemRequestOptions options) throws CosmosClientException {
        return this.mapItemResponseAndBlock(this.asyncContainer.createItem(item, options));
    }

    /**
     * Upsert item cosmos sync item response.
     *
     * @param <T> the type parameter
     * @param item the item
     * @return the cosmos sync item response
     * @throws CosmosClientException the cosmos client exception
     */
    public <T> CosmosItemResponse<T> upsertItem(T item) throws CosmosClientException {
        return this.mapItemResponseAndBlock(this.asyncContainer.upsertItem(item));
    }

    /**
     * Upsert item cosmos sync item response.
     *
     * @param <T> the type parameter
     * @param item the item
     * @param options the options
     * @return the cosmos sync item response
     * @throws CosmosClientException the cosmos client exception
     */
    @SuppressWarnings("unchecked")
    // Note: @kushagraThapar and @moderakh to ensure this casting is valid
    public <T> CosmosItemResponse<T> upsertItem(Object item, CosmosItemRequestOptions options) throws
        CosmosClientException {
        return (CosmosItemResponse<T>) this.mapItemResponseAndBlock(this.asyncContainer.upsertItem(item, options));
    }

    /**
     * Map item response and block cosmos sync item response.
     *
     * @param itemMono the item mono
     * @return the cosmos sync item response
     * @throws CosmosClientException the cosmos client exception
     */
    <T> CosmosItemResponse<T> mapItemResponseAndBlock(Mono<CosmosAsyncItemResponse<T>> itemMono) throws
        CosmosClientException {
        try {
            return itemMono
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

    private CosmosItemResponse<Object> mapDeleteItemResponseAndBlock(Mono<CosmosAsyncItemResponse<Object>> deleteItemMono)
        throws CosmosClientException {
        try {
            return deleteItemMono
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
     * Read all items {@link CosmosPagedIterable}.
     *
     * @param <T> the type parameter
     * @param options the options
     * @param classType the classType
     * @return the {@link CosmosPagedIterable}
     */
    public <T> CosmosPagedIterable<T> readAllItems(FeedOptions options, Class<T> classType) {
        return getCosmosPagedIterable(this.asyncContainer.readAllItems(options, classType));
    }

    /**
     * Query items {@link CosmosPagedIterable}.
     *
     * @param <T> the type parameter
     * @param query the query
     * @param options the options
     * @param classType the class type
     * @return the {@link CosmosPagedIterable}
     */
    public <T> CosmosPagedIterable<T> queryItems(String query, FeedOptions options, Class<T> classType) {
        return getCosmosPagedIterable(this.asyncContainer.queryItems(query, options, classType));
    }

    /**
     * Query items {@link CosmosPagedIterable}.
     *
     * @param <T> the type parameter
     * @param querySpec the query spec
     * @param options the options
     * @param classType the class type
     * @return the {@link CosmosPagedIterable}
     */
    public <T> CosmosPagedIterable<T> queryItems(SqlQuerySpec querySpec, FeedOptions options, Class<T> classType) {
        return getCosmosPagedIterable(this.asyncContainer.queryItems(querySpec, options, classType));
    }

    /**
     * Read cosmos sync item response.
     *
     * @param <T> the type parameter
     * @param itemId the item id
     * @param partitionKey the partition key
     * @param itemType the class type of item
     * @return the cosmos sync item response
     * @throws CosmosClientException the cosmos client exception
     */
    public <T> CosmosItemResponse<T> readItem(String itemId, PartitionKey partitionKey, Class<T> itemType) throws
        CosmosClientException {
        return this.mapItemResponseAndBlock(asyncContainer.readItem(itemId,
                                                                    partitionKey,
                                                                    new CosmosItemRequestOptions(),
                                                                    itemType));
    }

    /**
     * Read cosmos sync item response.
     *
     * @param <T> the type parameter
     * @param itemId the item id
     * @param partitionKey the partition key
     * @param options the options
     * @param itemType the class type of item
     * @return the cosmos sync item response
     * @throws CosmosClientException the cosmos client exception
     */
    public <T> CosmosItemResponse<T> readItem(
        String itemId, PartitionKey partitionKey,
        CosmosItemRequestOptions options, Class<T> itemType) throws CosmosClientException {
        return this.mapItemResponseAndBlock(asyncContainer.readItem(itemId, partitionKey, options, itemType));
    }

    /**
     * Replace cosmos sync item response.
     *
     * @param <T> the type parameter
     * @param item the item
     * @param itemId the item id
     * @param partitionKey the partition key
     * @param options the options
     * @return the cosmos sync item response
     * @throws CosmosClientException the cosmos client exception
     */
    public <T> CosmosItemResponse<T> replaceItem(T item,
                                             String itemId,
                                             PartitionKey partitionKey,
                                             CosmosItemRequestOptions options) throws CosmosClientException {
        return this.mapItemResponseAndBlock(asyncContainer.replaceItem(item, itemId, partitionKey, options));
    }

    /**
     * Delete cosmos sync item response.
     *
     * @param itemId the item id
     * @param partitionKey the partition key
     * @param options the options
     * @return the cosmos sync item response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosItemResponse<Object> deleteItem(String itemId, PartitionKey partitionKey,
                                            CosmosItemRequestOptions options) throws CosmosClientException {
        return  this.mapDeleteItemResponseAndBlock(asyncContainer.deleteItem(itemId, partitionKey, options));
    }

    /**
     * Gets the cosmos sync scripts.
     *
     * @return the cosmos sync scripts
     */
    public CosmosScripts getScripts() {
        if (this.scripts == null) {
            this.scripts = new CosmosScripts(this, asyncContainer.getScripts());
        }
        return this.scripts;
    }

    // TODO: should make partitionkey public in CosmosAsyncItem and fix the below call

    /**
     * Convert response cosmos sync item response.
     *
     * @param response the cosmos item response
     * @return the cosmos sync item response
     */
    private <T> CosmosItemResponse<T> convertResponse(CosmosAsyncItemResponse<T> response) {
        return ModelBridgeInternal.<T>createCosmosItemResponse(response);
    }

    private <T> CosmosPagedIterable<T> getCosmosPagedIterable(CosmosPagedFlux<T> cosmosPagedFlux) {
        return UtilBridgeInternal.createCosmosPagedIterable(cosmosPagedFlux);
    }

}
