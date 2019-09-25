// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.serializer;

import com.azure.core.implementation.CollectionFormat;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * An interface defining the behaviors of a serializer.
 */
public interface SerializerAdapter {
    /**
     * Serializes an object into a string.
     *
     * @param object the object to serialize
     * @param encoding the encoding to use for serialization
     * @return the serialized string. Null if the object to serialize is null
     * @throws IOException exception from serialization
     */
    String serialize(Object object, SerializerEncoding encoding) throws IOException;

    /**
     * Serializes an object into a raw string. The leading and trailing quotes will be trimmed.
     *
     * @param object the object to serialize
     * @return the serialized string. Null if the object to serialize is null
     */
    String serializeRaw(Object object);

    /**
     * Serializes a list into a string with the delimiter specified with the
     * Swagger collection format joining each individual serialized items in
     * the list.
     *
     * @param list the list to serialize
     * @param format the Swagger collection format
     * @return the serialized string
     */
    String serializeList(List<?> list, CollectionFormat format);

    /**
     * Deserializes a string into a {@code U} object.
     *
     * @param value the string value to deserialize
     * @param <U> the type of the deserialized object
     * @param type the type to deserialize
     * @param encoding the encoding used in the serialized value
     * @return the deserialized object
     * @throws IOException exception from deserialization
     */
    <U> U deserialize(String value, Type type, SerializerEncoding encoding) throws IOException;
}
