// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.test.annotation.DoNotRecord;
import com.azure.messaging.webpubsub.client.implementation.WebPubSubClientState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class ClientTests extends TestBase {

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testClientState() {
        WebPubSubAsyncClient asyncClient = getClientBuilder().buildAsyncClient();

        Assertions.assertEquals(WebPubSubClientState.STOPPED, asyncClient.getClientState());

        Mono<Void> startMono = asyncClient.start().doOnSuccess(ignored -> {
            Assertions.assertEquals(WebPubSubClientState.CONNECTED, asyncClient.getClientState());
        });
        // test transient state of CONNECTING
        Mono<Void> verifyMono = Mono.delay(Duration.ofMillis(10)).then().doOnSuccess(ignored -> {
            Assertions.assertEquals(WebPubSubClientState.CONNECTING, asyncClient.getClientState());
        });
        startMono.and(verifyMono).block();

        asyncClient.stop().doOnSuccess(ignored -> {
            Assertions.assertEquals(WebPubSubClientState.STOPPED, asyncClient.getClientState());
        }).block();

        asyncClient.start().block();
        Assertions.assertEquals(WebPubSubClientState.CONNECTED, asyncClient.getClientState());
        asyncClient.stop().block();
        Assertions.assertEquals(WebPubSubClientState.STOPPED, asyncClient.getClientState());
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    @Order(1000)    // last
    public void testClosed() {
        WebPubSubClient client = getClient();

        Assertions.assertEquals(WebPubSubClientState.STOPPED, client.getClientState());

        client.close();
        Assertions.assertEquals(WebPubSubClientState.CLOSED, client.getClientState());

        Assertions.assertThrows(IllegalStateException.class, () -> client.joinGroup("group"));
    }
}
