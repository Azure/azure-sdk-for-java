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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * An interface defining the behaviors of a serializer.
 */
public interface SerializerAdapter {

    /**
     * Serializes an object into a string.
     *
     * @param object The object to serialize.
     * @param encoding The serialization encoding.
     * @return The object serialized as a string using the specified encoding. If the object is null null is returned.
     * @throws IOException If an IO exception was thrown during serialization.
     */
    String serialize(Object object, SerializerEncoding encoding) throws IOException;

    /**
     * Serializes an object into a byte array.
     *
     * @param object The object to serialize.
     * @param encoding The serialization encoding.
     * @return The object serialized as a byte array.
     * @throws IOException If an IO exception was thrown during serialization.
     */
    default byte[] serializeToBytes(Object object, SerializerEncoding encoding) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        serialize(object, encoding, stream);

        return stream.toByteArray();
    }

    /**
     * Serializes an object and writes its output into an {@link OutputStream}.
     *
     * @param object The object to serialize.
     * @param encoding The serialization encoding.
     * @param outputStream The {@link OutputStream} where the serialized object will be written.
     * @throws IOException If an IO exception was thrown during serialization.
     */
    default void serialize(final Object object, final SerializerEncoding encoding, OutputStream outputStream)
        throws IOException {
        String serializedObject = serialize(object, encoding);

        if (serializedObject != null) {
            outputStream.write(serializedObject.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Serializes an object into a raw string, leading and trailing quotes will be trimmed.
     *
     * @param object The object to serialize.
     * @return The object serialized as a string. If the object is null null is returned.
     */
    String serializeRaw(Object object);

    /**
     * Serializes a list into a string with the delimiter specified with the Swagger collection format joining each
     * individual serialized items in the list.
     *
     * @param list The list to serialize.
     * @param format The collection joining format.
     * @return The list serialized as a joined string.
     */
    String serializeList(List<?> list, CollectionFormat format);

    /**
     * Serializes an iterable into a string with the delimiter specified with the Swagger collection format joining each
     * individual serialized items in the list.
     *
     * @param iterable The iterable to serialize.
     * @param format The collection joining format.
     * @return The iterable serialized as a joined string.
     */
    default String serializeIterable(Iterable<?> iterable, CollectionFormat format) {
        if (iterable == null) {
            return null;
        }

        return StreamSupport.stream(iterable.spliterator(), false)
            .map(this::serializeRaw)
            .map(serializedString -> serializedString == null ? "" : serializedString)
            .collect(Collectors.joining(format.getDelimiter()));
    }

    /**
     * Deserializes a string into an object.
     *
     * @param value The string to deserialize.
     * @param <T> The type of the deserialized object.
     * @param type The type of the deserialized object.
     * @param encoding The deserialization encoding.
     * @return The string deserialized into an object.
     * @throws IOException If an IO exception was thrown during deserialization.
     */
    <T> T deserialize(String value, Type type, SerializerEncoding encoding) throws IOException;

    /**
     * Deserializes a byte array into an object.
     *
     * @param bytes The byte array to deserialize.
     * @param type The type of the deserialized object.
     * @param encoding The deserialization encoding.
     * @param <T> The type of the deserialized object.
     * @return The string deserialized into an object.
     * @throws IOException If an IO exception was thrown during serialization.
     */
    default <T> T deserialize(byte[] bytes, Type type, SerializerEncoding encoding) throws IOException {
        return deserialize(new String(bytes, StandardCharsets.UTF_8), type, encoding);
    }

    /**
     * Deserializes a stream into an object.
     *
     * @param inputStream The {@link InputStream} to deserialize.
     * @param type The type of the deserialized object.
     * @param encoding The deserialization encoding.
     * @param <T> The type of the deserialized object.
     * @return The stream deserialized into an object.
     * @throws IOException If an IO exception was thrown during serialization.
     */
    default <T> T deserialize(final InputStream inputStream, final Type type, final SerializerEncoding encoding)
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
        return deserialize(converterStream.toString(StandardCharsets.UTF_8.name()), type, encoding);
    }

    /**
     * Deserialize the provided headers returned from a REST API to an entity instance declared as the model to hold
     * 'Matching' headers.
     * <p>
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
     * @param <T> the type of the deserialized object
     * @param type the type to deserialize
     * @return instance of header entity type created based on provided {@code headers}, if header entity model does not
     * not exists then return null
     * @throws IOException If an I/O error occurs
     */
    <T> T deserialize(HttpHeaders headers, Type type) throws IOException;
}
