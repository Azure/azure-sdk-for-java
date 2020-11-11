// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.fasterxml.jackson.core.JsonParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DateTimeDeserializer}.
 */
public class DateTimeDeserializerTests {
    @ParameterizedTest
    @MethodSource("deserializeOffsetDateTimeSupplier")
    public void deserializeJson(String offsetDateTimeString, OffsetDateTime expected) throws IOException {
        JsonParser parser = mock(JsonParser.class);
        when(parser.getValueAsString()).thenReturn(offsetDateTimeString);

        assertEquals(expected, new DateTimeDeserializer().deserialize(parser, null));
    }

    private static Stream<Arguments> deserializeOffsetDateTimeSupplier() {
        OffsetDateTime minValue = OffsetDateTime.of(1, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime unixEpoch = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        return Stream.of(
            Arguments.of("0001-01-01T00:00:00", minValue),
            Arguments.of("0001-01-01T00:00:00Z", minValue),
            Arguments.of("1970-01-01T00:00:00", unixEpoch),
            Arguments.of("1970-01-01T00:00:00Z", unixEpoch)
        );
    }
}
