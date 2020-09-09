// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import com.azure.cosmos.models.PartitionKey;
import reactor.core.publisher.Mono;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Represents a batch of operations against items with the same {@link PartitionKey} in a container that will be performed
 * in a transactional manner at the Azure Cosmos DB service.
 * <p>
 * Use {@link com.azure.cosmos.CosmosAsyncContainer#createTransactionalBatch(PartitionKey)} to create an instance of
 * TransactionalBatch
 * <b>Example</b>
 * This example atomically modifies a set of documents as a batch.
 * <pre>{@code
 * public class ToDoActivity {
 *     public final String type;
 *     public final String id;
 *     public final String status;
 *     public ToDoActivity(String type, String id, String status) {
 *         this.type = type;
 *         this.id = id;
 *         this.status = status;
 *     }
 * }
 *
 * String activityType = "personal";
 *
 * ToDoActivity test1 = new ToDoActivity(activityType, "learning", "ToBeDone");
 * ToDoActivity test2 = new ToDoActivity(activityType, "shopping", "Done");
 * ToDoActivity test3 = new ToDoActivity(activityType, "swimming", "ToBeDone");
 *
 * try (TransactionalBatchResponse response = container.createTransactionalBatch(new Cosmos.PartitionKey(activityType))
 *     .createItem<ToDoActivity>(test1)
 *     .replaceItem<ToDoActivity>(test2.id, test2)
 *     .upsertItem<ToDoActivity>(test3)
 *     .deleteItem("reading")
 *     .ExecuteAsync()) {
 *
 *     if (!response.IsSuccessStatusCode) {
 *        // Handle and log exception
 *        return;
 *     }
 *
 *     // Look up interested results - e.g., via typed access on operation results
 *
 *     TransactionalBatchOperationResult<ToDoActivity> result = response.getOperationResultAtIndex<ToDoActivity>(0, ToDoActivity.class);
 *     ToDoActivity readActivity = result.getResource();
 * }
 * }</pre>
 *
 * <b>Example</b>
 * <p>This example atomically reads a set of documents as a batch.
 *
 * <pre>{@code
 * String activityType = "personal";
 *
 * try (TransactionalBatchResponse response = container.createTransactionalBatch(new Cosmos.PartitionKey(activityType))
 *    .readItem("playing")
 *    .readItem("walking")
 *    .readItem("jogging")
 *    .readItem("running")
 *    .ExecuteAsync()) {
 *
 *     // Look up interested results - eg. via direct access to operation result stream
 *
 *     List<String> resultItems = new ArrayList<String>();
 *
 *     for (TransactionalBatchOperationResult result : response) {
 *         resultItems.add(result.getResourceObject().toString())
 *     }
 * }
 * }</pre>
 * <p>
 * <b>See:</b>
 * <a href="https://docs.microsoft.com/azure/cosmos-db/concepts-limits">Limits on TransactionalBatch requests</a>.
 */
public interface TransactionalBatch {

    /**
     * Adds an operation to create an item into the batch.
     *
     * @param item A JSON serializable object that must contain an id property.
     * @param <T> The type of item to be created.
     *
     * @return The transactional batch instance with the operation added.
     */
    default <T> TransactionalBatch createItem(T item) {
        checkNotNull(item, "expected non-null item");
        return this.createItem(item, new TransactionalBatchItemRequestOptions());
    }

    /**
     * Adds an operation to create an item into the batch.
     *
     * @param <T> The type of item to be created.
     *
     * @param item A JSON serializable object that must contain an id property.
     * @param requestOptions The options for the item request.
     * @return The transactional batch instance with the operation added.
     */
    <T> TransactionalBatch createItem(T item, TransactionalBatchItemRequestOptions requestOptions);

    /**
     * Adds an operation to delete an item into the batch.
     *
     * @param id The unique id of the item.
     *
     * @return The transactional batch instance with the operation added.
     */
    default TransactionalBatch deleteItem(String id) {
        checkNotNull(id, "expected non-null id");
        return this.deleteItem(id, new TransactionalBatchItemRequestOptions());
    }

    /**
     * Adds an operation to delete an item into the batch.
     *
     * @param id The unique id of the item.
     * @param requestOptions The options for the item request.
     *
     * @return The transactional batch instance with the operation added.
     */
    TransactionalBatch deleteItem(String id, TransactionalBatchItemRequestOptions requestOptions);

    /**
     * Executes the transactional batch at the Azure Cosmos service as an asynchronous operation.
     *
     * @return A Mono response which contains details of execution of the transactional batch.
     * <p>
     * If the transactional batch executes successfully, the value returned by {@link
     * TransactionalBatchResponse#getResponseStatus} on the response returned will be set to 200}.
     * <p>
     * If an operation within the transactional batch fails during execution, no changes from the batch will be
     * committed and the status of the failing operation is made available by {@link
     * TransactionalBatchResponse#getResponseStatus}. To obtain information about the operations that failed, the
     * response can be enumerated. This returns {@link TransactionalBatchOperationResult} instances corresponding to
     * each operation in the transactional batch in the order they were added to the transactional batch. For a result
     * corresponding to an operation within the transactional batch, use
     * {@link TransactionalBatchOperationResult#getResponseStatus}
     * to access the status of the operation. If the operation was not executed or it was aborted due to the failure of
     * another operation within the transactional batch, the value of this field will be 424;
     * for the operation that caused the batch to abort, the value of this field
     * will indicate the cause of failure.
     * <p>
     * The value returned by {@link TransactionalBatchResponse#getResponseStatus} on the response returned may also have
     * values such as 500 in case of server errors and 429.
     * <p>
     * Use {@link TransactionalBatchResponse#isSuccessStatusCode} on the response returned to ensure that the
     * transactional batch succeeded.
     */
    Mono<TransactionalBatchResponse> executeAsync();

    /**
     * Executes the transactional batch at the Azure Cosmos service as an asynchronous operation.
     *
     * @param requestOptions Options that apply specifically to batch request.
     *
     * @return A Mono response which contains details of execution of the transactional batch.
     * <p>
     * If the transactional batch executes successfully, the value returned by {@link
     * TransactionalBatchResponse#getResponseStatus} on the response returned will be set to 200}.
     * <p>
     * If an operation within the transactional batch fails during execution, no changes from the batch will be
     * committed and the status of the failing operation is made available by {@link
     * TransactionalBatchResponse#getResponseStatus}. To obtain information about the operations that failed, the
     * response can be enumerated. This returns {@link TransactionalBatchOperationResult} instances corresponding to
     * each operation in the transactional batch in the order they were added to the transactional batch. For a result
     * corresponding to an operation within the transactional batch, use
     * {@link TransactionalBatchOperationResult#getResponseStatus}
     * to access the status of the operation. If the operation was not executed or it was aborted due to the failure of
     * another operation within the transactional batch, the value of this field will be 424;
     * for the operation that caused the batch to abort, the value of this field
     * will indicate the cause of failure.
     * <p>
     * The value returned by {@link TransactionalBatchResponse#getResponseStatus} on the response returned may also have
     * values such as 500 in case of server errors and 429.
     * <p>
     * Use {@link TransactionalBatchResponse#isSuccessStatusCode} on the response returned to ensure that the
     * transactional batch succeeded.
     */
    Mono<TransactionalBatchResponse> executeAsync(TransactionalBatchRequestOptions requestOptions);

    /**
     * Adds an operation to read an item into the batch.
     *
     * @param id The unique id of the item.
     *
     * @return The transactional batch instance with the operation added.
     */
    default TransactionalBatch readItem(String id) {
        checkNotNull(id, "expected non-null id");
        return this.readItem(id, new TransactionalBatchItemRequestOptions());
    }

    /**
     * Adds an operation to read an item into the batch.
     *
     * @param id The unique id of the item.
     * @param requestOptions The options for the item request.
     *
     * @return The transactional batch instance with the operation added.
     */
    TransactionalBatch readItem(String id, TransactionalBatchItemRequestOptions requestOptions);

    /**
     * Adds an operation to replace an item into the batch.
     *
     * @param id The unique id of the item.
     * @param item A JSON serializable object that must contain an id property.
     * @param <TItem> The type of item to be created.
     *
     * @return The transactional batch instance with the operation added.
     */
    default <TItem> TransactionalBatch replaceItem(String id, TItem item) {
        checkNotNull(id, "expected non-null id");
        checkNotNull(item, "expected non-null item");
        return this.replaceItem(id, item, new TransactionalBatchItemRequestOptions());
    }

    /**
     * Adds an operation to replace an item into the batch.
     *
     * @param <TItem> The type of item to be created.
     *
     * @param id The unique id of the item.
     * @param item A JSON serializable object that must contain an id property.
     * @param requestOptions The options for the item request.
     * @return The transactional batch instance with the operation added.
     */
    <TItem> TransactionalBatch replaceItem(
        String id, TItem item, TransactionalBatchItemRequestOptions requestOptions);

    /**
     * Adds an operation to upsert an item into the batch.
     *
     * @param item A JSON serializable object that must contain an id property.
     * @param <TItem> The type of item to be created.
     *
     * @return The transactional batch instance with the operation added.
     */
    default <TItem> TransactionalBatch upsertItem(TItem item) {
        checkNotNull(item, "expected non-null item");
        return this.upsertItem(item, new TransactionalBatchItemRequestOptions());
    }

    /**
     * Adds an operation to upsert an item into the batch.
     *
     * @param <TItem> The type of item to be created.
     *
     * @param item A JSON serializable object that must contain an id property.
     * @param requestOptions The options for the item request.
     * @return The transactional batch instance with the operation added.
     */
    <TItem> TransactionalBatch upsertItem(TItem item, TransactionalBatchItemRequestOptions requestOptions);
}
