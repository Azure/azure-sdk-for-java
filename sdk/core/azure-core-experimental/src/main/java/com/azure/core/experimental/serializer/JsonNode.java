// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.serializer;

/**
 * Interfaces that represents a JSON node.
 */
public interface JsonNode {
    /**
     * Indicates whether this {@link JsonNode} is an instance of {@link JsonArray} ({@code []}).
     *
     * @return Whether this {@link JsonNode} is an instance of {@link JsonArray}.
     */
    default boolean isArray() {
        return false;
    }

    /**
     * Indicates whether this {@link JsonNode} is an instance of {@link JsonNull} ({@code null}).
     *
     * @return Whether this {@link JsonNode} is an instance of {@link JsonNull}.
     */
    default boolean isNull() {
        return false;
    }

    /**
     * Indicates whether this {@link JsonNode} is an instance of {@link JsonObject} ({@code {}}).
     *
     * @return Whether this {@link JsonNode} is an instance of {@link JsonObject}.
     */
    default boolean isObject() {
        return false;
    }

    /**
     * Indicates whether this {@link JsonNode} is an instance of {@link JsonPrimitive}
     * ({@code "string", 0, false, true}).
     *
     * @return Whether this {@link JsonNode} is an instance of {@link JsonPrimitive}.
     */
    default boolean isValue() {
        return false;
    }
}
