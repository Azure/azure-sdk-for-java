// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp.implementation;

import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.BinaryData;
import okhttp3.Response;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * An OkHttp response where the response body has been buffered into memory.
 */
public final class OkHttpAsyncBufferedResponse extends OkHttpAsyncResponseBase {
    private final BinaryData body;

    public OkHttpAsyncBufferedResponse(Response response, HttpRequest request, byte[] body) {
        super(response, request);
        this.body = BinaryData.fromBytes(body);
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return body.toFluxByteBuffer();
    }

    @Override
    public BinaryData getContent() {
        return body;
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return Mono.just(body.toBytes());
    }

    @Override
    public Mono<InputStream> getBodyAsInputStream() {
        return Mono.fromSupplier(body::toStream);
    }

    @Override
    public HttpResponse buffer() {
        return this; // This response is already buffered.
    }
}
