// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.gson;

import com.azure.json.JsonOptions;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

/**
 * GSON-based implementation of {@link JsonReader}
 */
public final class GsonJsonReader extends JsonReader {
    private final com.google.gson.stream.JsonReader reader;

    private final byte[] jsonBytes;
    private final String jsonString;
    private final boolean resetSupported;
    private final boolean nonNumericNumbersSupported;

    private JsonToken currentToken;
    private boolean consumed = false;
    private boolean complete = false;

    /**
     * Constructs an instance of {@link JsonReader} from a {@code byte[]}.
     *
     * @param json JSON {@code byte[]}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonReader}.
     * @return An instance of {@link JsonReader}.
     * @throws NullPointerException If {@code json} is null.
     */
    static JsonReader fromBytes(byte[] json, JsonOptions options) {
        Objects.requireNonNull(json, "'json' cannot be null.");
        return new GsonJsonReader(new InputStreamReader(new ByteArrayInputStream(json), StandardCharsets.UTF_8), true,
            json, null, options);
    }

    /**
     * Constructs an instance of {@link JsonReader} from a String.
     *
     * @param json JSON String.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonReader}.
     * @return An instance of {@link JsonReader}.
     * @throws NullPointerException If {@code json} is null.
     */
    static JsonReader fromString(String json, JsonOptions options) {
        Objects.requireNonNull(json, "'json' cannot be null.");
        return new GsonJsonReader(new StringReader(json), true, null, json, options);
    }

    /**
     * Constructs an instance of {@link JsonReader} from an {@link InputStream}.
     *
     * @param json JSON {@link InputStream}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonReader}.
     * @return An instance of {@link JsonReader}.
     * @throws NullPointerException If {@code json} is null.
     */
    static JsonReader fromStream(InputStream json, JsonOptions options) {
        Objects.requireNonNull(json, "'json' cannot be null.");
        return new GsonJsonReader(new InputStreamReader(json, StandardCharsets.UTF_8), json.markSupported(), null, null,
            options);
    }

    /**
     * Constructs an instance of {@link GsonJsonReader} from a {@link Reader}.
     *
     * @param json JSON {@link Reader}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonReader}.
     * @return An instance of {@link GsonJsonReader}.
     * @throws NullPointerException If {@code json} is null.
     */
    static JsonReader fromReader(Reader json, JsonOptions options) {
        Objects.requireNonNull(json, "'json' cannot be null.");
        return new GsonJsonReader(json, json.markSupported(), null, null, options);
    }

    private GsonJsonReader(Reader reader, boolean resetSupported, byte[] jsonBytes, String jsonString,
        JsonOptions options) {
        this(reader, resetSupported, jsonBytes, jsonString, options.isNonNumericNumbersSupported());
    }

    private GsonJsonReader(Reader reader, boolean resetSupported, byte[] jsonBytes, String jsonString,
        boolean nonNumericNumbersSupported) {
        this.reader = new com.google.gson.stream.JsonReader(reader);
        this.reader.setLenient(nonNumericNumbersSupported);
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
        if (complete) {
            return currentToken;
        }

        // GSON requires explicitly beginning and ending arrays and objects and consuming null values.
        // The contract of JsonReader implicitly overlooks these properties.
        if (currentToken == JsonToken.START_OBJECT) {
            reader.beginObject();
        } else if (currentToken == JsonToken.END_OBJECT) {
            reader.endObject();
        } else if (currentToken == JsonToken.START_ARRAY) {
            reader.beginArray();
        } else if (currentToken == JsonToken.END_ARRAY) {
            reader.endArray();
        } else if (currentToken == JsonToken.NULL) {
            reader.nextNull();
        }

        if (!consumed && currentToken != null) {
            switch (currentToken) {
                case FIELD_NAME:
                    reader.nextName();
                    break;

                case BOOLEAN:
                    reader.nextBoolean();
                    break;

                case NUMBER:
                    reader.nextDouble();
                    break;

                case STRING:
                    reader.nextString();
                    break;

                default:
                    break;
            }
        }

        com.google.gson.stream.JsonToken gsonToken = reader.peek();
        if (gsonToken == com.google.gson.stream.JsonToken.END_DOCUMENT) {
            complete = true;
        }

        currentToken = mapToken(reader.peek());
        consumed = false;
        return currentToken;
    }

    @Override
    public byte[] getBinary() throws IOException {
        consumed = true;

        if (currentToken == JsonToken.NULL) {
            reader.nextNull();
            return null;
        } else {
            return Base64.getDecoder().decode(reader.nextString());
        }
    }

    @Override
    public boolean getBoolean() throws IOException {
        consumed = true;

        return reader.nextBoolean();
    }

    @Override
    public double getDouble() throws IOException {
        consumed = true;

        return reader.nextDouble();
    }

    @Override
    public float getFloat() throws IOException {
        consumed = true;

        return (float) reader.nextDouble();
    }

    @Override
    public int getInt() throws IOException {
        consumed = true;

        return reader.nextInt();
    }

    @Override
    public long getLong() throws IOException {
        consumed = true;

        return reader.nextLong();
    }

    @Override
    public String getString() throws IOException {
        consumed = true;

        if (currentToken == JsonToken.NULL) {
            return null;
        } else {
            return reader.nextString();
        }
    }

    @Override
    public String getFieldName() throws IOException {
        consumed = true;

        return reader.nextName();
    }

    @Override
    public void skipChildren() throws IOException {
        consumed = true;

        reader.skipValue();
    }

    @Override
    public JsonReader bufferObject() throws IOException {
        if (currentToken == JsonToken.START_OBJECT || currentToken == JsonToken.FIELD_NAME) {
            consumed = true;
            String json = readRemainingFieldsAsJsonObject();
            return new GsonJsonReader(new StringReader(json), true, null, json, nonNumericNumbersSupported);
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
            return new GsonJsonReader(
                new InputStreamReader(new ByteArrayInputStream(jsonBytes), StandardCharsets.UTF_8), true, jsonBytes,
                null, nonNumericNumbersSupported);
        } else {
            return new GsonJsonReader(new StringReader(jsonString), true, null, jsonString, nonNumericNumbersSupported);
        }
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

            case END_DOCUMENT:
                return JsonToken.END_DOCUMENT;

            default:
                throw new IllegalStateException("Unsupported token type: '" + token + "'.");
        }
    }
}
