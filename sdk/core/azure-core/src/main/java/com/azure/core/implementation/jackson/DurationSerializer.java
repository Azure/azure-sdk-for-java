// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.util.CoreUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.time.Duration;

/**
 * Custom serializer for serializing {@link Duration} object into ISO8601 formats.
 */
final class DurationSerializer extends JsonSerializer<Duration> {
    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson
     * ObjectMapper.
     *
     * @return a simple module to be plugged onto Jackson ObjectMapper.
     */
    public static SimpleModule getModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Duration.class, new DurationSerializer());
        return module;
    }

    @Override
    public void serialize(Duration duration, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
        throws IOException {
        jsonGenerator.writeString(DurationSerializer.toString(duration));
    }

    /**
     * Convert to provided Duration to an ISO 8601 String with a days component.
     * @param duration The Duration to convert.
     * @return The String representation of the provided Duration.
     */
    public static String toString(Duration duration) {
        return CoreUtils.durationToStringWithDays(duration);
    }
}
