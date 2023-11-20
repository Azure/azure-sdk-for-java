package com.azure.json;

import java.io.IOException;

/**
 * Class representing the JSON number type
 */
public class JsonNumber extends JsonElement {
    /**
     * Stores the String representation of the current state of the JsonNumber
     * object.
     */
    private Number numberValue;

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
        try {
            this.numberValue = Integer.parseInt(value);
        } catch (Exception e) {
            try {
                this.numberValue = Float.parseFloat(value);
            } catch (Exception x) {
                throw new IllegalArgumentException("JsonNumber object must be constructed from a parseable int or float value.");
            }
        }
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

    public Number getNumberValue(){ return this.numberValue;}

    /**
     * Returns the String representation of the JsonNumber object
     *
     * @return the numberValue field which is a String representation of the
     * current state of this JsonNumber object.
     */
    @Override
    public String toString() {
        try {
            return this.numberValue.toString();
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * @return boolean of whether this JsonElement object is of type JsonNumber.
     */
    @Override
    public boolean isNumber() {
        return true;
    }


    public JsonBoolean asBoolean() throws IOException {
        if (numberValue.floatValue() == 1) {
            return JsonBoolean.getInstance(true);
        } else if (numberValue.floatValue() == 0) {
            return JsonBoolean.getInstance(false);
        } else {
            throw new IOException();
        }
    }

    public JsonString asString() {
        try {
            return new JsonString(numberValue.toString());
        } catch (NullPointerException e) {
            return new JsonString("");
        }
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
