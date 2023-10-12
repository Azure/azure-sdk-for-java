// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import java.io.IOException;

/**
 * Represents JSON boolean values.
 * 
 * @see JsonElement
 */
public final class JsonBoolean extends JsonElement {
    private static final JsonBoolean TRUE = new JsonBoolean(true);
    private static final JsonBoolean FALSE = new JsonBoolean(false);
    private final boolean booleanValue;

    private JsonBoolean(boolean value) { 
        this.booleanValue = value; 
    }

    /**
     * Returns the instance of JsonBoolean for the specified boolean value.
     * <p>
     * TODO Javadoc comment
     *
     * @param value The boolean value for which to obtain the JsonBoolean instance.
     * @return The instance of JsonBoolean representing the specified boolean value.
     */
    public static JsonBoolean getInstance(boolean value) {
        return value ? TRUE : FALSE;
    }

    /**
     * Returns boolean value from a JsonBoolean object.
     * 
     * @return The boolean value.
     */
    public boolean getBooleanValue() {
        return booleanValue;
    }

    /**
     * Returns the String representation of a JsonBoolean object.
     *
     * @return the booleanValue field which is a String representation of the
     * current state of this JsonBoolean object.
     */
    @Override
    public String toString() { 
        return Boolean.toString(booleanValue); 
    }

    /**
     * Identifies if an object is of type JsonBoolean. 
     * 
     * @return boolean of whether this JsonElement object is of type JsonBoolean.
     */
    @Override
    public boolean isBoolean() { 
        return true; 
    }
    
    /**
     * JsonBoolean as an element in a JsonArray.
     * <p>
     * TODO Javadoc comment
     * 
     * @return A JsonArray with a JsonBoolean instance as its first element.
     */
    @Override
    public JsonArray asArray() {
        JsonArray output = new JsonArray();
        output.addElement(this);
        return output;
    }

    /**
     * JsonBoolean as a property in a JsonObject.
     * <p>
     * TODO Javadoc comment
     * 
     * @return The JsonObject with the JsonBoolean value as a property.
     */
    @Override
    public JsonObject asObject() {
        JsonObject output = new JsonObject();
        output.setProperty("Value", this);
        return output;
    }

    /**
     * The current JsonBoolean object instance.
     * <p>
     * TODO Javadoc comment
     * 
     * @return The current instance of JsonBoolean object.
     */
    @Override
    public JsonBoolean asBoolean() { 
        return this; 
    }

    /**
     * Represents JsonBoolean as a JsonNumber.
     * <p>
     * Truth values true and false respectively, 1 or 0.
     * TODO Javadoc comment
     * 
     * @return The JsonNumber representation of a JsonBoolean, 0 or 1.
     */
    @Override
    public JsonNumber asNumber() {
        if (booleanValue) {
            return new JsonNumber(1);
        } else {
            return new JsonNumber(0);
        }
    }

    /**
     * Converts JsonBoolean value to type JsonString.
     * 
     * @return The JsonString representation of a JsonBoolean object.
     */
    @Override
    public JsonString asString() { 
        return new JsonString(Boolean.toString(booleanValue)); 
    }
    
    /**
     * Writes the JsonSerializable object JsonBoolean. 
     * 
     * @param jsonWriter JsonWriter that the serialized JsonBoolean is written to.
     * @return JsonWriter state after the serialized JsonBoolean has been written to it.
     * @throws IOException Thrown when JsonWriter.writeBoolean call throws an IOException.
     */
    @Override
    public JsonWriter serialize(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeBoolean(booleanValue);
        return jsonWriter;
    }
}
