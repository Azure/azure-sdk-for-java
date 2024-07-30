// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import com.azure.json.implementation.StringBuilderWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
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
 * @see com.azure.json
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
        try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
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
        try (JsonWriter jsonWriter = JsonProviders.createWriter(writer)) {
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
        try (JsonWriter jsonWriter = JsonProviders.createWriter(writer)) {
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
        try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
            toJson(jsonWriter).flush();
            return outputStream.toByteArray();
        }
    }

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

    /**
     * Convenience method for reading a JSON string into an object.
     *
     * @param string The JSON string to read.
     * @param <T> The type of the object.
     * @return The object that the JSON string represented, may return null.
     * @throws IOException If an object fails to be read from the {@code string}.
     */
    static <T extends JsonSerializable<T>> T fromJson(String string) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(string)) {
            return fromJson(jsonReader);
        }
    }

    /**
     * Convenience method for reading a JSON byte array into an object.
     *
     * @param bytes The JSON byte array to read.
     * @param <T> The type of the object.
     * @return The object that the JSON byte array represented, may return null.
     * @throws IOException If an object fails to be read from the {@code bytes}.
     */
    static <T extends JsonSerializable<T>> T fromJson(byte[] bytes) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(bytes)) {
            return fromJson(jsonReader);
        }
    }

    /**
     * Convenience method for reading a JSON {@link InputStream} into an object.
     *
     * @param inputStream The JSON {@link InputStream} to read.
     * @param <T> The type of the object.
     * @return The object that the JSON {@link InputStream} represented, may return null.
     * @throws IOException If an object fails to be read from the {@code inputStream}.
     */
    static <T extends JsonSerializable<T>> T fromJson(InputStream inputStream) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(inputStream)) {
            return fromJson(jsonReader);
        }
    }

    /**
     * Convenience method for reading a JSON {@link Reader} into an object.
     *
     * @param reader The JSON {@link Reader} to read.
     * @param <T> The type of the object.
     * @return The object that the JSON {@link Reader} represented, may return null.
     * @throws IOException If an object fails to be read from the {@code reader}.
     */
    static <T extends JsonSerializable<T>> T fromJson(Reader reader) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(reader)) {
            return fromJson(jsonReader);
        }
    }
}
