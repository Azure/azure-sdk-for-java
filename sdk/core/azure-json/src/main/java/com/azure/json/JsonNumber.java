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
     * Default constructor.
     * Default sets numberValue to "0" through the other constructor.
     * TODO: may need to remove this due to design guidelines? May only want to
     * have the public JsonNumber(Number value) constructor.
     * TODO: may need to double check that 0 is correctly cast to a Number type
     */
    JsonNumber() { }//this(0); }

    //TODO remove this commented code
//    public JsonNumber(String value) {
//        try {
//            this.numberValue = Integer.parseInt(value);
//        } catch (Exception e) {
//            try {
//                this.numberValue = Float.parseFloat(value);
//            } catch (Exception x) {
//                x.printStackTrace();
//            }
//        }
//    }

    /**
     * Constructor used to explicitly set the number value of the JsonNumber object
     *
     * @param value specifies the number this JsonNumber object represents
     *
     * TODO: check for invalid number values or types
     */
    public JsonNumber(Number value) {
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
        output.addProperty("Value", this);
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
     * TODO: remove commented out code when confirmed that it is not needed.
     */
//    @Override
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
