// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx.implementation;

import com.azure.core.http.HttpRequest;
import com.azure.core.util.FluxUtil;
import io.vertx.core.http.HttpClientResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

/**
 * Default HTTP response for Vert.x.
 */
public final class VertxHttpAsyncResponse extends VertxHttpResponseBase {
    private volatile boolean closed;

    /**
     * Creates a http response.
     *
     * @param azureHttpRequest the original azure http request
     * @param vertxHttpResponse the vertx http response
     */
    public VertxHttpAsyncResponse(HttpRequest azureHttpRequest, HttpClientResponse vertxHttpResponse) {
        super(azureHttpRequest, vertxHttpResponse.pause());
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return streamResponseBody();
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return FluxUtil.collectBytesFromNetworkResponse(streamResponseBody(), getHeaders());
    }

    @SuppressWarnings("deprecation")
    private Flux<ByteBuffer> streamResponseBody() {
        HttpClientResponse vertxHttpResponse = getVertxHttpResponse();
        return Flux.create(sink -> {
            vertxHttpResponse.handler(buffer -> sink.next(buffer.getByteBuf().nioBuffer())).endHandler(event -> {
                closed = true;
                sink.complete();
            }).exceptionHandler(sink::error);

            vertxHttpResponse.resume();
        });
    }

    @Override
    public void close() {
        HttpClientResponse vertxHttpResponse = getVertxHttpResponse();
        if (vertxHttpResponse != null && !closed) {
            vertxHttpResponse.netSocket().close(ignored -> closed = true);
        }
    }
}
