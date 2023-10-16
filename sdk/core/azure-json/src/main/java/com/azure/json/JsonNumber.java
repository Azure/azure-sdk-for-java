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
     */
    JsonNumber(String value) {
        try {
            this.numberValue = Integer.parseInt(value);
        } catch (Exception e) {
            try {
                this.numberValue = Float.parseFloat(value);
            } catch (Exception x) {
                x.printStackTrace();
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

    /**
     * Returns the String representation of the JsonNumber object
     *
     * @return the numberValue field which is a String representation of the
     * current state of this JsonNumber object.
     */
    @Override
    public String toString() {
        return this.numberValue.toString();
    }

    /**
     * @return boolean of whether this JsonElement object is of type JsonNumber.
     */
    @Override
    public boolean isNumber() {
        return true;
    }

    /*

    Methods to be defined:

    public boolean isInteger() { }
    public boolean isFloat() {}
    public boolean isPositive() {}
    public boolean isNegative() {}

    */

    @Override
    public JsonArray asArray() {
        JsonArray output = new JsonArray();
        output.addElement(this);
        return output;
    }

    @Override
    public JsonObject asObject() {
        JsonObject output = new JsonObject();
        output.setProperty("Value", this);
        return output;
    }

    @Override
    public JsonBoolean asBoolean() {
        try {
            if (numberValue.floatValue() == 1) {
                return JsonBoolean.getInstance(true);
            } else {
                return JsonBoolean.getInstance(false);
            }
        } catch (NullPointerException e) {
            return JsonBoolean.getInstance(true);
        }
    }

    @Override
    public JsonNumber asNumber() {
        return this;
    }

    @Override
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
     * TODO: this needs to be extended to consider all of the number values
     */
    @Override
    public JsonWriter serialize(JsonWriter jsonWriter) throws IOException {

        jsonWriter.writeNumber(numberValue);

        //        int integerForm = this.numberValue.intValue();
//        float floatForm = this.numberValue.floatValue();
//        if (integerForm == floatForm) {
//            jsonWriter.writeInt(integerForm);
//        } else {
//            jsonWriter.writeFloat(floatForm);
//        }
        return jsonWriter;
    }
}
