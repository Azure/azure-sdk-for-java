// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Handles serialization of a {@link JsonPatchOperation}.
 */
class JsonPatchOperationSerializer extends JsonSerializer<JsonPatchOperation> {
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
