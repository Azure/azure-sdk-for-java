// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx.implementation;

import com.azure.core.http.HttpRequest;
import com.azure.core.util.FluxUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.streams.Pipe;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

/**
 * Default HTTP response for Vert.x.
 */
public class VertxHttpAsyncResponse extends VertxHttpResponseBase {
    private final Pipe<Buffer> pipe;

    public VertxHttpAsyncResponse(HttpRequest azureHttpRequest, HttpClientResponse vertxHttpResponse,
        Pipe<Buffer> pipe) {
        super(azureHttpRequest, vertxHttpResponse);
        this.pipe = pipe;
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
        return Flux.create(sink -> pipe.to(new FluxByteBufferWriteStream(sink), result -> {
            if (result.failed()) {
                sink.error(result.cause());
            } else {
                sink.complete();
            }
        }));
    }
}
