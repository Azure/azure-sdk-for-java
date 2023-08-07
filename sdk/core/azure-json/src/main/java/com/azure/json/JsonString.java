package com.azure.json;
/**
 * Class representing the JSON string type
 */
public class JsonString extends JsonElement{
    /**
     * Stores the String representation of the current state of the JsonString
     * object.
     */
    private String stringValue;

    /**
     * Default constructor.
     * TODO: may need to remove this due to design guidelines? May only want to
     * have the public JsonString(String value) constructor.
     */
    public JsonString() {}

    /**
     * Constructor used to explicitly set the string value of the JsonString object.
     * Adds the starting and ending double quotation marks.
     * TODO: may have errors occur when value passed has explicilty included other
     * quotation marks through escape characters.
     *
     * @param value specifies the text string this JsonString object represents
     */
    public JsonString(String value) { this.stringValue = "\"" + value + "\""; }

    /**
     * Returns the String representation of the JsonString object
     *
     * @return the stringValue field which is a String representation of the
     * current state of this JsonString object.
     * TODO: need to decide whether to remove or include the double quotes
     * expliclity added in the public JsonString(String value) constructor. Maybe
     * we could make another method which does/doesn't include them.
     */
    @Override
    public String toString() { return this.stringValue; }

    /**
     * @return boolean of whether this JsonElement object is of type JsonString.
     */
    @Override
    public boolean isString() { return true; };

}
