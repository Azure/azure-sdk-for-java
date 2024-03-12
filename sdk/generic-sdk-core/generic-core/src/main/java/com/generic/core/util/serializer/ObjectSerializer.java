// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.util.serializer;

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
     */
    <T> T deserializeFromBytes(byte[] data, Type type);

    /**
     * Reads a stream into its object representation.
     *
     * @param stream {@link InputStream} of data.
     * @param type {@link Type} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized stream.
     */
    <T> T deserializeFromStream(InputStream stream, Type type);

    /**
     * Serializes an object into a byte array.
     *
     * @param value The object to serialize.
     * @return The binary representation of the serialized object.
     */
    byte[] serializeToBytes(Object value);

    /**
     * Serializes and writes an object into a provided stream.
     *
     * @param stream {@link OutputStream} where the serialized object will be written.
     * @param value The object to serialize.
     */
    void serializeToStream(OutputStream stream, Object value);
}
