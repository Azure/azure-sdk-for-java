// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;

public class HttpHeadersTests {
    private static final HttpHeaderName A = HttpHeaderName.fromString("a");

    @ParameterizedTest
    @MethodSource("testAddSupplier")
    public void testAdd(HttpHeaders initialHeaders, HttpHeaderName keyToSet, String valueToSet,
        List<String> expectedValues) {
        initialHeaders.add(keyToSet, valueToSet);

        assertLinesMatch(expectedValues, initialHeaders.getValues(keyToSet));
    }

    private static Stream<Arguments> testAddSupplier() {
        return Stream.of(
            // Empty HttpHeaders will add the value.
            Arguments.of(new HttpHeaders(), A, "b", Collections.singletonList("b")),

            // Non-empty HttpHeaders will add to the previous value.
            Arguments.of(new HttpHeaders().set(A, "b"), A, "c", Arrays.asList("b", "c")),

            // Non-empty HttpHeaders will do nothing when previously set and the value is null.
            Arguments.of(new HttpHeaders().set(A, "b"), A, null, Collections.singletonList("b")),

            // HttpHeaders is case-insensitive.
            Arguments.of(new HttpHeaders().set(A, "b"), A, "c", Arrays.asList("b", "c")));
    }

    @ParameterizedTest
    @MethodSource("testSetSupplier")
    public void testSet(HttpHeaders initialHeaders, HttpHeaderName keyToSet, String valueToSet, String expectedValue) {
        initialHeaders.set(keyToSet, valueToSet);

        assertEquals(expectedValue, initialHeaders.getValue(keyToSet));
    }

    private static Stream<Arguments> testSetSupplier() {
        return Stream.of(
            // Empty HttpHeaders will set the value.
            Arguments.of(new HttpHeaders(), A, "b", "b"),

            // Non-empty HttpHeaders will override the previously set value.
            Arguments.of(new HttpHeaders().set(A, "b"), A, "c", "c"),

            // Non-empty HttpHeaders will remove the previously set value when null.
            Arguments.of(new HttpHeaders().set(A, "b"), A, null, null),

            // HttpHeaders is case-insensitive.
            Arguments.of(new HttpHeaders().set(A, "b"), A, "c", "c"));
    }

    @Test
    public void testToStringShouldBeRepresentingKeyEqualSignValue() {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaderName.fromString("key1"), "value1");
        headers.set(HttpHeaderName.fromString("key2"), "value2");
        headers.set(HttpHeaderName.fromString("key3"), "value3");

        assertEquals("key1:value1, key2:value2, key3:value3", headers.toString());
    }
}
