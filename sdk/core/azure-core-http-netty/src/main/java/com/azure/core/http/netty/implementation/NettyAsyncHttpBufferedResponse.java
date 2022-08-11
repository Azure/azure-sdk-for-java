// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * A Reactor Netty response where the response body has been buffered into memory.
 */
public final class NettyAsyncHttpBufferedResponse extends NettyAsyncHttpResponseBase {
    private final byte[] body;

    public NettyAsyncHttpBufferedResponse(HttpClientResponse httpClientResponse, HttpRequest httpRequest, byte[] body) {
        super(httpClientResponse, httpRequest);
        this.body = body;
    }

    @Override
    public BinaryData getBodyAsBinaryData() {
        return BinaryData.fromBytes(body);
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return Mono.fromSupplier(() -> ByteBuffer.wrap(body)).flux();
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return Mono.just(body);
    }

    @Override
    public Mono<String> getBodyAsString() {
        return Mono.fromSupplier(() -> CoreUtils.bomAwareToString(body, getHeaderValue("Content-Type")));
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        return Mono.fromSupplier(() -> new String(body, charset));
    }

    @Override
    public Mono<InputStream> getBodyAsInputStream() {
        return Mono.fromSupplier(() -> new ByteArrayInputStream(body));
    }

    @Override
    public HttpResponse buffer() {
        return this; // This response is already buffered.
    }
}
