// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.util.serializer.JsonNode;
import com.google.gson.JsonElement;

/**
 * Helper methods for converting between Azure Core and GSON types.
 */
final class JsonNodeUtils {
    public static JsonElement toGsonElement(JsonNode jsonNode) {
        if (jsonNode.isArray()) {
            if (jsonNode instanceof GsonJsonArray) {
                return ((GsonJsonArray) jsonNode).getJsonArray();
            }

            throw new IllegalArgumentException("JsonNode is an array but isn't GsonJsonArray.");
        } else if (jsonNode.isNull()) {
            if (jsonNode instanceof GsonJsonNull) {
                return ((GsonJsonNull) jsonNode).getJsonNull();
            }

            throw new IllegalArgumentException("JsonNode is a null but isn't GsonJsonNull.");
        } else if (jsonNode.isObject()) {
            if (jsonNode instanceof GsonJsonObject) {
                return ((GsonJsonObject) jsonNode).getJsonObject();
            }

            throw new IllegalArgumentException("JsonNode is an array but isn't GsonJsonObject.");
        } else if (jsonNode.isValue()) {
            if (jsonNode instanceof GsonJsonValue) {
                return ((GsonJsonValue) jsonNode).getJsonPrimitive();
            }

            throw new IllegalArgumentException("JsonNode is a value but isn't GsonJsonValue.");
        }

        throw new IllegalArgumentException("Unknown JsonNode type.");
    }

    public static JsonNode fromGsonElement(JsonElement jsonElement) {
        if (jsonElement.isJsonArray()) {
            return new GsonJsonArray(jsonElement.getAsJsonArray());
        } else if (jsonElement.isJsonNull()) {
            return new GsonJsonNull(jsonElement.getAsJsonNull());
        } else if (jsonElement.isJsonObject()) {
            return new GsonJsonObject(jsonElement.getAsJsonObject());
        } else if (jsonElement.isJsonPrimitive()) {
            return new GsonJsonValue(jsonElement.getAsJsonPrimitive());
        }

        throw new IllegalArgumentException("Unknown JsonElement type.");
    }
}
