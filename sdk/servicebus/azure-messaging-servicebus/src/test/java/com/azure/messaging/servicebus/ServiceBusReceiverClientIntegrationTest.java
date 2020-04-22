// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.models.ReceiveMode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Integration tests for {@link ServiceBusReceiverClient} from queues or subscriptions.
 */
public class ServiceBusReceiverClientIntegrationTest extends IntegrationTestBase {

    private final ClientLogger logger = new ClientLogger(ServiceBusReceiverAsyncClientIntegrationTest.class);

    ServiceBusReceiverClient receiver;
    ServiceBusSenderClient sender;
    /**
     * Receiver used to clean up resources in {@link #afterTest()}.
     */
    ServiceBusReceiverClient receiveAndDeleteReceiver;

    final AtomicInteger messagesPending = new AtomicInteger();


    protected ServiceBusReceiverClientIntegrationTest(ClientLogger logger) {
        super(new ClientLogger(ServiceBusReceiverClientIntegrationTest.class));
    }

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @Override
    protected void beforeTest() {
        sessionId = UUID.randomUUID().toString();
    }

    @Override
    protected void afterTest() {
        dispose(sender);
    }

    /**
     * Verifies that we can send and receive two messages.
     */
    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void receiveTwoMessagesAutoComplete(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);
        int howManyMessage = 2;

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message);
        sendMessage(message);

        // Assert & Act
        Iterable<ServiceBusReceivedMessage> iterableMessages = receiver.receive(howManyMessage, TIMEOUT);

        for(ServiceBusReceivedMessage receivedMessage: iterableMessages) {
            assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
        }

        messagesPending.decrementAndGet();
        messagesPending.decrementAndGet();
    }

    /**
     * Verifies that we can send and receive one messages.
     */
    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void receiveMessageAutoComplete(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);
        int howManyMessage = 1;

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message);

        // Assert & Act
        Iterable<ServiceBusReceivedMessage> iterableMessages = receiver.receive(howManyMessage, TIMEOUT);

        for(ServiceBusReceivedMessage receivedMessage: iterableMessages) {
            assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
        }

        messagesPending.decrementAndGet();
        messagesPending.decrementAndGet();
    }

    private void setSenderAndReceiver(MessagingEntityType entityType, boolean isSessionEnabled) {
        setSenderAndReceiver(entityType, isSessionEnabled, Function.identity());
    }

    private void setSenderAndReceiver(MessagingEntityType entityType, boolean isSessionEnabled,
                                      Function<ServiceBusClientBuilder.ServiceBusReceiverClientBuilder, ServiceBusClientBuilder.ServiceBusReceiverClientBuilder> onReceiverCreate) {

        switch (entityType) {
            case QUEUE:
                final String queueName = isSessionEnabled ? getSessionQueueName() : getQueueName();

                Assertions.assertNotNull(queueName, "'queueName' cannot be null.");

                sender = createBuilder().sender()
                    .queueName(queueName)
                    .buildClient();
                receiver = onReceiverCreate.apply(
                    createBuilder().receiver()
                        .queueName(queueName)
                        .sessionId(isSessionEnabled ? sessionId : null)
                ).buildClient();

                receiveAndDeleteReceiver = createBuilder().receiver()
                    .queueName(queueName)
                    .sessionId(isSessionEnabled ? sessionId : null)
                    .receiveMode(ReceiveMode.RECEIVE_AND_DELETE)
                    .buildClient();
                break;
            case SUBSCRIPTION:
                final String topicName = getTopicName();
                final String subscriptionName = isSessionEnabled ? getSessionSubscriptionName() : getSubscriptionName();

                Assertions.assertNotNull(topicName, "'topicName' cannot be null.");
                Assertions.assertNotNull(subscriptionName, "'subscriptionName' cannot be null.");

                sender = createBuilder().sender()
                    .topicName(topicName)
                    .buildClient();
                receiver = onReceiverCreate.apply(
                    createBuilder().receiver()
                        .topicName(topicName).subscriptionName(subscriptionName)
                        .sessionId(isSessionEnabled ? sessionId : null))
                    .buildClient();

                receiveAndDeleteReceiver = createBuilder().receiver()
                    .topicName(topicName).subscriptionName(subscriptionName)
                    .sessionId(isSessionEnabled ? sessionId : null)
                    .receiveMode(ReceiveMode.RECEIVE_AND_DELETE)
                    .buildClient();
                break;
            default:
                throw logger.logExceptionAsError(new IllegalArgumentException("Unknown entity type: " + entityType));
        }
    }

    protected void sendMessage(ServiceBusMessage message) {
        sender.send(message);
        int number = messagesPending.incrementAndGet();
        logger.info("Number sent: {}", number);
    }
}
