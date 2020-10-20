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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link FluxAutoRenew}.
 */
public class FluxAutoRenewTest {

    private static final UUID LOCK_TOKEN_UUID = UUID.randomUUID();
    private static final String LOCK_TOKEN_STRING = LOCK_TOKEN_UUID.toString();
    private static final Duration MAX_AUTO_LOCK_RENEW_DURATION = Duration.ofSeconds(6);
    private static final Duration DISABLE_AUTO_LOCK_RENEW_DURATION = Duration.ofSeconds(0);

    private final ClientLogger logger = new ClientLogger(FluxAutoRenewTest.class);

    private final ServiceBusReceivedMessage message = new ServiceBusReceivedMessage("Some Data".getBytes());
    private final int durationToRenewLockForSeconds = 1;
    private final TestPublisher<ServiceBusReceivedMessage> messagesPublisher = TestPublisher.create();
    private final Flux<? extends ServiceBusReceivedMessage> messageSource = messagesPublisher.flux();
    private Function<String, Mono<OffsetDateTime>> renewalFunction;

    private OffsetDateTime lockedUntil;

    @Mock
    LockContainer<LockRenewalOperation> messageLockContainer;

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
        renewalFunction = (lockToken) -> Mono.just(OffsetDateTime.now().plusSeconds(1));
        /*when(renewalFunction.apply(LOCK_TOKEN_STRING))
            .thenReturn(Mono.fromCallable(() -> message.getLockedUntil().plusSeconds(durationToRenewLockForSeconds)));

         */
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

        final FluxAutoRenew renewOperator = new FluxAutoRenew(messageSource,
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
        final FluxAutoRenew renewOperator = new FluxAutoRenew(messageSource,
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
        assertThrows(NullPointerException.class, () -> new FluxAutoRenew(null,
            MAX_AUTO_LOCK_RENEW_DURATION, messageLockContainer, renewalFunction));

        assertThrows(NullPointerException.class, () -> new FluxAutoRenew(messageSource,
            MAX_AUTO_LOCK_RENEW_DURATION, null, renewalFunction));

        assertThrows(NullPointerException.class, () -> new FluxAutoRenew(messageSource,
            MAX_AUTO_LOCK_RENEW_DURATION, messageLockContainer, null));

        assertThrows(IllegalArgumentException.class, () -> new FluxAutoRenew(messageSource,
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
        final AtomicInteger actualTokenRenewCalledTimes = new AtomicInteger();
        final Function<String, Mono<OffsetDateTime>> lockTokenRenewFunction = (lockToken) -> {
            actualTokenRenewCalledTimes.getAndIncrement();
            return Mono.just(OffsetDateTime.now().plusSeconds(1));
        };

        final FluxAutoRenew renewOperator = new FluxAutoRenew(messageSource,
            MAX_AUTO_LOCK_RENEW_DURATION, messageLockContainer, lockTokenRenewFunction);

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
        assertTrue(actualTokenRenewCalledTimes.get() >= renewedForAtLeast);
    }


    /**
     * Test if we have error in
     */
    @Test
    void lockRenewedError() {
        // Arrange
        final FluxAutoRenew renewOperator = new FluxAutoRenew(messageSource,
            MAX_AUTO_LOCK_RENEW_DURATION, messageLockContainer, renewalFunction);

        when(messageLockContainer.addOrUpdate(eq(LOCK_TOKEN_STRING), any(OffsetDateTime.class), any(LockRenewalOperation.class)))
            .thenThrow(new RuntimeException("contained closed."));

        // Act & Assert
        StepVerifier.create(renewOperator.take(1))
            .then(() -> {
                messagesPublisher.next(message);
            })
            .assertNext(actual -> Assertions.assertEquals(LOCK_TOKEN_STRING, actual.getLockToken()))
            .thenCancel()
            .verify();

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

        final FluxAutoRenew renewOperator = new FluxAutoRenew(messageSource,
            MAX_AUTO_LOCK_RENEW_DURATION, messageLockContainer, renewalFunction);

        // Act
        Disposable disposable = renewOperator
            .subscribe(serviceBusReceivedMessage -> {
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
        final FluxAutoRenew renewOperator = new FluxAutoRenew(messageSource,
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
        final FluxAutoRenew renewOperator = new FluxAutoRenew(messageSource,
            MAX_AUTO_LOCK_RENEW_DURATION, messageLockContainer, renewalFunction);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> renewOperator.map(null));
    }

    /**
     * Test that the function to renew Renew Operator can be subscribed multiple times.
     */
    @Test
    void renewCanBeSubscribedMultipleTimes() {
        // Arrange
        final FluxAutoRenew renewOperator = new FluxAutoRenew(messageSource,
            MAX_AUTO_LOCK_RENEW_DURATION, messageLockContainer, renewalFunction);

        // Act & Assert
        StepVerifier.create(renewOperator.take(1))
            .then(() -> {
                messagesPublisher.next(message);
            })
            .assertNext(actual -> {
                Assertions.assertEquals(LOCK_TOKEN_STRING, actual.getLockToken());
            })
            .verifyComplete();

        StepVerifier.create(renewOperator.take(1))
            .then(() -> {
                messagesPublisher.next(message);
            })
            .assertNext(actual -> {
                Assertions.assertEquals(LOCK_TOKEN_STRING, actual.getLockToken());

            })
            .verifyComplete();

        verify(messageLockContainer, times(2)).addOrUpdate(eq(LOCK_TOKEN_STRING), any(OffsetDateTime.class), any(LockRenewalOperation.class));
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

        final FluxAutoRenew renewOperator = new FluxAutoRenew(messageSource,
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
        final FluxAutoRenew renewOperator = new FluxAutoRenew(messageSource,
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
        final OffsetDateTime lockedUntil = OffsetDateTime.now().plusSeconds(1);
        final ServiceBusReceivedMessage message2 = new ServiceBusReceivedMessage("data".getBytes());
        message2.setLockToken(UUID.randomUUID());
        message2.setLockedUntil(lockedUntil);
        message2.setEnqueuedSequenceNumber(1);

        final ServiceBusReceivedMessage message3 = new ServiceBusReceivedMessage("data".getBytes());
        message2.setLockToken(UUID.randomUUID());
        message3.setLockedUntil(lockedUntil);
        message2.setEnqueuedSequenceNumber(expectedEnqueuedSequenceNumber);

        final FluxAutoRenew renewOperator = new FluxAutoRenew(messageSource,
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

    @Test
    public void contextPropagationTest() {
        // Arrange
        final FluxAutoRenew renewOperator = new FluxAutoRenew(messageSource,
            MAX_AUTO_LOCK_RENEW_DURATION, messageLockContainer, renewalFunction);

        // Act & Assert
        StepVerifier.create(renewOperator
            .subscriberContext(context -> {
                return context.put("A", "B");
            }))
            .thenRequest(1)
            .expectAccessibleContext()
            .contains("A", "B")
            .hasSize(1)
            .then()
            .then(() -> {
                messagesPublisher.next(message);
            })
            .expectNext(message)
            .thenCancel()
            .verify();
    }
}
