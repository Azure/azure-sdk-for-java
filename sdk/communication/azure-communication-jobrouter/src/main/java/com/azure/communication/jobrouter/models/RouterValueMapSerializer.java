// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Map;

/**
 * This class provides logic to serialize Map<String, RouterValue>
 */
final class RouterValueMapSerializer extends JsonSerializer<Map<String, RouterValue>> {
    @Override
    public void serialize(Map<String, RouterValue> map, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        map.forEach((key, value) -> {
            try {
                if (value.getDoubleValue() != null) {
                    gen.writeNumberField(key, value.getDoubleValue());
                } else if (value.getBooleanValue() != null) {
                    gen.writeBooleanField(key, value.getBooleanValue());
                } else if (value.getIntValue() != null) {
                    gen.writeNumberField(key, value.getIntValue());
                } else if (value.getStringValue() != null) {
                    gen.writeStringField(key, value.getStringValue());
                } else {
                    gen.writeNullField(key);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        gen.writeEndObject();
    }
}
