// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import com.azure.core.util.Configuration;
import com.azure.messaging.webpubsub.models.WebPubSubContentType;

public class BroadcastingSample {
    private static final String CONNECTION_STRING = Configuration.getGlobalConfiguration().get("WEB_PUB_SUB_CS");

    public static void main(String[] args) {
        // build a sync client
        WebPubSubServiceClient chatHub = new WebPubSubClientBuilder()
            .connectionString(CONNECTION_STRING)
            .hub("chat")
            .buildClient();

        // send a text message to the entire hub
        chatHub.sendToAll("{\"message\": \"Hello world!\"}");

        // send a text message to a particular group
        WebPubSubGroup adminGroup = chatHub.getGroup("admin");
        adminGroup.sendToAll("Hi admins!", WebPubSubContentType.TEXT_PLAIN);

        // send binary data to the entire hub
        byte[] data = new byte[10];
        for (int i = 0; i < 10; i++) {
            data[i] = (byte) i;
        }
        chatHub.sendToAll(data);
    }
}
