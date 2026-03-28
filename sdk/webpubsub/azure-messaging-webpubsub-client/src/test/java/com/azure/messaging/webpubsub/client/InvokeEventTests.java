// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.util.BinaryData;
import com.azure.messaging.webpubsub.client.implementation.MessageDecoder;
import com.azure.messaging.webpubsub.client.implementation.models.ConnectedMessage;
import com.azure.messaging.webpubsub.client.implementation.models.InvokeResponseMessage;
import com.azure.messaging.webpubsub.client.implementation.models.WebPubSubMessage;
import com.azure.messaging.webpubsub.client.implementation.websocket.SendResult;
import com.azure.messaging.webpubsub.client.implementation.websocket.WebSocketClient;
import com.azure.messaging.webpubsub.client.implementation.websocket.WebSocketSession;
import com.azure.messaging.webpubsub.client.models.AckResponseError;
import com.azure.messaging.webpubsub.client.models.InvocationException;
import com.azure.messaging.webpubsub.client.models.InvokeEventOptions;
import com.azure.messaging.webpubsub.client.models.InvokeEventResult;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class InvokeEventTests {

    private static final Duration SMALL_DELAY = Duration.ofMillis(10);

    @Test
    public void testInvokeEventSuccess() throws InterruptedException {
        final List<String> sentMessages = new ArrayList<>();
        final AtomicReference<Consumer<WebPubSubMessage>> messageHandlerRef = new AtomicReference<>();
        final CountDownLatch connectedLatch = new CountDownLatch(1);
        final CountDownLatch sendLatch = new CountDownLatch(1);

        WebSocketSession mockWsSession = new MockWebSocketSession() {
            @Override
            public void sendObjectAsync(Object data, Consumer<SendResult> handler) {
                sentMessages.add(data.toString());
                handler.accept(new SendResult());
                sendLatch.countDown();
            }
        };

        WebSocketClient mockWsClient = (cec, path, loggerReference, messageHandler, openHandler, closeHandler) -> {
            messageHandlerRef.set(messageHandler);
            openHandler.accept(mockWsSession);
            sendConnectedEvent(messageHandler);
            return mockWsSession;
        };

        WebPubSubClientBuilder builder = new WebPubSubClientBuilder();
        builder.webSocketClient = mockWsClient;
        WebPubSubClient client = builder.clientAccessUrl("mock").buildClient();
        client.addOnConnectedEventHandler(e -> connectedLatch.countDown());
        client.start();

        // Wait for connected event to be processed
        Assertions.assertTrue(connectedLatch.await(5, TimeUnit.SECONDS), "Timed out waiting for connected event");

        // We need a separate thread to send the response because invokeEvent blocks
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<InvokeEventResult> resultRef = new AtomicReference<>();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        Thread invokeThread = new Thread(() -> {
            try {
                InvokeEventResult result = client.invokeEvent("echo", BinaryData.fromString("ping"),
                    WebPubSubDataFormat.TEXT, new InvokeEventOptions().setInvocationId("inv-test-1"));
                resultRef.set(result);
            } catch (Exception e) {
                errorRef.set(e);
            } finally {
                latch.countDown();
            }
        });
        invokeThread.start();

        // Wait for the invoke message to be sent
        Assertions.assertTrue(sendLatch.await(5, TimeUnit.SECONDS), "Timed out waiting for invoke message to be sent");

        // Deliver the invoke response
        Consumer<WebPubSubMessage> messageHandler = messageHandlerRef.get();
        Assertions.assertNotNull(messageHandler);
        messageHandler.accept(new InvokeResponseMessage("inv-test-1", true, WebPubSubDataFormat.TEXT,
            BinaryData.fromString("pong"), null));

        latch.await(5, TimeUnit.SECONDS);

        Assertions.assertNull(errorRef.get(), () -> "Unexpected error: " + errorRef.get());
        InvokeEventResult result = resultRef.get();
        Assertions.assertNotNull(result);
        Assertions.assertEquals("inv-test-1", result.getInvocationId());
        Assertions.assertEquals(WebPubSubDataFormat.TEXT, result.getDataFormat());
        Assertions.assertEquals("pong", result.getData().toString());
    }

    @Test
    public void testInvokeEventServiceError() throws InterruptedException {
        final AtomicReference<Consumer<WebPubSubMessage>> messageHandlerRef = new AtomicReference<>();
        final CountDownLatch connectedLatch = new CountDownLatch(1);
        final CountDownLatch sendLatch = new CountDownLatch(1);

        WebSocketSession mockWsSession = new MockWebSocketSession() {
            @Override
            public void sendObjectAsync(Object data, Consumer<SendResult> handler) {
                handler.accept(new SendResult());
                sendLatch.countDown();
            }
        };

        WebSocketClient mockWsClient = (cec, path, loggerReference, messageHandler, openHandler, closeHandler) -> {
            messageHandlerRef.set(messageHandler);
            openHandler.accept(mockWsSession);
            sendConnectedEvent(messageHandler);
            return mockWsSession;
        };

        WebPubSubClientBuilder builder = new WebPubSubClientBuilder();
        builder.webSocketClient = mockWsClient;
        WebPubSubClient client = builder.clientAccessUrl("mock").buildClient();
        client.addOnConnectedEventHandler(e -> connectedLatch.countDown());
        client.start();

        Assertions.assertTrue(connectedLatch.await(5, TimeUnit.SECONDS), "Timed out waiting for connected event");

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        Thread invokeThread = new Thread(() -> {
            try {
                client.invokeEvent("echo", BinaryData.fromString("ping"), WebPubSubDataFormat.TEXT,
                    new InvokeEventOptions().setInvocationId("inv-err-1"));
            } catch (Exception e) {
                errorRef.set(e);
            } finally {
                latch.countDown();
            }
        });
        invokeThread.start();

        Assertions.assertTrue(sendLatch.await(5, TimeUnit.SECONDS), "Timed out waiting for invoke message to be sent");

        Consumer<WebPubSubMessage> messageHandler = messageHandlerRef.get();
        Assertions.assertNotNull(messageHandler);
        messageHandler.accept(
            new InvokeResponseMessage("inv-err-1", false, null, null, new AckResponseError("BadRequest", "oops")));

        latch.await(5, TimeUnit.SECONDS);

        Assertions.assertNotNull(errorRef.get());
        Assertions.assertTrue(errorRef.get() instanceof InvocationException);
        InvocationException ex = (InvocationException) errorRef.get();
        Assertions.assertEquals("inv-err-1", ex.getInvocationId());
        Assertions.assertNotNull(ex.getErrorDetail());
        Assertions.assertEquals("BadRequest", ex.getErrorDetail().getName());
        Assertions.assertEquals("oops", ex.getErrorDetail().getMessage());
    }

    @Test
    public void testInvokeEventWithJsonData() throws InterruptedException {
        final AtomicReference<Consumer<WebPubSubMessage>> messageHandlerRef = new AtomicReference<>();
        final CountDownLatch connectedLatch = new CountDownLatch(1);
        final CountDownLatch sendLatch = new CountDownLatch(1);

        WebSocketSession mockWsSession = new MockWebSocketSession() {
            @Override
            public void sendObjectAsync(Object data, Consumer<SendResult> handler) {
                handler.accept(new SendResult());
                sendLatch.countDown();
            }
        };

        WebSocketClient mockWsClient = (cec, path, loggerReference, messageHandler, openHandler, closeHandler) -> {
            messageHandlerRef.set(messageHandler);
            openHandler.accept(mockWsSession);
            sendConnectedEvent(messageHandler);
            return mockWsSession;
        };

        WebPubSubClientBuilder builder = new WebPubSubClientBuilder();
        builder.webSocketClient = mockWsClient;
        WebPubSubClient client = builder.clientAccessUrl("mock").buildClient();
        client.addOnConnectedEventHandler(e -> connectedLatch.countDown());
        client.start();

        Assertions.assertTrue(connectedLatch.await(5, TimeUnit.SECONDS), "Timed out waiting for connected event");

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<InvokeEventResult> resultRef = new AtomicReference<>();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        Thread invokeThread = new Thread(() -> {
            try {
                InvokeEventResult result = client.invokeEvent("processOrder", BinaryData.fromString("{\"orderId\":1}"),
                    WebPubSubDataFormat.JSON, new InvokeEventOptions().setInvocationId("inv-json-1"));
                resultRef.set(result);
            } catch (Exception e) {
                errorRef.set(e);
            } finally {
                latch.countDown();
            }
        });
        invokeThread.start();

        Assertions.assertTrue(sendLatch.await(5, TimeUnit.SECONDS), "Timed out waiting for invoke message to be sent");

        // Deliver response with JSON data - use the decoder to parse it as the client would
        Consumer<WebPubSubMessage> messageHandler = messageHandlerRef.get();
        Assertions.assertNotNull(messageHandler);

        MessageDecoder decoder = new MessageDecoder();
        WebPubSubMessage responseMsg = decoder.decode("{\n" + "    \"type\": \"invokeResponse\",\n"
            + "    \"invocationId\": \"inv-json-1\",\n" + "    \"success\": true,\n" + "    \"dataType\": \"json\",\n"
            + "    \"data\": {\"status\":\"completed\"}\n" + "}");
        messageHandler.accept(responseMsg);

        latch.await(5, TimeUnit.SECONDS);

        Assertions.assertNull(errorRef.get(), () -> "Unexpected error: " + errorRef.get());
        InvokeEventResult result = resultRef.get();
        Assertions.assertNotNull(result);
        Assertions.assertEquals("inv-json-1", result.getInvocationId());
        Assertions.assertEquals(WebPubSubDataFormat.JSON, result.getDataFormat());
        Assertions.assertEquals("{\"status\":\"completed\"}", result.getData().toString());
    }

    @Test
    public void testInvokeEventTimeout() throws InterruptedException {
        final CountDownLatch connectedLatch = new CountDownLatch(1);

        WebSocketSession mockWsSession = new MockWebSocketSession() {
            @Override
            public void sendObjectAsync(Object data, Consumer<SendResult> handler) {
                handler.accept(new SendResult());
            }
        };

        WebSocketClient mockWsClient = (cec, path, loggerReference, messageHandler, openHandler, closeHandler) -> {
            openHandler.accept(mockWsSession);
            sendConnectedEvent(messageHandler);
            return mockWsSession;
        };

        WebPubSubClientBuilder builder = new WebPubSubClientBuilder();
        builder.webSocketClient = mockWsClient;
        WebPubSubClient client = builder.clientAccessUrl("mock").buildClient();
        client.addOnConnectedEventHandler(e -> connectedLatch.countDown());
        client.start();

        Assertions.assertTrue(connectedLatch.await(5, TimeUnit.SECONDS), "Timed out waiting for connected event");

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        Thread invokeThread = new Thread(() -> {
            try {
                // Set a short timeout; no response will be delivered, so it should time out
                InvokeEventOptions options
                    = new InvokeEventOptions().setInvocationId("inv-timeout-1").setTimeout(Duration.ofMillis(500));
                client.invokeEvent("echo", BinaryData.fromString("ping"), WebPubSubDataFormat.TEXT, options);
            } catch (Exception e) {
                errorRef.set(e);
            } finally {
                latch.countDown();
            }
        });
        invokeThread.start();

        // Do NOT deliver any response — let the timeout expire
        latch.await(5, TimeUnit.SECONDS);

        Assertions.assertNotNull(errorRef.get());
        Assertions.assertTrue(errorRef.get() instanceof InvocationException);
        InvocationException ex = (InvocationException) errorRef.get();
        Assertions.assertEquals("inv-timeout-1", ex.getInvocationId());
        Assertions.assertTrue(ex.getMessage().contains("timed out"));
    }

    private static void sendConnectedEvent(Consumer<WebPubSubMessage> messageHandler) {
        Mono.delay(SMALL_DELAY)
            .then(Mono.fromRunnable(() -> messageHandler.accept(new ConnectedMessage("mock_connection_id")))
                .subscribeOn(Schedulers.boundedElastic()))
            .subscribe();
    }

    private static class MockWebSocketSession implements WebSocketSession {
        @Override
        public boolean isOpen() {
            return true;
        }

        @Override
        public void sendObjectAsync(Object data, Consumer<SendResult> handler) {
            handler.accept(new SendResult());
        }

        @Override
        public void close() {
        }

        @Override
        public void sendTextAsync(String text, Consumer<SendResult> handler) {
            handler.accept(new SendResult());
        }

        @Override
        public void closeSocket() {
        }
    }
}
