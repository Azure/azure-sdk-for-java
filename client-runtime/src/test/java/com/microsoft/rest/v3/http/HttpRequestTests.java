package com.microsoft.rest.v3.http;

import org.junit.Test;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;

public class HttpRequestTests {
    @Test
    public void constructor() throws MalformedURLException {
        final HttpRequest request = new HttpRequest("request caller method", HttpMethod.fromString("request http method"), new URL("http://request.url"), null);
        assertEquals("request caller method", request.callerMethod());
        assertEquals(HttpMethod.fromString("request http method"), request.httpMethod());
        assertEquals(new URL("http://request.url"), request.url());
    }

    @Test
    public void testClone() throws IOException {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("my-header", "my-value");
        headers.set("other-header", "other-value");

        final HttpRequest request = new HttpRequest(
                "request caller method",
                HttpMethod.fromString("request http method"),
                new URL("http://request.url"),
                headers,
                Flux.just(ByteBuffer.allocate(0)), null);

        final HttpRequest bufferedRequest = request.buffer();

        assertNotSame(request, bufferedRequest);

        assertEquals(request.callerMethod(), bufferedRequest.callerMethod());
        assertEquals(request.httpMethod(), bufferedRequest.httpMethod());
        assertEquals(request.url(), bufferedRequest.url());

        assertNotSame(request.headers(), bufferedRequest.headers());
        assertEquals(request.headers().toMap().size(), bufferedRequest.headers().toMap().size());
        for (HttpHeader clonedHeader : bufferedRequest.headers()) {
            for (HttpHeader originalHeader : request.headers()) {
                assertNotSame(clonedHeader, originalHeader);
            }

            assertEquals(clonedHeader.value(), request.headers().value(clonedHeader.name()));
        }

        assertSame(request.body(), bufferedRequest.body());
    }
}
