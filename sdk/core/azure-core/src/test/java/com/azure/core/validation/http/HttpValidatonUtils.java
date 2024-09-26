// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.validation.http;

import com.azure.core.util.UrlBuilder;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Utility methods for testing {@code azure-core}.
 */
public final class HttpValidatonUtils {
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

    private HttpValidatonUtils() {
    }
}
