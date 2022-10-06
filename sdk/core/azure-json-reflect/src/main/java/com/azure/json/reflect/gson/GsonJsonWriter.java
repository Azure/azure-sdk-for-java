package com.azure.json.reflect.gson;

import com.azure.json.JsonToken;
import com.azure.json.JsonWriteContext;
import com.azure.json.JsonWriter;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

import static java.lang.invoke.MethodType.methodType;

public class GsonJsonWriter extends JsonWriter {
    private static boolean initialized = false;
    private static final MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();

    private static MethodHandle gsonWriterConstructor;
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
    public static JsonWriter toStream(OutputStream stream) {
        return new GsonJsonWriter(new OutputStreamWriter(stream, StandardCharsets.UTF_8));
    }

    private GsonJsonWriter(OutputStreamWriter writer) {
        try {
            if (!initialized) {
                initialize();
            }
            gsonWriter = gsonWriterConstructor.invoke(writer);
        } catch (Throwable e) {
            if (e instanceof RuntimeException) {
                throw new RuntimeException(e);
            } else {
                throw new IllegalStateException("Incorrect Library Present");
            }
        }
    }

    static void initialize() throws ReflectiveOperationException {
        Class<?> gsonWriterClass = Class.forName("com.google.gson.stream.JsonWriter");
        gsonWriterConstructor = publicLookup.findConstructor(gsonWriterClass, methodType(void.class, Writer.class));

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
            closeMethod.invoke(gsonWriter);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public JsonWriter flush() {
        try {
            flushMethod.invoke(gsonWriter);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
            } else {
                throw new RuntimeException(e);
            }
        }
        return this;
    }

    @Override
    public JsonWriter writeStartObject() {
        context.validateToken(JsonToken.START_OBJECT);

        try {
            beginObjectMethod.invoke(gsonWriter);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
            } else {
                throw new RuntimeException(e);
            }
        }

        context = context.updateContext(JsonToken.START_OBJECT);
        return this;
    }

    @Override
    public JsonWriter writeEndObject() {
        context.validateToken(JsonToken.END_OBJECT);

        try {
            endObjectMethod.invoke(gsonWriter);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
            } else {
                throw new RuntimeException(e);
            }
        }

        context = context.updateContext(JsonToken.END_OBJECT);
        return this;
    }

    @Override
    public JsonWriter writeStartArray() {
        context.validateToken(JsonToken.START_ARRAY);

        try {
            beginArrayMethod.invoke(gsonWriter);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
            } else {
                throw new RuntimeException(e);
            }
        }

        context = context.updateContext(JsonToken.START_ARRAY);
        return this;
    }

    @Override
    public JsonWriter writeEndArray() {
        context.validateToken(JsonToken.END_ARRAY);

        try {
            endArrayMethod.invoke(gsonWriter);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
            } else {
                throw new RuntimeException(e);
            }
        }

        context = context.updateContext(JsonToken.END_ARRAY);
        return this;
    }

    @Override
    public JsonWriter writeFieldName(String fieldName) {
        Objects.requireNonNull(fieldName, "'fieldName cannot be null.");
        context.validateToken(JsonToken.FIELD_NAME);

        try {
            nameMethod.invoke(gsonWriter, fieldName);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
            } else {
                throw new RuntimeException(e);
            }
        }

        context = context.updateContext(JsonToken.FIELD_NAME);
        return this;
    }

    @Override
    public JsonWriter writeBinary(byte[] value) {
        context.validateToken(JsonToken.STRING);

        try {
            if (value == null) {
                valueNullMethod.invoke(gsonWriter);
            } else {
                valueStringMethod.invoke(gsonWriter, Base64.getEncoder().encodeToString(value));
            }
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
            } else {
                throw new RuntimeException(e);
            }
        }

        context = context.updateContext(JsonToken.STRING);
        return this;
    }

    @Override
    public JsonWriter writeBoolean(boolean value) {
        context.validateToken(JsonToken.BOOLEAN);

        try {
            valueBooleanMethod.invoke(gsonWriter, value);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
            } else {
                throw new RuntimeException(e);
            }
        }

        context = context.updateContext(JsonToken.BOOLEAN);
        return this;
    }

    @Override
    public JsonWriter writeDouble(double value) {
        context.validateToken(JsonToken.NUMBER);

        try {
            valueDoubleMethod.invoke(gsonWriter, value);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
            } else {
                throw new RuntimeException(e);
            }
        }

        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeFloat(float value) {
        context.validateToken(JsonToken.NUMBER);

        try {
            valueDoubleMethod.invoke(gsonWriter, value);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
            } else {
                throw new RuntimeException(e);
            }
        }

        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeInt(int value) {
        context.validateToken(JsonToken.NUMBER);

        try {
            valueLongMethod.invoke(gsonWriter, value);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
            } else {
                throw new RuntimeException(e);
            }
        }

        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeLong(long value) {
        context.validateToken(JsonToken.NUMBER);

        try {
            valueLongMethod.invoke(gsonWriter, value);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
            } else {
                throw new RuntimeException(e);
            }
        }

        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeNull() {
        context.validateToken(JsonToken.NULL);

        try {
            valueNullMethod.invoke(gsonWriter);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
            } else {
                throw new RuntimeException(e);
            }
        }

        context = context.updateContext(JsonToken.NULL);
        return this;
    }

    @Override
    public JsonWriter writeString(String value) {
        context.validateToken(JsonToken.STRING);

        try {
            valueStringMethod.invoke(gsonWriter, value);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
            } else {
                throw new RuntimeException(e);
            }
        }

        context = context.updateContext(JsonToken.STRING);
        return this;
    }

    @Override
    public JsonWriter writeRawValue(String value) {
        Objects.requireNonNull(value, "'value' cannot be null.");
        context.validateToken(JsonToken.STRING);

        try {
            valueRawMethod.invoke(gsonWriter, value);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
            } else {
                throw new RuntimeException(e);
            }
        }

        context = context.updateContext(JsonToken.STRING);
        return this;
    }
}
