// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import com.azure.core.util.Configuration;

public class DirectMessageSample {
    private static final String CONNECTION_STRING = Configuration.getGlobalConfiguration().get("WEB_PUB_SUB_CS");

    public static void main(String[] args) {
        // build a sync client
        WebPubSubServiceClient chatHub = new WebPubSubServiceClientBuilder()
            .connectionString(CONNECTION_STRING)
            .hub("chat")
            .buildClient();

        // send a text message directly to a user
        chatHub.sendToUser("jogiles", "Hi there!", "text/plain");

        // send a text message to a specific connection
        chatHub.sendToConnection("Tn3XcrAbHI0OE36XvbWwige4ac096c1", "Hi there!", "text/plain");
    }
}
