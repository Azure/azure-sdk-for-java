// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.azure.messaging.servicebus.TestUtils.getServiceBusMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration tests for {@link ServiceBusSessionManager}.
 */
@Tag("integration")
class ServiceBusSessionManagerIntegrationTest extends IntegrationTestBase {
    private final AtomicInteger messagesPending = new AtomicInteger();

    private ServiceBusReceiverAsyncClient receiver;
    private ServiceBusSenderAsyncClient sender;
    private ServiceBusSessionReceiverAsyncClient sessionReceiver;

    ServiceBusSessionManagerIntegrationTest() {
        super(new ClientLogger(ServiceBusSessionManagerIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        sessionId = UUID.randomUUID().toString();
    }

    @Override
    protected void afterTest() {
        final int pending = messagesPending.get();
        logger.info("Pending messages: {}", pending);
        try {
            dispose(receiver, sender, sessionReceiver);
        } catch (Exception e) {
            logger.warning("Error occurred when draining queue.", e);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    void singleUnnamedSession(MessagingEntityType entityType) {
        // Arrange
        final int entityIndex = TestUtils.USE_CASE_SINGLE_SESSION;
        final String messageId = "singleUnnamedSession";
        final String contents = "Some-contents";
        final int numberToSend = 5;

        setSender(entityType, entityIndex);
        final Disposable subscription = Flux.interval(Duration.ofMillis(500))
            .take(numberToSend)
            .flatMap(index -> {
                final ServiceBusMessage message = getServiceBusMessage(contents, messageId)
                    .setSessionId(sessionId);
                messagesPending.incrementAndGet();
                return sender.sendMessage(message).thenReturn(index);
            })
            .subscribe(
                number -> logger.info("sessionId[{}] sent[{}] Message sent.", sessionId, number),
                error -> logger.error("sessionId[{}] Error encountered.", sessionId, error),
                () -> logger.info("sessionId[{}] Finished sending.", sessionId));

        setReceiver(entityType, entityIndex, Function.identity());

        // Act & Assert
        StepVerifier.create(receiver.receiveMessages().concatMap(
            receivedMessage -> receiver.complete(receivedMessage).thenReturn(receivedMessage)
        ))
            .assertNext(serviceBusReceivedMessage ->
                assertMessageEquals(sessionId, messageId, contents, serviceBusReceivedMessage))
            .assertNext(serviceBusReceivedMessage ->
                assertMessageEquals(sessionId, messageId, contents, serviceBusReceivedMessage))
            .assertNext(serviceBusReceivedMessage ->
                assertMessageEquals(sessionId, messageId, contents, serviceBusReceivedMessage))
            .assertNext(serviceBusReceivedMessage ->
                assertMessageEquals(sessionId, messageId, contents, serviceBusReceivedMessage))
            .assertNext(serviceBusReceivedMessage ->
                assertMessageEquals(sessionId, messageId, contents, serviceBusReceivedMessage))
            .thenCancel()
            .verify(Duration.ofMinutes(2));
    }

    /**
     * Sets the sender and receiver. If session is enabled, then a single-named session receiver is created.
     */
    private void setSender(MessagingEntityType entityType, int entityIndex) {

        this.sender = getSenderBuilder(false, entityType, entityIndex, true, false)
            .buildAsyncClient();
    }
    private void setReceiver(MessagingEntityType entityType, int entityIndex,
                             Function<ServiceBusSessionReceiverClientBuilder, ServiceBusSessionReceiverClientBuilder> onBuild) {
        ServiceBusSessionReceiverClientBuilder sessionBuilder = getSessionReceiverBuilder(false,
            entityType, entityIndex, false).disableAutoComplete();

        this.sessionReceiver = onBuild.apply(sessionBuilder).buildAsyncClient();
        this.receiver = this.sessionReceiver.acceptSession(sessionId).block();
    }

    private static void assertMessageEquals(String sessionId, String messageId, String contents, ServiceBusReceivedMessage message) {

        assertNotNull(message, "'message' should not be null.");

        if (!CoreUtils.isNullOrEmpty(sessionId)) {
            assertEquals(sessionId, message.getSessionId());
        }

        assertEquals(messageId, message.getMessageId());
        assertEquals(contents, message.getBody().toString());
    }
}
