// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.aot.graalvm.support.netty {
    requires com.azure.aot.graalvm.support;
    requires io.netty.buffer;
    requires io.netty.codec;
    requires io.netty.codec.http;
    requires io.netty.common;
    requires io.netty.handler;
    requires io.netty.transport;
}
