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
import com.azure.core.util.CoreUtils;
import com.azure.core.util.ProgressReporter;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.io.IOUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static final byte[] EXPECTED_RETURN_BYTES = "Hello World!".getBytes(StandardCharsets.UTF_8);

    private static final Map<Integer, byte[]> SHARED_DATA;
    private static final Map<Integer, Path> SHARED_TEST_FILES;
    private static final Map<Integer, Path> SHARED_TEST_SLICE_FILES;

    static {
        SHARED_DATA = new HashMap<>();
        SHARED_TEST_FILES = new HashMap<>();
        SHARED_TEST_SLICE_FILES = new HashMap<>();
        byte[] copyBytes = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

        List<Integer> fileSizes = Arrays.asList(1, 2, 10, 127, 1024, 1024 + 157, 8 * 1024 + 3, 10 * 1024 * 1024 + 13);
        for (int fileSize : fileSizes) {
            byte[] sharedData = new byte[fileSize];
            createCopyBytesArray(sharedData, copyBytes);
            SHARED_DATA.put(fileSize, sharedData);

            SHARED_TEST_FILES.put(fileSize, createFile("sharedHttpClientTestsFile" + CoreUtils.randomUuid(),
                sharedData));

            byte[] sliceFileData = new byte[8192 + fileSize + 8192];
            System.arraycopy(sharedData, 0, sliceFileData, 8192, fileSize);
            SHARED_TEST_SLICE_FILES.put(fileSize, createFile("sharedHttpClientTestsSliceFile" + CoreUtils.randomUuid(),
                sliceFileData));
        }
    }

    private static void createCopyBytesArray(byte[] array, byte[] copyBytes) {
        int loops = array.length / copyBytes.length;
        int remainder = array.length % copyBytes.length;

        for (int i = 0; i < loops; i++) {
            System.arraycopy(copyBytes, 0, array, i * copyBytes.length, copyBytes.length);
        }

        System.arraycopy(copyBytes, 0, array, loops * copyBytes.length, remainder);
    }

    private static Path createFile(String fileName, byte[] data) {
        try {
            Path sharedFile = Files.createTempFile(fileName, String.valueOf(data.length));
            sharedFile.toFile().deleteOnExit();
            Files.write(sharedFile, data);

            return sharedFile;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

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
     *
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
     * @param testDataBinaryData The test data.
     */
    @TestTemplate
    @ExtendWith(RepeatedParameterizedTestTemplate.class)
    public void canSendBinaryData(BinaryDataTestData testDataBinaryData) {
        HttpRequest request = new HttpRequest(HttpMethod.PUT, getRequestUrl(ECHO_RESPONSE), new HttpHeaders(),
            testDataBinaryData.getBinaryData());

        StepVerifier.create(createHttpClient()
                .send(request)
                .flatMap(HttpResponse::getBodyAsByteArray))
            .assertNext(responseBytes -> assertArrayEquals(testDataBinaryData.getBytes(), responseBytes))
            .verifyComplete();
    }

    /**
     * Tests that send random bytes in various forms to an endpoint that echoes bytes back to sender.
     *
     * @param testDataBinaryData The test data.
     */
    @TestTemplate
    @ExtendWith(RepeatedParameterizedTestTemplate.class)
    public void canSendBinaryDataSync(BinaryDataTestData testDataBinaryData) {
        HttpRequest request = new HttpRequest(HttpMethod.PUT, getRequestUrl(ECHO_RESPONSE), new HttpHeaders(),
            testDataBinaryData.getBinaryData());

        try (HttpResponse httpResponse = createHttpClient().sendSync(request, Context.NONE)) {
            byte[] responseBytes = httpResponse.getBodyAsByteArray().block();

            assertArrayEquals(testDataBinaryData.getBytes(), responseBytes);
        }
    }

    /**
     * Tests that send random bytes in various forms to an endpoint that echoes bytes back to sender.
     *
     * @param testDataBinaryData The test data.
     */
    @TestTemplate
    @ExtendWith(RepeatedParameterizedTestTemplate.class)
    public void canSendBinaryDataWithProgressReporting(BinaryDataTestData testDataBinaryData) {
        HttpRequest request = new HttpRequest(HttpMethod.PUT, getRequestUrl(ECHO_RESPONSE), new HttpHeaders(),
            testDataBinaryData.getBinaryData());

        AtomicLong progress = new AtomicLong();
        Context context = Contexts.empty()
            .setHttpRequestProgressReporter(ProgressReporter.withProgressListener(progress::set))
            .getContext();

        StepVerifier.create(createHttpClient().send(request, context).flatMap(HttpResponse::getBodyAsByteArray))
            .assertNext(responseBytes -> assertArrayEquals(testDataBinaryData.getBytes(), responseBytes))
            .verifyComplete();

        assertEquals(testDataBinaryData.getBytes().length, progress.intValue());
    }

    /**
     * Tests that send random bytes in various forms to an endpoint that echoes bytes back to sender.
     *
     * @param testDataBinaryData The test data.
     */
    @TestTemplate
    @ExtendWith(RepeatedParameterizedTestTemplate.class)
    public void canSendBinaryDataWithProgressReportingSync(BinaryDataTestData testDataBinaryData) {
        HttpRequest request = new HttpRequest(HttpMethod.PUT, getRequestUrl(ECHO_RESPONSE), new HttpHeaders(),
            testDataBinaryData.getBinaryData());

        AtomicLong progress = new AtomicLong();
        Context context = Contexts.empty()
            .setHttpRequestProgressReporter(ProgressReporter.withProgressListener(progress::set))
            .getContext();

        try (HttpResponse httpResponse = createHttpClient().sendSync(request, context)) {
            byte[] responseBytes = httpResponse.getBodyAsByteArray().block();

            assertArrayEquals(testDataBinaryData.getBytes(), responseBytes);
            assertEquals(testDataBinaryData.getBytes().length, progress.intValue());
        }
    }

    private static final class RepeatedParameterizedTestTemplate implements TestTemplateInvocationContextProvider {

        @Override
        public boolean supportsTestTemplate(ExtensionContext context) {
            return true;
        }

        @Override
        public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
            // Repeat BinaryData tests 100 times for validation.
            Stream<TestTemplateInvocationContext> stream = Stream.empty();
            for (int i = 0; i < 100; i++) {
                stream = Stream.concat(stream, getBinaryDataBodyVariants()
                    .map(RepeatedParameterizedTestTemplate::createInvocationContext));
            }

            return stream;
        }

        private static TestTemplateInvocationContext createInvocationContext(BinaryDataTestData testData) {
            return new TestTemplateInvocationContext() {
                @Override
                public String getDisplayName(int invocationIndex) {
                    return TestTemplateInvocationContext.super.getDisplayName(invocationIndex);
                }

                @Override
                public List<Extension> getAdditionalExtensions() {
                    return Collections.singletonList(new ParameterResolver() {
                        @Override
                        public boolean supportsParameter(ParameterContext parameterContext,
                            ExtensionContext extensionContext) throws ParameterResolutionException {
                            return true;
                        }

                        @Override
                        public Object resolveParameter(ParameterContext parameterContext,
                            ExtensionContext extensionContext) throws ParameterResolutionException {
                            return testData;
                        }
                    });
                }
            };
        }
    }

    /**
     * Content for a BinaryData test.
     */
    public static final class BinaryDataTestData {
        private final BinaryData binaryData;
        private final byte[] bytes;

        /**
         * Creates a new BinaryDataTestData.
         *
         * @param binaryData The BinaryData being sent over the wire.
         * @param bytes The expected bytes to be received.
         */
        public BinaryDataTestData(BinaryData binaryData, byte[] bytes) {
            this.binaryData = binaryData;
            this.bytes = bytes;
        }

        /**
         * Gets the BinaryData to send over the wire.
         *
         * @return The BinaryData to send over the wire.
         */
        public BinaryData getBinaryData() {
            return binaryData;
        }

        /**
         * Gets the expected bytes to be returned from the echo body endpoint.
         *
         * @return The expected response bytes.
         */
        public byte[] getBytes() {
            return bytes;
        }
    }

    private static Stream<BinaryDataTestData> getBinaryDataBodyVariants() {
        return Stream.of(1, 2, 10, 127, 1024, 1024 + 157, 8 * 1024 + 3, 10 * 1024 * 1024 + 13)
            .flatMap(size -> {
                byte[] bytes = SHARED_DATA.get(size);

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

                BinaryData fileData = BinaryData.fromFile(SHARED_TEST_FILES.get(size));

                BinaryData sliceFileData = BinaryData.fromFile(SHARED_TEST_SLICE_FILES.get(size), 8192L, (long) size);

                return Stream.of(
                    new BinaryDataTestData(byteArrayData, bytes),
                    new BinaryDataTestData(stringBinaryData, randomStringBytes),
                    new BinaryDataTestData(streamData, bytes),
                    new BinaryDataTestData(fluxBinaryData, bytes),
                    new BinaryDataTestData(fluxBinaryDataWithLength, bytes),
                    new BinaryDataTestData(asyncFluxBinaryData, bytes),
                    new BinaryDataTestData(asyncFluxBinaryDataWithLength, bytes),
                    new BinaryDataTestData(objectBinaryData, bytes),
                    new BinaryDataTestData(fileData, bytes),
                    new BinaryDataTestData(sliceFileData, bytes));
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
            String prefix = isSecure() ? "https://" : "http://";
            return UrlBuilder.parse(prefix + REQUEST_HOST + ":" + getWireMockPort() + "/" + requestPath).toUrl();
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
