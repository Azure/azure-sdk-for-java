// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.experimental.serializer.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

/**
 * Helper methods for converting between Azure Core and Jackson types.
 */
final class JsonNodeUtils {
    private static final ClientLogger LOGGER = new ClientLogger(ClientLogger.class);

    /**
     * Converts an Azure Core {@link JsonNode} into a Jackson {@link com.fasterxml.jackson.databind.JsonNode}.
     *
     * @param jsonNode The Azure Core {@link JsonNode}.
     * @return The corresponding Jackson {@link com.fasterxml.jackson.databind.JsonNode}.
     * @throws IllegalArgumentException If the {@link JsonNode} cannot be converted to a
     * {@link com.fasterxml.jackson.databind.JsonNode}.
     */
    public static com.fasterxml.jackson.databind.JsonNode toJacksonNode(JsonNode jsonNode) {
        if (jsonNode.isArray()) {
            if (jsonNode instanceof JacksonJsonArray) {
                return ((JacksonJsonArray) jsonNode).getArrayNode();
            }

            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("JsonNode is an array but isn't JacksonJsonArray."));
        } else if (jsonNode.isNull()) {
            if (jsonNode instanceof JacksonJsonNull) {
                return ((JacksonJsonNull) jsonNode).getNullNode();
            }

            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("JsonNode is a null but isn't JacksonJsonNull."));
        } else if (jsonNode.isObject()) {
            if (jsonNode instanceof JacksonJsonObject) {
                return ((JacksonJsonObject) jsonNode).getObjectNode();
            }

            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("JsonNode is an array but isn't JacksonJsonObject."));
        } else if (jsonNode.isValue()) {
            if (jsonNode instanceof JacksonJsonPrimitive) {
                return ((JacksonJsonPrimitive) jsonNode).getValueNode();
            }

            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("JsonNode is a value but isn't JacksonJsonPrimitive."));
        }

        throw LOGGER.logExceptionAsError(new IllegalArgumentException("Unknown JsonNode type."));
    }

    /**
     * Converts an Jackson {@link com.fasterxml.jackson.databind.JsonNode} into an Azure Core {@link JsonNode}.
     *
     * @param jsonNode The Jackson {@link com.fasterxml.jackson.databind.JsonNode}.
     * @return The corresponding Azure Core {@link JsonNode}.
     * @throws IllegalArgumentException If the {@link com.fasterxml.jackson.databind.JsonNode} cannot be converted to a
     * {@link JsonNode}.
     */
    public static JsonNode fromJacksonNode(com.fasterxml.jackson.databind.JsonNode jsonNode) {
        if (jsonNode.isArray() && jsonNode instanceof ArrayNode) {
            return new JacksonJsonArray((ArrayNode) jsonNode);
        } else if (jsonNode.isNull() && jsonNode instanceof NullNode) {
            return JacksonJsonNull.INSTANCE;
        } else if (jsonNode.isObject() && jsonNode instanceof ObjectNode) {
            return new JacksonJsonObject((ObjectNode) jsonNode);
        } else if (jsonNode.isValueNode() && jsonNode instanceof ValueNode) {
            return new JacksonJsonPrimitive((ValueNode) jsonNode);
        }

        throw LOGGER.logExceptionAsError(new IllegalArgumentException("Unknown JsonNode type."));
    }
}

