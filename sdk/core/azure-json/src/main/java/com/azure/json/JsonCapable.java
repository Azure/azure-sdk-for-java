// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import com.azure.json.implementation.AccessibleByteArrayOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Indicates that the implementing class can be serialized to and deserialized from JSON.
 * <p>
 * Since deserialization needs to work without an instance of the class, implementing this interface it's assumed the
 * class has a static method {@code fromJson(JsonReader)} that deserializes an instance of that class. The contract for
 * reading JSON from {@link JsonReader} is that the initial state of the reader on call will either be a null {@link
 * JsonToken} or be the {@link JsonToken} after the {@link JsonToken#FIELD_NAME} for the object. So, for objects calling
 * out to other {@link JsonCapable} objects for deserialization, they'll pass the reader pointing to the token after the
 * {@link JsonToken#FIELD_NAME}. This way objects reading JSON will be self-encapsulated for reading properly formatted
 * JSON. And, if an error occurs during deserialization an {@link IllegalStateException} should be thrown.
 *
 * @param <T> The type of the object that is JSON capable.
 */
public interface JsonCapable<T extends JsonCapable<T>> {
    /**
     * Writes the object to the passed {@link StringBuilder}.
     * <p>
     * The contract for writing JSON to {@link StringBuilder} is that the object being written will handle opening and
     * closing its own JSON object. So, for objects calling out to other {@link JsonCapable} objects for serialization,
     * they'll write the field name only then pass the {@link StringBuilder} to the other {@link JsonCapable} object.
     * This way objects writing JSON will be self-encapsulated for writing properly formatted JSON.
     * <p>
     * The default implementation calls into {@link #toJson(JsonWriter)} with an accessible {@link OutputStream} and
     * writes the result to the {@link StringBuilder} once JSON writing is completed. This API is meant to act as an
     * optimization for classes with simple JSON structures that can be written directly to {@link StringBuilder}
     * without handling for complex JSON scenarios.
     * <p>
     * It's recommended to use {@link #toJson(JsonWriter)} when it's unsure on which method should be used.
     *
     * @param stringBuilder Where the object's JSON will be written.
     * @return The {@link StringBuilder} where the JSON was written.
     */
    default StringBuilder toJson(StringBuilder stringBuilder) {
        // For simpler classes the method should be overridden and use more efficient serialization containers such
        // as StringBuilder.
        //
        // Default implementation will use the performant AccessibleByteArrayOutputStream.
        AccessibleByteArrayOutputStream outputStream = new AccessibleByteArrayOutputStream();
        try (JsonWriter writer = DefaultJsonWriter.toStream(outputStream)) {
            toJson(writer);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return stringBuilder.append(outputStream.toString(StandardCharsets.UTF_8));
    }

    /**
     * Writes the object to the passed {@link JsonWriter}.
     * <p>
     * The contract for writing JSON to {@link JsonWriter} is that the object being written will handle opening and
     * closing its own JSON object. So, for objects calling out to other {@link JsonCapable} objects for serialization,
     * they'll write the field name only then pass the {@link JsonWriter} to the other {@link JsonCapable} object. This
     * way objects writing JSON will be self-encapsulated for writing properly formatted JSON.
     *
     * @param jsonWriter Where the object's JSON will be written.
     * @return The {@link JsonWriter} where the JSON was written.
     */
    JsonWriter toJson(JsonWriter jsonWriter);
}
