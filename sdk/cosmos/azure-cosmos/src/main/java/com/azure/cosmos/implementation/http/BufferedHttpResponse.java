// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.IllegalReferenceCountException;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * HTTP response which will buffer the response's body when/if it is read.
 */
public class BufferedHttpResponse extends HttpResponse {
    private final HttpResponse innerHttpResponse;
    private final Mono<byte[]> cachedBody;

    /**
     * Creates a buffered HTTP response.
     *
     * @param innerHttpResponse The HTTP response to buffer
     */
    BufferedHttpResponse(HttpResponse innerHttpResponse) {
        this.innerHttpResponse = innerHttpResponse;
        Mono<byte[]> bodyAsByteArrayMono = innerHttpResponse
            .body()
            .handle((bb, sink) -> {
                try {
                    byte[] bytes = new byte[bb.readableBytes()];
                    bb.readBytes(bytes);
                    sink.next(bytes);
                } catch (IllegalReferenceCountException var3) {
                    sink.complete();
                }
            });
        this.cachedBody = bodyAsByteArrayMono.cache();
        this.withRequest(innerHttpResponse.request());
    }

    @Override
    public int statusCode() {
        return innerHttpResponse.statusCode();
    }

    @Override
    public String headerValue(String name) {
        return innerHttpResponse.headerValue(name);
    }

    @Override
    public HttpHeaders headers() {
        return innerHttpResponse.headers();
    }

    @Override
    public Mono<ByteBuf> body() {
        return this.cachedBody
            .map(actualCachedByteArray -> Unpooled.wrappedBuffer(actualCachedByteArray));
    }

    @Override
    public Mono<String> bodyAsString() {
        return body()
                .map(buffer -> buffer == null ? null : buffer.toString(StandardCharsets.UTF_8));
    }

    @Override
    public BufferedHttpResponse buffer() {
        return this;
    }
}
