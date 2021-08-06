// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link TimeInterval}.
 */
public class TimeIntervalTest {
    private static final OffsetDateTime REFERENCE_TIME = OffsetDateTime.now()
            .withDayOfMonth(1)
            .withMonth(7)
            .withYear(2021)
            .withSecond(0)
            .withMinute(0)
            .withHour(0)
            .withNano(0)
            .withOffsetSameLocal(ZoneOffset.UTC);

    @Test
    public void testDurationTimeSpan() {
        TimeInterval timeInterval = new TimeInterval(Duration.ofDays(1));
        assertEquals("PT24H", timeInterval.toIso8601Format());
    }

    @Test
    public void testStartAndEndDateTimeSpan() {
        TimeInterval timeInterval =
                new TimeInterval(REFERENCE_TIME, REFERENCE_TIME.plusDays(5));
        assertEquals("2021-07-01T00:00Z/2021-07-06T00:00Z", timeInterval.toIso8601Format());
    }

    @Test
    public void testStartDateAndEndDurationTimeSpan() {
        TimeInterval timeInterval = new TimeInterval(REFERENCE_TIME, Duration.ofDays(1));
        assertEquals("2021-07-01T00:00Z/PT24H", timeInterval.toIso8601Format());
    }

    @Test
    public void testStartDurationAndEndTimeSpan() {
        TimeInterval timeInterval = new TimeInterval(Duration.ofDays(1), REFERENCE_TIME);
        assertEquals("PT24H/2021-07-01T00:00Z", timeInterval.toIso8601Format());
    }

    @Test
    public void testInvalidTimeSpans() {
        Assertions.assertThrows(NullPointerException.class, () -> new TimeInterval(null));
        Assertions.assertThrows(NullPointerException.class, () -> new TimeInterval(null, (Duration) null));
        Assertions.assertThrows(NullPointerException.class, () -> new TimeInterval((OffsetDateTime) null, (OffsetDateTime) null));

        Assertions.assertThrows(NullPointerException.class, () -> new TimeInterval((OffsetDateTime) null, OffsetDateTime.now()));
        Assertions.assertThrows(NullPointerException.class, () -> new TimeInterval(OffsetDateTime.now(),
                (OffsetDateTime) null));

        Assertions.assertThrows(NullPointerException.class, () -> new TimeInterval(null, Duration.ofDays(2)));
        Assertions.assertThrows(NullPointerException.class, () -> new TimeInterval(OffsetDateTime.now(),
                (Duration) null));
    }

    @Test
    public void testParse() {
        TimeInterval timeInterval = TimeInterval.parse("2021-07-01T00:00Z/2021-07-06T00:00Z");
        assertEquals(REFERENCE_TIME, timeInterval.getStartTime());
        assertEquals(REFERENCE_TIME.plusDays(5), timeInterval.getEndTime());

        timeInterval = TimeInterval.parse("PT24H/2021-07-01T00:00Z");
        assertEquals(Duration.ofDays(1), timeInterval.getDuration());
        assertEquals(REFERENCE_TIME, timeInterval.getEndTime());

        timeInterval = TimeInterval.parse("2021-07-01T00:00Z/PT24H");
        assertEquals(REFERENCE_TIME, timeInterval.getStartTime());
        assertEquals(Duration.ofDays(1), timeInterval.getDuration());

        timeInterval = TimeInterval.parse("PT24H");
        assertEquals(Duration.ofDays(1), timeInterval.getDuration());

        // empty string
        Assertions.assertThrows(IllegalArgumentException.class, () -> TimeInterval.parse(""));

        // no end time/duration
        Assertions.assertThrows(IllegalArgumentException.class, () -> TimeInterval.parse("2021-07-01T00:00Z/"));

        // no start time
        Assertions.assertThrows(IllegalArgumentException.class, () -> TimeInterval.parse("/2021-07-01T00:00Z"));

        // no range
        Assertions.assertThrows(IllegalArgumentException.class, () -> TimeInterval.parse("2021-07-01T00:00Z"));

        // invalid duration
        Assertions.assertThrows(IllegalArgumentException.class, () -> TimeInterval.parse("PT24"));

        // no end time
        Assertions.assertThrows(IllegalArgumentException.class, () -> TimeInterval.parse("PT24H/"));

        // start and end both are durations
        Assertions.assertThrows(IllegalArgumentException.class, () -> TimeInterval.parse("PT24H/PT48H"));

        // no start time
        Assertions.assertThrows(IllegalArgumentException.class, () -> TimeInterval.parse("/PT24H"));

        // invalid datetime
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> TimeInterval.parse("2021-07-01T00:00Z/2021-07-01T00:00"));
    }

    @Test
    public void testEquality() {
        assertEquals(TimeInterval.LAST_DAY, TimeInterval.parse("PT24H"));
        assertEquals(TimeInterval.LAST_2_DAYS, TimeInterval.parse("PT48H"));
        assertEquals(new TimeInterval(Duration.ofDays(1)), TimeInterval.parse("PT24H"));
        assertEquals(new TimeInterval(REFERENCE_TIME, Duration.ofDays(1)),
                TimeInterval.parse("2021-07-01T00:00Z/PT24H"));
        assertEquals(new TimeInterval(Duration.ofDays(1), REFERENCE_TIME),
                TimeInterval.parse("PT24H/2021-07-01T00:00Z"));
        assertEquals(new TimeInterval(REFERENCE_TIME, REFERENCE_TIME.plusDays(2)),
                TimeInterval.parse("2021-07-01T00:00Z/2021-07-03T00:00Z"));
    }
}
