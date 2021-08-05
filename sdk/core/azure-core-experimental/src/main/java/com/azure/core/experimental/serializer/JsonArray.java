// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.serializer;

import java.util.stream.Stream;

/**
 * Interface that represents a JSON array.
 */
public interface JsonArray extends JsonNode {
    @Override
    default boolean isArray() {
        return true;
    }

    /**
     * Adds a {@link JsonNode} to the end of this array.
     *
     * @param jsonNode The {@link JsonNode} to add to the array.
     * @return The updated {@link JsonArray} object.
     */
    JsonArray add(JsonNode jsonNode);

    /**
     * Clears all values in the array.
     *
     * @return The updated {@link JsonArray} object.
     */
    JsonArray clear();

    /**
     * @return {@link Stream} for all elements in the array.
     */
    Stream<JsonNode> elements();

    /**
     * Gets the {@link JsonNode} at the given index of the array.
     *
     * @param index Index of the array.
     * @return The {@link JsonNode} at the given index of the array if set, otherwise {@code null}.
     * @throws IndexOutOfBoundsException If {@code index} is less than {@code 0} or greater than {@link #size()}.
     */
    JsonNode get(int index);

    /**
     * Determines if the array contains a value for the given index.
     * <p>
     * If {@code index} is less than {@code 0} or greater than equal to {@link #size()} {@code false} is returned.
     *
     * @param index Index of the array.
     * @return Whether the array contains a value for the index.
     */
    boolean has(int index);

    /**
     * Removes the {@link JsonNode} at the specified index of the array.
     *
     * @param index Index of the array.
     * @return The {@link JsonNode} at the given index of the array.
     * @throws IndexOutOfBoundsException If {@code index} is less than {@code 0} or greater than {@link #size()}.
     */
    JsonNode remove(int index);

    /**
     * Replaces the {@link JsonNode} at the specified index with a new node.
     *
     * @param index Index of the array.
     * @param jsonNode The new {@link JsonNode} value to set.
     * @return The old {@link JsonNode} value at the given index of the array.
     * @throws IndexOutOfBoundsException If {@code index} is less than {@code 0} or greater than {@link #size()}.
     */
    JsonNode set(int index, JsonNode jsonNode);

    /**
     * @return The size of the array.
     */
    int size();
}
