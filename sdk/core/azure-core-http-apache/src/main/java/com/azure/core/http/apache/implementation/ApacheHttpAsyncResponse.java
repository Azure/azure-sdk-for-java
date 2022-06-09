// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.apache.implementation;

import com.azure.core.http.HttpRequest;
import com.azure.core.util.FluxUtil;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.Message;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Default HTTP response for Apache HTTP.
 */
public final class ApacheHttpAsyncResponse extends ApacheHttpAsyncResponseBase {
    private final Flux<ByteBuffer> body;

    public ApacheHttpAsyncResponse(Message<HttpResponse, Publisher<ByteBuffer>> response, HttpRequest request) {
        super(response, request);
        body = Flux.from(response.getBody());
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return body;
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {


        return Mono.from(FluxUtil.collectBytesInByteBufferStream(getBody()));
    }

    @Override
    public Mono<InputStream> getBodyAsInputStream() {
        return Mono.empty();
    }
}
