// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.implementation.LockContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link ServiceBusMessageRenewOperator}.
 */
public class ServiceBusMessageRenewOperatorTest {

    private static final UUID LOCK_TOKEN_UUID = UUID.randomUUID();
    private static final String LOCK_TOKEN = LOCK_TOKEN_UUID.toString();
    private static final Duration maxAutoLockRenewDuration = Duration.ofSeconds(10);

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
    }

    @AfterEach
    void teardown() {
        Mockito.framework().clearInlineMocks();
    }

    /**
     * Test that the function to renew lock is invoked.
     */
    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void lockRenewed(boolean autoLockRenewal){
        // Arrange
        final int atLeast = 1;
        final int processingTimeSeconds = 3;
        final OffsetDateTime lockedUntil = OffsetDateTime.now().plusSeconds(3);
        final OffsetDateTime renewLockedUntil = lockedUntil.plusSeconds(3);
        final ServiceBusReceivedMessage message = new ServiceBusReceivedMessage("data".getBytes());
        message.setLockToken(LOCK_TOKEN_UUID);
        message.setLockedUntil(lockedUntil);

        when(renewalFunction.apply(LOCK_TOKEN))
            .thenReturn(Mono.fromCallable(() -> renewLockedUntil));

        final Flux<? extends ServiceBusReceivedMessage> messageSource = Flux.fromArray(new ServiceBusReceivedMessage[]{message});

        ServiceBusMessageRenewOperator renewOperator = new ServiceBusMessageRenewOperator(messageSource,
            autoLockRenewal, maxAutoLockRenewDuration, messageLockContainer, renewalFunction);

        // Act & Assert

        StepVerifier.create(renewOperator.take(1))
            .expectNextMatches(actual -> {
                try {
                    // Assuming the processing time for the message
                    TimeUnit.SECONDS.sleep(processingTimeSeconds);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return actual.getLockToken().equalsIgnoreCase(message.getLockToken());
            })
            .verifyComplete();

        if (autoLockRenewal) {
            verify(messageLockContainer).addOrUpdate(eq(LOCK_TOKEN), any(OffsetDateTime.class), any(LockRenewalOperation.class));
            verify(renewalFunction, Mockito.atLeast(atLeast)).apply(eq(LOCK_TOKEN));
            verify(renewalFunction, Mockito.atLeast(atLeast)).apply(LOCK_TOKEN);
        } else {
            verify(messageLockContainer, never()).addOrUpdate(eq(LOCK_TOKEN), any(OffsetDateTime.class), any(LockRenewalOperation.class));
            verify(renewalFunction, never()).apply(eq(LOCK_TOKEN));
            verify(renewalFunction, never()).apply(LOCK_TOKEN);
        }
    }

    /**
     * Test when user that the function to renew lock is invoked.
     */
    @Test
    void messageProcessingThrowException() throws InterruptedException {

        // Arrange
        final boolean autoLockRenewal = true;
        final OffsetDateTime lockedUntil = OffsetDateTime.now().plusSeconds(3);
        final OffsetDateTime renewLockedUntil = lockedUntil.plusSeconds(3);
        final int waitForSubscriberSeconds = 5;
        final AtomicBoolean onErrorCalled = new AtomicBoolean(false);
        final AtomicBoolean onCompleteCalled = new AtomicBoolean(false);
        final ServiceBusReceivedMessage message = new ServiceBusReceivedMessage("data".getBytes());
        message.setLockToken(LOCK_TOKEN_UUID);
        message.setLockedUntil(lockedUntil);

        when(renewalFunction.apply(LOCK_TOKEN))
            .thenReturn(Mono.fromCallable(() -> renewLockedUntil));

        final Flux<? extends ServiceBusReceivedMessage> messageSource = Flux.fromArray(new ServiceBusReceivedMessage[]{message, message});

        ServiceBusMessageRenewOperator renewOperator = new ServiceBusMessageRenewOperator(messageSource,
            autoLockRenewal, maxAutoLockRenewDuration, messageLockContainer, renewalFunction);

        // Act
        Disposable disposable = renewOperator
            .subscribe(serviceBusReceivedMessage -> {
                throw new RuntimeException("fake user generated exception.");
                },
                throwable -> {
                onErrorCalled.set(true);
                },
                () -> {
                onCompleteCalled.set(true);
            });
        TimeUnit.SECONDS.sleep(waitForSubscriberSeconds);

        // Assert
        Assertions.assertTrue(onErrorCalled.get());
        Assertions.assertFalse(onCompleteCalled.get());

        disposable.dispose();
    }

    /**
     * Check that null values are not allowed in constructor.
     */
    @Test
    void nullValueConstructor() {
        // Arrange
        final OffsetDateTime lockedUntil = OffsetDateTime.now().plusSeconds(3);
        final OffsetDateTime renewLockedUntil = lockedUntil.plusSeconds(3);

        final ServiceBusReceivedMessage message = new ServiceBusReceivedMessage("data".getBytes());
        message.setLockToken(LOCK_TOKEN_UUID);
        message.setLockedUntil(lockedUntil);
        when(renewalFunction.apply(LOCK_TOKEN))
            .thenReturn(Mono.fromCallable(() -> renewLockedUntil));

        final Flux<? extends ServiceBusReceivedMessage> messageSource = Flux.fromArray(new ServiceBusReceivedMessage[]{message});

        // Act & Assert
        assertThrows(NullPointerException.class, () -> new ServiceBusMessageRenewOperator(null,
            false, maxAutoLockRenewDuration, messageLockContainer, renewalFunction));

        assertThrows(NullPointerException.class, () -> new ServiceBusMessageRenewOperator(messageSource,
            false, maxAutoLockRenewDuration, null, renewalFunction));

        assertThrows(NullPointerException.class, () -> new ServiceBusMessageRenewOperator(messageSource,
            false, maxAutoLockRenewDuration, messageLockContainer, null));

    }
}
