package com.azure.json.reflect;

import com.azure.json.JsonOptions;
import com.azure.json.JsonWriteContext;
import com.azure.json.JsonWriter;
import com.azure.json.JsonToken;
import com.azure.json.implementation.DefaultJsonWriter;

import java.io.*;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

import static java.lang.invoke.MethodType.methodType;

class JacksonJsonWriter extends JsonWriter {
    private static boolean initialized = false;
    private static boolean attemptedInitialization = false;
    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private static JsonWriterJsonGenerator JSON_WRITER_GENERATOR;
    private static JsonWriterConstructor JSON_FACTORY_CONSTRUCTOR;
    private static JsonWriterWriteRawValue JSON_WRITER_WRITE_RAW_VALUE;
    private static JsonWriterFlush JSON_WRITER_FLUSH;
    private static JsonWriterClose JSON_WRITER_CLOSE;
    private static JsonWriterWriteStartObject JSON_WRITER_WRITE_START_OBJECT;
    private static JsonWriterWriteEndObject JSON_WRITER_WRITE_END_OBJECT;
    private static JsonWriterWriteStartArray JSON_WRITER_WRITE_START_ARRAY;
    private static JsonWriterWriteEndArray JSON_WRITER_WRITE_END_ARRAY;
    private static JsonWriterWriteFieldName JSON_WRITER_WRITE_FIELD_NAME;
    private static JsonWriterWriteNull JSON_WRITER_WRITE_NULL;
    private static JsonWriterWriteBinary JSON_WRITER_WRITE_BINARY;
    private static JsonWriterWriteBoolean JSON_WRITER_WRITE_BOOLEAN;
    private static JsonWriterWriteDouble JSON_WRITER_WRITE_DOUBLE;
    private static JsonWriterWriteFloat JSON_WRITER_WRITE_FLOAT;
    private static JsonWriterWriteInt JSON_WRITER_WRITE_INT;
    private static JsonWriterWriteLong JSON_WRITER_WRITE_LONG;
    private static JsonWriterWriteString JSON_WRITER_WRITE_STRING;
    private static JsonWriterConfigure JSON_WRITER_CONFIGURE;




    private JsonWriteContext context = JsonWriteContext.ROOT;

    private final Object jacksonGenerator;
    private static Object FACTORY;

    private static Class jacksonFeatureEnum;
    private static Class<?> jacksonGeneratorClass;
    private static Class<?> factoryClass;


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
        try {
            initialize();

            jacksonGenerator = JSON_WRITER_GENERATOR.createGenerator(FACTORY,writer);//fails here
            // Configure Jackson to support non-numeric numbers
            JSON_WRITER_CONFIGURE.configure(jacksonFeatureEnum, Enum.valueOf(jacksonFeatureEnum, "QUOTE_NON_NUMERIC_NUMBERS"), options.isNonNumericNumbersSupported());

        } catch (ReflectiveOperationException e) {

                throw new IllegalStateException("Jackson is not present or an incorrect version is present.");
            }


    }

    static synchronized void initialize() throws ReflectiveOperationException{
        if (initialized) {
            return;
        } else if (attemptedInitialization) {
            throw new ReflectiveOperationException("Initialization of JacksonJsonWriter has failed in the past.");
        }

        attemptedInitialization = true;

        factoryClass = Class.forName("com.fasterxml.jackson.core.JsonFactory");
        jacksonGeneratorClass = Class.forName("com.fasterxml.jackson.core.JsonGenerator");
        jacksonFeatureEnum = (Class) Arrays.stream(jacksonGeneratorClass.getDeclaredClasses()).filter(c -> "Feature".equals(c.getSimpleName())).findAny().orElse(null);
        try{
            MethodType voidMT = methodType(void.class);
            MethodType voidStringMt = methodType(void.class,String.class);
            MethodType voidObjectMT = methodType(void.class, Object.class);

            MethodHandle constructorFactory = lookup.findConstructor(factoryClass, voidMT);
            JSON_FACTORY_CONSTRUCTOR =(JsonWriterConstructor) LambdaMetafactory.metafactory(lookup,"jsonFactory",methodType(JsonWriterConstructor.class), constructorFactory.type().generic(), constructorFactory, constructorFactory.type()).getTarget().invoke();
            JSON_WRITER_GENERATOR = createMetaFactoryJsonFactory("createGenerator",methodType(jacksonGeneratorClass, Writer.class),JsonWriterJsonGenerator.class,methodType(Object.class,Object.class, Writer.class));
            FACTORY = JSON_FACTORY_CONSTRUCTOR.jsonFactory();

            JSON_WRITER_WRITE_RAW_VALUE = createMetaFactory("writeRawValue",voidStringMt,JsonWriterWriteRawValue.class,methodType(void.class,Object.class,String.class));
            JSON_WRITER_FLUSH = createMetaFactory("flush",voidMT,JsonWriterFlush.class,voidObjectMT);
            JSON_WRITER_CLOSE = createMetaFactory("close", voidMT, JsonWriterClose.class, voidObjectMT);
            JSON_WRITER_WRITE_START_OBJECT = createMetaFactory("writeStartObject",voidMT,JsonWriterWriteStartObject.class, voidObjectMT);
            JSON_WRITER_WRITE_END_OBJECT = createMetaFactory("writeEndObject",voidMT,JsonWriterWriteEndObject.class,voidObjectMT);
            JSON_WRITER_WRITE_START_ARRAY = createMetaFactory("writeStartArray",voidMT,JsonWriterWriteStartArray.class,voidObjectMT);
            JSON_WRITER_WRITE_END_ARRAY = createMetaFactory("writeEndArray",voidMT, JsonWriterWriteEndArray.class,voidObjectMT);
            JSON_WRITER_WRITE_FIELD_NAME = createMetaFactory("writeFieldName",voidStringMt,JsonWriterWriteFieldName.class,methodType(void.class,Object.class,String.class));
            JSON_WRITER_WRITE_NULL = createMetaFactory("writeNull",voidMT,JsonWriterWriteNull.class,voidObjectMT);
            JSON_WRITER_WRITE_BINARY = createMetaFactory("writeBinary",methodType(void.class,byte[].class),JsonWriterWriteBinary.class, methodType(void.class, Object.class, byte[].class));
            JSON_WRITER_WRITE_BOOLEAN = createMetaFactory("writeBoolean",methodType(void.class,boolean.class),JsonWriterWriteBoolean.class,methodType(void.class, Object.class, boolean.class));
            JSON_WRITER_WRITE_DOUBLE = createMetaFactory("writeNumber",methodType(void.class,double.class),JsonWriterWriteDouble.class,methodType(void.class,Object.class, double.class));
            JSON_WRITER_WRITE_FLOAT = createMetaFactory("writeNumber",methodType(void.class,float.class),JsonWriterWriteFloat.class,methodType(void.class, Object.class, float.class));
            JSON_WRITER_WRITE_INT = createMetaFactory("writeNumber",methodType(void.class,int.class),JsonWriterWriteInt.class,methodType(void.class, Object.class, int.class));
            JSON_WRITER_WRITE_LONG = createMetaFactory("writeNumber",methodType(void.class,long.class),JsonWriterWriteLong.class,methodType(void.class, Object.class, long.class));
            JSON_WRITER_WRITE_STRING = createMetaFactory("writeString",voidStringMt,JsonWriterWriteString.class,methodType(void.class,Object.class,String.class));
            JSON_WRITER_CONFIGURE = createMetaFactory("configure",methodType(jacksonGeneratorClass,jacksonFeatureEnum,boolean.class),JsonWriterConfigure.class,methodType(Object.class, Object.class,Object.class,boolean.class));
        }
        catch (Throwable e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new ReflectiveOperationException("Initialization of JacksonJsonWriter failed.");
            }
        }


        initialized = true;
    }

    @SuppressWarnings("unchecked")
    private static <T> T createMetaFactory(String methodName, MethodType implType, Class<T> invokedClass, MethodType invokedType) throws Throwable {
        MethodHandle handle = lookup.findVirtual(jacksonGeneratorClass, methodName, implType);
        return (T) LambdaMetafactory.metafactory(lookup, methodName, methodType(invokedClass), invokedType, handle, handle.type()).getTarget().invoke();
    }

    @SuppressWarnings("unchecked")
    private static <T> T createMetaFactoryJsonFactory(String methodName, MethodType implType, Class<T> invokedClass, MethodType invokedType) throws Throwable {
        MethodHandle handle = lookup.findVirtual(factoryClass, methodName, implType);
        return (T) LambdaMetafactory.metafactory(lookup, methodName, methodType(invokedClass), invokedType, handle, handle.type()).getTarget().invoke();
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
    private interface  JsonWriterConstructor {
        Object jsonFactory();
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
        void configure(Object JsonWriter, Object featureEnum, boolean options);
    }

}
