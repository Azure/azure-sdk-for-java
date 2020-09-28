// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.batch.ItemBatchOperation;
import com.azure.cosmos.models.PartitionKey;

import java.util.ArrayList;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Represents a batch of operations against items with the same {@link PartitionKey} in a container that will be performed
 * in a transactional manner at the Azure Cosmos DB service.
 * <p>
 * Use {@link TransactionalBatch#createTransactionalBatch(PartitionKey)} or new {@link #TransactionalBatch(PartitionKey)}
 * to create an instance of TransactionalBatch
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
 * TransactionalBatch batch = TransactionalBatch.createTransactionalBatch(new Cosmos.PartitionKey(activityType))
 *     .createItem<ToDoActivity>(test1)
 *     .replaceItem<ToDoActivity>(test2.id, test2)
 *     .upsertItem<ToDoActivity>(test3)
 *     .deleteItem("reading");
 *
 * try (TransactionalBatchResponse response = container.executeTransactionalBatch(batch) {
 *
 *     if (!response.IsSuccessStatusCode) {
 *        // Handle and log exception
 *        return;
 *     }
 *
 *     // Look up interested results - e.g., via typed access on operation results
 *
 *     TransactionalBatchOperationResult<ToDoActivity> result = response.getOperationResultAtIndex<ToDoActivity>(0, ToDoActivity.class);
 *     ToDoActivity readActivity = result.getItem();
 * }
 * }</pre>
 *
 * <b>Example</b>
 * <p>This example atomically reads a set of documents as a batch.
 * <pre>{@code
 * String activityType = "personal";
 *
 * TransactionalBatch batch = TransactionalBatch.createTransactionalBatch(new Cosmos.PartitionKey(activityType))
 *     .readItem("playing")
 *     .readItem("walking")
 *     .readItem("jogging")
 *     .readItem("running")
 *
 * try (TransactionalBatchResponse response = container.executeTransactionalBatch(new Cosmos.PartitionKey(activityType)) {
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
public final class TransactionalBatch {

    private final ArrayList<ItemBatchOperation<?>> operations;
    private final PartitionKey partitionKey;

    public TransactionalBatch(PartitionKey partitionKey) {
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
    public <T> TransactionalBatch createItem(T item) {
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
    public <T> TransactionalBatch createItem(T item, TransactionalBatchItemRequestOptions requestOptions) {

        checkNotNull(item, "expected non-null item");
        if (requestOptions == null) {
            requestOptions = new TransactionalBatchItemRequestOptions();
        }

        this.operations.add(
            new ItemBatchOperation.Builder<T>(
                OperationType.Create,
                this.operations.size())
                .requestOptions(requestOptions.toRequestOptions())
                .resource(item)
                .build());

        return this;
    }

    /**
     * Adds an operation to delete an item into the batch.
     *
     * @param id The unique id of the item.
     *
     * @return The transactional batch instance with the operation added.
     */
    public TransactionalBatch deleteItem(String id) {
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
    public TransactionalBatch deleteItem(String id, TransactionalBatchItemRequestOptions requestOptions) {

        checkNotNull(id, "expected non-null id");
        if (requestOptions == null) {
            requestOptions = new TransactionalBatchItemRequestOptions();
        }

        this.operations.add(new ItemBatchOperation.Builder<Void>(OperationType.Delete, this.operations.size())
            .requestOptions(requestOptions.toRequestOptions())
            .id(id)
            .build());

        return this;
    }

    /**
     * Adds an operation to read an item into the batch.
     *
     * @param id The unique id of the item.
     *
     * @return The transactional batch instance with the operation added.
     */
    public TransactionalBatch readItem(String id) {
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
    public TransactionalBatch readItem(String id, TransactionalBatchItemRequestOptions requestOptions) {

        checkNotNull(id, "expected non-null id");
        if (requestOptions == null) {
            requestOptions = new TransactionalBatchItemRequestOptions();
        }

        this.operations.add(new ItemBatchOperation.Builder<Void>(OperationType.Read, this.operations.size())
            .requestOptions(requestOptions.toRequestOptions())
            .id(id)
            .build());

        return this;
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
    public <T> TransactionalBatch replaceItem(String id, T item) {
        checkNotNull(id, "expected non-null id");
        checkNotNull(item, "expected non-null item");
        return this.replaceItem(id, item, new TransactionalBatchItemRequestOptions());
    }

    /**
     * Adds an operation to replace an item into the batch.
     *
     * @param <T> The type of item to be replaced.
     *
     * @param id The unique id of the item.
     * @param item A JSON serializable object that must contain an id property.
     * @param requestOptions The options for the item request.
     * @return The transactional batch instance with the operation added.
     */
    public <T> TransactionalBatch replaceItem(
        String id, T item, TransactionalBatchItemRequestOptions requestOptions) {

        checkNotNull(id, "expected non-null id");
        checkNotNull(item, "expected non-null item");
        if (requestOptions == null) {
            requestOptions = new TransactionalBatchItemRequestOptions();
        }

        this.operations.add(new ItemBatchOperation.Builder<T>(OperationType.Replace, this.operations.size())
            .requestOptions(requestOptions.toRequestOptions())
            .resource(item)
            .id(id)
            .build());

        return this;
    }

    /**
     * Adds an operation to upsert an item into the batch.
     *
     * @param item A JSON serializable object that must contain an id property.
     * @param <T> The type of item to be upserted.
     *
     * @return The transactional batch instance with the operation added.
     */
    public <T> TransactionalBatch upsertItem(T item) {
        checkNotNull(item, "expected non-null item");
        return this.upsertItem(item, new TransactionalBatchItemRequestOptions());
    }

    /**
     * Adds an operation to upsert an item into the batch.
     *
     * @param <T> The type of item to be upserted.
     *
     * @param item A JSON serializable object that must contain an id property.
     * @param requestOptions The options for the item request.
     * @return The transactional batch instance with the operation added.
     */
    public <T> TransactionalBatch upsertItem(T item, TransactionalBatchItemRequestOptions requestOptions) {

        checkNotNull(item, "expected non-null item");
        if (requestOptions == null) {
            requestOptions = new TransactionalBatchItemRequestOptions();
        }

        this.operations.add(new ItemBatchOperation.Builder<T>(OperationType.Upsert, this.operations.size())
            .requestOptions(requestOptions.toRequestOptions())
            .resource(item)
            .build());

        return this;
    }

    public ArrayList<ItemBatchOperation<?>> getOperations() {
        return operations;
    }

    public PartitionKey getPartitionKey() {
        return partitionKey;
    }
}
