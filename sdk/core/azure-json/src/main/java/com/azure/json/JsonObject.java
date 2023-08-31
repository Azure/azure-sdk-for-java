package com.azure.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class representing the JSON object type.
 */
public class JsonObject extends JsonElement {

    public JsonObject() {
    }

    /**
     * Constructor specifically used to invoke a build from a reader. It
     * @param reader is any currently open JsonReader object. This is utilised by JsonBuilder and also
     *               called recursively by objects. If the developer knows they want to build an object
     *               from a JSON, then they can bypass the JsonBuilder and just use this constructor.
     */
    public JsonObject(JsonReader reader) {
        try {
            this.build(reader);
        } catch (Exception e) {
            // todo sort this logging out so it's not just printing a stack trace...
            e.printStackTrace();
        }
    }


    /**
     * Stores the key and values of each property in the JsonObject. The values
     * may be any valid JSON type: object, array, string, number, boolean, and null
     */

    private Map<String, JsonElement> properties = new LinkedHashMap<>();

    /**
     * Adds a new property into the JsonObject object from the object or primitive directly.
     * It will wrap the object in the appropriate JsonElement type.
     *
     * @param key specifies the key of the property being added
     * @param element specifies the value of the property being added
     * @return JsonObject representing the new state of the JsonObject object
     * after the addition of the new property.
     */
    public JsonObject addProperty(
        String key,
        Object element
    ) throws IllegalArgumentException {
        // Stores element cast or converted to a valid JsonElement type if such
        // cast or conversion exists for it.
        JsonElement value;

        // Case: element is not null, therefore more instance checking needs to
        // be done to check for valid casts or conversions
        if(element != null) {
            // Case: element is a String or Character, therefore value stores
            // element as a JsonString
            if(
                (element instanceof String)
                ||
                (element instanceof Character)
            ) { value = new JsonString(String.valueOf(element)); }
            // Case: element is a Number, therefore value stores element as a
            // JsonNumber
            else if(element instanceof Number) {
                value = new JsonNumber((Number)element);
            }
            // Case: element is a Boolean, therefore value stores element as a
            // JsonBoolean
            else if(element instanceof Boolean) {
                value = new JsonBoolean((Boolean)element);
            }
            // Case: element is a JsonObject, therefore value stores element as
            // a JsonObject
            else if(element instanceof JsonObject) { value = (JsonObject)element; }
            // Case: element is a JsonArray, therefore value stores element as
            // a JsonArray
            else if(element instanceof JsonArray) { value = (JsonArray)element; }
            // Case: element is an Array
            else if(element.getClass().isArray()) {
                // Value references a newly constructed JsonArray object. The
                // array elements in element will be added to this JsonArray object.
                value = new JsonArray();

                // Case: element is an int array, therefore all elements can be
                // added as JsonNumber objects.
                if(element instanceof int[]) {
                    for(int i : (int[])element) {
                        ((JsonArray)value).addElement(new JsonNumber(i));
                    }
                }
                // Case: element is a float array, therefore all elements can be
                // added as JsonNumber objects.
                else if(element instanceof float[]) {
                    for(float i : (float[])element) {
                        ((JsonArray)value).addElement(new JsonNumber(i));
                    }
                }
                // Case: element is a String array, therefore all elements can be
                // added as JsonString objects.
                else if(element instanceof String[]) {
                    for(String i : (String[])element) {
                        ((JsonArray)value).addElement(new JsonString(i));
                    }
                }
                // Case: element is a boolean array, therefore all elements can
                // be added as JsonBoolean objects.
                else if(element instanceof boolean[]) {
                    for(boolean i : (boolean[])element) {
                        ((JsonArray)value).addElement(new JsonBoolean(i));
                    }
                }
                // Case: element is a JsonElement array, therefore all elements
                // can be added as their respective JsonElement objects.
                else if(element instanceof JsonElement[]) {
                    for(JsonElement i : (JsonElement[])element) {
                        // Case: i is another JsonArray, therefore the element
                        // is added as a JsonArray object
                        if(i instanceof JsonArray) {
                            ((JsonArray)value).addElement((JsonArray)i);
                        }
                        // Case: i is another JsonArray, therefore the element
                        // is added as a JsonArray object
                        else if(i instanceof JsonBoolean) {
                            ((JsonArray)value).addElement((JsonBoolean)i);
                        }
                        // Case: i is a JsonNull object, therefore the element
                        // is added as a JsonNull object
                        else if(i instanceof JsonNull) {
                            ((JsonArray)value).addElement((JsonNull)i);
                        }
                        // Case: i is a JsonNumber object, therefore the element
                        // is added as a JsonNumber object
                        else if(i instanceof JsonNumber) {
                            ((JsonArray)value).addElement((JsonNumber)i);
                        }
                        // Case: i is a JsonObject object, therefore the element
                        // is added as a JsonObject object
                        else if(i instanceof JsonObject) {
                            ((JsonArray)value).addElement((JsonObject)i);
                        }
                        // Case: i is a JsonString object, therefore the element
                        // is added as a JsonString object
                        else if(i instanceof JsonString) {
                            ((JsonArray)value).addElement((JsonString)i);
                        }
                        // Case: i is not of a type that can be casted or
                        // converted to a valid JsonElement type. The valid
                        // JsonElement types are:
                        //      - JsonArray
                        //      - JsonBoolean
                        //      - JsonNull
                        //      - JsonNumber
                        //      - JsonObject
                        //      - JsonString
                        else { throw new IllegalArgumentException(); }
                    }
                }
                // Case: element is not an array of a type that can be casted or
                // converted to a valid JsonElement type.
                else { throw new IllegalArgumentException(); }
            }
            // Case: element is not null, but is also not a valid type that can
            // be cast or converted to a valid JsonElement type.
            else { throw new IllegalArgumentException(); }
        }
        // Case: element is null, therefore value is a JsonNull object.
        else { value = new JsonNull(); }

        // Adding the new property, the key with its value pair. The value is
        // the respective JsonElement cast/conversion of element.
        this.addProperty(key, value);
        //properties.put(key, value);

        // Returning the new state of the JsonObject after the successful
        // addition of the new property
        return this;
    }

    /**
     * This overload of addProperty is utilised when a JsonElement exists. The primary user is the build
     * method of JsonObject and JsonArray.
    */

    public JsonObject addProperty(
        String key,
        JsonElement element
    ) throws IllegalArgumentException {
        // Adding the new property, the key with its value pair. The value is
        // the respective JsonElement cast/conversion of element.
        properties.put(key, element);

        // Returning the new state of the JsonObject after the successful
        // addition of the new property
        return this;
    }

    /**
     * @param key specifies the property to return by its respective key
     * @return JsonElement representing the
     *
     * TODO: this method currently does what the new getValueBykey method does.
     * This getProperty method should do what it suggests, which is get a property.
     * Properties are the whole key-value pair not just their key or value.
     * Therefore, this method needs to be redefined to return key value pairs,
     * for example Entry objects since properties is currently utilising HashMap.
     * Perhaps we could confirm with the dev team on discord if this method is
     * required to behave in the current way that it is, if so, then we could
     * keep this method as it is and remove the getKeyByValue and getValueByKey
     * methods that I have defined.
     */
    public JsonElement getProperty(String key) { return properties.get(key); }

    /**
     * Gets the key for a given property by value.
     * TODO: this method may be matching values based off objects matching, not
     * their substance. Either implementation could be useful.
     *
     * @param value specifying the value to match a property with to find the
     * respective key pair of
     * @return String object representing the respective key pair of the search value
     */
    public String getKeyByValue(JsonElement value) throws IllegalArgumentException {
        // Iterating over all entries in the properties hash map, inspecting each
        // key-value pair (property) for the one whose value matches the value
        // argument passed.
        for(Entry<String, JsonElement> entry : properties.entrySet()) {
            // Case: found entry with matching value. Returning the key as a result.
            if(((JsonElement)value).equals(entry.getValue())) { return entry.getKey(); }
            // TODO: the following code may be a better substitute for equating
            // two objects:
            // if(((JsonElement)value) == entry.getValue()) { return entry.getKey(); }
        }
        // Case: if the for loop didn't return at any point, then there is no
        // property in the JsonObject where the value matches the value passed
        // to this method invocation.
        throw new IllegalArgumentException();
    }

    /**
     * Gets the value for a given property by key.
     *
     * @param key specifying the key to match a property with to find the
     * respective value pair of
     * @return JsonElement object representing the respective value pair of the
     * search key
     */
    public JsonElement getValueByKey(String key) { return properties.get(key); }

    /**
     * Returns the String representation of the JsonObject object
     *
     * @return String representation of the JsonObject object
     */
//    public String toJson() {
//        // String reference that will store the resulting JSON string output to
//        // be returned by toJson
//        String jsonOutput = "";
//
//        // Iterating over the keys in the key set of the properties hash map,
//        // ....
//        for(Iterator<String> itr = properties.keySet().iterator(); itr.hasNext();) {
//            // Get the next key String from the properties hash map
//            String key = itr.next();
//
//            // Including the double quotations marks, via escape characters, which
//            // will surround the given key in the String representation of the
//            // JSON object. All keys must be in quotes.
//            jsonOutput += "\""+ key + "\": ";
//
//            // Case: the value pair is another JsonObject, therefore the nested
//            // JSON object can be stringified via recursively calling toJson and
//            // then appending the returned String to jsonoutput
//            if(this.getProperty(key) instanceof JsonObject) {
//                jsonOutput += ((JsonObject) this.getProperty(key)).toJson();
//            }
//            // Case: the value pair is a JsonArray, therefore the whole JSON
//            // arary can be stringified via its toJson method, and the returned
//            // String can be appended to jsonOutput
//            else if(this.getProperty(key) instanceof JsonArray) {
//                jsonOutput += ((JsonArray) this.getProperty(key)).toJson();
//            }
//            // Case: the value pair is not a JsonObject or JsonArray, therefore
//            // it can be simply converted to a String object and appended to
//            // jsonOutput
//            else { jsonOutput += this.getProperty(key); }
//
//            // Case: haven't reached the end of the JSON Object, therefore there
//            // are more properties that must be converted to String objects and
//            // appended to the jsonOutput.
//            // Properties are separated by commas.
//            if(itr.hasNext()) { jsonOutput += ", "; }
//        }
//
//        // Returning the resulting String representation of the JsonArray object
//        // JSON objects are delimited by opening and closing curly brackets.
//        return "{" + jsonOutput + "}";
//    }

    /**
     * @return boolean of whether this JsonElement object is of type JsonObject.
     */
    @Override
    public boolean isObject() { return true; }

    /**
     * @return String representation of the JsonObject. This functionality is
     * defined within the toJson method.
     */
    @Override
    public String toString() {
            return toJson();
    }


    private void build(JsonReader reader) throws IOException {

        String fieldName = null;
        JsonToken token = reader.nextToken();
        while(token != JsonToken.END_OBJECT) {

            //todo remove this debug line
            //System.out.printf("Token: %s%n", token);

            switch (token) {

                case FIELD_NAME:
                    fieldName = reader.getFieldName();
                    break;
                case START_OBJECT:
                    this.addProperty(fieldName, new JsonObject(reader));
                    break;
                case START_ARRAY:
                    this.addProperty(fieldName, new JsonArray(reader));
                    break;
                case STRING:
                    this.addProperty(fieldName, new JsonString(reader.getString()));
                    break;
                case NUMBER:
                    this.addProperty(fieldName, new JsonNumber(reader.getString()));
                    break;
                case BOOLEAN:
                    this.addProperty(fieldName, new JsonBoolean(reader.getBoolean()));
                    break;
                case NULL:
                    this.addProperty(fieldName, new JsonNull());
                    break;
//                 These two cases are picked up by the overall while statement.
                case END_DOCUMENT:
                    return;
                case END_OBJECT:
                    //It shouldn't get here assuming all is working correctly, and the JSON
                    //Is properly formed
                    return;
                case END_ARRAY:
                    //this should be an error, it's not possible
                    return;

            }
            token = reader.nextToken();

        }



    }

    /**
     * Serializes the JsonObject to a String. This is a convenience method that utilises the toWriter method.
     * @return the String representation of the JsonObject
     */
    public String toJson() {

        String s = null;

        try (StringWriter stringOutput = new StringWriter()) {
            toWriter(stringOutput);
            s = stringOutput.toString();
        } catch (IOException e) {
            // TODO: 22/08/2023 add better logging than this
            e.printStackTrace();
        }


        return s;

    }

    /**
     * Serializes the JsonObject to a Writer. May need to be named better
     * than toWriter or toStream, but I'm unsure exactly what to use (as toJsonWriter is confusing as JsonWriter is a class in
     * itself.
     * @param writer is the Writer to write to - this is in turn wrapped in a JsonWriter to abstract the underlying writer or stream
     * @return the Writer that was passed in
     * @exception IOException if the underlying writer or stream throws an exception
     */
    public Writer toWriter(Writer writer) throws IOException {
        try (JsonWriter jsonWriter = JsonProviders.createWriter(writer)) {
            serialize(jsonWriter);
        }
        return writer;
    }

    /**
     * Serializes the JsonObject to an OutputStream. May need to be named better as per above
     *
     * @param stream is the OutputStream to write to - this is in turn wrapped in a JsonWriter to abstract the underlying stream
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
     * Serializes the JsonObject to a JsonWriter (which could in turn contain a stream or writer). All serialisation methods
     * eventually utilise this method.
     * @param writer is the JsonWriter to write to - this is generally going to be provided by the wrapper toStream or toWriter methods
     * @return the JsonWriter that was passed in
     * @throws IOException if the underlying writer or stream throws an exception
     */
    public JsonWriter serialize(JsonWriter writer) throws IOException {
        writer.writeStartObject();

        //for each item in the linked hashmap
        for (Map.Entry<String, JsonElement> entry : properties.entrySet()) {
            //write the key
            writer.writeFieldName(entry.getKey());
            //write the value
            entry.getValue().serialize(writer);
        }

        writer.writeEndObject();

        return writer;
    }

}
