// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.http.HttpProtocolVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link HttpClientOptions}.
 */
public class HttpClientOptionsTests {
    @Test
    public void defaultMaximumHttpVersionRemainsNull() {
        assertNull(new HttpClientOptions().getMaximumHttpVersion());
    }

    @ParameterizedTest
    @EnumSource(HttpProtocolVersion.class)
    public void maximumHttpVersionReturnsAsIs(HttpProtocolVersion version) {
        HttpClientOptions options = new HttpClientOptions();
        assertSame(options, options.setMaximumHttpVersion(version));
        assertEquals(version, options.getMaximumHttpVersion());
    }

    @ParameterizedTest
    @EnumSource(HttpProtocolVersion.class)
    public void clearingMaximumHttpVersionRestoresDefault(HttpProtocolVersion version) {
        HttpClientOptions options = new HttpClientOptions().setMaximumHttpVersion(version);
        assertSame(options, options.setMaximumHttpVersion(null));
        assertNull(options.getMaximumHttpVersion());
    }

    @Test
    public void nullMaximumHttpVersionRemainsNull() {
        HttpClientOptions options = new HttpClientOptions();
        assertSame(options, options.setMaximumHttpVersion(null));
        assertNull(options.getMaximumHttpVersion());
    }

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

    @ParameterizedTest
    @MethodSource("timeoutSupplier")
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

    @ParameterizedTest
    @MethodSource("configuredTimeoutSupplier")
    public void configurationTimeoutsAreUsedUnlessExplicitlyOverridden(String property,
        BiFunction<HttpClientOptions, Duration, HttpClientOptions> setter,
        Function<HttpClientOptions, Duration> getter) {
        Configuration configuration = new ConfigurationBuilder(path -> Collections.emptyMap(),
            path -> Collections.singletonMap(property, "123000"), path -> Collections.emptyMap()).build();
        HttpClientOptions options = new HttpClientOptions().setConfiguration(configuration);
        assertEquals(Duration.ofMillis(123000), getter.apply(options));

        Duration[] explicitTimeouts
            = { Duration.ZERO, Duration.ofNanos(-1), Duration.ofNanos(1), Duration.ofSeconds(3) };
        long[] expectedMillis = { 0, 0, 1, 3000 };
        for (int i = 0; i < explicitTimeouts.length; i++) {
            setter.apply(options, explicitTimeouts[i]);
            assertEquals(Duration.ofMillis(expectedMillis[i]), getter.apply(options));
        }

        setter.apply(options, null);
        assertEquals(Duration.ofMillis(123000), getter.apply(options));
    }

    @Test
    public void explicitConfigurationPropertiesAreUsed() {
        Configuration configuration = new ConfigurationBuilder(path -> Collections.emptyMap(),
            path -> Collections.emptyMap(), path -> Collections.emptyMap())
                .putProperty(Configuration.PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT, "123000")
                .putProperty(Configuration.PROPERTY_AZURE_REQUEST_READ_TIMEOUT, "234000")
                .putProperty(Configuration.PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT, "345000")
                .putProperty(Configuration.PROPERTY_AZURE_REQUEST_RESPONSE_TIMEOUT, "456000")
                .build();
        HttpClientOptions options = new HttpClientOptions().setConfiguration(configuration);
        assertEquals(Duration.ofMillis(123000), options.getConnectTimeout());
        assertEquals(Duration.ofMillis(234000), options.getReadTimeout());
        assertEquals(Duration.ofMillis(345000), options.getWriteTimeout());
        assertEquals(Duration.ofMillis(456000), options.getResponseTimeout());
    }

    private static Stream<Arguments> configuredTimeoutSupplier() {
        BiFunction<HttpClientOptions, Duration, HttpClientOptions> connect = HttpClientOptions::setConnectTimeout;
        BiFunction<HttpClientOptions, Duration, HttpClientOptions> write = HttpClientOptions::setWriteTimeout;
        BiFunction<HttpClientOptions, Duration, HttpClientOptions> response = HttpClientOptions::setResponseTimeout;
        BiFunction<HttpClientOptions, Duration, HttpClientOptions> read = HttpClientOptions::setReadTimeout;
        Function<HttpClientOptions, Duration> getConnect = HttpClientOptions::getConnectTimeout;
        Function<HttpClientOptions, Duration> getWrite = HttpClientOptions::getWriteTimeout;
        Function<HttpClientOptions, Duration> getResponse = HttpClientOptions::getResponseTimeout;
        Function<HttpClientOptions, Duration> getRead = HttpClientOptions::getReadTimeout;
        return Stream.of(Arguments.of(Configuration.PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT, connect, getConnect),
            Arguments.of(Configuration.PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT, write, getWrite),
            Arguments.of(Configuration.PROPERTY_AZURE_REQUEST_RESPONSE_TIMEOUT, response, getResponse),
            Arguments.of(Configuration.PROPERTY_AZURE_REQUEST_READ_TIMEOUT, read, getRead));
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
