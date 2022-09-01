// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.implementation;

import com.azure.json.JsonOptions;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.json.implementation.jackson.core.JsonFactory;
import com.azure.json.implementation.jackson.core.JsonParser;
import com.azure.json.implementation.jackson.core.json.JsonReadFeature;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UncheckedIOException;

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
     * Constructs an instance of {@link DefaultJsonReader} from a {@code byte[]}.
     *
     * @param json JSON {@code byte[]}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonReader}.
     * @return An instance of {@link DefaultJsonReader}.
     * @throws UncheckedIOException If a {@link DefaultJsonReader} wasn't able to be constructed from the JSON
     * {@code byte[]}.
     */
    public static JsonReader fromBytes(byte[] json, JsonOptions options) {
        try {
            return new DefaultJsonReader(FACTORY.createParser(json), true, json, null, options);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Constructs an instance of {@link DefaultJsonReader} from a String.
     *
     * @param json JSON String.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonWriter}.
     * @return An instance of {@link DefaultJsonReader}.
     * @throws UncheckedIOException If a {@link DefaultJsonReader} wasn't able to be constructed from the JSON String.
     */
    public static JsonReader fromString(String json, JsonOptions options) {
        try {
            return new DefaultJsonReader(FACTORY.createParser(json), true, null, json, options);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Constructs an instance of {@link DefaultJsonReader} from an {@link InputStream}.
     *
     * @param json JSON {@link InputStream}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonWriter}.
     * @return An instance of {@link DefaultJsonReader}.
     * @throws UncheckedIOException If a {@link DefaultJsonReader} wasn't able to be constructed from the JSON
     * {@link InputStream}.
     */
    public static JsonReader fromStream(InputStream json, JsonOptions options) {
        try {
            return new DefaultJsonReader(FACTORY.createParser(json), json.markSupported(), null, null, options);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Constructs an instance of {@link DefaultJsonReader} from a {@link Reader}.
     *
     * @param reader JSON {@link Reader}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonWriter}.
     * @return An instance of {@link DefaultJsonReader}.
     * @throws UncheckedIOException If a {@link DefaultJsonReader} wasn't able to be constructed from the JSON
     * {@link Reader}.
     */
    public static JsonReader fromReader(Reader reader, JsonOptions options) {
        try {
            return new DefaultJsonReader(FACTORY.createParser(reader), reader.markSupported(), null, null, options);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
    public JsonToken nextToken() {
        try {
            currentToken = mapToken(parser.nextToken(), currentToken);
            return currentToken;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public byte[] getBinary() {
        if (currentToken() == JsonToken.NULL) {
            return null;
        } else {
            try {
                return parser.getBinaryValue();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    @Override
    public boolean getBoolean() {
        try {
            return parser.getBooleanValue();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public double getDouble() {
        try {
            return parser.getDoubleValue();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public float getFloat() {
        try {
            return parser.getFloatValue();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public int getInt() {
        try {
            return parser.getIntValue();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public long getLong() {
        try {
            return parser.getLongValue();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String getString() {
        try {
            return parser.getValueAsString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String getFieldName() {
        try {
            return parser.currentName();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void skipChildren() {
        try {
            parser.skipChildren();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public JsonReader bufferObject() {
        JsonToken currentToken = currentToken();
        if (currentToken == JsonToken.START_OBJECT
            || (currentToken == JsonToken.FIELD_NAME && nextToken() == JsonToken.START_OBJECT)) {
            StringBuilder bufferedObject = new StringBuilder();
            readChildren(bufferedObject);
            String json = bufferedObject.toString();
            try {
                return new DefaultJsonReader(FACTORY.createParser(json), true, null, json, nonNumericNumbersSupported);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
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
    public JsonReader reset() {
        if (!resetSupported) {
            throw new IllegalStateException("'reset' isn't supported by this JsonReader.");
        }

        try {
            if (jsonBytes != null) {
                return new DefaultJsonReader(FACTORY.createParser(jsonBytes), true, jsonBytes, null,
                    nonNumericNumbersSupported);
            } else {
                return new DefaultJsonReader(FACTORY.createParser(jsonString), true, null, jsonString,
                    nonNumericNumbersSupported);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
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
    private static JsonToken mapToken(com.azure.json.implementation.jackson.core.JsonToken nextToken,
        JsonToken currentToken) {
        // Special case for when currentToken is called after instantiating the JsonReader.
        if (nextToken == null && currentToken == null) {
            return null;
        } else if (nextToken == null) {
            return JsonToken.END_DOCUMENT;
        }

        switch (nextToken) {
            case START_OBJECT: return JsonToken.START_OBJECT;
            case END_OBJECT: return JsonToken.END_OBJECT;

            case START_ARRAY: return JsonToken.START_ARRAY;
            case END_ARRAY: return JsonToken.END_ARRAY;

            case FIELD_NAME: return JsonToken.FIELD_NAME;
            case VALUE_STRING: return JsonToken.STRING;

            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT:
                return JsonToken.NUMBER;

            case VALUE_TRUE:
            case VALUE_FALSE:
                return JsonToken.BOOLEAN;

            case VALUE_NULL: return JsonToken.NULL;

            default:
                throw new IllegalStateException("Unsupported token type: '" + nextToken + "'.");
        }
    }
}
