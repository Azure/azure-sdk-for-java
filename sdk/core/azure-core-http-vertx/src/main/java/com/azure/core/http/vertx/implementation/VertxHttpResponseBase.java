// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.CoreUtils;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpClientResponse;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;

/**
 * Base response class for Vert.x HTTP client with implementations for response metadata.
 */
abstract class VertxHttpResponseBase extends HttpResponse {

    private final HttpClientResponse vertxHttpResponse;
    private final HttpHeaders azureHttpHeaders;

    VertxHttpResponseBase(HttpRequest azureHttpRequest, HttpClientResponse vertxHttpResponse) {
        super(azureHttpRequest);
        this.vertxHttpResponse = vertxHttpResponse;
        this.vertxHttpResponse.pause();
        this.azureHttpHeaders = fromVertxHttpHeaders(vertxHttpResponse.headers());
    }

    protected HttpClientResponse getVertxHttpResponse() {
        return this.vertxHttpResponse;
    }

    @Override
    public int getStatusCode() {
        return this.vertxHttpResponse.statusCode();
    }

    @Override
    public String getHeaderValue(String name) {
        return this.azureHttpHeaders.getValue(name);
    }

    @Override
    public HttpHeaders getHeaders() {
        return this.azureHttpHeaders;
    }

    @Override
    public final Mono<String> getBodyAsString() {
        return getBodyAsByteArray().map(bytes -> CoreUtils.bomAwareToString(bytes, getHeaderValue("Content-Type")));
    }

    @Override
    public final Mono<String> getBodyAsString(Charset charset) {
        return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
    }

    /**
     * Creates azure-core HttpHeaders from Vert.x HTTP headers.
     *
     * @param vertxHttpHeaders Vert.x HTTP response headers.
     * @return Azure HTTP headers.
     */
    private HttpHeaders fromVertxHttpHeaders(MultiMap vertxHttpHeaders) {
        HttpHeaders azureHeaders = new HttpHeaders(vertxHttpHeaders.size());
        vertxHttpHeaders.forEach(azureHeaders::add);
        return azureHeaders;
    }
}
