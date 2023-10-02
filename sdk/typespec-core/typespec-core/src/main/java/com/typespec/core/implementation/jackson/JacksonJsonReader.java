// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.jackson;

import com.typespec.core.util.logging.ClientLogger;
import com.typespec.json.JsonOptions;
import com.typespec.json.JsonReader;
import com.typespec.json.JsonToken;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;
import java.util.Objects;

// Copied from azure-core-serializer-json-jackson, with minor edits.
/**
 * Jackson-based implementation of {@link JsonReader}.
 */
final class JacksonJsonReader extends JsonReader {
    private static final ClientLogger LOGGER = new ClientLogger(JacksonJsonReader.class);

    private final JsonParser parser;
    private final byte[] jsonBytes;
    private final String jsonString;
    private final boolean resetSupported;
    private final JsonOptions jsonOptions;

    private JsonToken currentToken;


    /**
     * Creates an instance of {@link JacksonJsonReader}.
     *
     * @param parser The {@link JsonParser} that will handle deserialization.
     * @param jsonBytes Bytes that are being parsed by the {@code parser}. May be null if using another source for
     * JSON.
     * @param jsonString String that is being parsed by the {@code parser}. May be null if using another source for
     * JSON.
     * @param resetSupported Whether this {@link JsonReader} can be reset to the beginning of the JSON stream.
     * @param jsonOptions The {@link JsonOptions} used to configure this {@link JacksonJsonReader}.
     * @throws NullPointerException If {@code parser} is null.
     */
    JacksonJsonReader(JsonParser parser, byte[] jsonBytes, String jsonString, boolean resetSupported,
        JsonOptions jsonOptions) {
        this.parser = Objects.requireNonNull(parser,
            "Cannot create a Jackson-based instance of com.azure.json.JsonReader with a null Jackson JsonParser.");
        this.jsonBytes = jsonBytes;
        this.jsonString = jsonString;
        this.resetSupported = resetSupported;
        this.jsonOptions = jsonOptions;
        this.currentToken = mapToken(parser.currentToken(), null);
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
    public void close() throws IOException {
        parser.close();
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
    public float getFloat() throws IOException {
        return parser.getFloatValue();
    }

    @Override
    public double getDouble() throws IOException {
        return parser.getDoubleValue();
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
            return AzureJsonUtils.createReader(json, jsonOptions);
        } else {
            throw LOGGER.logExceptionAsError(new IllegalStateException("Cannot buffer a JSON object from a non-object, "
                + "non-field name starting location. Starting location: " + currentToken()));
        }
    }

    @Override
    public boolean isResetSupported() {
        return resetSupported;
    }

    @Override
    public JsonReader reset() throws IOException {
        if (!resetSupported) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("'reset' isn't supported by this JsonReader."));
        }

        return (jsonBytes != null)
            ? AzureJsonUtils.createReader(jsonBytes, jsonOptions)
            : AzureJsonUtils.createReader(jsonString, jsonOptions);
    }

    /*
     * Maps the Jackson Core JsonToken to the azure-json JsonToken.
     *
     * azure-json doesn't support the EMBEDDED_OBJECT or NOT_AVAILABLE Jackson Core JsonTokens, but those should only
     * be returned by specialty implementations that aren't used.
     */
    private static JsonToken mapToken(com.fasterxml.jackson.core.JsonToken nextToken,
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
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "Unsupported token type: '" + nextToken + "'."));
        }
    }
}
