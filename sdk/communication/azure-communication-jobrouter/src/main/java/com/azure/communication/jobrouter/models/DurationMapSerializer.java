// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

/**
 * This class provides logic to serialize Map<Integer, Duration>
 */
final class DurationMapSerializer extends JsonSerializer<Map<Integer, Duration>> {
    @Override
    public void serialize(Map<Integer, Duration> map, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        map.forEach((key, value) -> {
            try {
                if (value != null) {
                    gen.writeNumberField(String.valueOf(key), value.getSeconds() / 60);
                } else {
                    gen.writeNullField(String.valueOf(key));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        gen.writeEndObject();
    }
}
