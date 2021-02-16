// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Generic interface covering basic JSON serialization and deserialization methods.
 */
public interface JsonSerializer extends ObjectSerializer {
    /**
     * Reads a JSON byte array into its object representation.
     *
     * @param data JSON byte array.
     * @param typeReference {@link TypeReference} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized JSON byte array.
     */
    @Override
    default <T> T deserialize(byte[] data, TypeReference<T> typeReference) {
        if (data == null) {
            return null;
        }

        return deserialize(new ByteArrayInputStream(data), typeReference);
    }

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
     * Reads a JSON byte array into its object representation.
     *
     * @param data JSON byte array.
     * @param typeReference {@link TypeReference} representing the object.
     * @param <T> Type of the object.
     * @return Reactive stream that emits the object represented by the deserialized JSON byte array.
     */
    @Override
    default <T> Mono<T> deserializeAsync(byte[] data, TypeReference<T> typeReference) {
        if (data == null) {
            return Mono.empty();
        }

        return deserializeAsync(new ByteArrayInputStream(data), typeReference);
    }

    /**
     * Reads a JSON stream into its object representation.
     *
     * @param stream JSON stream.
     * @param typeReference {@link TypeReference} representing the object.
     * @param <T> Type of the object.
     * @return Reactive stream that emits the object represented by the deserialized JSON stream.
     */
    @Override
    <T> Mono<T> deserializeAsync(InputStream stream, TypeReference<T> typeReference);

    /**
     * Converts the object into a JSON byte array.
     *
     * @param value The object.
     * @return The JSON binary representation of the serialized object.
     */
    @Override
    default byte[] serialize(Object value) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        serialize(stream, value);

        return stream.toByteArray();
    }

    /**
     * Writes an object's JSON representation into a stream.
     *
     * @param stream {@link OutputStream} where the object's JSON representation will be written.
     * @param value The object.
     */
    @Override
    void serialize(OutputStream stream, Object value);

    /**
     * Converts the object into a JSON byte array.
     *
     * @param value The object.
     * @return Reactive stream that emits the JSON binary representation of the serialized object.
     */
    @Override
    default Mono<byte[]> serializeAsync(Object value) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        return serializeAsync(stream, value).thenReturn(stream.toByteArray());
    }

    /**
     * Writes an object's JSON representation into a stream.
     *
     * @param stream {@link OutputStream} where the object's JSON representation will be written.
     * @param value The object.
     * @return Reactive stream that will indicate operation completion.
     */
    @Override
    Mono<Void> serializeAsync(OutputStream stream, Object value);
}
