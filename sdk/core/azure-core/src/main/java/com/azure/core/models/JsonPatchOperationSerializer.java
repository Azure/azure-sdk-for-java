// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.implementation.Option;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;

/**
 * Handles the serialization of a {@link JsonPatchOperation}.
 *
 * <p>This class is responsible for converting a {@link JsonPatchOperation} into a JSON representation. It extends the
 * {@link JsonSerializer} class from the Jackson library and overrides the
 * {@link #serialize(JsonPatchOperation, JsonGenerator, SerializerProvider)} method to perform the serialization.</p>
 *
 * <p>The {@link #getModule()} method returns a {@link SimpleModule} that wraps this serializer, which can be
 * registered with a Jackson {@link com.fasterxml.jackson.databind.ObjectMapper} to handle the serialization of
 * {@link JsonPatchOperation} instances.</p>
 *
 * <p>This class is useful when you want to serialize a {@link JsonPatchOperation} to JSON. For example, you can use
 * it when you want to send a JSON Patch document as part of an HTTP request.</p>
 *
 * @see JsonPatchOperation
 * @see JsonSerializer
 * @see JsonGenerator
 * @see SerializerProvider
 * @see SimpleModule
 */
final class JsonPatchOperationSerializer extends JsonSerializer<JsonPatchOperation> {
    private static final SimpleModule MODULE;

    static {
        MODULE = new SimpleModule().addSerializer(JsonPatchOperation.class, new JsonPatchOperationSerializer());
    }

    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson ObjectMapper.
     *
     * @return a simple module to be plugged onto Jackson ObjectMapper.
     */
    public static SimpleModule getModule() {
        return MODULE;
    }

    @Override
    public void serialize(JsonPatchOperation value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
        gen.writeStartObject();

        gen.writeStringField("op", value.getOp().toString());

        String from = value.getFrom();
        if (from != null) {
            gen.writeStringField("from", from);
        }

        gen.writeStringField("path", value.getPath());

        Option<String> optionalValue = value.getValue();
        if (optionalValue.isInitialized()) {
            String val = optionalValue.getValue();
            if (val != null) {
                gen.writeFieldName("value");
                gen.writeRawValue(val);
            } else {
                gen.writeNullField("value");
            }
        }

        gen.writeEndObject();
    }
}
