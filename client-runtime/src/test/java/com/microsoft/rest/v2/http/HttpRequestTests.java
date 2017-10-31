package com.microsoft.rest.v2.http;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class HttpRequestTests {
    @Test
    public void constructor() {
        final HttpRequest request = new HttpRequest("request caller method", "request http method", "request url");
        assertEquals("request caller method", request.callerMethod());
        assertEquals("request http method", request.httpMethod());
        assertEquals("request url", request.url());
    }

    @Test
    public void testClone() {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("my-header", "my-value");
        headers.set("other-header", "other-value");

        final HttpRequest request = new HttpRequest(
                "request caller method",
                "request http method",
                "request url",
                headers,
                new ByteArrayHttpRequestBody(new byte[0], "application/octet-stream"));

        final HttpRequest clonedRequest = request.clone();

        assertNotSame(request, clonedRequest);

        assertEquals(request.callerMethod(), clonedRequest.callerMethod());
        assertEquals(request.httpMethod(), clonedRequest.httpMethod());
        assertEquals(request.url(), clonedRequest.url());

        assertNotSame(request.headers(), clonedRequest.headers());
        assertEquals(request.headers().toMap().size(), clonedRequest.headers().toMap().size());
        for (HttpHeader clonedHeader : clonedRequest.headers()) {
            for (HttpHeader originalHeader : request.headers()) {
                assertNotSame(clonedHeader, originalHeader);
            }

            assertEquals(clonedHeader.value(), request.headers().value(clonedHeader.name()));
        }

        assertSame(request.body(), clonedRequest.body());
    }
}
