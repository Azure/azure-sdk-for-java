/**
 * Class representing the JSON boolean type 
 */
public class JsonBoolean extends JsonElement {
    // Stores the boolean value (true, false) as text in String object 
    private String booleanText;

    // Default constructor 
    // TODO: may need to remove this due to design guidelines? Or perhaps we set 
    // booleanText to a default value, e.g. defaults to "true" 
    public JsonBoolean() {}

    /**
     * Constructor used to set the value of the JsonBoolean.  
     * 
     * @param value the boolean value to set the JsonBoolean object to. Either 
     * true or false. If value is true, then booleanText set to "true"; otherwise, 
     * set to "false" 
     */
    public JsonBoolean(boolean value) { booleanText = (value)? "true" : "false"; }

    /**
     * Returns String representation of the JsonBoolean object 
     * 
     * @return the booleanText field. booleanText already represents the String 
     * representation of a JSON boolean type.  
     */
    public String toString() { return booleanText; }

    /**
     * @return boolean of whether this JsonElement object is of type JsonBoolean. 
     */
    public boolean isBoolean() { return true; }

    /**
     * @return boolean of whether this JsonBoolean object is currently representing 
     * true  
     */
    public boolean isTrue() { return booleanText.equals("true"); }

    /**
     * @return boolean of whether this JsonBoolean object is currently representing 
     * false 
     */
    public boolean isFalse() { return booleanText.equals("false"); }
}
