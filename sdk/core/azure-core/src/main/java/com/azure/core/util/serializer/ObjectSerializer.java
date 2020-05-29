// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

/**
 * Generic interface covering serializing and deserialization objects.
 */
public interface ObjectSerializer {
    /**
     * Reads a byte array into its object representation.
     *
     * @param input Byte array.
     * @param clazz {@link Class} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized byte array.
     */
    <T> T deserialize(byte[] input, Class<T> clazz);

    /**
     * Writes the object into a byte array representation.
     *
     * @param value The object.
     * @return The byte array representing the serialized object.
     */
    byte[] serialize(Object value);
}
