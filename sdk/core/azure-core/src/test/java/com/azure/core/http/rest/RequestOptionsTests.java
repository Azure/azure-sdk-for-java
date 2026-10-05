// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.implementation.http.rest.ErrorOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.GeneratedCodeUtils;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.net.MalformedURLException;
import java.util.EnumSet;

import static com.azure.core.CoreTestUtils.createUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    public void generatedCodeAppliesRequestOptionsInOrder() throws MalformedURLException {
        HttpRequest request = new HttpRequest(HttpMethod.POST, createUrl("http://request.url"));
        BinaryData body = BinaryData.fromString("body");
        RequestOptions options
            = new RequestOptions().setHeader(X_MS_FOO, "initial").addRequestCallback(updatedRequest -> {
                assertEquals("initial", updatedRequest.getHeaders().getValue(X_MS_FOO));
                updatedRequest.setHttpMethod(HttpMethod.GET);
                updatedRequest.setUrl("https://request.url");
            }).addQueryParam("query", "a b").setHeader(X_MS_FOO, "updated").setBody(body);

        GeneratedCodeUtils.applyRequestOptions(request, options);

        assertEquals(HttpMethod.GET, request.getHttpMethod());
        assertEquals("https://request.url?query=a%20b", request.getUrl().toString());
        assertEquals("updated", request.getHeaders().getValue(X_MS_FOO));
        assertSame(body, request.getBodyAsBinaryData());
    }

    @Test
    public void generatedCodeAllowsNullRequestOptions() throws MalformedURLException {
        HttpRequest request = new HttpRequest(HttpMethod.POST, createUrl("http://request.url"));

        GeneratedCodeUtils.applyRequestOptions(request, null);

        assertEquals(HttpMethod.POST, request.getHttpMethod());
        assertEquals("http://request.url", request.getUrl().toString());
    }

    @Test
    public void generatedCodeRejectsNullRequest() {
        assertThrows(NullPointerException.class, () -> GeneratedCodeUtils.applyRequestOptions(null, null));
    }

    @Test
    public void generatedCodeThrowsOnUnexpectedResponsesByDefault() {
        assertTrue(GeneratedCodeUtils.shouldThrowException(null));
        assertTrue(GeneratedCodeUtils.shouldThrowException(new RequestOptions()));
    }

    @Test
    public void generatedCodeHonorsNoThrowErrorOptions() {
        RequestOptions options = new RequestOptions().setErrorOptions(EnumSet.of(ErrorOptions.NO_THROW));

        assertFalse(GeneratedCodeUtils.shouldThrowException(options));
    }

    @Test
    public void generatedCodeThrowsWhenErrorOptionsAreEmpty() {
        RequestOptions options = new RequestOptions().setErrorOptions(EnumSet.noneOf(ErrorOptions.class));

        assertTrue(GeneratedCodeUtils.shouldThrowException(options));
    }
}
