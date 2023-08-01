import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class JsonObject extends JsonElement {
    private HashMap<String, JsonElement> properties = new HashMap<>();

    public JsonObject addProperty(String key, Object element){
        JsonElement value;
        if (element != null){
            if ((element instanceof String)||(element instanceof Character)){
                String conversion = String.valueOf(element);
                value = new JsonString(conversion);
            } else if (element instanceof Number){
                value = new JsonNumber((Number) element);
            } else if (element instanceof Boolean) {
                value = new JsonBoolean((Boolean) element);
            } else if (element instanceof JsonObject) {
                value = (JsonObject) element;
            } else if (element instanceof JsonArray){
                value = (JsonArray) element;
            }
            else if (element.getClass().isArray()){
                value = new JsonArray();
                if (element instanceof int[]){
                    int[] entries = (int[]) element;
                    for (int i: entries){
                        ((JsonArray)value).addElement(new JsonNumber(i));
                    }
                } else if (element instanceof float[]){
                    float[] entries = (float[]) element;
                    for (float i: entries){
                        ((JsonArray)value).addElement(new JsonNumber(i));
                    }
                }   else if (element instanceof String[]) {
                    String[] entries = (String[]) element;
                    for (String i: entries){
                        ((JsonArray)value).addElement(new JsonString(i));
                    }
                } else if (element instanceof boolean[]){
                    boolean[] entries = (boolean[]) element;
                    for (boolean i: entries){
                        value.asArray().addElement(new JsonBoolean(i));
                    }
                } else {
                    throw new IllegalArgumentException();
                }

                //Need to add ability to add arrays.
            /*} else if (element.getClass().isArray()) {
                if (element instanceof int[]){

                } else if (element instanceof float[]) {

                } else if (element instanceof String[]){

                } else if (element instanceof boolean[]){
                    System.out.println("It was found!!!");
                }*/
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            value = new JsonNull();
        }
        properties.put(key, value);
        return this;
    }

    public JsonElement getProperty(String key){
        return properties.get(key);
    }

    public String toJson(){
        String JsonOutput = "";
        Iterator<String> keyList = properties.keySet().iterator();
        for (Iterator<String> it = keyList; it.hasNext(); ) {
            String key = it.next();
            JsonOutput += "\""+ key + "\": ";
            if (getProperty(key) instanceof JsonObject){
                JsonOutput += ((JsonObject) getProperty(key)).toJson();
            } else if (getProperty(key) instanceof JsonArray){
                JsonOutput += ((JsonArray) getProperty(key)).toJson();
            } else {
                JsonOutput += getProperty(key);
            }

            //If it is an Object or Array, call toJson methods on them individually.
            //If it is anything else, use the value as is.

            if (it.hasNext()){
                JsonOutput += ", ";
            }
        }
        return "{" + JsonOutput + "}";
    }

    public String toString(){
        return toJson();
    }

    public boolean isObject(){
        return true;
    }
}
