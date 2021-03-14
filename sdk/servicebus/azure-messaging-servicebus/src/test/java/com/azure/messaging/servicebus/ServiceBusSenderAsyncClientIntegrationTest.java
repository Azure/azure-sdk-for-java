// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.models.CompleteOptions;
import com.azure.messaging.servicebus.models.CreateMessageBatchOptions;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link ServiceBusSenderAsyncClient} from queues or subscriptions.
 */
@Tag("integration")
class ServiceBusSenderAsyncClientIntegrationTest extends IntegrationTestBase {
    private ServiceBusSenderAsyncClient sender;
    private ServiceBusReceiverAsyncClient receiver;
    private final AtomicInteger messagesPending = new AtomicInteger();

    ServiceBusSenderAsyncClientIntegrationTest() {
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

    static Stream<Arguments> transactionMessageSendAndCompleteTransaction() {
        return Stream.of(
            Arguments.of(MessagingEntityType.QUEUE, true),
            Arguments.of(MessagingEntityType.SUBSCRIPTION, true),
            Arguments.of(MessagingEntityType.QUEUE, false),
            Arguments.of(MessagingEntityType.SUBSCRIPTION, false)
        );
    }

    /**
     * Verifies that we can send a message to a non-session queue.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void nonSessionQueueSendMessage(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, 0, false);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS_BYTES, messageId);

        // Assert & Act
        StepVerifier.create(sender.sendMessage(message).doOnSuccess(aVoid -> messagesPending.incrementAndGet()))
            .verifyComplete();
    }

    /**
     * Verifies that we can send a list of messages to a non-session entity.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void nonSessionEntitySendMessageList(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, 0, false);
        int count = 4;

        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(count, UUID.randomUUID().toString(), CONTENTS_BYTES);

        // Assert & Act
        StepVerifier.create(sender.sendMessages(messages).doOnSuccess(aVoid -> {
            messages.forEach(serviceBusMessage -> messagesPending.incrementAndGet());
        }))
            .verifyComplete();
    }

    /**
     * Verifies that we can send a {@link ServiceBusMessageBatch} to a non-session queue.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void nonSessionMessageBatch(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, 0, false);

        final String messageId = UUID.randomUUID().toString();
        final CreateMessageBatchOptions options = new CreateMessageBatchOptions().setMaximumSizeInBytes(1024);
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(3, messageId, CONTENTS_BYTES);

        // Assert & Act
        StepVerifier.create(sender.createMessageBatch(options)
            .flatMap(batch -> {
                for (ServiceBusMessage message : messages) {
                    Assertions.assertTrue(batch.tryAddMessage(message));
                }

                return sender.sendMessages(batch).doOnSuccess(aVoid -> messagesPending.incrementAndGet());
            }))
            .verifyComplete();
    }

    /**
     * Verifies that we can send message to final destination using via-queue.
     */
    @Disabled("The send via functionality is removing for first GA release, later we will come back to it.")
    @Test
    void viaQueueMessageSendTest() {
        // Arrange
        final boolean useCredentials = false;
        final Duration shortTimeout = Duration.ofSeconds(15);
        final int viaIntermediateEntity = TestUtils.USE_CASE_TXN_QUEUE_1;
        final int destinationEntity = TestUtils.USE_CASE_TXN_QUEUE_2;
        final boolean shareConnection = true;
        final MessagingEntityType entityType = MessagingEntityType.QUEUE;
        final boolean isSessionEnabled = false;
        final String messageId = UUID.randomUUID().toString();
        final int total = 1;
        final int totalToDestination = 2;
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(total, messageId, CONTENTS_BYTES);
        final String viaQueueName = getQueueName(viaIntermediateEntity);

        setSenderAndReceiver(entityType, viaIntermediateEntity, useCredentials);

        final ServiceBusSenderAsyncClient destination1ViaSender = getSenderBuilder(useCredentials, entityType,
            destinationEntity, false, shareConnection)
            //.viaQueueName(viaQueueName)
            .buildAsyncClient();
        final ServiceBusReceiverAsyncClient destination1Receiver = getReceiverBuilder(useCredentials, entityType,
            destinationEntity, shareConnection)
            .receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
            .disableAutoComplete()
            .buildAsyncClient();

        final AtomicReference<ServiceBusTransactionContext> transaction = new AtomicReference<>();

        // Act
        try {
            StepVerifier.create(destination1ViaSender.createTransaction())
                .assertNext(transactionContext -> {
                    transaction.set(transactionContext);
                    assertNotNull(transaction);
                })
                .verifyComplete();
            assertNotNull(transaction.get());

            StepVerifier.create(sender.sendMessages(messages, transaction.get()))
                .verifyComplete();
            StepVerifier.create(destination1ViaSender.sendMessages(messages, transaction.get()))
                .verifyComplete();
            StepVerifier.create(destination1ViaSender.sendMessages(messages, transaction.get()))
                .verifyComplete();

            StepVerifier.create(destination1ViaSender.commitTransaction(transaction.get())
                .delaySubscription(Duration.ofSeconds(1)))
                .verifyComplete();

            // Assert
            // Verify message is received by final destination Entity
            StepVerifier.create(destination1Receiver.receiveMessages().take(totalToDestination).timeout(shortTimeout))
                .assertNext(receivedMessage -> {
                    assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                    messagesPending.decrementAndGet();
                })
                .assertNext(receivedMessage -> {
                    assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                    messagesPending.decrementAndGet();
                })
                .verifyComplete();

            // Verify, intermediate-via queue has it delivered to intermediate Entity.
            StepVerifier.create(receiver.receiveMessages().take(total).timeout(shortTimeout))
                .assertNext(receivedMessage -> {
                    assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                    messagesPending.decrementAndGet();
                })
                .verifyComplete();
        } finally {
            destination1Receiver.close();
            destination1ViaSender.close();
        }
    }


    /**
     * Verifies that we can send message to final destination using via-topic.
     */
    @Disabled("The send via functionality is removed for first GA release, later we will come back to it.")
    @Test
    void viaTopicMessageSendTest() {
        // Arrange
        final boolean useCredentials = false;
        final Duration shortTimeout = Duration.ofSeconds(15);
        final int viaIntermediateEntity = TestUtils.USE_CASE_SEND_VIA_TOPIC_1;
        final int destinationEntity = TestUtils.USE_CASE_SEND_VIA_TOPIC_2;
        final boolean shareConnection = true;
        final MessagingEntityType entityType = MessagingEntityType.SUBSCRIPTION;
        final boolean isSessionEnabled =  false;
        final String messageId = UUID.randomUUID().toString();
        final int total = 1;
        final int totalToDestination = 2;
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(total, messageId, CONTENTS_BYTES);
        final String viaTopicName = getTopicName(viaIntermediateEntity);

        setSenderAndReceiver(entityType, viaIntermediateEntity, useCredentials);
        final ServiceBusReceiverAsyncClient intermediateReceiver =  receiver;
        final ServiceBusSenderAsyncClient intermediateSender = sender;

        final ServiceBusSenderAsyncClient destination1ViaSender = getSenderBuilder(useCredentials, entityType,
            destinationEntity, false, shareConnection)
            //.viaTopicName(viaTopicName)
            .buildAsyncClient();

        final ServiceBusReceiverAsyncClient destination1Receiver = getReceiverBuilder(useCredentials, entityType,
            destinationEntity, shareConnection)
            .receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
            .disableAutoComplete()
            .buildAsyncClient();

        final AtomicReference<ServiceBusTransactionContext> transaction = new AtomicReference<>();

        // Act
        StepVerifier.create(destination1ViaSender.createTransaction())
            .assertNext(transactionContext -> {
                transaction.set(transactionContext);
                assertNotNull(transaction);
            })
            .verifyComplete();
        assertNotNull(transaction.get());

        StepVerifier.create(intermediateSender.sendMessages(messages, transaction.get()))
            .verifyComplete();
        StepVerifier.create(destination1ViaSender.sendMessages(messages, transaction.get()))
            .verifyComplete();
        StepVerifier.create(destination1ViaSender.sendMessages(messages, transaction.get()))
            .verifyComplete();

        StepVerifier.create(destination1ViaSender.commitTransaction(transaction.get()).delaySubscription(Duration.ofSeconds(1)))
            .verifyComplete();

        // Assert
        // Verify message is received by final destination Entity
        StepVerifier.create(destination1Receiver.receiveMessages().take(totalToDestination).timeout(shortTimeout))
            .assertNext(receivedMessage -> {
                assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                messagesPending.decrementAndGet();
            })
            .assertNext(receivedMessage -> {
                assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                messagesPending.decrementAndGet();
            })
            .verifyComplete();

        // Verify, intermediate-via topic has it delivered to intermediate Entity.
        StepVerifier.create(intermediateReceiver.receiveMessages().take(total).timeout(shortTimeout))
            .assertNext(receivedMessage -> {
                assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                messagesPending.decrementAndGet();
            })
            .verifyComplete();
    }

    /**
     * Verifies that we can do following
     * 1. create transaction
     * 2. send message  with transactionContext
     * 3. Rollback/commit this transaction.
     */
    @MethodSource
    @ParameterizedTest
    void transactionMessageSendAndCompleteTransaction(MessagingEntityType entityType, boolean isCommit) {
        // Arrange
        Duration shortTimeout = Duration.ofSeconds(15);
        setSenderAndReceiver(entityType, TestUtils.USE_CASE_SEND_READ_BACK_MESSAGES, false);
        final boolean isSessionEnabled = false;
        final String messageId = UUID.randomUUID().toString();
        final int total = 3;
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(total, messageId, CONTENTS_BYTES);

        // Assert & Act
        AtomicReference<ServiceBusTransactionContext> transaction = new AtomicReference<>();
        StepVerifier.create(sender.createTransaction())
            .assertNext(transactionContext -> {
                transaction.set(transactionContext);
                assertNotNull(transaction);
            })
            .verifyComplete();
        assertNotNull(transaction.get());

        // Assert & Act
        StepVerifier.create(sender.sendMessages(messages, transaction.get()))
            .verifyComplete();
        if (isCommit) {
            StepVerifier.create(sender.commitTransaction(transaction.get()).delaySubscription(Duration.ofSeconds(1)))
                .verifyComplete();
            StepVerifier.create(receiver.receiveMessages().take(total))
                .assertNext(receivedMessage -> {
                    System.out.println("1");
                    assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                    messagesPending.decrementAndGet();
                })
                .assertNext(receivedMessage -> {
                    System.out.println("2");
                    assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                    messagesPending.decrementAndGet();
                })
                .assertNext(receivedMessage -> {
                    System.out.println("3");
                    assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                    messagesPending.decrementAndGet();
                })
                .expectComplete()
                .verify(shortTimeout);
        } else {
            StepVerifier.create(sender.rollbackTransaction(transaction.get()).delaySubscription(Duration.ofSeconds(1)))
                .verifyComplete();
            StepVerifier.create(receiver.receiveMessages().take(total))
                .verifyTimeout(shortTimeout);
        }
    }

    /**
     * Verifies that we can send using credentials.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void sendWithCredentials(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, 0, true);

        final String messageId = UUID.randomUUID().toString();
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(5, messageId, CONTENTS_BYTES);

        // Act & Assert
        StepVerifier.create(sender.createMessageBatch()
            .flatMap(batch -> {
                messages.forEach(m -> Assertions.assertTrue(batch.tryAddMessage(m)));

                return sender.sendMessages(batch).doOnSuccess(aVoid -> messagesPending.incrementAndGet());
            }))
            .expectComplete()
            .verify();
    }

    /**
     * Verifies that we can create transaction, scheduleMessage and commit.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void transactionScheduleAndCommitTest(MessagingEntityType entityType) {

        // Arrange
        boolean isSessionEnabled = false;
        setSenderAndReceiver(entityType, 0, isSessionEnabled);
        final Duration scheduleDuration = Duration.ofSeconds(3);
        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        // Assert & Act
        AtomicReference<ServiceBusTransactionContext> transaction = new AtomicReference<>();
        StepVerifier.create(sender.createTransaction())
            .assertNext(transactionContext -> {
                transaction.set(transactionContext);
                assertNotNull(transaction);
            })
            .verifyComplete();
        StepVerifier.create(sender.scheduleMessage(message, OffsetDateTime.now().plusSeconds(5), transaction.get()))
            .assertNext(sequenceNumber -> {
                assertNotNull(sequenceNumber);
                assertTrue(sequenceNumber.intValue() > 0);
            })
            .verifyComplete();

        StepVerifier.create(sender.commitTransaction(transaction.get()))
            .verifyComplete();
        StepVerifier.create(Mono.delay(scheduleDuration).then(receiver.receiveMessages().next()))
            .assertNext(receivedMessage -> {
                assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                messagesPending.decrementAndGet();
            })
            .verifyComplete();
    }

    /**
     * Verifies that we can create transaction, scheduleMessages and commit.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void transactionScheduleMessagesTest(MessagingEntityType entityType) {

        // Arrange
        final boolean isSessionEnabled = false;
        final int total = 2;
        final Duration shortWait = Duration.ofSeconds(3);

        setSenderAndReceiver(entityType, TestUtils.USE_CASE_SCHEDULE_MESSAGES, isSessionEnabled);

        final Duration scheduleDuration = Duration.ofSeconds(5);
        final String messageId = UUID.randomUUID().toString();
        final List<ServiceBusMessage> messages = new ArrayList<>();
        for (int i = 0; i < total; ++i) {
            messages.add(getMessage(messageId, isSessionEnabled));
        }

        // Assert & Act
        AtomicReference<ServiceBusTransactionContext> transaction = new AtomicReference<>();
        StepVerifier.create(sender.createTransaction())
            .assertNext(transactionContext -> {
                transaction.set(transactionContext);
                assertNotNull(transaction);
            })
            .verifyComplete();

        StepVerifier.create(sender.scheduleMessages(messages, OffsetDateTime.now().plus(scheduleDuration), transaction.get()).collectList())
            .assertNext(longs -> {
                assertEquals(total, longs.size());
            })
            .verifyComplete();

        StepVerifier.create(sender.commitTransaction(transaction.get()))
            .verifyComplete();

        StepVerifier.create(Mono.delay(scheduleDuration).thenMany(receiver.receiveMessages().take(total)))
            .assertNext(receivedMessage -> {
                assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                messagesPending.decrementAndGet();
            })
            .assertNext(receivedMessage -> {
                assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                messagesPending.decrementAndGet();
            })
            .thenAwait(shortWait)
            .thenCancel()
            .verify();
    }

    /**
     * Test cross transaction entity
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void crossEntityTransactionTestWorking(MessagingEntityType entityType) throws InterruptedException {
        // Arrange
        final boolean useCredentials = false;
        final Duration shortTimeout = Duration.ofSeconds(15);
        final int viaIntermediateEntity = TestUtils.USE_CASE_TXN_QUEUE_1;
        //final int destinationEntity = TestUtils.USE_CASE_SEND_VIA_QUEUE_2;
        final boolean isSessionEnabled = false;
        final int total = 2;
        final Duration shortWait = Duration.ofSeconds(3);
        //boolean sharedConnection, int entityIndex, boolean useCredentials, boolean isSessionAware
        //TestConnectionOptions connectionOptions = new TestConnectionOptions(sharedConnection, TestUtils.USE_CASE_TXN_QUEUE_1, useCredentials, isSessionAware);

        //setSenderAndReceiver(entityType, TestUtils.USE_CASE_SCHEDULE_MESSAGES, isSessionEnabled);

        int destination1_Entity = 0;
        int destination2_Entity = 2;
        int destination3_Entity = 3;
        String queue1 = "queue-1"; // sender and receiver
        String queue2 = "queue-2"; // sender
        String queue3 = "queue-3"; // sender
        String queue4 = "queue-4"; // processorClient

        final boolean shareConnection = true;
        final String messageId = UUID.randomUUID().toString();
        final byte[] CONTENTS_BYTES1 = "Some-contents 1".getBytes(StandardCharsets.UTF_8);

        final byte[] CONTENTS_BYTES2 = "Some-contents 2".getBytes(StandardCharsets.UTF_8);
        final byte[] CONTENTS_BYTES3 = "Some-contents 3".getBytes(StandardCharsets.UTF_8);
        final List<ServiceBusMessage> messages1 = TestUtils.getServiceBusMessages(total, messageId, CONTENTS_BYTES1);
        final List<ServiceBusMessage> messages2 = TestUtils.getServiceBusMessages(total, messageId, CONTENTS_BYTES2);
        final List<ServiceBusMessage> messages3 = TestUtils.getServiceBusMessages(total, messageId, CONTENTS_BYTES3);

        ServiceBusClientBuilder builder = getBuilder(useCredentials).enableCrossEntityTransactions();

        AtomicReference<ServiceBusTransactionContext>  transactionContext =  new AtomicReference<>();
        final ServiceBusSenderAsyncClient destination1_Sender = builder
            .sender()
            .queueName(queue1)
            .buildAsyncClient();

        final ServiceBusSenderAsyncClient destination2_Sender = builder
            .sender()
            .queueName(queue2)
            .buildAsyncClient();

        final ServiceBusSenderAsyncClient destination3_Sender = builder
            .sender()
            .queueName(queue3)
            .buildAsyncClient();

        AtomicInteger receivedMessages =  new AtomicInteger();
        // Create an instance of the processor through the ServiceBusClientBuilder
        ServiceBusProcessorClient destination1_processor = builder
            .processor()
            .disableAutoComplete()
            .queueName(queue1)
            .processMessage(context -> {
                ServiceBusReceivedMessage message = context.getMessage();
                System.out.printf("!!!! Test Processor .. Processing message. MessageId: %s, Sequence #: %s. Contents: %s  %s %n", message.getMessageId(),
                    message.getSequenceNumber(), message.getBody(), transactionContext.get());
                if (receivedMessages.get() < 2 ) {
                    context.complete(new CompleteOptions().setTransactionContext(transactionContext.get()));
                    System.out.printf("!!!! Test Processor .. COMPLETED message. MessageId: %s, Sequence #: %s. Contents: %s  %s %n", message.getMessageId(),
                        message.getSequenceNumber(), message.getBody(), transactionContext.get());
                    receivedMessages.incrementAndGet();
                }
            })
            .processError(context -> {
                System.out.printf("!!!! Error when receiving messages from namespace: '%s'. Entity: '%s'%n",
                    context.getFullyQualifiedNamespace(), context.getEntityPath());

                if (!(context.getException() instanceof ServiceBusException)) {
                    System.out.printf("Non-ServiceBusException occurred: %s%n", context.getException());
                    return;
                }
            })
            .buildProcessorClient();
        StepVerifier.create(destination2_Sender.sendMessages(messages1)).verifyComplete();
        System.out.println("!!!! Test sent to queue 2 NO TXN..");

        ServiceBusTransactionContext transactionId = destination1_Sender.createTransaction().block();
        transactionContext.set(transactionId);

        System.out.println("!!!! Test transactionId " + transactionId);
        StepVerifier.create(destination1_Sender.sendMessages(messages1, transactionId)).verifyComplete();
        System.out.println("!!!! Test sent to queue 1 ..");

        destination1_processor.start();
        System.out.println("!!!! Test processor started ..");

        TimeUnit.SECONDS.sleep(8);

        destination3_Sender.sendMessages(messages3, transactionId)
            .then(destination3_Sender.commitTransaction(transactionId)
                .doOnSuccess(a -> {
                    System.out.println("!!!! rollbackTransaction     complete ");
                }))
            .subscribe();
        System.out.println("!!!! rollbackTransaction     processor stop");
        destination1_processor.stop();

        TimeUnit.SECONDS.sleep(4);
        System.out.println("!!!! DONE");
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
        final int viaIntermediateEntity = TestUtils.USE_CASE_TXN_QUEUE_1;
        //final int destinationEntity = TestUtils.USE_CASE_SEND_VIA_QUEUE_2;
        final boolean isSessionEnabled = false;
        final int total = 1;
        final Duration shortWait = Duration.ofSeconds(5);
        final CountDownLatch countdownLatch = new CountDownLatch(1);
        final AtomicInteger receivedMessages = new AtomicInteger();

        int destination1_Entity = 0;
        int destination2_Entity = 2;
        int destination3_Entity = 3;
        String queue1 = "queue-1"; // sender and receiver
        String queue2 = "queue-2"; // sender

        final String messageId = UUID.randomUUID().toString();
        final byte[] CONTENTS_BYTES1 = "Some-contents 1".getBytes(StandardCharsets.UTF_8);

        final List<ServiceBusMessage> messages1 = TestUtils.getServiceBusMessages(total, messageId, CONTENTS_BYTES1);

        ServiceBusClientBuilder builder = getBuilder(useCredentials).enableCrossEntityTransactions();

        AtomicReference<ServiceBusTransactionContext>  transactionContext =  new AtomicReference<>();
        final ServiceBusSenderAsyncClient destination1_Sender = builder
            .sender()
            .queueName(queue1)
            .buildAsyncClient();

        final ServiceBusSenderClient destination2_Sender = builder
            .sender()
            .queueName(queue2)
            .buildClient();

        // Send messages
        StepVerifier.create(destination1_Sender.sendMessages(messages1)).verifyComplete();
        ServiceBusTransactionContext transactionId = destination2_Sender.createTransaction();
        transactionContext.set(transactionId);
        // Create an instance of the processor through the ServiceBusClientBuilder
        ServiceBusProcessorClient destination1_processor = builder
            .processor()
            .disableAutoComplete()
            .queueName(queue1)
            .processMessage(context -> {
                ServiceBusReceivedMessage message = context.getMessage();
                System.out.printf("!!!! Test Processor .. Processing message. MessageId: %s, Sequence #: %s. Contents: %s  %s %n", message.getMessageId(),
                    message.getSequenceNumber(), message.getBody(), transactionContext.get());
                if (receivedMessages.get() == 0) {
                    //Start a transaction
                    //ServiceBusTransactionContext transactionId = destination2_Sender.createTransaction();
                    context.complete(new CompleteOptions().setTransactionContext(transactionContext.get()));
                    destination2_Sender.sendMessage(new ServiceBusMessage("Order Processed"), transactionId);
                    destination2_Sender.commitTransaction(transactionContext.get());
                    logger.verbose("!!!! Transaction completed for Message sequence number [{}].", message.getSequenceNumber());
                    receivedMessages.incrementAndGet();
                    countdownLatch.countDown();
                }
            })
            .processError(context -> {
                System.out.printf("!!!! Error when receiving messages from namespace: '%s'. Entity: '%s'%n",
                    context.getFullyQualifiedNamespace(), context.getEntityPath());

                if (!(context.getException() instanceof ServiceBusException)) {
                    System.out.printf("Non-ServiceBusException occurred: %s%n", context.getException());
                    return;
                }
            })
            .buildProcessorClient();
        System.out.println("Starting the processor");
        destination1_processor.start();

        System.out.println("Listening for 10 seconds...");
        if (countdownLatch.await(10, TimeUnit.SECONDS)) {
            System.out.println("Completed processing, closing processor");
        } else {
            System.out.println("Closing processor.");
        }

        destination1_processor.close();
    }

    /**
     * Test cross transaction entity when transaction is created by processor and processor first receive the message
     *
     * Error : When First Link is receiver and using Subscriber. The corresponding topic is taken as "send-via" queue But
     * when we want to send message in this setup, we get this error. (Confirmed from service team (This could be added as feature in future))
     * "Error occurred. Removing and disposing send link.
     * com.azure.core.amqp.exception.AmqpException: Unauthorized access. 'Send' claim(s) are required to perform this operation. Resource"
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void crossEntityTransactionOnProcessorTest(MessagingEntityType entityType) throws InterruptedException {
        // Arrange
        final boolean useCredentials = false;
        final Duration shortTimeout = Duration.ofSeconds(15);
        final int viaIntermediateEntity = TestUtils.USE_CASE_TXN_QUEUE_1;
        //final int destinationEntity = TestUtils.USE_CASE_SEND_VIA_QUEUE_2;
        final boolean isSessionEnabled = false;
        final int total = 2;
        final Duration shortWait = Duration.ofSeconds(3);
        //boolean sharedConnection, int entityIndex, boolean useCredentials, boolean isSessionAware
        //TestConnectionOptions connectionOptions = new TestConnectionOptions(sharedConnection, TestUtils.USE_CASE_TXN_QUEUE_1, useCredentials, isSessionAware);

        //setSenderAndReceiver(entityType, TestUtils.USE_CASE_SCHEDULE_MESSAGES, isSessionEnabled);

        int destination1_Entity = 0;
        int destination2_Entity = 2;
        int destination3_Entity = 3;
        String queue1 = "queue-1"; // sender and receiver
        String queue2 = "queue-2"; // sender
        String queue3 = "queue-3"; // sender
        String queue4 = "queue-4"; // processorClient
        String topic1 = "topic-1";
        String subscriberName1 = "subscription";

        final boolean shareConnection = true;
        final String messageId = UUID.randomUUID().toString();
        final byte[] CONTENTS_BYTES1 = "Some-contents 1".getBytes(StandardCharsets.UTF_8);

        final byte[] CONTENTS_BYTES2 = "Some-contents 2".getBytes(StandardCharsets.UTF_8);
        final byte[] CONTENTS_BYTES3 = "Some-contents 3".getBytes(StandardCharsets.UTF_8);
        final List<ServiceBusMessage> messages1 = TestUtils.getServiceBusMessages(total, messageId, CONTENTS_BYTES1);
        final List<ServiceBusMessage> messages2 = TestUtils.getServiceBusMessages(total, messageId, CONTENTS_BYTES2);
        final List<ServiceBusMessage> messages3 = TestUtils.getServiceBusMessages(total, messageId, CONTENTS_BYTES3);

        ServiceBusClientBuilder builder = getBuilder(useCredentials).enableCrossEntityTransactions();
        AtomicReference<ServiceBusTransactionContext>  transactionContext =  new AtomicReference<>();

        final ServiceBusSenderAsyncClient destination2_Sender = builder
            .sender()
            .queueName(queue2)
            .buildAsyncClient();

        final ServiceBusSenderAsyncClient destination3_Sender = builder
            .sender()
            .queueName(queue3)
            .buildAsyncClient();

        AtomicInteger messageProcessed = new AtomicInteger();

        // Create an instance of the processor through the ServiceBusClientBuilder
        ServiceBusClientBuilder.ServiceBusProcessorClientBuilder destination1_builder = builder
            .processor()
            .disableAutoComplete();
        if (entityType == MessagingEntityType.QUEUE) {
            destination1_builder.queueName(queue1);
        } else {
            destination1_builder.topicName(topic1).subscriptionName(subscriberName1);
        }

        ServiceBusProcessorClient destination1_processor = destination1_builder
            .processMessage(context -> {
                ServiceBusReceivedMessage message = context.getMessage();
                System.out.printf("!!!! Test Processor .. Processing message. MessageId: %s, Sequence #: %s. Contents: %s  %s %n", message.getMessageId(),
                    message.getSequenceNumber(), message.getBody(), transactionContext.get());

                // We are completing just one message in this test.
                if (messageProcessed.get() == 0 ) {
                    context.complete(new CompleteOptions().setTransactionContext(transactionContext.get()));
                }
                messageProcessed.incrementAndGet();

            })
            .processError(context -> {
                System.out.printf("!!!! Error when receiving messages from namespace: '%s'. Entity: '%s'%n",
                    context.getFullyQualifiedNamespace(), context.getEntityPath());

                if (!(context.getException() instanceof ServiceBusException)) {
                    System.out.printf("Non-ServiceBusException occurred: %s%n", context.getException());
                    return;
                }
            })
            .buildProcessorClient();

        ServiceBusTransactionContext transactionId = destination2_Sender.createTransaction().block();
        transactionContext.set(transactionId);

        System.out.println("!!!! Test transactionId " + transactionId);

        destination1_processor.start();
        System.out.println("!!!! Test processor started ..");

        TimeUnit.SECONDS.sleep(10);
        destination2_Sender.sendMessages(messages2, transactionContext.get()).block(TIMEOUT);
        System.out.println("!!!! Test Processor  sent messages to queue 2");

        destination3_Sender.sendMessages(messages3, transactionContext.get()).block(TIMEOUT);
        System.out.println("!!!! Test Processor  sent messages to queue 3");

        destination2_Sender.commitTransaction(transactionId);

        System.out.println("!!!! commitTransaction     processor stop");

        destination1_processor.stop();

        TimeUnit.SECONDS.sleep(4);
        System.out.println("!!!! DONE");
    }

    /**
     * Test cross transaction entity
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void crossEntityTransactionTestReceiverOnAnotherEntityFails(MessagingEntityType entityType) throws InterruptedException {
        // Arrange
        final boolean useCredentials = false;
        final Duration shortTimeout = Duration.ofSeconds(15);
        final int viaIntermediateEntity = TestUtils.USE_CASE_TXN_QUEUE_1;
        //final int destinationEntity = TestUtils.USE_CASE_SEND_VIA_QUEUE_2;
        final boolean isSessionEnabled = false;
        final int total = 2;
        final Duration shortWait = Duration.ofSeconds(3);
        //boolean sharedConnection, int entityIndex, boolean useCredentials, boolean isSessionAware
        //TestConnectionOptions connectionOptions = new TestConnectionOptions(sharedConnection, TestUtils.USE_CASE_TXN_QUEUE_1, useCredentials, isSessionAware);

        //setSenderAndReceiver(entityType, TestUtils.USE_CASE_SCHEDULE_MESSAGES, isSessionEnabled);

        int destination1_Entity = 0;
        int destination2_Entity = 2;
        int destination3_Entity = 3;
        String queue1 = "queue-1"; // sender and receiver
        String queue2 = "queue-2"; // sender
        String queue3 = "queue-3"; // sender
        String queue4 = "queue-4"; // processorClient

        final boolean shareConnection = true;
        final String messageId = UUID.randomUUID().toString();
        final byte[] CONTENTS_BYTES1 = "Some-contents 1".getBytes(StandardCharsets.UTF_8);

        final byte[] CONTENTS_BYTES2 = "Some-contents 2".getBytes(StandardCharsets.UTF_8);
        final byte[] CONTENTS_BYTES3 = "Some-contents 3".getBytes(StandardCharsets.UTF_8);
        final List<ServiceBusMessage> messages1 = TestUtils.getServiceBusMessages(total, messageId, CONTENTS_BYTES1);
        final List<ServiceBusMessage> messages2 = TestUtils.getServiceBusMessages(total, messageId, CONTENTS_BYTES2);
        final List<ServiceBusMessage> messages3 = TestUtils.getServiceBusMessages(total, messageId, CONTENTS_BYTES3);

        ServiceBusClientBuilder builder = getBuilder(useCredentials).enableCrossEntityTransactions();

        AtomicReference<ServiceBusTransactionContext>  transactionContext =  new AtomicReference<>();
        final ServiceBusSenderAsyncClient destination1_Sender = builder
            .sender()
            .queueName(queue1)
            .buildAsyncClient();

        final ServiceBusSenderAsyncClient destination2_Sender = builder
            .sender()
            .queueName(queue2)
            .buildAsyncClient();

        final ServiceBusSenderAsyncClient destination3_Sender = builder
            .sender()
            .queueName(queue3)
            .buildAsyncClient();

        final ServiceBusReceiverAsyncClient destination2_receiver = builder
            .receiver()
            .queueName(queue2)
            .disableAutoComplete()
            .buildAsyncClient();


        // Create an instance of the processor through the ServiceBusClientBuilder
        /*ServiceBusProcessorClient destination1_processor = builder
            .processor()
            .enableCrossEntityTransactions()
            .disableAutoComplete()
            .queueName(queue1)
            .processMessage(context -> {
                ServiceBusReceivedMessage message = context.getMessage();
                System.out.printf("!!!! Test Processor .. Processing message. MessageId: %s, Sequence #: %s. Contents: %s  %s %n", message.getMessageId(),
                    message.getSequenceNumber(), message.getBody(), transactionContext.get());
                if (message.getSequenceNumber() == 63 ) {
                    context.complete(new CompleteOptions().setTransactionContext(transactionContext.get()));
                }
            })
            .processError(context -> {
                System.out.printf("!!!! Error when receiving messages from namespace: '%s'. Entity: '%s'%n",
                    context.getFullyQualifiedNamespace(), context.getEntityPath());
                if (!(context.getException() instanceof ServiceBusException)) {
                    System.out.printf("Non-ServiceBusException occurred: %s%n", context.getException());
                    return;
                }
            })
            .buildProcessorClient();
        */

        ServiceBusTransactionContext transactionId = destination1_Sender.createTransaction().block();
        transactionContext.set(transactionId);

        System.out.println("!!!! Test transactionId " + transactionId);
        StepVerifier.create(destination1_Sender.sendMessages(messages1, transactionId)).verifyComplete();
        System.out.println("!!!! Test sent to queue 1 ..");

        //destination1_processor.start();
        //System.out.println("!!!! Test processor started ..");

/*
        destination2_Sender
            .sendMessages(messages2, transactionId)
            .block();
        System.out.println("!!!! Test sent to queue 2 .. and now receive from queue 1");
*/
        System.out.println("!!!! Test  now receive from queue 1");
        destination2_receiver.receiveMessages().take(1).flatMap(message-> {
            return destination2_receiver.complete(message, new CompleteOptions().setTransactionContext(transactionId))
                .thenReturn(message);
        }).subscribe(message -> {
            System.out.println("!!!! Test Receiver completed message queue1, SQ " + message.getSequenceNumber() + "  :" + message.getBody().toString());
        });

        TimeUnit.SECONDS.sleep(8);

        destination3_Sender.sendMessages(messages3, transactionId)
            .then(destination3_Sender.commitTransaction(transactionId)
                .doOnSuccess(a -> {
                    System.out.println("!!!! rollbackTransaction     complete " + a);
                }))
            .subscribe();
        System.out.println("!!!! rollbackTransaction.");
        //destination1_processor.stop();

        TimeUnit.SECONDS.sleep(4);
        System.out.println("!!!! DONE");
    }

    /**
     * Verifies that we can schedule messages and cancel them.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void cancelScheduledMessagesTest(MessagingEntityType entityType) {

        // Arrange
        final boolean isSessionEnabled = false;
        final Duration shortWaitTime = Duration.ofSeconds(5);
        final int total = 2;
        setSenderAndReceiver(entityType, TestUtils.USE_CASE_CANCEL_MESSAGES, isSessionEnabled);
        final Duration scheduleDuration = Duration.ofSeconds(15);
        final String messageId = UUID.randomUUID().toString();
        final List<ServiceBusMessage> messages = new ArrayList<>();
        for (int i = 0; i < total; ++i) {
            messages.add(getMessage(messageId, isSessionEnabled));
        }
        List<Long> seqNumbers = sender.scheduleMessages(messages, OffsetDateTime.now().plus(scheduleDuration)).collectList().block(shortWaitTime);

        // Assert & Act
        Assertions.assertNotNull(seqNumbers);
        Assertions.assertEquals(total, seqNumbers.size());

        StepVerifier.create(sender.cancelScheduledMessages(seqNumbers))
            .verifyComplete();

        // The messages should have been cancelled and we should not find any messages.
        StepVerifier.create(receiver.receiveMessages().take(total))
            .verifyTimeout(shortWaitTime);
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
