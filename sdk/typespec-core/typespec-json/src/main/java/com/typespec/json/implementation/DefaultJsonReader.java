// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.json.implementation;

import com.typespec.json.JsonOptions;
import com.typespec.json.JsonReader;
import com.typespec.json.JsonToken;
import com.typespec.json.JsonWriter;
import com.typespec.json.implementation.jackson.core.JsonFactory;
import com.typespec.json.implementation.jackson.core.JsonParser;
import com.typespec.json.implementation.jackson.core.json.JsonReadFeature;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Default {@link JsonReader} implementation.
 */
public final class DefaultJsonReader extends JsonReader {
    private static final JsonFactory FACTORY = JsonFactory.builder().build();

    private final JsonParser parser;
    private final byte[] jsonBytes;
    private final String jsonString;
    private final boolean resetSupported;
    private final boolean nonNumericNumbersSupported;

    private JsonToken currentToken;

    /**
     * Constructs an instance of {@link JsonReader} from a {@code byte[]}.
     *
     * @param json JSON {@code byte[]}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonWriter}.
     * @return An instance of {@link JsonReader}.
     * @throws IOException If a {@link JsonReader} wasn't able to be constructed from the JSON {@code byte[]}.
     */
    public static JsonReader fromBytes(byte[] json, JsonOptions options) throws IOException {
        return new DefaultJsonReader(FACTORY.createParser(json), true, json, null, options);
    }

    /**
     * Constructs an instance of {@link JsonReader} from a String.
     *
     * @param json JSON String.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonWriter}.
     * @return An instance of {@link JsonReader}.
     * @throws IOException If a {@link JsonReader} wasn't able to be constructed from the JSON String.
     */
    public static JsonReader fromString(String json, JsonOptions options) throws IOException {
        return new DefaultJsonReader(FACTORY.createParser(json), true, null, json, options);
    }

    /**
     * Constructs an instance of {@link JsonReader} from an {@link InputStream}.
     *
     * @param json JSON {@link InputStream}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonWriter}.
     * @return An instance of {@link JsonReader}.
     * @throws IOException If a {@link JsonReader} wasn't able to be constructed from the JSON {@link InputStream}.
     */
    public static JsonReader fromStream(InputStream json, JsonOptions options) throws IOException {
        return new DefaultJsonReader(FACTORY.createParser(json), json.markSupported(), null, null, options);
    }

    /**
     * Constructs an instance of {@link DefaultJsonReader} from a {@link Reader}.
     *
     * @param reader JSON {@link Reader}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonWriter}.
     * @return An instance of {@link DefaultJsonReader}.
     * @throws IOException If a {@link DefaultJsonReader} wasn't able to be constructed from the JSON {@link Reader}.
     */
    public static JsonReader fromReader(Reader reader, JsonOptions options) throws IOException {
        return new DefaultJsonReader(FACTORY.createParser(reader), reader.markSupported(), null, null, options);
    }

    private DefaultJsonReader(JsonParser parser, boolean resetSupported, byte[] jsonBytes, String jsonString,
        JsonOptions options) {
        this(parser, resetSupported, jsonBytes, jsonString, options.isNonNumericNumbersSupported());
    }

    private DefaultJsonReader(JsonParser parser, boolean resetSupported, byte[] jsonBytes, String jsonString,
        boolean nonNumericNumbersSupported) {
        this.parser = parser;
        this.parser.configure(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS.mappedFeature(), nonNumericNumbersSupported);
        this.resetSupported = resetSupported;
        this.jsonBytes = jsonBytes;
        this.jsonString = jsonString;
        this.nonNumericNumbersSupported = nonNumericNumbersSupported;
    }

    @Override
    public JsonToken currentToken() {
        return currentToken;
    }

    @Override
    public JsonToken nextToken() throws IOException {
        currentToken = mapToken(parser.nextToken(), currentToken);
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
        if (currentToken == JsonToken.START_OBJECT || currentToken == JsonToken.FIELD_NAME) {
            String json = readRemainingFieldsAsJsonObject();
            return new DefaultJsonReader(FACTORY.createParser(json), true, null, json, nonNumericNumbersSupported);
        } else {
            throw new IllegalStateException("Cannot buffer a JSON object from a non-object, non-field name "
                + "starting location. Starting location: " + currentToken());
        }
    }

    @Override
    public boolean isResetSupported() {
        return resetSupported;
    }

    @Override
    public JsonReader reset() throws IOException {
        if (!resetSupported) {
            throw new IllegalStateException("'reset' isn't supported by this JsonReader.");
        }

        if (jsonBytes != null) {
            return new DefaultJsonReader(FACTORY.createParser(jsonBytes), true, jsonBytes, null,
                nonNumericNumbersSupported);
        } else {
            return new DefaultJsonReader(FACTORY.createParser(jsonString), true, null, jsonString,
                nonNumericNumbersSupported);
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
    private static JsonToken mapToken(com.typespec.json.implementation.jackson.core.JsonToken nextToken,
        JsonToken currentToken) {
        // Special case for when currentToken is called after instantiating the JsonReader.
        if (nextToken == null && currentToken == null) {
            return null;
        } else if (nextToken == null) {
            return JsonToken.END_DOCUMENT;
        }

        switch (nextToken) {
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
                throw new IllegalStateException("Unsupported token type: '" + nextToken + "'.");
        }
    }
}
