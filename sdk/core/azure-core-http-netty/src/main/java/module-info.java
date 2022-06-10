// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.http.netty {
    requires transitive com.azure.core;
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

    exports com.azure.core.http.netty;

    provides com.azure.core.http.HttpClientProvider
        with com.azure.core.http.netty.NettyAsyncHttpClientProvider;

    uses com.azure.core.http.HttpClientProvider;
}
