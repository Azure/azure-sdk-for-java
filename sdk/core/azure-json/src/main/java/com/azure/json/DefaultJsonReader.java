// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import com.azure.json.implementation.jackson.core.JsonFactory;
import com.azure.json.implementation.jackson.core.JsonParser;

import java.io.IOException;
import java.io.InputStream;

/**
 * Default {@link JsonReader} implementation.
 */
public final class DefaultJsonReader extends JsonReader {
    private static final JsonFactory FACTORY = JsonFactory.builder().build();

    private final JsonParser parser;
    private final byte[] jsonBytes;
    private final String jsonString;
    private final boolean resetSupported;

    private JsonToken currentToken;

    /**
     * Constructs an instance of {@link JsonReader} from a {@code byte[]}.
     *
     * @param json JSON {@code byte[]}.
     * @return An instance of {@link JsonReader}.
     * @throws IOException If a {@link JsonReader} wasn't able to be constructed from the JSON {@code byte[]}.
     */
    public static JsonReader fromBytes(byte[] json) throws IOException {
        return new DefaultJsonReader(FACTORY.createParser(json), true, json, null);
    }

    /**
     * Constructs an instance of {@link JsonReader} from a String.
     *
     * @param json JSON String.
     * @return An instance of {@link JsonReader}.
     * @throws IOException If a {@link JsonReader} wasn't able to be constructed from the JSON String.
     */
    public static JsonReader fromString(String json) throws IOException {
        return new DefaultJsonReader(FACTORY.createParser(json), true, null, json);
    }

    /**
     * Constructs an instance of {@link JsonReader} from an {@link InputStream}.
     *
     * @param json JSON {@link InputStream}.
     * @return An instance of {@link JsonReader}.
     * @throws IOException If a {@link JsonReader} wasn't able to be constructed from the JSON {@link InputStream}.
     */
    public static JsonReader fromStream(InputStream json) throws IOException {
        return new DefaultJsonReader(FACTORY.createParser(json), true, null, null);
    }

    private DefaultJsonReader(JsonParser parser, boolean resetSupported, byte[] jsonBytes, String jsonString) {
        this.parser = parser;
        this.resetSupported = resetSupported;
        this.jsonBytes = jsonBytes;
        this.jsonString = jsonString;
    }

    @Override
    public JsonToken currentToken() {
        return currentToken;
    }

    @Override
    public JsonToken nextToken() throws IOException {
        currentToken = mapToken(parser.nextToken());
        return currentToken;
    }

    @Override
    public byte[] getBinary() throws IOException {
        if (currentToken() == JsonToken.NULL) {
            return null;
        } else {
            return parser.getBinaryValue();
        }
    }

    @Override
    public boolean getBoolean() throws IOException {
        return parser.getBooleanValue();
    }

    @Override
    public double getDouble() throws IOException {
        return parser.getDoubleValue();
    }

    @Override
    public float getFloat() throws IOException {
        return parser.getFloatValue();
    }

    @Override
    public int getInt() throws IOException {
        return parser.getIntValue();
    }

    @Override
    public long getLong() throws IOException {
        return parser.getLongValue();
    }

    @Override
    public String getString() throws IOException {
        return parser.getValueAsString();
    }

    @Override
    public String getFieldName() throws IOException {
        return parser.currentName();
    }

    @Override
    public void skipChildren() throws IOException {
        parser.skipChildren();
    }

    @Override
    public JsonReader bufferObject() throws IOException {
        JsonToken currentToken = currentToken();
        if (currentToken == JsonToken.START_OBJECT
            || (currentToken == JsonToken.FIELD_NAME && nextToken() == JsonToken.START_OBJECT)) {
            StringBuilder bufferedObject = new StringBuilder();
            readChildren(bufferedObject);
            return DefaultJsonReader.fromString(bufferedObject.toString());
        } else {
            throw new IllegalStateException("Cannot buffer a JSON object from a non-object, non-field name "
                + "starting location. Starting location: " + currentToken());
        }
    }

    @Override
    public boolean resetSupported() {
        return resetSupported;
    }

    @Override
    public JsonReader reset() throws IOException {
        if (!resetSupported) {
            throw new IllegalStateException("'reset' isn't supported by this JsonReader.");
        }

        if (jsonBytes != null) {
            return DefaultJsonReader.fromBytes(jsonBytes);
        } else {
            return DefaultJsonReader.fromString(jsonString);
        }
    }

    @Override
    public void close() throws IOException {
        parser.close();
    }

    /*
     * Maps the Jackson Core JsonToken to the azure-json JsonToken.
     *
     * azure-json doesn't support the EMBEDDED_OBJECT or NOT_AVAILABLE Jackson Core JsonTokens, but those should only
     * be returned by specialty implementations that aren't used.
     */
    private static JsonToken mapToken(com.azure.json.implementation.jackson.core.JsonToken token) {
        // Special case for when currentToken is called after instantiating the JsonReader.
        if (token == null) {
            return null;
        }

        switch (token) {
            case START_OBJECT:
                return JsonToken.START_OBJECT;

            case END_OBJECT:
                return JsonToken.END_OBJECT;

            case START_ARRAY:
                return JsonToken.START_ARRAY;

            case END_ARRAY:
                return JsonToken.END_ARRAY;

            case FIELD_NAME:
                return JsonToken.FIELD_NAME;

            case VALUE_STRING:
                return JsonToken.STRING;

            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT:
                return JsonToken.NUMBER;

            case VALUE_TRUE:
            case VALUE_FALSE:
                return JsonToken.BOOLEAN;

            case VALUE_NULL:
                return JsonToken.NULL;

            default:
                throw new IllegalStateException("Unsupported token type: '" + token + "'.");
        }
    }
}
