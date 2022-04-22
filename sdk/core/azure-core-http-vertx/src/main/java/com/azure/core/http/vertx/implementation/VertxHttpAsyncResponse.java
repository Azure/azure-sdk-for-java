// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx.implementation;

import com.azure.core.http.HttpRequest;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

/**
 * Default HTTP response for Vert.x.
 */
class VertxHttpAsyncResponse extends VertxHttpResponseBase {

    VertxHttpAsyncResponse(HttpRequest request, HttpResponse<Buffer> response) {
        super(request, response);
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        Buffer responseBody = getVertxHttpResponse().bodyAsBuffer();
        if (isEmptyResponse(responseBody)) {
            return Flux.empty();
        }
        return Flux.just(responseBody.getByteBuf().nioBuffer());
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return Mono.fromCallable(() -> {
            Buffer responseBody = getVertxHttpResponse().bodyAsBuffer();
            if (isEmptyResponse(responseBody)) {
                return null;
            }
            return responseBody.getBytes();
        });
    }
}
