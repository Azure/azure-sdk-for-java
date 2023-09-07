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
    private String numberValue;

    /**
     * Default constructor.
     * Default sets numberValue to "0" through the other constructor.
     * 
     * TODO: may need to remove this due to design guidelines? May only want to
     * have the public JsonNumber(Number value) constructor.
     * 
     * TODO: may need to double check that 0 is correctly cast to a Number type
     */
    public JsonNumber() { this(0); }

    public JsonNumber(String value) { this.numberValue = value; }

    /**
     * Constructor used to explicitly set the number value of the JsonNumber object
     *
     * @param value specifies the number this JsonNumber object represents
     * 
     * TODO: check for invalid number values or types
     */
    public JsonNumber(Number value) { this.numberValue = value.toString(); }

    /**
     * Returns the String representation of the JsonNumber object
     *
     * @return the numberValue field which is a String representation of the
     * current state of this JsonNumber object.
     */
    @Override
    public String toString() { return this.numberValue; }

    /**
     * @return boolean of whether this JsonElement object is of type JsonNumber.
     */
    @Override
    public boolean isNumber() { return true; }

    /*

    Methods to be defined:

    public boolean isInteger() { }
    public boolean isFloat() {}
    public boolean isPositive() {}
    public boolean isNegative() {}

    */

    /** 
     * @param jsonWriter JsonWriter that the serialized JsonNumber is written to. 
     * @return JsonWriter state after the serialized JsonNumber has been written 
     * to it. 
     * @throws IOException Thrown when JsonWriter.write* calls throw an IOException. 
     * 
     * TODO: this needs to be extended to consider all of the number values
     */
    @Override
    public JsonWriter serialize(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeInt(Integer.parseInt(this.numberValue));
        return jsonWriter;
    }
}
