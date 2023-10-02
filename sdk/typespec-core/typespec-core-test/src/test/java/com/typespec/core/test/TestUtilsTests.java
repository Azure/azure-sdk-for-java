// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test;

import com.typespec.core.test.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.AssertionFailedError;

import java.nio.ByteBuffer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link TestUtils}.
 */
public class TestUtilsTests {
    @ParameterizedTest
    @MethodSource("arraysAreEqualSupplier")
    public void arraysAreEqual(byte[] array1, byte[] array2) {
        assertDoesNotThrow(() -> TestUtils.assertArraysEqual(array1, array2));
    }

    private static Stream<Arguments> arraysAreEqualSupplier() {
        byte[] sameInstance = new byte[] { 1, 2, 3 };
        return Stream.of(
            Arguments.of(null, null),
            Arguments.of(new byte[0], new byte[0]),
            Arguments.of(sameInstance, sameInstance),
            Arguments.of(new byte[] { 1, 2, 3 }, new byte[] { 1, 2, 3 })
        );
    }

    @Test
    public void arraysAreNotEqual() {
        assertThrows(AssertionFailedError.class, () -> TestUtils.assertArraysEqual(new byte[0], new byte[] { 1, 2 }));
    }

    @ParameterizedTest
    @MethodSource("byteBuffersAreEqualSupplier")
    public void byteBuffersAreEqual(ByteBuffer byteBuffer1, ByteBuffer byteBuffer2) {
        assertDoesNotThrow(() -> TestUtils.assertByteBuffersEqual(byteBuffer1, byteBuffer2));
    }

    private static Stream<Arguments> byteBuffersAreEqualSupplier() {
        ByteBuffer sameInstance = ByteBuffer.wrap(new byte[] { 1, 2, 3 });
        return Stream.of(
            Arguments.of(null, null),
            Arguments.of(ByteBuffer.allocate(0), ByteBuffer.allocate(0)),
            Arguments.of(sameInstance, sameInstance),
            Arguments.of(ByteBuffer.wrap(new byte[] { 1, 2, 3 }), ByteBuffer.wrap(new byte[] { 1, 2, 3 }))
        );
    }

    @Test
    public void byteBuffersAreNotEqual() {
        assertThrows(AssertionFailedError.class, () -> TestUtils.assertByteBuffersEqual(
            ByteBuffer.wrap(new byte[] { 1 }), ByteBuffer.wrap(new byte[] { 1, 2 })));
    }
}
