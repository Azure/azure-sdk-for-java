// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.serializer.json.jackson.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.json.models.JsonArray;
import com.azure.json.models.JsonElement;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;

import java.io.IOException;
import java.util.Objects;

/**
 * Implementation of {@link JsonElement} which is backed by Jackson's {@link ArrayNode}.
 * <p>
 * This allows for using Jackson's {@link ArrayNode} in places where {@link JsonArray} is required, meaning the Jackson
 * {@link ArrayNode} doesn't need to be converted to azure-json's {@link JsonArray}.
 */
public final class JacksonJsonArray extends JsonElement {
    private final ArrayNode array;

    /**
     * Creates a new {@link JacksonJsonArray} using the default {@link JsonNodeFactory#instance} {@link ArrayNode}.
     */
    public JacksonJsonArray() {
        this(JsonNodeFactory.instance.arrayNode());
    }

    /**
     * Creates a new {@link JacksonJsonArray} using the provided {@link ArrayNode}.
     *
     * @param array The {@link ArrayNode} to use as the backing array.
     * @throws NullPointerException If {@code array} is null.
     */
    public JacksonJsonArray(ArrayNode array) {
        this.array = Objects.requireNonNull(array, "'array' cannot be null.");
    }

    @Override
    public boolean isArray() {
        return true;
    }

    /**
     * Adds a JsonNode to the JSON array. This node will be appended to the end of the array.
     *
     * @param node The JsonNode to add to the array.
     * @return The updated JacksonJsonArray object.
     * @throws NullPointerException If the {@code node} is null.
     */
    public JacksonJsonArray addElement(JsonNode node) {
        array.add(nullCheck(node));
        return this;
    }

    /**
     * Adds a JsonNode to the JSON array at the specified index. This node will be inserted at the specified index
     * and all elements at or after the index will be shifted.
     *
     * @param node The JsonNode to add to the array.
     * @param index The index at which to add the node.
     * @return The updated JacksonJsonArray object.
     * @throws NullPointerException If the {@code node} is null.
     * @throws IndexOutOfBoundsException If the {@code index} is less than zero or greater than or equal to
     * {@link #size()}.
     */
    public JacksonJsonArray addElement(int index, JsonNode node) {
        array.insert(index, nullCheck(node));
        return this;
    }

    /**
     * Sets a specified JsonNode object at a specified index within the JacksonJsonArray. This will replace the current
     * JsonNode at the specified index with the newly specified JsonNode object.
     *
     * @param node The JsonNode to set at the specified index.
     * @param index The index at which to set the node.
     * @return The updated JacksonJsonArray object.
     * @throws NullPointerException If the {@code node} is null.
     * @throws IndexOutOfBoundsException If the {@code index} is less than zero or greater than or equal to
     * {@link #size()}.
     */
    public JacksonJsonArray setElement(int index, JsonNode node) {
        array.set(index, nullCheck(node));
        return this;
    }

    /**
     * Gets the JsonNode at the specified index from the JacksonJsonArray.
     *
     * @param index The index at which to get the element.
     * @return The JsonNode at the specified index.
     * @throws IndexOutOfBoundsException If the {@code index} is less than zero or greater than or equal to
     * {@link #size()}.
     */
    public JsonNode getElement(int index) throws IndexOutOfBoundsException {
        return array.get(index);
    }

    /**
     * Removes the JsonNode at the specified index from the JacksonJsonArray. This will shift all elements after the
     * specified index.
     *
     * @param index The index at which to remove the element.
     * @return The removed JsonNode.
     * @throws IndexOutOfBoundsException If the {@code index} is less than zero or greater than or equal to
     * {@link #size()}.
     */
    public JsonNode removeElement(int index) throws IndexOutOfBoundsException {
        return array.remove(index);
    }

    /**
     * The number of elements in the JacksonJsonArray.
     *
     * @return The number of elements in the JacksonJsonArray.
     */
    public int size() {
        return array.size();
    }

    /**
     * Checks that the JsonNode isn't null.
     * <p>
     * In structured JSON nullness must be represented by {@link NullNode} and not {@code null}.
     *
     * @throws NullPointerException If the {@code element} is null.
     */
    private static JsonNode nullCheck(JsonNode node) {
        return Objects.requireNonNull(node,
            "The JsonNode cannot be null. If null must be represented in JSON, use NullNode.");
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return JacksonJsonElementUtils.writeArrayNode(jsonWriter, array);
    }

    /**
     * Deserializes a JSON array from a JsonReader.
     * <p>
     * If the JsonReader's current token is null, it is assumed the JsonReader hasn't begun reading and
     * {@link JsonReader#nextToken()} will be called to begin reading.
     * <p>
     * After ensuring the JsonReader has begun reading, if the current token is not {@link JsonToken#START_ARRAY}, an
     * {@link IllegalStateException} will be thrown. Otherwise, a JSON array representing the array will be created and
     * returned.
     *
     * @param jsonReader The JsonReader to deserialize from.
     * @return The deserialized JSON array.
     * @throws IOException If an error occurs while deserializing the JSON array.
     * @throws IllegalStateException If the current token is not {@link JsonToken#START_ARRAY}.
     */
    public static JacksonJsonArray fromJson(JsonReader jsonReader) throws IOException {
        JsonToken token = jsonReader.currentToken();
        if (token == null) {
            token = jsonReader.nextToken();
        }

        if (token != JsonToken.START_ARRAY) {
            throw new IllegalStateException(
                "JsonReader is pointing to an invalid token for deserialization. Token was: " + token + ".");
        }

        return new JacksonJsonArray(JacksonJsonElementUtils.readArrayNode(jsonReader));
    }
}
