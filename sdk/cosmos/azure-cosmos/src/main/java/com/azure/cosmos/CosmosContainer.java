// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.throughputControl.config.GlobalThroughputControlGroup;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

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

    private TransactionalBatchResponse blockBatchResponse(Mono<TransactionalBatchResponse> batchResponseMono) {
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
    @Beta(value = Beta.SinceVersion.V4_12_0, warningText =
        Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public <T> CosmosPagedIterable<T> queryChangeFeed(
        CosmosChangeFeedRequestOptions options,
        Class<T> classType) {

        checkNotNull(options, "Argument 'options' must not be null.");
        checkNotNull(classType, "Argument 'classType' must not be null.");

        options.setMaxPrefetchPageCount(1);

        return getCosmosPagedIterable(
            this.asyncContainer
                .queryChangeFeed(options, classType));
    }

    /**
     * Reads many documents.
     *
     * @param <T> the type parameter
     * @param itemIdentityList CosmosItem id and partition key tuple of items that that needs to be read
     * @param classType   class type
     * @return a Mono with feed response of cosmos items
     */
    public <T> FeedResponse<T> readMany(
        List<CosmosItemIdentity> itemIdentityList,
        Class<T> classType) {

        return this.readMany(itemIdentityList, null, classType);
    }

    /**
     * Reads many documents.
     *
     * @param <T> the type parameter
     * @param itemIdentityList CosmosItem id and partition key tuple of items that that needs to be read
     * @param sessionToken the optional Session token - null if the read can be made without specific session token
     * @param classType   class type
     * @return a Mono with feed response of cosmos items
     */
    public <T> FeedResponse<T> readMany(
        List<CosmosItemIdentity> itemIdentityList,
        String sessionToken,
        Class<T> classType) {

        return this.blockFeedResponse(
            this.asyncContainer.readMany(
                itemIdentityList,
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
     * Run patch operations on an Item.
     *
     * @param <T> the type parameter.
     * @param itemId the item id.
     * @param partitionKey the partition key.
     * @param cosmosPatchOperations Represents a container having list of operations to be sequentially applied to the referred Cosmos item.
     * @param itemType the item type.
     *
     * @return the Cosmos item resource response with the patched item or an exception.
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public <T> CosmosItemResponse<T> patchItem(
        String itemId,
        PartitionKey partitionKey,
        CosmosPatchOperations cosmosPatchOperations,
        Class<T> itemType) {

        return this.blockItemResponse(asyncContainer.patchItem(itemId, partitionKey, cosmosPatchOperations, itemType));
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
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public <T> CosmosItemResponse<T> patchItem(
        String itemId,
        PartitionKey partitionKey,
        CosmosPatchOperations cosmosPatchOperations,
        CosmosPatchItemRequestOptions options,
        Class<T> itemType) {

        return this.blockItemResponse(asyncContainer.patchItem(itemId, partitionKey, cosmosPatchOperations, options, itemType));
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

    public CosmosItemResponse<Object> deleteAllItemsByPartitionKeyAsync(PartitionKey partitionKey, CosmosItemRequestOptions options) {
        return this.blockDeleteItemResponse(asyncContainer.deleteAllItemsByPartitionKeyAsync(partitionKey, options));
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
     * Executes the transactional batch.
     *
     * @param transactionalBatch Batch having list of operation and partition key which will be executed by this container.
     *
     * @return A TransactionalBatchResponse which contains details of execution of the transactional batch.
     * <p>
     * If the transactional batch executes successfully, the value returned by {@link
     * TransactionalBatchResponse#getStatusCode} on the response returned will be set to 200}.
     * <p>
     * If an operation within the transactional batch fails during execution, no changes from the batch will be
     * committed and the status of the failing operation is made available by {@link
     * TransactionalBatchResponse#getStatusCode} or by the exception. To obtain information about the operations
     * that failed in case of some user error like conflict, not found etc, the response can be enumerated.
     * This returns {@link TransactionalBatchOperationResult} instances corresponding to each operation in the
     * transactional batch in the order they were added to the transactional batch.
     * For a result corresponding to an operation within the transactional batch, use
     * {@link TransactionalBatchOperationResult#getStatusCode}
     * to access the status of the operation. If the operation was not executed or it was aborted due to the failure of
     * another operation within the transactional batch, the value of this field will be 424;
     * for the operation that caused the batch to abort, the value of this field
     * will indicate the cause of failure.
     * <p>
     * If there are issues such as request timeouts, Gone, session not available, network failure
     * or if the service somehow returns 5xx then this will throw an exception instead of returning a TransactionalBatchResponse.
     * <p>
     * Use {@link TransactionalBatchResponse#isSuccessStatusCode} on the response returned to ensure that the
     * transactional batch succeeded.
     */
    @Beta(value = Beta.SinceVersion.V4_7_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public TransactionalBatchResponse executeTransactionalBatch(TransactionalBatch transactionalBatch) {
        return this.blockBatchResponse(asyncContainer.executeTransactionalBatch(transactionalBatch));
    }

    /**
     * Executes the transactional batch.
     *
     * @param transactionalBatch Batch having list of operation and partition key which will be executed by this container.
     * @param requestOptions Options that apply specifically to batch request.
     *
     * @return A TransactionalBatchResponse which contains details of execution of the transactional batch.
     * <p>
     * If the transactional batch executes successfully, the value returned by {@link
     * TransactionalBatchResponse#getStatusCode} on the response returned will be set to 200}.
     * <p>
     * If an operation within the transactional batch fails during execution, no changes from the batch will be
     * committed and the status of the failing operation is made available by {@link
     * TransactionalBatchResponse#getStatusCode} or by the exception. To obtain information about the operations
     * that failed in case of some user error like conflict, not found etc, the response can be enumerated.
     * This returns {@link TransactionalBatchOperationResult} instances corresponding to each operation in the
     * transactional batch in the order they were added to the transactional batch.
     * For a result corresponding to an operation within the transactional batch, use
     * {@link TransactionalBatchOperationResult#getStatusCode}
     * to access the status of the operation. If the operation was not executed or it was aborted due to the failure of
     * another operation within the transactional batch, the value of this field will be 424;
     * for the operation that caused the batch to abort, the value of this field
     * will indicate the cause of failure.
     * <p>
     * If there are issues such as request timeouts, Gone, session not available, network failure
     * or if the service somehow returns 5xx then this will throw an exception instead of returning a TransactionalBatchResponse.
     * <p>
     * Use {@link TransactionalBatchResponse#isSuccessStatusCode} on the response returned to ensure that the
     * transactional batch succeeded.
     */
    @Beta(value = Beta.SinceVersion.V4_7_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public TransactionalBatchResponse executeTransactionalBatch(
        TransactionalBatch transactionalBatch,
        TransactionalBatchRequestOptions requestOptions) {

        return this.blockBatchResponse(asyncContainer.executeTransactionalBatch(transactionalBatch, requestOptions));
    }

    /**
     * Executes list of operations in Bulk.
     *
     * @param <TContext> The context for the bulk processing.
     * @param operations list of operation which will be executed by this container.
     *
     * @return A list of {@link CosmosBulkOperationResponse} which contains operation and it's response or exception.
     * <p>
     *     To create a operation which can be executed here, use {@link BulkOperations}. For eg.
     *     for a upsert operation use {@link BulkOperations#getUpsertItemOperation(Object, PartitionKey)}
     * </p>
     * <p>
     *     We can get the corresponding operation using {@link CosmosBulkOperationResponse#getOperation()} and
     *     it's response using {@link CosmosBulkOperationResponse#getResponse()}. If the operation was executed
     *     successfully, the value returned by {@link CosmosBulkItemResponse#isSuccessStatusCode()} will be true. To get
     *     actual status use {@link CosmosBulkItemResponse#getStatusCode()}.
     * </p>
     * To check if the operation had any exception, use {@link CosmosBulkOperationResponse#getException()} to
     * get the exception.
     */
    @Beta(value = Beta.SinceVersion.V4_9_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public <TContext> List<CosmosBulkOperationResponse<TContext>> processBulkOperations(
        Iterable<CosmosItemOperation> operations) {

        return this.blockBulkResponse(asyncContainer.processBulkOperations(Flux.fromIterable(operations)));
    }

    /**
     * Executes list of operations in Bulk.
     *
     * @deprecated forRemoval = true, since = "4.18"
     * This overload will be removed. Please use one of the following overloads instead
     * - {@link CosmosAsyncContainer#processBulkOperations(Flux)}
     * - {@link CosmosAsyncContainer#processBulkOperations(Flux, BulkExecutionOptions)}
     * - {@link CosmosContainer#processBulkOperations(Iterable)}
     * - {@link CosmosContainer#processBulkOperations(Iterable, BulkExecutionOptions)}
     * and to pass in a custom context use one of the {@link BulkOperations} factory methods allowing to provide
     * an operation specific context
     *
     * @param <TContext> The context for the bulk processing.
     *
     * @param operations list of operation which will be executed by this container.
     * @param bulkOptions Options that apply for this Bulk request which specifies options regarding execution like
     *                    concurrency, batching size, interval and context.
     *
     * @return A list of {@link CosmosBulkOperationResponse} which contains operation and it's response or exception.
     * <p>
     *     To create a operation which can be executed here, use {@link BulkOperations}. For eg.
     *     for a upsert operation use {@link BulkOperations#getUpsertItemOperation(Object, PartitionKey)}
     * </p>
     * <p>
     *     We can get the corresponding operation using {@link CosmosBulkOperationResponse#getOperation()} and
     *     it's response using {@link CosmosBulkOperationResponse#getResponse()}. If the operation was executed
     *     successfully, the value returned by {@link CosmosBulkItemResponse#isSuccessStatusCode()} will be true. To get
     *     actual status use {@link CosmosBulkItemResponse#getStatusCode()}.
     * </p>
     * To check if the operation had any exception, use {@link CosmosBulkOperationResponse#getException()} to
     * get the exception.
     */
    @Beta(value = Beta.SinceVersion.V4_9_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    @Deprecated() //forRemoval = true, since = "4.18"
    public <TContext> List<CosmosBulkOperationResponse<TContext>> processBulkOperations(
        Iterable<CosmosItemOperation> operations,
        BulkProcessingOptions<TContext> bulkOptions) {

        return this.blockBulkResponse(asyncContainer.processBulkOperations(Flux.fromIterable(operations), bulkOptions));
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
     * @return A list of {@link CosmosBulkOperationResponse} which contains operation and it's response or exception.
     * <p>
     *     To create a operation which can be executed here, use {@link BulkOperations}. For eg.
     *     for a upsert operation use {@link BulkOperations#getUpsertItemOperation(Object, PartitionKey)}
     * </p>
     * <p>
     *     We can get the corresponding operation using {@link CosmosBulkOperationResponse#getOperation()} and
     *     it's response using {@link CosmosBulkOperationResponse#getResponse()}. If the operation was executed
     *     successfully, the value returned by {@link CosmosBulkItemResponse#isSuccessStatusCode()} will be true. To get
     *     actual status use {@link CosmosBulkItemResponse#getStatusCode()}.
     * </p>
     * To check if the operation had any exception, use {@link CosmosBulkOperationResponse#getException()} to
     * get the exception.
     */
    @Beta(value = Beta.SinceVersion.V4_18_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public <TContext> List<CosmosBulkOperationResponse<TContext>> processBulkOperations(
        Iterable<CosmosItemOperation> operations,
        BulkExecutionOptions bulkOptions) {

        return this.blockBulkResponse(asyncContainer.processBulkOperations(Flux.fromIterable(operations), bulkOptions));
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
        return new CosmosPagedIterable<>(cosmosPagedFlux);
    }

    /**
     * Obtains a list of {@link FeedRange} that can be used to parallelize Feed
     * operations.
     *
     * @return An unmodifiable list of {@link FeedRange}
     */
    @Beta(value = Beta.SinceVersion.V4_9_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public List<FeedRange> getFeedRanges() {
        try {
            return asyncContainer.getFeedRanges().block();
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
     * Enable the throughput control group with local control mode.
     *
     * {@codesnippet com.azure.cosmos.throughputControl.localControl}
     *
     * @param groupConfig A {@link GlobalThroughputControlConfig}.
     */
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public void enableLocalThroughputControlGroup(ThroughputControlGroupConfig groupConfig) {
        this.asyncContainer.enableLocalThroughputControlGroup(groupConfig);
    }

    /**
     * Enable the throughput control group with global control mode.
     * The defined throughput limit will be shared across different clients.
     *
     * {@codesnippet com.azure.cosmos.throughputControl.globalControl}
     *
     * @param groupConfig The throughput control group configuration, see {@link GlobalThroughputControlGroup}.
     * @param globalControlConfig The global throughput control configuration, see {@link GlobalThroughputControlConfig}.
     */
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public void enableGlobalThroughputControlGroup(ThroughputControlGroupConfig groupConfig, GlobalThroughputControlConfig globalControlConfig) {
        this.asyncContainer.enableGlobalThroughputControlGroup(groupConfig, globalControlConfig);
    }

    /**
     * Initializes the container by warming up the caches and connections for the current read region.
     *
     * <p><br>The execution of this method is expected to result in some RU charges to your account.
     * The number of RU consumed by this request varies, depending on data consistency, size of the overall data in the container,
     * item indexing, number of projections. For more information regarding RU considerations please visit
     * <a href="https://docs.microsoft.com/en-us/azure/cosmos-db/request-units#request-unit-considerations">https://docs.microsoft.com/en-us/azure/cosmos-db/request-units#request-unit-considerations</a>.
     * </p>
     *
     * <p>
     * <br>NOTE: This API ideally should be called only once during application initialization before any workload.
     * <br>In case of any transient error, caller should consume the error and continue the regular workload.
     * </p>
     *
     */
    @Beta(value = Beta.SinceVersion.V4_14_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public void openConnectionsAndInitCaches() {
        blockVoidResponse(this.asyncContainer.openConnectionsAndInitCaches());
    }

    private void blockVoidResponse(Mono<Void> voidMono) {
        try {
            voidMono.block();
        } catch (Exception ex) {
            final Throwable throwable = Exceptions.unwrap(ex);
            if (throwable instanceof CosmosException) {
                throw (CosmosException) throwable;
            } else {
                throw Exceptions.propagate(ex);
            }
        }
    }
}
