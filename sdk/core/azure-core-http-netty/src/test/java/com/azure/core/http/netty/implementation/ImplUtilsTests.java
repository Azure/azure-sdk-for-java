// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link ImplUtils}.
 */
public class ImplUtilsTests {
    @ParameterizedTest
    @MethodSource("getTimeoutMillisSupplier")
    public void getTimeoutMillis(Duration timeout, long expected) {
        assertEquals(expected, ImplUtils.getTimeoutMillis(timeout));
    }

    private static Stream<Arguments> getTimeoutMillisSupplier() {
        return Stream.of(
            Arguments.of(null, TimeUnit.SECONDS.toMillis(60)),
            Arguments.of(Duration.ofSeconds(0), 0),
            Arguments.of(Duration.ofSeconds(-1), 0),
            Arguments.of(Duration.ofSeconds(120), TimeUnit.SECONDS.toMillis(120)),
            Arguments.of(Duration.ofNanos(1), TimeUnit.MILLISECONDS.toMillis(1))
        );
    }
}
