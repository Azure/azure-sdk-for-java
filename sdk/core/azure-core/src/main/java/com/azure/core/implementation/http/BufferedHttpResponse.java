// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.FluxUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * HTTP response which will buffer the response's body when/if it is read.
 */
public final class BufferedHttpResponse extends HttpResponse {
    private final HttpResponse innerHttpResponse;
    private final Flux<ByteBuffer> cachedBody;

    /**
     * Creates a buffered HTTP response.
     *
     * @param innerHttpResponse The HTTP response to buffer
     */
    public BufferedHttpResponse(HttpResponse innerHttpResponse) {
        super(innerHttpResponse.getRequest());
        this.innerHttpResponse = innerHttpResponse;
        this.cachedBody = innerHttpResponse.getBody().cache();
    }

    @Override
    public int getStatusCode() {
        return innerHttpResponse.getStatusCode();
    }

    @Override
    public String getHeaderValue(String name) {
        return innerHttpResponse.getHeaderValue(name);
    }

    @Override
    public HttpHeaders getHeaders() {
        return innerHttpResponse.getHeaders();
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
        return this;
    }
}
