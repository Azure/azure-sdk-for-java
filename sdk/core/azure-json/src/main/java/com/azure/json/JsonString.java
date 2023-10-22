package com.azure.json;

import java.io.IOException;

/**
 * Class representing the JSON string type
 */
public class JsonString extends JsonElement {
    /**
     * Stores the String representation of the current state of the JsonString
     * object.
     */
    private final String stringValue;


    /**
     * Constructor used to explicitly set the string value of the JsonString object.
     * Adds the starting and ending double quotation marks.
     *
     * TODO: may have errors occur when value passed has explicitly included other
     * quotation marks through escape characters.
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

    public JsonBoolean asBoolean() throws IOException {

        try {
            int a = Integer.parseInt(stringValue);
            if (a == 1) {
                return JsonBoolean.getInstance(true);
            } else {
                return JsonBoolean.getInstance(false);
            }
        } catch (NumberFormatException e) {
            if (stringValue.equals("true")){
                return JsonBoolean.getInstance(true);
            } else if (stringValue.equals("false")){
                return JsonBoolean.getInstance(false);
            } else {
                throw new IOException();
            }
        }
    }

    public JsonNumber asNumber() throws IOException {
        try {
            return new JsonNumber(Integer.parseInt(stringValue));
        } catch (NumberFormatException ignored) {

        } catch (NullPointerException e){
            throw new IOException("Input was null");
        }
        try {
            return new JsonNumber(Float.parseFloat(stringValue));
        } catch (NumberFormatException z) {
            throw new IOException("Can't convert to valid number");
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
