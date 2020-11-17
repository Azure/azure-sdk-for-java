// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosDatabaseRequestOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.fasterxml.jackson.databind.JsonNode;
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
     * @param cosmosQueryRequestOptions the options for the request; it can be set as null.
     * @return an a {@link Flux} containing one or several feed response pages of the obtained items or an error.
     */
    Flux<FeedResponse<PartitionKeyRange>> readPartitionKeyRangeFeed(String partitionKeyRangesOrCollectionLink, CosmosQueryRequestOptions cosmosQueryRequestOptions);

    /**
     * Method to create a change feed query for documents.
     *
     * @param collectionLink Specifies the collection to read documents from.
     * @param requestOptions The options for processing the query results feed.
     * @return a {@link Flux} containing one or several feed response pages of the obtained items or an error.
     */
    Flux<FeedResponse<JsonNode>> createDocumentChangeFeedQuery(CosmosAsyncContainer collectionLink,
                                                               CosmosChangeFeedRequestOptions requestOptions);

    /**
     * Reads a database.
     *
     * @param database a reference to the database.
     * @param options the {@link CosmosContainerRequestOptions} for this request; it can be set as null.
     * @return an {@link Mono} containing the single cosmos database response with the read database or an error.
     */
    Mono<CosmosDatabaseResponse> readDatabase(CosmosAsyncDatabase database, CosmosDatabaseRequestOptions options);

    /**
     * Reads a {@link CosmosAsyncContainer}.
     *
     * @param containerLink   a reference to the container.
     * @param options         the {@link CosmosContainerRequestOptions} for this request; it can be set as null.
     * @return an {@link Mono} containing the single cosmos container response with the read container or an error.
     */
    Mono<CosmosContainerResponse> readContainer(CosmosAsyncContainer containerLink, CosmosContainerRequestOptions options);

    /**
     * Creates a cosmos item.
     *
     * @param containerLink                the reference to the parent container.
     * @param document                     the document represented as a POJO or Document object.
     * @param options                      the request options.
     * @param disableAutomaticIdGeneration the flag for disabling automatic id generation.
     * @return an {@link Mono} containing the single resource response with the created cosmos item or an error.
     */
    <T> Mono<CosmosItemResponse<T>> createItem(CosmosAsyncContainer containerLink, T document,
                                               CosmosItemRequestOptions options,
                                               boolean disableAutomaticIdGeneration);

    /**
     * DELETE a cosmos item.
     *
     * @param itemId  the item id.
     * @param options   the request options.
     * @return an {@link Mono} containing the  cosmos item resource response with the deleted item or an error.
     */
    Mono<CosmosItemResponse<Object>> deleteItem(String itemId, PartitionKey partitionKey,
                                                CosmosItemRequestOptions options);

    /**
     * Replaces a cosmos item.
     *
     * @param itemId     the item id.
     * @param document     the document represented as a POJO or Document object.
     * @param options      the request options.
     * @return an {@link Mono} containing the  cosmos item resource response with the replaced item or an error.
     */
    <T> Mono<CosmosItemResponse<T>> replaceItem(String itemId, PartitionKey partitionKey, T document,
                                                CosmosItemRequestOptions options);

    /**
     * Reads a cosmos item
     *
     * @param itemId     the item id.
     * @param options      the request options.
     * @return an {@link Mono} containing the  cosmos item resource response with the read item or an error.
     */
    <T> Mono<CosmosItemResponse<T>> readItem(String itemId, PartitionKey partitionKey,
                                             CosmosItemRequestOptions options, Class<T> itemType);

    /**
     * Query for items in a container.
     *
     * @param containerLink  the reference to the parent container.
     * @param querySpec      the SQL query specification.
     * @param options        the query request options.
     * @return a {@link Flux} containing one or several feed response pages of the obtained items or an error.
     */
    <T> Flux<FeedResponse<T>> queryItems(CosmosAsyncContainer containerLink, SqlQuerySpec querySpec, CosmosQueryRequestOptions options, Class<T> klass);

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
