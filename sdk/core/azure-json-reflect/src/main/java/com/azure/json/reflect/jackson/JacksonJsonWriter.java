package com.azure.json.reflect.jackson;

import com.azure.json.JsonWriteContext;
import com.azure.json.JsonWriter;
import com.azure.json.JsonToken;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;

import static java.lang.invoke.MethodType.methodType;

public class JacksonJsonWriter extends JsonWriter {
    private static boolean init = false;
    private static final MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();

    private JsonWriteContext context = JsonWriteContext.ROOT;


    Object generator;
    static Object FACTORY;


    private static MethodHandle flush;
    private static MethodHandle close;
    private static MethodHandle writeStartObject;
    private static MethodHandle writeEndObject;
    private static MethodHandle writeStartArray;
    private static MethodHandle writeEndArray;
    private static MethodHandle writeFieldName;
    private static MethodHandle writeNull;
    private static MethodHandle writeBinary;
    private static MethodHandle writeBoolean;
    private static MethodHandle writeDouble;
    private static MethodHandle writeFloat;
    private static MethodHandle writeInt;
    private static MethodHandle writeLong;
    private static MethodHandle writeString;
    private static MethodHandle writeRawValue;
    private static MethodHandle createGenerator;

    public static JsonWriter toStream(OutputStream stream) {
        try {
            return new JacksonJsonWriter(new OutputStreamWriter(stream));
        } catch (Throwable e) {
            throw new UncheckedIOException((IOException) e);
        }
    }


    public JacksonJsonWriter(OutputStreamWriter gen){
        try{
            if (!init) {
                initializeMethodHandles();
            }

            generator = createGenerator.invoke(FACTORY,gen);

        } catch (Throwable e){
            throw new RuntimeException(e);

        }

    }

    private static void initializeMethodHandles() throws Throwable {
        try{
            Class<?> jacksonGeneratorClass = Class.forName("com.fasterxml.jackson.core.JsonGenerator");
            Class<?> factoryClass = Class.forName("com.fasterxml.jackson.core.JsonFactory");

            //method handles
            MethodHandle constructorFactory = publicLookup.findConstructor(factoryClass, methodType(void.class));
            createGenerator = publicLookup.findVirtual(factoryClass, "createGenerator", methodType(jacksonGeneratorClass, Writer.class));
            FACTORY = constructorFactory.invoke();

            writeRawValue = publicLookup.findVirtual(jacksonGeneratorClass, "writeRawValue", MethodType.methodType(void.class,String.class));
            flush = publicLookup.findVirtual(jacksonGeneratorClass, "flush", MethodType.methodType(void.class));
            close = publicLookup.findVirtual(jacksonGeneratorClass, "close", MethodType.methodType(void.class));
            writeStartObject = publicLookup.findVirtual(jacksonGeneratorClass, "writeStartObject", MethodType.methodType(void.class));
            writeEndObject = publicLookup.findVirtual(jacksonGeneratorClass, "writeEndObject", MethodType.methodType(void.class));
            writeStartArray = publicLookup.findVirtual(jacksonGeneratorClass, "writeStartArray", MethodType.methodType(void.class));
            writeEndArray = publicLookup.findVirtual(jacksonGeneratorClass, "writeEndArray", MethodType.methodType(void.class));
            writeFieldName = publicLookup.findVirtual(jacksonGeneratorClass, "writeFieldName", MethodType.methodType(void.class,String.class));
            writeNull = publicLookup.findVirtual(jacksonGeneratorClass, "writeNull", MethodType.methodType(void.class));
            writeBinary = publicLookup.findVirtual(jacksonGeneratorClass, "writeBinary", methodType(void.class, byte[].class));
            writeBoolean = publicLookup.findVirtual(jacksonGeneratorClass, "writeBoolean", MethodType.methodType(void.class,boolean.class));
            writeDouble = publicLookup.findVirtual(jacksonGeneratorClass, "writeNumber", MethodType.methodType(void.class,double.class));
            writeFloat = publicLookup.findVirtual(jacksonGeneratorClass, "writeNumber", MethodType.methodType(void.class,float.class));
            writeInt = publicLookup.findVirtual(jacksonGeneratorClass, "writeNumber", MethodType.methodType(void.class,int.class));
            writeLong = publicLookup.findVirtual(jacksonGeneratorClass, "writeNumber", MethodType.methodType(void.class,long.class));
            writeString = publicLookup.findVirtual(jacksonGeneratorClass, "writeString", MethodType.methodType(void.class,String.class));
        }
        catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalStateException("Incorrect Library Present");
        }
        init = true;

    }
    @Override
    public JsonWriteContext getWriteContext() {
        try{
            return context;
        }
        catch(RuntimeException e){
            throw new RuntimeException(e);

        }
    }

    @Override
    public void close() throws IOException {
        if (context != JsonWriteContext.COMPLETED) {
            throw new IllegalStateException("Writing of the JSON object must be completed before the writer can be "
                + "closed. Current writing state is '" + context.getWriteState() + "'.");
        }
        try {
            close.invoke(generator);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public JsonWriter flush() {
        try {
            flush.invoke(generator);
            return this;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public JsonWriter writeStartObject() {
        context.validateToken(JsonToken.START_OBJECT);
        try {
            writeStartObject.invoke(generator);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        context = context.updateContext(JsonToken.START_OBJECT);
        return this;
    }

    @Override
    public JsonWriter writeEndObject() {
        context.validateToken(JsonToken.END_OBJECT);

        try {
            writeEndObject.invoke(generator);
        } catch (Throwable e) {
            throw new UncheckedIOException((IOException) e);
        }

        context = context.updateContext(JsonToken.END_OBJECT);
        return this;
    }

    @Override
    public JsonWriter writeStartArray() {
        context.validateToken(JsonToken.START_ARRAY);

        try {
            writeStartArray.invoke(generator);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        context = context.updateContext(JsonToken.START_ARRAY);
        return this;

    }

    @Override
    public JsonWriter writeEndArray() {
        context.validateToken(JsonToken.END_ARRAY);

        try {
            writeEndArray.invoke(generator);
        } catch (Throwable e) {
            throw new UncheckedIOException((IOException) e);
        }

        context = context.updateContext(JsonToken.END_ARRAY);
        return this;
    }

    @Override
    public JsonWriter writeFieldName(String fieldName) {
        Objects.requireNonNull(fieldName, "'fieldName' cannot be null.");

        context.validateToken(JsonToken.FIELD_NAME);

        try {
            writeFieldName.invoke(generator,fieldName);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        context = context.updateContext(JsonToken.FIELD_NAME);
        return this;
    }

    @Override
    public JsonWriter writeBinary(byte[] value) {
         context.validateToken(JsonToken.STRING);

        try {
            if (value == null) {
                writeNull.invoke(generator);
            } else {
                writeBinary.invoke(generator,value);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        context = context.updateContext(JsonToken.STRING);
        return this;
    }

    @Override
    public JsonWriter writeBoolean(boolean value) {
        context.validateToken(JsonToken.BOOLEAN);

        try {
            writeBoolean.invoke(generator,value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        context = context.updateContext(JsonToken.BOOLEAN);
        return this;
    }

    @Override
    public JsonWriter writeDouble(double value) {
        context.validateToken(JsonToken.NUMBER);

        try {
            writeDouble.invoke(generator,value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeFloat(float value) {
        context.validateToken(JsonToken.NUMBER);

        try {
            writeFloat.invoke(generator,value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeInt(int value) {
        context.validateToken(JsonToken.NUMBER);

        try {
            writeInt.invoke(generator,value);
        } catch (Throwable e) {
            throw new UncheckedIOException((IOException) e);
        }

        context = context.updateContext(JsonToken.NUMBER);
        return this;

    }

    @Override
    public JsonWriter writeLong(long value) {
        context.validateToken(JsonToken.NUMBER);

        try {
            writeLong.invoke(generator,value);
        } catch (Throwable e) {
            throw new UncheckedIOException((IOException) e);
        }

        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeNull() {
        context.validateToken(JsonToken.NULL);

        try {
            writeNull.invoke(generator);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        context = context.updateContext(JsonToken.NULL);
        return this;
    }

    @Override
    public JsonWriter writeString(String value) {
        context.validateToken(JsonToken.STRING);

        try {
            writeString.invoke(generator,value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        context = context.updateContext(JsonToken.STRING);
        return this;
    }

    @Override
    public JsonWriter writeRawValue(String value) {
        Objects.requireNonNull(value, "'value' cannot be null.");

        context.validateToken(JsonToken.STRING);

        try {
            writeRawValue.invoke(generator,value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        context = context.updateContext(JsonToken.STRING);
        return this;
    }
}
