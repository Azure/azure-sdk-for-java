// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.test;

import io.clientcore.annotation.processor.test.implementation.TestInterfaceClientService;
import io.clientcore.annotation.processor.test.implementation.models.Foo;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.PagedIterable;
import io.clientcore.core.http.models.PagedResponse;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.Context;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class to test paging support in code generation.
 */
public class PagingOperationTests {

    /**
     * Tests that a response with List is correctly handled with a paging wrapper.
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
                    return createMockResponse(200, "[{\"id\":1, \"name\":\"Foo1\"}, {\"id\":2, \"name\":\"Foo2\"}]", nextLinkUri);
                } else if (nextLinkUri.equals(requestUri)) {
                    return createMockResponse(200, "[{\"id\":3, \"name\":\"Foo3\"}]", null);
                }

                return new MockHttpResponse(request, 404);
            })
            .build();
        TestInterfaceClientService testInterface = TestInterfaceClientService.getNewInstance(pipeline, null);

        // Retrieve initial response
        Response<List<Foo>> initialResponse = testInterface.listFoo(uri, RequestOptions.none(), Context.none());

        // Convert List<Foo> response to PagedResponse<Foo>
        PagedResponse<Foo> firstPage = toPagedResponse(initialResponse, null);

        PagedIterable<Foo> pagedIterable = new PagedIterable<>(
            pagingOptions -> firstPage,  // First page
            (pagingOptions, nextLink) -> {
                Response<List<Foo>> nextResponse = testInterface.listNextFoo(nextLink, RequestOptions.none(), Context.none());
                return toPagedResponse(nextResponse, nextLink);
            }
        );

        assertNotNull(pagedIterable);
        Set<Foo> allItems = pagedIterable.stream().collect(Collectors.toSet());
        assertEquals(3, allItems.size());
    }

    /**
     * Converts a Response<List<Foo>> to a PagedResponse<Foo>
     */
    private PagedResponse<Foo> toPagedResponse(Response<List<Foo>> response, String nextLink) {
        if (response == null || response.getValue() == null) {
            return new PagedResponse<>(
                new HttpRequest().setMethod(HttpMethod.GET).setUri("https://example.com"),
                200,
                response.getHeaders(),
                response.getBody(),
                response.getValue()
            );
        }

        return new PagedResponse<>(
            new HttpRequest().setMethod(HttpMethod.GET).setUri("https://example.com"),
            response.getStatusCode(),
            response.getHeaders(),
            response.getBody(),
            response.getValue(),
            nextLink,  // Use the provided nextLink
            null,
            null,
            null,
            null
        );
    }

    /**
     * Creates a mock HTTP response with JSON body and optional nextLink header.
     */
    private MockHttpResponse createMockResponse(int statusCode, String jsonBody, String nextLink) {
        HttpHeaders headers = new HttpHeaders();
        if (nextLink != null) {
            headers.set(HttpHeaderName.fromString("nextLink"), nextLink);
        }

        return new MockHttpResponse(null, statusCode, headers, BinaryData.fromObject(jsonBody));
    }
}
