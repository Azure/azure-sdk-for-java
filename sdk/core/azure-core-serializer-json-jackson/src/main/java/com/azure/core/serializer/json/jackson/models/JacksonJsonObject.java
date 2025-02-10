// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.serializer.json.jackson.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.json.models.JsonElement;
import com.azure.json.models.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Objects;

/**
 * Implementation of {@link JsonElement} which is backed by Jackson's {@link ObjectNode}.
 * <p>
 * This allows for using Jackson's {@link ObjectNode} in places where {@link JsonObject} is required, meaning the
 * Jackson {@link ObjectNode} doesn't need to be converted to azure-json's {@link JsonObject}.
 */
public final class JacksonJsonObject extends JsonElement {
    private final ObjectNode object;

    /**
     * Creates a new {@link JacksonJsonObject} using the default {@link JsonNodeFactory#instance} {@link ObjectNode}.
     */
    public JacksonJsonObject() {
        this(JsonNodeFactory.instance.objectNode());
    }

    /**
     * Creates a new {@link JacksonJsonObject} using the provided {@link ObjectNode}.
     *
     * @param object The {@link ObjectNode} to use as the backing object.
     * @throws NullPointerException If {@code object} is null.
     */
    public JacksonJsonObject(ObjectNode object) {
        this.object = Objects.requireNonNull(object, "'object' cannot be null.");
    }

    /**
     * Gets the JsonNode value corresponding to the specified key. If the key doesn't exist, null will be returned.
     *
     * @param key The key of the property to get.
     * @return The JsonNode value corresponding to the specified key, or null if the property doesn't exist.
     */
    public JsonNode getProperty(String key) {
        return object.get(key);
    }

    /**
     * Sets the JsonNode value corresponding to the specified key. If the key already exists, the value will be
     * overwritten.
     *
     * @param key The key of the property to set.
     * @param node The JsonNode value to set the property to.
     * @return The updated JacksonJsonObject object.
     * @throws NullPointerException If the {@code key} or {@code node} is null.
     */
    public JacksonJsonObject setProperty(String key, JsonNode node) {
        object.set(key, nullCheck(node));
        return this;
    }

    /**
     * Removes the JsonNode value corresponding to the specified key. If the key doesn't exist, null will be
     * returned.
     *
     * @param key The key of the property to remove.
     * @return The JsonNode value corresponding to the specified key, or null if the property doesn't exist.
     * @throws NullPointerException If the {@code key} is null.
     */
    public JsonNode removeProperty(String key) {
        return object.remove(key);
    }

    /**
     * Checks that the JsonNode isn't null.
     * <p>
     * In structured JSON nullness must be represented by {@link NullNode} and not {@code null}.
     *
     * @throws NullPointerException If the {@code node} is null.
     */
    private static JsonNode nullCheck(JsonNode node) {
        return Objects.requireNonNull(node,
            "The JsonNode cannot be null. If null must be represented in JSON, use NullNode.");
    }

    /**
     * The number of properties in the JSON object.
     *
     * @return The number of properties in the JSON object.
     */
    public int size() {
        return object.size();
    }

    @Override
    public boolean isObject() {
        return true;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return JacksonJsonElementUtils.writeObjectNode(jsonWriter, object);
    }

    /**
     * Deserializes a JSON object from a JsonReader.
     * <p>
     * If the JsonReader's current token is null, it is assumed the JsonReader hasn't begun reading and
     * {@link JsonReader#nextToken()} will be called to begin reading.
     * <p>
     * After ensuring the JsonReader has begun reading, if the current token is not {@link JsonToken#START_OBJECT}, an
     * {@link IllegalStateException} will be thrown. Otherwise, a JSON object representing the object will be created
     * and returned.
     *
     * @param jsonReader The JsonReader to deserialize from.
     * @return The deserialized JSON object.
     * @throws IOException If an error occurs while deserializing the JSON object.
     * @throws IllegalStateException If the current token is not {@link JsonToken#START_OBJECT}.
     */
    public static JacksonJsonObject fromJson(JsonReader jsonReader) throws IOException {
        JsonToken token = jsonReader.currentToken();
        if (token == null) {
            token = jsonReader.nextToken();
        }

        if (token != JsonToken.START_OBJECT) {
            throw new IllegalStateException(
                "JsonReader is pointing to an invalid token for deserialization. Token was: " + token + ".");
        }

        return new JacksonJsonObject(JacksonJsonElementUtils.readObjectNode(jsonReader));
    }
}
