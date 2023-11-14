// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client;

import com.generic.core.annotation.BodyParam;
import com.generic.core.annotation.Delete;
import com.generic.core.annotation.ExpectedResponses;
import com.generic.core.annotation.Get;
import com.generic.core.annotation.Head;
import com.generic.core.annotation.HeaderParam;
import com.generic.core.annotation.Host;
import com.generic.core.annotation.HostParam;
import com.generic.core.annotation.Patch;
import com.generic.core.annotation.PathParam;
import com.generic.core.annotation.Post;
import com.generic.core.annotation.Put;
import com.generic.core.annotation.QueryParam;
import com.generic.core.annotation.ServiceInterface;
import com.generic.core.annotation.UnexpectedResponseExceptionType;
import com.generic.core.exception.HttpResponseException;
import com.generic.core.http.Response;
import com.generic.core.http.RestProxy;
import com.generic.core.http.StreamResponse;
import com.generic.core.http.models.HttpHeaderName;
import com.generic.core.http.models.HttpMethod;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.http.pipeline.HttpPipeline;
import com.generic.core.http.pipeline.HttpPipelineBuilder;
import com.generic.core.http.policy.logging.HttpLogDetailLevel;
import com.generic.core.http.policy.logging.HttpLogOptions;
import com.generic.core.implementation.http.ContentType;
import com.generic.core.implementation.http.policy.logging.HttpLoggingPolicy;
import com.generic.core.implementation.http.serializer.DefaultJsonSerializer;
import com.generic.core.implementation.util.UrlBuilder;
import com.generic.core.models.BinaryData;
import com.generic.core.models.Context;
import com.generic.core.models.Headers;
import com.generic.core.models.HttpBinJSON;
import com.generic.core.models.MyRestException;
import com.generic.core.models.RequestOptions;
import com.generic.core.models.TypeReference;
import com.generic.core.util.logging.ClientLogger;
import com.generic.core.util.serializer.ObjectSerializer;
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
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.generic.core.http.client.HttpClientTestsServer.md5;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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


    /**
     * Get the HTTP client that will be used for each test. This will be called once per test.
     *
     * @return The HTTP client to use for each test.
     */
    protected abstract HttpClient createHttpClient();

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
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        String actual = new String(sendRequest(PLAIN_RESPONSE).toBytes(), StandardCharsets.UTF_8);

        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a 'Content-Type' header encodes using the specified charset.
     */
    @Test
    public void headerResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_16BE);

        String actual = new String(sendRequest(HEADER_RESPONSE).toBytes(), StandardCharsets.UTF_16BE);

        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a 'Content-Type' containing an invalid or unsupported charset encodes using UTF-8.
     */
    @Test
    public void invalidHeaderResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        String actual = new String(sendRequest(INVALID_HEADER_RESPONSE).toBytes(), StandardCharsets.UTF_8);

        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @Test
    public void utf8BomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        String actual = new String(sendRequest(UTF_8_BOM_RESPONSE).toBytes(), StandardCharsets.UTF_8);

        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @Test
    public void utf16BeBomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_16BE);

        String actual = new String(sendRequest(UTF_16BE_BOM_RESPONSE).toBytes(), StandardCharsets.UTF_16BE);

        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @Test
    public void utf16LeBomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_16LE);

        String actual = new String(sendRequest(UTF_16LE_BOM_RESPONSE).toBytes(), StandardCharsets.UTF_16LE);

        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @Test
    public void utf32BeBomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, Charset.forName("UTF-32BE"));

        String actual = new String(sendRequest(UTF_32BE_BOM_RESPONSE).toBytes(), Charset.forName("UTF-32BE"));

        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @Test
    public void utf32LeBomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, Charset.forName("UTF-32LE"));

        String actual = new String(sendRequest(UTF_32LE_BOM_RESPONSE).toBytes(), Charset.forName("UTF-32LE"));

        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a byte order marker and 'Content-Type' header will defer to using the BOM encoding.
     */
    @Test
    public void bomWithSameHeader() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        String actual = new String(sendRequest(BOM_WITH_SAME_HEADER).toBytes(), StandardCharsets.UTF_8);

        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a byte order marker and 'Content-Type' header will defer to using the BOM encoding.
     */
    @Test
    public void bomWithDifferentHeader() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        String actual = new String(sendRequest(BOM_WITH_DIFFERENT_HEADER).toBytes(), StandardCharsets.UTF_8);

        assertEquals(expected, actual);
    }

    /**
     * Tests that unbuffered response body can be accessed.
     *
     * @throws IOException When IO fails.
     */
    @Test
    public void canAccessResponseBody() throws IOException {
        BinaryData requestBody = BinaryData.fromString("test body");
        HttpRequest request = new HttpRequest(
            HttpMethod.PUT,
            getRequestUrl(ECHO_RESPONSE),
            new com.generic.core.models.Headers(),
            requestBody);

        Supplier<HttpResponse> responseSupplier = () -> createHttpClient().send(request, Context.NONE);

        assertEquals(requestBody.toString(), responseSupplier.get().getBody().toString());

        assertArrayEquals(requestBody.toBytes(), responseSupplier.get().getBody().toBytes());

        assertArrayEquals(requestBody.toBytes(), responseSupplier.get().getBody().toBytes());

        assertArrayEquals(requestBody.toBytes(), responseSupplier.get().getBody().toBytes());

    }

    /**
     * Tests that buffered response is indeed buffered, i.e. content can be accessed many times.
     *
     * @throws IOException When IO fails.
     */
    @Test
    public void bufferedResponseCanBeReadMultipleTimes() throws IOException {
        BinaryData requestBody = BinaryData.fromString("test body");
        HttpRequest request = new HttpRequest(
            HttpMethod.PUT,
            getRequestUrl(ECHO_RESPONSE),
            new Headers(),
            requestBody);

        Context context = Context.NONE.addData("azure-eagerly-read-response", true);

        HttpResponse response = createHttpClient().send(request, context);

        // Read response twice using all accessors.
        assertEquals(requestBody.toString(), response.getBody().toString());
        assertEquals(requestBody.toString(), response.getBody().toString());

        assertArrayEquals(requestBody.toBytes(), response.getBody().toBytes());
        assertArrayEquals(requestBody.toBytes(), response.getBody().toBytes());

        assertArrayEquals(requestBody.toBytes(), response.getBody().toBytes());
        assertArrayEquals(requestBody.toBytes(), response.getBody().toBytes());

        assertArrayEquals(requestBody.toBytes(), response.getBody().toBytes());
        assertArrayEquals(requestBody.toBytes(), response.getBody().toBytes());
    }

    /**
     * Tests that eagerly converting implementation HTTP headers to azure-core Headers is done.
     */
    @Test
    public void eagerlyConvertedHeadersAreHeaders() {
        BinaryData requestBody = BinaryData.fromString("test body");
        HttpRequest request = new HttpRequest(HttpMethod.PUT, getRequestUrl(ECHO_RESPONSE), new Headers(),
            requestBody);

        Context context = Context.NONE.addData("azure-eagerly-convert-headers", true);

        try (HttpResponse response = createHttpClient().send(request, context)) {
            // Validate getHeaders type is Headers (not instanceof)
            assertEquals(Headers.class, response.getHeaders().getClass());
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
            new Headers(),
            requestBody);

        byte[] responseBytes = createHttpClient().send(request, Context.NONE).getBody().toBytes();

        assertArrayEquals(expectedResponseBody, responseBytes);
    }

    /*
     * Tests that send random bytes in various forms to an endpoint that echoes bytes back to sender.
     *
     * @param requestBody The BinaryData that contains random bytes.
     * @param expectedResponseBody The expected bytes in the echo response.
     */
    /*@ParameterizedTest
    @MethodSource("getBinaryDataBodyVariants")
    public void canSendBinaryDataWithProgressReportingSync(BinaryData requestBody, byte[] expectedResponseBody) {
        HttpRequest request = new HttpRequest(
            HttpMethod.PUT,
            getRequestUrl(ECHO_RESPONSE),
            new Headers(),
            requestBody);

        AtomicLong progress = new AtomicLong();
        Context context = Contexts.empty()
            .setHttpRequestProgressReporter(
                ProgressReporter.withProgressListener(progress::set))
            .getContext();

        HttpResponse httpResponse = createHttpClient()
            .send(request, context);

        byte[] responseBytes = httpResponse
            .getBodyAsByteArray()
            .block();

        assertArrayEquals(expectedResponseBody, responseBytes);
        assertEquals(expectedResponseBody.length, progress.intValue());
    }*/

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
                        Arguments.of(Named.named("Object", objectBinaryData), Named.named(String.valueOf(size), bytes)),
                        Arguments.of(Named.named("File", fileData), Named.named(String.valueOf(size), bytes)),
                        Arguments.of(Named.named("File slice", sliceFileData), Named.named(String.valueOf(size), bytes))
                    );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    private BinaryData sendRequest(String requestPath) {
        HttpResponse httpResponse = createHttpClient()
            .send(new HttpRequest(HttpMethod.GET, getRequestUrl(requestPath)), Context.NONE);

        return httpResponse.getBody();
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
            throw LOGGER.logThrowableAsError(new RuntimeException(e));
        }
    }

    private static class ByteArraySerializer implements ObjectSerializer {

        @Override
        public <T> T deserialize(InputStream stream, TypeReference<T> typeReference) {
            return null;
        }

        @Override
        public <T> T deserialize(Headers headers, Type type) throws IOException {
            return null;
        }

        @Override
        public void serialize(OutputStream stream, Object value) {
            try {
                stream.write((byte[]) value);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }

    @Host("{url}")
    @ServiceInterface(name = "Service1")
    private interface Service1 {
        @Get("bytes/100")
        @ExpectedResponses({200})
        byte[] getByteArray(@HostParam("url") String url);
    }

    /**
     * Tests that the response body is correctly returned as a byte array.
     */
    @Test
    public void requestWithByteArrayReturnType() {
        final byte[] result = createService(Service1.class).getByteArray(getRequestUri());

        assertNotNull(result);
        assertEquals(100, result.length);
    }


    @Host("{scheme}://{hostName}")
    @ServiceInterface(name = "Service2")
    private interface Service2 {
        @Get("bytes/{numberOfBytes}")
        @ExpectedResponses({200})
        byte[] getByteArray(@HostParam("scheme") String scheme, @HostParam("hostName") String host,
            @PathParam("numberOfBytes") int numberOfBytes);
    }

    /**
     * Tests that the response body is correctly returned as a byte array.
     */
    @Test
    public void requestWithByteArrayReturnTypeAndParameterizedHostAndPath() {
        final byte[] result =
            createService(Service2.class).getByteArray(getRequestScheme(), "localhost:" + getPort(), 100);

        assertNotNull(result);
        assertEquals(result.length, 100);
    }

    /**
     * Tests that the response body is correctly returned as a byte array.
     */
    @Test
    public void requestWithEmptyByteArrayReturnTypeAndParameterizedHostAndPath() {
        final byte[] result =
            createService(Service2.class).getByteArray(getRequestScheme(), "localhost:" + getPort(), 0);

        assertNull(result);
    }

    @Host("{url}")
    @ServiceInterface(name = "Service3")
    private interface Service3 {
        @Get("bytes/100")
        @ExpectedResponses({200})
        void getNothing(@HostParam("url") String url);
    }

    /**
     * Tests that a response with no return type is correctly handled.
     */
    @Test
    public void getRequestWithNoReturn() {
        assertDoesNotThrow(() -> createService(Service3.class).getNothing(getRequestUri()));
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
    }

    @Test
    public void getRequestWithAnything() {
        final HttpBinJSON json = createService(Service5.class).getAnything(getRequestUri());

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything", json.url());
    }

    @Test
    public void getRequestWithAnythingWithPlus() {
        final HttpBinJSON json = createService(Service5.class).getAnythingWithPlus(getRequestUri());

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with+plus", json.url());
    }

    @Test
    public void getRequestWithAnythingWithPathParam() {
        final HttpBinJSON json = createService(Service5.class).getAnythingWithPathParam(getRequestUri(),
            "withpathparam");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/withpathparam", json.url());
    }

    @Test
    public void getRequestWithAnythingWithPathParamWithSpace() {
        final HttpBinJSON json = createService(Service5.class).getAnythingWithPathParam(getRequestUri(),
            "with path param");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with path param", json.url());
    }

    @Test
    public void getRequestWithAnythingWithPathParamWithPlus() {
        final HttpBinJSON json = createService(Service5.class).getAnythingWithPathParam(getRequestUri(),
            "with+path+param");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with+path+param", json.url());
    }

    @Test
    public void getRequestWithAnythingWithEncodedPathParam() {
        final HttpBinJSON json = createService(Service5.class).getAnythingWithEncodedPathParam(getRequestUri(),
            "withpathparam");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/withpathparam", json.url());
    }

    @Test
    public void getRequestWithAnythingWithEncodedPathParamWithPercent20() {
        final HttpBinJSON json = createService(Service5.class).getAnythingWithEncodedPathParam(getRequestUri(),
            "with%20path%20param");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with path param", json.url());
    }

    @Test
    public void getRequestWithAnythingWithEncodedPathParamWithPlus() {
        final HttpBinJSON json = createService(Service5.class).getAnythingWithEncodedPathParam(getRequestUri(),
            "with+path+param");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with+path+param", json.url());
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
    }

    @Test
    public void getRequestWithQueryParametersAndAnything() {
        final HttpBinJSON json = createService(Service6.class).getAnything(getRequestUri(), "A", 15);

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything?a=A&b=15", json.url());
    }

    @Test
    public void getRequestWithQueryParametersAndAnythingWithPercent20() {
        final HttpBinJSON json = createService(Service6.class).getAnything(getRequestUri(), "A%20Z", 15);

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything?a=A%2520Z&b=15", json.url());
    }

    @Test
    public void getRequestWithQueryParametersAndAnythingWithEncodedWithPercent20() {
        final HttpBinJSON json = createService(Service6.class).getAnythingWithEncoded(getRequestUri(), "x%20y", 15);

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything?a=x y&b=15", json.url());
    }

    @Test
    public void getRequestWithNullQueryParameter() {
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

    }

    private static final HttpHeaderName HEADER_A = HttpHeaderName.fromString("A");
    private static final HttpHeaderName HEADER_B = HttpHeaderName.fromString("B");

    @Test
    public void getRequestWithHeaderParametersAndAnythingReturn() {
        final HttpBinJSON json = createService(Service7.class).getAnything(getRequestUri(), "A", 15);

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything", json.url());
        assertNotNull(json.headers());
        final Headers headers = new Headers().setAll(json.headers());

        assertEquals("A", headers.getValue(HEADER_A));
        assertArrayEquals(new String[]{"A"}, headers.getValues(HEADER_A));

        assertEquals("15", headers.getValue(HEADER_B));
        assertArrayEquals(new String[]{"15"}, headers.getValues(HEADER_B));
    }

    @Test
    public void getRequestWithNullHeader() {
        final HttpBinJSON json = createService(Service7.class).getAnything(getRequestUri(), null, 15);

        final Headers headers = new Headers().setAll(json.headers());

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

    }

    @Test
    public void postRequestWithStringBody() {
        final HttpBinJSON json = createService(Service8.class).post(getRequestUri(), "I'm a post body!");

        assertEquals(String.class, json.data().getClass());
        assertEquals("I'm a post body!", json.data());
    }

    @Test
    public void postRequestWithNullBody() {
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
        @UnexpectedResponseExceptionType(MyRestException.class)
        HttpBinJSON putBodyAndContentLength(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) ByteBuffer body,
            @HeaderParam("Content-Length") long contentLength);

        @Put("put")
        @ExpectedResponses({201})
        HttpBinJSON putWithUnexpectedResponse(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(MyRestException.class)
        HttpBinJSON putWithUnexpectedResponseAndExceptionType(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(code = {200}, value = MyRestException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        HttpBinJSON putWithUnexpectedResponseAndDeterminedExceptionType(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(code = {400}, value = HttpResponseException.class)
        @UnexpectedResponseExceptionType(MyRestException.class)
        HttpBinJSON putWithUnexpectedResponseAndFallthroughExceptionType(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(code = {400}, value = MyRestException.class)
        HttpBinJSON putWithUnexpectedResponseAndNoFallthroughExceptionType(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

    }

    @Test
    public void putRequestWithIntBody() {
        final HttpBinJSON json = createService(Service9.class).put(getRequestUri(), 42);

        assertEquals(String.class, json.data().getClass());
        assertEquals("42", json.data());
    }

    // Test all scenarios for the body length and content length comparison for sync API
    @Test
    public void putRequestWithBodyAndEqualContentLength() {
        ByteBuffer body = ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8));
        final HttpBinJSON json = createService(Service9.class).putBodyAndContentLength(getRequestUri(), body, 4L);

        assertEquals("test", json.data());
        assertEquals(ContentType.APPLICATION_OCTET_STREAM, json.getHeaderValue("Content-Type"));
        assertEquals("4", json.getHeaderValue("Content-Length"));
    }

    @Test
    public void putRequestWithBodyLessThanContentLength() {
        ByteBuffer body = ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8));
        Exception unexpectedLengthException = assertThrows(Exception.class, () -> {
            createService(Service9.class).putBodyAndContentLength(getRequestUri(), body, 5L);
            body.clear();
        });
        assertTrue(unexpectedLengthException.getMessage().contains("less than"));
    }

    @Test
    public void putRequestWithBodyMoreThanContentLength() {
        ByteBuffer body = ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8));
        Exception unexpectedLengthException = assertThrows(Exception.class, () -> {
            createService(Service9.class).putBodyAndContentLength(getRequestUri(), body, 3L);
            body.clear();
        });
        assertTrue(unexpectedLengthException.getMessage().contains("more than"));
    }

    @Test
    public void putRequestWithUnexpectedResponseAndNoFallthroughExceptionType() {
        HttpResponseException e = assertThrows(HttpResponseException.class,
            () -> createService(Service9.class).putWithUnexpectedResponseAndNoFallthroughExceptionType(getRequestUri(),
                "I'm the body!"));

        assertNotNull(e.getValue());
        assertTrue(e.getValue() instanceof LinkedHashMap);

        @SuppressWarnings("unchecked") final LinkedHashMap<String, String> expectedBody = (LinkedHashMap<String, String>) e.getValue();
        assertEquals("I'm the body!", expectedBody.get("data"));
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

    }

    @Test
    public void headRequest() {
        final Void body = createService(Service10.class).head(getRequestUri()).getValue();
        assertNull(body);
    }

    @Test
    public void headBooleanRequest() {
        final boolean result = createService(Service10.class).headBoolean(getRequestUri());
        assertTrue(result);
    }

    @Test
    public void voidHeadRequest() {
        createService(Service10.class).voidHead(getRequestUri());
    }

    @Host("{url}")
    @ServiceInterface(name = "Service11")
    private interface Service11 {
        @Delete("delete")
        @ExpectedResponses({200})
        HttpBinJSON delete(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) boolean bodyBoolean);

    }

    @Test
    public void deleteRequest() {
        final HttpBinJSON json = createService(Service11.class).delete(getRequestUri(), false);

        assertEquals(String.class, json.data().getClass());
        assertEquals("false", json.data());
    }

    @Host("{url}")
    @ServiceInterface(name = "Service12")
    private interface Service12 {
        @Patch("patch")
        @ExpectedResponses({200})
        HttpBinJSON patch(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String bodyString);

        }

    @Test
    public void patchRequest() {
        final HttpBinJSON json = createService(Service12.class).patch(getRequestUri(), "body-contents");

        assertEquals(String.class, json.data().getClass());
        assertEquals("body-contents", json.data());
    }

    @Host("{url}")
    @ServiceInterface(name = "Service13")
    private interface Service13 {
        @Get("anything")
        @ExpectedResponses({200})
        @com.generic.core.annotation.Headers({"MyHeader:MyHeaderValue", "MyOtherHeader:My,Header,Value"})
        HttpBinJSON get(@HostParam("url") String url);
    }

    private static final HttpHeaderName MY_HEADER = HttpHeaderName.fromString("MyHeader");
    private static final HttpHeaderName MY_OTHER_HEADER = HttpHeaderName.fromString("MyOtherHeader");

    @Test
    public void headersRequest() {
        final HttpBinJSON json = createService(Service13.class).get(getRequestUri());
        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything", json.url());
        assertNotNull(json.headers());
        final Headers headers = new Headers().setAll(json.headers());
        assertEquals("MyHeaderValue", headers.getValue(MY_HEADER));
        assertArrayEquals(new String[]{"MyHeaderValue"}, headers.getValues(MY_HEADER));
        assertEquals("My,Header,Value", headers.getValue(MY_OTHER_HEADER));
        assertArrayEquals(new String[]{"My", "Header", "Value"}, headers.getValues(MY_OTHER_HEADER));
    }

    @Host("{url}")
    @ServiceInterface(name = "Service14")
    private interface Service14 {
        @Get("anything")
        @ExpectedResponses({200})
        @com.generic.core.annotation.Headers({"MyHeader:MyHeaderValue"})
        HttpBinJSON get(@HostParam("url") String url);
    }

    @Host("{url}")
    @ServiceInterface(name = "Service16")
    private interface Service16 {
        @Put("put")
        @ExpectedResponses({200})
        HttpBinJSON putByteArray(@HostParam("url") String url,
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

    @Host("{scheme}://{hostPart1}{hostPart2}")
    @ServiceInterface(name = "Service17")
    private interface Service17 {
        @Get("get")
        @ExpectedResponses({200})
        HttpBinJSON get(@HostParam("scheme") String scheme, @HostParam("hostPart1") String hostPart1,
            @HostParam("hostPart2") String hostPart2);
    }

    @Test
    public void requestWithMultipleHostParams() {
        final HttpBinJSON result = createService(Service17.class).get(getRequestScheme(), "local", "host:" + getPort());

        assertNotNull(result);
        assertMatchWithHttpOrHttps("localhost/get", result.url());
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
        @com.generic.core.annotation.Headers({"Content-Type: application/json"})
        HttpBinJSON putWithHeaderApplicationJsonContentTypeAndByteArrayBody(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_JSON) byte[] body);

        @Put("put")
        @com.generic.core.annotation.Headers({"Content-Type: application/json; charset=utf-8"})
        HttpBinJSON putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Put("put")
        @com.generic.core.annotation.Headers({"Content-Type: application/octet-stream"})
        HttpBinJSON putWithHeaderApplicationOctetStreamContentTypeAndStringBody(@HostParam("url") String url,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Put("put")
        @com.generic.core.annotation.Headers({"Content-Type: application/octet-stream"})
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
        Response<Void> getVoidResponse(@HostParam("url") String url);

        @Put("put")
        Response<HttpBinJSON> putBody(@HostParam("url") String url,
                                      @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);
    }

    @Test
    public void service20PutBodyAndHeaders() {
        /*final Response<HttpBinHeaders, HttpBinJSON> response = createService(Service20.class)
            .putBodyAndHeaders(getRequestUri(), "body string");
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());

        assertEquals(Headers.class, response.getHeaders().getClass());

        final HttpBinJSON body = response.getValue();
        assertNotNull(body);
        assertMatchWithHttpOrHttps("localhost/put", body.url());
        assertEquals("body string", body.data());

        final HttpBinHeaders headers = response.getDeserializedHeaders();
        assertNotNull(headers);
        assertTrue(headers.accessControlAllowCredentials());
        assertNotNull(headers.date());
        assertNotEquals(0, (Object) headers.xProcessedTime());

         */
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

        final Headers headers = response.getHeaders();
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
    }

    @ParameterizedTest
    @MethodSource("downloadTestArgumentProvider")
    public void simpleDownloadTest(Context context) {
        StreamResponse response = createService(DownloadService.class).getBytes(getRequestUri(), context);
        ByteBuffer byteBuffer = response.getValue();

        assertEquals(30720, byteBuffer.remaining());

        StreamResponse otherResponse = createService(DownloadService.class).getBytes(getRequestUri(), context);
        ByteBuffer otherByteBuffer = otherResponse.getValue();
        byte[] bytes = new byte[otherByteBuffer.remaining()];

        otherByteBuffer.get(bytes, 0, byteBuffer.remaining());

        String contentHash = md5(bytes);
        String eTag = otherResponse.getHeaders().getValue(HttpHeaderName.ETAG);

        assertEquals(eTag, contentHash);
    }

    private static Stream<Arguments> downloadTestArgumentProvider() {
        return Stream.of(
            Arguments.of(Named.named("default", Context.NONE)));
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
            .policies(
                new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)))
            .build();
        //
        Response<HttpBinJSON> response = RestProxy.create(BinaryDataUploadService.class, httpPipeline, new DefaultJsonSerializer())
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

        final Headers resultHeaders = new Headers().setAll(result.headers());

        assertEquals("GHIJ", resultHeaders.getValue(HttpHeaderName.fromString("ABCDEF")));
        assertEquals("45", resultHeaders.getValue(HttpHeaderName.fromString("ABC123")));
    }

    /*@Host("{url}")
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
     */

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
            (url, service28) -> service28.headResponseVoid(url)
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
            (url, service29) -> service29.headResponseVoid(url)
        );
    }

    // Helpers
    protected <T> T createService(Class<T> serviceClass) {
        final HttpClient httpClient = createHttpClient();
        return createService(serviceClass, httpClient);
    }

    protected <T> T createService(Class<T> serviceClass, HttpClient httpClient) {
        final HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .build();

        return RestProxy.create(serviceClass, httpPipeline, new DefaultJsonSerializer());
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
        Assertions.fail("'" + url2 + "' does not match with '" + s1 + "' or '" + s2 + "'.");
    }
}
