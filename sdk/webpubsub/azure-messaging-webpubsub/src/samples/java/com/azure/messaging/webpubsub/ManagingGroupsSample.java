// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import com.azure.core.util.Configuration;

public class ManagingGroupsSample {
    private static final String CONNECTION_STRING = Configuration.getGlobalConfiguration().get("WEB_PUB_SUB_CS");

    public static void main(String[] args) {
        // build a sync client
        WebPubSubClient chatHub = new WebPubSubClientBuilder()
            .connectionString(CONNECTION_STRING)
            .hub("chat")
            .buildClient();

        WebPubSubGroupClient adminGroup = chatHub.getGroupClient("admin");

        // adding and removing users
        adminGroup.addUser("jogiles");
        adminGroup.userExists("jogiles");
        adminGroup.removeUser("another_user");

        // adding and removing specific connections
        adminGroup.addConnection("Tn3XcrAbHI0OE36XvbWwige4ac096c1");
        adminGroup.removeConnection("Tn3XcrAbHI0OE36XvbWwige4ac096c1");
    }
}
