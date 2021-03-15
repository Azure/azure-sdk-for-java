// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.models.CompleteOptions;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Test where various clients are involved for example Sender, Receiver and Processor client.
 */
public class ServiceBusMixClientIntegrationTest extends IntegrationTestBase {
    private ServiceBusSenderAsyncClient sender;
    private ServiceBusReceiverAsyncClient receiver;
    private final AtomicInteger messagesPending = new AtomicInteger();

    ServiceBusMixClientIntegrationTest() {
        super(new ClientLogger(ServiceBusSenderAsyncClientIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        sessionId = UUID.randomUUID().toString();
    }

    @Override
    protected void afterTest() {
        dispose(sender);

        final int numberOfMessages = messagesPending.get();
        if (numberOfMessages < 1) {
            dispose(receiver);
            return;
        }

        try {
            if (receiver == null) {
                return;
            }
            receiver.receiveMessages()
                .take(numberOfMessages)
                .map(message -> {
                    logger.info("Message received: {}", message.getSequenceNumber());
                    return message;
                })
                .timeout(Duration.ofSeconds(5), Mono.empty())
                .blockLast();
        } catch (Exception e) {
            logger.warning("Error occurred when draining queue.", e);
        } finally {
            dispose(receiver);
        }
    }

    /**
     * Use case: Test cross entity transaction using processor client and sender.
     * 1. Read messages from entity A.
     * 2. complete the messages from entity A and write to entity B.
     * 2. commit the transaction.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void crossEntityTransactionWithProcessorTest(MessagingEntityType entityType, boolean isSessionEnabled) throws InterruptedException {

        // Arrange
        final boolean useCredentials = false;
        final int receiveQueueAIndex = TestUtils.USE_CASE_TXN_1;
        final int sendQueueBIndex = TestUtils.USE_CASE_TXN_2;
        final String queueA = isSessionEnabled ? getSessionQueueName(receiveQueueAIndex) : getQueueName(receiveQueueAIndex);
        final String queueB = isSessionEnabled ? getSessionQueueName(sendQueueBIndex) : getQueueName(sendQueueBIndex);
        final String topicA = getTopicName(receiveQueueAIndex);
        final String topicB = getTopicName(sendQueueBIndex);
        final AtomicBoolean transactionComplete = new AtomicBoolean();

        final CountDownLatch countdownLatch = new CountDownLatch(1);
        final AtomicInteger receivedMessages = new AtomicInteger();

        final String messageId = UUID.randomUUID().toString();
        ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS_BYTES, messageId);
        message.setSessionId(sessionId);
        final List<ServiceBusMessage> messages = Arrays.asList(message);

        ServiceBusClientBuilder builder = getBuilder(useCredentials).enableCrossEntityTransactions();

        final ServiceBusSenderAsyncClient senderAsyncA;
        final ServiceBusSenderClient senderSyncB;

        // Initialize sender
        switch (entityType) {
            case QUEUE:
                senderAsyncA = builder.sender().queueName(queueA).buildAsyncClient();
                senderSyncB = builder.sender().queueName(queueB).buildClient();
                break;
            case SUBSCRIPTION:
                senderAsyncA = builder.sender().topicName(topicA).buildAsyncClient();
                senderSyncB = builder.sender().topicName(topicB).buildClient();
                break;
            default:
                logger.error("Invalid entity type {}.", entityType);
                throw new RuntimeException("Invalid entity type.");
        }

        Consumer<ServiceBusReceivedMessageContext> processMessage = (context) -> {
            receivedMessages.incrementAndGet();
            messagesPending.incrementAndGet();
            ServiceBusReceivedMessage myMessage = context.getMessage();
            System.out.printf("Processing message. MessageId: %s, Sequence #: %s. Contents: %s %n", myMessage.getMessageId(),
                myMessage.getSequenceNumber(), myMessage.getBody());
            if (receivedMessages.get() == 1) {

                //Start a transaction
                ServiceBusTransactionContext transactionId = senderSyncB.createTransaction();
                context.complete(new CompleteOptions().setTransactionContext(transactionId));
                senderSyncB.sendMessage(new ServiceBusMessage(CONTENTS_BYTES).setMessageId(messageId).setSessionId(sessionId), transactionId);
                senderSyncB.commitTransaction(transactionId);
                transactionComplete.set(true);
                countdownLatch.countDown();
                logger.verbose("Transaction committed.");
            }
        };

        Consumer<ServiceBusErrorContext> processError = context -> {
            System.out.printf("Error when receiving messages from namespace: '%s'. Entity: '%s'. Error Source: '%s' %n",
                context.getFullyQualifiedNamespace(), context.getEntityPath(), context.getErrorSource());
            Assertions.fail("Failed processing of message.", context.getException());

            if (!(context.getException() instanceof ServiceBusException)) {
                System.out.printf("Non-ServiceBusException occurred: %s%n", context.getException());
            }
        };

        final ServiceBusProcessorClient processorA;
        // Initialize processor client
        switch (entityType) {
            case QUEUE:
                if (isSessionEnabled) {
                    processorA = builder.sessionProcessor().disableAutoComplete().queueName(queueA)
                        .processMessage(processMessage).processError(processError)
                        .buildProcessorClient();
                } else {
                    processorA = builder.processor().disableAutoComplete().queueName(queueA)
                        .processMessage(processMessage).processError(processError)
                        .buildProcessorClient();
                }
                break;
            case SUBSCRIPTION:
                if (isSessionEnabled) {
                    processorA = builder.sessionProcessor().disableAutoComplete().topicName(topicA).subscriptionName("session-subscription")
                        .processMessage(processMessage).processError(processError)
                        .buildProcessorClient();
                } else {
                    processorA = builder.processor().disableAutoComplete().topicName(topicA).subscriptionName("subscription")
                        .processMessage(processMessage).processError(processError)
                        .buildProcessorClient();
                }
                break;
            default:
                logger.error("Invalid entity type {}.", entityType);
                throw new RuntimeException("Invalid entity type.");
        }

        // Send messages
        StepVerifier.create(senderAsyncA.sendMessages(messages)).verifyComplete();
        // Create an instance of the processor through the ServiceBusClientBuilder

        // Act
        System.out.println("Starting the processor");
        processorA.start();

        // Assert
        System.out.println("Listening for 10 seconds...");
        if (countdownLatch.await(10, TimeUnit.SECONDS)) {
            System.out.println("Completed processing successfully.");
            Assertions.assertTrue(transactionComplete.get());
        } else {
            System.out.println("Closing processor.");
            Assertions.fail("Failed to process message.");
        }

        processorA.close();


        // Verify that message is received by queue B
        if (!isSessionEnabled) {
            setSenderAndReceiver(entityType, sendQueueBIndex, false);
            StepVerifier.create(receiver.receiveMessages().take(1))
                .assertNext(receivedMessage -> {
                    assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                    messagesPending.decrementAndGet();
                }).verifyComplete();
        }
    }

    /**
     * Use case: Test cross entity transaction using receiver and senders.
     * 1. Read messages from entity A.
     * 2. complete the messages from entity A and write to entity B.
     * 2. commit the transaction.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void crossEntityTransactionWithReceiverSenderTest(MessagingEntityType entityType, boolean isSessionEnabled) throws InterruptedException {

        // Arrange
        final boolean useCredentials = false;
        final int receiveQueueAIndex = TestUtils.USE_CASE_TXN_1;
        final int sendQueueBIndex = TestUtils.USE_CASE_TXN_2;
        final String queueA = isSessionEnabled ? getSessionQueueName(receiveQueueAIndex) : getQueueName(receiveQueueAIndex);
        final String queueB = isSessionEnabled ? getSessionQueueName(sendQueueBIndex) : getQueueName(sendQueueBIndex);
        final String topicA = getTopicName(receiveQueueAIndex);
        final String topicB = getTopicName(sendQueueBIndex);
        final AtomicBoolean transactionComplete = new AtomicBoolean();

        final CountDownLatch countdownLatch = new CountDownLatch(1);

        final String messageId = UUID.randomUUID().toString();
        ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS_BYTES, messageId);
        message.setSessionId(sessionId);
        final List<ServiceBusMessage> messages = Arrays.asList(message);

        ServiceBusClientBuilder builder = getBuilder(useCredentials).enableCrossEntityTransactions();

        final ServiceBusSenderAsyncClient senderAsyncA;
        final ServiceBusSenderClient senderSyncB;

        // Initialize sender
        switch (entityType) {
            case QUEUE:
                senderAsyncA = builder.sender().queueName(queueA).buildAsyncClient();
                senderSyncB = builder.sender().queueName(queueB).buildClient();
                break;
            case SUBSCRIPTION:
                senderAsyncA = builder.sender().topicName(topicA).buildAsyncClient();
                senderSyncB = builder.sender().topicName(topicB).buildClient();
                break;
            default:
                logger.error("Invalid entity type {}.", entityType);
                throw new RuntimeException("Invalid entity type.");
        }

        // Send messages
        StepVerifier.create(senderAsyncA.sendMessages(messages)).verifyComplete();

        final ServiceBusReceiverAsyncClient receiverA;

        switch (entityType) {
            case QUEUE:
                if (isSessionEnabled) {
                    receiverA = builder.sessionReceiver().disableAutoComplete().queueName(queueA)
                        .buildAsyncClient().acceptNextSession().block();
                } else {
                    receiverA = builder.receiver().disableAutoComplete().queueName(queueA)
                        .buildAsyncClient();
                }
                break;
            case SUBSCRIPTION:
                if (isSessionEnabled) {
                    receiverA = builder.sessionReceiver().disableAutoComplete().topicName(topicA).subscriptionName("session-subscription")
                        .buildAsyncClient().acceptNextSession().block();
                } else {
                    receiverA = builder.receiver().disableAutoComplete().topicName(topicA).subscriptionName("subscription")
                        .buildAsyncClient();
                }
                break;
            default:
                logger.error("Invalid entity type {}.", entityType);
                throw new RuntimeException("Invalid entity type.");
        }

        receiverA.receiveMessages().flatMap(receivedMessage -> {
            //Start a transaction
            logger.verbose("Received message sequence number {}. Creating transaction", receivedMessage.getSequenceNumber());
            ServiceBusTransactionContext transactionId = senderSyncB.createTransaction();
            receiverA.complete(receivedMessage, new CompleteOptions().setTransactionContext(transactionId)).block();
            senderSyncB.sendMessage(new ServiceBusMessage(CONTENTS_BYTES).setMessageId(messageId).setSessionId(sessionId), transactionId);
            senderSyncB.commitTransaction(transactionId);
            transactionComplete.set(true);
            countdownLatch.countDown();
            logger.verbose("Transaction committed.");
            return Mono.just(receivedMessage);
        }).subscribe();


        // Act
        System.out.println("Listening for 10 seconds...");
        if (countdownLatch.await(10, TimeUnit.SECONDS)) {
            System.out.println("Completed message processing successfully.");
            Assertions.assertTrue(transactionComplete.get());
        } else {
            System.out.println("Some error.");
            Assertions.fail("Failed to process message.");
        }

        // Assert
        // Verify that message is received by entity B
        if (!isSessionEnabled) {
            setSenderAndReceiver(entityType, sendQueueBIndex, false);
            StepVerifier.create(receiver.receiveMessages().take(1))
                .assertNext(receivedMessage -> {
                    assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                    messagesPending.decrementAndGet();
                }).verifyComplete();
        }
    }

    /**
     * Sets the sender and receiver. If session is enabled, then a single-named session receiver is created.
     */
    private void setSenderAndReceiver(MessagingEntityType entityType, int entityIndex, boolean useCredentials) {
        final boolean isSessionAware = false;
        final boolean sharedConnection = true;

        this.sender = getSenderBuilder(useCredentials, entityType, entityIndex, isSessionAware, sharedConnection)
            .buildAsyncClient();
        this.receiver = getReceiverBuilder(useCredentials, entityType, entityIndex, sharedConnection)
            .receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
            .disableAutoComplete()
            .buildAsyncClient();
    }
}
