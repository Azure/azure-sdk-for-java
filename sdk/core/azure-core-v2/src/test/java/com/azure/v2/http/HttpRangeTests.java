// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.http;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link HttpRange}.
 */
public class HttpRangeTests {
    @ParameterizedTest
    @MethodSource("equalsTestSupplier")
    public void equalsTest(long offset, Long length, HttpRange expected) {
        assertEquals(expected.getOffset(), offset);
        assertEquals(expected.getLength(), length);
        assertEquals(expected, new HttpRange(offset, length));
    }

    private static Stream<Arguments> equalsTestSupplier() {
        return Stream.of(Arguments.of(0, null, new HttpRange(0)), Arguments.of(0, null, new HttpRange(0, null)),
            Arguments.of(0, 10L, new HttpRange(0, 10L)));
    }

    @ParameterizedTest
    @MethodSource("toStringTestSupplier")
    public void toStringTest(HttpRange range, String expected) {
        assertEquals(expected, range.toString());
    }

    private static Stream<Arguments> toStringTestSupplier() {
        return Stream.of(Arguments.of(new HttpRange(0), "bytes=0-"), Arguments.of(new HttpRange(0, null), "bytes=0-"),
            Arguments.of(new HttpRange(0, 10L), "bytes=0-9"), Arguments.of(new HttpRange(10, 10L), "bytes=10-19"));
    }

    @ParameterizedTest
    @MethodSource("invalidRangeSupplier")
    public void invalidRange(long offset, Long length) {
        assertThrows(IllegalArgumentException.class, () -> new HttpRange(offset, length));
    }

    private static Stream<Arguments> invalidRangeSupplier() {
        return Stream.of(Arguments.of(-1, null), Arguments.of(0, -1L));
    }
}
