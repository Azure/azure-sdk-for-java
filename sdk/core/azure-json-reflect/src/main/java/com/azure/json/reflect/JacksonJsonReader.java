// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.reflect;

import com.azure.json.JsonOptions;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static java.lang.invoke.MethodType.methodType;
import static com.azure.json.reflect.MetaFactoryFactory.createMetaFactory;

final class JacksonJsonReader extends JsonReader {
    private static final Class<?> JACKSON_JSON_TOKEN;
    private static final Object ALLOW_NAN_MAPPED;
    private static final Object JSON_FACTORY;

    private static final JsonFactoryCreateJsonParser JSON_FACTORY_CREATE_JSON_PARSER;
    private static final JsonParserConfigure JSON_PARSER_CONFIGURE;
    private static final JsonParserClose JSON_PARSER_CLOSE;
    private static final JsonParserSkipChildren JSON_PARSER_SKIP_CHILDREN;
    private static final JsonParserNextToken JSON_PARSER_NEXT_TOKEN;
    private static final JsonParserCurrentName JSON_PARSER_CURRENT_NAME;
    private static final JsonParserGetValueAsString JSON_PARSER_GET_VALUE_AS_STRING;
    private static final JsonParserGetBinaryValue JSON_PARSER_GET_BINARY_VALUE;
    private static final JsonParserGetBooleanValue JSON_PARSER_GET_BOOLEAN_VALUE;
    private static final JsonParserGetIntValue JSON_PARSER_GET_INT_VALUE;
    private static final JsonParserGetLongValue JSON_PARSER_GET_LONG_VALUE;
    private static final JsonParserGetFloatValue JSON_PARSER_GET_FLOAT_VALUE;
    private static final JsonParserGetDoubleValue JSON_PARSER_GET_DOUBLE_VALUE;

    static final boolean INITIALIZED;

    static {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();

        Class<?> jacksonJsonToken = null;

        Object allowNaNMapped = null;
        Object jsonFactory = null;

        JsonFactoryCreateJsonParser jsonFactoryCreateJsonParser = null;
        JsonParserConfigure jsonParserConfigure = null;
        JsonParserClose jsonParserClose = null;
        JsonParserSkipChildren jsonParserSkipChildren = null;
        JsonParserNextToken jsonParserNextToken = null;
        JsonParserCurrentName jsonParserCurrentName = null;
        JsonParserGetValueAsString jsonParserGetValueAsString = null;
        JsonParserGetBinaryValue jsonParserGetBinaryValue = null;
        JsonParserGetBooleanValue jsonParserGetBooleanValue = null;
        JsonParserGetIntValue jsonParserGetIntValue = null;
        JsonParserGetLongValue jsonParserGetLongValue = null;
        JsonParserGetFloatValue jsonParserGetFloatValue = null;
        JsonParserGetDoubleValue jsonParserGetDoubleValue = null;

        boolean initialized = false;

        try {
            Class<?> jacksonJsonFactoryClass = Class.forName("com.fasterxml.jackson.core.JsonFactory");
            Class<?> jacksonJsonParserClass = Class.forName("com.fasterxml.jackson.core.JsonParser");

            jacksonJsonToken = Class.forName("com.fasterxml.jackson.core.JsonToken");

            // Get JsonParser.Feature enum value for allowing non-numeric numbers
            Class<?> jsonParserFeature = Arrays.stream(jacksonJsonParserClass.getDeclaredClasses())
                .filter(c -> "Feature".equals(c.getSimpleName()))
                .findAny()
                .orElse(null);
            Class<?> jsonReadFeature = Class.forName("com.fasterxml.jackson.core.json.JsonReadFeature");
            MethodHandle jsonReadFeatureMappedFeature
                = lookup.findVirtual(jsonReadFeature, "mappedFeature", methodType(jsonParserFeature));
            MethodHandle jsonReadFeatureValueOf
                = lookup.findStatic(jsonReadFeature, "valueOf", methodType(jsonReadFeature, String.class));
            allowNaNMapped
                = jsonReadFeatureMappedFeature.invoke(jsonReadFeatureValueOf.invoke("ALLOW_NON_NUMERIC_NUMBERS"));

            jsonFactory = lookup.findConstructor(jacksonJsonFactoryClass, methodType(void.class)).invoke();

            jsonFactoryCreateJsonParser = createMetaFactory("createParser", jacksonJsonFactoryClass,
                methodType(jacksonJsonParserClass, Reader.class), JsonFactoryCreateJsonParser.class,
                methodType(Object.class, Object.class, Reader.class), lookup);
            jsonParserConfigure = createMetaFactory("configure", jacksonJsonParserClass,
                methodType(jacksonJsonParserClass, jsonParserFeature, boolean.class), JsonParserConfigure.class,
                methodType(Object.class, Object.class, Object.class, boolean.class), lookup);
            jsonParserClose = createMetaFactory("close", jacksonJsonParserClass, methodType(void.class),
                JsonParserClose.class, methodType(void.class, Object.class), lookup);
            jsonParserSkipChildren
                = createMetaFactory("skipChildren", jacksonJsonParserClass, methodType(jacksonJsonParserClass),
                    JsonParserSkipChildren.class, methodType(Object.class, Object.class), lookup);
            jsonParserNextToken = createMetaFactory("nextToken", jacksonJsonParserClass, methodType(jacksonJsonToken),
                JsonParserNextToken.class, methodType(Object.class, Object.class), lookup);
            jsonParserCurrentName = createMetaFactory("currentName", jacksonJsonParserClass, methodType(String.class),
                JsonParserCurrentName.class, methodType(String.class, Object.class), lookup);
            jsonParserGetValueAsString
                = createMetaFactory("getValueAsString", jacksonJsonParserClass, methodType(String.class),
                    JsonParserGetValueAsString.class, methodType(String.class, Object.class), lookup);
            jsonParserGetBinaryValue
                = createMetaFactory("getBinaryValue", jacksonJsonParserClass, methodType(byte[].class),
                    JsonParserGetBinaryValue.class, methodType(byte[].class, Object.class), lookup);
            jsonParserGetBooleanValue
                = createMetaFactory("getBooleanValue", jacksonJsonParserClass, methodType(boolean.class),
                    JsonParserGetBooleanValue.class, methodType(boolean.class, Object.class), lookup);
            jsonParserGetIntValue = createMetaFactory("getIntValue", jacksonJsonParserClass, methodType(int.class),
                JsonParserGetIntValue.class, methodType(int.class, Object.class), lookup);
            jsonParserGetLongValue = createMetaFactory("getLongValue", jacksonJsonParserClass, methodType(long.class),
                JsonParserGetLongValue.class, methodType(long.class, Object.class), lookup);
            jsonParserGetFloatValue = createMetaFactory("getFloatValue", jacksonJsonParserClass,
                methodType(float.class), JsonParserGetFloatValue.class, methodType(float.class, Object.class), lookup);
            jsonParserGetDoubleValue
                = createMetaFactory("getDoubleValue", jacksonJsonParserClass, methodType(double.class),
                    JsonParserGetDoubleValue.class, methodType(double.class, Object.class), lookup);

            initialized = true;
        } catch (Throwable e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else if (e instanceof Error) {
                throw (Error) e;
            }
        }

        JACKSON_JSON_TOKEN = jacksonJsonToken;
        ALLOW_NAN_MAPPED = allowNaNMapped;
        JSON_FACTORY = jsonFactory;

        JSON_FACTORY_CREATE_JSON_PARSER = jsonFactoryCreateJsonParser;
        JSON_PARSER_CONFIGURE = jsonParserConfigure;
        JSON_PARSER_CLOSE = jsonParserClose;
        JSON_PARSER_SKIP_CHILDREN = jsonParserSkipChildren;
        JSON_PARSER_NEXT_TOKEN = jsonParserNextToken;
        JSON_PARSER_CURRENT_NAME = jsonParserCurrentName;
        JSON_PARSER_GET_VALUE_AS_STRING = jsonParserGetValueAsString;
        JSON_PARSER_GET_BINARY_VALUE = jsonParserGetBinaryValue;
        JSON_PARSER_GET_BOOLEAN_VALUE = jsonParserGetBooleanValue;
        JSON_PARSER_GET_INT_VALUE = jsonParserGetIntValue;
        JSON_PARSER_GET_LONG_VALUE = jsonParserGetLongValue;
        JSON_PARSER_GET_FLOAT_VALUE = jsonParserGetFloatValue;
        JSON_PARSER_GET_DOUBLE_VALUE = jsonParserGetDoubleValue;

        INITIALIZED = initialized;
    }

    private final Object jacksonJsonParser;
    private JsonToken currentToken;

    private final byte[] jsonBytes;
    private final String jsonString;
    private final boolean resetSupported;
    private final boolean nonNumericNumbersSupported;

    static JsonReader fromBytes(byte[] json, JsonOptions options) throws IOException {
        return new JacksonJsonReader(new InputStreamReader(new ByteArrayInputStream(json), StandardCharsets.UTF_8),
            true, json, null, options);
    }

    static JsonReader fromString(String json, JsonOptions options) throws IOException {
        return new JacksonJsonReader(new StringReader(json), true, null, json, options);
    }

    static JsonReader fromStream(InputStream json, JsonOptions options) throws IOException {
        return new JacksonJsonReader(new InputStreamReader(json, StandardCharsets.UTF_8), false, null, null, options);
    }

    static JsonReader fromReader(Reader reader, JsonOptions options) throws IOException {
        return new JacksonJsonReader(reader, reader.markSupported(), null, null, options);
    }

    private JacksonJsonReader(Reader reader, boolean resetSupported, byte[] jsonBytes, String jsonString,
        JsonOptions options) throws IOException {
        this(reader, resetSupported, jsonBytes, jsonString, options.isNonNumericNumbersSupported());
    }

    private JacksonJsonReader(Reader reader, boolean resetSupported, byte[] jsonBytes, String jsonString,
        boolean nonNumericNumbersSupported) throws IOException {
        if (!INITIALIZED) {
            throw new IllegalStateException("No compatible version of Jackson is present on the classpath.");
        }

        jacksonJsonParser = JSON_FACTORY_CREATE_JSON_PARSER.createParser(JSON_FACTORY, reader);
        // Configure Jackson to support non-numeric numbers
        JSON_PARSER_CONFIGURE.configure(jacksonJsonParser, ALLOW_NAN_MAPPED, nonNumericNumbersSupported);

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
        currentToken = mapToken((Enum<?>) JSON_PARSER_NEXT_TOKEN.nextToken(jacksonJsonParser));
        return currentToken;
    }

    @Override
    public byte[] getBinary() throws IOException {
        // GetBinaryValue cannot handle a Null token
        if (currentToken() == JsonToken.NULL) {
            return null;
        }

        return JSON_PARSER_GET_BINARY_VALUE.getBinaryValue(jacksonJsonParser);
    }

    @Override
    public boolean getBoolean() throws IOException {
        return JSON_PARSER_GET_BOOLEAN_VALUE.getBooleanValue(jacksonJsonParser);
    }

    @Override
    public float getFloat() throws IOException {
        return JSON_PARSER_GET_FLOAT_VALUE.getFloatValue(jacksonJsonParser);
    }

    @Override
    public double getDouble() throws IOException {
        return JSON_PARSER_GET_DOUBLE_VALUE.getDoubleValue(jacksonJsonParser);
    }

    @Override
    public int getInt() throws IOException {
        return JSON_PARSER_GET_INT_VALUE.getIntValue(jacksonJsonParser);
    }

    @Override
    public long getLong() throws IOException {
        return JSON_PARSER_GET_LONG_VALUE.getLongValue(jacksonJsonParser);
    }

    @Override
    public String getString() throws IOException {
        return JSON_PARSER_GET_VALUE_AS_STRING.getValueAsString(jacksonJsonParser);
    }

    @Override
    public String getFieldName() throws IOException {
        return JSON_PARSER_CURRENT_NAME.currentName(jacksonJsonParser);
    }

    @Override
    public void skipChildren() throws IOException {
        JSON_PARSER_SKIP_CHILDREN.skipChildren(jacksonJsonParser);
    }

    @Override
    public JsonReader bufferObject() throws IOException {
        JsonToken currentToken = currentToken();
        if (currentToken == JsonToken.START_OBJECT || currentToken == JsonToken.FIELD_NAME) {
            String json = readRemainingFieldsAsJsonObject();
            return new JacksonJsonReader(new StringReader(json), true, null, json, nonNumericNumbersSupported);
        } else {
            throw new IllegalStateException("Cannot buffer a JSON object from a non-object, non-field name "
                + "starting location. Starting location: " + currentToken());
        }
    }

    @Override
    public boolean isResetSupported() {
        return this.resetSupported;
    }

    @Override
    public JsonReader reset() throws IOException {
        if (!resetSupported) {
            throw new IllegalStateException("'reset' isn't supported by this JsonReader.");
        }

        if (jsonBytes != null) {
            return new JacksonJsonReader(
                new InputStreamReader(new ByteArrayInputStream(jsonBytes), StandardCharsets.UTF_8), true, jsonBytes,
                null, nonNumericNumbersSupported);
        } else {
            return new JacksonJsonReader(new StringReader(jsonString), true, null, jsonString,
                nonNumericNumbersSupported);
        }
    }

    @Override
    public void close() throws IOException {
        JSON_PARSER_CLOSE.close(jacksonJsonParser);
    }

    /*
     * Maps the Jackson JsonToken to azure-json JsonToken
     */
    private JsonToken mapToken(Enum<?> token) {
        if (token == null) {
            return null;
        }

        // Check token is Jackson token
        if (token.getClass() != JACKSON_JSON_TOKEN) {
            throw new IllegalStateException("Unsupported enum, pass a Jackson JsonToken");
        }

        switch (token.name()) {
            case "START_OBJECT":
                return JsonToken.START_OBJECT;

            case "END_OBJECT":
                return JsonToken.END_OBJECT;

            case "START_ARRAY":
                return JsonToken.START_ARRAY;

            case "END_ARRAY":
                return JsonToken.END_ARRAY;

            case "FIELD_NAME":
                return JsonToken.FIELD_NAME;

            case "VALUE_STRING":
                return JsonToken.STRING;

            case "VALUE_NUMBER_INT":
            case "VALUE_NUMBER_FLOAT":
                return JsonToken.NUMBER;

            case "VALUE_TRUE":
            case "VALUE_FALSE":
                return JsonToken.BOOLEAN;

            case "VALUE_NULL":
                return JsonToken.NULL;

            default:
                throw new IllegalStateException("Unsupported token type: '" + token + "'.");
        }
    }

    @FunctionalInterface
    private interface JsonFactoryCreateJsonParser {
        Object createParser(Object jsonFactory, Reader reader) throws IOException;
    }

    @FunctionalInterface
    private interface JsonParserConfigure {
        Object configure(Object jsonParser, Object feature, boolean state);
    }

    @FunctionalInterface
    private interface JsonParserClose {
        void close(Object jsonParser) throws IOException;
    }

    @FunctionalInterface
    private interface JsonParserSkipChildren {
        Object skipChildren(Object jsonParser) throws IOException;
    }

    @FunctionalInterface
    private interface JsonParserNextToken {
        Object nextToken(Object jsonParser) throws IOException;
    }

    @FunctionalInterface
    private interface JsonParserCurrentName {
        String currentName(Object jsonParser) throws IOException;
    }

    @FunctionalInterface
    private interface JsonParserGetValueAsString {
        String getValueAsString(Object jsonParser) throws IOException;
    }

    @FunctionalInterface
    private interface JsonParserGetBinaryValue {
        byte[] getBinaryValue(Object jsonParser) throws IOException;
    }

    @FunctionalInterface
    private interface JsonParserGetBooleanValue {
        boolean getBooleanValue(Object jsonParser) throws IOException;
    }

    @FunctionalInterface
    private interface JsonParserGetIntValue {
        int getIntValue(Object jsonParser) throws IOException;
    }

    @FunctionalInterface
    private interface JsonParserGetLongValue {
        long getLongValue(Object jsonParser) throws IOException;
    }

    @FunctionalInterface
    private interface JsonParserGetFloatValue {
        float getFloatValue(Object jsonParser) throws IOException;
    }

    @FunctionalInterface
    private interface JsonParserGetDoubleValue {
        double getDoubleValue(Object jsonParser) throws IOException;
    }
}
