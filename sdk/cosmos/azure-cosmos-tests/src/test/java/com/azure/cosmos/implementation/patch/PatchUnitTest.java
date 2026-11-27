// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.patch;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class PatchUnitTest {

    public static String path = "/random";

    @Test(groups = { "unit" })
    public void throwsOnNullArgument() {

        try {
            CosmosPatchOperations.create().add(null, "1");
            fail("Should throw IllegalArgumentException");

        } catch(IllegalArgumentException ex) {
            assertThat(ex.getMessage()).contains("path empty");
        }

        try {
            CosmosPatchOperations.create().remove(null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).contains("path empty");
        }
    }

    @Test(groups = { "unit" })
    public void constructPatchOperation() {
        CosmosPatchOperations cosmosPatchOperations = CosmosPatchOperations.create().add(path, "string");
        PatchUnitTest.validateOperations(cosmosPatchOperations, PatchOperationType.ADD, "string");

        Instant current = Instant.now();
        cosmosPatchOperations = CosmosPatchOperations.create().add(path, current);
        PatchUnitTest.validateOperations(cosmosPatchOperations, PatchOperationType.ADD, current);

        Object object = new  Object();
        cosmosPatchOperations = CosmosPatchOperations.create().add(path, object);
        PatchUnitTest.validateOperations(cosmosPatchOperations, PatchOperationType.ADD, object);

        cosmosPatchOperations = CosmosPatchOperations.create().remove(path);
        PatchUnitTest.validateOperations(cosmosPatchOperations, PatchOperationType.REMOVE, "value not required");

        int[] arrayObject = { 1, 2, 3 };
        cosmosPatchOperations = CosmosPatchOperations.create().replace(path, arrayObject);
        PatchUnitTest.validateOperations(cosmosPatchOperations, PatchOperationType.REPLACE, arrayObject);

        UUID uuid = UUID.randomUUID();
        cosmosPatchOperations = CosmosPatchOperations.create().set(path, uuid);
        PatchUnitTest.validateOperations(cosmosPatchOperations, PatchOperationType.SET, uuid);

        long incr = new Random().nextLong();
        cosmosPatchOperations = CosmosPatchOperations.create().increment(path, incr);
        PatchUnitTest.validateOperations(cosmosPatchOperations, PatchOperationType.INCREMENT, incr);

        double value = new Random().nextDouble();
        cosmosPatchOperations = CosmosPatchOperations.create().increment(path, value);
        PatchUnitTest.validateOperations(cosmosPatchOperations, PatchOperationType.INCREMENT, value);
    }

    @Test(groups = { "unit" })
    public void patchOperationValueUsesEffectiveCustomItemSerializer() {
        CosmosPatchOperations cosmosPatchOperations = CosmosPatchOperations.create().replace(path, "originalValue");

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setEffectiveItemSerializer(new MarkerCosmosItemSerializer());

        JsonSerializable serialized = PatchUtil.serializableBatchPatchOperation(cosmosPatchOperations, requestOptions);
        ObjectNode operation = (ObjectNode) serialized.getPropertyBag().get(PatchConstants.OPERATIONS).get(0);

        assertThat(operation.get(PatchConstants.PropertyNames_Value).isObject()).isTrue();
        assertThat(operation.get(PatchConstants.PropertyNames_Value).get("markerApplied").asBoolean()).isTrue();
        assertThat(operation.get(PatchConstants.PropertyNames_Value).get("rawValue").asText()).isEqualTo("originalValue");
    }

    @Test(groups = { "unit" })
    public void incrementOperationValueBypassesCustomItemSerializerForLong() {
        long incr = new Random().nextLong();
        CosmosPatchOperations cosmosPatchOperations = CosmosPatchOperations.create().increment(path, incr);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setEffectiveItemSerializer(new MarkerCosmosItemSerializer());

        JsonSerializable serialized = PatchUtil.serializableBatchPatchOperation(cosmosPatchOperations, requestOptions);
        ObjectNode operation = (ObjectNode) serialized.getPropertyBag().get(PatchConstants.OPERATIONS).get(0);

        assertThat(operation.get(PatchConstants.PropertyNames_Value).isObject()).isFalse();
        assertThat(operation.get(PatchConstants.PropertyNames_Value).isNumber()).isTrue();
        assertThat(operation.get(PatchConstants.PropertyNames_Value).longValue()).isEqualTo(incr);
    }

    @Test(groups = { "unit" })
    public void incrementOperationValueBypassesCustomItemSerializerForDouble() {
        double incr = new Random().nextDouble();
        CosmosPatchOperations cosmosPatchOperations = CosmosPatchOperations.create().increment(path, incr);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setEffectiveItemSerializer(new MarkerCosmosItemSerializer());

        JsonSerializable serialized = PatchUtil.serializableBatchPatchOperation(cosmosPatchOperations, requestOptions);
        ObjectNode operation = (ObjectNode) serialized.getPropertyBag().get(PatchConstants.OPERATIONS).get(0);

        assertThat(operation.get(PatchConstants.PropertyNames_Value).isObject()).isFalse();
        assertThat(operation.get(PatchConstants.PropertyNames_Value).isNumber()).isTrue();
        assertThat(operation.get(PatchConstants.PropertyNames_Value).doubleValue()).isEqualTo(incr);
    }

    @Test(groups = { "unit" })
    public void incrementOperationValueBypassesExplicitEffectiveItemSerializer() {
        long incr = new Random().nextLong();
        CosmosPatchOperations cosmosPatchOperations = CosmosPatchOperations.create().increment(path, incr);

        RequestOptions requestOptions = new RequestOptions();

        JsonSerializable serialized = PatchUtil.serializableBatchPatchOperation(
            cosmosPatchOperations, requestOptions, new MarkerCosmosItemSerializer());
        ObjectNode operation = (ObjectNode) serialized.getPropertyBag().get(PatchConstants.OPERATIONS).get(0);

        assertThat(operation.get(PatchConstants.PropertyNames_Value).isObject()).isFalse();
        assertThat(operation.get(PatchConstants.PropertyNames_Value).isNumber()).isTrue();
        assertThat(operation.get(PatchConstants.PropertyNames_Value).longValue()).isEqualTo(incr);
    }

    @Test(groups = { "unit" })
    public void patchOperationValueFallsBackToDefaultSerializerWhenNoCustomSerializerConfigured() {
        CosmosPatchOperations cosmosPatchOperations = CosmosPatchOperations.create().replace(path, "originalValue");

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setEffectiveItemSerializer(null);

        JsonSerializable serialized = PatchUtil.serializableBatchPatchOperation(cosmosPatchOperations, requestOptions);
        ObjectNode operation = (ObjectNode) serialized.getPropertyBag().get(PatchConstants.OPERATIONS).get(0);

        assertThat(operation.get(PatchConstants.PropertyNames_Value).asText()).isEqualTo("originalValue");
    }

    @Test(groups = { "unit" })
    public void patchOperationValueWorksWithNullRequestOptions() {
        CosmosPatchOperations cosmosPatchOperations = CosmosPatchOperations.create().replace(path, "originalValue");

        JsonSerializable serialized = PatchUtil.serializableBatchPatchOperation(cosmosPatchOperations, null);
        ObjectNode operation = (ObjectNode) serialized.getPropertyBag().get(PatchConstants.OPERATIONS).get(0);

        assertThat(operation.get(PatchConstants.PropertyNames_Value).asText()).isEqualTo("originalValue");
    }

    @Test(groups = { "unit" })
    public void patchOperationValueUsesExplicitEffectiveItemSerializerOverload() {
        CosmosPatchOperations cosmosPatchOperations = CosmosPatchOperations.create().replace(path, "originalValue");

        RequestOptions requestOptions = new RequestOptions();

        JsonSerializable serialized = PatchUtil.serializableBatchPatchOperation(
            cosmosPatchOperations, requestOptions, new MarkerCosmosItemSerializer());
        ObjectNode operation = (ObjectNode) serialized.getPropertyBag().get(PatchConstants.OPERATIONS).get(0);

        assertThat(operation.get(PatchConstants.PropertyNames_Value).isObject()).isTrue();
        assertThat(operation.get(PatchConstants.PropertyNames_Value).get("markerApplied").asBoolean()).isTrue();
        assertThat(operation.get(PatchConstants.PropertyNames_Value).get("rawValue").asText()).isEqualTo("originalValue");

        assertThat(requestOptions.getEffectiveItemSerializer())
            .isSameAs(CosmosItemSerializer.DEFAULT_SERIALIZER)
            .isNotInstanceOf(MarkerCosmosItemSerializer.class);
    }

    @Test(groups = { "unit" })
    public void explicitEffectiveItemSerializerOverloadIsIndependentOfRequestOptions() {
        CosmosPatchOperations cosmosPatchOperations = CosmosPatchOperations.create().replace(path, "originalValue");

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setEffectiveItemSerializer(new OtherMarkerCosmosItemSerializer());

        JsonSerializable serialized = PatchUtil.serializableBatchPatchOperation(
            cosmosPatchOperations, requestOptions, new MarkerCosmosItemSerializer());
        ObjectNode operation = (ObjectNode) serialized.getPropertyBag().get(PatchConstants.OPERATIONS).get(0);

        assertThat(operation.get(PatchConstants.PropertyNames_Value).get("markerApplied").asBoolean()).isTrue();
        assertThat(operation.get(PatchConstants.PropertyNames_Value).has("otherMarkerApplied")).isFalse();
    }

    @Test(groups = { "unit" })
    public void patchOperationValueFallsBackToDefaultSerializerWhenCandidateCannotSerialize() {
        CosmosPatchOperations cosmosPatchOperations = CosmosPatchOperations.create().replace(path, "originalValue");

        MarkerCosmosItemSerializer candidate = new MarkerCosmosItemSerializer();
        ImplementationBridgeHelpers.CosmosItemSerializerHelper.getCosmosItemSerializerAccessor()
            .setCanSerialize(candidate, false);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setEffectiveItemSerializer(candidate);

        JsonSerializable serialized = PatchUtil.serializableBatchPatchOperation(cosmosPatchOperations, requestOptions);
        ObjectNode operation = (ObjectNode) serialized.getPropertyBag().get(PatchConstants.OPERATIONS).get(0);

        assertThat(operation.get(PatchConstants.PropertyNames_Value).asText()).isEqualTo("originalValue");
    }

    @Test(groups = { "unit" })
    public void explicitEffectiveItemSerializerFallsBackToDefaultWhenCandidateCannotSerialize() {
        CosmosPatchOperations cosmosPatchOperations = CosmosPatchOperations.create().replace(path, "originalValue");

        MarkerCosmosItemSerializer candidate = new MarkerCosmosItemSerializer();
        ImplementationBridgeHelpers.CosmosItemSerializerHelper.getCosmosItemSerializerAccessor()
            .setCanSerialize(candidate, false);

        RequestOptions requestOptions = new RequestOptions();

        JsonSerializable serialized = PatchUtil.serializableBatchPatchOperation(
            cosmosPatchOperations, requestOptions, candidate);
        ObjectNode operation = (ObjectNode) serialized.getPropertyBag().get(PatchConstants.OPERATIONS).get(0);

        assertThat(operation.get(PatchConstants.PropertyNames_Value).asText()).isEqualTo("originalValue");
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

    private static class OtherMarkerCosmosItemSerializer extends CosmosItemSerializer {
        @Override
        public <T> Map<String, Object> serialize(T item) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("otherMarkerApplied", true);
            map.put("rawValue", item);
            return map;
        }

        @Override
        public <T> T deserialize(Map<String, Object> jsonNodeMap, Class<T> classType) {
            throw new UnsupportedOperationException();
        }
    }

    private static <T> void validateOperations(CosmosPatchOperations cosmosPatchOperations, PatchOperationType operationType, T value) {
        List<PatchOperation> patchOperations = ModelBridgeInternal.getPatchOperationsFromCosmosPatch(cosmosPatchOperations);
        assertThat(patchOperations.size()).isEqualTo(1);

        PatchOperation patchOperation = patchOperations.get(0);
        assertThat(patchOperation.getOperationType()).isEqualTo(operationType);

        if(patchOperation instanceof PatchOperationCore) {
            assertThat(((PatchOperationCore) patchOperation).getPath()).isEqualTo(path);

            if(!operationType.equals(PatchOperationType.REMOVE)) {
                assertThat(((PatchOperationCore) patchOperation).getResource()).isEqualTo(value);
            }
        }
    }
}
