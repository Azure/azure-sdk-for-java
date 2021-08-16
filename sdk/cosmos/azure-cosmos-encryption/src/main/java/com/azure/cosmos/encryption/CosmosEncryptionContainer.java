// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.encryption.models.SqlQuerySpecWithEncryption;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.azure.cosmos.util.UtilBridgeInternal;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

/**
 * CosmosContainer with encryption capabilities.
 */
public class CosmosEncryptionContainer {
    private final CosmosContainer cosmosContainer;
    private final CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer;

    CosmosEncryptionContainer(CosmosContainer container,
                              CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer) {
        this.cosmosContainer = container;
        this.cosmosEncryptionAsyncContainer = cosmosEncryptionAsyncContainer;
    }

    /**
     * create item and encrypts the requested fields
     *
     * @param item           the Cosmos item represented as a POJO or Cosmos item object.
     * @param partitionKey   the partition key.
     * @param requestOptions request option
     * @param <T>            serialization class type
     * @return the Cosmos item resource response.
     */
    @SuppressWarnings("unchecked")
    public <T> CosmosItemResponse<T> createItem(T item,
                                                PartitionKey partitionKey,
                                                CosmosItemRequestOptions requestOptions) {
        return blockItemResponse(this.cosmosEncryptionAsyncContainer.createItem(item, partitionKey, requestOptions));
    }

    /**
     * Deletes the item.
     *
     * @param itemId         id of the item.
     * @param partitionKey   partitionKey of the item.
     * @param requestOptions the request options.
     * @return the Cosmos item resource response.
     */
    public CosmosItemResponse<Object> deleteItem(String itemId,
                                                 PartitionKey partitionKey,
                                                 CosmosItemRequestOptions requestOptions) {
        return blockDeleteItemResponse(this.cosmosEncryptionAsyncContainer.deleteItem(itemId, partitionKey,
            requestOptions));
    }

    /**
     * upserts item and encrypts the requested fields
     *
     * @param item           the Cosmos item represented as a POJO or Cosmos item object.
     * @param partitionKey   the partition key.
     * @param requestOptions request option
     * @param <T>            serialization class type
     * @return the Cosmos item resource response.
     */
    @SuppressWarnings("unchecked")
    public <T> CosmosItemResponse<T> upsertItem(T item,
                                                PartitionKey partitionKey,
                                                CosmosItemRequestOptions requestOptions) {
        return blockItemResponse(this.cosmosEncryptionAsyncContainer.upsertItem(item, partitionKey, requestOptions));
    }

    /**
     * replaces item and encrypts the requested fields
     *
     * @param item           the Cosmos item represented as a POJO or Cosmos item object.
     * @param itemId         the item id.
     * @param partitionKey   the partition key.
     * @param requestOptions request option
     * @param <T>            serialization class type
     * @return the Cosmos item resource response.
     */
    @SuppressWarnings("unchecked")
    public <T> CosmosItemResponse<T> replaceItem(T item,
                                                 String itemId,
                                                 PartitionKey partitionKey,
                                                 CosmosItemRequestOptions requestOptions) {
        return blockItemResponse(this.cosmosEncryptionAsyncContainer.replaceItem(item, itemId, partitionKey,
            requestOptions));
    }

    /**
     * Reads item and decrypt the encrypted fields
     *
     * @param id             item id
     * @param partitionKey   the partition key.
     * @param requestOptions request options
     * @param classType      deserialization class type
     * @param <T>            type
     * @return the Cosmos item resource response.
     */
    public <T> CosmosItemResponse<T> readItem(String id,
                                              PartitionKey partitionKey,
                                              CosmosItemRequestOptions requestOptions,
                                              Class<T> classType) {
        return blockItemResponse(this.cosmosEncryptionAsyncContainer.readItem(id, partitionKey, requestOptions,
            classType));
    }

    /**
     * Query for items in the current container using a string.
     *
     * @param <T>       the type parameter.
     * @param query     the query text.
     * @param options   the query request options.
     * @param classType the class type.
     * @return a {@link CosmosPagedIterable}.
     */
    public <T> CosmosPagedIterable<T> queryItems(String query, CosmosQueryRequestOptions options,
                                                 Class<T> classType) {

        return getCosmosPagedIterable(this.cosmosEncryptionAsyncContainer.queryItems(query, options, classType));
    }

    /**
     * Query for items in the current container using a {@link SqlQuerySpec}.
     *
     * @param <T>       the type parameter.
     * @param query     the query.
     * @param options   the query request options.
     * @param classType the class type.
     * @return a {@link CosmosPagedIterable}.
     */
    public <T> CosmosPagedIterable<T> queryItems(SqlQuerySpec query,
                                                 CosmosQueryRequestOptions options,
                                                 Class<T> classType) {
        return getCosmosPagedIterable(this.cosmosEncryptionAsyncContainer.queryItems(query, options, classType));
    }

    /**
     * Query for items in the current container using a {@link SqlQuerySpecWithEncryption}.
     *
     * @param <T>                        the type parameter.
     * @param sqlQuerySpecWithEncryption the sqlQuerySpecWithEncryption.
     * @param options                    the query request options.
     * @param classType                  the class type.
     * @return a {@link CosmosPagedIterable}.
     */
    public <T> CosmosPagedIterable<T> queryItemsOnEncryptedProperties(SqlQuerySpecWithEncryption sqlQuerySpecWithEncryption,
                                                                      CosmosQueryRequestOptions options,
                                                                      Class<T> classType) {
        return getCosmosPagedIterable(this.cosmosEncryptionAsyncContainer.queryItemsOnEncryptedProperties(sqlQuerySpecWithEncryption, options, classType));
    }

    /**
     * Gets the CosmosContainer
     *
     * @return cosmos container
     */
    public CosmosContainer getCosmosContainer() {
        return cosmosContainer;
    }

    private <T> CosmosItemResponse<T> blockItemResponse(Mono<CosmosItemResponse<T>> itemMono) {
        try {
            return itemMono.block();
        } catch (Exception ex) {
            if (ex instanceof CosmosException) {
                throw (CosmosException) ex;
            } else {
                throw ex;
            }
        }
    }

    private <T> CosmosPagedIterable<T> getCosmosPagedIterable(CosmosPagedFlux<T> cosmosPagedFlux) {
        return new CosmosPagedIterable<>(cosmosPagedFlux);
    }

    private CosmosItemResponse<Object> blockDeleteItemResponse(Mono<CosmosItemResponse<Object>> deleteItemMono) {
        try {
            return deleteItemMono.block();
        } catch (Exception ex) {
            if (ex instanceof CosmosException) {
                throw (CosmosException) ex;
            } else {
                throw ex;
            }
        }
    }
}
