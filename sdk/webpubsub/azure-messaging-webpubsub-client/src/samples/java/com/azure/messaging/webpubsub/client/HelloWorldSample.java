// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.messaging.webpubsub.WebPubSubServiceAsyncClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import com.azure.messaging.webpubsub.client.models.WebPubSubClientCredential;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataType;
import com.azure.messaging.webpubsub.client.models.WebPubSubResult;
import com.azure.messaging.webpubsub.models.GetClientAccessTokenOptions;
import com.azure.messaging.webpubsub.models.WebPubSubClientAccessToken;
import reactor.core.publisher.Mono;

public final class HelloWorldSample {

    public static void main(String[] args) throws Exception {

        final String groupName = "bot";
        final String hubName = "hub1";
        final String userName = "user1";

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
            BinaryData.fromString("hello world"), WebPubSubDataType.TEXT);

        // stop client
        client.stop();
    }
}
