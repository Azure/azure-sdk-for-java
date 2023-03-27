// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.util.Configuration;
import com.azure.messaging.webpubsub.WebPubSubServiceAsyncClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import com.azure.messaging.webpubsub.client.models.WebPubSubClientCredential;
import com.azure.messaging.webpubsub.models.GetClientAccessTokenOptions;
import com.azure.messaging.webpubsub.models.WebPubSubClientAccessToken;
import reactor.core.publisher.Mono;

public class TestBase extends com.azure.core.test.TestBase {

    protected static WebPubSubClientBuilder getClientBuilder() {
        return getClientBuilder("user1");
    }

    protected static WebPubSubClientBuilder getClientBuilder(String userId) {
        WebPubSubServiceAsyncClient client = new WebPubSubServiceClientBuilder()
            .connectionString(Configuration.getGlobalConfiguration().get("CONNECTION_STRING"))
            .hub("hub1")
            .buildAsyncClient();

        Mono<WebPubSubClientAccessToken> accessToken = client.getClientAccessToken(new GetClientAccessTokenOptions()
            .setUserId(userId)
            .addRole("webpubsub.joinLeaveGroup")
            .addRole("webpubsub.sendToGroup"));

        // client builder
        return new WebPubSubClientBuilder()
            .credential(new WebPubSubClientCredential(Mono.defer(() -> accessToken.map(WebPubSubClientAccessToken::getUrl))));
    }

    protected static WebPubSubClient getClient() {
        return getClientBuilder().buildClient();
    }
}
