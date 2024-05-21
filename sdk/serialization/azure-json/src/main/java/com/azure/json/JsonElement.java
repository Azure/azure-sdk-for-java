// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import static com.azure.json.JsonToken.END_DOCUMENT;

/**
 * Abstract class that defines the basic, abstract methods that all valid JsonElement
 * types need to define.
 * Concrete subclasses of JsonElement should each define valid JSON types.
 * Currently, the valid JSON types are: object, array, string, number, boolean,
 * and null. These are defined by the JsonObject, JsonArray, JsonString, JsonNumber,
 * JsonBoolean, and JsonNull classes respectively.
 */
public abstract class JsonElement {
    /**
     * Default constructor is required by some test code, but should not be used
     */
    JsonElement() {
    }

    /**
     * Abstract method that should be defined in a JsonElement subclass to
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
     */
    public String toJson() throws IOException {
        return null;
    }

    //------------------------------------------------------------------------//
    //------------------------ Methods for JSON deserializing -----------------//
    //------------------------------------------------------------------------//
    // The following are the methods that build a JsonObject or JsonArray from
    // an injected String, byte[] array, Reader or InputStream.

    /**
     * Deserializes a JSON element from a String.
     *
     * @param json String to be deserialized.
     * @return JsonElement object that represents the deserialized JSON object.
     * @throws IOException Thrown when the deserialization process fails.
     */
    public static JsonElement fromString(String json) throws IOException {
        return deserializeOutput(JsonProviders.createReader(json));
    }

    /**
     * Deserializes a JSON element from a byte array.
     *
     * @param json byte array to be deserialized.
     * @return JsonElement object that represents the deserialized JSON object.
     * @throws IOException Thrown when the deserialization process fails.
     */
    public static JsonElement fromBytes(byte[] json) throws IOException {
        return deserializeOutput(JsonProviders.createReader(json));
    }

    /**
     * Deserializes a JSON element from an InputStream.
     *
     * @param json InputStream object to be deserialized.
     * @return JsonElement object that represents the deserialized JSON object.
     * @throws IOException Thrown when the deserialization process fails.
     */
    public static JsonElement fromStream(InputStream json) throws IOException {
        return deserializeOutput(JsonProviders.createReader(json));
    }

    /**
     * Deserializes a JSON element from a Reader.
     *
     * @param json Reader object to be deserialized.
     * @return JsonElement object that represents the deserialized JSON object.
     * @throws IOException Thrown when the deserialization process fails.
     */
    public static JsonElement fromReader(Reader json) throws IOException {
        return deserializeOutput(JsonProviders.createReader(json));
    }

    private static JsonElement deserializeOutput(JsonReader jsonReader) throws IOException {
        JsonElement output = null;
        JsonToken token = jsonReader.nextToken();
        boolean elementFound = false;

        // TODO (alzimmer): rework this so that it closes the first object and returns the reader. At the moment what
        //  happens, if there are 2 JSON objects in the string, then the second overwrites the first

        while ((token != END_DOCUMENT) && (!elementFound)) {

            switch (token) {
                // Case: deserialising top level JSON array
                case START_ARRAY:
                    elementFound = true;
                    output = new JsonArray(jsonReader);
                    break;

                // Case: deserialising top level JSON object
                case START_OBJECT:
                    elementFound = true;
                    output = new JsonObject(jsonReader);
                    break;

                // Invalid JsonToken token cases:
                case END_ARRAY:
                    break;

                //              throw new IOException("Invalid JsonToken.END_ARRAY token read from deserialised JSON. Deserialisation aborted.");
                case END_OBJECT:
                    throw new IOException(
                        "Invalid JsonToken.END_OBJECT token read from deserialised JSON. Deserialisation aborted.");

                case FIELD_NAME:
                    throw new IOException(
                        "Invalid JsonToken.FIELD_NAME token read from deserialised JSON. Deserialisation aborted.");

                case STRING:
                    throw new IOException(
                        "Invalid JsonToken.STRING token read from deserialised JSON. Deserialisation aborted.");

                case NUMBER:
                    throw new IOException(
                        "Invalid JsonToken.NUMBER token read from deserialised JSON. Deserialisation aborted.");

                case BOOLEAN:
                    throw new IOException(
                        "Invalid JsonToken.BOOLEAN token read from deserialised JSON. Deserialisation aborted.");

                case NULL:
                    throw new IOException(
                        "Invalid JsonToken.NULL token read from deserialised JSON. Deserialisation aborted.");

                // Case: this should never be the case because of the while condition.
                case END_DOCUMENT:
                    throw new IOException(
                        "Invalid JsonToken.END_DOCUMENT token read from deserialised JSON. Deserialisation aborted.");

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
    // The following isX methods are necessary in order for the subclasses of
    // JsonElement to be able to return false if the type does not match, however
    // each JsonElement MUST override one of these methods to return true for
    // their respective type.

    /**
     * Whether the given JsonElement is a JsonArray
     *
     * @return boolean on whether the given JsonElement is a JsonArray
     */
    public boolean isArray() {
        return false;
    }

    /**
     * Whether the given JsonElement is a JsonObject
     *
     * @return boolean on whether the given JsonElement is a JsonObject
     */
    public boolean isObject() {
        return false;
    }

    /**
     * Whether the given JsonElement is a JsonBoolean
     *
     * @return boolean on whether the given JsonElement is a JsonBoolean
     */
    public boolean isBoolean() {
        return false;
    }

    /**
     * Whether the given JsonElement is a JsonNull
     *
     * @return boolean on whether the given JsonElement is a JsonNull
     */
    public boolean isNull() {
        return false;
    }

    /**
     * Whether the given JsonElement is a JsonNumber
     *
     * @return boolean on whether the given JsonElement is a JsonNumber
     */
    public boolean isNumber() {
        return false;
    }

    /**
     * Whether the given JsonElement is a JsonString
     *
     * @return boolean on whether the given JsonElement is a JsonString
     */
    public boolean isString() {
        return false;
    }

}
