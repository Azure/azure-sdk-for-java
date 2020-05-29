// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

/**
 * Generic interface covering basic JSON serialization and deserialization methods.
 */
public interface JsonSerializer extends ObjectSerializer {
    /**
     * Reads a JSON byte array into its object representation.
     *
     * @param input JSON byte array.
     * @param clazz {@link Class} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized JSON byte array.
     */
    @Override
    <T> T deserialize(byte[] input, Class<T> clazz);

    /**
     * Reads a JSON tree into its object representation.
     *
     * @param jsonNode The JSON tree.
     * @param clazz {@link Class} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized JSON tree.
     */
    <T> T deserializeTree(JsonNode jsonNode, Class<T> clazz);

    /**
     * Writes an object into its JSON byte array representation.
     *
     * @param value The object.
     * @return The JSON byte array representing the object.
     */
    @Override
    byte[] serialize(Object value);

    /**
     * Writes a JSON tree into its JSON byte array representation.
     *
     * @param jsonNode The JSON tree.
     * @return The JSON byte array representing the object.
     */
    byte[] serializeTree(JsonNode jsonNode);

    /**
     * Reads a JSON byte array into its JSON tree representation.
     *
     * @param input JSON byte array.
     * @return The JSON tree representing the deserialized JSON byte array.
     */
    JsonNode toTree(byte[] input);

    /**
     * Writes an object into its JSON tree representation.
     *
     * @param value The object.
     * @return The JSON tree representing the object.
     */
    JsonNode toTree(Object value);
}
