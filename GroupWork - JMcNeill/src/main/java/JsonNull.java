public class JsonNull extends JsonElement{
    private final String nullValue = "null";

    public JsonNull(){

    }

    public String toString(){
        return nullValue;
    }

    public boolean isNull() {
        return true;
    }
}
