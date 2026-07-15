// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.batch.hybridrow.HybridRowBatchCodec;
import com.azure.cosmos.implementation.json.CosmosBinaryJacksonCodec;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemOperationType;
import com.azure.cosmos.models.IndexingDirective;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;

final class HybridRowBatchMapper {
    private HybridRowBatchMapper() {
    }

    static byte[] encodeOperation(CosmosItemOperation operation, CosmosItemSerializer serializer) {
        if (!(operation instanceof CosmosItemOperationBase)) {
            throw new IllegalArgumentException("Unsupported Cosmos batch operation: " + operation.getClass());
        }
        CosmosItemOperationBase internalOperation = (CosmosItemOperationBase) operation;
        JsonSerializable serialized = internalOperation.getSerializedOperation(serializer);
        ObjectNode fields = serialized.getPropertyBag();
        WireOptions options = options(internalOperation);
        return HybridRowBatchCodec.encodeOperation(
            operation.getOperationType(),
            text(fields, BatchRequestResponseConstants.FIELD_PARTITION_KEY),
            operation.getId(),
            resourceBody(operation.getOperationType(), fields),
            options.indexingDirective,
            options.ifMatch,
            options.ifNoneMatch,
            options.minimalReturn);
    }

    private static byte[] resourceBody(CosmosItemOperationType operationType, ObjectNode fields) {
        JsonNode value = fields.get(BatchRequestResponseConstants.FIELD_RESOURCE_BODY);
        if (value == null || value.isNull()) {
            return null;
        }
        if (operationType == CosmosItemOperationType.PATCH) {
            return value.toString().getBytes(StandardCharsets.UTF_8);
        }
        return CosmosBinaryJacksonCodec.encode(value);
    }

    private static WireOptions options(CosmosItemOperationBase operation) {
        RequestOptions options = null;
        if (operation instanceof ItemBatchOperation<?>) {
            options = ((ItemBatchOperation<?>) operation).getRequestOptions();
        } else if (operation instanceof ItemBulkOperation<?, ?>) {
            options = ((ItemBulkOperation<?, ?>) operation).getRequestOptions();
        }
        if (options == null) {
            return WireOptions.NONE;
        }
        IndexingDirective indexing = options.getIndexingDirective();
        boolean minimalReturn = options.isContentResponseOnWriteEnabled() != null
            && !options.isContentResponseOnWriteEnabled();
        return new WireOptions(
            indexing == null ? null : indexing.toString(),
            options.getIfMatchETag(),
            options.getIfNoneMatchETag(),
            minimalReturn);
    }

    private static final class WireOptions {
        private static final WireOptions NONE = new WireOptions(null, null, null, false);
        private final String indexingDirective;
        private final String ifMatch;
        private final String ifNoneMatch;
        private final boolean minimalReturn;

        private WireOptions(String indexingDirective, String ifMatch, String ifNoneMatch, boolean minimalReturn) {
            this.indexingDirective = indexingDirective;
            this.ifMatch = ifMatch;
            this.ifNoneMatch = ifNoneMatch;
            this.minimalReturn = minimalReturn;
        }
    }

    private static String text(ObjectNode fields, String name) {
        JsonNode value = fields.get(name);
        return value == null || value.isNull() ? null : value.textValue();
    }
}
