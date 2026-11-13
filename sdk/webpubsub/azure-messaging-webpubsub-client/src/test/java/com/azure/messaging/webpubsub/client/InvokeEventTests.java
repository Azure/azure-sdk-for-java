// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.http.policy.FixedDelay;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.messaging.webpubsub.client.implementation.MessageDecoder;
import com.azure.messaging.webpubsub.client.implementation.MessageEncoder;
import com.azure.messaging.webpubsub.client.implementation.models.ConnectedMessage;
import com.azure.messaging.webpubsub.client.implementation.models.InvokeMessage;
import com.azure.messaging.webpubsub.client.implementation.models.InvokeResponseMessage;
import com.azure.messaging.webpubsub.client.implementation.models.WebPubSubMessage;
import com.azure.messaging.webpubsub.client.implementation.websocket.CloseReason;
import com.azure.messaging.webpubsub.client.implementation.websocket.SendResult;
import com.azure.messaging.webpubsub.client.implementation.websocket.WebSocketClient;
import com.azure.messaging.webpubsub.client.implementation.websocket.WebSocketSession;
import com.azure.messaging.webpubsub.client.models.AckResponseError;
import com.azure.messaging.webpubsub.client.models.InvocationException;
import com.azure.messaging.webpubsub.client.models.InvokeEventOptions;
import com.azure.messaging.webpubsub.client.models.InvokeEventResult;
import com.azure.messaging.webpubsub.client.models.SendMessageFailedException;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataFormat;
import com.azure.messaging.webpubsub.client.models.WebPubSubProtocolType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class InvokeEventTests {

    private static final Duration SMALL_DELAY = Duration.ofMillis(10);

    @ParameterizedTest
    @ValueSource(strings = { "{\"orderId\":1}", "[1,true]", "42", "true", "\"done\"", "null" })
    public void testInvokeJsonWireType(String json) throws IOException {
        InvokeMessage message = new InvokeMessage().setInvocationId("json-wire")
            .setTarget("event")
            .setEvent("echo")
            .setDataType("json")
            .setData(BinaryData.fromString(json));
        String encoded = new MessageEncoder().encode(message);
        try (JsonReader expectedReader = JsonProviders.createReader(json);
            JsonReader actualReader = JsonProviders.createReader(encoded)) {
            Map<?, ?> frame = (Map<?, ?>) actualReader.readUntyped();
            Assertions.assertTrue(frame.containsKey("data"));
            Assertions.assertEquals(expectedReader.readUntyped(), frame.get("data"));
        }
    }

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

    @ParameterizedTest
    @ValueSource(
        strings = {
            "\"done\"",
            "\"true\"",
            "\"42\"",
            "\"\"",
            "\"a\\\"b\\nc\"",
            "{\"status\":\"done\"}",
            "[1,true]",
            "42",
            "0.12345678901234567890123456789",
            "9223372036854775808123456789",
            "1e400",
            "{\"amount\":0.12345678901234567890123456789}",
            "[0.12345678901234567890123456789,9223372036854775808123456789]",
            "true",
            "null" })
    public void testInvokeJsonResponseType(String json) throws IOException {
        for (boolean dataFirst : new boolean[] { true, false }) {
            String fields
                = dataFirst ? "\"data\":" + json + ",\"dataType\":\"json\"" : "\"dataType\":\"json\",\"data\":" + json;
            InvokeResponseMessage response = (InvokeResponseMessage) new MessageDecoder()
                .decode("{\"type\":\"invokeResponse\",\"invocationId\":\"json\",\"success\":true," + fields + "}");
            Assertions.assertEquals(WebPubSubDataFormat.JSON, response.getDataType());
            if ("null".equals(json)) {
                Assertions.assertNull(response.getData());
            } else {
                Assertions.assertNotNull(response.getData());
                Assertions.assertEquals(json, response.getData().toString());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void testDuplicateInvocationId(boolean generatedId) {
        try (InvocationTestClient fixture = new InvocationTestClient(false)) {
            CompletableFuture<InvokeEventResult> first = fixture.invoke("0").toFuture();
            CompletableFuture<InvokeEventResult> duplicate = fixture.invoke(generatedId ? null : "0").toFuture();
            assertInvocationFailure(duplicate, "0");
            Assertions.assertEquals(1, fixture.sentInvocations.size());
            Assertions.assertFalse(first.isDone());
            fixture.respond("0", "first");
            Assertions.assertEquals("first", completedResult(first).getData().toString());

            CompletableFuture<InvokeEventResult> reused = fixture.invoke("0").toFuture();
            Assertions.assertFalse(reused.isDone());
            fixture.respond("0", "second");
            Assertions.assertEquals("second", completedResult(reused).getData().toString());
            Assertions.assertEquals(2, fixture.sentInvocations.size());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void testStopTerminatesInvocation(boolean withTimeout) {
        try (InvocationTestClient fixture = new InvocationTestClient(false)) {
            InvokeEventOptions options = new InvokeEventOptions().setInvocationId("stop");
            if (withTimeout) {
                options.setTimeout(Duration.ofMinutes(1));
            }
            CompletableFuture<InvokeEventResult> pending
                = fixture.client.invokeEvent("echo", BinaryData.fromString("ping"), WebPubSubDataFormat.TEXT, options)
                    .toFuture();
            Assertions.assertEquals(1, fixture.sentInvocations.size());
            fixture.client.stop().block(Duration.ofSeconds(5));
            assertInvocationFailure(pending, "stop");

            fixture.client.start().block(Duration.ofSeconds(5));
            CompletableFuture<InvokeEventResult> restarted = fixture.invoke("stop").toFuture();
            fixture.respond("stop", "restarted");
            Assertions.assertEquals("restarted", completedResult(restarted).getData().toString());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void testConnectionLossTerminatesInvocation(boolean reliable) throws InterruptedException {
        try (InvocationTestClient fixture = new InvocationTestClient(reliable)) {
            CompletableFuture<InvokeEventResult> pending = fixture.invoke("disconnect").toFuture();
            fixture.closeHandler.get().accept(new CloseReason(1006, "Connection lost"));
            assertInvocationFailure(pending, "disconnect");
            Assertions.assertTrue(fixture.reconnected.await(5, TimeUnit.SECONDS));
            CompletableFuture<InvokeEventResult> next = fixture.invoke("disconnect").toFuture();
            fixture.respond("disconnect", "new response");
            Assertions.assertEquals("new response", completedResult(next).getData().toString());
        }
    }

    @Test
    public void testLateResponseAfterTimeoutIsDropped() {
        try (InvocationTestClient fixture = new InvocationTestClient(false)) {
            StepVerifier
                .withVirtualTime(
                    () -> fixture.client.invokeEvent("echo", BinaryData.fromString("ping"), WebPubSubDataFormat.TEXT,
                        new InvokeEventOptions().setInvocationId("reuse").setTimeout(Duration.ofSeconds(1))))
                .thenAwait(Duration.ofSeconds(1))
                .expectErrorSatisfies(error -> {
                    Assertions.assertTrue(error instanceof InvocationException);
                    Assertions.assertEquals("reuse", ((InvocationException) error).getInvocationId());
                    Assertions.assertTrue(error.getMessage().contains("timed out"));
                })
                .verify(Duration.ofSeconds(5));
            fixture.respond("reuse", "old-result");
            CompletableFuture<InvokeEventResult> next = fixture.invoke("reuse").toFuture();
            Assertions.assertEquals(2, fixture.sentInvocations.size());
            Assertions.assertFalse(next.isDone());
            fixture.respond("reuse", "new-result");
            Assertions.assertEquals("new-result", completedResult(next).getData().toString());
        }
    }

    @Test
    public void testCancellationReleasesInvocationId() {
        try (InvocationTestClient fixture = new InvocationTestClient(false)) {
            CompletableFuture<InvokeEventResult> pending = fixture.invoke("cancel").toFuture();
            Assertions.assertTrue(pending.cancel(false));
            fixture.respond("cancel", "old-result");
            CompletableFuture<InvokeEventResult> next = fixture.invoke("cancel").toFuture();
            Assertions.assertFalse(next.isDone());
            fixture.respond("cancel", "new-result");
            Assertions.assertEquals("new-result", completedResult(next).getData().toString());
            Assertions.assertEquals(2, fixture.sentInvocations.size());
        }
    }

    @Test
    public void testServiceErrorReleasesInvocationId() {
        try (InvocationTestClient fixture = new InvocationTestClient(false)) {
            CompletableFuture<InvokeEventResult> pending = fixture.invoke("error").toFuture();
            fixture.messageHandler.get()
                .accept(new InvokeResponseMessage("error", false, null, null,
                    new AckResponseError("BadRequest", "failed")));
            assertInvocationFailure(pending, "error");
            CompletableFuture<InvokeEventResult> next = fixture.invoke("error").toFuture();
            fixture.respond("error", "new-result");
            Assertions.assertEquals("new-result", completedResult(next).getData().toString());
        }
    }

    @Test
    public void testSendFailureReleasesInvocationId() {
        try (InvocationTestClient fixture = new InvocationTestClient(false)) {
            fixture.sendFailure = new IllegalStateException("Send failed");
            StepVerifier.create(fixture.invoke("send-failure"))
                .expectError(SendMessageFailedException.class)
                .verify(Duration.ofSeconds(5));
            fixture.sendFailure = null;
            CompletableFuture<InvokeEventResult> next = fixture.invoke("send-failure").toFuture();
            fixture.respond("send-failure", "new-result");
            Assertions.assertEquals("new-result", completedResult(next).getData().toString());
        }
    }

    @Test
    public void testImmediateResponseIsReceived() {
        try (InvocationTestClient fixture = new InvocationTestClient(false)) {
            fixture.immediateResponse = true;
            StepVerifier.create(fixture.invoke("immediate"))
                .assertNext(result -> Assertions.assertEquals("immediate-result", result.getData().toString()))
                .expectComplete()
                .verify(Duration.ofSeconds(5));
        }
    }

    private static InvokeEventResult completedResult(CompletableFuture<InvokeEventResult> future) {
        Assertions.assertTrue(future.isDone(), "Invocation must complete after receiving its response");
        return future.join();
    }

    private static void assertInvocationFailure(CompletableFuture<InvokeEventResult> future, String invocationId) {
        Assertions.assertTrue(future.isCompletedExceptionally(),
            "Invocation must fail without waiting for its timeout");
        CompletionException error = Assertions.assertThrows(CompletionException.class, future::join);
        Assertions.assertTrue(error.getCause() instanceof InvocationException);
        Assertions.assertEquals(invocationId, ((InvocationException) error.getCause()).getInvocationId());
    }

    private static final class InvocationTestClient implements AutoCloseable {
        private final AtomicReference<Consumer<WebPubSubMessage>> messageHandler = new AtomicReference<>();
        private final AtomicReference<Consumer<CloseReason>> closeHandler = new AtomicReference<>();
        private final List<InvokeMessage> sentInvocations = new ArrayList<>();
        private final CountDownLatch reconnected = new CountDownLatch(1);
        private final WebPubSubAsyncClient client;
        private RuntimeException sendFailure;
        private boolean immediateResponse;

        private InvocationTestClient(boolean reliable) {
            AtomicInteger connectionCount = new AtomicInteger();
            WebSocketClient transport = (configuration, url, logger, onMessage, onOpen, onClose) -> {
                messageHandler.set(onMessage);
                closeHandler.set(onClose);
                WebSocketSession session = new MockWebSocketSession() {
                    @Override
                    public void sendObjectAsync(Object data, Consumer<SendResult> handler) {
                        if (data instanceof InvokeMessage) {
                            InvokeMessage message = (InvokeMessage) data;
                            sentInvocations.add(message);
                            if (immediateResponse) {
                                respond(message.getInvocationId(), "immediate-result");
                            }
                        }
                        handler.accept(sendFailure == null ? new SendResult() : new SendResult(sendFailure));
                    }

                    @Override
                    public void close() {
                        onClose.accept(new CloseReason());
                    }
                };
                onOpen.accept(session);
                onMessage.accept(new MessageDecoder().decode("{\"type\":\"system\",\"event\":\"connected\","
                    + "\"connectionId\":\"connection\",\"reconnectionToken\":\"token\"}"));
                if (connectionCount.incrementAndGet() > 1) {
                    reconnected.countDown();
                }
                return session;
            };
            client = new WebPubSubAsyncClient(transport, () -> "https://localhost/client",
                reliable ? WebPubSubProtocolType.JSON_RELIABLE_PROTOCOL : WebPubSubProtocolType.JSON_PROTOCOL, null,
                "test", new FixedDelay(0, Duration.ofMillis(1)), true, false);
            client.start().block(Duration.ofSeconds(5));
        }

        private Mono<InvokeEventResult> invoke(String invocationId) {
            return client.invokeEvent("echo", BinaryData.fromString("ping"), WebPubSubDataFormat.TEXT,
                new InvokeEventOptions().setInvocationId(invocationId));
        }

        private void respond(String invocationId, String data) {
            messageHandler.get()
                .accept(new InvokeResponseMessage(invocationId, true, WebPubSubDataFormat.TEXT,
                    BinaryData.fromString(data), null));
        }

        @Override
        public void close() {
            client.close();
        }
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
