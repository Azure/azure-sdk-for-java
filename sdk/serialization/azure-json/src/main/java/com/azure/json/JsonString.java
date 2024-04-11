// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import java.io.IOException;

/**
 * Class representing the JSON string type
 */
public final class JsonString extends JsonElement {
    /**
     * Stores the String representation of the current state of the JsonString
     * object.
     */
    private final String stringValue;

    /**
     * Constructor used to explicitly set the string value of the JsonString object.
     * Adds the starting and ending double quotation marks.
     *
     * @param value specifies the text string this JsonString object represents
     */
    public JsonString(String value) {
        this.stringValue = value;
    }

    /**
     * Returns the String representation of the JsonString object
     *
     * @return the stringValue field which is a String representation of the
     * current state of this JsonString object.
     */
    @Override
    public String toString() {
        return this.stringValue;
    }

    /**
     * @return boolean of whether this JsonElement object is of type JsonString.
     */
    @Override
    public boolean isString() {
        return true;
    }

    /**
     * Represents JsonString as a JsonBoolean.
     *
     * @return JsonBoolean representation of the JsonString object.
     * @throws IOException Thrown when the stringValue is not a valid boolean.
     */
    public JsonBoolean asBoolean() throws IOException {

        try {
            int a = Integer.parseInt(stringValue);
            if (a == 1) {
                return JsonBoolean.getInstance(true);
            } else {
                return JsonBoolean.getInstance(false);
            }
        } catch (NumberFormatException e) {
            if ("true".equalsIgnoreCase(stringValue)) {
                return JsonBoolean.getInstance(true);
            } else if ("false".equalsIgnoreCase(stringValue)) {
                return JsonBoolean.getInstance(false);
            } else {
                throw new IOException(e);
            }
        }
    }

    /**
     * Represents JsonString as a JsonNumber.
     *
     * @return JsonNumber representation of the JsonString object.
     * @throws IOException Thrown when the stringValue is not a valid number.
     */
    public JsonNumber asNumber() throws IOException {
        try {
            return new JsonNumber(Integer.parseInt(stringValue));
        } catch (NumberFormatException ignored) {
        }

        try {
            return new JsonNumber(Float.parseFloat(stringValue));
        } catch (NumberFormatException z) {
            throw new IOException("Can't convert to valid number", z);
        }

    }

    /**
     * @param jsonWriter JsonWriter that the serialized JsonString is written to.
     * @return JsonWriter state after the serialized JsonString has been written
     * to it.
     * @throws IOException Thrown when JsonWriter.writeString calls throw an IOException.
     */
    @Override
    public JsonWriter serialize(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeString(this.stringValue);
        return jsonWriter;
    }
}
