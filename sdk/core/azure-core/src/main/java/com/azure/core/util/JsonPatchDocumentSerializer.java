// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.List;

/**
 * Handles serialization of a {@link JsonPatchDocument}.
 */
class JsonPatchDocumentSerializer extends JsonSerializer<JsonPatchDocument> {
    @Override
    public void serialize(JsonPatchDocument value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
        List<JsonPatchOperation> operations = value.getOperations();
        if (CoreUtils.isNullOrEmpty(operations)) {
            return;
        }

        gen.writeStartArray(operations.size());

        for (JsonPatchOperation operation : operations) {
            gen.writeObject(operation);
        }

        gen.writeEndArray();
    }
}
