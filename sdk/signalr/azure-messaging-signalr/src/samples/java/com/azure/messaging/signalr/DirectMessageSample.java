// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.signalr;

import com.azure.core.util.Configuration;

public class DirectMessageSample {
    private static final String CONNECTION_STRING = Configuration.getGlobalConfiguration().get("SIGNALR_CS");

    public static void main(String[] args) {
        // build a sync client
        SignalRClient chatHub = new SignalRClientBuilder()
            .connectionString(CONNECTION_STRING)
            .hub("chat")
            .buildClient();

        // send a text message directly to a user
        chatHub.sendToUser("jogiles", "Hi there!");

        // send a text message to a specific connection
        chatHub.sendToUser("Tn3XcrAbHI0OE36XvbWwige4ac096c1", "Hi there!");
    }
}
