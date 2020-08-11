// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.azure.core.http.HttpHeaders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
     * Serializes an object and writes its output into an {@link OutputStream}.
     *
     * @param object The object to serialize.
     * @param encoding The encoding to use for serialization.
     * @param outputStream The {@link OutputStream} where the serialized object will be written.
     * @throws IOException exception from serialization
     */
    default void serialize(final Object object, final SerializerEncoding encoding, OutputStream outputStream)
        throws IOException {
        String serializedObject = serialize(object, encoding);

        if (serializedObject != null) {
            outputStream.write(serializedObject.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Serializes an object into a raw string. The leading and trailing quotes will be trimmed.
     *
     * @param object the object to serialize
     * @return the serialized string. Null if the object to serialize is null
     */
    String serializeRaw(Object object);

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
     * Deserializes a byte[] into a {@code U} object.
     *
     * @param inputStream The {@link InputStream} containing the serialized object data to deserialize.
     * @param type The type to deserialize.
     * @param encoding The encoding used to serialize value.
     * @param <U> The type of the deserialized object.
     * @return The deserialized object, or null if it cannot be deserialized.
     * @throws IOException exception from deserialization
     */
    default <U> U deserialize(final InputStream inputStream, final Type type, final SerializerEncoding encoding)
        throws IOException {
        if (inputStream == null) {
            return deserialize((String) null, type, encoding);
        }

        /*
         * Default implementation reads the entire InputStream into a ByteArrayOutputStream. This is done to enable
         * converting to a String and calling into the implemented API.
         */
        ByteArrayOutputStream converterStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            converterStream.write(buffer, 0, length);
        }

        /*
         * Using ByteArrayOutputStream.toString is better as it won't duplicate the underlying buffer as toByteArray
         * would but it doesn't have support for passing a Charset until Java 10.
         */
        return deserialize(converterStream.toString("UTF-8"), type, encoding);
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
