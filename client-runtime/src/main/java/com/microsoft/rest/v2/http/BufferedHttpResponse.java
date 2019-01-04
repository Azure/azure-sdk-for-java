/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * HTTP response which will buffer the response's body when/if it is read.
 */
public final class BufferedHttpResponse extends HttpResponse {
    private final HttpResponse innerHttpResponse;
    private final Mono<byte[]> cachedBody;

    /**
     * Creates a buffered HTTP response.
     * @param innerHttpResponse The HTTP response to buffer.
     */
    public BufferedHttpResponse(HttpResponse innerHttpResponse) {
        this.innerHttpResponse = innerHttpResponse;
        this.cachedBody = innerHttpResponse.bodyAsByteArray().cache();
    }

    @Override
    public int statusCode() {
        return innerHttpResponse.statusCode();
    }

    @Override
    public String headerValue(String headerName) {
        return innerHttpResponse.headerValue(headerName);
    }

    @Override
    public HttpHeaders headers() {
        return innerHttpResponse.headers();
    }

    @Override
    public Mono<byte[]> bodyAsByteArray() {
        return cachedBody;
    }

    @Override
    public Flux<ByteBuffer> body() {
        return bodyAsByteArray().flatMapMany(bytes -> Flux.just(ByteBuffer.wrap(bytes)));
    }

    @Override
    public Mono<String> bodyAsString() {
        return bodyAsByteArray()
                .map(bytes -> bytes == null ? null : new String(bytes, StandardCharsets.UTF_8));
    }

    @Override
    public BufferedHttpResponse buffer() {
        return this;
    }

    @Override
    public boolean isDecoded() {
        return innerHttpResponse.isDecoded();
    }

    @Override
    public boolean withIsDecoded(boolean isDecoded) {
        return innerHttpResponse.withIsDecoded(isDecoded);
    }

    @Override
    public Object deserializedHeaders() {
        return innerHttpResponse.deserializedHeaders();
    }

    @Override
    public HttpResponse withDeserializedHeaders(Object deserializedHeaders) {
        innerHttpResponse.withDeserializedHeaders(deserializedHeaders);
        return this;
    }

    @Override
    public Object deserializedBody() {
        return innerHttpResponse.deserializedBody();
    }

    @Override
    public HttpResponse withDeserializedBody(Object deserializedBody) {
        innerHttpResponse.withDeserializedBody(deserializedBody);
        return this;
    }
}
