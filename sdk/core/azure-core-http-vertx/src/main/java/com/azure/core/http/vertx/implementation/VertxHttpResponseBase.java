// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.CoreUtils;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;

abstract class VertxHttpResponseBase extends HttpResponse {

    private final io.vertx.ext.web.client.HttpResponse<Buffer> response;
    private final HttpHeaders headers;

    VertxHttpResponseBase(HttpRequest request, io.vertx.ext.web.client.HttpResponse<Buffer> response) {
        super(request);
        this.response = response;
        this.headers = fromVertxHttpHeaders(response.headers());
    }

    private HttpHeaders fromVertxHttpHeaders(MultiMap headers) {
        HttpHeaders azureHeaders = new HttpHeaders();
        headers.names().forEach(name -> azureHeaders.set(name, headers.getAll(name)));
        return azureHeaders;
    }

    protected io.vertx.ext.web.client.HttpResponse<Buffer> getVertxHttpResponse() {
        return this.response;
    }

    @Override
    public int getStatusCode() {
        return response.statusCode();
    }

    @Override
    public String getHeaderValue(String name) {
        return this.headers.getValue(name);
    }

    @Override
    public HttpHeaders getHeaders() {
        return this.headers;
    }

    @Override
    public final Mono<String> getBodyAsString() {
        return getBodyAsByteArray().map(bytes -> CoreUtils.bomAwareToString(bytes, getHeaderValue("Content-Type")));
    }

    @Override
    public final Mono<String> getBodyAsString(Charset charset) {
        return Mono.fromCallable(() -> this.response.bodyAsString(charset.toString()));
    }

    boolean isEmptyResponse(Buffer responseBody) {
        return responseBody == null || responseBody.length() == 0;
    }
}
