package com.azure.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Class representing the JSON array type
 */
public class JsonArray extends JsonElement {
    /**
     * Stores the JsonElements nested in this JsonArray object.
     * Each of these elements should be one of the following valid JSON types:
     * object, array, string, number, boolean, and null.
     */
    private List<JsonElement> elements = new ArrayList<>(0);

    /**
     * Default constructor. 
     */
    public JsonArray() {
        super();
    }

    /**
     * Constructor used to construct JsonArray from a JsonReader. 
     * 
     * If the developer knows they want to build an array from JSON, then they 
     * can bypass the JsonBuilder and just use this constructor directly. 
     * 
     * @param reader The opened JsonReader to construct the JsonArray object from. 
     * @throws IOException Thrown when the build method call throws an IOException. 
     */
    public JsonArray(JsonReader reader) throws IOException {
        super();
        this.build(reader);
    }

    /**
     * Adds a JsonElement object as a nested element within the JsonArray object,
     * appending it to the end.
     *
     * @param element a JsonElement object representing one of the valid JSON
     * types: object, array, string, number, boolean, and null
     * @return JsonArray object representing the new state of the JsonArray object
     * after the addition of the new JsonElement object appended.
     * @throws IllegalArgumentException Thrown when a null JsonElement is passed. 
     * Instantiated JsonElements must be passed. Must pass instantiated JsonNull 
     * to represent JSON null value. 
     */
    public JsonArray addElement(JsonElement element) throws IllegalArgumentException {
        if (element == null) { 
            throw new IllegalArgumentException("Cannot add a null JsonElement to the JsonArray. Use a JsonNull object to represent a valid JSON null value."); 
        }
        this.elements.add(element);
        return this;
    }

    /**
     * Adds a JsonElement object as a nested element within the JsonArray object
     * at a particular index, shifting the existing elements from that index to
     * the right.
     *
     * @param element a JsonElement object representing one of the valid JSON
     * types: object, array, string, number, boolean, and null
     * @param index the element index within the JsonArray to add the new JsonElement
     * object and where to shift from
     * @return JsonArray object representing the new state of the JsonArray object
     * after the addition of the new JsonElement object.
     * @throws IllegalArgumentException Thrown when a null JsonElement is passed. 
     * Instantiated JsonElements must be passed. Must pass instantiated JsonNull 
     * to represent JSON null value. 
     * @throws IndexOutOfBoundsException Thrown when index parameter is < 0 or 
     * > size() 
     */
    public JsonArray addElement(int index, JsonElement element) throws IllegalArgumentException, IndexOutOfBoundsException {
        if (element == null) { 
            throw new IllegalArgumentException("Cannot add a null JsonElement to the JsonArray. Use a JsonNull object to represent a valid JSON null value."); 
        }
        this.elements.add(index, element);
        return this;
    }

    /**
     * Sets the index of the JsonArray to the specified JsonElement object,
     * replacing the existing JsonElement object at this index.
     *
     * @param element a JsonElement object representing one of the valid JSON
     * types: object, array, string, number, boolean, and null
     * @param index the element index within the JsonArray to replace the current
     * JsonElement with the newly specified JsonElement object
     * @return JsonArray object representing the new state of the JsonArray object
     * after the setting of the new JsonElement object at index.
     * @throws IllegalArgumentException Thrown when a null JsonElement is passed. 
     * Instantiated JsonElements must be passed. Must pass instantiated JsonNull 
     * to represent JSON null value. 
     * @throws IndexOutOfBoundsException Thrown when index parameter is < 0 or 
     * >= size() 
     */
    public JsonArray setElement(int index, JsonElement element) throws IllegalArgumentException, IndexOutOfBoundsException {
        if (element == null) { 
            throw new IllegalArgumentException("Cannot set a null JsonElement in the JsonArray. Use a JsonNull object to represent a valid JSON null value."); 
        }
        this.elements.set(index, element);
        return this;
    }

    /**
     * Returns a specified JsonElement object from the JsonArray by index
     *
     * @param index the index specifying which JsonElement from the JsonArray
     * object to return.
     * @return the JsonElement by index from the JsonArray
     * @throws IndexOutOfBoundsException Thrown when index parameter is < 0 or 
     * >= size() 
     */
    public JsonElement getElement(int index) throws IndexOutOfBoundsException {
        return this.elements.get(index);
    }

    /**
     * Removes a specified element according to its index from the JsonArray, 
     * shifting the existing elements from that index to the left and decrementing 
     * the size of the JsonArray by 1.
     *
     * @param index the index specifying which JsonElement from the JsonArray
     * object to remove.
     * @return the resulting JsonArray after the specified JsonElement has been 
     * removed. 
     * @throws IndexOutOfBoundsException Thrown when index parameter is < 0 or 
     * >= size() 
     */
    public JsonElement removeElement(int index) throws IndexOutOfBoundsException {
        this.elements.remove(index);
        return this;
    }

    /**
     * Returns the String representation of the JsonArray object
     * utilising the JsonWriter to abstract. It passes a StringWriter to the
     * toWriter method and then returns the resulting String.
     * @return String representation of the JsonArray object
     */
    public String toJson() throws IOException {
        try (StringWriter stringOutput = new StringWriter()) {
            toWriter(stringOutput);
            return stringOutput.toString();
        }
    }

    /**
     * Takes a writer and uses it to serialize the JsonArray object
     * @param writer the writer to use, which is then wrapped in a JsonWriter
     * @return the writer
     * @throws IOException if the writer throws an exception
     */
    public Writer toWriter(Writer writer) throws IOException {
        try (JsonWriter jsonWriter = JsonProviders.createWriter(writer)) {
            serialize(jsonWriter);
        }
        return writer;
    }

    /**
     * Takes an output stream and uses it to serialize the JsonArray object
     * @param stream the output stream to use, which is then wrapped in a JsonWriter
     * @return the output stream
     * @throws IOException if the output stream throws an exception
     */
    public OutputStream toStream(OutputStream stream) throws IOException {
        try (JsonWriter jsonWriter = JsonProviders.createWriter(stream)) {
            serialize(jsonWriter);
        }
        return stream;
    }

    /**
     * Takes a JsonWriter and uses it to serialize the JsonArray object. At 
     * every step, it utilises serialize() method, which is inherited from the 
     * parent. This means essentially it doesn't matter what JsonElement is 
     * passed in to the add method, all it has to do is pass the JsonWriter to 
     * the serialize() method of whatever JsonElement it is using, and it will 
     * serialize.
     *
     * @param jsonWriter the JsonWriter to use
     * @return the JsonWriter
     * @throws IOException if the JsonWriter throws an exception
     */
    public JsonWriter serialize(JsonWriter jsonWriter) throws IOException {
        // Start writing the array into jsonWriter, if it hits another array or 
        // object, then pass the writer in to that objects serialize (which will 
        // call this method again) and then return the writer to here. This will 
        // unnest the lot.

        jsonWriter.writeStartArray();

        for (JsonElement element : elements) {
            element.serialize(jsonWriter);
        }

        jsonWriter.writeEndArray();
        return jsonWriter;
    }


//    public String toJson() {
//        // String reference that will store the resulting JSON string output to
//        // be returned by toJson
//        String jsonOutput = "";
//
//        // Iterating over all JsonElement objects in the elements JsonArray object,
//        // appending each JsonElement to jsonOutput
//        for(Iterator<JsonElement> itr = this.elements.iterator(); itr.hasNext();) {
//            // Get the next JsonElement from the elements JsonArray object
//            JsonElement element = itr.next();
//
//            // Case: element is a JsonObject, therefore the whole JSON object can
//            // be stringified via its toJson method, then appended to jsonOutput
//            if(element instanceof JsonObject) {
//                jsonOutput += ((JsonObject)element).toJson();
//            }
//            // Case: element is a JsonArray, therefore the nested JSON array can
//            // be stringified via recursively calling toJson and then appending
//            // the returned String to jsonOutput
//            else if(element instanceof JsonArray) {
//                jsonOutput += ((JsonArray)element).toJson();
//            }
//            // Case: element is not a JsonObject or JsonArray, therefore it can
//            // be simply converted to a String object and appended to jsonOutput
//            else { jsonOutput += element; }
//
//            // Case: haven't reached the end of the JSON array, therefore more
//            // elements must be converted to String objects and appended to the
//            // jsonOutput
//            // Elements are separated by commas.
//            if(itr.hasNext()) { jsonOutput += ", "; }
//        }
//
//        // Returning the resulting String representation of the JsonArray object
//        // JSON arrays are delimited by opening and closing square brackets.
//        return "[" + jsonOutput + "]";
//    }

    /**
     * @return boolean of whether this JsonElement object is of type JsonArray.
     */
    @Override
    public boolean isArray() { return true; }

    /**
     * @return String representation of the JsonArray. This functionality is
     * defined within the toJson method.
     */
    @Override
    public String toString() {
        try {
            return this.toJson();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Builds the JsonArray from an opened JsonReader. 
     * 
     * JsonReader is passed to the nested JsonElements to recursively build. 
     * 
     * @param reader the JsonReader to build the JsonArray from. 
     * @throws IOException Thrown when build is aborted due to encountering an 
     * invalid JsonToken indicating a improperly formed JsonArray. 
     */
    private void build(JsonReader reader) throws IOException {
        while (reader.currentToken() != JsonToken.END_ARRAY) {
            JsonToken token = reader.nextToken();

            switch (token) {
                // Case: the currently read token is a JsonToken.FIELD_NAME token. 
                // No field names should be present within a valid JSON array.  
                case FIELD_NAME:
                    throw new IOException("Invalid JsonToken.FIELD_NAME token read from deserialised JSON array. This is not a valid JSON array. Deserialisation aborted."); 
                case START_OBJECT:
                    this.addElement(new JsonObject(reader));
                    break;
                case START_ARRAY:
                    this.addElement(new JsonArray(reader));
                    break;
                case STRING:
                    this.addElement(new JsonString(reader.getString()));
                    break;
                case NUMBER:
                    this.addElement(new JsonNumber(reader.getString()));
                    break;
                case BOOLEAN:
                    this.addElement(new JsonBoolean(reader.getBoolean()));
                    break;
                case NULL:
                    this.addElement(new JsonNull());
                    break;
                // END_DOCUMENT and END_ARRAY cases are picked up by the overall 
                // while statement. These cases should not be reached, assuming 
                // the JSON array being deserialised is properly formed, so 
                // exception is thrown. 
                case END_DOCUMENT:
                    throw new IOException("Invalid JsonToken.END_DOCUMENT token read prematurely from deserialised JSON array. Deserialisation aborted."); 
                case END_ARRAY:
                    throw new IOException("Invalid JsonToken.END_ARRAY token read prematurely from deserialised JSON array. Deserialisation aborted."); 
                // Case: the currently read token is a JsonToken.END_OBJECT token. 
                // JSON array is being deserialised, not a JSON object. 
                case END_OBJECT:
                    throw new IOException("Invalid JsonToken.END_OBJECT token read from deserialised JSON array. JSON array is being deserialised not a JSON object. This is not a valid JSON array. Deserialisation aborted."); 
            }
        }
    }
}
