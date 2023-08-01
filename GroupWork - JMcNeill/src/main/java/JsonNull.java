/**
 * Class representing the JSON null type 
 */
public class JsonNull extends JsonElement{
    private final String nullValue = "null";

    // Default constructor 
    public JsonNull() {}

    public String toString(){
        return nullValue;
    }

    public boolean isNull() {
        return true;
    }
}
