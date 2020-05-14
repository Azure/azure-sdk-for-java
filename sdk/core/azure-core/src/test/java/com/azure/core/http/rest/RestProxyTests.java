// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link RestProxy}.
 */
public class RestProxyTests {
    private static final byte[] EXPECTED = new byte[]{0, 1, 2, 3, 4};

    @Test
    public void emptyRequestBody() {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost");

        StepVerifier.create(RestProxy.validateLength(httpRequest))
            .verifyComplete();
    }

    @Test
    public void expectedBodyLength() {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost")
            .setBody(EXPECTED)
            .setHeader("Content-Length", "5");

        StepVerifier.create(collectRequest(httpRequest))
            .assertNext(bytes -> assertArrayEquals(EXPECTED, bytes))
            .verifyComplete();
    }

    @Test
    public void unexpectedBodyLength() {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost")
            .setBody(EXPECTED);

        StepVerifier.create(collectRequest(httpRequest.setHeader("Content-Length", "4")))
            .verifyErrorSatisfies(throwable -> {
                assertTrue(throwable instanceof UnexpectedLengthException);
                assertEquals("Request body emitted 5 bytes, more than the expected 4 bytes.", throwable.getMessage());
            });

        StepVerifier.create(collectRequest(httpRequest.setHeader("Content-Length", "6")))
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

        Flux<ByteBuffer> verifierFlux = RestProxy.validateLength(httpRequest);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(verifierFlux))
            .assertNext(bytes -> assertArrayEquals(EXPECTED, bytes))
            .verifyComplete();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(verifierFlux))
            .assertNext(bytes -> assertArrayEquals(EXPECTED, bytes))
            .verifyComplete();
    }

    private static Mono<byte[]> collectRequest(HttpRequest request) {
        return FluxUtil.collectBytesInByteBufferStream(RestProxy.validateLength(request));
    }
}
