// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.json.implementation.JsonUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

/**
 * Class representing the JSON number type
 */
public final class JsonNumber extends JsonElement {
    /**
     * Stores the String representation of the current state of the JsonNumber
     * object.
     */
    private final Number value;

    /**
     * Creates a JsonNumber representing the string-based number.
     * <p>
     * Parsing of the string value is decided by the format of the string. If the string contains a decimal point
     * ({@code .}) or an exponent ({@code e} or {@code E}), the string will be parsed as a floating point number,
     * otherwise it will be parsed as an integer.
     * <p>
     * For integer numbers, this method will return the smallest number type that can represent the number. Where
     * {@link Integer} is preferred over {@link Long} and {@link Long} is preferred over {@link BigInteger}.
     * <p>
     * For floating point numbers, {@link Double} will be preferred but {@link BigDecimal} will be used if the number
     * is too large to fit in a {@link Double}.
     * <p>
     * If the string is one of the special floating point representations ({@code NaN}, {@code Infinity}, etc), then
     * the value will be represented using {@link Double}.
     *
     * @param value The string-based numeric value the JsonNumber will represent.
     * @throws NumberFormatException If the string is not a valid number.
     */
    JsonNumber(String value) throws IllegalArgumentException {
        this.value = JsonUtils.parseNumber(value);
    }

    /**
     * Creates a JsonNumber representing the specified number.
     *
     * @param value The number value the JsonNumber will represent.
     * @throws NullPointerException If {@code value} is null.
     */
    public JsonNumber(Number value) {
        this.value = Objects.requireNonNull(value, "JsonNumber cannot represent a null value.");
    }

    /**
     * Returns the Number value from a JsonNumber object.
     * <p>
     * The value returned by this method will never be null.
     *
     * @return The Number value.
     */
    public Number getValue() {
        return this.value;
    }

    /**
     * Whether the JsonElement is a JsonNumber.
     *
     * @return boolean of whether this JsonElement object is of type JsonNumber.
     */
    @Override
    public boolean isNumber() {
        return true;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeRawValue(value.toString());
    }

    /**
     * Deserializes a JSON number from a JsonReader.
     * <p>
     * If the JsonReader's current token is null, it is assumed the JsonReader hasn't begun reading and
     * {@link JsonReader#nextToken()} will be called to begin reading.
     * <p>
     * After ensuring the JsonReader has begun reading, if the current token is not {@link JsonToken#NUMBER}, an
     * {@link IllegalStateException} will be thrown. Otherwise, a JSON number representing the numeric value will be
     * created and returned.
     * <p>
     * For integer numbers, this method will return the smallest number type that can represent the number. Where
     * {@link Integer} is preferred over {@link Long} and {@link Long} is preferred over {@link BigInteger}.
     * <p>
     * For floating point numbers, {@link Double} will be preferred but {@link BigDecimal} will be used if the number
     * is too large to fit in a {@link Double}.
     * <p>
     * If the string is one of the special floating point representations ({@code NaN}, {@code Infinity}, etc), then
     * the value will be represented using {@link Double}.
     *
     * @param jsonReader The JsonReader to deserialize from.
     * @return The deserialized JSON number.
     * @throws IOException If an error occurs while deserializing the JSON number.
     * @throws IllegalStateException If the current token is not {@link JsonToken#NUMBER}.
     */
    public static JsonNumber fromJson(JsonReader jsonReader) throws IOException {
        JsonToken token = jsonReader.currentToken();
        if (token == null) {
            token = jsonReader.nextToken();
        }

        if (token != JsonToken.NUMBER) {
            throw new IllegalStateException(
                "JsonReader is pointing to an invalid token for deserialization. Token was: " + token + ".");
        }

        return new JsonNumber(jsonReader.getString());
    }

    @Override
    public String toJsonString() throws IOException {
        return value.toString();
    }
}
