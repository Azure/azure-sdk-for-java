// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.util.serializer;

import com.generic.core.http.annotation.HeaderCollection;
import com.generic.core.models.Header;
import com.generic.core.models.Headers;
import com.generic.core.models.TypeReference;

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
     * @param data The byte array.
     * @param typeReference {@link TypeReference} representing the object.
     * @param <T> Type of the object.
     *
     * @return The object represented by the deserialized byte array.
     */
    default <T> T deserializeFromBytes(byte[] data, TypeReference<T> typeReference) {
        /*
         * If the byte array is null then pass an empty one by default. This is better than returning null as a previous
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
     *
     * @return The object represented by the deserialized stream.
     */
    <T> T deserialize(InputStream stream, TypeReference<T> typeReference);

    /**
     * Deserialize the provided headers returned from a REST API to an entity instance declared as the model to hold the
     * 'Matching' headers.
     *
     * <p>'Matching' headers are the REST API returned headers those with:</p>
     *
     * <ol>
     *   <li>Header names same as name of a properties in the entity.</li>
     *   <li>Header names start with value of {@link HeaderCollection} annotation applied to the properties in the
     *   entity.</li>
     * </ol>
     *
     * <p>In the case of the example above, this method produces an instance of {@code FooMetadataHeaders} from the
     * provided {@link Headers}.</p>
     *
     * @param headers The {@link Headers} returned by the REST API.
     * @param <T> The type that the {@link Headers} will be deserialized to.
     * @param type The {@link Type} that the {@link Headers} will be deserialized to.
     *
     * @return An instance of the provided {@link Type} based on the provided {@link Headers}. If the {@link Header}
     * entity model does not exist then {@code null} is returned.
     *.
     * @throws IOException If an I/O error occurs.
     */
    <T> T deserialize(Headers headers, Type type) throws IOException;

    /**
     * Serializes an object into a byte array.
     *
     * @param value The object to serialize.
     *
     * @return The binary representation of the serialized object.
     */
    default byte[] serializeToBytes(Object value) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        serialize(stream, value);

        return stream.toByteArray();
    }

    /**
     * Serializes and writes an object into a provided stream.
     *
     * @param stream {@link OutputStream} where the serialized object will be written.
     * @param value The object to serialize.
     */
    void serialize(OutputStream stream, Object value);
}
