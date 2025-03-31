// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * Provides the classes necessary to create an HTTP client using Netty.
 */
module io.clientcore.http.netty {
    requires transitive io.clientcore.core;

    requires io.netty.transport;
    requires io.netty.buffer;
    requires io.netty.codec.http;
    requires io.netty.handler;
    requires io.netty.common;
    requires io.netty.handler.proxy;
    requires io.netty.codec;
    requires io.netty.resolver;

    exports io.clientcore.http.netty;

    provides io.clientcore.core.http.client.HttpClientProvider with io.clientcore.http.netty.NettyHttpClientProvider;

    uses io.clientcore.core.http.client.HttpClientProvider;
}
