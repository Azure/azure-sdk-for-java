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
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

    private final Duration operationTimeout = Duration.ofSeconds(10);

    @Mock
    private ServiceBusReceiverAsyncClient asyncClient;
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

        syncSubscriber = new SynchronousMessageSubscriber(asyncClient, work1, false, operationTimeout);
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
        syncSubscriber = new SynchronousMessageSubscriber(asyncClient, work1, false, operationTimeout);

        // Act
        syncSubscriber.queueWork(work2);

        // Assert
        assertEquals(2, syncSubscriber.getWorkQueueSize());
    }

    /**
     * Verifies that this processes multiple work items and current work encounter timeout
     */
    @Test
    public void processesMultipleWorkItemsAndCurrentWorkTimeout() {
        // Arrange
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

        // WORK 2 is update to current work after the work1 is terminal and successfully emits message3
        when(work2.emitNext(message3)).thenReturn(true);
        when(work2.isTerminal()).thenReturn(false);

        // WORK 3 is placed in queue
        final SynchronousReceiveWork work3 = mock(SynchronousReceiveWork.class);
        when(work3.getId()).thenReturn(3L);
        when(work3.getNumberOfEvents()).thenReturn(1);

        syncSubscriber = new SynchronousMessageSubscriber(asyncClient, work1, false, operationTimeout);
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
        assertEquals(NUMBER_OF_WORK_ITEMS, allRequests.get(0));

        final long requestedAfterWork1 = NUMBER_OF_WORK_ITEMS - remaining.get();
        final long expectedDifference = work2.getNumberOfEvents() - requestedAfterWork1;
        assertEquals(expectedDifference, allRequests.get(1));
    }

    /**
     * Verifies that this processes multiple work items and current work can emit all messages successfully
     */
    @Test
    public void processesMultipleWorkItemsAndCurrentWorkEmitAllMessages() {
        // Arrange
        final ServiceBusReceivedMessage message1 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message3 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message4 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message5 = mock(ServiceBusReceivedMessage.class);

        // WORK 1 successfully emits all messages and is terminal after emit message4.
        final AtomicBoolean isTerminal = new AtomicBoolean(false);
        final AtomicInteger remaining = new AtomicInteger(NUMBER_OF_WORK_ITEMS);
        doAnswer(invocation -> {
            ServiceBusReceivedMessage arg = invocation.getArgument(0);
            remaining.decrementAndGet();
            if (arg == message4) {
                isTerminal.set(true);
            }
            return true;
        }).when(work1).emitNext(any(ServiceBusReceivedMessage.class));
        doAnswer(invocation -> isTerminal.get()).when(work1).isTerminal();
        doAnswer(invocation -> remaining.get()).when(work1).getRemainingEvents();

        // WORK 2 is updated to current work after work1 completed and successfully emit message5
        when(work2.isTerminal()).thenReturn(false);
        when(work2.emitNext(message5)).thenReturn(true);

        // WORK 3 is placed in queue
        final SynchronousReceiveWork work3 = mock(SynchronousReceiveWork.class);
        when(work3.getId()).thenReturn(3L);
        when(work3.getNumberOfEvents()).thenReturn(1);

        syncSubscriber = new SynchronousMessageSubscriber(asyncClient, work1, false, operationTimeout);
        syncSubscriber.queueWork(work2);
        syncSubscriber.queueWork(work3);

        syncSubscriber.hookOnSubscribe(subscription);

        assertEquals(2, syncSubscriber.getWorkQueueSize());

        // Act
        syncSubscriber.hookOnNext(message1);
        syncSubscriber.hookOnNext(message2);
        syncSubscriber.hookOnNext(message3);
        syncSubscriber.hookOnNext(message4);

        // Assert
        verify(work2).start();

        // work2 emits message5
        syncSubscriber.hookOnNext(message5);

        verify(work2).emitNext(message5);

        assertEquals(1, syncSubscriber.getWorkQueueSize());

        // Verify that we requested:
        // 1st time: hookOnSubscribe(work1.getNumberOfEvents())
        // 2nd time: requestUpstream(work2.getNumberOfEvents()) and REQUESTED = 0
        verify(subscription, times(2)).request(subscriptionArgumentCaptor.capture());
        final List<Long> allRequests = subscriptionArgumentCaptor.getAllValues();
        assertEquals(NUMBER_OF_WORK_ITEMS, allRequests.get(0));
        assertEquals(NUMBER_OF_WORK_ITEMS_2, allRequests.get(1));
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

    @Test
    public void releaseIfNoActiveReceive() {
        // Arrange

        // The work1 happily accept any message.
        when(work1.emitNext(any(ServiceBusReceivedMessage.class))).thenReturn(true);

        // The four messages produced by the link (two before work1 timeout and two after).
        final ServiceBusReceivedMessage message1beforeTimeout = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message2beforeTimeout = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message1afterTimeout = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message2afterTimeout = mock(ServiceBusReceivedMessage.class);

        // work1 enters terminal-state when isTerminal is set (e.g. when something like timeout happens).
        final AtomicBoolean isTerminal = new AtomicBoolean(false);
        doAnswer(invocation -> isTerminal.get()).when(work1).isTerminal();

        // Expect drain loop to invoke release for two messages those were received after timeout of work1
        // and there were no work queued to receive those late messages.
        final AtomicInteger expectedReleaseCalls = new AtomicInteger(0);
        final AtomicBoolean hadUnexpectedReleaseCall = new AtomicBoolean(false);
        doAnswer(invocation -> {
            ServiceBusReceivedMessage arg = invocation.getArgument(0);
            if (arg == message1afterTimeout || arg == message2afterTimeout) {
                expectedReleaseCalls.incrementAndGet();
            } else {
                hadUnexpectedReleaseCall.set(true);
            }
            return Mono.empty();
        }).when(asyncClient).release(any(ServiceBusReceivedMessage.class));

        // The subscriber with prefetch-disabled - indicate any received messages that cannot be emitted should be released.
        syncSubscriber = new SynchronousMessageSubscriber(asyncClient, work1, true, operationTimeout);

        // Act
        syncSubscriber.hookOnSubscribe(subscription);

        // The work1 places a credit of 4; let's say link produced two messages
        syncSubscriber.hookOnNext(message1beforeTimeout);
        syncSubscriber.hookOnNext(message2beforeTimeout);
        // then the work1 timeout (terminated)
        isTerminal.set(true);
        // then the remaining two messages produced by the link
        syncSubscriber.hookOnNext(message1afterTimeout);
        syncSubscriber.hookOnNext(message2afterTimeout);

        // Assert

        // the work1 received first two messages before the timeout
        verify(work1).emitNext(message1beforeTimeout);
        verify(work1).emitNext(message2beforeTimeout);
        // the work1 should never receive two messages arrived later
        verify(work1, never()).emitNext(message1afterTimeout);
        verify(work1, never()).emitNext(message2afterTimeout);
        // rather those late two messages should be released.
        assertEquals(2, expectedReleaseCalls.get());
        assertFalse(hadUnexpectedReleaseCall.get());
    }
}
