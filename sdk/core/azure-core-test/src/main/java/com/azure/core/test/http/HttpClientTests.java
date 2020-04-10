// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Generic test suite for {@link HttpClient HttpClients}.
 */
public abstract class HttpClientTests {
    private static final String REQUEST_HOST = "http://localhost";
    private static final String PLAIN_RESPONSE = "plainBytesNoHeader";
    private static final String HEADER_RESPONSE = "plainBytesWithHeader";
    private static final String INVALID_HEADER_RESPONSE = "plainBytesInvalidHeader";
    private static final String UTF_8_BOM_RESPONSE = "utf8BomBytes";
    private static final String UTF_16BE_BOM_RESPONSE = "utf16BeBomBytes";
    private static final String UTF_16LE_BOM_RESPONSE = "utf16LeBomBytes";
    private static final String UTF_32BE_BOM_RESPONSE = "utf32BeBomBytes";
    private static final String UTF_32LE_BOM_RESPONSE = "utf32LeBomBytes";
    private static final String BOM_WITH_SAME_HEADER = "bomBytesWithSameHeader";
    private static final String BOM_WITH_DIFFERENT_HEADER = "bomBytesWithDifferentHeader";

    private static final byte[] EXPECTED_RETURN_BYTES = "Hello World!".getBytes(StandardCharsets.UTF_8);

    /**
     * Get the HTTP client that will be used for each test. This will be called once per test.
     *
     * @return The HTTP client to use for each test.
     */
    protected abstract HttpClient createHttpClient();

    /**
     * Get the dynamic port the WireMock server is using to properly route the request.
     *
     * @return The HTTP port WireMock is using.
     */
    protected abstract int getWireMockPort();

    /**
     * Tests that a response without a byte order mark or a 'Content-Type' header encodes using UTF-8.
     */
    @Test
    public void plainResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        StepVerifier.create(sendRequest(PLAIN_RESPONSE))
            .assertNext(actual -> assertEquals(expected, actual))
            .verifyComplete();
    }

    /**
     * Tests that a response with a 'Content-Type' header encodes using the specified charset.
     */
    @Test
    public void headerResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_16BE);

        StepVerifier.create(sendRequest(HEADER_RESPONSE))
            .assertNext(actual -> assertEquals(expected, actual))
            .verifyComplete();
    }

    /**
     * Tests that a response with a 'Content-Type' containing an invalid or unsupported charset encodes using UTF-8.
     */
    @Test
    public void invalidHeaderResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        StepVerifier.create(sendRequest(INVALID_HEADER_RESPONSE))
            .assertNext(actual -> assertEquals(expected, actual))
            .verifyComplete();
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @Test
    public void utf8BomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        StepVerifier.create(sendRequest(UTF_8_BOM_RESPONSE))
            .assertNext(actual -> assertEquals(expected, actual))
            .verifyComplete();
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @Test
    public void utf16BeBomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_16BE);

        StepVerifier.create(sendRequest(UTF_16BE_BOM_RESPONSE))
            .assertNext(actual -> assertEquals(expected, actual))
            .verifyComplete();
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @Test
    public void utf16LeBomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_16LE);

        StepVerifier.create(sendRequest(UTF_16LE_BOM_RESPONSE))
            .assertNext(actual -> assertEquals(expected, actual))
            .verifyComplete();
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @Test
    public void utf32BeBomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, Charset.forName("UTF-32BE"));

        StepVerifier.create(sendRequest(UTF_32BE_BOM_RESPONSE))
            .assertNext(actual -> assertEquals(expected, actual))
            .verifyComplete();
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @Test
    public void utf32LeBomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, Charset.forName("UTF-32LE"));

        StepVerifier.create(sendRequest(UTF_32LE_BOM_RESPONSE))
            .assertNext(actual -> assertEquals(expected, actual))
            .verifyComplete();
    }

    /**
     * Tests that a response with a byte order marker and 'Content-Type' header will defer to using the BOM encoding.
     */
    @Test
    public void bomWithSameHeader() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        StepVerifier.create(sendRequest(BOM_WITH_SAME_HEADER))
            .assertNext(actual -> assertEquals(expected, actual))
            .verifyComplete();
    }

    /**
     * Tests that a response with a byte order marker and 'Content-Type' header will defer to using the BOM encoding.
     */
    @Test
    public void bomWithDifferentHeader() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        StepVerifier.create(sendRequest(BOM_WITH_DIFFERENT_HEADER))
            .assertNext(actual -> assertEquals(expected, actual))
            .verifyComplete();
    }

    private Mono<String> sendRequest(String requestPath) {
        return createHttpClient()
            .send(new HttpRequest(HttpMethod.GET, REQUEST_HOST + ":" + getWireMockPort() + "/" + requestPath))
            .flatMap(HttpResponse::getBodyAsString);
    }
}
