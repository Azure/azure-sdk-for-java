package com.azure.json;

import java.io.IOException;

/**
 * Class representing the JSON string type
 */
public class JsonString extends JsonElement{
    /**
     * Stores the String representation of the current state of the JsonString
     * object.
     */
    private String stringValue;

    /**
     * Default constructor.
     *
     * TODO: may need to remove this due to design guidelines? May only want to
     * have the public JsonString(String value) constructor.
     */
    public JsonString() {}

    /**
     * Constructor used to explicitly set the string value of the JsonString object.
     * Adds the starting and ending double quotation marks.
     *
     * TODO: may have errors occur when value passed has explicilty included other
     * quotation marks through escape characters.
     *
     * @param value specifies the text string this JsonString object represents
     */
    public JsonString(String value) { this.stringValue = value; }

    /**
     * Returns the String representation of the JsonString object
     *
     * @return the stringValue field which is a String representation of the
     * current state of this JsonString object.
     */
    @Override
    public String toString() { return this.stringValue; }

    /**
     * @return boolean of whether this JsonElement object is of type JsonString.
     */
    @Override
    public boolean isString() { return true; };


    @Override
    public JsonArray asArray() {
        JsonArray output = new JsonArray();
        output.addElement(this);
        return output;
    }

    @Override
    public JsonObject asObject() {
        JsonObject output = new JsonObject();
        output.addProperty("Value", this);
        return output;
    }

    @Override
    public JsonBoolean asBoolean() {
        try {
            int a = Integer.parseInt(stringValue);
            if (a == 1){
                return new JsonBoolean(true);
            } else {
                return new JsonBoolean(false);
            }
        } catch (NumberFormatException e){
            return new JsonBoolean(Boolean.parseBoolean(stringValue));
        }
    }

    @Override
    public JsonNumber asNumber() {
        try {
            return new JsonNumber(Integer.parseInt(stringValue));
        } catch (NullPointerException e){
            try {
                return new JsonNumber(Float.parseFloat(stringValue));
            }catch (NullPointerException x){
                return new JsonNumber();
            }
        } catch (NumberFormatException z){
            return new JsonNumber();
        }
    }

    @Override
    public JsonString asString() { return this; }





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
