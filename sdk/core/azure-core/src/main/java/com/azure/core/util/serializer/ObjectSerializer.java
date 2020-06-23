// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Generic interface covering serializing and deserialization objects.
 */
public interface ObjectSerializer {
    /**
     * Reads a stream into its object representation.
     *
     * @param stream {@link InputStream} of data.
     * @param clazz {@link Class} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized stream.
     */
    <T> Mono<T> deserialize(InputStream stream, Class<T> clazz);

    /**
     * Writes the object into a stream.
     *
     * @param stream {@link OutputStream} where the object will be written.
     * @param value The object.
     * @return The stream where the object was written.
     */
    Mono<OutputStream> serialize(OutputStream stream, Object value);
}
