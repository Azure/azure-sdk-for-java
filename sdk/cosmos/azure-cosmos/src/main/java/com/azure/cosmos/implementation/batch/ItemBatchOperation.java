// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.patch.PatchUtil;
import com.azure.cosmos.models.CosmosItemOperationType;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Represents an operation on an item which will be executed as part of a batch request on a container. This will be
 * serialized and sent in the request.
 *
 * @param <TInternal> The type of item.
 */
public final class ItemBatchOperation<TInternal> extends CosmosItemOperationBase {

    private final TInternal item;

    private final String id;
    private final PartitionKey partitionKey;
    private final CosmosItemOperationType operationType;
    private final RequestOptions requestOptions;
    private CosmosItemSerializer effectiveItemSerializerForResult;

    public ItemBatchOperation(
        final CosmosItemOperationType operationType,
        final String id,
        final PartitionKey partitionKey,
        final RequestOptions requestOptions,
        final TInternal item) {

        checkNotNull(operationType, "expected non-null operationType");

        this.operationType = operationType;
        this.partitionKey = partitionKey;
        this.id = id;
        this.item = item;
        this.requestOptions = requestOptions;
    }

    @Override
    public CosmosItemSerializer getEffectiveItemSerializerForResult() {
        return this.effectiveItemSerializerForResult != null
            ? this.effectiveItemSerializerForResult
            : CosmosItemSerializer.DEFAULT_SERIALIZER;
    }

    /**
     * Writes a single operation to JsonSerializable.
     *
     * @return instance of JsonSerializable containing values for an operation.
     */
    @Override
    JsonSerializable getSerializedOperationInternal(CosmosItemSerializer effectiveItemSerializer) {
        final JsonSerializable jsonSerializable = new JsonSerializable();
        this.effectiveItemSerializerForResult = effectiveItemSerializer;

        jsonSerializable.set(
            BatchRequestResponseConstants.FIELD_OPERATION_TYPE,
            ModelBridgeInternal.getOperationValueForCosmosItemOperationType(this.getOperationType()),
            CosmosItemSerializer.DEFAULT_SERIALIZER);

        if (StringUtils.isNotEmpty(this.getId())) {
            jsonSerializable.set(
                BatchRequestResponseConstants.FIELD_ID,
                this.getId(),
                CosmosItemSerializer.DEFAULT_SERIALIZER);
        }

        if (this.getItemInternal() != null) {
            if (this.getOperationType() == CosmosItemOperationType.PATCH) {
                jsonSerializable.set(
                    BatchRequestResponseConstants.FIELD_RESOURCE_BODY,
                    PatchUtil.serializableBatchPatchOperation((CosmosPatchOperations) this.getItemInternal(), this.getRequestOptions()),
                    CosmosItemSerializer.DEFAULT_SERIALIZER);
            } else {
                jsonSerializable.set(
                    BatchRequestResponseConstants.FIELD_RESOURCE_BODY,
                    this.getItemInternal(),
                    effectiveItemSerializer,
                    true);
            }
        }

        if (this.getRequestOptions() != null) {
            RequestOptions requestOptions = this.getRequestOptions();

            if (StringUtils.isNotEmpty(requestOptions.getIfMatchETag())) {
                jsonSerializable.set(
                    BatchRequestResponseConstants.FIELD_IF_MATCH,
                    requestOptions.getIfMatchETag(),
                    CosmosItemSerializer.DEFAULT_SERIALIZER);
            }

            if (StringUtils.isNotEmpty(requestOptions.getIfNoneMatchETag())) {
                jsonSerializable.set(
                    BatchRequestResponseConstants.FIELD_IF_NONE_MATCH,
                    requestOptions.getIfNoneMatchETag(),
                    CosmosItemSerializer.DEFAULT_SERIALIZER);
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

    @Override
    public <T> T getContext() {
        return null;
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

    public RequestOptions getRequestOptions() {
        return this.requestOptions;
    }
}
