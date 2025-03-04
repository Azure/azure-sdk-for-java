// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.serialization.json;

import io.clientcore.core.serialization.json.implementation.StringBuilderWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

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
 * @see io.clientcore.core.serialization.json
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
     * Convenience method for writing the {@link JsonSerializable} to the passed {@link OutputStream}.
     *
     * @param outputStream The {@link OutputStream} to write the JSON to.
     * @throws IOException If the object fails to be written to the {@code outputStream}.
     */
    default void toJson(OutputStream outputStream) throws IOException {
        try (JsonWriter jsonWriter = JsonWriter.toStream(outputStream)) {
            toJson(jsonWriter).flush();
        }
    }

    /**
     * Convenience method for writing the {@link JsonSerializable} to the passed {@link Writer}.
     *
     * @param writer The {@link Writer} to write the JSON to.
     * @throws IOException If the object fails to be written to the {@code writer}.
     */
    default void toJson(Writer writer) throws IOException {
        try (JsonWriter jsonWriter = JsonWriter.toWriter(writer)) {
            toJson(jsonWriter).flush();
        }
    }

    /**
     * Convenience method for writing the {@link JsonSerializable} to a JSON string.
     *
     * @return The JSON string representing the object.
     * @throws IOException If the object fails to be written as a JSON string.
     */
    default String toJsonString() throws IOException {
        StringBuilderWriter writer = new StringBuilderWriter();
        try (JsonWriter jsonWriter = JsonWriter.toWriter(writer)) {
            toJson(jsonWriter).flush();
            return writer.toString();
        }
    }

    /**
     * Convenience method for writing the {@link JsonSerializable} to a byte array.
     *
     * @return The byte array representing the object.
     * @throws IOException If the object fails to be written as a byte array.
     */
    default byte[] toJsonBytes() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter jsonWriter = JsonWriter.toStream(outputStream)) {
            toJson(jsonWriter).flush();
            return outputStream.toByteArray();
        }
    }
}
