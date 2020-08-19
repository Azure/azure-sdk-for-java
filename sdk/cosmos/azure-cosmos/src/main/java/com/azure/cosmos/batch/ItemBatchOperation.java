// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import com.azure.cosmos.implementation.*;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;
import com.azure.cosmos.models.PartitionKey;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.azure.cosmos.batch.BatchRequestResponseConstant.*;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Represents an operation on an item which will be executed as part of a batch request on a container.
 */
public final class ItemBatchOperation<TResource> implements AutoCloseable {

    private TResource resource;
    private String materialisedResource;

    private ItemBatchOperationContext context;
    private String id;
    private int operationIndex;
    private PartitionKey partitionKey;
    private String partitionKeyJson;
    private final OperationType operationType;
    private RequestOptions requestOptions;

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
    public static JsonSerializable writeOperation(ItemBatchOperation<?> operation) {
        JsonSerializable jsonSerializable = new JsonSerializable();

        jsonSerializable.set(FIELD_OPERATION_TYPE, BatchExecUtils.getStringOperationType(operation.getOperationType()));

        if (operation.getPartitionKey() != null && StringUtils.isNotEmpty(operation.getPartitionKey().toString())) {
            jsonSerializable.set(FIELD_PARTITION_KEY, operation.getPartitionKeyJson());
        }

        if (StringUtils.isNotEmpty(operation.getId())) {
            jsonSerializable.set(FIELD_ID, operation.getId());
        }

        if (operation.getResource() != null) {
            jsonSerializable.set(FIELD_RESOURCE_BODY, operation.getResource());
        }

        if (operation.getRequestOptions() != null) {
            RequestOptions requestOptions = operation.getRequestOptions();

            if (StringUtils.isNotEmpty(requestOptions.getIfMatchETag())) {
                jsonSerializable.set(FIELD_IF_MATCH, requestOptions.getIfMatchETag());
            }

            if (StringUtils.isNotEmpty(requestOptions.getIfNoneMatchETag())) {
                jsonSerializable.set(FIELD_IF_NONE_MATCH, requestOptions.getIfNoneMatchETag());
            }
        }

        return jsonSerializable;
    }

    /**
     * Computes an underestimate of the serialized length of this {@link ItemBatchOperation}.
     *
     * @return an underestimate of the serialized length of this {@link ItemBatchOperation}.
     */
    public int getApproximateSerializedLength() {

        int length = 0;

        if (this.getPartitionKeyJson() != null) {
            length += this.getPartitionKeyJson().length();
        }

        if (this.getId() != null) {
            length += this.getId().length();
        }

        if (!Strings.isNullOrEmpty(this.materialisedResource)) {
            length += this.materialisedResource.length();
        }

        RequestOptions requestOptions = this.getRequestOptions();

        if (requestOptions != null) {

            if (requestOptions.getIfMatchETag() != null) {
                length += requestOptions.getIfNoneMatchETag().length();
            } else if (requestOptions.getIfNoneMatchETag() != null) {
                length += requestOptions.getIfNoneMatchETag().length();
            }

            if (requestOptions.getIndexingDirective() != null) {
                length += 7; // "Default", "Include", "Exclude" are possible values
            }

            Map<String, Object> properties = requestOptions.getProperties();

            if (properties != null) {

                byte[] binaryId = (byte[]) properties.computeIfPresent(WFConstants.BackendHeaders.BINARY_ID, (k, v) ->
                    v instanceof byte[] ? (byte[]) v : null);

                if (binaryId != null) {
                    length += binaryId.length;
                }

                byte[] epk = (byte[]) properties.computeIfPresent(WFConstants.BackendHeaders.EFFECTIVE_PARTITION_KEY, (k, v) ->
                    v instanceof byte[] ? (byte[]) v : null);

                if (epk != null) {
                    length += epk.length;
                }
            }
        }

        return length;
    }

    /**
     * Materializes the operation's resource into a String asynchronously.
     *
     * @return a {@link CompletableFuture future} that will complete when the resource is materialized or an error
     * occurs.
     */
    public CompletableFuture<Boolean> materializeResource() {

        if (this.materialisedResource == null && this.resource != null) {

            return CompletableFuture.completedFuture(true).thenApply(t -> {
                try{
                    this.materialisedResource = Utils.getSimpleObjectMapper().writeValueAsString(this.resource);
                } catch (Exception ex) {
                    throw new CompletionException(ex);
                }

                return true;
            });
        }

        return CompletableFuture.completedFuture(Boolean.TRUE);
    }

    /**
     * @see BatchAsyncBatcher
     * @see BatchAsyncContainerExecutor
     * @see BatchAsyncStreamer
     *
     * @return a {@link ItemBatchOperationContext operational context} used in stream operations.
     */
    public ItemBatchOperationContext getContext() {
        return context;
    }

    /**
     * Attaches a {@link ItemBatchOperationContext context} to the {@link ItemBatchOperation current operation}.
     * <p>
     * The attached {@link ItemBatchOperationContext context} is used to track resolution.
     *
     * @param context the {@link ItemBatchOperationContext context} to attach.
     *
     * @return a reference to the {@link ItemBatchOperation current operation}.
     */
    public ItemBatchOperation<?> attachContext(final ItemBatchOperationContext context) {
        checkNotNull(context, "expected non-null context");

        if (this.context != null)
        {
            throw new IllegalArgumentException("Cannot modify the current context of an operation.");
        }

        this.context = context;
        return this;
    }

    public String getId() {
        return this.id;
    }

    public int getOperationIndex() {
        return this.operationIndex;
    }

    public ItemBatchOperation<?> setOperationIndex(final int value) {
        this.operationIndex = value;
        return this;
    }

    public OperationType getOperationType() {
        return this.operationType;
    }

    public PartitionKey getPartitionKey() {
        return partitionKey;
    }

    public ItemBatchOperation<?> setPartitionKey(final PartitionKey value) {
        partitionKey = value;
        return this;
    }

    public String getPartitionKeyJson() {
        return partitionKeyJson;
    }

    public ItemBatchOperation<?> setPartitionKeyJson(final String value) {
        partitionKeyJson = value;
        return this;
    }

    public RequestOptions getRequestOptions() {
        return requestOptions;
    }

    public TResource getResource() {
        return resource;
    }

    public String getMaterialisedResource() {
        return materialisedResource;
    }

    public void setMaterialisedResource(String materialisedResource) {
        this.materialisedResource = materialisedResource;
    }

    /**
     * Closes this {@link ItemBatchOperation}.
     */
    public void close() {
        try {
            if (this.resource instanceof AutoCloseable) {
                ((AutoCloseable) this.resource).close();  // assumes an idempotent close implementation
            }
        } catch (Exception ex) {
            //
        }
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
