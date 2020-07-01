// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.serializer;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Interface that represents a JSON object.
 */
public interface JsonObject extends JsonNode {
    @Override
    default boolean isObject() {
        return true;
    }

    /**
     * @return {@link Stream} for all fields in the object.
     */
    Stream<Map.Entry<String, JsonNode>> fields();

    /**
     * @return {@link Stream} for all field names in the object.
     */
    Stream<String> fieldNames();

    /**
     * Gets the {@link JsonNode} field with the specified name in the object.
     *
     * @param name Name of the node.
     * @return {@link JsonNode} for the specified field in the object if it exist, {@code null} otherwise.
     */
    JsonNode get(String name);

    /**
     * Returns whether the object has a value for the specified field.
     *
     * @param name Name of the node.
     * @return Whether the object has a value for the specified field.
     */
    boolean has(String name);

    /**
     * Puts a {@link JsonNode} field with the specified name into the object.
     *
     * @param name Name of the node.
     * @param jsonNode The {@link JsonNode} to put into the object.
     * @return The update {@link JsonObject} object.
     */
    JsonObject put(String name, JsonNode jsonNode);

    /**
     * Removes the {@link JsonNode} with the specified name from the object.
     *
     * @param name Name of the node.
     * @return {@link JsonNode} removed from the object if it existed, {@code null} otherwise.
     */
    JsonNode remove(String name);

    /**
     * Sets the {@link JsonNode} field with the specified name with a new node value.
     *
     * @param name Name of the node.
     * @param jsonNode The new {@link JsonNode} value.
     * @return The old {@link JsonNode} value if it was set, {@code null} otherwise.
     */
    JsonNode set(String name, JsonNode jsonNode);
}
