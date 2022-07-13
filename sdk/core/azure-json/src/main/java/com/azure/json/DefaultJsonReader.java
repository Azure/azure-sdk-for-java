// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import com.azure.json.implementation.jackson.core.JsonFactory;
import com.azure.json.implementation.jackson.core.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import static com.azure.json.implementation.CheckExceptionUtils.callWithWrappedIoException;

/**
 * Default {@link JsonReader} implementation.
 */
public final class DefaultJsonReader extends JsonReader {
    private static final JsonFactory FACTORY = JsonFactory.builder().build();

    private final JsonParser parser;
    private final byte[] jsonBytes;
    private final String jsonString;
    private final boolean resetSupported;

    /**
     * Constructs an instance of {@link DefaultJsonReader} from a {@code byte[]}.
     *
     * @param json JSON {@code byte[]}.
     * @return An instance of {@link DefaultJsonReader}.
     * @throws UncheckedIOException If a {@link DefaultJsonReader} wasn't able to be constructed from the JSON
     * {@code byte[]}.
     */
    public static JsonReader fromBytes(byte[] json) {
        return callWithWrappedIoException(() ->
            new DefaultJsonReader(FACTORY.createParser(json), true, json, null));
    }

    /**
     * Constructs an instance of {@link DefaultJsonReader} from a String.
     *
     * @param json JSON String.
     * @return An instance of {@link DefaultJsonReader}.
     * @throws UncheckedIOException If a {@link DefaultJsonReader} wasn't able to be constructed from the JSON String.
     */
    public static JsonReader fromString(String json) {
        return callWithWrappedIoException(() ->
            new DefaultJsonReader(FACTORY.createParser(json), true, null, json));
    }

    /**
     * Constructs an instance of {@link DefaultJsonReader} from an {@link InputStream}.
     *
     * @param json JSON {@link InputStream}.
     * @return An instance of {@link DefaultJsonReader}.
     * @throws UncheckedIOException If a {@link DefaultJsonReader} wasn't able to be constructed from the JSON
     * {@link InputStream}.
     */
    public static JsonReader fromStream(InputStream json) {
        return callWithWrappedIoException(() ->
            new DefaultJsonReader(FACTORY.createParser(json), false, null, null));
    }

    private DefaultJsonReader(JsonParser parser, boolean resetSupported, byte[] jsonBytes, String jsonString) {
        this.parser = parser;
        this.resetSupported = resetSupported;
        this.jsonBytes = jsonBytes;
        this.jsonString = jsonString;
    }

    @Override
    public JsonToken currentToken() {
        return mapToken(parser.currentToken());
    }

    @Override
    public JsonToken nextToken() {
        return callWithWrappedIoException(() -> mapToken(parser.nextToken()));
    }

    @Override
    public byte[] getBinaryValue() {
        if (currentToken() == JsonToken.NULL) {
            return null;
        } else {
            return callWithWrappedIoException(parser::getBinaryValue);
        }
    }

    @Override
    public boolean getBooleanValue() {
        return callWithWrappedIoException(parser::getBooleanValue);
    }

    @Override
    public double getDoubleValue() {
        return callWithWrappedIoException(parser::getDoubleValue);
    }

    @Override
    public float getFloatValue() {
        return callWithWrappedIoException(parser::getFloatValue);
    }

    @Override
    public int getIntValue() {
        return callWithWrappedIoException(parser::getIntValue);
    }

    @Override
    public long getLongValue() {
        return callWithWrappedIoException(parser::getLongValue);
    }

    @Override
    public String getStringValue() {
        return callWithWrappedIoException(parser::getValueAsString);
    }

    @Override
    public String getFieldName() {
        return callWithWrappedIoException(parser::currentName);
    }

    @Override
    public void skipChildren() {
        callWithWrappedIoException(parser::skipChildren);
    }

    @Override
    public JsonReader bufferObject() {
        StringBuilder bufferedObject = new StringBuilder();
        if (isStartArrayOrObject()) {
            // If the current token is the beginning of an array or object use JsonReader's readChildren method.
            readChildren(bufferedObject);
        } else if (currentToken() == JsonToken.FIELD_NAME) {
            // Otherwise, we're in a complex case where the reading needs to be handled.

            // Add a starting object token.
            bufferedObject.append("{");

            JsonToken token = currentToken();
            boolean needsComa = false;
            while (token != JsonToken.END_OBJECT) {
                // Appending comas happens in the subsequent loop run to prevent the case of appending comas before
                // the end of the object, ex {"fieldName":true,}
                if (needsComa) {
                    bufferedObject.append(",");
                }

                if (token == JsonToken.FIELD_NAME) {
                    // Field names need to have quotes added and a trailing colon.
                    bufferedObject.append("\"").append(getFieldName()).append("\":");

                    // Comas shouldn't happen after a field name.
                    needsComa = false;
                } else {
                    if (token == JsonToken.STRING) {
                        // String fields need to have quotes added.
                        bufferedObject.append("\"").append(getStringValue()).append("\"");
                    } else if (isStartArrayOrObject()) {
                        // Structures use readChildren.
                        readChildren(bufferedObject);
                    } else {
                        // All other value types use text value.
                        bufferedObject.append(getTextValue());
                    }

                    // Comas should happen after a field value.
                    needsComa = true;
                }

                token = nextToken();
            }

            bufferedObject.append("}");
        } else {
            throw new IllegalStateException("Cannot buffer a JSON object from a non-object, non-field name "
                + "starting location. Starting location: " + currentToken());
        }

        return DefaultJsonReader.fromString(bufferedObject.toString());
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
