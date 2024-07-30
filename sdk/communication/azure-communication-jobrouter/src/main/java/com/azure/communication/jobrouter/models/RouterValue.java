// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * Wrapper class for labels. Supports String, int, double and boolean types.
 * <p>
 * If multiple values are set only one value will be used with following precedence.
 * <p>
 * 1. stringValue.
 * 2. intValue.
 * 3. doubleValue.
 * 4. boolValue.
 */
@Immutable
public final class RouterValue implements JsonSerializable<RouterValue> {

    /**
     * String Value to pass to server.
     */
    private final String stringValue;

    /**
     * Integer Value to pass to server.
     */
    private final Integer intValue;

    /**
     * Double Value to pass to server.
     */
    private final Double doubleValue;

    /**
     * Boolean Value to pass to server.
     */
    private final Boolean boolValue;

    /**
     * Constructor
     * @param stringValue stringValue.
     * @param intValue intValue.
     * @param doubleValue doubleValue.
     * @param boolValue boolValue.
     */
    RouterValue(String stringValue, Integer intValue, Double doubleValue, Boolean boolValue) {
        this.stringValue = stringValue;
        this.intValue = intValue;
        this.doubleValue = doubleValue;
        this.boolValue = boolValue;
    }

    /**
     * String constructor.
     * @param stringValue stringValue.
     */
    public RouterValue(String stringValue) {
        this(stringValue, null, null, null);
    }

    /**
     * Constructor for intValue.
     * @param intValue intValue.
     */
    public RouterValue(Integer intValue) {
        this(null, intValue, null, null);
    }

    /**
     * DoubleValue constructor.
     * @param doubleValue doubleValue.
     */
    public RouterValue(Double doubleValue) {
        this(null, null, doubleValue, null);
    }

    /**
     * BoolValue constructor.
     * @param boolValue boolValue
     */
    public RouterValue(Boolean boolValue) {
        this(null, null, null, boolValue);
    }

    /**
     * Returns stringValue
     * @return stringValue.
     */
    public String getStringValue() {
        return stringValue;
    }

    /**
     * Returns intValue.
     * @return intValue
     */
    public Integer getIntValue() {
        return intValue;
    }

    /**
     * Returns doubleValue.
     * @return doubleValue
     */
    public Double getDoubleValue() {
        return doubleValue;
    }

    /**
     * Returns boolValue.
     * @return boolValue.
     */
    public Boolean getBooleanValue() {
        return boolValue;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        if (stringValue != null) {
            return jsonWriter.writeString(stringValue);
        } else if (intValue != null) {
            return jsonWriter.writeInt(intValue);
        } else if (doubleValue != null) {
            return jsonWriter.writeDouble(doubleValue);
        } else if (boolValue != null) {
            return jsonWriter.writeBoolean((boolean) boolValue);
        } else {
            return jsonWriter.writeNull();
        }
    }

    /**
     * Deserializes an instance of RouterValue from the JSON content.
     * <p>
     * RouterValue deserializes from a JSON value, which can be a JSON string, number, boolean, or null.
     *
     * @param jsonReader The JSON reader to deserialize from.
     * @return An instance of RouterValue.
     * @throws IOException If there is an error while reading JSON content.
     */
    public static RouterValue fromJson(JsonReader jsonReader) throws IOException {
        JsonToken currentToken = jsonReader.currentToken();
        if (currentToken == null) {
            currentToken = jsonReader.nextToken();
        }

        if (currentToken == JsonToken.STRING) {
            return new RouterValue(jsonReader.getString());
        } else if (currentToken == JsonToken.NUMBER) {
            String rawNumber = jsonReader.getRawText();
            try {
                return new RouterValue(Integer.parseInt(rawNumber));
            } catch (NumberFormatException ignored) {
                return new RouterValue(Double.parseDouble(rawNumber));
            }
        } else if (currentToken == JsonToken.BOOLEAN) {
            return new RouterValue(jsonReader.getBoolean());
        } else {
            // null or new RouterValue(null, null, null, null)?
            return null;
        }
    }
}
