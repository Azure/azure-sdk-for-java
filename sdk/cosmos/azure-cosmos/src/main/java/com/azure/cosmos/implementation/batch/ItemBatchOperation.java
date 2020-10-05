// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.models.PartitionKey;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Represents an operation on an item which will be executed as part of a batch request on a container.
 */
public final class ItemBatchOperation<TResource> {

    private TResource resource;

    private final String id;
    private final int operationIndex;
    private final PartitionKey partitionKey;
    private String partitionKeyJson;
    private final OperationType operationType;
    private final RequestOptions requestOptions;

    private ItemBatchOperation(
        final OperationType operationType,
        final int operationIndex,
        final PartitionKey partitionKey,
        final String id,
        final TResource resource,
        final RequestOptions requestOptions) {

        checkArgument(operationIndex >= 0, "expected operationIndex >= 0, not %s", operationIndex);
        checkNotNull(operationType, "expected non-null operationType");

        this.operationType = operationType;
        this.operationIndex = operationIndex;
        this.partitionKey = partitionKey;
        this.id = id;
        this.resource = resource;
        this.requestOptions = requestOptions;
    }

    // TODO(rakkuma): Similarly for hybrid row, operation needs to be written in Hybrid row.
    // Issue: https://github.com/Azure/azure-sdk-for-java/issues/15856
    static JsonSerializable writeOperation(final ItemBatchOperation<?> operation) {
        final JsonSerializable jsonSerializable = new JsonSerializable();

        jsonSerializable.set(BatchRequestResponseConstant.FIELD_OPERATION_TYPE, BatchExecUtils.getStringOperationType(operation.getOperationType()));

        if (StringUtils.isNotEmpty(operation.getPartitionKeyJson())) {
            // Used for non transactional batch.
            jsonSerializable.set(BatchRequestResponseConstant.FIELD_PARTITION_KEY, operation.getPartitionKeyJson());
        }

        if (StringUtils.isNotEmpty(operation.getId())) {
            jsonSerializable.set(BatchRequestResponseConstant.FIELD_ID, operation.getId());
        }

        if (operation.getResource() != null) {
            jsonSerializable.set(BatchRequestResponseConstant.FIELD_RESOURCE_BODY, operation.getResource());
        }

        if (operation.getRequestOptions() != null) {
            RequestOptions requestOptions = operation.getRequestOptions();

            if (StringUtils.isNotEmpty(requestOptions.getIfMatchETag())) {
                jsonSerializable.set(BatchRequestResponseConstant.FIELD_IF_MATCH, requestOptions.getIfMatchETag());
            }

            if (StringUtils.isNotEmpty(requestOptions.getIfNoneMatchETag())) {
                jsonSerializable.set(BatchRequestResponseConstant.FIELD_IF_NONE_MATCH, requestOptions.getIfNoneMatchETag());
            }
        }

        return jsonSerializable;
    }

    String getId() {
        return this.id;
    }

    OperationType getOperationType() {
        return this.operationType;
    }

    String getPartitionKeyJson() {
        return this.partitionKeyJson;
    }

    int getOperationIndex() {
        return operationIndex;
    }

    PartitionKey getPartitionKey() {
        return partitionKey;
    }

    void setPartitionKeyJson(String partitionKeyJson) {
        this.partitionKeyJson = partitionKeyJson;
    }

    RequestOptions getRequestOptions() {
        return this.requestOptions;
    }

    TResource getResource() {
        return this.resource;
    }

    public static final class Builder<TResource> {

        private final OperationType operationType;
        private final int operationIndex;
        private String id;
        private PartitionKey partitionKey;
        private RequestOptions requestOptions;
        private TResource resource;

        public Builder(final OperationType type, final int index) {

            checkNotNull(type, "expected non-null type");
            checkArgument(index >= 0, "expected index >= 0, not %s", index);

            this.operationType = type;
            this.operationIndex = index;
        }

        public Builder<TResource> id(String value) {
            this.id = value;
            return this;
        }

        public Builder<TResource> partitionKey(PartitionKey value) {
            this.partitionKey = value;
            return this;
        }

        public Builder<TResource> requestOptions(RequestOptions value) {
            this.requestOptions = value;
            return this;
        }

        public Builder<TResource> resource(TResource value) {
            this.resource = value;
            return this;
        }

        public ItemBatchOperation<TResource> build() {
            return new ItemBatchOperation<>(
                this.operationType,
                this.operationIndex,
                this.partitionKey,
                this.id,
                this.resource,
                this.requestOptions);
        }
    }
}
