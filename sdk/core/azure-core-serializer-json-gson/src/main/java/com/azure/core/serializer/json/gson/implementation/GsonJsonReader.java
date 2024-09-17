// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonOptions;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.google.gson.TypeAdapter;

import java.io.IOException;
import java.io.Reader;
import java.util.Base64;
import java.util.Objects;

/**
 * GSON-based implementation of {@link JsonReader}.
 */
public final class GsonJsonReader extends JsonReader {
    private static final ClientLogger LOGGER = new ClientLogger(GsonJsonReader.class);

    private final com.google.gson.stream.JsonReader reader;

    private final byte[] jsonBytes;
    private final String jsonString;
    private final boolean resetSupported;
    private final JsonOptions jsonOptions;
    private final boolean typeAdapterContext;

    private JsonToken currentToken;
    private boolean consumed = false;
    private boolean complete = false;
    private int objectDepth = 0;

    /**
     * Creates an instance of {@link JsonReader} based on the passed GSON {@link com.google.gson.stream.JsonReader}.
     *
     * @param reader The GSON JSON reader.
     * @param jsonBytes The bytes representing the JSON, may be null.
     * @param jsonString The string representing the JSON, may be null.
     * @param resetSupported Whether resetting is supported by the {@link JsonReader}.
     * @param options The {@link JsonOptions} used to create the JSON reader.
     */
    public GsonJsonReader(Reader reader, byte[] jsonBytes, String jsonString, boolean resetSupported,
        JsonOptions options) {
        this(createGsonReader(
            Objects.requireNonNull(reader,
                "Cannot create a GSON-based instance of com.azure.json.JsonReader with a null GSON JsonReader."),
            options), jsonBytes, jsonString, resetSupported, options, false);
    }

    private static com.google.gson.stream.JsonReader createGsonReader(Reader reader, JsonOptions options) {
        com.google.gson.stream.JsonReader gsonReader = new com.google.gson.stream.JsonReader(reader);
        gsonReader.setLenient(options == null || options.isNonNumericNumbersSupported() || options.isJsoncSupported());

        return gsonReader;
    }

    /**
     * Creates an instance of {@link JsonReader} based on the passed GSON {@link com.google.gson.stream.JsonReader}.
     *
     * @param reader The GSON JSON reader.
     * @param options The {@link JsonOptions} used to create the JSON reader.
     * @param typeAdapterContext Whether this {@link JsonReader} was created within the context of a
     * {@link TypeAdapter}.
     */
    public GsonJsonReader(com.google.gson.stream.JsonReader reader, JsonOptions options, boolean typeAdapterContext) {
        this(
            Objects.requireNonNull(reader,
                "Cannot create a GSON-based instance of com.azure.json.JsonReader with a null GSON JsonReader."),
            null, null, false, options, typeAdapterContext);
    }

    private GsonJsonReader(com.google.gson.stream.JsonReader reader, byte[] jsonBytes, String jsonString,
        boolean resetSupported, JsonOptions options, boolean typeAdapterContext) {
        this.reader = reader;
        this.jsonBytes = jsonBytes;
        this.jsonString = jsonString;
        this.resetSupported = resetSupported;
        this.jsonOptions = options;
        this.typeAdapterContext = typeAdapterContext;
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
            objectDepth++;
        } else if (currentToken == JsonToken.END_OBJECT) {
            reader.endObject();
            objectDepth--;
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

        // When within a type adapter context special handling needs to be performed.
        // GSON validates that the JSON sub-object stream is complete read when the value is returned from the
        // TypeAdapter, the design by default doesn't call 'endObject' when the end of the sub-object stream is seen.
        // To comply with how TypeAdapter is expected to work, if the context is within a TypeAdapter, the current
        // object depth is 1 (the root object being read), and the current token is end object ('}') eagerly call
        // 'endObject'.
        if (typeAdapterContext && objectDepth == 1 && currentToken == JsonToken.END_OBJECT) {
            reader.endObject();
        }

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
                throw LOGGER.logExceptionAsError(new IllegalStateException("Unsupported token type: '" + token + "'."));
        }
    }
}
