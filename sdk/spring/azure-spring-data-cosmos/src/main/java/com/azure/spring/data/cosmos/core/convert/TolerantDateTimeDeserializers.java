// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.convert;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Tolerant deserializers for java.time types that handle both epoch-millis numbers
 * (backward compatibility) and ISO-8601 strings (new default format).
 *
 * <p>This ensures existing data stored as epoch numbers can still be read after the
 * default serialization format is changed to ISO-8601 strings.</p>
 */
final class TolerantDateTimeDeserializers {

    private TolerantDateTimeDeserializers() {
    }

    /**
     * Creates a Jackson module with tolerant deserializers for all supported java.time types.
     *
     * @return a {@link SimpleModule} with tolerant deserializers registered
     */
    static SimpleModule createModule() {
        SimpleModule module = new SimpleModule("CosmosTolerantDateTimeModule");
        module.addDeserializer(ZonedDateTime.class, new TolerantZonedDateTimeDeserializer());
        module.addDeserializer(LocalDateTime.class, new TolerantLocalDateTimeDeserializer());
        module.addDeserializer(Instant.class, new TolerantInstantDeserializer());
        module.addDeserializer(OffsetDateTime.class, new TolerantOffsetDateTimeDeserializer());
        return module;
    }

    /**
     * Deserializer for {@link ZonedDateTime} that accepts both epoch-millis and ISO-8601 strings.
     */
    static final class TolerantZonedDateTimeDeserializer extends JsonDeserializer<ZonedDateTime> {
        @Override
        public ZonedDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.currentToken() == JsonToken.VALUE_NUMBER_INT
                || p.currentToken() == JsonToken.VALUE_NUMBER_FLOAT) {
                long epochMillis = p.getLongValue();
                return Instant.ofEpochMilli(epochMillis).atZone(ZoneOffset.UTC);
            }
            String text = p.getText().trim();
            return ZonedDateTime.parse(text, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        }
    }

    /**
     * Deserializer for {@link LocalDateTime} that accepts both epoch-millis and ISO-8601 strings.
     */
    static final class TolerantLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.currentToken() == JsonToken.VALUE_NUMBER_INT
                || p.currentToken() == JsonToken.VALUE_NUMBER_FLOAT) {
                long epochMillis = p.getLongValue();
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC);
            }
            String text = p.getText().trim();
            return LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }

    /**
     * Deserializer for {@link Instant} that accepts both epoch-millis and ISO-8601 strings.
     */
    static final class TolerantInstantDeserializer extends JsonDeserializer<Instant> {
        @Override
        public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.currentToken() == JsonToken.VALUE_NUMBER_INT
                || p.currentToken() == JsonToken.VALUE_NUMBER_FLOAT) {
                long epochMillis = p.getLongValue();
                return Instant.ofEpochMilli(epochMillis);
            }
            String text = p.getText().trim();
            return Instant.parse(text);
        }
    }

    /**
     * Deserializer for {@link OffsetDateTime} that accepts both epoch-millis and ISO-8601 strings.
     */
    static final class TolerantOffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {
        @Override
        public OffsetDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.currentToken() == JsonToken.VALUE_NUMBER_INT
                || p.currentToken() == JsonToken.VALUE_NUMBER_FLOAT) {
                long epochMillis = p.getLongValue();
                return OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC);
            }
            String text = p.getText().trim();
            return OffsetDateTime.parse(text, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
    }
}
