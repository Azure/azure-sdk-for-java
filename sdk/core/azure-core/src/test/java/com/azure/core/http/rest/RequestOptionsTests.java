// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RequestOptionsTests {
    @Test
    public void addQueryParam() throws MalformedURLException {
        final HttpRequest request = new HttpRequest(HttpMethod.POST, new URL("http://request.url"));

        RequestOptions options = new RequestOptions()
            .addQueryParam("foo", "bar")
            .addQueryParam("$skipToken", "1");
        options.getRequestCallback().accept(request);

        assertTrue(request.getUrl().toString().contains("?foo=bar&%24skipToken=1"));
    }

    @Test
    public void addHeader() throws MalformedURLException {
        final HttpRequest request = new HttpRequest(HttpMethod.POST, new URL("http://request.url"));

        RequestOptions options = new RequestOptions()
            .addHeader("x-ms-foo", "bar")
            .addHeader("Content-Type", "application/json");
        options.getRequestCallback().accept(request);

        HttpHeaders headers = request.getHeaders();
        assertEquals("bar", headers.getValue("x-ms-foo"));
        assertEquals("application/json", headers.getValue("Content-Type"));
    }

    @Test
    public void setBody() throws MalformedURLException {
        final HttpRequest request = new HttpRequest(HttpMethod.POST, new URL("http://request.url"));

        String expected = "{\"id\":\"123\"}";

        RequestOptions options = new RequestOptions()
            .setBody(BinaryData.fromString(expected));
        options.getRequestCallback().accept(request);

        StepVerifier.create(BinaryData.fromFlux(request.getBody()).map(BinaryData::toString))
            .expectNext(expected)
            .verifyComplete();
    }

    @Test
    public void addRequestCallback() throws MalformedURLException {
        final HttpRequest request = new HttpRequest(HttpMethod.POST, new URL("http://request.url"));

        RequestOptions options = new RequestOptions()
            .addHeader("x-ms-foo", "bar")
            .addRequestCallback(r -> r.setHttpMethod(HttpMethod.GET))
            .addRequestCallback(r -> r.setUrl("https://request.url"))
            .addQueryParam("$skipToken", "1")
            .addRequestCallback(r -> r.setHeader("x-ms-foo", "baz"));

        options.getRequestCallback().accept(request);

        HttpHeaders headers = request.getHeaders();
        assertEquals("baz", headers.getValue("x-ms-foo"));
        assertEquals(HttpMethod.GET, request.getHttpMethod());
        assertEquals("https://request.url?%24skipToken=1", request.getUrl().toString());
    }
}
