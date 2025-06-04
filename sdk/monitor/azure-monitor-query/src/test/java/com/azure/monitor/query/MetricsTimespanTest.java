// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.monitor.query.implementation.metrics.models.MetricsHelper;
import com.azure.monitor.query.models.QueryTimeInterval;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the toMetricsTimespan method fix.
 */
public class MetricsTimespanTest {

    @Test
    public void testDurationOnlyInterval() {
        // Test the case that was failing: QueryTimeInterval.LAST_30_MINUTES
        QueryTimeInterval interval = QueryTimeInterval.LAST_30_MINUTES;
        String result = MetricsHelper.toMetricsTimespan(interval);

        assertNotNull(result, "Result should not be null");
        assertTrue(result.contains("/"), "Result should contain '/' separator");
        assertTrue(result.matches(".*\\d{4}-\\d{2}-\\d{2}T.*"), "Result should contain absolute timestamps");

        // Verify it's not just the duration string
        assertTrue(!result.equals("PT30M"), "Result should not be just the duration string");
    }

    @Test
    public void testStartAndEndTimes() {
        OffsetDateTime start = OffsetDateTime.parse("2025-01-01T00:00:00Z");
        OffsetDateTime end = OffsetDateTime.parse("2025-01-01T01:00:00Z");
        QueryTimeInterval interval = new QueryTimeInterval(start, end);

        String result = MetricsHelper.toMetricsTimespan(interval);
        assertEquals("2025-01-01T00:00Z/2025-01-01T01:00Z", result);
    }

    @Test
    public void testStartTimeAndDuration() {
        OffsetDateTime start = OffsetDateTime.parse("2025-01-01T00:00:00Z");
        Duration duration = Duration.ofHours(1);
        QueryTimeInterval interval = new QueryTimeInterval(start, duration);

        String result = MetricsHelper.toMetricsTimespan(interval);
        assertEquals("2025-01-01T00:00Z/2025-01-01T01:00Z", result);
    }

    @Test
    public void testNullInterval() {
        String result = MetricsHelper.toMetricsTimespan(null);
        assertEquals(null, result);
    }
}
