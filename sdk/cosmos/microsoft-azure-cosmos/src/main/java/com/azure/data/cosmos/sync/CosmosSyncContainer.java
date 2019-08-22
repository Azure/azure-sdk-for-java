// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.sync;

import com.azure.data.cosmos.ChangeFeedOptions;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosContainerProperties;
import com.azure.data.cosmos.CosmosContainerRequestOptions;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.CosmosItemRequestOptions;
import com.azure.data.cosmos.CosmosItemResponse;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.SqlQuerySpec;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Iterator;

/**
 * Provides synchronous methods for reading, deleting, and replacing existing Containers
 * Provides methods for interacting with child resources (Items, Scripts, Conflicts)
 */
public class CosmosSyncContainer {

    private final CosmosContainer containerWrapper;
    private final CosmosSyncDatabase database;
    private final String id;

    /**
     * Instantiates a new Cosmos sync container.
     *
     * @param id the id
     * @param database the database
     * @param container the container
     */
    CosmosSyncContainer(String id, CosmosSyncDatabase database, CosmosContainer container) {
        this.id = id;
        this.database = database;
        this.containerWrapper = container;
    }

    /**
     * Id string.
     *
     * @return the string
     */
    public String id() {
        return id;
    }

    /**
     * Read cosmos sync container response.
     *
     * @return the cosmos sync container response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncContainerResponse read() throws CosmosClientException {
        return database.mapContainerResponseAndBlock(this.containerWrapper.read());
    }

    /**
     * Read cosmos sync container response.
     *
     * @param options the options
     * @return the cosmos sync container response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncContainerResponse read(CosmosContainerRequestOptions options) throws CosmosClientException {
        return database.mapContainerResponseAndBlock(this.containerWrapper.read(options));
    }

    /**
     * Delete cosmos sync container response.
     *
     * @param options the options
     * @return the cosmos sync container response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncContainerResponse delete(CosmosContainerRequestOptions options) throws CosmosClientException {
        return database.mapContainerResponseAndBlock(this.containerWrapper.delete(options));
    }

    /**
     * Delete cosmos sync container response.
     *
     * @return the cosmos sync container response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncContainerResponse delete() throws CosmosClientException {
        return database.mapContainerResponseAndBlock(this.containerWrapper.delete());
    }

    /**
     * Replace cosmos sync container response.
     *
     * @param containerProperties the container properties
     * @return the cosmos sync container response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncContainerResponse replace(CosmosContainerProperties containerProperties) throws CosmosClientException {
        return database.mapContainerResponseAndBlock(this.containerWrapper.replace(containerProperties));
    }

    /**
     * Replace cosmos sync container response.
     *
     * @param containerProperties the container properties
     * @param options the options
     * @return the cosmos sync container response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncContainerResponse replace(CosmosContainerProperties containerProperties,
                                               CosmosContainerRequestOptions options) throws CosmosClientException {
        return database.mapContainerResponseAndBlock(this.containerWrapper.replace(containerProperties, options));
    }


    /* CosmosItem operations */

    /**
     * Create item cosmos sync item response.
     *
     * @param item the item
     * @return the cosmos sync item response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncItemResponse createItem(Object item) throws CosmosClientException {
        return this.mapItemResponseAndBlock(this.containerWrapper.createItem(item));
    }

    /**
     * Create item cosmos sync item response.
     *
     * @param item the item
     * @param options the options
     * @return the cosmos sync item response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncItemResponse createItem(Object item, CosmosItemRequestOptions options) throws CosmosClientException {
        return this.mapItemResponseAndBlock(this.containerWrapper.createItem(item, options));
    }

    /**
     * Upsert item cosmos sync item response.
     *
     * @param item the item
     * @return the cosmos sync item response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncItemResponse upsertItem(Object item) throws CosmosClientException {
        return this.mapItemResponseAndBlock(this.containerWrapper.upsertItem(item));
    }

    /**
     * Upsert item cosmos sync item response.
     *
     * @param item the item
     * @param options the options
     * @return the cosmos sync item response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncItemResponse upsertItem(Object item, CosmosItemRequestOptions options) throws CosmosClientException {
        return this.mapItemResponseAndBlock(this.containerWrapper.createItem(item, options));
    }

    /**
     * Map item response and block cosmos sync item response.
     *
     * @param itemMono the item mono
     * @return the cosmos sync item response
     * @throws CosmosClientException the cosmos client exception
     */
    CosmosSyncItemResponse mapItemResponseAndBlock(Mono<CosmosItemResponse> itemMono)
            throws CosmosClientException {
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

    /**
     * Read all items iterator.
     *
     * @param options the options
     * @return the iterator
     */
    public Iterator<FeedResponse<CosmosItemProperties>> readAllItems(FeedOptions options) {
        return getFeedIterator(this.containerWrapper.readAllItems(options));
    }

    /**
     * Query items iterator.
     *
     * @param query the query
     * @param options the options
     * @return the iterator
     */
    public Iterator<FeedResponse<CosmosItemProperties>> queryItems(String query, FeedOptions options) {
        return getFeedIterator(this.containerWrapper.queryItems(query, options));
    }

    /**
     * Query items iterator.
     *
     * @param querySpec the query spec
     * @param options the options
     * @return the iterator
     */
    public Iterator<FeedResponse<CosmosItemProperties>> queryItems(SqlQuerySpec querySpec, FeedOptions options) {
        return getFeedIterator(this.containerWrapper.queryItems(querySpec, options));
    }

    /**
     * Query change feed items iterator.
     *
     * @param changeFeedOptions the change feed options
     * @return the iterator
     */
    public Iterator<FeedResponse<CosmosItemProperties>> queryChangeFeedItems(ChangeFeedOptions changeFeedOptions) {
        return getFeedIterator(this.containerWrapper.queryChangeFeedItems(changeFeedOptions));
    }

    /**
     * Gets item.
     *
     * @param id the id
     * @param partitionKey the partition key
     * @return the item
     */
    public CosmosSyncItem getItem(String id, Object partitionKey) {
        return new CosmosSyncItem(id,
                partitionKey,
                this,
                containerWrapper.getItem(id, partitionKey));
    }

    /**
     * Convert response cosmos sync item response.
     *
     * @param response the cosmos item response
     * @return the cosmos sync item response
     */
    CosmosSyncItemResponse convertResponse(CosmosItemResponse response) {
        return new CosmosSyncItemResponse(response, null, this);
    }

    private Iterator<FeedResponse<CosmosItemProperties>> getFeedIterator(Flux<FeedResponse<CosmosItemProperties>> itemFlux) {
        return itemFlux.toIterable(1).iterator();
    }

}
