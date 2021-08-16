// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpRequest;
import com.azure.core.util.FluxUtil;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Flow;

import static com.azure.core.http.jdk.httpclient.JdkAsyncHttpClient.fromJdkHttpHeaders;

final class JdkHttpResponse extends JdkHttpResponseBase {
    private final Flux<ByteBuffer> contentFlux;
    private volatile boolean disposed = false;

    JdkHttpResponse(final HttpRequest request,
        java.net.http.HttpResponse<Flow.Publisher<List<ByteBuffer>>> response) {
        super(request, response.statusCode(), fromJdkHttpHeaders(response.headers()));
        this.contentFlux = JdkFlowAdapter.flowPublisherToFlux(response.body())
            .flatMapSequential(Flux::fromIterable);
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return this.contentFlux.doFinally(signalType -> disposed = true);
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return FluxUtil.collectBytesFromNetworkResponse(getBody(), getHeaders());
    }

    @Override
    public void close() {
        if (!this.disposed) {
            this.disposed = true;
            this.contentFlux
                .subscribe()
                .dispose();
        }
    }
}
