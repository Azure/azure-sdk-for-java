// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.util.Configuration;
import com.azure.messaging.webpubsub.WebPubSubServiceAsyncClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import com.azure.messaging.webpubsub.client.models.GroupDataMessage;
import com.azure.messaging.webpubsub.client.models.WebPubSubClientCredential;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataType;
import com.azure.messaging.webpubsub.models.GetClientAccessTokenOptions;
import com.azure.messaging.webpubsub.models.WebPubSubClientAccessToken;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

public final class EchoSample {

    public static void main(String[] args) throws Exception {

        // browser https://learn.microsoft.com/azure/azure-web-pubsub/quickstart-live-demo

        final String groupName = "bot";
        final String hubName = "hub1";
        final String userName = "bot1";

        // prepare the clientCredential
        WebPubSubServiceAsyncClient serverClient = new WebPubSubServiceClientBuilder()
            .connectionString(Configuration.getGlobalConfiguration().get("CONNECTION_STRING"))
            .hub(hubName)
            .buildAsyncClient();
        WebPubSubClientCredential clientCredential = new WebPubSubClientCredential(Mono.defer(() ->
            serverClient.getClientAccessToken(new GetClientAccessTokenOptions()
                    .setUserId(userName)
                    .addRole("webpubsub.joinLeaveGroup")
                    .addRole("webpubsub.sendToGroup"))
                .map(WebPubSubClientAccessToken::getUrl)));

        CompletableFuture<Void> clientStopped = new CompletableFuture<>();

        // create client
        WebPubSubClient client = new WebPubSubClientBuilder()
            .credential(clientCredential)
            .buildClient();

        // event handler
        client.addOnGroupMessageEventHandler(event -> {
            String group = event.getMessage().getGroup();
            if (groupName.equals(event.getMessage().getGroup())
                && !userName.equals(event.getMessage().getFromUserId())
                && (event.getMessage().getDataType() == WebPubSubDataType.TEXT || event.getMessage().getDataType() == WebPubSubDataType.JSON)) {

                String text = parseMessage(event.getMessage());
                if ("exit".equals(text)) {
                    // asked to exit
                    client.sendToGroup(group, "Goodbye.");
                    client.stop();
                } else {
                    // echo the message text
                    client.sendToGroup(group, "Received: " + text);
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

    private static String parseMessage(GroupDataMessage message) {
        return message.getDataType() == WebPubSubDataType.TEXT
            ? message.getData().toString()
            : message.getData().toObject(String.class);
    }
}
