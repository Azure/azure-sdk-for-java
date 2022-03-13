// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.encryption.models.SqlQuerySpecWithEncryption;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchOperationResult;
import com.azure.cosmos.models.CosmosBatchRequestOptions;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.CosmosPagedIterable;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * CosmosContainer with encryption capabilities.
 */
public final class CosmosEncryptionContainer {
    private final CosmosContainer cosmosContainer;
    private final CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer;

    CosmosEncryptionContainer(CosmosContainer container,
                              CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer) {
        this.cosmosContainer = container;
        this.cosmosEncryptionAsyncContainer = cosmosEncryptionAsyncContainer;
    }

    /**
     * Creates a new item synchronously and returns its respective Cosmos item response.
     *
     * @param <T> the type parameter
     * @param item the item
     * @return the Cosmos item response
     */
    public <T> CosmosItemResponse<T> createItem(T item) {
        return this.blockItemResponse(this.cosmosEncryptionAsyncContainer.createItem(item));
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
        return this.blockItemResponse(this.cosmosEncryptionAsyncContainer.createItem(item, options));
    }

    /**
     * Creates a new item synchronously and returns its respective Cosmos item response
     * while specifying additional options.
     *
     * @param <T> the type parameter.
     * @param item the item.
     * @param partitionKey the partition key.
     * @param requestOptions the options.
     * @return the Cosmos item response.
     */
    public <T> CosmosItemResponse<T> createItem(T item,
                                                PartitionKey partitionKey,
                                                CosmosItemRequestOptions requestOptions) {
        return blockItemResponse(this.cosmosEncryptionAsyncContainer.createItem(item, partitionKey, requestOptions));
    }

    /**
     * Deletes an item in the current container.
     *
     * @param itemId the item id.
     * @param partitionKey the partition key.
     * @param requestOptions the options.
     * @return the Cosmos item response.
     */
    public CosmosItemResponse<Object> deleteItem(String itemId,
                                                 PartitionKey partitionKey,
                                                 CosmosItemRequestOptions requestOptions) {
        return blockDeleteItemResponse(this.cosmosEncryptionAsyncContainer.deleteItem(itemId, partitionKey,
            requestOptions));
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
        return  this.blockDeleteItemResponse(this.cosmosEncryptionAsyncContainer.deleteItem(item, options));
    }

    /**
     * Deletes all items in the Container with the specified partitionKey value.
     * Starts an asynchronous Cosmos DB background operation which deletes all items in the Container with the specified value.
     * The asynchronous Cosmos DB background operation runs using a percentage of user RUs.
     *
     * @param partitionKey the partition key.
     * @param options the options.
     * @return the Cosmos item response
     */
    // TODO Make this api public once it is GA in cosmos core library
    CosmosItemResponse<Object> deleteAllItemsByPartitionKey(PartitionKey partitionKey, CosmosItemRequestOptions options) {
        return this.blockDeleteItemResponse(this.cosmosEncryptionAsyncContainer.deleteAllItemsByPartitionKey(partitionKey, options));
    }

    /**
     * Upserts an Cosmos item in the current container.
     *
     * @param <T> the type parameter.
     * @param item the item.
     * @return the Cosmos item response.
     */
    public <T> CosmosItemResponse<T> upsertItem(T item) {
        return this.blockItemResponse(this.cosmosEncryptionAsyncContainer.upsertItem(item));
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
        return this.blockItemResponse(this.cosmosEncryptionAsyncContainer.upsertItem(item, options));
    }

    /**
     * Upserts a item Cosmos sync item while specifying additional options.
     *
     * @param <T> the type parameter.
     * @param item the item.
     * @param partitionKey the partitionKey.
     * @param requestOptions the options.
     * @return the Cosmos item response.
     */
    @SuppressWarnings("unchecked")
    public <T> CosmosItemResponse<T> upsertItem(T item,
                                                PartitionKey partitionKey,
                                                CosmosItemRequestOptions requestOptions) {
        return blockItemResponse(this.cosmosEncryptionAsyncContainer.upsertItem(item, partitionKey, requestOptions));
    }

    /**
     * Replaces an item in the current container.
     *
     * @param <T> the type parameter.
     * @param item the item.
     * @param itemId the item id.
     * @param partitionKey the partition key.
     * @param requestOptions the options.
     * @return the Cosmos item response.
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
     * Reads an item in the current container.
     *
     * @param <T> the type parameter.
     * @param id the item id.
     * @param partitionKey the partition key.
     * @param classType the class type of item.
     * @return the Cosmos item response.
     */
    public <T> CosmosItemResponse<T> readItem(String id, PartitionKey partitionKey, Class<T> classType) {
        return this.blockItemResponse(this.cosmosEncryptionAsyncContainer.readItem(id,
            partitionKey,
            new CosmosItemRequestOptions(),
            classType));
    }

    /**
     * Reads an item in the current container while specifying additional options.
     *
     * @param <T> the type parameter.
     * @param id the item id.
     * @param partitionKey the partition key.
     * @param requestOptions the options.
     * @param classType the class type of item.
     * @return the Cosmos item response.
     */
    public <T> CosmosItemResponse<T> readItem(String id,
                                              PartitionKey partitionKey,
                                              CosmosItemRequestOptions requestOptions,
                                              Class<T> classType) {
        return blockItemResponse(this.cosmosEncryptionAsyncContainer.readItem(id, partitionKey, requestOptions,
            classType));
    }

    /**
     * Query items in the current container returning the results as {@link CosmosPagedIterable}.
     *
     * @param <T> the type parameter.
     * @param query the query.
     * @param requestOptions the options.
     * @param classType the class type.
     * @return the {@link CosmosPagedIterable}.
     */
    public <T> CosmosPagedIterable<T> queryItems(String query, CosmosQueryRequestOptions requestOptions,
                                                 Class<T> classType) {

        return getCosmosPagedIterable(this.cosmosEncryptionAsyncContainer.queryItems(query, requestOptions, classType));
    }

    /**
     * Query items in the current container returning the results as {@link CosmosPagedIterable}.
     *
     * @param <T> the type parameter.
     * @param query the query spec.
     * @param requestOptions the options.
     * @param classType the class type.
     * @return the {@link CosmosPagedIterable}.
     */
    public <T> CosmosPagedIterable<T> queryItems(SqlQuerySpec query,
                                                 CosmosQueryRequestOptions requestOptions,
                                                 Class<T> classType) {
        return getCosmosPagedIterable(this.cosmosEncryptionAsyncContainer.queryItems(query, requestOptions, classType));
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
     * Query for items in the change feed of the current container using the {@link CosmosChangeFeedRequestOptions}.
     * <p>
     * The next page can be retrieved by calling queryChangeFeed again with a new instance of
     * {@link CosmosChangeFeedRequestOptions} created from the continuation token of the previously returned
     * {@link FeedResponse} instance.
     *
     * @param <T> the type parameter.
     * @param options the change feed request options.
     * @param classType the class type.
     * @return a {@link CosmosPagedFlux} containing one feed response page
     */
    // TODO Make this api public once it is GA in cosmos core library
    <T> CosmosPagedIterable<T> queryChangeFeed(
        CosmosChangeFeedRequestOptions options,
        Class<T> classType) {
        checkNotNull(options, "Argument 'options' must not be null.");
        checkNotNull(classType, "Argument 'classType' must not be null.");

        options.setMaxPrefetchPageCount(1);

        return getCosmosPagedIterable(
            this.cosmosEncryptionAsyncContainer
                .queryChangeFeed(options, classType));
    }

    /**
     * Executes the encrypted transactional batch.
     *
     * @param cosmosBatch Batch having list of operation and partition key which will be executed by this container.
     *
     * @return A CosmosBatchResponse which contains details of execution of the transactional batch.
     * <p>
     * If the transactional batch executes successfully, the value returned by {@link
     * CosmosBatchResponse#getStatusCode} on the response returned will be set to 200}.
     * <p>
     * If an operation within the transactional batch fails during execution, no changes from the batch will be
     * committed and the status of the failing operation is made available by {@link
     * CosmosBatchResponse#getStatusCode} or by the exception. To obtain information about the operations
     * that failed in case of some user error like conflict, not found etc, the response can be enumerated.
     * This returns {@link CosmosBatchOperationResult} instances corresponding to each operation in the
     * transactional batch in the order they were added to the transactional batch.
     * For a result corresponding to an operation within the transactional batch, use
     * {@link CosmosBatchOperationResult#getStatusCode}
     * to access the status of the operation. If the operation was not executed or it was aborted due to the failure of
     * another operation within the transactional batch, the value of this field will be 424;
     * for the operation that caused the batch to abort, the value of this field
     * will indicate the cause of failure.
     * <p>
     * If there are issues such as request timeouts, Gone, session not available, network failure
     * or if the service somehow returns 5xx then this will throw an exception instead of returning a CosmosBatchResponse.
     * <p>
     * Use {@link CosmosBatchResponse#isSuccessStatusCode} on the response returned to ensure that the
     * transactional batch succeeded.
     */
    public CosmosBatchResponse executeCosmosBatch(CosmosBatch cosmosBatch) {
        return this.blockBatchResponse(this.cosmosEncryptionAsyncContainer.executeCosmosBatch(cosmosBatch));
    }

    /**
     * Executes the encrypted transactional batch.
     *
     * @param cosmosBatch Batch having list of operation and partition key which will be executed by this container.
     * @param requestOptions Options that apply specifically to batch request.
     *
     * @return A CosmosBatchResponse which contains details of execution of the transactional batch.
     * <p>
     * If the transactional batch executes successfully, the value returned by {@link
     * CosmosBatchResponse#getStatusCode} on the response returned will be set to 200}.
     * <p>
     * If an operation within the transactional batch fails during execution, no changes from the batch will be
     * committed and the status of the failing operation is made available by {@link
     * CosmosBatchResponse#getStatusCode} or by the exception. To obtain information about the operations
     * that failed in case of some user error like conflict, not found etc, the response can be enumerated.
     * This returns {@link CosmosBatchOperationResult} instances corresponding to each operation in the
     * transactional batch in the order they were added to the transactional batch.
     * For a result corresponding to an operation within the transactional batch, use
     * {@link CosmosBatchOperationResult#getStatusCode}
     * to access the status of the operation. If the operation was not executed or it was aborted due to the failure of
     * another operation within the transactional batch, the value of this field will be 424;
     * for the operation that caused the batch to abort, the value of this field
     * will indicate the cause of failure.
     * <p>
     * If there are issues such as request timeouts, Gone, session not available, network failure
     * or if the service somehow returns 5xx then this will throw an exception instead of returning a CosmosBatchResponse.
     * <p>
     * Use {@link CosmosBatchResponse#isSuccessStatusCode} on the response returned to ensure that the
     * transactional batch succeeded.
     */
    public CosmosBatchResponse executeCosmosBatch(
        CosmosBatch cosmosBatch,
        CosmosBatchRequestOptions requestOptions) {
        return this.blockBatchResponse(this.cosmosEncryptionAsyncContainer.executeCosmosBatch(cosmosBatch, requestOptions));
    }

    /**
     * Run patch operations on an Item.
     *
     * @param <T> the type parameter.
     * @param itemId the item id.
     * @param partitionKey the partition key.
     * @param cosmosPatchOperations Represents a container having list of operations to be sequentially applied to the referred Cosmos item.
     * @param options the request options.
     * @param itemType the item type.
     *
     * @return the Cosmos item resource response with the patched item or an exception.
     */
    public <T> CosmosItemResponse<T> patchItem(
        String itemId,
        PartitionKey partitionKey,
        CosmosPatchOperations cosmosPatchOperations,
        CosmosPatchItemRequestOptions options,
        Class<T> itemType) {

        return this.blockItemResponse(this.cosmosEncryptionAsyncContainer.patchItem(itemId, partitionKey, cosmosPatchOperations, options, itemType));
    }

    /**
     * Executes list of operations in Bulk.
     *
     * @param <TContext> The context for the bulk processing.
     * @param operations list of operation which will be executed by this container.
     *
     * @return An Iterable of {@link CosmosBulkOperationResponse} which contains operation and it's response or exception.
     * <p>
     *     To create a operation which can be executed here, use {@link com.azure.cosmos.models.CosmosBulkOperations}. For eg.
     *     for a upsert operation use {@link com.azure.cosmos.models.CosmosBulkOperations#getUpsertItemOperation(Object, PartitionKey)}
     * </p>
     * <p>
     *     We can get the corresponding operation using {@link CosmosBulkOperationResponse#getOperation()} and
     *     it's response using {@link CosmosBulkOperationResponse#getResponse()}. If the operation was executed
     *     successfully, the value returned by {@link com.azure.cosmos.models.CosmosBulkItemResponse#isSuccessStatusCode()} will be true. To get
     *     actual status use {@link com.azure.cosmos.models.CosmosBulkItemResponse#getStatusCode()}.
     * </p>
     * To check if the operation had any exception, use {@link CosmosBulkOperationResponse#getException()} to
     * get the exception.
     */
    public <TContext> Iterable<CosmosBulkOperationResponse<TContext>> executeBulkOperations(
        Iterable<CosmosItemOperation> operations) {
        return this.blockBulkResponse(this.cosmosEncryptionAsyncContainer.executeBulkOperations(Flux.fromIterable(operations)));
    }

    /**
     * Executes list of operations in Bulk.
     *
     * @param <TContext> The context for the bulk processing.
     *
     * @param operations list of operation which will be executed by this container.
     * @param bulkOptions Options that apply for this Bulk request which specifies options regarding execution like
     *                    concurrency, batching size, interval and context.
     *
     * @return An Iterable of {@link CosmosBulkOperationResponse} which contains operation and it's response or exception.
     * <p>
     *     To create a operation which can be executed here, use {@link com.azure.cosmos.models.CosmosBulkOperations}. For eg.
     *     for a upsert operation use {@link com.azure.cosmos.models.CosmosBulkOperations#getUpsertItemOperation(Object, PartitionKey)}
     * </p>
     * <p>
     *     We can get the corresponding operation using {@link CosmosBulkOperationResponse#getOperation()} and
     *     it's response using {@link CosmosBulkOperationResponse#getResponse()}. If the operation was executed
     *     successfully, the value returned by {@link com.azure.cosmos.models.CosmosBulkItemResponse#isSuccessStatusCode()} will be true. To get
     *     actual status use {@link com.azure.cosmos.models.CosmosBulkItemResponse#getStatusCode()}.
     * </p>
     * To check if the operation had any exception, use {@link CosmosBulkOperationResponse#getException()} to
     * get the exception.
     */
    public <TContext> Iterable<CosmosBulkOperationResponse<TContext>> executeBulkOperations(
        Iterable<CosmosItemOperation> operations,
        CosmosBulkExecutionOptions bulkOptions) {
        return this.blockBulkResponse(this.cosmosEncryptionAsyncContainer.executeBulkOperations(Flux.fromIterable(operations), bulkOptions));
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

    private CosmosBatchResponse blockBatchResponse(Mono<CosmosBatchResponse> batchResponseMono) {
        try {
            return batchResponseMono.block();
        } catch (Exception ex) {
            final Throwable throwable = Exceptions.unwrap(ex);
            if (throwable instanceof CosmosException) {
                throw (CosmosException) throwable;
            } else {
                throw ex;
            }
        }
    }

    private <TContext> List<CosmosBulkOperationResponse<TContext>> blockBulkResponse(
        Flux<CosmosBulkOperationResponse<TContext>> bulkResponse) {

        try {
            return bulkResponse.collectList().block();
        } catch (Exception ex) {
            final Throwable throwable = Exceptions.unwrap(ex);
            if (throwable instanceof CosmosException) {
                throw (CosmosException) throwable;
            } else {
                throw ex;
            }
        }
    }
}
