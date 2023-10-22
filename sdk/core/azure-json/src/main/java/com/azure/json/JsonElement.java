package com.azure.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import static com.azure.json.JsonToken.END_DOCUMENT;

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
public abstract class JsonElement {
    /**
     * Default constructor.
     *
     * TODO: may need to remove this due to design guidelines? Unnecessary having
     * this constructor defined in the source code if compiler is already adding
     * this constructor implicitly when no other constructor is defined.
     */
    JsonElement() {}

    /**
     * Abstract method that should be defined in a JsonElement sub class to
     * handle how to serialize the given JsonElement.
     *
     * @param jsonWriter JsonWriter to serialize the JsonElement to.
     * @return JsonWriter after the given JsonElement has been serialized and
     * written to it.
     * @throws IOException thrown by the given JsonElement's serialize implementation
     */
    public abstract JsonWriter serialize(JsonWriter jsonWriter) throws IOException;

    /**
     * Abstract method that should be defined in a JsonElement sub class to
     * handle how to represent the given JsonElement as a String.
     *
     * @return String representation of the JsonElement
     */
    public abstract String toString();

    /**
     * JsonElement it in of itself cannot be converted to JSON. Subclasses of
     * JsonElement are expected to implement toJson.
     *
     * @return String object with the corresponding String representation of the
     * JSON.
     * @throws IOException Thrown by the toJson implementation.
     *
     * TODO: should this be an abstract method?
     */
    public String toJson() throws IOException {
        return null;
    }


    //------------------------------------------------------------------------//
    //------------------------ Methods for JSON deserialzing -----------------//
    //------------------------------------------------------------------------//
    // The following are the methods that build a JsonObject or JsonArray from
    // an injected String, byte[] array, Reader or InputStream.

    public static JsonElement fromString(String json) throws IOException {
        return deserializeOutput(JsonProviders.createReader(json));
    }

    public static JsonElement fromBytes(byte[] json) throws IOException {
        return deserializeOutput(JsonProviders.createReader(json));
    }

    public static JsonElement fromStream(InputStream json) throws IOException {
        return deserializeOutput(JsonProviders.createReader(json));
    }

    public static JsonElement fromReader(Reader json) throws IOException {
        return deserializeOutput(JsonProviders.createReader(json));
    }

    private static JsonElement deserializeOutput(JsonReader jsonReader) throws IOException {

        JsonElement output = null;
        JsonToken token = jsonReader.nextToken();

        while (token != END_DOCUMENT) {

            switch (token) {
                // Case: deserialising top level JSON array
                case START_ARRAY:
                    output = new JsonArray(jsonReader);
                    break;
                // Case: deserialising top level JSON object
                case START_OBJECT:
                    output = new JsonObject(jsonReader);
                    break;
                // Invalid JsonToken token cases:
                // NOTE: previous comment mentioned "In theory the reader takes care of this"
                case END_ARRAY:
                    break;
                //              throw new IOException("Invalid JsonToken.END_ARRAY token read from deserialised JSON. Deserialisation aborted.");
                case END_OBJECT:
                    throw new IOException("Invalid JsonToken.END_OBJECT token read from deserialised JSON. Deserialisation aborted.");
                case FIELD_NAME:
                    throw new IOException("Invalid JsonToken.FIELD_NAME token read from deserialised JSON. Deserialisation aborted.");
                case STRING:
                    throw new IOException("Invalid JsonToken.STRING token read from deserialised JSON. Deserialisation aborted.");
                case NUMBER:
                    throw new IOException("Invalid JsonToken.NUMBER token read from deserialised JSON. Deserialisation aborted.");
                case BOOLEAN:
                    throw new IOException("Invalid JsonToken.BOOLEAN token read from deserialised JSON. Deserialisation aborted.");
                case NULL:
                    throw new IOException("Invalid JsonToken.NULL token read from deserialised JSON. Deserialisation aborted.");
                    // Case: this should never be the case because of the while condition.
                case END_DOCUMENT:
                    throw new IOException("Invalid JsonToken.END_DOCUMENT token read from deserialised JSON. Deserialisation aborted.");
                    // Case: tokens read from JsonReader must be JsonTokens. This
                    // default case would only succeed if an unknown token type is
                    // encountered.
                default:
                    throw new IOException("Invalid token deserialised from JsonReader. Deserialisation aborted.");
            }
            token = jsonReader.nextToken();
        }
        return output;
    }


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
    public boolean isArray() {
        return false;
    }

    /**
     * @return boolean on whether the given JsonElement is a JsonObject
     */
    public boolean isObject() {
        return false;
    }

    /**
     * @return boolean on whether the given JsonElement is a JsonBoolean
     */
    public boolean isBoolean() {
        return false;
    }

    /**
     * @return boolean on whether the given JsonElement is a JsonNull
     */
    public boolean isNull() {
        return false;
    }

    /**
     * @return boolean on whether the given JsonElement is a JsonNumber
     */
    public boolean isNumber() {
        return false;
    }

    /**
     * @return boolean on whether the given JsonElement is a JsonString
     */
    public boolean isString() {
        return false;
    }

    //------------------------------------------------------------------------//
    //------------------------ Methods for Conversion ------------------------//
    // TODO: not sure if these asX methods really need to be defined or how   //
    // they will be used in their current implementation.                     //
    //------------------------------------------------------------------------//

}

