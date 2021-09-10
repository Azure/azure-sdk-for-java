package com.azure.monitor.opentelemetry.exporter;

import com.azure.monitor.opentelemetry.exporter.utils.FormattedDuration;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FormattedDurationTest {
    @Test
    void testGetFormattedDurationMilliSeconds() {
        Duration duration= Duration.ofNanos(42657024);
        String formattedDuration = FormattedDuration.getFormattedDuration(duration);
        assertEquals("00:00:00.042657", formattedDuration);
    }

    @Test
    void testGetFormattedDurationSeconds() {
        Duration duration= Duration.ofNanos(42657024000L);
        String formattedDuration = FormattedDuration.getFormattedDuration(duration);
        assertEquals("00:00:42.42657024", formattedDuration);
    }

    @Test
    void testGetFormattedDurationMinutes() {
        Duration duration= Duration.ofNanos(426570240000L);
        String formattedDuration = FormattedDuration.getFormattedDuration(duration);
        assertEquals("00:07:426.426570240", formattedDuration);
    }
}
