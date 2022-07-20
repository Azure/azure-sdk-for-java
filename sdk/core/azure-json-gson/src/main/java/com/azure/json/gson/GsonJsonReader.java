// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.gson;

import com.azure.json.DefaultJsonReader;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * GSON-based implementation of {@link JsonReader}
 */
public final class GsonJsonReader extends JsonReader {
    private final com.google.gson.stream.JsonReader reader;

    private final byte[] jsonBytes;
    private final String jsonString;
    private final boolean resetSupported;

    private com.google.gson.stream.JsonToken gsonCurrentToken;
    private JsonToken currentToken;

    /**
     * Constructs an instance of {@link GsonJsonReader} from a {@code byte[]}.
     *
     * @param json JSON {@code byte[]}.
     * @return An instance of {@link GsonJsonReader}.
     */
    public static JsonReader fromBytes(byte[] json) {
        return new GsonJsonReader(new InputStreamReader(new ByteArrayInputStream(json), StandardCharsets.UTF_8),
            true, json, null);
    }

    /**
     * Constructs an instance of {@link GsonJsonReader} from a String.
     *
     * @param json JSON String.
     * @return An instance of {@link GsonJsonReader}.
     */
    public static JsonReader fromString(String json) {
        return new GsonJsonReader(new StringReader(json), true, null, json);
    }

    /**
     * Constructs an instance of {@link GsonJsonReader} from an {@link InputStream}.
     *
     * @param json JSON {@link InputStream}.
     * @return An instance of {@link GsonJsonReader}.
     */
    public static JsonReader fromStream(InputStream json) {
        return new GsonJsonReader(new InputStreamReader(json, StandardCharsets.UTF_8), false, null, null);
    }

    private GsonJsonReader(Reader reader, boolean resetSupported, byte[] jsonBytes, String jsonString) {
        this.reader = new com.google.gson.stream.JsonReader(reader);
        this.resetSupported = resetSupported;
        this.jsonBytes = jsonBytes;
        this.jsonString = jsonString;
    }

    @Override
    public JsonToken currentToken() {
        return currentToken;
    }

    @Override
    public JsonToken nextToken() {
        // GSON requires explicitly beginning and ending arrays and objects and consuming null values.
        // The contract of JsonReader implicitly overlooks these properties.
        try {
            if (gsonCurrentToken == com.google.gson.stream.JsonToken.BEGIN_OBJECT) {
                reader.beginObject();
            } else if (gsonCurrentToken == com.google.gson.stream.JsonToken.END_OBJECT) {
                reader.endObject();
            } else if (gsonCurrentToken == com.google.gson.stream.JsonToken.BEGIN_ARRAY) {
                reader.beginArray();
            } else if (gsonCurrentToken == com.google.gson.stream.JsonToken.END_ARRAY) {
                reader.endArray();
            } else if (currentToken == JsonToken.NULL) {
                reader.nextNull();
            }

            gsonCurrentToken = reader.peek();
            currentToken = mapToken(gsonCurrentToken);
            return currentToken;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public byte[] getBinaryValue() {
        try {
            if (currentToken == JsonToken.NULL) {
                reader.nextNull();
                return null;
            } else {
                return Base64.getDecoder().decode(reader.nextString());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean getBooleanValue() {
        try {
            return reader.nextBoolean();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public double getDoubleValue() {
        try {
            return reader.nextDouble();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public float getFloatValue() {
        try {
            return (float) reader.nextDouble();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public int getIntValue() {
        try {
            return reader.nextInt();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public long getLongValue() {
        try {
            return reader.nextLong();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String getStringValue() {
        try {
            if (currentToken == JsonToken.NULL) {
                return null;
            } else {
                return reader.nextString();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String getFieldName() {
        try {
            return reader.nextName();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void skipChildren() {
        try {
            reader.skipValue();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
