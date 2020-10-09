// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.models.ItemBatchOperation;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.Beta;

import java.util.ArrayList;
import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Represents a batch of operations against items with the same {@link PartitionKey} in a container that will be performed
 * in a transactional manner at the Azure Cosmos DB service.
 * <p>
 * Use {@link TransactionalBatch#createTransactionalBatch(PartitionKey)} to create an instance of TransactionalBatch.
 * <b>Example</b>
 * This example atomically modifies a set of items as a batch.
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
 * TransactionalBatch batch = TransactionalBatch.createTransactionalBatch(new Cosmos.PartitionKey(activityType));
 * batch.createItem<ToDoActivity>(test1);
 * batch.replaceItem<ToDoActivity>(test2.id, test2);
 * batch.upsertItem<ToDoActivity>(test3);
 * batch.deleteItem("reading");
 *
 * TransactionalBatchResponse response = container.executeTransactionalBatch(batch);
 *
 * if (!response.isSuccessStatusCode()) {
 *      // Handle and log exception
 *      return;
 * }
 *
 * // Look up interested results - e.g., via typed access on operation results
 *
 * TransactionalBatchOperationResult result = response.get(0);
 * ToDoActivity readActivity = result.getItem(ToDoActivity.class);
 *
 * }</pre>
 *
 * <b>Example</b>
 * <p>This example atomically reads a set of items as a batch.
 * <pre>{@code
 * String activityType = "personal";
 *
 * TransactionalBatch batch = TransactionalBatch.createTransactionalBatch(new Cosmos.PartitionKey(activityType));
 * batch.readItem("playing");
 * batch.readItem("walking");
 * batch.readItem("jogging");
 * batch.readItem("running")v
 *
 * TransactionalBatchResponse response = container.executeTransactionalBatch(batch);
 * List<ToDoActivity> resultItems = new ArrayList<ToDoActivity>();
 *
 * for (int i = 0; i < response.size(); i++) {
 *     TransactionalBatchOperationResult result = response.get(0);
 *     resultItems.add(result.getItem(ToDoActivity.class));
 * }
 *
 * }</pre>
 * <p>
 * <b>See:</b>
 * <a href="https://docs.microsoft.com/azure/cosmos-db/concepts-limits">Limits on TransactionalBatch requests</a>.
 */
@Beta(Beta.SinceVersion.V4_7_0)
public final class TransactionalBatch {

    private final List<ItemBatchOperation<?>> operations;
    private final PartitionKey partitionKey;

    TransactionalBatch(PartitionKey partitionKey) {
        checkNotNull(partitionKey, "expected non-null partitionKey");

        this.operations = new ArrayList<>();
        this.partitionKey = partitionKey;
    }

    /**
     * Initializes a new instance of {@link TransactionalBatch}
     * that will contain operations to be performed across multiple items in the container with the provided partition
     * key in a transactional manner
     *
     * @param partitionKey the partition key for all items in the batch.
     *
     * @return A new instance of {@link TransactionalBatch}.
     */
    public static TransactionalBatch createTransactionalBatch(PartitionKey partitionKey) {
        return new TransactionalBatch(partitionKey);
    }

    /**
     * Adds an operation to create an item into the batch.
     *
     * @param item A JSON serializable object that must contain an id property.
     * @param <T> The type of item to be created.
     *
     * @return The transactional batch instance with the operation added.
     */
    public <T> ItemBatchOperation<T> createItem(T item) {
        checkNotNull(item, "expected non-null item");
        return this.createItem(item, new ItemBatchRequestOptions());
    }

    /**
     * Adds an operation to create an item into the batch.
     *
     * @param <T> The type of item to be created.
     *
     * @param item A JSON serializable object that must contain an id property.
     * @param requestOptions The options for the item request.
     *
     * @return The transactional batch instance with the operation added.
     */
    public <T> ItemBatchOperation<T> createItem(T item, ItemBatchRequestOptions requestOptions) {

        checkNotNull(item, "expected non-null item");
        if (requestOptions == null) {
            requestOptions = new ItemBatchRequestOptions();
        }

        ItemBatchOperation<T> operation = ModelBridgeInternal.createItemBatchOperation(
            CosmosItemOperationType.Create,
            this.operations.size(),
            null,
            this.getPartitionKey(),
            requestOptions,
            item
        );

        this.operations.add(operation);

        return operation;
    }

    /**
     * Adds an operation to delete an item into the batch.
     *
     * @param id The unique id of the item.
     *
     * @return The transactional batch instance with the operation added.
     */
    public ItemBatchOperation<?> deleteItem(String id) {
        checkNotNull(id, "expected non-null id");
        return this.deleteItem(id, new ItemBatchRequestOptions());
    }

    /**
     * Adds an operation to delete an item into the batch.
     *
     * @param id The unique id of the item.
     * @param requestOptions The options for the item request.
     *
     * @return The transactional batch instance with the operation added.
     */
    public ItemBatchOperation<?> deleteItem(String id, ItemBatchRequestOptions requestOptions) {

        checkNotNull(id, "expected non-null id");
        if (requestOptions == null) {
            requestOptions = new ItemBatchRequestOptions();
        }

        ItemBatchOperation<?> operation = ModelBridgeInternal.createItemBatchOperation(
            CosmosItemOperationType.Delete,
            this.operations.size(),
            id,
            this.getPartitionKey(),
            requestOptions,
            null
        );

        this.operations.add(operation);

        return operation;
    }

    /**
     * Adds an operation to read an item into the batch.
     *
     * @param id The unique id of the item.
     *
     * @return The transactional batch instance with the operation added.
     */
    public ItemBatchOperation<?> readItem(String id) {
        checkNotNull(id, "expected non-null id");
        return this.readItem(id, new ItemBatchRequestOptions());
    }

    /**
     * Adds an operation to read an item into the batch.
     *
     * @param id The unique id of the item.
     * @param requestOptions The options for the item request.
     *
     * @return The transactional batch instance with the operation added.
     */
    public ItemBatchOperation<?> readItem(String id, ItemBatchRequestOptions requestOptions) {

        checkNotNull(id, "expected non-null id");
        if (requestOptions == null) {
            requestOptions = new ItemBatchRequestOptions();
        }

        ItemBatchOperation<?> operation = ModelBridgeInternal.createItemBatchOperation(
            CosmosItemOperationType.Read,
            this.operations.size(),
            id,
            this.getPartitionKey(),
            requestOptions,
            null
        );

        this.operations.add(operation);

        return operation;
    }

    /**
     * Adds an operation to replace an item into the batch.
     *
     * @param id The unique id of the item.
     * @param item A JSON serializable object that must contain an id property.
     * @param <T> The type of item to be replaced.
     *
     * @return The transactional batch instance with the operation added.
     */
    public <T> ItemBatchOperation<T> replaceItem(String id, T item) {
        checkNotNull(id, "expected non-null id");
        checkNotNull(item, "expected non-null item");
        return this.replaceItem(id, item, new ItemBatchRequestOptions());
    }

    /**
     * Adds an operation to replace an item into the batch.
     *
     * @param <T> The type of item to be replaced.
     *
     * @param id The unique id of the item.
     * @param item A JSON serializable object that must contain an id property.
     * @param requestOptions The options for the item request.
     *
     * @return The transactional batch instance with the operation added.
     */
    public <T> ItemBatchOperation<T> replaceItem(
        String id, T item, ItemBatchRequestOptions requestOptions) {

        checkNotNull(id, "expected non-null id");
        checkNotNull(item, "expected non-null item");
        if (requestOptions == null) {
            requestOptions = new ItemBatchRequestOptions();
        }

        ItemBatchOperation<T> operation = ModelBridgeInternal.createItemBatchOperation(
            CosmosItemOperationType.Replace,
            this.operations.size(),
            id,
            this.getPartitionKey(),
            requestOptions,
            item
        );

        this.operations.add(operation);

        return operation;
    }

    /**
     * Adds an operation to upsert an item into the batch.
     *
     * @param item A JSON serializable object that must contain an id property.
     * @param <T> The type of item to be upserted.
     *
     * @return The transactional batch instance with the operation added.
     */
    public <T> ItemBatchOperation<T> upsertItem(T item) {
        checkNotNull(item, "expected non-null item");
        return this.upsertItem(item, new ItemBatchRequestOptions());
    }

    /**
     * Adds an operation to upsert an item into the batch.
     *
     * @param <T> The type of item to be upserted.
     *
     * @param item A JSON serializable object that must contain an id property.
     * @param requestOptions The options for the item request.
     *
     * @return The transactional batch instance with the operation added.
     */
    public <T> ItemBatchOperation<T> upsertItem(T item, ItemBatchRequestOptions requestOptions) {

        checkNotNull(item, "expected non-null item");
        if (requestOptions == null) {
            requestOptions = new ItemBatchRequestOptions();
        }

        ItemBatchOperation<T> operation = ModelBridgeInternal.createItemBatchOperation(
            CosmosItemOperationType.Upsert,
            this.operations.size(),
            null,
            this.getPartitionKey(),
            requestOptions,
            item
        );

        this.operations.add(operation);

        return operation;
    }

    /**
     * Return the list of operation in an unmodifiable instace so no one can change it in the down path.
     *
     * @return The list of operations which are to be executed.
     */
    public List<ItemBatchOperation<?>> getOperations() {
        return UnmodifiableList.unmodifiableList(operations);
    }

    /**
     * Return the partition key for this batch.
     *
     * @return The partition key for this batch.
     */
    public PartitionKey getPartitionKey() {
        return partitionKey;
    }
}
