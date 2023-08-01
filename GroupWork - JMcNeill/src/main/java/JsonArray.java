import java.util.ArrayList;
import java.util.Iterator;

public class JsonArray extends JsonElement{
    ArrayList<JsonElement> elements = new ArrayList<>();

    public JsonArray addElement(JsonElement element){
        elements.add(element);
        return this;
    }

    public JsonArray addElement(int index, JsonElement element){
        elements.add(index, element);
        return this;
    }

    public JsonArray setElement(int index, JsonElement element){
        elements.set(index, element);
        return this;
    }

    public JsonElement getElement(int index){
        return elements.get(index);
    }

    public JsonElement removeElement(int index){
        elements.remove(index);
        return this;
    }

    public String toJson(){
        String jsonoutput = "";
        for (Iterator<JsonElement> it = elements.iterator(); it.hasNext(); ) {
            JsonElement element = it.next();
            if (element instanceof JsonObject){
                jsonoutput += ((JsonObject)element).toJson();
            } else {
                jsonoutput += element;
            }
            if (it.hasNext()) {
                jsonoutput += ", ";
            }
        }
        return "[" + jsonoutput + "]";
    }

    public boolean isArray(){return true;}

    public String toString() {
        return toJson();
    }
}
