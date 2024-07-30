// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

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
     * Parsing attempts to use the smallest container that can represent the number. For floating points it'll attempt
     * to use {@link Float#parseFloat(String)}, if that fails it'll use {@link Double#parseDouble(String)}, and finally
     * if that fails it'll use {@link BigDecimal#BigDecimal(String)}. For integers it'll attempt to use
     * {@link Integer#parseInt(String)}, if that fails it'll use {@link Long#parseLong(String)}, and finally if that
     * fails it'll use {@link BigInteger#BigInteger(String)}.
     * <p>
     * If the string is one of the special floating point representations ({@code NaN}, {@code Infinity}, etc), then
     * the value will be represented using {@link Float}.
     *
     * @param value The string-based numeric value the JsonNumber will represent.
     * @throws NumberFormatException If the string is not a valid number.
     */
    JsonNumber(String value) throws IllegalArgumentException {
        int length = value.length();
        boolean floatingPoint = false;
        boolean infinity = value.contains("Infinity");
        if (infinity) {
            // Use Double.parseDouble to handle Infinity.
            this.value = Double.parseDouble(value);
            return;
        }

        for (int i = 0; i < length; i++) {
            char c = value.charAt(i);
            if (c == '.' || c == 'e' || c == 'E') {
                floatingPoint = true;
                break;
            }
        }

        this.value = floatingPoint ? handleFloatingPoint(value) : handleInteger(value);
    }

    private static Number handleFloatingPoint(String value) {
        // Floating point parsing will return Infinity if the String value is larger than what can be contained by
        // the numeric type. Check if the String contains the Infinity representation to know when to scale up the
        // numeric type.
        // Additionally, due to the handling of values that can't fit into the numeric type, the only time floating
        // point parsing will throw is when the string value is invalid.
        float f = Float.parseFloat(value);

        // If the float wasn't infinite, return it.
        if (!Float.isInfinite(f)) {
            return f;
        }

        double d = Double.parseDouble(value);
        if (!Double.isInfinite(d)) {
            return d;
        }

        return new BigDecimal(value);
    }

    private static Number handleInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException failedInteger) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException failedLong) {
                failedLong.addSuppressed(failedInteger);
                try {
                    return new BigInteger(value);
                } catch (NumberFormatException failedBigDecimal) {
                    failedBigDecimal.addSuppressed(failedLong);
                    throw failedBigDecimal;
                }
            }
        }
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
     * The {@link JsonNumber} returned will have a {@link JsonNumber#getValue()} that is the smallest type that can
     * represent the numeric value. Numeric types used are {@link Integer}, {@link Long}, {@link BigInteger},
     * {@link Float}, {@link Double}, and {@link BigDecimal}.
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
                "JsonReader is pointing to an invalid token for deserialization. " + "Token was: " + token + ".");
        }

        return new JsonNumber(jsonReader.getString());
    }

    @Override
    public String toJsonString() throws IOException {
        return value.toString();
    }
}
