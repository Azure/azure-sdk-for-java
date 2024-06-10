// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util;

import io.clientcore.core.implementation.AccessibleByteArrayOutputStream;
import io.clientcore.core.implementation.util.UrlBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;

import static io.clientcore.core.implementation.util.ImplUtils.isNullOrEmpty;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Utility methods for testing {@code core}.
 */
public final class TestUtils {
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
     *
     * @return The byte array representing its contents.
     *
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
     *
     * @return The byte array representing its contents.
     *
     * @throws IOException If an error happens during reading.
     */
    public static byte[] readStream(InputStream stream, int bufferSize) throws IOException {
        AccessibleByteArrayOutputStream outputStream = new AccessibleByteArrayOutputStream();
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
     *
     * @return The byte array representing its contents.
     *
     * @throws IOException If an error happens during reading.
     */
    public static byte[] readStreamByteByByte(InputStream stream) throws IOException {
        AccessibleByteArrayOutputStream outputStream = new AccessibleByteArrayOutputStream();
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
     *
     * @return The URL representing the URL string.
     *
     * @throws MalformedURLException If the URL string isn't a valid URL.
     */
    public static URL createUrl(String urlString) throws MalformedURLException {
        return UrlBuilder.parse(urlString).toUrl();
    }

    private TestUtils() {
    }

    /**
     * Creates a copy of the source byte array.
     *
     * @param source Array to make copy of.
     *
     * @return A copy of the array, or null if source was null.
     */
    public static byte[] cloneByteArray(byte[] source) {
        if (source == null) {
            return null;
        }

        byte[] copy = new byte[source.length];

        System.arraycopy(source, 0, copy, 0, source.length);

        return copy;
    }

    /**
     * Returns the first instance of the given class from an array of Objects.
     *
     * @param args Array of objects to search through to find the first instance of the given `clazz` type.
     * @param clazz The type trying to be found.
     * @param <T> Generic type
     *
     * @return The first object of the desired type, otherwise null.
     */
    public static <T> T findFirstOfType(Object[] args, Class<T> clazz) {
        if (isNullOrEmpty(args)) {
            return null;
        }

        for (Object arg : args) {
            if (clazz.isInstance(arg)) {
                return clazz.cast(arg);
            }
        }

        return null;
    }
}
