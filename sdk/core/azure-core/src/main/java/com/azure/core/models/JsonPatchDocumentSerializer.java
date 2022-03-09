// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.util.CoreUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.util.List;

/**
 * Handles serialization of a {@link JsonPatchDocument}.
 */
final class JsonPatchDocumentSerializer extends JsonSerializer<JsonPatchDocument> {
    private static final SimpleModule MODULE;

    static {
        MODULE = new SimpleModule().addSerializer(JsonPatchDocument.class, new JsonPatchDocumentSerializer());
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
    public void serialize(JsonPatchDocument value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
        List<JsonPatchOperation> operations = value.getOperations();
        if (CoreUtils.isNullOrEmpty(operations)) {
            return;
        }

        gen.writeStartArray();

        for (JsonPatchOperation operation : operations) {
            gen.writeObject(operation);
        }

        gen.writeEndArray();
    }
}
