// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import com.azure.core.credential.AzureKeyCredential;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS ARE USED TO EXTRACT
 * APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING LINE NUMBERS OF EXISTING CODE
 * SAMPLES.
 * <p>
 * Class containing code snippets that will be injected to README.md.
 */
public class ReadmeSamples {

    /**
     * Create a client using connection string.
     */
    public void createClientWithConnectionString() {
        WebPubSubServiceClient webPubSubServiceClient = new WebPubSubClientBuilder()
            .connectionString("{connection-string}")
            .hub("chat")
            .buildClient();
    }

    /**
     * Create a client using access key.
     */
    public void createClientWithKey() {
        WebPubSubServiceClient webPubSubServiceClient = new WebPubSubClientBuilder()
            .credential(new AzureKeyCredential("{access-key}"))
            .endpoint("<Insert endpoint from Azure Portal>")
            .hub("chat")
            .buildClient();
    }

    /**
     * Create a web pubsub group client using access key.
     */
    public void createGroupClientWithKey() {
        WebPubSubServiceClient webPubSubServiceClient = new WebPubSubClientBuilder()
            .credential(new AzureKeyCredential("{access-key}"))
            .hub("chat")
            .buildClient();
        WebPubSubGroup javaGroup = webPubSubServiceClient.getGroup("java");
    }

    /**
     * Send a message to everyone in the hub.
     */
    public void broadcastToAll() {
        WebPubSubServiceClient webPubSubServiceClient = new WebPubSubClientBuilder()
            .connectionString("{connection-string}")
            .hub("chat")
            .buildClient();

        webPubSubServiceClient.sendToAll("Hello world!");
    }

    /**
     * Send a message to everyone in a group.
     */
    public void broadcastToGroup() {
        WebPubSubServiceClient webPubSubServiceClient = new WebPubSubClientBuilder()
            .connectionString("{connection-string}")
            .hub("chat")
            .buildClient();

        WebPubSubGroup javaGroup = webPubSubServiceClient.getGroup("Java");
        javaGroup.sendToAll("Hello Java!");
    }

    /**
     * Send a message to a specific connection id.
     */
    public void sendToConnection() {
        WebPubSubServiceClient webPubSubServiceClient = new WebPubSubClientBuilder()
            .connectionString("{connection-string}")
            .hub("chat")
            .buildClient();

        webPubSubServiceClient.sendToConnection("myconnectionid", "Hello connection!");
    }

    /**
     * Send a message to a specific user.
     */
    public void sendToUser() {
        WebPubSubServiceClient webPubSubServiceClient = new WebPubSubClientBuilder()
            .connectionString("{connection-string}")
            .hub("chat")
            .buildClient();

        webPubSubServiceClient.sendToUser("Andy", "Hello Andy!");
    }
}
