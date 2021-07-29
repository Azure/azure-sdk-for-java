// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;

public class ManagingGroupsSample {
    private static final String CONNECTION_STRING = Configuration.getGlobalConfiguration().get("WEB_PUB_SUB_CS");

    public static void main(String[] args) {
        // build a sync client
        WebPubSubServiceClient chatHub = new WebPubSubServiceClientBuilder()
            .connectionString(CONNECTION_STRING)
            .hub("chat")
            .buildClient();

        // adding and removing users
        chatHub.addUserToGroupWithResponse("admin", "jogiles", new RequestOptions(), Context.NONE);
        chatHub.removeUserFromGroupWithResponse("admin", "another_user", new RequestOptions(), Context.NONE);

        // adding and removing specific connections
        chatHub.addConnectionToGroupWithResponse("admin", "Tn3XcrAbHI0OE36XvbWwige4ac096c1", new RequestOptions(),
                Context.NONE);
        chatHub.removeConnectionFromGroupWithResponse("admin", "Tn3XcrAbHI0OE36XvbWwige4ac096c1",
                new RequestOptions(), Context.NONE);
    }
}
