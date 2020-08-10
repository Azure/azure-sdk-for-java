// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Custom serializer for serializing {@link OffsetDateTime} object into ISO8601 formats.
 */
final class DateTimeSerializer extends JsonSerializer<OffsetDateTime> {
    private static final SimpleModule MODULE;

    static {
        MODULE = new SimpleModule().addSerializer(OffsetDateTime.class, new DateTimeSerializer());
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
    public void serialize(OffsetDateTime value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        if (provider.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)) {
            jgen.writeNumber(value.toInstant().toEpochMilli());
        } else {
            jgen.writeString(toString(value));
        }
    }

    /**
     * Convert the provided OffsetDateTime to its String representation.
     *
     * @param offsetDateTime The OffsetDateTime to convert.
     * @return The String representation of the provided offsetDateTime.
     */
    public static String toString(OffsetDateTime offsetDateTime) {
        String result = null;
        if (offsetDateTime != null) {
            offsetDateTime = offsetDateTime.withOffsetSameInstant(ZoneOffset.UTC);
            result = DateTimeFormatter.ISO_INSTANT.format(offsetDateTime);
            if (result.startsWith("+")) {
                result = result.substring(1);
            }
        }
        return result;
    }
}
