import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;

/**
 * Class representing the JSON object type.  
 */
public class JsonObject extends JsonElement {
    /**
     * Stores the key and values of each property in the JsonObject. The values 
     * may be any valid JSON type: object, array, string, number, boolean, and null  
     */
    private Map<String, JsonElement> properties = new HashMap<>();

    /**
     * Adds a new property into the JsonObject object. 
     * TODO: need code changes to make the properties stored in the order they 
     * are added 
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
        // Stores element casted or converted to a valid JsonElement type if such 
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
            // be casted or converted to a valid JsonElement type. 
            else { throw new IllegalArgumentException(); }
        } 
        // Case: element is null, therefore value is a JsonNull object. 
        else { value = new JsonNull(); }

        // Adding the new property, the key with its value pair. The value is 
        // the respective JsonElement casted/conversion of element. 
        properties.put(key, value);

        // Returning the new state of the JsonObject after the successful 
        // addition of the new property 
        return this;
    }

    /**
     * @param key 
     * @return 
     */
    public JsonElement getProperty(String key) { return properties.get(key); }

    /**
     * @return
     */
    public String toJson() {
        String JsonOutput = "";

        Iterator<String> keyList = properties.keySet().iterator();

        for(Iterator<String> it = keyList; it.hasNext();) {
            String key = it.next();

            JsonOutput += "\""+ key + "\": ";

            // Case: 
            if(getProperty(key) instanceof JsonObject) {
                JsonOutput += ((JsonObject) getProperty(key)).toJson();
            } 
            // Case: 
            else if(getProperty(key) instanceof JsonArray) {
                JsonOutput += ((JsonArray) getProperty(key)).toJson();
            } 
            // Case: 
            else { JsonOutput += getProperty(key); }

            //If it is an Object or Array, call toJson methods on them individually.
            //If it is anything else, use the value as is.

            // Case: 
            if(it.hasNext()) { JsonOutput += ", "; }
        }
        return "{" + JsonOutput + "}";
    }

    /**
     * @return 
     */
    public String toString() { return toJson(); }

    /**
     * @return 
     */
    public boolean isObject() { return true; }
}
