// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module azure.messaging.webpubsub.client {
    requires transitive com.azure.core;

    requires jakarta.websocket;
    requires org.glassfish.tyrus.core;
    requires org.glassfish.tyrus.client;

    exports com.azure.messaging.webpubsub.client;
    exports com.azure.messaging.webpubsub.client.models;
    exports com.azure.messaging.webpubsub.client.protocol;
    exports com.azure.messaging.webpubsub.client.exception;

    opens com.azure.messaging.webpubsub.client.implementation to
        com.azure.core,
        com.fasterxml.jackson.databind;
}
