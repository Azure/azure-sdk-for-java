// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.shared;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.models.ServerSentEvent;
import io.clientcore.core.http.models.ServerSentEventListener;
import io.clientcore.core.http.pipeline.HttpInstrumentationOptions;
import io.clientcore.core.http.pipeline.HttpInstrumentationPolicy;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.implementation.http.ContentType;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.ObjectSerializer;
import io.clientcore.core.serialization.SerializationFormat;
import io.clientcore.core.utils.IOExceptionCheckedFunction;
import io.clientcore.core.utils.UriBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static io.clientcore.core.implementation.utils.ImplUtils.bomAwareToString;
import static io.clientcore.core.shared.HttpClientTestsServer.BOM_WITH_DIFFERENT_HEADER;
import static io.clientcore.core.shared.HttpClientTestsServer.ECHO_RESPONSE;
import static io.clientcore.core.shared.HttpClientTestsServer.HEADER_RESPONSE;
import static io.clientcore.core.shared.HttpClientTestsServer.HUGE_HEADER_NAME;
import static io.clientcore.core.shared.HttpClientTestsServer.HUGE_HEADER_RESPONSE;
import static io.clientcore.core.shared.HttpClientTestsServer.HUGE_HEADER_VALUE;
import static io.clientcore.core.shared.HttpClientTestsServer.INVALID_HEADER_RESPONSE;
import static io.clientcore.core.shared.HttpClientTestsServer.PLAIN_RESPONSE;
import static io.clientcore.core.shared.HttpClientTestsServer.RETURN_BYTES;
import static io.clientcore.core.shared.HttpClientTestsServer.SSE_RESPONSE;
import static io.clientcore.core.shared.HttpClientTestsServer.UTF_16BE_BOM_RESPONSE;
import static io.clientcore.core.shared.HttpClientTestsServer.UTF_16LE_BOM_RESPONSE;
import static io.clientcore.core.shared.HttpClientTestsServer.UTF_32BE_BOM_RESPONSE;
import static io.clientcore.core.shared.HttpClientTestsServer.UTF_32LE_BOM_RESPONSE;
import static io.clientcore.core.shared.HttpClientTestsServer.UTF_8_BOM_RESPONSE;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Generic test suite for {@link HttpClient HttpClients}.
 */
@Execution(ExecutionMode.SAME_THREAD)
public abstract class HttpClientTests {
    private static final ClientLogger LOGGER = new ClientLogger(HttpClientTests.class);

    /**
     * Get the HTTP client that will be used for each test. This will be called once per test.
     *
     * @return The HTTP client to use for each test.
     */
    protected abstract HttpClient getHttpClient();

    /**
     * Gets the dynamic URI the server is using to properly route the request.
     *
     * @param secure Flag indicating if the URI should be for a secure connection or not.
     * @return The URI the server is using.
     */
    protected abstract String getServerUri(boolean secure);

    /**
     * Gets the port the server is using to properly route the request.
     *
     * @return The port the server is using.
     */
    protected abstract int getPort();

    /**
     * Get a flag indicating if communication should be secured or not (https or http).
     *
     * @return A flag indicating if communication should be secured or not (https or http).
     */
    protected boolean isSecure() {
        return false;
    }

    /**
     * Flag indicating if testing is using HTTP/2.
     *
     * @return Whether the test is using HTTP/2.
     */
    protected boolean isHttp2() {
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
    @Test
    public void plainResponse() {
        String expected = new String(RETURN_BYTES, StandardCharsets.UTF_8);

        assertEquals(expected, new String(sendRequest(PLAIN_RESPONSE), StandardCharsets.UTF_8));
    }

    /**
     * Tests that a response with a 'Content-Type' header encodes using the specified charset.
     */
    @Test
    public void headerResponse() {
        String expected = new String(RETURN_BYTES, StandardCharsets.UTF_16BE);

        assertEquals(expected, new String(sendRequest(HEADER_RESPONSE), StandardCharsets.UTF_16BE));
    }

    /**
     * Tests that a response with a 'Content-Type' containing an invalid or unsupported charset encodes using UTF-8.
     */
    @Test
    public void invalidHeaderResponse() {
        String expected = new String(RETURN_BYTES, StandardCharsets.UTF_8);

        assertEquals(expected, new String(sendRequest(INVALID_HEADER_RESPONSE), StandardCharsets.UTF_8));
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @Test
    public void utf8BomResponse() {
        String expected = new String(RETURN_BYTES, StandardCharsets.UTF_8);
        byte[] response = sendRequest(UTF_8_BOM_RESPONSE);

        assertEquals(expected, bomAwareToString(response, 0, response.length, null));
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @Test
    public void utf16BeBomResponse() {
        String expected = new String(RETURN_BYTES, StandardCharsets.UTF_16BE);
        byte[] response = sendRequest(UTF_16BE_BOM_RESPONSE);

        assertEquals(expected, bomAwareToString(response, 0, response.length, null));
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @Test
    public void utf16LeBomResponse() {
        String expected = new String(RETURN_BYTES, StandardCharsets.UTF_16LE);
        byte[] response = sendRequest(UTF_16LE_BOM_RESPONSE);

        assertEquals(expected, bomAwareToString(response, 0, response.length, null));
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @Test
    public void utf32BeBomResponse() {
        String expected = new String(RETURN_BYTES, Charset.forName("UTF-32BE"));

        assertEquals(expected, new String(sendRequest(UTF_32BE_BOM_RESPONSE), Charset.forName("UTF-32BE")));
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @Test
    public void utf32LeBomResponse() {
        String expected = new String(RETURN_BYTES, Charset.forName("UTF-32LE"));

        assertEquals(expected, new String(sendRequest(UTF_32LE_BOM_RESPONSE), Charset.forName("UTF-32LE")));
    }

    /**
     * Tests that a response with a byte order marker and 'Content-Type' header will defer to using the BOM encoding.
     */
    @Test
    public void bomWithSameHeader() {
        String expected = new String(RETURN_BYTES, StandardCharsets.UTF_8);
        byte[] response = sendRequest(BOM_WITH_DIFFERENT_HEADER);

        assertEquals(expected, bomAwareToString(response, 0, response.length, "charset=utf-8"));
    }

    /**
     * Tests that a response with a byte order marker and 'Content-Type' header will defer to using the BOM encoding.
     */
    @Test
    public void bomWithDifferentHeader() {
        String expected = new String(RETURN_BYTES, StandardCharsets.UTF_8);
        byte[] response = sendRequest(BOM_WITH_DIFFERENT_HEADER);

        assertEquals(expected, bomAwareToString(response, 0, response.length, "charset=utf-16"));
    }

    /**
     * Tests that unbuffered response body can be accessed.
     */
    @Test
    public void canAccessResponseBody() {
        BinaryData requestBody = BinaryData.fromString("test body");
        HttpRequest request
            = new HttpRequest().setMethod(HttpMethod.PUT).setUri(getRequestUri(ECHO_RESPONSE)).setBody(requestBody);

        try (Response<BinaryData> response = getHttpClient().send(request)) {
            assertEquals(requestBody.toString(), response.getValue().toString());
            assertArrayEquals(requestBody.toBytes(), response.getValue().toBytes());
        }
    }

    /**
     * Tests that buffered response is indeed buffered, i.e. content can be accessed many times.
     */
    @Test
    public void bufferedResponseCanBeReadMultipleTimes() {
        BinaryData requestBody = BinaryData.fromString("test body");
        HttpRequest request
            = new HttpRequest().setMethod(HttpMethod.PUT).setUri(getRequestUri(ECHO_RESPONSE)).setBody(requestBody);

        try (Response<BinaryData> response = getHttpClient().send(request)) {
            // Read response twice using all accessors.
            assertEquals(requestBody.toString(), response.getValue().toString());
            assertEquals(requestBody.toString(), response.getValue().toString());

            assertArrayEquals(requestBody.toBytes(), response.getValue().toBytes());
            assertArrayEquals(requestBody.toBytes(), response.getValue().toBytes());
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
        HttpRequest request
            = new HttpRequest().setMethod(HttpMethod.PUT).setUri(getRequestUri(ECHO_RESPONSE)).setBody(requestBody);

        try (Response<BinaryData> response = getHttpClient().send(request)) {
            assertArrayEquals(expectedResponseBody, response.getValue().toBytes());
        }
    }

    /*
     * Tests that send random bytes in various forms to an endpoint that echoes bytes back to sender.
     *
     * @param requestBody The BinaryData that contains random bytes.
     * @param expectedResponseBody The expected bytes in the echo response.
     */
    /*@ParameterizedTest
    @MethodSource("getBinaryDataBodyVariants")
    public void canSendBinaryDataWithProgressReporting(BinaryData requestBody, byte[] expectedResponseBody) {
        HttpRequest request = new HttpRequest(
            HttpMethod.PUT,
            getProtocol(ECHO_RESPONSE),
            new Headers(),
            requestBody);
    
        AtomicLong progress = new AtomicLong();
        Context context = Contexts.empty()
            .setHttpRequestProgressReporter(
                ProgressReporter.withProgressListener(progress::set))
            .getContext();
    
        Response<?> response = createHttpClient()
            .send(request);
    
        byte[] responseBytes = response
            .getBodyAsByteArray()
            .block();
    
        assertArrayEquals(expectedResponseBody, responseBytes);
        assertEquals(expectedResponseBody.length, progress.intValue());
    }*/

    private static Stream<Arguments> getBinaryDataBodyVariants() {
        return Stream.of(1, 2, 10, 127, 1024, 1024 + 157, 8 * 1024 + 3, 10 * 1024 * 1024 + 13).flatMap(size -> {
            try {
                byte[] bytes = new byte[size];

                ThreadLocalRandom.current().nextBytes(bytes);

                BinaryData byteArrayData = BinaryData.fromBytes(bytes);
                String randomString = new String(bytes, StandardCharsets.UTF_8);
                byte[] randomStringBytes = randomString.getBytes(StandardCharsets.UTF_8);
                BinaryData stringBinaryData = BinaryData.fromString(randomString);
                BinaryData streamData = BinaryData.fromStream(new ByteArrayInputStream(bytes), (long) bytes.length);

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
                    Arguments.of(Named.named("InputStream", streamData), Named.named(String.valueOf(size), bytes)),
                    Arguments.of(Named.named("Object", objectBinaryData), Named.named(String.valueOf(size), bytes)),
                    Arguments.of(Named.named("File", fileData), Named.named(String.valueOf(size), bytes)),
                    Arguments.of(Named.named("File slice", sliceFileData), Named.named(String.valueOf(size), bytes)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private byte[] sendRequest(String requestPath) {
        try (Response<BinaryData> response
            = getHttpClient().send(new HttpRequest().setMethod(HttpMethod.GET).setUri(getRequestUri(requestPath)))) {
            return response.getValue().toBytes();
        }
    }

    /**
     * Gets the request URI for given path.
     *
     * @param requestPath The path.
     * @return The request URI for given path.
     * @throws RuntimeException if uri is invalid.
     */
    protected URI getRequestUri(String requestPath) {
        try {
            return UriBuilder.parse(getServerUri(isSecure()) + "/" + requestPath).toUri();
        } catch (URISyntaxException e) {
            throw LOGGER.throwableAtError().log(e, RuntimeException::new);
        }
    }

    private static class ByteArraySerializer implements ObjectSerializer {
        @Override
        public <T> T deserializeFromBytes(byte[] data, Type type) {
            return null;
        }

        @Override
        public <T> T deserializeFromStream(InputStream stream, Type type) {
            return null;
        }

        @Override
        public byte[] serializeToBytes(Object value) {
            return (byte[]) value;
        }

        @Override
        public void serializeToStream(OutputStream stream, Object value) {
            try {
                stream.write((byte[]) value);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean supportsFormat(SerializationFormat format) {
            return false;
        }
    }

    /**
     * Tests that the response body is correctly returned as a byte array.
     */
    @Test
    public void requestWithByteArrayReturnType() {
        String uri = UriBuilder.parse(getRequestUri()).setPath("bytes/100").toString();

        try (Response<BinaryData> response
            = getHttpClient().send(new HttpRequest().setMethod(HttpMethod.GET).setUri(uri))) {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            assertEquals(100, response.getValue().toBytes().length);
        }
    }

    /**
     * Tests that the response body is correctly returned as a byte array.
     */
    @Test
    public void requestWithByteArrayReturnTypeAndParameterizedHostAndPath() {
        String uri = new UriBuilder().setScheme(getRequestScheme())
            .setHost("localhost")
            .setPort(getPort())
            .setPath("bytes/" + 100)
            .toString();

        try (Response<BinaryData> response
            = getHttpClient().send(new HttpRequest().setMethod(HttpMethod.GET).setUri(uri))) {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            assertEquals(100, response.getValue().toBytes().length);
        }
    }

    /**
     * Tests that the response body is correctly returned as a byte array.
     */
    @Test
    public void requestWithEmptyByteArrayReturnTypeAndParameterizedHostAndPath() {
        String uri = new UriBuilder().setScheme(getRequestScheme())
            .setHost("localhost")
            .setPort(getPort())
            .setPath("bytes/" + 0)
            .toString();

        try (Response<BinaryData> response
            = getHttpClient().send(new HttpRequest().setMethod(HttpMethod.GET).setUri(uri))) {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            assertEquals(0, response.getValue().toBytes().length);
        }
    }

    @Test
    public void getRequestWithAnything() {
        String uri = UriBuilder.parse(getRequestUri()).setPath("anything").toString();

        sendRequestAndConsumeHttpBinJson(new HttpRequest().setMethod(HttpMethod.GET).setUri(uri),
            json -> assertMatchWithHttpOrHttps("localhost/anything", json.uri()));
    }

    @Test
    public void getRequestWithAnythingWithPlus() {
        String uri = UriBuilder.parse(getRequestUri()).setPath("anything/with+plus").toString();

        sendRequestAndConsumeHttpBinJson(new HttpRequest().setMethod(HttpMethod.GET).setUri(uri),
            json -> assertMatchWithHttpOrHttps("localhost/anything/with+plus", json.uri()));
    }

    @Test
    public void getRequestWithAnythingWithPathParam() {
        String uri = UriBuilder.parse(getRequestUri()).setPath("anything/withpathparam").toString();

        sendRequestAndConsumeHttpBinJson(new HttpRequest().setMethod(HttpMethod.GET).setUri(uri),
            json -> assertMatchWithHttpOrHttps("localhost/anything/withpathparam", json.uri()));
    }

    @Test
    public void getRequestWithAnythingWithPathParamWithSpace() {
        String uri = UriBuilder.parse(getRequestUri()).setPath("anything/with%20path%20param").toString();

        sendRequestAndConsumeHttpBinJson(new HttpRequest().setMethod(HttpMethod.GET).setUri(uri),
            json -> assertMatchWithHttpOrHttps("localhost/anything/with path param", json.uri()));
    }

    @Test
    public void getRequestWithAnythingWithPathParamWithPlus() {
        String uri = UriBuilder.parse(getRequestUri()).setPath("anything/with+path+param").toString();

        sendRequestAndConsumeHttpBinJson(new HttpRequest().setMethod(HttpMethod.GET).setUri(uri),
            json -> assertMatchWithHttpOrHttps("localhost/anything/with+path+param", json.uri()));
    }

    @Test
    public void getRequestWithQueryParametersAndAnything() {
        String uri = UriBuilder.parse(getRequestUri())
            .setPath("anything")
            .addQueryParameter("a", "A")
            .addQueryParameter("b", String.valueOf(15))
            .toString();

        sendRequestAndConsumeHttpBinJson(new HttpRequest().setMethod(HttpMethod.GET).setUri(uri),
            json -> assertMatchWithHttpOrHttps("localhost/anything?a=A&b=15", json.uri()));
    }

    @Test
    public void getRequestWithQueryParametersAndAnythingWithPercent20() {
        String uri = UriBuilder.parse(getRequestUri())
            .setPath("anything")
            .addQueryParameter("a", "A%20Z")
            .addQueryParameter("b", String.valueOf(15))
            .toString();

        sendRequestAndConsumeHttpBinJson(new HttpRequest().setMethod(HttpMethod.GET).setUri(uri),
            json -> assertMatchWithHttpOrHttps("localhost/anything?a=A Z&b=15", json.uri()));
    }

    private static final HttpHeaderName HEADER_A = HttpHeaderName.fromString("A");
    private static final HttpHeaderName HEADER_B = HttpHeaderName.fromString("B");

    @Test
    public void getRequestWithHeaderParametersAndAnythingReturn() {
        String uri = UriBuilder.parse(getRequestUri()).setPath("anything").toString();
        HttpHeaders headers = new HttpHeaders().set(HEADER_A, "A").set(HEADER_B, String.valueOf(15));

        sendRequestAndConsumeHttpBinJson(new HttpRequest().setMethod(HttpMethod.GET).setUri(uri).setHeaders(headers),
            json -> {
                assertMatchWithHttpOrHttps("localhost/anything", json.uri());
                assertNotNull(json.headers());
                HttpHeaders jsonHeaders = toHttpHeaders(json.headers());

                assertEquals("A", jsonHeaders.getValue(HEADER_A));
                assertListEquals(Collections.singletonList("A"), jsonHeaders.getValues(HEADER_A));

                assertEquals("15", jsonHeaders.getValue(HEADER_B));
                assertListEquals(Collections.singletonList("15"), jsonHeaders.getValues(HEADER_B));
            });
    }

    @Test
    public void postRequestWithStringBody() {
        BinaryData body = BinaryData.fromString("I'm a post body!");
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, ContentType.APPLICATION_OCTET_STREAM);
        String uri = UriBuilder.parse(getRequestUri()).setPath("post").toString();

        sendRequestAndConsumeHttpBinJson(
            new HttpRequest().setMethod(HttpMethod.POST).setUri(uri).setHeaders(headers).setBody(body), json -> {
                assertEquals(String.class, json.data().getClass());
                assertEquals("I'm a post body!", json.data());
            });
    }

    @Test
    public void postRequestWithNullBody() {
        BinaryData body = BinaryData.empty();
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, ContentType.APPLICATION_OCTET_STREAM);
        String uri = UriBuilder.parse(getRequestUri()).setPath("post").toString();

        sendRequestAndConsumeHttpBinJson(
            new HttpRequest().setMethod(HttpMethod.POST).setUri(uri).setHeaders(headers).setBody(body), json -> {
                assertEquals(String.class, json.data().getClass());
                assertEquals("", json.data());
            });
    }

    @Test
    public void putRequestWithIntBody() {
        BinaryData body = BinaryData.fromObject(42);
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, ContentType.APPLICATION_OCTET_STREAM);
        String uri = UriBuilder.parse(getRequestUri()).setPath("put").toString();

        sendRequestAndConsumeHttpBinJson(
            new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri).setHeaders(headers).setBody(body), json -> {
                assertEquals(String.class, json.data().getClass());
                assertEquals("42", json.data());
            });
    }

    // Test all scenarios for the body length and content length comparison for sync API
    @Test
    public void putRequestWithBodyAndEqualContentLength() {
        BinaryData body = BinaryData.fromBytes("test".getBytes(StandardCharsets.UTF_8));
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, ContentType.APPLICATION_OCTET_STREAM)
            .set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(4L));
        String uri = UriBuilder.parse(getRequestUri()).setPath("put").toString();

        sendRequestAndConsumeHttpBinJson(
            new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri).setHeaders(headers).setBody(body), json -> {
                assertEquals(String.class, json.data().getClass());
                assertEquals("test", json.data());
                assertEquals(ContentType.APPLICATION_OCTET_STREAM,
                    json.getHeaderValue(isHttp2() ? "content-type" : "Content-Type"));
                assertEquals("4", json.getHeaderValue(isHttp2() ? "content-length" : "Content-Length"));
            });
    }

    @Test
    public void headRequest() {
        String uri = UriBuilder.parse(getRequestUri()).setPath("anything").toString();
        try (Response<BinaryData> response
            = getHttpClient().send(new HttpRequest().setMethod(HttpMethod.HEAD).setUri(uri))) {
            assertEquals(BinaryData.empty(), response.getValue());
        }
    }

    @Test
    public void deleteRequest() {
        BinaryData body = BinaryData.fromObject(false);
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, ContentType.APPLICATION_OCTET_STREAM);
        String uri = UriBuilder.parse(getRequestUri()).setPath("delete").toString();

        sendRequestAndConsumeHttpBinJson(
            new HttpRequest().setMethod(HttpMethod.DELETE).setUri(uri).setHeaders(headers).setBody(body), json -> {
                assertEquals(String.class, json.data().getClass());
                assertEquals("false", json.data());
            });
    }

    @Test
    public void patchRequest() {
        BinaryData body = BinaryData.fromString("body-contents");
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, ContentType.APPLICATION_OCTET_STREAM);
        String uri = UriBuilder.parse(getRequestUri()).setPath("patch").toString();

        sendRequestAndConsumeHttpBinJson(
            new HttpRequest().setMethod(HttpMethod.PATCH).setUri(uri).setHeaders(headers).setBody(body), json -> {
                assertEquals(String.class, json.data().getClass());
                assertEquals("body-contents", json.data());
            });
    }

    private static HttpHeaders toHttpHeaders(Map<String, List<String>> jsonHeaders) {
        HttpHeaders headers = new HttpHeaders();
        for (Map.Entry<String, List<String>> entry : jsonHeaders.entrySet()) {
            HttpHeaderName headerName = HttpHeaderName.fromString(entry.getKey());
            for (String value : entry.getValue()) {
                headers.add(headerName, value);
            }
        }
        return headers;
    }

    @Test
    public void service16Put() {
        byte[] expectedBytes = new byte[] { 1, 2, 3, 4 };
        BinaryData body = BinaryData.fromBytes(expectedBytes);
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, ContentType.APPLICATION_OCTET_STREAM);
        String uri = UriBuilder.parse(getRequestUri()).setPath("put").toString();

        sendRequestAndConsumeHttpBinJson(
            new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri).setHeaders(headers).setBody(body), json -> {
                // httpbin sends the data back as a string like "\u0001\u0002\u0003\u0004"
                assertInstanceOf(String.class, json.data());

                final String base64String = (String) json.data();
                final byte[] actualBytes = base64String.getBytes();

                assertArrayEquals(expectedBytes, actualBytes);
            });
    }

    @Test
    public void binaryDataUploadTest() throws Exception {
        Path filePath = Paths.get(getClass().getClassLoader().getResource("upload.txt").toURI());
        BinaryData body = BinaryData.fromFile(filePath);

        // Scenario: Log the body so that body buffering/replay behavior is exercised.
        // Order in which policies applied will be the order in which they added to builder
        final HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(getHttpClient())
            .addPolicy(new HttpInstrumentationPolicy(new HttpInstrumentationOptions()
                .setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.BODY_AND_HEADERS)))
            .build();

        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "text/plain")
            .set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(Files.size(filePath)));
        String uri = UriBuilder.parse(getRequestUri()).setPath("put").toString();

        sendRequestAndConsumeHttpBinJson(
            new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri).setHeaders(headers).setBody(body),
            httpPipeline::send, json -> {
                assertInstanceOf(String.class, json.data());
                assertEquals("The quick brown fox jumps over the lazy dog", json.data());
            });
    }

    private void sendRequestAndConsumeHttpBinJson(HttpRequest request, Consumer<HttpBinJSON> jsonConsumer) {
        sendRequestAndConsumeHttpBinJson(request, getHttpClient()::send, jsonConsumer);
    }

    private void sendRequestAndConsumeHttpBinJson(HttpRequest request,
        IOExceptionCheckedFunction<HttpRequest, Response<BinaryData>> requestSend, Consumer<HttpBinJSON> jsonConsumer) {
        try (Response<BinaryData> response = requestSend.apply(request)) {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            assertNotNull(response.getValue());

            HttpBinJSON json = response.getValue().toObject(HttpBinJSON.class);
            assertNotNull(json);
            jsonConsumer.accept(json);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Test
    public void canReceiveServerSentEvents() {
        String uri = UriBuilder.parse(getRequestUri()).setPath(SSE_RESPONSE).toString();
        final int[] i = { 0 };
        ServerSentEventListener serverSentEventListener = sse -> {
            String expected;
            String id;
            if (i[0] == 0) {
                expected = "first event";
                id = "1";
                Assertions.assertEquals("test stream", sse.getComment());
            } else {
                expected = "This is the second message, it";
                String line2 = "has two lines.";

                id = "2";
                Assertions.assertEquals(line2, sse.getData().get(1));
            }
            Assertions.assertEquals(expected, sse.getData().get(0));
            Assertions.assertEquals(id, sse.getId());
            if (++i[0] > 2) {
                fail("Should not have received more than two messages.");
            }
        };

        getHttpClient().send(
            new HttpRequest().setMethod(HttpMethod.GET).setUri(uri).setServerSentEventListener(serverSentEventListener))
            .close();

        assertEquals(2, i[0]);
    }

    /**
     * Tests that eagerly converting implementation HTTP headers to Client Core Headers is done.
     */
    @Test
    public void canRecognizeServerSentEvent() {
        List<String> expected = Arrays.asList("YHOO", "+2", "10");
        String uri = UriBuilder.parse(getRequestUri()).setPath(SSE_RESPONSE).toString();
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, ContentType.APPLICATION_OCTET_STREAM);
        ServerSentEventListener serverSentEventListener = sse -> assertEquals(expected, sse.getData());

        try (Response<BinaryData> response = getHttpClient().send(new HttpRequest().setMethod(HttpMethod.POST)
            .setUri(uri)
            .setHeaders(headers)
            .setServerSentEventListener(serverSentEventListener))) {
            assertNotNull(response.getValue());
            assertNotEquals(0, response.getValue().getLength());
            assertNotNull(response.getValue());
            assertEquals(String.join("\n", expected), response.getValue().toString());
        }
    }

    @Test
    public void onErrorServerSentEvents() throws IOException {
        String uri = UriBuilder.parse(getRequestUri()).setPath(SSE_RESPONSE).toString();
        final int[] i = { 0 };
        ServerSentEventListener serverSentEventListener = new ServerSentEventListener() {
            @Override
            public void onEvent(ServerSentEvent sse) throws IOException {
                throw new IOException("test exception");
            }

            @Override
            public void onError(Throwable throwable) {
                assertEquals("test exception", throwable.getMessage());
                i[0]++;
            }
        };

        getHttpClient().send(
            new HttpRequest().setMethod(HttpMethod.GET).setUri(uri).setServerSentEventListener(serverSentEventListener))
            .close();

        assertEquals(1, i[0]);
    }

    @Test
    public void onRetryWithLastEventIdReceiveServerSentEvents() {
        String uri = UriBuilder.parse(getRequestUri()).setPath(SSE_RESPONSE).toString();
        final int[] i = { 0 };
        ServerSentEventListener serverSentEventListener = sse -> {
            i[0]++;
            if (i[0] == 1) {
                assertEquals("test stream", sse.getComment());
                assertEquals("first event", sse.getData().get(0));
                assertEquals("1", sse.getId());
            } else if (i[0] == 2) {
                assertTimeout(Duration.ofMillis(100L), () -> assertEquals("2", sse.getId()));
                assertEquals("This is the second message, it", sse.getData().get(0));
                assertEquals("has two lines.", sse.getData().get(1));
            }
            if (i[0] >= 3) {
                fail("Should not have received more than two messages.");
            }
        };

        getHttpClient().send(
            new HttpRequest().setMethod(HttpMethod.GET).setUri(uri).setServerSentEventListener(serverSentEventListener))
            .close();

        assertEquals(2, i[0]);
    }

    /**
     * Test throws Runtime exception for no listener attached.
     */
    @Test
    public void throwsExceptionForNoListener() {
        BinaryData body = BinaryData.fromString("test body");
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, ContentType.APPLICATION_OCTET_STREAM);
        String uri = UriBuilder.parse(getRequestUri()).setPath(SSE_RESPONSE).toString();

        assertThrows(RuntimeException.class,
            () -> getHttpClient().send(new HttpRequest().setMethod(HttpMethod.PUT)
                .setUri(uri)
                .setBody(body)
                .setHeaders(headers)
                .setServerSentEventListener(null)).close());
    }

    @Test
    public void testHugeHeader() {
        try (Response<BinaryData> response = getHttpClient()
            .send(new HttpRequest().setMethod(HttpMethod.GET).setUri(getRequestUri(HUGE_HEADER_RESPONSE)))) {
            String hugeHeaderValue = response.getHeaders().getValue(HUGE_HEADER_NAME);
            assertNotNull(hugeHeaderValue, "Huge header value is null.");
            assertEquals(HUGE_HEADER_VALUE, hugeHeaderValue, () -> "Huge header value didn't match what was expected. "
                + "Actual length: " + hugeHeaderValue.length() + " Expected length: " + HUGE_HEADER_VALUE.length());
        }
    }

    // Helpers
    private static void assertMatchWithHttpOrHttps(String uri1, String uri2) {
        final String s1 = "http://" + uri1;

        if (s1.equalsIgnoreCase(uri2)) {
            return;
        }

        final String s2 = "https://" + uri1;

        if (s2.equalsIgnoreCase(uri2)) {
            return;
        }

        fail("'" + uri2 + "' does not match with '" + s1 + "' or '" + s2 + "'.");
    }

    public static void inputStreamToOutputStream(InputStream source, OutputStream target) throws IOException {
        byte[] buf = new byte[8192];
        int length;

        while ((length = source.read(buf)) != -1) {
            target.write(buf, 0, length);
        }
    }

    public static void assertListEquals(List<?> source, List<?> target) {
        if (source != null && target != null) {
            assertEquals(source.size(), target.size());

            for (int i = 0; i < source.size(); i++) {
                assertEquals(source.get(i), target.get(i));
            }
        } else if (source != null || target != null) {
            fail("One list is null but the other is not.");
        }
    }
}
