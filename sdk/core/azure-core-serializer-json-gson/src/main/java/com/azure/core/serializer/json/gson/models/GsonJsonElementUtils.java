// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.serializer.json.gson.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class that helps convert between GSON and Azure JSON models.
 */
public final class GsonJsonElementUtils {
    /**
     * Reads a {@link JsonElement} from the {@link JsonReader}.
     * <p>
     * Throws an {@link IllegalArgumentException} if the {@link JsonReader} is pointing to an unsupported token type.
     *
     * @param jsonReader The {@link JsonReader} being read from.
     * @return The {@link JsonElement} read from the {@link JsonReader}.
     * @throws IOException If an error occurs while reading the {@link JsonReader}.
     * @throws IllegalArgumentException If the {@link JsonReader} is pointing to an unsupported token type.
     * @throws NullPointerException If the {@code jsonReader} is null.
     */
    public static JsonElement readJsonElement(JsonReader jsonReader) throws IOException {
        Objects.requireNonNull(jsonReader, "'jsonReader' cannot be null.");

        JsonToken currentToken = jsonReader.currentToken();
        if (currentToken == null) {
            currentToken = jsonReader.nextToken();
        }

        switch (currentToken) {
            case START_OBJECT:
                return readJsonObject(jsonReader);

            case START_ARRAY:
                return readJsonArray(jsonReader);

            case BOOLEAN:
                return new JsonPrimitive(jsonReader.getBoolean());

            case NULL:
                return JsonNull.INSTANCE;

            case NUMBER:
                return parseNumeric(jsonReader.getString());

            case STRING:
                return new JsonPrimitive(jsonReader.getString());

            default:
                throw new IllegalArgumentException("Unsupported JsonToken type: " + currentToken);
        }
    }

    /**
     * Creates a NumericNode representing the string-based number.
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
    private static JsonPrimitive parseNumeric(String value) {
        int length = value.length();
        boolean floatingPoint = false;
        boolean infinity = value.contains("Infinity");
        if (infinity) {
            // Use Double.parseDouble to handle Infinity.
            return new JsonPrimitive(Double.parseDouble(value));
        }

        for (int i = 0; i < length; i++) {
            char c = value.charAt(i);
            if (c == '.' || c == 'e' || c == 'E') {
                floatingPoint = true;
                break;
            }
        }

        return floatingPoint ? handleFloatingPoint(value) : handleInteger(value);
    }

    private static JsonPrimitive handleFloatingPoint(String value) {
        // Floating point parsing will return Infinity if the String value is larger than what can be contained by
        // the numeric type. Check if the String contains the Infinity representation to know when to scale up the
        // numeric type.
        // Additionally, due to the handling of values that can't fit into the numeric type, the only time floating
        // point parsing will throw is when the string value is invalid.
        float f = Float.parseFloat(value);

        // If the float wasn't infinite, return it.
        if (!Float.isInfinite(f)) {
            return new JsonPrimitive(f);
        }

        double d = Double.parseDouble(value);
        if (!Double.isInfinite(d)) {
            return new JsonPrimitive(d);
        }

        return new JsonPrimitive(new BigDecimal(value));
    }

    private static JsonPrimitive handleInteger(String value) {
        try {
            return new JsonPrimitive(Integer.parseInt(value));
        } catch (NumberFormatException failedInteger) {
            try {
                return new JsonPrimitive(Long.parseLong(value));
            } catch (NumberFormatException failedLong) {
                failedLong.addSuppressed(failedInteger);
                try {
                    return new JsonPrimitive(new BigInteger(value));
                } catch (NumberFormatException failedBigDecimal) {
                    failedBigDecimal.addSuppressed(failedLong);
                    throw failedBigDecimal;
                }
            }
        }
    }

    /**
     * Reads an array value from the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} to read the array value from.
     * @return The {@link JsonArray} read from the {@link JsonReader}.
     * @throws IOException If an error occurs while reading the array value.
     * @throws IllegalArgumentException If the array contains an unsupported {@link JsonElement} type.
     */
    static JsonArray readJsonArray(JsonReader jsonReader) throws IOException {
        JsonArray jsonArray = new JsonArray();

        while (jsonReader.nextToken() != JsonToken.END_ARRAY) {
            jsonArray.add(readJsonElement(jsonReader));
        }

        return jsonArray;
    }

    /**
     * Reads an object value from the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} to read the object value from.
     * @return The {@link JsonObject} read from the {@link JsonReader}.
     * @throws IOException If an error occurs while reading the object value.
     * @throws IllegalArgumentException If the object contains an unsupported {@link JsonElement} type.
     */
    static JsonObject readJsonObject(JsonReader jsonReader) throws IOException {
        JsonObject jsonObject = new JsonObject();

        while (jsonReader.nextToken() != JsonToken.END_ARRAY) {
            String fieldName = jsonReader.getFieldName();
            jsonReader.nextToken();
            jsonObject.add(fieldName, readJsonElement(jsonReader));
        }

        return jsonObject;
    }

    /**
     * Writes the {@link JsonElement} to the {@link JsonWriter}.
     *
     * @param jsonWriter The {@link JsonWriter} to write the {@link JsonElement} to.
     * @param jsonElement The {@link JsonElement} to write to the {@link JsonWriter}.
     * @return The {@link JsonWriter} after writing the {@link JsonElement}.
     * @throws IllegalArgumentException If the {@link JsonElement} contains an unsupported type.
     * @throws IOException If an error occurs while writing the {@link JsonElement}.
     */
    public static JsonWriter writeJsonElement(JsonWriter jsonWriter, JsonElement jsonElement) throws IOException {
        if (jsonElement == null) {
            return jsonWriter.writeNull();
        } else if (jsonElement.isJsonArray()) {
            return writeJsonArray(jsonWriter, jsonElement.getAsJsonArray());
        } else if (jsonElement.isJsonNull()) {
            return jsonWriter.writeNull();
        } else if (jsonElement.isJsonObject()) {
            return writeJsonObject(jsonWriter, jsonElement.getAsJsonObject());
        } else if (jsonElement.isJsonPrimitive()) {
            JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
            if (jsonPrimitive.isBoolean()) {
                return jsonWriter.writeBoolean(jsonPrimitive.getAsBoolean());
            } else if (jsonPrimitive.isNumber()) {
                return jsonWriter.writeNumber(jsonPrimitive.getAsNumber());
            } else if (jsonPrimitive.isString()) {
                return jsonWriter.writeString(jsonPrimitive.getAsString());
            }
        }

        throw new IllegalArgumentException("Unsupported JsonNode type: " + jsonElement.getClass());
    }

    /**
     * Writes the array value of the {@link JsonArray} to the {@link JsonWriter}.
     * <p>
     * If {@code arrayNode} is null the method will write a null value to the {@link JsonWriter}.
     *
     * @param jsonWriter The {@link JsonWriter} to write the array value to.
     * @param jsonArray The {@link JsonArray} to write the array value from.
     * @return The {@link JsonWriter} after writing the array value.
     * @throws IOException If an error occurs while writing the array value.
     */
    static JsonWriter writeJsonArray(JsonWriter jsonWriter, JsonArray jsonArray) throws IOException {
        if (jsonArray == null) {
            return jsonWriter.writeNull();
        }

        jsonWriter.writeStartArray();
        for (JsonElement jsonNode : jsonArray) {
            writeJsonElement(jsonWriter, jsonNode);
        }
        jsonWriter.writeEndArray();

        return jsonWriter;
    }

    /**
     * Writes the object value of the {@link JsonObject} to the {@link JsonWriter}.
     * <p>
     * If {@code jsonObject} is null the method will write a null value to the {@link JsonWriter}.
     *
     * @param jsonWriter The {@link JsonWriter} to write the object value to.
     * @param jsonObject The {@link JsonObject} to write the object value from.
     * @return The {@link JsonWriter} after writing the object value.
     * @throws IOException If an error occurs while writing the object value.
     * @throws IllegalArgumentException If the {@link JsonObject} contains an unsupported {@link JsonElement} type.
     */
    static JsonWriter writeJsonObject(JsonWriter jsonWriter, JsonObject jsonObject) throws IOException {
        if (jsonObject == null) {
            return jsonWriter.writeNull();
        }

        jsonWriter.writeStartObject();
        for (Map.Entry<String, JsonElement> entry : jsonObject.asMap().entrySet()) {
            jsonWriter.writeFieldName(entry.getKey());
            writeJsonElement(jsonWriter, entry.getValue());
        }
        jsonWriter.writeEndObject();

        return jsonWriter;
    }

    private GsonJsonElementUtils() {
    }
}
