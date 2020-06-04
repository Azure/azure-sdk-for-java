// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.azure.core.http.HttpHeaders;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
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
     * Serializes an object into a byte array.
     * <p>
     * Default implementation calls {@link #serialize(Object, SerializerEncoding)} and converts the returned String into
     * a {@link StandardCharsets#UTF_8 UTF-8} byte array.
     *
     * @param object The object to serialize.
     * @param encoding The encoding used for serialization.
     * @return The object serialized into a byte array. If {@code object} is {@code null} then {@code null}.
     * @throws IOException If an exception occurs during serialization.
     */
    default byte[] serializeToByteArray(Object object, SerializerEncoding encoding) throws IOException {
        return serialize(object, encoding).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Serializes an object into a raw string. The leading and trailing quotes will be trimmed.
     *
     * @param object the object to serialize
     * @return the serialized string. Null if the object to serialize is null
     */
    String serializeRaw(Object object);

    /**
     * Serializes an object into a byte array.
     * <p>
     * Default implementation calls {@link #serializeRaw(Object)} and converts the returned String into a {@link
     * StandardCharsets#UTF_8 UTF-8} byte array.
     *
     * @param object The object to serialize.
     * @return The object serialized into a byte array. If {@code object} is {@code null} then {@code null}.
     */
    default byte[] serializeRawToByteArray(Object object) {
        return serializeRaw(object).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Serializes a list into a string with the delimiter specified with the Swagger collection format joining each
     * individual serialized items in the list.
     *
     * @param list the list to serialize
     * @param format the Swagger collection format
     * @return the serialized string
     */
    String serializeList(List<?> list, CollectionFormat format);

    /**
     * Serializes a list into a byte array with the delimiter specified with Swagger collection format joining each
     * individual serialized item in the list.
     * <p>
     * Default implementation calls {@link #serializeRaw(Object)} and converts the returned String into a {@link
     * StandardCharsets#UTF_8 UTF-8} byte array.
     *
     * @param list The list to serialize.
     * @param format The Swagger collection format.
     * @return The list serialized into a byte array.
     */
    default byte[] serializeListToByteArray(List<?> list, CollectionFormat format) {
        return serializeList(list, format).getBytes(StandardCharsets.UTF_8);
    }

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

    /**
     * Deserializes a byte array into a {@code U} object.
     * <p>
     * Default implementation converts the {@code value} into a {@link StandardCharsets#UTF_8 UTF-8} string and passes
     * it into {@link #deserialize(String, Type, SerializerEncoding)}.
     *
     * @param value The byte array to deserialize.
     * @param type The type to deserialize.
     * @param encoding The {@link SerializerEncoding} used in the serialized value.
     * @param <U> The type of the deserialized object.
     * @return The deserialized object.
     * @throws IOException If an exception occurs during deserialization.
     */
    default <U> U deserialize(byte[] value, Type type, SerializerEncoding encoding) throws IOException {
        return deserialize(new String(value, StandardCharsets.UTF_8), type, encoding);
    }

    /**
     * Deserialize the provided headers returned from a REST API to an entity instance declared as the model to hold
     * 'Matching' headers.
     *
     * 'Matching' headers are the REST API returned headers those with:
     *
     * <ol>
     *   <li>header names same as name of a properties in the entity.</li>
     *   <li>header names start with value of {@link com.azure.core.annotation.HeaderCollection} annotation applied to
     *   the properties in the entity.</li>
     * </ol>
     *
     * When needed, the 'header entity' types must be declared as first generic argument of
     * {@link com.azure.core.http.rest.ResponseBase} returned by java proxy method corresponding to the REST API.
     * e.g.
     * {@code Mono<RestResponseBase<FooMetadataHeaders, Void>> getMetadata(args);}
     * {@code
     *      class FooMetadataHeaders {
     *          String name;
     *          {@literal @}HeaderCollection("header-collection-prefix-")
     *          Map<String,String> headerCollection;
     *      }
     * }
     *
     * in the case of above example, this method produces an instance of FooMetadataHeaders from provided
     * {@code headers}.
     *
     * @param headers the REST API returned headers
     * @param <U> the type of the deserialized object
     * @param type the type to deserialize
     * @return instance of header entity type created based on provided {@code headers}, if header entity model does not
     * not exists then return null
     * @throws IOException If an I/O error occurs
     */
    <U> U deserialize(HttpHeaders headers, Type type) throws IOException;
}
