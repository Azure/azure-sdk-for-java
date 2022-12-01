// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.gson;

import com.azure.json.JsonOptions;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriteContext;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

/**
 * GSON-based implementation of {@link JsonWriter}.
 */
public final class GsonJsonWriter extends JsonWriter {
    private final com.google.gson.stream.JsonWriter writer;

    // Initial state is always root.
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
    static JsonWriter toWriter(Writer json, JsonOptions options) {
        Objects.requireNonNull(json, "'json' cannot be null.");
        return new GsonJsonWriter(json, options);
    }

    private GsonJsonWriter(Writer writer, JsonOptions options) {
        this.writer = new com.google.gson.stream.JsonWriter(writer);
        this.writer.setLenient(options.isNonNumericNumbersSupported());
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

        writer.flush();
        writer.close();
    }

    @Override
    public JsonWriter flush() throws IOException {
        writer.flush();
        return this;
    }

    @Override
    public JsonWriter writeStartObject() throws IOException {
        context.validateToken(JsonToken.START_OBJECT);
        writer.beginObject();

        context = context.updateContext(JsonToken.START_OBJECT);
        return this;
    }

    @Override
    public JsonWriter writeEndObject() throws IOException {
        context.validateToken(JsonToken.END_OBJECT);
        writer.endObject();

        context = context.updateContext(JsonToken.END_OBJECT);
        return this;
    }

    @Override
    public JsonWriter writeStartArray() throws IOException {
        context.validateToken(JsonToken.START_ARRAY);
        writer.beginArray();

        context = context.updateContext(JsonToken.START_ARRAY);
        return this;
    }

    @Override
    public JsonWriter writeEndArray() throws IOException {
        context.validateToken(JsonToken.END_ARRAY);
        writer.endArray();

        context = context.updateContext(JsonToken.END_ARRAY);
        return this;
    }

    @Override
    public JsonWriter writeFieldName(String fieldName) throws IOException {
        Objects.requireNonNull(fieldName, "'fieldName' cannot be null.");

        context.validateToken(JsonToken.FIELD_NAME);
        writer.name(fieldName);

        context = context.updateContext(JsonToken.FIELD_NAME);
        return this;
    }

    @Override
    public JsonWriter writeBinary(byte[] value) throws IOException {
        context.validateToken(JsonToken.STRING);
        writeBinaryInternal(value);

        context = context.updateContext(JsonToken.STRING);
        return this;
    }

    @Override
    public JsonWriter writeBoolean(boolean value) throws IOException {
        context.validateToken(JsonToken.BOOLEAN);
        writer.value(value);

        context = context.updateContext(JsonToken.BOOLEAN);
        return this;
    }

    @Override
    public JsonWriter writeDouble(double value) throws IOException {
        context.validateToken(JsonToken.NUMBER);
        writer.value(value);

        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeFloat(float value) throws IOException {
        context.validateToken(JsonToken.NUMBER);
        writer.value(value);

        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeInt(int value) throws IOException {
        context.validateToken(JsonToken.NUMBER);
        writer.value(value);

        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeLong(long value) throws IOException {
        context.validateToken(JsonToken.NUMBER);
        writer.value(value);

        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeNull() throws IOException {
        context.validateToken(JsonToken.NULL);
        writer.nullValue();

        context = context.updateContext(JsonToken.NULL);
        return this;
    }

    @Override
    public JsonWriter writeString(String value) throws IOException {
        context.validateToken(JsonToken.STRING);
        writer.value(value);

        context = context.updateContext(JsonToken.STRING);
        return this;
    }

    @Override
    public JsonWriter writeRawValue(String value) throws IOException {
        Objects.requireNonNull(value, "'value' cannot be null.");

        context.validateToken(JsonToken.STRING);
        writer.jsonValue(value);

        context = context.updateContext(JsonToken.STRING);
        return this;
    }

    private void writeBinaryInternal(byte[] value) throws IOException {
        if (value == null) {
            writer.nullValue();
        } else {
            writer.value(Base64.getEncoder().encodeToString(value));
        }
    }
}
