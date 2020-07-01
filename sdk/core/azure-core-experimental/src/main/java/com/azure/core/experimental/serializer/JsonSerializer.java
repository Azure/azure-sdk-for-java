// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.serializer;

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
     * @param clazz {@link Class} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized JSON stream.
     */
    @Override
    <T> Mono<T> deserialize(InputStream stream, Class<T> clazz);

    /**
     * Reads a JSON tree into its object representation.
     *
     * @param jsonNode The JSON tree.
     * @param clazz {@link Class} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized JSON tree.
     */
    <T> Mono<T> deserializeTree(JsonNode jsonNode, Class<T> clazz);

    /**
     * Writes an object's JSON into a stream..
     *
     * @param stream {@link OutputStream} where the object's JSON will be written.
     * @param value The object.
     * @return The stream where the object's JSON was written.
     */
    @Override
    Mono<OutputStream> serialize(OutputStream stream, Object value);

    /**
     * Writes a JSON tree into a stream.
     *
     * @param stream {@link OutputStream} where the JSON tree will be written.
     * @param jsonNode The JSON tree.
     * @return The stream where the JSON tree was written.
     */
    Mono<OutputStream> serializeTree(OutputStream stream, JsonNode jsonNode);

    /**
     * Reads a JSON stream into its JSON tree representation.
     *
     * @param stream JSON stream.
     * @return The JSON tree representing the deserialized JSON byte array.
     */
    Mono<JsonNode> toTree(InputStream stream);

    /**
     * Writes an object into its JSON tree representation.
     *
     * @param value The object.
     * @return The JSON tree representing the object.
     */
    Mono<JsonNode> toTree(Object value);
}
