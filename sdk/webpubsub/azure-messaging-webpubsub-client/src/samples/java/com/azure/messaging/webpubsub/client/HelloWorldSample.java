// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.messaging.webpubsub.WebPubSubServiceClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import com.azure.messaging.webpubsub.client.models.WebPubSubClientCredential;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataFormat;
import com.azure.messaging.webpubsub.client.models.WebPubSubResult;
import com.azure.messaging.webpubsub.models.GetClientAccessTokenOptions;

public final class HelloWorldSample {

    public static void main(String[] args) throws Exception {

        final String groupName = "bot";
        final String hubName = "hub1";
        final String userName = "user1";

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

        // create client
        WebPubSubClient client = new WebPubSubClientBuilder()
            .credential(clientCredential)
            .buildClient();

        // event handler
        client.addOnGroupMessageEventHandler(event -> {
            System.out.println("Received group message from " + event.getFromUserId() + ": "
                + event.getData().toString());
        });
        client.addOnServerMessageEventHandler(event -> {
            System.out.println("Received server message: "
                + event.getData().toString());
        });

        // start client
        client.start();

        // join group
        WebPubSubResult result = client.joinGroup(groupName);

        // send message to group
        result = client.sendToGroup(groupName, "hello world");

        // send custom event to server
        result = client.sendEvent("testEvent",
            BinaryData.fromString("hello world"), WebPubSubDataFormat.TEXT);

        // stop client
        client.stop();
    }
}
