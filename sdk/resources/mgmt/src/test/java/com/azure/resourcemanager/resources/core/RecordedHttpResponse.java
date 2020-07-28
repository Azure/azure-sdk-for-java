// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.core;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.http.BufferedHttpResponse;
import com.azure.core.util.FluxUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * HTTP response which will cache the response's body when/if it is read.
 */
public class RecordedHttpResponse extends HttpResponse {
    private final HttpHeaders headers;
    private final int statusCode;
    private Flux<ByteBuffer> cachedBody;

    public RecordedHttpResponse(int statusCode, HttpRequest request) {
        super(request);
        this.headers = new HttpHeaders();
        this.statusCode = statusCode;
    }

    public RecordedHttpResponse setBody(byte[] body) {
        this.cachedBody = Flux.just(ByteBuffer.wrap(body));
        return this;
    }

    public RecordedHttpResponse setBody(ByteBuffer body) {
        this.cachedBody = Flux.just(body);
        return this;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getHeaderValue(String name) {
        return headers.getValue(name);
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return cachedBody;
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return FluxUtil.collectBytesInByteBufferStream(cachedBody.map(ByteBuffer::duplicate));
    }

    @Override
    public Mono<String> getBodyAsString() {
        return getBodyAsByteArray()
                .map(bytes -> bytes == null ? null : new String(bytes, StandardCharsets.UTF_8));
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        return getBodyAsByteArray()
                .map(bytes -> bytes == null ? null : new String(bytes, charset));
    }

    @Override
    public BufferedHttpResponse buffer() {
        return new BufferedHttpResponse(this);
    }
}
