public class JsonBoolean extends JsonElement {
    private String booleanText;

    public JsonBoolean() {

    }

    public JsonBoolean(boolean value) {
        if(value){
            booleanText = "true";
        } else {
            booleanText = "false";
        }
    }

    public String toString() {
        return booleanText;
    }

    public boolean isBoolean() {
        return true;
    }
}
