// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.messaging.webpubsub.models.WebPubSubContentType;
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
        WebPubSubServiceClient webPubSubServiceClient = new WebPubSubServiceClientBuilder()
            .connectionString("{connection-string}")
            .hub("chat")
            .buildClient();
    }

    /**
     * Create a client using access key.
     */
    public void createClientWithKey() {
        WebPubSubServiceClient webPubSubServiceClient = new WebPubSubServiceClientBuilder()
            .credential(new AzureKeyCredential("{access-key}"))
            .endpoint("<Insert endpoint from Azure Portal>")
            .hub("chat")
            .buildClient();
    }

    /**
     * Send a message to everyone in the hub.
     */
    public void broadcastToAll() {
        WebPubSubServiceClient webPubSubServiceClient = new WebPubSubServiceClientBuilder()
            .connectionString("{connection-string}")
            .hub("chat")
            .buildClient();

        webPubSubServiceClient.sendToAll("Hello world!", WebPubSubContentType.TEXT_PLAIN);
    }

    /**
     * Send a message to everyone in a group.
     */
    public void broadcastToGroup() {
        WebPubSubServiceClient webPubSubServiceClient = new WebPubSubServiceClientBuilder()
            .connectionString("{connection-string}")
            .hub("chat")
            .buildClient();

        webPubSubServiceClient.sendToGroup("java", "Hello Java!", WebPubSubContentType.TEXT_PLAIN);
    }

    /**
     * Send a message to a specific connection id.
     */
    public void sendToConnection() {
        WebPubSubServiceClient webPubSubServiceClient = new WebPubSubServiceClientBuilder()
            .connectionString("{connection-string}")
            .hub("chat")
            .buildClient();

        webPubSubServiceClient.sendToConnection("myconnectionid", "Hello connection!", WebPubSubContentType.TEXT_PLAIN);
    }

    /**
     * Send a message to a specific user.
     */
    public void sendToUser() {
        WebPubSubServiceClient webPubSubServiceClient = new WebPubSubServiceClientBuilder()
            .connectionString("{connection-string}")
            .hub("chat")
            .buildClient();

        webPubSubServiceClient.sendToUser("Andy", "Hello Andy!", WebPubSubContentType.TEXT_PLAIN);
    }
}
