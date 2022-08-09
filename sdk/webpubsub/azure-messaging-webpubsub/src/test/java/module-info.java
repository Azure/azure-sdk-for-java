// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

open module com.azure.messaging.webpubsub {
    requires com.azure.core;
    requires com.azure.core.test;
    requires com.azure.identity;

    requires java.desktop;
    requires javax.websocket.api;

    requires com.nimbusds.jose.jwt;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;
    requires org.junit.jupiter.params;
}
