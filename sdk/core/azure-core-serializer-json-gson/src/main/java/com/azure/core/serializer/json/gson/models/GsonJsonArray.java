// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.serializer.json.gson.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

import java.io.IOException;
import java.util.Objects;

/**
 * Implementation of {@link com.azure.json.models.JsonElement} which is backed by GSON's {@link JsonArray}.
 * <p>
 * This allows for using GSON's {@link JsonArray} in places where {@link com.azure.json.models.JsonArray} is
 * required, meaning the GSON {@link JsonArray} doesn't need to be converted to azure-json's {@link JsonArray}.
 */
public final class GsonJsonArray extends com.azure.json.models.JsonElement {
    private final JsonArray array;

    /**
     * Creates a new {@link GsonJsonArray} using the default {@link JsonArray#JsonArray()}.
     */
    public GsonJsonArray() {
        this(new JsonArray());
    }

    /**
     * Creates a new {@link GsonJsonArray} using the provided {@link JsonArray}.
     *
     * @param array The {@link com.azure.json.models.JsonArray} to use as the backing array.
     * @throws NullPointerException If {@code array} is null.
     */
    public GsonJsonArray(JsonArray array) {
        this.array = Objects.requireNonNull(array, "'array' cannot be null.");
    }

    @Override
    public boolean isArray() {
        return true;
    }

    /**
     * Adds a JsonElement to the JSON array. This element will be appended to the end of the array.
     *
     * @param element The JsonElement to add to the array.
     * @return The updated GsonJsonArray object.
     * @throws NullPointerException If the {@code element} is null.
     */
    public GsonJsonArray addElement(JsonElement element) {
        array.add(nullCheck(element));
        return this;
    }

    /**
     * Adds a JsonElement to the JSON array at the specified index. This element will be inserted at the specified index
     * and all elements at or after the index will be shifted.
     *
     * @param element The JsonElement to add to the array.
     * @param index The index at which to add the element.
     * @return The updated GsonJsonArray object.
     * @throws NullPointerException If the {@code element} is null.
     * @throws IndexOutOfBoundsException If the {@code index} is less than zero or greater than or equal to
     * {@link #size()}.
     */
    public GsonJsonArray addElement(int index, JsonElement element) {
        array.asList().add(index, nullCheck(element));
        return this;
    }

    /**
     * Sets a specified JsonElement object at a specified index within the GsonJsonArray. This will replace the current
     * JsonElement at the specified index with the newly specified JsonElement object.
     *
     * @param element The JsonElement to set at the specified index.
     * @param index The index at which to set the element.
     * @return The updated GsonJsonArray object.
     * @throws NullPointerException If the {@code element} is null.
     * @throws IndexOutOfBoundsException If the {@code index} is less than zero or greater than or equal to
     * {@link #size()}.
     */
    public GsonJsonArray setElement(int index, JsonElement element) {
        array.set(index, nullCheck(element));
        return this;
    }

    /**
     * Gets the JsonElement at the specified index from the GsonJsonArray.
     *
     * @param index The index at which to get the element.
     * @return The JsonElement at the specified index.
     * @throws IndexOutOfBoundsException If the {@code index} is less than zero or greater than or equal to
     * {@link #size()}.
     */
    public JsonElement getElement(int index) throws IndexOutOfBoundsException {
        return array.get(index);
    }

    /**
     * Removes the JsonElement at the specified index from the GsonJsonArray. This will shift all elements after the
     * specified index.
     *
     * @param index The index at which to remove the element.
     * @return The removed JsonElement.
     * @throws IndexOutOfBoundsException If the {@code index} is less than zero or greater than or equal to
     * {@link #size()}.
     */
    public JsonElement removeElement(int index) throws IndexOutOfBoundsException {
        return array.remove(index);
    }

    /**
     * The number of elements in the GsonJsonArray.
     *
     * @return The number of elements in the GsonJsonArray.
     */
    public int size() {
        return array.size();
    }

    /**
     * Checks that the JsonElement isn't null.
     * <p>
     * In structured JSON nullness must be represented by {@link JsonNull} and not {@code null}.
     *
     * @throws NullPointerException If the {@code element} is null.
     */
    private static JsonElement nullCheck(JsonElement element) {
        return Objects.requireNonNull(element,
            "The JsonElement cannot be null. If null must be represented in JSON, use JsonNull.");
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return GsonJsonElementUtils.writeJsonArray(jsonWriter, array);
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
    public static GsonJsonArray fromJson(JsonReader jsonReader) throws IOException {
        JsonToken token = jsonReader.currentToken();
        if (token == null) {
            token = jsonReader.nextToken();
        }

        if (token != JsonToken.START_ARRAY) {
            throw new IllegalStateException(
                "JsonReader is pointing to an invalid token for deserialization. Token was: " + token + ".");
        }

        return new GsonJsonArray(GsonJsonElementUtils.readJsonArray(jsonReader));
    }
}
