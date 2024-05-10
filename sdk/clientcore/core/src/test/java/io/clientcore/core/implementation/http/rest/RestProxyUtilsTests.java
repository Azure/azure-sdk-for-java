// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.rest;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.util.binarydata.BinaryData;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static io.clientcore.core.util.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertThrows(IllegalStateException.class, () -> validateAndCollectRequest(httpRequest), "Request body "
            + "emitted " + EXPECTED.length + " bytes, less than the expected " + (EXPECTED.length + 1) + " bytes.");
    }

    public static Stream<Arguments> unexpectedBodyLengthTooSmallDataProvider() throws Exception {
        return dataProvider(EXPECTED.length + 1);
    }

    @ParameterizedTest
    @MethodSource("unexpectedBodyLengthTooLargeDataProvider")
    public void unexpectedBodyLengthTooLarge(HttpRequest httpRequest) {
        assertThrows(IllegalStateException.class, () -> validateAndCollectRequest(httpRequest), "Request body "
            + "emitted " + EXPECTED.length + " bytes, more than the expected " + (EXPECTED.length - 1) + " bytes.");
    }

    public static Stream<Arguments> unexpectedBodyLengthTooLargeDataProvider() throws Exception {
        return dataProvider(EXPECTED.length - 1);
    }

    @Test
    public void multipleToBytesToCheckBodyLength() {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost")
            .setBody(BinaryData.fromBytes(EXPECTED));
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(EXPECTED.length));

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
                Named.of("bytes", createHttpRequest("http://localhost", BinaryData.fromBytes(EXPECTED), contentLength))
            ),
            Arguments.of(
                Named.of("string", createHttpRequest("http://localhost", BinaryData.fromString(SAMPLE), contentLength))
            ),
            Arguments.of(
                Named.of("stream", createHttpRequest("http://localhost", BinaryData.fromFile(file), contentLength))
            ),
            Arguments.of(
                Named.of("file", createHttpRequest("http://localhost", BinaryData.fromFile(file), contentLength))
            )
        );
    }

    private static HttpRequest createHttpRequest(String url, BinaryData body, int contentLength) {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, url)
            .setBody(body);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength));
        return httpRequest;
    }


    @Test
    public void userProvidedLengthShouldNotBeTrustedTooLarge() throws IOException {
        try (InputStream byteArrayInputStream = new ByteArrayInputStream(EXPECTED)) {
            HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost")
                .setBody(BinaryData.fromStream(byteArrayInputStream, EXPECTED.length - 1L));
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(EXPECTED.length - 1L));

            IllegalStateException thrown =
                assertThrows(IllegalStateException.class, () -> validateAndCollectRequest(httpRequest),
                    "Expected validateLength() to throw, but it didn't");
            assertEquals("Request body emitted " + EXPECTED.length + " bytes, more than the expected "
                + (EXPECTED.length - 1) + " bytes.", thrown.getMessage());
        }
    }

    @Test
    public void userProvidedLengthShouldNotBeTrustedTooSmall() throws IOException {
        try (InputStream byteArrayInputStream = new ByteArrayInputStream(EXPECTED)) {
            HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost")
                .setBody(BinaryData.fromStream(byteArrayInputStream, EXPECTED.length + 1L));
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(EXPECTED.length + 1L));

            IllegalStateException thrown =
                assertThrows(IllegalStateException.class, () -> validateAndCollectRequest(httpRequest),
                    "Expected validateLength() to throw, but it didn't");

            assertEquals("Request body emitted " + EXPECTED.length + " bytes, less than the expected "
                + (EXPECTED.length + 1) + " bytes.", thrown.getMessage());
        }
    }

    @Test
    public void expectedBodyLength() throws IOException {
        try (InputStream byteArrayInputStream = new ByteArrayInputStream(EXPECTED)) {
            HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost")
                .setBody(BinaryData.fromStream(byteArrayInputStream, (long) EXPECTED.length));
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(EXPECTED.length));

            assertArraysEqual(EXPECTED, validateAndCollectRequest(httpRequest));
        }
    }

    private static byte[] validateAndCollectRequest(HttpRequest request) {
        return RestProxyUtils.validateLength(request).toBytes();
    }
}
