// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.models.CosmosItemOperationType;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that patch operations executed as part of a {@link com.azure.cosmos.models.CosmosBatch} or
 * bulk ({@link com.azure.cosmos.models.CosmosBulkOperations}) request honor the effective item serializer
 * that was resolved for the batch/bulk request (merged from the per-operation/batch-level request options
 * and the client-level default custom serializer), rather than silently falling back to the default serializer.
 */
public class ItemOperationPatchSerializerTest {

    private static final String RESOURCE_BODY = "resourceBody";
    private static final String OPERATIONS = "operations";
    private static final String VALUE = "value";

    @Test(groups = { "unit" })
    public void itemBatchOperationPatchUsesEffectiveItemSerializer() {
        CosmosPatchOperations patchOperations = CosmosPatchOperations.create().replace("/foo", "originalValue");
        RequestOptions requestOptions = new RequestOptions();

        ItemBatchOperation<CosmosPatchOperations> operation = new ItemBatchOperation<>(
            CosmosItemOperationType.PATCH,
            "id1",
            PartitionKey.NONE,
            requestOptions,
            patchOperations
        );

        JsonSerializable serialized = operation.getSerializedOperation(new MarkerCosmosItemSerializer());

        ObjectNode patchValue = extractPatchValueNode(serialized);
        assertThat(patchValue.get("markerApplied").asBoolean()).isTrue();
        assertThat(patchValue.get("rawValue").asText()).isEqualTo("originalValue");

        assertThat(requestOptions.getEffectiveItemSerializer())
            .isSameAs(CosmosItemSerializer.DEFAULT_SERIALIZER)
            .isNotInstanceOf(MarkerCosmosItemSerializer.class);
        assertThat(operation.getRequestOptions()).isSameAs(requestOptions);
    }

    @Test(groups = { "unit" })
    public void itemBulkOperationPatchUsesEffectiveItemSerializer() {
        CosmosPatchOperations patchOperations = CosmosPatchOperations.create().replace("/foo", "originalValue");
        RequestOptions requestOptions = new RequestOptions();

        ItemBulkOperation<CosmosPatchOperations, Void> operation = new ItemBulkOperation<>(
            CosmosItemOperationType.PATCH,
            "id1",
            PartitionKey.NONE,
            requestOptions,
            patchOperations,
            null
        );

        JsonSerializable serialized = operation.getSerializedOperation(new MarkerCosmosItemSerializer());

        ObjectNode patchValue = extractPatchValueNode(serialized);
        assertThat(patchValue.get("markerApplied").asBoolean()).isTrue();
        assertThat(patchValue.get("rawValue").asText()).isEqualTo("originalValue");

        assertThat(requestOptions.getEffectiveItemSerializer())
            .isSameAs(CosmosItemSerializer.DEFAULT_SERIALIZER)
            .isNotInstanceOf(MarkerCosmosItemSerializer.class);
        assertThat(operation.getRequestOptions()).isSameAs(requestOptions);
    }

    private static ObjectNode extractPatchValueNode(JsonSerializable serializedOperation) {
        ObjectNode resourceBody = (ObjectNode) serializedOperation.getPropertyBag().get(RESOURCE_BODY);
        ArrayNode operations = (ArrayNode) resourceBody.get(OPERATIONS);
        return (ObjectNode) operations.get(0).get(VALUE);
    }

    private static class MarkerCosmosItemSerializer extends CosmosItemSerializer {
        @Override
        public <T> Map<String, Object> serialize(T item) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("markerApplied", true);
            map.put("rawValue", item);
            return map;
        }

        @Override
        public <T> T deserialize(Map<String, Object> jsonNodeMap, Class<T> classType) {
            throw new UnsupportedOperationException();
        }
    }
}
