package com.azure.json;

import java.io.IOException;

/**
 * Class representing the JSON boolean type
 */
public class JsonBoolean extends JsonElement {
    /**
     * Stores the String representation of the JsonBoolean object.
     * Can only be either "true" or "false".
     */
    private String booleanValue;

    /**
     * Default constructor
     * Default sets booleanValue to "true" through the other constructor.
     * 
     * TODO: may need to remove this due to design guidelines? May only want to
     * have the public JsonBoolean(boolean value) constructor.
     */
    public JsonBoolean() { this(true); }

    /**
     * Constructor used to set the value of the JsonBoolean.
     *
     * @param value the boolean value to set the JsonBoolean object to. Either
     * true or false. If value is true, then booleanValue set to "true"; otherwise,
     * set to "false"
     */
    public JsonBoolean(boolean value) { this.booleanValue = (value)? "true" : "false"; }

    /**
     * Returns the String representation of the JsonBoolean object
     *
     * @return the booleanValue field which is a String representation of the
     * current state of this JsonBoolean object.
     */
    @Override
    public String toString() { return this.booleanValue; }

    /**
     * @return boolean of whether this JsonElement object is of type JsonBoolean.
     */
    @Override
    public boolean isBoolean() { return true; }

    /**
     * @return boolean of whether this JsonBoolean object is currently representing
     * true
     */
    public boolean isTrue() { return this.booleanValue.equals("true"); }

    /**
     * @return boolean of whether this JsonBoolean object is currently representing
     * false
     */
    public boolean isFalse() { return this.booleanValue.equals("false"); }

    /** 
     * @param jsonWriter JsonWriter that the serialised JsonBoolean is written to. 
     * @return JsonWriter state after the serialised JsonBoolean has been 
     * written to it. 
     * @throws IOException Thrown when JsonWriter.writeBoolean call throws an 
     * IOException. 
     */
    @Override
    public JsonWriter serialize(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeBoolean(Boolean.parseBoolean(this.booleanValue));
        return jsonWriter;
    }
}
