// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpHeadersTests {
    @ParameterizedTest
    @MethodSource("testAddSupplier")
    public void testAdd(HttpHeaders initialHeaders, String keyToSet, String valueToSet, String[] expectedValues) {
        initialHeaders.add(keyToSet, valueToSet);

        assertArrayEquals(expectedValues, initialHeaders.getValues(keyToSet));
    }

    private static Stream<Arguments> testAddSupplier() {
        return Stream.of(
            // Empty HttpHeaders will add the value.
            Arguments.of(new HttpHeaders(), "a", "b", new String[] { "b" }),

            // Non-empty HttpHeaders will add to the previous value.
            Arguments.of(new HttpHeaders().set("a", "b"), "a", "c", new String[] { "b", "c" }),

            // Non-empty HttpHeaders will do nothing when previously set and the value is null.
            Arguments.of(new HttpHeaders().set("a", "b"), "a", null, new String[] { "b" }),

            // HttpHeaders is case-insensitive.
            Arguments.of(new HttpHeaders().set("a", "b"), "A", "c", new String[] { "b", "c" })
        );
    }

    @ParameterizedTest
    @MethodSource("testSetSupplier")
    public void testSet(HttpHeaders initialHeaders, String keyToSet, String valueToSet, String expectedValue) {
        initialHeaders.set(keyToSet, valueToSet);

        assertEquals(expectedValue, initialHeaders.getValue(keyToSet));
    }

    private static Stream<Arguments> testSetSupplier() {
        return Stream.of(
            // Empty HttpHeaders will set the value.
            Arguments.of(new HttpHeaders(), "a", "b", "b"),

            // Non-empty HttpHeaders will override the previously set value.
            Arguments.of(new HttpHeaders().set("a", "b"), "a", "c", "c"),

            // Non-empty HttpHeaders will remove the previously set value when null.
            Arguments.of(new HttpHeaders().set("a", "b"), "a", null, null),

            // HttpHeaders is case-insensitive.
            Arguments.of(new HttpHeaders().set("a", "b"), "A", "c", "c")
        );
    }

    @ParameterizedTest
    @MethodSource("testToMapSupplier")
    public void testToMap(HttpHeaders headers, Map<String, String> expectedMap) {
        Map<String, String> actualMap = headers.toMap();

        assertEquals(expectedMap.size(), actualMap.size());
        for (Map.Entry<String, String> entry : expectedMap.entrySet()) {
            assertTrue(actualMap.containsKey(entry.getKey()));
            assertEquals(entry.getValue(), actualMap.get(entry.getKey()));
        }
    }

    private static Stream<Arguments> testToMapSupplier() {
        return Stream.of(
            // Empty HttpHeaders will return an empty map.
            Arguments.of(new HttpHeaders(), Collections.emptyMap()),

            // Non-empty HttpHeaders will return a map containing header values as key-value pairs.
            Arguments.of(new HttpHeaders().set("a", "b"), Collections.singletonMap("a", "b")),

            // Non-empty HttpHeaders will return comma-delimited header values if multiple are set.
            Arguments.of(new HttpHeaders().set("a", "b").add("a", "c"), Collections.singletonMap("a", "b,c"))
        );
    }

    @ParameterizedTest
    @MethodSource("testToMultiMapSupplier")
    public void testToMultiMap(HttpHeaders headers, Map<String, String[]> expectedMultiMap) {
        Map<String, String[]> actualMultiMap = headers.toMultiMap();

        assertEquals(expectedMultiMap.size(), actualMultiMap.size());
        for (Map.Entry<String, String[]> entry : expectedMultiMap.entrySet()) {
            assertTrue(actualMultiMap.containsKey(entry.getKey()));
            assertArrayEquals(entry.getValue(), actualMultiMap.get(entry.getKey()));
        }
    }

    private static Stream<Arguments> testToMultiMapSupplier() {
        return Stream.of(
            // Empty HttpHeaders will return an empty map.
            Arguments.of(new HttpHeaders(), Collections.emptyMap()),

            // Non-empty HttpHeaders will return a map containing header values as key-value pairs.
            Arguments.of(new HttpHeaders().set("a", "b"), Collections.singletonMap("a", new String[] { "b" })),

            // Non-empty HttpHeaders will return comma-delimited header values if multiple are set.
            Arguments.of(new HttpHeaders().set("a", "b").add("a", "c"),
                Collections.singletonMap("a", new String[] { "b", "c" }))
        );
    }

    @Test
    public void testToStringShouldBeRepresentingKeyEqualsignValue() {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("key1", "value1");
        headers.set("key2", "value2");
        headers.set("key3", "value3");

        assertEquals("key1=value1, key2=value2, key3=value3", headers.toString());
    }
}
