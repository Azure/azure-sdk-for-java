// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.rest;

import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.BinaryData;
import com.azure.core.util.FluxUtil;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RestProxyUtilsTests {

    private static final String SAMPLE = "sample";
    private static final byte[] EXPECTED = SAMPLE.getBytes(StandardCharsets.UTF_8);

    @ParameterizedTest
    @MethodSource("expectedBodyLengthDataProvider")
    public void expectedBodyLength(HttpRequest httpRequest) {
        StepVerifier.create(
                RestProxyUtils.validateLengthAsync(httpRequest)
                        .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getBody())))
            .assertNext(bytes -> assertArrayEquals(EXPECTED, bytes))
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
        StepVerifier.create(validateAndCollectRequest(httpRequest))
            .verifyErrorSatisfies(throwable -> {
                assertTrue(throwable instanceof UnexpectedLengthException);
                assertEquals(
                    "Request body emitted " + EXPECTED.length + " bytes, less than the expected " + (EXPECTED.length + 1) + " bytes.",
                    throwable.getMessage());
            });
    }

    public static Stream<Arguments> unexpectedBodyLengthTooSmallDataProvider() throws Exception {
        return dataProvider(EXPECTED.length + 1);
    }

    @ParameterizedTest
    @MethodSource("unexpectedBodyLengthTooLargeDataProvider")
    public void unexpectedBodyLengthTooLarge(HttpRequest httpRequest) {
        StepVerifier.create(validateAndCollectRequest(httpRequest))
            .verifyErrorSatisfies(throwable -> {
                assertTrue(throwable instanceof UnexpectedLengthException);
                assertEquals(
                    "Request body emitted " + EXPECTED.length + " bytes, more than the expected " + (EXPECTED.length - 1) + " bytes.",
                    throwable.getMessage());
            });
    }

    public static Stream<Arguments> unexpectedBodyLengthTooLargeDataProvider() throws Exception {
        return dataProvider(EXPECTED.length - 1);
    }

    @Test
    public void multipleSubscriptionsToCheckBodyLength() {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost")
            .setBody(EXPECTED)
            .setHeader("Content-Length", String.valueOf(EXPECTED.length));

        Flux<ByteBuffer> verifierFlux = RestProxyUtils.validateLengthAsync(httpRequest)
            .flatMapMany(HttpRequest::getBody);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(verifierFlux))
            .assertNext(bytes -> assertArrayEquals(EXPECTED, bytes))
            .verifyComplete();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(verifierFlux))
            .assertNext(bytes -> assertArrayEquals(EXPECTED, bytes))
            .verifyComplete();
    }

    private static Stream<Arguments> dataProvider(int contentLength) throws Exception {
        Path file = Files.createTempFile(RestProxyUtils.class.getSimpleName(), null);
        file.toFile().deleteOnExit();
        Files.write(file, EXPECTED);
        return Stream.of(
            Arguments.of(
                Named.of("bytes", new HttpRequest(HttpMethod.GET, "http://localhost")
                    .setBody(EXPECTED)
                    .setHeader("Content-Length", String.valueOf(contentLength)))
            ),
            Arguments.of(
                Named.of("string", new HttpRequest(HttpMethod.GET, "http://localhost")
                    .setBody(SAMPLE)
                    .setHeader("Content-Length", String.valueOf(contentLength)))
            ),
            Arguments.of(
                Named.of("flux", new HttpRequest(HttpMethod.GET, "http://localhost")
                    .setBody(Flux.just(ByteBuffer.wrap(EXPECTED)))
                    .setHeader("Content-Length", String.valueOf(contentLength)))
            ),
            Arguments.of(
                Named.of("stream", new HttpRequest(HttpMethod.GET, "http://localhost")
                    .setBody(BinaryData.fromStream(new ByteArrayInputStream(EXPECTED)))
                    .setHeader("Content-Length", String.valueOf(contentLength)))
            ),
            Arguments.of(
                Named.of("file", new HttpRequest(HttpMethod.GET, "http://localhost")
                    .setBody(BinaryData.fromFile(file))
                    .setHeader("Content-Length", String.valueOf(contentLength)))
            )
        );
    }

    private static Mono<byte[]> validateAndCollectRequest(HttpRequest request) {
        return RestProxyUtils.validateLengthAsync(request)
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getBody()));
    }
}
