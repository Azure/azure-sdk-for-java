// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.messaging.webpubsub.models.WebPubSubContentType;
/**
 * Class containing code snippets that will be injected to README.md.
 */
public class ReadmeSamples {

    /**
     * Create a client using connection string.
     */
    public void createClientWithConnectionString() {
        // BEGIN: readme-sample-createClientWithConnectionString
        WebPubSubServiceClient webPubSubServiceClient = new WebPubSubServiceClientBuilder()
            .connectionString("{connection-string}")
            .hub("chat")
            .buildClient();
        // END: readme-sample-createClientWithConnectionString
    }

    /**
     * Create a client using access key.
     */
    public void createClientWithKey() {
        // BEGIN: readme-sample-createClientWithKey
        WebPubSubServiceClient webPubSubServiceClient = new WebPubSubServiceClientBuilder()
            .credential(new AzureKeyCredential("{access-key}"))
            .endpoint("<Insert endpoint from Azure Portal>")
            .hub("chat")
            .buildClient();
        // END: readme-sample-createClientWithKey
    }

    /**
     * Send a message to everyone in the hub.
     */
    public void broadcastToAll() {
        WebPubSubServiceClient webPubSubServiceClient = new WebPubSubServiceClientBuilder()
            .connectionString("{connection-string}")
            .hub("chat")
            .buildClient();

        // BEGIN: readme-sample-broadcastToAll
        webPubSubServiceClient.sendToAll("Hello world!", WebPubSubContentType.TEXT_PLAIN);
        // END: readme-sample-broadcastToAll
    }

    /**
     * Send a message to everyone in a group.
     */
    public void broadcastToGroup() {
        WebPubSubServiceClient webPubSubServiceClient = new WebPubSubServiceClientBuilder()
            .connectionString("{connection-string}")
            .hub("chat")
            .buildClient();

        // BEGIN: readme-sample-broadcastToGroup
        webPubSubServiceClient.sendToGroup("java", "Hello Java!", WebPubSubContentType.TEXT_PLAIN);
        // END: readme-sample-broadcastToGroup
    }

    /**
     * Send a message to a specific connection id.
     */
    public void sendToConnection() {
        WebPubSubServiceClient webPubSubServiceClient = new WebPubSubServiceClientBuilder()
            .connectionString("{connection-string}")
            .hub("chat")
            .buildClient();

        // BEGIN: readme-sample-sendToConnection
        webPubSubServiceClient.sendToConnection("myconnectionid", "Hello connection!", WebPubSubContentType.TEXT_PLAIN);
        // END: readme-sample-sendToConnection
    }

    /**
     * Send a message to a specific user.
     */
    public void sendToUser() {
        WebPubSubServiceClient webPubSubServiceClient = new WebPubSubServiceClientBuilder()
            .connectionString("{connection-string}")
            .hub("chat")
            .buildClient();

        // BEGIN: readme-sample-sendToUser
        webPubSubServiceClient.sendToUser("Andy", "Hello Andy!", WebPubSubContentType.TEXT_PLAIN);
        // END: readme-sample-sendToUser
    }
}
