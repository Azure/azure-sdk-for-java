// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.test.common;

import org.junit.jupiter.api.Assertions;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

/**
 * Utility methods for testing HTTP clients.
 */
public final class HttpTestUtils {
    /**
     * Returns base64 encoded MD5 of bytes.
     *
     * @param bytes bytes.
     * @return base64 encoded MD5 of bytes.
     * @throws RuntimeException if md5 is not found.
     */
    public static String md5(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(bytes);
            return Base64.getEncoder().encodeToString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns base64 encoded MD5 of flux of byte buffers.
     *
     * @param bufferFlux flux of byte buffers.
     * @return Mono that emits base64 encoded MD5 of bytes.
     */
    public static Mono<String> md5(Flux<ByteBuffer> bufferFlux) {
        return bufferFlux.reduceWith(() -> {
            try {
                return MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw Exceptions.propagate(e);
            }
        }, (digest, buffer) -> {
            digest.update(buffer);
            return digest;
        }).map(digest -> Base64.getEncoder().encodeToString(digest.digest()));
    }

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
    public static void assertArraysEqual(byte[] expected, int expectedOffset, byte[] actual, int actualOffset,
        int length) {
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
        int expectedPosition = 0;
        int actualPosition = 0;
        if (expected != null) {
            expectedPosition = expected.position();
        }

        if (actual != null) {
            actualPosition = actual.position();
        }

        if (!Objects.equals(expected, actual)) {
            // Reset the ByteBuffers in case their position was changed.
            byte[] expectedArray = null;
            if (expected != null) {
                expected.position(expectedPosition);
                expectedArray = new byte[expected.remaining()];
                expected.get(expectedArray);
            }

            byte[] actualArray = null;
            if (actual != null) {
                actual.position(actualPosition);
                actualArray = new byte[actual.remaining()];
                actual.get(actualArray);
            }

            Assertions.assertArrayEquals(expectedArray, actualArray);
        }
    }

    /**
     * Copies the data from the input stream to the output stream.
     *
     * @param source The input stream to copy from.
     * @param destination The output stream to copy to.
     * @throws IOException If an I/O error occurs.
     */
    public static void copy(InputStream source, OutputStream destination) throws IOException {
        byte[] buffer = new byte[8192];
        int read;

        while ((read = source.read(buffer, 0, buffer.length)) != -1) {
            destination.write(buffer, 0, read);
        }
    }

    private HttpTestUtils() {
    }
}
