// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.rest;

import com.generic.core.exception.UnexpectedLengthException;
import com.generic.core.http.models.HttpHeaderName;
import com.generic.core.http.models.HttpMethod;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.models.BinaryData;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static com.generic.core.CoreTestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class RestProxyUtilsTests {
    private static final String SAMPLE = "sample";
    private static final byte[] EXPECTED = SAMPLE.getBytes(StandardCharsets.UTF_8);

    @ParameterizedTest
    @MethodSource("expectedBodyLengthDataProvider")
    public void expectedBodyLength(HttpRequest httpRequest) {
        BinaryData binaryData = RestProxyUtils.validateLength(httpRequest);

        assertNotNull(binaryData);
        assertArraysEqual(EXPECTED, binaryData.toBytes());
    }

    public static Stream<Arguments> expectedBodyLengthDataProvider() throws Exception {
        return dataProvider(EXPECTED.length);
    }

    @Test
    public void emptyRequestBody() {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost");

        assertNull(RestProxyUtils.validateLength(httpRequest));
    }

    @ParameterizedTest
    @MethodSource("unexpectedBodyLengthTooSmallDataProvider")
    public void unexpectedBodyLengthTooSmall(HttpRequest httpRequest) {
        assertThrows(UnexpectedLengthException.class, () -> validateAndCollectRequest(httpRequest), "Request body "
            + "emitted " + EXPECTED.length + " bytes, less than the expected " + (EXPECTED.length + 1) + " bytes.");
    }

    public static Stream<Arguments> unexpectedBodyLengthTooSmallDataProvider() throws Exception {
        return dataProvider(EXPECTED.length + 1);
    }

    @ParameterizedTest
    @MethodSource("unexpectedBodyLengthTooLargeDataProvider")
    public void unexpectedBodyLengthTooLarge(HttpRequest httpRequest) {
        assertThrows(UnexpectedLengthException.class, () -> validateAndCollectRequest(httpRequest), "Request body "
            + "emitted " + EXPECTED.length + " bytes, more than the expected " + (EXPECTED.length - 1) + " bytes.");
    }

    public static Stream<Arguments> unexpectedBodyLengthTooLargeDataProvider() throws Exception {
        return dataProvider(EXPECTED.length - 1);
    }

    @Test
    public void multipleToBytesToCheckBodyLength() {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost")
            .setBody(EXPECTED)
            .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(EXPECTED.length));

        BinaryData binaryData = RestProxyUtils.validateLength(httpRequest);

        assertNotNull(binaryData);
        assertArraysEqual(EXPECTED, binaryData.toBytes());
        assertArraysEqual(EXPECTED, binaryData.toBytes());
    }

    private static Stream<Arguments> dataProvider(int contentLength) throws Exception {
        Path file = Files.createTempFile(RestProxyUtils.class.getSimpleName(), null);
        file.toFile().deleteOnExit();
        Files.write(file, EXPECTED);
        return Stream.of(
            Arguments.of(
                Named.of("bytes", new HttpRequest(HttpMethod.GET, "http://localhost")
                    .setBody(EXPECTED)
                    .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength)))
            ),
            Arguments.of(
                Named.of("string", new HttpRequest(HttpMethod.GET, "http://localhost")
                    .setBody(SAMPLE)
                    .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength)))
            ),
            Arguments.of(
                Named.of("stream", new HttpRequest(HttpMethod.GET, "http://localhost")
                    .setBody(BinaryData.fromStream(new ByteArrayInputStream(EXPECTED), (long) EXPECTED.length))
                    .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength)))
            ),
            Arguments.of(
                Named.of("file", new HttpRequest(HttpMethod.GET, "http://localhost")
                    .setBody(BinaryData.fromFile(file))
                    .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength)))
            )
        );
    }

    @Test
    public void userProvidedLengthShouldNotBeTrustedTooLarge() throws IOException {
        try (InputStream byteArrayInputStream = new ByteArrayInputStream(EXPECTED)) {
            HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost")
                .setBody(BinaryData.fromStream(byteArrayInputStream, EXPECTED.length - 1L))
                .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(EXPECTED.length - 1L));

            UnexpectedLengthException thrown =
                assertThrows(UnexpectedLengthException.class, () -> validateAndCollectRequest(httpRequest),
                    "Expected validateLengthSync() to throw, but it didn't");

            assertEquals("Request body emitted " + EXPECTED.length + " bytes, more than the expected "
                + (EXPECTED.length - 1) + " bytes.", thrown.getMessage());
        }
    }

    @Test
    public void userProvidedLengthShouldNotBeTrustedTooSmall() throws IOException {
        try (InputStream byteArrayInputStream = new ByteArrayInputStream(EXPECTED)) {
            HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost")
                .setBody(BinaryData.fromStream(byteArrayInputStream, EXPECTED.length + 1L))
                .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(EXPECTED.length + 1L));

            UnexpectedLengthException thrown =
                assertThrows(UnexpectedLengthException.class, () -> validateAndCollectRequest(httpRequest),
                    "Expected validateLengthSync() to throw, but it didn't");

            assertEquals("Request body emitted " + EXPECTED.length + " bytes, less than the expected "
                + (EXPECTED.length + 1) + " bytes.", thrown.getMessage());
        }
    }

    @Test
    public void emptyRequestBodySync() {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost");

        try {
            RestProxyUtils.validateLength(httpRequest);
        } catch (Exception e) {
            fail("The test Should not have thrown any exception.");
        }
    }

    @Test
    public void expectedBodyLengthSync() throws IOException {
        try (InputStream byteArrayInputStream = new ByteArrayInputStream(EXPECTED)) {
            HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost")
                .setBody(BinaryData.fromStream(byteArrayInputStream, (long) EXPECTED.length))
                .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(EXPECTED.length));

            assertArraysEqual(EXPECTED, validateAndCollectRequest(httpRequest));
        }
    }

    private static byte[] validateAndCollectRequest(HttpRequest request) {
        return RestProxyUtils.validateLength(request).toBytes();
    }
}
