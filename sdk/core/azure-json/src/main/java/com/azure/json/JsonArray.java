package com.azure.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * Class representing the JSON array type
 */
public class JsonArray extends JsonDataStructure {

    public JsonArray() {
        super();
    }

    public JsonArray(JsonReader reader) throws IOException {
        super();
        this.build(reader);
    }
    /**
     * Stores the JsonElements nested in this JsonArray object.
     * Each of these elements should be one of the following valid JSON types:
     * object, array, string, number, boolean, and null.
     */


    List<JsonElement> elements = new ArrayList<>();


    /**
     * Adds a JsonElement object as a nested element within the JsonArray object,
     * appending it to the end.
     *
     * @param element a JsonElement object representing one of the valid JSON
     * types: object, array, string, number, boolean, and null
     * @return JsonArray object representing the new state of the JsonArray object
     * after the addition of the new JsonElement object appended.
     */
    public JsonArray addElement(JsonElement element) {
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
     * TODO: throws error(s) relating to the index in respect to the current state
     * of the elements ArrayList
     */
    public JsonArray addElement(int index, JsonElement element) {
        // TODO: check for out of bounds
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
     *
     * TODO: maybe rename this to replaceElement due to the nature of the
     * ArrayList.set() method
     * TODO: throws error(s) relating to the index in respect to the current state
     * of the elements ArrayList
     */
    public JsonArray setElement(int index, JsonElement element) {
        // TODO: check for out of bounds
        this.elements.set(index, element);
        return this;
    }

    /**
     * Returns a specified JsonElement object from the JsonArray by index
     *
     * @param index the index specifying which JsonElement from the JsonArray
     * object to return.
     * @return the JsonElement by index from the JsonArray
     *
     * TODO: throws error(s) relating to the index in respect to the current state
     * of the elements ArrayList
     */
    public JsonElement getElement(int index) {
        // TODO: check for index out of bounds
        return this.elements.get(index);
    }

    /**
     * Removes a specified JsonElement object from the JsonArray
     *
     * @param index the index specifying which JsonElement from the JsonArray
     * object to remove.
     *
     * TODO: may want to change the return to returning the removed JsonElement
     * object.
     *
     * TODO: throws error(s) relating to the index in respect to the current state
     * of the elements ArrayList
     */
    public JsonElement removeElement(int index) {
        // TODO: check for index out of bounds
        this.elements.remove(index);
        return this;
    }

    /**
     * Returns the String representation of the JsonArray object
     * utilising the JsonWriter to abstract. It passes a StringWriter to the
     * toWriter method and then returns the resulting String.
     * @return String representation of the JsonArray object
     */
    @Override
    public String toJson() throws IOException {

        String s = null;
        try (StringWriter stringOutput = new StringWriter()) {
            toWriter(stringOutput);
            s = stringOutput.toString();
        }
        return s;

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
     * Takes a JsonWriter and uses it to serialize the JsonArray object. At every step, it utilises
     * serialize() method, which is inherited from the parent. This means essentially it doesn't matter
     * what JsonElement is passed in to the add method, all it has to do is pass the JsonWriter to the serialize()
     * method of whatever JsonElement it is using, and it will serialize.
     *
     * @param jsonWriter the JsonWriter to use
     * @return the JsonWriter
     * @throws IOException if the JsonWriter throws an exception
     */

    public JsonWriter serialize(JsonWriter jsonWriter) throws IOException {

        // Start writing the array into jsonWriter, if it hits another array or object, then pass the writer in to
        // that objects serialize (which will call this method again) and then return the writer to here.
        // this will unnest the lot.

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
     * The build method is used when a JsonReader is passed in to the array (usually through the constructor)
     * It will then read the JsonReader and build the JsonArray from it. If it hits a nested element, the reader
     * is passed into the constructor of the new element, and it builds recursively.
     * @param reader the JsonReader to use
     * @throws IOException if the JsonReader throws an exception
     */
    @Override
    public void build(JsonReader reader) throws IOException {

        while(reader.currentToken() != JsonToken.END_ARRAY) {

            JsonToken token = reader.nextToken();
            System.out.printf("Token: %s%n", token);


            switch (token) {

                case FIELD_NAME:
                    //fieldName = reader.getFieldName();
                    break;
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
//                 These two cases are picked up by the overall while statement.
                case END_DOCUMENT:
                    return;
                case END_ARRAY:
                    // It shouldn't get here, the loop prevents it BUT just in case...
                    return;
                case END_OBJECT:
                    //this should be an error, it's not possible
                    break;

            }

        }



    }


}
