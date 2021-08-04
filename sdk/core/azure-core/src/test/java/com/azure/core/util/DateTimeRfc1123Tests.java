// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link DateTimeRfc1123}.
 */
public class DateTimeRfc1123Tests {
    private static final DateTimeFormatter RFC1123_DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").withZone(ZoneId.of("UTC")).withLocale(Locale.US);

    @Test
    public void parseDateTimeRfc1123String() {
        OffsetDateTime instantDate = OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
        String dateString = RFC1123_DATE_TIME_FORMATTER.format(instantDate);

        OffsetDateTime expectDateTime = OffsetDateTime.parse(dateString, DateTimeFormatter.RFC_1123_DATE_TIME);
        OffsetDateTime actualDateTime = new DateTimeRfc1123(dateString).getDateTime();
        assertEquals(expectDateTime, actualDateTime);
    }
}
