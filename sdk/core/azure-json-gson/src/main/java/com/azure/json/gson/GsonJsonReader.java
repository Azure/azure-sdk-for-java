// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.gson;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.azure.json.implementation.CheckExceptionUtils.callWithWrappedIoException;
import static com.azure.json.implementation.CheckExceptionUtils.invokeWithWrappedIoException;

/**
 * GSON-based implementation of {@link JsonReader}
 */
public final class GsonJsonReader extends JsonReader {
    private final com.google.gson.stream.JsonReader reader;

    private JsonToken currentToken;

    /**
     * Constructs an instance of {@link GsonJsonReader} from a {@code byte[]}.
     *
     * @param json JSON {@code byte[]}.
     * @return An instance of {@link GsonJsonReader}.
     */
    public static JsonReader fromBytes(byte[] json) {
        return fromStream(new ByteArrayInputStream(json));
    }

    /**
     * Constructs an instance of {@link GsonJsonReader} from a String.
     *
     * @param json JSON String.
     * @return An instance of {@link GsonJsonReader}.
     */
    public static JsonReader fromString(String json) {
        return new GsonJsonReader(new com.google.gson.stream.JsonReader(new StringReader(json)));
    }

    /**
     * Constructs an instance of {@link GsonJsonReader} from an {@link InputStream}.
     *
     * @param json JSON {@link InputStream}.
     * @return An instance of {@link GsonJsonReader}.
     */
    public static JsonReader fromStream(InputStream json) {
        return new GsonJsonReader(new com.google.gson.stream.JsonReader(
            new InputStreamReader(json, StandardCharsets.UTF_8)));
    }

    private GsonJsonReader(com.google.gson.stream.JsonReader reader) {
        this.reader = reader;
    }

    @Override
    public JsonToken currentToken() {
        return currentToken;
    }

    @Override
    public JsonToken nextToken() {
        if (currentToken != null) {
            invokeWithWrappedIoException(reader::skipValue);
        }

        currentToken = callWithWrappedIoException(() -> mapToken(reader.peek()));
        return currentToken;
    }

    @Override
    public byte[] getBinaryValue() {
        if (currentToken == JsonToken.NULL) {
            invokeWithWrappedIoException(reader::nextNull);
            return null;
        } else {
            return Base64.getDecoder().decode(callWithWrappedIoException(reader::nextString));
        }
    }

    @Override
    public boolean getBooleanValue() {
        return callWithWrappedIoException(reader::nextBoolean);
    }

    @Override
    public double getDoubleValue() {
        return callWithWrappedIoException(reader::nextDouble);
    }

    @Override
    public float getFloatValue() {
        return callWithWrappedIoException(reader::nextDouble).floatValue();
    }

    @Override
    public int getIntValue() {
        return callWithWrappedIoException(reader::nextInt);
    }

    @Override
    public long getLongValue() {
        return callWithWrappedIoException(reader::nextLong);
    }

    @Override
    public String getStringValue() {
        if (currentToken == JsonToken.NULL) {
            return null;
        } else {
            return callWithWrappedIoException(reader::nextString);
        }
    }

    @Override
    public String getFieldName() {
        return callWithWrappedIoException(reader::nextName);
    }

    @Override
    public void skipChildren() {
        invokeWithWrappedIoException(reader::skipValue);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    /*
     * Maps the GSON JsonToken to the azure-json JsonToken.
     */
    private static JsonToken mapToken(com.google.gson.stream.JsonToken token) {
        // Special case for when currentToken is called after instantiating the JsonReader.
        if (token == null) {
            return null;
        }

        switch (token) {
            case BEGIN_OBJECT:
                return JsonToken.START_OBJECT;

            case END_OBJECT:
            case END_DOCUMENT:
                return JsonToken.END_OBJECT;

            case BEGIN_ARRAY:
                return JsonToken.START_ARRAY;

            case END_ARRAY:
                return JsonToken.END_ARRAY;

            case NAME:
                return JsonToken.FIELD_NAME;

            case STRING:
                return JsonToken.STRING;

            case NUMBER:
                return JsonToken.NUMBER;

            case BOOLEAN:
                return JsonToken.BOOLEAN;

            case NULL:
                return JsonToken.NULL;

            default:
                throw new IllegalStateException("Unsupported token type: '" + token + "'.");
        }
    }
}
