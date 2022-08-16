package com.azure.json.reflect.gson;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.Base64;

import static java.lang.invoke.MethodType.methodType;

public class GsonJsonReader extends JsonReader {
    private final MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
    private Class<Enum> gsonTokenEnum;
    private Class<?> gsonReaderClass;
    private Object gsonReader;

    private MethodHandle nextNull;
    private MethodHandle nextBoolean;
    private MethodHandle nextString;

    private Object gsonCurrentToken;
    private JsonToken currentToken;

    public GsonJsonReader(Reader reader) {
        try {
            gsonTokenEnum = (Class<Enum>) Class.forName("com.google.gson.stream.JsonToken");
            gsonReaderClass = Class.forName("com.google.gson.stream.JsonReader");
            gsonReader = gsonReaderClass.getConstructor(Reader.class).newInstance(reader);

            nextNull = publicLookup.findVirtual(gsonReaderClass, "nextNull", methodType(void.class));
            nextBoolean = publicLookup.findVirtual(gsonReaderClass, "nextBoolean", methodType(boolean.class));
            nextString = publicLookup.findVirtual(gsonReaderClass, "nextString", methodType(String.class));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public JsonToken currentToken() {
        return currentToken;
    }

    @Override
    public JsonToken nextToken() {
        return null;
    }

    @Override
    public byte[] getBinary() {
        try {
            if (currentToken == JsonToken.NULL) {
                nextNull.invoke(gsonReader);
                return null;
            } else {
                return Base64.getDecoder().decode((String) nextString.invoke(gsonReader));
            }
        } catch (Throwable e) {
            throw new UncheckedIOException((IOException) e);
        }
    }

    @Override
    public boolean getBoolean() {
                try {
                    return (boolean) nextBoolean.invoke(gsonReader);
                } catch (Throwable e) {
                    throw new UncheckedIOException((IOException) e);
        }
    }

    @Override
    public float getFloat() {
        return 0;
    }

    @Override
    public double getDouble() {
        return 0;
    }

    @Override
    public int getInt() {
        return 0;
    }

    @Override
    public long getLong() {
        return 0;
    }

    @Override
    public String getString() {
        return null;
    }

    @Override
    public String getFieldName() {
        return null;
    }

    @Override
    public void skipChildren() {

    }

    @Override
    public JsonReader bufferObject() {
        return null;
    }

    @Override
    public boolean resetSupported() {
        return false;
    }

    @Override
    public JsonReader reset() {
        return null;
    }

    @Override
    public void close() throws IOException {
        try {
            publicLookup.findVirtual(gsonReaderClass, "close", methodType(Boolean.class)).invoke();
        } catch (Throwable e) {
            throw new IOException(e);
        }
    }
}
