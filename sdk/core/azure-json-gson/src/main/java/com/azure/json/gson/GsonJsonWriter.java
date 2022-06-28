// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.gson;

import com.azure.json.JsonToken;
import com.azure.json.JsonWriteContext;
import com.azure.json.JsonWriter;
import com.azure.json.implementation.CheckExceptionUtils;
import com.azure.json.implementation.IoExceptionInvoker;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.azure.json.implementation.CheckExceptionUtils.invokeWithWrappedIoException;

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
     * @param stream The {@link OutputStream} that will be written.
     * @return An instance of {@link GsonJsonWriter}.
     */
    public static JsonWriter fromStream(OutputStream stream) {
        return new GsonJsonWriter(new com.google.gson.stream.JsonWriter(
            new OutputStreamWriter(stream, StandardCharsets.UTF_8)));
    }

    private GsonJsonWriter(com.google.gson.stream.JsonWriter writer) {
        this.writer = writer;
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

        writer.close();
    }

    @Override
    public JsonWriter flush() {
        invokeWithWrappedIoException(writer::flush);
        return this;
    }

    @Override
    public JsonWriter writeStartObject() {
        validateAndUpdate(writer::beginObject, JsonToken.START_OBJECT, false);
        return this;
    }

    @Override
    public JsonWriter writeEndObject() {
        validateAndUpdate(writer::beginObject, JsonToken.END_OBJECT, false);
        return this;
    }

    @Override
    public JsonWriter writeStartArray() {
        validateAndUpdate(writer::beginObject, JsonToken.START_ARRAY, false);
        return this;
    }

    @Override
    public JsonWriter writeEndArray() {
        validateAndUpdate(writer::beginObject, JsonToken.END_ARRAY, false);
        return this;
    }

    @Override
    public JsonWriter writeFieldName(String fieldName) {
        validateAndUpdate(() -> writer.name(fieldName), JsonToken.FIELD_NAME, false);
        return this;
    }

    @Override
    public JsonWriter writeBinary(byte[] value) {
        validateAndUpdate(() -> writeBinaryInternal(value), JsonToken.STRING, false);
        return this;
    }

    @Override
    public JsonWriter writeBoolean(boolean value) {
        validateAndUpdate(() -> writer.value(value), JsonToken.BOOLEAN, false);
        return this;
    }

    @Override
    public JsonWriter writeDouble(double value) {
        validateAndUpdate(() -> writer.value(value), JsonToken.NUMBER, false);
        return this;
    }

    @Override
    public JsonWriter writeFloat(float value) {
        validateAndUpdate(() -> writer.value(value), JsonToken.NUMBER, false);
        return this;
    }

    @Override
    public JsonWriter writeInt(int value) {
        validateAndUpdate(() -> writer.value(value), JsonToken.NUMBER, false);
        return this;
    }

    @Override
    public JsonWriter writeLong(long value) {
        validateAndUpdate(() -> writer.value(value), JsonToken.NUMBER, false);
        return this;
    }

    @Override
    public JsonWriter writeNull() {
        validateAndUpdate(writer::nullValue, JsonToken.NULL, false);
        return this;
    }

    @Override
    public JsonWriter writeString(String value) {
        validateAndUpdate(() -> writer.value(value), JsonToken.STRING, false);
        return this;
    }

    @Override
    public JsonWriter writeRawValue(String value) {
        validateAndUpdate(() -> writer.jsonValue(value), JsonToken.STRING, false);
        return this;
    }

    @Override
    public JsonWriter writeBinaryField(String fieldName, byte[] value) {
        validateAndUpdate(() -> {
            writer.name(fieldName);
            writeBinaryInternal(value);
        }, JsonToken.FIELD_NAME, true);
        return this;
    }

    @Override
    public JsonWriter writeBooleanField(String fieldName, boolean value) {
        validateAndUpdate(() -> writer.name(fieldName).value(value), JsonToken.FIELD_NAME, true);
        return this;
    }

    @Override
    public JsonWriter writeDoubleField(String fieldName, double value) {
        validateAndUpdate(() -> writer.name(fieldName).value(value), JsonToken.FIELD_NAME, true);
        return this;
    }

    @Override
    public JsonWriter writeFloatField(String fieldName, float value) {
        validateAndUpdate(() -> writer.name(fieldName).value(value), JsonToken.FIELD_NAME, true);
        return this;
    }

    @Override
    public JsonWriter writeIntField(String fieldName, int value) {
        validateAndUpdate(() -> writer.name(fieldName).value(value), JsonToken.FIELD_NAME, true);
        return this;
    }

    @Override
    public JsonWriter writeLongField(String fieldName, long value) {
        validateAndUpdate(() -> writer.name(fieldName).value(value), JsonToken.FIELD_NAME, true);
        return this;
    }

    @Override
    public JsonWriter writeNullField(String fieldName) {
        validateAndUpdate(() -> writer.name(fieldName).nullValue(), JsonToken.FIELD_NAME, true);
        return this;
    }

    @Override
    public JsonWriter writeStringField(String fieldName, String value) {
        validateAndUpdate(() -> writer.name(fieldName).value(value), JsonToken.FIELD_NAME, true);
        return this;
    }

    @Override
    public JsonWriter writeRawField(String fieldName, String value) {
        validateAndUpdate(() -> writer.name(fieldName).jsonValue(value), JsonToken.FIELD_NAME, true);
        return this;
    }

    private void writeBinaryInternal(byte[] value) throws IOException {
        if (value == null) {
            writer.nullValue();
        } else {
            writer.value(Base64.getEncoder().encodeToString(value));
        }
    }

    private void validateAndUpdate(IoExceptionInvoker func, JsonToken token, boolean fieldAndValue) {
        CheckExceptionUtils.validateAndUpdate(func, context1 -> context = context1, context, token, fieldAndValue);
    }
}
