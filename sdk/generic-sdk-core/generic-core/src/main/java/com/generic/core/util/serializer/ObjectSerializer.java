// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.util.serializer;

import com.generic.core.models.TypeReference;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Generic interface covering serializing and deserialization objects.
 */
public interface ObjectSerializer {
    /**
     * Reads a byte array into its object representation.
     *
     * @param data The byte array.
     * @param typeReference {@link TypeReference} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized byte array.
     * @throws IOException If the byte array cannot be deserialized.
     */
    <T> T deserializeFromBytes(byte[] data, TypeReference<T> typeReference) throws IOException;

    /**
     * Reads a stream into its object representation.
     *
     * @param stream {@link InputStream} of data.
     * @param typeReference {@link TypeReference} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized stream.
     * @throws IOException If the stream cannot be deserialized.
     */
    <T> T deserializeFromStream(InputStream stream, TypeReference<T> typeReference) throws IOException;

    /**
     * Serializes an object into a byte array.
     *
     * @param value The object to serialize.
     * @return The binary representation of the serialized object.
     * @throws IOException If the object cannot be serialized.
     */
    byte[] serializeToBytes(Object value) throws IOException;

    /**
     * Serializes and writes an object into a provided stream.
     *
     * @param stream {@link OutputStream} where the serialized object will be written.
     * @param value The object to serialize.
     * @throws IOException If the object cannot be serialized.
     */
    void serializeToStream(OutputStream stream, Object value) throws IOException;
}
