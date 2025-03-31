// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.implementation.utils;

import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.shared.TestConfigurationSource;
import io.clientcore.core.utils.CoreUtilsTests;
import io.clientcore.core.utils.configuration.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import static io.clientcore.core.utils.TestUtils.assertArraysEqual;
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

    @ParameterizedTest
    @MethodSource("bomAwareToStringSupplier")
    public void bomAwareToString(byte[] bytes, String contentType, String expected) {
        assertEquals(expected, ImplUtils.bomAwareToString(bytes, 0, bytes.length, contentType));
    }

    private static Stream<Arguments> bomAwareToStringSupplier() {
        return Stream.of(
            // no content type returns UTF-8
            Arguments.of(BYTES, null, new String(BYTES, StandardCharsets.UTF_8)),

            // no content type returns UTF-8
            Arguments.of(BYTES, "application/text", new String(BYTES, StandardCharsets.UTF_8)),

            // charset=UTF-16E returns UTF-16E
            Arguments.of(BYTES, "charset=UTF-16BE", new String(BYTES, StandardCharsets.UTF_16BE)),

            // invalid charset returns UTF-8
            Arguments.of(BYTES, "charset=invalid", new String(BYTES, StandardCharsets.UTF_8)),

            // UTF-8 BOM returns UTF-8
            Arguments.of(addBom(UTF_8_BOM), null, new String(BYTES, StandardCharsets.UTF_8)),

            // UTF-16BE BOM returns UTF-16BE
            Arguments.of(addBom(UTF_16BE_BOM), null, new String(BYTES, StandardCharsets.UTF_16BE)),

            // UTF-16LE BOM returns UTF-16LE
            Arguments.of(addBom(UTF_16LE_BOM), null, new String(BYTES, StandardCharsets.UTF_16LE)),

            // UTF-32BE BOM returns UTF-32BE
            Arguments.of(addBom(UTF_32BE_BOM), null, new String(BYTES, Charset.forName("UTF-32BE"))),

            // UTF-32LE BOM returns UTF-32LE
            Arguments.of(addBom(UTF_32LE_BOM), null, new String(BYTES, Charset.forName("UTF-32LE"))),

            // BOM preferred over charset
            Arguments.of(addBom(UTF_8_BOM), "charset=UTF-8", new String(BYTES, StandardCharsets.UTF_8)),
            Arguments.of(addBom(UTF_8_BOM), "charset=UTF-16BE", new String(BYTES, StandardCharsets.UTF_8)));
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
            Arguments.of(Configuration.from(new TestConfigurationSource().put(TIMEOUT_PROPERTY_NAME, "")),
                Duration.ofMillis(10000), logger, Duration.ofMillis(10000)),

            // Configuration has a value that isn't a valid number.
            Arguments.of(Configuration.from(new TestConfigurationSource().put(TIMEOUT_PROPERTY_NAME, "ten")),
                Duration.ofMillis(10000), logger, Duration.ofMillis(10000)),

            // Configuration has a negative value.
            Arguments.of(Configuration.from(new TestConfigurationSource().put(TIMEOUT_PROPERTY_NAME, "-10")),
                Duration.ofMillis(10000), logger, Duration.ZERO),

            // Configuration has a zero value.
            Arguments.of(Configuration.from(new TestConfigurationSource().put(TIMEOUT_PROPERTY_NAME, "0")),
                Duration.ofMillis(10000), logger, Duration.ZERO),

            // Configuration has a positive value.
            Arguments.of(Configuration.from(new TestConfigurationSource().put(TIMEOUT_PROPERTY_NAME, "42")),
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

    @ParameterizedTest
    @MethodSource("byteBufferToArraySupplier")
    public void byteBufferToArray(ByteBuffer buffer, byte[] expected) {
        assertArraysEqual(expected, ImplUtils.byteBufferToArray(buffer));
    }

    private static Stream<Arguments> byteBufferToArraySupplier() {
        return Stream.of(
            // empty buffer returns empty array
            Arguments.of(ByteBuffer.allocate(0), new byte[0]),

            // non-empty buffer returns byte array
            Arguments.of(ByteBuffer.wrap(BYTES), BYTES),

            // direct buffer
            Arguments.of(ByteBuffer.allocateDirect(BYTES.length).put(BYTES).position(0), BYTES));
    }
}
