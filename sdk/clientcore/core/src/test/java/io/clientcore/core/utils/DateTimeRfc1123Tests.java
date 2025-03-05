// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.DateTimeException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoField.YEAR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link DateTimeRfc1123}.
 */
public class DateTimeRfc1123Tests {
    private static final DateTimeFormatter FORMATTER;

    // Need to custom create the DateTimeFormatter for RFC1123 as Java supports the obsolete ANSI C variant which
    // allows one or two numbers in the day of month.
    static {
        Map<Long, String> dow = new HashMap<>();
        dow.put(1L, "Mon");
        dow.put(2L, "Tue");
        dow.put(3L, "Wed");
        dow.put(4L, "Thu");
        dow.put(5L, "Fri");
        dow.put(6L, "Sat");
        dow.put(7L, "Sun");
        Map<Long, String> moy = new HashMap<>();
        moy.put(1L, "Jan");
        moy.put(2L, "Feb");
        moy.put(3L, "Mar");
        moy.put(4L, "Apr");
        moy.put(5L, "May");
        moy.put(6L, "Jun");
        moy.put(7L, "Jul");
        moy.put(8L, "Aug");
        moy.put(9L, "Sep");
        moy.put(10L, "Oct");
        moy.put(11L, "Nov");
        moy.put(12L, "Dec");
        FORMATTER = new DateTimeFormatterBuilder().parseCaseInsensitive()
            .parseLenient()
            .optionalStart()
            .appendText(DAY_OF_WEEK, dow)
            .appendLiteral(", ")
            .optionalEnd()
            .appendValue(DAY_OF_MONTH, 2, 2, SignStyle.NOT_NEGATIVE)
            .appendLiteral(' ')
            .appendText(MONTH_OF_YEAR, moy)
            .appendLiteral(' ')
            .appendValue(YEAR, 4)  // 2 digit year not handled
            .appendLiteral(' ')
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .optionalStart()
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
            .optionalEnd()
            .appendLiteral(" GMT")
            .toFormatter(Locale.getDefault(Locale.Category.FORMAT));
    }

    @Test
    public void nullDateTimeStringReturnsNull() {
        assertNull(DateTimeRfc1123.fromString(null));
    }

    @Test
    public void emptyDateTimeStringReturnsNull() {
        assertNull(DateTimeRfc1123.fromString(""));
    }

    @ParameterizedTest
    @MethodSource("validDateTimeSupplier")
    public void validParseDateTime(OffsetDateTime expected) {
        assertEquals(expected, DateTimeRfc1123.fromString(FORMATTER.format(expected)).getDateTime());
    }

    @ParameterizedTest
    @MethodSource("validDateTimeSupplier")
    public void validFormatDateTime(OffsetDateTime expected) {
        assertEquals(FORMATTER.format(expected), DateTimeRfc1123.toRfc1123String(expected));
    }

    private static Stream<OffsetDateTime> validDateTimeSupplier() {
        // Each one of these dates is a different month and uses different years, days, hours, minutes, seconds
        // to ensure that the parsing is correct.
        OffsetDateTime january = OffsetDateTime.of(1, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime february = OffsetDateTime.of(10, 2, 2, 2, 5, 5, 0, ZoneOffset.UTC);
        OffsetDateTime march = OffsetDateTime.of(100, 3, 4, 4, 10, 10, 0, ZoneOffset.UTC);
        OffsetDateTime april = OffsetDateTime.of(1000, 4, 6, 6, 15, 15, 0, ZoneOffset.UTC);
        OffsetDateTime may = OffsetDateTime.of(1970, 5, 8, 8, 20, 20, 0, ZoneOffset.UTC);
        OffsetDateTime june = OffsetDateTime.of(2000, 6, 10, 10, 25, 25, 0, ZoneOffset.UTC);
        OffsetDateTime july = OffsetDateTime.of(2010, 7, 12, 12, 30, 30, 0, ZoneOffset.UTC);
        OffsetDateTime august = OffsetDateTime.of(2020, 8, 14, 14, 35, 35, 0, ZoneOffset.UTC);
        OffsetDateTime september = OffsetDateTime.of(2030, 9, 16, 16, 40, 40, 0, ZoneOffset.UTC);
        OffsetDateTime october = OffsetDateTime.of(2040, 10, 18, 18, 45, 45, 0, ZoneOffset.UTC);
        OffsetDateTime november = OffsetDateTime.of(2050, 11, 20, 20, 50, 50, 0, ZoneOffset.UTC);
        OffsetDateTime december = OffsetDateTime.of(2060, 12, 22, 22, 59, 59, 0, ZoneOffset.UTC);

        return Stream.of(january, february, march, april, may, june, july, august, september, october, november,
            december);
    }

    @ParameterizedTest
    @MethodSource("invalidDateStringSupplier")
    public void invalidDateTimeStringThrowsException(String dateTimeString) {
        assertThrows(DateTimeException.class, () -> DateTimeRfc1123.fromString(dateTimeString));
    }

    private static Stream<String> invalidDateStringSupplier() {
        // Day of the week is ignored when parsing.
        return Stream.of("Wed, ab Jan 2021 01:01:01 GMT", // Day of month isn't numeric
            "Wed, 99 Jan 2021 01:01:01 GMT", // Day of month isn't in range
            "Wed, 01 Jan abcd 01:01:01 GMT", // Year isn't numeric
            "Wed, 01 Jan 2021 ab:01:01 GMT", // Hour isn't numeric
            "Wed, 01 Jan 2021 25:01:01 GMT", // Hour isn't in range
            "Wed, 01 Jan 2021 01:ab:01 GMT", // Minute isn't numeric
            "Wed, 01 Jan 2021 01:61:01 GMT", // Minute isn't in range
            "Wed, 01 Jan 2021 01:01:ab GMT", // Second isn't numeric
            "Wed, 01 Jan 2021 01:01:61 GMT", // Second isn't in range
            "Wed, 01 Jan 2021 01:01:01", // Missing GMT
            "Wed, 01 Jan 2021 01:01:01 GMT GMT", // Extra GMT
            "Wed, 01 Jab 2021 01:01:01 GMT", // Invalid month
            "Wed, 01 Juk 2021 01:01:01 GMT", // Invalid month
            "Wed, 01 Fed 2021 01:01:01 GMT", // Invalid month
            "Wed, 01 Nay 2021 01:01:01 GMT", // Invalid month
            "Wed, 01 Mae 2021 01:01:01 GMT", // Invalid month
            "Wed, 01 Apo 2021 01:01:01 GMT", // Invalid month
            "Wed, 01 Auh 2021 01:01:01 GMT", // Invalid month
            "Wed, 01 Seo 2021 01:01:01 GMT", // Invalid month
            "Wed, 01 Ocr 2021 01:01:01 GMT", // Invalid month
            "Wed, 01 Nob 2021 01:01:01 GMT", // Invalid month
            "Wed, 01 Dev 2021 01:01:01 GMT" // Invalid month
        );
    }
}
