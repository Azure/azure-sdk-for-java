// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.models.PartitionKey;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpStatusClass;
import reactor.core.publisher.Mono;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Represents a batch of operations against items with the same {@link PartitionKey} in a container.
 * <p>
 * The batch operations will be performed in a transactional manner at the Azure Cosmos DB service. Use
 * <h3>Example</h3>
 * <p>
 * This example atomically modifies a set of documents as a batch.<pre>{@code
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
 * try (TransactionalBatchResponse response = container.CreateTransactionalBatch(new Cosmos.PartitionKey(activityType))
 *     .CreateItem<ToDoActivity>(test1)
 *     .ReplaceItem<ToDoActivity>(test2.id, test2)
 *     .UpsertItem<ToDoActivity>(test3)
 *     .DeleteItem("reading")
 *     .CreateItemStream(streamPayload1)
 *     .ReplaceItemStream("eating", streamPayload2)
 *     .UpsertItemStream(streamPayload3)
 *     .ExecuteAsync()) {
 *
 *     if (!response.IsSuccessStatusCode) {
 *        // Handle and log exception
 *        return;
 *     }
 *
 *     // Look up interested results - e.g., via typed access on operation results
 *
 *     TransactionalBatchOperationResult<ToDoActivity> result = response.GetOperationResultAtIndex<ToDoActivity>(0);
 *     ToDoActivity readActivity = result.Resource;
 * }
 * }</pre>
 * <h3>Example</h3>
 * <p>This example atomically reads a set of documents as a batch.<pre>{@code
 * String activityType = "personal";
 *
 * try (TransactionalBatchResponse response = container.CreateTransactionalBatch(new Cosmos.PartitionKey(activityType))
 *    .ReadItem("playing")
 *    .ReadItem("walking")
 *    .ReadItem("jogging")
 *    .ReadItem("running")
 *    .ExecuteAsync()) {
 *
 *     // Look up interested results - eg. via direct access to operation result stream
 *
 *     List<String> items = new ArrayList<String>();
 *
 *     for (TransactionalBatchOperationResult result : response) {
 *         try (InputStreamReader reader = new InputStreamReader(result.ResourceStream)) {
 *             resultItems.addAsync(await reader.ReadToEndAsync());
 *         }
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
        return this.createItem(item, null);
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
    <T> TransactionalBatch createItem(T item, RequestOptions requestOptions);

    /**
     * Adds an operation to delete an item into the batch.
     *
     * @param id The unique id of the item.
     *
     * @return The transactional batch instance with the operation added.
     */
    default TransactionalBatch deleteItem(String id) {
        checkNotNull(id, "expected non-null id");
        return this.deleteItem(id, null);
    }

    /**
     * Adds an operation to delete an item into the batch.
     *
     * @param id The unique id of the item.
     * @param requestOptions The options for the item request.
     *
     * @return The transactional batch instance with the operation added.
     */
    TransactionalBatch deleteItem(String id, RequestOptions requestOptions);

    /**
     * Executes the transactional batch at the Azure Cosmos service as an asynchronous operation.
     *
     * @return An awaitable response which contains details of execution of the transactional batch.
     * <p>
     * If the transactional batch executes successfully, the value returned by {@link
     * TransactionalBatchResponse#getResponseStatus} on the response returned will be set to {@link
     * HttpResponseStatus#OK}.
     * <p>
     * If an operation within the transactional batch fails during execution, no changes from the batch will be
     * committed and the status of the failing operation is made available by {@link
     * TransactionalBatchResponse#getResponseStatus}. To obtain information about the operations that failed, the
     * response can be enumerated. This returns {@link TransactionalBatchOperationResult} instances corresponding to
     * each operation in the transactional batch in the order they were added to the transactional batch. For a result
     * corresponding to an operation within the transactional batch, use
     * {@link TransactionalBatchOperationResult#getResponseStatus}
     * to access the status of the operation. If the operation was not executed or it was aborted due to the failure of
     * another operation within the transactional batch, the value of this field will be {@link
     * HttpResponseStatus#FAILED_DEPENDENCY}; for the operation that caused the batch to abort, the value of this field
     * will indicate the cause of failure as an {@link HttpResponseStatus}.
     * <p>
     * The value returned by {@link TransactionalBatchResponse#getResponseStatus} on the response returned may also have
     * values such as {@link HttpStatusClass#SERVER_ERROR} in case of server errors and {@link
     * HttpResponseStatus#TOO_MANY_REQUESTS}.
     * <p>
     * Use {@link TransactionalBatchResponse#isSuccessStatusCode} on the response returned to ensure that the
     * transactional batch succeeded.
     */
    Mono<TransactionalBatchResponse> executeAsync();

    /**
     * Adds an operation to read an item into the batch.
     *
     * @param id The unique id of the item.
     *
     * @return The transactional batch instance with the operation added.
     */
    default TransactionalBatch readItem(String id) {
        checkNotNull(id, "expected non-null id");
        return this.readItem(id, null);
    }

    /**
     * Adds an operation to read an item into the batch.
     *
     * @param id The unique id of the item.
     * @param requestOptions The options for the item request.
     *
     * @return The transactional batch instance with the operation added.
     */
    TransactionalBatch readItem(String id, RequestOptions requestOptions);

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
        return this.replaceItem(id, item, null);
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
        String id, TItem item, RequestOptions requestOptions);

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
        return this.upsertItem(item, null);
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
    <TItem> TransactionalBatch upsertItem(TItem item, RequestOptions requestOptions);
}
