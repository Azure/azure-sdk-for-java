// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

/**
 * Generic interface covering serializing and deserialization objects.
 */
public interface ObjectSerializer {
    /**
     * Reads a byte array into its object representation.
     *
     * @param data The byte array.
     * @param type {@link Type} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized byte array.
     * @throws IOException If the deserialization fails.
     */
    <T> T deserializeFromBytes(byte[] data, Type type) throws IOException;

    /**
     * Reads a stream into its object representation.
     *
     * @param stream {@link InputStream} of data.
     * @param type {@link Type} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized stream.
     * @throws IOException If the deserialization fails.
     */
    <T> T deserializeFromStream(InputStream stream, Type type) throws IOException;

    /**
     * Serializes an object into a byte array.
     *
     * @param value The object to serialize.
     * @return The binary representation of the serialized object.
     * @throws IOException If the serialization fails.
     */
    byte[] serializeToBytes(Object value) throws IOException;

    /**
     * Serializes and writes an object into a provided stream.
     *
     * @param stream {@link OutputStream} where the serialized object will be written.
     * @param value The object to serialize.
     * @throws IOException If the serialization fails.
     */
    void serializeToStream(OutputStream stream, Object value) throws IOException;
}
