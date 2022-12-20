// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;

public class BroadcastingWithFilterSample {
    private static final String CONNECTION_STRING = Configuration.getGlobalConfiguration().get("WEB_PUB_SUB_CS");

    public static void main(String[] args) {
        // build a sync client
        WebPubSubServiceClient chatHub = new WebPubSubServiceClientBuilder()
            .connectionString(CONNECTION_STRING)
            .hub("chat")
            .buildClient();

        // send a text message to the entire hub with a filter on userId
        chatHub.sendToAllWithResponse(
            BinaryData.fromString("Hello World - Broadcast test!"),
            new RequestOptions().setHeader("Content-Type", "text/plain")
                .addQueryParam("filter", "userId ne 'user1'"));

        // send a text message to the entire hub with another filter on group
        chatHub.sendToAllWithResponse(
            BinaryData.fromString("Hello World - Broadcast test!"),
            new RequestOptions().setHeader("Content-Type", "text/plain")
                .addQueryParam("filter", "'GroupA' in groups and not({'GroupB'} in groups)"));
    }
}
