// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx.implementation;

import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.BinaryData;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

public final class BufferedVertxHttpResponse extends VertxHttpAsyncResponse {

    private final Buffer body;

    public BufferedVertxHttpResponse(HttpRequest azureHttpRequest, HttpClientResponse vertxHttpResponse, Buffer body) {
        super(azureHttpRequest, vertxHttpResponse);
        this.body = body;
    }

    @Override
    public BinaryData getBodyAsBinaryData() {
        return BinaryData.fromBytes(body.getBytes());
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return Flux.defer(() -> {
            if (this.body.length() == 0) {
                return Flux.empty();
            }
            return Flux.just(ByteBuffer.wrap(this.body.getBytes()));
        });
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return Mono.defer(() -> {
            if (this.body.length() == 0) {
                return Mono.empty();
            }
            return Mono.just(this.body.getBytes());
        });
    }

    @Override
    public HttpResponse buffer() {
        return this;
    }
}
