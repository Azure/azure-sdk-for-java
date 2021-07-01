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
     * Reads an Avro byte array into its object representation.
     *
     * @param data Avro byte array.
     * @param typeReference {@link TypeReference} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized Avro byte array.
     */
    @Override
    default <T> T deserializeFromBytes(byte[] data, TypeReference<T> typeReference) {
        return ObjectSerializer.super.deserializeFromBytes(data, typeReference);
    }

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
     * Reads an Avro byte array into its object representation.
     *
     * @param data Avro byte array.
     * @param typeReference {@link TypeReference} representing the object.
     * @param <T> Type of the object.
     * @return Reactive stream that emits the object represented by the deserialized Avro byte array.
     */
    default <T> Mono<T> deserializeFromBytesAsync(byte[] data, TypeReference<T> typeReference) {
        return ObjectSerializer.super.deserializeFromBytesAsync(data, typeReference);
    }

    /**
     * Reads an Avro stream into its object representation.
     *
     * @param stream Avro stream.
     * @param typeReference {@link TypeReference} representing the object.
     * @param <T> Type of the object.
     * @return Reactive stream that emits the object represented by the deserialized Avro stream.
     */
    @Override
    <T> Mono<T> deserializeAsync(InputStream stream, TypeReference<T> typeReference);

    /**
     * Converts the object into an Avro byte array.
     *
     * @param value The object.
     * @return The Avro binary representation of the serialized object.
     */
    default byte[] serializeToBytes(Object value) {
        return ObjectSerializer.super.serializeToBytes(value);
    }

    /**
     * Writes an object's Avro representation into a stream.
     *
     * @param stream {@link OutputStream} where the object's Avro representation will be written.
     * @param value The object.
     */
    @Override
    void serialize(OutputStream stream, Object value);

    /**
     * Converts the object into a Avro byte array.
     *
     * @param value The object.
     * @return Reactive stream that emits the Avro binary representation of the serialized object.
     */
    @Override
    default Mono<byte[]> serializeToBytesAsync(Object value) {
        return ObjectSerializer.super.serializeToBytesAsync(value);
    }

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
