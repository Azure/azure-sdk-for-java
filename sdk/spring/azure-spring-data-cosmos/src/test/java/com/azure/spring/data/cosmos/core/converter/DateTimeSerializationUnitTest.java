// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.converter;

import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.convert.ObjectMapperFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for date/time serialization and deserialization behavior,
 * verifying ISO-8601 string output and backward-compatible epoch-millis reading.
 */
public class DateTimeSerializationUnitTest {

    private final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    // ===== Serialization tests: verify ISO string output =====

    @Test
    public void serializeZonedDateTimeAsIsoString() throws JsonProcessingException {
        ZonedDateTime zdt = ZonedDateTime.of(2024, 1, 15, 10, 30, 45, 0, ZoneOffset.UTC);
        String json = objectMapper.writeValueAsString(zdt);
        assertThat(json).contains("2024-01-15");
        assertThat(json).contains("10:30:45");
        // Must NOT be a plain number (epoch)
        assertThat(json).startsWith("\"");
    }

    @Test
    public void serializeLocalDateTimeAsIsoString() throws JsonProcessingException {
        LocalDateTime ldt = LocalDateTime.of(2024, 3, 20, 14, 0, 0);
        String json = objectMapper.writeValueAsString(ldt);
        assertThat(json).contains("2024-03-20");
        assertThat(json).contains("14:00:00");
        assertThat(json).startsWith("\"");
    }

    @Test
    public void serializeInstantAsIsoString() throws JsonProcessingException {
        Instant instant = Instant.parse("2024-06-01T12:00:00Z");
        String json = objectMapper.writeValueAsString(instant);
        assertThat(json).contains("2024-06-01");
        assertThat(json).contains("12:00:00");
        assertThat(json).startsWith("\"");
    }

    @Test
    public void serializeOffsetDateTimeAsIsoString() throws JsonProcessingException {
        OffsetDateTime odt = OffsetDateTime.of(2024, 9, 10, 8, 15, 30, 0, ZoneOffset.ofHours(5));
        String json = objectMapper.writeValueAsString(odt);
        assertThat(json).contains("2024-09-10");
        assertThat(json).contains("08:15:30");
        assertThat(json).startsWith("\"");
    }

    // ===== Deserialization tests: epoch-millis backward compat =====

    @Test
    public void deserializeZonedDateTimeFromEpochMillis() throws JsonProcessingException {
        // 2024-01-15T10:30:45Z = 1705312245000L
        long epochMillis = 1705312245000L;
        ZonedDateTime result = objectMapper.readValue(String.valueOf(epochMillis), ZonedDateTime.class);
        assertThat(result.toInstant().toEpochMilli()).isEqualTo(epochMillis);
    }

    @Test
    public void deserializeLocalDateTimeFromEpochMillis() throws JsonProcessingException {
        long epochMillis = 1705312245000L;
        LocalDateTime result = objectMapper.readValue(String.valueOf(epochMillis), LocalDateTime.class);
        Instant reconverted = result.toInstant(ZoneOffset.UTC);
        assertThat(reconverted.toEpochMilli()).isEqualTo(epochMillis);
    }

    @Test
    public void deserializeInstantFromEpochMillis() throws JsonProcessingException {
        long epochMillis = 1705312245000L;
        Instant result = objectMapper.readValue(String.valueOf(epochMillis), Instant.class);
        assertThat(result.toEpochMilli()).isEqualTo(epochMillis);
    }

    @Test
    public void deserializeOffsetDateTimeFromEpochMillis() throws JsonProcessingException {
        long epochMillis = 1705312245000L;
        OffsetDateTime result = objectMapper.readValue(String.valueOf(epochMillis), OffsetDateTime.class);
        assertThat(result.toInstant().toEpochMilli()).isEqualTo(epochMillis);
    }

    // ===== Deserialization tests: ISO string input =====

    @Test
    public void deserializeZonedDateTimeFromIsoString() throws JsonProcessingException {
        String isoString = "\"2024-01-15T10:30:45Z\"";
        ZonedDateTime result = objectMapper.readValue(isoString, ZonedDateTime.class);
        assertThat(result.getYear()).isEqualTo(2024);
        assertThat(result.getMonthValue()).isEqualTo(1);
        assertThat(result.getDayOfMonth()).isEqualTo(15);
        assertThat(result.getHour()).isEqualTo(10);
    }

    @Test
    public void deserializeLocalDateTimeFromIsoString() throws JsonProcessingException {
        String isoString = "\"2024-03-20T14:00:00\"";
        LocalDateTime result = objectMapper.readValue(isoString, LocalDateTime.class);
        assertThat(result.getYear()).isEqualTo(2024);
        assertThat(result.getMonthValue()).isEqualTo(3);
        assertThat(result.getHour()).isEqualTo(14);
    }

    @Test
    public void deserializeInstantFromIsoString() throws JsonProcessingException {
        String isoString = "\"2024-06-01T12:00:00Z\"";
        Instant result = objectMapper.readValue(isoString, Instant.class);
        assertThat(result).isEqualTo(Instant.parse("2024-06-01T12:00:00Z"));
    }

    @Test
    public void deserializeOffsetDateTimeFromIsoString() throws JsonProcessingException {
        String isoString = "\"2024-09-10T08:15:30+05:00\"";
        OffsetDateTime result = objectMapper.readValue(isoString, OffsetDateTime.class);
        assertThat(result.getYear()).isEqualTo(2024);
        assertThat(result.getOffset()).isEqualTo(ZoneOffset.ofHours(5));
    }

    // ===== Round-trip tests: serialize → deserialize → equals =====

    @Test
    public void roundTripZonedDateTime() throws JsonProcessingException {
        ZonedDateTime original = ZonedDateTime.of(2024, 7, 4, 16, 30, 0, 0, ZoneId.of("UTC"));
        String json = objectMapper.writeValueAsString(original);
        ZonedDateTime restored = objectMapper.readValue(json, ZonedDateTime.class);
        assertThat(restored.toInstant()).isEqualTo(original.toInstant());
    }

    @Test
    public void roundTripLocalDateTime() throws JsonProcessingException {
        LocalDateTime original = LocalDateTime.of(2024, 11, 25, 9, 45, 15);
        String json = objectMapper.writeValueAsString(original);
        LocalDateTime restored = objectMapper.readValue(json, LocalDateTime.class);
        assertThat(restored).isEqualTo(original);
    }

    @Test
    public void roundTripInstant() throws JsonProcessingException {
        Instant original = Instant.parse("2024-12-31T23:59:59Z");
        String json = objectMapper.writeValueAsString(original);
        Instant restored = objectMapper.readValue(json, Instant.class);
        assertThat(restored).isEqualTo(original);
    }

    @Test
    public void roundTripOffsetDateTime() throws JsonProcessingException {
        OffsetDateTime original = OffsetDateTime.of(2024, 5, 1, 12, 0, 0, 0, ZoneOffset.ofHours(-4));
        String json = objectMapper.writeValueAsString(original);
        OffsetDateTime restored = objectMapper.readValue(json, OffsetDateTime.class);
        assertThat(restored.toInstant()).isEqualTo(original.toInstant());
    }

    // ===== toCosmosDbValue tests =====

    @Test
    public void toCosmosDbValueConvertsZonedDateTimeToIsoString() {
        ZonedDateTime zdt = ZonedDateTime.of(2024, 1, 15, 10, 30, 45, 0, ZoneOffset.UTC);
        Object result = MappingCosmosConverter.toCosmosDbValue(zdt);
        assertThat(result).isInstanceOf(String.class);
        assertThat((String) result).contains("2024-01-15");
    }

    @Test
    public void toCosmosDbValueConvertsLocalDateTimeToIsoString() {
        LocalDateTime ldt = LocalDateTime.of(2024, 3, 20, 14, 0, 0);
        Object result = MappingCosmosConverter.toCosmosDbValue(ldt);
        assertThat(result).isInstanceOf(String.class);
        assertThat((String) result).isEqualTo("2024-03-20T14:00:00");
    }

    @Test
    public void toCosmosDbValueConvertsOffsetDateTimeToIsoString() {
        OffsetDateTime odt = OffsetDateTime.of(2024, 9, 10, 8, 15, 30, 0, ZoneOffset.ofHours(5));
        Object result = MappingCosmosConverter.toCosmosDbValue(odt);
        assertThat(result).isInstanceOf(String.class);
        assertThat((String) result).contains("2024-09-10T08:15:30");
    }

    @Test
    public void toCosmosDbValueConvertsInstantToIsoString() {
        Instant instant = Instant.parse("2024-06-01T12:00:00Z");
        Object result = MappingCosmosConverter.toCosmosDbValue(instant);
        assertThat(result).isInstanceOf(String.class);
        assertThat((String) result).isEqualTo("2024-06-01T12:00:00Z");
    }

    @Test
    public void toCosmosDbValueReturnsNullForNull() {
        Object result = MappingCosmosConverter.toCosmosDbValue(null);
        assertThat(result).isNull();
    }
}
