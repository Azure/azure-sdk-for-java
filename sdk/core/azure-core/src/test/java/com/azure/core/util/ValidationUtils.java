// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.api.Assertions;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

/**
 * Utility class containing methods which help validate testing expectations.
 * <p>
 * Copy of ValidationUtils in azure-core-test as azure-core cannot take a dependency on azure-core-test.
 */
public final class ValidationUtils {
    /**
     * An optimized version of {@link Assertions#assertArrayEquals(byte[], byte[])}.
     * <p>
     * The built-in JUnit {@code assertArraysEquals} performs a byte-by-byte comparison, this implementation leverages
     * optimizations in the JDK which use hardware intrinsics to vectorize comparisons. The downside of the vectorized
     * comparison is that it won't indicate the range of differences between the arrays, so if the arrays aren't equals
     * this will re-compare them using JUnit's {@code assertArraysEquals} to provide the easy to parse error message.
     * This adds overhead which is fine as the common case should be equal arrays.
     *
     * @param expected The expected array.
     * @param actual The actual array.
     */
    public static void assertArraysEqual(byte[] expected, byte[] actual) {
        // If one array is null assert that they're equal.
        if (expected == null || actual == null) {
            Assertions.assertSame(expected, actual);
            return;
        }

        assertArraysEqual(expected, 0, actual, 0, expected.length);
    }

    /**
     * An optimized version of {@link Assertions#assertArrayEquals(byte[], byte[])}.
     * <p>
     * The built-in JUnit {@code assertArraysEquals} performs a byte-by-byte comparison, this implementation leverages
     * optimizations in the JDK which use hardware intrinsics to vectorize comparisons. The downside of the vectorized
     * comparison is that it won't indicate the range of differences between the arrays, so if the arrays aren't equals
     * this will re-compare them using JUnit's {@code assertArraysEquals} to provide the easy to parse error message.
     * This adds overhead which is fine as the common case should be equal arrays.
     *
     * @param expected The expected array.
     * @param expectedOffset The offset to begin matching at in the expected array.
     * @param actual The actual array.
     * @param actualOffset The offset to begin matching at in the actual array.
     * @param length The length to match in the arrays.
     */
    public static void assertArraysEqual(byte[] expected, int expectedOffset, byte[] actual, int actualOffset,
        int length) {
        // If one array is null assert that they're equal.
        if (expected == null || actual == null) {
            Assertions.assertSame(expected, actual);
            return;
        }

        // If the offsets are 0 and the length is equal to the length of both arrays compare the arrays directly.
        if (expectedOffset == 0 && actualOffset == 0 && expected.length == length && actual.length == length) {
            if (!Arrays.equals(expected, actual)) {
                Assertions.assertArrayEquals(expected, actual);
            }

            return;
        }

        // Otherwise use a ByteBuffer to wrap the ranges (later versions of Java support comparing array ranges)
        // ByteBuffers will use a vectorized comparison on ranges.
        ByteBuffer expectedBuffer = ByteBuffer.wrap(expected, expectedOffset, length);
        ByteBuffer actualBuffer = ByteBuffer.wrap(actual, actualOffset, length);
        if (!Objects.equals(expectedBuffer, actualBuffer)) {
            byte[] expectedRange = Arrays.copyOfRange(expected, expectedOffset, expectedOffset + length);
            byte[] actualRange = Arrays.copyOfRange(actual, actualOffset, actualOffset + length);
            Assertions.assertArrayEquals(expectedRange, actualRange);
        }
    }
}
