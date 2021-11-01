// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.util.DateTimeRfc1123;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Custom deserializer for deserializing RFC1123 date strings into {@link DateTimeRfc1123} objects.
 */
final class DateTimeRfc1123Deserializer extends JsonDeserializer<DateTimeRfc1123> {
    private static final SimpleModule MODULE;

    static {
        MODULE = new SimpleModule().addDeserializer(DateTimeRfc1123.class, new DateTimeRfc1123Deserializer());
    }

    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson
     * ObjectMapper.
     *
     * @return a simple module to be plugged onto Jackson ObjectMapper.
     */
    public static SimpleModule getModule() {
        return MODULE;
    }

    @Override
    public DateTimeRfc1123 deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonToken token = p.currentToken();

        if (token == JsonToken.VALUE_NUMBER_INT) {
            return new DateTimeRfc1123(OffsetDateTime.ofInstant(Instant.ofEpochSecond(p.getValueAsLong()),
                ZoneOffset.UTC));
        } else {
            return new DateTimeRfc1123(p.getValueAsString());
        }
    }
}
