// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;

import static com.azure.core.CoreTestUtils.createUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    public void unknownLengthBodyReplacesEmptyRequestFraming() {
        HttpRequest request
            = new HttpRequest(HttpMethod.POST, "https://request.url").setHeader(HttpHeaderName.CONTENT_LENGTH, "0");
        BinaryData body = BinaryData.fromStream(new ByteArrayInputStream(new byte[] { 1, 2, 3 }));

        new RequestOptions().setBody(body).getRequestCallback().accept(request);

        assertSame(body, request.getBodyAsBinaryData());
        assertNull(request.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));
    }

    @Test
    public void explicitLengthCanFollowUnknownLengthBody() {
        HttpRequest request
            = new HttpRequest(HttpMethod.POST, "https://request.url").setHeader(HttpHeaderName.CONTENT_LENGTH, "0");
        BinaryData body = BinaryData.fromStream(new ByteArrayInputStream(new byte[] { 1, 2, 3 }));

        new RequestOptions().setBody(body)
            .setHeader(HttpHeaderName.CONTENT_LENGTH, "3")
            .getRequestCallback()
            .accept(request);

        assertEquals("3", request.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));
    }

    @Test
    public void nonzeroExplicitLengthIsPreservedForUnknownLengthBody() {
        HttpRequest request
            = new HttpRequest(HttpMethod.POST, "https://request.url").setHeader(HttpHeaderName.CONTENT_LENGTH, "3");
        BinaryData body = BinaryData.fromStream(new ByteArrayInputStream(new byte[] { 1, 2, 3 }));

        new RequestOptions().setBody(body).getRequestCallback().accept(request);

        assertEquals("3", request.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));
    }

    @Test
    public void unknownLengthBodyRemovesLengthInferredFromReplacedBody() {
        HttpRequest request
            = new HttpRequest(HttpMethod.POST, "https://request.url").setBody(BinaryData.fromBytes(new byte[5]));
        BinaryData body = BinaryData.fromStream(new ByteArrayInputStream(new byte[] { 1, 2, 3 }));

        new RequestOptions().setBody(body).getRequestCallback().accept(request);

        assertSame(body, request.getBodyAsBinaryData());
        assertNull(request.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));
    }

    @Test
    public void unknownLengthBodyRemovesLengthOfReplacedZeroLengthBody() {
        HttpRequest request
            = new HttpRequest(HttpMethod.POST, "https://request.url").setBody(BinaryData.fromBytes(new byte[0]));
        BinaryData body = BinaryData.fromStream(new ByteArrayInputStream(new byte[] { 1, 2, 3 }));

        new RequestOptions().setBody(body).getRequestCallback().accept(request);

        assertNull(request.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));
    }

    @Test
    public void explicitLengthSetBeforeUnknownLengthBodyIsPreserved() {
        HttpRequest request
            = new HttpRequest(HttpMethod.POST, "https://request.url").setBody(BinaryData.fromBytes(new byte[5]));
        BinaryData body = BinaryData.fromStream(new ByteArrayInputStream(new byte[] { 1, 2, 3 }));

        new RequestOptions().setHeader(HttpHeaderName.CONTENT_LENGTH, "3")
            .setBody(body)
            .getRequestCallback()
            .accept(request);

        assertEquals("3", request.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));
    }

    @Test
    public void explicitLengthMatchingReplacedBodyIsPreserved() {
        HttpRequest request
            = new HttpRequest(HttpMethod.POST, "https://request.url").setBody(BinaryData.fromBytes(new byte[5]));
        BinaryData body = BinaryData.fromStream(new ByteArrayInputStream(new byte[5]));

        new RequestOptions().setHeader(HttpHeaderName.CONTENT_LENGTH, "5")
            .setBody(body)
            .getRequestCallback()
            .accept(request);

        assertSame(body, request.getBodyAsBinaryData());
        assertEquals("5", request.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));
    }

    @Test
    public void explicitZeroLengthFromCallbackIsPreserved() {
        HttpRequest request = new HttpRequest(HttpMethod.POST, "https://request.url");
        BinaryData body = BinaryData.fromStream(new ByteArrayInputStream(new byte[0]));

        new RequestOptions().addRequestCallback(r -> r.setHeader(HttpHeaderName.CONTENT_LENGTH, "0"))
            .setBody(body)
            .getRequestCallback()
            .accept(request);

        assertEquals("0", request.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));
    }

    @Test
    public void unknownLengthBodyRemovesLengthInferredFromPreviouslySetBody() {
        HttpRequest request = new HttpRequest(HttpMethod.POST, "https://request.url");
        BinaryData body = BinaryData.fromStream(new ByteArrayInputStream(new byte[] { 1, 2, 3 }));

        new RequestOptions().setBody(BinaryData.fromBytes(new byte[5]))
            .setBody(body)
            .getRequestCallback()
            .accept(request);

        assertSame(body, request.getBodyAsBinaryData());
        assertNull(request.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));
    }

    @Test
    public void explicitLengthBetweenBodyReplacementsIsPreserved() {
        HttpRequest request = new HttpRequest(HttpMethod.POST, "https://request.url");
        BinaryData body = BinaryData.fromStream(new ByteArrayInputStream(new byte[5]));

        new RequestOptions().setBody(BinaryData.fromBytes(new byte[5]))
            .addRequestCallback(r -> r.setHeader(HttpHeaderName.CONTENT_LENGTH, "5"))
            .setBody(body)
            .getRequestCallback()
            .accept(request);

        assertSame(body, request.getBodyAsBinaryData());
        assertEquals("5", request.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));
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
