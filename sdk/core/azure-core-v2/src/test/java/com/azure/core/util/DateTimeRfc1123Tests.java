// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link DateTimeRfc1123}.
 */
public class DateTimeRfc1123Tests {
    private static final DateTimeFormatter RFC1123_DATE_TIME_FORMATTER
        = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
            .withZone(ZoneId.of("UTC"))
            .withLocale(Locale.US);
    private static final String ZERO_PAD_DATE = "Wed, 01 Dec 2021 01:01:01 GMT";

    @Test
    public void parseDateTimeRfc1123String() {
        OffsetDateTime instantDate = OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
        String dateString = RFC1123_DATE_TIME_FORMATTER.format(instantDate);

        OffsetDateTime expectDateTime = OffsetDateTime.parse(dateString, DateTimeFormatter.RFC_1123_DATE_TIME);
        OffsetDateTime actualDateTime = new DateTimeRfc1123(dateString).getDateTime();
        assertEquals(expectDateTime, actualDateTime);
    }

    @Test
    public void parseDateTimeRfc1123StringWithIllegalArgumentException() {
        // Invalid empty string
        assertThrows(DateTimeException.class, () -> new DateTimeRfc1123(""));
        // Invalid year
        assertThrows(DateTimeException.class, () -> new DateTimeRfc1123("Thu, 01 Jan 19aa 00:00:00 GMT"));
        // Invalid date
        assertThrows(DateTimeException.class, () -> new DateTimeRfc1123("Thu, 00 Jan 1970 00:00:00 GMT"));
        // Invalid month
        assertThrows(DateTimeException.class, () -> new DateTimeRfc1123("Thu, 01 Jjj 1970 00:00:00 GMT"));
        // Invalid hour
        assertThrows(DateTimeException.class, () -> new DateTimeRfc1123("Thu, 01 Jan 1970 25:00:00 GMT"));
        // Invalid minute
        assertThrows(DateTimeException.class, () -> new DateTimeRfc1123("Thu, 01 Jan 1970 00:61:00 GMT"));
        // Invalid second
        assertThrows(DateTimeException.class, () -> new DateTimeRfc1123("Thu, 01 Jan 1970 00:00:61 GMT"));

        // Invalid RFC1123 date time format
        assertThrows(DateTimeException.class, () -> new DateTimeRfc1123("00 Jan 1970 00:00:00 GMT"));
    }

    @ParameterizedTest
    @MethodSource("dateTimeInputs")
    public void toRfc1123String(String expectedDateString, OffsetDateTime dateTime) {
        assertEquals(expectedDateString, DateTimeRfc1123.toRfc1123String(dateTime));
    }

    private static Stream<Arguments> dateTimeInputs() {
        // Epoch instant
        final OffsetDateTime epochInstant = OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
        // Random anytime
        final OffsetDateTime now = OffsetDateTime.now();
        // Zero pad verified
        final OffsetDateTime zeroPadDate = OffsetDateTime.parse(ZERO_PAD_DATE, DateTimeFormatter.RFC_1123_DATE_TIME);
        return Stream.of(Arguments.of(RFC1123_DATE_TIME_FORMATTER.format(epochInstant), epochInstant),
            Arguments.of(RFC1123_DATE_TIME_FORMATTER.format(now), now), Arguments.of(ZERO_PAD_DATE, zeroPadDate));
    }
}
