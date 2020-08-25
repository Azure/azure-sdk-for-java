// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.serializer;

import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Generic interface covering basic Avro serialization and deserialization methods.
 */
public interface AvroSerializer extends ObjectSerializer {
    /**
     * Reads an Avro stream into its object representation.
     *
     * @param stream Avro stream.
     * @param typeReference {@link TypeReference} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized Avro stream.
     */
    @Override
    <T> T deserialize(InputStream stream, TypeReference<T> typeReference);

    /**
     * Reads an Avro stream into its object representation.
     *
     * @param stream Avro stream.
     * @param typeReference {@link TypeReference} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized Avro stream.
     */
    @Override
    <T> Mono<T> deserializeAsync(InputStream stream, TypeReference<T> typeReference);

    /**
     * Writes an object's Avro representation into a stream.
     *
     * @param stream {@link OutputStream} where the object's Avro representation will be written.
     * @param value The object.
     */
    @Override
    void serialize(OutputStream stream, Object value);

    /**
     * Writes an object's Avro representation into a stream.
     *
     * @param stream {@link OutputStream} where the object's Avro representation will be written.
     * @param value The object.
     * @return Reactive stream that will indicate operation completion.
     */
    @Override
    Mono<Void> serializeAsync(OutputStream stream, Object value);
}
