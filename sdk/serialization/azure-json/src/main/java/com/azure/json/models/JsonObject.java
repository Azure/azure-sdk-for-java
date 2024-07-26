// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.json.implementation.StringBuilderWriter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Class representing the JSON object type.
 */
public class JsonObject extends JsonElement {
    private final Map<String, JsonElement> properties;

    /**
     * Default constructor.
     */
    public JsonObject() {
        this.properties = new LinkedHashMap<>();
    }

    private JsonObject(Map<String, JsonElement> properties) {
        this.properties = properties;
    }

    /**
     * Gets the JsonElement value corresponding to the specified key. If the key doesn't exist, null will be returned.
     *
     * @param key The key of the property to get.
     * @return The JsonElement value corresponding to the specified key, or null if the property doesn't exist.
     */
    public JsonElement getProperty(String key) {
        return properties.get(key);
    }

    /**
     * Sets the JsonElement value corresponding to the specified key. If the key already exists, the value will be
     * overwritten.
     *
     * @param key The key of the property to set.
     * @param element The JsonElement value to set the property to.
     * @return The updated JsonObject object.
     * @throws NullPointerException If the {@code key} or {@code element} is null.
     */
    public JsonObject setProperty(String key, JsonElement element) {
        properties.put(key, nullCheck(element));
        return this;
    }

    /**
     * Removes the JsonElement value corresponding to the specified key. If the key doesn't exist, null will be
     * returned.
     *
     * @param key The key of the property to remove.
     * @return The JsonElement value corresponding to the specified key, or null if the property doesn't exist.
     * @throws NullPointerException If the {@code key} is null.
     */
    public JsonElement removeProperty(String key) {
        return properties.remove(key);
    }

    /**
     * The number of properties in the JSON object.
     *
     * @return The number of properties in the JSON object.
     */
    public int size() {
        return properties.size();
    }

    @Override
    public final boolean isObject() {
        return true;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeMap(properties, JsonWriter::writeJson);
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
    public static JsonObject fromJson(JsonReader jsonReader) throws IOException {
        JsonToken token = jsonReader.currentToken();
        if (token == null) {
            token = jsonReader.nextToken();
        }

        if (token != JsonToken.START_OBJECT) {
            throw new IllegalStateException(
                "JsonReader is pointing to an invalid token for deserialization. Token was: " + token + ".");
        }

        return new JsonObject(jsonReader.readMap(JsonElement::fromJson));
    }

    @Override
    public String toJsonString() throws IOException {
        // TODO (alzimmer): This could be cached and reset each time the array is mutated.
        StringBuilderWriter writer = new StringBuilderWriter();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(writer)) {
            toJson(jsonWriter).flush();
            return writer.toString();
        }
    }

    /**
     * Checks that the JsonElement isn't null.
     * <p>
     * In structured JSON nullness must be represented by {@link JsonNull} and not {@code null}.
     *
     * @throws NullPointerException If the {@code element} is null.
     */
    private static JsonElement nullCheck(JsonElement element) {
        return Objects.requireNonNull(element,
            "The JsonElement cannot be null. If null must be represented in JSON, use JsonNull.");
    }

    // Following methods are overridden as final to prevent subtypes from changing the behavior.
    @Override
    public final boolean isArray() {
        return false;
    }

    @Override
    public final boolean isBoolean() {
        return false;
    }

    @Override
    public final boolean isNull() {
        return false;
    }

    @Override
    public final boolean isNumber() {
        return false;
    }

    @Override
    public final boolean isString() {
        return false;
    }
}
