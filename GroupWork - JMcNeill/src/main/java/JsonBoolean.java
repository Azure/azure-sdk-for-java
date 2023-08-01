/**
 * Class representing the JSON boolean type 
 */
public class JsonBoolean extends JsonElement {
    /**
     * Stores the String representation of the JsonBoolean object.  
     * Can only be either "true" or "false". 
     */
    private String booleanValue;

    /**
     * Default constructor 
     * Default sets booleanValue to "true" through the other constructor. 
     * TODO: may need to remove this due to design guidelines? May only want to 
     * have the public JsonBoolean(boolean value) constructor. 
     */
    public JsonBoolean() { this(true); }

    /**
     * Constructor used to set the value of the JsonBoolean.  
     * 
     * @param value the boolean value to set the JsonBoolean object to. Either 
     * true or false. If value is true, then booleanValue set to "true"; otherwise, 
     * set to "false" 
     */
    public JsonBoolean(boolean value) { booleanValue = (value)? "true" : "false"; }

    /**
     * Returns String representation of the JsonBoolean object 
     * 
     * @return the booleanValue field, a String representation of this JsonBoolean 
     * object.  
     */
    public String toString() { return booleanValue; }

    /**
     * @return boolean of whether this JsonElement object is of type JsonBoolean. 
     */
    public boolean isBoolean() { return true; }

    /**
     * @return boolean of whether this JsonBoolean object is currently representing 
     * true  
     */
    public boolean isTrue() { return booleanValue.equals("true"); }

    /**
     * @return boolean of whether this JsonBoolean object is currently representing 
     * false 
     */
    public boolean isFalse() { return booleanValue.equals("false"); }
}
