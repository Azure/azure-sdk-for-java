// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * Model representing a JSON null value.
 */
public final class JsonNull extends JsonElement {
    private static final JsonNull INSTANCE = new JsonNull();

    private JsonNull() {
    }

    /**
     * Gets the instance of the JsonNull class.
     *
     * @return The JsonNull instance.
     */
    public static JsonNull getInstance() {
        return INSTANCE;
    }

    /**
     * Identifies if a JsonElement is of type JsonNull.
     *
     * @return A boolean value of whether a JsonElement is null.
     */
    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeNull();
    }

    /**
     * Deserializes a JSON null from a JsonReader.
     * <p>
     * If the JsonReader's current token is null, it is assumed the JsonReader hasn't begun reading and
     * {@link JsonReader#nextToken()} will be called to begin reading.
     * <p>
     * After ensuring the JsonReader has begun reading, if the current token is not {@link JsonToken#NULL}, an
     * {@link IllegalStateException} will be thrown. Otherwise, {@link JsonNull#getInstance()} will be called
     * to return the deserialized JSON null.
     *
     * @param jsonReader The JsonReader to deserialize from.
     * @return The deserialized JSON null.
     * @throws IOException If an error occurs while deserializing the JSON null.
     * @throws IllegalStateException If the current token is not {@link JsonToken#NULL}.
     */
    public static JsonNull fromJson(JsonReader jsonReader) throws IOException {
        JsonToken token = jsonReader.currentToken();
        if (token == null) {
            token = jsonReader.nextToken();
        }

        if (token != JsonToken.NULL) {
            throw new IllegalStateException(
                "JsonReader is pointing to an invalid token for deserialization. " + "Token was: " + token + ".");
        }

        return getInstance();
    }

    @Override
    public String toJsonString() throws IOException {
        return "null";
    }
}
