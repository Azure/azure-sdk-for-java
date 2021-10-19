// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link FluxAutoLockRenew}.
 */
public class FluxAutoLockRenewTest {

    private static final UUID LOCK_TOKEN_UUID = UUID.randomUUID();
    private static final String LOCK_TOKEN_STRING = LOCK_TOKEN_UUID.toString();
    private static final Duration MAX_AUTO_LOCK_RENEW_DURATION = Duration.ofSeconds(6);
    private static final Duration DISABLE_AUTO_LOCK_RENEW_DURATION = Duration.ofSeconds(0);

    private final ClientLogger logger = new ClientLogger(FluxAutoLockRenewTest.class);

    private final ServiceBusReceivedMessage receivedMessage = new ServiceBusReceivedMessage(BinaryData.fromString("Some Data"));
    private final ServiceBusMessageContext message = new ServiceBusMessageContext(receivedMessage);
    private final TestPublisher<ServiceBusMessageContext> messagesPublisher = TestPublisher.create();
    private final Flux<? extends ServiceBusMessageContext> messageSource = messagesPublisher.flux();

    private Function<String, Mono<OffsetDateTime>> renewalFunction;

    private OffsetDateTime lockedUntil;
    private ReceiverOptions defaultReceiverOptions;

    @Captor
    private ArgumentCaptor<String> lockTokenCaptor;
    @Captor
    private ArgumentCaptor<OffsetDateTime> lockedUntilCapture;
    @Captor
    private ArgumentCaptor<LockRenewalOperation> lockRenewalOperationCapture;
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
        receivedMessage.setLockToken(LOCK_TOKEN_UUID);
        receivedMessage.setLockedUntil(lockedUntil);
        renewalFunction = (lockToken) -> Mono.just(OffsetDateTime.now().plusSeconds(10));
        defaultReceiverOptions = new ReceiverOptions(ServiceBusReceiveMode.RECEIVE_AND_DELETE, 1,
            MAX_AUTO_LOCK_RENEW_DURATION, true);

    }

    @AfterEach
    void teardown() {
        Mockito.framework().clearInlineMock(this);
    }

    /**
     * Test that the user can cancel the receive function.
     */
    @Test
    void canCancel() {
        // Arrange
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

        verify(messageLockContainer, times(1)).addOrUpdate(lockTokenCaptor.capture(), lockedUntilCapture.capture(), lockRenewalOperationCapture.capture());
        LockRenewalOperation actualLockRenewalOperation =  lockRenewalOperationCapture.getValue();
        String actualLockToken = lockTokenCaptor.getValue();

        assertEquals(LockRenewalStatus.CANCELLED, actualLockRenewalOperation.getStatus());
        assertEquals(LOCK_TOKEN_STRING, actualLockToken);

    }

    /**
     * Check that illegal values are not allowed in constructor.
     */
    @Test
    void illegalValueConstructor() {
        // Arrange, Act & Assert
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
                    logger.warning("Exception while wait. ", e);
                }
                Assertions.assertNotNull(actual);
                Assertions.assertEquals(LOCK_TOKEN_STRING, actual.getMessage().getLockToken());
                Assertions.assertTrue(actual.getMessage().getLockedUntil().isAfter(previousLockedUntil));
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
        final FluxAutoLockRenew renewOperator = new FluxAutoLockRenew(messageSource,
            defaultReceiverOptions, messageLockContainer, renewalFunction);

        when(messageLockContainer.addOrUpdate(eq(LOCK_TOKEN_STRING), any(OffsetDateTime.class), any(LockRenewalOperation.class)))
            .thenThrow(new RuntimeException("contained closed."));

        // Act & Assert
        StepVerifier.create(renewOperator.take(1))
            .then(() -> messagesPublisher.next(message))
            .assertNext(actual -> Assertions.assertEquals(LOCK_TOKEN_STRING, actual.getMessage().getLockToken()))
            .thenCancel()
            .verify();

        verify(messageLockContainer, times(1)).addOrUpdate(eq(LOCK_TOKEN_STRING), any(OffsetDateTime.class), any(LockRenewalOperation.class));

    }

    /**
     * Test if we have ServiceBusReceivedMessageContex with null ServiceBusReceivedMessage, it will never not try to renew lock.
     */
    @Test
    void messageWithError() {
        // Arrange
        final String expectedSessionId = "1";
        final FluxAutoLockRenew renewOperator = new FluxAutoLockRenew(messageSource,
            defaultReceiverOptions, messageLockContainer, renewalFunction);
        final ServiceBusMessageContext errorContext =  new ServiceBusMessageContext(expectedSessionId, new RuntimeException("fake error"));

        when(messageLockContainer.addOrUpdate(eq(LOCK_TOKEN_STRING), any(OffsetDateTime.class), any(LockRenewalOperation.class)))
            .thenThrow(new RuntimeException("contained closed."));

        // Act & Assert
        StepVerifier.create(renewOperator.take(1))
            .then(() -> messagesPublisher.next(errorContext))
            .assertNext(actual -> Assertions.assertEquals(expectedSessionId, actual.getSessionId()))
            .thenCancel()
            .verify();

        verify(messageLockContainer, never()).addOrUpdate(anyString(), any(OffsetDateTime.class), any(LockRenewalOperation.class));

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
        Assertions.assertFalse(onCompleteCalled.get());

        disposable.dispose();
    }

    @Test
    public void mapperReturnNullValue() {
        // Arrange
        final String expectedMappedValue = null;
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

        verify(messageLockContainer, times(2)).addOrUpdate(eq(LOCK_TOKEN_STRING), any(OffsetDateTime.class), any(LockRenewalOperation.class));
    }

    /***
     * Ensure user can control backpreassure with simple filter function.
     */
    @Test
    public void simpleFilterAndBackpressured() {
        // Arrange
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
        final FluxAutoLockRenew renewOperator = new FluxAutoLockRenew(messageSource,
            defaultReceiverOptions, messageLockContainer, renewalFunction);

        // Act & Assert
        StepVerifier.create(renewOperator
            .subscriberContext((context) -> context.put("A", "B")))
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
        final boolean enableAutoComplete = false;
        final int totalProcessingTimeSeconds = 2;
        final int renewedForAtLeast = 3;
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
                    logger.warning("Exception while wait. ", e);
                }
                Assertions.assertNotNull(actual);
                Assertions.assertEquals(LOCK_TOKEN_STRING, actual.getMessage().getLockToken());
                Assertions.assertTrue(actual.getMessage().getLockedUntil().isAfter(previousLockedUntil));
            })
            .verifyComplete();

        verify(messageLockContainer, times(1)).addOrUpdate(eq(LOCK_TOKEN_STRING), any(OffsetDateTime.class), any(LockRenewalOperation.class));
        assertTrue(actualTokenRenewCalledTimes.get() >= renewedForAtLeast);

        // ensure that we do not remove lockToken from 'messageLockContainer' because user can do it at their will since
        // enableAutoComplete = false
        verify(messageLockContainer, never()).remove(LOCK_TOKEN_STRING);
    }
}
