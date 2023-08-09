package com.azure.json;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Class representing the JSON object type.
 */
public class JsonObject extends JsonElement {
    /**
     * Stores the key and values of each property in the JsonObject. The values
     * may be any valid JSON type: object, array, string, number, boolean, and null
     */
    private Map<String, JsonElement> properties = new LinkedHashMap<>();

    /**
     * Adds a new property into the JsonObject object.
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
        properties.put(key, value);

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
    public String toJson() {
        // String reference that will store the resulting JSON string output to
        // be returned by toJson
        String jsonOutput = "";

        // Iterating over the keys in the key set of the properties hash map,
        // ....
        for(Iterator<String> itr = properties.keySet().iterator(); itr.hasNext();) {
            // Get the next key String from the properties hash map
            String key = itr.next();

            // Including the double quotations marks, via escape characters, which
            // will surround the given key in the String representation of the
            // JSON object. All keys must be in quotes.
            jsonOutput += "\""+ key + "\": ";

            // Case: the value pair is another JsonObject, therefore the nested
            // JSON object can be stringified via recursively calling toJson and
            // then appending the returned String to jsonoutput
            if(this.getProperty(key) instanceof JsonObject) {
                jsonOutput += ((JsonObject) this.getProperty(key)).toJson();
            }
            // Case: the value pair is a JsonArray, therefore the whole JSON
            // arary can be stringified via its toJson method, and the returned
            // String can be appended to jsonOutput
            else if(this.getProperty(key) instanceof JsonArray) {
                jsonOutput += ((JsonArray) this.getProperty(key)).toJson();
            }
            // Case: the value pair is not a JsonObject or JsonArray, therefore
            // it can be simply converted to a String object and appended to
            // jsonOutput
            else { jsonOutput += this.getProperty(key); }

            // Case: haven't reached the end of the JSON Object, therefore there
            // are more properties that must be converted to String objects and
            // appended to the jsonOutput.
            // Properties are separated by commas.
            if(itr.hasNext()) { jsonOutput += ", "; }
        }

        // Returning the resulting String representation of the JsonArray object
        // JSON objects are delimited by opening and closing curly brackets.
        return "{" + jsonOutput + "}";
    }

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
    public String toString() { return toJson(); }

}
