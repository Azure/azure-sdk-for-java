// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.test.common;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Delete;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.FormParam;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.Head;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Headers;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.Patch;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.QueryParam;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.annotation.UnexpectedResponseExceptionType;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.ContentType;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.PortPolicy;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.http.rest.StreamResponse;
import com.azure.core.http.test.common.junitextensions.SyncAsyncExtension;
import com.azure.core.http.test.common.junitextensions.SyncAsyncTest;
import com.azure.core.http.test.common.models.HttpBinFormDataJson;
import com.azure.core.http.test.common.models.HttpBinHeaders;
import com.azure.core.http.test.common.models.HttpBinJson;
import com.azure.core.http.test.common.models.MyRestException;
import com.azure.core.http.test.common.models.PizzaSize;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.Contexts;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.ProgressReporter;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.io.IOUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
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
import reactor.util.function.Tuple3;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.azure.core.http.test.common.HttpTestUtils.assertArraysEqual;
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

    /**
     * The endpoint that the server will echo back the request body.
     */
    protected static final String ECHO_RESPONSE = "echo";

    private static final byte[] EXPECTED_RETURN_BYTES = "Hello World!".getBytes(StandardCharsets.UTF_8);

    private static final String HTTP_REST_PROXY_SYNC_PROXY_ENABLE = "com.azure.core.http.restproxy.syncproxy.enable";

    /**
     * Creates a new instance of {@link HttpClientTests}.
     */
    public HttpClientTests() {
    }

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
     * Tests that a response without a byte order manrk or a 'Content-Type' header encodes using UTF-8.
     */
    @SyncAsyncTest
    public void plainResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        String actual
            = SyncAsyncExtension.execute(() -> sendRequestSync(PLAIN_RESPONSE), () -> sendRequest(PLAIN_RESPONSE));

        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a 'Content-Type' header encodes using the specified charset.
     */
    @SyncAsyncTest
    public void headerResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_16BE);

        String actual
            = SyncAsyncExtension.execute(() -> sendRequestSync(HEADER_RESPONSE), () -> sendRequest(HEADER_RESPONSE));

        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a 'Content-Type' containing an invalid or unsupported charset encodes using UTF-8.
     */
    @SyncAsyncTest
    public void invalidHeaderResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        String actual = SyncAsyncExtension.execute(() -> sendRequestSync(INVALID_HEADER_RESPONSE),
            () -> sendRequest(INVALID_HEADER_RESPONSE));

        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @SyncAsyncTest
    public void utf8BomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        String actual = SyncAsyncExtension.execute(() -> sendRequestSync(UTF_8_BOM_RESPONSE),
            () -> sendRequest(UTF_8_BOM_RESPONSE));

        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @SyncAsyncTest
    public void utf16BeBomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_16BE);

        String actual = SyncAsyncExtension.execute(() -> sendRequestSync(UTF_16BE_BOM_RESPONSE),
            () -> sendRequest(UTF_16BE_BOM_RESPONSE));

        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @SyncAsyncTest
    public void utf16LeBomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_16LE);

        String actual = SyncAsyncExtension.execute(() -> sendRequestSync(UTF_16LE_BOM_RESPONSE),
            () -> sendRequest(UTF_16LE_BOM_RESPONSE));

        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @SyncAsyncTest
    public void utf32BeBomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, Charset.forName("UTF-32BE"));

        String actual = SyncAsyncExtension.execute(() -> sendRequestSync(UTF_32BE_BOM_RESPONSE),
            () -> sendRequest(UTF_32BE_BOM_RESPONSE));

        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @SyncAsyncTest
    public void utf32LeBomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, Charset.forName("UTF-32LE"));

        String actual = SyncAsyncExtension.execute(() -> sendRequestSync(UTF_32LE_BOM_RESPONSE),
            () -> sendRequest(UTF_32LE_BOM_RESPONSE));

        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a byte order marker and 'Content-Type' header will defer to using the BOM encoding.
     */
    @SyncAsyncTest
    public void bomWithSameHeader() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        String actual = SyncAsyncExtension.execute(() -> sendRequestSync(BOM_WITH_SAME_HEADER),
            () -> sendRequest(BOM_WITH_SAME_HEADER));

        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a byte order marker and 'Content-Type' header will defer to using the BOM encoding.
     */
    @SyncAsyncTest
    public void bomWithDifferentHeader() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        String actual = SyncAsyncExtension.execute(() -> sendRequestSync(BOM_WITH_DIFFERENT_HEADER),
            () -> sendRequest(BOM_WITH_DIFFERENT_HEADER));

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
        HttpRequest request
            = new HttpRequest(HttpMethod.PUT, getRequestUrl(ECHO_RESPONSE), new HttpHeaders(), requestBody);

        Supplier<HttpResponse> responseSupplier = () -> SyncAsyncExtension
            .execute(() -> createHttpClient().sendSync(request, Context.NONE), () -> createHttpClient().send(request));

        assertEquals(requestBody.toString(), responseSupplier.get().getBodyAsString().block());
        assertArraysEqual(requestBody.toBytes(), responseSupplier.get().getBodyAsByteArray().block());
        assertArraysEqual(requestBody.toBytes(), responseSupplier.get().getBodyAsBinaryData().toBytes());
        assertArraysEqual(requestBody.toBytes(),
            responseSupplier.get().getBodyAsInputStream().map(s -> BinaryData.fromStream(s).toBytes()).block());
        assertArraysEqual(requestBody.toBytes(),
            BinaryData.fromFlux(responseSupplier.get().getBody()).map(BinaryData::toBytes).block());
        assertArraysEqual(requestBody.toBytes(), getResponseBytesViaWritableChannel(responseSupplier.get()));
        assertArraysEqual(requestBody.toBytes(), getResponseBytesViaAsynchronousChannel(responseSupplier.get()));
    }

    /**
     * Tests that client returns buffered response if requested via azure-eagerly-read-response Context flag.
     */
    @SyncAsyncTest
    public void shouldBufferResponse() {
        HttpRequest request = new HttpRequest(HttpMethod.PUT, getRequestUrl(ECHO_RESPONSE), new HttpHeaders(),
            BinaryData.fromString("test body"));

        Context context = Context.NONE.addData("azure-eagerly-read-response", true);

        HttpResponse response = SyncAsyncExtension.execute(() -> createHttpClient().sendSync(request, context),
            () -> createHttpClient().send(request, context));

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
        HttpRequest request
            = new HttpRequest(HttpMethod.PUT, getRequestUrl(ECHO_RESPONSE), new HttpHeaders(), requestBody);

        Context context = Context.NONE.addData("azure-eagerly-read-response", true);

        HttpResponse response = SyncAsyncExtension.execute(() -> createHttpClient().sendSync(request, context),
            () -> createHttpClient().send(request, context));

        // Read response twice using all accessors.
        assertEquals(requestBody.toString(), response.getBodyAsString().block());
        assertEquals(requestBody.toString(), response.getBodyAsString().block());

        assertArraysEqual(requestBody.toBytes(), response.getBodyAsByteArray().block());
        assertArraysEqual(requestBody.toBytes(), response.getBodyAsByteArray().block());

        assertArraysEqual(requestBody.toBytes(), response.getBodyAsBinaryData().toBytes());
        assertArraysEqual(requestBody.toBytes(), response.getBodyAsBinaryData().toBytes());

        assertArraysEqual(requestBody.toBytes(),
            response.getBodyAsInputStream().map(s -> BinaryData.fromStream(s).toBytes()).block());
        assertArraysEqual(requestBody.toBytes(),
            response.getBodyAsInputStream().map(s -> BinaryData.fromStream(s).toBytes()).block());

        assertArraysEqual(requestBody.toBytes(),
            BinaryData.fromFlux(response.getBody()).map(BinaryData::toBytes).block());
        assertArraysEqual(requestBody.toBytes(),
            BinaryData.fromFlux(response.getBody()).map(BinaryData::toBytes).block());

        assertArraysEqual(requestBody.toBytes(), getResponseBytesViaWritableChannel(response));
        assertArraysEqual(requestBody.toBytes(), getResponseBytesViaWritableChannel(response));

        assertArraysEqual(requestBody.toBytes(), getResponseBytesViaAsynchronousChannel(response));
        assertArraysEqual(requestBody.toBytes(), getResponseBytesViaAsynchronousChannel(response));
    }

    /**
     * Tests that eagerly converting implementation HTTP headers to azure-core HttpHeaders is done.
     */
    @SyncAsyncTest
    public void eagerlyConvertedHeadersAreHttpHeaders() {
        BinaryData requestBody = BinaryData.fromString("test body");
        HttpRequest request
            = new HttpRequest(HttpMethod.PUT, getRequestUrl(ECHO_RESPONSE), new HttpHeaders(), requestBody);

        Context context = Context.NONE.addData("azure-eagerly-convert-headers", true);

        try (HttpResponse response = SyncAsyncExtension.execute(() -> createHttpClient().sendSync(request, context),
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
     * @param consumer The consumer that sends the request and validates the response.
     */
    @ParameterizedTest
    @MethodSource("byteArrayBinaryData")
    public void canSendByteArrayBinaryData(BinaryData requestBody, byte[] expectedResponseBody,
        BinaryDataTestConsumer consumer) {
        consumer.accept(getRequestUrl(ECHO_RESPONSE), requestBody, createHttpClient(), expectedResponseBody);
    }

    /**
     * Tests that send random bytes in various forms to an endpoint that echoes bytes back to sender.
     *
     * @param requestBody The BinaryData that contains random bytes.
     * @param expectedResponseBody The expected bytes in the echo response.
     * @param consumer The consumer that sends the request and validates the response.
     */
    @ParameterizedTest
    @MethodSource("byteBufferBinaryData")
    public void canSendByteBufferBinaryData(BinaryData requestBody, byte[] expectedResponseBody,
        BinaryDataTestConsumer consumer) {
        consumer.accept(getRequestUrl(ECHO_RESPONSE), requestBody, createHttpClient(), expectedResponseBody);
    }

    /**
     * Tests that send random bytes in various forms to an endpoint that echoes bytes back to sender.
     *
     * @param requestBody The BinaryData that contains random bytes.
     * @param expectedResponseBody The expected bytes in the echo response.
     * @param consumer The consumer that sends the request and validates the response.
     */
    @ParameterizedTest
    @MethodSource("stringBinaryData")
    public void canSendStringBinaryData(BinaryData requestBody, byte[] expectedResponseBody,
        BinaryDataTestConsumer consumer) {
        consumer.accept(getRequestUrl(ECHO_RESPONSE), requestBody, createHttpClient(), expectedResponseBody);
    }

    /**
     * Tests that send random bytes in various forms to an endpoint that echoes bytes back to sender.
     *
     * @param requestBody The BinaryData that contains random bytes.
     * @param expectedResponseBody The expected bytes in the echo response.
     * @param consumer The consumer that sends the request and validates the response.
     */
    @ParameterizedTest
    @MethodSource("streamBinaryData")
    public void canSendStreamBinaryData(BinaryData requestBody, byte[] expectedResponseBody,
        BinaryDataTestConsumer consumer) {
        consumer.accept(getRequestUrl(ECHO_RESPONSE), requestBody, createHttpClient(), expectedResponseBody);
    }

    /**
     * Tests that send random bytes in various forms to an endpoint that echoes bytes back to sender.
     *
     * @param requestBody The BinaryData that contains random bytes.
     * @param expectedResponseBody The expected bytes in the echo response.
     * @param consumer The consumer that sends the request and validates the response.
     */
    @ParameterizedTest
    @MethodSource("unknownLengthNoBufferFluxBinaryData")
    public void canSendUnknownLengthNoBufferFluxBinaryData(BinaryData requestBody, byte[] expectedResponseBody,
        BinaryDataTestConsumer consumer) {
        consumer.accept(getRequestUrl(ECHO_RESPONSE), requestBody, createHttpClient(), expectedResponseBody);
    }

    /**
     * Tests that send random bytes in various forms to an endpoint that echoes bytes back to sender.
     *
     * @param requestBody The BinaryData that contains random bytes.
     * @param expectedResponseBody The expected bytes in the echo response.
     * @param consumer The consumer that sends the request and validates the response.
     */
    @ParameterizedTest
    @MethodSource("knownLengthNoBufferFluxBinaryData")
    public void canSendKnownLengthNoBufferFluxBinaryData(BinaryData requestBody, byte[] expectedResponseBody,
        BinaryDataTestConsumer consumer) {
        consumer.accept(getRequestUrl(ECHO_RESPONSE), requestBody, createHttpClient(), expectedResponseBody);
    }

    /**
     * Tests that send random bytes in various forms to an endpoint that echoes bytes back to sender.
     *
     * @param requestBody The BinaryData that contains random bytes.
     * @param expectedResponseBody The expected bytes in the echo response.
     * @param consumer The consumer that sends the request and validates the response.
     */
    @ParameterizedTest
    @MethodSource("unknownLengthNoBufferAsyncFluxBinaryData")
    public void canSendUnknownLengthNoBufferAsyncFluxBinaryData(BinaryData requestBody, byte[] expectedResponseBody,
        BinaryDataTestConsumer consumer) {
        consumer.accept(getRequestUrl(ECHO_RESPONSE), requestBody, createHttpClient(), expectedResponseBody);
    }

    /**
     * Tests that send random bytes in various forms to an endpoint that echoes bytes back to sender.
     *
     * @param requestBody The BinaryData that contains random bytes.
     * @param expectedResponseBody The expected bytes in the echo response.
     * @param consumer The consumer that sends the request and validates the response.
     */
    @ParameterizedTest
    @MethodSource("knownLengthNoBufferAsyncFluxBinaryData")
    @Disabled
    public void canSendKnownLengthNoBufferAsyncFluxBinaryData(BinaryData requestBody, byte[] expectedResponseBody,
        BinaryDataTestConsumer consumer) {
        consumer.accept(getRequestUrl(ECHO_RESPONSE), requestBody, createHttpClient(), expectedResponseBody);
    }

    /**
     * Tests that send random bytes in various forms to an endpoint that echoes bytes back to sender.
     *
     * @param requestBody The BinaryData that contains random bytes.
     * @param expectedResponseBody The expected bytes in the echo response.
     * @param consumer The consumer that sends the request and validates the response.
     */
    @ParameterizedTest
    @MethodSource("objectBinaryData")
    public void canSendObjectBinaryData(BinaryData requestBody, byte[] expectedResponseBody,
        BinaryDataTestConsumer consumer) {
        consumer.accept(getRequestUrl(ECHO_RESPONSE), requestBody, createHttpClient(), expectedResponseBody);
    }

    /**
     * Tests that send random bytes in various forms to an endpoint that echoes bytes back to sender.
     *
     * @param requestBody The BinaryData that contains random bytes.
     * @param expectedResponseBody The expected bytes in the echo response.
     * @param consumer The consumer that sends the request and validates the response.
     */
    @ParameterizedTest
    @MethodSource("fileBinaryData")
    public void canSendFileBinaryData(BinaryData requestBody, byte[] expectedResponseBody,
        BinaryDataTestConsumer consumer) {
        consumer.accept(getRequestUrl(ECHO_RESPONSE), requestBody, createHttpClient(), expectedResponseBody);
    }

    /**
     * Tests that send random bytes in various forms to an endpoint that echoes bytes back to sender.
     *
     * @param requestBody The BinaryData that contains random bytes.
     * @param expectedResponseBody The expected bytes in the echo response.
     * @param consumer The consumer that sends the request and validates the response.
     */
    @ParameterizedTest
    @MethodSource("sliceFileBinaryData")
    public void canSendSliceFileBinaryData(BinaryData requestBody, byte[] expectedResponseBody,
        BinaryDataTestConsumer consumer) {
        consumer.accept(getRequestUrl(ECHO_RESPONSE), requestBody, createHttpClient(), expectedResponseBody);
    }

    private static void canSendBinaryData(URL requestUrl, BinaryData requestBody, HttpClient httpClient,
        byte[] expectedResponseBody) {
        HttpRequest request = new HttpRequest(HttpMethod.PUT, requestUrl, new HttpHeaders(), requestBody);

        StepVerifier.create(httpClient.send(request).flatMap(HttpResponse::getBodyAsByteArray))
            .assertNext(responseBytes -> assertArraysEqual(expectedResponseBody, responseBytes))
            .verifyComplete();
    }

    private static void canSendBinaryDataSync(URL requestUrl, BinaryData requestBody, HttpClient httpClient,
        byte[] expectedResponseBody) {
        HttpRequest request = new HttpRequest(HttpMethod.PUT, requestUrl, new HttpHeaders(), requestBody);

        try (HttpResponse httpResponse = httpClient.sendSync(request, Context.NONE)) {
            byte[] responseBytes = httpResponse.getBodyAsBinaryData().toBytes();
            assertArraysEqual(expectedResponseBody, responseBytes);
        }
    }

    private static void canSendBinaryDataWithProgressReporter(URL requestUrl, BinaryData requestBody,
        HttpClient httpClient, byte[] expectedResponseBody) {
        HttpRequest request = new HttpRequest(HttpMethod.PUT, requestUrl, new HttpHeaders(), requestBody);

        AtomicLong progress = new AtomicLong();
        Context context = Contexts.empty()
            .setHttpRequestProgressReporter(ProgressReporter.withProgressListener(progress::set))
            .getContext();

        StepVerifier.create(httpClient.send(request, context).flatMap(HttpResponse::getBodyAsByteArray))
            .assertNext(responseBytes -> assertArraysEqual(expectedResponseBody, responseBytes))
            .verifyComplete();

        assertEquals(expectedResponseBody.length, progress.intValue());
    }

    private static void canSendBinaryDataSyncWithProgressReporter(URL requestUrl, BinaryData requestBody,
        HttpClient httpClient, byte[] expectedResponseBody) {
        HttpRequest request = new HttpRequest(HttpMethod.PUT, requestUrl, new HttpHeaders(), requestBody);

        AtomicLong progress = new AtomicLong();
        Context context = Contexts.empty()
            .setHttpRequestProgressReporter(ProgressReporter.withProgressListener(progress::set))
            .getContext();

        try (HttpResponse httpResponse = httpClient.sendSync(request, context)) {
            byte[] responseBytes = httpResponse.getBodyAsBinaryData().toBytes();
            assertArraysEqual(expectedResponseBody, responseBytes);
            assertEquals(expectedResponseBody.length, progress.intValue());
        }
    }

    private static Stream<byte[]> getChunks() {
        return IntStream.of(1, 2, 10, 127, 1024, 1024 + 157, 8 * 1024 + 3, 10 * 1024 * 1024 + 13)
            .mapToObj(HttpClientTests::getBytes);
    }

    private static Stream<BinaryDataTestConsumer> getTestConsumers() {
        return Stream.of(HttpClientTests::canSendBinaryData, HttpClientTests::canSendBinaryDataSync,
            HttpClientTests::canSendBinaryDataWithProgressReporter,
            HttpClientTests::canSendBinaryDataSyncWithProgressReporter);
    }

    private static Stream<Arguments> createBinaryDataTestSet(Function<byte[], BinaryData> binaryDataCreator) {
        return getChunks().flatMap(
            bytes -> getTestConsumers().map(consumer -> Arguments.of(binaryDataCreator.apply(bytes), bytes, consumer)));
    }

    /**
     * Interface defining a consumer that accepts a URL, BinaryData, HttpClient, and byte[] used to test sending
     * BinaryData to an endpoint.
     */
    public interface BinaryDataTestConsumer {
        /**
         * Accepts a URL, BinaryData, HttpClient, and byte[] used to test sending BinaryData to an endpoint.
         *
         * @param requestUrl The URL to send the request to.
         * @param binaryData The BinaryData to send in the request.
         * @param httpClient The HttpClient to use to send the request.
         * @param expectedResponseBody The expected bytes in the echo response.
         */
        void accept(URL requestUrl, BinaryData binaryData, HttpClient httpClient, byte[] expectedResponseBody);
    }

    private static Stream<Arguments> byteArrayBinaryData() {
        return createBinaryDataTestSet(BinaryData::fromBytes);
    }

    private static Stream<Arguments> byteBufferBinaryData() {
        return createBinaryDataTestSet(bytes -> BinaryData.fromByteBuffer(ByteBuffer.wrap(bytes)));
    }

    private static Stream<Arguments> stringBinaryData() {
        return createBinaryDataTestSet(bytes -> BinaryData.fromString(new String(bytes, StandardCharsets.UTF_8)));
    }

    private static Stream<Arguments> streamBinaryData() {
        return createBinaryDataTestSet(
            bytes -> BinaryData.fromStream(new ByteArrayInputStream(bytes), (long) bytes.length));
    }

    private static List<ByteBuffer> getFluxByteBuffers(byte[] bytes) {
        List<ByteBuffer> bufferList = new ArrayList<>();
        int bufferSize = 1023;
        for (int startIndex = 0; startIndex < bytes.length; startIndex += bufferSize) {
            bufferList.add(ByteBuffer.wrap(bytes, startIndex, Math.min(bytes.length - startIndex, bufferSize)));
        }

        return bufferList;
    }

    private static Stream<Arguments> unknownLengthNoBufferFluxBinaryData() {
        return createBinaryDataTestSet(bytes -> BinaryData
            .fromFlux(Flux.fromIterable(getFluxByteBuffers(bytes)).map(ByteBuffer::duplicate), null, false)
            .block());
    }

    private static Stream<Arguments> knownLengthNoBufferFluxBinaryData() {
        return createBinaryDataTestSet(
            bytes -> BinaryData
                .fromFlux(Flux.fromIterable(getFluxByteBuffers(bytes)).map(ByteBuffer::duplicate), (long) bytes.length,
                    false)
                .block());
    }

    private static Stream<Arguments> unknownLengthNoBufferAsyncFluxBinaryData() {
        return createBinaryDataTestSet(bytes -> BinaryData.fromFlux(
            Flux.fromIterable(getFluxByteBuffers(bytes)).map(ByteBuffer::duplicate).delayElements(Duration.ofNanos(10)),
            null, false).block());
    }

    private static Stream<Arguments> knownLengthNoBufferAsyncFluxBinaryData() {
        return createBinaryDataTestSet(bytes -> BinaryData.fromFlux(
            Flux.fromIterable(getFluxByteBuffers(bytes)).map(ByteBuffer::duplicate).delayElements(Duration.ofNanos(10)),
            (long) bytes.length, false).block());
    }

    private static Stream<Arguments> objectBinaryData() {
        return createBinaryDataTestSet(bytes -> BinaryData.fromObject(bytes, new ByteArraySerializer()));
    }

    private static Stream<Arguments> fileBinaryData() {
        return getChunks().flatMap(bytes -> getTestConsumers().map(consumer -> {
            try {
                Path wholeFile = Files.createTempFile("http-client-tests", null);
                wholeFile.toFile().deleteOnExit();
                Files.write(wholeFile, bytes);

                return Arguments.of(BinaryData.fromFile(wholeFile), bytes, consumer);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }));
    }

    private static Stream<Arguments> sliceFileBinaryData() {
        return getChunks().flatMap(bytes -> getTestConsumers().map(consumer -> {
            try {
                Path sliceFile = Files.createTempFile("http-client-tests", null);
                sliceFile.toFile().deleteOnExit();
                Files.write(sliceFile, new byte[1023], StandardOpenOption.APPEND);
                Files.write(sliceFile, bytes, StandardOpenOption.APPEND);
                Files.write(sliceFile, new byte[1023], StandardOpenOption.APPEND);

                return Arguments.of(BinaryData.fromFile(sliceFile, 1023L, (long) bytes.length), bytes, consumer);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }));
    }

    private static final byte[] RANDOM_BYTES = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17 };

    private static byte[] getBytes(int size) {
        byte[] bytes = new byte[size];

        int copies = size / RANDOM_BYTES.length;
        int remainder = size % RANDOM_BYTES.length;

        for (int i = 0; i < copies; i++) {
            System.arraycopy(RANDOM_BYTES, 0, bytes, i * RANDOM_BYTES.length, RANDOM_BYTES.length);
        }

        if (remainder > 0) {
            System.arraycopy(RANDOM_BYTES, 0, bytes, copies * RANDOM_BYTES.length, remainder);
        }

        return bytes;
    }

    private Mono<String> sendRequest(String requestPath) {
        return createHttpClient().send(new HttpRequest(HttpMethod.GET, getRequestUrl(requestPath)))
            .flatMap(HttpResponse::getBodyAsString);
    }

    private String sendRequestSync(String requestPath) {
        try (HttpResponse httpResponse
            = createHttpClient().sendSync(new HttpRequest(HttpMethod.GET, getRequestUrl(requestPath)), Context.NONE)) {
            return httpResponse.getBodyAsString().block();
        }
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
            try (AsynchronousByteChannel channel = IOUtils
                .toAsynchronousByteChannel(AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE), 0)) {
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
        @ExpectedResponses({ 200 })
        byte[] getByteArray(@HostParam("url") String url);

        @Get("bytes/100")
        @ExpectedResponses({ 200 })
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
        @ExpectedResponses({ 200 })
        byte[] getByteArray(@HostParam("scheme") String scheme, @HostParam("hostName") String host,
            @PathParam("numberOfBytes") int numberOfBytes);

        @Get("bytes/{numberOfBytes}")
        @ExpectedResponses({ 200 })
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
        @ExpectedResponses({ 200 })
        void getNothing(@HostParam("url") String url);

        @Get("bytes/100")
        @ExpectedResponses({ 200 })
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
        StepVerifier.create(createService(Service3.class).getNothingAsync(getRequestUri())).verifyComplete();
    }

    @Host("{url}")
    @ServiceInterface(name = "Service5")
    private interface Service5 {
        @Get("anything")
        @ExpectedResponses({ 200 })
        HttpBinJson getAnything(@HostParam("url") String url);

        @Get("anything/with+plus")
        @ExpectedResponses({ 200 })
        HttpBinJson getAnythingWithPlus(@HostParam("url") String url);

        @Get("anything/{path}")
        @ExpectedResponses({ 200 })
        HttpBinJson getAnythingWithPathParam(@HostParam("url") String url, @PathParam("path") String pathParam);

        @Get("anything/{path}")
        @ExpectedResponses({ 200 })
        HttpBinJson getAnythingWithEncodedPathParam(@HostParam("url") String url,
            @PathParam(value = "path", encoded = true) String pathParam);

        @Get("anything")
        @ExpectedResponses({ 200 })
        Mono<HttpBinJson> getAnythingAsync(@HostParam("url") String url);
    }

    /**
     * Tests that a response with a return type of {@link HttpBinJson} is correctly handled.
     */
    @Test
    public void syncGetRequestWithAnything() {
        final HttpBinJson json = createService(Service5.class).getAnything(getRequestUri());

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything", json.url());
    }

    /**
     * Tests that a request with a path containing a plus (+) is correctly handled.
     */
    @Test
    public void syncGetRequestWithAnythingWithPlus() {
        final HttpBinJson json = createService(Service5.class).getAnythingWithPlus(getRequestUri());

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with+plus", json.url());
    }

    /**
     * Tests that a request with a path parameter ({@link PathParam}) is correctly handled.
     */
    @Test
    public void syncGetRequestWithAnythingWithPathParam() {
        final HttpBinJson json
            = createService(Service5.class).getAnythingWithPathParam(getRequestUri(), "withpathparam");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/withpathparam", json.url());
    }

    /**
     * Tests that a request with a path parameter ({@link PathParam}) that needs encoding is correctly handled.
     */
    @Test
    public void syncGetRequestWithAnythingWithPathParamWithSpace() {
        final HttpBinJson json
            = createService(Service5.class).getAnythingWithPathParam(getRequestUri(), "with path param");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with path param", json.url());
    }

    /**
     * Tests that a request with a path parameter ({@link PathParam}) that needs encoding is correctly handled.
     */
    @Test
    public void syncGetRequestWithAnythingWithPathParamWithPlus() {
        final HttpBinJson json
            = createService(Service5.class).getAnythingWithPathParam(getRequestUri(), "with+path+param");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with+path+param", json.url());
    }

    /**
     * Tests that a request with a path parameter ({@link PathParam}) that is already encoded is correctly handled.
     */
    @Test
    public void syncGetRequestWithAnythingWithEncodedPathParam() {
        final HttpBinJson json
            = createService(Service5.class).getAnythingWithEncodedPathParam(getRequestUri(), "withpathparam");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/withpathparam", json.url());
    }

    /**
     * Tests that a request with a path parameter ({@link PathParam}) that is already encoded is correctly handled.
     */
    @Test
    public void syncGetRequestWithAnythingWithEncodedPathParamWithPercent20() {
        final HttpBinJson json
            = createService(Service5.class).getAnythingWithEncodedPathParam(getRequestUri(), "with%20path%20param");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with path param", json.url());
    }

    /**
     * Tests that a request with a path parameter ({@link PathParam}) that is already encoded is correctly handled.
     */
    @Test
    public void syncGetRequestWithAnythingWithEncodedPathParamWithPlus() {
        final HttpBinJson json
            = createService(Service5.class).getAnythingWithEncodedPathParam(getRequestUri(), "with+path+param");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with+path+param", json.url());
    }

    /**
     * Tests that a response with a return type of {@link HttpBinJson} is correctly handled.
     */
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
        @ExpectedResponses({ 200 })
        HttpBinJson getAnything(@HostParam("url") String url, @QueryParam("a") String a, @QueryParam("b") int b);

        @Get("anything")
        @ExpectedResponses({ 200 })
        HttpBinJson getAnythingWithEncoded(@HostParam("url") String url,
            @QueryParam(value = "a", encoded = true) String a, @QueryParam("b") int b);

        @Get("anything")
        @ExpectedResponses({ 200 })
        Mono<HttpBinJson> getAnythingAsync(@HostParam("url") String url, @QueryParam("a") String a,
            @QueryParam("b") int b);
    }

    /**
     * Tests that a request with query parameters ({@link QueryParam}) that need encoding is correctly handled.
     */
    @Test
    public void syncGetRequestWithQueryParametersAndAnything() {
        final HttpBinJson json = createService(Service6.class).getAnything(getRequestUri(), "A", 15);

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything?a=A&b=15", json.url());
    }

    /**
     * Tests that a request with query parameters ({@link QueryParam}) that need encoding is correctly handled.
     */
    @Test
    public void syncGetRequestWithQueryParametersAndAnythingWithPercent20() {
        final HttpBinJson json = createService(Service6.class).getAnything(getRequestUri(), "A%20Z", 15);

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything?a=A%2520Z&b=15", json.url());
    }

    /**
     * Tests that a request with query parameters ({@link QueryParam}) where some need encoding and some are already
     * encoded is correctly handled.
     */
    @Test
    public void syncGetRequestWithQueryParametersAndAnythingWithEncodedWithPercent20() {
        final HttpBinJson json = createService(Service6.class).getAnythingWithEncoded(getRequestUri(), "x%20y", 15);

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything?a=x y&b=15", json.url());
    }

    /**
     * Tests that a request with query parameters ({@link QueryParam}) that need encoding is correctly handled.
     */
    @Test
    public void asyncGetRequestWithQueryParametersAndAnything() {
        StepVerifier.create(createService(Service6.class).getAnythingAsync(getRequestUri(), "A", 15))
            .assertNext(json -> assertMatchWithHttpOrHttps("localhost/anything?a=A&b=15", json.url()))
            .verifyComplete();
    }

    /**
     * Tests that a request with query parameters ({@link QueryParam}) that need encoding where a value is null is
     * correctly handled.
     */
    @Test
    public void syncGetRequestWithNullQueryParameter() {
        final HttpBinJson json = createService(Service6.class).getAnything(getRequestUri(), null, 15);

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything?b=15", json.url());
    }

    @Host("{url}")
    @ServiceInterface(name = "Service7")
    private interface Service7 {
        @Get("anything")
        @ExpectedResponses({ 200 })
        HttpBinJson getAnything(@HostParam("url") String url, @HeaderParam("a") String a, @HeaderParam("b") int b);

        @Get("anything")
        @ExpectedResponses({ 200 })
        Mono<HttpBinJson> getAnythingAsync(@HostParam("url") String url, @HeaderParam("a") String a,
            @HeaderParam("b") int b);
    }

    private static final HttpHeaderName HEADER_A = HttpHeaderName.fromString("A");
    private static final HttpHeaderName HEADER_B = HttpHeaderName.fromString("B");

    /**
     * Tests that a request with header parameters ({@link HeaderParam}) is correctly handled.
     */
    @Test
    public void syncGetRequestWithHeaderParametersAndAnythingReturn() {
        final HttpBinJson json = createService(Service7.class).getAnything(getRequestUri(), "A", 15);

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything", json.url());
        assertNotNull(json.headers());
        final HttpHeaders headers = new HttpHeaders().setAll(json.headers());

        assertEquals("A", headers.getValue(HEADER_A));
        assertArrayEquals(new String[] { "A" }, headers.getValues(HEADER_A));

        assertEquals("15", headers.getValue(HEADER_B));
        assertArrayEquals(new String[] { "15" }, headers.getValues(HEADER_B));
    }

    /**
     * Tests that a request with header parameters ({@link HeaderParam}) is correctly handled.
     */
    @Test
    public void asyncGetRequestWithHeaderParametersAndAnything() {
        StepVerifier.create(createService(Service7.class).getAnythingAsync(getRequestUri(), "A", 15))
            .assertNext(json -> {
                assertMatchWithHttpOrHttps("localhost/anything", json.url());
                assertNotNull(json.headers());
                final HttpHeaders headers = new HttpHeaders().setAll(json.headers());

                assertEquals("A", headers.getValue(HEADER_A));
                assertArrayEquals(new String[] { "A" }, headers.getValues(HEADER_A));

                assertEquals("15", headers.getValue(HEADER_B));
                assertArrayEquals(new String[] { "15" }, headers.getValues(HEADER_B));
            })
            .verifyComplete();
    }

    /**
     * Tests that a request with header parameters ({@link HeaderParam}) where a value is null is correctly handled.
     */
    @Test
    public void syncGetRequestWithNullHeader() {
        final HttpBinJson json = createService(Service7.class).getAnything(getRequestUri(), null, 15);

        final HttpHeaders headers = new HttpHeaders().setAll(json.headers());

        assertNull(headers.getValue(HEADER_A));
        assertArrayEquals(null, headers.getValues(HEADER_A));

        assertEquals("15", headers.getValue(HEADER_B));
        assertArrayEquals(new String[] { "15" }, headers.getValues(HEADER_B));
    }

    @Host("{url}")
    @ServiceInterface(name = "Service8")
    private interface Service8 {
        @Post("post")
        @ExpectedResponses({ 200 })
        HttpBinJson post(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String postBody);

        @Post("post")
        @ExpectedResponses({ 200 })
        Mono<HttpBinJson> postAsync(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String postBody);
    }

    /**
     * Tests that a request with an octet-stream body is correctly handled.
     */
    @Test
    public void syncPostRequestWithStringBody() {
        final HttpBinJson json = createService(Service8.class).post(getRequestUri(), "I'm a post body!");

        assertEquals(String.class, json.data().getClass());
        assertEquals("I'm a post body!", json.data());
    }

    /**
     * Tests that a request with an octet-stream body is correctly handled.
     */
    @Test
    public void asyncPostRequestWithStringBody() {
        StepVerifier.create(createService(Service8.class).postAsync(getRequestUri(), "I'm a post body!"))
            .assertNext(json -> {
                assertEquals(String.class, json.data().getClass());
                assertEquals("I'm a post body!", json.data());
            })
            .verifyComplete();
    }

    /**
     * Tests that a request with an octet-stream body where the body is null is correctly handled.
     */
    @Test
    public void syncPostRequestWithNullBody() {
        final HttpBinJson result = createService(Service8.class).post(getRequestUri(), null);

        assertEquals("", result.data());
    }

    @SuppressWarnings("UnusedReturnValue")
    @Host("{url}")
    @ServiceInterface(name = "Service9")
    private interface Service9 {
        @Put("put")
        @ExpectedResponses({ 200 })
        HttpBinJson put(@HostParam("url") String url, @BodyParam(ContentType.APPLICATION_OCTET_STREAM) int putBody);

        @Put("put")
        @ExpectedResponses({ 200 })
        Mono<HttpBinJson> putAsync(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) int putBody);

        @Put("put")
        @ExpectedResponses({ 200 })
        @UnexpectedResponseExceptionType(MyRestException.class)
        HttpBinJson putBodyAndContentLength(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) ByteBuffer body,
            @HeaderParam("Content-Length") long contentLength);

        @Put("put")
        @ExpectedResponses({ 200 })
        @UnexpectedResponseExceptionType(MyRestException.class)
        Mono<HttpBinJson> putAsyncBodyAndContentLength(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) Flux<ByteBuffer> body,
            @HeaderParam("Content-Length") long contentLength);

        @Put("put")
        @ExpectedResponses({ 200 })
        @UnexpectedResponseExceptionType(MyRestException.class)
        Mono<HttpBinJson> putAsyncBodyAndContentLength(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) BinaryData body,
            @HeaderParam("Content-Length") long contentLength);

        @Put("put")
        @ExpectedResponses({ 201 })
        HttpBinJson putWithUnexpectedResponse(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({ 201 })
        Mono<HttpBinJson> putWithUnexpectedResponseAsync(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({ 201 })
        @UnexpectedResponseExceptionType(MyRestException.class)
        HttpBinJson putWithUnexpectedResponseAndExceptionType(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({ 201 })
        @UnexpectedResponseExceptionType(MyRestException.class)
        Mono<HttpBinJson> putWithUnexpectedResponseAndExceptionTypeAsync(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({ 201 })
        @UnexpectedResponseExceptionType(code = { 200 }, value = MyRestException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        HttpBinJson putWithUnexpectedResponseAndDeterminedExceptionType(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({ 201 })
        @UnexpectedResponseExceptionType(code = { 200 }, value = MyRestException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<HttpBinJson> putWithUnexpectedResponseAndDeterminedExceptionTypeAsync(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({ 201 })
        @UnexpectedResponseExceptionType(code = { 400 }, value = HttpResponseException.class)
        @UnexpectedResponseExceptionType(MyRestException.class)
        HttpBinJson putWithUnexpectedResponseAndFallthroughExceptionType(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({ 201 })
        @UnexpectedResponseExceptionType(code = { 400 }, value = HttpResponseException.class)
        @UnexpectedResponseExceptionType(MyRestException.class)
        Mono<HttpBinJson> putWithUnexpectedResponseAndFallthroughExceptionTypeAsync(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({ 201 })
        @UnexpectedResponseExceptionType(code = { 400 }, value = MyRestException.class)
        HttpBinJson putWithUnexpectedResponseAndNoFallthroughExceptionType(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({ 201 })
        @UnexpectedResponseExceptionType(code = { 400 }, value = MyRestException.class)
        Mono<HttpBinJson> putWithUnexpectedResponseAndNoFallthroughExceptionTypeAsync(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);
    }

    /**
     * Tests that a request with an octet-stream body where th body is a single value binary is correctly handled.
     */
    @Test
    public void syncPutRequestWithIntBody() {
        final HttpBinJson json = createService(Service9.class).put(getRequestUri(), 42);

        assertEquals(String.class, json.data().getClass());
        assertEquals("42", json.data());
    }

    /**
     * Tests that a request with an octet-stream body where th body is a single value binary is correctly handled.
     */
    @Test
    public void asyncPutRequestWithIntBody() {
        StepVerifier.create(createService(Service9.class).putAsync(getRequestUri(), 42)).assertNext(json -> {
            assertEquals(String.class, json.data().getClass());
            assertEquals("42", json.data());
        }).verifyComplete();
    }

    // Test all scenarios for the body length and content length comparison for sync API

    /**
     * Tests that a request with an octet-stream body where the body length is equal to the content length is
     * correctly handled.
     */
    @Test
    public void syncPutRequestWithBodyAndEqualContentLength() {
        ByteBuffer body = ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8));
        final HttpBinJson json = createService(Service9.class).putBodyAndContentLength(getRequestUri(), body, 4L);

        assertEquals("test", json.data());
        assertEquals(ContentType.APPLICATION_OCTET_STREAM, json.getHeaderValue("Content-Type"));
        assertEquals("4", json.getHeaderValue("Content-Length"));
    }

    /**
     * Tests that a request with an octet-stream body where the body length is less than the content length is
     * correctly handled.
     */
    @Test
    public void syncPutRequestWithBodyLessThanContentLength() {
        ByteBuffer body = ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8));
        Exception unexpectedLengthException = assertThrows(Exception.class, () -> {
            createService(Service9.class).putBodyAndContentLength(getRequestUri(), body, 5L);
            body.clear();
        });
        assertTrue(unexpectedLengthException.getMessage().contains("less than"));
    }

    /**
     * Tests that a request with an octet-stream body where the body length is greater than the content length is
     * correctly handled.
     */
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

    /**
     * Tests that a request with an octet-stream body where the body length is equal to the content length is
     * correctly handled.
     */
    @Test
    public void asyncPutRequestWithBodyAndEqualContentLength() {
        Flux<ByteBuffer> body = Flux.just(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
        StepVerifier.create(createService(Service9.class).putAsyncBodyAndContentLength(getRequestUri(), body, 4L))
            .assertNext(json -> {
                assertEquals("test", json.data());
                assertEquals(ContentType.APPLICATION_OCTET_STREAM, json.getHeaderValue("Content-Type"));
                assertEquals("4", json.getHeaderValue("Content-Length"));
            })
            .verifyComplete();
    }

    /**
     * Tests that a request with an octet-stream body where the body length is less than the content length is
     * correctly handled.
     */
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

    /**
     * Tests that a request with an octet-stream body where the body length is greater than the content length is
     * correctly handled.
     */
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

    /**
     * Tests that a request with an octet-stream body where the body length is equal to the content length is
     * correctly handled.
     */
    @Test
    public void asyncPutRequestWithBinaryDataBodyAndEqualContentLength() {
        Mono<BinaryData> bodyMono
            = BinaryData.fromFlux(Flux.just(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8))));
        StepVerifier
            .create(bodyMono
                .flatMap(body -> createService(Service9.class).putAsyncBodyAndContentLength(getRequestUri(), body, 4L)))
            .assertNext(json -> {
                assertEquals("test", json.data());
                assertEquals(ContentType.APPLICATION_OCTET_STREAM, json.getHeaderValue("Content-Type"));
                assertEquals("4", json.getHeaderValue("Content-Length"));
            })
            .verifyComplete();
    }

    /**
     * Tests that a request with an octet-stream body where the body length is less than the content length is
     * correctly handled.
     */
    @Test
    public void asyncPutRequestWithBinaryDataBodyAndLessThanContentLength() {
        Mono<BinaryData> bodyMono
            = BinaryData.fromFlux(Flux.just(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8))));
        StepVerifier
            .create(bodyMono
                .flatMap(body -> createService(Service9.class).putAsyncBodyAndContentLength(getRequestUri(), body, 5L)))
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
        Mono<BinaryData> bodyMono
            = Mono.just(BinaryData.fromStream(new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8))));
        StepVerifier
            .create(bodyMono
                .flatMap(body -> createService(Service9.class).putAsyncBodyAndContentLength(getRequestUri(), body, 5L)))
            .verifyErrorSatisfies(exception -> {
                assertTrue(exception instanceof UnexpectedLengthException
                    || (exception.getSuppressed().length > 0
                        && exception.getSuppressed()[0] instanceof UnexpectedLengthException));
                assertTrue(exception.getMessage().contains("less than"));
            });
    }

    /**
     * Tests that a request with an octet-stream body where the body length is greater than the content length is
     * correctly handled.
     */
    @Test
    public void asyncPutRequestWithBinaryDataBodyAndMoreThanContentLength() {
        Mono<BinaryData> bodyMono
            = BinaryData.fromFlux(Flux.just(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8))));
        StepVerifier
            .create(bodyMono
                .flatMap(body -> createService(Service9.class).putAsyncBodyAndContentLength(getRequestUri(), body, 3L)))
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

    /**
     * Tests that an unexpected response is handled correctly.
     */
    @Test
    public void syncPutRequestWithUnexpectedResponse() {
        HttpResponseException e = assertThrows(HttpResponseException.class,
            () -> createService(Service9.class).putWithUnexpectedResponse(getRequestUri(), "I'm the body!"));

        assertNotNull(e.getValue());
        assertInstanceOf(LinkedHashMap.class, e.getValue());

        @SuppressWarnings("unchecked")
        final LinkedHashMap<String, String> expectedBody = (LinkedHashMap<String, String>) e.getValue();
        assertEquals("I'm the body!", expectedBody.get("data"));
    }

    /**
     * Tests that an unexpected response is handled correctly.
     */
    @Test
    public void asyncPutRequestWithUnexpectedResponse() {
        StepVerifier
            .create(createService(Service9.class).putWithUnexpectedResponseAsync(getRequestUri(), "I'm the body!"))
            .verifyErrorSatisfies(throwable -> {
                HttpResponseException exception = assertInstanceOf(HttpResponseException.class, throwable);
                assertNotNull(exception.getValue());
                assertInstanceOf(LinkedHashMap.class, exception.getValue());

                @SuppressWarnings("unchecked")
                final LinkedHashMap<String, String> expectedBody = (LinkedHashMap<String, String>) exception.getValue();
                assertEquals("I'm the body!", expectedBody.get("data"));
            });
    }

    /**
     * Tests that a specific unexpected response is handled correctly.
     */
    @Test
    public void syncPutRequestWithUnexpectedResponseAndExceptionType() {
        MyRestException e = assertThrows(MyRestException.class, () -> createService(Service9.class)
            .putWithUnexpectedResponseAndExceptionType(getRequestUri(), "I'm the body!"));

        assertNotNull(e.getValue());
        assertEquals("I'm the body!", e.getValue().data());
    }

    /**
     * Tests that a specific unexpected response is handled correctly.
     */
    @Test
    public void asyncPutRequestWithUnexpectedResponseAndExceptionType() {
        StepVerifier.create(createService(Service9.class)
            .putWithUnexpectedResponseAndExceptionTypeAsync(getRequestUri(), "I'm the body!"))
            .verifyErrorSatisfies(throwable -> {
                MyRestException myRestException = assertInstanceOf(MyRestException.class, throwable,
                    "Expected MyRestException would be thrown. Instead got " + throwable.getClass().getSimpleName());
                assertNotNull(myRestException.getValue());
                assertEquals("I'm the body!", myRestException.getValue().data());
            });
    }

    /**
     * Tests that a status code specific unexpected response is handled correctly.
     */
    @Test
    public void syncPutRequestWithUnexpectedResponseAndDeterminedExceptionType() {
        MyRestException e = assertThrows(MyRestException.class, () -> createService(Service9.class)
            .putWithUnexpectedResponseAndDeterminedExceptionType(getRequestUri(), "I'm the body!"));

        assertNotNull(e.getValue());
        assertEquals("I'm the body!", e.getValue().data());
    }

    /**
     * Tests that a status code specific unexpected response is handled correctly.
     */
    @Test
    public void asyncPutRequestWithUnexpectedResponseAndDeterminedExceptionType() {
        StepVerifier
            .create(createService(Service9.class)
                .putWithUnexpectedResponseAndDeterminedExceptionTypeAsync(getRequestUri(), "I'm the body!"))
            .verifyErrorSatisfies(throwable -> {
                MyRestException myRestException = assertInstanceOf(MyRestException.class, throwable,
                    "Expected MyRestException would be thrown. Instead got " + throwable.getClass().getSimpleName());
                assertNotNull(myRestException.getValue());
                assertEquals("I'm the body!", myRestException.getValue().data());
            });
    }

    /**
     * Tests that an unexpected response that falls through to the default type is handled correctly.
     */
    @Test
    public void syncPutRequestWithUnexpectedResponseAndFallthroughExceptionType() {
        MyRestException e = assertThrows(MyRestException.class, () -> createService(Service9.class)
            .putWithUnexpectedResponseAndFallthroughExceptionType(getRequestUri(), "I'm the body!"));

        assertNotNull(e.getValue());
        assertEquals("I'm the body!", e.getValue().data());
    }

    /**
     * Tests that an unexpected response that falls through to the default type is handled correctly.
     */
    @Test
    public void asyncPutRequestWithUnexpectedResponseAndFallthroughExceptionType() {
        StepVerifier
            .create(createService(Service9.class)
                .putWithUnexpectedResponseAndFallthroughExceptionTypeAsync(getRequestUri(), "I'm the body!"))
            .verifyErrorSatisfies(throwable -> {
                MyRestException myRestException = assertInstanceOf(MyRestException.class, throwable,
                    "Expected MyRestException would be thrown. Instead got " + throwable.getClass().getSimpleName());
                assertNotNull(myRestException.getValue());
                assertEquals("I'm the body!", myRestException.getValue().data());
            });
    }

    /**
     * Tests that an unexpected response that falls through without a default type falls back to a global default is
     * handled correctly.
     */
    @Test
    public void syncPutRequestWithUnexpectedResponseAndNoFallthroughExceptionType() {
        HttpResponseException e = assertThrows(HttpResponseException.class, () -> createService(Service9.class)
            .putWithUnexpectedResponseAndNoFallthroughExceptionType(getRequestUri(), "I'm the body!"));

        assertNotNull(e.getValue());
        assertInstanceOf(LinkedHashMap.class, e.getValue());

        @SuppressWarnings("unchecked")
        final LinkedHashMap<String, String> expectedBody = (LinkedHashMap<String, String>) e.getValue();
        assertEquals("I'm the body!", expectedBody.get("data"));
    }

    /**
     * Tests that an unexpected response that falls through without a default type falls back to a global default is
     * handled correctly.
     */
    @Test
    public void asyncPutRequestWithUnexpectedResponseAndNoFallthroughExceptionType() {
        StepVerifier
            .create(createService(Service9.class)
                .putWithUnexpectedResponseAndNoFallthroughExceptionTypeAsync(getRequestUri(), "I'm the body!"))
            .verifyErrorSatisfies(throwable -> {
                HttpResponseException responseException = assertInstanceOf(HttpResponseException.class, throwable,
                    "Expected HttpResponseException would be thrown. Instead got "
                        + throwable.getClass().getSimpleName());
                assertNotNull(responseException.getValue());
                assertInstanceOf(LinkedHashMap.class, responseException.getValue());

                @SuppressWarnings("unchecked")
                final LinkedHashMap<String, String> expectedBody
                    = (LinkedHashMap<String, String>) responseException.getValue();
                assertEquals("I'm the body!", expectedBody.get("data"));
            });
    }

    @Host("{url}")
    @ServiceInterface(name = "Service10")
    interface Service10 {
        @Head("anything")
        @ExpectedResponses({ 200 })
        Response<Void> head(@HostParam("url") String url);

        @Head("anything")
        @ExpectedResponses({ 200 })
        boolean headBoolean(@HostParam("url") String url);

        @Head("anything")
        @ExpectedResponses({ 200 })
        void voidHead(@HostParam("url") String url);

        @Head("anything")
        @ExpectedResponses({ 200 })
        Mono<Response<Void>> headAsync(@HostParam("url") String url);

        @Head("anything")
        @ExpectedResponses({ 200 })
        Mono<Boolean> headBooleanAsync(@HostParam("url") String url);

        @Head("anything")
        @ExpectedResponses({ 200 })
        Mono<Void> completableHeadAsync(@HostParam("url") String url);
    }

    /**
     * Tests that a HEAD request is sent correctly.
     */
    @Test
    public void syncHeadRequest() {
        final Void body = createService(Service10.class).head(getRequestUri()).getValue();
        assertNull(body);
    }

    /**
     * Tests that a HEAD request is sent correctly.
     */
    @Test
    public void syncHeadBooleanRequest() {
        final boolean result = createService(Service10.class).headBoolean(getRequestUri());
        assertTrue(result);
    }

    /**
     * Tests that a HEAD request is sent correctly.
     */
    @Test
    public void syncVoidHeadRequest() {
        createService(Service10.class).voidHead(getRequestUri());
    }

    /**
     * Tests that a HEAD request is sent correctly.
     */
    @Test
    public void asyncHeadRequest() {
        StepVerifier.create(createService(Service10.class).headAsync(getRequestUri()))
            .assertNext(response -> assertNull(response.getValue()))
            .verifyComplete();
    }

    /**
     * Tests that a HEAD request is sent correctly.
     */
    @Test
    public void asyncHeadBooleanRequest() {
        StepVerifier.create(createService(Service10.class).headBooleanAsync(getRequestUri()))
            .assertNext(Assertions::assertTrue)
            .verifyComplete();
    }

    /**
     * Tests that a HEAD request is sent correctly.
     */
    @Test
    public void asyncCompletableHeadRequest() {
        StepVerifier.create(createService(Service10.class).completableHeadAsync(getRequestUri())).verifyComplete();
    }

    @Host("{url}")
    @ServiceInterface(name = "Service11")
    interface Service11 {
        @Delete("delete")
        @ExpectedResponses({ 200 })
        HttpBinJson delete(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) boolean bodyBoolean);

        @Delete("delete")
        @ExpectedResponses({ 200 })
        Mono<HttpBinJson> deleteAsync(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) boolean bodyBoolean);
    }

    /**
     * Tests that a DELETE request is sent correctly.
     */
    @Test
    public void syncDeleteRequest() {
        final HttpBinJson json = createService(Service11.class).delete(getRequestUri(), false);

        assertEquals(String.class, json.data().getClass());
        assertEquals("false", json.data());
    }

    /**
     * Tests that a DELETE request is sent correctly.
     */
    @Test
    public void asyncDeleteRequest() {
        StepVerifier.create(createService(Service11.class).deleteAsync(getRequestUri(), false)).assertNext(json -> {
            assertEquals(String.class, json.data().getClass());
            assertEquals("false", json.data());
        }).verifyComplete();
    }

    @Host("{url}")
    @ServiceInterface(name = "Service12")
    interface Service12 {
        @Patch("patch")
        @ExpectedResponses({ 200 })
        HttpBinJson patch(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String bodyString);

        @Patch("patch")
        @ExpectedResponses({ 200 })
        Mono<HttpBinJson> patchAsync(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String bodyString);
    }

    /**
     * Tests that a PATCH request is sent correctly.
     */
    @Test
    public void syncPatchRequest() {
        final HttpBinJson json = createService(Service12.class).patch(getRequestUri(), "body-contents");

        assertEquals(String.class, json.data().getClass());
        assertEquals("body-contents", json.data());
    }

    /**
     * Tests that a PATCH request is sent correctly.
     */
    @Test
    public void asyncPatchRequest() {
        StepVerifier.create(createService(Service12.class).patchAsync(getRequestUri(), "body-contents"))
            .assertNext(json -> {
                assertEquals(String.class, json.data().getClass());
                assertEquals("body-contents", json.data());
            })
            .verifyComplete();
    }

    @Host("{url}")
    @ServiceInterface(name = "Service13")
    interface Service13 {
        @Get("anything")
        @ExpectedResponses({ 200 })
        @Headers({ "MyHeader:MyHeaderValue", "MyOtherHeader:My,Header,Value" })
        HttpBinJson get(@HostParam("url") String url);

        @Get("anything")
        @ExpectedResponses({ 200 })
        @Headers({ "MyHeader:MyHeaderValue", "MyOtherHeader:My,Header,Value" })
        Mono<HttpBinJson> getAsync(@HostParam("url") String url);
    }

    private static final HttpHeaderName MY_HEADER = HttpHeaderName.fromString("MyHeader");
    private static final HttpHeaderName MY_OTHER_HEADER = HttpHeaderName.fromString("MyOtherHeader");

    /**
     * Tests that a request with {@link Headers} adds the headers to the request correctly.
     */
    @Test
    public void syncHeadersRequest() {
        final HttpBinJson json = createService(Service13.class).get(getRequestUri());
        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything", json.url());
        assertNotNull(json.headers());
        final HttpHeaders headers = new HttpHeaders().setAll(json.headers());
        assertEquals("MyHeaderValue", headers.getValue(MY_HEADER));
        assertArrayEquals(new String[] { "MyHeaderValue" }, headers.getValues(MY_HEADER));
        assertEquals("My,Header,Value", headers.getValue(MY_OTHER_HEADER));
        assertArrayEquals(new String[] { "My", "Header", "Value" }, headers.getValues(MY_OTHER_HEADER));
    }

    /**
     * Tests that a request with {@link Headers} adds the headers to the request correctly.
     */
    @Test
    public void asyncHeadersRequest() {
        StepVerifier.create(createService(Service13.class).getAsync(getRequestUri())).assertNext(json -> {
            assertMatchWithHttpOrHttps("localhost/anything", json.url());
            assertNotNull(json.headers());
            final HttpHeaders headers = new HttpHeaders().setAll(json.headers());

            assertEquals("MyHeaderValue", headers.getValue(MY_HEADER));
            assertArrayEquals(new String[] { "MyHeaderValue" }, headers.getValues(MY_HEADER));
        }).verifyComplete();
    }

    @Host("{url}")
    @ServiceInterface(name = "Service14")
    interface Service14 {
        @Get("anything")
        @ExpectedResponses({ 200 })
        @Headers({ "MyHeader:MyHeaderValue" })
        HttpBinJson get(@HostParam("url") String url);

        @Get("anything")
        @ExpectedResponses({ 200 })
        @Headers({ "MyHeader:MyHeaderValue" })
        Mono<HttpBinJson> getAsync(@HostParam("url") String url);
    }

    /**
     * Tests that a request with {@link Headers} adds the headers to the request correctly.
     */
    @Test
    public void asyncHttpsHeadersRequest() {
        StepVerifier.create(createService(Service14.class).getAsync(getRequestUri())).assertNext(json -> {
            assertMatchWithHttpOrHttps("localhost/anything", json.url());
            assertNotNull(json.headers());
            final HttpHeaders headers = new HttpHeaders().setAll(json.headers());
            assertEquals("MyHeaderValue", headers.getValue(MY_HEADER));
        }).verifyComplete();
    }

    @Host("{url}")
    @ServiceInterface(name = "Service16")
    interface Service16 {
        @Put("put")
        @ExpectedResponses({ 200 })
        HttpBinJson putByteArray(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] bytes);

        @Put("put")
        @ExpectedResponses({ 200 })
        Mono<HttpBinJson> putByteArrayAsync(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] bytes);
    }

    /**
     * Tests that a request with an octet-stream body where the body is a byte array is correctly handled.
     */
    @Test
    public void service16Put() {
        final Service16 service16 = createService(Service16.class);
        final byte[] expectedBytes = new byte[] { 1, 2, 3, 4 };
        final HttpBinJson httpBinJSON = service16.putByteArray(getRequestUri(), expectedBytes);

        final String base64String = httpBinJSON.data();
        final byte[] actualBytes = base64String.getBytes(StandardCharsets.UTF_8);
        assertArraysEqual(expectedBytes, actualBytes);
    }

    /**
     * Tests that a request with an octet-stream body where the body is a byte array is correctly handled.
     */
    @Test
    public void service16PutAsync() {
        final byte[] expectedBytes = new byte[] { 1, 2, 3, 4 };
        StepVerifier.create(createService(Service16.class).putByteArrayAsync(getRequestUri(), expectedBytes))
            .assertNext(json -> assertArraysEqual(expectedBytes, json.data().getBytes(StandardCharsets.UTF_8)))
            .verifyComplete();
    }

    @Host("{scheme}://{hostPart1}{hostPart2}")
    @ServiceInterface(name = "Service17")
    interface Service17 {
        @Get("get")
        @ExpectedResponses({ 200 })
        HttpBinJson get(@HostParam("scheme") String scheme, @HostParam("hostPart1") String hostPart1,
            @HostParam("hostPart2") String hostPart2);

        @Get("get")
        @ExpectedResponses({ 200 })
        Mono<HttpBinJson> getAsync(@HostParam("scheme") String scheme, @HostParam("hostPart1") String hostPart1,
            @HostParam("hostPart2") String hostPart2);
    }

    /**
     * Tests that a request with multiple host parameters is correctly handled.
     */
    @Test
    public void syncRequestWithMultipleHostParams() {
        final HttpBinJson result = createService(Service17.class).get(getRequestScheme(), "local", "host");

        assertNotNull(result);
        assertMatchWithHttpOrHttps("localhost/get", result.url());
    }

    /**
     * Tests that a request with multiple host parameters is correctly handled.
     */
    @Test
    public void asyncRequestWithMultipleHostParams() {
        StepVerifier.create(createService(Service17.class).getAsync(getRequestScheme(), "local", "host"))
            .assertNext(json -> assertMatchWithHttpOrHttps("localhost/get", json.url()))
            .verifyComplete();
    }

    @Host("{url}")
    @ServiceInterface(name = "Service18")
    interface Service18 {
        @Get("status/200")
        void getStatus200(@HostParam("url") String url);

        @Get("status/200")
        @ExpectedResponses({ 200 })
        void getStatus200WithExpectedResponse200(@HostParam("url") String url);

        @Get("status/300")
        void getStatus300(@HostParam("url") String url);

        @Get("status/300")
        @ExpectedResponses({ 300 })
        void getStatus300WithExpectedResponse300(@HostParam("url") String url);

        @Get("status/400")
        void getStatus400(@HostParam("url") String url);

        @Get("status/400")
        @ExpectedResponses({ 400 })
        void getStatus400WithExpectedResponse400(@HostParam("url") String url);

        @Get("status/500")
        void getStatus500(@HostParam("url") String url);

        @Get("status/500")
        @ExpectedResponses({ 500 })
        void getStatus500WithExpectedResponse500(@HostParam("url") String url);
    }

    /**
     * This test verifies that a response with a 200 status code does not throw if the status code wasn't expected.
     */
    @Test
    public void service18GetStatus200() {
        createService(Service18.class).getStatus200(getRequestUri());
    }

    /**
     * This test verifies that a response with a 200 status code does not throw if the status code was expected.
     */
    @Test
    public void service18GetStatus200WithExpectedResponse200() {
        assertDoesNotThrow(() -> createService(Service18.class).getStatus200WithExpectedResponse200(getRequestUri()));
    }

    /**
     * This test verifies that a response with a non-200 status code does throw if the status code wasn't expected.
     */
    @Test
    public void service18GetStatus300() {
        createService(Service18.class).getStatus300(getRequestUri());
    }

    /**
     * This test verifies that a response with a non-200 status code does not throw if the status code was expected.
     */
    @Test
    public void service18GetStatus300WithExpectedResponse300() {
        assertDoesNotThrow(() -> createService(Service18.class).getStatus300WithExpectedResponse300(getRequestUri()));
    }

    /**
     * This test verifies that a response with a non-200 status code does throw if the status code wasn't expected.
     */
    @Test
    public void service18GetStatus400() {
        assertThrows(HttpResponseException.class, () -> createService(Service18.class).getStatus400(getRequestUri()));
    }

    /**
     * This test verifies that a response with a non-200 status code does not throw if the status code was expected.
     */
    @Test
    public void service18GetStatus400WithExpectedResponse400() {
        assertDoesNotThrow(() -> createService(Service18.class).getStatus400WithExpectedResponse400(getRequestUri()));
    }

    /**
     * This test verifies that a response with a non-200 status code does throw if the status code wasn't expected.
     */
    @Test
    public void service18GetStatus500() {
        assertThrows(HttpResponseException.class, () -> createService(Service18.class).getStatus500(getRequestUri()));
    }

    /**
     * This test verifies that a response with a non-200 status code does not throw if the status code was expected.
     */
    @Test
    public void service18GetStatus500WithExpectedResponse500() {
        assertDoesNotThrow(() -> createService(Service18.class).getStatus500WithExpectedResponse500(getRequestUri()));
    }

    @Host("{url}")
    @ServiceInterface(name = "Service19")
    interface Service19 {
        @Put("put")
        HttpBinJson putWithNoContentTypeAndStringBody(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Put("put")
        HttpBinJson putWithNoContentTypeAndByteArrayBody(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] body);

        @Put("put")
        HttpBinJson putWithHeaderApplicationJsonContentTypeAndStringBody(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_JSON) String body);

        @Put("put")
        @Headers({ "Content-Type: application/json" })
        HttpBinJson putWithHeaderApplicationJsonContentTypeAndByteArrayBody(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_JSON) byte[] body);

        @Put("put")
        @Headers({ "Content-Type: application/json; charset=utf-8" })
        HttpBinJson putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Put("put")
        @Headers({ "Content-Type: application/octet-stream" })
        HttpBinJson putWithHeaderApplicationOctetStreamContentTypeAndStringBody(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Put("put")
        @Headers({ "Content-Type: application/octet-stream" })
        HttpBinJson putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] body);

        @Put("put")
        HttpBinJson putWithBodyParamApplicationJsonContentTypeAndStringBody(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_JSON) String body);

        @Put("put")
        HttpBinJson putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_JSON + "; charset=utf-8") String body);

        @Put("put")
        HttpBinJson putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_JSON) byte[] body);

        @Put("put")
        HttpBinJson putWithBodyParamApplicationOctetStreamContentTypeAndStringBody(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Put("put")
        HttpBinJson putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] body);
    }

    /**
     * This test verifies a String octet-stream that is null is sent correctly.
     */
    @Test
    public void service19PutWithNoContentTypeAndStringBodyWithNullBody() {
        final HttpBinJson result
            = createService(Service19.class).putWithNoContentTypeAndStringBody(getRequestUri(), null);

        assertEquals("", result.data());
    }

    /**
     * This test verifies a String octet-stream that is empty is sent correctly.
     */
    @Test
    public void service19PutWithNoContentTypeAndStringBodyWithEmptyBody() {
        final HttpBinJson result
            = createService(Service19.class).putWithNoContentTypeAndStringBody(getRequestUri(), "");

        assertEquals("", result.data());
    }

    /**
     * This test verifies a String octet-stream is sent correctly.
     */
    @Test
    public void service19PutWithNoContentTypeAndStringBodyWithNonEmptyBody() {
        final HttpBinJson result
            = createService(Service19.class).putWithNoContentTypeAndStringBody(getRequestUri(), "hello");

        assertEquals("hello", result.data());
    }

    /**
     * This test verifies a byte array octet-stream that is null is sent correctly.
     */
    @Test
    public void service19PutWithNoContentTypeAndByteArrayBodyWithNullBody() {
        final HttpBinJson result
            = createService(Service19.class).putWithNoContentTypeAndByteArrayBody(getRequestUri(), null);

        assertEquals("", result.data());
    }

    /**
     * This test verifies a byte array octet-stream that is empty is sent correctly.
     */
    @Test
    public void service19PutWithNoContentTypeAndByteArrayBodyWithEmptyBody() {
        final HttpBinJson result
            = createService(Service19.class).putWithNoContentTypeAndByteArrayBody(getRequestUri(), new byte[0]);

        assertEquals("", result.data());
    }

    /**
     * This test verifies a byte array octet-stream is sent correctly.
     */
    @Test
    public void service19PutWithNoContentTypeAndByteArrayBodyWithNonEmptyBody() {
        final HttpBinJson result = createService(Service19.class).putWithNoContentTypeAndByteArrayBody(getRequestUri(),
            new byte[] { 0, 1, 2, 3, 4 });

        assertEquals(new String(new byte[] { 0, 1, 2, 3, 4 }, StandardCharsets.UTF_8), result.data());
    }

    /**
     * This test verifies a String application-json that is null is sent correctly.
     */
    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndStringBodyWithNullBody() {
        final HttpBinJson result = createService(Service19.class)
            .putWithHeaderApplicationJsonContentTypeAndStringBody(getRequestUri(), null);

        assertEquals("", result.data());
    }

    /**
     * This test verifies a String application-json that is empty is sent correctly.
     */
    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndStringBodyWithEmptyBody() {
        final HttpBinJson result
            = createService(Service19.class).putWithHeaderApplicationJsonContentTypeAndStringBody(getRequestUri(), "");

        assertEquals("\"\"", result.data());
    }

    /**
     * This test verifies a String application-json is sent correctly.
     */
    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndStringBodyWithNonEmptyBody() {
        final HttpBinJson result = createService(Service19.class)
            .putWithHeaderApplicationJsonContentTypeAndStringBody(getRequestUri(), "soups and stuff");

        assertEquals("\"soups and stuff\"", result.data());
    }

    /**
     * This test verifies a byte array application-json with ignored content type that is null is sent correctly.
     */
    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndByteArrayBodyWithNullBody() {
        final HttpBinJson result = createService(Service19.class)
            .putWithHeaderApplicationJsonContentTypeAndByteArrayBody(getRequestUri(), null);

        assertEquals("", result.data());
    }

    /**
     * This test verifies a byte array application-json with ignored content type that is empty is sent correctly.
     */
    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndByteArrayBodyWithEmptyBody() {
        final HttpBinJson result = createService(Service19.class)
            .putWithHeaderApplicationJsonContentTypeAndByteArrayBody(getRequestUri(), new byte[0]);

        assertEquals("\"\"", result.data());
    }

    /**
     * This test verifies a byte array application-json with ignored content type is sent correctly.
     */
    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndByteArrayBodyWithNonEmptyBody() {
        final HttpBinJson result = createService(Service19.class)
            .putWithHeaderApplicationJsonContentTypeAndByteArrayBody(getRequestUri(), new byte[] { 0, 1, 2, 3, 4 });

        assertEquals("\"AAECAwQ=\"", result.data());
    }

    /**
     * This test verifies a String octet-stream with an ignored content type that is null is sent correctly.
     */
    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndCharsetAndStringBodyWithNullBody() {
        final HttpBinJson result = createService(Service19.class)
            .putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(getRequestUri(), null);

        assertEquals("", result.data());
    }

    /**
     * This test verifies a String octet-stream with an ignored content type that is empty is sent correctly.
     */
    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndCharsetAndStringBodyWithEmptyBody() {
        final HttpBinJson result = createService(Service19.class)
            .putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(getRequestUri(), "");

        assertEquals("", result.data());
    }

    /**
     * This test verifies a String octet-stream with an ignored content type is sent correctly.
     */
    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndCharsetAndStringBodyWithNonEmptyBody() {
        final HttpBinJson result = createService(Service19.class)
            .putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(getRequestUri(), "soups and stuff");

        assertEquals("soups and stuff", result.data());
    }

    /**
     * This test verifies a String octet-stream with an ignored content type that is null is sent correctly.
     */
    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndStringBodyWithNullBody() {
        final HttpBinJson result = createService(Service19.class)
            .putWithHeaderApplicationOctetStreamContentTypeAndStringBody(getRequestUri(), null);

        assertEquals("", result.data());
    }

    /**
     * This test verifies a String octet-stream with an ignored content type that is empty is sent correctly.
     */
    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndStringBodyWithEmptyBody() {
        final HttpBinJson result = createService(Service19.class)
            .putWithHeaderApplicationOctetStreamContentTypeAndStringBody(getRequestUri(), "");

        assertEquals("", result.data());
    }

    /**
     * This test verifies a String octet-stream with an ignored content type is sent correctly.
     */
    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndStringBodyWithNonEmptyBody() {
        final HttpBinJson result = createService(Service19.class)
            .putWithHeaderApplicationOctetStreamContentTypeAndStringBody(getRequestUri(), "penguins");

        assertEquals("penguins", result.data());
    }

    /**
     * This test verifies a byte array octet-stream with an ignored content type that is null is sent correctly.
     */
    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndByteArrayBodyWithNullBody() {
        final HttpBinJson result = createService(Service19.class)
            .putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(getRequestUri(), null);

        assertEquals("", result.data());
    }

    /**
     * This test verifies a byte array octet-stream with an ignored content type that is empty is sent correctly.
     */
    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndByteArrayBodyWithEmptyBody() {
        final HttpBinJson result = createService(Service19.class)
            .putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(getRequestUri(), new byte[0]);

        assertEquals("", result.data());
    }

    /**
     * This test verifies a byte array octet-stream with an ignored content type is sent correctly.
     */
    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndByteArrayBodyWithNonEmptyBody() {
        final HttpBinJson result
            = createService(Service19.class).putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(
                getRequestUri(), new byte[] { 0, 1, 2, 3, 4 });

        assertEquals(new String(new byte[] { 0, 1, 2, 3, 4 }, StandardCharsets.UTF_8), result.data());
    }

    /**
     * This test verifies a String application-json that is null is sent correctly.
     */
    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndStringBodyWithNullBody() {
        final HttpBinJson result = createService(Service19.class)
            .putWithBodyParamApplicationJsonContentTypeAndStringBody(getRequestUri(), null);

        assertEquals("", result.data());
    }

    /**
     * This test verifies a String application-json that is empty is sent correctly.
     */
    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndStringBodyWithEmptyBody() {
        final HttpBinJson result = createService(Service19.class)
            .putWithBodyParamApplicationJsonContentTypeAndStringBody(getRequestUri(), "");

        assertEquals("\"\"", result.data());
    }

    /**
     * This test verifies a String application-json is sent correctly.
     */
    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndStringBodyWithNonEmptyBody() {
        final HttpBinJson result = createService(Service19.class)
            .putWithBodyParamApplicationJsonContentTypeAndStringBody(getRequestUri(), "soups and stuff");

        assertEquals("\"soups and stuff\"", result.data());
    }

    /**
     * This test verifies a String application-json with charset that is empty is sent correctly.
     */
    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBodyWithNullBody() {
        final HttpBinJson result = createService(Service19.class)
            .putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody(getRequestUri(), null);

        assertEquals("", result.data());
    }

    /**
     * This test verifies a String application-json with charset that is null is sent correctly.
     */
    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBodyWithEmptyBody() {
        final HttpBinJson result = createService(Service19.class)
            .putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody(getRequestUri(), "");

        assertEquals("\"\"", result.data());
    }

    /**
     * This test verifies a String application-json with charset is sent correctly.
     */
    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBodyWithNonEmptyBody() {
        final HttpBinJson result = createService(Service19.class)
            .putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody(getRequestUri(), "soups and stuff");

        assertEquals("\"soups and stuff\"", result.data());
    }

    /**
     * This test verifies a byte array application-json that is null is sent correctly.
     */
    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndByteArrayBodyWithNullBody() {
        final HttpBinJson result = createService(Service19.class)
            .putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(getRequestUri(), null);

        assertEquals("", result.data());
    }

    /**
     * This test verifies a byte array application-json that is empty is sent correctly.
     */
    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndByteArrayBodyWithEmptyBody() {
        final HttpBinJson result = createService(Service19.class)
            .putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(getRequestUri(), new byte[0]);

        assertEquals("\"\"", result.data());
    }

    /**
     * This test verifies a byte array application-json is sent correctly.
     */
    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndByteArrayBodyWithNonEmptyBody() {
        final HttpBinJson result = createService(Service19.class)
            .putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(getRequestUri(), new byte[] { 0, 1, 2, 3, 4 });

        assertEquals("\"AAECAwQ=\"", result.data());
    }

    /**
     * This test verifies a String octet-stream that is null is sent correctly.
     */
    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndStringBodyWithNullBody() {
        final HttpBinJson result = createService(Service19.class)
            .putWithBodyParamApplicationOctetStreamContentTypeAndStringBody(getRequestUri(), null);

        assertEquals("", result.data());
    }

    /**
     * This test verifies a String octet-stream that is empty is sent correctly.
     */
    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndStringBodyWithEmptyBody() {
        final HttpBinJson result = createService(Service19.class)
            .putWithBodyParamApplicationOctetStreamContentTypeAndStringBody(getRequestUri(), "");

        assertEquals("", result.data());
    }

    /**
     * This test verifies a String octet-stream is sent correctly.
     */
    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndStringBodyWithNonEmptyBody() {
        final HttpBinJson result = createService(Service19.class)
            .putWithBodyParamApplicationOctetStreamContentTypeAndStringBody(getRequestUri(), "penguins");

        assertEquals("penguins", result.data());
    }

    /**
     * This test verifies a byte array octet-stream that is null is sent correctly.
     */
    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBodyWithNullBody() {
        final HttpBinJson result = createService(Service19.class)
            .putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(getRequestUri(), null);

        assertEquals("", result.data());
    }

    /**
     * This test verifies a byte array octet-stream that is empty is sent correctly.
     */
    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBodyWithEmptyBody() {
        final HttpBinJson result = createService(Service19.class)
            .putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(getRequestUri(), new byte[0]);

        assertEquals("", result.data());
    }

    /**
     * This test verifies a byte array octet-stream is sent correctly.
     */
    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBodyWithNonEmptyBody() {
        final HttpBinJson result
            = createService(Service19.class).putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(
                getRequestUri(), new byte[] { 0, 1, 2, 3, 4 });

        assertEquals(new String(new byte[] { 0, 1, 2, 3, 4 }, StandardCharsets.UTF_8), result.data());
    }

    @Host("{url}")
    @ServiceInterface(name = "Service20")
    interface Service20 {
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
        ResponseBase<HttpBinHeaders, HttpBinJson> putBodyAndHeaders(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Get("bytes/100")
        ResponseBase<Void, Void> getBytesOnlyStatus(@HostParam("url") String url);

        @Get("bytes/100")
        Response<Void> getVoidResponse(@HostParam("url") String url);

        @Put("put")
        Response<HttpBinJson> putBody(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);
    }

    /**
     * This test verifies a deserialized body and deserialized headers are handled correctly.
     */
    @Test
    public void service20GetBytes100OnlyHeaders() {
        final ResponseBase<HttpBinHeaders, Void> response
            = createService(Service20.class).getBytes100OnlyHeaders(getRequestUri());
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());

        assertEquals(HttpHeaders.class, response.getHeaders().getClass());

        final HttpBinHeaders headers = response.getDeserializedHeaders();
        assertNotNull(headers);
        assertTrue(headers.accessControlAllowCredentials());
        assertNotNull(headers.date());
        assertNotEquals(0, (Object) headers.xProcessedTime());
    }

    /**
     * This test verifies that a byte array body with deserialized headers are handled correctly.
     */
    @Test
    public void service20GetBytes100BodyAndHeaders() {
        final ResponseBase<HttpBinHeaders, byte[]> response
            = createService(Service20.class).getBytes100BodyAndHeaders(getRequestUri());
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

    /**
     * This test verifies that a void body, even if data is returned, with void deserialized headers are handled
     * correctly.
     */
    @Test
    public void service20GetBytesOnlyStatus() {
        final Response<Void> response = createService(Service20.class).getBytesOnlyStatus(getRequestUri());

        assertEquals(HttpHeaders.class, response.getHeaders().getClass());

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }

    /**
     * This test verifies that a void body, even if data is returned, with deserialized headers are handled correctly.
     */
    @Test
    public void service20GetBytesOnlyHeaders() {
        final Response<Void> response = createService(Service20.class).getBytes100OnlyRawHeaders(getRequestUri());

        assertEquals(HttpHeaders.class, response.getHeaders().getClass());

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getHeaders());
        assertNotEquals(0, response.getHeaders().getSize());
    }

    /**
     * This test verifies that a void body with deserialized headers are handled correctly.
     */
    @Test
    public void service20PutOnlyHeaders() {
        final ResponseBase<HttpBinHeaders, Void> response
            = createService(Service20.class).putOnlyHeaders(getRequestUri(), "body string");
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());

        assertEquals(HttpHeaders.class, response.getHeaders().getClass());

        final HttpBinHeaders headers = response.getDeserializedHeaders();
        assertNotNull(headers);
        assertTrue(headers.accessControlAllowCredentials());
        assertNotNull(headers.date());
        assertNotEquals(0, (Object) headers.xProcessedTime());
    }

    /**
     * This test verifies that deserialized headers are handled correctly.
     */
    @Test
    public void service20PutBodyAndHeaders() {
        final ResponseBase<HttpBinHeaders, HttpBinJson> response
            = createService(Service20.class).putBodyAndHeaders(getRequestUri(), "body string");
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());

        assertEquals(HttpHeaders.class, response.getHeaders().getClass());

        final HttpBinJson body = response.getValue();
        assertNotNull(body);
        assertMatchWithHttpOrHttps("localhost/put", body.url());
        assertEquals("body string", body.data());

        final HttpBinHeaders headers = response.getDeserializedHeaders();
        assertNotNull(headers);
        assertTrue(headers.accessControlAllowCredentials());
        assertNotNull(headers.date());
        assertNotEquals(0, (Object) headers.xProcessedTime());
    }

    /**
     * This test verifies that a void response is handled correctly.
     */
    @Test
    public void service20GetVoidResponse() {
        final Response<Void> response = createService(Service20.class).getVoidResponse(getRequestUri());
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }

    /**
     * This test verifies a String-based octet-stream is sent correctly.
     */
    @Test
    public void service20GetResponseBody() {
        final Response<HttpBinJson> response = createService(Service20.class).putBody(getRequestUri(), "body string");
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());

        final HttpBinJson body = response.getValue();
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
        @ExpectedResponses({ 400 })
        StreamResponse getBytes(@HostParam("url") String url);
    }

    /**
     * This test verifies that an unexpected 200 response is handled correctly.
     */
    @Test
    public void unexpectedHttpOk() {
        HttpResponseException e = assertThrows(HttpResponseException.class,
            () -> createService(UnexpectedOKService.class).getBytes(getRequestUri()));

        assertEquals("Status code 200, (1024-byte body)", e.getMessage());
    }

    @Host("{url}")
    @ServiceInterface(name = "Service21")
    private interface Service21 {
        @Get("/bytes/100")
        @ExpectedResponses({ 200 })
        byte[] getBytes100(@HostParam("url") String url);
    }

    /**
     * This test verifies that a byte array is downloaded correctly.
     */
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

    /**
     * This test verifies that a StreamResponse is downloaded correctly.
     *
     * @param context The context to use.
     */
    @ParameterizedTest
    @MethodSource("downloadTestArgumentProvider")
    public void simpleDownloadTest(Context context) {
        Mono<Tuple3<Integer, String, String>> downloadData
            = Mono.using(() -> createService(DownloadService.class).getBytes(getRequestUri(), context),
                response -> FluxUtil.collectBytesInByteBufferStream(response.getValue())
                    .map(bytes -> Tuples.of(bytes.length, response.getHeaders().getValue(HttpHeaderName.ETAG),
                        HttpTestUtils.md5(bytes))),
                StreamResponse::close);

        StepVerifier.create(downloadData).assertNext(tuple -> {
            assertEquals(30720, tuple.getT1().intValue());
            assertEquals(tuple.getT2(), tuple.getT3());
        }).verifyComplete();
    }

    /**
     * This test verifies that a StreamResponse is downloaded correctly.
     *
     * @param context The context to use.
     */
    @ParameterizedTest
    @MethodSource("downloadTestArgumentProvider")
    public void simpleDownloadTestAsync(Context context) {
        StepVerifier
            .create(createService(DownloadService.class).getBytesAsync(getRequestUri(), context)
                .flatMap(response -> Mono.using(() -> response,
                    r -> r.getValue().map(ByteBuffer::remaining).reduce(0, Integer::sum), StreamResponse::close)))
            .assertNext(count -> assertEquals(30720, count))
            .verifyComplete();

        StepVerifier
            .create(createService(DownloadService.class).getBytesAsync(getRequestUri(), context)
                .flatMap(response -> Mono.using(() -> response,
                    r -> Mono.zip(HttpTestUtils.md5(r.getValue()),
                        Mono.just(r.getHeaders().getValue(HttpHeaderName.ETAG))),
                    StreamResponse::close)))
            .assertNext(hashTuple -> assertEquals(hashTuple.getT2(), hashTuple.getT1()))
            .verifyComplete();
    }

    /**
     * This test verifies that a {@link StreamResponse} transfers correctly.
     *
     * @param context The context to use.
     * @throws IOException If an IO error occurs.
     */
    @ParameterizedTest
    @MethodSource("downloadTestArgumentProvider")
    public void streamResponseCanTransferBody(Context context) throws IOException {
        try (StreamResponse streamResponse = createService(DownloadService.class).getBytes(getRequestUri(), context)) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            streamResponse.writeValueTo(Channels.newChannel(bos));
            assertEquals(streamResponse.getHeaders().getValue(HttpHeaderName.ETAG),
                HttpTestUtils.md5(bos.toByteArray()));
        }

        Path tempFile = Files.createTempFile("streamResponseCanTransferBody", null);
        tempFile.toFile().deleteOnExit();
        try (StreamResponse streamResponse = createService(DownloadService.class).getBytes(getRequestUri(), context)) {
            StepVerifier.create(Mono.using(
                () -> IOUtils
                    .toAsynchronousByteChannel(AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE), 0),
                streamResponse::writeValueToAsync, channel -> {
                    try {
                        channel.close();
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                }).then(Mono.fromCallable(() -> HttpTestUtils.md5(Files.readAllBytes(tempFile)))))
                .assertNext(hash -> assertEquals(streamResponse.getHeaders().getValue(HttpHeaderName.ETAG), hash))
                .verifyComplete();
        }
    }

    /**
     * This test verifies that a {@link StreamResponse} transfers correctly.
     *
     * @param context The context to use.
     * @throws IOException If an IO error occurs.
     */
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
                    HttpTestUtils.md5(bos.toByteArray()));
            })).assertNext(hashTuple -> assertEquals(hashTuple.getT1(), hashTuple.getT2())).verifyComplete();

        Path tempFile = Files.createTempFile("streamResponseCanTransferBody", null);
        tempFile.toFile().deleteOnExit();
        StepVerifier.create(createService(DownloadService.class).getBytesAsync(getRequestUri(), context)
            .flatMap(streamResponse -> Mono.using(
                () -> IOUtils
                    .toAsynchronousByteChannel(AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE), 0),
                streamResponse::writeValueToAsync, channel -> {
                    streamResponse.close();
                    try {
                        channel.close();
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                }).then(Mono.just(streamResponse.getHeaders().getValue(HttpHeaderName.ETAG)))))
            .assertNext(hash -> {
                try {
                    assertEquals(hash, HttpTestUtils.md5(Files.readAllBytes(tempFile)));
                } catch (IOException e) {
                    throw Exceptions.propagate(e);
                }
            })
            .verifyComplete();
    }

    private static Stream<Arguments> downloadTestArgumentProvider() {
        return Stream.of(Arguments.of(Named.named("default", Context.NONE)), Arguments
            .of(Named.named("sync proxy enabled", Context.NONE.addData(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true))));
    }

    /**
     * This test verifies that a raw Flux is downloaded correctly.
     */
    @Test
    public void rawFluxDownloadTest() {
        StepVerifier.create(createService(DownloadService.class).getBytesFlux(getRequestUri())
            .map(ByteBuffer::remaining)
            .reduce(0, Integer::sum)).assertNext(count -> assertEquals(30720, count)).verifyComplete();
    }

    @Host("{url}")
    @ServiceInterface(name = "FluxUploadService")
    interface FluxUploadService {
        @Put("/put")
        Response<HttpBinJson> put(@HostParam("url") String url, @BodyParam("text/plain") Flux<ByteBuffer> content,
            @HeaderParam("Content-Length") long contentLength);
    }

    /**
     * This test verifies that a File-based Flux is uploaded correctly.
     *
     * @throws Exception If the file resource cannot be found.
     */
    @Test
    public void fluxUploadTest() throws Exception {
        Path filePath = Paths.get(getClass().getClassLoader().getResource("upload.txt").toURI());
        Flux<ByteBuffer> stream = FluxUtil.readFile(AsynchronousFileChannel.open(filePath));

        final HttpClient httpClient = createHttpClient();
        // Scenario: Log the body so that body buffering/replay behavior is exercised.
        //
        // Order in which policies applied will be the order in which they added to builder
        //
        final HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(httpClient)
            .policies(new PortPolicy(getPort(), true),
                new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)))
            .build();
        //
        Response<HttpBinJson> response = RestProxy.create(FluxUploadService.class, httpPipeline)
            .put(getRequestUri(), stream, Files.size(filePath));

        assertEquals("The quick brown fox jumps over the lazy dog", response.getValue().data());
    }

    /**
     * This test verifies that a File-based Flux with a range is uploaded correctly.
     *
     * @throws Exception If the file resource cannot be found.
     */
    @Test
    public void segmentUploadTest() throws Exception {
        Path filePath = Paths.get(getClass().getClassLoader().getResource("upload.txt").toURI());
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(filePath, StandardOpenOption.READ);
        Response<HttpBinJson> response
            = createService(FluxUploadService.class).put(getRequestUri(), FluxUtil.readFile(fileChannel, 4, 15), 15);

        assertEquals("quick brown fox", response.getValue().data());
    }

    @Host("{url}")
    @ServiceInterface(name = "BinaryDataUploadServ")
    interface BinaryDataUploadService {
        @Put("/put")
        Response<HttpBinJson> put(@HostParam("url") String host, @BodyParam("text/plain") BinaryData content,
            @HeaderParam("Content-Length") long contentLength);
    }

    /**
     * This test verifies that a File-based BinaryData is uploaded correctly.
     *
     * @throws Exception If the file resource cannot be found.
     */
    @Test
    public void binaryDataUploadTest() throws Exception {
        Path filePath = Paths.get(getClass().getClassLoader().getResource("upload.txt").toURI());
        BinaryData data = BinaryData.fromFile(filePath);

        final HttpClient httpClient = createHttpClient();
        // Scenario: Log the body so that body buffering/replay behavior is exercised.
        //
        // Order in which policies applied will be the order in which they added to builder
        //
        final HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(httpClient)
            .policies(new PortPolicy(getPort(), true),
                new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)))
            .build();
        //
        Response<HttpBinJson> response = RestProxy.create(BinaryDataUploadService.class, httpPipeline)
            .put(getServerUri(isSecure()), data, Files.size(filePath));

        assertEquals("The quick brown fox jumps over the lazy dog", response.getValue().data());
    }

    @Host("{url}")
    @ServiceInterface(name = "Service22")
    interface Service22 {
        @Get("/")
        byte[] getBytes(@HostParam("url") String url);
    }

    /**
     * This test verifies that the response body is returned as a byte array.
     */
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

    /**
     * This test verifies that the response body is returned as a byte array.
     */
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
        HttpBinJson put(@HostParam("url") String url, @HeaderParam("ABC") Map<String, String> headerCollection);
    }

    /**
     * This test verifies that the header collection sets headers correctly.
     */
    @Test
    public void service24Put() {
        final Map<String, String> headerCollection = new HashMap<>();
        headerCollection.put("DEF", "GHIJ");
        headerCollection.put("123", "45");
        final HttpBinJson result = createService(Service24.class).put(getRequestUri(), headerCollection);
        assertNotNull(result.headers());

        final HttpHeaders resultHeaders = new HttpHeaders().setAll(result.headers());

        assertEquals("GHIJ", resultHeaders.getValue(HttpHeaderName.fromString("ABCDEF")));
        assertEquals("45", resultHeaders.getValue(HttpHeaderName.fromString("ABC123")));
    }

    @Host("{url}")
    @ServiceInterface(name = "Service26")
    interface Service26 {
        @Post("post")
        HttpBinFormDataJson postForm(@HostParam("url") String url, @FormParam("custname") String name,
            @FormParam("custtel") String telephone, @FormParam("custemail") String email,
            @FormParam("size") PizzaSize size, @FormParam("toppings") List<String> toppings);

        @Post("post")
        HttpBinFormDataJson postEncodedForm(@HostParam("url") String url, @FormParam("custname") String name,
            @FormParam("custtel") String telephone, @FormParam(value = "custemail", encoded = true) String email,
            @FormParam("size") PizzaSize size, @FormParam("toppings") List<String> toppings);
    }

    /**
     * This test verifies that the form parameters are encoded when the encoded flag is set to false.
     */
    @Test
    public void postUrlForm() {
        Service26 service = createService(Service26.class);
        HttpBinFormDataJson response = service.postForm(getRequestUri(), "Foo", "123", "foo@bar.com", PizzaSize.LARGE,
            Arrays.asList("Bacon", "Onion"));
        assertNotNull(response);
        assertNotNull(response.form());
        assertEquals("Foo", response.form().customerName());
        assertEquals("123", response.form().customerTelephone());
        assertEquals("foo%40bar.com", response.form().customerEmail());
        assertEquals(PizzaSize.LARGE, response.form().pizzaSize());

        assertEquals(2, response.form().toppings().size());
        assertEquals("Bacon", response.form().toppings().get(0));
        assertEquals("Onion", response.form().toppings().get(1));
    }

    /**
     * This test verifies that the form parameters aren't encoded when the encoded flag is set to true.
     */
    @Test
    public void postUrlFormEncoded() {
        Service26 service = createService(Service26.class);
        HttpBinFormDataJson response = service.postEncodedForm(getRequestUri(), "Foo", "123", "foo@bar.com",
            PizzaSize.LARGE, Arrays.asList("Bacon", "Onion"));
        assertNotNull(response);
        assertNotNull(response.form());
        assertEquals("Foo", response.form().customerName());
        assertEquals("123", response.form().customerTelephone());
        assertEquals("foo@bar.com", response.form().customerEmail());
        assertEquals(PizzaSize.LARGE, response.form().pizzaSize());

        assertEquals(2, response.form().toppings().size());
        assertEquals("Bacon", response.form().toppings().get(0));
        assertEquals("Onion", response.form().toppings().get(1));
    }

    @Host("{url}")
    @ServiceInterface(name = "Service27")
    interface Service27 {
        @Put("put")
        @ExpectedResponses({ 200 })
        HttpBinJson put(@HostParam("url") String url, @BodyParam(ContentType.APPLICATION_OCTET_STREAM) int putBody,
            RequestOptions requestOptions);
    }

    /**
     * This test verifies that the body is overridden by the request options.
     */
    @Test
    public void requestOptionsChangesBody() {
        Service27 service = createService(Service27.class);

        HttpBinJson response
            = service.put(getServerUri(isSecure()), 42, new RequestOptions().setBody(BinaryData.fromString("24")));
        assertNotNull(response);
        assertNotNull(response.data());
        assertInstanceOf(String.class, response.data());
        assertEquals("24", response.data());
    }

    /**
     * This test verifies that the body and content length header is overridden by the request options.
     */
    @Test
    public void requestOptionsChangesBodyAndContentLength() {
        Service27 service = createService(Service27.class);

        HttpBinJson response = service.put(getServerUri(isSecure()), 42,
            new RequestOptions().setBody(BinaryData.fromString("4242")).setHeader(HttpHeaderName.CONTENT_LENGTH, "4"));
        assertNotNull(response);
        assertNotNull(response.data());
        assertInstanceOf(String.class, response.data());
        assertEquals("4242", response.data());
        assertEquals("4", response.getHeaderValue("Content-Length"));
    }

    private static final HttpHeaderName RANDOM_HEADER = HttpHeaderName.fromString("randomHeader");

    /**
     * This test verifies that add header adds a header to the request.
     */
    @Test
    public void requestOptionsAddAHeader() {
        Service27 service = createService(Service27.class);

        HttpBinJson response
            = service.put(getServerUri(isSecure()), 42, new RequestOptions().addHeader(RANDOM_HEADER, "randomValue"));
        assertNotNull(response);
        assertNotNull(response.data());
        assertInstanceOf(String.class, response.data());
        assertEquals("42", response.data());
        assertEquals("randomValue", response.getHeaderValue("randomHeader"));
    }

    /**
     * This test verifies that set header overrides any previously added headers with the same name.
     */
    @Test
    public void requestOptionsSetsAHeader() {
        Service27 service = createService(Service27.class);

        HttpBinJson response = service.put(getServerUri(isSecure()), 42,
            new RequestOptions().addHeader(RANDOM_HEADER, "randomValue").setHeader(RANDOM_HEADER, "randomValue2"));
        assertNotNull(response);
        assertNotNull(response.data());
        assertInstanceOf(String.class, response.data());
        assertEquals("42", response.data());
        assertEquals("randomValue2", response.getHeaderValue("randomHeader"));
    }

    /**
     * Service28
     */
    @SuppressWarnings("UnusedReturnValue")
    @Host("{url}")
    @ServiceInterface(name = "Service28")
    public interface Service28 {
        /**
         * Head method with void return type.
         * @param url The URL.
         */
        @Head("voideagerreadoom")
        @ExpectedResponses({ 200 })
        void headvoid(@HostParam("url") String url);

        /**
         * Head method with Void return type.
         * @param url The URL.
         * @return Void
         */
        @Head("voideagerreadoom")
        @ExpectedResponses({ 200 })
        Void headVoid(@HostParam("url") String url);

        /**
         * Head method with Response&lt;Void&gt; return type.
         * @param url The URL.
         * @return Response&lt;Void&gt;
         */
        @Head("voideagerreadoom")
        @ExpectedResponses({ 200 })
        Response<Void> headResponseVoid(@HostParam("url") String url);

        /**
         * Head method with ResponseBase&lt;Void, Void&gt; return type.
         * @param url The URL.
         * @return ResponseBase&lt;Void, Void&gt;
         */
        @Head("voideagerreadoom")
        @ExpectedResponses({ 200 })
        ResponseBase<Void, Void> headResponseBaseVoid(@HostParam("url") String url);

        /**
         * Head method with Mono&lt;Void&gt; return type.
         * @param url The URL.
         * @return Mono&lt;Void&gt;
         */
        @Head("voideagerreadoom")
        @ExpectedResponses({ 200 })
        Mono<Void> headMonoVoid(@HostParam("url") String url);

        /**
         * Head method with Mono&lt;Response&lt;Void&gt;&gt; return type.
         * @param url The URL.
         * @return Mono&lt;Response&lt;Void&gt;&gt;
         */
        @Head("voideagerreadoom")
        @ExpectedResponses({ 200 })
        Mono<Response<Void>> headMonoResponseVoid(@HostParam("url") String url);

        /**
         * Head method with Mono&lt;ResponseBase&lt;Void, Void&gt;&gt; return type.
         * @param url The URL.
         * @return Mono&lt;ResponseBase&lt;Void, Void&gt;&gt;
         */
        @Head("voideagerreadoom")
        @ExpectedResponses({ 200 })
        Mono<ResponseBase<Void, Void>> headMonoResponseBaseVoid(@HostParam("url") String url);
    }

    /**
     * This test verifies that the response body is not eagerly read when the response is void.
     *
     * @param executable The executable to test.
     */
    @ParameterizedTest
    @MethodSource("voidDoesNotEagerlyReadResponseSupplier")
    public void voidDoesNotEagerlyReadResponse(BiConsumer<String, Service28> executable) {
        assertDoesNotThrow(() -> executable.accept(getServerUri(isSecure()), createService(Service28.class)));
    }

    private static Stream<BiConsumer<String, Service28>> voidDoesNotEagerlyReadResponseSupplier() {
        return Stream.of((url, service28) -> service28.headvoid(url), (url, service28) -> service28.headVoid(url),
            (url, service28) -> service28.headResponseVoid(url),
            (url, service28) -> service28.headResponseBaseVoid(url),
            (url, service28) -> service28.headMonoVoid(url).block(),
            (url, service28) -> service28.headMonoResponseVoid(url).block(),
            (url, service28) -> service28.headMonoResponseBaseVoid(url).block());
    }

    /**
     * Service29
     */
    @SuppressWarnings("UnusedReturnValue")
    @Host("{url}")
    @ServiceInterface(name = "Service29")
    public interface Service29 {
        /**
         * Put method with void return type.
         * @param url The URL.
         */
        @Put("voiderrorreturned")
        @ExpectedResponses({ 200 })
        void headvoid(@HostParam("url") String url);

        /**
         * Put method with Void return type.
         * @param url The URL.
         * @return Void
         */
        @Put("voiderrorreturned")
        @ExpectedResponses({ 200 })
        Void headVoid(@HostParam("url") String url);

        /**
         * Put method with Response&lt;Void&gt; return type.
         * @param url The URL.
         * @return Response&lt;Void&gt;
         */
        @Put("voiderrorreturned")
        @ExpectedResponses({ 200 })
        Response<Void> headResponseVoid(@HostParam("url") String url);

        /**
         * Put method with ResponseBase&lt;Void, Void&gt; return type.
         * @param url The URL.
         * @return ResponseBase&lt;Void, Void&gt;
         */
        @Put("voiderrorreturned")
        @ExpectedResponses({ 200 })
        ResponseBase<Void, Void> headResponseBaseVoid(@HostParam("url") String url);

        /**
         * Put method with Mono&lt;Void&gt; return type.
         * @param url The URL.
         * @return Mono&lt;Void&gt;
         */
        @Put("voiderrorreturned")
        @ExpectedResponses({ 200 })
        Mono<Void> headMonoVoid(@HostParam("url") String url);

        /**
         * Put method with Mono&lt;Response&lt;Void&gt;&gt; return type.
         * @param url The URL.
         * @return Mono&lt;Response&lt;Void&gt;&gt;
         */
        @Put("voiderrorreturned")
        @ExpectedResponses({ 200 })
        Mono<Response<Void>> headMonoResponseVoid(@HostParam("url") String url);

        /**
         * Put method with Mono&lt;ResponseBase&lt;Void, Void&gt;&gt; return type.
         * @param url The URL.
         * @return Mono&lt;ResponseBase&lt;Void, Void&gt;&gt;
         */
        @Put("voiderrorreturned")
        @ExpectedResponses({ 200 })
        Mono<ResponseBase<Void, Void>> headMonoResponseBaseVoid(@HostParam("url") String url);
    }

    /**
     * This test verifies that the error response body is returned when the response is void.
     *
     * @param executable The executable to test.
     */
    @ParameterizedTest
    @MethodSource("voidErrorReturnsErrorBodySupplier")
    public void voidErrorReturnsErrorBody(BiConsumer<String, Service29> executable) {
        HttpResponseException exception = assertThrows(HttpResponseException.class,
            () -> executable.accept(getServerUri(isSecure()), createService(Service29.class)));

        assertTrue(exception.getMessage().contains("void exception body thrown"));
    }

    private static Stream<BiConsumer<String, Service29>> voidErrorReturnsErrorBodySupplier() {
        return Stream.of((url, service29) -> service29.headvoid(url), (url, service29) -> service29.headVoid(url),
            (url, service29) -> service29.headResponseVoid(url),
            (url, service29) -> service29.headResponseBaseVoid(url),
            (url, service29) -> service29.headMonoVoid(url).block(),
            (url, service29) -> service29.headMonoResponseVoid(url).block(),
            (url, service29) -> service29.headMonoResponseBaseVoid(url).block());
    }

    // Helpers

    /**
     * Creates the service instance for the given class.
     *
     * @param <T> The type of the service.
     * @param serviceClass The service class.
     * @return The service instance.
     */
    protected <T> T createService(Class<T> serviceClass) {
        final HttpClient httpClient = createHttpClient();
        return createService(serviceClass, httpClient);
    }

    /**
     * Creates the service instance for the given class.
     *
     * @param <T> The type of the service.
     * @param serviceClass The service class.
     * @param httpClient The HTTP client to use.
     * @return The service instance.
     */
    protected <T> T createService(Class<T> serviceClass, HttpClient httpClient) {
        final HttpPipeline httpPipeline
            = new HttpPipelineBuilder().policies(new PortPolicy(getPort(), true)).httpClient(httpClient).build();

        return RestProxy.create(serviceClass, httpPipeline);
    }

    @SuppressWarnings("HttpUrlsUsage")
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
