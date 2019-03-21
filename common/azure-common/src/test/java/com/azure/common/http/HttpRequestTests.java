package com.azure.common.http;

import io.netty.buffer.Unpooled;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.*;

public class HttpRequestTests {
    @Test
    public void constructor() throws MalformedURLException {
        final HttpRequest request = new HttpRequest(HttpMethod.POST, new URL("http://request.url"));
        assertEquals(HttpMethod.POST, request.httpMethod());
        assertEquals(new URL("http://request.url"), request.url());
    }

    @Test
    public void testClone() throws IOException {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("my-header", "my-value");
        headers.set("other-header", "other-value");

        final HttpRequest request = new HttpRequest(
                HttpMethod.PUT,
                new URL("http://request.url"),
                headers,
                Flux.just(Unpooled.buffer(0, 0)));

        final HttpRequest bufferedRequest = request.buffer();

        assertNotSame(request, bufferedRequest);

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
