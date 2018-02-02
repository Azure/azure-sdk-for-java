/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.protocol;

import com.microsoft.rest.v2.CollectionFormat;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * This interface defines the behaviors an adapter of a serializer
 * needs to implement.
 *
 * @param <T> the original serializer
 */
public interface SerializerAdapter<T> {
    /**
     * @deprecated original serializer type should not be exposed
     * @return the adapted original serializer
     */
    @Deprecated
    T serializer();

    /**
     * Serializes an object into a string.
     *
     * @param object the object to serialize.
     * @param encoding the encoding to use for serialization.
     * @return the serialized string. Null if the object to serialize is null.
     * @throws IOException exception from serialization.
     */
    String serialize(Object object, SerializerEncoding encoding) throws IOException;

    /**
     * @deprecated Use serialize(Object, Encoding) instead.
     *
     * Serializes an object into a JSON string.
     *
     * @param object the object to serialize.
     * @return the serialized string. Null if the object to serialize is null.
     * @throws IOException exception from serialization.
     */
    @Deprecated
    String serialize(Object object) throws IOException;

    /**
     * Serializes an object into a raw string. The leading and trailing quotes will be trimmed.
     *
     * @param object the object to serialize.
     * @return the serialized string. Null if the object to serialize is null.
     */
    String serializeRaw(Object object);

    /**
     * Serializes a list into a string with the delimiter specified with the
     * Swagger collection format joining each individual serialized items in
     * the list.
     *
     * @param list the list to serialize.
     * @param format the Swagger collection format.
     * @return the serialized string
     */
    String serializeList(List<?> list, CollectionFormat format);

    /**
     * @deprecated Use deserialize(String, Type, Encoding) instead.
     *
     * Deserializes a JSON string into a {@link U} object using the current {@link T}.
     *
     * @param value the string value to deserialize.
     * @param <U> the type of the deserialized object.
     * @param type the type to deserialize.
     * @return the deserialized object.
     * @throws IOException exception in deserialization
     */
    @Deprecated
    <U> U deserialize(String value, Type type) throws IOException;

    /**
     * Deserializes a string into a {@link U} object using the current {@link T}.
     *
     * @param value the string value to deserialize.
     * @param <U> the type of the deserialized object.
     * @param type the type to deserialize.
     * @param encoding the encoding used in the serialized value.
     * @return the deserialized object.
     * @throws IOException exception in deserialization
     */
    <U> U deserialize(String value, Type type, SerializerEncoding encoding) throws IOException;

    /**
     * Get the TypeFactory for this SerializerAdapter.
     * @return The TypeFactory for this SerializerAdapter.
     */
    TypeFactory getTypeFactory();
}
