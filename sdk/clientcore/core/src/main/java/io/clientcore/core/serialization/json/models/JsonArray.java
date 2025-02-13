// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.serialization.json.models;

import io.clientcore.core.serialization.json.JsonProviders;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonToken;
import io.clientcore.core.serialization.json.JsonWriter;
import io.clientcore.core.serialization.json.implementation.StringBuilderWriter;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Model representing a JSON array.
 */
public final class JsonArray extends JsonElement {
    private final List<JsonElement> elements;

    /**
     * Default constructor.
     */
    public JsonArray() {
        this.elements = new LinkedList<>();
    }

    private JsonArray(List<JsonElement> elements) {
        this.elements = elements;
    }

    /**
     * Adds a JsonElement to the JSON array. This element will be appended to the end of the array.
     *
     * @param element The JsonElement to add to the array.
     * @return The updated JsonArray object.
     * @throws NullPointerException If the {@code element} is null.
     */
    public JsonArray addElement(JsonElement element) {
        elements.add(nullCheck(element));
        return this;
    }

    /**
     * Adds a boolean element to the JSON array. This element will be appended to the end of the array.
     *
     * @param element The boolean element to add to the array.
     * @return The updated JsonArray object.
     */
    public JsonArray addElement(boolean element) {
        elements.add(JsonBoolean.getInstance(element));
        return this;
    }

    /**
     * Adds a number element to the JSON array. This element will be appended to the end of the array.
     * <p>
     * If the {@code element} is null this will be treated as a JSON null.
     *
     * @param element The number element to add to the array.
     * @return The updated JsonArray object.
     */
    public JsonArray addElement(Number element) {
        elements.add(element == null ? JsonNull.getInstance() : new JsonNumber(element));
        return this;
    }

    /**
     * Adds a string element to the JSON array. This element will be appended to the end of the array.
     * <p>
     * If the {@code element} is null this will be treated as a JSON null.
     *
     * @param element The string element to add to the array.
     * @return The updated JsonArray object.
     */
    public JsonArray addElement(String element) {
        elements.add(element == null ? JsonNull.getInstance() : new JsonString(element));
        return this;
    }

    /**
     * Adds a JsonElement to the JSON array at the specified index. This element will be inserted at the specified index
     * and all elements at or after the index will be shifted.
     *
     * @param element The JsonElement to add to the array.
     * @param index The index at which to add the element.
     * @return The updated JsonArray object.
     * @throws NullPointerException If the {@code element} is null.
     * @throws IndexOutOfBoundsException If the {@code index} is less than zero or greater than or equal to
     * {@link #size()}.
     */
    public JsonArray addElement(int index, JsonElement element) {
        elements.add(index, nullCheck(element));
        return this;
    }

    /**
     * Adds a boolean element to the JSON array at the specified index. This element will be inserted at the specified index
     * and all elements at or after the index will be shifted.
     *
     * @param element The boolean element to add to the array.
     * @param index The index at which to add the element.
     * @return The updated JsonArray object.
     * @throws IndexOutOfBoundsException If the {@code index} is less than zero or greater than or equal to
     * {@link #size()}.
     */
    public JsonArray addElement(int index, boolean element) {
        elements.add(index, JsonBoolean.getInstance(element));
        return this;
    }

    /**
     * Adds a number element to the JSON array at the specified index. This element will be inserted at the specified index
     * and all elements at or after the index will be shifted.
     * <p>
     * If the {@code element} is null this will be treated as a JSON null.
     *
     * @param element The number element to add to the array.
     * @param index The index at which to add the element.
     * @return The updated JsonArray object.
     * @throws IndexOutOfBoundsException If the {@code index} is less than zero or greater than or equal to
     * {@link #size()}.
     */
    public JsonArray addElement(int index, Number element) {
        elements.add(index, element == null ? JsonNull.getInstance() : new JsonNumber(element));
        return this;
    }

    /**
     * Adds a string element to the JSON array at the specified index. This element will be inserted at the specified index
     * and all elements at or after the index will be shifted.
     * <p>
     * If the {@code element} is null this will be treated as a JSON null.
     *
     * @param element The string element to add to the array.
     * @param index The index at which to add the element.
     * @return The updated JsonArray object.
     * @throws IndexOutOfBoundsException If the {@code index} is less than zero or greater than or equal to
     * {@link #size()}.
     */
    public JsonArray addElement(int index, String element) {
        elements.add(index, element == null ? JsonNull.getInstance() : new JsonString(element));
        return this;
    }

    /**
     * Sets a specified JsonElement object at a specified index within the JsonArray. This will replace the current
     * JsonElement at the specified index with the newly specified JsonElement object.
     *
     * @param element The JsonElement to set at the specified index.
     * @param index The index at which to set the element.
     * @return The updated JsonArray object.
     * @throws NullPointerException If the {@code element} is null.
     * @throws IndexOutOfBoundsException If the {@code index} is less than zero or greater than or equal to
     * {@link #size()}.
     */
    public JsonArray setElement(int index, JsonElement element) {
        elements.set(index, nullCheck(element));
        return this;
    }

    /**
     * Sets a specified boolean element at a specified index within the JsonArray. This will replace the current
     * JsonElement at the specified index with the newly specified boolean element.
     *
     * @param element The boolean element to set at the specified index.
     * @param index The index at which to set the element.
     * @return The updated JsonArray object.
     * @throws IndexOutOfBoundsException If the {@code index} is less than zero or greater than or equal to
     * {@link #size()}.
     */
    public JsonArray setElement(int index, boolean element) {
        elements.set(index, JsonBoolean.getInstance(element));
        return this;
    }

    /**
     * Sets a specified number element at a specified index within the JsonArray. This will replace the current
     * JsonElement at the specified index with the newly specified number element.
     * <p>
     * If the {@code element} is null this will be treated as a JSON null.
     *
     * @param element The number element to set at the specified index.
     * @param index The index at which to set the element.
     * @return The updated JsonArray object.
     * @throws IndexOutOfBoundsException If the {@code index} is less than zero or greater than or equal to
     * {@link #size()}.
     */
    public JsonArray setElement(int index, Number element) {
        elements.set(index, element == null ? JsonNull.getInstance() : new JsonNumber(element));
        return this;
    }

    /**
     * Sets a specified string element at a specified index within the JsonArray. This will replace the current
     * JsonElement at the specified index with the newly specified string element.
     * <p>
     * If the {@code element} is null this will be treated as a JSON null.
     *
     * @param element The string element to set at the specified index.
     * @param index The index at which to set the element.
     * @return The updated JsonArray object.
     * @throws IndexOutOfBoundsException If the {@code index} is less than zero or greater than or equal to
     * {@link #size()}.
     */
    public JsonArray setElement(int index, String element) {
        elements.set(index, element == null ? JsonNull.getInstance() : new JsonString(element));
        return this;
    }

    /**
     * Gets the JsonElement at the specified index from the JsonArray.
     *
     * @param index The index at which to get the element.
     * @return The JsonElement at the specified index.
     * @throws IndexOutOfBoundsException If the {@code index} is less than zero or greater than or equal to
     * {@link #size()}.
     */
    public JsonElement getElement(int index) throws IndexOutOfBoundsException {
        return this.elements.get(index);
    }

    /**
     * Removes the JsonElement at the specified index from the JsonArray. This will shift all elements after the
     * specified index.
     *
     * @param index The index at which to remove the element.
     * @return The removed JsonElement.
     * @throws IndexOutOfBoundsException If the {@code index} is less than zero or greater than or equal to
     * {@link #size()}.
     */
    public JsonElement removeElement(int index) throws IndexOutOfBoundsException {
        return elements.remove(index);
    }

    /**
     * The number of elements in the JsonArray.
     *
     * @return The number of elements in the JsonArray.
     */
    public int size() {
        return elements.size();
    }

    /**
     * @return boolean of whether this JsonElement object is of type JsonArray.
     */
    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeArray(elements, JsonWriter::writeJson);
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
    public static JsonArray fromJson(JsonReader jsonReader) throws IOException {
        JsonToken token = jsonReader.currentToken();
        if (token == null) {
            token = jsonReader.nextToken();
        }

        if (token != JsonToken.START_ARRAY) {
            throw new IllegalStateException(
                "JsonReader is pointing to an invalid token for deserialization. Token was: " + token + ".");
        }

        return new JsonArray(jsonReader.readArray(JsonElement::fromJson));
    }

    @Override
    public String toJsonString() throws IOException {
        // TODO (alzimmer): This could be cached and reset each time the array is mutated.
        StringBuilderWriter writer = new StringBuilderWriter();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(writer)) {
            toJson(jsonWriter).flush();
            return writer.toString();
        }
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
}
