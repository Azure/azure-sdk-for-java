// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.messaging.webpubsub.client.implementation.WebPubSubClientState;
import com.azure.messaging.webpubsub.client.implementation.models.ConnectedMessage;
import com.azure.messaging.webpubsub.client.implementation.models.WebPubSubMessage;
import com.azure.messaging.webpubsub.client.implementation.websocket.SendResult;
import com.azure.messaging.webpubsub.client.implementation.websocket.WebSocketClient;
import com.azure.messaging.webpubsub.client.implementation.websocket.WebSocketSession;
import com.azure.messaging.webpubsub.client.models.ConnectFailedException;
import com.azure.messaging.webpubsub.client.models.ConnectedEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MockClientTests {

    private static final Duration SMALL_DELAY = Duration.ofMillis(10);

    @Test
    public void testConnectFailure() {
        WebSocketClient mockWsClient = (cec, path, loggerReference, messageHandler, openHandler, closeHandler) -> {
            throw new ConnectFailedException("mock error", new IllegalArgumentException());
        };

        WebPubSubClientBuilder builder = new WebPubSubClientBuilder();
        builder.webSocketClient = mockWsClient;
        WebPubSubClient client = builder.clientAccessUrl("mock").buildClient();

        Assertions.assertThrows(ConnectFailedException.class, client::start);
    }

    @Test
    public void testConnect() throws InterruptedException {
        WebSocketSession mockWsSession = new MockWebSocketSession();
        WebSocketClient mockWsClient = (cec, path, loggerReference, messageHandler, openHandler, closeHandler) -> {
            openHandler.accept(mockWsSession);
            sendConnectedEvent(messageHandler);
            return mockWsSession;
        };

        CountDownLatch latch = new CountDownLatch(1);
        List<ConnectedEvent> events = new ArrayList<>();

        WebPubSubClientBuilder builder = new WebPubSubClientBuilder();
        builder.webSocketClient = mockWsClient;
        WebPubSubClient client = builder.clientAccessUrl("mock").buildClient();

        client.addOnConnectedEventHandler(event -> {
            events.add(event);
            latch.countDown();
        });

        Assertions.assertEquals(WebPubSubClientState.STOPPED, client.getClientState());

        client.start();

        Assertions.assertEquals(WebPubSubClientState.CONNECTED, client.getClientState());

        latch.await(1, TimeUnit.SECONDS);
        Assertions.assertEquals(1, events.size());
    }

    private static void sendConnectedEvent(Consumer<WebPubSubMessage> messageHandler) {
        Mono.delay(SMALL_DELAY)
            .then(Mono.fromRunnable(() -> messageHandler.accept(new ConnectedMessage("mock_connection_id")))
                .subscribeOn(Schedulers.boundedElastic()))
            .subscribe();
    }

    private static final class MockWebSocketSession implements WebSocketSession {
        @Override
        public boolean isOpen() {
            return false;
        }

        @Override
        public void sendObjectAsync(Object data, Consumer<SendResult> handler) {

        }

        @Override
        public void close() {

        }

        @Override
        public void sendTextAsync(String text, Consumer<SendResult> handler) {

        }

        @Override
        public void closeSocket() {

        }
    }
}
