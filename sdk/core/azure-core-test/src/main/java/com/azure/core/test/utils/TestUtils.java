// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.utils;

import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

/**
 * Contains utility methods used for testing.
 */
public final class TestUtils {

    private static final ClientLogger LOGGER = new ClientLogger(TestUtils.class);

    private static final String RECORD_FOLDER = "session-records/";

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
     * Get the {@link File} pointing to the folder where session records live.
     * @return The session-records folder.
     * @throws IllegalStateException if the session-records folder cannot be found.
     */
    public static File getRecordFolder() {
        URL folderUrl = TestUtils.class.getClassLoader().getResource(RECORD_FOLDER);

        if (folderUrl != null) {
            // Use toURI as getResource will return a URL encoded file path that can only be cleaned up using the
            // URI-based constructor of File.
            return new File(toURI(folderUrl));
        }

        throw new IllegalStateException("Unable to locate session-records folder. Please create a session-records "
            + "folder in '/src/test/resources' of the module (ex. for azure-core-test this is "
            + "'/sdk/core/azure-core-test/src/test/resources/session-records').");
    }


    /**
     *  Returns a {@link java.net.URI} equivalent to this URL.
     * @param url the url to be converted to URI
     * @return the URI
     */
    public static URI toURI(URL url) {
        try {
            return url.toURI();
        } catch (URISyntaxException ex) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(ex));
        }
    }

    private TestUtils() {
    }

    /**
     * Locates the root of the current repo until the provided folder's parent.
     *
     * @param testClassPath the test class path
     * @param resolveFolder the folder parent to resolve the path until
     * @return The {@link Path} to the root of the repo.
     * @throws RuntimeException The specified folder could not be located.
     */
    public static Path getRepoRootResolveUntil(Path testClassPath, String resolveFolder) {
        String repoName = "\\azure-sdk-for-java";
        Path path = testClassPath;
        Path candidate = null;
        while (path != null && !path.endsWith(repoName)) {
            candidate = path.resolve(resolveFolder);
            if (Files.exists(candidate)) {
                break;
            }
            path = path.getParent();
        }
        if (path == null) {
            throw new RuntimeException(String.format(
                "Could not locate %s folder within repository %s", resolveFolder, repoName));
        }
        return path;
    }
}
