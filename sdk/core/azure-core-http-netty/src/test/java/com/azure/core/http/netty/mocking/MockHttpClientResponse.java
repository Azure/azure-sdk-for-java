// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.netty.mocking;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.Cookie;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientResponse;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import java.util.Map;
import java.util.Set;

public class MockHttpClientResponse implements HttpClientResponse {
    private final HttpHeaders headers;
    private final HttpResponseStatus status;

    public MockHttpClientResponse(HttpHeaders headers, HttpResponseStatus status) {
        this.headers = headers;
        this.status = status;
    }

    @Override
    public HttpHeaders responseHeaders() {
        return headers;
    }

    @Override
    public HttpResponseStatus status() {
        return status;
    }

    @Override
    public Mono<HttpHeaders> trailerHeaders() {
        return null;
    }

    @Override
    @Deprecated
    public Context currentContext() {
        return null;
    }

    @Override
    public ContextView currentContextView() {
        return null;
    }

    @Override
    public String[] redirectedFrom() {
        return new String[0];
    }

    @Override
    public HttpHeaders requestHeaders() {
        return null;
    }

    @Override
    public String resourceUrl() {
        return null;
    }

    @Override
    public Map<CharSequence, Set<Cookie>> cookies() {
        return null;
    }

    @Override
    public String fullPath() {
        return null;
    }

    @Override
    public String requestId() {
        return null;
    }

    @Override
    public boolean isKeepAlive() {
        return false;
    }

    @Override
    public boolean isWebsocket() {
        return false;
    }

    @Override
    public HttpMethod method() {
        return null;
    }

    @Override
    public String uri() {
        return null;
    }

    @Override
    public HttpVersion version() {
        return null;
    }
}
