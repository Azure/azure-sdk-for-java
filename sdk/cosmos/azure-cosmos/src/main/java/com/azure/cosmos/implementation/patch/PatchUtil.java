// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.patch;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers.CosmosItemSerializerHelper.CosmosItemSerializerAccessor;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.List;

public final class PatchUtil {

    private static CosmosItemSerializerAccessor cosmosItemSerializerAccessor() {
        return ImplementationBridgeHelpers.CosmosItemSerializerHelper.getCosmosItemSerializerAccessor();
    }

    public static JsonSerializable serializableBatchPatchOperation(CosmosPatchOperations patchOperations, RequestOptions requestOptions) {
        return cosmosPatchToJsonSerializable(patchOperations, requestOptions, resolveEffectiveItemSerializer(requestOptions));
    }

    public static JsonSerializable serializableBatchPatchOperation(
        CosmosPatchOperations patchOperations,
        RequestOptions requestOptions,
        CosmosItemSerializer effectiveItemSerializer) {

        return cosmosPatchToJsonSerializable(patchOperations, requestOptions, resolveEffectiveItemSerializer(effectiveItemSerializer));
    }

    public static byte[] serializeCosmosPatchToByteArray(CosmosPatchOperations patchOperations, RequestOptions requestOptions) {
        JsonSerializable jsonSerializable =
            cosmosPatchToJsonSerializable(patchOperations, requestOptions, resolveEffectiveItemSerializer(requestOptions));

        byte[] serializedBody;
        try {
            // Object mapper also writes with utf-8. Need to change when object mapper change it's behaviour
            serializedBody = Utils.getSimpleObjectMapper().writeValueAsBytes(jsonSerializable.getPropertyBag());
        } catch (Exception e) {
            throw new IllegalArgumentException("Can't serialize the object into the byte array", e);
        }

        return serializedBody;
    }

    private static JsonSerializable cosmosPatchToJsonSerializable(
        CosmosPatchOperations patchOperations,
        RequestOptions requestOptions,
        CosmosItemSerializer effectiveItemSerializer) {

        JsonSerializable jsonSerializable = new JsonSerializable();
        ArrayNode operations = Utils.getSimpleObjectMapper().createArrayNode();
        List<PatchOperation> patchOperationList = ModelBridgeInternal.getPatchOperationsFromCosmosPatch(patchOperations);

        for (PatchOperation patchOperation : patchOperationList) {

            JsonSerializable operationJsonSerializable = new JsonSerializable();
            operationJsonSerializable.set(
                PatchConstants.PropertyNames_OperationType,
                patchOperation.getOperationType().getOperationValue()
            );

            if (patchOperation instanceof PatchOperationCore) {
                if (patchOperation.getOperationType() == PatchOperationType.MOVE) {
                    operationJsonSerializable.set(
                        PatchConstants.PropertyNames_Path,
                        ((PatchOperationCore)patchOperation).getPath()
                    );
                    operationJsonSerializable.set(
                        PatchConstants.PropertyNames_From,
                        ((PatchOperationCore)patchOperation).getFrom()
                    );
                }
                else {
                    operationJsonSerializable.set(
                        PatchConstants.PropertyNames_Path,
                        ((PatchOperationCore)patchOperation).getPath()
                    );

                    if (patchOperation.getOperationType() == PatchOperationType.INCREMENT) {
                        operationJsonSerializable.set(
                            PatchConstants.PropertyNames_Value,
                            ((PatchOperationCore)patchOperation).getResource()
                        );
                    } else {
                        operationJsonSerializable.set(
                            PatchConstants.PropertyNames_Value,
                            ((PatchOperationCore)patchOperation).getResource(),
                            effectiveItemSerializer,
                            true
                        );
                    }
                }
            } else {
                throw new IllegalArgumentException("Invalid patch operation type");
            }

            operations.add(operationJsonSerializable.getPropertyBag());
        }

        jsonSerializable.set(
            PatchConstants.OPERATIONS,
            operations
        );

        if(requestOptions != null) {
            String filterPredicate = requestOptions.getFilterPredicate();
            if(filterPredicate != null) {
                if (!filterPredicate.isEmpty()) {
                    jsonSerializable.set(
                        PatchConstants.CONDITION,
                        filterPredicate
                    );
                }
            }
        }

        return jsonSerializable;
    }

    private static CosmosItemSerializer resolveEffectiveItemSerializer(RequestOptions requestOptions) {
        if (requestOptions == null) {
            return CosmosItemSerializer.DEFAULT_SERIALIZER;
        }

        return resolveEffectiveItemSerializer(requestOptions.getEffectiveItemSerializer());
    }

    private static CosmosItemSerializer resolveEffectiveItemSerializer(CosmosItemSerializer candidateSerializer) {
        if (candidateSerializer == null) {
            return CosmosItemSerializer.DEFAULT_SERIALIZER;
        }

        if (candidateSerializer != CosmosItemSerializer.DEFAULT_SERIALIZER
            && cosmosItemSerializerAccessor().canSerialize(candidateSerializer)) {
            return candidateSerializer;
        }

        return CosmosItemSerializer.DEFAULT_SERIALIZER;
    }
}
