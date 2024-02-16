// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link HttpClientOptions}.
 */
public class HttpClientOptionsTests {
    @ParameterizedTest
    @MethodSource("timeoutSupplier")
    public void nullTimeoutDefaultsTo60Seconds(BiFunction<HttpClientOptions, Duration, HttpClientOptions> timeoutSetter,
        Function<HttpClientOptions, Duration> timeoutGetter) {
        HttpClientOptions httpClientOptions = timeoutSetter.apply(new HttpClientOptions(), null);

        assertEquals(Duration.ofSeconds(60), timeoutGetter.apply(httpClientOptions));
    }

    @ParameterizedTest
    @MethodSource("timeoutSupplier")
    public void negativeTimeoutDefaultsToInfiniteTimeout(
        BiFunction<HttpClientOptions, Duration, HttpClientOptions> timeoutSetter,
        Function<HttpClientOptions, Duration> timeoutGetter) {
        HttpClientOptions httpClientOptions = timeoutSetter.apply(new HttpClientOptions(), Duration.ofSeconds(-1));

        assertEquals(Duration.ZERO, timeoutGetter.apply(httpClientOptions));
    }

    public void zeroTimeoutDefaultsToInfiniteTimeout(
        BiFunction<HttpClientOptions, Duration, HttpClientOptions> timeoutSetter,
        Function<HttpClientOptions, Duration> timeoutGetter) {
        HttpClientOptions httpClientOptions = timeoutSetter.apply(new HttpClientOptions(), Duration.ZERO);

        assertEquals(Duration.ZERO, timeoutGetter.apply(httpClientOptions));
    }

    @ParameterizedTest
    @MethodSource("timeoutSupplier")
    public void smallTimeoutDefaultsToOneMillisecond(
        BiFunction<HttpClientOptions, Duration, HttpClientOptions> timeoutSetter,
        Function<HttpClientOptions, Duration> timeoutGetter) {
        HttpClientOptions httpClientOptions = timeoutSetter.apply(new HttpClientOptions(), Duration.ofNanos(1));

        assertEquals(Duration.ofMillis(1), timeoutGetter.apply(httpClientOptions));
    }

    @ParameterizedTest
    @MethodSource("timeoutSupplier")
    public void timeoutReturnsAsIs(BiFunction<HttpClientOptions, Duration, HttpClientOptions> timeoutSetter,
        Function<HttpClientOptions, Duration> timeoutGetter) {
        HttpClientOptions httpClientOptions = timeoutSetter.apply(new HttpClientOptions(), Duration.ofMinutes(5));

        assertEquals(Duration.ofMinutes(5), timeoutGetter.apply(httpClientOptions));
    }

    private static Stream<Arguments> timeoutSupplier() {
        BiFunction<HttpClientOptions, Duration, HttpClientOptions> setWriteTimeout = HttpClientOptions::setWriteTimeout;
        Function<HttpClientOptions, Duration> getWriteTimeout = HttpClientOptions::getWriteTimeout;

        BiFunction<HttpClientOptions, Duration, HttpClientOptions> responseTimeout = HttpClientOptions::responseTimeout;
        BiFunction<HttpClientOptions, Duration, HttpClientOptions> setResponseTimeout
            = HttpClientOptions::setResponseTimeout;
        Function<HttpClientOptions, Duration> getResponseTimeout = HttpClientOptions::getResponseTimeout;

        BiFunction<HttpClientOptions, Duration, HttpClientOptions> readTimeout = HttpClientOptions::readTimeout;
        BiFunction<HttpClientOptions, Duration, HttpClientOptions> setReadTimeout = HttpClientOptions::setReadTimeout;
        Function<HttpClientOptions, Duration> getReadTimeout = HttpClientOptions::getReadTimeout;

        BiFunction<HttpClientOptions, Duration, HttpClientOptions> setConnectionIdleTimeout
            = HttpClientOptions::setConnectionIdleTimeout;
        Function<HttpClientOptions, Duration> getConnectionIdleTimeout = HttpClientOptions::getConnectionIdleTimeout;

        return Stream.of(Arguments.of(setWriteTimeout, getWriteTimeout),

            Arguments.of(responseTimeout, getResponseTimeout), Arguments.of(setResponseTimeout, getResponseTimeout),

            Arguments.of(readTimeout, getReadTimeout), Arguments.of(setReadTimeout, getReadTimeout),

            Arguments.of(setConnectionIdleTimeout, getConnectionIdleTimeout));
    }

    @Test
    public void nullMaximumConnectionPoolSizeRemainsNull() {
        HttpClientOptions httpClientOptions = new HttpClientOptions().setMaximumConnectionPoolSize(null);
        assertNull(httpClientOptions.getMaximumConnectionPoolSize());
    }

    @Test
    public void maximumConnectionPoolSizeReturnsAsIs() {
        HttpClientOptions httpClientOptions = new HttpClientOptions().setMaximumConnectionPoolSize(50);
        assertEquals(50, httpClientOptions.getMaximumConnectionPoolSize());
    }

    @Test
    public void zeroOrNegativeMaximumConnectionPoolSizeThrows() {
        assertThrows(IllegalArgumentException.class, () -> new HttpClientOptions().setMaximumConnectionPoolSize(0));
        assertThrows(IllegalArgumentException.class, () -> new HttpClientOptions().setMaximumConnectionPoolSize(-1));
    }
}
