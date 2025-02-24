// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.util.Configuration;
import com.azure.messaging.webpubsub.WebPubSubServiceClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import com.azure.messaging.webpubsub.client.models.GroupMessageEvent;
import com.azure.messaging.webpubsub.client.models.WebPubSubClientCredential;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataFormat;
import com.azure.messaging.webpubsub.models.GetClientAccessTokenOptions;

import java.util.concurrent.CompletableFuture;

public final class EchoSample {

    public static void main(String[] args) throws Exception {

        // browser https://learn.microsoft.com/azure/azure-web-pubsub/quickstart-live-demo

        final String groupName = "bot";
        final String hubName = "hub1";
        final String userName = "bot1";

        // prepare the clientCredential
        WebPubSubServiceClient serverClient = new WebPubSubServiceClientBuilder()
            .connectionString(Configuration.getGlobalConfiguration().get("CONNECTION_STRING"))
            .hub(hubName)
            .buildClient();
        WebPubSubClientCredential clientCredential = new WebPubSubClientCredential(
            () -> serverClient.getClientAccessToken(new GetClientAccessTokenOptions()
                    .setUserId(userName)
                    .addRole("webpubsub.joinLeaveGroup")
                    .addRole("webpubsub.sendToGroup"))
                .getUrl());

        CompletableFuture<Void> clientStopped = new CompletableFuture<>();

        // create client
        WebPubSubClient client = new WebPubSubClientBuilder()
            .credential(clientCredential)
            .buildClient();

        // event handler
        client.addOnGroupMessageEventHandler(event -> {
            String group = event.getGroup();
            if (groupName.equals(event.getGroup())
                && (event.getDataFormat() == WebPubSubDataFormat.TEXT || event.getDataFormat() == WebPubSubDataFormat.JSON)) {

                String text = parseMessageEvent(event);
                if ("exit".equals(text)) {
                    // asked to exit
                    client.sendToGroup(group, "Goodbye.");
                    client.stop();
                } else {
                    // echo the message text
                    System.out.println("Received: " + text);
                }
            }
        });
        client.addOnStoppedEventHandler(event -> clientStopped.complete(null));

        // start client, join group and send "Hello"
        client.start();
        client.joinGroup(groupName);
        client.sendToGroup(groupName, "Hello.");

        // wait for client to stop
        clientStopped.get();
    }

    private static String parseMessageEvent(GroupMessageEvent event) {
        if (event.getDataFormat() == WebPubSubDataFormat.TEXT) {
            return event.getData().toString();
        }
        if (event.getDataFormat() == WebPubSubDataFormat.JSON) {
            return event.getData().toString();
        }

        return "unknown format: " + event.getDataFormat();
    }
}
