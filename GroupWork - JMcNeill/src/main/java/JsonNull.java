import com.fasterxml.jackson.databind.ser.std.StdKeySerializers.Default;

/**
 * Class representing the JSON null type 
 */
public class JsonNull extends JsonElement{
    /**
     * Stores the String representation of the JSON null type. 
     * Always set to "null". Cannot be changed. 
     */
    private final String nullValue = "null";

    /** 
     * Default constructor. 
     * TODO: may need to remove this due to design guidelines? Unnecessary having 
     * this constructor defined in the source code if compiler is already adding 
     * this constructor implicitly when no other constructor is defined. 
     */
    public JsonNull() {}

    /**
     * Returns String representation of the JsonNull object 
     * 
     * @return the nullValue field, a String representation of this JsonNull object. 
     */
    public String toString() { return nullValue; }

    /**
     * @return boolean of whether this JsonElement object is of type JsonNull. 
     */
    public boolean isNull() { return true; }
}
