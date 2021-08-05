// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.serializer;

/**
 * Interfaces that represents a JSON node.
 */
public interface JsonNode {
    /**
     * @return True if this {@link JsonNode} is an instance of {@link JsonArray}.
     */
    default boolean isArray() {
        return false;
    }

    /**
     * @return True if this {@link JsonNode} is an instance of {@link JsonNull}.
     */
    default boolean isNull() {
        return false;
    }

    /**
     * @return True if this {@link JsonNode} is an instance of {@link JsonObject}.
     */
    default boolean isObject() {
        return false;
    }

    /**
     * @return True if this {@link JsonNode} is an instance of {@link JsonPrimitive}.
     */
    default boolean isValue() {
        return false;
    }
}
