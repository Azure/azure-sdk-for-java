// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer;

import reactor.core.publisher.Mono;

/**
 * Generic interface covering basic schema serialization and deserialization methods.
 */
public interface ObjectSerializer {
    /**
     * Reads the byte stream into its object representation.
     *
     * @param <T> Type of the object.
     * @param input Incoming byte stream.
     * @param clazz The {@link Class} representing the object.
     * @return The object representing the byte stream based on the schema.
     */
    <T> Mono<T> deserialize(byte[] input, Class<T> clazz);

    /**
     * Writes the object into its byte stream.
     *
     * @param value The object.
     * @return The byte stream representing the object based on the schema.
     */
    Mono<byte[]> serialize(Object value);
}
