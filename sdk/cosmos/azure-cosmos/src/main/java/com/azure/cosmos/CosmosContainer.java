// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.ThroughputResponse;
import com.azure.cosmos.util.Beta;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.azure.cosmos.util.UtilBridgeInternal;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.azure.cosmos.implementation.Utils.setContinuationTokenAndMaxItemCount;

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
     * @param id the container id.
     * @param database the database.
     * @param container the container.
     */
    CosmosContainer(String id, CosmosDatabase database, CosmosAsyncContainer container) {
        this.id = id;
        this.database = database;
        this.asyncContainer = container;
    }

    /**
     * Gets the current container id.
     *
     * @return the container id.
     */
    public String getId() {
        return id;
    }

    /**
     * Reads the current container.
     *
     * @return the Cosmos container response with the read container.
     */
    public CosmosContainerResponse read() {
        return database.blockContainerResponse(this.asyncContainer.read());
    }

    /**
     * Reads the current container while specifying additional options such as If-Match.
     *
     * @param options the options.
     * @return the Cosmos container response.
     */
    public CosmosContainerResponse read(CosmosContainerRequestOptions options) {
        return database.blockContainerResponse(this.asyncContainer.read(options));
    }

    /**
     * Deletes the current Cosmos container while specifying additional options such as If-Match.
     *
     * @param options the options.
     * @return the cosmos container response.
     */
    public CosmosContainerResponse delete(CosmosContainerRequestOptions options) {
        return database.blockContainerResponse(this.asyncContainer.delete(options));
    }

    /**
     * Deletes the current cosmos container.
     *
     * @return the cosmos container response.
     */
    public CosmosContainerResponse delete() {
        return database.blockContainerResponse(this.asyncContainer.delete());
    }

    /**
     * Replaces the current container properties.
     *
     * @param containerProperties the container properties.
     * @return the cosmos container response.
     */
    public CosmosContainerResponse replace(CosmosContainerProperties containerProperties) {
        return database.blockContainerResponse(this.asyncContainer.replace(containerProperties));
    }

    /**
     * Replaces the current container properties while specifying additional options such as If-Match.
     *
     * @param containerProperties the container properties.
     * @param options the options.
     * @return the cosmos container response.
     */
    public CosmosContainerResponse replace(CosmosContainerProperties containerProperties,
                                           CosmosContainerRequestOptions options) {
        return database.blockContainerResponse(this.asyncContainer.replace(containerProperties, options));
    }

    /**
     * Sets the throughput for the current container.
     *
     * @param throughputProperties the throughput properties.
     * @return the throughput response.
     */
    public ThroughputResponse replaceThroughput(ThroughputProperties throughputProperties) {
        return database.throughputResponseToBlock(this.asyncContainer.replaceThroughput(throughputProperties));
    }

    /**
     * Gets the throughput for the current container.
     *
     * @return the throughput response.
     */
    public ThroughputResponse readThroughput() {
        return database.throughputResponseToBlock(this.asyncContainer.readThroughput());
    }

    /* Cosmos item operations */

    /**
     * Creates a new item synchronously and returns its respective Cosmos item response.
     *
     * @param <T> the type parameter
     * @param item the item
     * @return the Cosmos item response
     */
    public <T> CosmosItemResponse<T> createItem(T item) {
        return this.blockItemResponse(this.asyncContainer.createItem(item));
    }

    /**
     * Creates a new item synchronously and returns its respective Cosmos item response
     * while specifying additional options.
     *
     * @param <T> the type parameter.
     * @param item the item.
     * @param partitionKey the partition key.
     * @param options the options.
     * @return the Cosmos item response.
     */
    public <T> CosmosItemResponse<T> createItem(T item,
                                                PartitionKey partitionKey,
                                                CosmosItemRequestOptions options) {
        return this.blockItemResponse(this.asyncContainer.createItem(item, partitionKey, options));
    }

    /**
     * Creates a new item synchronously and returns its respective Cosmos item response
     * while specifying additional options.
     * <p>
     * The partition key value will be automatically extracted from the item's content.
     *
     * @param <T> the type parameter.
     * @param item the item.
     * @param options the options.
     * @return the cosmos item response.
     */

    public <T> CosmosItemResponse<T> createItem(T item, CosmosItemRequestOptions options) {
        return this.blockItemResponse(this.asyncContainer.createItem(item, options));
    }

    /**
     * Upserts an Cosmos item in the current container.
     *
     * @param <T> the type parameter.
     * @param item the item.
     * @return the Cosmos item response.
     */
    public <T> CosmosItemResponse<T> upsertItem(T item) {
        return this.blockItemResponse(this.asyncContainer.upsertItem(item));
    }

    /**
     * Upserts a item Cosmos sync item while specifying additional options.
     *
     * @param <T> the type parameter.
     * @param item the item.
     * @param options the options.
     * @return the Cosmos item response.
     */
    public <T> CosmosItemResponse<T> upsertItem(T item, CosmosItemRequestOptions options) {
        return this.blockItemResponse(this.asyncContainer.upsertItem(item, options));
    }

    /**
     * Upserts a item Cosmos sync item while specifying additional options.
     *
     * @param <T> the type parameter.
     * @param item the item.
     * @param partitionKey the partitionKey.
     * @param options the options.
     * @return the Cosmos item response.
     */
    public <T> CosmosItemResponse<T> upsertItem(T item, PartitionKey partitionKey, CosmosItemRequestOptions options) {
        return this.blockItemResponse(this.asyncContainer.upsertItem(item, partitionKey, options));
    }

    /**
     * Block cosmos item response.
     *
     * @param itemMono the item mono.
     * @return the cosmos item response.
     */
    <T> CosmosItemResponse<T> blockItemResponse(Mono<CosmosItemResponse<T>> itemMono) {
        try {
            return itemMono.block();
        } catch (Exception ex) {
            final Throwable throwable = Exceptions.unwrap(ex);
            if (throwable instanceof CosmosException) {
                throw (CosmosException) throwable;
            } else {
                throw ex;
            }
        }
    }

    /**
     * Block cosmos item response.
     *
     * @param itemMono the item mono.
     * @return the cosmos item response.
     */
    <T> FeedResponse<T> blockFeedResponse(Mono<FeedResponse<T>> itemMono) {
        try {
            return itemMono.block();
        } catch (Exception ex) {
            final Throwable throwable = Exceptions.unwrap(ex);
            if (throwable instanceof CosmosException) {
                throw (CosmosException) throwable;
            } else {
                throw ex;
            }
        }
    }

    private CosmosItemResponse<Object> blockDeleteItemResponse(Mono<CosmosItemResponse<Object>> deleteItemMono) {
        try {
            return deleteItemMono.block();
        } catch (Exception ex) {
            final Throwable throwable = Exceptions.unwrap(ex);
            if (throwable instanceof CosmosException) {
                throw (CosmosException) throwable;
            } else {
                throw ex;
            }
        }
    }

    /**
     * Read all items as {@link CosmosPagedIterable} in the current container.
     *
     * @param <T> the type parameter.
     * @param options the options.
     * @param classType the classType.
     * @return the {@link CosmosPagedIterable}.
     */
    <T> CosmosPagedIterable<T> readAllItems(CosmosQueryRequestOptions options, Class<T> classType) {
        return getCosmosPagedIterable(this.asyncContainer.readAllItems(options, classType));
    }

    /**
     * Query items in the current container returning the results as {@link CosmosPagedIterable}.
     *
     * @param <T> the type parameter.
     * @param query the query.
     * @param options the options.
     * @param classType the class type.
     * @return the {@link CosmosPagedIterable}.
     */
    public <T> CosmosPagedIterable<T> queryItems(String query, CosmosQueryRequestOptions options, Class<T> classType) {
        return getCosmosPagedIterable(this.asyncContainer.queryItems(query, options, classType));
    }

    /**
     * Query items in the current container returning the results as {@link CosmosPagedIterable}.
     *
     * @param <T> the type parameter.
     * @param querySpec the query spec.
     * @param options the options.
     * @param classType the class type.
     * @return the {@link CosmosPagedIterable}.
     */
    public <T> CosmosPagedIterable<T> queryItems(SqlQuerySpec querySpec, CosmosQueryRequestOptions options, Class<T> classType) {
        return getCosmosPagedIterable(this.asyncContainer.queryItems(querySpec, options, classType));
    }

    /**
     * Reads many documents.
     *
     * @param <T> the type parameter
     * @param itemKeyList document id and partition key pair that needs to be read
     * @param classType   class type
     * @return a Mono with feed response of cosmos items
     */
    @Beta(Beta.SinceVersion.V4_4_0)
    public <T> FeedResponse<T> readMany(
        List<Pair<String, PartitionKey>> itemKeyList,
        Class<T> classType) {

        return this.readMany(itemKeyList, null, classType);
    }

    /**
     * Reads many documents.
     *
     * @param <T> the type parameter
     * @param itemKeyList document id and partition key pair that needs to be read
     * @param sessionToken the optional Session token - null if the read can be made without specific session token
     * @param classType   class type
     * @return a Mono with feed response of cosmos items
     */
    @Beta(Beta.SinceVersion.V4_4_0)
    public <T> FeedResponse<T> readMany(
        List<Pair<String, PartitionKey>> itemKeyList,
        String sessionToken,
        Class<T> classType) {

        return this.blockFeedResponse(
            this.asyncContainer.readMany(
                itemKeyList,
                sessionToken,
                classType));
    }

    /**
     * Reads all the items of a logical partition returning the results as {@link CosmosPagedIterable}.
     *
     * @param <T> the type parameter.
     * @param partitionKey the partition key value of the documents that need to be read
     * @param classType the class type.
     * @return the {@link CosmosPagedIterable}.
     */
    @Beta(Beta.SinceVersion.V4_4_0)
    public <T> CosmosPagedIterable<T> readAllItems(
        PartitionKey partitionKey,
        Class<T> classType) {

        return this.readAllItems(partitionKey, new CosmosQueryRequestOptions(), classType);
    }

    /**
     * Reads all the items of a logical partition returning the results as {@link CosmosPagedIterable}.
     * <p>
     *
     * @param <T> the type parameter.
     * @param partitionKey the partition key value of the documents that need to be read
     * @param options the feed options.
     * @param classType the class type.
     * @return the {@link CosmosPagedIterable}.
     */
    @Beta(Beta.SinceVersion.V4_4_0)
    public <T> CosmosPagedIterable<T> readAllItems(
        PartitionKey partitionKey,
        CosmosQueryRequestOptions options,
        Class<T> classType) {

        return getCosmosPagedIterable(this.asyncContainer.readAllItems(partitionKey, options, classType));
    }

    /**
     * Reads an item in the current container.
     *
     * @param <T> the type parameter.
     * @param itemId the item id.
     * @param partitionKey the partition key.
     * @param itemType the class type of item.
     * @return the Cosmos item response.
     */
    public <T> CosmosItemResponse<T> readItem(String itemId, PartitionKey partitionKey, Class<T> itemType) {
        return this.blockItemResponse(asyncContainer.readItem(itemId,
                                                                    partitionKey,
                                                                    new CosmosItemRequestOptions(),
                                                                    itemType));
    }

    /**
     * Reads an item in the current container while specifying additional options.
     *
     * @param <T> the type parameter.
     * @param itemId the item id.
     * @param partitionKey the partition key.
     * @param options the options.
     * @param itemType the class type of item.
     * @return the Cosmos item response.
     */
    public <T> CosmosItemResponse<T> readItem(
        String itemId, PartitionKey partitionKey,
        CosmosItemRequestOptions options, Class<T> itemType) {
        return this.blockItemResponse(asyncContainer.readItem(itemId, partitionKey, options, itemType));
    }

    /**
     * Replaces an item in the current container.
     *
     * @param <T> the type parameter.
     * @param item the item.
     * @param itemId the item id.
     * @param partitionKey the partition key.
     * @param options the options.
     * @return the Cosmos item response.
     */
    public <T> CosmosItemResponse<T> replaceItem(T item,
                                                 String itemId,
                                                 PartitionKey partitionKey,
                                                 CosmosItemRequestOptions options) {
        return this.blockItemResponse(asyncContainer.replaceItem(item, itemId, partitionKey, options));
    }

    /**
     * Deletes an item in the current container.
     *
     * @param itemId the item id.
     * @param partitionKey the partition key.
     * @param options the options.
     * @return the Cosmos item response.
     */
    public CosmosItemResponse<Object> deleteItem(String itemId, PartitionKey partitionKey,
                                                 CosmosItemRequestOptions options) {
        return  this.blockDeleteItemResponse(asyncContainer.deleteItem(itemId, partitionKey, options));
    }

    /**
     * Deletes an item in the current container.
     *
     * @param <T> the type parameter.
     * @param item the item to be deleted.
     * @param options the options.
     * @return the Cosmos item response.
     */
    public <T> CosmosItemResponse<Object> deleteItem(T item, CosmosItemRequestOptions options) {
        return  this.blockDeleteItemResponse(asyncContainer.deleteItem(item, options));
    }

    /**
     * Gets the Cosmos scripts using the current container as context.
     *
     * @return the Cosmos sync scripts.
     */
    public CosmosScripts getScripts() {
        if (this.scripts == null) {
            this.scripts = new CosmosScripts(this, asyncContainer.getScripts());
        }
        return this.scripts;
    }

    // TODO: should make partitionkey public in CosmosAsyncItem and fix the below call

    private <T> CosmosPagedIterable<T> getCosmosPagedIterable(CosmosPagedFlux<T> cosmosPagedFlux) {
        return UtilBridgeInternal.createCosmosPagedIterable(cosmosPagedFlux);
    }

}
