// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.rest;

import io.clientcore.core.annotation.ServiceInterface;
import io.clientcore.core.http.MockHttpResponse;
import io.clientcore.core.http.RestProxy;
import io.clientcore.core.http.annotation.BodyParam;
import io.clientcore.core.http.annotation.HeaderParam;
import io.clientcore.core.http.annotation.HttpRequestInformation;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.implementation.util.JsonSerializer;
import io.clientcore.core.util.Context;
import io.clientcore.core.util.binarydata.BinaryData;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static io.clientcore.core.util.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link RestProxy}.
 */
public class RestProxyImplTests {
    private static final String SAMPLE = "sample";
    private static final byte[] EXPECTED = SAMPLE.getBytes(StandardCharsets.UTF_8);

    @ServiceInterface(name = "myService", host = "https://somecloud.com")
    interface TestInterface {
        @HttpRequestInformation(method = HttpMethod.POST, path = "my/uri/path", expectedStatusCodes = { 200 })
        Response<Void> testMethod(@BodyParam("application/octet-stream") BinaryData data,
            @HeaderParam("Content-Type") String contentType, @HeaderParam("Content-Length") Long contentLength,
            Context context);

        @HttpRequestInformation(method = HttpMethod.GET, path = "my/uri/path", expectedStatusCodes = { 200 })
        void testVoidMethod(Context context);
    }

    @Test
    public void voidReturningApiClosesResponse() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();
        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new JsonSerializer());

        testInterface.testVoidMethod(Context.none());

        assertTrue(client.lastResponseClosed);
    }

    @Test
    public void contentTypeHeaderPriorityOverBodyParamAnnotationTest() {
        HttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();
        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new JsonSerializer());
        byte[] bytes = "hello".getBytes();
        Response<Void> response
            = testInterface.testMethod(BinaryData.fromStream(new ByteArrayInputStream(bytes), (long) bytes.length),
                "application/json", (long) bytes.length, Context.none());

        assertEquals(200, response.getStatusCode());
    }

    private static final class LocalHttpClient implements HttpClient {
        private volatile boolean lastResponseClosed;

        @Override
        public Response<?> send(HttpRequest request) {
            boolean success = request.getUri().getPath().equals("/my/uri/path");

            if (request.getHttpMethod().equals(HttpMethod.POST)) {
                success &= "application/json".equals(request.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
            } else {
                success &= request.getHttpMethod().equals(HttpMethod.GET);
            }

            return new MockHttpResponse(request, success ? 200 : 400) {
                @Override
                public void close() throws IOException {
                    lastResponseClosed = true;

                    super.close();
                }
            };
        }
    }

    @ParameterizedTest
    @MethodSource("expectedBodyLengthDataProvider")
    public void expectedBodyLength(HttpRequest httpRequest) {
        BinaryData binaryData = RestProxyImpl.validateLength(httpRequest);

        assertNotNull(binaryData);
        assertArraysEqual(EXPECTED, binaryData.toBytes());
    }

    public static Stream<Arguments> expectedBodyLengthDataProvider() throws Exception {
        return dataProvider(EXPECTED.length);
    }

    @Test
    public void emptyRequestBody() {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost");

        assertNull(RestProxyImpl.validateLength(httpRequest));
    }

    @ParameterizedTest
    @MethodSource("unexpectedBodyLengthTooSmallDataProvider")
    public void unexpectedBodyLengthTooSmall(HttpRequest httpRequest) {
        assertThrows(IllegalStateException.class, () -> validateAndCollectRequest(httpRequest), "Request body "
            + "emitted " + EXPECTED.length + " bytes, less than the expected " + (EXPECTED.length + 1) + " bytes.");
    }

    public static Stream<Arguments> unexpectedBodyLengthTooSmallDataProvider() throws Exception {
        return dataProvider(EXPECTED.length + 1);
    }

    @ParameterizedTest
    @MethodSource("unexpectedBodyLengthTooLargeDataProvider")
    public void unexpectedBodyLengthTooLarge(HttpRequest httpRequest) {
        assertThrows(IllegalStateException.class, () -> validateAndCollectRequest(httpRequest), "Request body "
            + "emitted " + EXPECTED.length + " bytes, more than the expected " + (EXPECTED.length - 1) + " bytes.");
    }

    public static Stream<Arguments> unexpectedBodyLengthTooLargeDataProvider() throws Exception {
        return dataProvider(EXPECTED.length - 1);
    }

    @Test
    public void multipleToBytesToCheckBodyLength() {
        HttpRequest httpRequest
            = new HttpRequest(HttpMethod.GET, "http://localhost").setBody(BinaryData.fromBytes(EXPECTED));
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(EXPECTED.length));

        BinaryData binaryData = RestProxyImpl.validateLength(httpRequest);

        assertNotNull(binaryData);
        assertArraysEqual(EXPECTED, binaryData.toBytes());
        assertArraysEqual(EXPECTED, binaryData.toBytes());
    }

    private static Stream<Arguments> dataProvider(int contentLength) throws Exception {
        Path file = Files.createTempFile(RestProxyImpl.class.getSimpleName(), null);
        file.toFile().deleteOnExit();
        Files.write(file, EXPECTED);

        return Stream.of(
            Arguments.of(Named.of("bytes",
                createHttpRequest("http://localhost", BinaryData.fromBytes(EXPECTED), contentLength))),
            Arguments.of(Named.of("string",
                createHttpRequest("http://localhost", BinaryData.fromString(SAMPLE), contentLength))),
            Arguments.of(
                Named.of("stream", createHttpRequest("http://localhost", BinaryData.fromFile(file), contentLength))),
            Arguments
                .of(Named.of("file", createHttpRequest("http://localhost", BinaryData.fromFile(file), contentLength))));
    }

    private static HttpRequest createHttpRequest(String uri, BinaryData body, int contentLength) {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, uri).setBody(body);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength));
        return httpRequest;
    }

    @Test
    public void userProvidedLengthShouldNotBeTrustedTooLarge() throws IOException {
        try (InputStream byteArrayInputStream = new ByteArrayInputStream(EXPECTED)) {
            HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost")
                .setBody(BinaryData.fromStream(byteArrayInputStream, EXPECTED.length - 1L));
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(EXPECTED.length - 1L));

            IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> validateAndCollectRequest(httpRequest), "Expected validateLength() to throw, but it didn't");
            assertEquals("Request body emitted " + EXPECTED.length + " bytes, more than the expected "
                + (EXPECTED.length - 1) + " bytes.", thrown.getMessage());
        }
    }

    @Test
    public void userProvidedLengthShouldNotBeTrustedTooSmall() throws IOException {
        try (InputStream byteArrayInputStream = new ByteArrayInputStream(EXPECTED)) {
            HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost")
                .setBody(BinaryData.fromStream(byteArrayInputStream, EXPECTED.length + 1L));
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(EXPECTED.length + 1L));

            IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> validateAndCollectRequest(httpRequest), "Expected validateLength() to throw, but it didn't");

            assertEquals("Request body emitted " + EXPECTED.length + " bytes, less than the expected "
                + (EXPECTED.length + 1) + " bytes.", thrown.getMessage());
        }
    }

    @Test
    public void expectedBodyLength() throws IOException {
        try (InputStream byteArrayInputStream = new ByteArrayInputStream(EXPECTED)) {
            HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost")
                .setBody(BinaryData.fromStream(byteArrayInputStream, (long) EXPECTED.length));
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(EXPECTED.length));

            assertArraysEqual(EXPECTED, validateAndCollectRequest(httpRequest));
        }
    }

    private static byte[] validateAndCollectRequest(HttpRequest request) {
        return RestProxyImpl.validateLength(request).toBytes();
    }
}
