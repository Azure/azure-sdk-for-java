// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DateTimeSerializerTests {
    @Test
    public void toStringWithNull() {
        assertNull(DateTimeSerializer.toString(null));
    }

    @ParameterizedTest
    @MethodSource("toStringOffsetDateTimeSupplier")
    public void toStringOffsetDateTime(OffsetDateTime dateTime, String expected) {
        assertEquals(expected, DateTimeSerializer.toString(dateTime));
    }

    private static Stream<Arguments> toStringOffsetDateTimeSupplier() {
        return Stream.of(
            Arguments.of(OffsetDateTime.of(1, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(-14)), "0001-01-01T14:00:00Z"),
            Arguments.of(OffsetDateTime.of(LocalDate.of(10000, 1, 1), LocalTime.parse("13:59:59.999"), ZoneOffset.UTC),
                "10000-01-01T13:59:59.999Z"),
            Arguments.of(OffsetDateTime.of(2010, 1, 1, 12, 34, 56, 0, ZoneOffset.UTC), "2010-01-01T12:34:56Z"),
            Arguments.of(OffsetDateTime.of(1, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), "0001-01-01T00:00:00Z"));
    }
}
