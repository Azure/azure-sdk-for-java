// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DurationSerializerTests {
    @Test
    public void toStringWithNull() {
        assertNull(DurationSerializer.toString(null));
    }

    @ParameterizedTest
    @MethodSource("toStringTestSupplier")
    public void toStringTest(Duration duration, String expected) {
        assertEquals(expected, DurationSerializer.toString(duration));
    }

    private static Stream<Arguments> toStringTestSupplier() {
        return Stream.of(Arguments.of(Duration.ofMillis(0), "PT0S"), Arguments.of(Duration.ofMillis(1), "PT0.001S"),
            Arguments.of(Duration.ofMillis(9), "PT0.009S"), Arguments.of(Duration.ofMillis(10), "PT0.01S"),
            Arguments.of(Duration.ofMillis(11), "PT0.011S"), Arguments.of(Duration.ofMillis(99), "PT0.099S"),
            Arguments.of(Duration.ofMillis(100), "PT0.1S"), Arguments.of(Duration.ofMillis(101), "PT0.101S"),
            Arguments.of(Duration.ofMillis(999), "PT0.999S"), Arguments.of(Duration.ofMillis(1000), "PT1S"),
            Arguments.of(Duration.ofSeconds(1), "PT1S"), Arguments.of(Duration.ofSeconds(9), "PT9S"),
            Arguments.of(Duration.ofSeconds(10), "PT10S"), Arguments.of(Duration.ofSeconds(11), "PT11S"),
            Arguments.of(Duration.ofSeconds(59), "PT59S"), Arguments.of(Duration.ofSeconds(60), "PT1M"),
            Arguments.of(Duration.ofSeconds(61), "PT1M1S"), Arguments.of(Duration.ofMinutes(1), "PT1M"),
            Arguments.of(Duration.ofMinutes(9), "PT9M"), Arguments.of(Duration.ofMinutes(10), "PT10M"),
            Arguments.of(Duration.ofMinutes(11), "PT11M"), Arguments.of(Duration.ofMinutes(59), "PT59M"),
            Arguments.of(Duration.ofMinutes(60), "PT1H"), Arguments.of(Duration.ofMinutes(61), "PT1H1M"),
            Arguments.of(Duration.ofHours(1), "PT1H"), Arguments.of(Duration.ofHours(9), "PT9H"),
            Arguments.of(Duration.ofHours(10), "PT10H"), Arguments.of(Duration.ofHours(11), "PT11H"),
            Arguments.of(Duration.ofHours(23), "PT23H"), Arguments.of(Duration.ofHours(24), "P1D"),
            Arguments.of(Duration.ofHours(25), "P1DT1H"), Arguments.of(Duration.ofDays(1), "P1D"),
            Arguments.of(Duration.ofDays(9), "P9D"), Arguments.of(Duration.ofDays(10), "P10D"),
            Arguments.of(Duration.ofDays(11), "P11D"), Arguments.of(Duration.ofDays(99), "P99D"),
            Arguments.of(Duration.ofDays(100), "P100D"), Arguments.of(Duration.ofDays(101), "P101D"));
    }

    @ParameterizedTest
    @MethodSource("negativeToStringTestSupplier")
    @Execution(ExecutionMode.SAME_THREAD)
    public void negativeToStringTest(Duration duration, String expected) {
        assertEquals(expected, DurationSerializer.toString(duration));
    }

    private static Stream<Arguments> negativeToStringTestSupplier() {
        return Stream.of(Arguments.of(Duration.ofMillis(-1), "-PT0.001S"),
            Arguments.of(Duration.ofMillis(-9), "-PT0.009S"), Arguments.of(Duration.ofMillis(-10), "-PT0.01S"),
            Arguments.of(Duration.ofMillis(-11), "-PT0.011S"), Arguments.of(Duration.ofMillis(-99), "-PT0.099S"),
            Arguments.of(Duration.ofMillis(-100), "-PT0.1S"), Arguments.of(Duration.ofMillis(-101), "-PT0.101S"),
            Arguments.of(Duration.ofMillis(-999), "-PT0.999S"), Arguments.of(Duration.ofMillis(-1000), "-PT1S"),
            Arguments.of(Duration.ofSeconds(-1), "-PT1S"), Arguments.of(Duration.ofSeconds(-9), "-PT9S"),
            Arguments.of(Duration.ofSeconds(-10), "-PT10S"), Arguments.of(Duration.ofSeconds(-11), "-PT11S"),
            Arguments.of(Duration.ofSeconds(-59), "-PT59S"), Arguments.of(Duration.ofSeconds(-60), "-PT1M"),
            Arguments.of(Duration.ofSeconds(-61), "-PT1M1S"), Arguments.of(Duration.ofMinutes(-1), "-PT1M"),
            Arguments.of(Duration.ofMinutes(-9), "-PT9M"), Arguments.of(Duration.ofMinutes(-10), "-PT10M"),
            Arguments.of(Duration.ofMinutes(-11), "-PT11M"), Arguments.of(Duration.ofMinutes(-59), "-PT59M"),
            Arguments.of(Duration.ofMinutes(-60), "-PT1H"), Arguments.of(Duration.ofMinutes(-61), "-PT1H1M"),
            Arguments.of(Duration.ofHours(-1), "-PT1H"), Arguments.of(Duration.ofHours(-9), "-PT9H"),
            Arguments.of(Duration.ofHours(-10), "-PT10H"), Arguments.of(Duration.ofHours(-11), "-PT11H"),
            Arguments.of(Duration.ofHours(-23), "-PT23H"), Arguments.of(Duration.ofHours(-24), "-P1D"),
            Arguments.of(Duration.ofHours(-25), "-P1DT1H"), Arguments.of(Duration.ofDays(-1), "-P1D"),
            Arguments.of(Duration.ofDays(-9), "-P9D"), Arguments.of(Duration.ofDays(-10), "-P10D"),
            Arguments.of(Duration.ofDays(-11), "-P11D"), Arguments.of(Duration.ofDays(-99), "-P99D"),
            Arguments.of(Duration.ofDays(-100), "-P100D"), Arguments.of(Duration.ofDays(-101), "-P101D"));
    }
}
