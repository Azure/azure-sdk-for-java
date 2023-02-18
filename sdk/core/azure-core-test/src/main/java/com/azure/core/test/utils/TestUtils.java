// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.utils;

import org.junit.jupiter.api.Assertions;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

/**
 * Contains utility methods used for testing.
 */
public final class TestUtils {
    /**
     * Asserts that two arrays are equal.
     * <p>
     * This method is similar to JUnit's {@link Assertions#assertArrayEquals(byte[], byte[])} except that it takes
     * advantage of hardware intrinsics offered by the JDK to optimize comparing the byte arrays.
     * <p>
     * If the arrays aren't equal this will call {@link Assertions#assertArrayEquals(byte[], byte[])} to take advantage
     * of the better error message, but this is the exceptional case and worth the double comparison performance hit.
     *
     * @param expected The expected byte array.
     * @param actual The actual byte array.
     */
    public static void assertArraysEqual(byte[] expected, byte[] actual) {
        if (!Arrays.equals(expected, actual)) {
            Assertions.assertArrayEquals(expected, actual);
        }
    }

    /**
     * Asserts that two arrays are equal.
     * <p>
     * This method is similar to JUnit's {@link Assertions#assertArrayEquals(byte[], byte[])} except that it takes
     * advantage of hardware intrinsics offered by the JDK to optimize comparing the byte arrays and allows for
     * comparing subsections of the arrays.
     * <p>
     * If the arrays aren't equal this will copy the array ranges and call
     * {@link Assertions#assertArrayEquals(byte[], byte[])} to take advantage of the better error message, but this is
     * the exceptional case and worth the double comparison performance hit.
     *
     * @param expected The expected byte array.
     * @param expectedOffset Starting offset to begin comparing in the expected array.
     * @param actual The actual byte array.
     * @param actualOffset Starting offset to begin comparing in the actual array.
     * @param length Amount of bytes to compare.
     */
    public static void assertArraysEqual(byte[] expected, int expectedOffset, byte[] actual,
        int actualOffset, int length) {
        // Use ByteBuffer comparison as it provides an optimized byte array comparison.
        // In Java 9+ there is Arrays.mismatch that provides this functionality directly, but Java 8 needs support.
        assertByteBuffersEqual(ByteBuffer.wrap(expected, expectedOffset, length),
            ByteBuffer.wrap(actual, actualOffset, length));
    }

    /**
     * Asserts that two {@link ByteBuffer ByteBuffers} are equal.
     * <p>
     * This method is similar to JUnit's {@link Assertions#assertArrayEquals(byte[], byte[])} except that it takes
     * advantage of hardware intrinsics offered by the JDK to optimize comparing the ByteBuffers.
     * <p>
     * If the ByteBuffers aren't equal this will copy the ByteBuffer contents into byte arrays and call
     * {@link Assertions#assertArrayEquals(byte[], byte[])} to take advantage of the better error message, but this is
     * the exceptional case and worth the double comparison performance hit.
     *
     * @param expected The expected {@link ByteBuffer}.
     * @param actual The actual {@link ByteBuffer}.
     */
    public static void assertByteBuffersEqual(ByteBuffer expected, ByteBuffer actual) {
        if (!Objects.equals(expected, actual)) {
            // Reset the ByteBuffers in case their position was changed.
            expected.reset();
            actual.reset();
            byte[] expectedArray = new byte[expected.remaining()];
            expected.get(expectedArray);
            byte[] actualArray = new byte[actual.remaining()];
            actual.get(actualArray);

            Assertions.assertArrayEquals(expectedArray, actualArray);
        }
    }

    private TestUtils() {
    }
}
