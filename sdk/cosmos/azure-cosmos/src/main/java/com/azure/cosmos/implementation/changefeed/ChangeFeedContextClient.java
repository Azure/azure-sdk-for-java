// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed;

import com.azure.cosmos.ChangeFeedOptions;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncContainerResponse;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosAsyncDatabaseResponse;
import com.azure.cosmos.CosmosAsyncItemResponse;
import com.azure.cosmos.CosmosContainerProperties;
import com.azure.cosmos.CosmosContainerRequestOptions;
import com.azure.cosmos.CosmosDatabaseRequestOptions;
import com.azure.cosmos.CosmosItemProperties;
import com.azure.cosmos.CosmosItemRequestOptions;
import com.azure.cosmos.FeedOptions;
import com.azure.cosmos.FeedResponse;
import com.azure.cosmos.PartitionKey;
import com.azure.cosmos.SqlQuerySpec;
import com.azure.cosmos.implementation.PartitionKeyRange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * The interface that captures the APIs required to handle change feed processing logic.
 */
public interface ChangeFeedContextClient {
    /**
     * Reads the feed (sequence) of {@link PartitionKeyRange} for a database account from the Azure Cosmos DB service as an asynchronous operation.
     *
     * @param partitionKeyRangesOrCollectionLink the link of the resources to be read, or owner collection link, SelfLink or AltLink. E.g. /dbs/db_rid/colls/coll_rid/pkranges.
     * @param feedOptions the options for the request; it can be set as null.
     * @return an a {@link Flux} containing one or several feed response pages of the obtained items or an error.
     */
    Flux<FeedResponse<PartitionKeyRange>> readPartitionKeyRangeFeed(String partitionKeyRangesOrCollectionLink, FeedOptions feedOptions);

    /**
     * Method to create a change feed query for documents.
     *
     * @param collectionLink Specifies the collection to read documents from.
     * @param feedOptions The options for processing the query results feed.
     * @return a {@link Flux} containing one or several feed response pages of the obtained items or an error.
     */
    Flux<FeedResponse<CosmosItemProperties>> createDocumentChangeFeedQuery(CosmosAsyncContainer collectionLink, ChangeFeedOptions feedOptions);

    /**
     * Reads a database.
     *
     * @param database a reference to the database.
     * @param options the {@link CosmosContainerRequestOptions} for this request; it can be set as null.
     * @return an {@link Mono} containing the single cosmos database response with the read database or an error.
     */
    Mono<CosmosAsyncDatabaseResponse> readDatabase(CosmosAsyncDatabase database, CosmosDatabaseRequestOptions options);

    /**
     * Reads a {@link CosmosAsyncContainer}.
     *
     * @param containerLink   a reference to the container.
     * @param options         the {@link CosmosContainerRequestOptions} for this request; it can be set as null.
     * @return an {@link Mono} containing the single cosmos container response with the read container or an error.
     */
    Mono<CosmosAsyncContainerResponse> readContainer(CosmosAsyncContainer containerLink, CosmosContainerRequestOptions options);

    /**
     * Creates a cosmos item.
     *
     * @param containerLink                the reference to the parent container.
     * @param document                     the document represented as a POJO or Document object.
     * @param options                      the request options.
     * @param disableAutomaticIdGeneration the flag for disabling automatic id generation.
     * @return an {@link Mono} containing the single resource response with the created cosmos item or an error.
     */
    <T> Mono<CosmosAsyncItemResponse<T>> createItem(CosmosAsyncContainer containerLink, T document,
                                              CosmosItemRequestOptions options,
                                             boolean disableAutomaticIdGeneration);

    /**
     * DELETE a cosmos item.
     *
     * @param itemId  the item id.
     * @param options   the request options.
     * @return an {@link Mono} containing the  cosmos item resource response with the deleted item or an error.
     */
    Mono<CosmosAsyncItemResponse> deleteItem(String itemId, PartitionKey partitionKey, 
                                             CosmosItemRequestOptions options);

    /**
     * Replaces a cosmos item.
     *
     * @param itemId     the item id.
     * @param document     the document represented as a POJO or Document object.
     * @param options      the request options.
     * @return an {@link Mono} containing the  cosmos item resource response with the replaced item or an error.
     */
    <T> Mono<CosmosAsyncItemResponse<T>> replaceItem(String itemId, PartitionKey partitionKey, T document,
                                              CosmosItemRequestOptions options);

    /**
     * Reads a cosmos item
     *
     * @param itemId     the item id.
     * @param options      the request options.
     * @return an {@link Mono} containing the  cosmos item resource response with the read item or an error.
     */
    <T> Mono<CosmosAsyncItemResponse<T>> readItem(String itemId, PartitionKey partitionKey,
                                             CosmosItemRequestOptions options, Class<T> itemType);

    /**
     * Query for items in a document container.
     *
     * @param containerLink  the reference to the parent container.
     * @param querySpec      the SQL query specification.
     * @param options        the feed options.
     * @return a {@link Flux} containing one or several feed response pages of the obtained items or an error.
     */
    <T> Flux<FeedResponse<T>> queryItems(CosmosAsyncContainer containerLink, SqlQuerySpec querySpec, FeedOptions options, Class<T> klass);

    /**
     * @return the Cosmos client's service endpoint.
     */
    URI getServiceEndpoint();

    /**
     * Reads and returns the container properties.
     *
     * @param containerLink   a reference to the container.
     * @param options         the {@link CosmosContainerRequestOptions} for this request; it can be set as null.
     * @return an {@link Mono} containing the read container properties.
     */
    Mono<CosmosContainerProperties> readContainerSettings(CosmosAsyncContainer containerLink, CosmosContainerRequestOptions options);

    /**
     * @return the Cosmos container client.
     */
    CosmosAsyncContainer getContainerClient();

    /**
     * @return the Cosmos database client.
     */
    CosmosAsyncDatabase getDatabaseClient();

    /**
     * Closes the document client instance and cleans up the resources.
     */
    void close();
}
