// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util.serializer;

import com.typespec.core.annotation.HeaderCollection;
import com.typespec.core.http.models.HttpHeaders;
import com.typespec.core.models.TypeReference;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
     * @param data Byte array.
     * @param typeReference {@link TypeReference} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized byte array.
     */
    default <T> T deserializeFromBytes(byte[] data, TypeReference<T> typeReference) {
        /*
         * If the byte array is null pass an empty one by default. This is better than returning null as a previous
         * implementation of this may have custom handling for empty data.
         */
        return (data == null)
            ? deserialize(new ByteArrayInputStream(new byte[0]), typeReference)
            : deserialize(new ByteArrayInputStream(data), typeReference);
    }

    /**
     * Reads a stream into its object representation.
     *
     * @param stream {@link InputStream} of data.
     * @param typeReference {@link TypeReference} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized stream.
     */
    <T> T deserialize(InputStream stream, TypeReference<T> typeReference);

    /**
     * Deserialize the provided headers returned from a REST API to an entity instance declared as the model to hold
     * 'Matching' headers.
     * <p>
     * 'Matching' headers are the REST API returned headers those with:
     *
     * <ol>
     *   <li>header names same as name of a properties in the entity.</li>
     *   <li>header names start with value of {@link HeaderCollection} annotation applied to
     *   the properties in the entity.</li>
     * </ol>
     *
     *
     * in the case of above example, this method produces an instance of FooMetadataHeaders from provided
     * {@code headers}.
     *
     * @param httpHeaders the REST API returned headers
     * @param <T> the type of the deserialized object
     * @param type the type to deserialize
     * @return instance of header entity type created based on provided {@code headers}, if header entity model does
     * not exist then return null
     * @throws IOException If an I/O error occurs
     */
    <T> T deserialize(HttpHeaders httpHeaders, Type type) throws IOException;

    /**
     * Converts the object into a byte array.
     *
     * @param value The object.
     * @return The binary representation of the serialized object.
     */
    default byte[] serializeToBytes(Object value) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        serialize(stream, value);

        return stream.toByteArray();
    }

    /**
     * Writes the serialized object into a stream.
     *
     * @param stream {@link OutputStream} where the serialized object will be written.
     * @param value The object.
     */
    void serialize(OutputStream stream, Object value);
}
