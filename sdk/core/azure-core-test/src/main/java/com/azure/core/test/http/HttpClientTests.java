// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.junit.extensions.SyncAsyncExtension;
import com.azure.core.test.junit.extensions.annotation.SyncAsyncTest;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Generic test suite for {@link HttpClient HttpClients}.
 */
public abstract class HttpClientTests {
    private static final ClientLogger LOGGER = new ClientLogger(HttpClientTests.class);
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
    private static final String ECHO_REQUEST = "echoRequest";

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
    @SyncAsyncTest
    public void plainResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        SyncAsyncExtension.execute(
            () -> {
                String actual = sendRequestSync(PLAIN_RESPONSE);
                assertEquals(expected, actual);
            },
            () -> StepVerifier.create(sendRequest(PLAIN_RESPONSE))
                .assertNext(actual -> assertEquals(expected, actual))
                .verifyComplete());
    }

    /**
     * Tests that a response with a 'Content-Type' header encodes using the specified charset.
     */
    @SyncAsyncTest
    public void headerResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_16BE);

        SyncAsyncExtension.execute(
            () -> {
                String actual = sendRequestSync(HEADER_RESPONSE);
                assertEquals(expected, actual);
            },
            () -> StepVerifier.create(sendRequest(HEADER_RESPONSE))
                .assertNext(actual -> assertEquals(expected, actual))
                .verifyComplete());
    }

    /**
     * Tests that a response with a 'Content-Type' containing an invalid or unsupported charset encodes using UTF-8.
     */
    @SyncAsyncTest
    public void invalidHeaderResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        SyncAsyncExtension.execute(
            () -> {
                String actual = sendRequestSync(INVALID_HEADER_RESPONSE);
                assertEquals(expected, actual);
            },
            () -> StepVerifier.create(sendRequest(INVALID_HEADER_RESPONSE))
                .assertNext(actual -> assertEquals(expected, actual))
                .verifyComplete());
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @SyncAsyncTest
    public void utf8BomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        SyncAsyncExtension.execute(
            () -> {
                String actual = sendRequestSync(UTF_8_BOM_RESPONSE);
                assertEquals(expected, actual);
            },
            () -> StepVerifier.create(sendRequest(UTF_8_BOM_RESPONSE))
                .assertNext(actual -> assertEquals(expected, actual))
                .verifyComplete());
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @SyncAsyncTest
    public void utf16BeBomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_16BE);

        SyncAsyncExtension.execute(
            () -> {
                String actual = sendRequestSync(UTF_16BE_BOM_RESPONSE);
                assertEquals(expected, actual);
            },
            () -> StepVerifier.create(sendRequest(UTF_16BE_BOM_RESPONSE))
                .assertNext(actual -> assertEquals(expected, actual))
                .verifyComplete());
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @SyncAsyncTest
    public void utf16LeBomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_16LE);

        SyncAsyncExtension.execute(
            () -> {
                String actual = sendRequestSync(UTF_16LE_BOM_RESPONSE);
                assertEquals(expected, actual);
            },
            () -> StepVerifier.create(sendRequest(UTF_16LE_BOM_RESPONSE))
                .assertNext(actual -> assertEquals(expected, actual))
                .verifyComplete());
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @SyncAsyncTest
    public void utf32BeBomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, Charset.forName("UTF-32BE"));

        SyncAsyncExtension.execute(
            () -> {
                String actual = sendRequestSync(UTF_32BE_BOM_RESPONSE);
                assertEquals(expected, actual);
            },
            () -> StepVerifier.create(sendRequest(UTF_32BE_BOM_RESPONSE))
                .assertNext(actual -> assertEquals(expected, actual))
                .verifyComplete());
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @SyncAsyncTest
    public void utf32LeBomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, Charset.forName("UTF-32LE"));

        SyncAsyncExtension.execute(
            () -> {
                String actual = sendRequestSync(UTF_32LE_BOM_RESPONSE);
                assertEquals(expected, actual);
            },
            () -> StepVerifier.create(sendRequest(UTF_32LE_BOM_RESPONSE))
                .assertNext(actual -> assertEquals(expected, actual))
                .verifyComplete());
    }

    /**
     * Tests that a response with a byte order marker and 'Content-Type' header will defer to using the BOM encoding.
     */
    @SyncAsyncTest
    public void bomWithSameHeader() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        SyncAsyncExtension.execute(
            () -> {
                String actual = sendRequestSync(BOM_WITH_SAME_HEADER);
                assertEquals(expected, actual);
            },
            () -> StepVerifier.create(sendRequest(BOM_WITH_SAME_HEADER))
                .assertNext(actual -> assertEquals(expected, actual))
                .verifyComplete());
    }

    /**
     * Tests that a response with a byte order marker and 'Content-Type' header will defer to using the BOM encoding.
     */
    @SyncAsyncTest
    public void bomWithDifferentHeader() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        SyncAsyncExtension.execute(
            () -> {
                String actual = sendRequestSync(BOM_WITH_DIFFERENT_HEADER);
                assertEquals(expected, actual);
            },
            () -> StepVerifier.create(sendRequest(BOM_WITH_DIFFERENT_HEADER))
                .assertNext(actual -> assertEquals(expected, actual))
                .verifyComplete());
    }

    /**
     * Tests that a request is echoed back.
     */
    @SyncAsyncTest
    public void stringRequest() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        BinaryData content = BinaryData.fromString(expected);

        SyncAsyncExtension.execute(
            () -> {
                String actual = sendEchoRequestSync(content);
                assertEquals(expected, actual);
            },
            () -> StepVerifier.create(sendEchoRequest(content))
                .assertNext(actual -> assertEquals(expected, actual))
                .verifyComplete());
    }

    /**
     * Tests that a request is echoed back.
     */
    @SyncAsyncTest
    public void byteArrayRequest() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        BinaryData content = BinaryData.fromBytes(EXPECTED_RETURN_BYTES);

        SyncAsyncExtension.execute(
            () -> {
                String actual = sendEchoRequestSync(content);
                assertEquals(expected, actual);
            },
            () -> StepVerifier.create(sendEchoRequest(content))
                .assertNext(actual -> assertEquals(expected, actual))
                .verifyComplete());
    }

    /**
     * Tests that a request is echoed back.
     */
    @SyncAsyncTest
    public void streamRequest() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        BinaryData content = BinaryData.fromStream(new ByteArrayInputStream(EXPECTED_RETURN_BYTES));

        SyncAsyncExtension.execute(
            () -> {
                String actual = sendEchoRequestSync(content);
                assertEquals(expected, actual);
            },
            () -> StepVerifier.create(sendEchoRequest(content))
                .assertNext(actual -> assertEquals(expected, actual))
                .verifyComplete());
    }

    /**
     * Tests that a request is echoed back.
     */
    @SyncAsyncTest
    public void fluxRequest() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        BinaryData content = BinaryData.fromFlux(Flux.just(ByteBuffer.wrap(EXPECTED_RETURN_BYTES))).block();

        SyncAsyncExtension.execute(
            () -> {
                String actual = sendEchoRequestSync(content);
                assertEquals(expected, actual);
            },
            () -> StepVerifier.create(sendEchoRequest(content))
                .assertNext(actual -> assertEquals(expected, actual))
                .verifyComplete());
    }

    /**
     * Tests that a string request is echoed back.
     * @throws IOException IOException.
     */
    @SyncAsyncTest
    public void fileRequest() throws IOException {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        File tempFile = File.createTempFile("fileRequestTest", null);
        tempFile.deleteOnExit();
        Files.write(tempFile.toPath(), EXPECTED_RETURN_BYTES);

        BinaryData content = BinaryData.fromFile(tempFile.toPath());

        SyncAsyncExtension.execute(
            () -> {
                String actual = sendEchoRequestSync(content);
                assertEquals(expected, actual);
            },
            () -> StepVerifier.create(sendEchoRequest(content))
                .assertNext(actual -> assertEquals(expected, actual))
                .verifyComplete());
    }

    private Mono<String> sendRequest(String requestPath) {
        return createHttpClient()
            .send(new HttpRequest(HttpMethod.GET, REQUEST_HOST + ":" + getWireMockPort() + "/" + requestPath))
            .flatMap(HttpResponse::getBodyAsString);
    }

    private String sendRequestSync(String requestPath) {
        HttpResponse response = createHttpClient()
            .sendSynchronously(
                new HttpRequest(HttpMethod.GET, REQUEST_HOST + ":" + getWireMockPort() + "/" + requestPath),
                Context.NONE);
        // TODO (kasobol-msft) response.getContent().toString() doesn't respect the encoding. How do we do this?
        return response.getBodyAsString().block();
    }

    private Mono<String> sendEchoRequest(BinaryData content) {
        try {
            return createHttpClient()
                .send(new HttpRequest(
                    HttpMethod.PUT,
                    new URL(REQUEST_HOST + ":" + getWireMockPort() + "/" + ECHO_REQUEST),
                    new HttpHeaders(),
                    content))
                .flatMap(HttpResponse::getBodyAsString);
        } catch (MalformedURLException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    private String sendEchoRequestSync(BinaryData content) {
        try {
            HttpResponse response = createHttpClient()
                .sendSynchronously(new HttpRequest(
                    HttpMethod.PUT,
                    new URL(REQUEST_HOST + ":" + getWireMockPort() + "/" + ECHO_REQUEST),
                    new HttpHeaders(),
                    content), Context.NONE);
            // TODO (kasobol-msft) response.getContent().toString() doesn't respect the encoding. How do we do this?
            return response.getBodyAsString().block();
        } catch (MalformedURLException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }
}
