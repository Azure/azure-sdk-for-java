/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.protocol;

import com.microsoft.rest.CollectionFormat;
import retrofit2.Converter;

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
     * @return the adapted original serializer
     */
    T serializer();

    /**
     * @return a converter factory for Retrofit
     */
    Converter.Factory converterFactory();

    /**
     * Serializes an object into a JSON string.
     *
     * @param object the object to serialize.
     * @return the serialized string. Null if the object to serialize is null.
     * @throws IOException exception from serialization.
     */
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
     * Deserializes a string into a {@link U} object using the current {@link T}.
     *
     * @param value the string value to deserialize.
     * @param <U> the type of the deserialized object.
     * @param type the type to deserialize.
     * @return the deserialized object.
     * @throws IOException exception in deserialization
     */
    <U> U deserialize(String value, final Type type) throws IOException;
}
