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

public final class BufferedVertxHttpResponse extends VertxHttpResponseBase {

    private final byte[] body;

    public BufferedVertxHttpResponse(HttpRequest azureHttpRequest, HttpClientResponse vertxHttpResponse, Buffer body) {
        super(azureHttpRequest, vertxHttpResponse);
        this.body = body.getBytes();
    }

    @Override
    public BinaryData getBodyAsBinaryData() {
        return BinaryData.fromBytes(body);
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return (body.length == 0) ? Flux.empty() : Flux.just(ByteBuffer.wrap(body));
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return (body.length == 0) ? Mono.empty() : Mono.just(body);
    }

    @Override
    public HttpResponse buffer() {
        return this;
    }
}
