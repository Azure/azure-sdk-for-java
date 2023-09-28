// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.client.http.netty {
    requires transitive com.client.core;
    requires io.netty.buffer;
    requires io.netty.codec;
    requires io.netty.codec.http;
    requires io.netty.common;
    requires io.netty.handler;
    requires io.netty.handler.proxy;
    requires io.netty.resolver;
    requires io.netty.transport;
    requires reactor.netty.core;
    requires reactor.netty.http;

    exports com.client.core.http.netty;

    provides com.client.core.http.HttpClientProvider
        with com.client.core.http.netty.NettyAsyncHttpClientProvider;

    uses com.client.core.http.HttpClientProvider;
}
