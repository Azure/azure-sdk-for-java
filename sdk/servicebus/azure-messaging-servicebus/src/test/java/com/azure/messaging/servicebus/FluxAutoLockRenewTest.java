// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.LockContainer;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link FluxAutoLockRenew}.
 */
public class FluxAutoLockRenewTest {

    private static final UUID LOCK_TOKEN_UUID = UUID.randomUUID();
    private static final String LOCK_TOKEN_STRING = LOCK_TOKEN_UUID.toString();
    private static final Duration MAX_AUTO_LOCK_RENEW_DURATION = Duration.ofSeconds(6);
    private static final Duration DISABLE_AUTO_LOCK_RENEW_DURATION = Duration.ofSeconds(0);

    private static final ClientLogger LOGGER = new ClientLogger(FluxAutoLockRenewTest.class);

    private final ServiceBusReceivedMessage receivedMessage = new ServiceBusReceivedMessage(BinaryData.fromString("Some Data"));
    private final ServiceBusMessageContext message = new ServiceBusMessageContext(receivedMessage);
    private final TestPublisher<ServiceBusMessageContext> messagesPublisher = TestPublisher.create();
    private final Flux<? extends ServiceBusMessageContext> messageSource = messagesPublisher.flux();

    private Function<String, Mono<OffsetDateTime>> renewalFunction;

    private OffsetDateTime lockedUntil;
    private AutoCloseable mocksCloseable;
    private ReceiverOptions defaultReceiverOptions;

    @BeforeAll
    public static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    public static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    public void setup() {
        lockedUntil = OffsetDateTime.now().plusSeconds(2);
        receivedMessage.setLockToken(LOCK_TOKEN_UUID);
        receivedMessage.setLockedUntil(lockedUntil);
        renewalFunction = (lockToken) -> Mono.just(OffsetDateTime.now().plusSeconds(10));
        defaultReceiverOptions = new ReceiverOptions(ServiceBusReceiveMode.RECEIVE_AND_DELETE, 1,
            MAX_AUTO_LOCK_RENEW_DURATION, true);
    }

    @AfterEach
    public void teardown() throws Exception {
        Mockito.framework().clearInlineMock(this);
    }

    /**
     * Test that the user can cancel the receive function.
     */
    @Test
    public void canCancel() {
        // Arrange
        final TestContainer messageLockContainer = new TestContainer();

        final ServiceBusReceivedMessage receivedMessage2 = new ServiceBusReceivedMessage(BinaryData.fromString("data"));
        final ServiceBusMessageContext message2 = new ServiceBusMessageContext(receivedMessage2);
        receivedMessage2.setLockToken(UUID.randomUUID());
        receivedMessage2.setLockedUntil(OffsetDateTime.now().plusSeconds(2));

        final FluxAutoLockRenew renewOperator = new FluxAutoLockRenew(messageSource,
            defaultReceiverOptions, messageLockContainer, renewalFunction);

        // Act & Assert
        StepVerifier.create(renewOperator)
            .then(() -> {
                messagesPublisher.next(message);
                messagesPublisher.next(message2);
            })
            .assertNext(actual -> Assertions.assertEquals(LOCK_TOKEN_STRING, actual.getMessage().getLockToken()))
            .thenCancel()
            .verify();

        assertEquals(1, messageLockContainer.addOrUpdateInvocations.size(),
            "should have at least one invocation.");

        assertTrue(messageLockContainer.addOrUpdateInvocations.containsKey(LOCK_TOKEN_STRING));

        final List<LockRenewalOperation> lockRenewalOperations =
            messageLockContainer.addOrUpdateOperations.get(LOCK_TOKEN_STRING);

        assertNotNull(lockRenewalOperations);
        assertEquals(1, lockRenewalOperations.size());
        assertEquals(LockRenewalStatus.CANCELLED, lockRenewalOperations.get(0).getStatus());
    }

    /**
     * Check that illegal values are not allowed in constructor.
     */
    @Test
    public void illegalValueConstructor() {
        // Arrange
        final TestContainer messageLockContainer = new TestContainer();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> new FluxAutoLockRenew(null,
            defaultReceiverOptions, messageLockContainer, renewalFunction));

        assertThrows(NullPointerException.class, () -> new FluxAutoLockRenew(messageSource,
            defaultReceiverOptions, null, renewalFunction));

        assertThrows(NullPointerException.class, () -> new FluxAutoLockRenew(messageSource,
            defaultReceiverOptions, messageLockContainer, null));

        ReceiverOptions zeroLockDurationOptions = new ReceiverOptions(ServiceBusReceiveMode.RECEIVE_AND_DELETE, 1,
            DISABLE_AUTO_LOCK_RENEW_DURATION, true);
        assertThrows(IllegalArgumentException.class, () -> new FluxAutoLockRenew(messageSource,
            zeroLockDurationOptions, messageLockContainer, renewalFunction));

    }

    /**
     * Test that the function to renew lock is invoked. It will verify 1. The renew lock function is invoked multiple
     * times. 2. The updated new lockedUntil is reflected on ServiceBusReceivedMessage object.
     */
    @Test
    public void lockRenewedMultipleTimes() {
        // Arrange
        final int renewedForAtLeast = 3;
        final int totalProcessingTimeSeconds = 5;
        final AtomicInteger actualTokenRenewCalledTimes = new AtomicInteger();
        final Function<String, Mono<OffsetDateTime>> lockTokenRenewFunction = (lockToken) -> {
            actualTokenRenewCalledTimes.getAndIncrement();
            return Mono.just(OffsetDateTime.now().plusSeconds(1));
        };
        final TestContainer messageLockContainer = new TestContainer();

        final FluxAutoLockRenew renewOperator = new FluxAutoLockRenew(messageSource,
            defaultReceiverOptions, messageLockContainer, lockTokenRenewFunction);

        // Act & Assert
        StepVerifier.create(renewOperator.take(1))
            .then(() -> {
                messagesPublisher.next(message);
            })
            .assertNext(actual -> {
                OffsetDateTime previousLockedUntil = actual.getMessage().getLockedUntil();
                try {
                    TimeUnit.SECONDS.sleep(totalProcessingTimeSeconds);
                } catch (InterruptedException e) {
                    LOGGER.warning("Exception while wait. ", e);
                }
                Assertions.assertNotNull(actual);
                Assertions.assertEquals(LOCK_TOKEN_STRING, actual.getMessage().getLockToken());
                Assertions.assertTrue(actual.getMessage().getLockedUntil().isAfter(previousLockedUntil));
            })
            .verifyComplete();

        assertEquals(1, messageLockContainer.addOrUpdateInvocations.get(LOCK_TOKEN_STRING));
        assertTrue(actualTokenRenewCalledTimes.get() >= renewedForAtLeast);
    }

    /**
     * Test if we have error in
     */
    @Test
    void lockRenewedError() {
        // Arrange
        final ErrorLockContainer errorTestContainer = new ErrorLockContainer(LOCK_TOKEN_STRING);
        final FluxAutoLockRenew renewOperator = new FluxAutoLockRenew(messageSource, defaultReceiverOptions,
            errorTestContainer, renewalFunction);

        // Act & Assert
        StepVerifier.create(renewOperator.take(1))
            .then(() -> messagesPublisher.next(message))
            .assertNext(actual -> Assertions.assertEquals(LOCK_TOKEN_STRING, actual.getMessage().getLockToken()))
            .thenCancel()
            .verify();

        assertTrue(errorTestContainer.addOrUpdateInvocations.containsKey(LOCK_TOKEN_STRING));
        assertEquals(1, errorTestContainer.addOrUpdateInvocations.get(LOCK_TOKEN_STRING));
    }

    /**
     * Test if we have ServiceBusReceivedMessageContext with null ServiceBusReceivedMessage, it will never not try to
     * renew lock.
     */
    @Test
    void messageWithError() {
        // Arrange
        final ErrorLockContainer errorTestContainer = new ErrorLockContainer(LOCK_TOKEN_STRING);
        final String expectedSessionId = "1";
        final FluxAutoLockRenew renewOperator = new FluxAutoLockRenew(messageSource,
            defaultReceiverOptions, errorTestContainer, renewalFunction);
        final ServiceBusMessageContext errorContext = new ServiceBusMessageContext(expectedSessionId,
            new RuntimeException("fake error"));

        // Act & Assert
        StepVerifier.create(renewOperator.take(1))
            .then(() -> messagesPublisher.next(errorContext))
            .assertNext(actual -> Assertions.assertEquals(expectedSessionId, actual.getSessionId()))
            .thenCancel()
            .verify();

        assertFalse(errorTestContainer.addOrUpdateInvocations.containsKey(LOCK_TOKEN_STRING),
            "addOrUpdate should not be invoked because the context errored.");
    }

    /**
     * Test when user code throw Exception, onError handler is called.
     */
    @Test
    void messageProcessingThrowException() throws InterruptedException {
        // Arrange
        final int waitForSubscriberSeconds = 6;
        final AtomicBoolean onErrorCalled = new AtomicBoolean(false);
        final AtomicBoolean onCompleteCalled = new AtomicBoolean(false);
        final TestContainer messageLockContainer = new TestContainer();

        final FluxAutoLockRenew renewOperator = new FluxAutoLockRenew(messageSource,
            defaultReceiverOptions, messageLockContainer, renewalFunction);

        // Act
        Disposable disposable = renewOperator
            .subscribe(serviceBusReceivedMessage -> {
                throw new RuntimeException("fake user generated exception.");
            },
                (throwable) -> onErrorCalled.set(true),
                () -> onCompleteCalled.set(true));

        messagesPublisher.next(message);
        TimeUnit.SECONDS.sleep(waitForSubscriberSeconds);

        // Assert
        Assertions.assertTrue(onErrorCalled.get());
        assertFalse(onCompleteCalled.get());

        disposable.dispose();
    }

    @Test
    public void mapperReturnNullValue() {
        // Arrange
        final String expectedMappedValue = null;
        final TestContainer messageLockContainer = new TestContainer();
        final FluxAutoLockRenew renewOperator = new FluxAutoLockRenew(messageSource,
            defaultReceiverOptions, messageLockContainer, renewalFunction);

        // Act & Assert
        StepVerifier.create(renewOperator.map(serviceBusReceivedMessage -> expectedMappedValue))
            .then(() -> {
                messagesPublisher.next(message);
            })
            .verifyError(NullPointerException.class);
    }

    @Test
    public void nullMapperTest() {
        // Arrange
        final TestContainer messageLockContainer = new TestContainer();
        final FluxAutoLockRenew renewOperator = new FluxAutoLockRenew(messageSource,
            defaultReceiverOptions, messageLockContainer, renewalFunction);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> renewOperator.map(null));
    }

    /**
     * Test that the function to renew Renew Operator can be subscribed multiple times.
     */
    @Test
    void renewCanBeSubscribedMultipleTimes() {
        // Arrange
        final TestContainer messageLockContainer = new TestContainer();
        final FluxAutoLockRenew renewOperator = new FluxAutoLockRenew(messageSource,
            defaultReceiverOptions, messageLockContainer, renewalFunction);

        // Act & Assert
        StepVerifier.create(renewOperator.take(1))
            .then(() -> {
                messagesPublisher.next(message);
            })
            .assertNext(actual -> {
                Assertions.assertEquals(LOCK_TOKEN_STRING, actual.getMessage().getLockToken());
            })
            .verifyComplete();

        StepVerifier.create(renewOperator.take(1))
            .then(() -> messagesPublisher.next(message))
            .assertNext(actual -> Assertions.assertEquals(LOCK_TOKEN_STRING, actual.getMessage().getLockToken()))
            .verifyComplete();

        assertEquals(2, messageLockContainer.addOrUpdateInvocations.get(LOCK_TOKEN_STRING));
    }

    /***
     * Ensure user can control backpreassure with simple filter function.
     */
    @Test
    public void simpleFilterAndBackpressured() {
        // Arrange
        final TestContainer messageLockContainer = new TestContainer();

        final ServiceBusReceivedMessage receivedMessage2 = new ServiceBusReceivedMessage(BinaryData.fromString("data"));
        receivedMessage2.setEnqueuedSequenceNumber(2);
        receivedMessage2.setLockToken(UUID.randomUUID());
        receivedMessage2.setLockedUntil(OffsetDateTime.now().plusSeconds(2));
        final ServiceBusMessageContext message2 = new ServiceBusMessageContext(receivedMessage2);

        final ServiceBusReceivedMessage receivedMessage3 = new ServiceBusReceivedMessage(BinaryData.fromString("data"));
        receivedMessage3.setEnqueuedSequenceNumber(3);
        receivedMessage3.setLockToken(UUID.randomUUID());
        receivedMessage3.setLockedUntil(OffsetDateTime.now().plusSeconds(2));
        final ServiceBusMessageContext message3 = new ServiceBusMessageContext(receivedMessage3);


        final FluxAutoLockRenew renewOperator = new FluxAutoLockRenew(messageSource,
            defaultReceiverOptions, messageLockContainer, renewalFunction);

        final Flux<Long> renewOperatorSource = renewOperator
            .filter(actual -> actual.getMessage().getEnqueuedSequenceNumber() > 1)
            .map(messageContext -> messageContext.getMessage().getEnqueuedSequenceNumber());

        // Act & Assert
        StepVerifier.create(renewOperatorSource)
            .expectNextCount(0)
            .thenRequest(1)
            .then(() -> {
                messagesPublisher.next(message, message2, message3);
            })
            .assertNext(actual -> assertEquals(message2.getMessage().getEnqueuedSequenceNumber(), actual))
            .thenRequest(1)
            .assertNext(actual -> assertEquals(message3.getMessage().getEnqueuedSequenceNumber(), actual))
            .thenCancel()
            .verify();
    }

    /***
     * Ensure user can control backpreassure with simple map function.
     */
    @Test
    public void simpleMappingBackpressured() {
        // Arrange
        final TestContainer messageLockContainer = new TestContainer();

        final ServiceBusReceivedMessage receivedMessage2 = new ServiceBusReceivedMessage(BinaryData.fromString("data"));
        receivedMessage2.setLockToken(UUID.randomUUID());
        receivedMessage2.setLockedUntil(OffsetDateTime.now().plusSeconds(2));
        final ServiceBusMessageContext message2 = new ServiceBusMessageContext(receivedMessage2);

        final ServiceBusReceivedMessage receivedMessage3 = new ServiceBusReceivedMessage(BinaryData.fromString("data"));
        receivedMessage3.setLockToken(UUID.randomUUID());
        receivedMessage3.setLockedUntil(OffsetDateTime.now().plusSeconds(2));
        final ServiceBusMessageContext message3 = new ServiceBusMessageContext(receivedMessage3);


        final String expectedMappedValue = "New Expected Mapped Value";
        final FluxAutoLockRenew renewOperator = new FluxAutoLockRenew(messageSource,
            defaultReceiverOptions, messageLockContainer, renewalFunction);

        final Flux<String> renewOperatorSource = renewOperator.map(serviceBusReceivedMessage -> expectedMappedValue);

        // Act & Assert
        StepVerifier.create(renewOperatorSource)
            .expectNextCount(0)
            .thenRequest(1)
            .then(() -> messagesPublisher.next(message, message2, message3))
            .assertNext(actual -> Assertions.assertEquals(expectedMappedValue, actual))
            .thenRequest(1)
            .assertNext(actual -> Assertions.assertEquals(expectedMappedValue, actual))
            .thenCancel()
            .verify();
    }

    /***
     * Ensure user can apply filter operator and  simple map function.
     */
    @Test
    public void simpleMappingAndFilter() {
        // Arrange
        final TestContainer messageLockContainer = new TestContainer();

        final Long expectedEnqueuedSequenceNumber = 2L;
        final OffsetDateTime lockedUntil = OffsetDateTime.now().plusSeconds(1);
        final ServiceBusReceivedMessage receivedMessage2 = new ServiceBusReceivedMessage(BinaryData.fromString("data"));
        final ServiceBusMessageContext message2 = new ServiceBusMessageContext(receivedMessage2);
        receivedMessage2.setLockToken(UUID.randomUUID());
        receivedMessage2.setLockedUntil(lockedUntil);
        receivedMessage2.setEnqueuedSequenceNumber(1);

        final ServiceBusReceivedMessage receivedMessage3 = new ServiceBusReceivedMessage(BinaryData.fromString("data"));
        final ServiceBusMessageContext message3 = new ServiceBusMessageContext(receivedMessage3);
        receivedMessage2.setLockToken(UUID.randomUUID());
        receivedMessage2.setLockedUntil(lockedUntil);
        receivedMessage2.setEnqueuedSequenceNumber(expectedEnqueuedSequenceNumber);

        final FluxAutoLockRenew renewOperator = new FluxAutoLockRenew(messageSource,
            defaultReceiverOptions, messageLockContainer, renewalFunction);

        // Act & Assert
        StepVerifier.create(renewOperator
                .filter(actual -> actual.getMessage().getEnqueuedSequenceNumber() > 1)
                .map(messageContext -> messageContext.getMessage().getEnqueuedSequenceNumber()))
            .thenRequest(1)
            .then(() -> messagesPublisher.next(message, message2, message3))
            .assertNext(actualEnqueuedSequenceNumber -> assertEquals(expectedEnqueuedSequenceNumber, actualEnqueuedSequenceNumber))
            .thenCancel()
            .verify();
    }

    @Test
    public void contextPropagationTest() {
        // Arrange
        final TestContainer messageLockContainer = new TestContainer();

        final FluxAutoLockRenew renewOperator = new FluxAutoLockRenew(messageSource,
            defaultReceiverOptions, messageLockContainer, renewalFunction);

        // Act & Assert
        StepVerifier.create(renewOperator
                .contextWrite((context) -> context.put("A", "B")))
            .thenRequest(1)
            .expectAccessibleContext()
            .contains("A", "B")
            .hasSize(1)
            .then()
            .then(() -> messagesPublisher.next(message))
            .expectNext(message)
            .thenCancel()
            .verify();
    }

    /***
     * When auto complete is disabled by user, we do not perform message lock clean up.
     */
    @Test
    void autoCompleteDisabledLockRenewNotClosed() {
        // Arrange
        final TestContainer messageLockContainer = new TestContainer();

        final boolean enableAutoComplete = false;
        final int totalProcessingTimeSeconds = 2;
        final int renewedForAtLeast = 2;
        final AtomicInteger actualTokenRenewCalledTimes = new AtomicInteger();
        final Function<String, Mono<OffsetDateTime>> lockTokenRenewFunction = (lockToken) -> {
            actualTokenRenewCalledTimes.getAndIncrement();
            return Mono.just(OffsetDateTime.now().plusSeconds(1));
        };
        ReceiverOptions receiverOptions = new ReceiverOptions(ServiceBusReceiveMode.RECEIVE_AND_DELETE, 1,
            MAX_AUTO_LOCK_RENEW_DURATION, enableAutoComplete);
        final FluxAutoLockRenew renewOperator = new FluxAutoLockRenew(messageSource,
            receiverOptions, messageLockContainer, lockTokenRenewFunction);

        // Act & Assert
        StepVerifier.create(renewOperator.take(1))
            .then(() -> {
                messagesPublisher.next(message);
            })
            .assertNext(actual -> {
                OffsetDateTime previousLockedUntil = actual.getMessage().getLockedUntil();
                try {
                    TimeUnit.SECONDS.sleep(totalProcessingTimeSeconds);
                } catch (InterruptedException e) {
                    LOGGER.warning("Exception while wait. ", e);
                }
                Assertions.assertNotNull(actual);
                Assertions.assertEquals(LOCK_TOKEN_STRING, actual.getMessage().getLockToken());
                Assertions.assertTrue(actual.getMessage().getLockedUntil().isAfter(previousLockedUntil));
            })
            .verifyComplete();

        assertEquals(1, messageLockContainer.addOrUpdateInvocations.get(LOCK_TOKEN_STRING));
        assertTrue(actualTokenRenewCalledTimes.get() >= renewedForAtLeast);

        // ensure that we do not remove lockToken from 'messageLockContainer' because user can do it at their will since
        // enableAutoComplete = false
        assertEquals(0, messageLockContainer.removeInvocations.size());
    }

    /**
     * Exists so that Mockito doesn't fall over with not a mock exception.
     */
    private static class TestContainer extends LockContainer<LockRenewalOperation> {
        private final HashMap<String, Integer> addOrUpdateInvocations = new HashMap<>();
        private final HashMap<String, List<LockRenewalOperation>> addOrUpdateOperations = new HashMap<>();
        private final HashMap<String, Integer> removeInvocations = new HashMap<>();

        TestContainer() {
            super(Duration.ofSeconds(60));
        }

        @Override
        public OffsetDateTime addOrUpdate(String lockToken, OffsetDateTime lockTokenExpiration, LockRenewalOperation item) {
            addOrUpdateInvocations.compute(lockToken, (existingKey, existingValue) -> {
                if (existingValue == null) {
                    return 1;
                } else {
                    return existingValue + 1;
                }
            });

            addOrUpdateOperations.compute(lockToken, (existingKey, existingValue) -> {
                if (existingValue == null) {
                    ArrayList<LockRenewalOperation> operations = new ArrayList<>();
                    operations.add(item);
                    return operations;
                } else {
                    existingValue.add(item);
                    return existingValue;
                }
            });

            return super.addOrUpdate(lockToken, lockTokenExpiration, item);
        }

        @Override
        public void remove(String lockToken) {
            removeInvocations.compute(lockToken,
                (key, existingValue) -> existingValue == null ? 1 : existingValue + 1);

            super.remove(lockToken);
        }
    }

    /**
     * Exists so that Mockito doesn't fall over with not a mock exception.
     */
    private static class ErrorLockContainer extends LockContainer<LockRenewalOperation> {
        private final AmqpException error = new AmqpException(false, "test-exception", new AmqpErrorContext("namespace"));
        private final HashMap<String, Integer> addOrUpdateInvocations = new HashMap<>();
        private final String matchingLockToken;

        ErrorLockContainer(String matchingLockToken) {
            super(Duration.ofSeconds(60));
            this.matchingLockToken = matchingLockToken;
        }

        @Override
        public OffsetDateTime addOrUpdate(String lockToken, OffsetDateTime lockTokenExpiration, LockRenewalOperation item) {
            addOrUpdateInvocations.compute(lockToken, (existingKey, existingValue) -> {
                if (existingValue == null) {
                    return 1;
                } else {
                    return existingValue + 1;
                }
            });

            if (this.matchingLockToken.equals(lockToken)) {
                throw error;
            } else {
                return super.addOrUpdate(lockToken, lockTokenExpiration, item);
            }
        }
    }
}
