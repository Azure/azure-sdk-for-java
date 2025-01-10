// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.serialization.json.implementation;

import io.clientcore.core.serialization.json.JsonOptions;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonToken;
import io.clientcore.core.serialization.json.JsonWriter;
import io.clientcore.core.serialization.json.implementation.jackson.core.JsonFactory;
import io.clientcore.core.serialization.json.implementation.jackson.core.JsonParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

/**
 * Default {@link JsonReader} implementation.
 */
public final class DefaultJsonReader extends JsonReader {
    private static final JsonFactory FACTORY = new JsonFactory();

    private final JsonParser parser;
    private final Reader jsonReader;
    private final boolean resetSupported;
    private final boolean nonNumericNumbersSupported;
    private final boolean jsoncSupported;

    private JsonToken currentToken;

    /**
     * Constructs an instance of {@link JsonReader} from a {@code byte[]}.
     *
     * @param json JSON {@code byte[]}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonWriter}.
     * @return An instance of {@link JsonReader}.
     * @throws IOException If a {@link JsonReader} could not be constructed from the JSON {@code byte[]}.
     */
    public static JsonReader fromBytes(byte[] json, JsonOptions options) throws IOException {
        return fromStream(new ByteArrayInputStream(json), options);
    }

    /**
     * Constructs an instance of {@link JsonReader} from a String.
     *
     * @param json JSON String.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonWriter}.
     * @return An instance of {@link JsonReader}.
     * @throws IOException If a {@link JsonReader} could not be constructed from the JSON String.
     */
    public static JsonReader fromString(String json, JsonOptions options) throws IOException {
        return fromReader(new StringReader(json), options);
    }

    /**
     * Constructs an instance of {@link JsonReader} from an {@link InputStream}.
     *
     * @param json JSON {@link InputStream}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonWriter}.
     * @return An instance of {@link JsonReader}.
     * @throws IOException If a {@link JsonReader} could not be constructed from the JSON {@link InputStream}.
     */
    public static JsonReader fromStream(InputStream json, JsonOptions options) throws IOException {
        return fromReader(new InputStreamReader(json), options);
    }

    /**
     * Constructs an instance of {@link DefaultJsonReader} from a {@link Reader}.
     *
     * @param reader JSON {@link Reader}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonWriter}.
     * @return An instance of {@link DefaultJsonReader}.
     * @throws IOException If a {@link DefaultJsonReader} could not be constructed from the JSON {@link Reader}.
     */
    public static JsonReader fromReader(Reader reader, JsonOptions options) throws IOException {
        return new DefaultJsonReader(FACTORY.createParser(reader), reader.markSupported(), reader, options);
    }

    private DefaultJsonReader(JsonParser parser, boolean resetSupported, Reader jsonReader, JsonOptions options) {
        this.parser = parser;

        if (options != null) {
            this.parser.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, options.isNonNumericNumbersSupported())
                .configure(JsonParser.Feature.ALLOW_COMMENTS, options.isJsoncSupported());
            this.nonNumericNumbersSupported = options.isNonNumericNumbersSupported();
            this.jsoncSupported = options.isJsoncSupported();
        } else {
            this.parser.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
            this.nonNumericNumbersSupported = true;
            this.jsoncSupported = false;
        }
        this.resetSupported = resetSupported;
        this.jsonReader = jsonReader;
    }

    private DefaultJsonReader(JsonParser parser, boolean resetSupported, Reader jsonReader,
        boolean nonNumericNumbersSupported, boolean jsoncSupported) {
        this.parser = parser.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, nonNumericNumbersSupported)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, jsoncSupported);
        this.resetSupported = resetSupported;
        this.jsonReader = jsonReader;
        this.nonNumericNumbersSupported = nonNumericNumbersSupported;
        this.jsoncSupported = jsoncSupported;
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
        return parser.getCurrentName();
    }

    @Override
    public void skipChildren() throws IOException {
        parser.skipChildren();
    }

    @Override
    public JsonReader bufferObject() throws IOException {
        JsonToken currentToken = currentToken();
        if (currentToken == JsonToken.START_OBJECT || currentToken == JsonToken.FIELD_NAME) {
            Reader jsonReader = new StringReader(readRemainingFieldsAsJsonObject());
            return new DefaultJsonReader(FACTORY.createParser(jsonReader), jsonReader.markSupported(), jsonReader,
                nonNumericNumbersSupported, jsoncSupported);
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

        jsonReader.reset();
        return new DefaultJsonReader(FACTORY.createParser(jsonReader), true, jsonReader, nonNumericNumbersSupported,
            jsoncSupported);
    }

    @Override
    public void close() throws IOException {
        parser.close();
    }

    /*
     * Maps the Jackson Core JsonToken to the core JsonToken.
     *
     * core doesn't support the EMBEDDED_OBJECT or NOT_AVAILABLE Jackson Core JsonTokens, but those should only be
     * returned by specialty implementations that aren't used.
     */
    private static JsonToken mapToken(
        io.clientcore.core.serialization.json.implementation.jackson.core.JsonToken nextToken, JsonToken currentToken) {
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
