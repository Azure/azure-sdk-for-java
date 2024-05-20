// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx.implementation;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.CoreUtils;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpClientResponse;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.util.Map;

abstract class VertxHttpResponseBase extends HttpResponse {

    private final HttpClientResponse vertxHttpResponse;
    private final HttpHeaders headers;

    VertxHttpResponseBase(HttpRequest azureHttpRequest, HttpClientResponse vertxHttpResponse) {
        super(azureHttpRequest);
        this.vertxHttpResponse = vertxHttpResponse;
        this.headers = fromVertxHttpHeaders(vertxHttpResponse.headers());
    }

    @SuppressWarnings("deprecation")
    private HttpHeaders fromVertxHttpHeaders(MultiMap headers) {
        HttpHeaders azureHeaders = new HttpHeaders();
        for (Map.Entry<String, String> header : headers) {
            azureHeaders.add(header.getKey(), header.getValue());
        }
        return azureHeaders;
    }

    HttpClientResponse getVertxHttpResponse() {
        return this.vertxHttpResponse;
    }

    @Override
    public int getStatusCode() {
        return this.vertxHttpResponse.statusCode();
    }

    @Override
    @Deprecated
    public String getHeaderValue(String name) {
        return this.headers.getValue(name);
    }

    @Override
    public String getHeaderValue(HttpHeaderName headerName) {
        return headers.getValue(headerName);
    }

    @Override
    public HttpHeaders getHeaders() {
        return this.headers;
    }

    @Override
    public final Mono<String> getBodyAsString() {
        return getBodyAsByteArray()
            .map(bytes -> CoreUtils.bomAwareToString(bytes, getHeaderValue(HttpHeaderName.CONTENT_TYPE)));
    }

    @Override
    public final Mono<String> getBodyAsString(Charset charset) {
        return getBodyAsByteArray().map(bytes -> CoreUtils.bomAwareToString(bytes, charset.toString()));
    }
}
