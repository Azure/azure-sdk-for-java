// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.http;

import io.netty.handler.codec.http.HttpResponseStatus;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.io.Closeable;
import java.util.Map;

/**
 * A simple {@link HttpServer} for unit testing the test proxy infrastructure.
 */
public class TestProxyTestServer implements Closeable {
    private final DisposableServer server;

    private static final String TEST_RESPONSE_BODY = "{\"modelId\":\"0cd2728b-210e-4c05-b706-f70554276bcc\",\"createdDateTime\":\"2022-08-31T00:00:00Z\",\"apiVersion\":\"2022-08-31\"}";

    /**
     * Constructor for TestProxyTestServer
     */
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
                })
                .get("/fr/models", (req, res) -> {
                    for (Map.Entry<String, String> requestHeader : req.requestHeaders()) {
                        res.addHeader(requestHeader.getKey(), requestHeader.getValue());
                    }
                    return res.status(HttpResponseStatus.OK)
                        .addHeader("Content-Type","application/json")
                        .sendString(Mono.just(TEST_RESPONSE_BODY));
                }))
            .bindNow();
    }

    @Override
    public void close() {
        server.disposeNow();
    }
}
