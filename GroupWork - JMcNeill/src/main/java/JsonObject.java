import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;

/**
 * Class representing the JSON object type 
 */
public class JsonObject extends JsonElement {
    /**
     * 
     */
    private Map<String, JsonElement> properties = new HashMap<>();

    /**
     * @param key 
     * @param element 
     * @return 
     */
    public JsonObject addProperty(String key, Object element){

        JsonElement value;

        // Case: 
        if(element != null) {
            // Case: 
            if(
                (element instanceof String)
                || 
                (element instanceof Character) 
            ) {
                String conversion = String.valueOf(element);
                value = new JsonString(conversion);
            } 
            // Case: 
            else if(element instanceof Number) {
                value = new JsonNumber((Number) element);
            } 
            // Case: 
            else if(element instanceof Boolean) {
                value = new JsonBoolean((Boolean) element);
            } 
            // Case: 
            else if(element instanceof JsonObject) { value = (JsonObject)element; } 
            // Case: 
            else if(element instanceof JsonArray) { value = (JsonArray)element; }
            // Case: 
            else if(element.getClass().isArray()) {

                value = new JsonArray();

                // Case: 
                if(element instanceof int[]) {
                    int[] entries = (int[]) element;

                    for(int i : entries) {
                        ((JsonArray)value).addElement(new JsonNumber(i));
                    }
                } 
                // Case: 
                else if (element instanceof float[]) {
                    float[] entries = (float[]) element;

                    for(float i : entries) {
                        ((JsonArray)value).addElement(new JsonNumber(i));
                    }
                } 
                // Case: 
                else if(element instanceof String[]) {
                    String[] entries = (String[]) element;

                    for(String i : entries) {
                        ((JsonArray)value).addElement(new JsonString(i));
                    }
                } 
                // Case: 
                else if(element instanceof boolean[]) {
                    boolean[] entries = (boolean[]) element; 

                    for(boolean i : entries) {
                        value.asArray().addElement(new JsonBoolean(i));
                    }
                } 
                // Case: 
                else { throw new IllegalArgumentException(); }

                //Need to add ability to add arrays.
            /*} else if (element.getClass().isArray()) {
                if (element instanceof int[]){

                } else if (element instanceof float[]) {

                } else if (element instanceof String[]){

                } else if (element instanceof boolean[]){
                    System.out.println("It was found!!!");
                }*/
            } 
            // Case: 
            else { throw new IllegalArgumentException(); }
        } 
        // Case: 
        else { value = new JsonNull(); }

        properties.put(key, value);

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
