// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.messaging.webpubsub.client {
    requires transitive com.azure.core;

    requires io.netty.common;
    requires io.netty.buffer;
    requires io.netty.transport;
    requires io.netty.handler;
    requires io.netty.codec;
    requires io.netty.codec.http;

    exports com.azure.messaging.webpubsub.client;
    exports com.azure.messaging.webpubsub.client.models;

    opens com.azure.messaging.webpubsub.client to com.azure.core;
    opens com.azure.messaging.webpubsub.client.models to com.azure.core;
    opens com.azure.messaging.webpubsub.client.implementation to com.azure.core;
    opens com.azure.messaging.webpubsub.client.implementation.models to com.azure.core;
}

