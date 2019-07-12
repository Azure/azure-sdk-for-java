// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import io.netty.buffer.Unpooled;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public class HttpRequestTests {
    @Test
    public void constructor() throws MalformedURLException {
        final HttpRequest request = new HttpRequest(HttpMethod.POST, new URL("http://request.url"));
        assertEquals(HttpMethod.POST, request.httpMethod());
        assertEquals(new URL("http://request.url"), request.url());
    }

    @Test
    public void testClone() throws IOException {
        final HttpHeaders headers = new HttpHeaders().put("my-header", "my-value")
            .put("other-header", "other-value");

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
        assertEquals(request.headers().size(), bufferedRequest.headers().size());
        for (HttpHeader clonedHeader : bufferedRequest.headers()) {
            for (HttpHeader originalHeader : request.headers()) {
                assertNotSame(clonedHeader, originalHeader);
            }

            assertEquals(clonedHeader.value(), request.headers().value(clonedHeader.name()));
        }

        assertSame(request.body(), bufferedRequest.body());
    }
}
