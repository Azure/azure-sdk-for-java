// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.json;

import java.io.IOException;

/**
 * Indicates that the implementing class can be serialized to and deserialized from JSON.
 * <p>
 * Since deserialization needs to work without an instance of the class, implementing this interface it's assumed the
 * class has a static method {@code fromJson(JsonReader)} that deserializes an instance of that class. The contract for
 * reading JSON from {@link JsonReader} is that the initial state of the reader on call will either be a null
 * {@link JsonToken} or be the {@link JsonToken} after the {@link JsonToken#FIELD_NAME} for the object. So, for objects
 * calling out to other {@link JsonSerializable} objects for deserialization, they'll pass the reader pointing to the
 * token after the {@link JsonToken#FIELD_NAME}. This way objects reading JSON will be self-encapsulated for reading
 * properly formatted JSON. And, if an error occurs during deserialization an {@link IllegalStateException} should be
 * thrown.
 *
 * @param <T> The type of the object that is JSON serializable.
 *
 * @see com.typespec.json
 * @see JsonReader
 * @see JsonWriter
 */
public interface JsonSerializable<T extends JsonSerializable<T>> {
    /**
     * Writes the object to the passed {@link JsonWriter}.
     * <p>
     * The contract for writing JSON to {@link JsonWriter} is that the object being written will handle opening and
     * closing its own JSON object. So, for objects calling out to other {@link JsonSerializable} objects for
     * serialization, they'll write the field name only then pass the {@link JsonWriter} to the other
     * {@link JsonSerializable} object. This way objects writing JSON will be self-encapsulated for writing properly
     * formatted JSON.
     *
     * @param jsonWriter Where the object's JSON will be written.
     * @return The {@link JsonWriter} where the JSON was written.
     * @throws IOException If the object fails to be written to the {@code jsonWriter}.
     */
    JsonWriter toJson(JsonWriter jsonWriter) throws IOException;

    /**
     * Reads a JSON stream into an object.
     * <p>
     * Implementations of {@link JsonSerializable} must define this method, otherwise an
     * {@link UnsupportedOperationException} will be thrown.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @param <T> The type of the object.
     * @return The object that the JSON stream represented, may return null.
     * @throws IOException If an object fails to be read from the {@code jsonReader}.
     */
    static <T extends JsonSerializable<T>> T fromJson(JsonReader jsonReader) throws IOException {
        throw new UnsupportedOperationException("Implementation of JsonSerializable must define this factory method.");
    }
}
