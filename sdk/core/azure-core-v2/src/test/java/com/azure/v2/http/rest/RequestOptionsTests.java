// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import com.azure.core.v2.util.BinaryData;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.net.MalformedURLException;

import static com.azure.core.CoreTestUtils.createUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RequestOptionsTests {
    private static final HttpHeaderName X_MS_FOO = HttpHeaderName.fromString("x-ms-foo");

    @Test
    public void addQueryParam() throws MalformedURLException {
        final HttpRequest request = new HttpRequest(HttpMethod.POST, createUrl("http://request.url"));

        RequestOptions options = new RequestOptions().addQueryParam("foo", "bar").addQueryParam("$skipToken", "1");
        options.getRequestCallback().accept(request);

        assertTrue(request.getUrl().toString().contains("?foo=bar&%24skipToken=1"));
    }

    @Test
    public void addHeader() throws MalformedURLException {
        final HttpRequest request = new HttpRequest(HttpMethod.POST, createUrl("http://request.url"));

        RequestOptions options = new RequestOptions().addHeader(X_MS_FOO, "bar")
            .addHeader(HttpHeaderName.CONTENT_TYPE, "application/json");
        options.getRequestCallback().accept(request);

        HttpHeaders headers = request.getHeaders();
        assertEquals("bar", headers.getValue(X_MS_FOO));
        assertEquals("application/json", headers.getValue(HttpHeaderName.CONTENT_TYPE));
    }

    @Test
    public void setBody() throws MalformedURLException {
        final HttpRequest request = new HttpRequest(HttpMethod.POST, createUrl("http://request.url"));

        String expected = "{\"id\":\"123\"}";

        BinaryData requestBody = BinaryData.fromString(expected);
        RequestOptions options = new RequestOptions().setBody(requestBody);
        options.getRequestCallback().accept(request);

        assertSame(requestBody, request.getBodyAsBinaryData());
        StepVerifier.create(BinaryData.fromFlux(request.getBody()).map(BinaryData::toString))
            .expectNext(expected)
            .verifyComplete();
    }

    @Test
    public void addRequestCallback() throws MalformedURLException {
        final HttpRequest request = new HttpRequest(HttpMethod.POST, createUrl("http://request.url"));

        RequestOptions options = new RequestOptions().addHeader(X_MS_FOO, "bar")
            .addRequestCallback(r -> r.setHttpMethod(HttpMethod.GET))
            .addRequestCallback(r -> r.setUrl("https://request.url"))
            .addQueryParam("$skipToken", "1")
            .addRequestCallback(r -> r.setHeader(X_MS_FOO, "baz"));

        options.getRequestCallback().accept(request);

        HttpHeaders headers = request.getHeaders();
        assertEquals("baz", headers.getValue(X_MS_FOO));
        assertEquals(HttpMethod.GET, request.getHttpMethod());
        assertEquals("https://request.url?%24skipToken=1", request.getUrl().toString());
    }
}
