// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.jsonpatch;

import com.azure.core.util.CoreUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;

/**
 * Handles serialization of a {@link JsonPatchDocument}.
 */
public class JsonPatchDocumentSerializer extends JsonSerializer<JsonPatchDocument> {
    private static final Module MODULE;

    static {
        MODULE = new SimpleModule().addSerializer(JsonPatchDocument.class, new JsonPatchDocumentSerializer());
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
    public void serialize(JsonPatchDocument value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
        if (CoreUtils.isNullOrEmpty(value.getJsonPatchOperations())) {
            return;
        }

        gen.writeStartArray(value.getJsonPatchOperations().size());

        for (JsonPatchOperation operation : value.getJsonPatchOperations()) {
            gen.writeObject(operation);
        }

        gen.writeEndArray();
    }
}
