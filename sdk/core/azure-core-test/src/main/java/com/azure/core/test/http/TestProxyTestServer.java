// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.http;

import io.netty.handler.codec.http.HttpResponseStatus;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.io.Closeable;
import java.util.Map;

public class TestProxyTestServer implements Closeable {
    private final DisposableServer server;
    public TestProxyTestServer() {
        server = HttpServer.create()
            .host("localhost")
            .port(3000)
            .route(routes -> routes
                .get("/", (req, res) -> res.status(HttpResponseStatus.OK).sendString(Mono.just("hello world")))
                .get("/first/path", (req, res) -> res.status(HttpResponseStatus.OK).sendString(Mono.just("first path")))
                .get("/echoheaders", (req, res) -> {
                    for (Map.Entry<String, String> requestHeader : req.requestHeaders()) {
                        res.addHeader(requestHeader.getKey(), requestHeader.getValue());
                    }
                    return res.status(HttpResponseStatus.OK).sendString(Mono.just("echoheaders"));
                }))
            .bindNow();
    }

    @Override
    public void close() {
        server.disposeNow();
    }
}
