// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
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
    @Deprecated
    public final String getHeaderValue(String name) {
        return this.headers.getValue(name);
    }

    @Override
    public final String getHeaderValue(HttpHeaderName headerName) {
        return headers.getValue(headerName);
    }

    @Override
    public final HttpHeaders getHeaders() {
        return this.headers;
    }

    @Override
    public final Mono<String> getBodyAsString() {
        return getBodyAsByteArray().map(bytes -> CoreUtils.bomAwareToString(bytes,
            getHeaderValue(HttpHeaderName.CONTENT_TYPE)));
    }

    @Override
    public final Mono<String> getBodyAsString(Charset charset) {
        return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
    }


    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return FluxUtil.collectBytesFromNetworkResponse(getBody(), getHeaders())
            // Map empty byte[] into Mono.empty, this matches how the other HttpResponse implementations handle this.
            .flatMap(bytes -> (bytes == null || bytes.length == 0)
                ? Mono.empty()
                : Mono.just(bytes));
    }
}
