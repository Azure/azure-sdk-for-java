// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Unit test for {@link SynchronousReceiveWork}.
 */
public class SynchronousReceiveWorkTest {
    private final Sinks.Many<ServiceBusReceivedMessage> messageSink = Sinks.many().multicast().directAllOrNothing();

    @AfterEach
    public void teardown() {
        Mockito.framework().clearInlineMock(this);
    }

    @Test
    public void constructor() {
        // Arrange
        final long id = 12;
        final int numberToReceive = 3;
        final Duration timeout = Duration.ofSeconds(5);

        // Act
        final SynchronousReceiveWork work = new SynchronousReceiveWork(id, numberToReceive, timeout, messageSink);

        // Assert
        assertEquals(id, work.getId());
        assertEquals(numberToReceive, work.getNumberOfEvents());
        assertEquals(numberToReceive, work.getRemainingEvents());
        assertEquals(timeout, work.getTimeout());
    }

    /**
     * Ensure that work completes when it emits the same number of items as expected.
     */
    @Test
    public void emitsWorkItems() {
        // Arrange
        int numberToReceive = 3;

        final long id = 12;
        final Duration timeout = Duration.ofMinutes(5);

        final ServiceBusReceivedMessage message1 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message3 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message4 = mock(ServiceBusReceivedMessage.class);

        final SynchronousReceiveWork work = new SynchronousReceiveWork(id, numberToReceive, timeout, messageSink);

        // Act
        assertTrue(work.emitNext(message1), "Should have returned true for message1.");
        assertEquals(--numberToReceive, work.getRemainingEvents());

        assertTrue(work.emitNext(message2), "Should have returned true for message2.");
        assertEquals(--numberToReceive, work.getRemainingEvents());

        assertTrue(work.emitNext(message3), "Should have returned true for message3.");
        assertEquals(--numberToReceive, work.getRemainingEvents());

        assertTrue(work.isTerminal());

        assertFalse(work.emitNext(message4), "Should not emit message after it has received all items.");
    }

    /**
     * Ensure that completes when an item is emitted and then completes when timeout has elapsed.
     */
    @Test
    public void emitsWorkItemsThenTimeout() {
        // Arrange
        int numberToReceive = 2;

        final long id = 12;
        final Duration timeout = Duration.ofSeconds(5);

        final ServiceBusReceivedMessage message1 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);

        final SynchronousReceiveWork work = new SynchronousReceiveWork(id, numberToReceive, timeout, messageSink);

        // Act
        StepVerifier.create(messageSink.asFlux())
            .then(() -> {
                assertTrue(work.emitNext(message1), "Could not emit the first message.");
            })
            .expectNext(message1)
            .verifyComplete();

        assertEquals(1, work.getRemainingEvents());
        assertTrue(work.isTerminal());

        assertFalse(work.emitNext(message2), "Should not have been able to emit a message after it is terminal.");
    }

    /**
     * Ensure that completes when a message is emitted and then the 1s timeout between messages elapses.
     */
    @Test
    public void emitsWorkItemsAndEmitsCompleteWhenTimeoutBetweenMessagesElapses() {
        // Arrange
        int numberToReceive = 2;

        final long id = 12;
        final Duration timeout = Duration.ofMinutes(5);
        final Duration verifyTimeout = Duration.ofSeconds(10);

        final ServiceBusReceivedMessage message1 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);

        final SynchronousReceiveWork work = new SynchronousReceiveWork(id, numberToReceive, timeout, messageSink);

        // Act
        StepVerifier.create(messageSink.asFlux())
            .then(() -> {
                assertTrue(work.emitNext(message1), "Could not emit the first message.");
            })
            .expectNext(message1)
            .expectComplete()
            .verify(verifyTimeout);

        assertEquals(1, work.getRemainingEvents());
        assertTrue(work.isTerminal());

        assertFalse(work.emitNext(message2), "Should not have been able to emit a message after it is terminal.");
    }

    /**
     * Verify that an error is published downstream.
     */
    @Test
    public void emitsError() {
        // Arrange
        int numberToReceive = 2;

        final long id = 12;
        final Duration timeout = Duration.ofMinutes(5);

        final ServiceBusReceivedMessage message1 = mock(ServiceBusReceivedMessage.class);
        final Throwable testException = new IllegalArgumentException("Test-exception");
        final SynchronousReceiveWork work = new SynchronousReceiveWork(id, numberToReceive, timeout, messageSink);

        // Act
        StepVerifier.create(messageSink.asFlux())
            .then(() -> assertTrue(work.emitNext(message1), "Could not emit the first message."))
            .expectNext(message1)
            .then(() -> work.complete("Error", testException))
            .expectErrorMatches(error -> error.equals(testException))
            .verify();

        assertEquals(1, work.getRemainingEvents());
        assertTrue(work.isTerminal());

        assertFalse(work.emitNext(message1), "Should not have been able to emit a message after it is terminal.");
    }

    /**
     * Ensure that completes when it is started but it timeout before any work is received.
     */
    @Test
    public void emitsTimeout() {
        // Arrange
        int numberToReceive = 2;

        final long id = 12;
        final Duration timeout = Duration.ofSeconds(2);

        final ServiceBusReceivedMessage message1 = mock(ServiceBusReceivedMessage.class);
        final SynchronousReceiveWork work = new SynchronousReceiveWork(id, numberToReceive, timeout, messageSink);

        // Act
        StepVerifier.create(messageSink.asFlux())
            .then(() -> work.start())
            .thenAwait(timeout)
            .expectComplete()
            .verify();

        assertEquals(numberToReceive, work.getRemainingEvents());
        assertTrue(work.isTerminal());

        assertFalse(work.emitNext(message1), "Should not have been able to emit a message after it is terminal.");
    }
}
