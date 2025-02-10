// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.serializer.json.gson.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Objects;

/**
 * Implementation of {@link com.azure.json.models.JsonElement} which is backed by GSON's {@link JsonObject}.
 * <p>
 * This allows for using GSON's {@link JsonObject} in places where {@link com.azure.json.models.JsonObject} is required,
 * meaning the GSON {@link JsonObject} doesn't need to be converted to azure-json's
 * {@link com.azure.json.models.JsonObject}.
 */
public final class GsonJsonObject extends com.azure.json.models.JsonElement {
    private final JsonObject object;

    /**
     * Creates a new {@link GsonJsonObject} using the default {@link JsonObject#JsonObject()}.
     */
    public GsonJsonObject() {
        this(new JsonObject());
    }

    /**
     * Creates a new {@link GsonJsonObject} using the provided {@link JsonObject}.
     *
     * @param object The {@link JsonObject} to use as the backing object.
     * @throws NullPointerException If {@code object} is null.
     */
    public GsonJsonObject(JsonObject object) {
        this.object = Objects.requireNonNull(object, "'object' cannot be null.");
    }

    /**
     * Gets the JsonElement value corresponding to the specified key. If the key doesn't exist, null will be returned.
     *
     * @param key The key of the property to get.
     * @return The JsonElement value corresponding to the specified key, or null if the property doesn't exist.
     */
    public JsonElement getProperty(String key) {
        return object.get(key);
    }

    /**
     * Sets the JsonElement value corresponding to the specified key. If the key already exists, the value will be
     * overwritten.
     *
     * @param key The key of the property to set.
     * @param element The JsonElement value to set the property to.
     * @return The updated GsonJsonObject object.
     * @throws NullPointerException If the {@code key} or {@code element} is null.
     */
    public GsonJsonObject setProperty(String key, JsonElement element) {
        object.add(key, nullCheck(element));
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
        return object.remove(key);
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
        return GsonJsonElementUtils.writeJsonObject(jsonWriter, object);
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
    public static GsonJsonObject fromJson(JsonReader jsonReader) throws IOException {
        JsonToken token = jsonReader.currentToken();
        if (token == null) {
            token = jsonReader.nextToken();
        }

        if (token != JsonToken.START_OBJECT) {
            throw new IllegalStateException(
                "JsonReader is pointing to an invalid token for deserialization. Token was: " + token + ".");
        }

        return new GsonJsonObject(GsonJsonElementUtils.readJsonObject(jsonReader));
    }
}
