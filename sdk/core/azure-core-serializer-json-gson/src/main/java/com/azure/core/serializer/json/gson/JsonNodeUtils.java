// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.experimental.serializer.JsonNode;
import com.google.gson.JsonElement;

/**
 * Helper methods for converting between Azure Core and GSON types.
 */
final class JsonNodeUtils {
    private static final ClientLogger LOGGER = new ClientLogger(JsonNodeUtils.class);

    /**
     * Converts an Azure Core {@link JsonNode} into a GSON {@link JsonElement}.
     *
     * @param jsonNode The Azure Core {@link JsonNode}.
     * @return The corresponding GSON {@link JsonElement}.
     * @throws IllegalArgumentException If the {@link JsonNode} cannot be converted to a {@link JsonElement}.
     */
    public static JsonElement toGsonElement(JsonNode jsonNode) {
        if (jsonNode.isArray()) {
            if (jsonNode instanceof GsonJsonArray) {
                return ((GsonJsonArray) jsonNode).getJsonArray();
            }

            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("JsonNode is an array but isn't GsonJsonArray."));
        } else if (jsonNode.isNull()) {
            if (jsonNode instanceof GsonJsonNull) {
                return ((GsonJsonNull) jsonNode).getJsonNull();
            }

            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("JsonNode is a null but isn't GsonJsonNull."));
        } else if (jsonNode.isObject()) {
            if (jsonNode instanceof GsonJsonObject) {
                return ((GsonJsonObject) jsonNode).getJsonObject();
            }

            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("JsonNode is an array but isn't GsonJsonObject."));
        } else if (jsonNode.isValue()) {
            if (jsonNode instanceof GsonJsonPrimitive) {
                return ((GsonJsonPrimitive) jsonNode).getJsonPrimitive();
            }

            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("JsonNode is a value but isn't GsonJsonPrimitive."));
        }

        throw LOGGER.logExceptionAsError(new IllegalArgumentException("Unknown JsonNode type."));
    }

    /**
     * Converts an GSON {@link JsonElement} into an Azure Core {@link JsonNode}.
     *
     * @param jsonElement The GSON {@link JsonElement}.
     * @return The corresponding Azure Core {@link JsonNode}.
     * @throws IllegalArgumentException If the {@link JsonElement} cannot be converted to a {@link JsonNode}.
     */
    public static JsonNode fromGsonElement(JsonElement jsonElement) {
        if (jsonElement.isJsonArray()) {
            return new GsonJsonArray(jsonElement.getAsJsonArray());
        } else if (jsonElement.isJsonNull()) {
            return GsonJsonNull.INSTANCE;
        } else if (jsonElement.isJsonObject()) {
            return new GsonJsonObject(jsonElement.getAsJsonObject());
        } else if (jsonElement.isJsonPrimitive()) {
            return new GsonJsonPrimitive(jsonElement.getAsJsonPrimitive());
        }

        throw LOGGER.logExceptionAsError(new IllegalArgumentException("Unknown JsonElement type."));
    }
}
