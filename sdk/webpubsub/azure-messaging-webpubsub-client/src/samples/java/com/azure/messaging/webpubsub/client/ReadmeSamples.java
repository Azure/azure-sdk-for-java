// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.messaging.webpubsub.WebPubSubServiceClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import com.azure.messaging.webpubsub.client.models.SendMessageFailedException;
import com.azure.messaging.webpubsub.client.models.WebPubSubClientCredential;
import com.azure.messaging.webpubsub.client.models.WebPubSubProtocolType;
import com.azure.messaging.webpubsub.models.GetClientAccessTokenOptions;

public final class ReadmeSamples {

    public void createClientFromUrl() {
        // BEGIN: readme-sample-createClientFromUrl
        WebPubSubClient client = new WebPubSubClientBuilder()
            .clientAccessUrl("<client-access-url>")
            .buildClient();
        // END: readme-sample-createClientFromUrl
    }

    public void createClientFromCredential() {
        // BEGIN: readme-sample-createClientFromCredential
        // WebPubSubServiceClient is from com.azure:azure-messaging-webpubsub
        // create WebPubSub service client
        WebPubSubServiceClient serverClient = new WebPubSubServiceClientBuilder()
            .connectionString("<connection-string>")
            .hub("<hub>>")
            .buildClient();

        // wrap WebPubSubServiceClient.getClientAccessToken as WebPubSubClientCredential
        WebPubSubClientCredential clientCredential = new WebPubSubClientCredential(
            () -> serverClient.getClientAccessToken(new GetClientAccessTokenOptions()
                    .setUserId("<user-name>")
                    .addRole("webpubsub.joinLeaveGroup")
                    .addRole("webpubsub.sendToGroup"))
                .getUrl());

        // create WebPubSub client
        WebPubSubClient client = new WebPubSubClientBuilder()
            .credential(clientCredential)
            .buildClient();
        // END: readme-sample-createClientFromCredential
    }

    public void createClientWithProtocol() {
        // BEGIN: readme-sample-createClientWithProtocol
        WebPubSubClient client = new WebPubSubClientBuilder()
            .clientAccessUrl("<client-access-url>")
            .protocol(WebPubSubProtocolType.JSON_PROTOCOL)
            .buildClient();
        // END: readme-sample-createClientWithProtocol
    }

    public void listenMessages() {
        WebPubSubClient client = createMockClient();

        // BEGIN: readme-sample-listenMessages
        client.addOnGroupMessageEventHandler(event -> {
            System.out.println("Received group message from " + event.getFromUserId() + ": "
                + event.getData().toString());
        });
        client.addOnServerMessageEventHandler(event -> {
            System.out.println("Received server message: "
                + event.getData().toString());
        });
        // END: readme-sample-listenMessages
    }

    public void listenEvent() {
        WebPubSubClient client = createMockClient();

        // BEGIN: readme-sample-listenEvent
        client.addOnConnectedEventHandler(event -> {
            System.out.println("Connection is connected: " + event.getConnectionId());
        });
        client.addOnDisconnectedEventHandler(event -> {
            System.out.println("Connection is disconnected");
        });
        client.addOnStoppedEventHandler(event -> {
            System.out.println("Client is stopped");
        });
        // END: readme-sample-listenEvent
    }

    public void sendAndRetry() {
        WebPubSubClient client = createMockClient();

        // BEGIN: readme-sample-sendAndRetry
        try {
            client.joinGroup("testGroup");
        } catch (SendMessageFailedException e) {
            if (e.getAckId() != null) {
                client.joinGroup("testGroup", e.getAckId());
            }
        }
        // END: readme-sample-sendAndRetry
    }

    private WebPubSubClient createMockClient() {
        return new WebPubSubClientBuilder().clientAccessUrl("client-access-url").buildClient();
    }
}
