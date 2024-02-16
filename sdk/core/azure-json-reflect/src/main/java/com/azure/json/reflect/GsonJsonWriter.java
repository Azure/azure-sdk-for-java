// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.reflect;

import com.azure.json.JsonOptions;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriteContext;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

import static java.lang.invoke.MethodType.methodType;
import static com.azure.json.reflect.MetaFactoryFactory.createMetaFactory;

final class GsonJsonWriter extends JsonWriter {
    private static final JsonWriterConstructor JSON_WRITER_CONSTRUCTOR;
    private static final JsonWriterSetLenient JSON_WRITER_SET_LENIENT;
    private static final JsonWriterClose JSON_WRITER_CLOSE;
    private static final JsonWriterFlush JSON_WRITER_FLUSH;
    private static final JsonWriterBeginObject JSON_WRITER_BEGIN_OBJECT;
    private static final JsonWriterEndObject JSON_WRITER_END_OBJECT;
    private static final JsonWriterBeginArray JSON_WRITER_BEGIN_ARRAY;
    private static final JsonWriterEndArray JSON_WRITER_END_ARRAY;
    private static final JsonWriterNullValue JSON_WRITER_NULL_VALUE;
    private static final JsonWriterName JSON_WRITER_NAME;
    private static final JsonWriterStringValue JSON_WRITER_STRING_VALUE;
    private static final JsonWriterBooleanValue JSON_WRITER_BOOLEAN_VALUE;
    private static final JsonWriterLongValue JSON_WRITER_LONG_VALUE;
    private static final JsonWriterDoubleValue JSON_WRITER_DOUBLE_VALUE;
    private static final JsonWriterRawValue JSON_WRITER_RAW_VALUE;

    static final boolean INITIALIZED;

    static {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();

        JsonWriterConstructor jsonWriterConstructor = null;
        JsonWriterSetLenient jsonWriterSetLenient = null;
        JsonWriterClose jsonWriterClose = null;
        JsonWriterFlush jsonWriterFlush = null;
        JsonWriterBeginObject jsonWriterBeginObject = null;
        JsonWriterEndObject jsonWriterEndObject = null;
        JsonWriterBeginArray jsonWriterBeginArray = null;
        JsonWriterEndArray jsonWriterEndArray = null;
        JsonWriterNullValue jsonWriterNullValue = null;
        JsonWriterName jsonWriterName = null;
        JsonWriterStringValue jsonWriterStringValue = null;
        JsonWriterLongValue jsonWriterLongValue = null;
        JsonWriterBooleanValue jsonWriterBooleanValue = null;
        JsonWriterDoubleValue jsonWriterDoubleValue = null;
        JsonWriterRawValue jsonWriterRawValue = null;

        boolean initialized = false;

        try {
            Class<?> gsonJsonWriterClass = Class.forName("com.google.gson.stream.JsonWriter");

            MethodHandle gsonWriterConstructor
                = lookup.findConstructor(gsonJsonWriterClass, methodType(void.class, Writer.class));
            jsonWriterConstructor = (JsonWriterConstructor) LambdaMetafactory
                .metafactory(lookup, "createJsonWriter", methodType(JsonWriterConstructor.class),
                    methodType(Object.class, Writer.class), gsonWriterConstructor, gsonWriterConstructor.type())
                .getTarget()
                .invoke();

            jsonWriterSetLenient
                = createMetaFactory("setLenient", gsonJsonWriterClass, methodType(void.class, boolean.class),
                    JsonWriterSetLenient.class, methodType(void.class, Object.class, boolean.class), lookup);
            jsonWriterClose = createMetaFactory("close", gsonJsonWriterClass, methodType(void.class),
                JsonWriterClose.class, methodType(void.class, Object.class), lookup);
            jsonWriterFlush = createMetaFactory("flush", gsonJsonWriterClass, methodType(void.class),
                JsonWriterFlush.class, methodType(void.class, Object.class), lookup);
            jsonWriterBeginObject
                = createMetaFactory("beginObject", gsonJsonWriterClass, methodType(gsonJsonWriterClass),
                    JsonWriterBeginObject.class, methodType(Object.class, Object.class), lookup);
            jsonWriterEndObject = createMetaFactory("endObject", gsonJsonWriterClass, methodType(gsonJsonWriterClass),
                JsonWriterEndObject.class, methodType(Object.class, Object.class), lookup);
            jsonWriterBeginArray = createMetaFactory("beginArray", gsonJsonWriterClass, methodType(gsonJsonWriterClass),
                JsonWriterBeginArray.class, methodType(Object.class, Object.class), lookup);
            jsonWriterEndArray = createMetaFactory("endArray", gsonJsonWriterClass, methodType(gsonJsonWriterClass),
                JsonWriterEndArray.class, methodType(Object.class, Object.class), lookup);
            jsonWriterNullValue = createMetaFactory("nullValue", gsonJsonWriterClass, methodType(gsonJsonWriterClass),
                JsonWriterNullValue.class, methodType(Object.class, Object.class), lookup);
            jsonWriterName
                = createMetaFactory("name", gsonJsonWriterClass, methodType(gsonJsonWriterClass, String.class),
                    JsonWriterName.class, methodType(Object.class, Object.class, String.class), lookup);
            jsonWriterStringValue
                = createMetaFactory("value", gsonJsonWriterClass, methodType(gsonJsonWriterClass, String.class),
                    JsonWriterStringValue.class, methodType(Object.class, Object.class, String.class), lookup);
            jsonWriterBooleanValue
                = createMetaFactory("value", gsonJsonWriterClass, methodType(gsonJsonWriterClass, boolean.class),
                    JsonWriterBooleanValue.class, methodType(Object.class, Object.class, boolean.class), lookup);
            jsonWriterLongValue
                = createMetaFactory("value", gsonJsonWriterClass, methodType(gsonJsonWriterClass, long.class),
                    JsonWriterLongValue.class, methodType(Object.class, Object.class, long.class), lookup);
            jsonWriterDoubleValue
                = createMetaFactory("value", gsonJsonWriterClass, methodType(gsonJsonWriterClass, double.class),
                    JsonWriterDoubleValue.class, methodType(Object.class, Object.class, double.class), lookup);
            jsonWriterRawValue
                = createMetaFactory("jsonValue", gsonJsonWriterClass, methodType(gsonJsonWriterClass, String.class),
                    JsonWriterRawValue.class, methodType(Object.class, Object.class, String.class), lookup);

            initialized = true;
        } catch (Throwable e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else if (e instanceof Error) {
                throw (Error) e;
            }
        }

        JSON_WRITER_CONSTRUCTOR = jsonWriterConstructor;
        JSON_WRITER_SET_LENIENT = jsonWriterSetLenient;
        JSON_WRITER_CLOSE = jsonWriterClose;
        JSON_WRITER_FLUSH = jsonWriterFlush;
        JSON_WRITER_BEGIN_OBJECT = jsonWriterBeginObject;
        JSON_WRITER_END_OBJECT = jsonWriterEndObject;
        JSON_WRITER_BEGIN_ARRAY = jsonWriterBeginArray;
        JSON_WRITER_END_ARRAY = jsonWriterEndArray;
        JSON_WRITER_NULL_VALUE = jsonWriterNullValue;
        JSON_WRITER_NAME = jsonWriterName;
        JSON_WRITER_STRING_VALUE = jsonWriterStringValue;
        JSON_WRITER_BOOLEAN_VALUE = jsonWriterBooleanValue;
        JSON_WRITER_LONG_VALUE = jsonWriterLongValue;
        JSON_WRITER_DOUBLE_VALUE = jsonWriterDoubleValue;
        JSON_WRITER_RAW_VALUE = jsonWriterRawValue;

        INITIALIZED = initialized;

    }

    private final Object gsonJsonWriter;
    private JsonWriteContext context = JsonWriteContext.ROOT;

    static JsonWriter toStream(OutputStream json, JsonOptions options) {
        Objects.requireNonNull(json, "'json' cannot be null.");
        return new GsonJsonWriter(new OutputStreamWriter(json, StandardCharsets.UTF_8), options);
    }

    static JsonWriter toWriter(Writer json, JsonOptions options) {
        Objects.requireNonNull(json, "'json' cannot be null.");
        return new GsonJsonWriter(json, options);
    }

    private GsonJsonWriter(Writer writer, JsonOptions options) {
        if (!INITIALIZED) {
            throw new IllegalStateException("No compatible version of Gson is present on the classpath.");
        }

        gsonJsonWriter = JSON_WRITER_CONSTRUCTOR.createJsonWriter(writer);
        JSON_WRITER_SET_LENIENT.setLenient(gsonJsonWriter, options.isNonNumericNumbersSupported());
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

        flush();
        JSON_WRITER_CLOSE.close(gsonJsonWriter);
    }

    @Override
    public JsonWriter flush() throws IOException {
        JSON_WRITER_FLUSH.flush(gsonJsonWriter);
        return this;
    }

    @Override
    public JsonWriter writeStartObject() throws IOException {
        context.validateToken(JsonToken.START_OBJECT);

        JSON_WRITER_BEGIN_OBJECT.beginObject(gsonJsonWriter);

        context = context.updateContext(JsonToken.START_OBJECT);
        return this;
    }

    @Override
    public JsonWriter writeEndObject() throws IOException {
        context.validateToken(JsonToken.END_OBJECT);

        JSON_WRITER_END_OBJECT.endObject(gsonJsonWriter);

        context = context.updateContext(JsonToken.END_OBJECT);
        return this;
    }

    @Override
    public JsonWriter writeStartArray() throws IOException {
        context.validateToken(JsonToken.START_ARRAY);

        JSON_WRITER_BEGIN_ARRAY.beginArray(gsonJsonWriter);

        context = context.updateContext(JsonToken.START_ARRAY);
        return this;
    }

    @Override
    public JsonWriter writeEndArray() throws IOException {
        context.validateToken(JsonToken.END_ARRAY);

        JSON_WRITER_END_ARRAY.endArray(gsonJsonWriter);

        context = context.updateContext(JsonToken.END_ARRAY);
        return this;
    }

    @Override
    public JsonWriter writeFieldName(String fieldName) throws IOException {
        Objects.requireNonNull(fieldName, "'fieldName cannot be null.");
        context.validateToken(JsonToken.FIELD_NAME);

        JSON_WRITER_NAME.name(gsonJsonWriter, fieldName);

        context = context.updateContext(JsonToken.FIELD_NAME);
        return this;
    }

    @Override
    public JsonWriter writeBinary(byte[] value) throws IOException {
        context.validateToken(JsonToken.STRING);

        if (value == null) {
            JSON_WRITER_NULL_VALUE.nullValue(gsonJsonWriter);
        } else {
            JSON_WRITER_STRING_VALUE.value(gsonJsonWriter, Base64.getEncoder().encodeToString(value));
        }

        context = context.updateContext(JsonToken.STRING);
        return this;
    }

    @Override
    public JsonWriter writeBoolean(boolean value) throws IOException {
        context.validateToken(JsonToken.BOOLEAN);

        JSON_WRITER_BOOLEAN_VALUE.value(gsonJsonWriter, value);

        context = context.updateContext(JsonToken.BOOLEAN);
        return this;
    }

    @Override
    public JsonWriter writeDouble(double value) throws IOException {
        context.validateToken(JsonToken.NUMBER);

        JSON_WRITER_DOUBLE_VALUE.value(gsonJsonWriter, value);

        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeFloat(float value) throws IOException {
        return writeDouble(value);
    }

    @Override
    public JsonWriter writeInt(int value) throws IOException {
        return writeLong(value);
    }

    @Override
    public JsonWriter writeLong(long value) throws IOException {
        context.validateToken(JsonToken.NUMBER);

        JSON_WRITER_LONG_VALUE.value(gsonJsonWriter, value);

        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeNull() throws IOException {
        context.validateToken(JsonToken.NULL);

        JSON_WRITER_NULL_VALUE.nullValue(gsonJsonWriter);

        context = context.updateContext(JsonToken.NULL);
        return this;
    }

    @Override
    public JsonWriter writeString(String value) throws IOException {
        context.validateToken(JsonToken.STRING);

        JSON_WRITER_STRING_VALUE.value(gsonJsonWriter, value);

        context = context.updateContext(JsonToken.STRING);
        return this;
    }

    @Override
    public JsonWriter writeRawValue(String value) throws IOException {
        Objects.requireNonNull(value, "'value' cannot be null.");
        context.validateToken(JsonToken.STRING);

        JSON_WRITER_RAW_VALUE.jsonValue(gsonJsonWriter, value);

        context = context.updateContext(JsonToken.STRING);
        return this;
    }

    @FunctionalInterface
    private interface JsonWriterConstructor {
        Object createJsonWriter(Writer writer);
    }

    @FunctionalInterface
    private interface JsonWriterSetLenient {
        void setLenient(Object jsonWriter, boolean lenient);
    }

    @FunctionalInterface
    private interface JsonWriterClose {
        void close(Object jsonWriter) throws IOException;
    }

    @FunctionalInterface
    private interface JsonWriterFlush {
        void flush(Object jsonWriter) throws IOException;
    }

    @FunctionalInterface
    private interface JsonWriterBeginObject {
        Object beginObject(Object jsonWriter) throws IOException;
    }

    @FunctionalInterface
    private interface JsonWriterEndObject {
        Object endObject(Object jsonWriter) throws IOException;
    }

    @FunctionalInterface
    private interface JsonWriterBeginArray {
        Object beginArray(Object jsonWriter) throws IOException;
    }

    @FunctionalInterface
    private interface JsonWriterEndArray {
        Object endArray(Object jsonWriter) throws IOException;
    }

    @FunctionalInterface
    private interface JsonWriterNullValue {
        Object nullValue(Object jsonWriter) throws IOException;
    }

    @FunctionalInterface
    private interface JsonWriterName {
        Object name(Object jsonWriter, String name) throws IOException;
    }

    @FunctionalInterface
    private interface JsonWriterStringValue {
        Object value(Object jsonWriter, String value) throws IOException;
    }

    @FunctionalInterface
    private interface JsonWriterBooleanValue {
        Object value(Object jsonWriter, boolean value) throws IOException;
    }

    @FunctionalInterface
    private interface JsonWriterLongValue {
        Object value(Object jsonWriter, long value) throws IOException;
    }

    @FunctionalInterface
    private interface JsonWriterDoubleValue {
        Object value(Object jsonWriter, double value) throws IOException;
    }

    @FunctionalInterface
    private interface JsonWriterRawValue {
        Object jsonValue(Object jsonWriter, String value) throws IOException;
    }
}
