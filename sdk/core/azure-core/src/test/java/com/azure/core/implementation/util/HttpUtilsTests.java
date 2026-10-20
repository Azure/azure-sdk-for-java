// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpUtilsTests {
    private static final String TIMEOUT_PROPERTY = "HttpUtilsTests.timeout";

    @ParameterizedTest
    @MethodSource("timeoutPrecedenceSupplier")
    @ResourceLock(TIMEOUT_PROPERTY)
    @SuppressWarnings("deprecation")
    public void timeoutPrecedence(Duration explicit, String local, String global, long expectedMillis) {
        Configuration globalConfiguration = Configuration.getGlobalConfiguration();
        String previous = globalConfiguration.get(TIMEOUT_PROPERTY);
        try {
            globalConfiguration.put(TIMEOUT_PROPERTY, global == null ? "" : global);
            Configuration configuration = new ConfigurationBuilder(path -> Collections.emptyMap(),
                path -> local == null ? Collections.emptyMap() : Collections.singletonMap(TIMEOUT_PROPERTY, local),
                path -> Collections.emptyMap()).build();
            Configuration explicitConfiguration = new ConfigurationBuilder(
                path -> local == null ? Collections.emptyMap() : Collections.singletonMap(TIMEOUT_PROPERTY, local),
                path -> Collections.emptyMap(), path -> Collections.emptyMap()).build();

            assertEquals(Duration.ofMillis(expectedMillis),
                HttpUtils.getTimeout(explicit, configuration, TIMEOUT_PROPERTY, Duration.ofSeconds(10)));
            assertEquals(Duration.ofMillis(expectedMillis),
                HttpUtils.getTimeout(explicit, explicitConfiguration, TIMEOUT_PROPERTY, Duration.ofSeconds(10)));
        } finally {
            globalConfiguration.remove(TIMEOUT_PROPERTY);
            if (previous != null) {
                globalConfiguration.put(TIMEOUT_PROPERTY, previous);
            }
        }
    }

    private static Stream<Arguments> timeoutPrecedenceSupplier() {
        return Stream.of(Arguments.of(Duration.ofSeconds(3), "2000", "1000", 3000),
            Arguments.of(Duration.ZERO, "2000", "1000", 0), Arguments.of(Duration.ofNanos(-1), "2000", "1000", 0),
            Arguments.of(Duration.ofNanos(1), "2000", "1000", 1), Arguments.of(null, "2000", "1000", 2000),
            Arguments.of(null, "0", "1000", 0), Arguments.of(null, "-1", "1000", 0),
            Arguments.of(null, null, "1000", 1000), Arguments.of(null, "", "1000", 1000),
            Arguments.of(null, "invalid", "1000", 1000), Arguments.of(null, null, "0", 0),
            Arguments.of(null, null, "-1", 0), Arguments.of(null, null, "invalid", 10000),
            Arguments.of(null, null, null, 10000));
    }

    @Test
    @ResourceLock(TIMEOUT_PROPERTY)
    @SuppressWarnings("deprecation")
    public void globalTimeoutIsResolvedOnEachCall() {
        Configuration global = Configuration.getGlobalConfiguration();
        String previous = global.get(TIMEOUT_PROPERTY);
        try {
            for (String value : new String[] { "1000", "2000" }) {
                global.put(TIMEOUT_PROPERTY, value);
                for (Configuration configuration : new Configuration[] { null, global, Configuration.NONE }) {
                    assertEquals(Duration.ofMillis(Long.parseLong(value)),
                        HttpUtils.getTimeout(null, configuration, TIMEOUT_PROPERTY, Duration.ofSeconds(10)));
                }
            }
        } finally {
            global.remove(TIMEOUT_PROPERTY);
            if (previous != null) {
                global.put(TIMEOUT_PROPERTY, previous);
            }
        }
    }

    @Test
    public void textEventStreamContentTypeRequiresSingleMediaType() {
        assertTrue(HttpUtils.isTextEventStreamContentType("Text/Event-Stream; charset=utf-8"));
        assertTrue(HttpUtils.isTextEventStreamContentType("text/event-stream; charset=\"UTF-8\""));
        assertTrue(HttpUtils.isTextEventStreamContentType("text/event-stream; charset=iso-8859-1"));
        assertTrue(HttpUtils.isTextEventStreamContentType("text/event-stream; charset=utf-16"));
        assertTrue(HttpUtils.isTextEventStreamContentType("text/event-stream; charset=not-a-charset; charset=utf-16"));
        assertFalse(HttpUtils.isTextEventStreamContentType("application/json, text/event-stream"));
        assertTrue(HttpUtils.isTextEventStreamContentType("text/event-stream; note=\"x,y;z\""));
        assertFalse(HttpUtils.isTextEventStreamContentType("text/event-stream; note=\"unterminated"));
    }
}
