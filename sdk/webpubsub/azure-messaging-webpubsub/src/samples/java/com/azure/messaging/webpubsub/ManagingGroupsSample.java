// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.Configuration;

public class ManagingGroupsSample {
    private static final String CONNECTION_STRING = Configuration.getGlobalConfiguration().get("WEB_PUB_SUB_CS");

    public static void main(String[] args) {
        // build a sync client
        WebPubSubServiceClient chatHub = new WebPubSubServiceClientBuilder()
            .connectionString(CONNECTION_STRING)
            .hub("chat")
            .buildClient();

        // adding and removing users
        chatHub.addUserToGroup("admin", "jogiles", new RequestOptions());
        chatHub.removeUserFromGroup("admin", "another_user", new RequestOptions());

        // adding and removing specific connections
        chatHub.addConnectionToGroup("admin", "Tn3XcrAbHI0OE36XvbWwige4ac096c1", new RequestOptions());
        chatHub.removeConnectionFromGroup("admin", "Tn3XcrAbHI0OE36XvbWwige4ac096c1", new RequestOptions());
    }
}
