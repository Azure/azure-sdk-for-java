// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.rest;

import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.models.binarydata.BinaryData;
import java.net.URI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RequestContextTests {
    private static final HttpHeaderName X_MS_FOO = HttpHeaderName.fromString("x-ms-foo");

    @Test
    public void addQueryParam() {
        final HttpRequest request
            = new HttpRequest().setMethod(HttpMethod.POST).setUri(URI.create("http://request.uri"));

        RequestContext context
            = RequestContext.builder().addQueryParam("foo", "bar").addQueryParam("$skipToken", "1").build();
        context.getRequestCallback().accept(request);

        assertTrue(request.getUri().toString().contains("?foo=bar&%24skipToken=1"));
    }

    @Test
    public void addHeader() {
        final HttpRequest request
            = new HttpRequest().setMethod(HttpMethod.POST).setUri(URI.create("http://request.uri"));
        RequestContext context = RequestContext.builder()
            .addBeforeRequestInterceptor(request2 -> request2.getHeaders()
                .add(new HttpHeader(X_MS_FOO, "bar"))
                .set(HttpHeaderName.CONTENT_TYPE, "application/json"))
            .build();

        context.getRequestCallback().accept(request);

        HttpHeaders headers = request.getHeaders();
        assertEquals("bar", headers.getValue(X_MS_FOO));
        assertEquals("application/json", headers.getValue(HttpHeaderName.CONTENT_TYPE));
    }

    @Test
    public void setBody() {
        final HttpRequest request
            = new HttpRequest().setMethod(HttpMethod.POST).setUri(URI.create("http://request.uri"));

        String expected = "{\"id\":\"123\"}";

        BinaryData requestBody = BinaryData.fromString(expected);
        RequestContext context
            = RequestContext.builder().addBeforeRequestInterceptor(request2 -> request2.setBody(requestBody)).build();
        context.getRequestCallback().accept(request);
        BinaryData actual = request.getBody();

        assertSame(requestBody, actual);
        assertEquals(expected, actual.toString());
    }

    @Test
    public void addBeforeRequestInterceptor() {
        final HttpRequest request
            = new HttpRequest().setMethod(HttpMethod.POST).setUri(URI.create("http://request.uri"));
        RequestContext context = RequestContext.builder()
            .addBeforeRequestInterceptor(request2 -> request2
                // may already be set if request is created from a client
                .setUri("https://request.uri")
                .setMethod(HttpMethod.GET)
                .getHeaders()
                .set(X_MS_FOO, "baz"))
            .addQueryParam("$skipToken", "1")
            .build();
        context.getRequestCallback().accept(request);

        HttpHeaders headers = request.getHeaders();
        assertEquals("baz", headers.getValue(X_MS_FOO));
        assertEquals(HttpMethod.GET, request.getHttpMethod());
        assertEquals("https://request.uri?%24skipToken=1", request.getUri().toString());
    }

    @Test
    public void addBeforeRequestInterceptorWorks() {
        final HttpRequest request
            = new HttpRequest().setMethod(HttpMethod.POST).setUri(URI.create("http://request.uri"));
        RequestContext context = RequestContext.builder()
            .addBeforeRequestInterceptor(request2 -> request2.setUri("https://updated.uri")
                .setMethod(HttpMethod.GET)
                .getHeaders()
                .set(X_MS_FOO, "updated"))
            .build();
        context.getRequestCallback().accept(request);

        HttpHeaders headers = request.getHeaders();
        assertEquals("updated", headers.getValue(X_MS_FOO));
        assertEquals(HttpMethod.GET, request.getHttpMethod());
        assertEquals("https://updated.uri", request.getUri().toString());
    }
}
