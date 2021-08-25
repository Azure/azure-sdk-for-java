// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosItemOperation;
import com.azure.cosmos.CosmosItemOperationType;
import com.azure.cosmos.CosmosPatchOperations;
import com.azure.cosmos.implementation.batch.ItemBulkOperation;
import com.azure.cosmos.util.Beta;
import reactor.core.publisher.Flux;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Utility for creating bulk operations which can be executed by calling
 * {@link CosmosAsyncContainer#processBulkOperations(Flux, CosmosBulkExecutionOptions)} .
 *
 * Also while creating these operation, if some options which are only for individual operation can be provided by passing
 * a {@link CosmosBulkItemRequestOptions} while creating the bulk operation.
 *
 * See also {@link CosmosBulkExecutionOptions}.
 */
@Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class CosmosBulkOperations {

    /**
     * Instantiate an operation for Creating item in Bulk execution.
     *
     * @param <T> The type of item to be created.
     *
     * @param item A JSON serializable object that must contain an id property.
     * @param partitionKey the partition key for the operation
     *
     * @return the bulk operation.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static <T> CosmosItemOperation getCreateItemOperation(T item, PartitionKey partitionKey) {
        checkNotNull(item, "expected non-null item");
        checkNotNull(partitionKey, "expected non-null partitionKey");
        return getCreateItemOperation(item, partitionKey, new CosmosBulkItemRequestOptions(), null);
    }

    /**
     * Instantiate an operation for Creating item in Bulk execution.
     *
     * @param <T> The type of item to be created.
     * @param <TContext> The type of context to be used.
     *
     * @param item A JSON serializable object that must contain an id property.
     * @param partitionKey the partition key for the operation
     * @param context The caller provided context for this operation.
     *
     * @return the bulk operation.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static <T, TContext> CosmosItemOperation getCreateItemOperation(
        T item,
        PartitionKey partitionKey,
        TContext context) {

        checkNotNull(item, "expected non-null item");
        checkNotNull(partitionKey, "expected non-null partitionKey");
        return getCreateItemOperation(item, partitionKey, new CosmosBulkItemRequestOptions(), context);
    }

    /**
     * Instantiate an operation for Creating item in Bulk execution.
     *
     * @param <T> The type of item to be created.
     *
     * @param item A JSON serializable object that must contain an id property.
     * @param partitionKey the partition key for the operation.
     * @param requestOptions The options for the item request.
     *
     * @return the bulk operation.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static <T> CosmosItemOperation getCreateItemOperation(
        T item,
        PartitionKey partitionKey,
        CosmosBulkItemRequestOptions requestOptions) {

        checkNotNull(item, "expected non-null item");
        checkNotNull(partitionKey, "expected non-null partitionKey");

        return getCreateItemOperation(item, partitionKey, requestOptions, null);
    }

    /**
     * Instantiate an operation for Creating item in Bulk execution.
     *
     * @param <T> The type of item to be created.
     * @param <TContext> The type of context to be used.
     *
     * @param item A JSON serializable object that must contain an id property.
     * @param partitionKey the partition key for the operation.
     * @param requestOptions The options for the item request.
     * @param context The caller provided context for this operation.
     *
     * @return the bulk operation.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static <T, TContext> CosmosItemOperation getCreateItemOperation(
        T item,
        PartitionKey partitionKey,
        CosmosBulkItemRequestOptions requestOptions,
        TContext context) {

        checkNotNull(item, "expected non-null item");
        checkNotNull(partitionKey, "expected non-null partitionKey");

        if (requestOptions == null) {
            requestOptions = new CosmosBulkItemRequestOptions();
        }

        return new ItemBulkOperation<>(
            CosmosItemOperationType.CREATE,
            null,
            partitionKey,
            requestOptions.toRequestOptions(),
            item,
            context
        );
    }

    /**
     * Instantiate an operation for deleting item in Bulk execution.
     *
     * @param id The unique id of the item.
     * @param partitionKey the partition key for the operation.
     *
     * @return the bulk operation.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static CosmosItemOperation getDeleteItemOperation(String id, PartitionKey partitionKey) {
        checkNotNull(id, "expected non-null id");
        checkNotNull(partitionKey, "expected non-null partitionKey");

        return getDeleteItemOperation(id, partitionKey, new CosmosBulkItemRequestOptions());
    }

    /**
     * Instantiate an operation for deleting item in Bulk execution.
     *
     * @param <TContext> The type of context to be used.
     *
     * @param id The unique id of the item.
     * @param partitionKey the partition key for the operation.
     * @param context The caller provided context for this operation.
     *
     * @return the bulk operation.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static <TContext> CosmosItemOperation getDeleteItemOperation(
        String id,
        PartitionKey partitionKey,
        TContext context) {

        checkNotNull(id, "expected non-null id");
        checkNotNull(partitionKey, "expected non-null partitionKey");

        return getDeleteItemOperation(id, partitionKey, new CosmosBulkItemRequestOptions(), context);
    }

    /**
     * Instantiate an operation for deleting item in Bulk execution.
     *
     * @param id The unique id of the item.
     * @param partitionKey the partition key for the operation..
     * @param requestOptions The options for the item request.
     *
     * @return the bulk operation.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static CosmosItemOperation getDeleteItemOperation(
        String id,
        PartitionKey partitionKey,
        CosmosBulkItemRequestOptions requestOptions) {

        checkNotNull(id, "expected non-null id");
        checkNotNull(partitionKey, "expected non-null partitionKey");

        if (requestOptions == null) {
            requestOptions = new CosmosBulkItemRequestOptions();
        }

        return getDeleteItemOperation(id, partitionKey, requestOptions, null);
    }

    /**
     * Instantiate an operation for deleting item in Bulk execution.
     *
     * @param <TContext> The type of context to be used.
     *
     * @param id The unique id of the item.
     * @param partitionKey the partition key for the operation..
     * @param requestOptions The options for the item request.
     * @param context The caller provided context for this operation.
     *
     * @return the bulk operation.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static <TContext> CosmosItemOperation getDeleteItemOperation(
        String id,
        PartitionKey partitionKey,
        CosmosBulkItemRequestOptions requestOptions,
        TContext context) {

        checkNotNull(id, "expected non-null id");
        checkNotNull(partitionKey, "expected non-null partitionKey");

        if (requestOptions == null) {
            requestOptions = new CosmosBulkItemRequestOptions();
        }

        return new ItemBulkOperation<>(
            CosmosItemOperationType.DELETE,
            id,
            partitionKey,
            requestOptions.toRequestOptions(),
            null,
            context
        );
    }

    /**
     * Instantiate an operation for read item in Bulk execution.
     *
     * @param id The unique id of the item.
     * @param partitionKey the partition key for the operation.
     *
     * @return the bulk operation.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static CosmosItemOperation getReadItemOperation(String id, PartitionKey partitionKey) {
        checkNotNull(id, "expected non-null id");
        checkNotNull(partitionKey, "expected non-null partitionKey");

        return getReadItemOperation(id, partitionKey, new CosmosBulkItemRequestOptions(), null);
    }

    /**
     * Instantiate an operation for read item in Bulk execution.
     *
     * @param <TContext> The type of context to be used.
     *
     * @param id The unique id of the item.
     * @param partitionKey the partition key for the operation.
     * @param context The caller provided context for this operation.
     *
     * @return the bulk operation.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static <TContext> CosmosItemOperation getReadItemOperation(
        String id,
        PartitionKey partitionKey,
        TContext context) {

        checkNotNull(id, "expected non-null id");
        checkNotNull(partitionKey, "expected non-null partitionKey");

        return getReadItemOperation(id, partitionKey, new CosmosBulkItemRequestOptions(), context);
    }

    /**
     * Instantiate an operation for read item in Bulk execution.
     *
     * @param id The unique id of the item.
     * @param partitionKey the partition key for the operation..
     * @param requestOptions The options for the item request.
     *
     * @return the bulk operation.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static CosmosItemOperation getReadItemOperation(
        String id,
        PartitionKey partitionKey,
        CosmosBulkItemRequestOptions requestOptions) {

        checkNotNull(id, "expected non-null id");
        checkNotNull(partitionKey, "expected non-null partitionKey");

        return getReadItemOperation(id, partitionKey, requestOptions, null);
    }

    /**
     * Instantiate an operation for read item in Bulk execution.
     *
     * @param <TContext> The type of context to be used.
     *
     * @param id The unique id of the item.
     * @param partitionKey the partition key for the operation..
     * @param requestOptions The options for the item request.
     * @param context The caller provided context for this operation.
     *
     * @return the bulk operation.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static <TContext> CosmosItemOperation getReadItemOperation(
        String id,
        PartitionKey partitionKey,
        CosmosBulkItemRequestOptions requestOptions,
        TContext context) {

        checkNotNull(id, "expected non-null id");
        checkNotNull(partitionKey, "expected non-null partitionKey");

        if (requestOptions == null) {
            requestOptions = new CosmosBulkItemRequestOptions();
        }

        return new ItemBulkOperation<>(
            CosmosItemOperationType.READ,
            id,
            partitionKey,
            requestOptions.toRequestOptions(),
            null,
            context
        );
    }

    /**
     * Instantiate an operation for replace item in Bulk execution.
     *
     * @param <T> The type of item to be replaced.
     *
     * @param id The unique id of the item.
     * @param item A JSON serializable object that must contain an id property.
     * @param partitionKey the partition key for the operation.
     *
     * @return the bulk operation.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static <T> CosmosItemOperation getReplaceItemOperation(String id, T item, PartitionKey partitionKey) {
        checkNotNull(item, "expected non-null item");
        checkNotNull(id, "expected non-null id");
        checkNotNull(partitionKey, "expected non-null partitionKey");

        return getReplaceItemOperation(id, item, partitionKey, new CosmosBulkItemRequestOptions(), null);
    }

    /**
     * Instantiate an operation for replace item in Bulk execution.
     *
     * @param <T> The type of item to be replaced.
     * @param <TContext> The type of context to be used.
     *
     * @param id The unique id of the item.
     * @param item A JSON serializable object that must contain an id property.
     * @param partitionKey the partition key for the operation.
     * @param context The caller provided context for this operation.
     *
     * @return the bulk operation.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static <T, TContext> CosmosItemOperation getReplaceItemOperation(
        String id,
        T item,
        PartitionKey partitionKey,
        TContext context) {

        checkNotNull(item, "expected non-null item");
        checkNotNull(id, "expected non-null id");
        checkNotNull(partitionKey, "expected non-null partitionKey");

        return getReplaceItemOperation(id, item, partitionKey, new CosmosBulkItemRequestOptions(), context);
    }

    /**
     * Instantiate an operation for replace item in Bulk execution.
     *
     * @param <T> The type of item to be replaced.
     *
     * @param id The unique id of the item..
     * @param item A JSON serializable object that must contain an id property.
     * @param partitionKey the partition key for the operation.
     * @param requestOptions The options for the item request.
     *
     * @return the bulk operation.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static <T> CosmosItemOperation getReplaceItemOperation(
        String id,
        T item,
        PartitionKey partitionKey,
        CosmosBulkItemRequestOptions requestOptions) {

        checkNotNull(item, "expected non-null item");
        checkNotNull(id, "expected non-null id");
        checkNotNull(partitionKey, "expected non-null partitionKey");

        return getReplaceItemOperation(id, item, partitionKey, requestOptions,null);
    }

    /**
     * Instantiate an operation for replace item in Bulk execution.
     *
     * @param <T> The type of item to be replaced.
     * @param <TContext> The type of context to be used.
     *
     * @param id The unique id of the item..
     * @param item A JSON serializable object that must contain an id property.
     * @param partitionKey the partition key for the operation.
     * @param requestOptions The options for the item request.
     * @param context The caller provided context for this operation.
     *
     * @return the bulk operation.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static <T, TContext> CosmosItemOperation getReplaceItemOperation(
        String id,
        T item,
        PartitionKey partitionKey,
        CosmosBulkItemRequestOptions requestOptions,
        TContext context) {

        checkNotNull(item, "expected non-null item");
        checkNotNull(id, "expected non-null id");
        checkNotNull(partitionKey, "expected non-null partitionKey");

        if (requestOptions == null) {
            requestOptions = new CosmosBulkItemRequestOptions();
        }

        return new ItemBulkOperation<>(
            CosmosItemOperationType.REPLACE,
            id,
            partitionKey,
            requestOptions.toRequestOptions(),
            item,
            context
        );
    }

    /**
     * Instantiate an operation for upsert item in Bulk execution.
     *
     * @param <T> The type of item to be upserted.
     *
     * @param item A JSON serializable object that must contain an id property.
     * @param partitionKey the partition key for the operation.
     *
     * @return the bulk operation.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static <T> CosmosItemOperation getUpsertItemOperation(T item, PartitionKey partitionKey) {
        checkNotNull(item, "expected non-null item");
        checkNotNull(partitionKey, "expected non-null partitionKey");

        return getUpsertItemOperation(item, partitionKey, new CosmosBulkItemRequestOptions(), null);
    }

    /**
     * Instantiate an operation for upsert item in Bulk execution.
     *
     * @param <T> The type of item to be upserted.
     * @param <TContext> The type of context to be used.
     *
     * @param item A JSON serializable object that must contain an id property.
     * @param partitionKey the partition key for the operation.
     * @param context The caller provided context for this operation.
     *
     * @return the bulk operation.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static <T, TContext> CosmosItemOperation getUpsertItemOperation(
        T item,
        PartitionKey partitionKey,
        TContext context) {

        checkNotNull(item, "expected non-null item");
        checkNotNull(partitionKey, "expected non-null partitionKey");

        return getUpsertItemOperation(item, partitionKey, new CosmosBulkItemRequestOptions(), context);
    }

    /**
     * Instantiate an operation for upsert item in Bulk execution.
     *
     * @param <T> The type of item to be upserted.
     *
     * @param item A JSON serializable object that must contain an id property.
     * @param partitionKey the partition key for the operation.
     * @param requestOptions The options for the item request.
     *
     * @return the bulk operation.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static <T> CosmosItemOperation getUpsertItemOperation(
        T item,
        PartitionKey partitionKey,
        CosmosBulkItemRequestOptions requestOptions) {

        checkNotNull(item, "expected non-null item");
        checkNotNull(partitionKey, "expected non-null partitionKey");

        return getUpsertItemOperation(item, partitionKey, requestOptions, null);
    }

    /**
     * Instantiate an operation for upsert item in Bulk execution.
     *
     * @param <T> The type of item to be upserted.
     * @param <TContext> The type of context to be used.
     *
     * @param item A JSON serializable object that must contain an id property.
     * @param partitionKey the partition key for the operation.
     * @param requestOptions The options for the item request.
     * @param context The caller provided context for this operation.
     *
     * @return the bulk operation.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static <T, TContext> CosmosItemOperation getUpsertItemOperation(
        T item,
        PartitionKey partitionKey,
        CosmosBulkItemRequestOptions requestOptions,
        TContext context) {

        checkNotNull(item, "expected non-null item");
        checkNotNull(partitionKey, "expected non-null partitionKey");

        if (requestOptions == null) {
            requestOptions = new CosmosBulkItemRequestOptions();
        }

        return new ItemBulkOperation<>(
            CosmosItemOperationType.UPSERT,
            null,
            partitionKey,
            requestOptions.toRequestOptions(),
            item,
            context
        );
    }

    /**
     * Instantiate an operation for a patch in Bulk execution.
     *
     * @param id  the item id.
     * @param partitionKey the partition key for the operation.
     * @param cosmosPatchOperations Represents a container having list of operations to be sequentially applied to the referred Cosmos item.
     *
     * @return the bulk operation.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static CosmosItemOperation getPatchItemOperation(
        String id,
        PartitionKey partitionKey,
        CosmosPatchOperations cosmosPatchOperations) {

        checkNotNull(id, "expected non-null id");
        checkNotNull(partitionKey, "expected non-null partitionKey");
        checkNotNull(cosmosPatchOperations, "expected non-null cosmosPatchOperations");

        return getPatchItemOperation(
            id,
            partitionKey,
            cosmosPatchOperations,
            new CosmosBulkPatchItemRequestOptions(),
            null);
    }

    /**
     * Instantiate an operation for a patch in Bulk execution.
     *
     * @param <TContext> The type of context to be used.
     *
     * @param id  the item id.
     * @param partitionKey the partition key for the operation.
     * @param cosmosPatchOperations Represents a container having list of operations to be sequentially
     *        applied to the referred Cosmos item.
     * @param context The caller provided context for this operation.
     *
     * @return the bulk operation.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static <TContext> CosmosItemOperation getPatchItemOperation(
        String id,
        PartitionKey partitionKey,
        CosmosPatchOperations cosmosPatchOperations,
        TContext context) {

        checkNotNull(id, "expected non-null id");
        checkNotNull(partitionKey, "expected non-null partitionKey");
        checkNotNull(cosmosPatchOperations, "expected non-null cosmosPatchOperations");

        return getPatchItemOperation(
            id,
            partitionKey,
            cosmosPatchOperations,
            new CosmosBulkPatchItemRequestOptions(),
            context);
    }

    /**
     * Instantiate an operation for a patch in Bulk execution.
     *
     * @param id  the item id.
     * @param partitionKey the partition key for the operation.
     * @param cosmosPatchOperations Represents a container having list of operations to be sequentially applied to the referred Cosmos item.
     * @param requestOptions The options for the item request.
     *
     * @return the bulk operation.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static CosmosItemOperation getPatchItemOperation(
        String id,
        PartitionKey partitionKey,
        CosmosPatchOperations cosmosPatchOperations,
        CosmosBulkPatchItemRequestOptions requestOptions) {

        checkNotNull(id, "expected non-null id");
        checkNotNull(partitionKey, "expected non-null partitionKey");
        checkNotNull(cosmosPatchOperations, "expected non-null cosmosPatchOperations");

        return getPatchItemOperation(id, partitionKey, cosmosPatchOperations, requestOptions, null);
    }

    /**
     * Instantiate an operation for a patch in Bulk execution.
     *
     * @param <TContext> The type of context to be used.
     *
     * @param id  the item id.
     * @param partitionKey the partition key for the operation.
     * @param cosmosPatchOperations Represents a container having list of operations to be sequentially applied to the referred Cosmos item.
     * @param requestOptions The options for the item request.
     * @param context The caller provided context for this operation.
     *
     * @return the bulk operation.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static <TContext> CosmosItemOperation getPatchItemOperation(
        String id,
        PartitionKey partitionKey,
        CosmosPatchOperations cosmosPatchOperations,
        CosmosBulkPatchItemRequestOptions requestOptions,
        TContext context) {

        checkNotNull(id, "expected non-null id");
        checkNotNull(partitionKey, "expected non-null partitionKey");
        checkNotNull(cosmosPatchOperations, "expected non-null cosmosPatchOperations");

        if (requestOptions == null) {
            requestOptions = new CosmosBulkPatchItemRequestOptions();
        }

        return new ItemBulkOperation<>(
            CosmosItemOperationType.PATCH,
            id,
            partitionKey,
            requestOptions.toRequestOptions(),
            cosmosPatchOperations,
            context
        );
    }
}
