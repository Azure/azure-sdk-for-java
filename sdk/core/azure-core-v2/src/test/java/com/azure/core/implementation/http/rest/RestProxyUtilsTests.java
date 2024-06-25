// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.rest;

import com.azure.core.v2.exception.UnexpectedLengthException;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import com.azure.core.v2.util.BinaryData;
import com.azure.core.v2.util.FluxUtil;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;

import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static com.azure.core.CoreTestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        StepVerifier
            .create(RestProxyUtils.validateLengthAsync(httpRequest)
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getBody())))
            .assertNext(bytes -> assertArraysEqual(EXPECTED, bytes))
            .verifyComplete();
    }

    public static Stream<Arguments> expectedBodyLengthDataProvider() throws Exception {
        return dataProvider(EXPECTED.length);
    }

    @Test
    public void emptyRequestBody() {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost");

        httpRequest = RestProxyUtils.validateLengthAsync(httpRequest).block();

        assertNull(httpRequest.getBody());
        assertNull(httpRequest.getBodyAsBinaryData());
    }

    @ParameterizedTest
    @MethodSource("unexpectedBodyLengthTooSmallDataProvider")
    public void unexpectedBodyLengthTooSmall(HttpRequest httpRequest) {
        StepVerifier.create(validateAndCollectRequestAsync(httpRequest)).verifyErrorSatisfies(throwable -> {
            assertTrue(throwable instanceof UnexpectedLengthException);
            assertEquals("Request body emitted " + EXPECTED.length + " bytes, less than the expected "
                + (EXPECTED.length + 1) + " bytes.", throwable.getMessage());
        });
    }

    public static Stream<Arguments> unexpectedBodyLengthTooSmallDataProvider() throws Exception {
        return dataProvider(EXPECTED.length + 1);
    }

    @ParameterizedTest
    @MethodSource("unexpectedBodyLengthTooLargeDataProvider")
    public void unexpectedBodyLengthTooLarge(HttpRequest httpRequest) {
        StepVerifier.create(validateAndCollectRequestAsync(httpRequest)).verifyErrorSatisfies(throwable -> {
            assertTrue(throwable instanceof UnexpectedLengthException);
            assertEquals("Request body emitted " + EXPECTED.length + " bytes, more than the expected "
                + (EXPECTED.length - 1) + " bytes.", throwable.getMessage());
        });
    }

    public static Stream<Arguments> unexpectedBodyLengthTooLargeDataProvider() throws Exception {
        return dataProvider(EXPECTED.length - 1);
    }

    @Test
    public void multipleSubscriptionsToCheckBodyLength() {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost").setBody(EXPECTED)
            .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(EXPECTED.length));

        Flux<ByteBuffer> verifierFlux
            = RestProxyUtils.validateLengthAsync(httpRequest).flatMapMany(HttpRequest::getBody);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(verifierFlux))
            .assertNext(bytes -> assertArraysEqual(EXPECTED, bytes))
            .verifyComplete();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(verifierFlux))
            .assertNext(bytes -> assertArraysEqual(EXPECTED, bytes))
            .verifyComplete();
    }

    private static Stream<Arguments> dataProvider(int contentLength) throws Exception {
        Path file = Files.createTempFile(RestProxyUtils.class.getSimpleName(), null);
        file.toFile().deleteOnExit();
        Files.write(file, EXPECTED);
        return Stream.of(
            Arguments.of(Named.of("bytes",
                new HttpRequest(HttpMethod.GET, "http://localhost").setBody(EXPECTED)
                    .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength)))),
            Arguments.of(Named.of("string",
                new HttpRequest(HttpMethod.GET, "http://localhost").setBody(SAMPLE)
                    .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength)))),
            Arguments.of(Named.of("flux",
                new HttpRequest(HttpMethod.GET, "http://localhost").setBody(Flux.just(ByteBuffer.wrap(EXPECTED)))
                    .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength)))),
            Arguments.of(Named.of("stream",
                new HttpRequest(HttpMethod.GET, "http://localhost")
                    .setBody(BinaryData.fromStream(new ByteArrayInputStream(EXPECTED), (long) EXPECTED.length))
                    .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength)))),
            Arguments.of(
                Named.of("file", new HttpRequest(HttpMethod.GET, "http://localhost").setBody(BinaryData.fromFile(file))
                    .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength)))));
    }

    @Test
    public void userProvidedLengthShouldNotBeTrustedTooSmall() {

        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost")
            .setBody(BinaryData.fromFlux(Flux.just(ByteBuffer.wrap(EXPECTED)), EXPECTED.length + 1L, false).block())
            .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(EXPECTED.length + 1L));

        StepVerifier.create(validateAndCollectRequestAsync(httpRequest)).verifyErrorSatisfies(throwable -> {
            assertTrue(throwable instanceof UnexpectedLengthException);
            assertEquals("Request body emitted " + EXPECTED.length + " bytes, less than the expected "
                + (EXPECTED.length + 1) + " bytes.", throwable.getMessage());
        });
    }

    @Test
    public void userProvidedLengthShouldNotBeTrustedTooLarge() {

        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost")
            .setBody(BinaryData.fromFlux(Flux.just(ByteBuffer.wrap(EXPECTED)), EXPECTED.length - 1L, false).block())
            .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(EXPECTED.length - 1L));

        StepVerifier.create(validateAndCollectRequestAsync(httpRequest)).verifyErrorSatisfies(throwable -> {
            assertTrue(throwable instanceof UnexpectedLengthException);
            assertEquals("Request body emitted " + EXPECTED.length + " bytes, more than the expected "
                + (EXPECTED.length - 1) + " bytes.", throwable.getMessage());
        });
    }

    @Test
    public void userProvidedLengthShouldNotBeTrustedTooLargeSync() throws IOException {
        try (InputStream byteArrayInputStream = new ByteArrayInputStream(EXPECTED)) {
            HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost")
                .setBody(BinaryData.fromStream(byteArrayInputStream, EXPECTED.length - 1L))
                .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(EXPECTED.length - 1L));

            UnexpectedLengthException thrown
                = assertThrows(UnexpectedLengthException.class, () -> validateAndCollectRequestSync(httpRequest),
                    "Expected validateLengthSync() to throw, but it didn't");
            assertEquals("Request body emitted " + EXPECTED.length + " bytes, more than the expected "
                + (EXPECTED.length - 1) + " bytes.", thrown.getMessage());
        }
    }

    @Test
    public void userProvidedLengthShouldNotBeTrustedTooSmallSync() throws IOException {
        try (InputStream byteArrayInputStream = new ByteArrayInputStream(EXPECTED)) {
            HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost")
                .setBody(BinaryData.fromStream(byteArrayInputStream, EXPECTED.length + 1L))
                .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(EXPECTED.length + 1L));

            UnexpectedLengthException thrown
                = assertThrows(UnexpectedLengthException.class, () -> validateAndCollectRequestSync(httpRequest),
                    "Expected validateLengthSync() to throw, but it didn't");
            assertEquals("Request body emitted " + EXPECTED.length + " bytes, less than the expected "
                + (EXPECTED.length + 1) + " bytes.", thrown.getMessage());
        }
    }

    @Test
    public void emptyRequestBodySync() {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost");

        try {
            RestProxyUtils.validateLengthSync(httpRequest);
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
            assertArraysEqual(EXPECTED, validateAndCollectRequestSync(httpRequest));
        }
    }

    private static byte[]> validateAndCollectRequestAsync(HttpRequest request) {
        return RestProxyUtils.validateLengthAsync(request)
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getBody()));
    }

    private static byte[] validateAndCollectRequestSync(HttpRequest request) {
        return RestProxyUtils.validateLengthSync(request).toBytes();
    }
}
