package com.azure.json.reflect;

import com.azure.json.JsonOptions;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriteContext;
import com.azure.json.JsonWriter;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

import static java.lang.invoke.MethodType.methodType;

class GsonJsonWriter extends JsonWriter {
    private static boolean initialized = false;
    private static boolean attemptedInitialization = false;
    private static final MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();

    private static MethodHandle gsonWriterConstructor;
    private static MethodHandle setLenientMethod;
    private static MethodHandle closeMethod;
    private static MethodHandle flushMethod;
    private static MethodHandle beginObjectMethod;
    private static MethodHandle endObjectMethod;
    private static MethodHandle beginArrayMethod;
    private static MethodHandle endArrayMethod;
    private static MethodHandle nameMethod;
    private static MethodHandle valueBooleanMethod;
    private static MethodHandle valueDoubleMethod;
    private static MethodHandle valueLongMethod;
    private static MethodHandle valueNullMethod;
    private static MethodHandle valueStringMethod;
    private static MethodHandle valueRawMethod;

    private final Object gsonWriter;
    private JsonWriteContext context = JsonWriteContext.ROOT;

    /**
     * Creates a {@link GsonJsonWriter} that writes the given {@link OutputStream}.
     * <p>
     * The passed {@link OutputStream} won't be closed when {@link #close()} is called as the {@link GsonJsonWriter}
     * isn't the owner of the stream.
     *
     * @param json The {@link OutputStream} that will be written.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonWriter}.
     * @return An instance of {@link GsonJsonWriter}.
     * @throws NullPointerException If {@code json} is null.
     *
     */
    static JsonWriter toStream(OutputStream json, JsonOptions options) {
        Objects.requireNonNull(json, "'json' cannot be null.");
        return new GsonJsonWriter(new OutputStreamWriter(json, StandardCharsets.UTF_8), options);
    }

    /**
     * Creates a {@link GsonJsonWriter} that writes the given {@link Writer}.
     * <p>
     * The passed {@link Writer} won't be closed when {@link #close()} is called as the {@link GsonJsonWriter}
     * isn't the owner of the stream.
     *
     * @param json The {@link Writer} that will be written.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonWriter}.
     * @return An instance of {@link GsonJsonWriter}.
     * @throws NullPointerException If {@code json} is null.
     */
    public static JsonWriter toWriter(Writer json, JsonOptions options) {
        Objects.requireNonNull(json, "'json' cannot be null.");
        return new GsonJsonWriter(json, options);
    }

    private GsonJsonWriter(Writer writer, JsonOptions options) {
        try {
            initialize();

            gsonWriter = gsonWriterConstructor.invoke(writer);
            setLenientMethod.invoke(gsonWriter, options.isNonNumericNumbersSupported());
        } catch (Throwable e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else {
                throw new IllegalStateException("Gson is not present or an incorrect version is present.");
            }
        }
    }

    static synchronized void initialize() throws ReflectiveOperationException {
        if (initialized) {
            return;
        } else if (attemptedInitialization) {
            throw new ReflectiveOperationException("Initialization of GsonJsonWriter has failed in the past.");
        }

        attemptedInitialization = true;

        Class<?> gsonWriterClass = Class.forName("com.google.gson.stream.JsonWriter");

        gsonWriterConstructor = publicLookup.findConstructor(gsonWriterClass, methodType(void.class, Writer.class));
        setLenientMethod = publicLookup.findVirtual(gsonWriterClass, "setLenient", methodType(void.class, boolean.class));
        closeMethod = publicLookup.findVirtual(gsonWriterClass, "close", methodType(void.class));
        flushMethod = publicLookup.findVirtual(gsonWriterClass, "flush", methodType(void.class));
        beginObjectMethod = publicLookup.findVirtual(gsonWriterClass, "beginObject", methodType(gsonWriterClass));
        endObjectMethod = publicLookup.findVirtual(gsonWriterClass, "endObject", methodType(gsonWriterClass));
        beginArrayMethod = publicLookup.findVirtual(gsonWriterClass, "beginArray", methodType(gsonWriterClass));
        endArrayMethod = publicLookup.findVirtual(gsonWriterClass, "endArray", methodType(gsonWriterClass));
        nameMethod = publicLookup.findVirtual(gsonWriterClass, "name", methodType(gsonWriterClass, String.class));
        valueBooleanMethod = publicLookup.findVirtual(gsonWriterClass, "value", methodType(gsonWriterClass, boolean.class));
        valueDoubleMethod = publicLookup.findVirtual(gsonWriterClass, "value", methodType(gsonWriterClass, double.class));
        valueLongMethod = publicLookup.findVirtual(gsonWriterClass, "value", methodType(gsonWriterClass, long.class));
        valueNullMethod = publicLookup.findVirtual(gsonWriterClass, "nullValue", methodType(gsonWriterClass));
        valueStringMethod = publicLookup.findVirtual(gsonWriterClass, "value", methodType(gsonWriterClass, String.class));
        valueRawMethod = publicLookup.findVirtual(gsonWriterClass, "jsonValue", methodType(gsonWriterClass, String.class));

        initialized = true;
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

        try {
            flush();
            closeMethod.invoke(gsonWriter);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw (RuntimeException) e.getCause();
            }
        }
    }

    @Override
    public JsonWriter flush() throws IOException {
        try {
            flushMethod.invoke(gsonWriter);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw (RuntimeException) e.getCause();
            }
        }
        return this;
    }

    @Override
    public JsonWriter writeStartObject() throws IOException {
        context.validateToken(JsonToken.START_OBJECT);

        try {
            beginObjectMethod.invoke(gsonWriter);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw (RuntimeException) e.getCause();
            }
        }

        context = context.updateContext(JsonToken.START_OBJECT);
        return this;
    }

    @Override
    public JsonWriter writeEndObject() throws IOException {
        context.validateToken(JsonToken.END_OBJECT);

        try {
            endObjectMethod.invoke(gsonWriter);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw (RuntimeException) e.getCause();
            }
        }

        context = context.updateContext(JsonToken.END_OBJECT);
        return this;
    }

    @Override
    public JsonWriter writeStartArray() throws IOException {
        context.validateToken(JsonToken.START_ARRAY);

        try {
            beginArrayMethod.invoke(gsonWriter);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw (RuntimeException) e.getCause();
            }
        }

        context = context.updateContext(JsonToken.START_ARRAY);
        return this;
    }

    @Override
    public JsonWriter writeEndArray() throws IOException {
        context.validateToken(JsonToken.END_ARRAY);

        try {
            endArrayMethod.invoke(gsonWriter);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw (RuntimeException) e.getCause();
            }
        }

        context = context.updateContext(JsonToken.END_ARRAY);
        return this;
    }

    @Override
    public JsonWriter writeFieldName(String fieldName) throws IOException {
        Objects.requireNonNull(fieldName, "'fieldName cannot be null.");
        context.validateToken(JsonToken.FIELD_NAME);

        try {
            nameMethod.invoke(gsonWriter, fieldName);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw (RuntimeException) e.getCause();
            }
        }

        context = context.updateContext(JsonToken.FIELD_NAME);
        return this;
    }

    @Override
    public JsonWriter writeBinary(byte[] value) throws IOException {
        context.validateToken(JsonToken.STRING);

        try {
            if (value == null) {
                valueNullMethod.invoke(gsonWriter);
            } else {
                valueStringMethod.invoke(gsonWriter, Base64.getEncoder().encodeToString(value));
            }
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw (RuntimeException) e.getCause();
            }
        }

        context = context.updateContext(JsonToken.STRING);
        return this;
    }

    @Override
    public JsonWriter writeBoolean(boolean value) throws IOException {
        context.validateToken(JsonToken.BOOLEAN);

        try {
            valueBooleanMethod.invoke(gsonWriter, value);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw (RuntimeException) e.getCause();
            }
        }

        context = context.updateContext(JsonToken.BOOLEAN);
        return this;
    }

    @Override
    public JsonWriter writeDouble(double value) throws IOException {
        context.validateToken(JsonToken.NUMBER);

        try {
            valueDoubleMethod.invoke(gsonWriter, value);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw (RuntimeException) e.getCause();
            }
        }

        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeFloat(float value) throws IOException {
        context.validateToken(JsonToken.NUMBER);

        try {
            valueDoubleMethod.invoke(gsonWriter, value);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw (RuntimeException) e.getCause();
            }
        }

        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeInt(int value) throws IOException {
        context.validateToken(JsonToken.NUMBER);

        try {
            valueLongMethod.invoke(gsonWriter, value);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw (RuntimeException) e.getCause();
            }
        }

        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeLong(long value) throws IOException {
        context.validateToken(JsonToken.NUMBER);

        try {
            valueLongMethod.invoke(gsonWriter, value);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw (RuntimeException) e.getCause();
            }
        }

        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeNull() throws IOException {
        context.validateToken(JsonToken.NULL);

        try {
            valueNullMethod.invoke(gsonWriter);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw (RuntimeException) e.getCause();
            }
        }

        context = context.updateContext(JsonToken.NULL);
        return this;
    }

    @Override
    public JsonWriter writeString(String value) throws IOException {
        context.validateToken(JsonToken.STRING);

        try {
            valueStringMethod.invoke(gsonWriter, value);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw (RuntimeException) e.getCause();
            }
        }

        context = context.updateContext(JsonToken.STRING);
        return this;
    }

    @Override
    public JsonWriter writeRawValue(String value) throws IOException {
        Objects.requireNonNull(value, "'value' cannot be null.");
        context.validateToken(JsonToken.STRING);

        try {
            valueRawMethod.invoke(gsonWriter, value);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw (RuntimeException) e.getCause();
            }
        }

        context = context.updateContext(JsonToken.STRING);
        return this;
    }
}
