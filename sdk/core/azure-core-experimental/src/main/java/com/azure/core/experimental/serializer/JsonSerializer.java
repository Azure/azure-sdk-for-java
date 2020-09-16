// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.serializer;

import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Generic interface covering basic JSON serialization and deserialization methods.
 */
public interface JsonSerializer extends com.azure.core.util.serializer.JsonSerializer {
    /**
     * Reads a JSON tree into its object representation.
     *
     * @param jsonNode The JSON tree.
     * @param typeReference {@link TypeReference} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized JSON tree.
     */
    <T> T deserializeTree(JsonNode jsonNode, TypeReference<T> typeReference);

    /**
     * Reads a JSON tree into its object representation.
     *
     * @param jsonNode The JSON tree.
     * @param typeReference {@link TypeReference} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized JSON tree.
     */
    <T> Mono<T> deserializeTreeAsync(JsonNode jsonNode, TypeReference<T> typeReference);

    /**
     * Writes a JSON tree into a stream.
     *
     * @param stream {@link OutputStream} where the JSON tree will be written.
     * @param jsonNode The JSON tree.
     */
    void serializeTree(OutputStream stream, JsonNode jsonNode);

    /**
     * Writes a JSON tree into a stream.
     *
     * @param stream {@link OutputStream} where the JSON tree will be written.
     * @param jsonNode The JSON tree.
     * @return Reactive stream that will indicate operation completion.
     */
    Mono<Void> serializeTreeAsync(OutputStream stream, JsonNode jsonNode);

    /**
     * Reads a JSON stream into its JSON tree representation.
     *
     * @param stream JSON stream.
     * @return The JSON tree representing the deserialized JSON byte array.
     */
    JsonNode toTree(InputStream stream);

    /**
     * Reads a JSON stream into its JSON tree representation.
     *
     * @param stream JSON stream.
     * @return The JSON tree representing the deserialized JSON byte array.
     */
    Mono<JsonNode> toTreeAsync(InputStream stream);

    /**
     * Writes an object into its JSON tree representation.
     *
     * @param value The object.
     * @return The JSON tree representing the object.
     */
    JsonNode toTree(Object value);

    /**
     * Writes an object into its JSON tree representation.
     *
     * @param value The object.
     * @return The JSON tree representing the object.
     */
    Mono<JsonNode> toTreeAsync(Object value);
}
