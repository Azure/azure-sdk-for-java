// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.batch.ItemBatchOperation;
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
 * batch.createItemOperation<ToDoActivity>(test1);
 * batch.replaceItemOperation<ToDoActivity>(test2.id, test2);
 * batch.upsertItemOperation<ToDoActivity>(test3);
 * batch.deleteItemOperation("reading");
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
 * batch.readItemOperation("playing");
 * batch.readItemOperation("walking");
 * batch.readItemOperation("jogging");
 * batch.readItemOperation("running");
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
@Beta(value = Beta.SinceVersion.V4_7_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
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
    @Beta(value = Beta.SinceVersion.V4_7_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
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
    @Beta(value = Beta.SinceVersion.V4_7_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public <T> CosmosItemOperation createItemOperation(T item) {
        checkNotNull(item, "expected non-null item");
        return this.createItemOperation(item, new TransactionalBatchItemRequestOptions());
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
    @Beta(value = Beta.SinceVersion.V4_7_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public <T> CosmosItemOperation createItemOperation(T item, TransactionalBatchItemRequestOptions requestOptions) {

        checkNotNull(item, "expected non-null item");
        if (requestOptions == null) {
            requestOptions = new TransactionalBatchItemRequestOptions();
        }

        ItemBatchOperation<T> operation = new ItemBatchOperation<T>(
            CosmosItemOperationType.CREATE,
            null,
            this.getPartitionKeyValue(),
            requestOptions.toRequestOptions(),
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
    @Beta(value = Beta.SinceVersion.V4_7_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosItemOperation deleteItemOperation(String id) {
        checkNotNull(id, "expected non-null id");
        return this.deleteItemOperation(id, new TransactionalBatchItemRequestOptions());
    }

    /**
     * Adds an operation to delete an item into the batch.
     *
     * @param id The unique id of the item.
     * @param requestOptions The options for the item request.
     *
     * @return The transactional batch instance with the operation added.
     */
    @Beta(value = Beta.SinceVersion.V4_7_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosItemOperation deleteItemOperation(String id, TransactionalBatchItemRequestOptions requestOptions) {

        checkNotNull(id, "expected non-null id");
        if (requestOptions == null) {
            requestOptions = new TransactionalBatchItemRequestOptions();
        }

        ItemBatchOperation<?> operation = new ItemBatchOperation<>(
            CosmosItemOperationType.DELETE,
            id,
            this.getPartitionKeyValue(),
            requestOptions.toRequestOptions(),
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
    @Beta(value = Beta.SinceVersion.V4_7_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosItemOperation readItemOperation(String id) {
        checkNotNull(id, "expected non-null id");
        return this.readItemOperation(id, new TransactionalBatchItemRequestOptions());
    }

    /**
     * Adds an operation to read an item into the batch.
     *
     * @param id The unique id of the item.
     * @param requestOptions The options for the item request.
     *
     * @return The transactional batch instance with the operation added.
     */
    @Beta(value = Beta.SinceVersion.V4_7_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosItemOperation readItemOperation(String id, TransactionalBatchItemRequestOptions requestOptions) {

        checkNotNull(id, "expected non-null id");
        if (requestOptions == null) {
            requestOptions = new TransactionalBatchItemRequestOptions();
        }

        ItemBatchOperation<?> operation = new ItemBatchOperation<>(
            CosmosItemOperationType.READ,
            id,
            this.getPartitionKeyValue(),
            requestOptions.toRequestOptions(),
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
    @Beta(value = Beta.SinceVersion.V4_7_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public <T> CosmosItemOperation replaceItemOperation(String id, T item) {
        checkNotNull(id, "expected non-null id");
        checkNotNull(item, "expected non-null item");
        return this.replaceItemOperation(id, item, new TransactionalBatchItemRequestOptions());
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
    @Beta(value = Beta.SinceVersion.V4_7_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public <T> CosmosItemOperation replaceItemOperation(
        String id, T item, TransactionalBatchItemRequestOptions requestOptions) {

        checkNotNull(id, "expected non-null id");
        checkNotNull(item, "expected non-null item");
        if (requestOptions == null) {
            requestOptions = new TransactionalBatchItemRequestOptions();
        }

        ItemBatchOperation<T> operation = new ItemBatchOperation<T>(
            CosmosItemOperationType.REPLACE,
            id,
            this.getPartitionKeyValue(),
            requestOptions.toRequestOptions(),
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
    @Beta(value = Beta.SinceVersion.V4_7_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public <T> CosmosItemOperation upsertItemOperation(T item) {
        checkNotNull(item, "expected non-null item");
        return this.upsertItemOperation(item, new TransactionalBatchItemRequestOptions());
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
    @Beta(value = Beta.SinceVersion.V4_7_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public <T> CosmosItemOperation upsertItemOperation(T item, TransactionalBatchItemRequestOptions requestOptions) {

        checkNotNull(item, "expected non-null item");
        if (requestOptions == null) {
            requestOptions = new TransactionalBatchItemRequestOptions();
        }

        ItemBatchOperation<T> operation = new ItemBatchOperation<T>(
            CosmosItemOperationType.UPSERT,
            null,
            this.getPartitionKeyValue(),
            requestOptions.toRequestOptions(),
            item
        );

        this.operations.add(operation);

        return operation;
    }

    /**
     * Adds a patch operations for an item into the batch.
     *
     * @param id  the item id.
     * @param cosmosPatchOperations Represents a container having list of operations to be sequentially applied to the referred Cosmos item.
     *
     * @return The added operation.
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosItemOperation patchItemOperation(String id, CosmosPatchOperations cosmosPatchOperations) {
        checkNotNull(id, "expected non-null id");
        checkNotNull(cosmosPatchOperations, "expected non-null cosmosPatchOperations");

        return this.patchItemOperation(id, cosmosPatchOperations, new TransactionalBatchPatchItemRequestOptions());
    }

    /**
     * Adds a patch operations for an item into the batch.
     *
     * @param id  the item id.
     * @param cosmosPatchOperations Represents a container having list of operations to be sequentially applied to the referred Cosmos item.
     * @param requestOptions The options for the item request.
     *
     * @return The added operation.
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosItemOperation patchItemOperation(
        String id,
        CosmosPatchOperations cosmosPatchOperations,
        TransactionalBatchPatchItemRequestOptions requestOptions) {

        checkNotNull(id, "expected non-null id");
        checkNotNull(cosmosPatchOperations, "expected non-null cosmosPatchOperations");

        if (requestOptions == null) {
            requestOptions = new TransactionalBatchPatchItemRequestOptions();
        }

        ItemBatchOperation<?> operation = new ItemBatchOperation<>(
            CosmosItemOperationType.PATCH,
            id,
            this.getPartitionKeyValue(),
            requestOptions.toRequestOptions(),
            cosmosPatchOperations
        );

        this.operations.add(operation);

        return operation;
    }

    /**
     * Return the list of operation in an unmodifiable instance  so no one can change it in the down path.
     *
     * @return The list of operations which are to be executed.
     */
    @Beta(value = Beta.SinceVersion.V4_7_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public List<CosmosItemOperation> getOperations() {
        return UnmodifiableList.unmodifiableList(operations);
    }

    /**
     * Return the partition key for this batch.
     *
     * @return The partition key for this batch.
     */
    @Beta(value = Beta.SinceVersion.V4_7_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public PartitionKey getPartitionKeyValue() {
        return partitionKey;
    }
}
