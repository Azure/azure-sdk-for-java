package com.azure.json;

import java.io.IOException;
import java.io.StringWriter;

/*

TODO: the methods defined in this class for JSON arrays:
    - addElement
    - setElement
    - getElement
    - removeElement
Could be extracted as abstract methods into a separate interface, perhaps called
JsonArrayable or some other more appropriately named interface. The only (arguable)
advantage of having these methods defined in this class the way they currently
are is because they prevent any other sub class of JsonElement from having JSON
array capabilities/functionalities. Helping isolate the functionality to JsonArray.
The disadvantage of extracting to an interface means the functionality is opened
to other classes and could interfere with the current implementations.

TODO: the methods defined in this class for JSON objects:
    - addProperty
    - setProperty
    - getProperty
    - removeProperty
Could be extracted as abstract methods into a separate interface, perhaps called
JsonObjectable or some other more appropriately named interface. The only (arguable)
advantage of having these methods defined in this class the way they currently
are is because they prevent any other sub class of JsonElement from having JSON
object capabilities/functionalities. Helping isolate the functionality to JsonObject.
The disadvantage of extracting to an interface means the functionality is opened
to other classes and could interfere with the current implementations.

TODO: the conversion methods at the bottom of this class may be unnecessary,
especially in the way they are currently defined.


If all three previous TODOs were to be done, then this class would simply be
left with an abstract toString() method and the isX methods.

*/

/*
SUGGESTION: Could rename class to e.g. JsonType, although low priority and hard
to tell what would be the best name here.
*/

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

    //------------------------------------------------------------------------//
    //------------------------ Methods for JsonArray -------------------------//
    // TODO: could be extracted into an interface instead of type checking in //
    // JsonElement. Could create JsonArrayable interface or some other more   //
    // appropriately named interface to be implemented by JsonArray.          //
    //------------------------------------------------------------------------//

    // All throw unless isArray is true.
    /**
     *
     * @param element
     * @return
     * @throws InvalidJsonDataTypeException
     */
    public JsonArray addElement(JsonElement element) throws InvalidJsonDataTypeException {
        // Case:
        if(this.isArray()) { return (this.asArray()).addElement(element); }
        // Case:
        else { throw new InvalidJsonDataTypeException(); }
    }

    /**
     *
     * @param index
     * @param element
     * @return
     * @throws InvalidJsonDataTypeException
     */
    public JsonArray addElement(int index, JsonElement element) throws InvalidJsonDataTypeException {
        // Case:
        if(this.isArray()) { return (this.asArray()).addElement(index, element); }
        // Case:
        else { throw new InvalidJsonDataTypeException(); }
    }

    /**
     *
     * @param index
     * @param element
     * @return
     * @throws InvalidJsonDataTypeException
     */
    public JsonArray setElement(int index, JsonElement element) throws InvalidJsonDataTypeException {
        // Case:
        if(this.isArray()) { return (this.asArray()).setElement(index, element); }
        // Case:
        else { throw new InvalidJsonDataTypeException(); }
    }

    /**
     *
     * @param index
     * @return
     * @throws InvalidJsonDataTypeException
     */
    public JsonElement getElement(int index) throws InvalidJsonDataTypeException {
        // Case:
        if(this.isArray()) {return (this.asArray()).getElement(index); }
        // Case:
        else { throw new InvalidJsonDataTypeException(); }
    }

    /**
     *
     * @param index
     * @return
     * @throws InvalidJsonDataTypeException
     */
    public JsonElement removeElement(int index) throws InvalidJsonDataTypeException {
        // Case:
        if(this.isArray()) { return (this.asArray()).removeElement(index); }
        // Case:
        else { throw new InvalidJsonDataTypeException(); }
    }

    //------------------------------------------------------------------------//
    //------------------------ Methods for JsonObject ------------------------//
    // TODO: could be extracted into an interface instead of type checking in //
    // JsonElement. Could create JsonObjectable interface or some other more  //
    // appropriately named interface to be implemented by JsonObject.         //
    //------------------------------------------------------------------------//

    /**
     *
     * @param key
     * @param element
     * @return
     * @throws InvalidJsonDataTypeException
     */
    public JsonObject addProperty(String key, Object element) throws InvalidJsonDataTypeException {
        // Case:
        if(this.isObject()) { return (this.asObject()).addProperty(key, element); }
        // Case:
        else { throw new InvalidJsonDataTypeException(); }
    }
    /**
     *
     * @param key
     * @param element
     * @return
     * @throws InvalidJsonDataTypeException
     */
    public JsonObject setProperty(String key, Object element) throws InvalidJsonDataTypeException {
        // Case:
        if(this.isObject()) { return (this.asObject()).setProperty(key, element); }
        // Case:
        else { throw new InvalidJsonDataTypeException(); }
    }

    /**
     *
     * @param key
     * @return
     * @throws InvalidJsonDataTypeException
     */
    public JsonElement getProperty(String key) throws InvalidJsonDataTypeException {
        // Case:
        if(this.isObject()) { return (this.asObject()).getProperty(key); }
        // Case:
        else { throw new InvalidJsonDataTypeException(); }
    }

    /**
     *
     * @param key
     * @return
     * @throws InvalidJsonDataTypeException
     */
    public JsonElement removeProperty(String key) throws InvalidJsonDataTypeException {
        // Case:
        if(this.isObject()) { return (this.asObject()).removeProperty(key); }
        // Case:
        else { throw new InvalidJsonDataTypeException(); }
    }

    /**
     *
     * @param value
     * @return
     * @throws InvalidJsonDataTypeException
     * @throws IllegalArgumentException
     */
    public String getKeyByValue(JsonElement value) throws InvalidJsonDataTypeException, IllegalArgumentException {
        // Case:
        if(this.isObject()) { return (this.asObject()).getKeyByValue(value); }
        // Case:
        else { throw new InvalidJsonDataTypeException(); }
    }

    /**
     *
     * @param key
     * @return
     * @throws InvalidJsonDataTypeException
     */
    public JsonElement getValueByKey(String key) throws InvalidJsonDataTypeException {
        // Case:
        if(this.isObject()) { return (this.asObject()).getValueByKey(key); }
        // Case:
        else { throw new InvalidJsonDataTypeException(); }
    }

    /**
     * Abstract method that should be defined in a JsonElement sub class to
     * handle how to represent the given JsonElement as a String.
     *
     * @return String representation of the JsonElement
     */
    public abstract String toString();


    //------------------------------------------------------------------------//
    //--------------- isX Methods (JSON type checking methods) ---------------//
    //------------------------------------------------------------------------//
    // The following isX methods are necessary in order for the sub classes of
    // JsonElement to be able to return false if the type does not match, however
    // each JsonElement MUST override one of these methods to return true for
    // their respective type.

    /**
     * @return boolean on whether the given JsonElement is a JsonArray
     */
    public boolean isArray() { return false; }

    /**
     * @return boolean on whether the given JsonElement is a JsonObject
     */
    public boolean isObject() { return false; }

    /**
     * @return boolean on whether the given JsonElement is a JsonBoolean
     */
    public boolean isBoolean() { return false; }

    /**
     * @return boolean on whether the given JsonElement is a JsonNull
     */
    public boolean isNull() { return false; }

    /**
     * @return boolean on whether the given JsonElement is a JsonNumber
     */
    public boolean isNumber() { return false; }

    /**
     * @return boolean on whether the given JsonElement is a JsonString
     */
    public boolean isString() { return false; }


    //------------------------------------------------------------------------//
    //------------------------ Methods for Conversion ------------------------//
    // TODO: not sure if these asX methods really need to be defined or how   //
    // they will be used in their current implementation.                     //
    //------------------------------------------------------------------------//

    /**
     * @return
     */
    public JsonArray asArray() { return (JsonArray)this; }

    /**
     * @return
     */
    public JsonObject asObject() { return (JsonObject)this; }

    /**
     * @return
     */
    public JsonBoolean asBoolean() { return (JsonBoolean)this; }

    /**
     * @return
     */
    public JsonNull asNull() { return (JsonNull)this; }

    /**
     * @return
     */
    public JsonNumber asNumber() { return (JsonNumber)this; }

    /**
     * @return
     */
    public JsonString asString() { return new JsonString(); }


    public abstract JsonWriter serialize(JsonWriter jsonWriter) throws IOException;

    public String toJson() throws IOException {
        return null;
    }
}

