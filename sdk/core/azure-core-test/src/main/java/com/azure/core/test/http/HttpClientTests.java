// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.Contexts;
import com.azure.core.util.IOUtils;
import com.azure.core.util.ProgressReporter;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.Channels;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Generic test suite for {@link HttpClient HttpClients}.
 */
public abstract class HttpClientTests {
    private static final ClientLogger LOGGER = new ClientLogger(HttpClientTests.class);

    private static final String REQUEST_HOST = "localhost";
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
    protected static final String ECHO_RESPONSE = "echo";

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
     * Get a flag indicating if communication should be secured or not (https or http).
     * @return A flag indicating if communication should be secured or not (https or http).
     */
    protected boolean isSecure() {
        return false;
    }

    /**
     * Tests that a response without a byte order mark or a 'Content-Type' header encodes using UTF-8.
     */
    @SyncAsyncTest
    public void plainResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        String actual = SyncAsyncExtension.execute(
            () -> sendRequestSync(PLAIN_RESPONSE),
            () -> sendRequest(PLAIN_RESPONSE)
        );

        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a 'Content-Type' header encodes using the specified charset.
     */
    @SyncAsyncTest
    public void headerResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_16BE);

        String actual = SyncAsyncExtension.execute(
            () -> sendRequestSync(HEADER_RESPONSE),
            () -> sendRequest(HEADER_RESPONSE)
        );

        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a 'Content-Type' containing an invalid or unsupported charset encodes using UTF-8.
     */
    @SyncAsyncTest
    public void invalidHeaderResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        String actual = SyncAsyncExtension.execute(
            () -> sendRequestSync(INVALID_HEADER_RESPONSE),
            () -> sendRequest(INVALID_HEADER_RESPONSE)
        );

        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @SyncAsyncTest
    public void utf8BomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        String actual = SyncAsyncExtension.execute(
            () -> sendRequestSync(UTF_8_BOM_RESPONSE),
            () -> sendRequest(UTF_8_BOM_RESPONSE)
        );

        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @SyncAsyncTest
    public void utf16BeBomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_16BE);

        String actual = SyncAsyncExtension.execute(
            () -> sendRequestSync(UTF_16BE_BOM_RESPONSE),
            () -> sendRequest(UTF_16BE_BOM_RESPONSE)
        );

        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @SyncAsyncTest
    public void utf16LeBomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_16LE);

        String actual = SyncAsyncExtension.execute(
            () -> sendRequestSync(UTF_16LE_BOM_RESPONSE),
            () -> sendRequest(UTF_16LE_BOM_RESPONSE)
        );

        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @SyncAsyncTest
    public void utf32BeBomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, Charset.forName("UTF-32BE"));

        String actual = SyncAsyncExtension.execute(
            () -> sendRequestSync(UTF_32BE_BOM_RESPONSE),
            () -> sendRequest(UTF_32BE_BOM_RESPONSE)
        );

        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @SyncAsyncTest
    public void utf32LeBomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, Charset.forName("UTF-32LE"));

        String actual = SyncAsyncExtension.execute(
            () -> sendRequestSync(UTF_32LE_BOM_RESPONSE),
            () -> sendRequest(UTF_32LE_BOM_RESPONSE)
        );

        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a byte order marker and 'Content-Type' header will defer to using the BOM encoding.
     */
    @SyncAsyncTest
    public void bomWithSameHeader() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        String actual = SyncAsyncExtension.execute(
            () -> sendRequestSync(BOM_WITH_SAME_HEADER),
            () -> sendRequest(BOM_WITH_SAME_HEADER)
        );

        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a byte order marker and 'Content-Type' header will defer to using the BOM encoding.
     */
    @SyncAsyncTest
    public void bomWithDifferentHeader() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        String actual = SyncAsyncExtension.execute(
            () -> sendRequestSync(BOM_WITH_DIFFERENT_HEADER),
            () -> sendRequest(BOM_WITH_DIFFERENT_HEADER)
        );

        assertEquals(expected, actual);
    }

    /**
     * Tests that unbuffered response body can be accessed.
     * @throws IOException When IO fails.
     */
    @SyncAsyncTest
    public void canAccessResponseBody() throws IOException {
        BinaryData requestBody = BinaryData.fromString("test body");
        HttpRequest request = new HttpRequest(
            HttpMethod.PUT,
            getRequestUrl(ECHO_RESPONSE),
            new HttpHeaders(),
            requestBody);

        Supplier<HttpResponse> responseSupplier = () -> SyncAsyncExtension.execute(
            () -> createHttpClient().sendSync(request, Context.NONE),
            () -> createHttpClient().send(request)
        );

        assertEquals(requestBody.toString(), responseSupplier.get().getBodyAsString().block());

        assertArrayEquals(requestBody.toBytes(), responseSupplier.get().getBodyAsByteArray().block());

        assertArrayEquals(requestBody.toBytes(), responseSupplier.get().getBodyAsBinaryData().toBytes());

        assertArrayEquals(requestBody.toBytes(), responseSupplier.get().getBodyAsInputStream()
            .map(s -> BinaryData.fromStream(s).toBytes()).block());

        assertArrayEquals(requestBody.toBytes(), BinaryData.fromFlux(responseSupplier.get().getBody()).map(BinaryData::toBytes).block());

        assertArrayEquals(requestBody.toBytes(), getResponseBytesViaWritableChannel(responseSupplier.get()));

        assertArrayEquals(requestBody.toBytes(), getResponseBytesViaAsynchronousChannel(responseSupplier.get()));

    }

    /**
     * Tests that client returns buffered response if requested via azure-eagerly-read-response Context flag.
     */
    @SyncAsyncTest
    public void shouldBufferResponse() {
        HttpRequest request = new HttpRequest(
            HttpMethod.PUT,
            getRequestUrl(ECHO_RESPONSE),
            new HttpHeaders(),
            BinaryData.fromString("test body"));

        Context context = Context.NONE.addData("azure-eagerly-read-response", true);

        HttpResponse response = SyncAsyncExtension.execute(
            () -> createHttpClient().sendSync(request, context),
            () -> createHttpClient().send(request, context)
        );

        // Buffering buffered response is identity transformation.
        HttpResponse bufferedResponse = response.buffer();
        assertSame(response, bufferedResponse);
    }

    /**
     * Tests that buffered response is indeed buffered, i.e. content can be accessed many times.
     * @throws IOException When IO fails.
     */
    @SyncAsyncTest
    public void bufferedResponseCanBeReadMultipleTimes() throws IOException {
        BinaryData requestBody = BinaryData.fromString("test body");
        HttpRequest request = new HttpRequest(
            HttpMethod.PUT,
            getRequestUrl(ECHO_RESPONSE),
            new HttpHeaders(),
            requestBody);

        Context context = Context.NONE.addData("azure-eagerly-read-response", true);

        HttpResponse response = SyncAsyncExtension.execute(
            () -> createHttpClient().sendSync(request, context),
            () -> createHttpClient().send(request, context)
        );

        // Read response twice using all accessors.
        assertEquals(requestBody.toString(), response.getBodyAsString().block());
        assertEquals(requestBody.toString(), response.getBodyAsString().block());

        assertArrayEquals(requestBody.toBytes(), response.getBodyAsByteArray().block());
        assertArrayEquals(requestBody.toBytes(), response.getBodyAsByteArray().block());

        assertArrayEquals(requestBody.toBytes(), response.getBodyAsBinaryData().toBytes());
        assertArrayEquals(requestBody.toBytes(), response.getBodyAsBinaryData().toBytes());

        assertArrayEquals(requestBody.toBytes(), response.getBodyAsInputStream()
            .map(s -> BinaryData.fromStream(s).toBytes()).block());
        assertArrayEquals(requestBody.toBytes(), response.getBodyAsInputStream()
            .map(s -> BinaryData.fromStream(s).toBytes()).block());

        assertArrayEquals(requestBody.toBytes(), BinaryData.fromFlux(response.getBody()).map(BinaryData::toBytes).block());
        assertArrayEquals(requestBody.toBytes(), BinaryData.fromFlux(response.getBody()).map(BinaryData::toBytes).block());

        assertArrayEquals(requestBody.toBytes(), getResponseBytesViaWritableChannel(response));
        assertArrayEquals(requestBody.toBytes(), getResponseBytesViaWritableChannel(response));

        assertArrayEquals(requestBody.toBytes(), getResponseBytesViaAsynchronousChannel(response));
        assertArrayEquals(requestBody.toBytes(), getResponseBytesViaAsynchronousChannel(response));
    }

    /**
     * Tests that send random bytes in various forms to an endpoint that echoes bytes back to sender.
     * @param requestBody The BinaryData that contains random bytes.
     * @param expectedResponseBody The expected bytes in the echo response.
     */
    @ParameterizedTest
    @MethodSource("getBinaryDataBodyVariants")
    public void canSendBinaryData(BinaryData requestBody, byte[] expectedResponseBody) {
        HttpRequest request = new HttpRequest(
            HttpMethod.PUT,
            getRequestUrl(ECHO_RESPONSE),
            new HttpHeaders(),
            requestBody);

        StepVerifier.create(createHttpClient()
            .send(request)
            .flatMap(HttpResponse::getBodyAsByteArray))
            .assertNext(responseBytes -> assertArrayEquals(expectedResponseBody, responseBytes))
            .verifyComplete();
    }

    /**
     * Tests that send random bytes in various forms to an endpoint that echoes bytes back to sender.
     * @param requestBody The BinaryData that contains random bytes.
     * @param expectedResponseBody The expected bytes in the echo response.
     */
    @ParameterizedTest
    @MethodSource("getBinaryDataBodyVariants")
    public void canSendBinaryDataSync(BinaryData requestBody, byte[] expectedResponseBody) {
        HttpRequest request = new HttpRequest(
            HttpMethod.PUT,
            getRequestUrl(ECHO_RESPONSE),
            new HttpHeaders(),
            requestBody);

        HttpResponse httpResponse = createHttpClient()
            .sendSync(request, Context.NONE);

        byte[] responseBytes = httpResponse
            .getBodyAsByteArray()
            .block();

        assertArrayEquals(expectedResponseBody, responseBytes);
    }

    /**
     * Tests that send random bytes in various forms to an endpoint that echoes bytes back to sender.
     * @param requestBody The BinaryData that contains random bytes.
     * @param expectedResponseBody The expected bytes in the echo response.
     */
    @ParameterizedTest
    @MethodSource("getBinaryDataBodyVariants")
    public void canSendBinaryDataWithProgressReporting(BinaryData requestBody, byte[] expectedResponseBody) {
        HttpRequest request = new HttpRequest(
            HttpMethod.PUT,
            getRequestUrl(ECHO_RESPONSE),
            new HttpHeaders(),
            requestBody);

        AtomicLong progress = new AtomicLong();
        Context context = Contexts.empty()
            .setHttpRequestProgressReporter(
                ProgressReporter.withProgressListener(progress::set))
            .getContext();

        StepVerifier.create(createHttpClient()
                .send(request, context)
                .flatMap(HttpResponse::getBodyAsByteArray))
            .assertNext(responseBytes -> assertArrayEquals(expectedResponseBody, responseBytes))
            .verifyComplete();

        assertEquals(expectedResponseBody.length, progress.intValue());
    }

    /**
     * Tests that send random bytes in various forms to an endpoint that echoes bytes back to sender.
     * @param requestBody The BinaryData that contains random bytes.
     * @param expectedResponseBody The expected bytes in the echo response.
     */
    @ParameterizedTest
    @MethodSource("getBinaryDataBodyVariants")
    public void canSendBinaryDataWithProgressReportingSync(BinaryData requestBody, byte[] expectedResponseBody) {
        HttpRequest request = new HttpRequest(
            HttpMethod.PUT,
            getRequestUrl(ECHO_RESPONSE),
            new HttpHeaders(),
            requestBody);

        AtomicLong progress = new AtomicLong();
        Context context = Contexts.empty()
            .setHttpRequestProgressReporter(
                ProgressReporter.withProgressListener(progress::set))
            .getContext();

        HttpResponse httpResponse = createHttpClient()
            .sendSync(request, context);

        byte[] responseBytes = httpResponse
            .getBodyAsByteArray()
            .block();

        assertArrayEquals(expectedResponseBody, responseBytes);
        assertEquals(expectedResponseBody.length, progress.intValue());
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
                    int bufferSize = 113;
                    for (int startIndex = 0; startIndex < bytes.length; startIndex += bufferSize) {
                        bufferList.add(
                            ByteBuffer.wrap(
                                bytes, startIndex, Math.min(bytes.length - startIndex, bufferSize)));
                    }
                    BinaryData fluxBinaryData = BinaryData.fromFlux(
                        Flux.fromIterable(bufferList)
                            .map(ByteBuffer::duplicate),
                        null, false).block();

                    BinaryData fluxBinaryDataWithLength = BinaryData.fromFlux(
                        Flux.fromIterable(bufferList)
                            .map(ByteBuffer::duplicate),
                        size.longValue(), false).block();

                    BinaryData asyncFluxBinaryData = BinaryData.fromFlux(
                        Flux.fromIterable(bufferList)
                            .map(ByteBuffer::duplicate)
                            .delayElements(Duration.ofNanos(10))
                            .flatMapSequential(
                                buffer -> Mono.delay(Duration.ofNanos(10)).map(i -> buffer)
                            ),
                        null, false).block();

                    BinaryData asyncFluxBinaryDataWithLength = BinaryData.fromFlux(
                        Flux.fromIterable(bufferList)
                            .map(ByteBuffer::duplicate)
                            .delayElements(Duration.ofNanos(10))
                            .flatMapSequential(
                                buffer -> Mono.delay(Duration.ofNanos(10)).map(i -> buffer)
                            ),
                        size.longValue(), false).block();

                    BinaryData objectBinaryData = BinaryData.fromObject(bytes, new ByteArraySerializer());


                    Path wholeFile = Files.createTempFile("http-client-tests", null);
                    wholeFile.toFile().deleteOnExit();
                    Files.write(wholeFile, bytes);
                    BinaryData fileData = BinaryData.fromFile(wholeFile);

                    Path sliceFile = Files.createTempFile("http-client-tests", null);
                    sliceFile.toFile().deleteOnExit();
                    Files.write(sliceFile, new byte[size], StandardOpenOption.APPEND);
                    Files.write(sliceFile, bytes, StandardOpenOption.APPEND);
                    Files.write(sliceFile, new byte[size], StandardOpenOption.APPEND);
                    BinaryData sliceFileData = BinaryData.fromFile(sliceFile, Long.valueOf(size), Long.valueOf(size));


                    return Stream.of(
                        Arguments.of(Named.named("byte[]", byteArrayData), Named.named("" + size, bytes)),
                        Arguments.of(Named.named("String", stringBinaryData),
                            Named.named("" + randomStringBytes.length, randomStringBytes)),
                        Arguments.of(Named.named("InputStream",
                            streamData), Named.named("" + size, bytes)),
                        Arguments.of(Named.named("Flux", fluxBinaryData), Named.named("" + size, bytes)),
                        Arguments.of(Named.named("Flux with length", fluxBinaryDataWithLength), Named.named("" + size, bytes)),
                        Arguments.of(Named.named("async Flux", asyncFluxBinaryData), Named.named("" + size, bytes)),
                        Arguments.of(Named.named("async Flux with length", asyncFluxBinaryDataWithLength), Named.named("" + size, bytes)),
                        Arguments.of(Named.named("Object", objectBinaryData), Named.named("" + size, bytes)),
                        Arguments.of(Named.named("File", fileData), Named.named("" + size, bytes)),
                        Arguments.of(Named.named("File slice", sliceFileData), Named.named("" + size, bytes))
                    );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    private Mono<String> sendRequest(String requestPath) {
        return createHttpClient()
            .send(new HttpRequest(HttpMethod.GET, getRequestUrl(requestPath)))
            .flatMap(HttpResponse::getBodyAsString);
    }

    private String sendRequestSync(String requestPath) {
        HttpResponse httpResponse = createHttpClient()
            .sendSync(new HttpRequest(HttpMethod.GET, getRequestUrl(requestPath)), Context.NONE);
        return httpResponse
            .getBodyAsString()
            .block();
    }

    /**
     * Gets the request URL for given path.
     * @param requestPath The path.
     * @return The request URL for given path.
     * @throws RuntimeException if url is invalid.
     */
    protected URL getRequestUrl(String requestPath) {
        try {
            String prefix = isSecure() ? "https://" : "http://";
            return new URL(prefix + REQUEST_HOST + ":" + getWireMockPort() + "/" + requestPath);
        } catch (MalformedURLException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    private byte[] getResponseBytesViaWritableChannel(HttpResponse response) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        response.writeBodyTo(Channels.newChannel(byteArrayOutputStream));
        return byteArrayOutputStream.toByteArray();
    }

    private byte[] getResponseBytesViaAsynchronousChannel(HttpResponse response) {
        try {
            Path tempFile = Files.createTempFile("httpclienttestsasyncchannel", null);
            try (AsynchronousByteChannel channel = IOUtils.toAsynchronousByteChannel(
                AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE), 0)) {
                response.writeBodyToAsync(channel).block();
            }
            return Files.readAllBytes(tempFile);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }
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
