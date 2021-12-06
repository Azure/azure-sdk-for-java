// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.reactivestreams.Subscription;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for sync subscriber.
 */
public class SynchronousMessageSubscriberTest {
    private static final long WORK_ID = 10L;
    private static final long WORK_ID_2 = 10L;

    private static final int NUMBER_OF_WORK_ITEMS = 4;
    private static final int NUMBER_OF_WORK_ITEMS_2 = 3;

    @Mock
    private SynchronousReceiveWork work1;
    @Mock
    private SynchronousReceiveWork work2;

    @Mock
    private Subscription subscription;

    @Captor
    private ArgumentCaptor<Long> subscriptionArgumentCaptor;

    private SynchronousMessageSubscriber syncSubscriber;
    private AutoCloseable mocksCloseable;

    @BeforeEach
    public void setup() {
        mocksCloseable = MockitoAnnotations.openMocks(this);

        when(work1.getId()).thenReturn(WORK_ID);
        when(work1.getNumberOfEvents()).thenReturn(NUMBER_OF_WORK_ITEMS);

        when(work2.getId()).thenReturn(WORK_ID_2);
        when(work2.getNumberOfEvents()).thenReturn(NUMBER_OF_WORK_ITEMS_2);

        syncSubscriber = new SynchronousMessageSubscriber(work1);
    }

    @AfterEach
    public void teardown() throws Exception {
        if (mocksCloseable != null) {
            mocksCloseable.close();
        }

        syncSubscriber.dispose();

        Mockito.framework().clearInlineMock(this);
    }

    /**
     * Verify that the initial subscription requests work1's number of work items.
     */
    @Test
    public void workAddedAndRequestedUpstream() {
        // Arrange
        // There haven't been any events published yet, so return the same value.
        when(work1.getRemainingEvents()).thenReturn(NUMBER_OF_WORK_ITEMS);

        // Act
        syncSubscriber.hookOnSubscribe(subscription);

        // Assert
        verify(subscription).request(work1.getNumberOfEvents());
        verify(work1).start();

        // The current work has been polled, so this should be empty.
        assertEquals(0, syncSubscriber.getWorkQueueSize());
    }

    /**
     * A work gets queued in work queue.
     */
    @Test
    public void queueWorkTest() {
        // Arrange
        syncSubscriber = new SynchronousMessageSubscriber(work1);

        // Act
        syncSubscriber.queueWork(work2);

        // Assert
        assertEquals(2, syncSubscriber.getWorkQueueSize());
    }

    /**
     * Verifies that this processes multiple work items.
     */
    @Test
    public void processesMultipleWorkItems() {
        // Arrange
        final SynchronousReceiveWork work3 = mock(SynchronousReceiveWork.class);
        when(work3.getId()).thenReturn(3L);
        when(work3.getNumberOfEvents()).thenReturn(1);

        final ServiceBusReceivedMessage message1 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message3 = mock(ServiceBusReceivedMessage.class);

        // WORK 1
        // Successfully emits the first 2 messages then something like a timeout happens, return false for message3.
        final AtomicBoolean isTerminal = new AtomicBoolean(false);
        final AtomicInteger remaining = new AtomicInteger(NUMBER_OF_WORK_ITEMS);
        doAnswer(invocation -> {
            ServiceBusReceivedMessage arg = invocation.getArgument(0);
            if (arg == message1 || arg == message2) {
                remaining.decrementAndGet();
                return true;
            } else {
                return false;
            }
        }).when(work1).emitNext(any(ServiceBusReceivedMessage.class));

        doAnswer(invocation -> isTerminal.get()).when(work1).isTerminal();
        doAnswer(invocation -> remaining.get()).when(work1).getRemainingEvents();

        when(work2.emitNext(message3)).thenReturn(true);
        when(work2.isTerminal()).thenReturn(false);

        syncSubscriber = new SynchronousMessageSubscriber(work1);
        syncSubscriber.queueWork(work2);
        syncSubscriber.queueWork(work3);

        // Subscribe "upstream"
        syncSubscriber.hookOnSubscribe(subscription);

        // There should be 1 currentWork and 2 pending work items in queue.
        assertEquals(2, syncSubscriber.getWorkQueueSize());

        // Act
        syncSubscriber.hookOnNext(message1);
        syncSubscriber.hookOnNext(message2);

        // First work item is terminal after the first 2 messages.
        isTerminal.set(true);
        syncSubscriber.hookOnNext(message3);

        // Assert
        verify(work2).start();
        verify(work2).emitNext(message3);

        // Expect that we updated the currentWork to work2 and there is only 1 pending work left.
        assertEquals(1, syncSubscriber.getWorkQueueSize());

        // Verify that we requested:
        // 1st time: hookOnSubscribe (work1.getNumberOfEvents())
        // 2nd time: requestUpstream(work2.getNumberOfEvents()) which will be:
        //     - work2.getNumberOfEvents() - (REQUESTED - work1.getRemainingItems());
        verify(subscription, times(2)).request(subscriptionArgumentCaptor.capture());
        final List<Long> allRequests = subscriptionArgumentCaptor.getAllValues();
        final Set<Long> expected = new HashSet<>();
        expected.add((long) work1.getNumberOfEvents());

        final long requestedAfterWork1 = NUMBER_OF_WORK_ITEMS - remaining.get();
        final long expectedDifference = work2.getNumberOfEvents() - requestedAfterWork1;
        expected.add(expectedDifference);

        assertEquals(expected.size(), allRequests.size());
        allRequests.forEach(r -> assertTrue(expected.contains(r)));
    }

    /**
     * Verifies that all work items are completed if the subscriber is disposed.
     */
    @Test
    public void completesWorkOnCancel() {
        // Arrange
        when(work1.getRemainingEvents()).thenReturn(NUMBER_OF_WORK_ITEMS);
        when(work2.getRemainingEvents()).thenReturn(NUMBER_OF_WORK_ITEMS_2);

        syncSubscriber.queueWork(work2);

        // Act
        syncSubscriber.hookOnSubscribe(subscription);
        syncSubscriber.hookOnCancel();

        // Assert
        verify(work1).complete(any(String.class), isNull());
        verify(work2).complete(any(String.class), isNull());

        // The current work has been polled, so this should be empty.
        assertEquals(0, syncSubscriber.getWorkQueueSize());
    }

    /**
     * Verifies that all work items are completed if the subscriber encounters an error.
     */
    @Test
    public void completesWorkOnError() {
        // Arrange
        final Throwable error = new AmqpException(false, "Test-error", new AmqpErrorContext("foo.com"));

        when(work1.getRemainingEvents()).thenReturn(NUMBER_OF_WORK_ITEMS);
        when(work2.getRemainingEvents()).thenReturn(NUMBER_OF_WORK_ITEMS_2);

        syncSubscriber.queueWork(work2);

        // Act
        syncSubscriber.hookOnSubscribe(subscription);
        syncSubscriber.hookOnError(error);

        // Assert
        verify(work1).complete(any(String.class), eq(error));
        verify(work2).complete(any(String.class), eq(error));

        // The current work has been polled, so this should be empty.
        assertEquals(0, syncSubscriber.getWorkQueueSize());
    }
}
