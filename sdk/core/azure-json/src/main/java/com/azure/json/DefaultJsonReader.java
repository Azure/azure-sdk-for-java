// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import com.azure.json.implementation.IoExceptionSupplier;
import com.azure.json.implementation.jackson.core.JsonFactory;
import com.azure.json.implementation.jackson.core.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * Default {@link JsonReader} implementation.
 */
public final class DefaultJsonReader extends JsonReader {
    private static final JsonFactory FACTORY = JsonFactory.builder().build();

    private final JsonParser parser;

    /**
     * Constructs an instance of {@link DefaultJsonReader} from a {@code byte[]}.
     *
     * @param json JSON {@code byte[]}.
     * @return An instance of {@link DefaultJsonReader}.
     * @throws UncheckedIOException If a {@link DefaultJsonReader} wasn't able to be constructed from the JSON
     * {@code byte[]}.
     */
    public static DefaultJsonReader fromBytes(byte[] json) {
        return callWithWrappedIoException(() -> new DefaultJsonReader(FACTORY.createParser(json)));
    }

    /**
     * Constructs an instance of {@link DefaultJsonReader} from a String.
     *
     * @param json JSON String.
     * @return An instance of {@link DefaultJsonReader}.
     * @throws UncheckedIOException If a {@link DefaultJsonReader} wasn't able to be constructed from the JSON String.
     */
    public static DefaultJsonReader fromString(String json) {
        return callWithWrappedIoException(() -> new DefaultJsonReader(FACTORY.createParser(json)));
    }

    /**
     * Constructs an instance of {@link DefaultJsonReader} from an {@link InputStream}.
     *
     * @param json JSON {@link InputStream}.
     * @return An instance of {@link DefaultJsonReader}.
     * @throws UncheckedIOException If a {@link DefaultJsonReader} wasn't able to be constructed from the JSON
     * {@link InputStream}.
     */
    public static DefaultJsonReader fromStream(InputStream json) {
        return callWithWrappedIoException(() -> new DefaultJsonReader(FACTORY.createParser(json)));
    }

    private DefaultJsonReader(JsonParser parser) {
        this.parser = parser;
    }

    @Override
    public JsonToken currentToken() {
        return mapFromParserToken(parser.currentToken());
    }

    @Override
    public JsonToken nextToken() {
        return callWithWrappedIoException(() -> mapFromParserToken(parser.nextToken()));
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
    public void close() throws IOException {
        parser.close();
    }

    /*
     * Maps the Jackson Core JsonToken to the azure-core JsonToken.
     *
     * azure-core doesn't support the EMBEDDED_OBJECT or NOT_AVAILABLE Jackson Core JsonTokens, but those should only
     * be returned by specialty implementations that aren't used.
     */
    private static JsonToken mapFromParserToken(com.azure.json.implementation.jackson.core.JsonToken token) {
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

    private static <T> T callWithWrappedIoException(IoExceptionSupplier<T> func) {
        return func.getWithUncheckedIoException();
    }
}
