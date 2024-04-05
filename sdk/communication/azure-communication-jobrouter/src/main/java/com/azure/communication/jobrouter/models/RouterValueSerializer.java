// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * This class provides logic to serialize RouterValue
 */
final class RouterValueSerializer extends JsonSerializer<RouterValue> {
    @Override
    public void serialize(RouterValue value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value.getDoubleValue() != null) {
            gen.writeNumber(value.getDoubleValue());
        } else if (value.getBooleanValue() != null) {
            gen.writeBoolean(value.getBooleanValue());
        } else if (value.getIntValue() != null) {
            gen.writeNumber(value.getIntValue());
        } else if (value.getStringValue() != null) {
            gen.writeString(value.getStringValue());
        } else {
            gen.writeNull();
        }
    }
}
