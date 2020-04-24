// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import java.io.OutputStream;

/**
 * Generic interface covering basic JSON serialization and deserialization methods.
 */
public interface JsonSerializer {
    /**
     * Reads the JSON byte stream into its object representation.
     *
     * @param input JSON byte stream.
     * @param clazz {@link Class} representing the object.
     * @param <T> Type of the object.
     * @return The object representing the JSON string.
     */
    <T> T deserialize(byte[] input, Class<T> clazz);

    /**
     * Writes the object into its JSON byte stream.
     *
     * @param value The object.
     * @return The JSON byte stream representing the object.
     */
    byte[] serialize(Object value);

    /**
     * Converts the object into a JSON byte stream and writes it to the {@link OutputStream}.
     *
     * @param value The object.
     * @param stream The {@link OutputStream} where the JSON byte stream will be written.
     */
    void serialize(Object value, OutputStream stream);
}
