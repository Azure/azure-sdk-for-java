// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http;

import io.clientcore.core.annotations.ServiceInterface;
import io.clientcore.core.http.annotations.BodyParam;
import io.clientcore.core.http.annotations.HeaderParam;
import io.clientcore.core.http.annotations.HostParam;
import io.clientcore.core.http.annotations.HttpRequestInformation;
import io.clientcore.core.http.annotations.PathParam;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.PagedIterable;
import io.clientcore.core.http.models.PagedResponse;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.models.ResponseBodyMode;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.implementation.http.ContentType;
import io.clientcore.core.implementation.serializer.Foo;
import io.clientcore.core.implementation.utils.JsonSerializer;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.Context;
import org.junit.jupiter.api.Disabled;
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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link RestProxy}.
 */
public class RestProxyTests {
    private static String FIRST_PAGE_RESPONSE
        = "[{\"bar\":\"hello.world\",\"baz\":[\"hello\",\"hello.world\"],\"qux\":{\"a.b\":\"c.d\",\"bar.a\":\"ttyy\",\"bar.b\":\"uuzz\",\"hello\":\"world\"}}]";
    private static String NEXTLINK_RESPONSE
        = "[{\"bar\":\"hello.world2\",\"additionalProperties\":{\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}}]";

    @ServiceInterface(name = "myService", host = "https://somecloud.com")
    interface TestInterface {
        @HttpRequestInformation(method = HttpMethod.POST, path = "my/uri/path", expectedStatusCodes = { 200 })
        Response<Void> testMethod(@BodyParam("application/octet-stream") ByteBuffer request,
            @HeaderParam("Content-Type") String contentType, @HeaderParam("Content-Length") Long contentLength);

        @HttpRequestInformation(method = HttpMethod.POST, path = "my/uri/path", expectedStatusCodes = { 200 })
        Response<Void> testMethod(@BodyParam("application/octet-stream") BinaryData data,
            @HeaderParam("Content-Type") String contentType, @HeaderParam("Content-Length") Long contentLength);

        @HttpRequestInformation(method = HttpMethod.GET, path = "{nextLink}", expectedStatusCodes = { 200 })
        Response<Void> testListNext(@PathParam(value = "nextLink", encoded = true) String nextLink);

        @HttpRequestInformation(method = HttpMethod.GET, path = "my/uri/path", expectedStatusCodes = { 200 })
        Void testMethodReturnsVoid();

        @HttpRequestInformation(method = HttpMethod.HEAD, path = "my/uri/path", expectedStatusCodes = { 200 })
        void testHeadMethod();

        @HttpRequestInformation(method = HttpMethod.GET, path = "my/uri/path", expectedStatusCodes = { 200 })
        Response<Void> testMethodReturnsResponseVoid();

        @HttpRequestInformation(method = HttpMethod.GET, path = "my/uri/path", expectedStatusCodes = { 200 })
        Response<InputStream> testDownload();

        @HttpRequestInformation(method = HttpMethod.GET, path = "foos", expectedStatusCodes = { 200 })
        Response<FooListResult> listFooListResult(@HostParam("uri") String uri, RequestOptions requestOptions,
            Context context);

        @HttpRequestInformation(method = HttpMethod.GET, path = "{nextLink}", expectedStatusCodes = { 200 })
        Response<FooListResult> listNextFooListResult(@PathParam(value = "nextLink", encoded = true) String nextLink,
            RequestOptions requestOptions, Context context);

        @HttpRequestInformation(method = HttpMethod.GET, path = "foos", expectedStatusCodes = { 200 })
        Response<List<Foo>> listFoo(@HostParam("uri") String uri, RequestOptions requestOptions, Context context);

        @HttpRequestInformation(method = HttpMethod.GET, path = "{nextLink}", expectedStatusCodes = { 200 })
        Response<List<Foo>> listNextFoo(@PathParam(value = "nextLink", encoded = true) String nextLink,
            RequestOptions requestOptions, Context context);
    }

    @Test
    public void contentTypeHeaderPriorityOverBodyParamAnnotationTest() throws IOException {
        HttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new JsonSerializer());
        byte[] bytes = "hello".getBytes();
        try (Response<Void> response
            = testInterface.testMethod(ByteBuffer.wrap(bytes), "application/json", (long) bytes.length)) {
            assertEquals(200, response.getStatusCode());
        }
    }

    // TODO (vcolin7): Re-enable this test if we ever compose HttpResponse into a stream Response type.
    /*@Test
    public void streamResponseShouldHaveHttpResponseReference() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();
        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new JsonSerializer());
        StreamResponse streamResponse = testInterface.testDownload();
    
        streamResponse.close();
    
        // This indirectly tests that StreamResponse has HttpResponse reference.
        assertTrue(client.closeCalledOnResponse);
    }*/

    @ParameterizedTest
    @MethodSource("knownLengthBinaryDataIsPassthroughArgumentProvider")
    public void knownLengthBinaryDataIsPassthrough(BinaryData data, long contentLength) {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new JsonSerializer());
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

        return Stream.of(Arguments.of(Named.of("bytes", BinaryData.fromBytes(bytes)), bytes.length),
            Arguments.of(Named.of("string", BinaryData.fromString(string)), bytes.length),
            Arguments.of(Named.of("file", BinaryData.fromFile(file)), bytes.length), Arguments
                .of(Named.of("serializable", BinaryData.fromObject(bytes)), BinaryData.fromObject(bytes).getLength()));
    }

    @ParameterizedTest
    @MethodSource("doesNotChangeBinaryDataContentTypeDataProvider")
    public void doesNotChangeBinaryDataContentType(BinaryData data, long contentLength) {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();
        Class<? extends BinaryData> expectedContentClazz = data.getClass();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new JsonSerializer());
        Response<Void> response = testInterface.testMethod(data, ContentType.APPLICATION_JSON, contentLength);

        assertEquals(200, response.getStatusCode());

        Class<? extends BinaryData> actualContentClazz = client.getLastHttpRequest().getBody().getClass();

        assertEquals(expectedContentClazz, actualContentClazz);
    }

    @Test
    public void voidReturningApiClosesResponse() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new JsonSerializer());

        testInterface.testMethodReturnsVoid();

        assertTrue(client.closeCalledOnResponse);
    }

    private static Stream<Arguments> doesNotChangeBinaryDataContentTypeDataProvider() throws Exception {
        String string = "hello";
        byte[] bytes = string.getBytes();
        Path file = Files.createTempFile("doesNotChangeBinaryDataContentTypeDataProvider", null);

        file.toFile().deleteOnExit();

        Files.write(file, bytes);
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);

        return Stream.of(Arguments.of(Named.of("bytes", BinaryData.fromBytes(bytes)), bytes.length),
            Arguments.of(Named.of("string", BinaryData.fromString(string)), bytes.length),
            Arguments.of(Named.of("file", BinaryData.fromFile(file)), bytes.length),
            Arguments.of(Named.of("stream", BinaryData.fromStream(stream, (long) bytes.length)), bytes.length),
            Arguments.of(Named.of("serializable", BinaryData.fromObject(bytes)),
                BinaryData.fromObject(bytes).getLength()));
    }

    private static final class LocalHttpClient implements HttpClient {
        private volatile HttpRequest lastHttpRequest;
        private volatile boolean closeCalledOnResponse;

        @Override
        public Response<?> send(HttpRequest request) {
            lastHttpRequest = request;
            boolean success = request.getUri().getPath().equals("/my/uri/path");

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
    public void doesNotChangeEncodedPath() throws IOException {
        String nextLinkUri
            = "https://management.somecloud.com:443/subscriptions/000/resourceGroups/rg/providers/Microsoft.Compute/virtualMachineScaleSets/vmss1/virtualMachines?api-version=2021-11-01&$skiptoken=Mzk4YzFjMzMtM2IwMC00OWViLWI2NGYtNjg4ZTRmZGQ1Nzc2IS9TdWJzY3JpcHRpb25zL2VjMGFhNWY3LTllNzgtNDBjOS04NWNkLTUzNWM2MzA1YjM4MC9SZXNvdXJjZUdyb3Vwcy9SRy1XRUlEWFUtVk1TUy9WTVNjYWxlU2V0cy9WTVNTMS9WTXMvNzc=";
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient((request) -> {
            assertEquals(nextLinkUri, request.getUri().toString());

            return new MockHttpResponse(null, 200);
        }).build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new JsonSerializer());

        testInterface.testListNext(nextLinkUri).close();
    }

    @Test
    @Disabled("TODO: Confirm the data if using wrapper FooListResult since the deserializer fromJson expects it to be an object and not an array")
    public void testListFooListResult() {
        String uri = "https://somecloud.com";
        String firstPageUri = uri + "/foos";
        String nextLinkUri = uri + "/foos?page=2";
        RequestOptions requestOptions = new RequestOptions().setResponseBodyMode(ResponseBodyMode.DESERIALIZE);
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(request -> {
            String requestUri = request.getUri().toString();
            request.setRequestOptions(requestOptions);
            if (firstPageUri.equals(requestUri)) {
                return createMockResponse(request, 200, BinaryData.fromString(
                    "{\"bar\":\"hello.world\",\"baz\":[\"hello\",\"hello.world\"],\"qux\":{\"a.b\":\"c.d\",\"bar.a\":\"ttyy\",\"bar.b\":\"uuzz\",\"hello\":\"world\"}}"),
                    nextLinkUri);
            } else if (nextLinkUri.equals(requestUri)) {
                return createMockResponse(request, 200, BinaryData.fromString(
                    "{\"bar\":\"hello.world2\",\"additionalProperties\":{\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}}"),
                    null);
            }
            return new MockHttpResponse(request, 404);
        }).build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new JsonSerializer());

        // Fetch the first page
        PagedIterable<Foo> pagedIterable = new PagedIterable<>(
            pagingOptions -> toPagedResponse(
                testInterface.listFooListResult(uri, RequestOptions.none(), Context.none()), null),
            (pagingOptions, nextLink) -> toPagedResponse(
                testInterface.listNextFooListResult(nextLink, RequestOptions.none(), Context.none()), nextLink));

        assertNotNull(pagedIterable);
        Set<Foo> allItems = pagedIterable.stream().collect(Collectors.toSet());
        assertEquals(1, allItems.size());
    }

    /**
     * Creates a mock HTTP response with JSON body and optional nextLink header.
     */
    private MockHttpResponse createMockResponse(HttpRequest request, int statusCode, BinaryData jsonBody,
        String nextLink) {
        HttpHeaders headers = new HttpHeaders();
        if (nextLink != null) {
            headers.set(HttpHeaderName.fromString("nextLink"), nextLink);
        }

        return new MockHttpResponse(request, statusCode, headers, jsonBody);
    }

    /**
     * Tests that a response with List is correctly handled with a paging wrapper.
     */
    @Test
    public void testListFoo() {
        String uri = "https://somecloud.com";
        String firstPageUri = uri + "/foos";
        String nextLinkUri = uri + "/foos?page=2";
        RequestOptions requestOptions = new RequestOptions().setResponseBodyMode(ResponseBodyMode.DESERIALIZE);
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(request -> {
            String requestUri = request.getUri().toString();
            request.setRequestOptions(requestOptions);
            if (firstPageUri.equals(requestUri)) {
                return createMockResponse(request, 200, BinaryData.fromString(FIRST_PAGE_RESPONSE), nextLinkUri);
            } else if (nextLinkUri.equals(requestUri)) {
                return createMockResponse(request, 200, BinaryData.fromString(NEXTLINK_RESPONSE), null);
            }

            return new MockHttpResponse(request, 404);
        }).build();
        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new JsonSerializer());

        // Retrieve initial response
        Response<List<Foo>> initialResponse = testInterface.listFoo(uri, RequestOptions.none(), Context.none());

        List<Foo> fooFirstPageResponse = initialResponse.getValue();
        assertNotNull(fooFirstPageResponse);
        assertNotNull(fooFirstPageResponse.get(0).bar());

        // Convert List<Foo> response to PagedResponse<Foo>
        PagedResponse<Foo> firstPage = toPagedResponse(initialResponse, null);

        PagedIterable<Foo> pagedIterable = new PagedIterable<>(pagingOptions -> firstPage,  // First page
            (pagingOptions, nextLink) -> {
                Response<List<Foo>> nextResponse
                    = testInterface.listNextFoo(nextLink, RequestOptions.none(), Context.none());
                return toPagedResponse(nextResponse, nextLink);
            });

        assertNotNull(pagedIterable);
        Set<Foo> allItems = pagedIterable.stream().collect(Collectors.toSet());
        assertEquals(1, allItems.size());
    }

    /**
     * Converts a Response<T> to a PagedResponse<Foo>.
     * Supports both Response<FooListResult> and Response<List<Foo>>.
     */
    @SuppressWarnings({ "unchecked", "cast" })
    private <T> PagedResponse<Foo> toPagedResponse(Response<T> response, String nextLink) {
        if (response == null || response.getValue() == null) {
            return new PagedResponse<>(
                response != null
                    ? response.getRequest()
                    : new HttpRequest().setMethod(HttpMethod.GET).setUri("https://somecloud.com"),
                200, response != null ? response.getHeaders() : new HttpHeaders(),
                response != null ? response.getBody() : null, Collections.emptyList()  // Return an empty list when null
            );
        }

        List<Foo> items;
        if (response.getValue() instanceof FooListResult) {
            items = ((FooListResult) response.getValue()).getItems();  // Extract list from FooListResult
            nextLink = ((FooListResult) response.getValue()).getNextLink();
        } else if (response.getValue() instanceof List) {
            items = (List<Foo>) response.getValue();  // Directly use the List<Foo>
        } else {
            throw new IllegalArgumentException(
                "Unsupported response type: " + response.getValue().getClass().getName());
        }

        return new PagedResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            response.getBody(), items, nextLink, null, null, null, null);
    }
}
