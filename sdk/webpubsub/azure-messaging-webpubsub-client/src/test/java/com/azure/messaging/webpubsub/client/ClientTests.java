// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.messaging.webpubsub.WebPubSubServiceClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import com.azure.messaging.webpubsub.models.GetClientAccessTokenOptions;
import com.azure.messaging.webpubsub.models.WebPubSubClientAccessToken;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

public class ClientTests {

    public static void main(String[] args) throws Exception {
        WebPubSubServiceClient client = new WebPubSubServiceClientBuilder()
            .connectionString(Configuration.getGlobalConfiguration().get("CONNECTION_STRING"))
            .hub("test_hub")
            .buildClient();

        Mono<WebPubSubClientAccessToken> accessToken = Mono.just(client.getClientAccessToken(new GetClientAccessTokenOptions()
            .setUserId("weidxu")
            .addRole("webpubsub.joinLeaveGroup")
            .addRole("webpubsub.sendToGroup"))).subscribeOn(Schedulers.boundedElastic());

        WebPubSubAsyncClient asyncClient = new WebPubSubClientBuilder()
            .credential(new WebPubSubClientCredential(accessToken
                .map(WebPubSubClientAccessToken::getUrl)))
            .buildAsyncClient();

        asyncClient.receiveGroupMessages().doOnNext(m -> {
            System.out.println("data: " + m.getData());
        }).subscribe();

        asyncClient.start().block();

        long ackId = 0;

        asyncClient.joinGroup("group1", ++ackId).block();

        WebPubSubResult result = asyncClient.sendMessageToGroup("group1",
            BinaryData.fromString("abc"), WebPubSubDataType.TEXT,
            ++ackId, false, false).block();
        if (result != null) {
            System.out.println("result: " + result.getAckId());
        }

        asyncClient.leaveGroup("group1", ++ackId).block();

        result = asyncClient.sendMessageToGroup("group1",
            BinaryData.fromObject(Map.of("hello", "world")), WebPubSubDataType.JSON,
            ++ackId, false, false).block();
        if (result != null) {
            System.out.println("result: " + result.getAckId());
        }

        result = asyncClient.sendMessageToGroup("group1",
            BinaryData.fromString("binary"), WebPubSubDataType.BINARY,
            ++ackId, false, false).block();
        if (result != null) {
            System.out.println("result: " + result.getAckId());
        }

        asyncClient.close().block();

        Thread.sleep(5 * 1000);
    }
}
