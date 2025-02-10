// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.ObjectNodeMap;
import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

public final class ValueUnwrapCosmosItemSerializer extends CosmosItemSerializer {
    private static ValueUnwrapCosmosItemSerializer EnableUnwrapSingletonInstance = new ValueUnwrapCosmosItemSerializer(true);
    private static ValueUnwrapCosmosItemSerializer DisableUnwrapSingletonInstance = new ValueUnwrapCosmosItemSerializer(false);

    public static CosmosItemSerializer create(boolean shouldUnwrapValue) {
        if (shouldUnwrapValue) {
            return EnableUnwrapSingletonInstance;
        }

        return DisableUnwrapSingletonInstance;
    }


    private final boolean shouldUnwrapValue;
    private ValueUnwrapCosmosItemSerializer(boolean shouldUnwrapValue) {
        ImplementationBridgeHelpers
            .CosmosItemSerializerHelper
            .getCosmosItemSerializerAccessor()
            .setShouldWrapSerializationExceptions(this, false);
        this.shouldUnwrapValue = shouldUnwrapValue;
    }

    @Override
    public <T> Map<String, Object> serialize(T item) {
        throw new IllegalStateException("Method 'serialize' is not implemented for this serializer");
    }

    @Override
    public <T> T deserialize(Map<String, Object> jsonNodeMap, Class<T> classType) {

        if (jsonNodeMap == null) {
            return null;
        }

        ObjectNode jsonNode;
        if (jsonNodeMap instanceof ObjectNodeMap) {
            jsonNode = ((ObjectNodeMap)jsonNodeMap).getObjectNode();
        } else {
            jsonNode = Utils.getSimpleObjectMapper().convertValue(jsonNodeMap, ObjectNode.class);
        }

        return JsonSerializable.toObjectFromObjectNode(jsonNode, this.shouldUnwrapValue, classType);
    }
}