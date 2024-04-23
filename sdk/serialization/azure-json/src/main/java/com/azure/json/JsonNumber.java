// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import java.io.IOException;

/**
 * Class representing the JSON number type
 */
public final class JsonNumber extends JsonElement {
    /**
     * Stores the String representation of the current state of the JsonNumber
     * object.
     */
    private final Number numberValue;

    /**
     * Constructor used to explicitly set the number value of the JsonNumber
     * object via a String
     *
     * @param value specifies the String storing the number this JsonNumber
     * object represents
     * @throws IllegalArgumentException Thrown when the String value does not
     * represent a parseable int or float value.
     */
    JsonNumber(String value) throws IllegalArgumentException {
        Number number;
        try {
            number = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            try {
                number = Float.parseFloat(value);
            } catch (NumberFormatException x) {
                x.addSuppressed(e);
                throw new IllegalArgumentException(
                    "JsonNumber object must be constructed from a parseable int or float value.", x);
            }
        }

        this.numberValue = number;
    }

    /**
     * Constructor used to explicitly set the number value of the JsonNumber
     * object via a Number
     *
     * @param value Specifies the number this JsonNumber object represents
     * @throws IllegalArgumentException Thrown when the value is null.
     */
    public JsonNumber(Number value) throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException("JsonNumber object cannot be constructed with a null value.");
        }
        this.numberValue = value;
    }

    /**
     * Returns the Number value from a JsonNumber object.
     *
     * @return The Number value.
     */
    public Number getNumberValue() {
        return this.numberValue;
    }

    /**
     * Returns the String representation of the JsonNumber object
     *
     * @return the numberValue field which is a String representation of the
     * current state of this JsonNumber object.
     */
    @Override
    public String toString() {
        return numberValue.toString();
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

    /**
     * Represents JsonNumber as a JsonBoolean.
     *
     * @return JsonBoolean representation of the JsonNumber object.
     * @throws IOException Thrown when the numberValue is not 1 or 0.
     */
    public JsonBoolean asBoolean() throws IOException {
        if (numberValue.floatValue() == 1) {
            return JsonBoolean.getInstance(true);
        } else if (numberValue.floatValue() == 0) {
            return JsonBoolean.getInstance(false);
        } else {
            throw new IOException();
        }
    }

    /**
     * Represents JsonNumber as a JsonString.
     *
     * @return JsonString representation of the JsonNumber object.
     */
    public JsonString asString() {
        return new JsonString(numberValue.toString());
    }

    /**
     * @param jsonWriter JsonWriter that the serialized JsonNumber is written to.
     * @return JsonWriter state after the serialized JsonNumber has been written
     * to it.
     * @throws IOException Thrown when JsonWriter.write* calls throw an IOException.
     *
     */
    @Override
    public JsonWriter serialize(JsonWriter jsonWriter) throws IOException {

        jsonWriter.writeNumber(numberValue);

        return jsonWriter;
    }
}
