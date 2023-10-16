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
     * Called by JsonArray methods in order to verify that the JsonElement passed
     * to them is an instantiated JsonElement.
     *
     * @throws IllegalArgumentException Thrown when a null JsonElement is passed.
     * Instantiated JsonElements must be passed. Must pass instantiated JsonNull
     * to represent JSON null value.
     */
    private static void nullCheck(JsonElement element) throws IllegalArgumentException {
        if (element == null) {
            throw new IllegalArgumentException("Cannot add or set a null JsonElement to a JsonArray. Use an instantiated JsonNull object to represent a valid JSON null value.");
        }
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
        nullCheck(element);
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
        nullCheck(element);
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
        nullCheck(element);
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
     * @return the removed element from the array
     * @throws IndexOutOfBoundsException Thrown when index parameter is < 0 or
     * >= size()
     */
    public JsonElement removeElement(int index) throws IndexOutOfBoundsException {
        return this.elements.remove(index);
    }

    public int getSize() {
        return elements.size();
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
     * Serializes the JsonArray object to a JsonWriter. This method writes the array's contents
     * to the JsonWriter byt recursively calling the serialize method of each of the nested JsonElements.
     * @param jsonWriter the JsonWriter to serialize the JsonArray to
     * @return the same jsonWriter for method chaining
     * @throws IOException if the JsonWriter throws an exception during serialization
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

    /**
     * @return boolean of whether this JsonElement object is of type JsonArray.
     */
    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public JsonArray asArray() {
        return this;
    }

    @Override
    public JsonObject asObject() {
        JsonObject output = new JsonObject();
        for (int i = 0; i < elements.size(); i++) {
            String keyword = "Value";
            if (i > 0) {
                keyword += i;
            }
            output.setProperty(keyword, elements.get(i));
        }
        return output;
    }

    @Override
    public JsonBoolean asBoolean() {
        if (elements.size() >= 1) {
            return elements.get(0).asBoolean();
        } else {
            return JsonBoolean.getInstance(true);
        }
    }

    @Override
    public JsonNumber asNumber() {
        if (elements.size() >= 1) {
            return elements.get(0).asNumber();
        } else {
            return new JsonNumber(0);
        }
    }

    @Override
    public JsonString asString() {
        if (elements.size() >= 1) {
            return elements.get(0).asString();
        } else {
            //todo hacky fix for now
            return new JsonString("");
        }
    }

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
                    this.addElement(JsonBoolean.getInstance(reader.getBoolean()));
                    break;
                case NULL:
                    this.addElement(JsonNull.getInstance());
                    break;

                case END_ARRAY:
                    break;

                    /*
                    IMPORTANT!!!!!!
                    Even though it is apparently never supposed to get to END_ARRAY, it does, and throwing an error
                    means all user input that involves an array will fail. Either this should not throw an exception at
                    all, or the cases where it should must be more clearly specified.
                     */

                default:
                    throw new IOException(String.format("Default: Invalid JsonToken %s. Deserialisation aborted.", token));
            }
        }
    }
}
