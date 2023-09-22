package com.azure.json;

import java.io.IOException;

/**
 * Class representing the JSON boolean type
 */
public final class JsonBoolean extends JsonElement {
    /**
     * Stores the String representation of the JsonBoolean object.
     * Can only be either "true" or "false".
     */
    private boolean booleanValue;

    /**
     * Default constructor
     * Default sets booleanValue to "true" through the other constructor.
     *
     * TODO: may need to remove this due to design guidelines? May only want to
     * have the public JsonBoolean(boolean value) constructor.
     */

    /**
     * Constructor used to set the value of the JsonBoolean.
     *
     * @param value the boolean value to set the JsonBoolean object to. Either
     * true or false. If value is true, then booleanValue set to "true"; otherwise,
     * set to "false"
     */
    private JsonBoolean(boolean value) { 
        this.booleanValue = value; 
    }

    /**
     * Lazy-loading the Singleton instances of JsonBoolean of TRUE & FALSE.
     */
    private static class LoadSingleton {
        static final JsonBoolean TRUE = new JsonBoolean(true);
        static final JsonBoolean FALSE = new JsonBoolean(false);
    }

    /**
     * Returns the Singleton instance of JsonBoolean for the specified boolean value.
     *
     * @param value The boolean value for which to obtain the JsonBoolean instance.
     * @return The Singleton instance of JsonBoolean representing the specified boolean value.
     */
    public static JsonBoolean getInstance(boolean value) {
        return (value) ? LoadSingleton.TRUE : LoadSingleton.FALSE;
    }

    /**
     * Returns the String representation of the JsonBoolean object
     *
     * @return the booleanValue field which is a String representation of the
     * current state of this JsonBoolean object.
     */
    @Override
    public String toString() { 
        return Boolean.toString(booleanValue); 
    }

    /**
     * @return boolean of whether this JsonElement object is of type JsonBoolean.
     */
    @Override
    public boolean isBoolean() { 
        return true; 
    }

    /**
     * @return boolean of whether this JsonBoolean object is currently representing
     * true
     */
    public boolean isTrue() { 
        return booleanValue; 
    }

    /**
     * @return boolean of whether this JsonBoolean object is currently representing
     * false
     */
    public boolean isFalse() { 
        return !booleanValue; 
    }

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
        return this; 
    }

    @Override
    public JsonNumber asNumber() {
        if(booleanValue){
            return new JsonNumber(1);
        } else {
            return new JsonNumber(0);
        }
    }

    @Override
    public JsonString asString() { 
        return new JsonString(Boolean.toString(booleanValue)); 
    }
    
    /**
     * @param jsonWriter JsonWriter that the serialized JsonBoolean is written to.
     * @return JsonWriter state after the serialized JsonBoolean has been
     * written to it.
     * @throws IOException Thrown when JsonWriter.writeBoolean call throws an
     * IOException.
     */
    @Override
    public JsonWriter serialize(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeBoolean(Boolean.parseBoolean(Boolean.toString(booleanValue)));
        return jsonWriter;
    }
}
