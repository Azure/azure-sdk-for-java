// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.messaging.webpubsub.models.WebPubSubContentType;

public class BroadcastingSample {
    private static final String CONNECTION_STRING = Configuration.getGlobalConfiguration().get("WEB_PUB_SUB_CS");

    public static void main(String[] args) {
        // build a sync client
        WebPubSubServiceClient chatHub = new WebPubSubServiceClientBuilder()
            .connectionString(CONNECTION_STRING)
            .hub("chat")
            .buildClient();

        // send a text message to the entire hub
        chatHub.sendToAll("{\"message\": \"Hello world!\"}", WebPubSubContentType.APPLICATION_JSON);

        // send a text message to a particular group
        chatHub.sendToGroup("admin", "Hi admins!", WebPubSubContentType.TEXT_PLAIN);

        // send binary data to the entire hub
        byte[] data = new byte[10];
        for (int i = 0; i < 10; i++) {
            data[i] = (byte) i;
        }
        chatHub.sendToAllWithResponse(BinaryData.fromBytes(data), new RequestOptions()
                .addRequestCallback(request -> request.getHeaders().set("Content-Type", "application/octet-stream")),
                Context.NONE);
    }
}
