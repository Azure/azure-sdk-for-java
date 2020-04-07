// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer;

import reactor.core.publisher.Mono;

import java.io.OutputStream;

/**
 * Generic interface covering basic JSON serialization and deserialization methods.
 */
public interface JsonSerializer {
    /**
     * Reads the JSON string into its object representation.
     *
     * @param input The JSON string.
     * @param clazz {@link Class} representing the object.
     * @param <T> Type of the object.
     * @return The object representing the JSON string.
     */
    <T> Mono<T> read(String input, Class<T> clazz);

    /**
     * Writes the object into its JSON string.
     *
     * @param value The object.
     * @return The JSON string representing the object.
     */
    Mono<String> write(Object value);

    /**
     * Writes the object into its JSON string.
     *
     * @param value The object.
     * @param clazz {@link Class} representing the object.
     * @return The JSON string representing the object.
     */
    Mono<String> write(Object value, Class<?> clazz);

    /**
     * Converts the object into a JSON string and writes it to the {@link OutputStream}.
     *
     * @param value The object.
     * @param stream The {@link OutputStream} where the JSON string will be written.
     * @return An indicator that the object's JSON string has been written to the {@link OutputStream}.
     */
    Mono<Void> write(Object value, OutputStream stream);

    /**
     * Converts the object into a JSON string and writes it to the {@link OutputStream}.
     *
     * @param value The object.
     * @param stream The {@link OutputStream} where the JSON string will be written.
     * @param clazz {@link Class} representing the object.
     * @return An indicator that the object's JSON string has been written to the {@link OutputStream}.
     */
    Mono<Void> write(Object value, OutputStream stream, Class<?> clazz);
}
