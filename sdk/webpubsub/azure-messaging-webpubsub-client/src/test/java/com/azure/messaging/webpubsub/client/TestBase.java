// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.util.Configuration;
import com.azure.messaging.webpubsub.WebPubSubServiceAsyncClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import com.azure.messaging.webpubsub.models.GetClientAccessTokenOptions;
import com.azure.messaging.webpubsub.models.WebPubSubClientAccessToken;
import reactor.core.publisher.Mono;

public class TestBase {

    protected static WebPubSubClientBuilder getClientBuilder() {
        WebPubSubServiceAsyncClient client = new WebPubSubServiceClientBuilder()
            .connectionString(Configuration.getGlobalConfiguration().get("CONNECTION_STRING"))
            .hub("test_hub")
            .buildAsyncClient();

        Mono<WebPubSubClientAccessToken> accessToken = client.getClientAccessToken(new GetClientAccessTokenOptions()
            .setUserId("weidxu")
            .addRole("webpubsub.joinLeaveGroup")
            .addRole("webpubsub.sendToGroup"));

        // client builder
        return new WebPubSubClientBuilder()
            .credential(new WebPubSubClientCredential(accessToken
                .map(WebPubSubClientAccessToken::getUrl)));
    }

    protected static WebPubSubClient getClient() {
        return getClientBuilder().buildClient();
    }
}
