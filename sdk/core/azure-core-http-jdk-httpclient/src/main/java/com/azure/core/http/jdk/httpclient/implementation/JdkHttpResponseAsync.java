// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient.implementation;

import com.azure.core.http.HttpRequest;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import static com.azure.core.http.jdk.httpclient.implementation.JdkHttpUtils.fromJdkHttpHeaders;

/**
 * Asynchronous response implementation for JDK HttpClient.
 */
public final class JdkHttpResponseAsync extends JdkHttpResponseBase {
    private final Flux<ByteBuffer> contentFlux;
    private volatile int disposed = 0;
    private static final AtomicIntegerFieldUpdater<JdkHttpResponseAsync> DISPOSED_UPDATER
        = AtomicIntegerFieldUpdater.newUpdater(JdkHttpResponseAsync.class, "disposed");

    /**
     * Creates an instance of {@link JdkHttpResponseAsync}.
     *
     * @param request the request which resulted in this response.
     * @param response the JDK HttpClient response.
     */
    public JdkHttpResponseAsync(final HttpRequest request,
        java.net.http.HttpResponse<Flow.Publisher<List<ByteBuffer>>> response) {
        super(request, response.statusCode(), fromJdkHttpHeaders(response.headers()));
        this.contentFlux = JdkFlowAdapter.flowPublisherToFlux(response.body()).flatMapSequential(Flux::fromIterable);
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return Flux.using(() -> this, ignored -> contentFlux, ignored -> DISPOSED_UPDATER.set(this, 1));
    }

    @Override
    public void close() {
        if (DISPOSED_UPDATER.compareAndSet(this, 0, 1)) {
            this.contentFlux.subscribe().dispose();
        }
    }
}
