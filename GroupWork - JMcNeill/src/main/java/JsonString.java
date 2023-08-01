/**
 * Class representing the JSON string type 
 */
public class JsonString extends JsonElement{
    private String stringValue;

    public JsonString(){

    }

    public JsonString(String value){
        this.stringValue = "\"" + value + "\"";
    }

    public String toString(){
        return stringValue;
    }

    public boolean isString(){return true;};

}
