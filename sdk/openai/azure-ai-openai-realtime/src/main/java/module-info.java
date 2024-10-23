// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.ai.openai.realtime {
    requires transitive com.azure.core;
    requires transitive com.azure.json;

    requires io.netty.common;
    requires io.netty.buffer;
    requires io.netty.transport;
    requires io.netty.handler;
    requires io.netty.codec;
    requires io.netty.codec.http;

    exports com.azure.ai.openai.realtime;
    exports com.azure.ai.openai.realtime.models;

    opens com.azure.ai.openai.realtime.models to com.azure.core;
    opens com.azure.ai.openai.realtime.implementation to com.azure.core;
}
