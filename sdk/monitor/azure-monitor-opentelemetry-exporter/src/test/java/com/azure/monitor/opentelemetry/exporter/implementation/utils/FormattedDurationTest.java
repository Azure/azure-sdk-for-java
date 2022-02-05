// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FormattedDurationTest {
    @Test
    public void testGetFormattedDurationMilliSeconds() {
        String formattedDuration = FormattedDuration.fromNanos(42657024);
        assertEquals("00:00:00.042657", formattedDuration);
    }

    @Test
    public void testGetFormattedDurationSeconds() {
        String formattedDuration = FormattedDuration.fromNanos(42657024000L);
        assertEquals("00:00:42.657024", formattedDuration);
    }

    @Test
    public void testGetFormattedDurationMinutes() {
        String formattedDuration = FormattedDuration.fromNanos(426570240000L);
        assertEquals("00:07:06.570240", formattedDuration);
    }

    @Test
    public void testGetFormattedDurationHours() {
        String formattedDuration = FormattedDuration.fromNanos(4265702400000L);
        assertEquals("01:11:05.702400", formattedDuration);
    }

    @Test
    public void testGetFormattedDurationDays() {
        String formattedDuration = FormattedDuration.fromNanos(426570240000000L);
        assertEquals("4.22:29:30.240000", formattedDuration);
    }

}
