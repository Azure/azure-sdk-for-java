// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http;

import com.generic.core.annotation.ServiceInterface;
import com.generic.core.http.annotation.BodyParam;
import com.generic.core.http.annotation.HeaderParam;
import com.generic.core.http.annotation.HttpRequestInformation;
import com.generic.core.http.annotation.PathParam;
import com.generic.core.http.client.HttpClient;
import com.generic.core.http.models.ContentType;
import com.generic.core.http.models.HttpHeaderName;
import com.generic.core.http.models.HttpMethod;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.Response;
import com.generic.core.http.pipeline.HttpPipeline;
import com.generic.core.http.pipeline.HttpPipelineBuilder;
import com.generic.core.implementation.http.rest.RestProxyUtils;
import com.generic.core.implementation.http.serializer.DefaultJsonSerializer;
import com.generic.core.util.binarydata.BinaryData;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static com.generic.core.http.models.ResponseBodyMode.BUFFER;
import static com.generic.core.http.models.ResponseBodyMode.IGNORE;
import static com.generic.core.http.models.ResponseBodyMode.STREAM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link RestProxy}.
 */
public class RestProxyTests {
    @ServiceInterface(name = "myService", host = "https://azure.com")
    interface TestInterface {
        @HttpRequestInformation(method = HttpMethod.POST, path = "my/url/path", expectedStatusCodes = {200})
        Response<Void> testMethod(
            @BodyParam("application/octet-stream") ByteBuffer request,
            @HeaderParam("Content-Type") String contentType,
            @HeaderParam("Content-Length") Long contentLength
        );

        @HttpRequestInformation(method = HttpMethod.POST, path = "my/url/path", expectedStatusCodes = {200})
        Response<Void> testMethod(
            @BodyParam("application/octet-stream") BinaryData data,
            @HeaderParam("Content-Type") String contentType,
            @HeaderParam("Content-Length") Long contentLength
        );

        @HttpRequestInformation(method = HttpMethod.GET, path = "{nextLink}", expectedStatusCodes = {200})
        Response<Void> testListNext(@PathParam(value = "nextLink", encoded = true) String nextLink);

        @HttpRequestInformation(method = HttpMethod.GET, path = "my/url/path", expectedStatusCodes = {200})
        Void testMethodReturnsVoid();

        @HttpRequestInformation(method = HttpMethod.HEAD, path = "my/url/path", expectedStatusCodes = {200})
        void testHeadMethod();

        @HttpRequestInformation(method = HttpMethod.GET, path = "my/url/path", expectedStatusCodes = {200})
        Response<Void> testMethodReturnsResponseVoid();

        @HttpRequestInformation(method = HttpMethod.GET, path = "my/url/path", expectedStatusCodes = {200})
        Response<InputStream> testDownload();
    }

    @Test
    public void contentTypeHeaderPriorityOverBodyParamAnnotationTest() {
        HttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new DefaultJsonSerializer());
        byte[] bytes = "hello".getBytes();
        Response<Void> response =
            testInterface.testMethod(ByteBuffer.wrap(bytes), "application/json", (long) bytes.length);

        assertEquals(200, response.getStatusCode());
    }

    // TODO (vcolin7): Re-enable this test if we ever compose HttpResponse into a stream Response type.
    /*@Test
    public void streamResponseShouldHaveHttpResponseReference() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new DefaultJsonSerializer());
        StreamResponse streamResponse = testInterface.testDownload();

        streamResponse.close();

        // This indirectly tests that StreamResponse has HttpResponse reference.
        assertTrue(client.closeCalledOnResponse);
    }*/

    @ParameterizedTest
    @MethodSource("knownLengthBinaryDataIsPassthroughArgumentProvider")
    public void knownLengthBinaryDataIsPassthrough(BinaryData data, long contentLength) {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new DefaultJsonSerializer());
        Response<Void> response = testInterface.testMethod(data, "application/json", contentLength);

        assertEquals(200, response.getStatusCode());
        assertSame(data, client.getLastHttpRequest().getBody());
    }

    private static Stream<Arguments> knownLengthBinaryDataIsPassthroughArgumentProvider() throws Exception {
        String string = "hello";
        byte[] bytes = string.getBytes();
        Path file = Files.createTempFile("knownLengthBinaryDataIsPassthroughArgumentProvider", null);

        file.toFile().deleteOnExit();

        Files.write(file, bytes);

        return Stream.of(
            Arguments.of(Named.of("bytes", BinaryData.fromBytes(bytes)), bytes.length),
            Arguments.of(Named.of("string", BinaryData.fromString(string)), bytes.length),
            Arguments.of(Named.of("file", BinaryData.fromFile(file)), bytes.length),
            Arguments.of(Named.of("serializable", BinaryData.fromObject(bytes)),
                BinaryData.fromObject(bytes).getLength())
        );
    }

    @ParameterizedTest
    @MethodSource("doesNotChangeBinaryDataContentTypeDataProvider")
    public void doesNotChangeBinaryDataContentType(BinaryData data, long contentLength) {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();
        Class<? extends BinaryData> expectedContentClazz = data.getClass();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new DefaultJsonSerializer());
        Response<Void> response = testInterface.testMethod(data, ContentType.APPLICATION_JSON, contentLength);

        assertEquals(200, response.getStatusCode());

        Class<? extends BinaryData> actualContentClazz = client.getLastHttpRequest().getBody().getClass();

        assertEquals(expectedContentClazz, actualContentClazz);
    }

    @Test
    public void voidReturningApiClosesResponse() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new DefaultJsonSerializer());

        testInterface.testMethodReturnsVoid();

        assertTrue(client.closeCalledOnResponse);
    }

    @Test
    public void headApiIgnoresResponseBody() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new DefaultJsonSerializer());

        testInterface.testHeadMethod();

        assertEquals(IGNORE, client.getLastHttpRequest().getMetadata().getResponseBodyMode());
    }

    @Test
    public void responseVoidReturningApiBuffersResponseBody() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new DefaultJsonSerializer());

        testInterface.testMethodReturnsResponseVoid();

        assertEquals(BUFFER, client.getLastHttpRequest().getMetadata().getResponseBodyMode());
    }

    @Test
    public void streamingResponseDoesNotEagerlyDeserializeResponse() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new DefaultJsonSerializer());

        testInterface.testDownload();

        assertEquals(STREAM, client.getLastHttpRequest().getMetadata().getResponseBodyMode());
    }

    private static Stream<Arguments> doesNotChangeBinaryDataContentTypeDataProvider() throws Exception {
        String string = "hello";
        byte[] bytes = string.getBytes();
        Path file = Files.createTempFile("doesNotChangeBinaryDataContentTypeDataProvider", null);

        file.toFile().deleteOnExit();

        Files.write(file, bytes);
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);

        return Stream.of(
            Arguments.of(Named.of("bytes", BinaryData.fromBytes(bytes)), bytes.length),
            Arguments.of(Named.of("string", BinaryData.fromString(string)), bytes.length),
            Arguments.of(Named.of("file", BinaryData.fromFile(file)), bytes.length),
            Arguments.of(Named.of("stream", BinaryData.fromStream(stream, (long) bytes.length)), bytes.length),
            Arguments.of(Named.of("serializable", BinaryData.fromObject(bytes)),
                BinaryData.fromObject(bytes).getLength())
        );
    }

    private static final class LocalHttpClient implements HttpClient {
        private volatile HttpRequest lastHttpRequest;
        private volatile boolean closeCalledOnResponse;

        @Override
        public Response<?> send(HttpRequest request) {
            lastHttpRequest = request;
            boolean success = request.getUrl().getPath().equals("/my/url/path");

            if (request.getHttpMethod().equals(HttpMethod.POST)) {
                success &= "application/json".equals(request.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
            } else {
                success &= request.getHttpMethod().equals(HttpMethod.GET)
                    || request.getHttpMethod().equals(HttpMethod.HEAD);
            }

            return new MockHttpResponse(request, success ? 200 : 400) {
                @Override
                public void close() throws IOException {
                    closeCalledOnResponse = true;

                    super.close();
                }
            };
        }

        public HttpRequest getLastHttpRequest() {
            return lastHttpRequest;
        }
    }

    @Test
    public void doesNotChangeEncodedPath() {
        String nextLinkUrl =
            "https://management.azure.com:443/subscriptions/000/resourceGroups/rg/providers/Microsoft.Compute/virtualMachineScaleSets/vmss1/virtualMachines?api-version=2021-11-01&$skiptoken=Mzk4YzFjMzMtM2IwMC00OWViLWI2NGYtNjg4ZTRmZGQ1Nzc2IS9TdWJzY3JpcHRpb25zL2VjMGFhNWY3LTllNzgtNDBjOS04NWNkLTUzNWM2MzA1YjM4MC9SZXNvdXJjZUdyb3Vwcy9SRy1XRUlEWFUtVk1TUy9WTVNjYWxlU2V0cy9WTVNTMS9WTXMvNzc=";
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient((request) -> {
                assertEquals(nextLinkUrl, request.getUrl().toString());

                return new MockHttpResponse(null, 200);
            })
            .build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new DefaultJsonSerializer());

        testInterface.testListNext(nextLinkUrl);
    }
}
