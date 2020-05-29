// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.util.serializer.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

/**
 * Helper methods for converting between Azure Core and Jackson types.
 */
final class JsonNodeUtils {
    public static com.fasterxml.jackson.databind.JsonNode toJacksonNode(JsonNode jsonNode) {
        if (jsonNode.isArray()) {
            if (jsonNode instanceof JacksonJsonArray) {
                return ((JacksonJsonArray) jsonNode).getArrayNode();
            }

            throw new IllegalArgumentException("JsonNode is an array but isn't JacksonJsonArray.");
        } else if (jsonNode.isNull()) {
            if (jsonNode instanceof JacksonJsonNull) {
                return ((JacksonJsonNull) jsonNode).getNullNode();
            }

            throw new IllegalArgumentException("JsonNode is a null but isn't JacksonJsonNull.");
        } else if (jsonNode.isObject()) {
            if (jsonNode instanceof JacksonJsonObject) {
                return ((JacksonJsonObject) jsonNode).getObjectNode();
            }

            throw new IllegalArgumentException("JsonNode is an array but isn't JacksonJsonObject.");
        } else if (jsonNode.isValue()) {
            if (jsonNode instanceof JacksonJsonValue) {
                return ((JacksonJsonValue) jsonNode).getValueNode();
            }

            throw new IllegalArgumentException("JsonNode is a value but isn't JacksonJsonValue.");
        }

        throw new IllegalArgumentException("Unknown JsonNode type.");
    }

    public static JsonNode fromJacksonNode(com.fasterxml.jackson.databind.JsonNode jsonNode) {
        if (jsonNode.isArray()) {
            return new JacksonJsonArray((ArrayNode) jsonNode);
        } else if (jsonNode.isNull()) {
            return new JacksonJsonNull((NullNode) jsonNode);
        } else if (jsonNode.isObject()) {
            return new JacksonJsonObject((ObjectNode) jsonNode);
        } else if (jsonNode.isValueNode()) {
            return new JacksonJsonValue((ValueNode) jsonNode);
        }

        throw new IllegalArgumentException("Unknown JsonNode type.");
    }
}

