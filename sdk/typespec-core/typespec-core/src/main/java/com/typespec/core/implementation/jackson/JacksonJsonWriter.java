// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.jackson;

import com.typespec.core.util.logging.ClientLogger;
import com.typespec.json.JsonToken;
import com.typespec.json.JsonWriteContext;
import com.typespec.json.JsonWriter;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.Objects;

// Copied from azure-core-serializer-json-jackson
/**
 * Jackson-based implementation of {@link JsonWriter}.
 */
final class JacksonJsonWriter extends JsonWriter {
    private static final ClientLogger LOGGER = new ClientLogger(JacksonJsonWriter.class);

    private final JsonGenerator generator;

    // Initial state is always root.
    private JsonWriteContext context = JsonWriteContext.ROOT;

    JacksonJsonWriter(JsonGenerator generator) {
        this.generator = Objects.requireNonNull(generator,
            "Cannot create a Jackson-based instance of com.azure.json.JsonWriter with a null Jackson JsonGenerator.");
    }

    @Override
    public JsonWriteContext getWriteContext() {
        return context;
    }

    @Override
    public void close() throws IOException {
        if (context != JsonWriteContext.COMPLETED) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("Writing of the JSON object must be completed "
                + "before the writer can be closed. Current writing state is '" + context.getWriteState() + "'."));
        }

        generator.flush();
        generator.close();
    }

    @Override
    public JsonWriter flush() throws IOException {
        generator.flush();
        return this;
    }

    @Override
    public JsonWriter writeStartObject() throws IOException {
        context.validateToken(JsonToken.START_OBJECT);
        generator.writeStartObject();

        context = context.updateContext(JsonToken.START_OBJECT);
        return this;
    }

    @Override
    public JsonWriter writeEndObject() throws IOException {
        context.validateToken(JsonToken.END_OBJECT);
        generator.writeEndObject();

        context = context.updateContext(JsonToken.END_OBJECT);
        return this;
    }

    @Override
    public JsonWriter writeStartArray() throws IOException {
        context.validateToken(JsonToken.START_ARRAY);
        generator.writeStartArray();

        context = context.updateContext(JsonToken.START_ARRAY);
        return this;
    }

    @Override
    public JsonWriter writeEndArray() throws IOException {
        context.validateToken(JsonToken.END_ARRAY);
        generator.writeEndArray();

        context = context.updateContext(JsonToken.END_ARRAY);
        return this;
    }

    @Override
    public JsonWriter writeFieldName(String fieldName) throws IOException {
        Objects.requireNonNull(fieldName, "'fieldName' cannot be null.");

        context.validateToken(JsonToken.FIELD_NAME);
        generator.writeFieldName(fieldName);

        context = context.updateContext(JsonToken.FIELD_NAME);
        return this;
    }

    @Override
    public JsonWriter writeBinary(byte[] value) throws IOException {
        context.validateToken(JsonToken.STRING);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeBinary(value);
        }

        context = context.updateContext(JsonToken.STRING);
        return this;
    }

    @Override
    public JsonWriter writeBoolean(boolean value) throws IOException {
        context.validateToken(JsonToken.BOOLEAN);
        generator.writeBoolean(value);

        context = context.updateContext(JsonToken.BOOLEAN);
        return this;
    }

    @Override
    public JsonWriter writeDouble(double value) throws IOException {
        context.validateToken(JsonToken.NUMBER);
        generator.writeNumber(value);

        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeFloat(float value) throws IOException {
        context.validateToken(JsonToken.NUMBER);
        generator.writeNumber(value);

        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeInt(int value) throws IOException {
        context.validateToken(JsonToken.NUMBER);
        generator.writeNumber(value);

        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeLong(long value) throws IOException {
        context.validateToken(JsonToken.NUMBER);
        generator.writeNumber(value);

        context = context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeNull() throws IOException {
        context.validateToken(JsonToken.NULL);
        generator.writeNull();

        context = context.updateContext(JsonToken.NULL);
        return this;
    }

    @Override
    public JsonWriter writeString(String value) throws IOException {
        context.validateToken(JsonToken.STRING);
        generator.writeString(value);

        context = context.updateContext(JsonToken.STRING);
        return this;
    }

    @Override
    public JsonWriter writeRawValue(String value) throws IOException {
        Objects.requireNonNull(value, "'value' cannot be null.");

        context.validateToken(JsonToken.STRING);
        generator.writeRawValue(value);

        context = context.updateContext(JsonToken.STRING);
        return this;
    }
}
