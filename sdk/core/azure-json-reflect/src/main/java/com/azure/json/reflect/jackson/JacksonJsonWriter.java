package com.azure.json.reflect.jackson;

import com.azure.json.DefaultJsonWriter;
import com.azure.json.JsonWriteContext;
import com.azure.json.JsonWriter;
import com.azure.json.implementation.jackson.core.JsonFactory;
import com.azure.json.implementation.jackson.core.JsonGenerator;
import com.azure.json.JsonToken;
import com.azure.json.implementation.jackson.core.json.DupDetector;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import static java.lang.invoke.MethodType.methodType;

public class JacksonJsonWriter extends JsonWriter {
    private final MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
    private final MethodHandles.Lookup privateLookup = MethodHandles.lookup();

    private JsonWriteContext context = JsonWriteContext.ROOT;

    Class<?> factoryClass;
    Class<?> jacksonGeneratorClass;

    Object generator;
    static Object FACTORY;


    //method handles
    private static MethodHandle constructorGen;
    private static MethodHandle constructorFactory;
    private final MethodHandle flush;
    private final MethodHandle close;
    private static  MethodHandle createGenerator = null;
    private final MethodHandle writeStartObject;
    private final MethodHandle writeEndObject;
    private final MethodHandle writeStartArray;
    private final MethodHandle writeEndArray;
    private final MethodHandle writeFieldName;
    private final MethodHandle writeNull;
    private final MethodHandle writeBinary;
    private final MethodHandle writeBoolean;
    //private final MethodHandle writeNumber;
    private final MethodHandle writeDouble;
    private final MethodHandle writeFloat;
    private final MethodHandle writeInt;
    private final MethodHandle writeLong;
    private final MethodHandle writeString;
    private final MethodHandle writeRawValue;

    //private final MethodHandle validateToken;
    private JsonToken currentToken;
    public static JsonWriter toStream(OutputStream stream) {
        try {
            return new JacksonJsonWriter((JsonGenerator) createGenerator.invoke(stream));
        } catch (Throwable e) {
            throw new UncheckedIOException((IOException) e);
        }
    }


    public JacksonJsonWriter(JsonGenerator gen){
        try{
            jacksonGeneratorClass = Class.forName("com.fasterxml.jackson.core.JsonGenerator");
            factoryClass = Class.forName("com.fasterxml.jackson.core.JsonFactory");

            constructorGen = publicLookup.findConstructor(jacksonGeneratorClass, methodType(void.class));
            generator = constructorGen.invoke(gen);

            constructorFactory = publicLookup.findConstructor(factoryClass, methodType(void.class));
            FACTORY = constructorFactory.invoke();


            flush = publicLookup.findVirtual(jacksonGeneratorClass, "flush", MethodType.methodType(void.class));
            close = publicLookup.findVirtual(jacksonGeneratorClass, "close", MethodType.methodType(void.class));
            createGenerator = publicLookup.findVirtual(factoryClass, "createGenerator", MethodType.methodType(factoryClass));
            writeStartObject = publicLookup.findVirtual(jacksonGeneratorClass, "writeStartObject", MethodType.methodType(jacksonGeneratorClass));
            writeEndObject = publicLookup.findVirtual(jacksonGeneratorClass, "writeEndObject", MethodType.methodType(jacksonGeneratorClass));
            writeStartArray = publicLookup.findVirtual(jacksonGeneratorClass, "writeStartArray", MethodType.methodType(jacksonGeneratorClass));
            writeEndArray = publicLookup.findVirtual(jacksonGeneratorClass, "writeEndArray", MethodType.methodType(jacksonGeneratorClass));
            writeFieldName = publicLookup.findVirtual(jacksonGeneratorClass, "writeFieldName", MethodType.methodType(jacksonGeneratorClass,String.class));
            writeNull = publicLookup.findVirtual(jacksonGeneratorClass, "writeNull", MethodType.methodType(jacksonGeneratorClass));
            writeBinary = publicLookup.findVirtual(jacksonGeneratorClass, "writeBinary", MethodType.methodType(jacksonGeneratorClass,byte.class));
            writeBoolean = publicLookup.findVirtual(jacksonGeneratorClass, "writeBoolean", MethodType.methodType(jacksonGeneratorClass,boolean.class));
            //writeNumber = publicLookup.findVirtual(jacksonGeneratorClass, "writeNumber", MethodType.methodType(jacksonGeneratorClass,double.class));
            writeDouble = publicLookup.findVirtual(jacksonGeneratorClass, "writeDouble", MethodType.methodType(jacksonGeneratorClass,double.class));
            writeFloat = publicLookup.findVirtual(jacksonGeneratorClass, "writeFloat", MethodType.methodType(jacksonGeneratorClass,float.class));
            writeInt = publicLookup.findVirtual(jacksonGeneratorClass, "writeInt", MethodType.methodType(jacksonGeneratorClass,int.class));
            writeLong = publicLookup.findVirtual(jacksonGeneratorClass, "writeLong", MethodType.methodType(jacksonGeneratorClass,long.class));
            writeString = publicLookup.findVirtual(jacksonGeneratorClass, "writeString", MethodType.methodType(jacksonGeneratorClass,String.class));
            writeRawValue = publicLookup.findVirtual(jacksonGeneratorClass, "writeRawValue", MethodType.methodType(jacksonGeneratorClass,String.class));

        } catch (Throwable e){
            throw new RuntimeException(e);

        }

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
