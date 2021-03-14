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

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
            if (receiver ==  null) {
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
     * Test cross transaction entity
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void crossEntityTransactionTest(MessagingEntityType entityType) throws InterruptedException {

        if (entityType == MessagingEntityType.SUBSCRIPTION) return;
        // Arrange
        final boolean useCredentials = false;
        final Duration shortTimeout = Duration.ofSeconds(15);
        final int receiveQueueAIndex = TestUtils.USE_CASE_TXN_QUEUE_1;
        final int sendQueueBIndex = TestUtils.USE_CASE_TXN_QUEUE_2;

        final boolean isSessionEnabled = false;
        final int total = 1;
        final Duration shortWait = Duration.ofSeconds(5);
        final CountDownLatch countdownLatch = new CountDownLatch(1);
        final AtomicInteger receivedMessages = new AtomicInteger();

        int destination1_Entity = 0;
        int destination2_Entity = 2;
        int destination3_Entity = 3;
        String queueA = "queue-13"; // sender and receiver
        String queueB = "queue-14"; // sender

        final String messageId = UUID.randomUUID().toString();
        final byte[] CONTENTS_BYTES1 = "Some-contents 1".getBytes(StandardCharsets.UTF_8);

        final List<ServiceBusMessage> messages1 = TestUtils.getServiceBusMessages(total, messageId, CONTENTS_BYTES1);

        ServiceBusClientBuilder builder = getBuilder(useCredentials).enableCrossEntityTransactions();

        final ServiceBusSenderAsyncClient senderAsyncA = builder
            .sender()
            .queueName(queueA)
            .buildAsyncClient();

        final ServiceBusSenderClient senderSyncB = builder
            .sender()
            .queueName(queueB)
            .buildClient();


       // AtomicReference<>
        // Send messages
        StepVerifier.create(senderAsyncA.sendMessages(messages1)).verifyComplete();
        // Create an instance of the processor through the ServiceBusClientBuilder
        ServiceBusProcessorClient processorA = builder
            .processor()
            .disableAutoComplete()
            .queueName(queueA)
            .processMessage(context -> {
                receivedMessages.incrementAndGet();
                messagesPending.incrementAndGet();
                ServiceBusReceivedMessage message = context.getMessage();
                System.out.printf("Processing message. MessageId: %s, Sequence #: %s. Contents: %s %n", message.getMessageId(),
                    message.getSequenceNumber(), message.getBody());
                if (receivedMessages.get() == 1) {
                    //Start a transaction
                    ServiceBusTransactionContext transactionId = senderSyncB.createTransaction();
                    context.complete(new CompleteOptions().setTransactionContext(transactionId));
                    senderSyncB.sendMessage(new ServiceBusMessage("Order Processed").setMessageId(messageId), transactionId);
                    senderSyncB.commitTransaction(transactionId);
                    countdownLatch.countDown();
                    logger.verbose("!!!! Test transaction committed.");
                }
            })
            .processError(context -> {
                System.out.printf("Error when receiving messages from namespace: '%s'. Entity: '%s'%n",
                    context.getFullyQualifiedNamespace(), context.getEntityPath());

                if (!(context.getException() instanceof ServiceBusException)) {
                    System.out.printf("Non-ServiceBusException occurred: %s%n", context.getException());
                    return;
                }
            })
            .buildProcessorClient();
        System.out.println("Starting the processor");
        processorA.start();

        System.out.println("Listening for 10 seconds...");
        if (countdownLatch.await(10, TimeUnit.SECONDS)) {
            System.out.println("Completed processing successfully.");
        } else {
            System.out.println("Closing processor.");
        }

        processorA.close();

        // Assert & Act

        // Verify that message is received by queue B
        /*setSenderAndReceiver(entityType, sendQueueBIndex, false);
        StepVerifier.create(receiver.receiveMessages()
           // .flatMap(receivedMessage -> receiver.complete(receivedMessage).thenReturn(receivedMessage))
            .take(1))
            .assertNext(receivedMessage -> {
                assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                messagesPending.decrementAndGet();
            }).verifyComplete();
        */
        System.out.println("Exit.");
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
