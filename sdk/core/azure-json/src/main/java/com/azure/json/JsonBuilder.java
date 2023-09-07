package com.azure.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import static com.azure.json.JsonToken.END_DOCUMENT;

/** 
 * probably unnecessary, but I'm leaving it in for now
 * The thinking was should we consider hiding all the elements in the implementation
 * and only exposing the JsonBuilder class to the user? It may be good to prevent people
 * making random JsonElements without a proper container like Object or Array,
 * but it may also make it harder to use.
 */
public class JsonBuilder {
    /**
     * 
     * @return
     */
    public JsonArray createArray() {
        return new JsonArray();
    }

    /**
     * 
     * @return
     */
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

    /**
     * 
     * @param json
     * @return
     * @throws IOException
     */
    public JsonElement deserialize(byte[] json) throws IOException {
        return this.buildOutput(JsonProviders.createReader(json));
    }

    /**
     * 
     * @param json
     * @return
     * @throws IOException
     */
    public JsonElement deserialize(InputStream json) throws IOException {
        return this.buildOutput(JsonProviders.createReader(json));
    }

    /**
     * 
     * @param json
     * @return
     * @throws IOException
     */
    public JsonElement deserialize(Reader json) throws IOException {
        return this.buildOutput(JsonProviders.createReader(json));
    }

    /**
     * This method is used to build the output JsonElement from the JsonReader
     * 
     * @param reader
     * @return
     * @throws IOException if the JSON string is invalid 
     * 
     * TODO: should buildOutput only build one top level JsonArray or JsonObject 
     * then break out of the while loop? Current implementation enables the 
     * ability to have multiple top level JsonArray and JsonObjects. For example, 
     * the following grouped top level JSON objects and JSON arrays would be 
     * successfully built with our current implementation: 
     *      {
     *        "key1":"value1",
     *        ...,
     *        "keyN":"valueN"
     *      }
     *      {
     *        "key1":"value1",
     *        ...,
     *        "keyN":"valueN"
     *      }
     *      ["val1", "val2", ..., "valN"]
     */
    private JsonElement buildOutput(JsonReader reader) throws IOException {
        // Stores resulting deserialised JSON from reader that will be returned. 
        JsonElement output = null;

        JsonToken token = reader.nextToken();

        while (token != END_DOCUMENT) {
            switch (token) {
                // Case: deserialising top level JSON array 
                case START_ARRAY:
                    output = new JsonArray(reader);
                    break;
                // Case: deserialising top level JSON object  
                case START_OBJECT:
                    output = new JsonObject(reader);
                    break; 
                // Invalid JsonToken token cases: 
                // NOTE: previous comment mentioned "In theory the reader takes care of this" 
                case END_ARRAY: 
                    throw new IOException("Invalid JsonToken.END_ARRAY token read from deserialised JSON. Deserialisation aborted."); 
                case END_OBJECT: 
                    throw new IOException("Invalid JsonToken.END_OBJECT token read from deserialised JSON. Deserialisation aborted."); 
                case FIELD_NAME: 
                    throw new IOException("Invalid JsonToken.FIELD_NAME token read from deserialised JSON. Deserialisation aborted."); 
                case STRING: 
                    throw new IOException("Invalid JsonToken.STRING token read from deserialised JSON. Deserialisation aborted."); 
                case NUMBER: 
                    throw new IOException("Invalid JsonToken.NUMBER token read from deserialised JSON. Deserialisation aborted."); 
                case BOOLEAN: 
                    throw new IOException("Invalid JsonToken.BOOLEAN token read from deserialised JSON. Deserialisation aborted."); 
                case NULL: 
                    throw new IOException("Invalid JsonToken.NULL token read from deserialised JSON. Deserialisation aborted."); 
                // Case: this should never be the case because of the while condition. 
                case END_DOCUMENT: 
                    throw new IOException("Invalid JsonToken.END_DOCUMENT token read from deserialised JSON. Deserialisation aborted."); 
                // Case: tokens read from JsonReader must be JsonTokens. This 
                // default case would only succeed if an unknown token type is 
                // encountered. 
                default: 
                    throw new IOException("Invalid token deserialised from JsonReader. Deserialisation aborted."); 
            }
            token = reader.nextToken();
        }
        return output;
    }
}
