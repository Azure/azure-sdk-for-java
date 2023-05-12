// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.azure.messaging.servicebus.TestUtils.getServiceBusMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Integration tests for {@link ServiceBusSessionManager}.
 */
@Tag("integration")
class ServiceBusSessionManagerIntegrationTest extends IntegrationTestBase {
    private static final AmqpRetryOptions DEFAULT_RETRY_OPTIONS = null;
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
                return sender.sendMessage(message).thenReturn(index);
            })
            .subscribe(
                number -> logger.info("sessionId[{}] sent[{}] Message sent.", sessionId, number),
                error -> logger.error("sessionId[{}] Error encountered.", sessionId, error),
                () -> logger.info("sessionId[{}] Finished sending.", sessionId));
        toClose(subscription);
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

    @ParameterizedTest
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    void rollingSessionOnIdleTimeout(MessagingEntityType entityType) throws InterruptedException {
        final int entityIndex = TestUtils.USE_CASE_MULTIPLE_SESSIONS1;
        final Duration sessionIdleTimeout = Duration.ofSeconds(3);
        setSender(entityType, entityIndex);

        this.receiver = toClose(getSessionReceiverBuilder(false,
            entityType, entityIndex, false, DEFAULT_RETRY_OPTIONS)
            .disableAutoComplete()
            .maxConcurrentSessions(1)
            .sessionIdleTimeout(sessionIdleTimeout)
            .buildAsyncClientForProcessor());

        rollingSessionTest();
    }

    @ParameterizedTest
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    void rollingSessionOnTryTimeout(MessagingEntityType entityType) throws InterruptedException {
        final int entityIndex = TestUtils.USE_CASE_MULTIPLE_SESSIONS3;
        final Duration tryTimeout = Duration.ofSeconds(3);
        final AmqpRetryOptions retryOptions = new AmqpRetryOptions().setTryTimeout(tryTimeout);
        setSender(entityType, entityIndex);

        this.receiver = toClose(getSessionReceiverBuilder(false,
            entityType, entityIndex, false, retryOptions)
            .disableAutoComplete()
            .maxConcurrentSessions(1)
            .buildAsyncClientForProcessor());

        rollingSessionTest();
    }

    private void rollingSessionTest() throws InterruptedException {
        final String contents = "Some-contents";
        final String randomPrefix = UUID.randomUUID().toString();
        ServiceBusMessage message0 = getServiceBusMessage(contents, randomPrefix + "0").setSessionId(randomPrefix + "0");
        ServiceBusMessage message1 = getServiceBusMessage(contents, randomPrefix + "1").setSessionId(randomPrefix + "1");

        CountDownLatch latch = new CountDownLatch(2);
        toClose(sender.sendMessage(message0)
            .thenMany(receiver.receiveMessages())
            .flatMap(m -> receiver.complete(m).thenReturn(m))
            .filter(m -> m.getMessageId().startsWith(randomPrefix))
            .flatMap(m ->
                (message0.getMessageId().equals(m.getMessageId()))
                    ? sender.sendMessage(message1).thenReturn(m) : Mono.just(m)
            )
            .subscribe(m -> latch.countDown(), ex -> fail(ex)));

        assertTrue(latch.await(20, TimeUnit.SECONDS));
    }
    @ParameterizedTest
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    void noRollingSessionWhenNoConcurrentSessions(MessagingEntityType entityType) {
        final int entityIndex = TestUtils.USE_CASE_MULTIPLE_SESSIONS2;
        final String contents = "Some-contents";
        final Duration sessionIdleTimeout = Duration.ofSeconds(3);
        final Duration tryTimeout = Duration.ofSeconds(10);
        setSender(entityType, entityIndex);
        final String randomPrefix = UUID.randomUUID().toString();
        ServiceBusMessage message0 = getServiceBusMessage(contents, randomPrefix + "0").setSessionId(randomPrefix + "0");
        ServiceBusMessage message1 = getServiceBusMessage(contents, randomPrefix + "1").setSessionId(randomPrefix + "1");

        AmqpRetryOptions retryOptions = new AmqpRetryOptions().setTryTimeout(tryTimeout);
        this.receiver = toClose(getSessionReceiverBuilder(false,
            entityType, entityIndex, false, retryOptions)
            .disableAutoComplete()
            .sessionIdleTimeout(sessionIdleTimeout)
            .buildAsyncClientForProcessor());

        AtomicReference<String> sessionId = new AtomicReference<>();
        StepVerifier.create(
                sender.sendMessage(message0)
                    .thenMany(receiver.receiveMessages())
                    .flatMap(m -> receiver.complete(m).thenReturn(m))
                    .flatMap(m -> {
                        if (!sessionId.compareAndSet(null, m.getSessionId())) {
                            assertEquals(sessionId.get(), m.getSessionId(), "session rolling should not happen");
                        }
                        return sender.sendMessage(message1).thenReturn(m);
                    })
            )
            .expectNextCount(1)
            .verifyTimeout(tryTimeout.plusSeconds(20));
    }

    /**
     * Sets the sender and receiver. If session is enabled, then a single-named session receiver is created.
     */
    private void setSender(MessagingEntityType entityType, int entityIndex) {
        this.sender = toClose(getSenderBuilder(false, entityType, entityIndex, true, false)
            .buildAsyncClient());
    }

    private void setReceiver(MessagingEntityType entityType, int entityIndex,
                             Function<ServiceBusSessionReceiverClientBuilder, ServiceBusSessionReceiverClientBuilder> onBuild) {
        ServiceBusSessionReceiverClientBuilder sessionBuilder = getSessionReceiverBuilder(false,
            entityType, entityIndex, false, DEFAULT_RETRY_OPTIONS).disableAutoComplete();

        this.sessionReceiver = toClose(onBuild.apply(sessionBuilder).buildAsyncClient());
        this.receiver = toClose(this.sessionReceiver.acceptSession(sessionId).block());
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
