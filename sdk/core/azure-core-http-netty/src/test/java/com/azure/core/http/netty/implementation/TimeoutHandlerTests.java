// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link TimeoutHandler}.
 */
public class TimeoutHandlerTests {
    @ParameterizedTest
    @MethodSource("instantiationSupplier")
    public void instantiation(Duration timeout, long expectedTimeoutMillis) throws ReflectiveOperationException {
        TimeoutHandler timeoutHandler = new TimeoutHandler(timeout, timeout, timeout);

        assertEquals(expectedTimeoutMillis, getTimeoutValue("writeTimeoutMillis", timeoutHandler));
        assertEquals(expectedTimeoutMillis, getTimeoutValue("responseTimeoutMillis", timeoutHandler));
        assertEquals(expectedTimeoutMillis, getTimeoutValue("readTimeoutMillis", timeoutHandler));
    }

    private static Stream<Arguments> instantiationSupplier() {
        return Stream.of(
            Arguments.of(null, TimeUnit.SECONDS.toMillis(60)),
            Arguments.of(Duration.ZERO, 0),
            Arguments.of(Duration.ofMillis(-10), 0),
            Arguments.of(Duration.ofSeconds(120), TimeUnit.SECONDS.toMillis(120)),
            Arguments.of(Duration.ofNanos(1), TimeUnit.MILLISECONDS.toMillis(1))
        );
    }

    private static long getTimeoutValue(String fieldName, TimeoutHandler handler) throws ReflectiveOperationException {
        Field field = TimeoutHandler.class.getDeclaredField(fieldName);

        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            field.setAccessible(true);
            return null;
        });

        return field.getLong(handler);
    }
}
