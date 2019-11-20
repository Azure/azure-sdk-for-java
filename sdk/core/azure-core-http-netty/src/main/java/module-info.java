// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.http.netty {
    requires transitive com.azure.core;
    requires reactor.netty;
    requires io.netty.buffer;
    requires io.netty.common;
    requires io.netty.transport;
    requires io.netty.handler;
    requires io.netty.codec;
    requires io.netty.codec.http;
    requires org.reactivestreams;

    exports com.azure.core.http.netty;
    exports com.azure.core.http.netty.implementation;       // FIXME this should not be a long-term solution

    provides com.azure.core.http.HttpClientProvider
        with com.azure.core.http.netty.implementation.ReactorNettyClientProvider;

    uses com.azure.core.http.HttpClientProvider;
}
