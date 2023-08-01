public class JsonString extends JsonElement{
    private String jsonString;

    public JsonString(){

    }

    public JsonString(String value){
        this.jsonString = "\"" + value + "\"";
    }

    public String toString(){
        return jsonString;
    }

    public boolean isString(){return true;};

}
