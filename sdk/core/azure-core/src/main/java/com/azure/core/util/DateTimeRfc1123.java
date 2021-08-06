// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.util.logging.ClientLogger;

import java.time.DateTimeException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Wrapper over java.time.OffsetDateTime used for specifying RFC1123 format during serialization and deserialization.
 */
public final class DateTimeRfc1123 {
    private static final ClientLogger logger = new ClientLogger(DateTimeRfc1123.class);

    /**
     * The pattern of the datetime used for RFC1123 datetime format.
     */
    private static final DateTimeFormatter RFC1123_DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").withZone(ZoneId.of("UTC")).withLocale(Locale.US);
    /**
     * The actual datetime object.
     */
    private final OffsetDateTime dateTime;

    /**
     * Creates a new DateTimeRfc1123 object with the specified DateTime.
     * @param dateTime The DateTime object to wrap.
     */
    public DateTimeRfc1123(OffsetDateTime dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * Creates a new DateTimeRfc1123 object with the specified DateTime.
     * @param formattedString The datetime string in RFC1123 format
     */
    public DateTimeRfc1123(String formattedString) {
        this.dateTime = parse(formattedString);
    }

    /**
     * Returns the underlying DateTime.
     * @return The underlying DateTime.
     */
    public OffsetDateTime getDateTime() {
        return this.dateTime;
    }

    /**
     * Parses the RFC1123 format datetime string into OffsetDateTime.
     *
     * @param date The datetime string in RFC1123 format
     * @return The underlying OffsetDateTime.
     *
     * @throws DateTimeException If the processing character is not a digit character.
     * @throws IllegalArgumentException if the given character is not recognized in the pattern of Month. such as 'Jan'.
     * @throws IndexOutOfBoundsException if the {@code beginIndex} is negative, or beginIndex is larger than length of
     *   {@code date}.
     */
    private static OffsetDateTime parse(final String date) {
        try {
            return OffsetDateTime.of(
                parseInt(date, 12, 16),  // year
                parseMonth(date, 8), // month
                parseInt(date, 5, 7),    // dayOfMonth
                parseInt(date, 17, 19),  // hour
                parseInt(date, 20, 22),  // minute
                parseInt(date, 23, 25),  // second
                0,                    // nanoOfSecond
                ZoneOffset.UTC);
        } catch (Exception e) {
            return OffsetDateTime.parse(date, DateTimeFormatter.RFC_1123_DATE_TIME);
        }
    }

    /**
     * Parses the specified substring of datetime to a 'int' value.
     *
     * @param date The datetime string in RFC1123 format.
     * @param beginIndex The beginning index, inclusive.
     * @param endIndex The ending index, exclusive.
     * @return The specified substring.
     *
     * @throws DateTimeException If the processing character is not digit character.
     */
    private static int parseInt(final CharSequence date, final int beginIndex, final int endIndex) {
        int num = 0;
        for (int i = beginIndex; i < endIndex; i++) {
            final char c = date.charAt(i);
            if (c < '0' || c > '9') {
                throw logger.logExceptionAsError(new DateTimeException("Invalid date time: " + date));
            }
            num = num * 10 + (c - '0');
        }

        return num;
    }

    /**
     * Parses the specified month substring of datetime to a number value, '1' represents the month of January,
     * '12' represents the month of December.
     *
     * @param date The datetime string in RFC1123 format.
     * @param beginIndex The beginning index, inclusive, to the
     * @return The number value which represents the month of year. '1' represents the month of January,
     *   '12' represents the month of December.
     * @throws IllegalArgumentException if the given character is not recognized in the pattern of Month. such as 'Jan'.
     * @throws IndexOutOfBoundsException if the {@code beginIndex} is negative, or beginIndex is larger than length of
     *   {@code date}.
     */
    private static int parseMonth(final CharSequence date, final int beginIndex) {
        switch (date.charAt(beginIndex)) {
            case 'J':
                // Jan, Jun, Jul
                switch (date.charAt(beginIndex + 1)) {
                    case 'a': return 1; // Jan
                    case 'u':
                        switch (date.charAt(beginIndex + 2)) {
                            case 'n': return 6; // Jun
                            case 'l': return 7; // Jul
                            default: throw logger.logExceptionAsError(
                                new IllegalArgumentException("Unknown month " + date));
                        }
                    default: throw logger.logExceptionAsError(new IllegalArgumentException("Unknown month " + date));
                }
            case 'F': return 2; // Feb
            case 'M':
                // Mar, May
                switch (date.charAt(beginIndex + 2)) {
                    case 'r': return 3; // Mar
                    case 'y': return 5; // May
                    default: throw logger.logExceptionAsError(new IllegalArgumentException("Unknown month " + date));
                }
            case 'A':
                // Apr, Aug
                switch (date.charAt(beginIndex + 2)) {
                    case 'r': return 4; // Apr
                    case 'g': return 8; // Aug
                    default: throw logger.logExceptionAsError(new IllegalArgumentException("Unknown month " + date));
                }
            case 'S': return 9; //Sep
            case 'O': return 10; // Oct
            case 'N': return 11; // Nov
            case 'D': return 12; // Dec
            default: throw logger.logExceptionAsError(new IllegalArgumentException("Unknown month " + date));
        }
    }

    @Override
    public String toString() {
        return RFC1123_DATE_TIME_FORMATTER.format(this.dateTime);
    }

    @Override
    public int hashCode() {
        return this.dateTime.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof DateTimeRfc1123)) {
            return false;
        }

        DateTimeRfc1123 rhs = (DateTimeRfc1123) obj;
        return this.dateTime.equals(rhs.getDateTime());
    }
}
