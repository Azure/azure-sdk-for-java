/**
 * Class representing the JSON number type 
 */
public class JsonNumber extends JsonElement{
    /** 
     * Stores the String representation of the JSON number type. 
     */
    private String numberValue;

    /**
     * Default constructor. 
     * Default sets numberValue to "0" through the other constructor. 
     * TODO: may need to remove this due to design guidelines? May only want to 
     * have the public JsonNumber(Number value) constructor. 
     */
    public JsonNumber() { this(0); }

    public JsonNumber(Number value){
        numberValue = value.toString();
    }

    public String toString(){
        return numberValue;
    }

    public boolean isNumber() {
        return true;
    }
}
