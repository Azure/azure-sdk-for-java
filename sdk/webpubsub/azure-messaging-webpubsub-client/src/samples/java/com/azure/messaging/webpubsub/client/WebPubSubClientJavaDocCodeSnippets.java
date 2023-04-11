// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.util.BinaryData;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataType;

import java.util.HashMap;
import java.util.Map;

public final class WebPubSubClientJavaDocCodeSnippets {

    private final WebPubSubClient client = new WebPubSubClientBuilder().buildClient();

    public void joinAndSendGroupMessageAsText() {
        // BEGIN: com.azure.messaging.webpubsub.client.WebPubSubClient.sendToGroup.text
        client.start();
        client.sendToGroup("message-group", "hello world");
        // END: com.azure.messaging.webpubsub.client.WebPubSubClient.sendToGroup.text
    }

    public void joinAndSendGroupMessageAsJson() {
        // BEGIN: com.azure.messaging.webpubsub.client.WebPubSubClient.sendToGroup.json
        client.start();
        // it can be any class instance that can be serialized to JSON
        Map<String, String> jsonObject = new HashMap<>();
        jsonObject.put("name", "john");
        client.sendToGroup("message-group", BinaryData.fromObject(jsonObject), WebPubSubDataType.BINARY);
        // END: com.azure.messaging.webpubsub.client.WebPubSubClient.sendToGroup.json
    }

    public void addOnStoppedEventHandler() {
        // BEGIN: com.azure.messaging.webpubsub.client.WebPubSubClient.addOnStoppedEventHandler
        client.addOnStoppedEventHandler(event -> {
            System.out.println("Client is stopped");
        });
        // END: com.azure.messaging.webpubsub.client.WebPubSubClient.addOnStoppedEventHandler
    }
}
