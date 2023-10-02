// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.http;

import com.typespec.core.annotation.BodyParam;
import com.typespec.core.annotation.Delete;
import com.typespec.core.annotation.ExpectedResponses;
import com.typespec.core.annotation.FormParam;
import com.typespec.core.annotation.Get;
import com.typespec.core.annotation.Head;
import com.typespec.core.annotation.HeaderParam;
import com.typespec.core.annotation.Headers;
import com.typespec.core.annotation.Host;
import com.typespec.core.annotation.HostParam;
import com.typespec.core.annotation.Patch;
import com.typespec.core.annotation.PathParam;
import com.typespec.core.annotation.Post;
import com.typespec.core.annotation.Put;
import com.typespec.core.annotation.QueryParam;
import com.typespec.core.annotation.ServiceInterface;
import com.typespec.core.annotation.UnexpectedResponseExceptionType;
import com.typespec.core.exception.HttpResponseException;
import com.typespec.core.exception.UnexpectedLengthException;
import com.typespec.core.http.ContentType;
import com.typespec.core.http.HttpClient;
import com.typespec.core.http.HttpHeaderName;
import com.typespec.core.http.HttpHeaders;
import com.typespec.core.http.HttpMethod;
import com.typespec.core.http.HttpPipeline;
import com.typespec.core.http.HttpPipelineBuilder;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.http.policy.HttpLogDetailLevel;
import com.typespec.core.http.policy.HttpLogOptions;
import com.typespec.core.http.policy.HttpLoggingPolicy;
import com.typespec.core.http.policy.PortPolicy;
import com.typespec.core.http.rest.RequestOptions;
import com.typespec.core.http.rest.Response;
import com.typespec.core.http.rest.ResponseBase;
import com.typespec.core.http.rest.RestProxy;
import com.typespec.core.http.rest.StreamResponse;
import com.typespec.core.test.MyRestException;
import com.typespec.core.test.SyncAsyncExtension;
import com.typespec.core.test.annotation.SyncAsyncTest;
import com.typespec.core.test.implementation.entities.HttpBinFormDataJSON;
import com.typespec.core.test.implementation.entities.HttpBinHeaders;
import com.typespec.core.test.implementation.entities.HttpBinJSON;
import com.typespec.core.test.utils.MessageDigestUtils;
import com.typespec.core.util.BinaryData;
import com.typespec.core.util.Context;
import com.typespec.core.util.Contexts;
import com.typespec.core.util.FluxUtil;
import com.typespec.core.util.ProgressReporter;
import com.typespec.core.util.UrlBuilder;
import com.typespec.core.util.io.IOUtils;
import com.typespec.core.util.logging.ClientLogger;
import com.typespec.core.util.serializer.ObjectSerializer;
import com.typespec.core.util.serializer.TypeReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

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
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Generic test suite for {@link HttpClient HttpClients}.
 */
@Execution(ExecutionMode.SAME_THREAD)
public abstract class HttpClientTests {
    private static final ClientLogger LOGGER = new ClientLogger(HttpClientTests.class);

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

    private static final byte[] EXPECTED_RETURN_BYTES = "Hello World!".getBytes(StandardCharsets.UTF_8);

    private static final String HTTP_REST_PROXY_SYNC_PROXY_ENABLE = "com.azure.core.http.restproxy.syncproxy.enable";

    /**
     * Get the HTTP client that will be used for each test. This will be called once per test.
     *
     * @return The HTTP client to use for each test.
     */
    protected abstract HttpClient createHttpClient();

    /**
     * Get the dynamic port the server is using to properly route the request.
     *
     * @return The HTTP port is using.
     * @deprecated Use {@link #getServerUri(boolean)} instead.
     */
    @Deprecated
    protected abstract int getPort();

    /**
     * Gets the dynamic URI the server is using to properly route the request.
     *
     * @param secure Flag indicating if the URI should be for a secure connection or not.
     * @return The URI the server is using.
     */
    protected abstract String getServerUri(boolean secure);

    /**
     * Get a flag indicating if communication should be secured or not (https or http).
     *
     * @return A flag indicating if communication should be secured or not (https or http).
     */
    protected boolean isSecure() {
        return false;
    }

    private String getRequestUri() {
        return getServerUri(isSecure());
    }

    private String getRequestScheme() {
        return isSecure() ? "https" : "http";
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
     *
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
        HttpRequest request = new HttpRequest(HttpMethod.PUT, getRequestUrl(ECHO_RESPONSE), new HttpHeaders(),
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
     *
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
     * Tests that eagerly converting implementation HTTP headers to azure-core HttpHeaders is done.
     */
    @SyncAsyncTest
    public void eagerlyConvertedHeadersAreHttpHeaders() {
        BinaryData requestBody = BinaryData.fromString("test body");
        HttpRequest request = new HttpRequest(HttpMethod.PUT, getRequestUrl(ECHO_RESPONSE), new HttpHeaders(),
            requestBody);

        Context context = Context.NONE.addData("azure-eagerly-convert-headers", true);

        try (HttpResponse response = SyncAsyncExtension.execute(
            () -> createHttpClient().sendSync(request, context),
            () -> createHttpClient().send(request, context))) {
            // Validate getHttpHeaders type is HttpHeaders (not instanceof)
            assertEquals(HttpHeaders.class, response.getHeaders().getClass());
        }
    }

    /**
     * Tests that send random bytes in various forms to an endpoint that echoes bytes back to sender.
     *
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
     *
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
     *
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
     *
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
                    ThreadLocalRandom.current().nextBytes(bytes);

                    BinaryData byteArrayData = BinaryData.fromBytes(bytes);

                    String randomString = new String(bytes, StandardCharsets.UTF_8);
                    byte[] randomStringBytes = randomString.getBytes(StandardCharsets.UTF_8);
                    BinaryData stringBinaryData = BinaryData.fromString(randomString);

                    BinaryData streamData = BinaryData.fromStream(new ByteArrayInputStream(bytes), (long) bytes.length);

                    List<ByteBuffer> bufferList = new ArrayList<>();
                    int bufferSize = 1023;
                    for (int startIndex = 0; startIndex < bytes.length; startIndex += bufferSize) {
                        bufferList.add(ByteBuffer.wrap(bytes, startIndex,
                            Math.min(bytes.length - startIndex, bufferSize)));
                    }

                    BinaryData fluxBinaryData = BinaryData.fromFlux(Flux.fromIterable(bufferList)
                            .map(ByteBuffer::duplicate), null, false)
                        .block();

                    BinaryData fluxBinaryDataWithLength = BinaryData.fromFlux(Flux.fromIterable(bufferList)
                            .map(ByteBuffer::duplicate), size.longValue(), false)
                        .block();

                    BinaryData asyncFluxBinaryData = BinaryData.fromFlux(Flux.fromIterable(bufferList)
                            .map(ByteBuffer::duplicate)
                            .delayElements(Duration.ofNanos(10)), null, false)
                        .block();

                    BinaryData asyncFluxBinaryDataWithLength = BinaryData.fromFlux(Flux.fromIterable(bufferList)
                            .map(ByteBuffer::duplicate)
                            .delayElements(Duration.ofNanos(10)), size.longValue(), false)
                        .block();

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
                        Arguments.of(Named.named("byte[]", byteArrayData), Named.named(String.valueOf(size), bytes)),
                        Arguments.of(Named.named("String", stringBinaryData),
                            Named.named(String.valueOf(randomStringBytes.length), randomStringBytes)),
                        Arguments.of(Named.named("InputStream",
                            streamData), Named.named(String.valueOf(size), bytes)),
                        Arguments.of(Named.named("Flux", fluxBinaryData), Named.named(String.valueOf(size), bytes)),
                        Arguments.of(Named.named("Flux with length", fluxBinaryDataWithLength), Named.named(String.valueOf(size), bytes)),
                        Arguments.of(Named.named("async Flux", asyncFluxBinaryData), Named.named(String.valueOf(size), bytes)),
                        Arguments.of(Named.named("async Flux with length", asyncFluxBinaryDataWithLength), Named.named(String.valueOf(size), bytes)),
                        Arguments.of(Named.named("Object", objectBinaryData), Named.named(String.valueOf(size), bytes)),
                        Arguments.of(Named.named("File", fileData), Named.named(String.valueOf(size), bytes)),
                        Arguments.of(Named.named("File slice", sliceFileData), Named.named(String.valueOf(size), bytes))
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
     *
     * @param requestPath The path.
     * @return The request URL for given path.
     * @throws RuntimeException if url is invalid.
     */
    protected URL getRequestUrl(String requestPath) {
        try {
            return UrlBuilder.parse(getServerUri(isSecure()) + "/" + requestPath).toUrl();
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

    @Host("{url}")
    @ServiceInterface(name = "Service1")
    private interface Service1 {
        @Get("bytes/100")
        @ExpectedResponses({200})
        byte[] getByteArray(@HostParam("url") String url);

        @Get("bytes/100")
        @ExpectedResponses({200})
        Mono<byte[]> getByteArrayAsync(@HostParam("url") String url);

        @Get("bytes/100")
        Mono<byte[]> getByteArrayAsyncWithNoExpectedResponses(@HostParam("url") String url);
    }

    /**
     * Tests that the response body is correctly returned as a byte array.
     */
    @Test
    public void syncRequestWithByteArrayReturnType() {
        final byte[] result = createService(Service1.class).getByteArray(getRequestUri());

        assertNotNull(result);
        assertEquals(100, result.length);
    }

    /**
     * Tests that the response body is correctly returned as a byte array.
     */
    @Test
    public void asyncRequestWithByteArrayReturnType() {
        StepVerifier.create(createService(Service1.class).getByteArrayAsync(getRequestUri()))
            .assertNext(bytes -> assertEquals(100, bytes.length))
            .verifyComplete();
    }

    /**
     * Tests that the response body is correctly returned as a byte array.
     */
    @Test
    public void getByteArrayAsyncWithNoExpectedResponses() {
        StepVerifier.create(createService(Service1.class).getByteArrayAsyncWithNoExpectedResponses(getRequestUri()))
            .assertNext(bytes -> assertEquals(100, bytes.length))
            .verifyComplete();
    }

    @Host("{scheme}://{hostName}")
    @ServiceInterface(name = "Service2")
    private interface Service2 {
        @Get("bytes/{numberOfBytes}")
        @ExpectedResponses({200})
        byte[] getByteArray(@HostParam("scheme") String scheme, @HostParam("hostName") String host,
            @PathParam("numberOfBytes") int numberOfBytes);

        @Get("bytes/{numberOfBytes}")
        @ExpectedResponses({200})
        Mono<byte[]> getByteArrayAsync(@HostParam("scheme") String scheme, @HostParam("hostName") String host,
            @PathParam("numberOfBytes") int numberOfBytes);
    }

    /**
     * Tests that the response body is correctly returned as a byte array.
     */
    @Test
    public void syncRequestWithByteArrayReturnTypeAndParameterizedHostAndPath() {
        final byte[] result = createService(Service2.class).getByteArray(getRequestScheme(), "localhost", 100);

        assertNotNull(result);
        assertEquals(result.length, 100);
    }

    /**
     * Tests that the response body is correctly returned as a byte array.
     */
    @Test
    public void asyncRequestWithByteArrayReturnTypeAndParameterizedHostAndPath() {
        StepVerifier.create(createService(Service2.class).getByteArrayAsync(getRequestScheme(), "localhost", 100))
            .assertNext(bytes -> assertEquals(100, bytes.length))
            .verifyComplete();
    }

    /**
     * Tests that the response body is correctly returned as a byte array.
     */
    @Test
    public void syncRequestWithEmptyByteArrayReturnTypeAndParameterizedHostAndPath() {
        final byte[] result = createService(Service2.class).getByteArray(getRequestScheme(), "localhost", 0);

        // If there isn't a body then for async returns Mono.empty() for sync return null.
        assertNull(result);
    }

    @Host("{url}")
    @ServiceInterface(name = "Service3")
    private interface Service3 {
        @Get("bytes/100")
        @ExpectedResponses({200})
        void getNothing(@HostParam("url") String url);

        @Get("bytes/100")
        @ExpectedResponses({200})
        Mono<Void> getNothingAsync(@HostParam("url") String url);
    }

    /**
     * Tests that a response with no return type is correctly handled.
     */
    @Test
    public void syncGetRequestWithNoReturn() {
        assertDoesNotThrow(() -> createService(Service3.class).getNothing(getRequestUri()));
    }

    /**
     * Tests that a response with no return type is correctly handled.
     */
    @Test
    public void asyncGetRequestWithNoReturn() {
        StepVerifier.create(createService(Service3.class).getNothingAsync(getRequestUri()))
            .verifyComplete();
    }

    @Host("{url}")
    @ServiceInterface(name = "Service5")
    private interface Service5 {
        @Get("anything")
        @ExpectedResponses({200})
        HttpBinJSON getAnything(@HostParam("url") String url);

        @Get("anything/with+plus")
        @ExpectedResponses({200})
        HttpBinJSON getAnythingWithPlus(@HostParam("url") String url);

        @Get("anything/{path}")
        @ExpectedResponses({200})
        HttpBinJSON getAnythingWithPathParam(@HostParam("url") String url, @PathParam("path") String pathParam);

        @Get("anything/{path}")
        @ExpectedResponses({200})
        HttpBinJSON getAnythingWithEncodedPathParam(@HostParam("url") String url,
            @PathParam(value = "path", encoded = true) String pathParam);

        @Get("anything")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> getAnythingAsync(@HostParam("url") String url);
    }

    @Test
    public void syncGetRequestWithAnything() {
        final HttpBinJSON json = createService(Service5.class).getAnything(getRequestUri());

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything", json.url());
    }

    @Test
    public void syncGetRequestWithAnythingWithPlus() {
        final HttpBinJSON json = createService(Service5.class).getAnythingWithPlus(getRequestUri());

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with+plus", json.url());
    }

    @Test
    public void syncGetRequestWithAnythingWithPathParam() {
        final HttpBinJSON json = createService(Service5.class).getAnythingWithPathParam(getRequestUri(),
            "withpathparam");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/withpathparam", json.url());
    }

    @Test
    public void syncGetRequestWithAnythingWithPathParamWithSpace() {
        final HttpBinJSON json = createService(Service5.class).getAnythingWithPathParam(getRequestUri(),
            "with path param");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with path param", json.url());
    }

    @Test
    public void syncGetRequestWithAnythingWithPathParamWithPlus() {
        final HttpBinJSON json = createService(Service5.class).getAnythingWithPathParam(getRequestUri(),
            "with+path+param");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with+path+param", json.url());
    }

    @Test
    public void syncGetRequestWithAnythingWithEncodedPathParam() {
        final HttpBinJSON json = createService(Service5.class).getAnythingWithEncodedPathParam(getRequestUri(),
            "withpathparam");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/withpathparam", json.url());
    }

    @Test
    public void syncGetRequestWithAnythingWithEncodedPathParamWithPercent20() {
        final HttpBinJSON json = createService(Service5.class).getAnythingWithEncodedPathParam(getRequestUri(),
            "with%20path%20param");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with path param", json.url());
    }

    @Test
    public void syncGetRequestWithAnythingWithEncodedPathParamWithPlus() {
        final HttpBinJSON json = createService(Service5.class).getAnythingWithEncodedPathParam(getRequestUri(),
            "with+path+param");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with+path+param", json.url());
    }

    @Test
    public void asyncGetRequestWithAnything() {
        StepVerifier.create(createService(Service5.class).getAnythingAsync(getRequestUri()))
            .assertNext(json -> assertMatchWithHttpOrHttps("localhost/anything", json.url()))
            .verifyComplete();
    }

    @Host("{url}")
    @ServiceInterface(name = "Service6")
    private interface Service6 {
        @Get("anything")
        @ExpectedResponses({200})
        HttpBinJSON getAnything(@HostParam("url") String url, @QueryParam("a") String a, @QueryParam("b") int b);

        @Get("anything")
        @ExpectedResponses({200})
        HttpBinJSON getAnythingWithEncoded(@HostParam("url") String url,
            @QueryParam(value = "a", encoded = true) String a, @QueryParam("b") int b);

        @Get("anything")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> getAnythingAsync(@HostParam("url") String url, @QueryParam("a") String a,
            @QueryParam("b") int b);
    }

    @Test
    public void syncGetRequestWithQueryParametersAndAnything() {
        final HttpBinJSON json = createService(Service6.class).getAnything(getRequestUri(), "A", 15);

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything?a=A&b=15", json.url());
    }

    @Test
    public void syncGetRequestWithQueryParametersAndAnythingWithPercent20() {
        final HttpBinJSON json = createService(Service6.class).getAnything(getRequestUri(), "A%20Z", 15);

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything?a=A%2520Z&b=15", json.url());
    }

    @Test
    public void syncGetRequestWithQueryParametersAndAnythingWithEncodedWithPercent20() {
        final HttpBinJSON json = createService(Service6.class).getAnythingWithEncoded(getRequestUri(), "x%20y", 15);

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything?a=x y&b=15", json.url());
    }

    @Test
    public void asyncGetRequestWithQueryParametersAndAnything() {
        StepVerifier.create(createService(Service6.class).getAnythingAsync(getRequestUri(), "A", 15))
            .assertNext(json -> assertMatchWithHttpOrHttps("localhost/anything?a=A&b=15", json.url()))
            .verifyComplete();
    }

    @Test
    public void syncGetRequestWithNullQueryParameter() {
        final HttpBinJSON json = createService(Service6.class).getAnything(getRequestUri(), null, 15);

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything?b=15", json.url());
    }

    @Host("{url}")
    @ServiceInterface(name = "Service7")
    private interface Service7 {
        @Get("anything")
        @ExpectedResponses({200})
        HttpBinJSON getAnything(@HostParam("url") String url, @HeaderParam("a") String a, @HeaderParam("b") int b);

        @Get("anything")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> getAnythingAsync(@HostParam("url") String url, @HeaderParam("a") String a,
            @HeaderParam("b") int b);
    }

    private static final HttpHeaderName HEADER_A = HttpHeaderName.fromString("A");
    private static final HttpHeaderName HEADER_B = HttpHeaderName.fromString("B");

    @Test
    public void syncGetRequestWithHeaderParametersAndAnythingReturn() {
        final HttpBinJSON json = createService(Service7.class).getAnything(getRequestUri(), "A", 15);

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything", json.url());
        assertNotNull(json.headers());
        final HttpHeaders headers = new HttpHeaders().setAll(json.headers());

        assertEquals("A", headers.getValue(HEADER_A));
        assertArrayEquals(new String[]{"A"}, headers.getValues(HEADER_A));

        assertEquals("15", headers.getValue(HEADER_B));
        assertArrayEquals(new String[]{"15"}, headers.getValues(HEADER_B));
    }

    @Test
    public void asyncGetRequestWithHeaderParametersAndAnything() {
        StepVerifier.create(createService(Service7.class).getAnythingAsync(getRequestUri(), "A", 15))
            .assertNext(json -> {
                assertMatchWithHttpOrHttps("localhost/anything", json.url());
                assertNotNull(json.headers());
                final HttpHeaders headers = new HttpHeaders().setAll(json.headers());

                assertEquals("A", headers.getValue(HEADER_A));
                assertArrayEquals(new String[]{"A"}, headers.getValues(HEADER_A));

                assertEquals("15", headers.getValue(HEADER_B));
                assertArrayEquals(new String[]{"15"}, headers.getValues(HEADER_B));
            })
            .verifyComplete();
    }

    @Test
    public void syncGetRequestWithNullHeader() {
        final HttpBinJSON json = createService(Service7.class).getAnything(getRequestUri(), null, 15);

        final HttpHeaders headers = new HttpHeaders().setAll(json.headers());

        assertNull(headers.getValue(HEADER_A));
        assertArrayEquals(null, headers.getValues(HEADER_A));

        assertEquals("15", headers.getValue(HEADER_B));
        assertArrayEquals(new String[]{"15"}, headers.getValues(HEADER_B));
    }

    @Host("{url}")
    @ServiceInterface(name = "Service8")
    private interface Service8 {
        @Post("post")
        @ExpectedResponses({200})
        HttpBinJSON post(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String postBody);

        @Post("post")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> postAsync(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String postBody);
    }

    @Test
    public void syncPostRequestWithStringBody() {
        final HttpBinJSON json = createService(Service8.class).post(getRequestUri(), "I'm a post body!");

        assertEquals(String.class, json.data().getClass());
        assertEquals("I'm a post body!", json.data());
    }

    @Test
    public void asyncPostRequestWithStringBody() {
        StepVerifier.create(createService(Service8.class).postAsync(getRequestUri(), "I'm a post body!"))
            .assertNext(json -> {
                assertEquals(String.class, json.data().getClass());
                assertEquals("I'm a post body!", json.data());
            })
            .verifyComplete();
    }

    @Test
    public void syncPostRequestWithNullBody() {
        final HttpBinJSON result = createService(Service8.class).post(getRequestUri(), null);

        assertEquals("", result.data());
    }

    @SuppressWarnings("UnusedReturnValue")
    @Host("{url}")
    @ServiceInterface(name = "Service9")
    private interface Service9 {
        @Put("put")
        @ExpectedResponses({200})
        HttpBinJSON put(@HostParam("url") String url, @BodyParam(ContentType.APPLICATION_OCTET_STREAM) int putBody);

        @Put("put")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> putAsync(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) int putBody);

        @Put("put")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(MyRestException.class)
        HttpBinJSON putBodyAndContentLength(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) ByteBuffer body,
            @HeaderParam("Content-Length") long contentLength);

        @Put("put")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(MyRestException.class)
        Mono<HttpBinJSON> putAsyncBodyAndContentLength(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) Flux<ByteBuffer> body,
            @HeaderParam("Content-Length") long contentLength);

        @Put("put")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(MyRestException.class)
        Mono<HttpBinJSON> putAsyncBodyAndContentLength(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) BinaryData body,
            @HeaderParam("Content-Length") long contentLength);

        @Put("put")
        @ExpectedResponses({201})
        HttpBinJSON putWithUnexpectedResponse(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        Mono<HttpBinJSON> putWithUnexpectedResponseAsync(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(MyRestException.class)
        HttpBinJSON putWithUnexpectedResponseAndExceptionType(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(MyRestException.class)
        Mono<HttpBinJSON> putWithUnexpectedResponseAndExceptionTypeAsync(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(code = {200}, value = MyRestException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        HttpBinJSON putWithUnexpectedResponseAndDeterminedExceptionType(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(code = {200}, value = MyRestException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<HttpBinJSON> putWithUnexpectedResponseAndDeterminedExceptionTypeAsync(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(code = {400}, value = HttpResponseException.class)
        @UnexpectedResponseExceptionType(MyRestException.class)
        HttpBinJSON putWithUnexpectedResponseAndFallthroughExceptionType(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(code = {400}, value = HttpResponseException.class)
        @UnexpectedResponseExceptionType(MyRestException.class)
        Mono<HttpBinJSON> putWithUnexpectedResponseAndFallthroughExceptionTypeAsync(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(code = {400}, value = MyRestException.class)
        HttpBinJSON putWithUnexpectedResponseAndNoFallthroughExceptionType(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(code = {400}, value = MyRestException.class)
        Mono<HttpBinJSON> putWithUnexpectedResponseAndNoFallthroughExceptionTypeAsync(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);
    }

    @Test
    public void syncPutRequestWithIntBody() {
        final HttpBinJSON json = createService(Service9.class).put(getRequestUri(), 42);

        assertEquals(String.class, json.data().getClass());
        assertEquals("42", json.data());
    }

    @Test
    public void asyncPutRequestWithIntBody() {
        StepVerifier.create(createService(Service9.class).putAsync(getRequestUri(), 42))
            .assertNext(json -> {
                assertEquals(String.class, json.data().getClass());
                assertEquals("42", json.data());
            }).verifyComplete();
    }

    // Test all scenarios for the body length and content length comparison for sync API
    @Test
    public void syncPutRequestWithBodyAndEqualContentLength() {
        ByteBuffer body = ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8));
        final HttpBinJSON json = createService(Service9.class).putBodyAndContentLength(getRequestUri(), body, 4L);

        assertEquals("test", json.data());
        assertEquals(ContentType.APPLICATION_OCTET_STREAM, json.getHeaderValue("Content-Type"));
        assertEquals("4", json.getHeaderValue("Content-Length"));
    }

    @Test
    public void syncPutRequestWithBodyLessThanContentLength() {
        ByteBuffer body = ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8));
        Exception unexpectedLengthException = assertThrows(Exception.class, () -> {
            createService(Service9.class).putBodyAndContentLength(getRequestUri(), body, 5L);
            body.clear();
        });
        assertTrue(unexpectedLengthException.getMessage().contains("less than"));
    }

    @Test
    public void syncPutRequestWithBodyMoreThanContentLength() {
        ByteBuffer body = ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8));
        Exception unexpectedLengthException = assertThrows(Exception.class, () -> {
            createService(Service9.class).putBodyAndContentLength(getRequestUri(), body, 3L);
            body.clear();
        });
        assertTrue(unexpectedLengthException.getMessage().contains("more than"));
    }

    // Test all scenarios for the body length and content length comparison for Async API
    @Test
    public void asyncPutRequestWithBodyAndEqualContentLength() {
        Flux<ByteBuffer> body = Flux.just(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
        StepVerifier.create(createService(Service9.class).putAsyncBodyAndContentLength(getRequestUri(), body, 4L))
            .assertNext(json -> {
                assertEquals("test", json.data());
                assertEquals(ContentType.APPLICATION_OCTET_STREAM, json.getHeaderValue("Content-Type"));
                assertEquals("4", json.getHeaderValue("Content-Length"));
            }).verifyComplete();
    }

    @Test
    public void asyncPutRequestWithBodyAndLessThanContentLength() {
        Flux<ByteBuffer> body = Flux.just(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
        StepVerifier.create(createService(Service9.class).putAsyncBodyAndContentLength(getRequestUri(), body, 5L))
            .verifyErrorSatisfies(exception -> {
                assertTrue(exception instanceof UnexpectedLengthException
                    || (exception.getSuppressed().length > 0
                    && exception.getSuppressed()[0] instanceof UnexpectedLengthException));
                assertTrue(exception.getMessage().contains("less than"));
            });
    }

    @Test
    public void asyncPutRequestWithBodyAndMoreThanContentLength() {
        Flux<ByteBuffer> body = Flux.just(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
        StepVerifier.create(createService(Service9.class).putAsyncBodyAndContentLength(getRequestUri(), body, 3L))
            .verifyErrorSatisfies(exception -> {
                assertTrue(exception instanceof UnexpectedLengthException
                    || (exception.getSuppressed().length > 0
                    && exception.getSuppressed()[0] instanceof UnexpectedLengthException));
                assertTrue(exception.getMessage().contains("more than"));
            });
    }

    @Test
    public void asyncPutRequestWithBinaryDataBodyAndEqualContentLength() {
        Mono<BinaryData> bodyMono = BinaryData.fromFlux(
            Flux.just(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8))));
        StepVerifier.create(
                bodyMono.flatMap(body ->
                    createService(Service9.class).putAsyncBodyAndContentLength(getRequestUri(), body, 4L)))
            .assertNext(json -> {
                assertEquals("test", json.data());
                assertEquals(ContentType.APPLICATION_OCTET_STREAM, json.getHeaderValue("Content-Type"));
                assertEquals("4", json.getHeaderValue("Content-Length"));
            }).verifyComplete();
    }

    @Test
    public void asyncPutRequestWithBinaryDataBodyAndLessThanContentLength() {
        Mono<BinaryData> bodyMono = BinaryData.fromFlux(
            Flux.just(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8))));
        StepVerifier.create(
                bodyMono.flatMap(body ->
                    createService(Service9.class).putAsyncBodyAndContentLength(getRequestUri(), body, 5L)))
            .verifyErrorSatisfies(exception -> {
                assertTrue(exception instanceof UnexpectedLengthException
                    || (exception.getSuppressed().length > 0
                    && exception.getSuppressed()[0] instanceof UnexpectedLengthException));
                assertTrue(exception.getMessage().contains("less than"));
            });
    }

    /**
     * LengthValidatingInputStream in rest proxy relies on reader reaching EOF. This test specifically targets
     * InputStream to assert this behavior.
     */
    @Test
    public void asyncPutRequestWithStreamBinaryDataBodyAndLessThanContentLength() {
        Mono<BinaryData> bodyMono = Mono.just(BinaryData.fromStream(
            new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8))));
        StepVerifier.create(
                bodyMono.flatMap(body ->
                    createService(Service9.class).putAsyncBodyAndContentLength(getRequestUri(), body, 5L)))
            .verifyErrorSatisfies(exception -> {
                assertTrue(exception instanceof UnexpectedLengthException
                    || (exception.getSuppressed().length > 0
                    && exception.getSuppressed()[0] instanceof UnexpectedLengthException));
                assertTrue(exception.getMessage().contains("less than"));
            });
    }

    @Test
    public void asyncPutRequestWithBinaryDataBodyAndMoreThanContentLength() {
        Mono<BinaryData> bodyMono = BinaryData.fromFlux(
            Flux.just(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8))));
        StepVerifier.create(
                bodyMono.flatMap(body ->
                    createService(Service9.class).putAsyncBodyAndContentLength(getRequestUri(), body, 3L)))
            .verifyErrorSatisfies(exception -> {
                assertTrue(exception instanceof UnexpectedLengthException
                    || (exception.getSuppressed().length > 0
                    && exception.getSuppressed()[0] instanceof UnexpectedLengthException));
                assertTrue(exception.getMessage().contains("more than"));
            });
    }

    /**
     * LengthValidatingInputStream in rest proxy relies on reader reaching EOF. This test specifically targets
     * InputStream to assert this behavior.
     */
    @Test
    public void asyncPutRequestWithStreamBinaryDataBodyAndMoreThanContentLength() {
        BinaryData body = BinaryData.fromStream(new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)));
        StepVerifier.create(createService(Service9.class).putAsyncBodyAndContentLength(getRequestUri(), body, 3L))
            .verifyErrorSatisfies(exception -> {
                assertTrue(exception instanceof UnexpectedLengthException
                    || (exception.getSuppressed().length > 0
                    && exception.getSuppressed()[0] instanceof UnexpectedLengthException));
                assertTrue(exception.getMessage().contains("more than"));
            });
    }

    @Test
    public void syncPutRequestWithUnexpectedResponse() {
        HttpResponseException e = assertThrows(HttpResponseException.class,
            () -> createService(Service9.class).putWithUnexpectedResponse(getRequestUri(), "I'm the body!"));

        assertNotNull(e.getValue());
        assertTrue(e.getValue() instanceof LinkedHashMap);

        @SuppressWarnings("unchecked") final LinkedHashMap<String, String> expectedBody = (LinkedHashMap<String, String>) e.getValue();
        assertEquals("I'm the body!", expectedBody.get("data"));
    }

    @Test
    public void asyncPutRequestWithUnexpectedResponse() {
        StepVerifier.create(createService(Service9.class).putWithUnexpectedResponseAsync(getRequestUri(),
                "I'm the body!"))
            .verifyErrorSatisfies(throwable -> {
                assertTrue(throwable instanceof HttpResponseException);
                HttpResponseException exception = (HttpResponseException) throwable;
                assertNotNull(exception.getValue());
                assertTrue(exception.getValue() instanceof LinkedHashMap);

                @SuppressWarnings("unchecked") final LinkedHashMap<String, String> expectedBody = (LinkedHashMap<String, String>) exception.getValue();
                assertEquals("I'm the body!", expectedBody.get("data"));
            });
    }

    @Test
    public void syncPutRequestWithUnexpectedResponseAndExceptionType() {
        MyRestException e = assertThrows(MyRestException.class, () ->
            createService(Service9.class).putWithUnexpectedResponseAndExceptionType(getRequestUri(), "I'm the body!"));

        assertNotNull(e.getValue());
        assertEquals("I'm the body!", e.getValue().data());
    }

    @Test
    public void asyncPutRequestWithUnexpectedResponseAndExceptionType() {
        StepVerifier.create(createService(Service9.class).putWithUnexpectedResponseAndExceptionTypeAsync(
                getRequestUri(), "I'm the body!"))
            .verifyErrorSatisfies(throwable -> {
                MyRestException myRestException = assertInstanceOf(MyRestException.class, throwable,
                    "Expected MyRestException would be thrown. Instead got " + throwable.getClass().getSimpleName());
                assertNotNull(myRestException.getValue());
                assertEquals("I'm the body!", myRestException.getValue().data());
            });
    }

    @Test
    public void syncPutRequestWithUnexpectedResponseAndDeterminedExceptionType() {
        MyRestException e = assertThrows(MyRestException.class,
            () -> createService(Service9.class).putWithUnexpectedResponseAndDeterminedExceptionType(getRequestUri(),
                "I'm the body!"));

        assertNotNull(e.getValue());
        assertEquals("I'm the body!", e.getValue().data());
    }

    @Test
    public void asyncPutRequestWithUnexpectedResponseAndDeterminedExceptionType() {
        StepVerifier.create(createService(Service9.class).putWithUnexpectedResponseAndDeterminedExceptionTypeAsync(
                getRequestUri(), "I'm the body!"))
            .verifyErrorSatisfies(throwable -> {
                MyRestException myRestException = assertInstanceOf(MyRestException.class, throwable,
                    "Expected MyRestException would be thrown. Instead got " + throwable.getClass().getSimpleName());
                assertNotNull(myRestException.getValue());
                assertEquals("I'm the body!", myRestException.getValue().data());
            });
    }

    @Test
    public void syncPutRequestWithUnexpectedResponseAndFallthroughExceptionType() {
        MyRestException e = assertThrows(MyRestException.class,
            () -> createService(Service9.class).putWithUnexpectedResponseAndFallthroughExceptionType(getRequestUri(),
                "I'm the body!"));

        assertNotNull(e.getValue());
        assertEquals("I'm the body!", e.getValue().data());
    }

    @Test
    public void asyncPutRequestWithUnexpectedResponseAndFallthroughExceptionType() {
        StepVerifier.create(createService(Service9.class).putWithUnexpectedResponseAndFallthroughExceptionTypeAsync(
                getRequestUri(), "I'm the body!"))
            .verifyErrorSatisfies(throwable -> {
                MyRestException myRestException = assertInstanceOf(MyRestException.class, throwable,
                    "Expected MyRestException would be thrown. Instead got " + throwable.getClass().getSimpleName());
                assertNotNull(myRestException.getValue());
                assertEquals("I'm the body!", myRestException.getValue().data());
            });
    }

    @Test
    public void syncPutRequestWithUnexpectedResponseAndNoFallthroughExceptionType() {
        HttpResponseException e = assertThrows(HttpResponseException.class,
            () -> createService(Service9.class).putWithUnexpectedResponseAndNoFallthroughExceptionType(getRequestUri(),
                "I'm the body!"));

        assertNotNull(e.getValue());
        assertTrue(e.getValue() instanceof LinkedHashMap);

        @SuppressWarnings("unchecked") final LinkedHashMap<String, String> expectedBody = (LinkedHashMap<String, String>) e.getValue();
        assertEquals("I'm the body!", expectedBody.get("data"));
    }

    @Test
    public void asyncPutRequestWithUnexpectedResponseAndNoFallthroughExceptionType() {
        StepVerifier.create(createService(Service9.class).putWithUnexpectedResponseAndNoFallthroughExceptionTypeAsync(
                getRequestUri(), "I'm the body!"))
            .verifyErrorSatisfies(throwable -> {
                HttpResponseException responseException = assertInstanceOf(HttpResponseException.class, throwable,
                    "Expected HttpResponseException would be thrown. Instead got "
                        + throwable.getClass().getSimpleName());
                assertNotNull(responseException.getValue());
                assertTrue(responseException.getValue() instanceof LinkedHashMap);

                @SuppressWarnings("unchecked") final LinkedHashMap<String, String> expectedBody = (LinkedHashMap<String, String>) responseException.getValue();
                assertEquals("I'm the body!", expectedBody.get("data"));
            });
    }

    @Host("{url}")
    @ServiceInterface(name = "Service10")
    private interface Service10 {
        @Head("anything")
        @ExpectedResponses({200})
        Response<Void> head(@HostParam("url") String url);

        @Head("anything")
        @ExpectedResponses({200})
        boolean headBoolean(@HostParam("url") String url);

        @Head("anything")
        @ExpectedResponses({200})
        void voidHead(@HostParam("url") String url);

        @Head("anything")
        @ExpectedResponses({200})
        Mono<Response<Void>> headAsync(@HostParam("url") String url);

        @Head("anything")
        @ExpectedResponses({200})
        Mono<Boolean> headBooleanAsync(@HostParam("url") String url);

        @Head("anything")
        @ExpectedResponses({200})
        Mono<Void> completableHeadAsync(@HostParam("url") String url);
    }

    @Test
    public void syncHeadRequest() {
        final Void body = createService(Service10.class).head(getRequestUri()).getValue();
        assertNull(body);
    }

    @Test
    public void syncHeadBooleanRequest() {
        final boolean result = createService(Service10.class).headBoolean(getRequestUri());
        assertTrue(result);
    }

    @Test
    public void syncVoidHeadRequest() {
        createService(Service10.class).voidHead(getRequestUri());
    }

    @Test
    public void asyncHeadRequest() {
        StepVerifier.create(createService(Service10.class).headAsync(getRequestUri()))
            .assertNext(response -> assertNull(response.getValue()))
            .verifyComplete();
    }

    @Test
    public void asyncHeadBooleanRequest() {
        StepVerifier.create(createService(Service10.class).headBooleanAsync(getRequestUri()))
            .assertNext(Assertions::assertTrue)
            .verifyComplete();
    }

    @Test
    public void asyncCompletableHeadRequest() {
        StepVerifier.create(createService(Service10.class).completableHeadAsync(getRequestUri()))
            .verifyComplete();
    }

    @Host("{url}")
    @ServiceInterface(name = "Service11")
    private interface Service11 {
        @Delete("delete")
        @ExpectedResponses({200})
        HttpBinJSON delete(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) boolean bodyBoolean);

        @Delete("delete")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> deleteAsync(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) boolean bodyBoolean);
    }

    @Test
    public void syncDeleteRequest() {
        final HttpBinJSON json = createService(Service11.class).delete(getRequestUri(), false);

        assertEquals(String.class, json.data().getClass());
        assertEquals("false", json.data());
    }

    @Test
    public void asyncDeleteRequest() {
        StepVerifier.create(createService(Service11.class).deleteAsync(getRequestUri(), false))
            .assertNext(json -> {
                assertEquals(String.class, json.data().getClass());
                assertEquals("false", json.data());
            }).verifyComplete();
    }

    @Host("{url}")
    @ServiceInterface(name = "Service12")
    private interface Service12 {
        @Patch("patch")
        @ExpectedResponses({200})
        HttpBinJSON patch(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String bodyString);

        @Patch("patch")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> patchAsync(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String bodyString);
    }

    @Test
    public void syncPatchRequest() {
        final HttpBinJSON json = createService(Service12.class).patch(getRequestUri(), "body-contents");

        assertEquals(String.class, json.data().getClass());
        assertEquals("body-contents", json.data());
    }

    @Test
    public void asyncPatchRequest() {
        StepVerifier.create(createService(Service12.class).patchAsync(getRequestUri(), "body-contents"))
            .assertNext(json -> {
                assertEquals(String.class, json.data().getClass());
                assertEquals("body-contents", json.data());
            }).verifyComplete();
    }

    @Host("{url}")
    @ServiceInterface(name = "Service13")
    private interface Service13 {
        @Get("anything")
        @ExpectedResponses({200})
        @Headers({"MyHeader:MyHeaderValue", "MyOtherHeader:My,Header,Value"})
        HttpBinJSON get(@HostParam("url") String url);

        @Get("anything")
        @ExpectedResponses({200})
        @Headers({"MyHeader:MyHeaderValue", "MyOtherHeader:My,Header,Value"})
        Mono<HttpBinJSON> getAsync(@HostParam("url") String url);
    }

    private static final HttpHeaderName MY_HEADER = HttpHeaderName.fromString("MyHeader");
    private static final HttpHeaderName MY_OTHER_HEADER = HttpHeaderName.fromString("MyOtherHeader");

    @Test
    public void syncHeadersRequest() {
        final HttpBinJSON json = createService(Service13.class).get(getRequestUri());
        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything", json.url());
        assertNotNull(json.headers());
        final HttpHeaders headers = new HttpHeaders().setAll(json.headers());
        assertEquals("MyHeaderValue", headers.getValue(MY_HEADER));
        assertArrayEquals(new String[]{"MyHeaderValue"}, headers.getValues(MY_HEADER));
        assertEquals("My,Header,Value", headers.getValue(MY_OTHER_HEADER));
        assertArrayEquals(new String[]{"My", "Header", "Value"}, headers.getValues(MY_OTHER_HEADER));
    }

    @Test
    public void asyncHeadersRequest() {
        StepVerifier.create(createService(Service13.class).getAsync(getRequestUri()))
            .assertNext(json -> {
                assertMatchWithHttpOrHttps("localhost/anything", json.url());
                assertNotNull(json.headers());
                final HttpHeaders headers = new HttpHeaders().setAll(json.headers());

                assertEquals("MyHeaderValue", headers.getValue(MY_HEADER));
                assertArrayEquals(new String[]{"MyHeaderValue"}, headers.getValues(MY_HEADER));
            }).verifyComplete();
    }

    @Host("{url}")
    @ServiceInterface(name = "Service14")
    private interface Service14 {
        @Get("anything")
        @ExpectedResponses({200})
        @Headers({"MyHeader:MyHeaderValue"})
        HttpBinJSON get(@HostParam("url") String url);

        @Get("anything")
        @ExpectedResponses({200})
        @Headers({"MyHeader:MyHeaderValue"})
        Mono<HttpBinJSON> getAsync(@HostParam("url") String url);
    }

    @Test
    public void asyncHttpsHeadersRequest() {
        StepVerifier.create(createService(Service14.class).getAsync(getRequestUri()))
            .assertNext(json -> {
                assertMatchWithHttpOrHttps("localhost/anything", json.url());
                assertNotNull(json.headers());
                final HttpHeaders headers = new HttpHeaders().setAll(json.headers());
                assertEquals("MyHeaderValue", headers.getValue(MY_HEADER));
            }).verifyComplete();
    }

    @Host("{url}")
    @ServiceInterface(name = "Service16")
    private interface Service16 {
        @Put("put")
        @ExpectedResponses({200})
        HttpBinJSON putByteArray(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] bytes);

        @Put("put")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> putByteArrayAsync(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] bytes);
    }

    @Test
    public void service16Put() {
        final Service16 service16 = createService(Service16.class);
        final byte[] expectedBytes = new byte[]{1, 2, 3, 4};
        final HttpBinJSON httpBinJSON = service16.putByteArray(getRequestUri(), expectedBytes);

        // httpbin sends the data back as a string like "\u0001\u0002\u0003\u0004"
        assertTrue(httpBinJSON.data() instanceof String);

        final String base64String = (String) httpBinJSON.data();
        final byte[] actualBytes = base64String.getBytes();
        assertArrayEquals(expectedBytes, actualBytes);
    }

    @Test
    public void service16PutAsync() {
        final byte[] expectedBytes = new byte[]{1, 2, 3, 4};
        StepVerifier.create(createService(Service16.class).putByteArrayAsync(getRequestUri(), expectedBytes))
            .assertNext(json -> {
                assertTrue(json.data() instanceof String);
                assertArrayEquals(expectedBytes, ((String) json.data()).getBytes());
            }).verifyComplete();
    }

    @Host("{scheme}://{hostPart1}{hostPart2}")
    @ServiceInterface(name = "Service17")
    private interface Service17 {
        @Get("get")
        @ExpectedResponses({200})
        HttpBinJSON get(@HostParam("scheme") String scheme, @HostParam("hostPart1") String hostPart1,
            @HostParam("hostPart2") String hostPart2);

        @Get("get")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> getAsync(@HostParam("scheme") String scheme, @HostParam("hostPart1") String hostPart1,
            @HostParam("hostPart2") String hostPart2);
    }

    @Test
    public void syncRequestWithMultipleHostParams() {
        final HttpBinJSON result = createService(Service17.class).get(getRequestScheme(), "local", "host");

        assertNotNull(result);
        assertMatchWithHttpOrHttps("localhost/get", result.url());
    }

    @Test
    public void asyncRequestWithMultipleHostParams() {
        StepVerifier.create(createService(Service17.class).getAsync(getRequestScheme(), "local", "host"))
            .assertNext(json -> assertMatchWithHttpOrHttps("localhost/get", json.url()))
            .verifyComplete();
    }

    @Host("{url}")
    @ServiceInterface(name = "Service18")
    private interface Service18 {
        @Get("status/200")
        void getStatus200(@HostParam("url") String url);

        @Get("status/200")
        @ExpectedResponses({200})
        void getStatus200WithExpectedResponse200(@HostParam("url") String url);

        @Get("status/300")
        void getStatus300(@HostParam("url") String url);

        @Get("status/300")
        @ExpectedResponses({300})
        void getStatus300WithExpectedResponse300(@HostParam("url") String url);

        @Get("status/400")
        void getStatus400(@HostParam("url") String url);

        @Get("status/400")
        @ExpectedResponses({400})
        void getStatus400WithExpectedResponse400(@HostParam("url") String url);

        @Get("status/500")
        void getStatus500(@HostParam("url") String url);

        @Get("status/500")
        @ExpectedResponses({500})
        void getStatus500WithExpectedResponse500(@HostParam("url") String url);
    }

    @Test
    public void service18GetStatus200() {
        createService(Service18.class).getStatus200(getRequestUri());
    }

    @Test
    public void service18GetStatus200WithExpectedResponse200() {
        assertDoesNotThrow(() -> createService(Service18.class).getStatus200WithExpectedResponse200(getRequestUri()));
    }

    @Test
    public void service18GetStatus300() {
        createService(Service18.class).getStatus300(getRequestUri());
    }

    @Test
    public void service18GetStatus300WithExpectedResponse300() {
        assertDoesNotThrow(() -> createService(Service18.class).getStatus300WithExpectedResponse300(getRequestUri()));
    }

    @Test
    public void service18GetStatus400() {
        assertThrows(HttpResponseException.class, () -> createService(Service18.class).getStatus400(getRequestUri()));
    }

    @Test
    public void service18GetStatus400WithExpectedResponse400() {
        assertDoesNotThrow(() -> createService(Service18.class).getStatus400WithExpectedResponse400(getRequestUri()));
    }

    @Test
    public void service18GetStatus500() {
        assertThrows(HttpResponseException.class, () -> createService(Service18.class).getStatus500(getRequestUri()));
    }

    @Test
    public void service18GetStatus500WithExpectedResponse500() {
        assertDoesNotThrow(() -> createService(Service18.class).getStatus500WithExpectedResponse500(getRequestUri()));
    }

    @Host("{url}")
    @ServiceInterface(name = "Service19")
    private interface Service19 {
        @Put("put")
        HttpBinJSON putWithNoContentTypeAndStringBody(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Put("put")
        HttpBinJSON putWithNoContentTypeAndByteArrayBody(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] body);

        @Put("put")
        HttpBinJSON putWithHeaderApplicationJsonContentTypeAndStringBody(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_JSON) String body);

        @Put("put")
        @Headers({"Content-Type: application/json"})
        HttpBinJSON putWithHeaderApplicationJsonContentTypeAndByteArrayBody(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_JSON) byte[] body);

        @Put("put")
        @Headers({"Content-Type: application/json; charset=utf-8"})
        HttpBinJSON putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Put("put")
        @Headers({"Content-Type: application/octet-stream"})
        HttpBinJSON putWithHeaderApplicationOctetStreamContentTypeAndStringBody(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Put("put")
        @Headers({"Content-Type: application/octet-stream"})
        HttpBinJSON putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] body);

        @Put("put")
        HttpBinJSON putWithBodyParamApplicationJsonContentTypeAndStringBody(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_JSON) String body);

        @Put("put")
        HttpBinJSON putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_JSON + "; charset=utf-8") String body);

        @Put("put")
        HttpBinJSON putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_JSON) byte[] body);

        @Put("put")
        HttpBinJSON putWithBodyParamApplicationOctetStreamContentTypeAndStringBody(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Put("put")
        HttpBinJSON putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] body);
    }

    @Test
    public void service19PutWithNoContentTypeAndStringBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class).putWithNoContentTypeAndStringBody(getRequestUri(),
            null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithNoContentTypeAndStringBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class).putWithNoContentTypeAndStringBody(getRequestUri(),
            "");

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithNoContentTypeAndStringBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class).putWithNoContentTypeAndStringBody(getRequestUri(),
            "hello");

        assertEquals("hello", result.data());
    }

    @Test
    public void service19PutWithNoContentTypeAndByteArrayBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class).putWithNoContentTypeAndByteArrayBody(getRequestUri(),
            null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithNoContentTypeAndByteArrayBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class).putWithNoContentTypeAndByteArrayBody(getRequestUri(),
            new byte[0]);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithNoContentTypeAndByteArrayBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithNoContentTypeAndByteArrayBody(getRequestUri(), new byte[]{0, 1, 2, 3, 4});

        assertEquals(new String(new byte[]{0, 1, 2, 3, 4}), result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndStringBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithHeaderApplicationJsonContentTypeAndStringBody(getRequestUri(), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndStringBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithHeaderApplicationJsonContentTypeAndStringBody(getRequestUri(), "");

        assertEquals("\"\"", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndStringBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithHeaderApplicationJsonContentTypeAndStringBody(getRequestUri(), "soups and stuff");

        assertEquals("\"soups and stuff\"", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndByteArrayBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithHeaderApplicationJsonContentTypeAndByteArrayBody(getRequestUri(), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndByteArrayBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithHeaderApplicationJsonContentTypeAndByteArrayBody(getRequestUri(), new byte[0]);

        assertEquals("\"\"", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndByteArrayBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithHeaderApplicationJsonContentTypeAndByteArrayBody(getRequestUri(), new byte[]{0, 1, 2, 3, 4});

        assertEquals("\"AAECAwQ=\"", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndCharsetAndStringBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(getRequestUri(), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndCharsetAndStringBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(getRequestUri(), "");

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndCharsetAndStringBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(getRequestUri(), "soups and stuff");

        assertEquals("soups and stuff", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndStringBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithHeaderApplicationOctetStreamContentTypeAndStringBody(getRequestUri(), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndStringBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithHeaderApplicationOctetStreamContentTypeAndStringBody(getRequestUri(), "");

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndStringBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithHeaderApplicationOctetStreamContentTypeAndStringBody(getRequestUri(), "penguins");

        assertEquals("penguins", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndByteArrayBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(getRequestUri(), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndByteArrayBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(getRequestUri(), new byte[0]);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndByteArrayBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(getRequestUri(),
                new byte[]{0, 1, 2, 3, 4});

        assertEquals(new String(new byte[]{0, 1, 2, 3, 4}), result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndStringBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithBodyParamApplicationJsonContentTypeAndStringBody(getRequestUri(), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndStringBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithBodyParamApplicationJsonContentTypeAndStringBody(getRequestUri(), "");

        assertEquals("\"\"", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndStringBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithBodyParamApplicationJsonContentTypeAndStringBody(getRequestUri(), "soups and stuff");

        assertEquals("\"soups and stuff\"", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody(getRequestUri(), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody(getRequestUri(), "");

        assertEquals("\"\"", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody(getRequestUri(), "soups and stuff");

        assertEquals("\"soups and stuff\"", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndByteArrayBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(getRequestUri(), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndByteArrayBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(getRequestUri(), new byte[0]);

        assertEquals("\"\"", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndByteArrayBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(getRequestUri(), new byte[]{0, 1, 2, 3, 4});

        assertEquals("\"AAECAwQ=\"", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndStringBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithBodyParamApplicationOctetStreamContentTypeAndStringBody(getRequestUri(), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndStringBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithBodyParamApplicationOctetStreamContentTypeAndStringBody(getRequestUri(), "");

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndStringBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithBodyParamApplicationOctetStreamContentTypeAndStringBody(getRequestUri(), "penguins");

        assertEquals("penguins", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(getRequestUri(), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(getRequestUri(), new byte[0]);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(getRequestUri(),
                new byte[]{0, 1, 2, 3, 4});

        assertEquals(new String(new byte[]{0, 1, 2, 3, 4}), result.data());
    }

    @Host("{url}")
    @ServiceInterface(name = "Service20")
    private interface Service20 {
        @Get("bytes/100")
        ResponseBase<HttpBinHeaders, Void> getBytes100OnlyHeaders(@HostParam("url") String url);

        @Get("bytes/100")
        ResponseBase<HttpHeaders, Void> getBytes100OnlyRawHeaders(@HostParam("url") String url);

        @Get("bytes/100")
        ResponseBase<HttpBinHeaders, byte[]> getBytes100BodyAndHeaders(@HostParam("url") String url);

        @Put("put")
        ResponseBase<HttpBinHeaders, Void> putOnlyHeaders(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Put("put")
        ResponseBase<HttpBinHeaders, HttpBinJSON> putBodyAndHeaders(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Get("bytes/100")
        ResponseBase<Void, Void> getBytesOnlyStatus(@HostParam("url") String url);

        @Get("bytes/100")
        Response<Void> getVoidResponse(@HostParam("url") String url);

        @Put("put")
        Response<HttpBinJSON> putBody(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);
    }

    @Test
    public void service20GetBytes100OnlyHeaders() {
        final ResponseBase<HttpBinHeaders, Void> response = createService(Service20.class)
            .getBytes100OnlyHeaders(getRequestUri());
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());

        assertEquals(HttpHeaders.class, response.getHeaders().getClass());

        final HttpBinHeaders headers = response.getDeserializedHeaders();
        assertNotNull(headers);
        assertTrue(headers.accessControlAllowCredentials());
        assertNotNull(headers.date());
        assertNotEquals(0, (Object) headers.xProcessedTime());
    }

    @Test
    public void service20GetBytes100BodyAndHeaders() {
        final ResponseBase<HttpBinHeaders, byte[]> response = createService(Service20.class)
            .getBytes100BodyAndHeaders(getRequestUri());
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());

        assertEquals(HttpHeaders.class, response.getHeaders().getClass());

        final byte[] body = response.getValue();
        assertNotNull(body);
        assertEquals(100, body.length);

        final HttpBinHeaders headers = response.getDeserializedHeaders();
        assertNotNull(headers);
        assertTrue(headers.accessControlAllowCredentials());
        assertNotNull(headers.date());
        assertNotEquals(0, (Object) headers.xProcessedTime());
    }

    @Test
    public void service20GetBytesOnlyStatus() {
        final Response<Void> response = createService(Service20.class).getBytesOnlyStatus(getRequestUri());

        assertEquals(HttpHeaders.class, response.getHeaders().getClass());

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void service20GetBytesOnlyHeaders() {
        final Response<Void> response = createService(Service20.class).getBytes100OnlyRawHeaders(getRequestUri());

        assertEquals(HttpHeaders.class, response.getHeaders().getClass());

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getHeaders());
        assertNotEquals(0, response.getHeaders().getSize());
    }

    @Test
    public void service20PutOnlyHeaders() {
        final ResponseBase<HttpBinHeaders, Void> response = createService(Service20.class)
            .putOnlyHeaders(getRequestUri(), "body string");
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());

        assertEquals(HttpHeaders.class, response.getHeaders().getClass());

        final HttpBinHeaders headers = response.getDeserializedHeaders();
        assertNotNull(headers);
        assertTrue(headers.accessControlAllowCredentials());
        assertNotNull(headers.date());
        assertNotEquals(0, (Object) headers.xProcessedTime());
    }

    @Test
    public void service20PutBodyAndHeaders() {
        final ResponseBase<HttpBinHeaders, HttpBinJSON> response = createService(Service20.class)
            .putBodyAndHeaders(getRequestUri(), "body string");
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());

        assertEquals(HttpHeaders.class, response.getHeaders().getClass());

        final HttpBinJSON body = response.getValue();
        assertNotNull(body);
        assertMatchWithHttpOrHttps("localhost/put", body.url());
        assertEquals("body string", body.data());

        final HttpBinHeaders headers = response.getDeserializedHeaders();
        assertNotNull(headers);
        assertTrue(headers.accessControlAllowCredentials());
        assertNotNull(headers.date());
        assertNotEquals(0, (Object) headers.xProcessedTime());
    }

    @Test
    public void service20GetVoidResponse() {
        final Response<Void> response = createService(Service20.class).getVoidResponse(getRequestUri());
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void service20GetResponseBody() {
        final Response<HttpBinJSON> response = createService(Service20.class).putBody(getRequestUri(), "body string");
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());

        final HttpBinJSON body = response.getValue();
        assertNotNull(body);
        assertMatchWithHttpOrHttps("localhost/put", body.url());
        assertEquals("body string", body.data());

        final HttpHeaders headers = response.getHeaders();
        assertNotNull(headers);
    }

    @Host("{url}")
    @ServiceInterface(name = "UnexpectedOKService")
    interface UnexpectedOKService {
        @Get("/bytes/1024")
        @ExpectedResponses({400})
        StreamResponse getBytes(@HostParam("url") String url);
    }

    @Test
    public void unexpectedHTTPOK() {
        HttpResponseException e = assertThrows(HttpResponseException.class,
            () -> createService(UnexpectedOKService.class).getBytes(getRequestUri()));

        assertEquals("Status code 200, (1024-byte body)", e.getMessage());
    }

    @Host("{url}")
    @ServiceInterface(name = "Service21")
    private interface Service21 {
        @Get("/bytes/100")
        @ExpectedResponses({200})
        byte[] getBytes100(@HostParam("url") String url);
    }

    @Test
    public void service21GetBytes100() {
        final byte[] bytes = createService(Service21.class).getBytes100(getRequestUri());

        assertNotNull(bytes);
        assertEquals(100, bytes.length);
    }

    @Host("{url}")
    @ServiceInterface(name = "DownloadService")
    interface DownloadService {

        @Get("/bytes/30720")
        StreamResponse getBytes(@HostParam("url") String url, Context context);

        @Get("/bytes/30720")
        Mono<StreamResponse> getBytesAsync(@HostParam("url") String url, Context context);

        @Get("/bytes/30720")
        Flux<ByteBuffer> getBytesFlux(@HostParam("url") String url);
    }

    @ParameterizedTest
    @MethodSource("downloadTestArgumentProvider")
    public void simpleDownloadTest(Context context) {
        StepVerifier.create(Flux.using(() -> createService(DownloadService.class).getBytes(getRequestUri(), context),
                response -> response.getValue().map(ByteBuffer::remaining).reduce(0, Integer::sum),
                StreamResponse::close))
            .assertNext(count -> assertEquals(30720, count))
            .verifyComplete();

        StepVerifier.create(Flux.using(() -> createService(DownloadService.class).getBytes(getRequestUri(), context),
                response -> Mono.zip(MessageDigestUtils.md5(response.getValue()),
                    Mono.just(response.getHeaders().getValue(HttpHeaderName.ETAG))),
                StreamResponse::close))
            .assertNext(hashTuple -> assertEquals(hashTuple.getT2(), hashTuple.getT1()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("downloadTestArgumentProvider")
    public void simpleDownloadTestAsync(Context context) {
        StepVerifier.create(createService(DownloadService.class).getBytesAsync(getRequestUri(), context)
                .flatMap(response -> response.getValue().map(ByteBuffer::remaining)
                    .reduce(0, Integer::sum)
                    .doFinally(ignore -> response.close())))
            .assertNext(count -> assertEquals(30720, count))
            .verifyComplete();

        StepVerifier.create(createService(DownloadService.class).getBytesAsync(getRequestUri(), context)
                .flatMap(response -> Mono.zip(MessageDigestUtils.md5(response.getValue()),
                        Mono.just(response.getHeaders().getValue(HttpHeaderName.ETAG)))
                    .doFinally(ignore -> response.close())))
            .assertNext(hashTuple -> assertEquals(hashTuple.getT2(), hashTuple.getT1()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("downloadTestArgumentProvider")
    public void streamResponseCanTransferBody(Context context) throws IOException {
        try (StreamResponse streamResponse = createService(DownloadService.class).getBytes(getRequestUri(), context)) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            streamResponse.writeValueTo(Channels.newChannel(bos));
            assertEquals(streamResponse.getHeaders().getValue(HttpHeaderName.ETAG),
                MessageDigestUtils.md5(bos.toByteArray()));
        }

        Path tempFile = Files.createTempFile("streamResponseCanTransferBody", null);
        tempFile.toFile().deleteOnExit();
        try (StreamResponse streamResponse = createService(DownloadService.class).getBytes(getRequestUri(), context)) {
            StepVerifier.create(Mono.using(
                    () -> IOUtils.toAsynchronousByteChannel(AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE), 0),
                    streamResponse::writeValueToAsync,
                    channel -> {
                        try {
                            channel.close();
                        } catch (IOException e) {
                            throw Exceptions.propagate(e);
                        }
                    }).then(Mono.fromCallable(() -> MessageDigestUtils.md5(Files.readAllBytes(tempFile)))))
                .assertNext(hash -> assertEquals(streamResponse.getHeaders().getValue(HttpHeaderName.ETAG), hash))
                .verifyComplete();
        }
    }

    @ParameterizedTest
    @MethodSource("downloadTestArgumentProvider")
    public void streamResponseCanTransferBodyAsync(Context context) throws IOException {
        StepVerifier.create(createService(DownloadService.class).getBytesAsync(getRequestUri(), context)
                .publishOn(Schedulers.boundedElastic())
                .map(streamResponse -> {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    try {
                        streamResponse.writeValueTo(Channels.newChannel(bos));
                    } finally {
                        streamResponse.close();
                    }
                    return Tuples.of(streamResponse.getHeaders().getValue(HttpHeaderName.ETAG),
                        MessageDigestUtils.md5(bos.toByteArray()));
                }))
            .assertNext(hashTuple -> assertEquals(hashTuple.getT1(), hashTuple.getT2()))
            .verifyComplete();

        Path tempFile = Files.createTempFile("streamResponseCanTransferBody", null);
        tempFile.toFile().deleteOnExit();
        StepVerifier.create(createService(DownloadService.class).getBytesAsync(getRequestUri(), context)
                .flatMap(streamResponse -> Mono.using(
                        () -> IOUtils.toAsynchronousByteChannel(AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE), 0),
                        streamResponse::writeValueToAsync,
                        channel -> {
                            try {
                                channel.close();
                            } catch (IOException e) {
                                throw Exceptions.propagate(e);
                            }
                        }).doFinally(ignored -> streamResponse.close())
                    .then(Mono.just(streamResponse.getHeaders().getValue(HttpHeaderName.ETAG)))))
            .assertNext(hash -> {
                try {
                    assertEquals(hash, MessageDigestUtils.md5(Files.readAllBytes(tempFile)));
                } catch (IOException e) {
                    throw Exceptions.propagate(e);
                }
            })
            .verifyComplete();
    }

    public static Stream<Arguments> downloadTestArgumentProvider() {
        return Stream.of(
            Arguments.of(Named.named("default", Context.NONE)),
            Arguments.of(Named.named("sync proxy enabled", Context.NONE
                .addData(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true))));
    }

    @Test
    public void rawFluxDownloadTest() {
        StepVerifier.create(createService(DownloadService.class).getBytesFlux(getRequestUri())
                .map(ByteBuffer::remaining).reduce(0, Integer::sum))
            .assertNext(count -> assertEquals(30720, count))
            .verifyComplete();
    }

    @Host("{url}")
    @ServiceInterface(name = "FluxUploadService")
    interface FluxUploadService {
        @Put("/put")
        Response<HttpBinJSON> put(@HostParam("url") String url, @BodyParam("text/plain") Flux<ByteBuffer> content,
            @HeaderParam("Content-Length") long contentLength);
    }

    @Test
    public void fluxUploadTest() throws Exception {
        Path filePath = Paths.get(getClass().getClassLoader().getResource("upload.txt").toURI());
        Flux<ByteBuffer> stream = FluxUtil.readFile(AsynchronousFileChannel.open(filePath));

        final HttpClient httpClient = createHttpClient();
        // Scenario: Log the body so that body buffering/replay behavior is exercised.
        //
        // Order in which policies applied will be the order in which they added to builder
        //
        final HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new PortPolicy(getPort(), true),
                new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)))
            .build();
        //
        Response<HttpBinJSON> response = RestProxy
            .create(FluxUploadService.class, httpPipeline).put(getRequestUri(), stream, Files.size(filePath));

        assertEquals("The quick brown fox jumps over the lazy dog", response.getValue().data());
    }

    @Test
    public void segmentUploadTest() throws Exception {
        Path filePath = Paths.get(getClass().getClassLoader().getResource("upload.txt").toURI());
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(filePath, StandardOpenOption.READ);
        Response<HttpBinJSON> response = createService(FluxUploadService.class)
            .put(getRequestUri(), FluxUtil.readFile(fileChannel, 4, 15), 15);

        assertEquals("quick brown fox", response.getValue().data());
    }

    @Host("{url}")
    @ServiceInterface(name = "BinaryDataUploadServ")
    interface BinaryDataUploadService {
        @Put("/put")
        Response<HttpBinJSON> put(@HostParam("url") String host, @BodyParam("text/plain") BinaryData content,
            @HeaderParam("Content-Length") long contentLength);
    }

    @Test
    public void binaryDataUploadTest() throws Exception {
        Path filePath = Paths.get(getClass().getClassLoader().getResource("upload.txt").toURI());
        BinaryData data = BinaryData.fromFile(filePath);

        final HttpClient httpClient = createHttpClient();
        // Scenario: Log the body so that body buffering/replay behavior is exercised.
        //
        // Order in which policies applied will be the order in which they added to builder
        //
        final HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new PortPolicy(getPort(), true),
                new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)))
            .build();
        //
        Response<HttpBinJSON> response = RestProxy.create(BinaryDataUploadService.class, httpPipeline)
            .put(getServerUri(isSecure()), data, Files.size(filePath));

        assertEquals("The quick brown fox jumps over the lazy dog", response.getValue().data());
    }

    @Host("{url}")
    @ServiceInterface(name = "Service22")
    interface Service22 {
        @Get("/")
        byte[] getBytes(@HostParam("url") String url);
    }

    @Test
    public void service22GetBytes() {
        final byte[] bytes = createService(Service22.class).getBytes(getRequestUri() + "/bytes/27");
        assertNotNull(bytes);
        assertEquals(27, bytes.length);
    }

    @Host("{url}")
    @ServiceInterface(name = "Service23")
    interface Service23 {
        @Get("bytes/28")
        byte[] getBytes(@HostParam("url") String url);
    }

    @Test
    public void service23GetBytes() {
        final byte[] bytes = createService(Service23.class).getBytes(getRequestUri());

        assertNotNull(bytes);
        assertEquals(28, bytes.length);
    }

    @Host("{url}")
    @ServiceInterface(name = "Service24")
    interface Service24 {
        @Put("put")
        HttpBinJSON put(@HostParam("url") String url, @HeaderParam("ABC") Map<String, String> headerCollection);
    }

    @Test
    public void service24Put() {
        final Map<String, String> headerCollection = new HashMap<>();
        headerCollection.put("DEF", "GHIJ");
        headerCollection.put("123", "45");
        final HttpBinJSON result = createService(Service24.class).put(getRequestUri(), headerCollection);
        assertNotNull(result.headers());

        final HttpHeaders resultHeaders = new HttpHeaders().setAll(result.headers());

        assertEquals("GHIJ", resultHeaders.getValue(HttpHeaderName.fromString("ABCDEF")));
        assertEquals("45", resultHeaders.getValue(HttpHeaderName.fromString("ABC123")));
    }

    @Host("{url}")
    @ServiceInterface(name = "Service26")
    interface Service26 {
        @Post("post")
        HttpBinFormDataJSON postForm(@HostParam("url") String url, @FormParam("custname") String name,
            @FormParam("custtel") String telephone, @FormParam("custemail") String email,
            @FormParam("size") HttpBinFormDataJSON.PizzaSize size, @FormParam("toppings") List<String> toppings);

        @Post("post")
        HttpBinFormDataJSON postEncodedForm(@HostParam("url") String url, @FormParam("custname") String name,
            @FormParam("custtel") String telephone, @FormParam(value = "custemail", encoded = true) String email,
            @FormParam("size") HttpBinFormDataJSON.PizzaSize size, @FormParam("toppings") List<String> toppings);
    }

    @Test
    public void postUrlForm() {
        Service26 service = createService(Service26.class);
        HttpBinFormDataJSON response = service.postForm(getRequestUri(), "Foo", "123", "foo@bar.com",
            HttpBinFormDataJSON.PizzaSize.LARGE, Arrays.asList("Bacon", "Onion"));
        assertNotNull(response);
        assertNotNull(response.form());
        assertEquals("Foo", response.form().customerName());
        assertEquals("123", response.form().customerTelephone());
        assertEquals("foo%40bar.com", response.form().customerEmail());
        assertEquals(HttpBinFormDataJSON.PizzaSize.LARGE, response.form().pizzaSize());

        assertEquals(2, response.form().toppings().size());
        assertEquals("Bacon", response.form().toppings().get(0));
        assertEquals("Onion", response.form().toppings().get(1));
    }

    @Test
    public void postUrlFormEncoded() {
        Service26 service = createService(Service26.class);
        HttpBinFormDataJSON response = service.postEncodedForm(getRequestUri(), "Foo", "123", "foo@bar.com",
            HttpBinFormDataJSON.PizzaSize.LARGE, Arrays.asList("Bacon", "Onion"));
        assertNotNull(response);
        assertNotNull(response.form());
        assertEquals("Foo", response.form().customerName());
        assertEquals("123", response.form().customerTelephone());
        assertEquals("foo@bar.com", response.form().customerEmail());
        assertEquals(HttpBinFormDataJSON.PizzaSize.LARGE, response.form().pizzaSize());

        assertEquals(2, response.form().toppings().size());
        assertEquals("Bacon", response.form().toppings().get(0));
        assertEquals("Onion", response.form().toppings().get(1));
    }

    @Host("{url}")
    @ServiceInterface(name = "Service27")
    interface Service27 {
        @Put("put")
        @ExpectedResponses({200})
        HttpBinJSON put(@HostParam("url") String url, @BodyParam(ContentType.APPLICATION_OCTET_STREAM) int putBody,
            RequestOptions requestOptions);

        @Put("put")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(MyRestException.class)
        HttpBinJSON putBodyAndContentLength(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) ByteBuffer body,
            @HeaderParam("Content-Length") long contentLength, RequestOptions requestOptions);
    }

    @Test
    public void requestOptionsChangesBody() {
        Service27 service = createService(Service27.class);

        HttpBinJSON response = service.put(getServerUri(isSecure()), 42,
            new RequestOptions().setBody(BinaryData.fromString("24")));
        assertNotNull(response);
        assertNotNull(response.data());
        assertTrue(response.data() instanceof String);
        assertEquals("24", response.data());
    }

    @Test
    public void requestOptionsChangesBodyAndContentLength() {
        Service27 service = createService(Service27.class);

        HttpBinJSON response = service.put(getServerUri(isSecure()), 42,
            new RequestOptions().setBody(BinaryData.fromString("4242")).setHeader(HttpHeaderName.CONTENT_LENGTH, "4"));
        assertNotNull(response);
        assertNotNull(response.data());
        assertTrue(response.data() instanceof String);
        assertEquals("4242", response.data());
        assertEquals("4", response.getHeaderValue("Content-Length"));
    }

    private static final HttpHeaderName RANDOM_HEADER = HttpHeaderName.fromString("randomHeader");

    @Test
    public void requestOptionsAddAHeader() {
        Service27 service = createService(Service27.class);

        HttpBinJSON response = service.put(getServerUri(isSecure()), 42,
            new RequestOptions().addHeader(RANDOM_HEADER, "randomValue"));
        assertNotNull(response);
        assertNotNull(response.data());
        assertTrue(response.data() instanceof String);
        assertEquals("42", response.data());
        assertEquals("randomValue", response.getHeaderValue("randomHeader"));
    }

    @Test
    public void requestOptionsSetsAHeader() {
        Service27 service = createService(Service27.class);

        HttpBinJSON response = service.put(getServerUri(isSecure()), 42,
            new RequestOptions().addHeader(RANDOM_HEADER, "randomValue").setHeader(RANDOM_HEADER, "randomValue2"));
        assertNotNull(response);
        assertNotNull(response.data());
        assertTrue(response.data() instanceof String);
        assertEquals("42", response.data());
        assertEquals("randomValue2", response.getHeaderValue("randomHeader"));
    }

    @Host("{url}")
    @ServiceInterface(name = "Service28")
    public interface Service28 {
        @Head("voideagerreadoom")
        @ExpectedResponses({200})
        void headvoid(@HostParam("url") String url);

        @Head("voideagerreadoom")
        @ExpectedResponses({200})
        Void headVoid(@HostParam("url") String url);

        @Head("voideagerreadoom")
        @ExpectedResponses({200})
        Response<Void> headResponseVoid(@HostParam("url") String url);

        @Head("voideagerreadoom")
        @ExpectedResponses({200})
        ResponseBase<Void, Void> headResponseBaseVoid(@HostParam("url") String url);

        @Head("voideagerreadoom")
        @ExpectedResponses({200})
        Mono<Void> headMonoVoid(@HostParam("url") String url);

        @Head("voideagerreadoom")
        @ExpectedResponses({200})
        Mono<Response<Void>> headMonoResponseVoid(@HostParam("url") String url);

        @Head("voideagerreadoom")
        @ExpectedResponses({200})
        Mono<ResponseBase<Void, Void>> headMonoResponseBaseVoid(@HostParam("url") String url);
    }

    @ParameterizedTest
    @MethodSource("voidDoesNotEagerlyReadResponseSupplier")
    public void voidDoesNotEagerlyReadResponse(BiConsumer<String, Service28> executable) {
        assertDoesNotThrow(() -> executable.accept(getServerUri(isSecure()), createService(Service28.class)));
    }

    private static Stream<BiConsumer<String, Service28>> voidDoesNotEagerlyReadResponseSupplier() {
        return Stream.of(
            (url, service28) -> service28.headvoid(url),
            (url, service28) -> service28.headVoid(url),
            (url, service28) -> service28.headResponseVoid(url),
            (url, service28) -> service28.headResponseBaseVoid(url),
            (url, service28) -> service28.headMonoVoid(url).block(),
            (url, service28) -> service28.headMonoResponseVoid(url).block(),
            (url, service28) -> service28.headMonoResponseBaseVoid(url).block()
        );
    }

    @Host("{url}")
    @ServiceInterface(name = "Service29")
    public interface Service29 {
        @Put("voiderrorreturned")
        @ExpectedResponses({200})
        void headvoid(@HostParam("url") String url);

        @Put("voiderrorreturned")
        @ExpectedResponses({200})
        Void headVoid(@HostParam("url") String url);

        @Put("voiderrorreturned")
        @ExpectedResponses({200})
        Response<Void> headResponseVoid(@HostParam("url") String url);

        @Put("voiderrorreturned")
        @ExpectedResponses({200})
        ResponseBase<Void, Void> headResponseBaseVoid(@HostParam("url") String url);

        @Put("voiderrorreturned")
        @ExpectedResponses({200})
        Mono<Void> headMonoVoid(@HostParam("url") String url);

        @Put("voiderrorreturned")
        @ExpectedResponses({200})
        Mono<Response<Void>> headMonoResponseVoid(@HostParam("url") String url);

        @Put("voiderrorreturned")
        @ExpectedResponses({200})
        Mono<ResponseBase<Void, Void>> headMonoResponseBaseVoid(@HostParam("url") String url);
    }

    @ParameterizedTest
    @MethodSource("voidErrorReturnsErrorBodySupplier")
    public void voidErrorReturnsErrorBody(BiConsumer<String, Service29> executable) {
        HttpResponseException exception = assertThrows(HttpResponseException.class,
            () -> executable.accept(getServerUri(isSecure()), createService(Service29.class)));

        assertTrue(exception.getMessage().contains("void exception body thrown"));
    }

    private static Stream<BiConsumer<String, Service29>> voidErrorReturnsErrorBodySupplier() {
        return Stream.of(
            (url, service29) -> service29.headvoid(url),
            (url, service29) -> service29.headVoid(url),
            (url, service29) -> service29.headResponseVoid(url),
            (url, service29) -> service29.headResponseBaseVoid(url),
            (url, service29) -> service29.headMonoVoid(url).block(),
            (url, service29) -> service29.headMonoResponseVoid(url).block(),
            (url, service29) -> service29.headMonoResponseBaseVoid(url).block()
        );
    }

    // Helpers
    protected <T> T createService(Class<T> serviceClass) {
        final HttpClient httpClient = createHttpClient();
        return createService(serviceClass, httpClient);
    }

    protected <T> T createService(Class<T> serviceClass, HttpClient httpClient) {
        final HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .policies(new PortPolicy(getPort(), true))
            .httpClient(httpClient)
            .build();

        return RestProxy.create(serviceClass, httpPipeline);
    }

    private static void assertMatchWithHttpOrHttps(String url1, String url2) {
        final String s1 = "http://" + url1;
        if (s1.equalsIgnoreCase(url2)) {
            return;
        }
        final String s2 = "https://" + url1;
        if (s2.equalsIgnoreCase(url2)) {
            return;
        }
        fail("'" + url2 + "' does not match with '" + s1 + "' or '" + s2 + "'.");
    }
}
