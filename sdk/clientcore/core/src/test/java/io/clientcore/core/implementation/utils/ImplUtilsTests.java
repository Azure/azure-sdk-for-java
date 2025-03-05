// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.implementation.utils;

import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.shared.TestConfigurationSource;
import io.clientcore.core.utils.CoreUtilsTests;
import io.clientcore.core.utils.configuration.Configuration;
import io.clientcore.core.utils.configuration.ConfigurationBuilder;
import io.clientcore.core.utils.configuration.ConfigurationSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Unit tests for {@link ImplUtils}.
 */
public class ImplUtilsTests {
    private static final byte[] BYTES = "Hello world!".getBytes(StandardCharsets.UTF_8);

    private static final byte[] UTF_8_BOM = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
    private static final byte[] UTF_16BE_BOM = { (byte) 0xFE, (byte) 0xFF };
    private static final byte[] UTF_16LE_BOM = { (byte) 0xFF, (byte) 0xFE };
    private static final byte[] UTF_32BE_BOM = { (byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF };
    private static final byte[] UTF_32LE_BOM = { (byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00 };

    private static final String TIMEOUT_PROPERTY_NAME = "TIMEOUT_PROPERTY_NAME";
    private static final ConfigurationSource EMPTY_SOURCE = new TestConfigurationSource();

    @ParameterizedTest
    @MethodSource("bomAwareToStringSupplier")
    public void bomAwareToString(byte[] bytes, String contentType, String expected) {
        assertEquals(expected, ImplUtils.bomAwareToString(bytes, 0, bytes.length, contentType));
    }

    private static Stream<Arguments> bomAwareToStringSupplier() {
        return Stream.of(Arguments.arguments(BYTES, null, new String(BYTES, StandardCharsets.UTF_8)),
            Arguments.arguments(BYTES, "charset=UTF-16BE", new String(BYTES, StandardCharsets.UTF_16BE)),
            Arguments.arguments(BYTES, "charset=invalid", new String(BYTES, StandardCharsets.UTF_8)),
            Arguments.arguments(addBom(UTF_8_BOM), null, new String(BYTES, StandardCharsets.UTF_8)),
            Arguments.arguments(addBom(UTF_16BE_BOM), null, new String(BYTES, StandardCharsets.UTF_16BE)),
            Arguments.arguments(addBom(UTF_16LE_BOM), null, new String(BYTES, StandardCharsets.UTF_16LE)),
            Arguments.arguments(addBom(UTF_32BE_BOM), null, new String(BYTES, Charset.forName("UTF-32BE"))),
            Arguments.arguments(addBom(UTF_32LE_BOM), null, new String(BYTES, Charset.forName("UTF-32LE"))),
            Arguments.arguments(addBom(UTF_8_BOM), "charset=UTF-8", new String(BYTES, StandardCharsets.UTF_8)),
            Arguments.arguments(addBom(UTF_8_BOM), "charset=UTF-16BE", new String(BYTES, StandardCharsets.UTF_8)));
    }

    private static byte[] addBom(byte[] arr1) {
        byte[] mergedArray = new byte[arr1.length + BYTES.length];

        System.arraycopy(arr1, 0, mergedArray, 0, arr1.length);
        System.arraycopy(BYTES, 0, mergedArray, arr1.length, BYTES.length);

        return mergedArray;
    }

    @ParameterizedTest
    @MethodSource("getDefaultTimeoutFromEnvironmentSupplier")
    public void getDefaultTimeoutFromEnvironmentTests(Configuration configuration, Duration defaultTimeout,
        ClientLogger logger, Duration expectedTimeout) {
        assertEquals(expectedTimeout,
            ImplUtils.getDefaultTimeoutFromEnvironment(configuration, TIMEOUT_PROPERTY_NAME, defaultTimeout, logger));
    }

    private static Stream<Arguments> getDefaultTimeoutFromEnvironmentSupplier() {
        ClientLogger logger = new ClientLogger(CoreUtilsTests.class);

        return Stream.of(
            // Configuration has an empty string timeout property configured.
            Arguments.of(
                new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE,
                    new TestConfigurationSource().put(TIMEOUT_PROPERTY_NAME, "")).build(),
                Duration.ofMillis(10000), logger, Duration.ofMillis(10000)),

            // Configuration has a value that isn't a valid number.
            Arguments.of(
                new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE,
                    new TestConfigurationSource().put(TIMEOUT_PROPERTY_NAME, "ten")).build(),
                Duration.ofMillis(10000), logger, Duration.ofMillis(10000)),

            // Configuration has a negative value.
            Arguments.of(
                new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE,
                    new TestConfigurationSource().put(TIMEOUT_PROPERTY_NAME, "-10")).build(),
                Duration.ofMillis(10000), logger, Duration.ZERO),

            // Configuration has a zero value.
            Arguments.of(
                new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE,
                    new TestConfigurationSource().put(TIMEOUT_PROPERTY_NAME, "0")).build(),
                Duration.ofMillis(10000), logger, Duration.ZERO),

            // Configuration has a positive value.
            Arguments.of(
                new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE,
                    new TestConfigurationSource().put(TIMEOUT_PROPERTY_NAME, "42")).build(),
                Duration.ofMillis(10000), logger, Duration.ofMillis(42)));
    }

    @Test
    public void parseNullQueryParameters() {
        assertFalse(new ImplUtils.QueryParameterIterator(null).hasNext());
    }

    @Test
    public void parseEmptyQueryParameters() {
        assertFalse(new ImplUtils.QueryParameterIterator("").hasNext());
    }

    @ParameterizedTest
    @ValueSource(strings = { "key=value", "?key=value" })
    public void parseSimpleQueryParameter(String queryParameters) {
        Iterator<Map.Entry<String, String>> iterator = new ImplUtils.QueryParameterIterator(queryParameters);

        assertEquals(new AbstractMap.SimpleImmutableEntry<>("key", "value"), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @ParameterizedTest
    @ValueSource(strings = { "key=", "?key=" })
    public void parseSimpleEmptyValueQueryParameter(String queryParameters) {
        Iterator<Map.Entry<String, String>> iterator = new ImplUtils.QueryParameterIterator(queryParameters);

        assertEquals(new AbstractMap.SimpleImmutableEntry<>("key", ""), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @ParameterizedTest
    @ValueSource(strings = { "key", "?key" })
    public void parseSimpleKeyOnlyQueryParameter(String queryParameters) {
        Iterator<Map.Entry<String, String>> iterator = new ImplUtils.QueryParameterIterator(queryParameters);

        assertEquals(new AbstractMap.SimpleImmutableEntry<>("key", ""), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @ParameterizedTest
    @ValueSource(strings = { "key=value&key2=", "key=value&key2", "?key=value&key2=", "?key=value&key2" })
    public void parseQueryParameterLastParameterEmpty(String queryParameters) {
        Iterator<Map.Entry<String, String>> iterator = new ImplUtils.QueryParameterIterator(queryParameters);

        assertEquals(new AbstractMap.SimpleImmutableEntry<>("key", "value"), iterator.next());
        assertEquals(new AbstractMap.SimpleImmutableEntry<>("key2", ""), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @ParameterizedTest
    @ValueSource(strings = { "key=&key2=value2", "key&key2=value2", "?key=&key2=value2", "?key&key2=value2" })
    public void parseQueryParameterFirstParameterEmpty(String queryParameters) {
        Iterator<Map.Entry<String, String>> iterator = new ImplUtils.QueryParameterIterator(queryParameters);

        assertEquals(new AbstractMap.SimpleImmutableEntry<>("key", ""), iterator.next());
        assertEquals(new AbstractMap.SimpleImmutableEntry<>("key2", "value2"), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "key=value&key2=&key3=value3",
            "?key=value&key2=&key3=value3",
            "key=value&key2&key3=value3",
            "?key=value&key2&key3=value3", })
    public void parseQueryParameterMiddleParameterEmpty(String queryParameters) {
        Iterator<Map.Entry<String, String>> iterator = new ImplUtils.QueryParameterIterator(queryParameters);

        assertEquals(new AbstractMap.SimpleImmutableEntry<>("key", "value"), iterator.next());
        assertEquals(new AbstractMap.SimpleImmutableEntry<>("key2", ""), iterator.next());
        assertEquals(new AbstractMap.SimpleImmutableEntry<>("key3", "value3"), iterator.next());
        assertFalse(iterator.hasNext());
    }
}
