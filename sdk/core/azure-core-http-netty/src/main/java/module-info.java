// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.core.http.netty.ReactorNettyClientProvider;

module com.azure.http.netty {
    requires transitive com.azure.core;
    requires reactor.netty;
    requires io.netty.buffer;
    requires io.netty.common;
    requires io.netty.transport;
    requires io.netty.handler;
    requires io.netty.codec;
    requires io.netty.codec.http;
    requires io.netty.handler.proxy;

    exports com.azure.core.http.netty;

    provides com.azure.core.http.HttpClientProvider
        with ReactorNettyClientProvider;

    uses com.azure.core.http.HttpClientProvider;
}
