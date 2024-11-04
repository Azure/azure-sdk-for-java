// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;

import java.io.IOException;

/**
 * Interface defining methods that all JSON types must implement.
 */
public abstract class JsonElement implements JsonSerializable<JsonElement> {
    /**
     * Default constructor.
     */
    public JsonElement() {
    }

    /**
     * Deserializes a JSON element from a JsonReader.
     * <p>
     * The type of JSON element returned is dependent on what the JsonReader's current token is. If the current token is
     * null, it is assumed the JsonReader hasn't begun reading and {@link JsonReader#nextToken()} will be called to
     * begin reading.
     * <p>
     * After ensuring the JsonReader has begun reading, the following determines the type of JSON element returned:
     * <ul>
     *     <li>If the current token is {@link JsonToken#START_OBJECT}, a {@link JsonObject} is returned.</li>
     *     <li>If the current token is {@link JsonToken#START_ARRAY}, a {@link JsonArray} is returned.</li>
     *     <li>If the current token is {@link JsonToken#STRING}, a {@link JsonString} is returned.</li>
     *     <li>If the current token is {@link JsonToken#NUMBER}, a {@link JsonNumber} is returned.</li>
     *     <li>If the current token is {@link JsonToken#BOOLEAN}, a {@link JsonBoolean} is returned.</li>
     *     <li>If the current token is {@link JsonToken#NULL}, a {@link JsonNull} is returned.</li>
     *     <li>All other token types will throw an {@link IllegalStateException}.</li>
     * </ul>
     *
     * @param jsonReader The JsonReader to deserialize from.
     * @return The deserialized JSON element.
     * @throws IOException If an error occurs while deserializing the JSON element.
     * @throws IllegalStateException If the current token is not a valid JSON token.
     */
    static JsonElement fromJson(JsonReader jsonReader) throws IOException {
        JsonToken token = jsonReader.currentToken();
        if (token == null) {
            token = jsonReader.nextToken();
        }

        switch (token) {
            case START_OBJECT:
                return JsonObject.fromJson(jsonReader);

            case START_ARRAY:
                return JsonArray.fromJson(jsonReader);

            case STRING:
                return new JsonString(jsonReader.getString());

            case NUMBER:
                return new JsonNumber(jsonReader.getString());

            case BOOLEAN:
                return JsonBoolean.getInstance(jsonReader.getBoolean());

            case NULL:
                return JsonNull.getInstance();

            default:
                throw new IllegalStateException(
                    "JsonReader is pointing to an invalid token for deserialization. Token was: " + token + ".");
        }
    }

    /**
     * Indicates whether the element is an array.
     *
     * @return Whether the element is an array.
     */
    public boolean isArray() {
        return false;
    }

    /**
     * Indicates whether the element is an object.
     *
     * @return Whether the element is an object.
     */
    public boolean isObject() {
        return false;
    }

    /**
     * Indicates whether the element is a boolean.
     *
     * @return Whether the element is a boolean.
     */
    public boolean isBoolean() {
        return false;
    }

    /**
     * Indicates whether the element is a null.
     *
     * @return Whether the element is a null.
     */
    public boolean isNull() {
        return false;
    }

    /**
     * Indicates whether the element is a number.
     *
     * @return Whether the element is a number.
     */
    public boolean isNumber() {
        return false;
    }

    /**
     * Indicates whether the element is a string.
     *
     * @return Whether the element is a string.
     */
    public boolean isString() {
        return false;
    }
}
