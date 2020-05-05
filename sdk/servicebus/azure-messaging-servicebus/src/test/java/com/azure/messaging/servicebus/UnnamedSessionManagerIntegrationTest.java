// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.models.ReceiveAsyncOptions;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.azure.messaging.servicebus.TestUtils.getServiceBusMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Integration tests for {@link UnnamedSessionManager}.
 */
class UnnamedSessionManagerIntegrationTest extends IntegrationTestBase {
    private final ConcurrentHashMap<String, Instant> sessionsLockedUntil = new ConcurrentHashMap<>();
    private final AtomicInteger messagesPending = new AtomicInteger();

    private ServiceBusReceiverAsyncClient receiver;
    private ServiceBusSenderAsyncClient sender;

    UnnamedSessionManagerIntegrationTest() {
        super(new ClientLogger(UnnamedSessionManagerIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        sessionId = UUID.randomUUID().toString();
    }

    @Override
    protected void afterTest() {
        final int pending = messagesPending.get();
        logger.info("Pending messages: {}", pending);
    }

    @Test
    void singleUnnamedSession() {
        // Arrange
        final String messageId = "singleUnnamedSession";
        final String sessionId = "singleUnnamedSession-" + Instant.now().toString();
        final String contents = "hello world";
        final int numberToSend = 5;
        final ReceiveAsyncOptions receiveOptions = new ReceiveAsyncOptions()
            .setMaxAutoLockRenewalDuration(Duration.ofMinutes(2))
            .setIsAutoCompleteEnabled(true);

        setSenderAndReceiver(MessagingEntityType.QUEUE, Function.identity());

        final Disposable subscription = Flux.interval(Duration.ofMillis(500))
            .take(numberToSend)
            .flatMap(index -> {
                final ServiceBusMessage message = getServiceBusMessage(contents, messageId)
                    .setSessionId(sessionId);
                return sender.send(message).thenReturn(index);
            }).subscribe(
                number -> logger.info("sessionId[{}] sent[{}] Message sent.", sessionId, number),
                error -> logger.error("sessionId[{}] Error encountered.", sessionId, error),
                () -> logger.info("sessionId[{}] Finished sending.", sessionId));

        // Act & Assert
        try {
            StepVerifier.create(receiver.receive(receiveOptions))
                .assertNext(context -> assertMessageEquals(sessionId, messageId, contents, context))
                .assertNext(context -> assertMessageEquals(sessionId, messageId, contents, context))
                .assertNext(context -> assertMessageEquals(sessionId, messageId, contents, context))
                .assertNext(context -> assertMessageEquals(sessionId, messageId, contents, context))
                .assertNext(context -> assertMessageEquals(sessionId, messageId, contents, context))
                .expectComplete()
                .verify(Duration.ofMinutes(2));
        } finally {
            subscription.dispose();
        }
    }

    /**
     * Sets the sender and receiver. If session is enabled, then a single-named session receiver is created.
     */
    private void setSenderAndReceiver(MessagingEntityType entityType,
        Function<ServiceBusSessionReceiverClientBuilder, ServiceBusSessionReceiverClientBuilder> onBuild) {

        this.sender = getSenderBuilder(false, entityType, true).buildAsyncClient();
        this.receiver = getSessionReceiverBuilder(false, entityType, builder -> onBuild.apply(builder))
            .buildAsyncClient();
    }

    private static void assertMessageEquals(String sessionId, String messageId, String contents,
        ServiceBusReceivedMessageContext actual) {
        ServiceBusReceivedMessage message = actual.getMessage();

        assertNotNull(message, "'message' should not be null. Error? " + actual.getThrowable());
//        assertEquals(sessionId, message.getSessionId());
        assertEquals(messageId, message.getMessageId());
        assertEquals(contents, new String(message.getBody(), StandardCharsets.UTF_8));

        assertNull(actual.getThrowable());
    }
}
