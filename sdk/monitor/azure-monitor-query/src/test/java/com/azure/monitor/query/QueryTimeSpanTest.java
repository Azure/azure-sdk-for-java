// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.monitor.query.models.QueryTimeSpan;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Unit tests for {@link QueryTimeSpan}.
 */
public class QueryTimeSpanTest {

    @Test
    public void testDurationTimeSpan() {
        QueryTimeSpan queryTimeSpan = new QueryTimeSpan(Duration.ofDays(1));
        System.out.println("Duration " + queryTimeSpan);
    }

    @Test
    public void testStartAndEndDateTimeSpan() {
        QueryTimeSpan queryTimeSpan = new QueryTimeSpan(OffsetDateTime.now().minusDays(5),
            OffsetDateTime.now().minusDays(2));
        System.out.println("Start date, end date " + queryTimeSpan);
    }

    @Test
    public void testStartDateAndEndDurationTimeSpan() {
        QueryTimeSpan queryTimeSpan = new QueryTimeSpan(OffsetDateTime.now().minusDays(5), Duration.ofDays(1));
        System.out.println("Start date, end duration " + queryTimeSpan);
    }

    @Test
    public void testInvalidTimeSpans() {
        Assertions.assertThrows(NullPointerException.class, () -> new QueryTimeSpan(null));
        Assertions.assertThrows(NullPointerException.class, () -> new QueryTimeSpan(null, (Duration) null));
        Assertions.assertThrows(NullPointerException.class, () -> new QueryTimeSpan(null, (OffsetDateTime) null));

        Assertions.assertThrows(NullPointerException.class, () -> new QueryTimeSpan(null, OffsetDateTime.now()));
        Assertions.assertThrows(NullPointerException.class, () -> new QueryTimeSpan(OffsetDateTime.now(),
            (OffsetDateTime) null));

        Assertions.assertThrows(NullPointerException.class, () -> new QueryTimeSpan(null, Duration.ofDays(2)));
        Assertions.assertThrows(NullPointerException.class, () -> new QueryTimeSpan(OffsetDateTime.now(),
            (Duration) null));

    }
}
