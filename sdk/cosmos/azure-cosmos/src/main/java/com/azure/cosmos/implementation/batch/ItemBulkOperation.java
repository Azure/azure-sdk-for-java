// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.patch.PatchUtil;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemOperationType;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Represents an operation on an item which will be executed as part of a bulk request on a container. This will be
 * serialized and sent in the request.
 *
 * @param <TInternal> The type of item.
 */
public final class ItemBulkOperation<TInternal, TContext> implements CosmosItemOperation {

    private final TInternal item;
    private final TContext context;
    private final String id;
    private final PartitionKey partitionKey;
    private final CosmosItemOperationType operationType;
    private final RequestOptions requestOptions;
    private String partitionKeyJson;
    private BulkOperationRetryPolicy bulkOperationRetryPolicy;

    public ItemBulkOperation(
        CosmosItemOperationType operationType,
        String id,
        PartitionKey partitionKey,
        RequestOptions requestOptions,
        TInternal item,
        TContext context) {

        checkNotNull(operationType, "expected non-null operationType");

        this.operationType = operationType;
        this.partitionKey = partitionKey;
        this.id = id;
        this.item = item;
        this.context = context;
        this.requestOptions = requestOptions;
    }

    /**
     * Writes a single operation to JsonSerializable.
     * TODO(rakkuma): Similarly for hybrid row, operation needs to be written in Hybrid row.
     * Issue: https://github.com/Azure/azure-sdk-for-java/issues/15856
     *
     * @return instance of JsonSerializable containing values for a operation.
     */
    JsonSerializable serializeOperation() {
        final JsonSerializable jsonSerializable = new JsonSerializable();

        jsonSerializable.set(
            BatchRequestResponseConstants.FIELD_OPERATION_TYPE,
            ModelBridgeInternal.getOperationValueForCosmosItemOperationType(this.getOperationType()));

        if (StringUtils.isNotEmpty(this.getPartitionKeyJson())) {
            jsonSerializable.set(BatchRequestResponseConstants.FIELD_PARTITION_KEY, this.getPartitionKeyJson());
        }

        if (StringUtils.isNotEmpty(this.getId())) {
            jsonSerializable.set(BatchRequestResponseConstants.FIELD_ID, this.getId());
        }

        if (this.getItemInternal() != null) {
            if (this.getOperationType() == CosmosItemOperationType.PATCH) {
                jsonSerializable.set(BatchRequestResponseConstants.FIELD_RESOURCE_BODY,
                    PatchUtil.serializableBatchPatchOperation((CosmosPatchOperations) this.getItemInternal(), this.getRequestOptions()));
            } else {
                jsonSerializable.set(BatchRequestResponseConstants.FIELD_RESOURCE_BODY, this.getItemInternal());
            }
        }

        if (this.getRequestOptions() != null) {
            RequestOptions requestOptions = this.getRequestOptions();

            if (StringUtils.isNotEmpty(requestOptions.getIfMatchETag())) {
                jsonSerializable.set(BatchRequestResponseConstants.FIELD_IF_MATCH, requestOptions.getIfMatchETag());
            }

            if (StringUtils.isNotEmpty(requestOptions.getIfNoneMatchETag())) {
                jsonSerializable.set(BatchRequestResponseConstants.FIELD_IF_NONE_MATCH, requestOptions.getIfNoneMatchETag());
            }

            //  If content response on write is not enabled, and operation is document write - then add
            //  minimalReturnPreference field, Otherwise don't add this field, which means return the full response.
            if (requestOptions.isContentResponseOnWriteEnabled() != null) {
                if (!requestOptions.isContentResponseOnWriteEnabled() && BulkExecutorUtil.isWriteOperation(operationType)) {
                    jsonSerializable.set(BatchRequestResponseConstants.FIELD_MINIMAL_RETURN_PREFERENCE, true);
                }

            }
        }

        return jsonSerializable;
    }

    TInternal getItemInternal() {
        return this.item;
    }

    @SuppressWarnings("unchecked")
    public <T> T getItem() {
        return (T)this.item;
    }

    @SuppressWarnings("unchecked")
    public <T> T getContext() {
        return (T)this.context;
    }

    public String getId() {
        return this.id;
    }

    public PartitionKey getPartitionKeyValue() {
        return partitionKey;
    }

    public CosmosItemOperationType getOperationType() {
        return this.operationType;
    }

    private RequestOptions getRequestOptions() {
        return this.requestOptions;
    }

    private String getPartitionKeyJson() {
        return partitionKeyJson;
    }

    void setPartitionKeyJson(String value) {
        partitionKeyJson = value;
    }

    BulkOperationRetryPolicy getRetryPolicy() {
        return bulkOperationRetryPolicy;
    }

    void setRetryPolicy(BulkOperationRetryPolicy bulkOperationRetryPolicy) {
        this.bulkOperationRetryPolicy = bulkOperationRetryPolicy;
    }
}
