// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.reflect;

import com.azure.json.JsonOptions;
import com.azure.json.JsonWriteContext;
import com.azure.json.JsonWriter;
import com.azure.json.JsonToken;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

import static java.lang.invoke.MethodType.methodType;
import static com.azure.json.reflect.MetaFactoryFactory.createMetaFactory;

final class JacksonJsonWriter extends JsonWriter {
    private static final Object ALLOW_NAN_MAPPED;
    private static final Object JSON_FACTORY;

    private static final JsonFactoryCreateJsonGenerator JSON_FACTORY_CREATE_JSON_GENERATOR;
    private static final JsonGeneratorWriteRawValue JSON_GENERATOR_WRITE_RAW_VALUE;
    private static final JsonGeneratorFlush JSON_GENERATOR_FLUSH;
    private static final JsonGeneratorClose JSON_GENERATOR_CLOSE;
    private static final JsonGeneratorWriteStartObject JSON_GENERATOR_WRITE_START_OBJECT;
    private static final JsonGeneratorWriteEndObject JSON_GENERATOR_WRITE_END_OBJECT;
    private static final JsonGeneratorWriteStartArray JSON_GENERATOR_WRITE_START_ARRAY;
    private static final JsonGeneratorWriteEndArray JSON_GENERATOR_WRITE_END_ARRAY;
    private static final JsonGeneratorWriteFieldName JSON_GENERATOR_WRITE_FIELD_NAME;
    private static final JsonGeneratorWriteNull JSON_GENERATOR_WRITE_NULL;
    private static final JsonGeneratorWriteBinary JSON_GENERATOR_WRITE_BINARY;
    private static final JsonGeneratorWriteBoolean JSON_GENERATOR_WRITE_BOOLEAN;
    private static final JsonGeneratorWriteDouble JSON_GENERATOR_WRITE_DOUBLE;
    private static final JsonGeneratorWriteFloat JSON_GENERATOR_WRITE_FLOAT;
    private static final JsonGeneratorWriteInt JSON_GENERATOR_WRITE_INT;
    private static final JsonGeneratorWriteLong JSON_GENERATOR_WRITE_LONG;
    private static final JsonGeneratorWriteString JSON_GENERATOR_WRITE_STRING;
    private static final JsonGeneratorConfigure JSON_GENERATOR_CONFIGURE;

    static final boolean INITIALIZED;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        Object allowNaNMapped = null;
        Object jsonFactory = null;

        JsonFactoryCreateJsonGenerator jsonFactoryCreateJsonGenerator = null;
        JsonGeneratorWriteRawValue jsonGeneratorWriteRawValue = null;
        JsonGeneratorFlush jsonGeneratorFlush = null;
        JsonGeneratorClose jsonGeneratorClose = null;
        JsonGeneratorWriteStartObject jsonGeneratorWriteStartObject = null;
        JsonGeneratorWriteEndObject jsonGeneratorWriteEndObject = null;
        JsonGeneratorWriteStartArray jsonGeneratorWriteStartArray = null;
        JsonGeneratorWriteEndArray jsonGeneratorWriteEndArray = null;
        JsonGeneratorWriteFieldName jsonGeneratorWriteFieldName = null;
        JsonGeneratorWriteNull jsonGeneratorWriteNull = null;
        JsonGeneratorWriteBinary jsonGeneratorWriteBinary = null;
        JsonGeneratorWriteBoolean jsonGeneratorWriteBoolean = null;
        JsonGeneratorWriteDouble jsonGeneratorWriteDouble = null;
        JsonGeneratorWriteFloat jsonGeneratorWriteFloat = null;
        JsonGeneratorWriteInt jsonGeneratorWriteInt = null;
        JsonGeneratorWriteLong jsonGeneratorWriteLong = null;
        JsonGeneratorWriteString jsonGeneratorWriteString = null;
        JsonGeneratorConfigure jsonGeneratorConfigure = null;

        boolean initialized = false;

        try {
            Class<?> jacksonJsonFactoryClass = Class.forName("com.fasterxml.jackson.core.JsonFactory");
            Class<?> jacksonJsonGeneratorClass = Class.forName("com.fasterxml.jackson.core.JsonGenerator");

            // Get JsonGenerator.Feature enum value for allowing non-numeric numbers
            Class<?> jsonGeneratorFeature = Arrays.stream(jacksonJsonGeneratorClass.getDeclaredClasses())
                .filter(c -> "Feature".equals(c.getSimpleName()))
                .findAny()
                .orElse(null);
            Class<?> jsonWriteFeature = Class.forName("com.fasterxml.jackson.core.json.JsonWriteFeature");
            MethodHandle jsonWriteFeatureMappedFeature
                = lookup.findVirtual(jsonWriteFeature, "mappedFeature", methodType(jsonGeneratorFeature));
            MethodHandle jsonWriteFeatureValueOf
                = lookup.findStatic(jsonWriteFeature, "valueOf", methodType(jsonWriteFeature, String.class));
            allowNaNMapped
                = jsonWriteFeatureMappedFeature.invoke(jsonWriteFeatureValueOf.invoke("WRITE_NAN_AS_STRINGS"));

            jsonFactory = lookup.findConstructor(jacksonJsonFactoryClass, methodType(void.class)).invoke();

            MethodType voidMT = methodType(void.class);
            MethodType voidStringMt = methodType(void.class, String.class);
            MethodType voidObjectMT = methodType(void.class, Object.class);

            jsonFactoryCreateJsonGenerator = createMetaFactory("createGenerator", jacksonJsonFactoryClass,
                methodType(jacksonJsonGeneratorClass, Writer.class), JsonFactoryCreateJsonGenerator.class,
                methodType(Object.class, Object.class, Writer.class), lookup);
            jsonGeneratorWriteRawValue = createMetaFactory("writeRawValue", jacksonJsonGeneratorClass, voidStringMt,
                JsonGeneratorWriteRawValue.class, methodType(void.class, Object.class, String.class), lookup);
            jsonGeneratorFlush = createMetaFactory("flush", jacksonJsonGeneratorClass, voidMT, JsonGeneratorFlush.class,
                voidObjectMT, lookup);
            jsonGeneratorClose = createMetaFactory("close", jacksonJsonGeneratorClass, voidMT, JsonGeneratorClose.class,
                voidObjectMT, lookup);
            jsonGeneratorWriteStartObject = createMetaFactory("writeStartObject", jacksonJsonGeneratorClass, voidMT,
                JsonGeneratorWriteStartObject.class, voidObjectMT, lookup);
            jsonGeneratorWriteEndObject = createMetaFactory("writeEndObject", jacksonJsonGeneratorClass, voidMT,
                JsonGeneratorWriteEndObject.class, voidObjectMT, lookup);
            jsonGeneratorWriteStartArray = createMetaFactory("writeStartArray", jacksonJsonGeneratorClass, voidMT,
                JsonGeneratorWriteStartArray.class, voidObjectMT, lookup);
            jsonGeneratorWriteEndArray = createMetaFactory("writeEndArray", jacksonJsonGeneratorClass, voidMT,
                JsonGeneratorWriteEndArray.class, voidObjectMT, lookup);
            jsonGeneratorWriteFieldName = createMetaFactory("writeFieldName", jacksonJsonGeneratorClass, voidStringMt,
                JsonGeneratorWriteFieldName.class, methodType(void.class, Object.class, String.class), lookup);
            jsonGeneratorWriteNull = createMetaFactory("writeNull", jacksonJsonGeneratorClass, voidMT,
                JsonGeneratorWriteNull.class, voidObjectMT, lookup);
            jsonGeneratorWriteBinary
                = createMetaFactory("writeBinary", jacksonJsonGeneratorClass, methodType(void.class, byte[].class),
                    JsonGeneratorWriteBinary.class, methodType(void.class, Object.class, byte[].class), lookup);
            jsonGeneratorWriteBoolean
                = createMetaFactory("writeBoolean", jacksonJsonGeneratorClass, methodType(void.class, boolean.class),
                    JsonGeneratorWriteBoolean.class, methodType(void.class, Object.class, boolean.class), lookup);
            jsonGeneratorWriteDouble
                = createMetaFactory("writeNumber", jacksonJsonGeneratorClass, methodType(void.class, double.class),
                    JsonGeneratorWriteDouble.class, methodType(void.class, Object.class, double.class), lookup);
            jsonGeneratorWriteFloat
                = createMetaFactory("writeNumber", jacksonJsonGeneratorClass, methodType(void.class, float.class),
                    JsonGeneratorWriteFloat.class, methodType(void.class, Object.class, float.class), lookup);
            jsonGeneratorWriteInt
                = createMetaFactory("writeNumber", jacksonJsonGeneratorClass, methodType(void.class, int.class),
                    JsonGeneratorWriteInt.class, methodType(void.class, Object.class, int.class), lookup);
            jsonGeneratorWriteLong
                = createMetaFactory("writeNumber", jacksonJsonGeneratorClass, methodType(void.class, long.class),
                    JsonGeneratorWriteLong.class, methodType(void.class, Object.class, long.class), lookup);
            jsonGeneratorWriteString = createMetaFactory("writeString", jacksonJsonGeneratorClass, voidStringMt,
                JsonGeneratorWriteString.class, methodType(void.class, Object.class, String.class), lookup);
            jsonGeneratorConfigure = createMetaFactory("configure", jacksonJsonGeneratorClass,
                methodType(jacksonJsonGeneratorClass, jsonGeneratorFeature, boolean.class),
                JsonGeneratorConfigure.class, methodType(Object.class, Object.class, Object.class, boolean.class),
                lookup);

            initialized = true;
        } catch (Throwable e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else if (e instanceof Error) {
                throw (Error) e;
            }
        }

        ALLOW_NAN_MAPPED = allowNaNMapped;
        JSON_FACTORY = jsonFactory;

        JSON_FACTORY_CREATE_JSON_GENERATOR = jsonFactoryCreateJsonGenerator;
        JSON_GENERATOR_WRITE_RAW_VALUE = jsonGeneratorWriteRawValue;
        JSON_GENERATOR_FLUSH = jsonGeneratorFlush;
        JSON_GENERATOR_CLOSE = jsonGeneratorClose;
        JSON_GENERATOR_WRITE_START_OBJECT = jsonGeneratorWriteStartObject;
        JSON_GENERATOR_WRITE_END_OBJECT = jsonGeneratorWriteEndObject;
        JSON_GENERATOR_WRITE_START_ARRAY = jsonGeneratorWriteStartArray;
        JSON_GENERATOR_WRITE_END_ARRAY = jsonGeneratorWriteEndArray;
        JSON_GENERATOR_WRITE_FIELD_NAME = jsonGeneratorWriteFieldName;
        JSON_GENERATOR_WRITE_NULL = jsonGeneratorWriteNull;
        JSON_GENERATOR_WRITE_BINARY = jsonGeneratorWriteBinary;
        JSON_GENERATOR_WRITE_BOOLEAN = jsonGeneratorWriteBoolean;
        JSON_GENERATOR_WRITE_DOUBLE = jsonGeneratorWriteDouble;
        JSON_GENERATOR_WRITE_FLOAT = jsonGeneratorWriteFloat;
        JSON_GENERATOR_WRITE_INT = jsonGeneratorWriteInt;
        JSON_GENERATOR_WRITE_LONG = jsonGeneratorWriteLong;
        JSON_GENERATOR_WRITE_STRING = jsonGeneratorWriteString;
        JSON_GENERATOR_CONFIGURE = jsonGeneratorConfigure;

        INITIALIZED = initialized;
    }

    private final Object jacksonGenerator;
    JsonWriteContext context = JsonWriteContext.ROOT;

    static JsonWriter toStream(OutputStream json, JsonOptions options) throws IOException {
        Objects.requireNonNull(json, "'json' cannot be null.");
        return new JacksonJsonWriter(new OutputStreamWriter(json, StandardCharsets.UTF_8), options);
    }

    static JsonWriter toWriter(Writer json, JsonOptions options) throws IOException {
        Objects.requireNonNull(json, "'json' cannot be null.");
        return new JacksonJsonWriter(json, options);
    }

    private JacksonJsonWriter(Writer writer, JsonOptions options) throws IOException {
        if (!INITIALIZED) {
            throw new IllegalStateException("No compatible version of Jackson is present on the classpath.");
        }

        jacksonGenerator = JSON_FACTORY_CREATE_JSON_GENERATOR.createGenerator(JSON_FACTORY, writer);
        // Configure Jackson to support non-numeric numbers
        JSON_GENERATOR_CONFIGURE.configure(jacksonGenerator, ALLOW_NAN_MAPPED, options.isNonNumericNumbersSupported());
    }

    @Override
    public JsonWriteContext getWriteContext() {
        return context;
    }

    @Override
    public void close() throws IOException {
        if (context != JsonWriteContext.COMPLETED) {
            throw new IllegalStateException("Writing of the JSON object must be completed before the writer can be "
                + "closed. Current writing state is '" + context.getWriteState() + "'.");
        }
        JSON_GENERATOR_CLOSE.close(jacksonGenerator);
    }

    @Override
    public JsonWriter flush() throws IOException {
        JSON_GENERATOR_FLUSH.flush(jacksonGenerator);
        return this;
    }

    @Override
    public JsonWriter writeStartObject() throws IOException {
        context.validateToken(JsonToken.START_OBJECT);
        JSON_GENERATOR_WRITE_START_OBJECT.writeStartObject(jacksonGenerator);
        context = context.updateContext(JsonToken.START_OBJECT);
        return this;
    }

    @Override
    public JsonWriter writeEndObject() throws IOException {
        context.validateToken(JsonToken.END_OBJECT);
        JSON_GENERATOR_WRITE_END_OBJECT.writeEndObject(jacksonGenerator);
        context = context.updateContext(JsonToken.END_OBJECT);
        return this;
    }

    @Override
    public JsonWriter writeStartArray() throws IOException {
        context.validateToken(JsonToken.START_ARRAY);
        JSON_GENERATOR_WRITE_START_ARRAY.writeStartArray(jacksonGenerator);
        context = context.updateContext(JsonToken.START_ARRAY);
        return this;
    }

    @Override
    public JsonWriter writeEndArray() throws IOException {
        context.validateToken(JsonToken.END_ARRAY);
        JSON_GENERATOR_WRITE_END_ARRAY.writeEndArray(jacksonGenerator);
        context = context.updateContext(JsonToken.END_ARRAY);
        return this;
    }

    @Override
    public JsonWriter writeFieldName(String fieldName) throws IOException {
        Objects.requireNonNull(fieldName, "'fieldName' cannot be null.");
        context.validateToken(JsonToken.FIELD_NAME);
        JSON_GENERATOR_WRITE_FIELD_NAME.writeFieldName(jacksonGenerator, fieldName);
        context = context.updateContext(JsonToken.FIELD_NAME);
        return this;
    }

    @Override
    public JsonWriter writeBinary(byte[] value) throws IOException {
        context.validateToken(JsonToken.STRING);
        if (value == null) {
            JSON_GENERATOR_WRITE_NULL.writeNull(jacksonGenerator);
        } else {
            JSON_GENERATOR_WRITE_BINARY.writeBinary(jacksonGenerator, value);
        }
        context = context.updateContext(JsonToken.STRING);
        return this;
    }

    @Override
    public JsonWriter writeBoolean(boolean value) throws IOException {
        context.validateToken(JsonToken.BOOLEAN);
        JSON_GENERATOR_WRITE_BOOLEAN.writeBoolean(jacksonGenerator, value);
        context = context.updateContext(JsonToken.BOOLEAN);
        return this;
    }

    @Override
    public JsonWriter writeDouble(double value) throws IOException {
        context.validateToken(JsonToken.NUMBER);
        JSON_GENERATOR_WRITE_DOUBLE.writeNumber(jacksonGenerator, value);
        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeFloat(float value) throws IOException {
        context.validateToken(JsonToken.NUMBER);
        JSON_GENERATOR_WRITE_FLOAT.writeNumber(jacksonGenerator, value);
        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeInt(int value) throws IOException {
        context.validateToken(JsonToken.NUMBER);
        JSON_GENERATOR_WRITE_INT.writeNumber(jacksonGenerator, value);
        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeLong(long value) throws IOException {
        context.validateToken(JsonToken.NUMBER);
        JSON_GENERATOR_WRITE_LONG.writeNumber(jacksonGenerator, value);
        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeNull() throws IOException {
        context.validateToken(JsonToken.NULL);
        JSON_GENERATOR_WRITE_NULL.writeNull(jacksonGenerator);
        context = context.updateContext(JsonToken.NULL);
        return this;
    }

    @Override
    public JsonWriter writeString(String value) throws IOException {
        context.validateToken(JsonToken.STRING);
        JSON_GENERATOR_WRITE_STRING.writeString(jacksonGenerator, value);
        context = context.updateContext(JsonToken.STRING);
        return this;
    }

    @Override
    public JsonWriter writeRawValue(String value) throws IOException {
        Objects.requireNonNull(value, "'value' cannot be null.");
        context.validateToken(JsonToken.STRING);
        JSON_GENERATOR_WRITE_RAW_VALUE.writeRawValue(jacksonGenerator, value);
        context = context.updateContext(JsonToken.STRING);
        return this;
    }

    @FunctionalInterface
    private interface JsonFactoryCreateJsonGenerator {
        Object createGenerator(Object jsonFactory, Writer writer) throws IOException;
    }

    @FunctionalInterface
    private interface JsonGeneratorWriteRawValue {
        void writeRawValue(Object jsonWriter, String value) throws IOException;
    }

    @FunctionalInterface
    private interface JsonGeneratorFlush {
        void flush(Object jsonWriter) throws IOException;
    }

    @FunctionalInterface
    private interface JsonGeneratorClose {
        void close(Object jsonWriter) throws IOException;
    }

    @FunctionalInterface
    private interface JsonGeneratorWriteStartObject {
        void writeStartObject(Object jsonWriter) throws IOException;
    }

    @FunctionalInterface
    private interface JsonGeneratorWriteEndObject {
        void writeEndObject(Object jsonWriter) throws IOException;
    }

    @FunctionalInterface
    private interface JsonGeneratorWriteStartArray {
        void writeStartArray(Object jsonWriter) throws IOException;
    }

    @FunctionalInterface
    private interface JsonGeneratorWriteEndArray {
        void writeEndArray(Object jsonWriter) throws IOException;
    }

    @FunctionalInterface
    private interface JsonGeneratorWriteFieldName {
        void writeFieldName(Object jsonWriter, String fieldName) throws IOException;
    }

    @FunctionalInterface
    private interface JsonGeneratorWriteNull {
        void writeNull(Object jsonWriter) throws IOException;
    }

    @FunctionalInterface
    private interface JsonGeneratorWriteBinary {
        void writeBinary(Object jsonWriter, byte[] value) throws IOException;
    }

    @FunctionalInterface
    private interface JsonGeneratorWriteBoolean {
        void writeBoolean(Object jsonWriter, boolean value) throws IOException;
    }

    @FunctionalInterface
    private interface JsonGeneratorWriteDouble {
        void writeNumber(Object jsonWriter, double value) throws IOException;
    }

    @FunctionalInterface
    private interface JsonGeneratorWriteFloat {
        void writeNumber(Object jsonWriter, float value) throws IOException;
    }

    @FunctionalInterface
    private interface JsonGeneratorWriteInt {
        void writeNumber(Object jsonWriter, int value) throws IOException;
    }

    @FunctionalInterface
    private interface JsonGeneratorWriteLong {
        void writeNumber(Object jsonWriter, long value) throws IOException;
    }

    @FunctionalInterface
    private interface JsonGeneratorWriteString {
        void writeString(Object jsonWriter, String value) throws IOException;
    }

    @FunctionalInterface
    private interface JsonGeneratorConfigure {
        Object configure(Object jsonWriter, Object featureEnum, boolean options);
    }

}
