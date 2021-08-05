// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.CoreUtils;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;

/**
 * Base response class for JDK with implementations for response metadata.
 */
abstract class JdkHttpResponseBase extends HttpResponse {
    private final int statusCode;
    private final HttpHeaders headers;

    JdkHttpResponseBase(final HttpRequest request, int statusCode, HttpHeaders headers) {
        super(request);
        this.statusCode = statusCode;
        this.headers = headers;
    }

    @Override
    public final int getStatusCode() {
        return this.statusCode;
    }

    @Override
    public final String getHeaderValue(String name) {
        return this.headers.getValue(name);
    }

    @Override
    public final HttpHeaders getHeaders() {
        return this.headers;
    }

    @Override
    public final Mono<String> getBodyAsString() {
        return getBodyAsByteArray().map(bytes -> CoreUtils.bomAwareToString(bytes, getHeaderValue("Content-Type")));
    }

    @Override
    public final Mono<String> getBodyAsString(Charset charset) {
        return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
    }
}
