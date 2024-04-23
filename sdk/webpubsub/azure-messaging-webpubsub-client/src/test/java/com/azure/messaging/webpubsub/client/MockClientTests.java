// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.messaging.webpubsub.client.implementation.models.ConnectedMessage;
import com.azure.messaging.webpubsub.client.implementation.WebPubSubClientState;
import com.azure.messaging.webpubsub.client.implementation.websocket.WebSocketClient;
import com.azure.messaging.webpubsub.client.implementation.websocket.CloseReason;
import com.azure.messaging.webpubsub.client.implementation.websocket.WebSocketSession;
import com.azure.messaging.webpubsub.client.models.ConnectFailedException;
import com.azure.messaging.webpubsub.client.models.ConnectedEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class MockClientTests {

    private static final Duration SMALL_DELAY = Duration.ofMillis(10);

    @Test
    public void testConnectFailure() {
        ArgumentCaptor<Consumer<Object>> messageCaptor = ArgumentCaptor.forClass(Consumer.class);
        ArgumentCaptor<Consumer<WebSocketSession>> openCaptor = ArgumentCaptor.forClass(Consumer.class);
        ArgumentCaptor<Consumer<CloseReason>> closeCaptor = ArgumentCaptor.forClass(Consumer.class);

        WebSocketClient mockWsClient = Mockito.mock(WebSocketClient.class);
        Mockito.when(mockWsClient.connectToServer(Mockito.any(), Mockito.any(), Mockito.any(),
                messageCaptor.capture(), openCaptor.capture(), closeCaptor.capture()))
            .thenThrow(new ConnectFailedException("mock error", new IllegalArgumentException()));

        WebPubSubClientBuilder builder = new WebPubSubClientBuilder();
        builder.webSocketClient = mockWsClient;
        WebPubSubClient client = builder
            .clientAccessUrl("mock")
            .buildClient();

        Assertions.assertThrows(ConnectFailedException.class, () -> client.start());
    }

    @Test
    public void testConnect() throws InterruptedException {
        ArgumentCaptor<Consumer<Object>> messageCaptor = ArgumentCaptor.forClass(Consumer.class);
        ArgumentCaptor<Consumer<WebSocketSession>> openCaptor = ArgumentCaptor.forClass(Consumer.class);
        ArgumentCaptor<Consumer<CloseReason>> closeCaptor = ArgumentCaptor.forClass(Consumer.class);

        WebSocketSession mockWsSession = Mockito.mock(WebSocketSession.class);
        WebSocketClient mockWsClient = Mockito.mock(WebSocketClient.class);
        Mockito.when(mockWsClient.connectToServer(Mockito.any(), Mockito.any(), Mockito.any(),
                messageCaptor.capture(), openCaptor.capture(), closeCaptor.capture()))
            .thenAnswer(invocation -> {
                openCaptor.getValue().accept(mockWsSession);
                sendConnectedEvent(messageCaptor.getValue());
                return mockWsSession;
            });

        CountDownLatch latch = new CountDownLatch(1);
        List<ConnectedEvent> events = new ArrayList<>();

        WebPubSubClientBuilder builder = new WebPubSubClientBuilder();
        builder.webSocketClient = mockWsClient;
        WebPubSubClient client = builder
            .clientAccessUrl("mock")
            .buildClient();

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

    private static void sendConnectedEvent(Consumer<Object> messageHandler) {
        Mono.delay(SMALL_DELAY)
            .then(Mono.fromRunnable(() -> {
                messageHandler.accept(new ConnectedMessage("mock_connection_id"));
            }).subscribeOn(Schedulers.boundedElastic()))
            .subscribe();
    }
}
