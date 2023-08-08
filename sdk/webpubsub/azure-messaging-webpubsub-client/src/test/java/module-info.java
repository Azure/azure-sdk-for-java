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

    opens com.azure.messaging.webpubsub.client.implementation.models to
        com.azure.core,
        com.fasterxml.jackson.databind;

    requires transitive com.azure.core.test;
    requires com.azure.http.netty;
    requires com.azure.messaging.webpubsub;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;
    requires org.junit.jupiter.params;
    requires org.mockito;
    requires net.bytebuddy;
    requires net.bytebuddy.agent;
}

