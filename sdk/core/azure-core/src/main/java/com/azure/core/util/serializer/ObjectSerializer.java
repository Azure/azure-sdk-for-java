// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

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
     * Reads a byte array into its object representation.
     *
     * @param data Byte array.
     * @param typeReference {@link TypeReference} representing the object.
     * @param <T> Type of the object.
     * @return Reactive stream that emits the object represented by the deserialized byte array.
     */
    default <T> Mono<T> deserializeFromBytesAsync(byte[] data, TypeReference<T> typeReference) {
        return Mono.fromCallable(() -> deserializeFromBytes(data, typeReference));
    }

    /**
     * Reads a stream into its object representation.
     *
     * @param stream {@link InputStream} of data.
     * @param typeReference {@link TypeReference} representing the object.
     * @param <T> Type of the object.
     * @return Reactive stream that emits the object represented by the deserialized stream.
     */
    <T> Mono<T> deserializeAsync(InputStream stream, TypeReference<T> typeReference);

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

    /**
     * Converts the object into a byte array.
     *
     * @param value The object.
     * @return Reactive stream that emits the binary representation of the serialized object.
     */
    default Mono<byte[]> serializeToBytesAsync(Object value) {
        return Mono.fromCallable(() -> serializeToBytes(value));
    }

    /**
     * Writes the serialized object into a stream.
     *
     * @param stream {@link OutputStream} where the serialized object will be written.
     * @param value The object.
     * @return Reactive stream that will indicate operation completion.
     */
    Mono<Void> serializeAsync(OutputStream stream, Object value);
}
