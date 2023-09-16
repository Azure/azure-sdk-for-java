package com.azure.json;

import java.io.IOException;

/**
 * Class representing the JSON null type
 */
public final class JsonNull extends JsonElement {
    
    // Private constructor enforcing Singleton pattern.
    private JsonNull() { }

    /**
     * Helper class to hold Singleton instance.
     * - Thread-safe lazy-initialization of the JsonNull object without explicit 
     * synchronization.
     */
    private static class LoadSingleton {
        private static final JsonNull INSTANCE = new JsonNull();
    }

    /**
     * Returns the single instance of the JsonNull class.
     * 
     * @return The JsonNull instance, representing the JsonNull Object.
     */
    public static JsonNull getInstance() { return LoadSingleton.INSTANCE; }

    /**
     * Stores the String representation of the current state of the JsonNull
     * object.
     * Always set to "null". Cannot be changed.
     */
    private final String nullValue = "null";

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
