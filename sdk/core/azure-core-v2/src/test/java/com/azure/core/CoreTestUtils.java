// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core;

import io.clientcore.core.implementation.util.UrlBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Utility methods for testing {@code azure-core}.
 */
public final class CoreTestUtils {
    private static final byte[] BYTES;
    private static final int BYTES_LENGTH;

    static {
        BYTES = new byte[1024 * 1024];
        new SecureRandom().nextBytes(BYTES);
        BYTES_LENGTH = BYTES.length;
    }

    /**
     * Asserts that two arrays are equal in an optimized way when they are equal (common case).
     *
     * @param expected Expected array.
     * @param actual Actual array.
     */
    public static void assertArraysEqual(byte[] expected, byte[] actual) {
        assertArraysEqual(expected, 0, expected.length, actual, actual.length);
    }

    /**
     * Asserts that two arrays are equal in an optimized way when they are equal (common case).
     *
     * @param expected Expected array.
     * @param expectedOffset Offset to begin comparing in the expected array.
     * @param expectedLength Amount of bytes to compare in the expected array.
     * @param actual Actual array.
     */
    public static void assertArraysEqual(byte[] expected, int expectedOffset, int expectedLength, byte[] actual) {
        assertArraysEqual(expected, expectedOffset, expectedLength, actual, actual.length);
    }

    /**
     * Asserts that two arrays are equal in an optimized way when they are equal (common case).
     *
     * @param expected Expected array.
     * @param expectedOffset Offset to begin comparing in the expected array.
     * @param expectedLength Amount of bytes to compare in the expected array.
     * @param actual Actual array.
     * @param actualLength Amount of bytes to compare in the actual array.
     */
    public static void assertArraysEqual(byte[] expected, int expectedOffset, int expectedLength, byte[] actual,
        int actualLength) {
        if (!Objects.equals(ByteBuffer.wrap(expected, expectedOffset, expectedLength),
            ByteBuffer.wrap(actual, 0, actualLength))) {
            assertArrayEquals(Arrays.copyOfRange(expected, expectedOffset, expectedOffset + expectedLength),
                Arrays.copyOfRange(actual, 0, actualLength));
        }
    }

    /**
     * Fills the passed byte array with random bytes.
     *
     * @param array The array to fill.
     */
    public static void fillArray(byte[] array) {
        int size = array.length;
        int count = size / BYTES_LENGTH;
        int remainder = size % BYTES_LENGTH;

        for (int i = 0; i < count; i++) {
            System.arraycopy(BYTES, 0, array, i * BYTES_LENGTH, BYTES_LENGTH);
        }

        if (remainder > 0) {
            System.arraycopy(BYTES, 0, array, count * BYTES_LENGTH, remainder);
        }
    }

    /**
     * Reads an {@link InputStream} to completion returning its contents, using a read buffer.
     *
     * @param stream The stream to read.
     * @return The byte array representing its contents.
     * @throws IOException If an error happens during reading.
     */
    public static byte[] readStream(InputStream stream) throws IOException {
        return readStream(stream, 8 * 1024);
    }

    /**
     * Reads an {@link InputStream} to completion returning its contents, using a read buffer.
     *
     * @param stream The stream to read.
     * @param bufferSize The size of the read buffer.
     * @return The byte array representing its contents.
     * @throws IOException If an error happens during reading.
     */
    public static byte[] readStream(InputStream stream, int bufferSize) throws IOException {
        TestByteArrayOutputStream outputStream = new TestByteArrayOutputStream();
        byte[] buffer = new byte[bufferSize];
        int length;
        while ((length = stream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }
        return outputStream.toByteArray();
    }

    /**
     * Reads an {@link InputStream} to completion returning its contents, reading byte by byte.
     *
     * @param stream The stream to read.
     * @return The byte array representing its contents.
     * @throws IOException If an error happens during reading.
     */
    public static byte[] readStreamByteByByte(InputStream stream) throws IOException {
        TestByteArrayOutputStream outputStream = new TestByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int bufferPosition = 0;
        int tmp;
        while ((tmp = stream.read()) != -1) {
            if (bufferPosition >= 8192) {
                outputStream.write(buffer);
                bufferPosition = 0;
            }

            buffer[bufferPosition++] = (byte) tmp;
        }

        if (bufferPosition > 0) {
            outputStream.write(buffer, 0, bufferPosition);
        }

        return outputStream.toByteArray();
    }

    /**
     * Convenience method for creating {@link URL} now that as of Java 20+ all {@link URL} constructors are deprecated.
     * <p>
     * This uses the logic {@code URI.create(String).toURL()}, which is recommended instead of the URL constructors.
     *
     * @param urlString The URL string.
     * @return The URL representing the URL string.
     * @throws MalformedURLException If the URL string isn't a valid URL.
     */
    public static URL createUrl(String urlString) throws MalformedURLException {
        return UrlBuilder.parse(urlString).toUrl();
    }

    private CoreTestUtils() {
    }
}
