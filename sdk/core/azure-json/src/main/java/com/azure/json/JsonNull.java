package com.azure.json;

import java.io.IOException;

/**
 * Class representing the JSON null type
 */
public class JsonNull extends JsonElement{
    /**
     * Stores the String representation of the current state of the JsonNull
     * object.
     * Always set to "null". Cannot be changed.
     */
    private final String nullValue = "null";

    /**
     * Default constructor.
     * 
     * TODO: may need to remove this due to design guidelines? Unnecessary having
     * this constructor defined in the source code if compiler is already adding
     * this constructor implicitly when no other constructor is defined.
     */
    public JsonNull() {}

    /**
     * Returns the String representation of the JsonNull object
     *
     * @return the nullValue field which is a String representation of the
     * current state of this JsonNull object.
     */
    @Override
    public String toString() { return this.nullValue; }

    /**
     * @return boolean of whether this JsonElement object is of type JsonNull.
     */
    @Override
    public boolean isNull() { return true; }

    /** 
     * @param jsonWriter JsonWriter that the serialized JsonNull is written to. 
     * @return JsonWriter state after the serialized JsonNull has been written 
     * to it. 
     * @throws IOException Thrown when JsonWriter.writeNull call throws an 
     * IOException. 
     */
    @Override
    public JsonWriter serialize(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeNull();
        return jsonWriter;
    }
}
