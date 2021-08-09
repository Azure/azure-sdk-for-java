// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Represents a renewal session or message lock renewal operation that.
 */
class LockRenewalOperationTest {
    private static final String A_LOCK_TOKEN = "a-lock-token";

    private final ClientLogger logger = new ClientLogger(LockRenewalOperationTest.class);
    private LockRenewalOperation operation;

    @Mock
    private Function<String, Mono<OffsetDateTime>> renewalOperation;

    @BeforeEach
    void beforeEach() {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    void afterEach() {
        if (operation != null) {
            operation.close();
        }
    }

    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void constructor(boolean isSession) {
        // Arrange
        final Duration renewalPeriod = Duration.ofSeconds(4);
        final OffsetDateTime lockedUntil = OffsetDateTime.now().plus(renewalPeriod);
        final Duration maxDuration = Duration.ofSeconds(20);
        when(renewalOperation.apply(A_LOCK_TOKEN))
            .thenReturn(Mono.fromCallable(() -> OffsetDateTime.now().plus(renewalPeriod)));

        // Act
        operation = new LockRenewalOperation(A_LOCK_TOKEN, maxDuration, isSession, renewalOperation, lockedUntil);

        // Assert
        if (isSession) {
            assertEquals(A_LOCK_TOKEN, operation.getSessionId());
            assertNull(operation.getLockToken());
        } else {
            assertEquals(A_LOCK_TOKEN, operation.getLockToken());
            assertNull(operation.getSessionId());
        }

        assertEquals(lockedUntil, operation.getLockedUntil());
        assertEquals(LockRenewalStatus.RUNNING, operation.getStatus());
        assertNull(operation.getThrowable());
    }

    /**
     * Verify that when an error occurs, it is displayed.
     */
    @Test
    void errors() {
        // Arrange
        final boolean isSession = true;
        final Duration renewalPeriod = Duration.ofSeconds(2);
        final OffsetDateTime lockedUntil = OffsetDateTime.now().plus(renewalPeriod);
        final Duration maxDuration = Duration.ofSeconds(6);
        final Duration totalSleepPeriod = renewalPeriod.plus(renewalPeriod).plusMillis(500);
        final Throwable testError = new IllegalAccessException("A test error");
        final AtomicReference<OffsetDateTime> lastLockedUntil = new AtomicReference<>();

        final Deque<Mono<OffsetDateTime>> responses = new ArrayDeque<>();
        responses.add(Mono.fromCallable(() -> {
            final OffsetDateTime plus = OffsetDateTime.now().plus(renewalPeriod);
            lastLockedUntil.set(plus);
            return plus;
        }));
        responses.add(Mono.error(testError));
        responses.add(Mono.fromCallable(() -> {
            fail("Should not have been called.");
            return OffsetDateTime.now();
        }));

        when(renewalOperation.apply(A_LOCK_TOKEN)).thenAnswer(invocation -> {
            final Mono<OffsetDateTime> instantMono = responses.pollFirst();
            return instantMono != null
                ? instantMono
                : Mono.error(new IllegalStateException("Should have fetched an item."));
        });

        operation = new LockRenewalOperation(A_LOCK_TOKEN, maxDuration, isSession, renewalOperation, lockedUntil);

        // Act
        StepVerifier.create(operation.getCompletionOperation())
            .thenAwait(totalSleepPeriod)
            .expectErrorMatches(e -> e instanceof IllegalAccessException
                && e.getMessage().equals(testError.getMessage()))
            .verify();

        // Assert
        assertEquals(LockRenewalStatus.FAILED, operation.getStatus());
        assertEquals(testError, operation.getThrowable());
        assertEquals(lastLockedUntil.get(), operation.getLockedUntil());
    }

    /**
     * Verifies that it stops renewing after the duration has elapsed.
     */
    @Test
    void completes() {
        // Arrange
        final Duration maxDuration = Duration.ofSeconds(8);
        final Duration renewalPeriod = Duration.ofSeconds(3);

        // At most 4 times because we renew the lock before it expires (by some seconds).
        final int atMost = 4;
        final OffsetDateTime lockedUntil = OffsetDateTime.now().plus(renewalPeriod);
        final Duration totalSleepPeriod = maxDuration.plusMillis(500);

        when(renewalOperation.apply(A_LOCK_TOKEN))
            .thenReturn(Mono.fromCallable(() -> OffsetDateTime.now().plus(renewalPeriod)));

        operation = new LockRenewalOperation(A_LOCK_TOKEN, maxDuration, false, renewalOperation, lockedUntil);

        // Act
        StepVerifier.create(operation.getCompletionOperation())
            .thenAwait(totalSleepPeriod)
            .then(() -> logger.info("Finished renewals for first sleep."))
            .expectComplete()
            .verify(Duration.ofMillis(2000));

        // Assert
        assertEquals(LockRenewalStatus.COMPLETE, operation.getStatus());
        assertNull(operation.getThrowable());
        assertTrue(lockedUntil.isBefore(operation.getLockedUntil()), String.format(
            "initial lockedUntil[%s] is not before lockedUntil[%s]", lockedUntil, operation.getLockedUntil()));

        verify(renewalOperation, atMost(atMost)).apply(A_LOCK_TOKEN);
    }

    /**
     * Verify that we can cancel the operation.
     */
    @Test
    void cancellation() {
        // Arrange
        final Duration maxDuration = Duration.ofSeconds(20);
        final Duration renewalPeriod = Duration.ofSeconds(3);

        // At most 3 times because we renew the lock before it expires (by some seconds).
        final int atMost = 2;
        final OffsetDateTime lockedUntil = OffsetDateTime.now().plus(renewalPeriod);
        final Duration totalSleepPeriod = renewalPeriod.plusSeconds(1);

        when(renewalOperation.apply(A_LOCK_TOKEN))
            .thenReturn(Mono.fromCallable(() -> OffsetDateTime.now().plus(renewalPeriod)));

        operation = new LockRenewalOperation(A_LOCK_TOKEN, maxDuration, false, renewalOperation, lockedUntil);

        // Act
        StepVerifier.create(operation.getCompletionOperation())
            .thenAwait(totalSleepPeriod)
            .then(() -> {
                logger.info("Finished renewals for first sleep. Cancelling.");
                operation.close();
            })
            .expectComplete()
            .verify(renewalPeriod);

        // Assert
        assertEquals(LockRenewalStatus.CANCELLED, operation.getStatus());
        assertNull(operation.getThrowable());
        assertTrue(lockedUntil.isBefore(operation.getLockedUntil()), String.format(
            "initial lockedUntil[%s] is not before lockedUntil[%s]", lockedUntil, operation.getLockedUntil()));

        verify(renewalOperation, atMost(atMost)).apply(A_LOCK_TOKEN);
    }

    /**
     * Verify that when a duration of Duration.ZERO is passed, then we do not renew at all.
     */
    @Test
    void renewDurationZero() throws InterruptedException {
        // Arrange
        final Duration maxDuration = Duration.ZERO;
        final Duration renewalPeriod = Duration.ofSeconds(3);
        final OffsetDateTime lockedUntil = OffsetDateTime.now().plus(renewalPeriod);

        when(renewalOperation.apply(A_LOCK_TOKEN))
            .thenReturn(Mono.fromCallable(() -> OffsetDateTime.now().plus(renewalPeriod)));

        operation = new LockRenewalOperation(A_LOCK_TOKEN, maxDuration, false, renewalOperation, lockedUntil);

        // Act
        Thread.sleep(renewalPeriod.toMillis());

        // Assert
        assertEquals(LockRenewalStatus.COMPLETE, operation.getStatus());
        assertNull(operation.getThrowable());
        assertEquals(lockedUntil, operation.getLockedUntil(), String.format(
            "initial lockedUntil[%s] is not equal to lockedUntil[%s]", lockedUntil, operation.getLockedUntil()));

        verify(renewalOperation, never()).apply(A_LOCK_TOKEN);
    }

    /**
     * Verifies that if we do not pass in the lockedUntil parameter, it immediately tries to renew the lock.
     */
    @Test
    void completesRenewFirst() throws InterruptedException {
        // Arrange
        final Duration maxDuration = Duration.ofSeconds(8);
        final Duration renewalPeriod = Duration.ofSeconds(3);

        // At most 4 times because we renew the lock before it expires (by some seconds).
        final int atLeast = 4;
        final OffsetDateTime lockedUntil = OffsetDateTime.now();
        final Duration totalSleepPeriod = maxDuration.plusMillis(500);

        when(renewalOperation.apply(A_LOCK_TOKEN))
            .thenReturn(Mono.fromCallable(() -> OffsetDateTime.now().plus(renewalPeriod)));

        operation = new LockRenewalOperation(A_LOCK_TOKEN, maxDuration, false, renewalOperation);

        // Act
        Thread.sleep(totalSleepPeriod.toMillis());
        logger.info("Finished renewals for first sleep.");
        Thread.sleep(2000);
        logger.info("Finished second sleep. Should not have any more renewals.");

        // Assert
        assertEquals(LockRenewalStatus.COMPLETE, operation.getStatus());
        assertNull(operation.getThrowable());
        assertTrue(lockedUntil.isBefore(operation.getLockedUntil()), String.format(
            "initial lockedUntil[%s] is not before lockedUntil[%s]", lockedUntil, operation.getLockedUntil()));

        verify(renewalOperation, Mockito.atLeast(atLeast)).apply(A_LOCK_TOKEN);
    }
}
