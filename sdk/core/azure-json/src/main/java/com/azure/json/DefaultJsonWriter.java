// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import com.azure.json.implementation.jackson.core.JsonFactory;
import com.azure.json.implementation.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;

/**
 * Default {@link JsonWriter} implementation.
 */
public final class DefaultJsonWriter extends JsonWriter {
    private static final JsonFactory FACTORY = JsonFactory.builder().build();

    private final JsonGenerator generator;

    // Initial state is always root.
    private JsonWriteContext context = JsonWriteContext.ROOT;

    /**
     * Creates a {@link DefaultJsonWriter} that writes the given {@link OutputStream}.
     * <p>
     * The passed {@link OutputStream} won't be closed when {@link #close()} is called as the {@link DefaultJsonWriter}
     * isn't the owner of the stream.
     *
     * @param stream The {@link OutputStream} that will be written.
     * @return An instance of {@link DefaultJsonWriter}.
     * @throws UncheckedIOException If a {@link DefaultJsonWriter} wasn't able to be constructed from the
     * {@link OutputStream}.
     */
    public static JsonWriter toStream(OutputStream stream) {
        try {
            return new DefaultJsonWriter(FACTORY.createGenerator(stream));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private DefaultJsonWriter(JsonGenerator generator) {
        this.generator = generator;
    }

    @Override
    public JsonWriter flush() {
        try {
            generator.flush();
            return this;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public JsonWriter writeStartObject() {
        context.validateToken(JsonToken.START_OBJECT);

        try {
            generator.writeStartObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        context = context.updateContext(JsonToken.START_OBJECT);
        return this;
    }

    @Override
    public JsonWriter writeEndObject() {
        context.validateToken(JsonToken.END_OBJECT);

        try {
            generator.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        context = context.updateContext(JsonToken.END_OBJECT);
        return this;
    }

    @Override
    public JsonWriter writeStartArray() {
        context.validateToken(JsonToken.START_ARRAY);

        try {
            generator.writeStartArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        context = context.updateContext(JsonToken.START_ARRAY);
        return this;
    }

    @Override
    public JsonWriter writeEndArray() {
        context.validateToken(JsonToken.END_ARRAY);

        try {
            generator.writeEndArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        context = context.updateContext(JsonToken.END_ARRAY);
        return this;
    }

    @Override
    public JsonWriter writeFieldName(String fieldName) {
        context.validateToken(JsonToken.FIELD_NAME);

        try {
            generator.writeFieldName(fieldName);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        context = context.updateContext(JsonToken.FIELD_NAME);
        return this;
    }

    @Override
    public JsonWriter writeBinary(byte[] value) {
        context.validateToken(JsonToken.STRING);

        try {
            if (value == null) {
                generator.writeNull();
            } else {
                generator.writeBinary(value);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        context = context.updateContext(JsonToken.STRING);
        return this;
    }

    @Override
    public JsonWriter writeBoolean(boolean value) {
        context.validateToken(JsonToken.BOOLEAN);

        try {
            generator.writeBoolean(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        context = context.updateContext(JsonToken.BOOLEAN);
        return this;
    }

    @Override
    public JsonWriter writeDouble(double value) {
        context.validateToken(JsonToken.NUMBER);

        try {
            generator.writeNumber(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeFloat(float value) {
        context.validateToken(JsonToken.NUMBER);

        try {
            generator.writeNumber(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeInt(int value) {
        context.validateToken(JsonToken.NUMBER);

        try {
            generator.writeNumber(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeLong(long value) {
        context.validateToken(JsonToken.NUMBER);

        try {
            generator.writeNumber(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeNull() {
        context.validateToken(JsonToken.NULL);

        try {
            generator.writeNull();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        context = context.updateContext(JsonToken.NULL);
        return this;
    }

    @Override
    public JsonWriter writeString(String value) {
        context.validateToken(JsonToken.STRING);

        try {
            generator.writeString(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        context = context.updateContext(JsonToken.STRING);
        return this;
    }

    @Override
    public JsonWriter writeRawValue(String value) {
        context.validateToken(JsonToken.STRING);

        try {
            generator.writeRawValue(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        context = context.updateContext(JsonToken.STRING);
        return this;
    }

    @Override
    public JsonWriter writeBinaryField(String fieldName, byte[] value) {
        context.validateToken(JsonToken.FIELD_NAME);

        try {
            if (value == null) {
                generator.writeNullField(fieldName);
            } else {
                generator.writeBinaryField(fieldName, value);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return this;
    }

    @Override
    public JsonWriter writeBooleanField(String fieldName, boolean value) {
        context.validateToken(JsonToken.FIELD_NAME);

        try {
            generator.writeBooleanField(fieldName, value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return this;
    }

    @Override
    public JsonWriter writeDoubleField(String fieldName, double value) {
        context.validateToken(JsonToken.FIELD_NAME);

        try {
            generator.writeNumberField(fieldName, value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return this;
    }

    @Override
    public JsonWriter writeFloatField(String fieldName, float value) {
        context.validateToken(JsonToken.FIELD_NAME);

        try {
            generator.writeNumberField(fieldName, value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return this;
    }

    @Override
    public JsonWriter writeIntField(String fieldName, int value) {
        context.validateToken(JsonToken.FIELD_NAME);

        try {
            generator.writeNumberField(fieldName, value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return this;
    }

    @Override
    public JsonWriter writeLongField(String fieldName, long value) {
        context.validateToken(JsonToken.FIELD_NAME);

        try {
            generator.writeNumberField(fieldName, value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return this;
    }

    @Override
    public JsonWriter writeNullField(String fieldName) {
        context.validateToken(JsonToken.FIELD_NAME);

        try {
            generator.writeNullField(fieldName);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return this;
    }

    @Override
    public JsonWriter writeStringField(String fieldName, String value) {
        context.validateToken(JsonToken.FIELD_NAME);

        try {
            generator.writeStringField(fieldName, value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return this;
    }

    @Override
    public JsonWriter writeRawField(String fieldName, String value) {
        context.validateToken(JsonToken.FIELD_NAME);

        try {
            generator.writeFieldName(fieldName);
            generator.writeRawValue(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return this;
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

        generator.close();
    }
}
