// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Iterator;

/**
 * Provides synchronous methods for reading, deleting, and replacing existing Containers
 * Provides methods for interacting with child resources (Items, Scripts, Conflicts)
 */
public class CosmosContainer {

    private final CosmosAsyncContainer asyncContainer;
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


    /* CosmosAsyncItem operations */

    /**
     * Create item cosmos sync item response.
     *
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
     * @param item the item
     * @param options the options
     * @return the cosmos sync item response
     * @throws CosmosClientException the cosmos client exception
     */
    public <T> CosmosItemResponse<T> upsertItem(Object item, CosmosItemRequestOptions options) throws CosmosClientException {
        return (CosmosItemResponse<T>) this.mapItemResponseAndBlock(this.asyncContainer.upsertItem(item, options));
    }

    /**
     * Map item response and block cosmos sync item response.
     *
     * @param itemMono the item mono
     * @return the cosmos sync item response
     * @throws CosmosClientException the cosmos client exception
     */
    <T> CosmosItemResponse<T> mapItemResponseAndBlock(Mono<CosmosAsyncItemResponse<T>> itemMono) throws CosmosClientException {
        try {
            return (CosmosItemResponse<T>) itemMono
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

    private CosmosItemResponse mapDeleteItemResponseAndBlock(Mono<CosmosAsyncItemResponse> deleteItemMono) throws
        CosmosClientException {
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
     * Read all items iterator.
     *
     * @param <T> the type parameter
     * @param options the options
     * @param klass the klass
     * @return the iterator
     */
    public <T> Iterator<FeedResponse<T>> readAllItems(FeedOptions options, Class<T> klass) {
        return getFeedIterator(this.asyncContainer.readAllItems(options, klass));
    }

    /**
     * Query items iterator.
     *
     * @param <T> the type parameter
     * @param query the query
     * @param options the options
     * @param klass the class type
     * @return the iterator
     */
    public <T> Iterator<FeedResponse<T>> queryItems(String query, FeedOptions options, Class<T> klass) {
        return getFeedIterator(this.asyncContainer.queryItems(query, options, klass));
    }

    /**
     * Query items iterator.
     *
     * @param <T> the type parameter
     * @param querySpec the query spec
     * @param options the options
     * @param klass the class type
     * @return the iterator
     */
    public <T> Iterator<FeedResponse<T>> queryItems(SqlQuerySpec querySpec, FeedOptions options, Class<T> klass) {
        return getFeedIterator(this.asyncContainer.queryItems(querySpec, options, klass));
    }

    /**
     * Query change feed items iterator.
     *
     * @param changeFeedOptions the change feed options
     * @return the iterator
     */
    public Iterator<FeedResponse<CosmosItemProperties>> queryChangeFeedItems(ChangeFeedOptions changeFeedOptions) {
        return getFeedIterator(this.asyncContainer.queryChangeFeedItems(changeFeedOptions));
    }

    /**
     * Read cosmos sync item response.
     *
     * @param itemId the item id
     * @param partitionKey the partition key
     * @return the cosmos sync item response
     * @throws CosmosClientException the cosmos client exception
     */
    public <T> CosmosItemResponse<T> readItem(String itemId, PartitionKey partitionKey, Class<T> itemType) throws CosmosClientException {
        return this.mapItemResponseAndBlock(asyncContainer.readItem(itemId,
                                                                    partitionKey,
                                                                    new CosmosItemRequestOptions(),
                                                                    itemType));
    }

    /**
     * Read cosmos sync item response.
     *
     * @param options the options
     * @return the cosmos sync item response
     * @throws CosmosClientException the cosmos client exception
     */
    public <T> CosmosItemResponse<T> readItem(String itemId, PartitionKey partitionKey,
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
     * @param options the options
     * @return the cosmos sync item response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosItemResponse deleteItem(String itemId, PartitionKey partitionKey,
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
    private <T> CosmosItemResponse<T> convertResponse(CosmosAsyncItemResponse response) {
        return new CosmosItemResponse<T>(response);
    }

    private <T> Iterator<FeedResponse<T>> getFeedIterator(Flux<FeedResponse<T>> itemFlux) {
        return itemFlux.toIterable(1).iterator();
    }

}
