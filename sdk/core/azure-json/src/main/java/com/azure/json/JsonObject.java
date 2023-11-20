package com.azure.json;

import com.azure.json.implementation.StringBuilderWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Class representing the JSON object type.
 */
public class JsonObject extends JsonElement {
    /**
     * Stores the key and values of each property in the JsonObject. The values
     * may be any valid JSON type: object, array, string, number, boolean, and null
     */
    private final Map<String, JsonElement> properties = new LinkedHashMap<>(0);

    /**
     * Default constructor.
     */
    public JsonObject() {}

    /**
     * Constructor used to construct JsonObject from a JsonReader.
     * If the developer knows they want to build an object from JSON, then they
     * can bypass the JsonBuilder and just use this constructor directly.
     *
     * @param reader The opened JsonReader to construct the JsonObject object from.
     */
    public JsonObject(JsonReader reader) {
        try {
            this.build(reader);
        } catch (Exception e) {
            // TODO this logging is not very robust.
            e.printStackTrace();
        }
    }


    /**
     * Returns a property value for the corresponding specified property key
     * from the JsonObject.
     *
     * @param key Specifies the property key that identifies the property in the
     * JsonObject.
     * @return JsonElement representing the value of the property specified by
     * the key.
     */
    public JsonElement getProperty(String key) {
        return properties.get(key);
    }


    /**
     * Sets a property to a JsonObject by key and JsonElement value.
     * <p>
     * If {@code key} or {@code element} is null a {@link NullPointerException} will be thrown.
     *
     * @return The jsonObject with the added property.
     */
    public JsonObject setProperty(String key, JsonElement element) {
        properties.put(key, element);
        return this;
    }

    /**
     * @return boolean of whether this JsonElement object is of type JsonObject.
     */
    public JsonElement removeProperty(String key)  {
        if (properties.containsKey(key)) {
            return this.properties.remove(key);
        } else {
            return null;
        }
    }

    @Override
    public boolean isObject() {
        return true;
    }

    /**
     * @return String representation of the JsonObject. This functionality is
     * defined within the toJson method.
     */
    @Override
    public String toString() {
        try {
            return this.toJson();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Builds the JsonObject from an opened JsonReader.
     * JsonReader is passed to the nested JsonElements to recursively build.
     *
     * @param reader the JsonReader to build the JsonObject from.
     * @throws IOException Thrown when build is aborted due to encountering an
     * invalid JsonToken indicating a improperly formed JsonObject.
     */
    private void build(JsonReader reader) throws IOException {
        String fieldName = null;
        JsonToken token = reader.nextToken();

        while (token != JsonToken.END_OBJECT) {
            switch (token) {
                case FIELD_NAME:
                    fieldName = reader.getFieldName();
                    break;
                case START_OBJECT:
                    this.setProperty(fieldName, new JsonObject(reader));
                    break;
                case START_ARRAY:
                    this.setProperty(fieldName, new JsonArray(reader));
                    break;
                case STRING:
                    this.setProperty(fieldName, new JsonString(reader.getString()));
                    break;
                case NUMBER:
                    this.setProperty(fieldName, new JsonNumber(reader.getString()));
                    break;
                case BOOLEAN:
                    this.setProperty(fieldName, JsonBoolean.getInstance(reader.getBoolean()));
                    break;
                case NULL:
                    this.setProperty(fieldName, JsonNull.getInstance());
                    break;
                // END_DOCUMENT and END_OBJECT cases are picked up by the overall
                // while statement. These cases should not be reached, assuming
                // the JSON object being deserialised is properly formed, so
                // exception is thrown.
                case END_DOCUMENT:
                    throw new IOException("Invalid JsonToken.END_DOCUMENT token read prematurely from deserialised JSON object. Deserialisation aborted.");
                case END_OBJECT:
                    throw new IOException("Invalid JsonToken.END_OBJECT token read prematurely from deserialised JSON object. Deserialisation aborted.");
                // Case: the currently read token is a JsonToken.END_ARRAY token.
                // JSON object is being deserialised, not a JSON array.
              //  case END_ARRAY:
                 //   throw new IOException("Invalid JsonToken.END_ARRAY token read from deserialised JSON object. JSON object is being deserialised not a JSON array. This is not a valid JSON object. Deserialisation aborted.");
                default:
                    throw new IOException(String.format("Invalid JsonToken %s read from deserialised JSON object. Deserialisation aborted.",token));
            }
            token = reader.nextToken();
        }
    }

    /**
     * Serializes the JsonObject to a String. This is a convenience method that
     * utilises the toWriter method.
     * @return the String representation of the JsonObject
     */
    public String toJson() throws IOException {
        try (StringBuilderWriter stringOutput = new StringBuilderWriter(new StringBuilder())) {
            toWriter(stringOutput);
            return stringOutput.toString();
        }
    }

    /**
     * Serializes the JsonObject to a String. This is a convenience method that
     * utilises the toWriter method.
     * Then it gets reformatted with line breaks and tabs for easier readability.
     * @return the String representation of the JsonObject
     */

    public String toJsonPretty() throws IOException {
        int tabCount = 0;
        String input = toJson();
        for(int i = 0; i < input.length(); i++){
            if(input.charAt(i) == '{' || input.charAt(i) == '['){
                tabCount++;
                String firstHalf = input.substring(0, i+1);
                String lastHalf = input.substring(i+1);
                input = firstHalf + "\n" + "\t".repeat(Math.max(0, tabCount)) + lastHalf;
            } else if (input.charAt(i) == ',') {
                String firstHalf = input.substring(0, i+1);
                String lastHalf = input.substring(i+1);
                input = firstHalf + "\n" + "\t".repeat(Math.max(0, tabCount)) + lastHalf;
            } else if (input.charAt(i) == ']' || input.charAt(i) == '}'){
                tabCount--;
                String firstHalf = input.substring(0, i);
                String lastHalf = input.substring(i);
                StringBuilder tabs = new StringBuilder("\n");
                tabs.append("\t".repeat(Math.max(0, tabCount)));
                input = firstHalf + tabs + lastHalf;
                i = i + tabs.length();
            }
        }
        input = input.replace(":", ": ");
        return input;
    }

    /**
     * Serializes the JsonObject to a Writer. May need to be named better
     * than toWriter or toStream, but I'm unsure exactly what to use (as
     * toJsonWriter is confusing as JsonWriter is a class in itself.
     * @param writer is the Writer to write to - this is in turn wrapped in a
     * JsonWriter to abstract the underlying writer or stream
     * @return the Writer that was passed in
     * @exception IOException if the underlying writer or stream throws an exception
     * @throws IOException if the underlying writer or stream throws an exception
     */
    public Writer toWriter(Writer writer) throws IOException {
        try (JsonWriter jsonWriter = JsonProviders.createWriter(writer)) {
            serialize(jsonWriter);
        }
        return writer;
    }

    /**
     * Serializes the JsonObject to an OutputStream. May need to be named better
     * as per above
     *
     * @param stream is the OutputStream to write to - this is in turn wrapped
     * in a JsonWriter to abstract the underlying stream
     * @return the OutputStream that was passed in
     * @throws IOException if the underlying writer or stream throws an exception
     */
    public OutputStream toStream(OutputStream stream) throws IOException {
        try (JsonWriter jsonWriter = JsonProviders.createWriter(stream)) {
            serialize(jsonWriter);
        }
        return stream;
    }

    /**
     * Serializes the JsonObject to a JsonWriter (which could in turn contain a
     * stream or writer). All serialisation methods eventually utilise this method.
     *
     * @param writer is the JsonWriter to write to - this is generally going to
     * be provided by the wrapper toStream or toWriter methods
     * @return the JsonWriter that was passed in
     * @throws IOException if the underlying writer or stream throws an exception
     */
    public JsonWriter serialize(JsonWriter writer) throws IOException {
        writer.writeMap(properties, (entryValueWriter, entryValue) -> entryValue.serialize(entryValueWriter));
        return writer;
    }
}
