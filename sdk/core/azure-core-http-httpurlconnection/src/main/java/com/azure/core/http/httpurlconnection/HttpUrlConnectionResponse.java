// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.httpurlconnection;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

class HttpUrlConnectionResponse extends HttpResponse {
    private final int statusCode;
    private final HttpHeaders headers;
    private final byte[] body;

    public HttpUrlConnectionResponse(HttpRequest request, int statusCode, HttpHeaders headers, BinaryData body) {
        super(request);
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body.toBytes();
    }

    @Override
    public int getStatusCode() {
        return this.statusCode;
    }

    @Override
    @Deprecated
    public final String getHeaderValue(String name) {
        return this.headers.getValue(name);
    }

    @Override
    public String getHeaderValue(HttpHeaderName name) {
        return headers.getValue(name);
    }

    @Override
    public HttpHeaders getHeaders() {
        return this.headers;
    }

    @Override
    public final Mono<String> getBodyAsString() {
        return getBodyAsByteArray().map(bytes -> CoreUtils.bomAwareToString(bytes,
            getHeaderValue(HttpHeaderName.CONTENT_TYPE)));
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return FluxUtil.collectBytesFromNetworkResponse(getBody(), getHeaders())
            .flatMap(bytes -> (bytes == null || bytes.length == 0)
                ? Mono.empty()
                : Mono.just(bytes));
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return (body.length == 0) ? Flux.empty() : Flux.just(ByteBuffer.wrap(body));
    }

    public HttpResponse buffer() {
        return this;
    }
}
