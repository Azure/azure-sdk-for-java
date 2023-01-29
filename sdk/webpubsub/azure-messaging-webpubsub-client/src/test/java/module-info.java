// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module azure.messaging.webpubsub.client {
    requires transitive com.azure.core;

    requires jakarta.websocket;
    requires org.glassfish.tyrus.core;
    requires org.glassfish.tyrus.client;

    exports com.azure.messaging.webpubsub.client;

    requires transitive com.azure.core.test;
    requires com.azure.http.netty;
    requires com.azure.messaging.webpubsub;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;
    requires org.junit.jupiter.params;
}
