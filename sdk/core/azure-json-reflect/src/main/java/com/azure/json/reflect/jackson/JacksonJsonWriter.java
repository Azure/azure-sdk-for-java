package com.azure.json.reflect.jackson;

import com.azure.json.JsonOptions;
import com.azure.json.JsonWriteContext;
import com.azure.json.JsonWriter;
import com.azure.json.JsonToken;
import com.azure.json.implementation.DefaultJsonWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static java.lang.invoke.MethodType.methodType;

public class JacksonJsonWriter extends JsonWriter {
    private static boolean initialized = false;
    private static final MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();

    private JsonWriteContext context = JsonWriteContext.ROOT;

    private final Object jacksonGenerator;
    private static Object FACTORY;

    private static MethodHandle flushMethod;
    private static MethodHandle closeMethod;
    private static MethodHandle writeStartObjectMethod;
    private static MethodHandle writeEndObjectMethod;
    private static MethodHandle writeStartArrayMethod;
    private static MethodHandle writeEndArrayMethod;
    private static MethodHandle writeFieldNameMethod;
    private static MethodHandle writeNullMethod;
    private static MethodHandle writeBinaryMethod;
    private static MethodHandle writeBooleanMethod;
    private static MethodHandle writeDoubleMethod;
    private static MethodHandle writeFloatMethod;
    private static MethodHandle writeIntMethod;
    private static MethodHandle writeLongMethod;
    private static MethodHandle writeStringMethod;
    private static MethodHandle writeRawValueMethod;
    private static MethodHandle createGeneratorMethod;

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
            if (!initialized) {
                initialize();
            }

            jacksonGenerator = createGeneratorMethod.invoke(FACTORY, writer);
            // configure Jackson to support non-numeric numbers

        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else if (e instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else {
                throw new IllegalStateException("Incorrect Library Present");
            }
        }

    }

    static void initialize() throws ReflectiveOperationException {
        Class<?> factoryClass = Class.forName("com.fasterxml.jackson.core.JsonFactory");
        Class<?> jacksonGeneratorClass = Class.forName("com.fasterxml.jackson.core.JsonGenerator");

        MethodHandle constructorFactory = publicLookup.findConstructor(factoryClass, methodType(void.class));
        createGeneratorMethod = publicLookup.findVirtual(factoryClass, "createGenerator", methodType(jacksonGeneratorClass, Writer.class));
        try {
            FACTORY = constructorFactory.invoke();
        } catch (Throwable e) {
            throw (RuntimeException) e.getCause();
        }

        writeRawValueMethod = publicLookup.findVirtual(jacksonGeneratorClass, "writeRawValue", MethodType.methodType(void.class,String.class));
        flushMethod = publicLookup.findVirtual(jacksonGeneratorClass, "flush", MethodType.methodType(void.class));
        closeMethod = publicLookup.findVirtual(jacksonGeneratorClass, "close", MethodType.methodType(void.class));
        writeStartObjectMethod = publicLookup.findVirtual(jacksonGeneratorClass, "writeStartObject", MethodType.methodType(void.class));
        writeEndObjectMethod = publicLookup.findVirtual(jacksonGeneratorClass, "writeEndObject", MethodType.methodType(void.class));
        writeStartArrayMethod = publicLookup.findVirtual(jacksonGeneratorClass, "writeStartArray", MethodType.methodType(void.class));
        writeEndArrayMethod = publicLookup.findVirtual(jacksonGeneratorClass, "writeEndArray", MethodType.methodType(void.class));
        writeFieldNameMethod = publicLookup.findVirtual(jacksonGeneratorClass, "writeFieldName", MethodType.methodType(void.class,String.class));
        writeNullMethod = publicLookup.findVirtual(jacksonGeneratorClass, "writeNull", MethodType.methodType(void.class));
        writeBinaryMethod = publicLookup.findVirtual(jacksonGeneratorClass, "writeBinary", methodType(void.class, byte[].class));
        writeBooleanMethod = publicLookup.findVirtual(jacksonGeneratorClass, "writeBoolean", MethodType.methodType(void.class,boolean.class));
        writeDoubleMethod = publicLookup.findVirtual(jacksonGeneratorClass, "writeNumber", MethodType.methodType(void.class,double.class));
        writeFloatMethod = publicLookup.findVirtual(jacksonGeneratorClass, "writeNumber", MethodType.methodType(void.class,float.class));
        writeIntMethod = publicLookup.findVirtual(jacksonGeneratorClass, "writeNumber", MethodType.methodType(void.class,int.class));
        writeLongMethod = publicLookup.findVirtual(jacksonGeneratorClass, "writeNumber", MethodType.methodType(void.class,long.class));
        writeStringMethod = publicLookup.findVirtual(jacksonGeneratorClass, "writeString", MethodType.methodType(void.class,String.class));
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
            closeMethod.invoke(jacksonGenerator);
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
            flushMethod.invoke(jacksonGenerator);
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
            writeStartObjectMethod.invoke(jacksonGenerator);
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
            writeEndObjectMethod.invoke(jacksonGenerator);
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
            writeStartArrayMethod.invoke(jacksonGenerator);
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
            writeEndArrayMethod.invoke(jacksonGenerator);
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
        Objects.requireNonNull(fieldName, "'fieldName' cannot be null.");

        context.validateToken(JsonToken.FIELD_NAME);

        try {
            writeFieldNameMethod.invoke(jacksonGenerator, fieldName);
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
                writeNullMethod.invoke(jacksonGenerator);
            } else {
                writeBinaryMethod.invoke(jacksonGenerator, value);
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
            writeBooleanMethod.invoke(jacksonGenerator, value);
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
            writeDoubleMethod.invoke(jacksonGenerator, value);
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
            writeFloatMethod.invoke(jacksonGenerator, value);
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
            writeIntMethod.invoke(jacksonGenerator, value);
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
            writeLongMethod.invoke(jacksonGenerator, value);
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
            writeNullMethod.invoke(jacksonGenerator);
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
            writeStringMethod.invoke(jacksonGenerator, value);
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
            writeRawValueMethod.invoke(jacksonGenerator, value);
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
