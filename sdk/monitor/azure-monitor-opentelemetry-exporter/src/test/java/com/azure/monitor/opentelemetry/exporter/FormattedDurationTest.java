// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.monitor.opentelemetry.exporter.utils.FormattedDuration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FormattedDurationTest {
    @Test
    void testGetFormattedDurationMilliSeconds() {
        String formattedDuration = FormattedDuration.getFormattedDuration(42657024);
        assertEquals("00:00:00.042657", formattedDuration);
    }

    @Test
    void testGetFormattedDurationSeconds() {
        String formattedDuration = FormattedDuration.getFormattedDuration(42657024000L);
        assertEquals("00:00:42.657024", formattedDuration);
    }

    @Test
    void testGetFormattedDurationMinutes() {
        String formattedDuration = FormattedDuration.getFormattedDuration(426570240000L);
        assertEquals("00:07:06.570240", formattedDuration);
    }

    @Test
    void testGetFormattedDurationHours() {
        String formattedDuration = FormattedDuration.getFormattedDuration(4265702400000L);
        assertEquals("01:11:05.702400", formattedDuration);
    }

    @Test
    void testGetFormattedDurationDays() {
        String formattedDuration = FormattedDuration.getFormattedDuration(426570240000000L);
        assertEquals("4.22:29:30.240000", formattedDuration);
    }

}
