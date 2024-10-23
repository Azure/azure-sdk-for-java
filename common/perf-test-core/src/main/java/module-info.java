// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.core.test.perf {
    requires com.azure.core;
    requires com.azure.core.http.okhttp;
    requires com.azure.http.netty;
    requires com.azure.core.http.jdk.httpclient;
    requires com.azure.core.http.vertx;
    requires reactor.core;
    requires org.reactivestreams;
    requires jcommander;
    requires reactor.netty.http;
    requires io.netty.handler;
    requires reactor.netty.core;
    requires io.netty.codec.http;
    requires okhttp3;
    requires io.vertx.core;
}
