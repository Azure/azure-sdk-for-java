// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HmacDateTimeFormatTests {
    @Test
    public void twoDigitDayOfMonthTest() {
        assertEquals("Wed, 01 Jan 2020 01:01:00 GMT", getDateTimeString(2020, 1, 1, 1, 1, 0));
        assertEquals("Fri, 10 Jan 2020 01:01:00 GMT", getDateTimeString(2020, 1, 10, 1, 1, 0));
    }

    @Test
    public void leapYearTest() {
        assertEquals("Thu, 29 Feb 2024 01:01:00 GMT", getDateTimeString(2024, 2, 29, 1, 1, 0));
    }    

    @Test
    public void twentyFourHourClockTest() {
        assertEquals("Wed, 01 Jan 2020 23:01:00 GMT", getDateTimeString(2020, 1, 1, 23, 1, 0));
    }

    private String getDateTimeString(int year, int month, int day, int hour, int minute, int second) {
        ZonedDateTime dateTime = ZonedDateTime.of(year, month, day, hour, minute, second, 0 /* nanoOfSecond */, ZoneId.of("UTC"));
        return dateTime.format(CommunicationClientCredential.HMAC_DATETIMEFORMATTER_PATTERN);
    }
}
