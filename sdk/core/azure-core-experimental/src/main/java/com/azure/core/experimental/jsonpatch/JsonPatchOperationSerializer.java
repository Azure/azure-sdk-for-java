// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.jsonpatch;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.util.Optional;

/**
 * Handles serialization of a {@link JsonPatchOperation}.
 */
public class JsonPatchOperationSerializer extends JsonSerializer<JsonPatchOperation> {
    private static final Module MODULE;

    static {
        MODULE = new SimpleModule()
            .addSerializer(JsonPatchOperation.class, new JsonPatchOperationSerializer());
    }

    /**
     * Gets the module for this serializer that can be added into an {@link ObjectMapper}.
     *
     * @return The module for this serializer.
     */
    public static Module getModule() {
        return MODULE;
    }

    @Override
    public void serialize(JsonPatchOperation value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
        gen.writeStartObject();

        gen.writeStringField("op", value.getOperationKind().toString());

        String from = value.getFrom();
        if (from != null) {
            gen.writeStringField("from", from);
        }

        gen.writeStringField("path", value.getPath());

        Optional<String> optionalValue = value.getOptionalValue();
        if (optionalValue != null) {
            if (optionalValue.isPresent()) {
                gen.writeFieldName("value");
                gen.writeRawValue(optionalValue.get());
            } else {
                gen.writeNullField("value");
            }
        }

        gen.writeEndObject();
    }
}
