// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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
    private static final String ECHO_RESPONSE = "echo";

    private static final Random RANDOM = new Random();

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

    /**
     * Tests that send random bytes in various forms to an endpoint that echoes bytes back to sender.
     * @param requestBody The BinaryData that contains random bytes.
     * @param expectedResponseBody The expected bytes in the echo response.
     * @throws MalformedURLException If it can't parse URL
     */
    @ParameterizedTest
    @MethodSource("getBinaryDataBodyVariants")
    public void canSendBinaryData(BinaryData requestBody, byte[] expectedResponseBody)
        throws MalformedURLException {
        HttpRequest request = new HttpRequest(
            HttpMethod.PUT,
            new URL(REQUEST_HOST + ":" + getWireMockPort() + "/" + ECHO_RESPONSE),
            new HttpHeaders(),
            requestBody);

        StepVerifier.create(createHttpClient()
            .send(request)
            .flatMap(HttpResponse::getBodyAsByteArray))
            .assertNext(responseBytes -> assertArrayEquals(expectedResponseBody, responseBytes))
            .verifyComplete();
    }

    private static Stream<Arguments> getBinaryDataBodyVariants() {
        return Stream.of(1, 2, 10, 127, 1024, 1024 + 157, 8 * 1024 + 3, 10 * 1024 * 1024 + 13)
            .flatMap(size -> {
                try {
                    byte[] bytes = new byte[size];
                    RANDOM.nextBytes(bytes);

                    BinaryData byteArrayData = BinaryData.fromBytes(bytes);

                    String randomString = new String(bytes, StandardCharsets.UTF_8);
                    byte[] randomStringBytes = randomString.getBytes(StandardCharsets.UTF_8);
                    BinaryData stringBinaryData = BinaryData.fromString(randomString);

                    BinaryData streamData = BinaryData.fromStream(new ByteArrayInputStream(bytes));

                    List<ByteBuffer> bufferList = new ArrayList<>();
                    int bufferSize = 10;
                    for (int startIndex = 0; startIndex < bytes.length; startIndex += bufferSize) {
                        bufferList.add(
                            ByteBuffer.wrap(
                                bytes, startIndex, Math.min(bytes.length - startIndex, bufferSize)));
                    }
                    BinaryData fluxBinaryData = BinaryData.fromFlux(Flux.fromIterable(bufferList),
                        null, false).block();

                    BinaryData objectBinaryData = BinaryData.fromObject(bytes, new ByteArraySerializer());


                    Path wholeFile = Files.createTempFile("http-client-tests", null);
                    wholeFile.toFile().deleteOnExit();
                    Files.write(wholeFile, bytes);
                    BinaryData fileData = BinaryData.fromFile(wholeFile);


                    return Stream.of(
                        Arguments.of(Named.named("byte[]", byteArrayData), Named.named("" + size, bytes)),
                        Arguments.of(Named.named("String", stringBinaryData),
                            Named.named("" + randomStringBytes.length, randomStringBytes)),
                        Arguments.of(Named.named("InputStream",
                            streamData), Named.named("" + size, bytes)),
                        Arguments.of(Named.named("Flux", fluxBinaryData), Named.named("" + size, bytes)),
                        Arguments.of(Named.named("Object", objectBinaryData), Named.named("" + size, bytes)),
                        Arguments.of(Named.named("File", fileData), Named.named("" + size, bytes))
                    );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    private Mono<String> sendRequest(String requestPath) {
        return createHttpClient()
            .send(new HttpRequest(HttpMethod.GET, REQUEST_HOST + ":" + getWireMockPort() + "/" + requestPath))
            .flatMap(HttpResponse::getBodyAsString);
    }

    private static class ByteArraySerializer implements ObjectSerializer {
        @Override
        public <T> T deserialize(InputStream stream, TypeReference<T> typeReference) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public <T> Mono<T> deserializeAsync(InputStream stream, TypeReference<T> typeReference) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void serialize(OutputStream stream, Object value) {
            try {
                stream.write((byte[]) value);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Mono<Void> serializeAsync(OutputStream stream, Object value) {
            return Mono.fromRunnable(() -> serialize(stream, value));
        }
    }
}
