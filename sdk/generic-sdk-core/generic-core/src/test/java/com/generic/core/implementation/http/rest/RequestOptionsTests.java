// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.rest;

import com.generic.core.http.models.HeaderName;
import com.generic.core.http.models.Headers;
import com.generic.core.http.models.HttpMethod;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.RequestOptions;
import com.generic.core.util.binarydata.BinaryData;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;

import static com.generic.core.util.TestUtils.createUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RequestOptionsTests {
    private static final HeaderName X_MS_FOO = HeaderName.fromString("x-ms-foo");

    @Test
    public void addQueryParam() throws MalformedURLException {
        final HttpRequest request = new HttpRequest(HttpMethod.POST, createUrl("http://request.url"));

        RequestOptions options = new RequestOptions()
            .addQueryParam("foo", "bar")
            .addQueryParam("$skipToken", "1");

        options.getRequestCallback().accept(request);

        assertTrue(request.getUrl().toString().contains("?foo=bar&%24skipToken=1"));
    }

    @Test
    public void addHeader() throws MalformedURLException {
        final HttpRequest request = new HttpRequest(HttpMethod.POST, createUrl("http://request.url"));

        RequestOptions options = new RequestOptions()
            .addHeader(X_MS_FOO, "bar")
            .addHeader(HeaderName.CONTENT_TYPE, "application/json");
        options.getRequestCallback().accept(request);

        Headers headers = request.getHeaders();
        assertEquals("bar", headers.getValue(X_MS_FOO));
        assertEquals("application/json", headers.getValue(HeaderName.CONTENT_TYPE));
    }

    @Test
    public void setBody() throws MalformedURLException {
        final HttpRequest request = new HttpRequest(HttpMethod.POST, createUrl("http://request.url"));

        String expected = "{\"id\":\"123\"}";

        BinaryData requestBody = BinaryData.fromString(expected);
        RequestOptions options = new RequestOptions()
            .setBody(requestBody);
        options.getRequestCallback().accept(request);
        BinaryData actual = request.getBody();

        assertSame(requestBody, actual);
        assertEquals(expected, actual.toString());
    }

    @Test
    public void addRequestCallback() throws MalformedURLException {
        final HttpRequest request = new HttpRequest(HttpMethod.POST, createUrl("http://request.url"));

        RequestOptions options = new RequestOptions()
            .addHeader(X_MS_FOO, "bar")
            .addRequestCallback(r -> r.setHttpMethod(HttpMethod.GET))
            .addRequestCallback(r -> r.setUrl("https://request.url"))
            .addQueryParam("$skipToken", "1")
            .addRequestCallback(r -> r.getHeaders().set(X_MS_FOO, "baz"));

        options.getRequestCallback().accept(request);

        Headers headers = request.getHeaders();
        assertEquals("baz", headers.getValue(X_MS_FOO));
        assertEquals(HttpMethod.GET, request.getHttpMethod());
        assertEquals("https://request.url?%24skipToken=1", request.getUrl().toString());
    }
}
