import com.sun.jdi.InvalidTypeException;

import java.io.StringWriter;

/**
 * Abstract class that defines the basic, abstract methods that all valid JsonElement 
 * types need to define. 
 * 
 * Concrete sub classes of JsonElement should each define valid JSON types. 
 * Currently, the valid JSON types are: object, array, string, number, boolean, 
 * and null. These are defined by the JsonObject, JsonArray, JsonString, JsonNumber, 
 * JsonBoolean, and JsonNull classes respectively. 
 */
public abstract class JsonElement extends StringWriter {
    /** 
     * Default constructor. 
     * TODO: may need to remove this due to design guidelines? Unnecessary having 
     * this constructor defined in the source code if compiler is already adding 
     * this constructor implicitly when no other constructor is defined. 
     */
    public JsonElement() {}

    // All throw unless isArray is true.
    /**
     * 
     * @param element
     * @return
     * @throws InvalidTypeException
     */
    public JsonArray addElement(JsonElement element) throws InvalidTypeException {
        // Case: 
        if(this.isArray()) { return (this.asArray()).addElement(element); } 
        // Case: 
        else { throw new InvalidTypeException(); }
    }

    /**
     * 
     * @param index
     * @param element
     * @return
     * @throws InvalidTypeException
     */
    public JsonArray addElement(int index, JsonElement element) throws InvalidTypeException {
        // Case: 
        if(this.isArray()) { return (this.asArray()).addElement(index, element); } 
        // Case: 
        else { throw new InvalidTypeException(); }
    }

    /**
     * 
     * @param index
     * @param element
     * @return
     * @throws InvalidTypeException
     */
    public JsonArray setElement(int index, JsonElement element) throws InvalidTypeException {
        // Case: 
        if(this.isArray()) { return (this.asArray()).setElement(index, element); } 
        // Case: 
        else { throw new InvalidTypeException(); }
    }

    /**
     * 
     * @param index
     * @return
     * @throws InvalidTypeException
     */
    public JsonElement getElement(int index) throws InvalidTypeException {
        // Case: 
        if(this.isArray()) {return (this.asArray()).getElement(index); } 
        // Case: 
        else { throw new InvalidTypeException(); }
    }

    /**
     * 
     * @param index
     * @return
     * @throws InvalidTypeException
     */
    public JsonElement removeElement(int index) throws InvalidTypeException {
        // Case: 
        if(this.isArray()) { return (this.asArray()).removeElement(index); } 
        // Case: 
        else { throw new InvalidTypeException(); }
    }

    // All throw unless isObject is true.
    /**
     * 
     * @param key
     * @param element
     * @return
     * @throws InvalidTypeException
     */
    public JsonObject addProperty(String key, Object element) throws InvalidTypeException {
        // Case: 
        if(this.isObject()) { return (this.asObject()).addProperty(key, element); } 
        // Case: 
        else { throw new InvalidTypeException(); }
    }
    /**
     * 
     * @param key
     * @param element
     * @return
     * @throws InvalidTypeException
     */
    public JsonObject setProperty(String key, Object element) throws InvalidTypeException {
        // Case: 
        if(this.isObject()) { return (this.asObject()).setProperty(key, element); } 
        // Case: 
        else { throw new InvalidTypeException(); }
    }

    /**
     * 
     * @param key
     * @return
     * @throws InvalidTypeException
     */
    public JsonElement getProperty(String key) throws InvalidTypeException {
        // Case: 
        if(this.isObject()) { return (this.asObject()).getProperty(key); } 
        // Case: 
        else { throw new InvalidTypeException(); }
    }

    /**
     * 
     * @param key
     * @return
     * @throws InvalidTypeException
     */
    public JsonElement removeProperty(String key) throws InvalidTypeException {
        // Case: 
        if(this.isObject()) { return (this.asObject()).removeProperty(key); } 
        // Case: 
        else { throw new InvalidTypeException(); }
    }

    /**
     * 
     * @param value
     * @return
     * @throws InvalidTypeException
     * @throws IllegalArgumentException
     */
    public String getKeyByValue(JsonElement value) throws InvalidTypeException, IllegalArgumentException { 
        // Case: 
        if(this.isObject()) { return (this.asObject()).getKeyByValue(value); } 
        // Case: 
        else { throw new InvalidTypeException(); }
    } 

    /**
     * 
     * @param key
     * @return
     * @throws InvalidTypeException
     */
    public JsonElement getValueByKey(String key) throws InvalidTypeException {
        // Case: 
        if(this.isObject()) { return (this.asObject()).getValueByKey(key); } 
        // Case: 
        else { throw new InvalidTypeException(); }
    }

    // Conversion Methods
    /**
     * @return
     */
    public boolean isArray() { return false; }

    /**
     * @return
     */
    public JsonArray asArray() { return (JsonArray)this; }

    /**
     * @return
     */
    public boolean isObject() { return false; }

    /**
     * @return
     */
    public JsonObject asObject() { return (JsonObject)this; }

    /**
     * @return
     */
    public boolean isBoolean() { return false; }

    /**
     * @return
     */
    public JsonBoolean asBoolean() { return (JsonBoolean)this; }

    /**
     * @return
     */
    public boolean isNull() { return false; }

    /**
     * @return
     */
    public JsonNull asNull() { return (JsonNull)this; }

    /**
     * @return
     */
    public boolean isNumber() { return false; }

    /**
     * @return
     */
    public JsonNumber asNumber() { return (JsonNumber)this; }

    /**
     * @return
     */
    public boolean isString() { return false; }

    /**
     * @return
     */
    public JsonString asString() { return new JsonString(); }

    /**
     * @return String representation of the JsonElement 
     */
    public abstract String toString(); 
}

