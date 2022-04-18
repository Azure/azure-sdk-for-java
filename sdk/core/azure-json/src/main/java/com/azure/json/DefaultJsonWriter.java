// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import com.azure.json.implementation.IoExceptionInvoker;
import com.azure.json.implementation.IoExceptionSupplier;
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
    public static DefaultJsonWriter toStream(OutputStream stream) {
        return callWithWrappedIoException(() -> new DefaultJsonWriter(FACTORY.createGenerator(stream)));
    }

    private DefaultJsonWriter(JsonGenerator generator) {
        this.generator = generator;
    }

    @Override
    public JsonWriter flush() {
        callWithWrappedIoException(generator::flush);

        return this;
    }

    @Override
    public JsonWriter writeStartObject() {
        callWithWrappedIoException(generator::writeStartObject, JsonWriteOperation.START_OBJECT);

        return this;
    }

    @Override
    public JsonWriter writeEndObject() {
        callWithWrappedIoException(generator::writeEndObject, JsonWriteOperation.END_OBJECT);

        return this;
    }

    @Override
    public JsonWriter writeStartArray() {
        callWithWrappedIoException(generator::writeStartArray, JsonWriteOperation.START_ARRAY);

        return this;
    }

    @Override
    public JsonWriter writeEndArray() {
        callWithWrappedIoException(generator::writeEndArray, JsonWriteOperation.END_ARRAY);

        return this;
    }

    @Override
    public JsonWriter writeFieldName(String fieldName) {
        callWithWrappedIoException(() -> generator.writeFieldName(fieldName), JsonWriteOperation.FIELD_NAME);

        return this;
    }

    @Override
    public JsonWriter writeBinary(byte[] value) {
        callWithWrappedIoException(() -> {
            if (value == null) {
                generator.writeNull();
            } else {
                generator.writeBinary(value);
            }
        }, JsonWriteOperation.SIMPLE_VALUE);

        return this;
    }

    @Override
    public JsonWriter writeBoolean(boolean value) {
        callWithWrappedIoException(() -> generator.writeBoolean(value), JsonWriteOperation.SIMPLE_VALUE);

        return this;
    }

    @Override
    public JsonWriter writeDouble(double value) {
        callWithWrappedIoException(() -> generator.writeNumber(value), JsonWriteOperation.SIMPLE_VALUE);

        return this;
    }

    @Override
    public JsonWriter writeFloat(float value) {
        callWithWrappedIoException(() -> generator.writeNumber(value), JsonWriteOperation.SIMPLE_VALUE);

        return this;
    }

    @Override
    public JsonWriter writeInt(int value) {
        callWithWrappedIoException(() -> generator.writeNumber(value), JsonWriteOperation.SIMPLE_VALUE);

        return this;
    }

    @Override
    public JsonWriter writeLong(long value) {
        callWithWrappedIoException(() -> generator.writeNumber(value), JsonWriteOperation.SIMPLE_VALUE);

        return this;
    }

    @Override
    public JsonWriter writeNull() {
        callWithWrappedIoException(generator::writeNull, JsonWriteOperation.SIMPLE_VALUE);

        return this;
    }

    @Override
    public JsonWriter writeString(String value) {
        callWithWrappedIoException(() -> generator.writeString(value), JsonWriteOperation.SIMPLE_VALUE);

        return this;
    }

    @Override
    public JsonWriter writeRawValue(String value) {
        callWithWrappedIoException(() -> generator.writeRawValue(value), JsonWriteOperation.SIMPLE_VALUE);

        return this;
    }

    @Override
    public JsonWriter writeBinaryField(String fieldName, byte[] value) {
        callWithWrappedIoException(() -> {
            if (value == null) {
                generator.writeNullField(fieldName);
            } else {
                generator.writeBinaryField(fieldName, value);
            }
        }, JsonWriteOperation.FIELD_AND_VALUE);

        return this;
    }

    @Override
    public JsonWriter writeBooleanField(String fieldName, boolean value) {
        callWithWrappedIoException(() -> generator.writeBooleanField(fieldName, value),
            JsonWriteOperation.FIELD_AND_VALUE);

        return this;
    }

    @Override
    public JsonWriter writeDoubleField(String fieldName, double value) {
        callWithWrappedIoException(() -> generator.writeNumberField(fieldName, value),
            JsonWriteOperation.FIELD_AND_VALUE);

        return this;
    }

    @Override
    public JsonWriter writeFloatField(String fieldName, float value) {
        callWithWrappedIoException(() -> generator.writeNumberField(fieldName, value),
            JsonWriteOperation.FIELD_AND_VALUE);

        return this;
    }

    @Override
    public JsonWriter writeIntField(String fieldName, int value) {
        callWithWrappedIoException(() -> generator.writeNumberField(fieldName, value),
            JsonWriteOperation.FIELD_AND_VALUE);

        return this;
    }

    @Override
    public JsonWriter writeLongField(String fieldName, long value) {
        callWithWrappedIoException(() -> generator.writeNumberField(fieldName, value),
            JsonWriteOperation.FIELD_AND_VALUE);

        return this;
    }

    @Override
    public JsonWriter writeNullField(String fieldName) {
        callWithWrappedIoException(() -> generator.writeNullField(fieldName),
            JsonWriteOperation.FIELD_AND_VALUE);

        return this;
    }

    @Override
    public JsonWriter writeStringField(String fieldName, String value) {
        callWithWrappedIoException(() -> generator.writeStringField(fieldName, value),
            JsonWriteOperation.FIELD_AND_VALUE);

        return this;
    }

    @Override
    public JsonWriter writeRawField(String fieldName, String value) {
        callWithWrappedIoException(() -> {
            generator.writeFieldName(fieldName);
            generator.writeRawValue(value);
        }, JsonWriteOperation.FIELD_AND_VALUE);

        return this;
    }

    @Override
    public JsonWriteContext writeContext() {
        return context;
    }

    @Override
    public void close() throws IOException {
        if (context != JsonWriteContext.COMPLETED) {
            throw new IllegalStateException("Writing of the JSON object must be completed before the writer can be "
                + "closed. Current writing state is '" + context.getContext() + "'.");
        }

        generator.close();
    }

    private static <T> T callWithWrappedIoException(IoExceptionSupplier<T> func) {
        return func.getWithUncheckedIoException();
    }

    private void callWithWrappedIoException(IoExceptionInvoker func) {
        func.invokeWithUncheckedIoException();
    }

    private void callWithWrappedIoException(IoExceptionInvoker func, JsonWriteOperation operation) {
        // Validate the operation.
        context.validateOperation(operation);

        // Perform the operation.
        func.invokeWithUncheckedIoException();

        // Update state.
        context = context.updateContext(operation);
    }
}
