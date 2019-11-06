// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DurationSerializerTests {
    @Test
    public void toStringWithNull() {
        assertNull(DurationSerializer.toString(null));
    }

    @Test
    public void toStringWith0Milliseconds() {
        assertEquals("PT0S", DurationSerializer.toString(Duration.ofMillis(0)));
    }

    @Test
    public void toStringWith1Milliseconds() {
        assertEquals("PT0.001S", DurationSerializer.toString(Duration.ofMillis(1)));
    }

    @Test
    public void toStringWith9Milliseconds() {
        assertEquals("PT0.009S", DurationSerializer.toString(Duration.ofMillis(9)));
    }

    @Test
    public void toStringWith10Milliseconds() {
        assertEquals("PT0.01S", DurationSerializer.toString(Duration.ofMillis(10)));
    }

    @Test
    public void toStringWith11Milliseconds() {
        assertEquals("PT0.011S", DurationSerializer.toString(Duration.ofMillis(11)));
    }

    @Test
    public void toStringWith99Milliseconds() {
        assertEquals("PT0.099S", DurationSerializer.toString(Duration.ofMillis(99)));
    }

    @Test
    public void toStringWith100Milliseconds() {
        assertEquals("PT0.1S", DurationSerializer.toString(Duration.ofMillis(100)));
    }

    @Test
    public void toStringWith101Milliseconds() {
        assertEquals("PT0.101S", DurationSerializer.toString(Duration.ofMillis(101)));
    }

    @Test
    public void toStringWith999Milliseconds() {
        assertEquals("PT0.999S", DurationSerializer.toString(Duration.ofMillis(999)));
    }

    @Test
    public void toStringWith10illiseconds() {
        assertEquals("PT1S", DurationSerializer.toString(Duration.ofMillis(1000)));
    }

    @Test
    public void toStringWith1Second() {
        assertEquals("PT1S", DurationSerializer.toString(Duration.ofSeconds(1)));
    }

    @Test
    public void toStringWith9Seconds() {
        assertEquals("PT9S", DurationSerializer.toString(Duration.ofSeconds(9)));
    }

    @Test
    public void toStringWith10Seconds() {
        assertEquals("PT10S", DurationSerializer.toString(Duration.ofSeconds(10)));
    }

    @Test
    public void toStringWith11Seconds() {
        assertEquals("PT11S", DurationSerializer.toString(Duration.ofSeconds(11)));
    }

    @Test
    public void toStringWith59Seconds() {
        assertEquals("PT59S", DurationSerializer.toString(Duration.ofSeconds(59)));
    }

    @Test
    public void toStringWith60Seconds() {
        assertEquals("PT1M", DurationSerializer.toString(Duration.ofSeconds(60)));
    }

    @Test
    public void toStringWith61Seconds() {
        assertEquals("PT1M1S", DurationSerializer.toString(Duration.ofSeconds(61)));
    }

    @Test
    public void toStringWith1Minute() {
        assertEquals("PT1M", DurationSerializer.toString(Duration.ofMinutes(1)));
    }

    @Test
    public void toStringWith9Minutes() {
        assertEquals("PT9M", DurationSerializer.toString(Duration.ofMinutes(9)));
    }

    @Test
    public void toStringWith10Minutes() {
        assertEquals("PT10M", DurationSerializer.toString(Duration.ofMinutes(10)));
    }

    @Test
    public void toStringWith11Minutes() {
        assertEquals("PT11M", DurationSerializer.toString(Duration.ofMinutes(11)));
    }

    @Test
    public void toStringWith59Minutes() {
        assertEquals("PT59M", DurationSerializer.toString(Duration.ofMinutes(59)));
    }

    @Test
    public void toStringWith60Minutes() {
        assertEquals("PT1H", DurationSerializer.toString(Duration.ofMinutes(60)));
    }

    @Test
    public void toStringWith61Minutes() {
        assertEquals("PT1H1M", DurationSerializer.toString(Duration.ofMinutes(61)));
    }

    @Test
    public void toStringWith1Hour() {
        assertEquals("PT1H", DurationSerializer.toString(Duration.ofHours(1)));
    }

    @Test
    public void toStringWith9Hours() {
        assertEquals("PT9H", DurationSerializer.toString(Duration.ofHours(9)));
    }

    @Test
    public void toStringWith10Hours() {
        assertEquals("PT10H", DurationSerializer.toString(Duration.ofHours(10)));
    }

    @Test
    public void toStringWith11Hours() {
        assertEquals("PT11H", DurationSerializer.toString(Duration.ofHours(11)));
    }

    @Test
    public void toStringWith23Hours() {
        assertEquals("PT23H", DurationSerializer.toString(Duration.ofHours(23)));
    }

    @Test
    public void toStringWith24Hours() {
        assertEquals("P1D", DurationSerializer.toString(Duration.ofHours(24)));
    }

    @Test
    public void toStringWith25Hours() {
        assertEquals("P1DT1H", DurationSerializer.toString(Duration.ofHours(25)));
    }

    @Test
    public void toStringWith1Day() {
        assertEquals("P1D", DurationSerializer.toString(Duration.ofDays(1)));
    }

    @Test
    public void toStringWith9Days() {
        assertEquals("P9D", DurationSerializer.toString(Duration.ofDays(9)));
    }

    @Test
    public void toStringWith10Days() {
        assertEquals("P10D", DurationSerializer.toString(Duration.ofDays(10)));
    }

    @Test
    public void toStringWith11Days() {
        assertEquals("P11D", DurationSerializer.toString(Duration.ofDays(11)));
    }

    @Test
    public void toStringWith99Days() {
        assertEquals("P99D", DurationSerializer.toString(Duration.ofDays(99)));
    }

    @Test
    public void toStringWith100Days() {
        assertEquals("P100D", DurationSerializer.toString(Duration.ofDays(100)));
    }

    @Test
    public void toStringWith101Days() {
        assertEquals("P101D", DurationSerializer.toString(Duration.ofDays(101)));
    }
}
