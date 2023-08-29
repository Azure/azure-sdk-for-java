package com.azure.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import static com.azure.json.JsonToken.END_DOCUMENT;

public class JsonBuilder {


    /** probably unnecessary, but I'm leaving it in for now
     * The thinking was should we consider hiding all the elements in the implementation
     * and only exposing the JsonBuilder class to the user? It may be good to prevent people
     * making random JsonElements without a proper container like Object or Array,
     * but it may also make it harder to use.
     * */

    public JsonArray createArray() {
        return new JsonArray();
    }

    public JsonObject createObject() {
        return new JsonObject();
    }

    /**
     * Deserialize a JSON string into a JsonElement. There are overloads for byte[] arrays, InputStreams, and Readers
     * these are all valid dependency injections for JsonReader
     * @param json is a valid JSON string
     * @return a JsonElement that will contain an object or array with properties, and nested structures
     * ready to interact with
     * @throws IOException if the JSON string is invalid
     */
    public JsonElement deserialize(String json) throws IOException {
        return this.buildOutput(JsonProviders.createReader(json));
    }

    public JsonElement deserialize(byte[] json) throws IOException {
        return this.buildOutput(JsonProviders.createReader(json));
    }

    public JsonElement deserialize(InputStream json) throws IOException {
        return this.buildOutput(JsonProviders.createReader(json));
    }

    public JsonElement deserialize(Reader json) throws IOException {
        return this.buildOutput(JsonProviders.createReader(json));
    }

    /**
     * This method is used to build the output JsonElement from the JsonReader
     * @throws IOException if the JSON string is invalid
     */

    private JsonDataStructure buildOutput(JsonReader reader) throws IOException {
        JsonToken token = reader.nextToken();
        JsonDataStructure output = null;
        while (token != END_DOCUMENT) {
            //todo remove this debug line, and remove the comment out cases
            //System.out.printf("Token: %s%n", token);
            switch (token) {
                // I'm leaving in all the options in this switch commented out to make sure I don't miss anything,
                // but at this point anything other than opening a new object or array is an error.
                // In theory the reader takes care of this

                case START_ARRAY:
                    output = new JsonArray(reader);
                    break;
//                case END_ARRAY:
//                    break;
                case START_OBJECT:
                    output = new JsonObject(reader);
                    break;
//                case END_OBJECT:
//                    break;
//                case FIELD_NAME:
//                    break;
//                case STRING:
//                    break;
//                case NUMBER:
//                    break;
//                case BOOLEAN:
//                    break;
//                case NULL:
//                    break;
                case END_DOCUMENT:
                    break;
                default:
                    break;
                // If we get here then we have an unknown token type - which should not occur...
            }

            token = reader.nextToken();
        }

        return output;
    }
}
