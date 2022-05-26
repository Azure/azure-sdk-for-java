// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.rest;

import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.FluxUtil;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RestProxyUtilsTests {

    private static final byte[] EXPECTED = new byte[]{0, 1, 2, 3, 4};

    @Test
    public void expectedBodyLength() {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost")
            .setBody(EXPECTED)
            .setHeader("Content-Length", "5");

        StepVerifier.create(
                RestProxyUtils.validateLengthAsync(httpRequest)
                        .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getBody())))
            .assertNext(bytes -> assertArrayEquals(EXPECTED, bytes))
            .verifyComplete();
    }

    @Test
    public void emptyRequestBody() {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost");

        httpRequest = RestProxyUtils.validateLengthAsync(httpRequest).block();

        assertNull(httpRequest.getBody());
        assertNull(httpRequest.getBodyAsBinaryData());
    }

    @Test
    public void unexpectedBodyLength() {

        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost")
            .setBody(EXPECTED)
            .setHeader("Content-Length", "4");
        StepVerifier.create(validateAndCollectRequest(httpRequest))
            .verifyErrorSatisfies(throwable -> {
                assertTrue(throwable instanceof UnexpectedLengthException);
                assertEquals("Request body emitted 5 bytes, more than the expected 4 bytes.", throwable.getMessage());
            });

        httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost")
            .setBody(EXPECTED)
            .setHeader("Content-Length", "6");
        StepVerifier.create(validateAndCollectRequest(httpRequest))
            .verifyErrorSatisfies(throwable -> {
                assertTrue(throwable instanceof UnexpectedLengthException);
                assertEquals("Request body emitted 5 bytes, less than the expected 6 bytes.", throwable.getMessage());
            });
    }

    @Test
    public void multipleSubscriptionsToCheckBodyLength() {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost")
            .setBody(EXPECTED)
            .setHeader("Content-Length", "5");

        Flux<ByteBuffer> verifierFlux = RestProxyUtils.validateLengthAsync(httpRequest)
            .flatMapMany(HttpRequest::getBody);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(verifierFlux))
            .assertNext(bytes -> assertArrayEquals(EXPECTED, bytes))
            .verifyComplete();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(verifierFlux))
            .assertNext(bytes -> assertArrayEquals(EXPECTED, bytes))
            .verifyComplete();
    }

    private static Mono<byte[]> validateAndCollectRequest(HttpRequest request) {
        return RestProxyUtils.validateLengthAsync(request)
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getBody()));
    }
}
