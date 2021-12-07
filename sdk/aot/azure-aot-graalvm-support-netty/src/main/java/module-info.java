// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.aot.graalvm.support.netty {
    requires com.azure.aot.graalvm.support;
    requires io.netty.transport;
    requires io.netty.codec;
    requires io.netty.buffer;
    requires io.netty.handler;
    requires io.netty.codec.http;
}
