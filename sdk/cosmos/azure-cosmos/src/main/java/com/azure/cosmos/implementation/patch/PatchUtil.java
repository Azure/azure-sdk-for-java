// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.patch;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosPatchOperations;
import com.azure.cosmos.implementation.patch.PatchConstants;
import com.azure.cosmos.implementation.PatchSpec;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.List;

public final class PatchUtil {

    public static <T> JsonSerializable serializableBatchPatchOperation(T item) {
        if (item instanceof PatchSpec) {
            return cosmosPatchToJsonSerializable((PatchSpec) item);
        } else {
            throw new UnsupportedOperationException("Unknown Patch operations.");
        }
    }

    public static byte[] serializeCosmosPatchToByteArray(PatchSpec cosmosPatchSpec) {
        JsonSerializable jsonSerializable = cosmosPatchToJsonSerializable(cosmosPatchSpec);

        byte[] serializedBody;
        try {
            // Object mapper also writes with utf-8. Need to change when object mapper change it's behaviour
            serializedBody = Utils.getSimpleObjectMapper().writeValueAsBytes(jsonSerializable.getPropertyBag());
        } catch (Exception e) {
            throw new IllegalArgumentException("Can't serialize the object into the byte array", e);
        }

        return serializedBody;
    }

    private static JsonSerializable cosmosPatchToJsonSerializable(PatchSpec cosmosPatchSpec) {
        JsonSerializable jsonSerializable = new JsonSerializable();
        ArrayNode operations = Utils.getSimpleObjectMapper().createArrayNode();
        List<PatchOperation> patchOperations = BridgeInternal.getPatchOperationsFromCosmosPatch(cosmosPatchSpec.getCosmosPatchOperations());

        for (PatchOperation patchOperation : patchOperations) {

            JsonSerializable operationJsonSerializable = new JsonSerializable();
            operationJsonSerializable.set(PatchConstants.PropertyNames_OperationType, patchOperation.getOperationType().getOperationValue());

            if (patchOperation instanceof PatchOperationCore) {
                operationJsonSerializable.set(PatchConstants.PropertyNames_Path, ((PatchOperationCore)patchOperation).getPath());
                operationJsonSerializable.set(PatchConstants.PropertyNames_Value, ((PatchOperationCore)patchOperation).getResource());
            } else {
                throw new IllegalArgumentException("Invalid patch operation type");
            }

            operations.add(operationJsonSerializable.getPropertyBag());
        }

        jsonSerializable.set(PatchConstants.OPERATIONS, operations);

        if(cosmosPatchSpec.getRequestOptions() != null) {
            String filterPredicate = cosmosPatchSpec.getRequestOptions().getFilterPredicate();
            if(filterPredicate != null) {
                if (!filterPredicate.isEmpty()) {
                    jsonSerializable.set(PatchConstants.CONDITION, filterPredicate);
                }
            }
        }

        return jsonSerializable;
    }
}
