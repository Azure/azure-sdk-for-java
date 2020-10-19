// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.patch;

import com.azure.cosmos.PatchOperation;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.List;

public class PatchUtil {

    public static String serializePatchOperations(List<PatchOperation> patchOperations, RequestOptions options) {
        ArrayNode operations =  Utils.getSimpleObjectMapper().createArrayNode();

        for(PatchOperation patchOperation : patchOperations) {

            JsonSerializable jsonSerializable = new JsonSerializable();
            jsonSerializable.set(PatchConstants.PropertyNames_OperationType, patchOperation.getOperationType().getStringValue());

            if (patchOperation instanceof PatchOperationCore) {
                jsonSerializable.set(PatchConstants.PropertyNames_Path, ((PatchOperationCore)patchOperation).getPath());
                jsonSerializable.set(PatchConstants.PropertyNames_Value, ((PatchOperationCore)patchOperation).getResource());
            } else {
                throw IllegalArgumentException();
            }

            operations.add(jsonSerializable.getPropertyBag());
        }

        return operations.toString();
    }
}
