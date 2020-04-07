// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer;

import reactor.core.publisher.Mono;

import java.io.OutputStream;

/**
 * Generic interface covering basic Avro serialization and deserialization methods.
 *
 * @param <SCHEMA> Type representing the Avro schema model.
 */
public interface AvroSerializer<SCHEMA> {
    /**
     * Reads the Avro stream into its object representation.
     *
     * @param input Incoming Avro stream.
     * @param schema Avro schema representing the model.
     * @param <T> Type of the object.
     * @return The object representing the Avro stream.
     */
    <T> Mono<T> read(byte[] input, SCHEMA schema);

    /**
     * Writes the object into its Avro stream.
     *
     * @param value The object.
     * @param schema Avro schema representing the object.
     * @return The Avro stream representing the object.
     */
    Mono<byte[]> write(Object value, SCHEMA schema);

    /**
     * Converts the object into an Avro stream and writes it to the {@link OutputStream}.
     *
     * @param value The object.
     * @param schema Avro schema representing the object.
     * @param stream The {@link OutputStream} where the Avro stream will be written.
     * @return An indicator that the object's Avro stream has been written to the {@link OutputStream}.
     */
    Mono<Void> write(Object value, SCHEMA schema, OutputStream stream);
}
