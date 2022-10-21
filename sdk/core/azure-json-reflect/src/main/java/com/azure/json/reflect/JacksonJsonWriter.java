package com.azure.json.reflect;

import com.azure.json.JsonOptions;
import com.azure.json.JsonWriteContext;
import com.azure.json.JsonWriter;
import com.azure.json.JsonToken;
import com.azure.json.implementation.DefaultJsonWriter;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import static com.azure.json.reflect.MetaFactoryFactory.createMetaFactory;

import static java.lang.invoke.MethodType.methodType;

class JacksonJsonWriter extends JsonWriter {
    private static final Object JSON_WRITER_FACTORY;
    private static final Class JACKSON_FEATURE_ENUM;
    private static final JsonWriterJsonGenerator JSON_WRITER_GENERATOR;
    private static final JsonWriterWriteRawValue JSON_WRITER_WRITE_RAW_VALUE;
    private static final JsonWriterFlush JSON_WRITER_FLUSH;
    private static final JsonWriterClose JSON_WRITER_CLOSE;
    private static final JsonWriterWriteStartObject JSON_WRITER_WRITE_START_OBJECT;
    private static final JsonWriterWriteEndObject JSON_WRITER_WRITE_END_OBJECT;
    private static final JsonWriterWriteStartArray JSON_WRITER_WRITE_START_ARRAY;
    private static final JsonWriterWriteEndArray JSON_WRITER_WRITE_END_ARRAY;
    private static final JsonWriterWriteFieldName JSON_WRITER_WRITE_FIELD_NAME;
    private static final JsonWriterWriteNull JSON_WRITER_WRITE_NULL;
    private static final JsonWriterWriteBinary JSON_WRITER_WRITE_BINARY;
    private static final JsonWriterWriteBoolean JSON_WRITER_WRITE_BOOLEAN;
    private static final JsonWriterWriteDouble JSON_WRITER_WRITE_DOUBLE;
    private static final JsonWriterWriteFloat JSON_WRITER_WRITE_FLOAT;
    private static final JsonWriterWriteInt JSON_WRITER_WRITE_INT;
    private static final JsonWriterWriteLong JSON_WRITER_WRITE_LONG;
    private static final JsonWriterWriteString JSON_WRITER_WRITE_STRING;
    private static final JsonWriterConfigure JSON_WRITER_CONFIGURE;

    static final boolean INITIALIZED;

    static{
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Object jsonFactory = null;

        Class jacksonFeatureEnum = null;

        JsonWriterJsonGenerator jsonWriterJsonGenerator = null;
        JsonWriterWriteRawValue jsonWriterWriteRawValue = null;
        JsonWriterFlush jsonWriterFlush = null;
        JsonWriterClose jsonWriterClose = null;
        JsonWriterWriteStartObject jsonWriterWriteStartObject = null;
        JsonWriterWriteEndObject jsonWriterWriteEndObject = null;
        JsonWriterWriteStartArray jsonWriterWriteStartArray = null;
        JsonWriterWriteEndArray jsonWriterWriteEndArray = null;
        JsonWriterWriteFieldName jsonWriterWriteFieldName = null;
        JsonWriterWriteNull jsonWriterWriteNull= null;
        JsonWriterWriteBinary jsonWriterWriteBinary= null;
        JsonWriterWriteBoolean jsonWriterWriteBoolean= null;
        JsonWriterWriteDouble jsonWriterWriteDouble= null;
        JsonWriterWriteFloat jsonWriterWriteFloat= null;
        JsonWriterWriteInt jsonWriterWriteInt= null;
        JsonWriterWriteLong jsonWriterWriteLong= null;
        JsonWriterWriteString jsonWriterWriteString= null;
        JsonWriterConfigure jsonWriterConfigure= null;

        boolean initialized = false;

        try{
            Class<?> factoryClass = Class.forName("com.fasterxml.jackson.core.JsonFactory");
            Class<?> jacksonGeneratorClass = Class.forName("com.fasterxml.jackson.core.JsonGenerator");
            jacksonFeatureEnum = Arrays.stream(jacksonGeneratorClass.getDeclaredClasses()).filter(c -> "Feature".equals(c.getSimpleName())).findAny().orElse(null);
            jsonFactory = lookup.findConstructor(factoryClass, methodType(void.class)).invoke();

            MethodType voidMT = methodType(void.class);
            MethodType voidStringMt = methodType(void.class,String.class);
            MethodType voidObjectMT = methodType(void.class, Object.class);

            jsonWriterJsonGenerator = createMetaFactory("createGenerator",factoryClass,methodType(jacksonGeneratorClass, Writer.class),JsonWriterJsonGenerator.class,methodType(Object.class,Object.class, Writer.class),lookup);
            jsonWriterWriteRawValue = createMetaFactory("writeRawValue",jacksonGeneratorClass,voidStringMt,JsonWriterWriteRawValue.class,methodType(void.class,Object.class,String.class),lookup);
            jsonWriterFlush = createMetaFactory("flush",jacksonGeneratorClass,voidMT,JsonWriterFlush.class,voidObjectMT,lookup);
            jsonWriterClose = createMetaFactory("close",jacksonGeneratorClass, voidMT, JsonWriterClose.class, voidObjectMT,lookup);
            jsonWriterWriteStartObject = createMetaFactory("writeStartObject",jacksonGeneratorClass,voidMT,JsonWriterWriteStartObject.class, voidObjectMT,lookup);
            jsonWriterWriteEndObject = createMetaFactory("writeEndObject",jacksonGeneratorClass,voidMT,JsonWriterWriteEndObject.class,voidObjectMT,lookup);
            jsonWriterWriteStartArray = createMetaFactory("writeStartArray",jacksonGeneratorClass,voidMT,JsonWriterWriteStartArray.class,voidObjectMT,lookup);
            jsonWriterWriteEndArray = createMetaFactory("writeEndArray",jacksonGeneratorClass,voidMT, JsonWriterWriteEndArray.class,voidObjectMT,lookup);
            jsonWriterWriteFieldName = createMetaFactory("writeFieldName",jacksonGeneratorClass,voidStringMt,JsonWriterWriteFieldName.class,methodType(void.class,Object.class,String.class),lookup);
            jsonWriterWriteNull = createMetaFactory("writeNull",jacksonGeneratorClass,voidMT,JsonWriterWriteNull.class,voidObjectMT,lookup);
            jsonWriterWriteBinary = createMetaFactory("writeBinary",jacksonGeneratorClass,methodType(void.class,byte[].class),JsonWriterWriteBinary.class, methodType(void.class, Object.class, byte[].class),lookup);
            jsonWriterWriteBoolean = createMetaFactory("writeBoolean",jacksonGeneratorClass,methodType(void.class,boolean.class),JsonWriterWriteBoolean.class,methodType(void.class, Object.class, boolean.class),lookup);
            jsonWriterWriteDouble = createMetaFactory("writeNumber",jacksonGeneratorClass,methodType(void.class,double.class),JsonWriterWriteDouble.class,methodType(void.class,Object.class, double.class),lookup);
            jsonWriterWriteFloat = createMetaFactory("writeNumber",jacksonGeneratorClass,methodType(void.class,float.class),JsonWriterWriteFloat.class,methodType(void.class, Object.class, float.class),lookup);
            jsonWriterWriteInt = createMetaFactory("writeNumber",jacksonGeneratorClass,methodType(void.class,int.class),JsonWriterWriteInt.class,methodType(void.class, Object.class, int.class),lookup);
            jsonWriterWriteLong = createMetaFactory("writeNumber",jacksonGeneratorClass,methodType(void.class,long.class),JsonWriterWriteLong.class,methodType(void.class, Object.class, long.class),lookup);
            jsonWriterWriteString = createMetaFactory("writeString",jacksonGeneratorClass,voidStringMt,JsonWriterWriteString.class,methodType(void.class,Object.class,String.class),lookup);
            jsonWriterConfigure = createMetaFactory("configure",jacksonGeneratorClass,methodType(jacksonGeneratorClass,jacksonFeatureEnum,boolean.class),JsonWriterConfigure.class,methodType(Object.class, Object.class,Object.class,boolean.class),lookup);

            initialized = true;
        }
        catch (Throwable e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else if (e instanceof Error) {
                throw (Error) e;
            }
        }

        JACKSON_FEATURE_ENUM = jacksonFeatureEnum;
        JSON_WRITER_FACTORY = jsonFactory;
        JSON_WRITER_GENERATOR = jsonWriterJsonGenerator;
        JSON_WRITER_WRITE_RAW_VALUE = jsonWriterWriteRawValue;
        JSON_WRITER_FLUSH = jsonWriterFlush;
        JSON_WRITER_CLOSE = jsonWriterClose;
        JSON_WRITER_WRITE_START_OBJECT = jsonWriterWriteStartObject;
        JSON_WRITER_WRITE_END_OBJECT = jsonWriterWriteEndObject;
        JSON_WRITER_WRITE_START_ARRAY = jsonWriterWriteStartArray;
        JSON_WRITER_WRITE_END_ARRAY = jsonWriterWriteEndArray;
        JSON_WRITER_WRITE_FIELD_NAME = jsonWriterWriteFieldName;
        JSON_WRITER_WRITE_NULL = jsonWriterWriteNull;
        JSON_WRITER_WRITE_BINARY = jsonWriterWriteBinary;
        JSON_WRITER_WRITE_BOOLEAN = jsonWriterWriteBoolean;
        JSON_WRITER_WRITE_DOUBLE = jsonWriterWriteDouble;
        JSON_WRITER_WRITE_FLOAT = jsonWriterWriteFloat;
        JSON_WRITER_WRITE_INT = jsonWriterWriteInt;
        JSON_WRITER_WRITE_LONG = jsonWriterWriteLong;
        JSON_WRITER_WRITE_STRING = jsonWriterWriteString;
        JSON_WRITER_CONFIGURE = jsonWriterConfigure;

        INITIALIZED = initialized;
    }
    private final Object jacksonGenerator;
    JsonWriteContext context = JsonWriteContext.ROOT;

    /**
     * Creates a {@link DefaultJsonWriter} that writes the given {@link OutputStream}.
     * <p>
     * The passed {@link OutputStream} won't be closed when {@link #close()} is called as the {@link DefaultJsonWriter}
     * isn't the owner of the stream.
     *
     * @param json The {@link OutputStream} that will be written.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonWriter}.
     * @return An instance of {@link DefaultJsonWriter}.
     * @throws NullPointerException If {@code json} is null.
     * @throws IOException If a {@link DefaultJsonWriter} wasn't able to be constructed from the {@link OutputStream}.
     */
    static JsonWriter toStream(OutputStream json, JsonOptions options) throws IOException {
        Objects.requireNonNull(json, "'json' cannot be null.");
        return new JacksonJsonWriter(new OutputStreamWriter(json, StandardCharsets.UTF_8), options);
    }

    /**
     * Creates a {@link DefaultJsonWriter} that writes the given {@link Writer}.
     * <p>
     * The passed {@link Writer} won't be closed when {@link #close()} is called as the {@link DefaultJsonWriter} isn't
     * the owner of the stream.
     *
     * @param json The {@link Writer} that will be written.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonWriter}.
     * @return An instance of {@link DefaultJsonWriter}.
     * @throws NullPointerException If {@code json} is null.
     * @throws IOException If a {@link DefaultJsonWriter} wasn't able to be constructed from the {@link Writer}.
     */
    static JsonWriter toWriter(Writer json, JsonOptions options) throws IOException {
        Objects.requireNonNull(json, "'json' cannot be null.");
        return new JacksonJsonWriter(json, options);
    }

    private JacksonJsonWriter(Writer writer, JsonOptions options) throws IOException {
        if (!INITIALIZED) {
            throw new IllegalStateException("Jackson is not present or an incorrect version is present.");
        }

        jacksonGenerator = JSON_WRITER_GENERATOR.createGenerator(JSON_WRITER_FACTORY,writer);//fails here
        // Configure Jackson to support non-numeric numbers
        JSON_WRITER_CONFIGURE.configure(jacksonGenerator, Enum.valueOf(JACKSON_FEATURE_ENUM,
            "QUOTE_NON_NUMERIC_NUMBERS"), options.isNonNumericNumbersSupported());
    }

    @Override
    public JsonWriteContext getWriteContext() {
        return context;
    }

    @Override
    public void close() {
        if (context != JsonWriteContext.COMPLETED) {
            throw new IllegalStateException("Writing of the JSON object must be completed before the writer can be "
                + "closed. Current writing state is '" + context.getWriteState() + "'.");
        }
        JSON_WRITER_CLOSE.close(jacksonGenerator);
    }

    @Override
    public JsonWriter flush() {
        JSON_WRITER_FLUSH.flush(jacksonGenerator);
        return this;
    }

    @Override
    public JsonWriter writeStartObject() {
        context.validateToken(JsonToken.START_OBJECT);
        JSON_WRITER_WRITE_START_OBJECT.writeStartObject(jacksonGenerator);
        context = context.updateContext(JsonToken.START_OBJECT);
        return this;
    }

    @Override
    public JsonWriter writeEndObject() {
        context.validateToken(JsonToken.END_OBJECT);
        JSON_WRITER_WRITE_END_OBJECT.writeEndObject(jacksonGenerator);
        context = context.updateContext(JsonToken.END_OBJECT);
        return this;
    }

    @Override
    public JsonWriter writeStartArray() {
        context.validateToken(JsonToken.START_ARRAY);
        JSON_WRITER_WRITE_START_ARRAY.writeStartArray(jacksonGenerator);
        context = context.updateContext(JsonToken.START_ARRAY);
        return this;
    }

    @Override
    public JsonWriter writeEndArray() {
        context.validateToken(JsonToken.END_ARRAY);
        JSON_WRITER_WRITE_END_ARRAY.writeEndArray(jacksonGenerator);
        context = context.updateContext(JsonToken.END_ARRAY);
        return this;
    }

    @Override
    public JsonWriter writeFieldName(String fieldName) {
        Objects.requireNonNull(fieldName, "'fieldName' cannot be null.");
        context.validateToken(JsonToken.FIELD_NAME);
        JSON_WRITER_WRITE_FIELD_NAME.writeFieldName(jacksonGenerator,fieldName);
        context = context.updateContext(JsonToken.FIELD_NAME);
        return this;
    }

    @Override
    public JsonWriter writeBinary(byte[] value) {
        context.validateToken(JsonToken.STRING);
        if (value == null) {
            JSON_WRITER_WRITE_NULL.writeNull(jacksonGenerator);
        } else {
            JSON_WRITER_WRITE_BINARY.writeBinary(jacksonGenerator,value);
        }
        context = context.updateContext(JsonToken.STRING);
        return this;
    }

    @Override
    public JsonWriter writeBoolean(boolean value) {
        context.validateToken(JsonToken.BOOLEAN);
        JSON_WRITER_WRITE_BOOLEAN.writeBoolean(jacksonGenerator,value);
        context = context.updateContext(JsonToken.BOOLEAN);
        return this;
    }

    @Override
    public JsonWriter writeDouble(double value) {
        context.validateToken(JsonToken.NUMBER);
        JSON_WRITER_WRITE_DOUBLE.writeNumber(jacksonGenerator,value);
        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeFloat(float value) {
        context.validateToken(JsonToken.NUMBER);
        JSON_WRITER_WRITE_FLOAT.writeNumber(jacksonGenerator,value);
        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeInt(int value) {
        context.validateToken(JsonToken.NUMBER);
        JSON_WRITER_WRITE_INT.writeNumber(jacksonGenerator,value);
        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeLong(long value) {
        context.validateToken(JsonToken.NUMBER);
        JSON_WRITER_WRITE_LONG.writeNumber(jacksonGenerator,value);
        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeNull() {
        context.validateToken(JsonToken.NULL);
        JSON_WRITER_WRITE_NULL.writeNull(jacksonGenerator);
        context = context.updateContext(JsonToken.NULL);
        return this;
    }

    @Override
    public JsonWriter writeString(String value) {
        context.validateToken(JsonToken.STRING);
        JSON_WRITER_WRITE_STRING.writeString(jacksonGenerator,value);
        context = context.updateContext(JsonToken.STRING);
        return this;
    }

    @Override
    public JsonWriter writeRawValue(String value) throws IOException {
        Objects.requireNonNull(value, "'value' cannot be null.");
        context.validateToken(JsonToken.STRING);
        JSON_WRITER_WRITE_RAW_VALUE.writeRawValue(jacksonGenerator,value);
        context = context.updateContext(JsonToken.STRING);
        return this;
    }

    @FunctionalInterface
    private interface JsonWriterJsonGenerator {
        Object createGenerator(Object JsonFactory, Writer writer);
    }

    @FunctionalInterface
    private interface JsonWriterWriteRawValue {
        void writeRawValue(Object JsonWriter, String value);
    }

    @FunctionalInterface
    private interface JsonWriterFlush {
        void flush(Object JsonWriter);
    }

    @FunctionalInterface
    private interface JsonWriterClose {
        void close(Object JsonWriter);
    }

    @FunctionalInterface
    private interface JsonWriterWriteStartObject {
        void writeStartObject(Object JsonWriter);
    }

    @FunctionalInterface
    private interface JsonWriterWriteEndObject {
        void writeEndObject(Object JsonWriter);
    }

    @FunctionalInterface
    private interface JsonWriterWriteStartArray {
        void writeStartArray(Object JsonWriter);
    }

    @FunctionalInterface
    private interface JsonWriterWriteEndArray {
        void writeEndArray(Object JsonWriter);
    }

    @FunctionalInterface
    private interface JsonWriterWriteFieldName {
        void writeFieldName(Object JsonWriter, String fieldName);
    }

    @FunctionalInterface
    private interface JsonWriterWriteNull {
        void writeNull(Object JsonWriter);
    }

    @FunctionalInterface
    private interface JsonWriterWriteBinary {
        void writeBinary(Object JsonWriter, byte[] value);
    }

    @FunctionalInterface
    private interface JsonWriterWriteBoolean {
        void writeBoolean(Object JsonWriter, boolean value);
    }

    @FunctionalInterface
    private interface JsonWriterWriteDouble {
        void writeNumber(Object JsonWriter, double value);
    }

    @FunctionalInterface
    private interface JsonWriterWriteFloat {
        void writeNumber(Object JsonWriter, float value);
    }

    @FunctionalInterface
    private interface JsonWriterWriteInt {
        void writeNumber(Object JsonWriter,int value);
    }

    @FunctionalInterface
    private interface JsonWriterWriteLong {
        void writeNumber(Object JsonWriter,long value);
    }

    @FunctionalInterface
    private interface JsonWriterWriteString {
        void writeString(Object JsonWriter,String value);
    }

    @FunctionalInterface
    private interface JsonWriterConfigure {
        Object configure(Object JsonWriter, Object featureEnum, boolean options);
    }

}
