// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.OffsetTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.messaging.servicebus.TestUtils.getServiceBusMessage;
import static com.azure.messaging.servicebus.TestUtils.getSessionSubscriptionBaseName;
import static com.azure.messaging.servicebus.TestUtils.getSubscriptionBaseName;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for {@link ServiceBusProcessorClient}.
 */

public class ServiceBusProcessorClientIntegrationTest extends IntegrationTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusProcessorClientIntegrationTest.class);
    private final AtomicInteger messagesPending = new AtomicInteger();
    private final Scheduler scheduler = Schedulers.parallel();

    @Override
    protected void beforeTest() {
        sessionId = CoreUtils.randomUuid().toString();
    }

    @Override
    protected void afterTest() {
        sharedBuilder = null;
    }

    ServiceBusProcessorClientIntegrationTest() {
        super(new ClientLogger(ServiceBusProcessorClientIntegrationTest.class));
    }

    /**
     * Validate that processor receive the message and {@code MaxAutoLockRenewDuration} is set on the
     * {@link ServiceBusReceiverAsyncClient}. The message lock is released by the client and same message received
     * again.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void receiveMessage(MessagingEntityType entityType, boolean isSessionEnabled) throws InterruptedException {
        // Arrange
        // The message is locked for this duration at a time.
        final int lockTimeoutDurationSeconds = 15;
        final int entityIndex = TestUtils.USE_CASE_PROCESSOR_RECEIVE;
        final Duration expectedMaxAutoLockRenew = Duration.ofSeconds(35);

        final String messageId = UUID.randomUUID().toString();
        final AtomicReference<OffsetTime> lastMessageReceivedTime = new AtomicReference<>();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled).setMessageId(messageId);

        // The message should comeback after the client release the lock once because maxAutoLockRenewDuration is set
        // by user.
        CountDownLatch countDownLatch = new CountDownLatch(2);

        // send the message
        ServiceBusSenderAsyncClient sender = createSender(entityType, entityIndex, isSessionEnabled);
        sendMessage(sender, message).block(TIMEOUT);

        ServiceBusProcessorClient processor;
        if (isSessionEnabled) {
            assertNotNull(sessionId, "'sessionId' should have been set.");
            AmqpRetryOptions amqpRetryOptions = new AmqpRetryOptions()
                .setTryTimeout(Duration.ofSeconds(2 * lockTimeoutDurationSeconds));
            processor = toClose(getSessionProcessorBuilder(entityType, entityIndex, false, amqpRetryOptions)
                .maxAutoLockRenewDuration(expectedMaxAutoLockRenew)
                .disableAutoComplete()
                .processMessage(context -> processMessage(context, countDownLatch, messageId, lastMessageReceivedTime, lockTimeoutDurationSeconds))
                .processError(context -> processError(context, countDownLatch))
                .buildProcessorClient());

        } else {
            processor = toClose(getProcessorBuilder(entityType, entityIndex, false)
                .maxAutoLockRenewDuration(expectedMaxAutoLockRenew)
                .disableAutoComplete()
                .processMessage(context -> processMessage(context, countDownLatch, messageId, lastMessageReceivedTime, lockTimeoutDurationSeconds))
                .processError(context -> processError(context, countDownLatch))
                .buildProcessorClient());
        }

        // Assert & Act
        processor.start();
        toClose((AutoCloseable) () -> processor.stop());

        assertTrue(countDownLatch.await(lockTimeoutDurationSeconds * 6, TimeUnit.SECONDS), "Message not arrived, closing processor.");
        LOGGER.info("Message lock has been renewed. Now closing processor");
    }

    @ParameterizedTest
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    void rollingSessionOnIdleTimeout(MessagingEntityType entityType) throws InterruptedException {
        final int entityIndex = TestUtils.USE_CASE_MULTIPLE_SESSIONS1;
        final Duration sessionIdleTimeout = Duration.ofSeconds(3);

        ServiceBusSenderAsyncClient sender = createSender(entityType, entityIndex, true);

        ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder  processorBuilder =
            getSessionProcessorBuilder(entityType, entityIndex, false, RETRY_OPTIONS)
                .sessionIdleTimeout(sessionIdleTimeout)
                .disableAutoComplete();

        rollingSessionTest(sender, processorBuilder);
    }

    @ParameterizedTest
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    void rollingSessionOnTryTimeout(MessagingEntityType entityType) throws InterruptedException {
        final int entityIndex = TestUtils.USE_CASE_MULTIPLE_SESSIONS1;
        final Duration tryTimeout = Duration.ofSeconds(3);

        ServiceBusSenderAsyncClient sender = createSender(entityType, entityIndex, true);
        ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder  processorBuilder =
            getSessionProcessorBuilder(entityType, entityIndex, false,
                    new AmqpRetryOptions().setTryTimeout(tryTimeout))
                .disableAutoComplete();

        rollingSessionTest(sender, processorBuilder);
    }

    void rollingSessionTest(ServiceBusSenderAsyncClient sender, ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder processorBuilder) throws InterruptedException {
        final String contents = "Some-contents";
        final String randomPrefix = UUID.randomUUID().toString();
        ServiceBusMessage message0 = getServiceBusMessage(contents, randomPrefix + "0").setSessionId(randomPrefix + "0");
        ServiceBusMessage message1 = getServiceBusMessage(contents, randomPrefix + "1").setSessionId(randomPrefix + "1");

        CountDownLatch latch = new CountDownLatch(2);
        ServiceBusProcessorClient processor = toClose(processorBuilder
            .processMessage(context -> {
                ServiceBusReceivedMessage received = context.getMessage();
                context.complete();

                if (received.getMessageId().startsWith(randomPrefix)) {
                    latch.countDown();
                    if (message0.getMessageId().equals(received.getMessageId())) {
                        sendMessage(sender, message1).block();
                    }
                }
            })
            .processError(context -> fail(context.getException()))
            .buildProcessorClient());

        processor.start();
        sendMessage(sender, message0).block();

        toClose((AutoCloseable) () -> processor.stop());

        assertTrue(latch.await(20, TimeUnit.SECONDS), "Messages did not arrived, closing processor.");
    }

    private void processMessage(ServiceBusReceivedMessageContext context, CountDownLatch countDownLatch,
        String expectedMessageId, AtomicReference<OffsetTime> lastMessageReceivedTime, int lockTimeoutDurationSeconds) {
        ServiceBusReceivedMessage message = context.getMessage();
        if (message.getMessageId().equals(expectedMessageId)) {
            LOGGER.info("Processing message. Session: {}, Sequence #: {}. Contents: {}", message.getMessageId(),
                message.getSequenceNumber(), message.getBody());
            if (lastMessageReceivedTime.get() ==  null) {
                lastMessageReceivedTime.set(OffsetTime.now());
                countDownLatch.countDown();
            } else {
                long messageReceivedAfterSeconds = Duration.between(lastMessageReceivedTime.get(), OffsetTime.now()).getSeconds();
                LOGGER.info("Processing message again. Session: {}, Sequence #: {}. Contents: {}, message received after {} seconds.", message.getMessageId(),
                    message.getSequenceNumber(), message.getBody(), messageReceivedAfterSeconds);
                // Ensure that the lock is renewed and message is received again after atlest one lock renew
                if (messageReceivedAfterSeconds >= 2 * lockTimeoutDurationSeconds) {
                    countDownLatch.countDown();
                }
            }
        } else {
            LOGGER.info("Received message, message id did not match. Session: %s, Sequence #: %s. Contents: %s%n", message.getMessageId(),
                message.getSequenceNumber(), message.getBody());
        }
    }

    private void processError(ServiceBusErrorContext context, CountDownLatch countdownLatch) {
        LOGGER.info("Error when receiving messages from namespace: {}. Entity: {}}",
            context.getFullyQualifiedNamespace(), context.getEntityPath());
    }

    private ServiceBusClientBuilder.ServiceBusProcessorClientBuilder getProcessorBuilder(MessagingEntityType entityType,
        int entityIndex, boolean sharedConnection) {

        ServiceBusClientBuilder builder = getBuilder(sharedConnection);
        switch (entityType) {
            case QUEUE:
                final String queueName = getQueueName(entityIndex);
                assertNotNull(queueName, "'queueName' cannot be null.");

                return builder.processor().queueName(queueName);
            case SUBSCRIPTION:
                final String topicName = getTopicName(entityIndex);
                final String subscriptionName = getSubscriptionBaseName();
                assertNotNull(topicName, "'topicName' cannot be null.");
                assertNotNull(subscriptionName, "'subscriptionName' cannot be null.");

                return builder.processor().topicName(topicName).subscriptionName(subscriptionName);
            default:
                throw logger.logExceptionAsError(new IllegalArgumentException("Unknown entity type: " + entityType));
        }
    }

    private ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder getSessionProcessorBuilder(MessagingEntityType entityType,
        int entityIndex, boolean sharedConnection, AmqpRetryOptions amqpRetryOptions) {

        ServiceBusClientBuilder builder = getBuilder(sharedConnection);
        builder.retryOptions(amqpRetryOptions);

        switch (entityType) {
            case QUEUE:
                final String queueName = getSessionQueueName(entityIndex);
                assertNotNull(queueName, "'queueName' cannot be null.");
                return builder
                    .sessionProcessor()
                    .queueName(queueName);

            case SUBSCRIPTION:
                final String topicName = getTopicName(entityIndex);
                final String subscriptionName = getSessionSubscriptionBaseName();
                assertNotNull(topicName, "'topicName' cannot be null.");
                assertNotNull(subscriptionName, "'subscriptionName' cannot be null.");
                return builder.sessionProcessor()
                    .topicName(topicName).subscriptionName(subscriptionName);
            default:
                throw logger.logExceptionAsError(new IllegalArgumentException("Unknown entity type: " + entityType));
        }
    }

    private ServiceBusSenderAsyncClient createSender(MessagingEntityType entityType, int entityIndex, boolean isSessionEnabled) {
        final boolean shareConnection = false;
        return toClose(getSenderBuilder(entityType, entityIndex, isSessionEnabled, shareConnection)
            .buildAsyncClient());
    }

    private Mono<Void> sendMessage(ServiceBusSenderAsyncClient sender, ServiceBusMessage message) {
        return sender.sendMessage(message).doOnSuccess(aVoid -> {
            logMessage(message, sender.getEntityPath(), "sent");
            int number = messagesPending.incrementAndGet();
            LOGGER.atInfo().addKeyValue("number", number).log("Number sent");
        });
    }
}
