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
public class VertxHttpAsyncResponse extends VertxHttpResponseBase {

    public VertxHttpAsyncResponse(HttpRequest azureHttpRequest, HttpClientResponse vertxHttpResponse) {
        super(azureHttpRequest, vertxHttpResponse);
        vertxHttpResponse.pause();
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return streamResponseBody();
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return FluxUtil.collectBytesFromNetworkResponse(streamResponseBody(), getHeaders())
            .flatMap(bytes -> (bytes == null || bytes.length == 0)
                ? Mono.empty()
                : Mono.just(bytes));
    }

    private Flux<ByteBuffer> streamResponseBody() {
        HttpClientResponse vertxHttpResponse = getVertxHttpResponse();
        return Flux.create(sink -> {
            vertxHttpResponse.handler(buffer -> {
                sink.next(buffer.getByteBuf().nioBuffer());
            }).endHandler(event -> {
                sink.complete();
            }).exceptionHandler(sink::error);

            vertxHttpResponse.resume();
        });
    }
}
