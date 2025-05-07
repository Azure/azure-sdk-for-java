// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.test.http;

import io.clientcore.annotation.processor.test.implementation.TestInterfaceClientImpl;
import io.clientcore.annotation.processor.test.implementation.models.Foo;
import io.clientcore.annotation.processor.test.implementation.models.FooListResult;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.paging.PagedIterable;
import io.clientcore.core.http.paging.PagedResponse;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.implementation.http.ContentType;
import io.clientcore.core.models.binarydata.BinaryData;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for testing the implementation details of TestInterface.
 */
public class TestInterfaceGenerationTests {
    private static final String FIRST_PAGE_RESPONSE
        = "[{\"bar\":\"hello.world\",\"baz\":[\"hello\",\"hello.world\"],\"qux\":{\"a.b\":\"c.d\",\"bar.a\":\"ttyy\",\"bar.b\":\"uuzz\",\"hello\":\"world\"}}]";
    private static final String NEXTLINK_RESPONSE
        = "[{\"bar\":\"hello.world2\",\"additionalProperties\":{\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}}]";

    @Test
    public void contentTypeHeaderPriorityOverBodyParamAnnotationTest() throws IOException {
        String uri = "https://somecloud.com";
        HttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        byte[] bytes = "hello".getBytes();
        try (Response<Void> response
            = testInterface.testMethod(uri, ByteBuffer.wrap(bytes), "application/json", (long) bytes.length)) {
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
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        StreamResponse streamResponse = testInterface.testDownload();

        streamResponse.close();

        // This indirectly tests that StreamResponse has HttpResponse reference.
        assertTrue(client.closeCalledOnResponse);
    }*/

    @ParameterizedTest
    @MethodSource("knownLengthBinaryDataIsPassThroughArgumentProvider")
    public void knownLengthBinaryDataIsPassThrough(BinaryData data, long contentLength) {
        String uri = "https://somecloud.com";
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        Response<Void> response = testInterface.testMethod(uri, data, "application/json", contentLength);

        assertEquals(200, response.getStatusCode());
        assertSame(data, client.getLastHttpRequest().getBody());
    }

    private static Stream<Arguments> knownLengthBinaryDataIsPassThroughArgumentProvider() throws Exception {
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
        String uri = "https://somecloud.com";
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();
        Class<? extends BinaryData> expectedContentClazz = data.getClass();

        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        Response<Void> response = testInterface.testMethod(uri, data, ContentType.APPLICATION_JSON, contentLength);

        assertEquals(200, response.getStatusCode());

        Class<? extends BinaryData> actualContentClazz = client.getLastHttpRequest().getBody().getClass();

        assertEquals(expectedContentClazz, actualContentClazz);
    }

    @Test
    public void nullHeaderValueIsNotIncluded() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(request -> {
            assertNull(request.getHeaders().get(HttpHeaderName.CONTENT_TYPE));
            return new Response<>(request, 200, new HttpHeaders(), BinaryData.empty());
        }).build();

        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        try (Response<Void> response
            = testInterface.testMethod("https://somecloud.com", BinaryData.empty(), null, null)) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void nonNullHeaderValueIsIncluded() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(request -> {
            assertEquals(ContentType.APPLICATION_JSON, request.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
            return new Response<>(request, 200, new HttpHeaders(), BinaryData.empty());
        }).build();

        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        try (Response<Void> response = testInterface.testMethod("https://somecloud.com", BinaryData.empty(), ContentType.APPLICATION_JSON, null)) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void voidReturningApiClosesResponse() {
        String uri = "https://somecloud.com";
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        testInterface.testMethodReturnsVoid(uri);

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
        public Response<BinaryData> send(HttpRequest request) {
            lastHttpRequest = request;
            boolean success = request.getUri().getPath().equals("/my/uri/path");

            if (request.getHttpMethod().equals(HttpMethod.POST)) {
                success &= "application/json".equals(request.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
            } else {
                success &= request.getHttpMethod().equals(HttpMethod.GET)
                    || request.getHttpMethod().equals(HttpMethod.HEAD);
            }

            return new Response<BinaryData>(request, success ? 200 : 400, new HttpHeaders(), BinaryData.empty()) {
                @Override
                public void close() {
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

            return new Response<>(null, 200, new HttpHeaders(), BinaryData.empty());
        }).build();

        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        testInterface.testListNext(nextLinkUri).close();
    }

    @Test
    public void testListFooListResult() {
        String uri = "https://somecloud.com";
        String firstPageUri = uri + "/foos";
        String nextLinkUri = uri + "/foos?page=2";
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(request -> {
            String requestUri = request.getUri().toString();
            if (firstPageUri.equals(requestUri)) {
                return createMockResponse(request,
                    BinaryData.fromString(
                        "{\"items\":[{\"bar\":\"hello.world\",\"baz\":[\"hello\",\"hello.world\"],\"qux\":{\"a"
                            + ".b\":\"c.d\","
                            + "\"bar.a\":\"ttyy\",\"bar.b\":\"uuzz\",\"hello\":\"world\"}}], \"nextLink\":\""
                            + nextLinkUri + "\"}"),
                    nextLinkUri);
            } else if (nextLinkUri.equals(requestUri)) {
                return createMockResponse(request,
                    BinaryData.fromString(
                        "{\"items\":[{\"bar\":\"hello.world2\",\"additionalProperties\":{\"bar\":\"baz\",\"a"
                            + ".b\":\"c.d\",\"properties.bar\":\"barbar\"}}]"),
                    null);
            }
            return new Response<>(request, 404, new HttpHeaders(), BinaryData.empty());
        }).build();

        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        // Fetch the first page
        PagedIterable<Foo> pagedIterable = new PagedIterable<>(
            pagingOptions -> toPagedResponse(
                testInterface.listFooListResult(uri, RequestContext.none()), null),
            (pagingOptions, nextLink) -> toPagedResponse(
                testInterface.listNextFooListResult(nextLink, RequestContext.none()), nextLink));

        assertNotNull(pagedIterable);
        Set<Foo> allItems = pagedIterable.stream().collect(Collectors.toSet());
        assertEquals(1, allItems.size());
    }

    /**
     * Creates a mock HTTP response with JSON body and optional nextLink header.
     */
    private Response<BinaryData> createMockResponse(HttpRequest request, BinaryData jsonBody, String nextLink) {
        HttpHeaders headers = new HttpHeaders();
        if (nextLink != null) {
            headers.set(HttpHeaderName.fromString("nextLink"), nextLink);
        }

        return new Response<>(request, 200, headers, jsonBody);
    }

    /**
     * Tests that a response with List is correctly handled with a paging wrapper.
     */
    @Test
    public void testListFoo() {
        String uri = "https://somecloud.com";
        String firstPageUri = uri + "/foos";
        String nextLinkUri = uri + "/foos?page=2";
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(request -> {
            String requestUri = request.getUri().toString();
            if (firstPageUri.equals(requestUri)) {
                return createMockResponse(request, BinaryData.fromString(FIRST_PAGE_RESPONSE), nextLinkUri);
            } else if (nextLinkUri.equals(requestUri)) {
                return createMockResponse(request, BinaryData.fromString(NEXTLINK_RESPONSE), null);
            }

            return new Response<>(request, 404, new HttpHeaders(), BinaryData.empty());
        }).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        // Retrieve initial response
        Response<List<Foo>> initialResponse = testInterface.listFoo(uri, null, null, RequestContext.none());

        List<Foo> fooFirstPageResponse = initialResponse.getValue();
        assertNotNull(fooFirstPageResponse);
        assertNotNull(fooFirstPageResponse.get(0).bar());

        // Convert List<Foo> response to PagedResponse<Foo>
        PagedResponse<Foo> firstPage = toPagedResponse(initialResponse, null);

        PagedIterable<Foo> pagedIterable = new PagedIterable<>(pagingOptions -> firstPage,  // First page
            (pagingOptions, nextLink) -> {
                Response<List<Foo>> nextResponse
                    = testInterface.listNextFoo(nextLink, RequestContext.none());
                return toPagedResponse(nextResponse, nextLink);
            });

        assertNotNull(pagedIterable);
        Set<Foo> allItems = pagedIterable.stream().collect(Collectors.toSet());
        assertEquals(1, allItems.size());
    }

    /**
     * Converts a Response&lt;T&gt; to a PagedResponse&lt;Foo&gt;.
     * Supports both Response&lt;FooListResult&gt; and Response&lt;List&lt;Foo&gt;&gt;.
     */
    @SuppressWarnings({ "unchecked", "cast" })
    private <T> PagedResponse<Foo> toPagedResponse(Response<T> response, String nextLink) {
        if (response == null || response.getValue() == null) {
            return new PagedResponse<>(
                response != null
                    ? response.getRequest()
                    : new HttpRequest().setMethod(HttpMethod.GET).setUri("https://somecloud.com"),
                200, response != null ? response.getHeaders() : new HttpHeaders(), Collections.emptyList());  // Return an empty list when null
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

        return new PagedResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), items,
            nextLink, null, null, null, null);
    }
}
