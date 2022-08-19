package com.azure.json.reflect.jackson;

import com.azure.json.JsonWriteContext;
import com.azure.json.JsonWriter;
import com.azure.json.implementation.jackson.core.JsonFactory;
import com.azure.json.implementation.jackson.core.JsonGenerator;
import com.azure.json.implementation.jackson.core.JsonToken;
import com.azure.json.implementation.jackson.core.json.DupDetector;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;

import static java.lang.invoke.MethodType.methodType;

public class JacksonJsonWriter extends JsonWriter {
    private final MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
    private final MethodHandles.Lookup privateLookup = MethodHandles.lookup();

    private JsonWriteContext context = JsonWriteContext.ROOT;

    Class<?> factoryClass;
    Class<?> jacksonGeneratorClass;

    Object generator;
    Object FACTORY;


    //method handles
    private static MethodHandle constructorGen;
    private static MethodHandle constructorFactory;
    private final MethodHandle flush;
    private final MethodHandle close;
    //private final MethodHandle validateToken;
    private JsonToken currentToken;


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

        }
        catch (RuntimeException | ClassNotFoundException | NoSuchMethodException | InstantiationException |
               IllegalAccessException | InvocationTargetException e){
            throw new RuntimeException(e);

        } catch (Throwable e) {
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
        try {

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public JsonWriter writeEndObject() {
        try {

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JsonWriter writeStartArray() {
        try {

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public JsonWriter writeEndArray() {
        try {

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public JsonWriter writeFieldName(String fieldName) {
        try {

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public JsonWriter writeBinary(byte[] value) {
        try {

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public JsonWriter writeBoolean(boolean value) {
        try {

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public JsonWriter writeDouble(double value) {
        try {

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public JsonWriter writeFloat(float value) {
        try {

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public JsonWriter writeInt(int value) {
        try {

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public JsonWriter writeLong(long value) {
        try {

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public JsonWriter writeNull() {
        try {

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public JsonWriter writeString(String value) {
        try {

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public JsonWriter writeRawValue(String value) {
        try {

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
