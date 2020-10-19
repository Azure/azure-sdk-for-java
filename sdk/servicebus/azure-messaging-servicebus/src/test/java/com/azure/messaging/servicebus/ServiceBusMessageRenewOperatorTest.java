// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.LockContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;
import reactor.util.context.Context;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link ServiceBusMessageRenewOperator}.
 */
public class ServiceBusMessageRenewOperatorTest {

    private static final UUID LOCK_TOKEN_UUID = UUID.randomUUID();
    private static final String LOCK_TOKEN_STRING = LOCK_TOKEN_UUID.toString();
    private static final Duration MAX_AUTO_LOCK_RENEW_DURATION = Duration.ofSeconds(6);
    private static final Duration DISABLE_AUTO_LOCK_RENEW_DURATION = Duration.ofSeconds(0);

    private final ClientLogger logger = new ClientLogger(ServiceBusMessageRenewOperatorTest.class);

    private final ServiceBusReceivedMessage message = new ServiceBusReceivedMessage("Some Data".getBytes());
    private final int durationToRenewLockForSeconds = 1;
    private final TestPublisher<ServiceBusReceivedMessage> messagesPublisher = TestPublisher.create();
    private final Flux<? extends ServiceBusReceivedMessage> messageSource = messagesPublisher.flux();

    private OffsetDateTime lockedUntil;

    @Mock
    LockContainer<LockRenewalOperation> messageLockContainer;
    @Mock
    private Function<String, Mono<OffsetDateTime>> renewalFunction;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        lockedUntil = OffsetDateTime.now().plusSeconds(2);
        message.setLockToken(LOCK_TOKEN_UUID);
        message.setLockedUntil(lockedUntil);

        when(renewalFunction.apply(LOCK_TOKEN_STRING))
            .thenReturn(Mono.fromCallable(() -> message.getLockedUntil().plusSeconds(durationToRenewLockForSeconds)));
    }

    @AfterEach
    void teardown() {
        Mockito.framework().clearInlineMocks();
    }

    /**
     * Test that the user can cancel the receive function.
     */
    @Test
    void canCancel() {
        // Arrange
        final ServiceBusReceivedMessage message2 = new ServiceBusReceivedMessage("data".getBytes());
        message2.setLockToken(UUID.randomUUID());
        message2.setLockedUntil(OffsetDateTime.now().plusSeconds(2));

        final ServiceBusMessageRenewOperator renewOperator = new ServiceBusMessageRenewOperator(messageSource,
            MAX_AUTO_LOCK_RENEW_DURATION, messageLockContainer, renewalFunction);

        // Act & Assert
        StepVerifier.create(renewOperator)
            .then(() -> {
                messagesPublisher.next(message);
                messagesPublisher.next(message2);
            })
            .assertNext(actual -> Assertions.assertEquals(LOCK_TOKEN_STRING, actual.getLockToken()))
            .thenCancel()
            .verify();

    }

    @Test
    public void canMap() {
        // Arrange
        final String expectedMappedValue = "New Expected Mapped Value";
        final ServiceBusMessageRenewOperator renewOperator = new ServiceBusMessageRenewOperator(messageSource,
            MAX_AUTO_LOCK_RENEW_DURATION, messageLockContainer, renewalFunction);

        // Act & Assert
        StepVerifier.create(renewOperator.map(serviceBusReceivedMessage -> expectedMappedValue))
            .then(() -> {
                messagesPublisher.next(message);
            })
            .assertNext(actual -> Assertions.assertEquals(expectedMappedValue, actual))
            .thenCancel()
            .verify();
    }

    /**
     * Check that illegal values are not allowed in constructor.
     */
    @Test
    void illegalValueConstructor() {
        // Arrange, Act & Assert
        assertThrows(NullPointerException.class, () -> new ServiceBusMessageRenewOperator(null,
            MAX_AUTO_LOCK_RENEW_DURATION, messageLockContainer, renewalFunction));

        assertThrows(NullPointerException.class, () -> new ServiceBusMessageRenewOperator(messageSource,
            MAX_AUTO_LOCK_RENEW_DURATION, null, renewalFunction));

        assertThrows(NullPointerException.class, () -> new ServiceBusMessageRenewOperator(messageSource,
            MAX_AUTO_LOCK_RENEW_DURATION, messageLockContainer, null));

        assertThrows(IllegalArgumentException.class, () -> new ServiceBusMessageRenewOperator(messageSource,
            DISABLE_AUTO_LOCK_RENEW_DURATION, messageLockContainer, renewalFunction));

    }

    /**
     * Test that the function to renew lock is invoked. It will verify
     * 1. The renew lock function is invoked multiple times.
     * 2. The updated new lockedUntil is reflected on ServiceBusReceivedMessage object.
     */
    @Test
    void lockRenewedMultipleTimes() {
        // Arrange
        final int renewedForAtLeast = 3;
        final int totalProcessingTimeSeconds = 5;

        final ServiceBusMessageRenewOperator renewOperator = new ServiceBusMessageRenewOperator(messageSource,
            MAX_AUTO_LOCK_RENEW_DURATION, messageLockContainer, renewalFunction);

        // Act & Assert
        StepVerifier.create(renewOperator.take(1))
            .then(() -> {
                messagesPublisher.next(message);
            })
            .assertNext(actual -> {
                OffsetDateTime previousLockedUntil = actual.getLockedUntil();
                try {
                    TimeUnit.SECONDS.sleep(totalProcessingTimeSeconds);
                } catch (InterruptedException e) {
                    logger.warning("Exception while wait. ", e);
                }
                Assertions.assertEquals(LOCK_TOKEN_STRING, actual.getLockToken());
                Assertions.assertTrue(actual.getLockedUntil().isAfter(previousLockedUntil));
            })
            .verifyComplete();

        verify(messageLockContainer, times(1)).addOrUpdate(eq(LOCK_TOKEN_STRING), any(OffsetDateTime.class), any(LockRenewalOperation.class));
        verify(renewalFunction, atLeast(renewedForAtLeast)).apply(eq(LOCK_TOKEN_STRING));

    }

    /**
     * Test if we have error in
     */
    @Test
    void lockRenewedError() {
        // Arrange
        final ServiceBusMessageRenewOperator renewOperator = new ServiceBusMessageRenewOperator(messageSource,
            MAX_AUTO_LOCK_RENEW_DURATION, messageLockContainer, renewalFunction);

        when(messageLockContainer.addOrUpdate(eq(LOCK_TOKEN_STRING), any(OffsetDateTime.class), any(LockRenewalOperation.class)))
            .thenThrow(new RuntimeException("contained closed."));

        // Act & Assert
        StepVerifier.create(renewOperator.take(1))
            .then(() -> {
                messagesPublisher.next(message);
            })
            .verifyError(RuntimeException.class);

        verify(messageLockContainer, times(1)).addOrUpdate(eq(LOCK_TOKEN_STRING), any(OffsetDateTime.class), any(LockRenewalOperation.class));

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

        final ServiceBusMessageRenewOperator renewOperator = new ServiceBusMessageRenewOperator(messageSource,
            MAX_AUTO_LOCK_RENEW_DURATION, messageLockContainer, renewalFunction);

        // Act
        Disposable disposable = renewOperator
            .subscribe(serviceBusReceivedMessage -> {
                System.out.println("!!!! Test  throw runtime exception ");
                    throw new RuntimeException("fake user generated exception.");
                    },
                throwable -> onErrorCalled.set(true),
                () -> onCompleteCalled.set(true));

        messagesPublisher.next(message);
        TimeUnit.SECONDS.sleep(waitForSubscriberSeconds);

        // Assert
        Assertions.assertTrue(onErrorCalled.get());
        Assertions.assertFalse(onCompleteCalled.get());

        disposable.dispose();
    }

    @Test
    public void mapperReturnNullValue() {
        // Arrange
        final String expectedMappedValue = null;
        final ServiceBusMessageRenewOperator renewOperator = new ServiceBusMessageRenewOperator(messageSource,
            MAX_AUTO_LOCK_RENEW_DURATION, messageLockContainer, renewalFunction);

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
        final ServiceBusMessageRenewOperator renewOperator = new ServiceBusMessageRenewOperator(messageSource,
            MAX_AUTO_LOCK_RENEW_DURATION, messageLockContainer, renewalFunction);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> renewOperator.map(null));
    }

    /***
     * Ensure user can control backpreassure with simple filter function.
     */
    @Test
    public void simpleFilterAndBackpressured() {
        // Arrange
        final ServiceBusReceivedMessage message2 = new ServiceBusReceivedMessage("data".getBytes());
        message2.setEnqueuedSequenceNumber(2);
        message2.setLockToken(UUID.randomUUID());
        message2.setLockedUntil(OffsetDateTime.now().plusSeconds(2));

        final ServiceBusReceivedMessage message3 = new ServiceBusReceivedMessage("data".getBytes());
        message3.setEnqueuedSequenceNumber(3);
        message3.setLockToken(UUID.randomUUID());
        message3.setLockedUntil(OffsetDateTime.now().plusSeconds(2));

        final ServiceBusMessageRenewOperator renewOperator = new ServiceBusMessageRenewOperator(messageSource,
            MAX_AUTO_LOCK_RENEW_DURATION, messageLockContainer, renewalFunction);

        final Flux<Long> renewOperatorSource = renewOperator
            .filter(actual -> actual.getEnqueuedSequenceNumber() > 1)
            .map(ServiceBusReceivedMessage::getEnqueuedSequenceNumber);

        // Act & Assert
        StepVerifier.create(renewOperatorSource)
            .expectNextCount(0)
            .thenRequest(1)
            .then(() -> {
                messagesPublisher.next(message, message2, message3);
            })
            .assertNext(actual -> assertEquals(message2.getEnqueuedSequenceNumber(), actual))
            .thenRequest(1)
            .assertNext(actual -> assertEquals(message3.getEnqueuedSequenceNumber(), actual))
            .thenCancel()
            .verify();
    }

    /***
     * Ensure user can control backpreassure with simple map function.
     */
    @Test
    public void simpleMappingBackpressured() {
        // Arrange
        final ServiceBusReceivedMessage message2 = new ServiceBusReceivedMessage("data".getBytes());
        message2.setLockToken(UUID.randomUUID());
        message2.setLockedUntil(OffsetDateTime.now().plusSeconds(2));

        final ServiceBusReceivedMessage message3 = new ServiceBusReceivedMessage("data".getBytes());
        message3.setLockToken(UUID.randomUUID());
        message3.setLockedUntil(OffsetDateTime.now().plusSeconds(2));


        final String expectedMappedValue = "New Expected Mapped Value";
        final ServiceBusMessageRenewOperator renewOperator = new ServiceBusMessageRenewOperator(messageSource,
            MAX_AUTO_LOCK_RENEW_DURATION, messageLockContainer, renewalFunction);

        final Flux<String> renewOperatorSource = renewOperator.map(serviceBusReceivedMessage -> expectedMappedValue);

        // Act & Assert
        StepVerifier.create(renewOperatorSource)
            .expectNextCount(0)
            .thenRequest(1)
            .then(() -> {
                messagesPublisher.next(message, message2, message3);
            })
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
        final Long expectedEnqueuedSequenceNumber = 2L;
        final ServiceBusReceivedMessage message2 = new ServiceBusReceivedMessage("data".getBytes());
        message2.setEnqueuedSequenceNumber(1);

        final ServiceBusReceivedMessage message3 = new ServiceBusReceivedMessage("data".getBytes());
        message2.setEnqueuedSequenceNumber(expectedEnqueuedSequenceNumber);

        final ServiceBusMessageRenewOperator renewOperator = new ServiceBusMessageRenewOperator(messageSource,
            MAX_AUTO_LOCK_RENEW_DURATION, messageLockContainer, renewalFunction);

        // Act & Assert
        StepVerifier.create(renewOperator
            .filter(actual -> actual.getEnqueuedSequenceNumber() > 1)
            .map(ServiceBusReceivedMessage::getEnqueuedSequenceNumber))
            .thenRequest(1)
            .then(() -> {
                messagesPublisher.next(message, message2, message3);
            })
            .assertNext(actualEnqueuedSequenceNumber -> assertEquals(expectedEnqueuedSequenceNumber, actualEnqueuedSequenceNumber))
            .thenCancel()
            .verify();
    }
}
