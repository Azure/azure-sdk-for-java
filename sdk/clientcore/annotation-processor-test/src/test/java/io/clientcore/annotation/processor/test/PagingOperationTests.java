// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.test;

import io.clientcore.annotation.processor.test.implementation.TestInterfaceClientImpl.TestInterfaceClientService;
import io.clientcore.annotation.processor.test.implementation.models.Foo;
import io.clientcore.annotation.processor.test.implementation.models.FooListResult;
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
import io.clientcore.core.models.binarydata.BinaryData;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class for verifying the deserialization of paged responses and their handling in code generation.
 */
public class PagingOperationTests {
    private static final BinaryData FIRST_PAGE_RESPONSE = BinaryData.fromString("[{\"bar\":\"hello.world\",\"baz\":[\"hello\",\"hello.world\"],\"qux\":{\"a.b\":\"c.d\",\"bar.a\":\"ttyy\",\"bar.b\":\"uuzz\",\"hello\":\"world\"}}]");
    private static final BinaryData NEXTLINK_RESPONSE =  BinaryData.fromString("[{\"bar\":\"hello.world2\",\"additionalProperties\":{\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}}]");

    /**
     * Verifies that a response containing a List is correctly handled when returning a List<Foo>.
     */
    @Test
    public void testListFoo() {
        String uri = "https://example.com";
        String firstPageUri = uri + "/foos";
        String nextLinkUri = uri + "/foos?page=2";
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(request -> {
                String requestUri = request.getUri().toString();
                if (firstPageUri.equals(requestUri)) {
                    return createMockResponse(request, FIRST_PAGE_RESPONSE, nextLinkUri);
                } else if (nextLinkUri.equals(requestUri)) {
                    return createMockResponse(request, NEXTLINK_RESPONSE, null);
                }

                return new MockHttpResponse(request, 404);
            })
            .build();
        TestInterfaceClientService testInterface = TestInterfaceClientService.getNewInstance(pipeline);

        // Retrieve initial response
        Response<List<Foo>> initialResponse = testInterface.listFoo(uri, null, null, RequestContext.none());

        List<Foo> fooFirstPageResponse = initialResponse.getValue();
        assertNotNull(fooFirstPageResponse);
        assertNotNull(fooFirstPageResponse.get(0).bar());

        // Convert List<Foo> response to PagedResponse<Foo>
        PagedResponse<Foo> firstPage = toPagedResponse(initialResponse, null);

        PagedIterable<Foo> pagedIterable = new PagedIterable<>(
            pagingOptions -> firstPage,  // First page
            (pagingOptions, nextLink) -> {
                Response<List<Foo>> nextResponse = testInterface.listNextFoo(nextLink, RequestContext.none());
                return toPagedResponse(nextResponse, nextLink);
            }
        );

        assertNotNull(pagedIterable);
        Set<Foo> allItems = pagedIterable.stream().collect(Collectors.toSet());
        assertEquals(1, allItems.size());
    }

    @Test
    public void testListFooListResult() {
        String uri = "https://example.com";
        String firstPageUri = uri + "/foos";
        String nextLinkUri = uri + "/foos?page=2";
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(request -> {
                String requestUri = request.getUri().toString();
                if (firstPageUri.equals(requestUri)) {
                    return createMockResponse(request, BinaryData.fromString(
                            "{\"items\":[{\"bar\":\"hello.world\",\"baz\":[\"hello\",\"hello.world\"],\"qux\":{\"a" +
                                ".b\":\"c.d\"," +
                                "\"bar.a\":\"ttyy\",\"bar.b\":\"uuzz\",\"hello\":\"world\"}}], \"nextLink\":\"" + nextLinkUri + "\"}"),
                        nextLinkUri);
                } else if (nextLinkUri.equals(requestUri)) {
                    return createMockResponse(request, BinaryData.fromString(
                            "{\"items\":[{\"bar\":\"hello.world2\",\"additionalProperties\":{\"bar\":\"baz\",\"a" +
                                ".b\":\"c.d\",\"properties.bar\":\"barbar\"}}]"),
                        null);
                }
                return new MockHttpResponse(request, 404);
            })
            .build();

        TestInterfaceClientService testInterface = TestInterfaceClientService.getNewInstance(pipeline);

        // Fetch the first page
        PagedIterable<Foo> pagedIterable = new PagedIterable<>(
            pagingOptions -> toPagedResponse(testInterface.listFooListResult(uri, RequestContext.none()), nextLinkUri),
            (pagingOptions, nextLink) -> toPagedResponse(testInterface.listNextFooListResult(nextLink, RequestContext.none()), null)
        );

        assertNotNull(pagedIterable);
        Set<Foo> allItems = pagedIterable.stream().collect(Collectors.toSet());
        assertEquals(1, allItems.size());
    }

    /**
     * Creates a mock HTTP response with JSON body and optional nextLink header.
     */
    private MockHttpResponse createMockResponse(HttpRequest request, BinaryData jsonBody, String nextLink) {
        HttpHeaders headers = new HttpHeaders();
        if (nextLink != null) {
            headers.set(HttpHeaderName.fromString("nextLink"), nextLink);
        }

        return new MockHttpResponse(request, 200, headers, jsonBody);
    }

    /**
     * Converts a Response<T> to a PagedResponse<Foo>.
     * Supports both Response<FooListResult> and Response<List<Foo>>.
     */
    @SuppressWarnings({ "unchecked", "cast" })
    private <T> PagedResponse<Foo> toPagedResponse(Response<T> response, String nextLink) {
        if (response == null || response.getValue() == null) {
            return new PagedResponse<>(
                response != null ? response.getRequest() : new HttpRequest().setMethod(HttpMethod.GET).setUri("https://example.com"),
                200,
                response != null ? response.getHeaders() : new HttpHeaders(),
                Collections.emptyList()  // Return an empty list when null
            );
        }

        List<Foo> items;
        if (response.getValue() instanceof FooListResult) {
            items = ((FooListResult) response.getValue()).getItems();  // Extract list from FooListResult
            nextLink = ((FooListResult) response.getValue()).getNextLink();
        } else if (response.getValue() instanceof List) {
            items = (List<Foo>) response.getValue();  // Directly use the List<Foo>
        } else {
            throw new IllegalArgumentException("Unsupported response type: " + response.getValue().getClass().getName());
        }

        return new PagedResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), items,
            nextLink, null, null, null, null);
    }
}
