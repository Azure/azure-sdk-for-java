// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.util;

import org.reactivestreams.Publisher;
import reactor.netty.Connection;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

/**
 * Loopback endpoint for testing IMDS discovery without contacting Azure.
 */
public final class ImdsProbeTestServer implements AutoCloseable {
    private final Queue<Connection> connections = new ConcurrentLinkedQueue<>();
    private final AtomicInteger requestCount = new AtomicInteger();
    private final DisposableServer server;
    private volatile String requestUri;
    private volatile String metadataHeader;

    public ImdsProbeTestServer(BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>> handler) {
        server = HttpServer.create()
            .host("127.0.0.1")
            .port(0)
            .doOnConnection(connections::add)
            .handle((request, response) -> {
                requestUri = request.uri();
                metadataHeader = request.requestHeaders().get("Metadata");
                requestCount.incrementAndGet();
                return handler.apply(request, response);
            })
            .bindNow(Duration.ofSeconds(5));
    }

    public String getEndpoint() {
        return "http://127.0.0.1:" + server.port();
    }

    public int getRequestCount() {
        return requestCount.get();
    }

    public String getRequestUri() {
        return requestUri;
    }

    public String getMetadataHeader() {
        return metadataHeader;
    }

    @Override
    public void close() {
        connections.forEach(Connection::dispose);
        server.disposeNow(Duration.ofSeconds(5));
    }
}
