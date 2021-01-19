// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.azure.core.http.HttpRequest;
import okhttp3.Response;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

/**
 * An OkHttp response where the response body has been buffered into memory.
 */
final class BufferedOkHttpResponse extends OkHttpResponseBase {
    private final byte[] body;

    BufferedOkHttpResponse(Response response, HttpRequest request, byte[] body) {
        super(response, request);
        this.body = body;
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return Flux.defer(() -> Flux.just(ByteBuffer.wrap(body)));
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return Mono.defer(() -> Mono.just(body));
    }
}
