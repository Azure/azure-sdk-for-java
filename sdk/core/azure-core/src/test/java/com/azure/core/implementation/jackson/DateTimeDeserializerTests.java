// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.util.serializer.JacksonAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link DateTimeDeserializer}.
 */
public class DateTimeDeserializerTests {
    private static final ObjectMapper MAPPER = new JacksonAdapter().serializer();

    @ParameterizedTest
    @MethodSource("deserializeOffsetDateTimeSupplier")
    public void deserializeJson(String dateTimeJson, OffsetDateTime expected) throws IOException {
        assertEquals(expected, MAPPER.readValue(dateTimeJson, OffsetDateTime.class));
    }

    private static Stream<Arguments> deserializeOffsetDateTimeSupplier() {
        OffsetDateTime minValue = OffsetDateTime.of(1, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime unixEpoch = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        OffsetDateTime nonUtcTimeZone = OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(-7));

        return Stream.of(
            Arguments.of("\"0001-01-01T00:00:00\"", minValue),
            Arguments.of(String.valueOf(minValue.toEpochSecond()), minValue),
            Arguments.of("\"0001-01-01T00:00:00Z\"", minValue),
            Arguments.of("\"1970-01-01T00:00:00\"", unixEpoch),
            Arguments.of("\"1970-01-01T00:00:00Z\"", unixEpoch),
            Arguments.of("\"2020-01-01T00:00:00-07:00\"", nonUtcTimeZone)
        );
    }
}
