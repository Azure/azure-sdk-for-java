// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Generic interface covering basic JSON serialization and deserialization methods.
 */
public interface JsonSerializer extends ObjectSerializer {
    /**
     * Reads a JSON stream into its object representation.
     *
     * @param stream JSON stream.
     * @param typeReference {@link TypeReference} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized JSON stream.
     */
    @Override
    <T> T deserialize(InputStream stream, TypeReference<T> typeReference);

    /**
     * Reads a JSON stream into its object representation.
     *
     * @param stream JSON stream.
     * @param typeReference {@link TypeReference} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized JSON stream.
     */
    @Override
    <T> Mono<T> deserializeAsync(InputStream stream, TypeReference<T> typeReference);

    /**
     * Writes an object's JSON into a stream..
     *
     * @param stream {@link OutputStream} where the object's JSON will be written.
     * @param value The object.
     */
    @Override
    void serialize(OutputStream stream, Object value);

    /**
     * Writes an object's JSON into a stream..
     *
     * @param stream {@link OutputStream} where the object's JSON will be written.
     * @param value The object.
     * @return Reactive stream that will indicate operation completion.
     */
    @Override
    Mono<Void> serializeAsync(OutputStream stream, Object value);
}
