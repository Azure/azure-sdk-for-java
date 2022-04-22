// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx.implementation;

import com.azure.core.http.HttpRequest;
import io.vertx.core.buffer.Buffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

final class BufferedVertxHttpResponse extends VertxHttpAsyncResponse {

    private final Buffer body;

    BufferedVertxHttpResponse(HttpRequest request, io.vertx.ext.web.client.HttpResponse<Buffer> response, Buffer body) {
        super(request, response);
        this.body = body;
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return Flux.defer(() -> {
            if (isEmptyResponse(this.body)) {
                return Flux.empty();
            }
            return Flux.just(this.body.getByteBuf().nioBuffer());
        });
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return Mono.defer(() -> {
            if (isEmptyResponse(this.body)) {
                return Mono.empty();
            }
            return Mono.just(this.body.getBytes());
        });
    }
}
