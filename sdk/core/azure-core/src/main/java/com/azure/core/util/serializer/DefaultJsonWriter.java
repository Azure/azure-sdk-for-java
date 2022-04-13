// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.azure.core.implementation.jackson.core.JsonFactory;
import com.azure.core.implementation.jackson.core.JsonGenerator;
import com.azure.core.util.logging.ClientLogger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;

/**
 * Default {@link JsonWriter} implementation.
 */
public final class DefaultJsonWriter implements JsonWriter {
    private static final ClientLogger LOGGER = new ClientLogger(DefaultJsonWriter.class);

    private static final JsonFactory FACTORY = JsonFactory.builder().build();

    private final JsonGenerator generator;

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
        callWithWrappedIoException(() -> generator.writeStartObject());

        return this;
    }

    @Override
    public JsonWriter writeEndObject() {
        callWithWrappedIoException(generator::writeEndObject);

        return this;
    }

    @Override
    public JsonWriter writeStartArray() {
        callWithWrappedIoException(() -> generator.writeStartArray());

        return this;
    }

    @Override
    public JsonWriter writeEndArray() {
        callWithWrappedIoException(generator::writeEndArray);

        return this;
    }

    @Override
    public JsonWriter writeFieldName(String fieldName) {
        callWithWrappedIoException(() -> generator.writeFieldName(fieldName));

        return this;
    }

    @Override
    public JsonWriter writeBinary(byte[] value) {
        callWithWrappedIoException(() -> generator.writeBinary(value));

        return this;
    }

    @Override
    public JsonWriter writeBoolean(boolean value) {
        callWithWrappedIoException(() -> generator.writeBoolean(value));

        return this;
    }

    @Override
    public JsonWriter writeDouble(double value) {
        callWithWrappedIoException(() -> generator.writeNumber(value));

        return this;
    }

    @Override
    public JsonWriter writeFloat(float value) {
        callWithWrappedIoException(() -> generator.writeNumber(value));

        return this;
    }

    @Override
    public JsonWriter writeInt(int value) {
        callWithWrappedIoException(() -> generator.writeNumber(value));

        return this;
    }

    @Override
    public JsonWriter writeLong(long value) {
        callWithWrappedIoException(() -> generator.writeNumber(value));

        return this;
    }

    @Override
    public JsonWriter writeNull() {
        callWithWrappedIoException(generator::writeNull);

        return this;
    }

    @Override
    public JsonWriter writeString(String value) {
        callWithWrappedIoException(() -> generator.writeString(value));

        return this;
    }

    @Override
    public JsonWriter writeRawValue(String value) {
        callWithWrappedIoException(() -> generator.writeRawValue(value));

        return this;
    }

    @Override
    public JsonWriter writeBinaryField(String fieldName, byte[] value) {
        callWithWrappedIoException(() -> generator.writeBinaryField(fieldName, value));

        return this;
    }

    @Override
    public JsonWriter writeBooleanField(String fieldName, boolean value) {
        callWithWrappedIoException(() -> generator.writeBooleanField(fieldName, value));

        return this;
    }

    @Override
    public JsonWriter writeDoubleField(String fieldName, double value) {
        callWithWrappedIoException(() -> generator.writeNumberField(fieldName, value));

        return this;
    }

    @Override
    public JsonWriter writeFloatField(String fieldName, float value) {
        callWithWrappedIoException(() -> generator.writeNumberField(fieldName, value));

        return this;
    }

    @Override
    public JsonWriter writeIntField(String fieldName, int value) {
        callWithWrappedIoException(() -> generator.writeNumberField(fieldName, value));

        return this;
    }

    @Override
    public JsonWriter writeLongField(String fieldName, long value) {
        callWithWrappedIoException(() -> generator.writeNumberField(fieldName, value));

        return this;
    }

    @Override
    public JsonWriter writeNullField(String fieldName) {
        callWithWrappedIoException(() -> generator.writeNullField(fieldName));

        return this;
    }

    @Override
    public JsonWriter writeStringField(String fieldName, String value) {
        callWithWrappedIoException(() -> generator.writeStringField(fieldName, value));

        return this;
    }

    @Override
    public JsonWriter writeRawField(String fieldName, String value) {
        callWithWrappedIoException(() -> {
            generator.writeFieldName(fieldName);
            generator.writeRawValue(value);
        });

        return this;
    }

    @Override
    public void close() throws IOException {
        generator.close();
    }

    private static <T> T callWithWrappedIoException(IoExceptionSupplier<T> func) {
        return func.getWithUncheckedIoException(LOGGER);
    }

    private static void callWithWrappedIoException(IoExceptionInvoker func) {
        func.invokeWithUncheckedIoException(LOGGER);
    }
}
