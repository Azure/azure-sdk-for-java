// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FormattedDurationTest {
    @Test
    public void testGetFormattedDurationMilliSeconds() {
        String formattedDuration = FormattedDuration.fromNanos(42657024);
        assertThat(formattedDuration).isEqualTo("00:00:00.042657");
    }

    @Test
    public void testGetFormattedDurationSeconds() {
        String formattedDuration = FormattedDuration.fromNanos(42657024000L);
        assertThat(formattedDuration).isEqualTo("00:00:42.657024");
    }

    @Test
    public void testGetFormattedDurationMinutes() {
        String formattedDuration = FormattedDuration.fromNanos(426570240000L);
        assertThat(formattedDuration).isEqualTo("00:07:06.570240");
    }

    @Test
    public void testGetFormattedDurationHours() {
        String formattedDuration = FormattedDuration.fromNanos(4265702400000L);
        assertThat(formattedDuration).isEqualTo("01:11:05.702400");
    }

    @Test
    public void testGetFormattedDurationDays() {
        String formattedDuration = FormattedDuration.fromNanos(426570240000000L);
        assertThat(formattedDuration).isEqualTo("4.22:29:30.240000");
    }
}
