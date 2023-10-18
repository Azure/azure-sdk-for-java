// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.util.serializer;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Generic interface covering basic JSON serialization and deserialization methods.
 */
public interface JsonSerializer {
    /**
     * Reads a JSON byte array into its object representation.
     *
     * @param data JSON byte array.
     * @param typeReference {@link TypeReference} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized JSON byte array.
     */
    default <T> T deserializeFromBytes(byte[] data, TypeReference<T> typeReference) {
        return deserializeFromBytes(data, typeReference);
    }

    /**
     * Reads a JSON stream into its object representation.
     *
     * @param stream JSON stream.
     * @param typeReference {@link TypeReference} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized JSON stream.
     */
    <T> T deserialize(InputStream stream, TypeReference<T> typeReference);

    /**
     * Converts the object into a JSON byte array.
     *
     * @param value The object.
     * @return The JSON binary representation of the serialized object.
     */
    default byte[] serializeToBytes(Object value) {
        return serializeToBytes(value);
    }

    /**
     * Writes an object's JSON representation into a stream.
     *
     * @param stream {@link OutputStream} where the object's JSON representation will be written.
     * @param value The object.
     */
    void serialize(OutputStream stream, Object value);
}
