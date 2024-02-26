// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.reflect;

import com.azure.json.JsonOptions;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

import static java.lang.invoke.MethodType.methodType;
import static com.azure.json.reflect.MetaFactoryFactory.createMetaFactory;

final class GsonJsonReader extends JsonReader {
    private static final Class<?> GSON_JSON_TOKEN_ENUM;

    private static final JsonReaderConstructor JSON_READER_CONSTRUCTOR;
    private static final JsonReaderSetLenient JSON_READER_SET_LENIENT;
    private static final JsonReaderClose JSON_READER_CLOSE;
    private static final JsonReaderPeek JSON_READER_PEEK;
    private static final JsonReaderBeginObject JSON_READER_BEGIN_OBJECT;
    private static final JsonReaderEndObject JSON_READER_END_OBJECT;
    private static final JsonReaderBeginArray JSON_READER_BEGIN_ARRAY;
    private static final JsonReaderEndArray JSON_READER_END_ARRAY;
    private static final JsonReaderNextNull JSON_READER_NEXT_NULL;
    private static final JsonReaderNextName JSON_READER_NEXT_NAME;
    private static final JsonReaderNextString JSON_READER_NEXT_STRING;
    private static final JsonReaderNextBoolean JSON_READER_NEXT_BOOLEAN;
    private static final JsonReaderNextInt JSON_READER_NEXT_INT;
    private static final JsonReaderNextLong JSON_READER_NEXT_LONG;
    private static final JsonReaderNextDouble JSON_READER_NEXT_DOUBLE;
    private static final JsonReaderSkipValue JSON_READER_SKIP_VALUE;

    static final boolean INITIALIZED;

    static {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();

        final MethodType voidMT = methodType(void.class);
        final MethodType voidObjectMT = methodType(void.class, Object.class);

        Class<?> gsonJsonTokenEnum = null;

        JsonReaderConstructor jsonReaderConstructor = null;
        JsonReaderSetLenient jsonReaderSetLenient = null;
        JsonReaderClose jsonReaderClose = null;
        JsonReaderPeek jsonReaderPeek = null;
        JsonReaderBeginObject jsonReaderBeginObject = null;
        JsonReaderEndObject jsonReaderEndObject = null;
        JsonReaderBeginArray jsonReaderBeginArray = null;
        JsonReaderEndArray jsonReaderEndArray = null;
        JsonReaderNextNull jsonReaderNextNull = null;
        JsonReaderNextName jsonReaderNextName = null;
        JsonReaderNextString jsonReaderNextString = null;
        JsonReaderNextBoolean jsonReaderNextBoolean = null;
        JsonReaderNextInt jsonReaderNextInt = null;
        JsonReaderNextLong jsonReaderNextLong = null;
        JsonReaderNextDouble jsonReaderNextDouble = null;
        JsonReaderSkipValue jsonReaderSkipValue = null;

        boolean initialized = false;

        try {
            Class<?> gsonJsonReaderClass = Class.forName("com.google.gson.stream.JsonReader");
            gsonJsonTokenEnum = Class.forName("com.google.gson.stream.JsonToken");

            MethodHandle gsonReaderConstructor
                = lookup.findConstructor(gsonJsonReaderClass, methodType(void.class, Reader.class));
            jsonReaderConstructor = (JsonReaderConstructor) LambdaMetafactory
                .metafactory(lookup, "createJsonReader", methodType(JsonReaderConstructor.class),
                    methodType(Object.class, Reader.class), gsonReaderConstructor, gsonReaderConstructor.type())
                .getTarget()
                .invoke();

            jsonReaderSetLenient
                = createMetaFactory("setLenient", gsonJsonReaderClass, methodType(void.class, boolean.class),
                    JsonReaderSetLenient.class, methodType(void.class, Object.class, boolean.class), lookup);
            jsonReaderClose
                = createMetaFactory("close", gsonJsonReaderClass, voidMT, JsonReaderClose.class, voidObjectMT, lookup);
            jsonReaderPeek = createMetaFactory("peek", gsonJsonReaderClass, methodType(gsonJsonTokenEnum),
                JsonReaderPeek.class, methodType(Object.class, Object.class), lookup);
            jsonReaderBeginObject = createMetaFactory("beginObject", gsonJsonReaderClass, voidMT,
                JsonReaderBeginObject.class, voidObjectMT, lookup);
            jsonReaderEndObject = createMetaFactory("endObject", gsonJsonReaderClass, voidMT, JsonReaderEndObject.class,
                voidObjectMT, lookup);
            jsonReaderBeginArray = createMetaFactory("beginArray", gsonJsonReaderClass, voidMT,
                JsonReaderBeginArray.class, voidObjectMT, lookup);
            jsonReaderEndArray = createMetaFactory("endArray", gsonJsonReaderClass, voidMT, JsonReaderEndArray.class,
                voidObjectMT, lookup);
            jsonReaderNextNull = createMetaFactory("nextNull", gsonJsonReaderClass, voidMT, JsonReaderNextNull.class,
                voidObjectMT, lookup);
            jsonReaderNextName = createMetaFactory("nextName", gsonJsonReaderClass, methodType(String.class),
                JsonReaderNextName.class, methodType(String.class, Object.class), lookup);
            jsonReaderNextString = createMetaFactory("nextString", gsonJsonReaderClass, methodType(String.class),
                JsonReaderNextString.class, methodType(String.class, Object.class), lookup);
            jsonReaderNextBoolean = createMetaFactory("nextBoolean", gsonJsonReaderClass, methodType(boolean.class),
                JsonReaderNextBoolean.class, methodType(boolean.class, Object.class), lookup);
            jsonReaderNextInt = createMetaFactory("nextInt", gsonJsonReaderClass, methodType(int.class),
                JsonReaderNextInt.class, methodType(int.class, Object.class), lookup);
            jsonReaderNextLong = createMetaFactory("nextLong", gsonJsonReaderClass, methodType(long.class),
                JsonReaderNextLong.class, methodType(long.class, Object.class), lookup);
            jsonReaderNextDouble = createMetaFactory("nextDouble", gsonJsonReaderClass, methodType(double.class),
                JsonReaderNextDouble.class, methodType(double.class, Object.class), lookup);
            jsonReaderSkipValue = createMetaFactory("skipValue", gsonJsonReaderClass, voidMT, JsonReaderSkipValue.class,
                voidObjectMT, lookup);

            initialized = true;
        } catch (Throwable e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else if (e instanceof Error) {
                throw (Error) e;
            }
        }

        GSON_JSON_TOKEN_ENUM = gsonJsonTokenEnum;

        JSON_READER_CONSTRUCTOR = jsonReaderConstructor;
        JSON_READER_SET_LENIENT = jsonReaderSetLenient;
        JSON_READER_CLOSE = jsonReaderClose;
        JSON_READER_PEEK = jsonReaderPeek;
        JSON_READER_BEGIN_OBJECT = jsonReaderBeginObject;
        JSON_READER_END_OBJECT = jsonReaderEndObject;
        JSON_READER_BEGIN_ARRAY = jsonReaderBeginArray;
        JSON_READER_END_ARRAY = jsonReaderEndArray;
        JSON_READER_NEXT_NULL = jsonReaderNextNull;
        JSON_READER_NEXT_NAME = jsonReaderNextName;
        JSON_READER_NEXT_STRING = jsonReaderNextString;
        JSON_READER_NEXT_BOOLEAN = jsonReaderNextBoolean;
        JSON_READER_NEXT_INT = jsonReaderNextInt;
        JSON_READER_NEXT_LONG = jsonReaderNextLong;
        JSON_READER_NEXT_DOUBLE = jsonReaderNextDouble;
        JSON_READER_SKIP_VALUE = jsonReaderSkipValue;

        INITIALIZED = initialized;
    }

    private final Object gsonJsonReader;
    private JsonToken currentToken;

    private final byte[] jsonBytes;
    private final String jsonString;
    private final boolean resetSupported;
    private final boolean nonNumericNumbersSupported;

    private boolean consumed = false;
    private boolean complete = false;

    static JsonReader fromBytes(byte[] json, JsonOptions options) {
        Objects.requireNonNull(json, "'json' cannot be null.");
        return new GsonJsonReader(new InputStreamReader(new ByteArrayInputStream(json), StandardCharsets.UTF_8), true,
            json, null, options);
    }

    static JsonReader fromString(String json, JsonOptions options) {
        Objects.requireNonNull(json, "'json' cannot be null.");
        return new GsonJsonReader(new StringReader(json), true, null, json, options);
    }

    static JsonReader fromStream(InputStream json, JsonOptions options) {
        Objects.requireNonNull(json, "'json' cannot be null.");
        return new GsonJsonReader(new InputStreamReader(json, StandardCharsets.UTF_8), json.markSupported(), null, null,
            options);
    }

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
        if (!INITIALIZED) {
            throw new IllegalStateException("No compatible version of Gson is present on the classpath.");
        }

        gsonJsonReader = JSON_READER_CONSTRUCTOR.createJsonReader(reader);
        JSON_READER_SET_LENIENT.setLenient(gsonJsonReader, nonNumericNumbersSupported);

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
            JSON_READER_BEGIN_OBJECT.beginObject(gsonJsonReader);
        } else if (currentToken == JsonToken.END_OBJECT) {
            JSON_READER_END_OBJECT.endObject(gsonJsonReader);
        } else if (currentToken == JsonToken.START_ARRAY) {
            JSON_READER_BEGIN_ARRAY.beginArray(gsonJsonReader);
        } else if (currentToken == JsonToken.END_ARRAY) {
            JSON_READER_END_ARRAY.endArray(gsonJsonReader);
        } else if (currentToken == JsonToken.NULL) {
            JSON_READER_NEXT_NULL.nextNull(gsonJsonReader);
        }

        if (!consumed && currentToken != null) {
            switch (currentToken) {
                case FIELD_NAME:
                    JSON_READER_NEXT_NAME.nextName(gsonJsonReader);
                    break;

                case BOOLEAN:
                    JSON_READER_NEXT_BOOLEAN.nextBoolean(gsonJsonReader);
                    break;

                case NUMBER:
                    JSON_READER_NEXT_DOUBLE.nextDouble(gsonJsonReader);
                    break;

                case STRING:
                    JSON_READER_NEXT_STRING.nextString(gsonJsonReader);
                    break;

                default:
                    break;
            }
        }

        currentToken = mapToken((Enum<?>) JSON_READER_PEEK.peek(gsonJsonReader));

        if (currentToken == JsonToken.END_DOCUMENT) {
            complete = true;
        }

        consumed = false;
        return currentToken;
    }

    @Override
    public byte[] getBinary() throws IOException {
        consumed = true;

        if (currentToken == JsonToken.NULL) {
            JSON_READER_NEXT_NULL.nextNull(gsonJsonReader);
            return null;
        } else {
            return Base64.getDecoder().decode(JSON_READER_NEXT_STRING.nextString(gsonJsonReader));
        }
    }

    @Override
    public boolean getBoolean() throws IOException {
        consumed = true;
        return JSON_READER_NEXT_BOOLEAN.nextBoolean(gsonJsonReader);
    }

    @Override
    public float getFloat() throws IOException {
        consumed = true;
        return (float) JSON_READER_NEXT_DOUBLE.nextDouble(gsonJsonReader);
    }

    @Override
    public double getDouble() throws IOException {
        consumed = true;
        return JSON_READER_NEXT_DOUBLE.nextDouble(gsonJsonReader);
    }

    @Override
    public int getInt() throws IOException {
        consumed = true;
        return JSON_READER_NEXT_INT.nextInt(gsonJsonReader);
    }

    @Override
    public long getLong() throws IOException {
        consumed = true;
        return JSON_READER_NEXT_LONG.nextLong(gsonJsonReader);
    }

    @Override
    public String getString() throws IOException {
        consumed = true;

        if (currentToken == JsonToken.NULL) {
            return null;
        } else {
            return JSON_READER_NEXT_STRING.nextString(gsonJsonReader);
        }
    }

    @Override
    public String getFieldName() throws IOException {
        consumed = true;
        return JSON_READER_NEXT_NAME.nextName(gsonJsonReader);
    }

    @Override
    public void skipChildren() throws IOException {
        consumed = true;
        JSON_READER_SKIP_VALUE.skipValue(gsonJsonReader);
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
    public JsonReader reset() {
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
        JSON_READER_CLOSE.close(gsonJsonReader);
    }

    /*
     * Maps the GSON JsonToken to the azure-json JsonToken.
     */
    private JsonToken mapToken(Enum<?> token) {
        // Special case for when currentToken is called after instantiating the JsonReader.
        if (token == null) {
            return null;
        }

        // Check token is GSON JsonToken
        if (token.getClass() != GSON_JSON_TOKEN_ENUM) {
            throw new IllegalStateException("Unsupported enum, pass a Gson JsonToken");
        }

        switch (token.name()) {
            case "BEGIN_OBJECT":
                return JsonToken.START_OBJECT;

            case "END_OBJECT":
                return JsonToken.END_OBJECT;

            case "BEGIN_ARRAY":
                return JsonToken.START_ARRAY;

            case "END_ARRAY":
                return JsonToken.END_ARRAY;

            case "NAME":
                return JsonToken.FIELD_NAME;

            case "STRING":
                return JsonToken.STRING;

            case "NUMBER":
                return JsonToken.NUMBER;

            case "BOOLEAN":
                return JsonToken.BOOLEAN;

            case "NULL":
                return JsonToken.NULL;

            case "END_DOCUMENT":
                return JsonToken.END_DOCUMENT;

            default:
                throw new IllegalStateException("Unsupported token type: '" + token + "'.");
        }
    }

    @FunctionalInterface
    private interface JsonReaderConstructor {
        Object createJsonReader(Reader reader);
    }

    @FunctionalInterface
    private interface JsonReaderSetLenient {
        void setLenient(Object jsonReader, boolean lenient);
    }

    @FunctionalInterface
    private interface JsonReaderClose {
        void close(Object jsonReader) throws IOException;
    }

    @FunctionalInterface
    private interface JsonReaderPeek {
        Object peek(Object jsonReader) throws IOException;
    }

    @FunctionalInterface
    private interface JsonReaderBeginObject {
        void beginObject(Object jsonReader) throws IOException;
    }

    @FunctionalInterface
    private interface JsonReaderEndObject {
        void endObject(Object jsonReader) throws IOException;
    }

    @FunctionalInterface
    private interface JsonReaderBeginArray {
        void beginArray(Object jsonReader) throws IOException;
    }

    @FunctionalInterface
    private interface JsonReaderEndArray {
        void endArray(Object jsonReader) throws IOException;
    }

    @FunctionalInterface
    private interface JsonReaderNextNull {
        void nextNull(Object jsonReader) throws IOException;
    }

    @FunctionalInterface
    private interface JsonReaderNextName {
        String nextName(Object jsonReader) throws IOException;
    }

    @FunctionalInterface
    private interface JsonReaderNextString {
        String nextString(Object jsonReader) throws IOException;
    }

    @FunctionalInterface
    private interface JsonReaderNextBoolean {
        boolean nextBoolean(Object jsonReader) throws IOException;
    }

    @FunctionalInterface
    private interface JsonReaderNextInt {
        int nextInt(Object jsonReader) throws IOException;
    }

    @FunctionalInterface
    private interface JsonReaderNextLong {
        long nextLong(Object jsonReader) throws IOException;
    }

    @FunctionalInterface
    private interface JsonReaderNextDouble {
        double nextDouble(Object jsonReader) throws IOException;
    }

    @FunctionalInterface
    private interface JsonReaderSkipValue {
        void skipValue(Object jsonReader) throws IOException;
    }
}
