// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.reactor.netty.implementation;

import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
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
public final class ReactorNettyAsyncHttpBufferedResponse extends ReactorNettyAsyncHttpResponseBase {
    private final byte[] body;

    /**
     * Creates a new buffered response.
     *
     * @param httpClientResponse The Reactor Netty HTTP response.
     * @param httpRequest The HTTP request that initiated this response.
     * @param body The buffered response body.
     * @param headersEagerlyConverted Whether the headers were eagerly converted.
     */
    public ReactorNettyAsyncHttpBufferedResponse(HttpClientResponse httpClientResponse, HttpRequest httpRequest, byte[] body,
                                                 boolean headersEagerlyConverted) {
        super(httpClientResponse, httpRequest, headersEagerlyConverted);
        this.body = body;
    }

    public BinaryData getBody() {
        return BinaryData.fromBytes(body);
    }

    public Flux<ByteBuffer> getBodyAsFlux() {
        return Mono.fromSupplier(() -> ByteBuffer.wrap(body)).flux();
    }

    public Mono<byte[]> getBodyAsByteArray() {
        return Mono.just(body);
    }

    public Mono<String> getBodyAsString(Charset charset) {
        return Mono.fromSupplier(() -> new String(body, charset));
    }

    public Mono<InputStream> getBodyAsInputStream() {
        return Mono.fromSupplier(() -> new ByteArrayInputStream(body));
    }

    public Response<BinaryData> buffer() {
        return this; // This response is already buffered.
    }
}
