// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * Model representing a JSON boolean value.
 */
public final class JsonBoolean extends JsonElement {
    private static final JsonBoolean TRUE = new JsonBoolean(true);
    private static final JsonBoolean FALSE = new JsonBoolean(false);

    private final boolean value;

    private JsonBoolean(boolean value) {
        this.value = value;
    }

    /**
     * Gets the instance of JsonBoolean for the specified boolean value.
     *
     * @param value The boolean value for which to obtain the JsonBoolean instance.
     * @return The instance of JsonBoolean representing the specified boolean value.
     */
    public static JsonBoolean getInstance(boolean value) {
        return value ? TRUE : FALSE;
    }

    /**
     * Returns boolean value from a JsonBoolean object.
     *
     * @return The boolean value.
     */
    public boolean getValue() {
        return value;
    }

    /**
     * Identifies if an object is of type JsonBoolean.
     *
     * @return boolean of whether this JsonElement object is of type JsonBoolean.
     */
    @Override
    public boolean isBoolean() {
        return true;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeBoolean(value);
    }

    /**
     * Deserializes a JSON boolean from a JsonReader.
     * <p>
     * If the JsonReader's current token is null, it is assumed the JsonReader hasn't begun reading and
     * {@link JsonReader#nextToken()} will be called to begin reading.
     * <p>
     * After ensuring the JsonReader has begun reading, if the current token is not {@link JsonToken#BOOLEAN}, an
     * {@link IllegalStateException} will be thrown. Otherwise, {@link JsonBoolean#getInstance(boolean)} will be called
     * to return the deserialized JSON boolean.
     *
     * @param jsonReader The JsonReader to deserialize from.
     * @return The deserialized JSON boolean.
     * @throws IOException If an error occurs while deserializing the JSON boolean.
     * @throws IllegalStateException If the current token is not {@link JsonToken#BOOLEAN}.
     */
    public static JsonBoolean fromJson(JsonReader jsonReader) throws IOException {
        JsonToken token = jsonReader.currentToken();
        if (token == null) {
            token = jsonReader.nextToken();
        }

        if (token != JsonToken.BOOLEAN) {
            throw new IllegalStateException(
                "JsonReader is pointing to an invalid token for deserialization. Token was: " + token + ".");
        }

        return getInstance(jsonReader.getBoolean());
    }

    @Override
    public String toJsonString() throws IOException {
        return Boolean.toString(value);
    }
}
